package uk.ac.ed.inf;

import java.util.HashMap;
import java.util.List;

/**
 * Class Representing a Restaurant.
 * Contains a name, location and menu.
 */
public class Restaurant {

    /**
     * Restaurant name
     */
    private final String name;

    /**
     * Restaurant geographical location
     */
    private final String location;

    /**
     * Restaurant menu
     */
    private final List<FoodItem> menu;

    /**
     * Mapping of menu items to cost
     */
    private HashMap<String,Integer> menuMap;

    /**
     * Restaurant Class constructor
     * @param name restaurant name
     * @param location geographical location
     * @param menu list of all menu items
     * @param menuMap hashmap mapping menu items to their price
     */
    public Restaurant(String name, String location, List<FoodItem> menu, HashMap<String, Integer> menuMap) {
        this.name = name;
        this.location = location;
        this.menu = menu;
        this.menuMap = menuMap;
        setMenuMap();
    }

    /**
     * Getter method that retrieves restaurant name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter method that retrieves restaurant location
     * @return location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Getter method that retrieves the restaurant's menu items
     * @return menu
     */
    public List<FoodItem> getMenu() {
        return menu;
    }

    /**
     * Getter function for HashMap function which maps
     * an item's name to its price
     * @return menuMap
     */
    public HashMap<String, Integer> getMenuMap() {
        return menuMap;
    }

    /**
     * Setter method with maps FoodItem name to
     * its price (pence)
     */
    public void setMenuMap() {
        HashMap<String, Integer> menuMap = new HashMap<>();
        for (FoodItem item : menu) {
            menuMap.put(item.getItem(), item.getPence());
        }
        this.menuMap = menuMap;
    }

}