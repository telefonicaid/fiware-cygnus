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
            PostgisAggregator aggregator = getAggregator(rowAttrPersistence);
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
    } // expirateRecords

    /**
     * Class for aggregating fieldValues.
     */
    private abstract class PostgisAggregator {

        // string containing the data fieldValues
        protected String aggregation;

        protected String service;
        protected String servicePathForData;
        protected String servicePathForNaming;
        protected String entityForNaming;
        protected String attributeForNaming;
        protected String schemaName;
        protected String tableName;
        protected String typedFieldNames;
        protected String fieldNames;

        public PostgisAggregator() {
            aggregation = "";
        } // PostgisAggregator

        public String getAggregation() {
            return aggregation;
        } // getAggregation

        public String getSchemaName(boolean enableLowercase) {
            if (enableLowercase) {
                return schemaName.toLowerCase();
            } else {
                return schemaName;
            } // if else
        } // getSchemaDbName

        public String getTableName(boolean enableLowercase) {
            if (enableLowercase) {
                return tableName.toLowerCase();
            } else {
                return tableName;
            } // if else
        } // getTableName

        public String getTypedFieldNames() {
            return typedFieldNames;
        } // getTypedFieldNames

        public String getFieldNames() {
            return fieldNames;
        } // getFieldNames

        public void initialize(NGSIEvent event) throws CygnusBadConfiguration {
            service = event.getServiceForNaming(enableNameMappings);
            servicePathForData = event.getServicePathForData();
            servicePathForNaming = event.getServicePathForNaming(enableGrouping, enableNameMappings);
            entityForNaming = event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding);
            attributeForNaming = event.getAttributeForNaming(enableNameMappings);
            schemaName = buildSchemaName(service);
            tableName = buildTableName(servicePathForNaming, entityForNaming, attributeForNaming);
        } // initialize

        public abstract void aggregate(NGSIEvent cygnusEvent);

    } // PostgisAggregator

    /**
     * Class for aggregating batches in row mode.
     */
    protected class RowAggregator extends PostgisAggregator {

        @Override
        public void initialize(NGSIEvent cygnusEvent) throws CygnusBadConfiguration {
            super.initialize(cygnusEvent);
            typedFieldNames = "("
                    + NGSIConstants.RECV_TIME_TS + " bigint,"
                    + NGSIConstants.RECV_TIME + " text,"
                    + NGSIConstants.FIWARE_SERVICE_PATH + " text,"
                    + NGSIConstants.ENTITY_ID + " text,"
                    + NGSIConstants.ENTITY_TYPE + " text,"
                    + NGSIConstants.ATTR_NAME + " text,"
                    + NGSIConstants.ATTR_TYPE + " text,"
                    + NGSIConstants.ATTR_VALUE + " text,"
                    + NGSIConstants.ATTR_MD + " text"
                    + ")";
            fieldNames = "("
                    + NGSIConstants.RECV_TIME_TS + ","
                    + NGSIConstants.RECV_TIME + ","
                    + NGSIConstants.FIWARE_SERVICE_PATH + ","
                    + NGSIConstants.ENTITY_ID + ","
                    + NGSIConstants.ENTITY_TYPE + ","
                    + NGSIConstants.ATTR_NAME + ","
                    + NGSIConstants.ATTR_TYPE + ","
                    + NGSIConstants.ATTR_VALUE + ","
                    + NGSIConstants.ATTR_MD
                    + ")";
        } // initialize

        @Override
        public void aggregate(NGSIEvent event) {
            // get the getRecvTimeTs headers
            long recvTimeTs = event.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the getRecvTimeTs body
            ContextElement contextElement = event.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                ImmutablePair<String, Boolean> location =
                    NGSIUtils.getGeometry(attrValue,
                                          attrType,
                                          attrMetadata,
                                          swapCoordinates);

                // create a column and aggregate it
                String row = "('"
                    + recvTimeTs + "','"
                    + recvTime + "','"
                    + servicePathForData + "','"
                    + entityId + "','"
                    + entityType + "','"
                    + attrName + "','";

                if (location.right) {
                    LOGGER.debug("location=" + location.getLeft());
                    row += DEFAULT_POSTGIS_TYPE + "','" + location.getLeft() + "','"  + attrMetadata + "')";
                } else {
                    if (attrNativeTypes && attrType.equals("Number")) {
                        row += attrType + "'," + attrValue + ",'"  + attrMetadata + "')";
                    } else {
                        row += attrType + "','" + attrValue + "','"  + attrMetadata + "')";
                    }
                }

                if (aggregation.isEmpty()) {
                    aggregation += row;
                } else {
                    aggregation += "," + row;
                } // if else
            } // for
        } // aggregate

    } // RowAggregator

    /**
     * Class for aggregating batches in column mode.
     */
    protected class ColumnAggregator extends PostgisAggregator {

        @Override
        public void initialize(NGSIEvent cygnusEvent) throws CygnusBadConfiguration {
            super.initialize(cygnusEvent);

            // particulat initialization
            typedFieldNames = "(" + NGSIConstants.RECV_TIME + " text,"
                    + NGSIConstants.FIWARE_SERVICE_PATH + " text,"
                    + NGSIConstants.ENTITY_ID + " text,"
                    + NGSIConstants.ENTITY_TYPE + " text";
            fieldNames = "(" + NGSIConstants.RECV_TIME + ","
                    + NGSIConstants.FIWARE_SERVICE_PATH + ","
                    + NGSIConstants.ENTITY_ID + ","
                    + NGSIConstants.ENTITY_TYPE;

            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = cygnusEvent.getContextElement().getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                return;
            } // if

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();

                if (!NGSIUtils.getGeometry(attrValue,
                                           attrType,
                                           attrMetadata,
                                           swapCoordinates).getRight()) {
                    typedFieldNames += "," + attrName + " text," + attrName + "_md text";
                    fieldNames += "," + attrName + "," + attrName + "_md";
                } else {
                    typedFieldNames += "," + attrName + " " + DEFAULT_POSTGIS_TYPE + "," + attrName + "_md text";
                    fieldNames += "," + attrName + "," + attrName + "_md";
                }
            } // for

            typedFieldNames += ")";
            fieldNames += ")";
        } // initialize

        @Override
        public void aggregate(NGSIEvent cygnusEvent) {
            // get the getRecvTimeTs headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the getRecvTimeTs body
            ContextElement contextElement = cygnusEvent.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            String column = "('" + recvTime + "','" + servicePathForData + "','" + entityId + "','" + entityType + "'";

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                ImmutablePair<String, Boolean> location =
                    NGSIUtils.getGeometry(attrValue,
                                          attrType,
                                          attrMetadata,
                                          swapCoordinates);

                if (location.right) {
                    LOGGER.debug("location=" + location.getLeft());
                    column += "," + location.getLeft() + ",'"  + attrMetadata + "'";

                } else {
                    // create part of the column with the current attribute (a.k.a. a column)
                    if (attrNativeTypes && attrType.equals("Number")) {
                        column += "," + attrValue + ",'"  + attrMetadata + "'";
                    } else {
                        column += ",'" + attrValue + "','"  + attrMetadata + "'";
                    }
                }
            } // for

            // now, aggregate the column
            if (aggregation.isEmpty()) {
                aggregation += column + ")";
            } else {
                aggregation += "," + column + ")";
            } // if else
        } // aggregate

    } // ColumnAggregator

    private PostgisAggregator getAggregator(boolean rowAttrPersistence) {
        if (rowAttrPersistence) {
            return new RowAggregator();
        } else {
            return new ColumnAggregator();
        } // if else
    } // getAggregator

    private void persistAggregation(PostgisAggregator aggregator) throws CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
        String typedFieldNames = aggregator.getTypedFieldNames();
        String fieldNames = aggregator.getFieldNames();
        String fieldValues = aggregator.getAggregation();
        String schemaName = aggregator.getSchemaName(enableLowercase);
        String tableName = aggregator.getTableName(enableLowercase);

        LOGGER.info("[" + this.getName() + "] Persisting data at NGSIPostgisSink. Schema ("
                + schemaName + "), Table (" + tableName + "), Fields (" + fieldNames + "), Values ("
                + fieldValues + ")");
        
        if (aggregator instanceof RowAggregator) {
            persistenceBackend.createSchema(schemaName);
            persistenceBackend.createTable(schemaName, tableName, typedFieldNames);
        } // if
        // creating the database and the table has only sense if working in row mode, in column node
        // everything must be provisioned in advance
        if (fieldValues.equals("")) {
            LOGGER.debug("[" + this.getName() + "] no values for insert");
        } else {
            persistenceBackend.insertContextData(schemaName, tableName, fieldNames, fieldValues);
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
    public String buildTableName(String servicePath, String entity, String attribute) throws CygnusBadConfiguration {
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
                case DMBYATTRIBUTE:
                    truncatedServicePath = NGSIUtils.encode(servicePath, true, false);
                    name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '_')
                            + NGSIUtils.encode(entity, false, true)
                            + '_' + NGSIUtils.encode(attribute, false, true);
                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use DMBYSERVICEPATH, DMBYENTITY or DMBYATTRIBUTE");
            } // switch
        } // if else

        if (name.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building table name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildTableName

} // NGSIPostgisSink
