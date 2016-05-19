/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.Enums.DataModel;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.apache.flume.Context;

/**
 *
 * @author frb
 */
public class NGSICartoDBSink extends NGSISink {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(NGSICartoDBSink.class);
    private String host;
    private String port;
    private boolean ssl;
    private String apiKey;
    private boolean flipCoordinates;
    protected boolean enableRaw;
    protected boolean enableDistance;
    private String schema;
    private CartoDBBackendImpl backend;
    
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
    protected CartoDBBackendImpl getBackend() {
        return backend;
    } // getBackend
    
    @Override
    public void configure(Context context) {
        // Read NGSISink general configuration
        super.configure(context);
        
        // Impose enable ower case, since PostgreSQL only accepts lower case
        enableLowercase = true;
        
        // Check the data model is not different than dm-by-service-path or dm-by-entity
        if (dataModel == DataModel.DMBYSERVICE || dataModel == DataModel.DMBYATTRIBUTE) {
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Invalid configuration (data_model="
                    + dataModel.toString() + ") -- Must be 'dm-by-service-path' or 'dm-by-entity'");
            return;
        } // if else
        
        // Read NGSICartoDB specific configuration
        String endpoint = context.getString("endpoint");
        
        if (endpoint == null) {
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Invalid configuration (endpoint=null) -- Must not be empty");
            return;
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (endpoint=" + endpoint + ")");
        } // else
        
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
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Invalid configuration (endpoint=" + endpoint + ") "
                    + "-- The URI must use the http or https schemes");
            return;
        } // if else
        
        String hostPort = endpoint.substring(index);
        String[] split = hostPort.split(":");
        
        if (split.length == 2) {
            host = split[0];
            port = split[1];
        } else {
            host = split[0];
        } // if else
        
        apiKey = context.getString("api_key");
        String[] split2 = host.split("\\.");
        schema = split2[0];
        
        if (apiKey == null) {
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Invalid configuration (apiKey=null) -- Must not be empty");
            return;
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (apiKey=" + apiKey + ")");
        } // if else
        
        String flipCoordinatesStr = context.getString("flip_coordinates", "false");
        
        if (flipCoordinatesStr.equals("true") || flipCoordinatesStr.equals("false")) {
            flipCoordinates = flipCoordinatesStr.equals("true");
            LOGGER.debug("[" + this.getName() + "] Reading configuration (flip_coordinates="
                    + flipCoordinatesStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Invalid configuration (flip_coordinates="
                    + flipCoordinatesStr + ") -- Must be 'true' or 'false'");
        } // if else
        
        String enableRawStr = context.getString("enable_raw", "true");

        if (enableRawStr.equals("true") || enableRawStr.equals("false")) {
            enableRaw = enableRawStr.equals("true");
            LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_raw="
                    + enableRawStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Invalid configuration (enable_raw="
                    + enableRawStr + ") -- Must be 'true' or 'false'");
        } // if else

        String enableDistanceStr = context.getString("enable_distance", "false");

        if (enableDistanceStr.equals("true") || enableDistanceStr.equals("false")) {
            enableDistance = enableDistanceStr.equals("true");
            LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_distance="
                    + enableDistanceStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Invalid configuration (enable_distance="
                    + enableDistanceStr + ") -- Must be 'true' or 'false'");
        } // if else
    } // configure
    
    @Override
    public void start() {
        try {
            // create the persistence backend
            backend = new CartoDBBackendImpl(host, port, ssl, apiKey);
            LOGGER.debug("[" + this.getName() + "] CartoDB persistence backend created");
        } catch (Exception e) {
            LOGGER.error("Error while creating the CartoDB persistence backend. Details: " + e.getMessage());
        } // try catch

        super.start();
    } // start

    @Override
    void persistBatch(NGSIBatch batch) throws Exception {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if

        // iterate on the destinations, for each one a single create / append will be performed
        for (String destination : batch.getDestinations()) {
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the " + destination
                    + " destination");

            // get the sub-batch for this destination
            ArrayList<NGSIEvent> subBatch = batch.getEvents(destination);
            CartoDBAggregator aggregator = null;

            if (enableRaw) {
                // get an aggregator for this destination and initialize it
                aggregator = new CartoDBAggregator();
                aggregator.initialize(subBatch.get(0));
            } // if

            for (NGSIEvent cygnusEvent : subBatch) {
                if (enableRaw && aggregator != null) {
                    aggregator.aggregate(cygnusEvent);
                } // if
                
                if (enableDistance) {
                    persistDistanceEvent(cygnusEvent);
                } // if
            } // for

            if (enableRaw) {
                // persist the aggregation
                persistRawAggregation(aggregator);
            } // if
            
            batch.setPersisted(destination);
        } // for
    } // persistBatch
    
    /**
     * Convenience class for aggregating data.
     */
    protected class CartoDBAggregator {
        
        private LinkedHashMap<String, ArrayList<String>> aggregation;
        private String service;
        private String servicePath;
        private String entity;
        private String attribute;
        private String dbName;
        private String tableName;
        
        public String getDbName() {
            return dbName;
        } // getDbName
        
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
                Iterator it = aggregation.keySet().iterator();
            
                while (it.hasNext()) {
                    ArrayList<String> values = (ArrayList<String>) aggregation.get((String) it.next());
                    String value = values.get(i);
                    
                    if (!value.startsWith("ST_SetSRID(ST_MakePoint(")) {
                        value = "'" + value + "'";
                    } // if
                    
                    if (first) {
                        rows += value;
                        first = false;
                    } else {
                        rows += "," + value;
                    } // if else
                } // while
                
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
         * @throws Exception
         */
        public void initialize(NGSIEvent event) throws Exception {
            service = event.getService();
            servicePath = event.getServicePath();
            entity = event.getEntity();
            attribute = event.getAttribute();
            //dbName = buildDbName(service);
            tableName = buildTableName(servicePath, entity, attribute);
            
            // aggregation initialization
            aggregation = new LinkedHashMap<String, ArrayList<String>>();
            aggregation.put(NGSIConstants.RECV_TIME, new ArrayList<String>());
            aggregation.put(NGSIConstants.FIWARE_SERVICE_PATH, new ArrayList<String>());
            aggregation.put(NGSIConstants.ENTITY_ID, new ArrayList<String>());
            aggregation.put(NGSIConstants.ENTITY_TYPE, new ArrayList<String>());
            aggregation.put(NGSIConstants.THE_GEOM, new ArrayList<String>());
            
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
                
                if (!NGSIUtils.getLocation(attrValue, attrType, attrMetadata, flipCoordinates).startsWith(
                        "ST_SetSRID(ST_MakePoint(")) {
                    aggregation.put(attrName, new ArrayList<String>());
                    aggregation.put(attrName + "_md", new ArrayList<String>());
                } // if
            } // for
        } // initialize
        
        /**
         * Aggregates a given Cygnus event.
         * @param cygnusEvent
         * @throws Exception
         */
        public void aggregate(NGSIEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the event body
            NotifyContextRequest.ContextElement contextElement = cygnusEvent.getContextElement();
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
            aggregation.get(NGSIConstants.FIWARE_SERVICE_PATH).add(servicePath);
            aggregation.get(NGSIConstants.ENTITY_ID).add(entityId);
            aggregation.get(NGSIConstants.ENTITY_TYPE).add(entityType);
            
            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                String location = NGSIUtils.getLocation(attrValue, attrType, attrMetadata, flipCoordinates);
                
                if (location.startsWith("ST_SetSRID(ST_MakePoint(")) {
                    aggregation.get(NGSIConstants.THE_GEOM).add(location);
                } else {
                    aggregation.get(attrName).add(attrValue);
                    aggregation.get(attrName + "_md").add(attrMetadata);
                } // if else
            } // for
        } // aggregate
        
    } // CartoDBAggregator
    
    /*
    private String buildDbName(String service) throws Exception {
        String name = NGSIUtils.encodePostgreSQL(service, false);

        if (name.length() > NGSIConstants.POSTGRESQL_MAX_ID_LEN) {
            throw new CygnusBadConfiguration("Building database name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_ID_LEN);
        } // if

        return name;
    } // buildDbName
    */
    
    /**
     * Builds a table name for CartoDB given a service path, an entity and an attribute.
     * @param servicePath
     * @param entity
     * @param attribute
     * @return The table name for CartoDB
     * @throws Exception
     */
    protected String buildTableName(String servicePath, String entity, String attribute) throws Exception {
        String name;
        
        switch(dataModel) {
            case DMBYSERVICEPATH:
                if (servicePath.equals("/")) {
                    throw new CygnusBadConfiguration("Default service path '/' cannot be used with "
                            + "dm-by-service-path data model");
                } // if

                name = NGSIUtils.encodePostgreSQL(servicePath, true);
                break;
            case DMBYENTITY:
                String truncatedServicePath = NGSIUtils.encodePostgreSQL(servicePath, true);
                name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '_')
                        + NGSIUtils.encodePostgreSQL(entity, false);
                break;
            default:
                throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                        + "'. Please, use DMBYSERVICEPATH or DMBYENTITY");
        } // switch
        
        if (name.length() > NGSIConstants.POSTGRESQL_MAX_ID_LEN) {
            throw new CygnusBadConfiguration("Building table name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_ID_LEN);
        } // if

        return name;
    } // buildTableName
    
    private void persistRawAggregation(CartoDBAggregator aggregator) throws Exception {
        //String dbName = aggregator.getDbName(); // enable_lowercase is unncessary, PostgreSQL is case insensitive
        String tableName = aggregator.getTableName(); // enable_lowercase is unncessary, PostgreSQL is case insensitive
        String withs = "";
        String fields = aggregator.getFields();
        String rows = aggregator.getRows();
        LOGGER.info("[" + this.getName() + "] Persisting data at NGSICartoDBSink. Schema (" + schema
                + "), Table (" + tableName + "), Data (" + rows + ")");
        backend.insert(tableName, withs, fields, rows);
    } // persistRawAggregation
    
    private void persistDistanceEvent(NGSIEvent event) throws Exception {
        // Get some values
        String servicePath = event.getServicePath();
        String entityId = event.getContextElement().getId();
        String entityType = event.getContextElement().getType();
        
        // Iterate on all this context element attributes, if there are attributes
        ArrayList<ContextAttribute> contextAttributes = event.getContextElement().getAttributes();

        if (contextAttributes == null || contextAttributes.isEmpty()) {
            return;
        } // if

        for (ContextAttribute contextAttribute : contextAttributes) {
            long recvTimeMs = event.getRecvTimeTs();
            String attrType = contextAttribute.getType();
            String attrValue = contextAttribute.getContextValue(false);
            String attrMetadata = contextAttribute.getContextMetadata();
            String location = NGSIUtils.getLocation(attrValue, attrType, attrMetadata, flipCoordinates);
            String tableName = schema
                    + "." + buildTableName(event.getServicePath(), event.getEntity(), event.getAttribute())
                    + "_distance";

            if (location.startsWith("ST_SetSRID(ST_MakePoint(")) {
                // Try creating the table... the cost of checking if it exists and creating it is higher than directly
                // attempting to create it. If existing, nothing will be re-created and the new values will be inserted
                
                try {
                    String typedFields = "(recvTimeMs bigint, fiwareServicePath text, entityId text, entityType text, "
                            + "stageDistance float, stageTime float, stageSpeed float, sumDistance float, "
                            + "sumTime float, sumSpeed float, sum2Distance float, sum2Time float, sum2Speed float, "
                            + "maxDistance float, minDistance float, maxTime float, minTime float, maxSpeed float, "
                            + "minSpeed float, numSamples bigint)";
                    backend.createTable(schema, tableName, typedFields);
                    
                    // Once created, insert the first row
                    String withs = "";
                    String fields = "(recvTimeMs, fiwareServicePath, entityId, entityType, the_geom, stageDistance,"
                            + "stageTime, stageSpeed, sumDistance, sumTime, sumSpeed, sum2Distance, sum2Time,"
                            + "sum2Speed, maxDistance, minDistance, maxTime, mintime, maxSpeed, minSpeed, numSamples)";
                    String rows = "(" + recvTimeMs + ",'" + servicePath + "','" + entityId + "','" + entityType + "',"
                            + location + ",0,0,0,0,0,0,0,0,0,-999999,999999,-999999,999999,-999999,999999,1)";
                    LOGGER.info("[" + this.getName() + "] Persisting data at NGSICartoDBSink. Schema (" + schema
                            + "), Table (" + tableName + "), Data (" + rows + ")");
                    backend.insert(tableName, withs, fields, rows);
                } catch (Exception e) {
                    String withs = ""
                            + "WITH geom AS ("
                            + "   SELECT " + location + " AS point"
                            + "), calcs AS ("
                            + "   SELECT"
                            + "      cartodb_id,"
                            + "      ST_Distance(the_geom::geography, geom.point::geography) AS stage_distance,"
                            + "      (" + recvTimeMs + " - recvTimeMs) AS stage_time"
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
                    String rows = "(" + recvTimeMs + ",'" + servicePath + "','" + entityId + "','" + entityType + "',"
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
                    backend.insert(tableName, withs, fields, rows);
                } // try catch
            } // if
        } // for
    } // persistDistanceEvent
    
} // NGSICartoDBSink
