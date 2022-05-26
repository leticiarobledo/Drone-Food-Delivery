package uk.ac.ed.inf;

/**
 * Class representing a Location.
 * Transient class used for "unmarshalling" JSON filed.
 * Includes its coordinates in the format (longitude, latitude)
 * and its What3Words (W3W) identification.
 */
public class Location {

    /**
     * Coordinates attribute denoting the coordinates
     * to be located in the JSON file
     */
    private final Coordinates coordinates;

    /**
     * Location Class constructor
     */
    public Location(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Getter method for Location coordinates
     * @return coordinates of type Coordinate
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }



}
