package uk.ac.ed.inf;

import java.util.ArrayList;

/**
 * Application access entry point
 */
public class App 
{
    public static void main(String[] args)
    {
        if (args.length != 5) {
            System.err.print("Incorrect number of arguments");
            System.exit(0);
        }

        // parse input arguments
        String day = args[0];
        String month = args[1];
        String year = args[2];
        String webPort = args[3];
        String databasePort = args[4];

        int dayInteger = Integer.parseInt(day);
        int monthInteger = Integer.parseInt(month);

        // Check input day is valid
        if (dayInteger < 1 || dayInteger > 31) {
            System.err.print("Invalid day");
            System.exit(0);
        }
        // Check input month is valid
        if (monthInteger < 1 || monthInteger > 12 ) {
            System.err.print("Invalid month");
            System.exit(0);
        }

        // Set up variables
        String date = month + "/" + day + "/" + year;
        String host = "localhost";

        final Menus menu = new Menus(host,webPort);
        final DatabaseClient database = new DatabaseClient(host, databasePort, menu);
        final Drone drone = new Drone(host, webPort, menu);

        //get all the orders
        ArrayList<Order> orders = database.getOrdersTable(date);

        // execute delivery route
        drone.greedyOrdersInADay(orders);

        // Get path in GeoJSON file
        ClientIO.toGeoJSON(drone.getAllMovesSingle(),date);

        // Create Flightpath Table
        database.setFlightpathTable(drone.getFlightpath());

        // Create Deliveries Table
        database.setDeliveriesTable(drone.getDeliveredOrdersInADay());

    }
}
