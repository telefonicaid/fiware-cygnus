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

package com.telefonica.iot.cygnus.backends.hive;

import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author frb
 */
public class HiveBackendImpl implements HiveBackend {
    
    // JDBC driver required for Hive connections
    private static final CygnusLogger LOGGER = new CygnusLogger(HiveBackendImpl.class);
    private static final String DRIVERNAME1 = "org.apache.hadoop.hive.jdbc.HiveDriver";
    private static final String DRIVERNAME2 = "org.apache.hive.jdbc.HiveDriver";
    private final String hiveServerVersion;
    private final String hiveServer;
    private final String hivePort;
    private final String hadoopUser;
    private final String hadoopPassword;
    
    /**
     * Constructor.
     * @param hiveServerVersion
     * @param hiveServer
     * @param hivePort
     * @param hadoopUser
     * @param hadoopPassword
     */
    public HiveBackendImpl(String hiveServerVersion, String hiveServer, String hivePort, String hadoopUser,
            String hadoopPassword) {
        this.hiveServerVersion = hiveServerVersion;
        this.hiveServer = hiveServer;
        this.hivePort = hivePort;
        this.hadoopUser = hadoopUser;
        this.hadoopPassword = hadoopPassword;
    } // HiveBackendImpl
    
    @Override
    public boolean doCreateDatabase(String dbName) {
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
            stmt.execute("create database " + dbName);
            LOGGER.debug("Executing: 'create database " + dbName + "'");
        } catch (Throwable e) {
            LOGGER.error("Runtime error (The Hive database '" + dbName + "' cannot be created. Details="
                    + e.getMessage() + ")");
            res = false;
        } finally {
            return res && closeHiveObjects(con, stmt, rs);
        } // try catch finally
    } // doCreateDatabase
    
    @Override
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
            stmt.execute(query);
            LOGGER.debug("Executing: '" + query + "'");
        } catch (Throwable e) {
            LOGGER.error("Runtime error (The Hive table cannot be created. Hive query='" + query + "'. Details="
                    + e.getMessage() + ")");
            res = false;
        } finally {
            return res && closeHiveObjects(con, stmt, rs);
        } // try catch finally
    } // doCreateTable

    @Override
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
            LOGGER.debug("Executing: '" + query + "'");

            // iterate on the result
            while (rs.next()) {
                String s = "";

                for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
                    s += rs.getString(i) + ",";
                } // for
              
                s += rs.getString(rs.getMetaData().getColumnCount());
            } // while
        } catch (Throwable e) {
            LOGGER.error("Runtime error (The Hive query cannot be executed. Hive query='" + query + "'. Details="
                    + e.getMessage() + ")");
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
        // result
        boolean res = true;
        
        // the objects must be strictly closed in this order
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.error("Runtime error (The Hive result set could not be closed. Details=" + e.getMessage() + ")");
                res = false;
            } // try catch // try catch
        } // if
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.error("Runtime error (The Hive statement could not be closed. Details=" + e.getMessage() + ")");
                res = false;
            } // try catch // try catch
        } // if
        
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                LOGGER.error("Runtime error (The Hive connection could not be closed. Details=" + e.getMessage() + ")");
                res = false;
            } // try catch // try catch
        } // if
        
        return res;
    } // closeHiveObjects
    
    /**
     * Gets a connection to the Hive server.
     * @return
     * @throws Exception
     */
    private Connection getConnection() throws Exception {
        if (hiveServerVersion.equals("1")) {
            // dynamically load the Hive JDBC driver
            Class.forName(DRIVERNAME1);

            // return a connection based on the Hive JDBC driver
            LOGGER.debug("Connecting to jdbc:hive://" + hiveServer + ":" + hivePort + "/default?user=" + hadoopUser
                    + "&password=XXXXXXXXXX");
            return DriverManager.getConnection("jdbc:hive://" + hiveServer + ":" + hivePort + "/default?user="
                    + hadoopUser + "&password=" + hadoopPassword);
        } else if (hiveServerVersion.equals("2")) {
            // dynamically load the Hive JDBC driver
            Class.forName(DRIVERNAME2);

            // return a connection based on the Hive JDBC driver
            LOGGER.debug("Connecting to jdbc:hive2://" + hiveServer + ":" + hivePort + "/default?user=" + hadoopUser
                    + "&password=XXXXXXXXXX");
            return DriverManager.getConnection("jdbc:hive2://" + hiveServer + ":" + hivePort + "/default", hadoopUser,
                    hadoopPassword);
        } else {
            LOGGER.error("No version for Hive server was given, the connection to Hive could not be done");
            return null;
        } // if else if
    } // getConnection
    
} // HiveBackendImpl