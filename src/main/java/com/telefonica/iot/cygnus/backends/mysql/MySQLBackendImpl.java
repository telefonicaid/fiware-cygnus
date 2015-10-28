/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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

package com.telefonica.iot.cygnus.backends.mysql;

import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.HashMap;

/**
 *
 * @author frb
 * 
 * MySQL related operations (database and table creation, context data insertion) when dealing with a MySQL
 * persistence backend.
 */
public class MySQLBackendImpl implements MySQLBackend {
    
    private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";
    private MySQLDriver driver;
    private static final CygnusLogger LOGGER = new CygnusLogger(MySQLBackendImpl.class);
            
    /**
     * Constructor.
     * @param mysqlHost
     * @param mysqlPort
     * @param mysqlUsername
     * @param mysqlPassword
     */
    public MySQLBackendImpl(String mysqlHost, String mysqlPort, String mysqlUsername, String mysqlPassword) {
        driver = new MySQLDriver(mysqlHost, mysqlPort, mysqlUsername, mysqlPassword);
    } // MySQLBackendImpl
    
    /**
     * Sets the MySQL driver. It is protected since it is only used by the tests.
     * @param driver The MySQL driver to be set.
     */
    protected void setDriver(MySQLDriver driver) {
        this.driver = driver;
    } // setDriver
    
    protected MySQLDriver getDriver() {
        return driver;
    } // getDriver
    
    /**
     * Creates a database, given its name, if not exists.
     * @param dbName
     * @throws Exception
     */
    @Override
    public void createDatabase(String dbName) throws Exception {
        Statement stmt = null;
        
        // get a connection to an empty database
        Connection con = driver.getConnection("");
        
        try {
            stmt = con.createStatement();
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        try {
            String query = "create database if not exists `" + dbName + "`";
            LOGGER.debug("Executing MySQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        closeMySQLObjects(con, stmt);
    } // createDatabase
    
    /**
     * Creates a table, given its name, if not exists in the given database.
     * @param dbName
     * @param tableName
     * @throws Exception
     */
    @Override
    public void createTable(String dbName, String tableName, String typedFieldNames) throws Exception {
        Statement stmt = null;
        
        // get a connection to the given database
        Connection con = driver.getConnection(dbName);
        
        try {
            stmt = con.createStatement();
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        try {
            String query = "create table if not exists `" + tableName + "` " + typedFieldNames;
            LOGGER.debug("Executing MySQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        closeMySQLObjects(con, stmt);
    } // createTable
    
    @Override
    public void insertContextData(String dbName, String tableName, String fieldNames, String fieldValues)
        throws Exception {
        Statement stmt = null;
        
        // get a connection to the given database
        Connection con = driver.getConnection(dbName);
            
        try {
            stmt = con.createStatement();
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        try {
            String query = "insert into `" + tableName + "` " + fieldNames + " values " + fieldValues;
            LOGGER.debug("Executing MySQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } catch (SQLException e) {
            throw new CygnusBadContextData(e.getMessage());
        } // try catch
    } // insertContextData
    
    /**
     * Close all the MySQL objects previously opened by doCreateTable and doQuery.
     * @param con
     * @param stmt
     * @return True if the MySQL objects have been closed, false otherwise.
     */
    private void closeMySQLObjects(Connection con, Statement stmt) throws Exception {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                throw new CygnusRuntimeError("The Hive connection could not be closed. Details="
                        + e.getMessage());
            } // try catch
        } // if

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                throw new CygnusRuntimeError("The Hive statement could not be closed. Details="
                        + e.getMessage());
            } // try catch
        } // if
    } // closeMySQLObjects
    
    /**
     * This code has been extracted from MySQLBackendImpl.getConnection() for testing purposes. By extracting it into a
     * class then it can be mocked.
     */
    protected class MySQLDriver {
        
        private final HashMap<String, Connection> connections;
        private final String mysqlHost;
        private final String mysqlPort;
        private final String mysqlUsername;
        private final String mysqlPassword;
        
        /**
         * Constructor.
         * @param mysqlHost
         * @param mysqlPort
         * @param mysqlUsername
         * @param mysqlPassword
         */
        public MySQLDriver(String mysqlHost, String mysqlPort, String mysqlUsername, String mysqlPassword) {
            connections = new HashMap<String, Connection>();
            this.mysqlHost = mysqlHost;
            this.mysqlPort = mysqlPort;
            this.mysqlUsername = mysqlUsername;
            this.mysqlPassword = mysqlPassword;
        } // MySQLDriver
        
        /**
         * Gets a connection to the MySQL server.
         * @param dbName
         * @return
         * @throws Exception
         */
        public Connection getConnection(String dbName) throws Exception {
            try {
                // FIXME: the number of cached connections should be limited to a certain number; with such a limit
                //        number, if a new connection is needed, the oldest one is closed
                Connection con = connections.get(dbName);

                if (con == null || !con.isValid(0)) {
                    if (con != null) {
                        con.close();
                    } // if

                    con = createConnection(dbName);
                    connections.put(dbName, con);
                } // if

                return con;
            } catch (ClassNotFoundException e) {
                throw new CygnusPersistenceError(e.getMessage());
            } catch (SQLException e) {
                throw new CygnusPersistenceError(e.getMessage());
            } // try catch
        } // getConnection
        
        /**
         * Gets if a connection is created for the given database. It is protected since it is only used in the tests.
         * @param dbName
         * @return True if the connection exists, false other wise
         */
        protected boolean isConnectionCreated(String dbName) {
            return connections.containsKey(dbName);
        } // isConnectionCreated
        
        /**
         * Gets the number of connections created.
         * @return The number of connections created
         */
        protected int numConnectionsCreated() {
            return connections.size();
        } // numConnectionsCreated
        
        /**
         * Creates a MySQL connection.
         * @param host
         * @param port
         * @param dbName
         * @param user
         * @param password
         * @return A MySQL connection
         * @throws Exception
         */
        private Connection createConnection(String dbName)
            throws Exception {
            // dynamically load the MySQL JDBC driver
            Class.forName(DRIVER_NAME);

            // return a connection based on the MySQL JDBC driver
            LOGGER.debug("Connecting to jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + dbName + "?user="
                    + mysqlUsername + "&password=XXXXXXXXXX");
            return DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + dbName,
                    mysqlUsername, mysqlPassword);
        } // createConnection
        
    } // MySQLDriver
    
} // MySQLBackendImpl