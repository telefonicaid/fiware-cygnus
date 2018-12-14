/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.resources.hiveclients.hiveserver1client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Francisco Romero Bueno frb@tid.es
 * 
 * Basic remote client for HiveServer1 mimicing the native Hive CLI behaviour.
 * 
 * Can be used as the base for more complex clients, interactive or not interactive.
 */
public final class HiveServer1Client {
    // JDBC driver required for HiveServer1 connections
    private static final String DRIVERNAME = "org.apache.hadoop.hive.jdbc.HiveDriver";
    // persistent connection
    private static Connection con;
    
    /**
     * Constructor.
     */
    private HiveServer1Client() {
    } // HiveServer1Client

    /**
     * Gets a connection to HiveServer1.
     * @param hiveServer
     * @param hivePort
     * @param hadoopUser
     * @param hadoopPassword
     * @return A connection to HiveServer1
     */
    private static Connection getConnection(String hiveServer, String hivePort, String hadoopUser,
            String hadoopPassword) {
        try {
            // dynamically load the Hive JDBC driver
            Class.forName(DRIVERNAME);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return null;
        } // try catch
      
        try {
            System.out.println("Connecting to jdbc:hive://" + hiveServer + ":" + hivePort
                    + "/default?user=" + hadoopUser + "&password=XXXXXXXXXX");
            // return a connection based on the Hive JDBC driver
            return DriverManager.getConnection("jdbc:hive://" + hiveServer + ":" + hivePort
                    + "/default?user=" + hadoopUser + "&password=" + hadoopPassword);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        } // try catch
    } // getConnection

    /**
     * Executes a HiveQL query.
     * @param query
     */
    private static void doQuery(String query) {
        try {
            // from here on, everything is SQL!
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // iterate on the result
            while (rs.next()) {
                String s = "";

                for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
                    s += rs.getString(i) + ",";
                } // for

                s += rs.getString(rs.getMetaData().getColumnCount());
                System.out.println(s);
            } // while

            // close everything
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } // try catch
    } // doQuery

    /**
     * Main program.
     * @param args
     */
    public static void main(String[] args) {
        // get the arguments
        String hiveServer = args[0];
        String hivePort = args[1];
        String cosmosUser = args[2];
        String cosmosPassword = args[3];
        
        // get a connection to the Hive server running on the specified IP address, listening on 10000/TCP port
        // authenticate using my credentials
        con = getConnection(hiveServer, hivePort, cosmosUser, cosmosPassword);
                
        if (con == null) {
            System.out.println("Could not connect to the Hive server!");
            System.exit(-1);
        } // if
        
        // add JSON serde; this is only necessary if the serde is not in the HiveServer1 classpath
        //doQuery("add JAR /usr/local/hive-0.9.0-shark-0.8.0-bin/lib/json-serde-1.1.9.3-SNAPSHOT.jar");
        
        while (true) {
            // prompt the user for a set of HiveQL sentence (';' separated)
            System.out.print("remotehive> ");
            
            // open the standard input
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            
            // read the HiveQL sentences from the standard input
            String hiveqlSentences = null;
            
            try {
                hiveqlSentences = br.readLine();
            } catch (IOException e) {
                System.out.println("IO error trying to read a HiveQL query: " + e.getMessage());
                System.exit(1);
            } // try catch
            
            if (hiveqlSentences == null) {
                continue;
            } // if
            
            if (hiveqlSentences.equalsIgnoreCase("exit;")) {
                System.exit(0);
            } // if
            
            // get all the queries within the input HiveQL sentences
            String[] queries = hiveqlSentences.split(";");

            // for each query, execute it
            for (String querie : queries) {
                doQuery(querie);
            } // for
        } // while
    } // main
    
} // HiveServer1Client
