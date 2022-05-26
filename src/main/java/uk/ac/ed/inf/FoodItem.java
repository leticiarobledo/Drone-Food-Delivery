package uk.ac.ed.inf;

/**
 * Class representing a food item
 */
public class FoodItem {

    /**
     * Food item name
     */
    private final String item;

    /**
     * Food item price in pence
     */
    private final int pence;

    /**
     * Food Item class constructor
     * @param item item name
     * @param pence cost of item in pence
     */
    public FoodItem(String item, int pence) {
        this.item = item;
        this.pence = pence;
    }

    /**
     * Getter method for item
     * @return item
     */
    public String getItem() {
        return item;
    }

    /**
     * Getter method for pence
     * @return item price
     */
    public int getPence() {
        return pence;
    }

}
