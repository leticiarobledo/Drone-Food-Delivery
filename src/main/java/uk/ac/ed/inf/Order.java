package uk.ac.ed.inf;

/**
 * Class representing a customer's order in the delivery system.
 * Includes information about the customer (order number and customer identification number),
 * delivery address and items to deliver.
 */
public class Order implements Comparable<Order>  {

    /**
     * Order Number
     * Identifies unique order.
     */
    private final String orderNo;

    /**
     * Customer Identification Number, currently University
     * Student Number (unique identifier).
     */
    private final String customer;

    /**
     * What3Words address coordinate
     */
    private final String deliverTo;

    /**
     * Order items. Can range from 1 to 4.
     */
    private final String[] items;

    /**
     * Menus instance to enable order item identification
     * and price comparison.
     */
    private Menus menu;

    /**
     * Order Class constructor
     * @param orderNo order number
     * @param customer customer unique ID
     * @param deliverTo address identifying location for
     *                  order delivery
     * @param items food elements to be picked-up
     *              from restaurants and delivered
     */
    public Order(String orderNo, String customer, String deliverTo, String[] items, Menus menu) {
        this.orderNo = orderNo;
        this.customer = customer;
        this.deliverTo = deliverTo;
        this.items = items;
        this.menu = menu;
    }

    /**
     * Getter method that retrieves Order Number
     *
     * @return orderNo
     */
    public String getOrderNo() {
        return orderNo;
    }

    /**
     * Getter method that retrieves customer ID.
     *
     * @return customer
     */
    public String getCustomer() {
        return customer;
    }

    /**
     * Getter method that returns the location
     * for the order delivery, using What3Words location encoding.
     *
     * @return deliverTo
     */
    public String getDeliverTo() {
        return deliverTo;
    }

    /**
     * Getter method for order items.
     * If order contains only one item,
     * returns list of only 1 element
     *
     * @return item
     */
    public String[] getItems() {
        return items;
    }

    /**
     * Method that enables Order object comparison by
     * sorting on the items.
     *
     * @param order2 order to compare against
     * @return sorted comparison
     */
    @Override
    public int compareTo(Order order2) {
        return menu.getDeliveryCost(this.items) - menu.getDeliveryCost(order2.items);
    }

    /**
     * Calculates a single Order's cost of delivery
     *
     * @param order order including items to deliver
     * @return delivery cost (order cost and delivery charge)
     */
    public int getOrderCost(Order order) {
        return menu.getDeliveryCost(order.getItems());
    }

}
