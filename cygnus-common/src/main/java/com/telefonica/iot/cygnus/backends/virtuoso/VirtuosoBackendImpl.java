/**
 * Copyright 2025 Telefonica Espana
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

package com.telefonica.iot.cygnus.backends.virtuoso;

import com.google.gson.JsonElement;
import com.sun.rowset.CachedRowSetImpl;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import java.sql.*;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.Date;

import com.telefonica.iot.cygnus.backends.sql.SQLCache;



/**
 * The type Virtuoso backend.
 */
public class VirtuosoBackendImpl implements VirtuosoBackend{

    private static final CygnusLogger LOGGER = new CygnusLogger(VirtuosoBackendImpl.class);
    private VirtuosoBackendImpl.VirtuosoDriver driver;
    private final SQLCache cache;
    private String nlsTimestampFormat;
    private String nlsTimestampTzFormat;

    /**
     * Constructor.
     *
     * @param virtuosoHost
     * @param virtuosoPort
     * @param virtuosoUsername
     * @param virtuosoPassword
     * @param maxPoolSize
     * @param maxPoolIdle
     * @param minPoolIdle
     * @param minPoolIdleTimeMillis
     */
    public VirtuosoBackendImpl(String virtuosoHost, String virtuosoPort, String virtuosoUsername, String virtuosoPassword, int maxPoolSize, int maxPoolIdle, int minPoolIdle, int minPoolIdleTimeMillis) {
        this(virtuosoHost, virtuosoPort, virtuosoUsername, virtuosoPassword, maxPoolSize, maxPoolIdle, minPoolIdle, minPoolIdleTimeMillis, null);
    } // VirtuosoBackendImpl

    /**
     * Constructor.
     *
     * @param virtuosoHost
     * @param virtuosoPort
     * @param virtuosoUsername
     * @param virtuosoPassword
     * @param maxPoolSize
     * @param maxPoolIdle
     * @param minPoolIdle
     * @param minPoolIdleTimeMillis
     * @param virtuosoOptions
     */
    public VirtuosoBackendImpl(String virtuosoHost, String virtuosoPort, String virtuosoUsername, String virtuosoPassword, int maxPoolSize, int maxPoolIdle, int minPoolIdle, int minPoolIdleTimeMillis, String virtuosoOptions) {
        driver = new VirtuosoBackendImpl.VirtuosoDriver(virtuosoHost, virtuosoPort, virtuosoUsername, virtuosoPassword, maxPoolSize, maxPoolIdle, minPoolIdle, minPoolIdleTimeMillis, virtuosoOptions);
        cache = new SQLCache();
    } // VirtuosoBackendImpl

    /**
     * Releases resources
     */
    public void close(){
        if (driver != null) driver.close();
    } // close

    /**
     * Sets the SQL driver. It is protected since it is only used by the
     * tests.
     *
     * @param driver The SQL driver to be set.
     */
    public void setDriver(VirtuosoBackendImpl.VirtuosoDriver driver) {
        this.driver = driver;
    } // setDriver

    public VirtuosoBackendImpl.VirtuosoDriver getDriver() {
        return driver;
    } // getDriver


    /**
     * Set NLS_TIMESTAMP_FORMAT and NLS_TIMESTAMP_TZ_FORMAT
     *
     * @param format
     **/
    public void setNlsTimestampFormat(String format) {
        this.nlsTimestampFormat = format;
    } // setNlsTimestampFormat

    public String getNlsTimestampFormat() {
        return nlsTimestampFormat;
    } // getNlsTImestampFormat

    public void setNlsTimestampTzFormat(String format) {
        this.nlsTimestampTzFormat = format;
    } // setNlsTimestampTzFormat

    public String getNlsTimestampTzFormat() {
        return nlsTimestampTzFormat;
    } // getNlsTImestampTzFormat

    /**
     * Close all the SQL objects previously opened by doCreateTable and
     * doQuery.
     *
     * @param con
     * @param stmt
     * @return True if the SQL objects have been closed, false otherwise.
     */
    private void closeSQLObjects(Connection con, Statement stmt) throws CygnusRuntimeError {
        LOGGER.debug(" Closing SQL connection objects.");
        closeStatement(stmt);
        closeConnection(con);
    } // closeSQLObjects

    /**
     * Close SQL objects previously opened by doCreateTable and
     * doQuery.
     *
     * @param statement
     * @return True if the SQL objects have been closed, false otherwise.
     */

