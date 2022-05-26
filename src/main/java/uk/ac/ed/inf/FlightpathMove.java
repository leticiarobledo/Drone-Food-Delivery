package uk.ac.ed.inf;

/**
 * Class denoting a movement made by the drone.
 * The drone moves a fixed length from the original position
 * in a specified angle (direction) to the final position.
 */
public class FlightpathMove {

    /**
     * Order number of order that is being delivered during this move
     */
    private final String orderNo;

    /**
     * Initial position at the start of the move
     */
    private final LongLat originalPosition;

    /**
     * Final position at the end of the move
     */
    private final LongLat nextPosition;

    /**
     * Angle that specifies direction to move in.
     * Ranges from 0 to 350 (degrees)
     */
    private final int angle;

    /**
     * FlightpathMove class constructor
     *
     * @param orderNo order number
     * @param originalPosition initial drone position
     * @param nextPosition final drone position
     * @param angle direction drone moves in
     */
    public FlightpathMove(String orderNo, LongLat originalPosition, LongLat nextPosition, int angle) {
        this.orderNo = orderNo;
        this.originalPosition = originalPosition;
        this.nextPosition = nextPosition;
        this.angle = angle;
    }

    /**
     * Gets order number of order that is being delivered during move
     * @return orderNo order number
     */
    public String getOrderNo() {
        return orderNo;
    }

    /**
     * Gets original LongLat position of drone prior to moving
     * @return originalPosition
     */
    public LongLat getOriginalPosition() {
        return originalPosition;
    }

    /**
     * Gets the next drone's position after moving
     * @return the new position nextPosition
     */
    public LongLat getNextPosition() {
        return nextPosition;
    }

    /**
     * Gets the angle used to move from originalPosition to nextPosition
     * @return angle
     */
    public int getAngle() {
        return angle;
    }

}
