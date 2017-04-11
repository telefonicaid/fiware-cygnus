/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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

import com.telefonica.iot.cygnus.backends.cartodb.CartoDBBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.Enums.DataModel;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.utils.JsonUtils;
import com.telefonica.iot.cygnus.utils.NGSICharsets;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.flume.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author frb
 */
public class NGSICartoDBSink extends NGSISink {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(NGSICartoDBSink.class);
    private String keysConfFile;
    private boolean swapCoordinates;
    private boolean enableRawHistoric;
    private boolean enableDistanceHistoric;
    private boolean enableRawSnapshot;
    private int backendMaxConns;
    private int backendMaxConnsPerRoute;
    private HashMap<String, CartoDBBackendImpl> backends;
    
    /**
     * Constructor.
     */
    public NGSICartoDBSink() {
        super();
    } // NGSICartoDBSink
    
    /**
     * Gets the CartoDB backend. It is protected since it is only used by the tests.
     * @return The CartoDB backend
     */
    protected HashMap<String, CartoDBBackendImpl> getBackends() {
        return backends;
    } // getBackends
    
    /**
     * Sets the CartoDB backend. It is protected since it is only used by the tests.
     * @param backends
     */
    protected void setBackends(HashMap<String, CartoDBBackendImpl> backends) {
        this.backends = backends;
    } // setBackends
    
    /**
     * Gets the maximum number of connections in the backend.
     * @return The maximum number of connections in the backend
     */
    protected int getBackendMaxConns() {
        return backendMaxConns;
    } // getBackendMaxConns
    
    /**
     * Gets the maximum number of connections per route in the backend.
     * @return The maximum number of connections per route in the backend
     */
    protected int getBackendMaxConnsPerRoute() {
        return backendMaxConnsPerRoute;
    } // getBackendMaxConnsPerRoute
    
    /**
     * Gets if the distance-based analysis is enabled.
     * @return True if the distance-based analysis is enabled, false otherwise
     */
    protected boolean getEnableDistanceHistoric() {
        return enableDistanceHistoric;
    } // getEnableDistanceHistoric
    
    /**
     * Gets if the raw-based analysis is enabled.
     * @return True if the raw-based analysis is enabled, false otherwise
     */
    protected boolean getEnableRawHistoric() {
        return enableRawHistoric;
    } // getEnableRawHistoric
    
    protected boolean getEnableRawSnapshot() {
        return enableRawSnapshot;
    } // getEnableRawSnapshot
    
