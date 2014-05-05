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
import java.sql.SQLException;
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
    private String hadoopPassword;
    
    /**
     * Constructor.
     * @param hiveServer
     * @param hivePort
     * @param hadoopUser
     */
    public HiveClient(String hiveServer, String hivePort, String hadoopUser, String hadoopPassword) {
        logger = Logger.getLogger(HiveClient.class);
        this.hiveServer = hiveServer;
        this.hivePort = hivePort;
        this.hadoopUser = hadoopUser;
        this.hadoopPassword = hadoopPassword;
    } // HiveClient
    
    /**
     * Creates a HiveQL external table.
     * @param query
     * @return True if the table could be created, false otherwise.
     */
    public boolean doCreateTable(String query) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        boolean res = true;
        
        try {
            // get a connection to the Hive/Shark server
            con = getConnection();
            
            // create a statement
            stmt = con.createStatement();
            
            // execute the query
            rs = stmt.executeQuery(query);
        } catch (Exception e) {
            logger.error(e.getMessage());
            res = false;
        } finally {
            return res && closeHiveObjects(con, stmt, rs);
        } // try catch finally
    } // doCreateTable

    /**
     * Executes a HiveQL sentence.
     * @param query
     * @return True if the query succeded, false otherwise.
     */
    public boolean doQuery(String query) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        boolean res = true;
        
        try {
            // get a connection to the Hive/Shark server
            con = getConnection();
            
            // create a statement
            stmt = con.createStatement();
            
            // execute the query
            rs = stmt.executeQuery(query);

            // iterate on the result
            while (rs.next()) {
                String s = "";

                for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
                    s += rs.getString(i) + ",";
                } // for
              
                s += rs.getString(rs.getMetaData().getColumnCount());
                System.out.println(s);
            } // while
        } catch (Exception e) {
            logger.error(e.getMessage());
            res = false;
        } finally {
            return res && closeHiveObjects(con, stmt, rs);
        } // try catch finally
    } // doQuery
    
    /**
     * Close all the Hive objects previously opened by doCreateTable and doQuery.
     * @param con
     * @param stmt
     * @param rs
     * @return True if the Hive objects have been closed, false otherwise.
     */
    private boolean closeHiveObjects(Connection con, Statement stmt, ResultSet rs) {
        boolean res = true;
        
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                res = false;
            } // try catch
        } // if

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                res = false;
            } // try catch
        } // if

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                res = false;
            } // try catch
        } // if
        
        return res;
    } // closeHiveObjects
    
    /**
     * Gets a connection to the Hive server.
     * @return
     * @throws Exception
     */
    private Connection getConnection() throws Exception {
        // dynamically load the Hive JDBC driver
        Class.forName(driverName);

        // return a connection based on the Hive JDBC driver
        logger.debug("Connecting to jdbc:hive://" + hiveServer + ":" + hivePort + "/default?user=" + hadoopUser
                + "&password=XXXXXXXXXX");
        return DriverManager.getConnection("jdbc:hive://" + hiveServer + ":" + hivePort + "/default?user=" + hadoopUser
                + "&password=" + hadoopPassword);
    } // getConnection
    
} // HiveClient