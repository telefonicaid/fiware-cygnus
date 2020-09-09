/**
 * Copyright 2020 Telefonica
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


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telefonica.iot.cygnus.backends.sql.SQLBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequestLD;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSILDEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.*;

import java.util.ArrayList;
import java.util.Map;

import org.apache.flume.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author anmunoz
 */
public class NGSIPostgisSink extends NGSILDSink {

    private static final String DEFAULT_ROW_ATTR_PERSISTENCE = "row";
    private static final String DEFAULT_PASSWORD = "example";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_USER_NAME = "postgres";
    private static final String DEFAULT_DATABASE = "postgres";
    private static final String DEFAULT_ENABLE_CACHE = "false";
    private static final int DEFAULT_MAX_POOL_SIZE = 3;
    private static final String DEFAULT_POSTGIS_TYPE = "geometry";
    private static final String DEFAULT_ATTR_NATIVE_TYPES = "false";
    private static final String POSTGIS_DRIVER_NAME = "org.postgresql.Driver";
    private static final String POSTGIS_INSTANCE_NAME = "postgresql";

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIPostgisSink.class);
    private String postgisHost;
    private String postgisPort;
    private String postgisDatabase;
    private String postgisUsername;
    private String postgisPassword;
    private boolean rowAttrPersistence;
    private int maxPoolSize;
    private SQLBackendImpl postgisPersistenceBackend;
    private boolean enableCache;
    private boolean swapCoordinates;
    private boolean attrNativeTypes;
    private boolean attrMetadataStore;
    private String postgisOptions;

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
     * Gets the Postgis options. It is protected due to it is only required for testing purposes.
     * @return The Postgis options
     */
    protected String getPostgisOptions() {
        return postgisOptions;
    } // getPostgisOptions

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
        return postgisPersistenceBackend;
    } // getPersistenceBackend

    /**
     * Sets the persistence backend. It is protected due to it is only required for testing purposes.
     * @param postgisPersistenceBackend
     */
    protected void setPersistenceBackend(SQLBackendImpl postgisPersistenceBackend) {
        this.postgisPersistenceBackend = postgisPersistenceBackend;
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

        String attrMetadataStoreSrt = context.getString("attr_metadata_store", "true");

        if (attrMetadataStoreSrt.equals("true") || attrMetadataStoreSrt.equals("false")) {
            attrMetadataStore = Boolean.parseBoolean(attrMetadataStoreSrt);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_metadata_store="
                    + attrMetadataStore + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (attr_metadata_store="
                    + attrNativeTypesStr + ") -- Must be 'true' or 'false'");
        } // if else

        postgisOptions = context.getString("postgis_options", null);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgis_options=" + postgisOptions + ")");

    } // configure

    @Override
    public void stop() {
        super.stop();
        if (postgisPersistenceBackend != null) postgisPersistenceBackend.close();
    } // stop

    @Override
    public void start() {
        try {
            if (buildDBName(null) != null) {
                createPersistenceBackend(postgisHost, postgisPort, postgisUsername, postgisPassword, maxPoolSize, buildDBName(null), postgisOptions);
            }
        } catch (Exception e) {
            LOGGER.error("Error while creating the Postgis persistence backend. Details="
                    + e.getMessage());
        } // try catch

        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

    /**
     * Initialices a lazy singleton to share among instances on JVM
     */
    private void createPersistenceBackend(String sqlHost, String sqlPort, String sqlUsername, String sqlPassword, int maxPoolSize, String defaultSQLDataBase, String sqlOptions) {
        if (postgisPersistenceBackend == null) {
            postgisPersistenceBackend = new SQLBackendImpl(sqlHost, sqlPort, sqlUsername, sqlPassword, maxPoolSize, POSTGIS_INSTANCE_NAME, POSTGIS_DRIVER_NAME, defaultSQLDataBase, sqlOptions);
        }
    }

    @Override
    void persistBatch(NGSILDBatch batch)
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
            ArrayList<NGSILDEvent> events = batch.getNextEvents();

            // get an aggregator for this destination and initialize it
            NGSIPostgisSink.PostgisAggregator aggregator = getAggregator(rowAttrPersistence);
            aggregator.initialize(events.get(0));

            for (NGSILDEvent event : events) {
                aggregator.aggregate(event);
            } // for

            // persist the fieldValues
            persistAggregation(aggregator);
            batch.setNextPersisted(true);
        } // for
    } // persistBatch
    
    @Override
    public void capRecords(NGSILDBatch batch, long maxRecords) throws CygnusCappingError {
    } // capRecords

    @Override
    public void expirateRecords(long expirationTime) throws CygnusExpiratingError {
    } // expirateRecords

    private NGSIPostgisSink.PostgisAggregator getAggregator(boolean rowAttrPersistence) {
        if (rowAttrPersistence) {
            return new NGSIPostgisSink.RowAggregator();
        } else {
            return new NGSIPostgisSink.ColumnAggregator();
        } // if else
    } // getAggregator

    private void persistAggregation(NGSIPostgisSink.PostgisAggregator aggregator) throws CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
        String typedFieldNames = aggregator.getTypedFieldNames();
        String fieldNames = aggregator.getFieldNames();
        String fieldValues = aggregator.getAggregation();
        String schemaName = aggregator.getSchemaName(enableLowercase);
        String tableName = aggregator.getTableName(enableLowercase);

        LOGGER.info("[" + this.getName() + "] Persisting data at NGSIPostgreSQLSink. Schema ("
                + schemaName + "), Table (" + tableName + "), Fields (" + fieldNames + "), Values ("
                + fieldValues + ")");

        try {
            if (aggregator instanceof NGSIPostgisSink.RowAggregator) {
                postgisPersistenceBackend.createDestination(schemaName);
                postgisPersistenceBackend.createTable(schemaName, tableName, typedFieldNames);
            } else if (aggregator instanceof NGSIPostgisSink.ColumnAggregator){
                postgisPersistenceBackend.createDestination(schemaName);
                postgisPersistenceBackend.createTable(schemaName, tableName, typedFieldNames);
            }// if
            // creating the database and the table has only sense if working in row mode, in column node
            // everything must be provisioned in advance

            postgisPersistenceBackend.insertContextData(schemaName, tableName, fieldNames, fieldValues);
        } catch (Exception e) {
            throw new CygnusPersistenceError("-, " + e.getMessage());
        } // try catch
    } // persistAggregation

    /**
     * Creates a Postgis DB name given the FIWARE service.
     * @param service
     * @return The Postgis DB name
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
                    name = postgisDatabase;
            }
        } else {
            switch(dataModel) {
                case DMBYENTITYDATABASE:
                case DMBYENTITYDATABASESCHEMA:
                    if (service != null)
                        name = NGSIUtils.encode(service, false, true);
                    break;
                default:
                    name = postgisDatabase;
            }
        } // if else
        if (name.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building DB name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildSchemaName

    /**
     * Creates a Postgis scheme name given the FIWARE service.
     * @param service
     * @return The Postgis scheme name
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
     * @param entity
     * @return The Postgis table name
     * @throws CygnusBadConfiguration
     */
    public String buildTableName(String entity, String entityType) throws CygnusBadConfiguration {
        String name;

        if (enableEncoding) {
            switch(dataModel) {

                case DMBYENTITY:
                    name = NGSICharsets.encodePostgreSQL(entity);
                    break;
                case DMBYENTITYTYPE:
                    name = NGSICharsets.encodePostgreSQL(entityType);
                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use dm-by-entity or dm-by-entity-type");
            } // switch
        } else {
            switch(dataModel) {

                case DMBYENTITY:
                    name = NGSIUtils.encodePostgreSQL(entity);
                    break;
                case DMBYENTITYTYPE:
                    name = (NGSIUtils.encodePostgreSQL(entityType));

                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use DMBYENTITY or DMBYENTITYTYPE");
            } // switch
        } // if else

        if (name.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building table name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildTableName

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
        protected String entityTypeForNaming;
        protected String attributeForNaming;
        protected String schemaName;
        protected String tableName;
        protected String typedFieldNames;
        protected String fieldNames;

        public PostgisAggregator() {
            aggregation = "";
        } // PostgreSQLAggregator

        public String getAggregation() {
            return aggregation;
        } // getAggregation

        public String getSchemaName(boolean enableLowercase) {
            if (enableLowercase) {
                return schemaName.toLowerCase();
            } else {
                return schemaName;
            } // if else
        } // getDbName

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

        public void initialize(NGSILDEvent event) throws CygnusBadConfiguration {
            service = event.getServiceForNaming();
            servicePathForData = event.getServicePathForData();
            servicePathForNaming = event.getServicePathForNaming(enableGrouping);
            entityForNaming = event.getEntityForNaming(enableGrouping, enableEncoding);
            entityTypeForNaming = event.getEntityTypeForNaming(enableEncoding);
            attributeForNaming = event.getAttributeForNaming();
            schemaName = buildSchemaName(service);
            tableName = buildTableName(entityForNaming, entityTypeForNaming);
        } // initialize

        public abstract void aggregate(NGSILDEvent cygnusEvent);

    } // PostgreSQLAggregator

    /**
     * Class for aggregating batches in row mode.
     */
    private class RowAggregator extends NGSIPostgisSink.PostgisAggregator {

        @Override
        public void initialize(NGSILDEvent cygnusEvent) throws CygnusBadConfiguration {
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
        public void aggregate(NGSILDEvent event) {
            // get the getRecvTimeTs headers
            long recvTimeTs = event.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the getRecvTimeTs body
            NotifyContextRequestLD.ContextElement contextElement = event.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

        } // aggregate

    } // RowAggregator

    /**
     * Class for aggregating batches in column mode.
     */
    private class ColumnAggregator extends NGSIPostgisSink.PostgisAggregator {

        @Override
        public void initialize(NGSILDEvent cygnusEvent) throws CygnusBadConfiguration {
            super.initialize(cygnusEvent);

            // particulat initialization
            typedFieldNames = "(" + NGSIConstants.RECV_TIME + " text,"
                    + NGSIConstants.ENTITY_ID + " text,"
                    + NGSIConstants.ENTITY_TYPE + " text";
            fieldNames = "(" + NGSIConstants.RECV_TIME + ","
                    + NGSIConstants.ENTITY_ID + ","
                    + NGSIConstants.ENTITY_TYPE;

            // iterate on all this context element attributes, if there are attributes
            Map<String, Object> contextAttributes = cygnusEvent.getContextElement().getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                return;
            } // if

            for (Map.Entry<String, Object> entry : contextAttributes.entrySet()) {
                String x = entry.getKey();
                String attrName =x;
                JsonObject ya = (JsonObject) entry.getValue();
                String subAttrName="";
                if ("GeoProperty".equalsIgnoreCase(ya.get("type").getAsString())){
                    typedFieldNames += "," + attrName + " geometry";
                }else{
                    typedFieldNames += "," + attrName + " text";
                }
                fieldNames += "," + attrName;
                if (isValid(entry.getValue().toString())) {
                    JsonObject y = (JsonObject) entry.getValue();
                    for (Map.Entry<String, JsonElement> entry2 : y.entrySet()) {
                        String x2 = entry2.getKey();
                        Object y2 = entry2.getValue();
                        if (!"type".contentEquals(x2) && !"value".contentEquals(x2)
                                && !"object".contentEquals(x2)) {
                            subAttrName = x2;
                            typedFieldNames += "," + attrName + "_" + subAttrName + " text";
                            fieldNames += "," + attrName + "_" + subAttrName;
                        }
                    }
                }
            }
            typedFieldNames += ")";
            fieldNames += ")";
        } // initialize

        @Override
        public void aggregate(NGSILDEvent cygnusEvent) {
            // get the getRecvTimeTs headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the getRecvTimeTs body
            NotifyContextRequestLD.ContextElement contextElement = cygnusEvent.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

            // iterate on all this context element attributes, if there are attributes
            Map<String, Object> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            String column = "('" + recvTime + "','" + entityId + "','" + entityType + "'";
            for (Map.Entry<String, Object> entry : contextAttributes.entrySet()) {
                String x = entry.getKey();
                String attrValue ="";
                String attrType ="";
                String subAttrName="";
                String subAttrType="";
                if (isValid(entry.getValue().toString())){
                    JsonObject y = (JsonObject) entry.getValue();
                    attrType = y.get("type").getAsString();
                    if ("Relationship".contentEquals(attrType)){
                        attrValue ="'"+y.get("object").getAsString()+"'";
                    }else if ("Property".contentEquals(attrType)){
                        attrValue =(y.get("value").isJsonObject()) ? "'"+y.get("value").toString()+"'": "'"+y.get("value").getAsString()+"'";
                    }else if ("GeoProperty".contentEquals(attrType)){
                        JsonArray coordinates = y.getAsJsonObject("value").get("coordinates").getAsJsonArray();
                        JsonArray points;
                        JsonArray polygon;
                        String point="";
                        switch (y.getAsJsonObject("value").get("type").getAsString()){
                            case "Point":
                                point = coordinates.get(0)+" "+coordinates.get(1);
                                attrValue ="'POINT"+ "("+ point +")'";
                                break;
                            case "MultiPoint":
                                for (int i =0 ; i<coordinates.size(); i++){
                                    points = coordinates.get(i).getAsJsonArray();
                                    if (i==coordinates.size()-1){
                                        point+="("+points.get(0)+" "+points.get(1)+")";
                                    }else{
                                        point+="("+points.get(0)+" "+points.get(1)+ "),";
                                    }
                                }
                                attrValue ="'MULTIPOINT"+ "("+ point+")'";
                                break;
                            case "LineString":
                                for (int i =0 ; i<coordinates.size(); i++){
                                    points = coordinates.get(i).getAsJsonArray();
                                    if (i==coordinates.size()-1){
                                        point+=points.get(0)+" "+points.get(1);
                                    }else{
                                        point+=points.get(0)+" "+points.get(1)+ ",";
                                    }
                                }
                                attrValue ="'LINESTRING"+ "("+ point+")'";
                                break;
                            case "MultiLineString":
                                for (int i =0 ; i<coordinates.size(); i++){
                                    polygon = coordinates.get(i).getAsJsonArray();
                                    for (int j =0;j<polygon.size();j++){
                                        points = polygon.get(j).getAsJsonArray();
                                        if (j==polygon.size()-1 && i==coordinates.size()-1) {
                                            point += points.get(0) + " " + points.get(1) + ")";
                                        }else if (j==polygon.size()-1 && i!=coordinates.size()-1){
                                            point += points.get(0) + " " + points.get(1) + "),";
                                        }else if (j==0){
                                            point += "("+points.get(0)+" "+points.get(1)+ ",";
                                        }else {
                                            point+=points.get(0)+" "+points.get(1)+ ",";
                                        }
                                    }
                                }
                                attrValue ="'MULTILINESTRING"+ "("+ point+")'";
                                break;
                            case "Polygon":
                                for (int i =0 ; i<coordinates.size(); i++){
                                    polygon = coordinates.get(i).getAsJsonArray();
                                    for (int j =0;j<polygon.size();j++){
                                        points = polygon.get(j).getAsJsonArray();
                                        if (j==polygon.size()-1 && i !=coordinates.size()-1 ) {
                                            point += points.get(0)+" "+points.get(1) + "),";
                                        }else if (j==0){
                                            point += "("+points.get(0)+" "+points.get(1)+ ",";
                                        }else if (j==polygon.size()-1 && i ==coordinates.size()-1 ){
                                            point += points.get(0)+" "+points.get(1) + ")";
                                        }else {
                                            point+=points.get(0)+" "+points.get(1)+ ",";
                                        }
                                    }
                                }
                                attrValue ="'POLYGON"+ "("+point+")'";
                                break;
                        }
                    }
                    column += "," +attrValue;

                    for (Map.Entry<String, JsonElement> entry2 : y.entrySet()) {
                        String x2 = entry2.getKey();
                        Object y2 = entry2.getValue();
                        if ("observedAt".contentEquals(x2)){
                            column += "," + "'"+entry2.getValue().getAsString()+"'";
                        }
                        if (!"observedAt".contentEquals(x2) && !"type".contentEquals(x2) && !"value".contentEquals(x2) && !"object".contentEquals(x2)) {
                            if (entry2.getValue().isJsonObject()){
                                JsonObject subAttrJson = entry2.getValue().getAsJsonObject();
                                subAttrType = subAttrJson.get("type").getAsString();
                                if ("Relationship".contentEquals(subAttrType)){
                                    subAttrName ="'"+subAttrJson.get("object").getAsString()+"'";
                                }else if ("Property".contentEquals(subAttrType)){
                                    subAttrName ="'"+subAttrJson.get("value").getAsString()+"'";
                                }else if ("GeoProperty".contentEquals(subAttrType)){
                                    subAttrName ="'"+subAttrJson.get("value").toString()+"'";
                                }
                            }
                            column += "," + subAttrName ;
                        }
                    }
                }else {
                    attrValue= entry.getValue().toString();
                    column += ",'"+attrValue+"'";
                }
            }
            // for

            // now, aggregate the column
            if (aggregation.isEmpty()) {
                aggregation += column + ")";
            } else {
                aggregation += "," + column + ")";
            } // if else
        } // aggregate

    } // ColumnAggregator

    public boolean isValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

} // NGSIPostgisSink
