package uk.ac.ed.inf;

import java.awt.geom.Line2D;
import java.util.*;

public class Drone {

    /**
     * Imposed limit on maximum number of moves before returning to Appleton Tower.
     * Maximum number of moves drone can perform is 1500.
     */
    private int MAX_NUMBER_OF_MOVES = 1500;

    /**
     * Location of starting point and end-point of drone delivery route.
     */
    public final LongLat appletonTower = new LongLat(-3.186874, 55.944494);

    /**
     * All available landmarks to be used throughout the delivery process.
     */
    private ArrayList<LongLat> landmarks;

    /**
     * All polygon perimeter lines to be used
     * throughout the delivery process.
     */
    private List<Line2D> polygonPerimeters;

    /**
     * Boolean variable used to flag viability of order delivery. This viability is measured in terms of
     * the number of moves left to be performed by the drone and the number of moves it would take to go back
     * to the final destination (Appleton Tower)
     */
    private boolean continueIterations = true;

    /**
     * ArrayList containing all delivered orders in a single day
     */
    private ArrayList<Order> deliveredOrdersInADay = new ArrayList<>();

    /**
     * ArrayList containing all flight moves from start of delivery process
     * until arrival back to Appleton Tower.
     */
    private ArrayList<FlightpathMove> flightpath = new ArrayList<>();

    /**
     * Website connectivity host machine name
     */
    private final String machineName;

    /**
     * Website connectivity port
     */
    private final String webPort;

    /**
     * Menus object used to search through Restaurant's menus
     */
    private final Menus menu;


    /**
     * Drone class constructor
     *
     * @param machineName web server host connectivity name
     * @param webPort web server connectivity port
     * @param menu Menus object to acquire Restaurant information
     */
    public Drone(String machineName, String webPort, Menus menu) {
        this.machineName = machineName;
        this.webPort = webPort;
        this.menu = menu;
        setConstants(machineName, webPort);

    }

    /**
     * Getter method for all the drone's delivery movements of the day
     *
     * @return flightpath
     */
    public ArrayList<FlightpathMove> getFlightpath() {
        return flightpath;
    }

    /**
     * Getter method for the information concerning all of the drone's delivered orders
     *
     * @return all of the orders delivered in that day : deliveredOrdersInADay
     */
    public ArrayList<Order> getDeliveredOrdersInADay() {
        return deliveredOrdersInADay;
    }

    /**
     * Method which sets relevant map information.
     * Initialises landmarks and No-Fly Zone perimeters so web server
     * lookups are optimised.
     *
     * @param machineName host machine name
     * @param webPort web server access
     */
    private void setConstants(String machineName, String webPort) {
        // initialises landmarks
        landmarks = GeoClient.findAllLandMarks(machineName, webPort);
        // initialises polygon perimeters
        polygonPerimeters = GeoClient.getPolygonBorders(machineName, webPort);
    }

    /**
     * Gets list of all unique Restaurants to be visited for a single Order.
     * Ensures there are no single restaurants being visited more than once (removes duplicates)
     *
     * @param order Order that is being delivered
     * @return List of unique restaurants to be visited
     */
    private List<Restaurant> getUniqueRestaurants(Order order) {

        String[] items = order.getItems();
        // Set up list of restaurants to visit
        List<Restaurant> restaurantsToVisit = new ArrayList<>();
        // checks for duplicates
        HashSet<String> restaurantNames = new HashSet<>();

        for (String item : items) {
            for (Restaurant restaurant : menu.getRestaurantList()){
                // Check item is sold by the restaurant
                if (restaurant.getMenuMap().containsKey(item)) {
                    // if restaurant is already visited, do not visit again
                    if (!restaurantNames.contains(restaurant.getName())) {
                        restaurantsToVisit.add(restaurant);
                        restaurantNames.add(restaurant.getName());
                    }
                }
            }
        }
        return restaurantsToVisit;
    }