    private void closeStatement (Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOGGER.warn(" error closing invalid statement: " + e.getMessage());
            } // try catch
        } // if
    }

    /**
     * Close SQL objects previously opened by doCreateTable and
     * doQuery.
     *
     * @param connection
     * @return True if the SQL objects have been closed, false otherwise.
     */

    private void closeConnection (Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.warn(" error closing invalid connection: " + e.getMessage());
            } // try catch
        } // if
    }


    /**
     * Upsert transaction.
     *
     * @param aggregation     the aggregation
     * @param lastData        the last data
     * @param lastDataDelete  the last data delete
     * @param dataBase        the dataBase
     * @param tableName       the table name
     * @param tableSuffix     the table suffix
     * @param uniqueKey       the unique key
     * @param timestampKey    the timestamp key
     * @param timestampFormat the timestamp format
     * @param attrNativeTypes the attr native types
     * @throws CygnusPersistenceError the cygnus persistence error
     * @throws CygnusBadContextData   the cygnus bad context data
     * @throws CygnusRuntimeError     the cygnus runtime error
     * @throws CygnusPersistenceError the cygnus persistence error
     */
    public void upsertTransaction (LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                   LinkedHashMap<String, ArrayList<JsonElement>> lastData,
                                   LinkedHashMap<String, ArrayList<JsonElement>> lastDataDelete,
                                   String dataBase,
                                   String schema,
                                   String tableName,
                                   String tableSuffix,
                                   String uniqueKey,
                                   String timestampKey,
                                   String timestampFormat,
                                   boolean attrNativeTypes)
        throws CygnusPersistenceError, CygnusBadContextData, CygnusRuntimeError {

        Connection connection = null;
        String upsertQuerys = new String();
        String currentUpsertQuery = new String();

        try {

            connection = driver.getConnection(dataBase);
            connection.setAutoCommit(false);

            ArrayList<StringBuffer> upsertQuerysList = VirtuosoQueryUtils.virtuosoUpsertQuery(aggregation,
                    lastData,
                    lastDataDelete,
                    tableName,
                    tableSuffix,
                    uniqueKey,
                    timestampKey,
                    timestampFormat,
                    dataBase,
                    schema,
                    attrNativeTypes);
            
            // Ordering queries to avoid deadlocks. See issue #2197 for more detail
            upsertQuerysList.sort(Comparator.comparing(buff -> buff.toString()));

            for (StringBuffer query : upsertQuerysList) {
                PreparedStatement upsertStatement;
                currentUpsertQuery = query.toString();
                upsertStatement = connection.prepareStatement(currentUpsertQuery);
                // FIXME https://github.com/telefonicaid/fiware-cygnus/issues/1959
                upsertStatement.executeUpdate();
                upsertQuerys = upsertQuerys + " " + query;
            }

            connection.commit();
            LOGGER.info(" Finished transactions into database: " +
                        dataBase + " \n upsertQuerys: " + upsertQuerys);

        } catch (SQLTimeoutException e) {
            cygnusSQLRollback(connection);
            if (upsertQuerys.isEmpty() && currentUpsertQuery.isEmpty()) {
                throw new CygnusPersistenceError(" " + e.getNextException() +
                                                 " Data insertion error. database: " + dataBase +
                                                 " connection: " + connection,
                                                 " SQLTimeoutException", e.getMessage());
            } else {
                throw new CygnusPersistenceError(" " + e.getNextException() +
                                                 " Data insertion error. database: " + dataBase +
                                                 " upsertQuerys: " + upsertQuerys +
                                                 " currentUpsertQuery: " + currentUpsertQuery,
                                                 " SQLTimeoutException", e.getMessage());
            }
        } catch (SQLException e) {
            cygnusSQLRollback(connection);
            if (upsertQuerys.isEmpty() && currentUpsertQuery.isEmpty()) {
                throw new CygnusBadContextData(" " + e.getNextException() +
                                               " Data insertion error. database: " + dataBase +
                                               " connection: " + connection,
                                               " SQLException", e.getMessage());

            } else {
                String allQueries = " upsertQuerys: " + upsertQuerys +
                    " currentUpsertQuery: " + currentUpsertQuery;
                throw new CygnusBadContextData(" " + e.getNextException() +
                                               " Data insertion error. database: " + dataBase + allQueries,
                                               " SQLException", e.getMessage());
            }
        } finally {
            closeConnection(connection);
        } // try catch
        tableName = schema + "." + tableName;

        LOGGER.debug(" Trying to add '" + dataBase + "' and '" + tableName + "' to the cache after upsertion");
        cache.addDataBase(dataBase);
        cache.addTable(dataBase, tableName);
    }


    private void cygnusSQLRollback (Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            LOGGER.error("Error when rollingback transaction " + e.getMessage());
        }
    }


    public class VirtuosoDriver {

        private final HashMap<String, DataSource> datasources;
        private final HashMap<String, GenericObjectPool> pools;
        private final String virtuosoHost;
        private final String virtuosoPort;
        private final String virtuosoUsername;
        private final String virtuosoPassword;
        private final int maxPoolSize;
        private final int maxPoolIdle;
        private final int minPoolIdle;
        private final int minPoolIdleTimeMillis;
        private final String virtuosoOptions;

        /**
         * Constructor.
         *
         * @param virtuosoHost
         * @param virtuosoPort
         * @param virtuosoUsername
         * @param virtuosoPassword
         * @param maxPoolSize
         * @param maxPoolIdle
         * @param minPoolIdle
         * @param minPoolIdleTimeMillis
         * @param virtuosoOptions
         */
        public VirtuosoDriver(String virtuosoHost, String virtuosoPort, String virtuosoUsername, String virtuosoPassword, int maxPoolSize, int maxPoolIdle, int minPoolIdle, int minPoolIdleTimeMillis, String virtuosoOptions) {
            datasources = new HashMap<>();
            pools = new HashMap<>();
            this.virtuosoHost = virtuosoHost;
            this.virtuosoPort = virtuosoPort;
            this.virtuosoUsername = virtuosoUsername;
            this.virtuosoPassword = virtuosoPassword;
            this.maxPoolSize = maxPoolSize;
            this.maxPoolIdle = maxPoolIdle;
            this.minPoolIdle = minPoolIdle;
            this.minPoolIdleTimeMillis = minPoolIdleTimeMillis;
            this.virtuosoOptions = virtuosoOptions;
        } // VirtuosoDriver

        /**
         * Gets a connection to the SQL server.
         *
         * @param destination
         * @return
         * @throws CygnusRuntimeError
         * @throws CygnusPersistenceError
         */
        public Connection getConnection(String destination) throws CygnusRuntimeError, CygnusPersistenceError {
            try {
                // FIXME: the number of cached connections should be limited to
                // a certain number; with such a limit
                // number, if a new connection is needed, the oldest one is closed
                Connection connection = null;

                if (datasources.containsKey(destination)) {
                    connection = datasources.get(destination).getConnection();
                    LOGGER.debug(" Recovered destination connection from cache (" + destination + ")");
                }

                if (connection == null || !connection.isValid(0)) {
                    if (connection != null) {
                        LOGGER.debug(" Closing invalid sql connection for destination " + destination);
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            LOGGER.warn(" error closing invalid connection: " + e.getMessage());
                        }
                    } // if

                    DataSource datasource = createConnectionPool(destination);
                    datasources.put(destination, datasource);
                    connection = datasource.getConnection();
                } // if

                // Check Pool cache and log status
                if (pools.containsKey(destination)){
                    GenericObjectPool pool = pools.get(destination);
                    LOGGER.debug(" Pool status (" + destination + ") Max.: " + pool.getMaxActive() + "; Active: "
                            + pool.getNumActive() + "; Idle: " + pool.getNumIdle());
                }else{
                    LOGGER.error(" Can't find dabase in pool cache (" + destination + ")");
                }
                return connection;
            } catch (ClassNotFoundException e) {
                throw new CygnusRuntimeError(" Connection error", "ClassNotFoundException", e.getMessage());
            } catch (SQLException e) {
                throw new CygnusPersistenceError(" Connection error", "SQLException", e.getMessage());
            } catch (Exception e) {
                throw new CygnusRuntimeError(" Connection error creating new Pool", "Exception", e.getMessage());
            } // try catch
        } // getConnection

        /**
         * Gets if a connection is created for the given destination. It is
         * protected since it is only used in the tests.
         *
         * @param destination
         * @return True if the connection exists, false other wise
         */
        protected boolean isConnectionCreated(String destination) {
            return datasources.containsKey(destination);
        } // isConnectionCreated

        /**
         * Returns the actual number of active connections
         * @return
         */
        protected int activePoolConnections() {
            int connectionCount = 0;
            for ( String destination : pools.keySet()){
                GenericObjectPool pool = pools.get(destination);
                connectionCount += pool.getNumActive();
                LOGGER.debug(" Pool status (" + destination + ") Max.: " + pool.getMaxActive() + "; Active: "
                        + pool.getNumActive() + "; Idle: " + pool.getNumIdle());
            }
            LOGGER.debug(" Total pool's active connections: " + connectionCount);
            return connectionCount;
        } // activePoolConnections

        /**
         * Returns the Maximum number of connections
         * @return
         */
        protected int maxPoolConnections() {
            int connectionCount = 0;
            for ( String destination : pools.keySet()){
                GenericObjectPool pool = pools.get(destination);
                connectionCount += pool.getMaxActive();
                LOGGER.debug(" Pool status (" + destination + ") Max.: " + pool.getMaxActive() + "; Active: "
                        + pool.getNumActive() + "; Idle: " + pool.getNumIdle());
            }
            LOGGER.debug(" Max pool connections: " + connectionCount);
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
         * Create a connection pool for destination.
         *
         * @param destination
         * @return PoolingDataSource
         * @throws Exception
         */
        @SuppressWarnings("unused")
        private DataSource createConnectionPool(String destination) throws Exception {
            GenericObjectPool gPool = null;
            if (pools.containsKey(destination)){
                LOGGER.debug(" Pool recovered from Cache (" + destination + ")");
                gPool = pools.get(destination);
            }else{
                String jdbcUrl = generateJDBCUrl(destination);
                Class.forName("virtuoso.jdbc4.Driver");

                // Creates an Instance of GenericObjectPool That Holds Our Pool of Connections Object!
                gPool = new GenericObjectPool();
                // Tune from https://javadoc.io/static/commons-pool/commons-pool/1.6/org/apache/commons/pool/impl/GenericObjectPool.html
                // Sets the cap on the number of objects that can be allocated by the pool (checked out to clients, or idle awaiting checkout) at a given time.
                gPool.setMaxActive(this.maxPoolSize);
                // Sets the cap on the number of "idle" instances in the pool.
                gPool.setMaxIdle(this.maxPoolIdle);
                // Sets the minimum number of objects allowed in the pool before the evictor thread (if active) spawns new objects.
                gPool.setMinIdle(this.minPoolIdle);
                // Sets the minimum amount of time an object may sit idle in the pool before it is eligible for eviction by the idle object evictor (if any)
                gPool.setMinEvictableIdleTimeMillis(this.minPoolIdleTimeMillis);
                // Sets the number of milliseconds to sleep between runs of the idle object evictor thread
                gPool.setTimeBetweenEvictionRunsMillis(this.minPoolIdleTimeMillis*3);
                pools.put(destination, gPool);

                // Creates a ConnectionFactory Object Which Will Be Used by the Pool to Create the Connection Object!
                String sep = (virtuosoOptions != null && !virtuosoOptions.trim().isEmpty()) ? "&" : "?";
                String logJdbc = jdbcUrl + sep + "user=" + virtuosoUsername + "&password=XXXXXXXXXX";

                LOGGER.debug(" Creating connection pool jdbc: " + logJdbc);
                ConnectionFactory cf = new DriverManagerConnectionFactory(jdbcUrl, virtuosoUsername, virtuosoPassword);

                // Creates a PoolableConnectionFactory That Will Wraps the Connection Object Created by
                // the ConnectionFactory to Add Object Pooling Functionality!
                PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, gPool, null, null, false, true);
            } //else
            return new PoolingDataSource(gPool);
        } // createConnectionPool

        /**
         * Generate the JDBC Url. This method is portected since this is called from test class.
         *
         * @param destination
         * @return jdbcurl
         */
        protected String generateJDBCUrl(String destination) {
            String jdbcUrl = "";
            
            jdbcUrl = "jdbc:" + "virtuoso" + "://" + virtuosoHost + ":" + virtuosoPort + "/" + destination;

            if (virtuosoOptions != null && !virtuosoOptions.trim().isEmpty()) {
                jdbcUrl += "?" + virtuosoOptions;
            }

            return jdbcUrl;
        } // generateJDBCUrl

        /**
         * Closes the Driver releasing resources
         * @return
         */
        public void close() {
            int poolCount = 0;
            int poolsSize = pools.size();

            for ( String destination : pools.keySet()){
                GenericObjectPool pool = pools.get(destination);
                try {
                    pool.close();
                    pools.remove(destination);
                    poolCount ++;
                    LOGGER.debug(" Pool closed: (" + destination + ")");
                } catch (Exception e) {
                    LOGGER.error(" Error closing SQL pool " + destination +": " + e.getMessage());
                }
            }
            LOGGER.debug(" Number of Pools closed: " + poolCount + "/" + poolsSize);
        } // close

        /**
         * Last resort releasing resources
         */
        public void Finally(){
            this.close();
        }

    } // VirtuosoDriver
}
