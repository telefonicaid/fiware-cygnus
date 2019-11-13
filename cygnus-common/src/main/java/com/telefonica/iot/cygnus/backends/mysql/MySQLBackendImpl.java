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

package com.telefonica.iot.cygnus.backends.mysql;

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
import com.telefonica.iot.cygnus.utils.CommonUtils;

/**
 *
 * @author frb
 * 
 *         MySQL related operations (database and table creation, context data
 *         insertion) when dealing with a MySQL persistence backend.
 */
public class MySQLBackendImpl implements MySQLBackend {

    private static final CygnusLogger LOGGER = new CygnusLogger(MySQLBackendImpl.class);
    private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";
    private MySQLDriver driver;  // FIXME: Change it to static, to allow balanced sinks to share Driver and connection pooling. Be aware of concurrency issues.
    private final MySQLCache cache;

    /**
     * Constructor.
     * 
     * @param mysqlHost
     * @param mysqlPort
     * @param mysqlUsername
     * @param mysqlPassword
     */
    public MySQLBackendImpl(String mysqlHost, String mysqlPort, String mysqlUsername, String mysqlPassword, int maxPoolSize) {
        driver = new MySQLDriver(mysqlHost, mysqlPort, mysqlUsername, mysqlPassword, maxPoolSize);
        cache = new MySQLCache();
    } // MySQLBackendImpl
    
    /**
     * Releases resources
     */
    public void close(){
        if (driver != null) driver.close();
    } // close

    /**
     * Sets the MySQL driver. It is protected since it is only used by the
     * tests.
     * 
     * @param driver
     *            The MySQL driver to be set.
     */
    protected void setDriver(MySQLDriver driver) {
        this.driver = driver;
    } // setDriver

    protected MySQLDriver getDriver() {
        return driver;
    } // getDriver

    @Override
    public void createDatabase(String dbName) throws CygnusRuntimeError, CygnusPersistenceError {
        if (cache.isCachedDb(dbName)) {
            LOGGER.debug("'" + dbName + "' is cached, thus it is not created");
            return;
        } // if

        Statement stmt = null;

        // get a connection to an empty database
        Connection con = driver.getConnection("");

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeMySQLObjects(con, stmt);
            throw new CygnusRuntimeError("Database creation error", "SQLException", e.getMessage());
        } // try catch

        try {
            String query = "create database if not exists `" + dbName + "`";
            LOGGER.debug("Executing MySQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            closeMySQLObjects(con, stmt);
            throw new CygnusPersistenceError("Database creation error", "SQLException", e.getMessage());
        } // try catch

        closeMySQLObjects(con, stmt);

        LOGGER.debug("Trying to add '" + dbName + "' to the cache after database creation");
        cache.addDb(dbName);
    } // createDatabase

    @Override
    public void createTable(String dbName, String tableName, String typedFieldNames)
            throws CygnusRuntimeError, CygnusPersistenceError {
        if (cache.isCachedTable(dbName, tableName)) {
            LOGGER.debug("'" + tableName + "' is cached, thus it is not created");
            return;
        } // if

        Statement stmt = null;

        // get a connection to the given database
        Connection con = driver.getConnection(dbName);

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeMySQLObjects(con, stmt);
            throw new CygnusRuntimeError("Table creation error", "SQLException", e.getMessage());
        } // try catch

        try {
            String query = "create table if not exists `" + tableName + "`" + typedFieldNames;
            LOGGER.debug("Executing MySQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            closeMySQLObjects(con, stmt);
            throw new CygnusPersistenceError("Table creation error", "SQLException", e.getMessage());
        } // try catch

        closeMySQLObjects(con, stmt);

        LOGGER.debug("Trying to add '" + tableName + "' to the cache after table creation");
        cache.addTable(dbName, tableName);
    } // createTable

    @Override
    public void insertContextData(String dbName, String tableName, String fieldNames, String fieldValues)
            throws CygnusBadContextData, CygnusRuntimeError, CygnusPersistenceError {
        Statement stmt = null;

        // get a connection to the given database
        Connection con = driver.getConnection(dbName);

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeMySQLObjects(con, stmt);
            throw new CygnusRuntimeError("Data insertion error", "SQLException", e.getMessage());
        } // try catch

