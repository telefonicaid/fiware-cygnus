/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.sql;

import com.google.gson.JsonElement;
import com.sun.rowset.CachedRowSetImpl;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.backends.sql.Enum.SQLInstance;
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

/**
 * The type Sql backend.
 */
public class SQLBackendImpl implements SQLBackend{

    private static final CygnusLogger LOGGER = new CygnusLogger(SQLBackendImpl.class);
    private SQLBackendImpl.SQLDriver driver;
    private final SQLCache cache;
    protected SQLInstance sqlInstance;
    protected String sqlHost;    
    private final boolean persistErrors;
    private final int maxLatestErrors;
    private static final String DEFAULT_ERROR_TABLE_SUFFIX = "_error_log";
    private static final int DEFAULT_MAX_LATEST_ERRORS = 100;
    private static final String DEFAULT_LIMIT_SELECT_EXP_RECORDS = "4096";
    private String nlsTimestampFormat;
    private String nlsTimestampTzFormat;

    /**
     * Constructor.
     *
     * @param sqlHost
     * @param sqlPort
     * @param sqlUsername
     * @param sqlPassword
     * @param maxPoolSize
     * @param maxPoolIdle
     * @param minPoolIdle
     * @param minPoolIdleTimeMillis
     * @param sqlInstance
     * @param sqlDriverName
     * @param persistErrors
     * @param maxLatestErrors
     */
    public SQLBackendImpl(String sqlHost, String sqlPort, String sqlUsername, String sqlPassword, int maxPoolSize, int maxPoolIdle, int minPoolIdle, int minPoolIdleTimeMillis, SQLInstance sqlInstance, String sqlDriverName, boolean persistErrors, int maxLatestErrors) {
        this(sqlHost, sqlPort, sqlUsername, sqlPassword, maxPoolSize, maxPoolIdle, minPoolIdle, minPoolIdleTimeMillis, sqlInstance, sqlDriverName, null, persistErrors, maxLatestErrors);
    } // SQLBackendImpl

    /**
     * Constructor. (invoked by ngsild sinks)
     *
     * @param sqlHost
     * @param sqlPort
     * @param sqlUsername
     * @param sqlPassword
     * @param maxPoolSize
     * @param maxPoolIdle
     * @param minPoolIdle
     * @param minPoolIdleTimeMillis
     * @param sqlInstance
     * @param sqlDriverName
     * @param sqlOptions
     */
    public SQLBackendImpl(String sqlHost, String sqlPort, String sqlUsername, String sqlPassword, int maxPoolSize, int maxPoolIdle, int minPoolIdle, int minPoolIdleTimeMillis, SQLInstance sqlInstance, String sqlDriverName, String sqlOptions) {
        this(sqlHost, sqlPort, sqlUsername, sqlPassword, maxPoolSize, maxPoolIdle, minPoolIdle, minPoolIdleTimeMillis, sqlInstance, sqlDriverName, sqlOptions, true, DEFAULT_MAX_LATEST_ERRORS);
    } // SQLBackendImpl

    /**
     * Constructor.
     *
     * @param sqlHost
     * @param sqlPort
     * @param sqlUsername
     * @param sqlPassword
     * @param maxPoolSize
     * @param maxPoolIdle
     * @param minPoolIdle
     * @param minPoolIdleTimeMillis
     * @param sqlInstance
     * @param sqlDriverName
     * @param sqlOptions
     * @param persistErrors
     * @param maxLatestErrors
     */
    public SQLBackendImpl(String sqlHost, String sqlPort, String sqlUsername, String sqlPassword, int maxPoolSize, int maxPoolIdle, int minPoolIdle, int minPoolIdleTimeMillis, SQLInstance sqlInstance, String sqlDriverName, String sqlOptions, boolean persistErrors, int maxLatestErrors) {
        driver = new SQLBackendImpl.SQLDriver(sqlHost, sqlPort, sqlUsername, sqlPassword, maxPoolSize, maxPoolIdle, minPoolIdle, minPoolIdleTimeMillis, sqlInstance, sqlDriverName, sqlOptions);
        cache = new SQLCache();
        this.sqlHost = sqlHost;
        this.sqlInstance = sqlInstance;
        this.persistErrors = persistErrors;
        this.maxLatestErrors = maxLatestErrors;
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
    public void setDriver(SQLBackendImpl.SQLDriver driver) {
        this.driver = driver;
    } // setDriver

    public SQLBackendImpl.SQLDriver getDriver() {
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

    @Override
    public void createDestination(String destination) throws CygnusRuntimeError, CygnusPersistenceError {
        if (cache.isCachedDataBase(destination)) {
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " '" + destination + "' is cached, thus it is not created");
            return;
        } // if

        Statement stmt = null;

        // get a connection to an empty destination
        Connection con = driver.getConnection("", this.sqlHost);

        String query = "";
        if (sqlInstance == SQLInstance.MYSQL) {
            query = "create database if not exists `" + destination + "`";
        } else if (sqlInstance == SQLInstance.POSTGRESQL) {
            query = "CREATE SCHEMA IF NOT EXISTS " + destination;
        }

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Database/scheme creation error", "SQLException", e.getMessage());
        } // try catch

        try {
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " Database/scheme creation error", "SQLException", e.getMessage());
        } // try catch