    /**
     * Sorts restaurants by proximity to the current location.
     * There can only be at most two different restaurants visited per order.
     * Distance metric used is Euclidean Distance.
     * Ascending order sort (closest first).
     *
     * @param restaurants      List of Restaurant objects that
     *                         will be visited for the order
     * @param startingLocation Location of drone prior to starting the order's
     *                         delivery process
     * @return finalSorted     Sorted (by proximity) Restaurant objects
     */
    private List<Restaurant> sortRestaurants(List<Restaurant> restaurants, LongLat startingLocation) {

        // if there is only one restaurant to visit
        if (restaurants.size() == 1) {
            return restaurants;
        }
        // if invalid number of restaurants to visit
        if (restaurants.size() >= 3) {
            return null;
        }

        // define indices
        int first = 0;
        int second = 1;

        LongLat firstRestaurant = ClientIO.getW3WCoordinates(machineName,
                webPort, restaurants.get(first).getLocation());
        double firstDistance = startingLocation.distanceTo(firstRestaurant);

        LongLat secondRestaurant = ClientIO.getW3WCoordinates(machineName,
                webPort, restaurants.get(second).getLocation());
        double secondDistance = startingLocation.distanceTo(secondRestaurant);

        // Create final order queue
        ArrayList<Restaurant> finalSorted = new ArrayList<>();
        if (firstDistance <= secondDistance) {
            finalSorted.add(restaurants.get(first));
            finalSorted.add(restaurants.get(second));
        }
        else {
            finalSorted.add(restaurants.get(second));
            finalSorted.add(restaurants.get(first));
        }
        return finalSorted;
    }

    /**
     * Calculates drone delivery route for a given date.
     * Updates flightpath and deliveredOrdersInADay variables.
     *
     * @param orders that are going to be delivered
     */
    public void greedyOrdersInADay(ArrayList<Order> orders) {
        // get most expensive first
        orders.sort(Collections.reverseOrder());

        // Drone is launched from Appleton Tower
        LongLat previous = appletonTower;

        for (Order order : orders) {
            // if order does not meet requirements (invalid number of items)
            if (order.getOrderCost(order) == 0) {
                continue;
            }
            List<Restaurant> restaurantsNearest = sortRestaurants(getUniqueRestaurants(order), previous);
            if (restaurantsNearest == null) {
                continue;
            }
            // if the order delivery is possible && is within moves
            if (singleOrderRoute(previous, order, restaurantsNearest) && continueIterations) {
                deliveredOrdersInADay.add(order);
                previous = flightpath.get(flightpath.size() - 1).getNextPosition();
            }
        }

        // Go back to Appleton Tower at the end of moves or end of orders
        flightpath.addAll(travelToPosition(orders.get(orders.size()-1),
                flightpath.get(flightpath.size()-1).getNextPosition(), appletonTower));

        System.out.println("Number of orders delivered is " + deliveredOrdersInADay.size() +
                "/" + orders.size() + " in " + flightpath.size() + " moves.");
        double costDay = monetaryGain(deliveredOrdersInADay, orders);
        System.out.println("Monetary value performance of the day is " + costDay * 100 + "%");
    }

    /**
     * Method used to get all the unique moves from the drone's flightpath.
     * Gets only one value (LongLat) of the currentPosition and nextPosition pair (which identifies a
     * movement from one initial position to a newer position).
     * Uses global variable flightpath.
     *
     * @return allMoves : ArrayList of unique LongLat objects
     *                   within the drone's trajectory
     */
    public ArrayList<LongLat> getAllMovesSingle() {
        ArrayList<LongLat> allMoves = new ArrayList<>();
        // adds starting point since second element of pair is selected
        allMoves.add(appletonTower);
        for (FlightpathMove fp : flightpath) {
            allMoves.add(fp.getNextPosition());
        }
        return allMoves;
    }

