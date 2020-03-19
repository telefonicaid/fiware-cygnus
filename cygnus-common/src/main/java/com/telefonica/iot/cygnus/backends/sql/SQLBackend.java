package com.telefonica.iot.cygnus.backends.sql;

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
import java.util.Date;
import java.util.HashMap;

public class SQLBackend {

    private static final CygnusLogger LOGGER = new CygnusLogger(SQLBackend.class);
    private SQLBackend.SQLDriver driver;
    private final SQLCache cache;
    private String sqlInstance;

    /**
     * Constructor.
     *
     * @param sqlHost
     * @param sqlPort
     * @param sqlUsername
     * @param sqlPassword
     */
    public SQLBackend(String sqlHost, String sqlPort, String sqlUsername, String sqlPassword, int maxPoolSize, String sqlInstance, String sqlDriverName) {
        driver = new SQLBackend.SQLDriver(sqlHost, sqlPort, sqlUsername, sqlPassword, maxPoolSize, sqlInstance, sqlDriverName);
        cache = new SQLCache();
        this.sqlInstance = sqlInstance;
    } // SQLBackendImpl

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
    public void setDriver(SQLBackend.SQLDriver driver) {
        this.driver = driver;
    } // setDriver

    public SQLBackend.SQLDriver getDriver() {
        return driver;
    } // getDriver

    public void createDatabase(String dbName) throws CygnusRuntimeError, CygnusPersistenceError {
        if (cache.isCachedDb(dbName)) {
            LOGGER.debug("'" + dbName + "' is cached, thus it is not created");
            return;
        } // if

        Statement stmt = null;

        // get a connection to an empty database
        Connection con = driver.getConnection("");

        String query = "";
        if (sqlInstance.equals("mysql")) {
            query = "create database if not exists `" + dbName + "`";
        } else {
            query = "CREATE SCHEMA IF NOT EXISTS " + dbName;
        }

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusRuntimeError("Database creation error", "SQLException", e.getMessage());
        } // try catch

        try {
            LOGGER.debug("Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusPersistenceError("Database creation error", "SQLException", e.getMessage());
        } // try catch

        closeSQLObjects(con, stmt);

        LOGGER.debug("Trying to add '" + dbName + "' to the cache after database creation");
        cache.addDb(dbName);
    } // createDatabase

    public void createTable(String dbName, String tableName, String typedFieldNames)
            throws CygnusRuntimeError, CygnusPersistenceError {
        if (cache.isCachedTable(dbName, tableName)) {
            LOGGER.debug("'" + tableName + "' is cached, thus it is not created");
            return;
        } // if

        Statement stmt = null;

        // get a connection to the given database
        Connection con = driver.getConnection(dbName);
        String query = "";
        if (sqlInstance.equals("mysql")) {
            query = "create table if not exists `" + tableName + "`" + typedFieldNames;
        } else {
            query = "CREATE TABLE IF NOT EXISTS " + dbName + "." + tableName + " " + typedFieldNames;
        }

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusRuntimeError("Table creation error", "SQLException", e.getMessage());
        } // try catch

        try {
            LOGGER.debug("Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError("Table creation error. Query " + query, "SQLTimeoutException", e.getMessage());
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            persistError(dbName, query, e);
            throw new CygnusPersistenceError("Table creation error", "SQLException", e.getMessage());
        } // try catch

        closeSQLObjects(con, stmt);

        LOGGER.debug("Trying to add '" + tableName + "' to the cache after table creation");
        cache.addTable(dbName, tableName);
    } // createTable

    public void insertContextData(String dbName, String tableName, String fieldNames, String fieldValues)
            throws CygnusBadContextData, CygnusRuntimeError, CygnusPersistenceError {
        Statement stmt = null;

        // get a connection to the given database
        Connection con = driver.getConnection(dbName);
        String query = "";
        if (sqlInstance.equals("mysql")) {
            query = "insert into `" + tableName + "` " + fieldNames + " values " + fieldValues;
        } else {
            query = "INSERT INTO " + dbName + "." + tableName + " " + fieldNames + " VALUES " + fieldValues;
        }

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusRuntimeError("Data insertion error", "SQLException", e.getMessage());
        } // try catch

