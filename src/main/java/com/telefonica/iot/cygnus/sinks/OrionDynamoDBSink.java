/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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

import com.amazonaws.services.dynamodbv2.document.Item;
import com.telefonica.iot.cygnus.backends.dynamo.DynamoDBBackend;
import com.telefonica.iot.cygnus.backends.dynamo.DynamoDBBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import org.apache.flume.Context;

/**
 *
 * @author frb
 */
public class OrionDynamoDBSink extends OrionSink {
    
    /**
     * Available table types.
     */
    public enum TableType { TABLEBYDESTINATION, TABLEBYSERVICEPATH }
    
    private static final CygnusLogger LOGGER = new CygnusLogger(OrionDynamoDBSink.class);
    private DynamoDBBackend persistenceBackend;
    private String accessKeyId;
    private String secretAccessKey;
    private String region;
    private TableType tableType;
    private boolean attrPersistenceRow;
    private long id = new Date().getTime();
    
    protected DynamoDBBackend getPersistenceBackend() {
        return persistenceBackend;
    } // getPersistenceBackend
    
    protected void setPersistenceBackend(DynamoDBBackend persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    } // setPersistenceBackend
    
    protected String getAccessKeyId() {
        return accessKeyId;
    } // getAccessKeyId
    
    protected String getSecretAccessKey() {
        return secretAccessKey;
    } // getSecretAccessKey
    
    protected String getRegion() {
        return region;
    } // getRegion
    
    protected boolean getRowAttrPersistence() {
        return attrPersistenceRow;
    } // getRowAttrPersistence
    
    protected TableType getTableType() {
        return tableType;
    } // getTableType
    
