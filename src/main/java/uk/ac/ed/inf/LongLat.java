package uk.ac.ed.inf;

import java.awt.geom.Line2D;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * Class describing geographic location through longitude and
 * latitude.
 * Includes methods to calculate proximity between LongLat
 * objects (locations) and new position after moving.
 *
 */
public class LongLat {

    /*
    * Specific coordinates of delivery perimeter.
    * Longitudes and Latitudes correspond to coordinates
    * of locations: Appleton Tower, KFC, Buccleuch St Bus Stop, Top of Meadows.
    */
    private static final double EAST_LONGITUDE = -3.184319;
    private static final double WEST_LONGITUDE = -3.192473;
    private static final double SOUTH_LATITUDE = 55.942617;
    private static final double NORTH_LATITUDE = 55.946233;

    /**
     * Drone movement and flight constants
     */
    private static final double DISTANCE_TOLERANCE = 0.00015;
    private static final double DRONE_MOVE_LENGTH = 0.00015;
    public static final int HOVERING_ANGLE = -999;

    /**
     * LongLat object longitude
     */
    public final double longitude;

    /**
     * LongLat object latitude
     */
    public final double latitude;

    /**
     * LongLat class constructor. Identifies LongLat object with x and y coordinates
     * @param longitude : x-axis coordinate
     * @param latitude : y-axis coordinate
     */
    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * Method that checks drone is within specified coordinates
     * @return True if drone is within specified area
     */
    public boolean isConfined() {
        return ((WEST_LONGITUDE < this.longitude) &&
                (this.longitude < EAST_LONGITUDE) &&
                (SOUTH_LATITUDE < this.latitude) &&
                (NORTH_LATITUDE > this.latitude));
    }

    /**
     * Method which measures geographical distance (metres)
     * between two locations.
     * @param position LongLat object that represents
     *                 the next location.
     * @return Pythagorean distance between the two locations
     */
    public double distanceTo(LongLat position) {
        double longitudeDifference = Math.pow((this.longitude - position.longitude),2);
        double latitudeDifference = Math.pow((this.latitude - position.latitude),2);
        return Math.sqrt(longitudeDifference + latitudeDifference);
    }

    /**
     * Method which checks if two locations are close to each other.
     * Definition of closeness is set by constant DISTANCE_TOLERANCE
     *
     * @param position LongLat object to measure against
     * @return true if within distance tolerance range
     */
    public boolean closeTo(LongLat position) {
        return (this.distanceTo(position) < DISTANCE_TOLERANCE);
    }


    /**
     * Defines the new position of the drone if it makes a move in the
     * direction of the angle.
     * Returns current LongLat if drone is hovering (angle = -999)
     * or if move is invalid.
     *
     * @param angle defines direction of next move (degrees).
     * @return LongLat object denoting the new drone position if angle is
     *              valid, current position otherwise.
     */
    public LongLat nextPosition(int angle) {

        // if angle is hovering, return the current position (do not alter position)
        if (angle == HOVERING_ANGLE) {
            return this;
        }

        LongLat newPosition = this;

        // if angle is within valid range calculate the new LongLat position
        if ((angle >= 0) && (angle <= 350) && (angle % 10 == 0)) {
            double angleInRadians = Math.toRadians(angle);
            double newLongitude = longitude + (DRONE_MOVE_LENGTH * Math.cos(angleInRadians));
            double newLatitude = latitude + (DRONE_MOVE_LENGTH * Math.sin(angleInRadians));
            newPosition = new LongLat(newLongitude, newLatitude);
        }

        // Update position if it is within the allowed perimeter
        if (newPosition.isConfined()) {
            return newPosition;
        }

        return this;
    }

    /**
     * Returns closest LongLats with respect to a LongLat, sorted by proximity.
     * Distance metric used is Euclidean distance.
     *
     * @param locationsToVisit List of all the LongLats that need to be ordered
     *                         by proximity
     * @return LongLats sorted nearest-first
     */
    public ArrayList<LongLat> sortLongLats(ArrayList<LongLat> locationsToVisit) {

        ArrayList<Double> sortedLongLats = new ArrayList<>();
        // Initialise mapping of distances and LongLats
        HashMap<Double, LongLat> distanceToLongLat = new HashMap<>();

        // maps each landmark to their distance from the reference point
        for (LongLat landmark : locationsToVisit) {
            double distance = this.distanceTo(landmark);
            distanceToLongLat.put(distance, landmark);
            sortedLongLats.add(distance);
        }

        Collections.sort(sortedLongLats);

        // Create list (ordered) of sorted landmarks
        return getOrdering(sortedLongLats, distanceToLongLat);
    }

    /**
     * Transforms list of sorted distances into a sorted array of LongLats
     *
     * @param sortedLongLats list of distances from other LongLat (positions)
     *                       with respect to a specific location
     * @param distanceToLongLat mapping of LongLat location to distance
     * @return finalSorted sorted LongLat landmarks (distance)
     *                              with respect to a location
     */
    private ArrayList<LongLat> getOrdering(ArrayList<Double> sortedLongLats,
                                           HashMap<Double, LongLat> distanceToLongLat) {
        ArrayList<LongLat> finalSorted = new ArrayList<>();
        for (double landmarkDistance : sortedLongLats) {
            finalSorted.add(distanceToLongLat.get(landmarkDistance));
        }
        return finalSorted;
    }


    /**
     * Identifies whether a Line2D line segment made from one LongLat to the next
     * intersects with one of the polygon lines (input list of Line2D lines).
     *
     * @param newPosition future drone position
     * @param polygonPerimeters No-Fly Zone perimeter.
     *                          List of Line2D segments which the line segment
     *                          should not intersect with.
     * @return true if line crosses no fly zone
     */
    public boolean isIntersectingPath(LongLat newPosition, List<Line2D> polygonPerimeters) {
        Line2D line = new Line2D.Double(this.longitude, this.latitude,
                newPosition.longitude, newPosition.latitude);
        for (Line2D perimeter : polygonPerimeters) {
            if (line.intersectsLine(perimeter)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates angle in degrees between two LongLat objects, namely its current location
     * and the one it strives to go towards.
     *
     * @param goalPosition    LongLat object representing the final
     *                        destination to arrive to
     * @return angle    Angle between currentPosition and goalPosition
     */
    public int getAngle(LongLat goalPosition) {
        double arctan = Math.atan2((goalPosition.latitude - this.latitude),
                (goalPosition.longitude - this.longitude));
        long angle = (Math.round(Math.toDegrees(arctan)) / 10) * 10;
        if ((angle) < 0) {
            angle += 360;
        }
        return (int) angle;
    }

}