    @Override
    public void configure(Context context) {
        // Read NGSISink general configuration
        super.configure(context);
        
        // Impose enable lower case, since PostgreSQL only accepts lower case
        enableLowercase = true;
        
        // Impose enable encoding
        enableEncoding = true;
        
        // Check the data model is not different than dm-by-service-path or dm-by-entity
        if (dataModel == DataModel.DMBYSERVICE || dataModel == DataModel.DMBYATTRIBUTE) {
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Invalid configuration (data_model="
                    + dataModel.toString() + ") -- Must be 'dm-by-service-path' or 'dm-by-entity'");
            return;
        } // if else
        
        // Read other configuration parameters
        keysConfFile = context.getString("keys_conf_file");
        
        if (keysConfFile == null) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Reading configuration (keys_conf_file=null) "
                    + "-- Must not be empty");
            return;
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (keys_conf_file="
                    + keysConfFile + ")");
        } // if else
        
        String swapCoordinatesStr = context.getString("swap_coordinates");
        boolean wasFlipCoordinates = false;
        
        if (swapCoordinatesStr == null || swapCoordinatesStr.isEmpty()) {
            swapCoordinatesStr = context.getString("flip_coordinates");
            
            if (swapCoordinatesStr == null || swapCoordinatesStr.isEmpty()) {
                swapCoordinatesStr = "false";
            } else {
                wasFlipCoordinates = true;
            } // if else
        } // if
        
        if (swapCoordinatesStr.equals("true") || swapCoordinatesStr.equals("false")) {
            swapCoordinates = swapCoordinatesStr.equals("true");
            
            if (wasFlipCoordinates) {
                LOGGER.debug("[" + this.getName() + "] Reading configuration (flip_coordinates="
                        + swapCoordinatesStr + ") -- Deprecated, please use 'swap_coordinates' instead");
            } else {
                LOGGER.debug("[" + this.getName() + "] Reading configuration (swap_coordinates="
                        + swapCoordinatesStr + ")");
            } // if else
        } else {
            invalidConfiguration = true;
            
            if (wasFlipCoordinates) {
                LOGGER.error("[" + this.getName() + "] Invalid configuration (flip_coordinates="
                        + swapCoordinatesStr + ") -- Must be 'true' or 'false' -- Deprecated, please use "
                        + "'swap_coordinates' instead");
            } else {
                LOGGER.error("[" + this.getName() + "] Invalid configuration (swap_coordinates="
                        + swapCoordinatesStr + ") -- Must be 'true' or 'false'");
            } // if else
            
            return;
        } // if else
        
        String enableRawStr = context.getString("enable_raw");
        String enableRawHistoricStr = context.getString("enable_raw_historic");

        if (enableRawHistoricStr == null || enableRawHistoricStr.isEmpty()) {
            if (enableRawStr == null || enableRawStr.isEmpty()) {
                enableRawHistoric = true; // default value
            } else if (enableRawStr.equals("true") || enableRawStr.equals("false")) {
                enableRawHistoric = enableRawStr.equals("true");
                LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_raw="
                        + enableRawStr + ") -- Deprecated, use enable_raw_historic instead");
            } else {
                invalidConfiguration = true;
                LOGGER.error("[" + this.getName() + "] Invalid configuration (enable_raw="
                        + enableRawStr + ") -- Must be 'true' or 'false' -- Deprecated, use enable_raw_historic instead");
                return;
            } // if else
        } else {
            if (enableRawHistoricStr.equals("true") || enableRawHistoricStr.equals("false")) {
                enableRawHistoric = enableRawHistoricStr.equals("true");
                LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_raw_historic="
                        + enableRawHistoricStr + ")");
            } else {
                invalidConfiguration = true;
                LOGGER.error("[" + this.getName() + "] Invalid configuration (enable_raw_historic="
                        + enableRawHistoricStr + ") -- Must be 'true' or 'false'");
                return;
            } // if else
        } // if else

        String enableDistanceStr = context.getString("enable_distance");
        String enableDistanceHistoricStr = context.getString("enable_distance_historic");
        
        if (enableDistanceHistoricStr == null || enableDistanceHistoricStr.isEmpty()) {
            if (enableDistanceStr == null || enableDistanceStr.isEmpty()) {
                enableDistanceHistoric = false; // default value
            } else if (enableDistanceStr.equals("true") || enableDistanceStr.equals("false")) {
                enableDistanceHistoric = enableDistanceStr.equals("true");
                LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_distance="
                        + enableDistanceStr + ") -- Deprecated, use enable_distance_historic instead");
            } else {
                invalidConfiguration = true;
                LOGGER.error("[" + this.getName() + "] Invalid configuration (enable_distance="
                        + enableDistanceStr + ") -- Must be 'true' or 'false' -- Deprecated, use "
                        + "enable_distance_historic instead");
            } // if else
        } else {
            if (enableDistanceHistoricStr.equals("true") || enableDistanceHistoricStr.equals("false")) {
                enableDistanceHistoric = enableDistanceHistoricStr.equals("true");
                LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_distance_historic="
                        + enableDistanceHistoricStr + ")");
            } else {
                invalidConfiguration = true;
                LOGGER.error("[" + this.getName() + "] Invalid configuration (enable_distance_historic="
                        + enableDistanceHistoricStr + ") -- Must be 'true' or 'false'");
            } // if else
        } // if else
        
        String enableRawSnapshotStr = context.getString("enable_raw_snapshot", "false");

        if (enableRawSnapshotStr.equals("true") || enableRawSnapshotStr.equals("false")) {
            enableRawSnapshot = enableRawSnapshotStr.equals("true");
            LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_raw_snapshot="
                    + enableRawSnapshotStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Invalid configuration (enable_raw_snapshot="
                    + enableRawSnapshotStr + ") -- Must be 'true' or 'false'");
        } // if else
        
        backendMaxConns = context.getInteger("backend.max_conns", 500);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (backend.max_conns=" + backendMaxConns + ")");
        backendMaxConnsPerRoute = context.getInteger("backend.max_conns_per_route", 100);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (backend.max_conns_per_route="
                + backendMaxConnsPerRoute + ")");
    } // configure
    
    @Override
    public void start() {
        try {
            initializeBackend();
            LOGGER.debug("[" + this.getName() + "] CartoDB persistence backend created");
        } catch (Exception e) {
            backends = null;
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Error while creating the CartoDB persistence backend. Details: "
                    + e.getMessage());
        } // try catch

        super.start();
    } // start
    
    private void initializeBackend() throws Exception {
        // Read the keys file
        String jsonStr = JsonUtils.readJsonFile(keysConfFile);
        LOGGER.info("[" + this.getName() + "] Json containing CartoDB API keys has been read: " + jsonStr);

        // Parse the Json containing the keys
        JSONArray apiKeys = (JSONArray) JsonUtils.parseJsonString(jsonStr).get("cartodb_keys");
        LOGGER.info("[" + this.getName() + "] Json containing CartoDB API keys is syntactically OK");

        // Create the persistence backend
        backends = new HashMap<>();

        // Iterate on the JSONObject containing the API keys
        for (Object apiKey : apiKeys) {
            JSONObject obj = (JSONObject) apiKey;
            String endpoint = (String) obj.get("endpoint");

            if (endpoint == null || endpoint.isEmpty()) {
                LOGGER.warn("Invalid API key entry, endpoint is null or empty. Discarding it.");
                continue;
            } // if

            String host;
            String port;
            boolean ssl;
            int index;

            if (endpoint.startsWith("http://")) {
                port = "80";
                ssl = false;
                index = 7;
            } else if (endpoint.startsWith("https://")) {
                port = "443";
                ssl = true;
                index = 8;
            } else {
                LOGGER.warn("Invalid API key entry, the endpoint URI must use the http or https schemes. "
                        + "Discarding it.");
                continue;
            } // if else

            String hostPort = endpoint.substring(index);
            String[] split = hostPort.split(":");

            if (split.length == 2) {
                host = split[0];
                port = split[1];
            } else {
                host = split[0];
            } // if else

            String username = (String) obj.get("username");

            if (username == null || username.isEmpty()) {
                LOGGER.warn("[" + this.getName() + "] Invalid API key entry, username is null or empty. "
                        + "Discarding it.");
                continue;
            } // if

            String key = (String) obj.get("key");

            if (key == null || key.isEmpty()) {
                LOGGER.warn("[" + this.getName() + "] Invalid API key entry, key is null or empty. Discarding it.");
                continue;
            } // if
            
            String type = (String) obj.get("type");
            
            if (type == null || type.isEmpty()) {
                LOGGER.warn("[" + this.getName() + "] Invalid API key entry, type is null or empty. Discarding it.");
                continue;
            } // if
            
            if (!type.equals("personal") && !type.equals("enterprise")) {
                LOGGER.warn("[" + this.getName() + "] Invalid API key entry, type is not 'personal' or 'enterprise'. "
                        + "Discarding it.");
                continue;
            } // if
            
            backends.put(username, new CartoDBBackendImpl(host, port, ssl, key, type.equals("personal"),
                    backendMaxConns, backendMaxConnsPerRoute));
        } // for
        
        if (backends.isEmpty()) {
            throw new Exception("All the API key entries were discarded");
        } // if
    } // initializeBackend

    @Override
    void persistBatch(NGSIBatch batch) throws CygnusBadConfiguration, CygnusPersistenceError {
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

            // Get the events within the current sub-batch
            ArrayList<NGSIEvent> events = batch.getNextEvents();
            
            // Get the first event, it will give us useful information
            NGSIEvent firstEvent = events.get(0);
            String service = firstEvent.getServiceForData();
            String servicePath = firstEvent.getServicePathForData();
            
            // Raw aggregator
            CartoDBAggregator aggregator = null;

            if (enableRawHistoric) {
                // Get an aggregator for this destination and initialize it
                aggregator = new CartoDBAggregator();
                aggregator.initialize(firstEvent);
            } // if

            for (NGSIEvent event : events) {
                if (enableRawHistoric && aggregator != null) {
                    aggregator.aggregate(event);
                } // if
                
                if (enableDistanceHistoric) {
                    persistDistanceEvent(event);
                } // if
                
                if (enableRawSnapshot) {
                    rawUpdateEvent(event);
                } // if
            } // for

            if (enableRawHistoric) {
                // Persist the aggregation
                persistRawAggregation(aggregator, service, servicePath);
            } // if
            
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
     * Convenience class for aggregating data.
     */
    protected class CartoDBAggregator {
        
        private LinkedHashMap<String, ArrayList<String>> aggregation;
        private String service;
        private String servicePathForData;
        private String servicePathForNaming;
        private String entityForNaming;
        private String attributeForNaming;
        private String schemaName;
        private String tableName;
        
        public String getSchemaName() {
            return schemaName;
        } // getSchemaName
        
        public String getTableName() {
            return tableName;
        } // getTableName
        
        /**
         * Gets PostgreSQL-like rows from the aggregation.
         * @return
         */
        public String getRows() {
            String rows = "";
            int numEvents = aggregation.get(NGSIConstants.FIWARE_SERVICE_PATH).size();
            
            for (int i = 0; i < numEvents; i++) {
                if (i == 0) {
                    rows += "(";
                } else {
                    rows += ",(";
                } // if else
                
                boolean first = true;
                
                for (String field : aggregation.keySet()) {
                    ArrayList<String> values = (ArrayList<String>) aggregation.get(field);
                    String value = values.get(i);
                    
                    if (!field.equals("the_geom")) {
                        value = "'" + value + "'";
                    } // if
                    
                    if (first) {
                        rows += value;
                        first = false;
                    } else {
                        rows += "," + value;
                    } // if else
                }
                
                rows += ")";
            } // for
            
            return rows;
        } // getRows

        /**
         * Gets PostgreSLQ-like fields from the aggregation.
         * @return
         */
        public String getFields() {
            String fields = "(";
            boolean first = true;
            Iterator it = aggregation.keySet().iterator();
            
            while (it.hasNext()) {
                if (first) {
                    fields += (String) it.next();
                    first = false;
                } else {
                    fields += "," + (String) it.next();
                } // if else
            } // while
            
            fields += ")";
            return fields.toLowerCase();
        } // getFields
        
        /**
         * Initializes an aggregation.
         * @param event
         * @throws CygnusBadConfiguration
         */
        public void initialize(NGSIEvent event) throws CygnusBadConfiguration {
            service = event.getServiceForNaming(enableNameMappings);
            servicePathForData = event.getServicePathForData();
            servicePathForNaming = event.getServicePathForNaming(enableGrouping, enableNameMappings);
            entityForNaming = event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding);
            attributeForNaming = event.getAttributeForNaming(enableNameMappings);
            schemaName = buildSchemaName(service);
            tableName = buildTableName(servicePathForNaming, entityForNaming, attributeForNaming);
            
            // aggregation initialization
            aggregation = new LinkedHashMap<>();
            aggregation.put(NGSIConstants.RECV_TIME, new ArrayList<String>());
            aggregation.put(NGSIConstants.FIWARE_SERVICE_PATH, new ArrayList<String>());
            aggregation.put(NGSIConstants.ENTITY_ID, new ArrayList<String>());
            aggregation.put(NGSIConstants.ENTITY_TYPE, new ArrayList<String>());
            aggregation.put(NGSIConstants.CARTO_DB_THE_GEOM, new ArrayList<String>());
            
            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = event.getContextElement().getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                return;
            } // if
            
            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                
                if (!NGSIUtils.getGeometry(attrValue, attrType, attrMetadata, swapCoordinates).getRight()) {
                    aggregation.put(attrName, new ArrayList<String>());
                    aggregation.put(attrName + "_md", new ArrayList<String>());
                } // if
            } // for
        } // initialize
        
        /**
         * Aggregates a given Cygnus getRecvTimeTs.
         * @param event
         */
        public void aggregate(NGSIEvent event) {
            // get the getRecvTimeTs headers
            long recvTimeTs = event.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the getRecvTimeTs body
            NotifyContextRequest.ContextElement contextElement = event.getContextElement();
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
            
            aggregation.get(NGSIConstants.RECV_TIME).add(recvTime);
            aggregation.get(NGSIConstants.FIWARE_SERVICE_PATH).add(servicePathForData);
            aggregation.get(NGSIConstants.ENTITY_ID).add(entityId);
            aggregation.get(NGSIConstants.ENTITY_TYPE).add(entityType);
            
            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                ImmutablePair<String, Boolean> location = NGSIUtils.getGeometry(attrValue, attrType, attrMetadata,
                        swapCoordinates);
                
                if (location.right) {
                    aggregation.get(NGSIConstants.CARTO_DB_THE_GEOM).add(location.getLeft());
                } else {
                    aggregation.get(attrName).add(attrValue);
                    aggregation.get(attrName + "_md").add(attrMetadata);
                } // if else
            } // for
        } // aggregate
        
    } // CartoDBAggregator

    private void persistRawAggregation(CartoDBAggregator aggregator, String service, String servicePath)
        throws CygnusPersistenceError {
        //String dbName = aggregator.getDbName(); // enable_lowercase is unncessary, PostgreSQL is case insensitive
        String tableName = aggregator.getTableName(); // enable_lowercase is unncessary, PostgreSQL is case insensitive
        String schema = aggregator.getSchemaName();
        String withs = "";
        String fields = aggregator.getFields();
        String rows = aggregator.getRows();
        LOGGER.info("[" + this.getName() + "] Persisting data at NGSICartoDBSink. Schema (" + schema
                + "), Table (" + tableName + "), Data (" + rows + ")");
        
        try {
            ((CartoDBBackendImpl) backends.get(schema)).startTransaction();
            backends.get(schema).insert(schema, tableName, withs, fields, rows);
            ImmutablePair<Long, Long> bytes = ((CartoDBBackendImpl) backends.get(schema)).finishTransaction();
            serviceMetrics.add(service, servicePath, 0, 0, 0, 0, 0, 0, bytes.left, bytes.right, 0);
        } catch (Exception e) {
            ImmutablePair<Long, Long> bytes = ((CartoDBBackendImpl) backends.get(schema)).finishTransaction();
            serviceMetrics.add(service, servicePath, 0, 0, 0, 0, 0, 0, bytes.left, bytes.right, 0);
            throw new CygnusPersistenceError("-, " + e.getMessage());
        } // try catch
    } // persistRawAggregation
    
    private void persistDistanceEvent(NGSIEvent event) throws CygnusBadConfiguration, CygnusPersistenceError {
        // Get some values
        String schema = buildSchemaName(event.getServiceForNaming(enableNameMappings));
        String service = event.getServiceForData();
        String servicePath = event.getServicePathForData();
        String entityId = event.getContextElement().getId();
        String entityType = event.getContextElement().getType();
        
        // Iterate on all this context element attributes, if there are attributes
        ArrayList<ContextAttribute> contextAttributes = event.getContextElement().getAttributes();

        if (contextAttributes == null || contextAttributes.isEmpty()) {
            return;
        } // if

        ((CartoDBBackendImpl) backends.get(schema)).startTransaction();
        
        for (ContextAttribute contextAttribute : contextAttributes) {
            long recvTimeTs = event.getRecvTimeTs();
            String attrType = contextAttribute.getType();
            String attrValue = contextAttribute.getContextValue(false);
            String attrMetadata = contextAttribute.getContextMetadata();
            ImmutablePair<String, Boolean> location = NGSIUtils.getGeometry(attrValue, attrType, attrMetadata,
                    swapCoordinates);
            String tableName = buildTableName(event.getServicePathForNaming(enableGrouping, enableNameMappings),
                    event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding),
                    event.getAttributeForNaming(enableNameMappings))
                    + CommonConstants.CONCATENATOR + "distance";

            if (location.getRight()) {
                // Try creating the table... the cost of checking if it exists and creating it is higher than directly
                // attempting to create it. If existing, nothing will be re-created and the new values will be inserted
                
                try {
                    String typedFields = "(recvTimeMs bigint, fiwareServicePath text, entityId text, entityType text, "
                            + "stageDistance float, stageTime float, stageSpeed float, sumDistance float, "
                            + "sumTime float, sumSpeed float, sum2Distance float, sum2Time float, sum2Speed float, "
                            + "maxDistance float, minDistance float, maxTime float, minTime float, maxSpeed float, "
                            + "minSpeed float, numSamples bigint)";
                    backends.get(schema).createTable(schema, tableName, typedFields);
                    
                    // Once created, insert the first row
                    String withs = "";
                    String fields = "(recvTimeMs, fiwareServicePath, entityId, entityType, the_geom, stageDistance,"
                            + "stageTime, stageSpeed, sumDistance, sumTime, sumSpeed, sum2Distance, sum2Time,"
                            + "sum2Speed, maxDistance, minDistance, maxTime, mintime, maxSpeed, minSpeed, numSamples)";
                    String rows = "(" + recvTimeTs + ",'" + servicePath + "','" + entityId + "','" + entityType + "',"
                            + location.getLeft() + ",0,0,0,0,0,0,0,0,0," + Float.MIN_VALUE + "," + Float.MAX_VALUE + ","
                            + Float.MIN_VALUE + "," + Float.MAX_VALUE + "," + Float.MIN_VALUE + "," + Float.MAX_VALUE
                            + ",1)";
                    LOGGER.info("[" + this.getName() + "] Persisting data at NGSICartoDBSink. Schema (" + schema
                            + "), Table (" + tableName + "), Data (" + rows + ")");
                    backends.get(schema).insert(schema, tableName, withs, fields, rows);
                } catch (Exception e1) {
                    String withs = ""
                            + "WITH geom AS ("
                            + "   SELECT " + location.getLeft() + " AS point"
                            + "), calcs AS ("
                            + "   SELECT"
                            + "      cartodb_id,"
                            + "      ST_Distance(the_geom::geography, geom.point::geography) AS stage_distance,"
                            + "      (" + recvTimeTs + " - recvTimeMs) AS stage_time"
                            + "   FROM " + tableName + ", geom"
                            + "   ORDER BY cartodb_id DESC"
                            + "   LIMIT 1"
                            + "), speed AS ("
                            + "   SELECT"
                            + "      (calcs.stage_distance / NULLIF(calcs.stage_time, 0)) AS stage_speed"
                            + "   FROM calcs"
                            + "), inserts AS ("
                            + "   SELECT"
                            + "      (-1 * ((-1 * t1.sumDistance) - calcs.stage_distance)) AS sum_dist,"
                            + "      (-1 * ((-1 * t1.sumTime) - calcs.stage_time)) AS sum_time,"
                            + "      (-1 * ((-1 * t1.sumSpeed) - speed.stage_speed)) AS sum_speed,"
                            + "      (-1 * ((-1 * t1.sumDistance) - calcs.stage_distance)) "
                            + "          * (-1 * ((-1 * t1.sumDistance) - calcs.stage_distance)) AS sum2_dist,"
                            + "      (-1 * ((-1 * t1.sumTime) - calcs.stage_time)) "
                            + "          * (-1 * ((-1 * t1.sumTime) - calcs.stage_time)) AS sum2_time,"
                            + "      (-1 * ((-1 * t1.sumSpeed) - speed.stage_speed)) "
                            + "          * (-1 * ((-1 * t1.sumSpeed) - speed.stage_speed)) AS sum2_speed,"
                            + "      t1.max_distance,"
                            + "      t1.min_distance,"
                            + "      t1.max_time,"
                            + "      t1.min_time,"
                            + "      t1.max_speed,"
                            + "      t1.min_speed,"
                            + "      t2.num_samples"
                            + "   FROM"
                            + "      ("
                            + "         SELECT"
                            + "            GREATEST(calcs.stage_distance, maxDistance) AS max_distance,"
                            + "            LEAST(calcs.stage_distance, minDistance) AS min_distance,"
                            + "            GREATEST(calcs.stage_time, maxTime) AS max_time,"
                            + "            LEAST(calcs.stage_time, minTime) AS min_time,"
                            + "            GREATEST(speed.stage_speed, maxSpeed) AS max_speed,"
                            + "            LEAST(speed.stage_speed, minSpeed) AS min_speed,"
                            + "            sumDistance,"
                            + "            sumTime,"
                            + "            sumSpeed"
                            + "         FROM " + tableName + ", speed, calcs"
                            + "         ORDER BY " + tableName + ".cartodb_id DESC"
                            + "         LIMIT 1"
                            + "      ) AS t1,"
                            + "      ("
                            + "         SELECT (-1 * ((-1 * COUNT(*)) - 1)) AS num_samples"
                            + "         FROM " + tableName
                            + "      ) AS t2,"
                            + "      speed,"
                            + "      calcs"
                            + ")";
                    String fields = "(recvTimeMs, fiwareServicePath, entityId, entityType, the_geom, stageDistance,"
                            + "stageTime, stageSpeed, sumDistance, sumTime, sumSpeed, sum2Distance, sum2Time,"
                            + "sum2Speed, maxDistance, minDistance, maxTime, mintime, maxSpeed, minSpeed, numSamples)";
                    String rows = "(" + recvTimeTs + ",'" + servicePath + "','" + entityId + "','" + entityType + "',"
                            + "(SELECT point FROM geom),(SELECT stage_distance FROM calcs),"
                            + "(SELECT stage_time FROM calcs),(SELECT stage_speed FROM speed),"
                            + "(SELECT sum_dist FROM inserts),(SELECT sum_time FROM inserts),"
                            + "(SELECT sum_speed FROM inserts),(SELECT sum2_dist FROM inserts),"
                            + "(SELECT sum2_time FROM inserts),(SELECT sum2_speed FROM inserts),"
                            + "(SELECT max_distance FROM inserts),(SELECT min_distance FROM inserts),"
                            + "(SELECT max_time FROM inserts),(SELECT min_time FROM inserts),"
                            + "(SELECT max_speed FROM inserts),(SELECT min_speed FROM inserts),"
                            + "(SELECT num_samples FROM inserts))";
                    LOGGER.info("[" + this.getName() + "] Persisting data at NGSICartoDBSink. Schema (" + schema
                            + "), Table (" + tableName + "), Data (" + rows + ")");
                    
                    try {
                        backends.get(schema).insert(schema, tableName, withs, fields, rows);
                    } catch (Exception e2) {
                        ImmutablePair<Long, Long> bytes = ((CartoDBBackendImpl) backends.get(schema)).finishTransaction();
                        serviceMetrics.add(service, servicePath, 0, 0, 0, 0, 0, 0, bytes.left, bytes.right, 0);
                        throw new CygnusPersistenceError("-, " + e2.getMessage());
                    } // try catch
                } // try catch
            } // if
        } // for
        
        ImmutablePair<Long, Long> bytes = ((CartoDBBackendImpl) backends.get(schema)).finishTransaction();
        serviceMetrics.add(service, servicePath, 0, 0, 0, 0, 0, 0, bytes.left, bytes.right, 0);
    } // persistDistanceEvent
    
    private void rawUpdateEvent(NGSIEvent event) throws CygnusBadConfiguration, CygnusPersistenceError {
        String service = event.getServiceForData();
        String servicePath = event.getServicePathForData();
        long recvTimeTs = event.getRecvTimeTs();
        String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);
        String schema = buildSchemaName(event.getServiceForNaming(enableNameMappings));
        String tableName = NGSICharsets.encodePostgreSQL(
                event.getServicePathForNaming(enableGrouping, enableNameMappings))
                + CommonConstants.CONCATENATOR + "rawsnapshot";

        // Iterate on the context attribute and build both 'sets' and 'fields' and the 'rows' in a single loop
        String sets = "";
        String fields = "";
        String rows = "";
        
        for (ContextAttribute ca : event.getOriginalCE().getAttributes()) {
            String attrValue = ca.getContextValue(false);
            String attrType = ca.getType();
            String attrMetadata = ca.getContextMetadata();
            ImmutablePair<String, Boolean> location = NGSIUtils.getGeometry(attrValue, attrType, attrMetadata,
                    swapCoordinates);
            String set;
            String field;
            String row;
            
            if (location.right) {
                set = NGSIConstants.CARTO_DB_THE_GEOM + "=" + location.left;
                field = NGSIConstants.CARTO_DB_THE_GEOM;
                row = location.left;
            } else {
                set = ca.getName() + "='" + ca.getContextValue(false) + "',"
                        + ca.getName() + "_md='" + ca.getContextMetadata() + "'";
                field = ca.getName() + "," + ca.getName() + "_md";
                row = "'" + ca.getContextValue(false) + "','" + ca.getContextMetadata() + "'";
            } // if else
            
            if (sets.isEmpty()) {
                sets = set;
            } else {
                sets += "," + set;
            } // if else
            
            if (fields.isEmpty()) {
                fields = "(" + NGSIConstants.RECV_TIME + "," + NGSIConstants.FIWARE_SERVICE_PATH + ","
                        + NGSIConstants.ENTITY_ID + "," + NGSIConstants.ENTITY_TYPE + "," + field;
            } else {
                fields += "," + field;
            } // if else
            
            if (rows.isEmpty()) {
                rows = "('" + recvTime + "','" + event.getServicePathForData() + "','" + event.getOriginalCE().getId()
                        + "','" + event.getOriginalCE().getType() + "'," + row;
            } else {
                rows += "," + row;
            } // if else
        } // for
        
        fields += ")";
        rows += ")";
        
        // Update
        String where = "fiwareServicePath='" + event.getServicePathForData()
                + "' AND entityId='" + event.getOriginalCE().getId()
                + "' AND entityType='" + event.getOriginalCE().getType() + "'";
        LOGGER.info("[" + this.getName() + "] Updating data at NGSICartoDBSink. Schema (" + schema
                + "), Table (" + tableName + "), Sets (" + sets + "), Where (" + where + ")");
        boolean updated = false;
        
        ((CartoDBBackendImpl) backends.get(schema)).startTransaction();
        
        try {
            updated = backends.get(schema).update(schema, tableName, sets, where);
        } catch (Exception e) {
            ImmutablePair<Long, Long> bytes = ((CartoDBBackendImpl) backends.get(schema)).finishTransaction();
            serviceMetrics.add(service, servicePath, 0, 0, 0, 0, 0, 0, bytes.left, bytes.right, 0);
            throw new CygnusPersistenceError("-, " + e.getMessage());
        } // try catch
        
        if (!updated) {
            // Insert
            String withs = "";
            LOGGER.info("[" + this.getName() + "] Inserting initial data at NGSICartoDBSink. Schema (" + schema
                    + "), Table (" + tableName + "), Data (" + rows + ")");
            
            try {
                backends.get(schema).insert(schema, tableName, withs, fields, rows);
            } catch (Exception e) {
                ImmutablePair<Long, Long> bytes = ((CartoDBBackendImpl) backends.get(schema)).finishTransaction();
                serviceMetrics.add(service, servicePath, 0, 0, 0, 0, 0, 0, bytes.left, bytes.right, 0);
                throw new CygnusPersistenceError("-, " + e.getMessage());
            } // try catch
        } // if
        
        ImmutablePair<Long, Long> bytes = ((CartoDBBackendImpl) backends.get(schema)).finishTransaction();
        serviceMetrics.add(service, servicePath, 0, 0, 0, 0, 0, 0, bytes.left, bytes.right, 0);
    } // rawUpdateEvent
    
    /**
     * Builds a schema name for CartoDB given a service.
     * @param service
     * @return The schema name for CartoDB
     * @throws CygnusBadConfiguration
     */
    protected String buildSchemaName(String service) throws CygnusBadConfiguration {
        String name = NGSICharsets.encodePostgreSQL(service);

        if (name.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building schema name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildSchemaName
    
    /**
     * Builds a table name for CartoDB given a service path, an entity and an attribute.
     * @param servicePath
     * @param entity
     * @param attribute
     * @return The table name for CartoDB
     * @throws CygnusBadConfiguration
     */
    protected String buildTableName(String servicePath, String entity, String attribute)
        throws CygnusBadConfiguration {
        String name;
        
        switch(dataModel) {
            case DMBYSERVICEPATH:
                name = NGSICharsets.encodePostgreSQL(servicePath);
                break;
            case DMBYENTITY:
                String truncatedServicePath = NGSICharsets.encodePostgreSQL(servicePath);
                name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + CommonConstants.CONCATENATOR)
                        + NGSICharsets.encodePostgreSQL(entity);
                break;
            case DMBYATTRIBUTE:
                truncatedServicePath = NGSICharsets.encodePostgreSQL(servicePath);
                name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + CommonConstants.CONCATENATOR)
                        + NGSICharsets.encodePostgreSQL(entity) + CommonConstants.CONCATENATOR
                        + NGSICharsets.encodePostgreSQL(attribute);
                break;
            default:
                throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                        + "'. Please, use dm-by-service-path, dm-by-entity or dm-by-attribute");
        } // switch
        
        if (name.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building table name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildTableName
    
} // NGSICartoDBSink
