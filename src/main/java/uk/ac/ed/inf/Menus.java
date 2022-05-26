package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Class representing all Restaurant Menus
 * that can be ordered from
 */
public class Menus {

    /**
     * Standard delivery cost
     */
    private static final int DELIVERY_COST = 50;

    /**
     * Maximum number of restaurants to be visited per order
     */
    public final int MAXIMUM_NUMBER_OF_RESTAURANTS = 2;

    /**
     * Website connectivity host machine name
     */
    private final String machineName;

    /**
     * Website connectivity port
     */
    private final String port;

    /**
     * List of the Restaurants' Menus which can be ordered from
     */
    private final List<Restaurant> restaurantList;

    /**
     * Menus class constructor
     *
     * @param machineName user current device
     * @param port port connection
     */
    public Menus(String machineName, String port) {
        this.machineName = machineName;
        this.port = port;
        restaurantList = getRestaurants();
    }

    /**
     * Getter method for List of Restaurants that can be ordered from.
     *
     * @return restaurantList
     */
    public List<Restaurant> getRestaurantList() {
        return restaurantList;
    }

    /**
     * Method that gets restaurant data from website
     *
     * @throws Exception in case of illegal or invalid connection
     * @return responseMenu containing menu to order from
     */
    public List<Restaurant> getRestaurants() {
        // Construct endpoint request to access available menu
        String endpoint = "http://" + machineName + ":" + port + "/menus/menus.json";

        // setup the response recipient
        List<Restaurant> responseMenu = null;

        try {
            String request = ClientIO.getRequest(endpoint);
            // unmarshal response
            Type listType = new TypeToken<List<Restaurant>>() {}.getType();
            responseMenu = new Gson().fromJson(request, listType);

            for (Restaurant restaurant : responseMenu) {
                restaurant.setMenuMap();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseMenu;
    }

    /**
     * Method that returns total delivery cost.
     * Delivery price will only be calculated if constraints
     * are satisfied (1-4 items and 1-2 Restaurants).
     *
     * @param order variable number of items in the order
     * @return if constraints satisfied, total delivery cost including
     *          delivery fee DELIVERY_COST. Otherwise, return 0.
     */
    public int getDeliveryCost(String... order) {
        int totalCost = 0;
        int minimumOrderItemNumber = 1;
        int maximumOrderItemNumber = 4;

        // Set of all visited restaurants
        Set<Restaurant> orderedRestaurants = new HashSet<Restaurant>();

        // Orders can only have 1-4 items
        if (order.length >= minimumOrderItemNumber && order.length <= maximumOrderItemNumber) {

            for (String foodItem : order) {

                for (Restaurant restaurant : restaurantList) {
                    Integer cost = restaurant.getMenuMap().get(foodItem);
                    if (cost != null) {
                        totalCost += cost;
                        orderedRestaurants.add(restaurant);
                    }
                }
            }
            totalCost += DELIVERY_COST;
        }

        // Order cannot be from more than two different restaurants
        if (orderedRestaurants.size() > MAXIMUM_NUMBER_OF_RESTAURANTS) {
            totalCost = 0;
        }

        return totalCost;
    }

}