    @Override
    public void configure(Context context) {
        accessKeyId = context.getString("access_key_id");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (access_key_id=" + accessKeyId + ")");
        secretAccessKey = context.getString("secret_access_key");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (secret_access_key=" + secretAccessKey + ")");
        region = context.getString("region", "eu-central-1");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (region=" + region + ")");
        String tableTypeStr = context.getString("table_type", "table-per-destination");
        tableType = TableType.valueOf(tableTypeStr.replaceAll("-", "").toUpperCase());
        LOGGER.debug("[" + this.getName() + "] Reading configuration (table_type=" + tableTypeStr + ")");
        String attrPersistRowStr = context.getString("attr_persistence", "row");
        attrPersistenceRow = attrPersistRowStr.equals("row");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_persistence=" + attrPersistRowStr + ")");
        super.configure(context);
    } // configure
    
    @Override
    public void start() {
        try {
            persistenceBackend = new DynamoDBBackendImpl(accessKeyId, secretAccessKey, region);
            LOGGER.debug("[" + this.getName() + "] DynamoDB persistence backend created");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } // try catch
        
        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

    // TBD: to be removed once all the sinks have been migrated to persistBatch method
    @Override
    void persistOne(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        Accumulator accumulator = new Accumulator();
        accumulator.initializeBatching(new Date().getTime());
        accumulator.accumulate(eventHeaders, notification);
        persistBatch(accumulator.getDefaultBatch(), accumulator.getGroupedBatch());
    } // persistOne
    
    @Override
    void persistBatch(Batch defaultBatch, Batch groupedBatch) throws Exception {
        // select batch depending on the enable grouping parameter
        Batch batch = (enableGrouping ? groupedBatch : defaultBatch);
        
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if
 
        // iterate on the destinations, for each one a single create / append will be performed
        for (String destination : batch.getDestinations()) {
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the " + destination
                    + " destination");

            // get the sub-batch for this destination
            ArrayList<CygnusEvent> subBatch = batch.getEvents(destination);
            
            // get an aggregator for this destination and initialize it
            DynamoDBAggregator aggregator = getAggregator(attrPersistenceRow);
            aggregator.initialize(subBatch.get(0));

            for (CygnusEvent cygnusEvent : subBatch) {
                aggregator.aggregate(cygnusEvent);
            } // for
            
            // persist the aggregation
            persistAggregation(aggregator);
            batch.setPersisted(destination);
        } // for
    } // persistBatch

    /**
     * Class for aggregating data regarding a destination in a servicePath, in a service.
     */
    private abstract class DynamoDBAggregator {
        
        // string containing the data aggregation
        protected ArrayList<Item> aggregation;
        protected String service;
        protected String servicePath;
        protected String destination;
        protected String tableName;
        
        public ArrayList<Item> getAggregation() {
            return aggregation;
        } // getAggregation
        
        public String getTableName() {
            return tableName;
        } // getTableName
        
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            service = cygnusEvent.getService();
            servicePath = cygnusEvent.getServicePath();
            destination = cygnusEvent.getDestination();
            tableName = buildTableName(service, servicePath, destination, tableType);
            aggregation = new ArrayList<Item>();
        } // initialize
        
        public abstract void aggregate(CygnusEvent cygnusEvent) throws Exception;
        
    } // DynamoDBAggregator
    
    /**
     * Class for aggregating batches in row mode.
     */
    private class DynamoDBRowAggregator extends DynamoDBAggregator {
        
        @Override
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
        } // initialize
        
        @Override
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = Utils.getHumanReadable(recvTimeTs, true);

            // get the event body
            NotifyContextRequest.ContextElement contextElement = cygnusEvent.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");
            
            // iterate on all this context element attributes, if there are attributes
            ArrayList<NotifyContextRequest.ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if
            
            for (NotifyContextRequest.ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                
                // create an item and aggregate it
                Item item = new Item()
                        .withPrimaryKey(Constants.DYNAMO_DB_PRIMARY_KEY, id)
                        .withDouble(Constants.RECV_TIME_TS, recvTimeTs / 1000)
                        .withString(Constants.RECV_TIME, recvTime)
                        .withString(Constants.HEADER_NOTIFIED_SERVICE_PATH, servicePath)
                        .withString(Constants.ENTITY_ID, entityId)
                        .withString(Constants.ENTITY_TYPE, entityType)
                        .withString(Constants.ATTR_NAME, attrName)
                        .withString(Constants.ATTR_TYPE, attrType)
                        .withString(Constants.ATTR_VALUE, attrValue)
                        .withString(Constants.ATTR_MD, attrMetadata);
                aggregation.add(item);
                
                // id count update
                id++;
            } // for
        } // aggregate

    } // DynamoDBRowAggregator
    
    /**
     * Class for aggregating batches in column mode.
     */
    private class DynamoDBColumnAggregator extends DynamoDBAggregator {

        @Override
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
        } // initialize
        
        @Override
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = Utils.getHumanReadable(recvTimeTs, true);

            // get the event body
            NotifyContextRequest.ContextElement contextElement = cygnusEvent.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");
            
            // iterate on all this context element attributes, if there are attributes
            ArrayList<NotifyContextRequest.ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if
            
            // create an item
            Item item = new Item()
                    .withPrimaryKey(Constants.DYNAMO_DB_PRIMARY_KEY, id)
                    .withString(Constants.RECV_TIME, recvTime)
                    .withString(Constants.HEADER_NOTIFIED_SERVICE_PATH, servicePath)
                    .withString(Constants.ENTITY_ID, entityId)
                    .withString(Constants.ENTITY_TYPE, entityType);
            
            for (NotifyContextRequest.ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                
                item.withString(attrName, attrValue)
                        .withString(attrName + "_md", attrMetadata);
            } // for
            
            // aggregate the item
            aggregation.add(item);
            
            // id count update
            id++;
        } // aggregate
        
    } // DynamoDBColumnAggregator
    
    private DynamoDBAggregator getAggregator(boolean attrPersistenceRow) {
        if (attrPersistenceRow) {
            return new DynamoDBRowAggregator();
        } else {
            return new DynamoDBColumnAggregator();
        } // if else
    } // getAggregator
    
    private void persistAggregation(DynamoDBAggregator aggregator) throws Exception {
        ArrayList aggregation = aggregator.getAggregation();
        String tableName = aggregator.getTableName();
        
        LOGGER.info("[" + this.getName() + "] Persisting data at OrionDynamoSink. Dynamo table ("
                + tableName + "), Data (" + aggregation.toString() + ")");
        
        // tables can be always created in DynamoDB, independedntly of the attribute persistence mode,
        // since it is NoSQL and there is no fixed structure
        persistenceBackend.createTable(tableName, Constants.DYNAMO_DB_PRIMARY_KEY);
        persistenceBackend.putItems(tableName, aggregation);
    } // persistAggregation
    
    private String buildTableName(String service, String servicePath, String destination, TableType tableType)
        throws Exception {
        String tableName;
        
        switch (tableType) {
            case TABLEBYDESTINATION:
                tableName = service + "_" + servicePath + "_" + destination;
                break;
            case TABLEBYSERVICEPATH:
                tableName = service + "_" + servicePath;
                break;
            default:
                throw new CygnusBadConfiguration("Unknown table type (" + tableType.toString()
                        + " in OrionDynamoDBSink, cannot build the table name. Please, use "
                        + "TABLEBYSERVICEPATH or TABLEBYDESTINATION");
        } // switch
        
        if (tableName.length() > Constants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building tableName=fiwareServicePath + '_' + destination (" + tableName
                    + ") and its length is greater than " + Constants.MAX_NAME_LEN);
        } // if
        
        return tableName;
    } // buildTableName
    
} // OrionDynamoDBSink
