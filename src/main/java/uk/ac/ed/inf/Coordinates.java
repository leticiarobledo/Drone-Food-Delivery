package uk.ac.ed.inf;

/**
 * Coordinates transient subclass
 * Identifies a coordinate with longitude and latitude.
 * Used for unmarshalling purposes when identifying longitudes
 * and latitudes in What3Words coordinates
 */
public class Coordinates {

    /**
     * Longitude field in a coordinate
     */
    double lng;

    /**
     * Latitude field in a coordinate
     */
    double lat;

    /**
     * Coordinate class constructor
     * @param lng coordinate longitude
     * @param lat coordinate latitude
     */
    public Coordinates(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }
}