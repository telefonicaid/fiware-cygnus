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

package com.telefonica.iot.cygnus.backends.postgresql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.sun.rowset.CachedRowSetImpl;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;

import java.sql.DriverManager;
import java.util.Properties;

/**
 *
 * @author hermanjunge
 *
 * PostgreSQL related operations (database and table creation, context data insertion) when dealing with a PostgreSQL
 * persistence backend.
 */
public class PostgreSQLBackendImpl implements PostgreSQLBackend {

    private static final String DRIVER_NAME = "org.postgresql.Driver";
    private PostgreSQLDriver driver;
    private static final CygnusLogger LOGGER = new CygnusLogger(PostgreSQLBackendImpl.class);
    private PostgreSQLCache cache = null;

    /**
     * Constructor.
     * @param postgresqlHost
     * @param postgresqlPort
     * @param postgresqlUsername
     * @param postgresqlPassword
     * @param enableCache
     */
    public PostgreSQLBackendImpl(String postgresqlHost, String postgresqlPort, String postgresqlDatabase, String postgresqlUsername, String postgresqlPassword, int maxPoolSize) {

        driver = new PostgreSQLDriver(postgresqlHost, postgresqlPort, postgresqlDatabase,
                                      postgresqlUsername, postgresqlPassword, maxPoolSize);
        cache = new PostgreSQLCache();
    } // PostgreSQLBackendImpl


    /**
     * Releases resources
     */
    public void close(){
        if (driver != null) driver.close();
    } // close

    /**
     * Sets the PostgreSQLDriver driver. It is protected since it is only used by the tests.
     * @param driver The PostgreSQLDriver driver to be set.
     */
    protected void setDriver(PostgreSQLDriver driver) {
        this.driver = driver;
    } // setDriver

    protected PostgreSQLDriver getDriver() {
        return driver;
    } // getDriver

    /**
     * Creates a schema given its name, if not exists.
     * @param schemaName
     * @throws CygnusRuntimeError, CygnusPersistenceError
     */
    @Override
    public void createSchema(String schemaName) throws CygnusRuntimeError, CygnusPersistenceError {
        if (cache.isSchemaInCache(schemaName)) {
            LOGGER.debug("'" + schemaName + "' is cached, thus it is not created");
            return;
        } //

        Statement stmt = null;

        // get a connection to an empty database
        Connection con = driver.getConnection("");

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closePostgreSQLObjects(con, stmt);
            throw new CygnusRuntimeError("Schema creation error", "SQLException", e.getMessage());
        } // try catch

        try {
            String query = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
            LOGGER.debug("Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            closePostgreSQLObjects(con, stmt);
            throw new CygnusPersistenceError("Schema creation error", "SQLException", e.getMessage());
        } // try catch

        closePostgreSQLObjects(con, stmt);
        LOGGER.debug("Trying to add '" + schemaName + "' to the cache after database creation");
        cache.persistSchemaInCache(schemaName);
    } // createSchema

    /**
     * Creates a table, given its name, if not exists in the given schema.
     * @param schemaName
     * @param tableName
     * @param typedFieldNames
     * @throws Exception
     */
    @Override
    public void createTable(String schemaName, String tableName, String typedFieldNames) throws CygnusRuntimeError, CygnusPersistenceError {
        if (cache.isTableInCachedSchema(schemaName, tableName)) {
            LOGGER.debug("'" + tableName + "' is cached, thus it is not created");
            return;
        } // if
        
        Statement stmt = null;

        // get a connection to the given schema
        Connection con = driver.getConnection(schemaName);

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closePostgreSQLObjects(con, stmt);
            throw new CygnusRuntimeError("Table creation error", "SQLException", e.getMessage());
        } // try catch

        try {
            String query = "CREATE TABLE IF NOT EXISTS " + schemaName + "." + tableName + " " + typedFieldNames;
            LOGGER.debug("Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            closePostgreSQLObjects(con, stmt);
            throw new CygnusPersistenceError("Table creation error", "SQLException", e.getMessage());
        } // try catch

        closePostgreSQLObjects(con, stmt);
        LOGGER.debug("Trying to add '" + tableName + "' to the cache after table creation");
        cache.persistTableInCache(schemaName, tableName);
    } // createTable