        closeSQLObjects(con, stmt);

        LOGGER.debug(sqlInstance.toString().toUpperCase() + " Trying to add '" + destination + "' to the cache after database/scheme creation");
        cache.addDataBase(destination);
    } // createDestination

    // This method is an implementation for the method createDestination in order to make it easier to understand
    public void createDataBase (String dataBase) throws CygnusPersistenceError, CygnusRuntimeError {
        createDestination(dataBase);
    }

    // This method is an implementation for the method createDestination in order to make it easier to understand
    public void createSchema (String schema) throws CygnusPersistenceError, CygnusRuntimeError {
        createDestination(schema);
    }

    @Override
    public void createTable(String dataBase, String schema, String table, String typedFieldNames)
            throws CygnusRuntimeError, CygnusPersistenceError {
        String tableName = table;
        if (sqlInstance == SQLInstance.POSTGRESQL) {
            tableName = schema + "." + table;
        }
        if (cache.isCachedTable(dataBase, tableName)) {
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " '" + tableName + "' is cached, thus it is not created");
            return;
        } // if

        Statement stmt = null;

        // get a connection to the given destination
        Connection con = driver.getConnection(dataBase, this.sqlHost);
        String query = "";
        if (sqlInstance == SQLInstance.MYSQL) {
            query = "create table if not exists `" + tableName + "`" + typedFieldNames;
        } else if (sqlInstance == SQLInstance.POSTGRESQL) {
            query = "CREATE TABLE IF NOT EXISTS " + tableName + " " + typedFieldNames;
        } else if (sqlInstance == SQLInstance.ORACLE) {
            // FIXME: Add an oracle workaround for "if not exists"
            query = "CREATE TABLE " + tableName + " " + typedFieldNames;
        }

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Table creation error", "SQLException", e.getMessage());
        } // try catch

        try {
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " Table creation error. Query " + query, "SQLTimeoutException", e.getMessage());
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            persistError(dataBase, schema, query, e);
            throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " Table creation error", "SQLException", e.getMessage());
        } // try catch

        closeSQLObjects(con, stmt);

        LOGGER.debug(sqlInstance.toString().toUpperCase() + " Trying to add '" + tableName + "' to the cache after table creation");
        cache.addTable(dataBase, tableName);
    } // createTable


    // FXIME insertContextData Never used ?
    @Override
    public void insertContextData(String dataBase, String schema, String table, String fieldNames, String fieldValues)
            throws CygnusBadContextData, CygnusRuntimeError, CygnusPersistenceError {
        Statement stmt = null;

        String tableName = table;

        // get a connection to the given destination
        Connection con = driver.getConnection(dataBase, this.sqlHost);
        String query = "";
        if (sqlInstance == SQLInstance.MYSQL) {
            query = "insert into `" + tableName + "` " + fieldNames + " values " + fieldValues;
        } else if (sqlInstance == SQLInstance.POSTGRESQL) {
            tableName = schema + "." + table;
            query = "INSERT INTO " + tableName + " " + fieldNames + " VALUES " + fieldValues;
        } else if (sqlInstance == SQLInstance.ORACLE) {
            query = "INSERT INTO " + tableName + " " + fieldNames + " VALUES " + fieldValues;
        }

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Data insertion error", "SQLException", e.getMessage());
        } // try catch

        try {
            LOGGER.debug(sqlInstance.toString().toUpperCase() + "Database: " + dataBase + " Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
            LOGGER.info(sqlInstance.toString().toUpperCase() + "Database: " + dataBase + " Executed SQL query '" + query + "'");
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + "Database: " + dataBase + " Data insertion error. Query insert into `" + tableName + "` " + fieldNames + " values " + fieldValues, "SQLTimeoutException", e.getMessage());
        } catch (SQLException e) {
            persistError(dataBase, schema, query, e);
            throw new CygnusBadContextData(sqlInstance.toString().toUpperCase() + "Database: " + dataBase + " Data insertion error. Query: insert into `" + tableName + "` " + fieldNames + " values " + fieldValues, "SQLException", e.getMessage());
        } finally {
            closeSQLObjects(con, stmt);
        } // try catch

        LOGGER.debug(sqlInstance.toString().toUpperCase() + " Trying to add '" + dataBase + "' and '" + tableName + "' to the cache after insertion");
        cache.addDataBase(dataBase);
        cache.addTable(dataBase, tableName);
    } // insertContextData

    private CachedRowSet select(String dataBase, String schema, String tableName, String selection)
            throws CygnusRuntimeError, CygnusPersistenceError {
        Statement stmt = null;

        // get a connection to the given destination
        Connection con = driver.getConnection(dataBase, this.sqlHost);
        String query = "";
        if (sqlInstance == SQLInstance.MYSQL) {
            query = "select " + selection + " from `" + tableName + "` order by recvTime desc limit " + DEFAULT_LIMIT_SELECT_EXP_RECORDS;
        } else if (sqlInstance == SQLInstance.POSTGRESQL) {
            if (schema != null && (!tableName.startsWith(schema))) {
                tableName = schema + '.' + tableName;
            }
            query = "select " + selection + " from " + tableName + " order by recvTime desc limit " + DEFAULT_LIMIT_SELECT_EXP_RECORDS;
        } else {
            query = "select " + selection + " from " + tableName + " order by recvTime desc limit " + DEFAULT_LIMIT_SELECT_EXP_RECORDS;
        }

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Querying error", "SQLException", e.getMessage());
        } // try catch

        try {
            // to-do: refactor after implementing
            // https://github.com/telefonicaid/fiware-cygnus/issues/1371
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " Executing SQL query '" + query + "'");
            ResultSet rs = stmt.executeQuery(query);
            // A CachedRowSet is "disconnected" from the source, thus can be
            // used once the statement is closed
            @SuppressWarnings("restriction")
            CachedRowSet crs = new CachedRowSetImpl();
            crs.populate(rs); // FIXME: close Resultset Objects??
            closeSQLObjects(con, stmt);
            rs.close();
            return crs;
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " Data select error. Query " + query, "SQLTimeoutException", e.getMessage());
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            persistError(dataBase, schema, query, e);
            throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " Querying error", "SQLException", e.getMessage());
        } // try catch
    } // select

    private void delete(String dataBase, String schema, String tableName, String filters)
            throws CygnusRuntimeError, CygnusPersistenceError {
        Statement stmt = null;

        // get a connection to the given destination
        Connection con = driver.getConnection(dataBase, this.sqlHost);
        String query = "";
        if (sqlInstance == SQLInstance.MYSQL) {
            query = "delete from `" + tableName + "` where " + filters;
        } else if (sqlInstance == SQLInstance.POSTGRESQL) {
            if (schema != null && (!tableName.startsWith(schema))) {
                tableName = schema + '.' + tableName;
            }
            query = "delete from " + tableName + " where " + filters;
        } else {
            query = "delete from " + tableName + " where " + filters;
        }

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Deleting error", "SQLException", e.getMessage());
        } // try catch

        try {
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " Data delete error. Query " + query, "SQLTimeoutException", e.getMessage());
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            persistError(dataBase, schema, query, e);
            throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " Deleting error", "SQLException", e.getMessage());
        } // try catch

        closeSQLObjects(con, stmt);
    } // delete

    @Override
    public void capRecords(String dataBase, String schemaName, String tableName, long maxRecords)
            throws CygnusRuntimeError, CygnusPersistenceError {
        LOGGER.debug(sqlInstance.toString().toUpperCase() + " capRecords for database: " +
                     dataBase + " schema: " + schemaName + " tableName: " + tableName);
        // Get the records within the table
        CachedRowSet records = select(dataBase, schemaName, tableName, "*");

        // Get the number of records
        int numRecords = 0;

        try {
            if (records.last()) {
                numRecords = records.getRow();
                records.beforeFirst();
            } // if
        } catch (SQLException e) {
            throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Data capping error", "SQLException", e.getMessage());
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
            throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Data capping error", "SQLException", e.getMessage());
        } // try catch

        if (filters.isEmpty()) {
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " No records to be deleted");
        } else {
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " Records must be deleted (destination=" + dataBase + ",schemaName=" + schemaName + ",tableName=" + tableName + ", filters="
                    + filters + ")");
            delete(dataBase, schemaName, tableName, filters);
        } // if else
    } // capRecords

    @Override
    public void expirateRecordsCache(long expirationTime) throws CygnusRuntimeError, CygnusPersistenceError {
        // Iterate on the cached resource IDs
        cache.startDataBaseIterator();

        while (cache.hasNextDataBase()) {
            String dataBase = cache.nextDataBase();
            cache.startTableIterator(dataBase);

            while (cache.hasNextTable(dataBase)) {
                String tableName = cache.nextTable(dataBase);

                // Get schema from tableName if PSQL, just for persistError after
                String schema = null;
                if (sqlInstance == SQLInstance.POSTGRESQL) {
                    String[] parts = tableName.split("\\.");
                    if (parts.length > 0) {
                        schema = parts[0];
                    }
                }
                LOGGER.debug(sqlInstance.toString().toUpperCase() + " expirateRecordsCache for database: " +
                             dataBase + " schema: " + schema + " tableName: " + tableName);
                // Get the records within the table
                CachedRowSet records = select(dataBase, schema, tableName, "*");

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
                        LOGGER.debug(sqlInstance.toString().toUpperCase() + " Can't close CachedRowSet.");
                    }
                    throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Data expiration error", "SQLException", e.getMessage());
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
                    throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Data expiration error", "SQLException", e.getMessage());
                } catch (ParseException e) {
                    throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Data expiration error", "ParseException", e.getMessage());
                } catch (Exception e) {
                    throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Data expiration error", "Exception", e.getMessage());
                } // try catch

                if (filters.isEmpty()) {
                    LOGGER.debug(sqlInstance.toString().toUpperCase() + " No records to be deleted");
                } else {
                    LOGGER.debug(sqlInstance.toString().toUpperCase() + " Records must be deleted (destination=" + dataBase + ",schemaName=" + schema + ",tableName=" + tableName + ", filters="
                            + filters + ")");
                    delete(dataBase, schema, tableName, filters);
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
        LOGGER.debug(sqlInstance.toString().toUpperCase() + " Closing SQL connection objects.");
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
                LOGGER.warn(sqlInstance.toString().toUpperCase() + " error closing invalid statement: " + e.getMessage());
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
                LOGGER.warn(sqlInstance.toString().toUpperCase() + " error closing invalid connection: " + e.getMessage());
            } // try catch
        } // if
    }


    public void createErrorTable(String dataBase, String schema)
            throws CygnusRuntimeError, CygnusPersistenceError {
        // the default table for error log will be called the same as the destination name
        String errorTableName = dataBase + DEFAULT_ERROR_TABLE_SUFFIX;
        if (sqlInstance == SQLInstance.POSTGRESQL) {
            errorTableName = schema + "." + dataBase + DEFAULT_ERROR_TABLE_SUFFIX;
        }
        String typedFieldNames = "(" +
                "timestamp TIMESTAMP NOT NULL" +
                ", error text" +
                ", query text" +
            ", CONSTRAINT PK_ErrorLog PRIMARY KEY (timestamp) )";
        String typedFieldNamesOracle = "(" +
                "timestamp TIMESTAMP NOT NULL" +
                ", error clob" +
                ", query clob" +
            ", CONSTRAINT PK_ErrorLog PRIMARY KEY (timestamp) )";

        Statement stmt = null;
        // get a connection to the given destination
        Connection con = driver.getConnection(dataBase, this.sqlHost);

        String query = "";
        if (sqlInstance == SQLInstance.MYSQL) {
            query = "create table if not exists `" + errorTableName + "`" + typedFieldNames;
        } else if (sqlInstance == SQLInstance.POSTGRESQL) {
            query = "CREATE TABLE IF NOT EXISTS " + errorTableName + " " + typedFieldNames;
        } else if (sqlInstance == SQLInstance.ORACLE) {
            // FIXME: Add an oracle workaround for "if not exists"
            query = "CREATE TABLE " + errorTableName + " " + typedFieldNamesOracle;
        }

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Table creation error", "SQLException", e.getMessage());
        } // try catch

        try {
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " Table creation error", "SQLException", e.getMessage());
        } // try catch

        closeSQLObjects(con, stmt);
    } // createErrorTable

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
        int insertedRows[];

        try {

            connection = driver.getConnection(dataBase, this.sqlHost);
            connection.setAutoCommit(false);

            ArrayList<StringBuffer> upsertQuerysList = SQLQueryUtils.sqlUpsertQuery(aggregation,
                    lastData,
                    lastDataDelete,
                    tableName,
                    tableSuffix,
                    uniqueKey,
                    timestampKey,
                    timestampFormat,
                    sqlInstance,
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
            LOGGER.info(sqlInstance.toString().toUpperCase() + " Finished transactions into database: " +
                        dataBase + " \n upsertQuerys: " + upsertQuerys);

        } catch (SQLTimeoutException e) {
            cygnusSQLRollback(connection);
            if (upsertQuerys.isEmpty() && currentUpsertQuery.isEmpty()) {
                throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " " + e.getNextException() +
                                                 " Data insertion error. database: " + dataBase +
                                                 " connection: " + connection,
                                                 " SQLTimeoutException", e.getMessage());
            } else {
                throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " " + e.getNextException() +
                                                 " Data insertion error. database: " + dataBase +
                                                 " upsertQuerys: " + upsertQuerys +
                                                 " currentUpsertQuery: " + currentUpsertQuery,
                                                 " SQLTimeoutException", e.getMessage());
            }
        } catch (SQLException e) {
            cygnusSQLRollback(connection);
            if (upsertQuerys.isEmpty() && currentUpsertQuery.isEmpty()) {
                persistError(dataBase, schema, null, e);
                throw new CygnusBadContextData(sqlInstance.toString().toUpperCase() + " " + e.getNextException() +
                                               " Data insertion error. database: " + dataBase +
                                               " connection: " + connection,
                                               " SQLException", e.getMessage());

            } else {
                String allQueries = " upsertQuerys: " + upsertQuerys +
                    " currentUpsertQuery: " + currentUpsertQuery;
                persistError(dataBase, schema, allQueries, e);
                throw new CygnusBadContextData(sqlInstance.toString().toUpperCase() + " " + e.getNextException() +
                                               " Data insertion error. database: " + dataBase + allQueries,
                                               " SQLException", e.getMessage());
            }
        } finally {
            closeConnection(connection);
        } // try catch
        if (sqlInstance == SQLInstance.POSTGRESQL) {
            tableName = schema + "." + tableName;
        }
        LOGGER.debug(sqlInstance.toString().toUpperCase() + " Trying to add '" + dataBase + "' and '" + tableName + "' to the cache after upsertion");
        cache.addDataBase(dataBase);
        cache.addTable(dataBase, tableName);
    }


    public void insertTransaction (LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                   String dataBase,
                                   String schema,
                                   String tableName,
                                   boolean attrNativeTypes)
        throws CygnusPersistenceError, CygnusBadContextData, CygnusRuntimeError {

        Connection connection = null;
        String insertQuery = new String();

        try {

            connection = driver.getConnection(dataBase, this.sqlHost);
            connection.setAutoCommit(false);

            insertQuery = SQLQueryUtils.sqlInsertQuery(aggregation,
                                                       tableName,
                                                       sqlInstance,
                                                       dataBase,
                                                       schema,
                                                       attrNativeTypes).toString();

            PreparedStatement insertStatement;
            insertStatement = connection.prepareStatement(insertQuery);
            /*
            FIXME https://github.com/telefonicaid/fiware-cygnus/issues/1959
            Add SQLSafe values with native PreparedStatement methods
            insertPreparedStatement = SQLQueryUtils.addJsonValues(insertStatement, aggregation, attrNativeTypes);
            */
            insertStatement.executeUpdate();

            connection.commit();
            LOGGER.info(sqlInstance.toString().toUpperCase() + " Finished transactions into database: " +
                        dataBase + " \n insertQuery: " + insertQuery);

        } catch (SQLTimeoutException e) {
            cygnusSQLRollback(connection);
            if (insertQuery.isEmpty()) {
                throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " " + e.getNextException() +
                                                 " Data insertion error. database: " + dataBase +
                                                 " connection: " + connection,
                                                 " SQLTimeoutException", e.getMessage());
            } else {
                throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " " + e.getNextException() +
                                                 " Data insertion error. database: " + dataBase +
                                                 " insertQuery: " + insertQuery,
                                                 " SQLTimeoutException", e.getMessage());
            }
        } catch (SQLException e) {
            cygnusSQLRollback(connection);
            if (insertQuery.isEmpty()) {
                persistError(dataBase, schema, null, e);
                throw new CygnusBadContextData(sqlInstance.toString().toUpperCase() + " " + e.getNextException() +
                                               " Data insertion error. database: " + dataBase +
                                               " connection: `" + connection,
                                               " SQLException", e.getMessage());

            } else {
                String allQueries = " insertQuery: " + insertQuery;
                persistError(dataBase, schema, allQueries, e);
                throw new CygnusBadContextData(sqlInstance.toString().toUpperCase() + " " + e.getNextException() +
                                               " Data insertion error. database: " + dataBase + allQueries,
                                               " SQLException", e.getMessage());
            }
        } finally {
            closeConnection(connection);
        } // try catch
        if (sqlInstance == SQLInstance.POSTGRESQL) {
            tableName = schema + "." + tableName;
        }
        LOGGER.debug(sqlInstance.toString().toUpperCase() + " Trying to add '" + dataBase + "' and '" + tableName + "' to the cache after insertion");
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

    public void purgeErrorTable(String dataBase, String schema)
            throws CygnusRuntimeError, CygnusPersistenceError {
        // the default table for error log will be called the same as the destination name
        String errorTableName = dataBase + DEFAULT_ERROR_TABLE_SUFFIX;
        if (sqlInstance == SQLInstance.POSTGRESQL) {
            errorTableName = schema + "." + dataBase + DEFAULT_ERROR_TABLE_SUFFIX;
        }
        String limit = String.valueOf(maxLatestErrors);

        Statement stmt = null;
        // get a connection to the given destination
        Connection con = driver.getConnection(dataBase, this.sqlHost);

        String query = "";
        if (sqlInstance == SQLInstance.MYSQL) {
            query = "delete from `" + errorTableName + "` "  + "where timestamp not in (select timestamp from (select timestamp from `" + errorTableName + "` "  + "order by timestamp desc limit " + limit + " ) tmppurge )";
        } else if (sqlInstance == SQLInstance.POSTGRESQL) {
            query = "DELETE FROM " + errorTableName + " "  + "WHERE timestamp NOT IN (SELECT timestamp FROM (SELECT timestamp FROM " + errorTableName + " "  + "ORDER BY timestamp DESC LIMIT " + limit + " ) tmppurge )";
        } else if (sqlInstance == SQLInstance.ORACLE) {
            // FXIME: add limit
            query = "DELETE FROM " + errorTableName + " "  + "WHERE timestamp NOT IN (SELECT timestamp FROM (SELECT timestamp FROM " + errorTableName + " "  + "ORDER BY timestamp DESC ) tmppurge )";
        }

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Purge error table error", "SQLException", e.getMessage());
        } // try catch

        try {
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " Executing SQL query '" + query + "'");
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            closeSQLObjects(con, stmt);
            throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " Purge error table error", "SQLException", e.getMessage());
        } // try catch

        closeSQLObjects(con, stmt);
    }

    private void insertErrorLog(String dataBase, String schema, String errorQuery, Exception exception)
            throws CygnusBadContextData, CygnusRuntimeError, CygnusPersistenceError, SQLException {
        java.util.Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        String errorTableName = dataBase + DEFAULT_ERROR_TABLE_SUFFIX;
        if (sqlInstance == SQLInstance.POSTGRESQL) {
            errorTableName = schema + "." + dataBase + DEFAULT_ERROR_TABLE_SUFFIX;            
        }
        String fieldNames  = "(" +
                "timestamp" +
                ", error" +
                ", query)";

        // get a connection to the given destination
        Connection con = driver.getConnection(dataBase, this.sqlHost);

        String query = "";
        if (sqlInstance == SQLInstance.MYSQL) {
            query = "insert into `" + errorTableName + "` " + fieldNames + " values (?, ?, ?)";
        } else if (sqlInstance == SQLInstance.POSTGRESQL ||
                   sqlInstance == SQLInstance.ORACLE){
            query = "INSERT INTO " + errorTableName + " " + fieldNames + " VALUES (?, ?, ?)";
        }

        PreparedStatement preparedStatement = con.prepareStatement(query);

        try {
            preparedStatement.setObject(1, java.sql.Timestamp.from(Instant.now()));
            preparedStatement.setString(2, exception.getMessage());
            preparedStatement.setString(3, errorQuery);
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " Executing SQL query '" +  preparedStatement.toString() + "'");
            preparedStatement.executeUpdate();
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " Data insertion error. Query: `" + preparedStatement, "SQLTimeoutException", e.getMessage());
        } catch (SQLException e) {
            throw new CygnusBadContextData(sqlInstance.toString().toUpperCase() + " Data insertion error. Query: `" + preparedStatement, "SQLException", e.getMessage());
        } finally {
            closeSQLObjects(con, preparedStatement);
        } // try catch

    } // insertErrorLog

    private void persistError(String destination, String schema, String query, Exception exception) throws CygnusPersistenceError, CygnusRuntimeError {
        try {
            if (persistErrors) {
                try {
                    // try insert without create error table before
                    insertErrorLog(destination, schema, query, exception);
                } catch (CygnusBadContextData ex) {
                    createErrorTable(destination, schema);
                    insertErrorLog(destination, schema, query, exception);
                }
                purgeErrorTable(destination, schema);
            }
            return;
        } catch (CygnusBadContextData cygnusBadContextData) {
            LOGGER.warn(sqlInstance.toString().toUpperCase() + " failed to persist error on database/scheme " + destination + DEFAULT_ERROR_TABLE_SUFFIX + " " + cygnusBadContextData);
        } catch (Exception e) {
            LOGGER.warn(sqlInstance.toString().toUpperCase() + " failed to persist error on database/scheme " + destination + DEFAULT_ERROR_TABLE_SUFFIX + " " + e);
        }
    }

    public class SQLDriver {

        private final HashMap<String, DataSource> datasources;
        private final HashMap<String, GenericObjectPool> pools;
        private final String sqlHost;
        private final String sqlPort;
        private final String sqlUsername;
        private final String sqlPassword;
        private final SQLInstance sqlInstance;
        private final String sqlDriverName;
        private final int maxPoolSize;
        private final int maxPoolIdle;
        private final int minPoolIdle;
        private final int minPoolIdleTimeMillis;
        private final String sqlOptions;

        /**
         * Constructor.
         *
         * @param sqlHost
         * @param sqlPort
         * @param sqlUsername
         * @param sqlPassword
         * @param maxPoolSize
         * @param maxPoolIdle
         * @param minPoolIdle
         * @param minPoolIdleTimeMillis
         * @param sqlInstance
         * @param sqlDriverName
         * @param sqlOptions
         */
        public SQLDriver(String sqlHost, String sqlPort, String sqlUsername, String sqlPassword, int maxPoolSize, int maxPoolIdle, int minPoolIdle, int minPoolIdleTimeMillis, SQLInstance sqlInstance, String sqlDriverName, String sqlOptions) {
            datasources = new HashMap<>();
            pools = new HashMap<>();
            this.sqlHost = sqlHost;
            this.sqlPort = sqlPort;
            this.sqlUsername = sqlUsername;
            this.sqlPassword = sqlPassword;
            this.maxPoolSize = maxPoolSize;
            this.maxPoolIdle = maxPoolIdle;
            this.minPoolIdle = minPoolIdle;
            this.minPoolIdleTimeMillis = minPoolIdleTimeMillis;
            this.sqlInstance = sqlInstance;
            this.sqlDriverName = sqlDriverName;
            this.sqlOptions = sqlOptions;
        } // SQLDriver

        /**
         * Gets a connection to the SQL server.
         *
         * @param destination
         * @return
         * @throws CygnusRuntimeError
         * @throws CygnusPersistenceError
         */
        public Connection getConnection(String destination, String sqlHost) throws CygnusRuntimeError, CygnusPersistenceError {
            try {
                // FIXME: the number of cached connections should be limited to
                // a certain number; with such a limit
                // number, if a new connection is needed, the oldest one is closed
                Connection connection = null;

                if (datasources.containsKey(sqlHost)) {                    
                    connection = datasources.get(sqlHost).getConnection();
                    LOGGER.debug(sqlInstance.toString().toUpperCase() + " Recovered destination connection from cache (" + sqlHost + ")");                    
                }

                if (connection == null || !connection.isValid(0)) {
                    if (connection != null) {
                        LOGGER.debug(sqlInstance.toString().toUpperCase() + " Closing invalid sql connection for destination " + sqlHost);                        
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            LOGGER.warn(sqlInstance.toString().toUpperCase() + " error closing invalid connection: " + e.getMessage());
                        }
                    } // if

                    DataSource datasource = createConnectionPool(destination, sqlHost);
                    datasources.put(sqlHost, datasource);                    
                    connection = datasource.getConnection();
                    if (sqlInstance == SQLInstance.ORACLE) {
                        // set proper NLS_TIMESTAMP formats for current session
                        Statement alterStatement1 = connection.createStatement();
                        alterStatement1.execute("ALTER SESSION SET NLS_TIMESTAMP_FORMAT='" + getNlsTimestampFormat() + "'");
                        connection.commit();
                        Statement alterStatement2 = connection.createStatement();
                        alterStatement2.execute("ALTER SESSION SET NLS_TIMESTAMP_TZ_FORMAT='" + getNlsTimestampTzFormat() + "'");
                        connection.commit();
                    }
                } // if

                // Check Pool cache and log status
                if (pools.containsKey(sqlHost)){                    
                    GenericObjectPool pool = pools.get(sqlHost);
                    LOGGER.debug(sqlInstance.toString().toUpperCase() + " Pool status (" + sqlHost + ") Max.: " + pool.getMaxActive() + "; Active: "                                 
                            + pool.getNumActive() + "; Idle: " + pool.getNumIdle());
                }else{
                    LOGGER.error(sqlInstance.toString().toUpperCase() + " Can't find dabase in pool cache (" + sqlHost + ")");                    
                }
                return connection;
            } catch (ClassNotFoundException e) {
                throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Connection error", "ClassNotFoundException", e.getMessage());
            } catch (SQLException e) {
                throw new CygnusPersistenceError(sqlInstance.toString().toUpperCase() + " Connection error", "SQLException", e.getMessage());
            } catch (Exception e) {
                throw new CygnusRuntimeError(sqlInstance.toString().toUpperCase() + " Connection error creating new Pool", "Exception", e.getMessage());
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
                LOGGER.debug(sqlInstance.toString().toUpperCase() + " Pool status (" + destination + ") Max.: " + pool.getMaxActive() + "; Active: "
                        + pool.getNumActive() + "; Idle: " + pool.getNumIdle());
            }
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " Total pool's active connections: " + connectionCount);
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
                LOGGER.debug(sqlInstance.toString().toUpperCase() + " Pool status (" + destination + ") Max.: " + pool.getMaxActive() + "; Active: "
                        + pool.getNumActive() + "; Idle: " + pool.getNumIdle());
            }
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " Max pool connections: " + connectionCount);
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
        private DataSource createConnectionPool(String destination, String sqlHost) throws Exception {
            GenericObjectPool gPool = null;
            if (pools.containsKey(sqlHost)){                
                LOGGER.debug(sqlInstance.toString().toUpperCase() + " Pool recovered from Cache (" + sqlHost + ")");                
                gPool = pools.get(sqlHost);                
            }else{
                String jdbcUrl = generateJDBCUrl(destination);
                Class.forName(sqlDriverName);

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
                String sep = (sqlOptions != null && !sqlOptions.trim().isEmpty()) ? "&" : "?";
                String logJdbc = jdbcUrl + sep + "user=" + sqlUsername + "&password=XXXXXXXXXX";

                LOGGER.debug(sqlInstance.toString().toUpperCase() + " Creating connection pool jdbc: " + logJdbc);
                ConnectionFactory cf = new DriverManagerConnectionFactory(jdbcUrl, sqlUsername, sqlPassword);

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
            if (sqlInstance == SQLInstance.ORACLE) {
                jdbcUrl = "jdbc:" + sqlInstance + ":@" + sqlHost + ":" + sqlPort + ":" + destination;
            } else { // PostgreSQL and MySQL
                jdbcUrl = "jdbc:" + sqlInstance + "://" + sqlHost + ":" + sqlPort + "/" + destination;
            }
            if (sqlOptions != null && !sqlOptions.trim().isEmpty()) {
                jdbcUrl += "?" + sqlOptions;
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
                    LOGGER.debug(sqlInstance.toString().toUpperCase() + " Pool closed: (" + destination + ")");
                } catch (Exception e) {
                    LOGGER.error(sqlInstance.toString().toUpperCase() + " Error closing SQL pool " + destination +": " + e.getMessage());
                }
            }
            LOGGER.debug(sqlInstance.toString().toUpperCase() + " Number of Pools closed: " + poolCount + "/" + poolsSize);
        } // close

        /**
         * Last resort releasing resources
         */
        public void Finally(){
            this.close();
        }

    } // SQLDriver
}
