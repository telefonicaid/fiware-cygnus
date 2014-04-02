# Plague Tracker demo application
Conceptually speaking, this is an application running on top of the Cosmos Global Instance in FI-LAB. The Plague Tracker accesses and processes the historical data about the plagues affecting the spanish city of Malaga. More details on the nature, representation formats, location, etc. of the data can be found at:

http://forge.fi-ware.eu/plugins/mediawiki/wiki/fiware/index.php/M%C3%A1laga_open_datasets#Plagues_tracking

Under the above concept there is a Java-based Hive client querying the Cosmos cluster through the TCP/10000 port, where a Hive server listens for incoming connections. This Hive client is governed by a Web application exposing a GUI (a map of the city of Malaga and a set of controls) the final user operates in order to get certain visualizations of the data. These visualizations/operations are:
- Get the current focuses. The map shows the neighbourhoods affected by the selected type of plague. A neighbourhood is affected by a plague if a technician had to work in mitigating the plague in that neighbourhood the last month.
- Get an infection forecast. The map shows a forecast about the neighbourhoods that will probably fe infected by the selected type of plague. The forecast is based on the historical number of incidences, the weather and the proximity to already infected neighbourhoods.

The plague types the user can select are:
- Rats
- Mice
- Pigeons
- Cockroaches
- Bees
- Wasps
- Ticks
- Fleas

In addition to the map, three charts show the correlation index between the selected type of plague and three ambiental parameters such as the temperature, the rainfall and the humidity. These ambiental parameters are got from another dataset related to the city of Malaga:

http://forge.fi-ware.eu/plugins/mediawiki/wiki/fiware/index.php/M%C3%A1laga_open_datasets#Weather

## Requirements, dependencies and security concerns
Being a web application, the Plague Tracker needs an applications server such as Tomcat. The current code has been tested on Tomcat 7.0.14.0.

The code depends on Hive (0.7.1 or higher), Hadoop (0.20.0 / CDH3) and Gson (2.2.4 or higher). Nevertheless, the dependencies are automatically managed by Maven (see the pom.xml file), thus nothing should be done regarding this.

If you are thinking on deploying your own instance of the application, please take into account the hosting server will need permissions for accessing the Cosmos Global Instance. This is not a constraing when the hosting server is a virtual machine from FI-WARE, created through the FI-LAB Portal (http://lab.fi-lab.eu).

## Build and deployment
Build the Plague Tracker application with Maven:

    $ cd resources/plague-tracker
    $ mvn package

The result of the Maven packaging is a plague-tracker-0.1.war file within resources/plague-tracker/target. This file is prepared to be deployed in an Apache Tomcat application server. Use the Tomcat manager application to deploy the .war file, or uncompress it and copy the content in [APACHE_TOMCAT_HOME]/webapps/plague-tracker.

    $ cd [APACHE_TOMCAT_HOME]/webapps
    $ mkdir plague-tracker
    $ cp plague-tracker-0.1.war plague-tracker
    $ cd plague-tracker
    $ unzip plague-tracker-0.1.war

## Already deployed instances of this application
http://130.206.81.65/plague-tracker/

## Contact

For any question, bug report, suggestion or feedback in general, please contact with Francisco Romero (frb at tid dot es).

## License
This code is licensed under GNU Affero General Public License v3. You can find the license text in the LICENSE file in the repository root.
