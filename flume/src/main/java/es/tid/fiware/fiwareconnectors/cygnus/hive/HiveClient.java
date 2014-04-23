/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * frb@tid.es
 */

package es.tid.fiware.fiwareconnectors.cygnus.hive;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.apache.log4j.Logger;

/**
 *
 * @author frb
 */
public class HiveClient {
    
    // JDBC driver required for Hive connections
    private Logger logger;
    private static String driverName = "org.apache.hadoop.hive.jdbc.HiveDriver";
    private String hiveServer;
    private String hivePort;
    private String hadoopUser;
    
    /**
     * Constructor.
     * @param hiveServer
     * @param hivePort
     * @param hadoopUser
     */
    public HiveClient(String hiveServer, String hivePort, String hadoopUser) {
        logger = Logger.getLogger(HiveClient.class);
        this.hiveServer = hiveServer;
        this.hivePort = hivePort;
        this.hadoopUser = hadoopUser;
    } // HiveClient
    
    /**
     * Creates a HiveQL external table.
     * @param query
     * @return True if the table could be created, false otherwise.
     */
    public boolean doCreateTable(String query) {
        try {
            // get a connection to the Hive/Shark server
            Connection con = getConnection();
            
            // create a statement
            Statement stmt = con.createStatement();
            
            // execute the query
            ResultSet res = stmt.executeQuery(query);

            // close everything
            res.close();
            stmt.close();
            con.close();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        } // try catch
    } // doCreateTable

    /**
     * Executes a HiveQL sentence.
     * @param query
     * @return True if the query succeded, false otherwise.
     */
    public boolean doQuery(String query) {
        try {
            // get a connection to the Hive/Shark server
            Connection con = getConnection();
            
            // create a statement
            Statement stmt = con.createStatement();
            
            // execute the query
            ResultSet res = stmt.executeQuery(query);

            // iterate on the result
            while (res.next()) {
                String s = "";

                for (int i = 1; i < res.getMetaData().getColumnCount(); i++) {
                    s += res.getString(i) + ",";
                } // for
              
                s += res.getString(res.getMetaData().getColumnCount());
                System.out.println(s);
            } // while

            // close everything
            res.close();
            stmt.close();
            con.close();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        } // try catch
    } // doQuery
    
    /**
     * Gets a connection to the Hive server.
     * @param hiveServer
     * @param hivePort
     * @param hadoopUser
     * @param hadoopPassword
     * @return
     */
    private Connection getConnection() throws Exception {
        // dynamically load the Hive JDBC driver
        Class.forName(driverName);

        // return a connection based on the Hive JDBC driver
        logger.debug("Connecting to jdbc:hive://" + hiveServer + ":" + hivePort + "/default?user=" + hadoopUser
                + "&password=XXXXXXXXXX");
        return DriverManager.getConnection("jdbc:hive://" + hiveServer + ":" + hivePort + "/default?user=" + hadoopUser
                + "&password=XXXXXXXXXX");
    } // getConnection
    
} // HiveClient