# Informatics Large Practical (2021-2022)
This application develops an optimal route to be followed by a drone in a food delivery process, reading in the daily lunch orders from the database and the available restaurant's menus and food item prices from the website.
The drone collects information from student's lunch orders and generates the most optimal food delivery route with respect to the order cost and distance travelled by the drone, in order to maximize overall daily monetary profit.
The drone delivers lunch orders in the University of Edinburgh George Square Campus area.  
Developed in Java 14 and with the Maven build system for dependency management.  
Overall Course Mark: 83.6/100 - Grade A2


## Running the project
When the project is compiled with the Maven build system, the application will produce a JAR file `ilp-1.0-SNAPSHOT.jar`. 
Running this file with the command
`java -jar target/ilp-1.0-SNAPSHOT.jar 15 09 2022 80 1527` 
will produce the generated optimal route for all the lunch orders for the date 15/09/2022 from the database.  
The database is connecting at port 1527, and all the restaurant's menus are read from the website (web server at port 80).

## Route Examples
The red polygons denote No-Fly Zones, University buildings the drone cannot fly over.

![image](https://user-images.githubusercontent.com/76557301/170546946-efac1d75-65d0-42cd-9159-c6e8bf5ec194.png)
Drone Delivery Route for 1st December 2022. Total of 15/15 orders delivered. Monetary value performance: 100%. 997 total moves.




![image](https://user-images.githubusercontent.com/76557301/170546805-e2b5b116-2db9-4795-baec-bccb934011f2.png)
Drone Delivery Route for the 1st December 2023, 26 deliveries out of 27 orders (1491 moves). Percentage monetary value delivered: 96.8%.


Developed by Leticia Robledo
