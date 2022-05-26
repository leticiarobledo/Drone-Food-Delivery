package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class which enables connection with the database system,
 * including functions to create, read and write tables in the database system.
 */
public class DatabaseClient {

    /**
     * Database name
     */
    private static final String DATABASE_NAME = "derbyDB";

    /**
     * Host machine web server connectivity
     */
    public final String machineName;

    /**
     * web server connectivity port
     */
    public final String port;

    /**
     * Menus menu object
     */
    public Menus menu;


    /**
     * Database client class constructor
     * @param machineName Host machine web server connectivity
     * @param port web server connectivity port
     * @param menu Menus class object
     */
    public DatabaseClient(String machineName, String port, Menus menu) {
        this.machineName = machineName;
        this.port = port;
        this.menu = menu;
    }

    /**
     * Getter method that queries Orders database for the day's orders.
     * Creates a list of all the orders that match that date (@param date)
     * through Order objects.
     *
     * @param date Date with "MM/DD/YYYY" format, used to query the
     *             database for all the orders made in that day (DD)
     * @throws SQLException if database connection is not possible or table cannot be created
     * @return list of Order objects containing all orders made in the day
     */
    public ArrayList<Order> getOrdersTable(String date) {
        // set up endpoint
        String jdbcString = "jdbc:derby://" + machineName + ":"
                + port + "/" + DATABASE_NAME;

        ArrayList<Order> orderList = new ArrayList<>();

        // Table column name for delivery date
        String DELIVERY_DATE = "deliveryDate";

        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            // Orders table name
            String ORDERS = "orders";
            final String dayQuery =
                    "select * from " + ORDERS + " where " + DELIVERY_DATE + "=(?)";
            PreparedStatement psDayQuery =
                    conn.prepareStatement(dayQuery);
            psDayQuery.setString(1, date);

            // Search for the the day's orders and add Order objects to a list
            ResultSet rs = psDayQuery.executeQuery();
            while (rs.next()) {
                String orderNumber = rs.getString("orderNo");
                String customer = rs.getString("customer");
                String deliverTo = rs.getString("deliverTo");
                String[] items = getOrderDetailsTable(orderNumber).get(orderNumber).toArray(new String[0]);
                Order newOrder = new Order(orderNumber, customer, deliverTo, items, menu);
                orderList.add(newOrder);
            }

        } catch (java.sql.SQLException throwables) {
            throwables.printStackTrace();
        }

        return orderList;
    }

    /**
     * Getter method that queries orderDetails table to get all the items
     * associated with the student's order.
     *
     * @param orderNo student's unique order identifier
     * @throws SQLException if connection with database cannot be established or table cannot be created
     * @return HashMap mapping the order number to all
     *          the menu items associated with it
     */
    public HashMap<String, ArrayList<String>> getOrderDetailsTable(String orderNo) {
        String jdbcString = "jdbc:derby://" + machineName + ":"
                + port + "/" + DATABASE_NAME;
        HashMap<String, ArrayList<String>> orderNumberToItems = null;
        // Table column name for order number
        String ORDER_NUMBER = "orderNo";
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            // orderDetails table name
            String ORDER_DETAILS = "orderDetails";
            final String dayQuery =
                    "select * from " + ORDER_DETAILS + " where " + ORDER_NUMBER + "=(?)";
            PreparedStatement psDayQuery =
                    conn.prepareStatement(dayQuery);
            psDayQuery.setString(1, orderNo);

            // Search for the the day's orders and map order items to each order
            orderNumberToItems = new HashMap<>();
            ResultSet rs = psDayQuery.executeQuery();
            while (rs.next()) {
                String item = rs.getString("item");
                // if order has at least 1 item, add to the list
                if (!orderNumberToItems.containsKey(orderNo)) {
                    orderNumberToItems.put(orderNo, new ArrayList<>());
                }
                orderNumberToItems.get(orderNo).add(item);
            }

        } catch (java.sql.SQLException throwables) {
            throwables.printStackTrace();
        }
        return orderNumberToItems;
    }


    /**
     * Method which creates and fills in a DELIVERIES table in the dataset,
     * including information about the order number, the delivery location and
     * the cost in pence of the order
     *
     * @param orders delivered orders
     * @throws SQLException if database connection cannot be
     *          established or database table filled in
     */
    public void setDeliveriesTable(ArrayList<Order> orders) {
        String jdbcString = "jdbc:derby://" + machineName + ":"
                + port + "/" + DATABASE_NAME;
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            // Create a statement object that we can use for running various
            // SQL statement commands against the database.
            java.sql.Statement statement = conn.createStatement();
            DatabaseMetaData databaseMetadata = conn.getMetaData();

            // Deliveries table name
            String DELIVERIES = "DELIVERIES";
            ResultSet resultSet =
                    databaseMetadata.getTables(null, null, DELIVERIES, null);

            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet.next()) {
                statement.execute("drop table deliveries");
            }

            statement.execute(
                    "create table deliveries(" +
                            "orderNo char(8), " +
                            "deliveredTo varchar(19), " +
                            "costInPence int)");

            // fills in table
            for (Order order : orders) {
                PreparedStatement psFlight =
                        conn.prepareStatement("insert into deliveries values (?, ?, ?)");

                psFlight.setString(1, order.getOrderNo());
                psFlight.setString(2, order.getDeliverTo());
                psFlight.setInt(3, order.getOrderCost(order));
                psFlight.execute();
            }


        } catch (java.sql.SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Method which creates and fills in the FLIGHTPATH table in the database
     * with all of the drone's movements from the start of the delivery process
     * until the end of moves the drone can make.
     * Includes information about the order number of the order being delivered,
     * each move's starting point's longitude and latitude,
     * the end point's longitude and latitude,
     * and the angle between both points.
     *
     * @param flightpath all of the drone's movements that day.
     * @throws SQLException if database connection cannot be
     *          established or database table filled in
     *
     */
    public void setFlightpathTable(ArrayList<FlightpathMove> flightpath) {
        String jdbcString = "jdbc:derby://" + machineName + ":"
                + port + "/" + DATABASE_NAME;
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            // Create a statement object that we can use for running various
            // SQL statement commands against the database.
            java.sql.Statement statement = conn.createStatement();
            DatabaseMetaData databaseMetadata = conn.getMetaData();

            //Flightpath table name
            String FLIGHTPATH = "FLIGHTPATH";
            ResultSet resultSet =
                    databaseMetadata.getTables(null, null, FLIGHTPATH, null);

            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet.next()) {
                statement.execute("drop table flightpath");
            }

            statement.execute(
                    "create table flightpath(" +
                            "orderNo char(8), " +
                            "fromLongitude double, " +
                            "fromLatitude double, " +
                            "angle integer, " +
                            "toLongitude double, " +
                            "toLatitude double)");

            // fills in table
            for (FlightpathMove move : flightpath) {
                PreparedStatement psFlight =
                        conn.prepareStatement("insert into flightpath values (?, ?, ?, ?, ?, ?)");

                psFlight.setString(1, move.getOrderNo());
                psFlight.setDouble(2, move.getOriginalPosition().longitude);
                psFlight.setDouble(3, move.getOriginalPosition().latitude);
                psFlight.setInt(4, move.getAngle());
                psFlight.setDouble(5, move.getNextPosition().longitude);
                psFlight.setDouble(6, move.getNextPosition().latitude);
                psFlight.execute();
            }

        }  catch (java.sql.SQLException throwables) {
            throwables.printStackTrace();
        }
    }


}