    /**
     * Calculates delivery route from the initial starting location
     * (@param startingPoint) to the delivery location.
     * Returns a Boolean value regarding viability of delivery: returns false if
     * fulfilling the order delivery takes too many moves
     * (drone has limited number of moves), true otherwise.
     * Boolean value is determined by measuring whether the moves to execute
     * from the drop-off point back to
     * Appleton Tower are within the drone's maximum number of moves.
     *
     * @param startingPoint initial drone position
     * @param order order information including items to be delivered and delivery location
     * @param restaurants restaurants to be visited for item pick-up
     * @return true if order pick-up and delivery is successful
     */
    private Boolean singleOrderRoute(LongLat startingPoint, Order order, List<Restaurant> restaurants) {

        // cannot visit more than 2 restaurants per order
        if (restaurants.size() > menu.MAXIMUM_NUMBER_OF_RESTAURANTS) {
            return false;
        }

        // Get pick-up moves
        ArrayList<FlightpathMove> pickUp = getRestaurantPath(startingPoint, order, restaurants);
        if (pickUp == null) return false;

        // get drone's current location
        LongLat currentPosition = pickUp.get(pickUp.size() - 1).getNextPosition();
        // Get delivery location
        LongLat userLocation = ClientIO.getW3WCoordinates(machineName, webPort, order.getDeliverTo());
        // Get drop-off to the delivery location
        ArrayList<FlightpathMove> dropOff = travelToPosition(order, currentPosition, userLocation);
        // if drop-off route impossible
        if (dropOff == null) return false;

        // Calculate if route is possible within moves left
        int routeMoves = pickUp.size() + dropOff.size();
        int movesLeft = getMovesLeft(order, dropOff, routeMoves);

        // if route number of moves is possible, deliver order
        if (movesLeft >= 0) {
            updateFlightpath(pickUp, dropOff);
            return true;
        }

        // if movesLeft < 0: order will not be feasible to complete & return to AT.
        continueIterations = false;
        return true;
    }

    /**
     * Method which updates all variables that define the drone's
     * delivery flightpath. Concatenates the pick-up and drop-off
     * routes with the total sequence of moves followed by the drone,
     * updates the total number of moves left and ensures that the
     * next order's delivery is going to be attempted (continueIterations set to true)
     *
     * @param pickUp path for an order's pick-up route
     * @param dropOff drone's path to reach delivery location
     */
    private void updateFlightpath(ArrayList<FlightpathMove> pickUp, ArrayList<FlightpathMove> dropOff) {
        int routeMoves = pickUp.size() + dropOff.size();
        flightpath.addAll(pickUp);
        flightpath.addAll(dropOff);
        MAX_NUMBER_OF_MOVES -= routeMoves;
        continueIterations = true;
    }