    @Override
    public void insertContextData(String schemaName, String tableName, String fieldNames, String fieldValues)
        throws CygnusBadContextData, CygnusRuntimeError, CygnusPersistenceError {
        Statement stmt = null;

        // get a connection to the given database
        Connection con = driver.getConnection(schemaName);

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closePostgreSQLObjects(con, stmt);
            throw new CygnusRuntimeError("Data insertion error", "SQLException", e.getMessage());
        } // try catch

        try {
            String query = "INSERT INTO " + schemaName + "." + tableName + " " + fieldNames + " VALUES " + fieldValues;
            LOGGER.debug("Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError("Data insertion error", "SQLTimeoutException", e.getMessage());
        } catch (SQLException e) {
            throw new CygnusBadContextData("Data insertion error", "SQLException", e.getMessage());
        } finally {
            closePostgreSQLObjects(con, stmt);
        } // try catch
        LOGGER.debug("Trying to add '" + schemaName + "' and '" + tableName + "' to the cache after insertion");
        cache.persistSchemaInCache(schemaName);
        cache.persistTableInCache(schemaName, tableName);
    } // insertContextData

    /**
     * Close all the PostgreSQL objects previously opened by createSchema and createTable.
     * @param con
     * @param stmt
     * @return True if the PostgreSQL objects have been closed, false otherwise.
     */
    private void closePostgreSQLObjects(Connection con, Statement stmt) throws CygnusRuntimeError {
        LOGGER.debug("Closing PostgreSQL connection objects.");
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                throw new CygnusRuntimeError("Objects closing error", "SQLException", e.getMessage());
            } // try catch
        } // if

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                throw new CygnusRuntimeError("Objects closing error", "SQLException", e.getMessage());
            } // try catch
        } // if

    } // closePostgreSQLObjects

    /**
     * Driver class.
     */
    protected class PostgreSQLDriver {

        private final HashMap<String, DataSource> datasources;
        private final HashMap<String, GenericObjectPool> pools;
        private final String postgresqlHost;
        private final String postgresqlPort;
        private final String postgresqlDatabase;
        private final String postgresqlUsername;
        private final String postgresqlPassword;
        private final int maxPoolSize;

        /**
         * Constructor.
         * @param postgresqlHost
         * @param postgresqlPort
         * @param postgresqlDatabase
         * @param postgresqlUsername
         * @param postgresqlPassword
         */
        public PostgreSQLDriver(String postgresqlHost, String postgresqlPort, String postgresqlDatabase, String postgresqlUsername, String postgresqlPassword, int maxPoolSize) {
            datasources = new HashMap<>();
            pools = new HashMap<>();
            this.postgresqlHost = postgresqlHost;
            this.postgresqlPort = postgresqlPort;
            this.postgresqlUsername = postgresqlUsername;
            this.postgresqlPassword = postgresqlPassword;
            this.postgresqlDatabase = postgresqlDatabase;
            this.maxPoolSize = maxPoolSize;
        } // PostgreSQLDriver

        /**
         * Gets a connection to the PostgreSQL server.
         * @param schemaName
         * @return
         * @throws Exception
         */
        public Connection getConnection(String schemaName) throws CygnusRuntimeError, CygnusPersistenceError {
            try {
                // FIXME: the number of cached connections should be limited to a certain number; with such a limit
                //        number, if a new connection is needed, the oldest one is closed
                Connection connection = null;
                if (datasources.containsKey(schemaName)) {
                    connection = datasources.get(schemaName).getConnection();
                    LOGGER.debug("Recovered database connection from cache (" + schemaName + ")");
                }

                if (connection == null || !connection.isValid(0)) {
                    if (connection != null) {
                        LOGGER.debug("Closing invalid postgresql connection for db " + schemaName);
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            LOGGER.warn("error closing invalid connection: " + e.getMessage());
                        }
                    } // if

                    DataSource datasource = createConnectionPool(schemaName);
                    datasources.put(schemaName, datasource);
                    connection = datasource.getConnection();
                } // if

                // Check Pool cache and log status
                if (pools.containsKey(schemaName)) {
                    GenericObjectPool pool = pools.get(schemaName);
                    LOGGER.debug("Pool status (" + schemaName + ") Max.: " + pool.getMaxActive() + "; Active: " + pool.getNumActive() + "; Idle: " + pool.getNumIdle());
                } else {
                    LOGGER.error("Can't find dabase in pool cache (" + schemaName + ")");
                }
                return connection;
            } catch (ClassNotFoundException e) {
                throw new CygnusPersistenceError("Connection error", "ClassNotFoundException", e.getMessage());
            } catch (SQLException e) {
                throw new CygnusPersistenceError("Connection error", "SQLException", e.getMessage());
            } catch (Exception e) {
                throw new CygnusRuntimeError("Connection error creating new Pool", "Exception", e.getMessage());
            } // try catch
        } // getConnection

        /**
         * Gets if a connection is created for the given schema. It is protected since it is only used in the tests.
         * @param schemaName
         * @return True if the connection exists, false otherwise
         */
        protected boolean isConnectionCreated(String schemaName) {
            return datasources.containsKey(schemaName);
        } // isConnectionCreated

        /**
         * Returns the actual number of active connections
         * @return
         */
        protected int activePoolConnections() {
            int connectionCount = 0;
            for ( String schemaName : pools.keySet()){
                GenericObjectPool pool = pools.get(schemaName);
                connectionCount += pool.getNumActive();
                LOGGER.debug("Pool status (" + schemaName + ") Max.: " + pool.getMaxActive() + "; Active: " + pool.getNumActive() + "; Idle: " + pool.getNumIdle());
            }
            LOGGER.debug("Total pool's active connections: " + connectionCount);
            return connectionCount;
        } // activePoolConnections

        /**
         * Returns the Maximum number of connections
         * @return
         */
        protected int maxPoolConnections() {
            int connectionCount = 0;
            for ( String schemaName : pools.keySet()){
                GenericObjectPool pool = pools.get(schemaName);
                connectionCount += pool.getMaxActive();
                LOGGER.debug("Pool status (" + schemaName + ") Max.: " + pool.getMaxActive() + "; Active: " + pool.getNumActive() + "; Idle: " + pool.getNumIdle());
            }
            LOGGER.debug("Max pool connections: " + connectionCount);
            return connectionCount;
        } // maxPoolConnections

        /**
         * Gets the number of connections created.
         * @return The number of connections created
         */
        protected int numConnectionsCreated() {
            return activePoolConnections();
        } // numConnectionsCreated

        /**
         * Creates a connection pool for SchemaName
         * @param schemaName
         * @return PollingDataSource
         * @throws Exception
         */
        @SuppressWarnings("unused")
        private DataSource createConnectionPool(String schemaName)
            throws Exception {
            GenericObjectPool gPool = null;

            if (pools.containsKey(schemaName)){
                LOGGER.debug("Pool recovered from Cache (" + schemaName + ")");
                gPool = pools.get(schemaName);
            } else {
                String jdbcUrl = "jdbc:postgresql://" + this.postgresqlHost + ":" + this.postgresqlPort
                    + "/" + this.postgresqlDatabase;
                Class.forName(DRIVER_NAME);

                // Creates an Instance of GenericObjectPool That Holds Our Pool of Connections Object!
                gPool = new GenericObjectPool();
                gPool.setMaxActive(this.maxPoolSize);
                pools.put(schemaName, gPool);

                // Creates a ConnectionFactory Object Which Will Be Used by the Pool to Create the Connection Object!
                LOGGER.debug("Creating connection pool jdbc:postgresql://" + this.postgresqlHost + ":" + this.postgresqlPort + "/" + schemaName
                        + "?user=" + this.postgresqlUsername + "&password=XXXXXXXXXX");
                ConnectionFactory cf = new DriverManagerConnectionFactory(jdbcUrl, this.postgresqlUsername, this.postgresqlPassword);

                // Creates a PoolableConnectionFactory That Will Wraps the Connection Object Created by
                // the ConnectionFactory to Add Object Pooling Functionality!
                PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, gPool, null, null, false, true);
            } //else

            return new PoolingDataSource(gPool);
        } // createConnectionPool

        /**
         * Closes the Driver releasing resources
         * @return
         */
        public void close() {
            int poolCount = 0;
            int poolsSize = pools.size();

            for ( String schemaName : pools.keySet()){
                GenericObjectPool pool = pools.get(schemaName);
                try {
                    pool.close();
                    pools.remove(schemaName);
                    poolCount ++;
                    LOGGER.debug("Pool closed: (" + schemaName + ")");
                } catch (Exception e) {
                    LOGGER.error("Error closing PostgreSQL pool " + schemaName +": " + e.getMessage());
                }
            }
            LOGGER.debug("Number of Pools closed: " + poolCount + "/" + poolsSize);
        } // close

        /**
         * Last resort releasing resources
         */
        public void Finally(){
            this.close();
        }
    } // PostgreSQLDriver

} // PostgreSQLBackendImpl