        try {
            LOGGER.debug("Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError("Data insertion error. Query insert into `" + tableName + "` " + fieldNames + " values " + fieldValues, "SQLTimeoutException", e.getMessage());
        } catch (SQLException e) {
            persistError(dbName, query, e);
            throw new CygnusBadContextData("Data insertion error. Query: insert into `" + tableName + "` " + fieldNames + " values " + fieldValues, "SQLException", e.getMessage());
        } finally {
            closeSQLObjects(con, stmt);
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
        String query = "select " + selection + " from `" + tableName + "` order by recvTime";

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusRuntimeError("Querying error", "SQLException", e.getMessage());
        } // try catch

        try {
            // to-do: refactor after implementing
            // https://github.com/telefonicaid/fiware-cygnus/issues/1371
            LOGGER.debug("Executing SQL query '" + query + "'");
            ResultSet rs = stmt.executeQuery(query);
            // A CachedRowSet is "disconnected" from the source, thus can be
            // used once the statement is closed
            @SuppressWarnings("restriction")
            CachedRowSet crs = new CachedRowSetImpl();

            crs.populate(rs); // FIXME: close Resultset Objects??
            closeSQLObjects(con, stmt);
            return crs;
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError("Data select error. Query " + query, "SQLTimeoutException", e.getMessage());
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            persistError(dbName, query, e);
            throw new CygnusPersistenceError("Querying error", "SQLException", e.getMessage());
        } // try catch
    } // select

    private void delete(String dbName, String tableName, String filters)
            throws CygnusRuntimeError, CygnusPersistenceError {
        Statement stmt = null;

        // get a connection to the given database
        Connection con = driver.getConnection(dbName);
        String query = "delete from `" + tableName + "` where " + filters;

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusRuntimeError("Deleting error", "SQLException", e.getMessage());
        } // try catch

        try {
            LOGGER.debug("Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError("Data delete error. Query " + query, "SQLTimeoutException", e.getMessage());
        }catch (SQLException e) {
            closeSQLObjects(con, stmt);
            persistError(dbName, query, e);
            throw new CygnusPersistenceError("Deleting error", "SQLException", e.getMessage());
        } // try catch

        closeSQLObjects(con, stmt);
    } // delete

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
                        long currentTime = new java.util.Date().getTime();

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
     * Close all the SQL objects previously opened by doCreateTable and
     * doQuery.
     *
     * @param con
     * @param stmt
     * @return True if the SQL objects have been closed, false otherwise.
     */
    private void closeSQLObjects(Connection con, Statement stmt) throws CygnusRuntimeError {
        LOGGER.debug("Closing SQL connection objects.");
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

    } // closeSQLObjects


    public void createErrorTable(String dbName)
            throws CygnusRuntimeError, CygnusPersistenceError {
        // the defaul table for error log will be called the same as the db name
        String errorTable = dbName + "_error_log";
        if (cache.isCachedTable(dbName, errorTable)) {
            LOGGER.debug("'" + errorTable + "' is cached, thus it is not created");
            return;
        } // if
        String typedFieldNames = "(" +
                "timestamp TIMESTAMP" +
                ", error text" +
                ", query text)";

        Statement stmt = null;
        // get a connection to the given database
        Connection con = driver.getConnection(dbName);

        String query = "";
        if (sqlInstance.equals("mysql")) {
            query = "create table if not exists `" + errorTable + "`" + typedFieldNames;
        } else {
            query = "create table if not exists " + dbName + "." + errorTable + " " + typedFieldNames;
        }

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusRuntimeError("Table creation error", "SQLException", e.getMessage());
        } // try catch

        try {
            LOGGER.debug("Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusPersistenceError("Table creation error", "SQLException", e.getMessage());
        } // try catch

        closeSQLObjects(con, stmt);

        LOGGER.debug("Trying to add '" + errorTable + "' to the cache after table creation");
        cache.addTable(dbName, errorTable);
    } // createErrorTable

