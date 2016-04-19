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
    private CartoDBBackendImpl backend;
    
    /**
     * Constructor.
     */
    public NGSICartoDBSink() {
        super();
    } // NGSICartoDBSink
    
    @Override
    public void configure(Context context) {
        // Read NGSISink general configuration
        super.configure(context);
        
        // Read NGSICartoDB specific configuration
        String endpoint = context.getString("endpoint");
        
        if (endpoint == null) {
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Invalid configuration (endpoint=null) -- Must not be empty)");
            return;
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (endpoint=" + endpoint + ")");
        } // else
        
        int index = 0;
        
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
                    + "-- The URI must use the http or https schemes)");
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
        
        if (apiKey == null) {
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Invalid configuration (apiKey=null) -- Must not be empty)");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (apiKey=" + apiKey + ")");
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

            // get an aggregator for this destination and initialize it
            CartoDBAggregator aggregator = new CartoDBAggregator();
            aggregator.initialize(subBatch.get(0));

            for (NGSIEvent cygnusEvent : subBatch) {
                aggregator.aggregate(cygnusEvent);
            } // for

            // persist the aggregation
            persistAggregation(aggregator);
            batch.setPersisted(destination);
        } // for
    } // persistBatch
    
    /**
     * Convenience class for aggregating data.
     */
    private class CartoDBAggregator {
        
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
            
            return fields + ")";
        } // getFields
        
        public void initialize(NGSIEvent cygnusEvent) throws Exception {
            service = cygnusEvent.getService();
            servicePath = cygnusEvent.getServicePath();
            entity = cygnusEvent.getEntity();
            attribute = cygnusEvent.getAttribute();
            dbName = buildDbName(service);
            tableName = buildTableName(servicePath, entity, attribute);
            
            // aggregation initialization
            aggregation = new LinkedHashMap<String, ArrayList<String>>();
            aggregation.put(NGSIConstants.RECV_TIME, new ArrayList<String>());
            aggregation.put(NGSIConstants.FIWARE_SERVICE_PATH, new ArrayList<String>());
            aggregation.put(NGSIConstants.ENTITY_ID, new ArrayList<String>());
            aggregation.put(NGSIConstants.ENTITY_TYPE, new ArrayList<String>());
            
            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = cygnusEvent.getContextElement().getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                return;
            } // if
            
            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                aggregation.put(attrName, new ArrayList<String>());
                aggregation.put(attrName + "_md", new ArrayList<String>());
            } // for
        } // initialize
        
        public void aggregate(NGSIEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = NGSIUtils.getHumanReadable(recvTimeTs, true);

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
                aggregation.get(attrName).add(NGSIUtils.getLocation(attrValue, attrMetadata));
                aggregation.get(attrName + "_md").add(attrMetadata);
            } // for
        } // aggregate
        
    } // CartoDBAggregator
    
    private void persistAggregation(CartoDBAggregator aggregator) throws Exception {
        String rows = aggregator.getRows();
        String dbName = aggregator.getDbName(); // enable_lowercase is unncessary, PostgreSQL is case insensitive
        String tableName = aggregator.getTableName(); // enable_lowercase is unncessary, PostgreSQL is case insensitive
        String fields = aggregator.getFields();
        LOGGER.info("[" + this.getName() + "] Persisting data at NGSICartoDBSink. Database (" + dbName
                + "), Table (" + tableName + "), Data (" + rows + ")");
        backend.insert(tableName, fields, rows);
    } // persistAggregation
    
    private String buildDbName(String service) throws Exception {
        String name = NGSIUtils.encodePostgreSQL(service, false);

        if (name.length() > NGSIConstants.POSTGRESQL_MAX_ID_LEN) {
            throw new CygnusBadConfiguration("Building database name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_ID_LEN);
        } // if

        return name;
    } // buildDbName
    
    private String buildTableName(String servicePath, String entity, String attribute) throws Exception {
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
            case DMBYATTRIBUTE:
                truncatedServicePath = NGSIUtils.encodePostgreSQL(servicePath, true);
                name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '_')
                        + NGSIUtils.encodePostgreSQL(entity, false)
                        + '_' + NGSIUtils.encodePostgreSQL(attribute, false);
                break;
            default:
                throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                        + "'. Please, use DMBYSERVICEPATH, DMBYENTITY or DMBYATTRIBUTE");
        } // switch
        
        if (name.length() > NGSIConstants.POSTGRESQL_MAX_ID_LEN) {
            throw new CygnusBadConfiguration("Building table name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_ID_LEN);
        } // if

        return name;
    } // buildTableName
    
} // NGSICartoDBSink
