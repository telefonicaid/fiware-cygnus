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

package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.aggregation.NGSIGenericAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericColumnAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericRowAggregator;
import com.telefonica.iot.cygnus.backends.sql.SQLQueryUtils;
import com.telefonica.iot.cygnus.backends.sql.SQLBackendImpl;
import com.telefonica.iot.cygnus.backends.sql.Enum.SQLInstance;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.flume.Context;

/**
 * The type Ngsi postgre sql sink.
 *
 * @author hermanjunge Detailed documentation can be found at:https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/design/NGSIPostgreSQLSink.md
 */
public class NGSIPostgreSQLSink extends NGSISink {

    private static final String DEFAULT_ROW_ATTR_PERSISTENCE = "row";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_USER_NAME = "postgres";
    private static final String DEFAULT_DATABASE = "postgres";
    private static final String DEFAULT_ENABLE_CACHE = "false";
    private static final int DEFAULT_MAX_POOL_SIZE = 3;
    private static final String DEFAULT_ATTR_NATIVE_TYPES = "false";
    private static final String POSTGRESQL_DRIVER_NAME = "org.postgresql.Driver";
    private static final SQLInstance POSTGRESQL_INSTANCE_NAME = SQLInstance.POSTGRESQL;
    private static final String DEFAULT_FIWARE_SERVICE = "default";
    private static final String ESCAPED_DEFAULT_FIWARE_SERVICE = "default_service";
    private static final String DEFAULT_LAST_DATA = "false";
    private static final String DEFAULT_LAST_DATA_TABLE_SUFFIX = "_last_data";
    private static final String DEFAULT_LAST_DATA_UNIQUE_KEY = NGSIConstants.ENTITY_ID;
    private static final String DEFAULT_LAST_DATA_TIMESTAMP_KEY = NGSIConstants.RECV_TIME;
    private static final String DEFAULT_LAST_DATA_SQL_TS_FORMAT = "YYYY-MM-DD HH24:MI:SS.MS";
    private static final int DEFAULT_MAX_LATEST_ERRORS = 100;

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIPostgreSQLSink.class);
    private String postgresqlHost;
    private String postgresqlPort;
    private String postgresqlDatabase;
    private String postgresqlUsername;
    private String postgresqlPassword;
    private int maxPoolSize;
    private boolean rowAttrPersistence;
    private SQLBackendImpl postgreSQLPersistenceBackend;
    private boolean enableCache;
    private boolean attrNativeTypes;
    private boolean attrMetadataStore;
    private String postgresqlOptions;
    private boolean persistErrors;
    private boolean lastData;
    private String lastDataTableSuffix;
    private String lastDataUniqueKey;
    private String lastDataTimeStampKey;
    private String lastDataSQLTimestampFormat;
    private int maxLatestErrors;

    /**
     * Constructor.
     */
    public NGSIPostgreSQLSink() {
        super();
    } // NGSIPostgreSQLSink

    /**
     * Gets the PostgreSQL host. It is protected due to it is only required for testing purposes.
     * @return The PostgreSQL host
     */
    protected String getPostgreSQLHost() {
        return postgresqlHost;
    } // getPostgreSQLHost
    
    /**
     * Gets the PostgreSQL cache. It is protected due to it is only required for testing purposes.
     * @return The PostgreSQL cache state
     */
    protected boolean getEnableCache() {
        return enableCache;
    } // getPostgreSQLHost

    /**
     * Gets the PostgreSQL port. It is protected due to it is only required for testing purposes.
     * @return The PostgreSQL port
     */
    protected String getPostgreSQLPort() {
        return postgresqlPort;
    } // getPostgreSQLPort

    /**
     * Gets the PostgreSQL database. It is protected due to it is only required for testing purposes.
     * @return The PostgreSQL database
     */
    protected String getPostgreSQLDatabase() {
        return postgresqlDatabase;
    } // getPostgreSQLDatabase

    /**
     * Gets the PostgreSQL username. It is protected due to it is only required for testing purposes.
     * @return The PostgreSQL username
     */
    protected String getPostgreSQLUsername() {
        return postgresqlUsername;
    } // getPostgreSQLUsername

    /**
     * Gets the PostgreSQL password. It is protected due to it is only required for testing purposes.
     * @return The PostgreSQL password
     */
    protected String getPostgreSQLPassword() {
        return postgresqlPassword;
    } // getPostgreSQLPassword

    /**
     * Returns if the attribute persistence is row-based. It is protected due to it is only required for testing
     * purposes.
     * @return True if the attribute persistence is row-based, false otherwise
     */
    protected boolean getRowAttrPersistence() {
        return rowAttrPersistence;
    } // getRowAttrPersistence

    /**
     * Gets the PostgreSQL options. It is protected due to it is only required for testing purposes.
     * @return The PostgreSQL options
     */
    protected String getPostgreSQLOptions() {
        return postgresqlOptions;
    } // getPostgreSQLOptions

    /**
     * Returns if the attribute value will be native or stringfy. It will be stringfy due to backward compatibility
     * purposes.
     * @return True if the attribute value will be native, false otherwise
     */
    protected boolean getNativeAttrTypes() {
        return attrNativeTypes;
    } // attrNativeTypes

    /**
     * Returns the persistence backend. It is protected due to it is only required for testing purposes.
     * @return The persistence backend
     */
    protected SQLBackendImpl getPersistenceBackend() {
        return postgreSQLPersistenceBackend;
    } // getPersistenceBackend

    /**
     * Sets the persistence backend. It is protected due to it is only required for testing purposes.
     * @param postgreSQLPersistenceBackend
     */
    protected void setPersistenceBackend(SQLBackendImpl postgreSQLPersistenceBackend) {
        this.postgreSQLPersistenceBackend = postgreSQLPersistenceBackend;
    } // setPersistenceBackend

    @Override
    public void configure(Context context) {
        // Read NGSISink general configuration
        super.configure(context);
        
        // Impose enable lower case, since PostgreSQL only accepts lower case
        enableLowercase = true;
        
        postgresqlHost = context.getString("postgresql_host", DEFAULT_HOST);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgresql_host=" + postgresqlHost + ")");
        postgresqlPort = context.getString("postgresql_port", DEFAULT_PORT);
        int intPort = Integer.parseInt(postgresqlPort);

        if ((intPort <= 0) || (intPort > 65535)) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (postgresql_port=" + postgresqlPort + ")"
                    + " -- Must be between 0 and 65535");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (postgresql_port=" + postgresqlPort + ")");
        }  // if else

        postgresqlDatabase = context.getString("postgresql_database", DEFAULT_DATABASE);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgresql_database=" + postgresqlDatabase + ")");
        postgresqlUsername = context.getString("postgresql_username", DEFAULT_USER_NAME);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgresql_username=" + postgresqlUsername + ")");
        // FIXME: postgresqlPassword should be read as a SHA1 and decoded here
        postgresqlPassword = context.getString("postgresql_password", DEFAULT_PASSWORD);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgresql_password=" + postgresqlPassword + ")");

        maxPoolSize = context.getInteger("postgresql_maxPoolSize", DEFAULT_MAX_POOL_SIZE);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgresql_maxPoolSize=" + maxPoolSize + ")");

        rowAttrPersistence = context.getString("attr_persistence", DEFAULT_ROW_ATTR_PERSISTENCE).equals("row");
        String persistence = context.getString("attr_persistence", DEFAULT_ROW_ATTR_PERSISTENCE);

        if (persistence.equals("row") || persistence.equals("column")) {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_persistence="
                + persistence + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (attr_persistence="
                + persistence + ") -- Must be 'row' or 'column'");
        }  // if else
                
        String enableCacheStr = context.getString("backend.enable_cache", DEFAULT_ENABLE_CACHE);
        
        if (enableCacheStr.equals("true") || enableCacheStr.equals("false")) {
            enableCache = Boolean.valueOf(enableCacheStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (backend.enable_cache=" + enableCache + ")");
        }  else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (backend.enable_cache="
                + enableCache + ") -- Must be 'true' or 'false'");
        }  // if else

        String attrNativeTypesStr = context.getString("attr_native_types", DEFAULT_ATTR_NATIVE_TYPES);
        if (attrNativeTypesStr.equals("true") || attrNativeTypesStr.equals("false")) {
            attrNativeTypes = Boolean.valueOf(attrNativeTypesStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_native_types=" + attrNativeTypes + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (attr_native_types="
                + attrNativeTypesStr + ") -- Must be 'true' or 'false'");
        } // if else



        String attrMetadataStoreStr = context.getString("attr_metadata_store", "true");

        if (attrMetadataStoreStr.equals("true") || attrMetadataStoreStr.equals("false")) {
            attrMetadataStore = Boolean.parseBoolean(attrMetadataStoreStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_metadata_store="
                    + attrMetadataStore + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (attr_metadata_store="
                    + attrMetadataStoreStr + ") -- Must be 'true' or 'false'");
        }

        String lastDataStr = context.getString("enable_last_data", DEFAULT_LAST_DATA);

        if (lastDataStr.equals("true") || lastDataStr.equals("false")) {
            lastData = Boolean.parseBoolean(lastDataStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (last_data="
                    + lastDataStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (last_data="
                    + lastDataStr + ") -- Must be 'true' or 'false'");
        } // if else

        lastDataTableSuffix = context.getString("last_data_table_suffix", DEFAULT_LAST_DATA_TABLE_SUFFIX);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (last_data_table_suffix="
                + lastDataTableSuffix + ")");

        lastDataUniqueKey = context.getString("last_data_unique_key", DEFAULT_LAST_DATA_UNIQUE_KEY);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (last_data_unique_key="
                + lastDataUniqueKey + ")");

        lastDataTimeStampKey = context.getString("last_data_timestamp_key", DEFAULT_LAST_DATA_TIMESTAMP_KEY);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (last_data_timestamp_key="
                + lastDataTimeStampKey + ")");

        lastDataSQLTimestampFormat = context.getString("last_data_sql_timestamp_format", DEFAULT_LAST_DATA_SQL_TS_FORMAT);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (last_data_sql_timestamp_format="
                + lastDataSQLTimestampFormat + ")");

        postgresqlOptions = context.getString("postgresql_options", null);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgresql_options=" + postgresqlOptions + ")");

        String persistErrorsStr = context.getString("persist_errors", "true");

        if (persistErrorsStr.equals("true") || persistErrorsStr.equals("false")) {
            persistErrors = Boolean.parseBoolean(persistErrorsStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (persist_errors="
                    + persistErrors + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (persist_errors="
                    + persistErrorsStr + ") -- Must be 'true' or 'false'");
        } // if else

        maxLatestErrors = context.getInteger("max_latest_errors", DEFAULT_MAX_LATEST_ERRORS);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (max_latest_errors=" + maxLatestErrors + ")");

    } // configure

    @Override
    public void start() {
        try {
            createPersistenceBackend(postgresqlHost, postgresqlPort, postgresqlUsername, postgresqlPassword, maxPoolSize, postgresqlOptions, persistErrors, maxLatestErrors);
            LOGGER.debug("[" + this.getName() + "] Postgresql persistence backend created");
        } catch (Exception e) {
            LOGGER.error("Error while creating the PostgreSQL persistence backend. Details="
                    + e.getMessage());
        } // try catch

        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

    /**
     * Initialices a lazy singleton to share among instances on JVM
     */
    private void createPersistenceBackend(String sqlHost, String sqlPort, String sqlUsername, String sqlPassword, int maxPoolSize, String sqlOptions, boolean persistErrors, int maxLatestErrors) {
        if (postgreSQLPersistenceBackend == null) {
            postgreSQLPersistenceBackend = new SQLBackendImpl(sqlHost, sqlPort, sqlUsername, sqlPassword, maxPoolSize, POSTGRESQL_INSTANCE_NAME, POSTGRESQL_DRIVER_NAME, sqlOptions, persistErrors, maxLatestErrors);
        }
    }

    @Override
    void persistBatch(NGSIBatch batch)
        throws CygnusBadConfiguration, CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if

        // Iterate on the destinations
        batch.startIterator();
        
        while (batch.hasNext()) {
            String destination = batch.getNextDestination();
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the "
                    + destination + " destination");

            // get the sub-batch for this destination
            ArrayList<NGSIEvent> events = batch.getNextEvents();

            // get an aggregator for this destination and initialize it
            NGSIGenericAggregator aggregator = getAggregator(rowAttrPersistence);
            aggregator.setService(events.get(0).getServiceForNaming(enableNameMappings));
            aggregator.setServicePathForData(events.get(0).getServicePathForData());
            aggregator.setServicePathForNaming(events.get(0).getServicePathForNaming(enableGrouping, enableNameMappings));
            aggregator.setEntityForNaming(events.get(0).getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding));
            aggregator.setEntityType(events.get(0).getEntityTypeForNaming(enableGrouping, enableNameMappings));
            aggregator.setAttribute(events.get(0).getAttributeForNaming(enableNameMappings));
            aggregator.setSchemeName(buildSchemaName(aggregator.getService(), aggregator.getServicePathForNaming()));
            aggregator.setDbName(buildDBName(events.get(0).getServiceForNaming(enableNameMappings)));
            aggregator.setSchemeName(buildSchemaName(aggregator.getService(), aggregator.getServicePathForNaming()));
            aggregator.setTableName(buildTableName(aggregator.getServicePathForNaming(), aggregator.getEntityForNaming(), aggregator.getEntityType(), aggregator.getAttribute()));
            aggregator.setAttrNativeTypes(attrNativeTypes);
            aggregator.setAttrMetadataStore(attrMetadataStore);
            aggregator.setEnableNameMappings(enableNameMappings);
            aggregator.setEnableLastData(lastData);
            aggregator.setLastDataTimestampKey(lastDataTimeStampKey);
            aggregator.setLastDataUniqueKey(lastDataUniqueKey);
            aggregator.initialize(events.get(0));

            for (NGSIEvent event : events) {
                aggregator.aggregate(event);
            } // for

            // persist the fieldValues
            persistAggregation(aggregator);
            batch.setNextPersisted(true);
        } // for
    } // persistBatch
    
    @Override
    public void capRecords(NGSIBatch batch, long maxRecords) throws CygnusCappingError {
    } // capRecords

    @Override
    public void expirateRecords(long expirationTime) throws CygnusExpiratingError {
        LOGGER.debug("[" + this.getName() + "] Expirating records (time=" + expirationTime + ")");
        try {
            postgreSQLPersistenceBackend.expirateRecordsCache(expirationTime);
        } catch (CygnusRuntimeError e) {
            throw new CygnusExpiratingError("Data expiration error", "CygnusRuntimeError", e.getMessage());
        } catch (CygnusPersistenceError e) {
            throw new CygnusExpiratingError("Data expiration error", "CygnusPersistenceError", e.getMessage());
        } // try catch
    } // expirateRecords

    protected NGSIGenericAggregator getAggregator(boolean rowAttrPersistence) {
        if (rowAttrPersistence) {
            return new NGSIGenericRowAggregator();
        } else {
            return new NGSIGenericColumnAggregator();
        } // if else
    } // getAggregator

    private void persistAggregation(NGSIGenericAggregator aggregator) throws CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
        String fieldsForCreate = NGSIUtils.getFieldsForCreate(aggregator.getAggregationToPersist());
        String fieldsForInsert = NGSIUtils.getFieldsForInsert(aggregator.getAggregationToPersist());
        String valuesForInsert = NGSIUtils.getValuesForInsert(aggregator.getAggregationToPersist(), aggregator.isAttrNativeTypes());
        String schemaName = aggregator.getSchemeName(enableLowercase);
        String databaseName = aggregator.getDbName(enableLowercase);
        String tableName = aggregator.getTableName(enableLowercase);

        // Escape a syntax error in SQL
        if (schemaName.equals(DEFAULT_FIWARE_SERVICE)) {
            schemaName = ESCAPED_DEFAULT_FIWARE_SERVICE;
        }

        LOGGER.debug("[" + this.getName() + "] Persisting data at NGSIPostgreSQLSink. Database (" + databaseName + ")  Schema ("
                + schemaName + "), Table (" + tableName + "), Fields (" + fieldsForInsert + "), Values ("
                + valuesForInsert + ")");
        
        try {
            if (aggregator instanceof NGSIGenericRowAggregator) {
                postgreSQLPersistenceBackend.createDestination(schemaName);
                postgreSQLPersistenceBackend.createTable(databaseName, schemaName, tableName, fieldsForCreate);
            } // if
            // creating the database and the table has only sense if working in row mode, in column node
            // everything must be provisioned in advance

            if (valuesForInsert.equals("")) {
                LOGGER.debug("[" + this.getName() + "] no values for insert");
            } else {
                if (lastData && !rowAttrPersistence ) {
                    postgreSQLPersistenceBackend.upsertTransaction(aggregator.getAggregationToPersist(),
                            aggregator.getLastDataToPersist(),
                            schemaName,
                            tableName,
                            lastDataTableSuffix,
                            lastDataUniqueKey,
                            lastDataTimeStampKey,
                            lastDataSQLTimestampFormat,
                            attrNativeTypes);
                } else {
                    postgreSQLPersistenceBackend.insertContextData(databaseName, schemaName, tableName, fieldsForInsert, valuesForInsert);
                }
            }
        } catch (Exception e) {
            throw new CygnusPersistenceError("-, " + e.getMessage());
        } // try catch
    } // persistAggregation

    /**
     * Creates a PostgreSQL DB name given the FIWARE service.
     * @param service
     * @return The PostgreSQL DB name
     * @throws CygnusBadConfiguration
     */
    public String buildDBName(String service) throws CygnusBadConfiguration {
        String name = null;

        if (enableEncoding) {
            switch(dataModel) {
                case DMBYENTITYDATABASE:
                case DMBYENTITYDATABASESCHEMA:
                    if (service != null)
                        name = NGSICharsets.encodePostgreSQL(service);
                    break;
                default:
                    name = postgresqlDatabase;
            }
        } else {
            switch(dataModel) {
                case DMBYENTITYDATABASE:
                case DMBYENTITYDATABASESCHEMA:
                    if (service != null)
                        name = NGSIUtils.encode(service, false, true);
                    break;
                default:
                    name = postgresqlDatabase;
            }
        } // if else
        if (name.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building DB name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildSchemaName

    /**
     * Creates a PostgreSQL scheme name given the FIWARE service.
     * @param service
     * @return The PostgreSQL scheme name
     * @throws CygnusBadConfiguration
     */
    public String buildSchemaName(String service, String subService) throws CygnusBadConfiguration {
        String name;

        if (enableEncoding) {
            switch(dataModel) {
                case DMBYENTITYDATABASESCHEMA:
                    name = NGSICharsets.encodePostgreSQL(subService);
                    break;
                default:
                    name = NGSICharsets.encodePostgreSQL(service);
            }
        } else {
            switch(dataModel) {
                case DMBYENTITYDATABASESCHEMA:
                    name = NGSIUtils.encode(subService, false, true);
                    break;
                default:
                    name = NGSIUtils.encode(service, false, true);
            }
        } // if else

        if (name.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building schema name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildSchemaName

    /**
     * Creates a PostgreSQL table name given the FIWARE service path, the entity and the attribute.
     * @param servicePath
     * @param entity
     * @param attribute
     * @return The PostgreSQL table name
     * @throws CygnusBadConfiguration
     */
    public String buildTableName(String servicePath, String entity, String entityType, String attribute) throws CygnusBadConfiguration {
        String name;

        if (enableEncoding) {
            switch(dataModel) {
                case DMBYSERVICEPATH:
                    name = NGSICharsets.encodePostgreSQL(servicePath);
                    break;
                case DMBYENTITYDATABASE:
                case DMBYENTITYDATABASESCHEMA:
                case DMBYENTITY:
                    name = NGSICharsets.encodePostgreSQL(servicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodePostgreSQL(entity);
                    break;
                case DMBYENTITYTYPE:
                    name = NGSICharsets.encodeMySQL(servicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodeMySQL(entityType);
                    break;
                case DMBYATTRIBUTE:
                    name = NGSICharsets.encodePostgreSQL(servicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodePostgreSQL(entity)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodePostgreSQL(attribute);
                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use dm-by-service-path, dm-by-entity or dm-by-attribute");
            } // switch
        } else {
            switch(dataModel) {
                case DMBYSERVICEPATH:
                    if (servicePath.equals("/")) {
                        throw new CygnusBadConfiguration("Default service path '/' cannot be used with "
                                + "dm-by-service-path data model");
                    } // if
                    
                    name = NGSIUtils.encode(servicePath, true, false);
                    break;
                case DMBYENTITYDATABASE:
                case DMBYENTITYDATABASESCHEMA:
                case DMBYENTITY:
                    String truncatedServicePath = NGSIUtils.encode(servicePath, true, false);
                    name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '_')
                            + NGSIUtils.encode(entity, false, true);
                    break;
                case DMBYENTITYTYPE:
                    truncatedServicePath = NGSIUtils.encode(servicePath, true, false);
                    name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '_')
                            + NGSIUtils.encode(entityType, false, true);
                    break;
                case DMBYATTRIBUTE:
                    truncatedServicePath = NGSIUtils.encode(servicePath, true, false);
                    name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '_')
                            + NGSIUtils.encode(entity, false, true)
                            + '_' + NGSIUtils.encode(attribute, false, true);
                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use DMBYSERVICEPATH, DMBYENTITY, DMBYENTITYTYPE or DMBYATTRIBUTE");
            } // switch
        } // if else

        if (name.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building table name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildTableName

} // NGSIPostgreSQLSink
