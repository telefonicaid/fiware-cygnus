/**
 * Copyright 2019 Telefonica
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

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericColumnAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericRowAggregator;
import com.telefonica.iot.cygnus.backends.postgresql.PostgreSQLBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.utils.NGSICharsets;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.flume.Context;

/**
 *
 * @author smartcities

 Detailed documentation can be found at:
  *** TDB *** 
 */
public class NGSIPostgisSink extends NGSISink {

    private static final String DEFAULT_ROW_ATTR_PERSISTENCE = "row";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_USER_NAME = "postgres";
    private static final String DEFAULT_DATABASE = "postgres";
    private static final String DEFAULT_ENABLE_CACHE = "false";
    private static final int DEFAULT_MAX_POOL_SIZE = 3;
    private static final String DEFAULT_POSTGIS_TYPE = "geometry";
    private static final String DEFAULT_ATTR_NATIVE_TYPES = "false";

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIPostgisSink.class);
    private String postgisHost;
    private String postgisPort;
    private String postgisDatabase;
    private String postgisUsername;
    private String postgisPassword;
    private boolean rowAttrPersistence;
    private int maxPoolSize;
    private PostgreSQLBackendImpl persistenceBackend;
    private boolean enableCache;
    private boolean swapCoordinates;
    private boolean attrNativeTypes;

    /**
     * Constructor.
     */
    public NGSIPostgisSink() {
        super();
    } // NGSIPostgisSink

    /**
     * Gets the Postgis host. It is protected due to it is only required for testing purposes.
     * @return The Postgis host
     */
    protected String getPostgisHost() {
        return postgisHost;
    } // getPostgisHost
    
    /**
     * Gets the Postgis cache. It is protected due to it is only required for testing purposes.
     * @return The Postgis cache state
     */
    protected boolean getEnableCache() {
        return enableCache;
    } // getEnableCache

    /**
     * Gets the Postgis port. It is protected due to it is only required for testing purposes.
     * @return The Postgis port
     */
    protected String getPostgisPort() {
        return postgisPort;
    } // getPostgisPort

    /**
     * Gets the Postgis database. It is protected due to it is only required for testing purposes.
     * @return The Postgis database
     */
    protected String getPostgisDatabase() {
        return postgisDatabase;
    } // getPostgisDatabase

    /**
     * Gets the Postgis username. It is protected due to it is only required for testing purposes.
     * @return The Postgis username
     */
    protected String getPostgisUsername() {
        return postgisUsername;
    } // getPostgisUsername

    /**
     * Gets the Postgis password. It is protected due to it is only required for testing purposes.
     * @return The Postgis password
     */
    protected String getPostgisPassword() {
        return postgisPassword;
    } // getPostgisPassword

    /**
     * Returns if the attribute persistence is row-based. It is protected due to it is only required for testing
     * purposes.
     * @return True if the attribute persistence is row-based, false otherwise
     */
    protected boolean getRowAttrPersistence() {
        return rowAttrPersistence;
    } // getRowAttrPersistence

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
    protected PostgreSQLBackendImpl getPersistenceBackend() {
        return persistenceBackend;
    } // getPersistenceBackend

    /**
     * Sets the persistence backend. It is protected due to it is only required for testing purposes.
     * @param persistenceBackend
     */
    protected void setPersistenceBackend(PostgreSQLBackendImpl persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    } // setPersistenceBackend

    @Override
    public void configure(Context context) {
        // Read NGSISink general configuration
        super.configure(context);
        
        // Impose enable lower case, since Postgis only accepts lower case
        enableLowercase = true;
        
        postgisHost = context.getString("postgis_host", DEFAULT_HOST);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgis_host=" + postgisHost + ")");
        postgisPort = context.getString("postgis_port", DEFAULT_PORT);
        int intPort = Integer.parseInt(postgisPort);

        if ((intPort <= 0) || (intPort > 65535)) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (postgis_port=" + postgisPort + ")"
                    + " -- Must be between 0 and 65535");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (postgis_port=" + postgisPort + ")");
        }  // if else

        postgisDatabase = context.getString("postgis_database", DEFAULT_DATABASE);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgis_database=" + postgisDatabase + ")");
        postgisUsername = context.getString("postgis_username", DEFAULT_USER_NAME);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgis_username=" + postgisUsername + ")");
        // FIXME: postgisPassword should be read as a SHA1 and decoded here
        postgisPassword = context.getString("postgis_password", DEFAULT_PASSWORD);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgis_password=" + postgisPassword + ")");

        maxPoolSize = context.getInteger("postgis_maxPoolSize", DEFAULT_MAX_POOL_SIZE);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgis_maxPoolSize=" + maxPoolSize + ")");

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
                + enableCacheStr + ") -- Must be 'true' or 'false'");
        }  // if else

        // TBD: possible option for postgisSink
        swapCoordinates = false;


        String attrNativeTypesStr = context.getString("attr_native_types", DEFAULT_ATTR_NATIVE_TYPES);
        if (attrNativeTypesStr.equals("true") || attrNativeTypesStr.equals("false")) {
            attrNativeTypes = Boolean.valueOf(attrNativeTypesStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_native_types=" + attrNativeTypesStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (attr_native_types="
                + attrNativeTypesStr + ") -- Must be 'true' or 'false'");
        } // if else
        
    } // configure

    @Override
    public void stop() {
        super.stop();
        if (persistenceBackend != null) persistenceBackend.close();
    } // stop

    @Override
    public void start() {
        try {
            persistenceBackend = new PostgreSQLBackendImpl(postgisHost, postgisPort, postgisDatabase, postgisUsername, postgisPassword, maxPoolSize);
        } catch (Exception e) {
            LOGGER.error("Error while creating the Postgis persistence backend. Details="
                    + e.getMessage());
        } // try catch

        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

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
            aggregator.initialize(events.get(0));

            for (NGSIEvent event : events) {
                aggregator.aggregate(event);
            } // for
            LOGGER.debug("[" + getName() + "] adding event to aggregator object  (name=" + aggregator.getFieldsForInsert()+ ", values="
                    + aggregator.getValuesForInsert() + ")");

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
    } // expirateRecords

    /**
     * Class for aggregating batches in row mode.
     */
    protected class RowAggregator extends NGSIGenericRowAggregator {

        private String servicePathForNaming;
        private String entityForNaming;
        private String entityType;
        private String attribute;

        /**
         * Instantiates a new Ngsi generic row aggregator.
         *
         * @param enableGrouping     the enable grouping flag for initialization
         * @param enableNameMappings the enable name mappings flag for initialization
         * @param enableEncoding     the enable encoding flag for initialization
         * @param enableGeoParse     the enable geo parse flag for initialization
         */
        protected RowAggregator(boolean enableGrouping, boolean enableNameMappings, boolean enableEncoding, boolean enableGeoParse) {
            super(enableGrouping, enableNameMappings, enableEncoding, enableGeoParse, false);
        }

        @Override
        public void initialize(NGSIEvent event) throws CygnusBadConfiguration {
            super.initialize(event);
            servicePathForNaming = event.getServicePathForNaming(enableGrouping, enableNameMappings);
            entityForNaming = event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding);
            entityType = event.getEntityTypeForNaming(enableGrouping, enableNameMappings);
            attribute = event.getAttributeForNaming(enableNameMappings);
            setDbName(buildSchemaName(event.getServiceForNaming(enableNameMappings)));
            setTableName(buildTableName(servicePathForNaming, entityForNaming, entityType, attribute));
        } // initialize

    } // RowAggregator

    /**
     * Class for aggregating batches in column mode.
     */
    protected class ColumnAggregator extends NGSIGenericColumnAggregator {

        private String servicePathForNaming;
        private String entityForNaming;
        private String entityType;
        private String attribute;

        /**
         * Instantiates a new Ngsi generic column aggregator.
         *
         * @param enableGrouping     the enable grouping
         * @param enableNameMappings the enable name mappings
         * @param enableEncoding     the enable encoding
         * @param enableGeoParse     the enable geo parse
         */
        public ColumnAggregator(boolean enableGrouping, boolean enableNameMappings, boolean enableEncoding, boolean enableGeoParse) {
            super(enableGrouping, enableNameMappings, enableEncoding, enableGeoParse, attrNativeTypes);
        }

        @Override
        public void initialize(NGSIEvent event) throws CygnusBadConfiguration {
            super.initialize(event);
            servicePathForNaming = event.getServicePathForNaming(enableGrouping, enableNameMappings);
            entityForNaming = event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding);
            entityType = event.getEntityTypeForNaming(enableGrouping, enableNameMappings);
            attribute = event.getAttributeForNaming(enableNameMappings);
            setDbName(buildSchemaName(event.getServiceForNaming(enableNameMappings)));
            setTableName(buildTableName(servicePathForNaming, entityForNaming, entityType, attribute));
        } // initialize

    } // ColumnAggregator

    protected NGSIGenericAggregator getAggregator(boolean rowAttrPersistence) {
        if (rowAttrPersistence) {
            return new RowAggregator(enableGrouping, enableNameMappings, enableEncoding, true);
        } else {
            return new ColumnAggregator(enableGrouping, enableNameMappings, enableEncoding, true);
        } // if else
    } // getAggregator

    private void persistAggregation(NGSIGenericAggregator aggregator) throws CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
        String fieldsForCreate = aggregator.getFieldsForCreate();
        String fieldsForInsert = aggregator.getFieldsForInsert();
        String valuesForInsert = aggregator.getValuesForInsert();
        String schemaName = aggregator.getDbName(enableLowercase);
        String tableName = aggregator.getTableName(enableLowercase);

        LOGGER.info("[" + this.getName() + "] Persisting data at NGSIPostgisSink. Schema ("
                + schemaName + "), Table (" + tableName + "), Fields (" + fieldsForInsert + "), Values ("
                + valuesForInsert + ")");

            if (aggregator instanceof RowAggregator) {
                persistenceBackend.createSchema(schemaName);
                persistenceBackend.createTable(schemaName, tableName, fieldsForCreate);
            } // if
            // creating the database and the table has only sense if working in row mode, in column node
            // everything must be provisioned in advance
            if (valuesForInsert.equals("")) {
                LOGGER.debug("[" + this.getName() + "] no values for insert");
            } else {
                persistenceBackend.insertContextData(schemaName, tableName, fieldsForInsert, valuesForInsert);
            }
    } // persistAggregation
    
    /**
     * Creates a Postgis DB name given the FIWARE service.
     * @param service
     * @return The Postgis DB name
     * @throws CygnusBadConfiguration
     */
    public String buildSchemaName(String service) throws CygnusBadConfiguration {
        String name;
        
        if (enableEncoding) {
            name = NGSICharsets.encodePostgreSQL(service);
        } else {
            name = NGSIUtils.encode(service, false, true);
        } // if else

        if (name.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building schema name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildSchemaName

    /**
     * Creates a Postgis table name given the FIWARE service path, the entity and the attribute.
     * @param servicePath
     * @param entity
     * @param attribute
     * @return The Postgis table name
     * @throws CygnusBadConfiguration
     */
    public String buildTableName(String servicePath, String entity, String entityType, String attribute) throws CygnusBadConfiguration {
        String name;

        if (enableEncoding) {
            switch(dataModel) {
                case DMBYSERVICEPATH:
                    name = NGSICharsets.encodePostgreSQL(servicePath);
                    break;
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

} // NGSIPostgisSink