        try {
            String query = "insert into `" + tableName + "` " + fieldNames + " values " + fieldValues;
            LOGGER.debug("Executing MySQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError("Data insertion error", "SQLTimeoutException", e.getMessage());
        } catch (SQLException e) {
            throw new CygnusBadContextData("Data insertion error", "SQLException", e.getMessage());
        } finally {
            closeMySQLObjects(con, stmt);
        } // try catch

        LOGGER.debug("Trying to add '" + dbName + "' and '" + tableName + "' to the cache after insertion");
        cache.addDb(dbName);
        cache.addTable(dbName, tableName);
    } // insertContextData

    private CachedRowSet select(String dbName, String tableName, String selection)
            throws CygnusRuntimeError, CygnusPersistenceError {
        Statement stmt = null;

        // get a connection to the given database
        Connection con = driver.getConnection(dbName);

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeMySQLObjects(con, stmt);
            throw new CygnusRuntimeError("Querying error", "SQLException", e.getMessage());
        } // try catch

        try {
            // to-do: refactor after implementing
            // https://github.com/telefonicaid/fiware-cygnus/issues/1371
            String query = "select " + selection + " from `" + tableName + "` order by recvTime";
            LOGGER.debug("Executing MySQL query '" + query + "'");
            ResultSet rs = stmt.executeQuery(query);
            // A CachedRowSet is "disconnected" from the source, thus can be
            // used once the statement is closed
            @SuppressWarnings("restriction")
            CachedRowSet crs = new CachedRowSetImpl();
            
            crs.populate(rs); // FIXME: close Resultset Objects??
            closeMySQLObjects(con, stmt);
            return crs;
        } catch (SQLException e) {
            closeMySQLObjects(con, stmt);
            throw new CygnusPersistenceError("Querying error", "SQLException", e.getMessage());
        } // try catch
    } // select

    private void delete(String dbName, String tableName, String filters)
            throws CygnusRuntimeError, CygnusPersistenceError {
        Statement stmt = null;

        // get a connection to the given database
        Connection con = driver.getConnection(dbName);

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeMySQLObjects(con, stmt);
            throw new CygnusRuntimeError("Deleting error", "SQLException", e.getMessage());
        } // try catch

        try {
            String query = "delete from `" + tableName + "` where " + filters;
            LOGGER.debug("Executing MySQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            closeMySQLObjects(con, stmt);
            throw new CygnusPersistenceError("Deleting error", "SQLException", e.getMessage());
        } // try catch

        closeMySQLObjects(con, stmt);
    } // delete

    @Override
    public void capRecords(String dbName, String tableName, long maxRecords)
            throws CygnusRuntimeError, CygnusPersistenceError {
        // Get the records within the table
        CachedRowSet records = select(dbName, tableName, "*");

        // Get the number of records
        int numRecords = 0;

        try {
            if (records.last()) {
                numRecords = records.getRow();
                records.beforeFirst();
            } // if
        } catch (SQLException e) {
            throw new CygnusRuntimeError("Data capping error", "SQLException", e.getMessage());
        } // try catch

        // Get the reception times (they work as IDs) for future deletion
        // to-do: refactor after implementing
        // https://github.com/telefonicaid/fiware-cygnus/issues/1371
        String filters = "";

        try {
            if (numRecords > maxRecords) {
                for (int i = 0; i < (numRecords - maxRecords); i++) {
                    records.next();
                    String recvTime = records.getString("recvTime");

                    if (filters.isEmpty()) {
                        filters += "recvTime='" + recvTime + "'";
                    } else {
                        filters += " or recvTime='" + recvTime + "'";
                    } // if else
                } // for
            } // if

            records.close();
        } catch (SQLException e) {
            throw new CygnusRuntimeError("Data capping error", "SQLException", e.getMessage());
        } // try catch

        if (filters.isEmpty()) {
            LOGGER.debug("No records to be deleted");
        } else {
            LOGGER.debug("Records must be deleted (dbName=" + dbName + ",tableName=" + tableName + ", filters="
                    + filters + ")");
            delete(dbName, tableName, filters);
        } // if else
    } // capRecords

    @Override
    public void expirateRecordsCache(long expirationTime) throws CygnusRuntimeError, CygnusPersistenceError {
        // Iterate on the cached resource IDs
        cache.startDbIterator();

        while (cache.hasNextDb()) {
            String dbName = cache.nextDb();
            cache.startTableIterator(dbName);

            while (cache.hasNextTable(dbName)) {
                String tableName = cache.nextTable(dbName);

                // Get the records within the table
                CachedRowSet records = select(dbName, tableName, "*");

                // Get the number of records
                int numRecords = 0;

                try {
                    if (records.last()) {
                        numRecords = records.getRow();
                        records.beforeFirst();
                    } // if
                } catch (SQLException e) {
                    try {
                        records.close();
                    } catch (SQLException e1) {
                        LOGGER.debug("Can't close CachedRowSet.");
                    }
                    throw new CygnusRuntimeError("Data expiration error", "SQLException", e.getMessage());
                } // try catch

                // Get the reception times (they work as IDs) for future
                // deletion
                // to-do: refactor after implementing
                // https://github.com/telefonicaid/fiware-cygnus/issues/1371
                String filters = "";

                try {
                    for (int i = 0; i < numRecords; i++) {
                        records.next();
                        String recvTime = records.getString("recvTime");
                        long recordTime = CommonUtils.getMilliseconds(recvTime);
                        long currentTime = new Date().getTime();

                        if (recordTime < (currentTime - (expirationTime * 1000))) {
                            if (filters.isEmpty()) {
                                filters += "recvTime='" + recvTime + "'";
                            } else {
                                filters += " or recvTime='" + recvTime + "'";
                            } // if else
                        } else {
                            break;
                        } // if else
                    } // for
                } catch (SQLException e) {
                    throw new CygnusRuntimeError("Data expiration error", "SQLException", e.getMessage());
                } catch (ParseException e) {
                    throw new CygnusRuntimeError("Data expiration error", "ParseException", e.getMessage());
                } // try catch

                if (filters.isEmpty()) {
                    LOGGER.debug("No records to be deleted");
                } else {
                    LOGGER.debug("Records must be deleted (dbName=" + dbName + ",tableName=" + tableName + ", filters="
                            + filters + ")");
                    delete(dbName, tableName, filters);
                } // if else
            } // while
        } // while
    } // expirateRecordsCache

    /**
     * Close all the MySQL objects previously opened by doCreateTable and
     * doQuery.
     * 
     * @param con
     * @param stmt
     * @return True if the MySQL objects have been closed, false otherwise.
     */
    private void closeMySQLObjects(Connection con, Statement stmt) throws CygnusRuntimeError {
        LOGGER.debug("Closing MySQL connection objects.");
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

    } // closeMySQLObjects

    /**
     * This code has been extracted from MySQLBackendImpl.getConnection() for
     * testing purposes. By extracting it into a class then it can be mocked.
     */
    protected class MySQLDriver {

        private final HashMap<String, DataSource> datasources;
        private final HashMap<String, GenericObjectPool> pools;
        private final String mysqlHost;
        private final String mysqlPort;
        private final String mysqlUsername;
        private final String mysqlPassword;
        private final int maxPoolSize;

        /**
         * Constructor.
         * 
         * @param mysqlHost
         * @param mysqlPort
         * @param mysqlUsername
         * @param mysqlPassword
         */
        public MySQLDriver(String mysqlHost, String mysqlPort, String mysqlUsername, String mysqlPassword, int maxPoolSize) {
            datasources = new HashMap<>();
            pools = new HashMap<>();
            this.mysqlHost = mysqlHost;
            this.mysqlPort = mysqlPort;
            this.mysqlUsername = mysqlUsername;
            this.mysqlPassword = mysqlPassword;
            this.maxPoolSize = maxPoolSize;
        } // MySQLDriver

        /**
         * Gets a connection to the MySQL server.
         * 
         * @param dbName
         * @return
         * @throws CygnusRuntimeError
         * @throws CygnusPersistenceError
         */
        public Connection getConnection(String dbName) throws CygnusRuntimeError, CygnusPersistenceError {
            try {
                // FIXME: the number of cached connections should be limited to
                // a certain number; with such a limit
                // number, if a new connection is needed, the oldest one is closed
                Connection connection = null;

                if (datasources.containsKey(dbName)) {
                    connection = datasources.get(dbName).getConnection();
                    LOGGER.debug("Recovered database connection from cache (" + dbName + ")");
                }

                if (connection == null || !connection.isValid(0)) {
                    if (connection != null) {
                        LOGGER.debug("Closing invalid mysql connection for db " + dbName);
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            LOGGER.warn("error closing invalid connection: " + e.getMessage());
                        }
                    } // if

                    DataSource datasource = createConnectionPool(dbName);
                    datasources.put(dbName, datasource);
                    connection = datasource.getConnection();
                } // if
                
                // Check Pool cache and log status
                if (pools.containsKey(dbName)){
                    GenericObjectPool pool = pools.get(dbName);
                    LOGGER.debug("Pool status (" + dbName + ") Max.: " + pool.getMaxActive() + "; Active: " 
                            + pool.getNumActive() + "; Idle: " + pool.getNumIdle());
                }else{
                    LOGGER.error("Can't find dabase in pool cache (" + dbName + ")");
                }
                
                return connection;
            } catch (ClassNotFoundException e) {
                throw new CygnusRuntimeError("Connection error", "ClassNotFoundException", e.getMessage());
            } catch (SQLException e) {
                throw new CygnusPersistenceError("Connection error", "SQLException", e.getMessage());
            } catch (Exception e) {
                throw new CygnusRuntimeError("Connection error creating new Pool", "Exception", e.getMessage());
            } // try catch
        } // getConnection

        /**
         * Gets if a connection is created for the given database. It is
         * protected since it is only used in the tests.
         * 
         * @param dbName
         * @return True if the connection exists, false other wise
         */
        protected boolean isConnectionCreated(String dbName) {
            return datasources.containsKey(dbName);
        } // isConnectionCreated

        /**
         * Returns the actual number of active connections 
         * @return
         */
        protected int activePoolConnections() {
            int connectionCount = 0;
            for ( String dbName : pools.keySet()){
                GenericObjectPool pool = pools.get(dbName);
                connectionCount += pool.getNumActive();
                LOGGER.debug("Pool status (" + dbName + ") Max.: " + pool.getMaxActive() + "; Active: " 
                        + pool.getNumActive() + "; Idle: " + pool.getNumIdle());
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
            for ( String dbName : pools.keySet()){
                GenericObjectPool pool = pools.get(dbName);
                connectionCount += pool.getMaxActive();
                LOGGER.debug("Pool status (" + dbName + ") Max.: " + pool.getMaxActive() + "; Active: " 
                        + pool.getNumActive() + "; Idle: " + pool.getNumIdle());
            }
            LOGGER.debug("Max pool connections: " + connectionCount);
            return connectionCount;
        } // maxPoolConnections
        
        /**
         * Gets the number of connections created.
         * 
         * @return The number of connections created
         */
        protected int numConnectionsCreated() {
            return activePoolConnections();
        } // numConnectionsCreated

        /**
         * Create a connection pool for dbName.
         * 
         * @param dbName
         * @return PoolingDataSource
         * @throws Exception
         */
        @SuppressWarnings("unused")
        private DataSource createConnectionPool(String dbName) throws Exception {
            GenericObjectPool gPool = null;
            if (pools.containsKey(dbName)){
                LOGGER.debug("Pool recovered from Cache (" + dbName + ")");
                gPool = pools.get(dbName);
            }else{
                String jdbcUrl = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + dbName;
                Class.forName(DRIVER_NAME);
    
                // Creates an Instance of GenericObjectPool That Holds Our Pool of Connections Object!
                gPool = new GenericObjectPool();
                gPool.setMaxActive(this.maxPoolSize);
                pools.put(dbName, gPool);
    
                // Creates a ConnectionFactory Object Which Will Be Used by the Pool to Create the Connection Object!
                LOGGER.debug("Creating connection pool jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + dbName
                        + "?user=" + mysqlUsername + "&password=XXXXXXXXXX");
                ConnectionFactory cf = new DriverManagerConnectionFactory(jdbcUrl, mysqlUsername, mysqlPassword);
    
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
                        
            for ( String dbName : pools.keySet()){
                GenericObjectPool pool = pools.get(dbName);
                try {
                    pool.close();
                    pools.remove(dbName);
                    poolCount ++;
                    LOGGER.debug("Pool closed: (" + dbName + ")");
                } catch (Exception e) {
                    LOGGER.error("Error closing MySQL pool " + dbName +": " + e.getMessage());
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

    } // MySQLDriver

} // MySQLBackendImpl
