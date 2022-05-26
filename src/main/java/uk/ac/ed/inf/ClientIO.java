package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Class which performs HTTP requests
 */
public class ClientIO {

    /**
     * Single Client created
     */
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Performs a GET HTTP request and returns a String with the remote endpoint's reply
     *
     * @param endpoint : full website address to information location on website
     * @throws IOException if interrupted or illegal connection
     * @throws InterruptedException if connection with the web server has been interrupted
     * @throws IllegalArgumentException if endpoint contains invalid arguments
     * @return String request from website
     */
    public static String getRequest(String endpoint) {
        // HttpRequest assumes that it is a GET request by default.
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).build();
        HttpResponse<String> response = null;

        try {
            // Call the send method on the client
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed with HTTP code: " + response.statusCode());
            }

        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            System.out.println("Fatal error: Unable to connect to " + endpoint);
            // Exit the application
            System.exit(1);
        }

        return response.body();
    }


    /**
     * Performs a GET HTTP request for geoJSON objects.
     * Gets files within the "buildings" folder of the web-server, and
     * selects specific file through "objective" input parameter.
     *
     * @param machineName Website connectivity host machine name
     * @param port Website connectivity port connection
     * @param objective file name of geoJSON file
     * @throws Exception if illegal connection is attempted or connection is interrupted
     * @return FeatureCollection object containing all information from the geoJSON file
     */
    public static FeatureCollection getGeoJSON(String machineName, String port, String objective) {
        // construct endpoint
        String endpoint = "http://" + machineName + ":" + port + "/buildings/" + objective + ".geojson";

        FeatureCollection featureCollection = null;
        try {
            String request = ClientIO.getRequest(endpoint);
            // dump web-server contents into FeatureCollection object
            featureCollection = FeatureCollection.fromJson(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureCollection;
    }

    /**
     * Creates geoJSON file and saves it to the local directory.
     *
     * @param positions List of all the drone's positions throughout the delivery process
     * @param date date corresponding to the delivery process generated
     *             by the algorithm on the orders requested on that date.
     * @throws IOException if I/O connection is interrupted
     */
    public static void toGeoJSON(ArrayList<LongLat> positions, String date) {

        // transform LongLat objects into FeatureCollection
        List<Point> pl = new ArrayList<>();
        for (LongLat position : positions) {
            Point p = Point.fromLngLat(position.longitude,position.latitude);
            pl.add(p);
        }
        LineString ls = LineString.fromLngLats(pl);
        Feature f = Feature.fromGeometry(ls);

        FeatureCollection fc = FeatureCollection.fromFeature(f);

        // get working directory
        String fileName = getFileName(date);
        String userDirectory = System.getProperty("user.dir");
        String finalPath = userDirectory + File.separator + fileName;

        try {
            // Save file to local working directory
            FileWriter file = new FileWriter(finalPath,true);
            Files.newBufferedWriter(Path.of(finalPath), new StandardOpenOption[]{StandardOpenOption.TRUNCATE_EXISTING});
            file.write(fc.toJson());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Gets correct file name and ensures correct formatting
     *
     * @param date MM/DD/YYYY date format
     * @return complete file name to be saved as
     */
    private static String getFileName(String date) {

        String[] splitDate = date.split("\\/");
        String day = splitDate[1];
        String month = splitDate[0];
        String year = splitDate[2];
        String hyphen = "-";
        // build final filename
        return "drone" + hyphen + day + hyphen + month + hyphen + year + ".geojson";
    }


    /**
     * Performs an HTTP GET request by transforming What3Words delivery location encoding to
     * web-server folder accesses. Unmarshalls acquired information into Location object, which
     * is then transformed into a LongLat.
     *
     * @param machineName Website connectivity host machine name
     * @param port Website connectivity port connection
     * @param W3W What3Words location encoding. Denotes a specific location on the map
     * @throws Exception denoting corresponding exception if illegal
     *          arguments used or connection is interrupted.
     * @return LongLat object representing a location in the map.
     */
    public static LongLat getW3WCoordinates(String machineName, String port, String W3W) {

        String fixedW3W = W3W.replaceAll("\\.", "/");
        // Construct endpoint request to access coordinates
        String endpoint = "http://" + machineName + ":" + port + "/words/" + fixedW3W + "/details.json";

        // Set up response recipient
        Location address = null;
        try {
            String request = ClientIO.getRequest(endpoint);
            // unmarshal response into Location object
            Type listType = new TypeToken<Location>() {}.getType();
            address = new Gson().fromJson(request, listType);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cast W3W format to LongLat
        double longitude = 0;
        double latitude = 0;
        if (address != null) {
            longitude = address.getCoordinates().lng;
            latitude = address.getCoordinates().lat;
        }

        return new LongLat(longitude, latitude);
    }


}