    public void insertErrorLog(String dbName, String errorQuery, Exception exception)
            throws CygnusBadContextData, CygnusRuntimeError, CygnusPersistenceError, SQLException {
        Statement stmt = null;
        java.util.Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        String errorTable = dbName + "_error_log";
        String fieldNames  = "(" +
                "timestamp" +
                ", error" +
                ", query)";

        // get a connection to the given database
        Connection con = driver.getConnection(dbName);

        String query = "";
        if (sqlInstance.equals("mysql")) {
            query = "insert into `" + errorTable + "` " + fieldNames + " values (?, ?, ?)";
        } else {
            query = "INSERT INTO " + dbName + "." + errorTable + " " + fieldNames + " VALUES (?, ?, ?)";
        }

        PreparedStatement preparedStatement = con.prepareStatement(query);
        try {
            preparedStatement.setObject(1, java.sql.Timestamp.from(Instant.now()));
            preparedStatement.setString(2, exception.getMessage());
            preparedStatement.setString(3, errorQuery);
            LOGGER.debug("Executing SQL query '" + query + "'");
            preparedStatement.executeUpdate();
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError("Data insertion error. Query: `" + preparedStatement, "SQLTimeoutException", e.getMessage());
        } catch (SQLException e) {
            throw new CygnusBadContextData("Data insertion error. Query: `" + preparedStatement, "SQLException", e.getMessage());
        } finally {
            closeSQLObjects(con, preparedStatement);
        } // try catch

        LOGGER.debug("Trying to add '" + dbName + "' and '" + errorTable + "' to the cache after insertion");
        cache.addDb(dbName);
        cache.addTable(dbName, errorTable);
    } // insertErrorLog

    public void persistError(String bd, String query, Exception exception) throws CygnusPersistenceError, CygnusRuntimeError {
        try {
            createErrorTable(bd);
            insertErrorLog(bd, query, exception);
            return;
        } catch (CygnusBadContextData cygnusBadContextData) {
            LOGGER.debug("failed to persist error on db " + bd + "_error_log" + cygnusBadContextData);
            createErrorTable(bd);
        } catch (Exception e) {
            LOGGER.debug("failed to persist error on db " + bd + "_error_log" + e);
        }
    }

    public class SQLDriver {

        private final HashMap<String, DataSource> datasources;
        private final HashMap<String, GenericObjectPool> pools;
        private final String sqlHost;
        private final String sqlPort;
        private final String sqlUsername;
        private final String sqlPassword;
        private final String sqlInstance;
        private final String sqlDriverName;
        private final int maxPoolSize;

        /**
         * Constructor.
         *
         * @param sqlHost
         * @param sqlPort
         * @param sqlUsername
         * @param sqlPassword
         * @param maxPoolSize
         * @param sqlInstance
         * @param sqlDriverName
         */
        public SQLDriver(String sqlHost, String sqlPort, String sqlUsername, String sqlPassword, int maxPoolSize, String sqlInstance, String sqlDriverName) {
            datasources = new HashMap<>();
            pools = new HashMap<>();
            this.sqlHost = sqlHost;
            this.sqlPort = sqlPort;
            this.sqlUsername = sqlUsername;
            this.sqlPassword = sqlPassword;
            this.maxPoolSize = maxPoolSize;
            this.sqlInstance = sqlInstance;
            this.sqlDriverName = sqlDriverName;
        } // SQLDriver

        /**
         * Gets a connection to the SQL server.
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
                        LOGGER.debug("Closing invalid sql connection for db " + dbName);
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
                String jdbcUrl = "jdbc:" + sqlInstance + "://" + sqlHost + ":" + sqlPort + "/" + dbName;
                Class.forName(sqlDriverName);

                // Creates an Instance of GenericObjectPool That Holds Our Pool of Connections Object!
                gPool = new GenericObjectPool();
                gPool.setMaxActive(this.maxPoolSize);
                pools.put(dbName, gPool);

                // Creates a ConnectionFactory Object Which Will Be Used by the Pool to Create the Connection Object!
                LOGGER.debug("Creating connection pool jdbc:" + sqlInstance +"://" + sqlHost + ":" + sqlPort + "/" + dbName
                        + "?user=" + sqlUsername + "&password=XXXXXXXXXX");
                ConnectionFactory cf = new DriverManagerConnectionFactory(jdbcUrl, sqlUsername, sqlPassword);

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
                    LOGGER.error("Error closing SQL pool " + dbName +": " + e.getMessage());
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

    } // SQLDriver
}
