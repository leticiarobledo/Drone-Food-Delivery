package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Class handling GeoJSON data manipulation.
 */
public class GeoClient {

    /**
     * Method used to transform all landmarks from FeatureCollection objects to LongLats.
     * Creates endpoint and uses ClientIO.getGeoJSON to perform HTTP GET request
     *
     * @param machineName name of web server host machine
     * @throws NullPointerException if information is null
     * @throws IllegalArgumentException if argument used is illegal
     * @param port web server connectivity port
     * @return coordinates: list of all the landmarks
     */
    public static ArrayList<LongLat> findAllLandMarks(String machineName, String port) {

        // set up response recipient
        FeatureCollection fc = ClientIO.getGeoJSON(machineName, port, "landmarks");

        if (fc == null || fc.features() == null) {
            throw new NullPointerException();
        }

        // initialise response array
        ArrayList<LongLat> coordinates = new ArrayList<>();
        try {
            // cast data to LongLats
            for (Feature feature : fc.features()) {
                Geometry data = feature.geometry();
                if (data instanceof Point) {
                    Point p = (Point)data;
                    // Create LongLat from Point
                    LongLat landmark = new LongLat(p.coordinates().get(0),p.coordinates().get(1));
                    coordinates.add(landmark);
                }
            }

        } catch (NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return coordinates;
    }

    /**
     * Method used to identify all the No Fly Zones,
     * creates endpoint and uses ClientIO.getGeoJSON to perform HTTP GET request
     *
     * @param machineName name of web server host machine
     * @param port web server connectivity port
     * @throws NullPointerException if data collected from web server is null
     * @throws IllegalArgumentException if data from featureCollection cannot be transformed.
     * @return list of polygon objects which represent all no-fly zone buildings
     */
    private static List<Polygon> getNoFlyZones(String machineName, String port) {
        // set up response recipient
        FeatureCollection fc = ClientIO.getGeoJSON(machineName, port, "no-fly-zones");

        if (fc == null || fc.features() == null) {
            throw new NullPointerException();
        }
        // Initialise response array
        List<Polygon> protectedBuildings = new ArrayList<>();
        try {

            for (Feature feature : fc.features()) {
                Geometry data = feature.geometry();
                if (data instanceof Polygon){
                    Polygon newBuilding = (Polygon)data;
                    protectedBuildings.add(newBuilding);
                }
            }

        } catch (NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return protectedBuildings;
    }

    /**
     * Method which transforms a Polygon into a List of Line2D segments
     *
     * @param machineName web server connectivity host
     * @param port web port enabling connectivity access
     * @return polygonBorders : list of all the polygon's perimeters
     */
    public static List<Line2D> getPolygonBorders(String machineName, String port) {
        List<Polygon> noFlyZones = getNoFlyZones(machineName, port);
        List<Line2D> polygonBorders = new ArrayList<>();
        for (Polygon polygon : noFlyZones) {
            LineString ls = polygon.outer();
            List<Point> points = ls.coordinates();

            // Create Line2D objects from Point
            for (int i = 1; i < points.size(); i++) {
                Line2D lineSegment = new Line2D.Double(points.get(i-1).longitude(),
                        points.get(i-1).latitude(),
                        points.get(i).longitude(),
                        points.get(i).latitude());
                polygonBorders.add(lineSegment);
            }
        }
        return polygonBorders;
    }


}