    /**
     * Method which generates the path the drone will follow from the starting location to
     * the last restaurant visited. Represents pick-up phase of order delivery process.
     *
     * @param startingPoint initial position of the drone
     * @param order order that is being delivered
     * @param restaurants list of restaurants that will be visited. Can range from 1 to 2
     * @return pathToRestaurant path of drone moves that accomplish the pick-up of all the food items.
     */
    private ArrayList<FlightpathMove> getRestaurantPath(LongLat startingPoint, Order order, List<Restaurant> restaurants) {
        LongLat start = startingPoint;

        // Gather all moves for pick-up
        ArrayList<FlightpathMove> pathToRestaurant = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            // transform restaurant location into LongLat object
            LongLat restaurantPosition = ClientIO.getW3WCoordinates(machineName,
                    webPort, restaurant.getLocation());
            // Calculate location-to-location route from start position to the restaurant location
            ArrayList<FlightpathMove> flightpathMovesTemp = travelToPosition(order,start,restaurantPosition);
            if (flightpathMovesTemp == null) {
                return null;
            }
            // update route with restaurant pick-up path
            pathToRestaurant.addAll(flightpathMovesTemp);

            // update start position (for next restaurant, if any)
            start = pathToRestaurant.get(pathToRestaurant.size() - 1).getNextPosition();
        }
        return pathToRestaurant;
    }

    /**
     * Calculates the total moves the drone would perform from its current location
     * (given moves already performed in delivery route) back to Appleton Tower.
     *
     * @param order  food order that is being delivered
     * @param dropOff  route to deliver food to user. Last location of this route is the last restaurant visited.
     * @param routeMoves  Total number of moves that the route would take
     *                    from the start of the order (@param order) delivery to returning back to Appleton Tower.
     * @return the total amount of moves that the drone would still be able to perform.
     *          Returns MIN_VALUE if route is impossible.
     */
    private int getMovesLeft(Order order, ArrayList<FlightpathMove> dropOff, int routeMoves) {
        List<FlightpathMove> pathToAppletonTower = travelToPosition(order,
                dropOff.get(dropOff.size() - 1).getNextPosition(), appletonTower);
        // check return is possible
        if (pathToAppletonTower == null) {
            return Integer.MIN_VALUE;
        }
        return MAX_NUMBER_OF_MOVES - routeMoves - pathToAppletonTower.size();
    }

    /**
     * Calculates drone route between startingPoint and endPoint. Re-calculates drone path route
     * if initial path intersects with the perimeter of the No Fly Zone, returning to the startingPoint.
     * Re-routing process also ensures that the new path is not intersecting with the No Fly Zone.
     * If route is not possible, returns null.
     *
     * @param order Order object being delivered
     * @param startingPoint initial position of route
     * @param endPoint location drone is trying to reach
     * @return new LongLat position
     */
    private ArrayList<FlightpathMove> travelToPosition(Order order, LongLat startingPoint, LongLat endPoint) {

        // constant ensures no infinite loops when drone cannot find viable route
        int clearedFlightpathCounter = 0;

        LongLat currentPosition = startingPoint;
        ArrayList<FlightpathMove> moves = new ArrayList<>();

        while (!currentPosition.closeTo(endPoint)) {

            int angle = currentPosition.getAngle(endPoint);
            LongLat newPosition = currentPosition.nextPosition(angle);
            Line2D lineSegment = new Line2D.Double(currentPosition.longitude, currentPosition.latitude,
                    newPosition.longitude, newPosition.latitude);

            for (Line2D line : polygonPerimeters) {
                // if current path line intersects with polygon perimeter
                if (line.intersectsLine(lineSegment)) {
                    // undo all moves performed by the drone so far
                    moves.clear();
                    // move drone back to start location
                    currentPosition = startingPoint;

                    LongLat nearestLandmark = currentPosition.sortLongLats(landmarks).get(0);

                    while (!currentPosition.closeTo(nearestLandmark)) {
                        // impossible order delivery if drone has attempted new route too many times
                        if (clearedFlightpathCounter >= landmarks.size()) {
                            return null;
                        }

                        int newAngle = currentPosition.getAngle(nearestLandmark);
                        newPosition = currentPosition.nextPosition(newAngle);

                        // if route to new location crosses no fly zone,
                        // then it is an impossible route
                        if (currentPosition.isIntersectingPath(newPosition, polygonPerimeters)) {
                            return null;
                        }

                        FlightpathMove newMove = new FlightpathMove(order.getOrderNo(),
                                currentPosition, newPosition, newAngle);
                        moves.add(newMove);
                        currentPosition = newPosition;
                    }
                    clearedFlightpathCounter += 1;
                }
            }
            // save move
            FlightpathMove move = new FlightpathMove(order.getOrderNo(), currentPosition, newPosition, angle);
            moves.add(move);

            // move drone
            currentPosition = newPosition;
        }

        // hover for delivery or pick-up if location is not Appleton Tower
        if (endPoint != appletonTower) {
            // calculate new hovering position
            currentPosition = currentPosition.nextPosition(LongLat.HOVERING_ANGLE);
            FlightpathMove hover = new FlightpathMove(order.getOrderNo(),
                    currentPosition, currentPosition, LongLat.HOVERING_ANGLE);
            // ensure drone performs the hovering by adding it to the route
            moves.add(hover);
        }

        return moves;
    }

    /**
     * Calculates monetary gain of delivered orders with respect to cost of
     * all the day's orders.
     *
     * @param delivered     all orders that have been delivered
     * @param totalOrders   all orders that have been ordered that day
     * @return  monetary value managed to acquire with respect to the total
     */
    private double monetaryGain(Collection<Order> delivered, Collection<Order> totalOrders) {
        return monetaryValue(delivered) / (double) monetaryValue(totalOrders);
    }

    /**
     * Calculates monetary value of a list of orders
     * Ensures that each order's cost includes standard delivery charge of 50p.
     *
     * @param orders collection of orders, each with their own monetary costs
     * @return total cost of all of orders
     */
    private int monetaryValue(Collection<Order> orders) {
        int totalCost = 0;
        for (Order order : orders) {
            totalCost += menu.getDeliveryCost(order.getItems());
        }
        return totalCost;
    }

}