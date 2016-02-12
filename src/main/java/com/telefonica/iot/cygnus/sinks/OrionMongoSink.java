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

import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import static com.telefonica.iot.cygnus.sinks.OrionMongoBaseSink.LOGGER;
import java.util.ArrayList;
import java.util.Date;
import org.bson.Document;

/**
 * @author frb
 * @author xdelox
 * 
 * Detailed documentation can be found at:
 * https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/flume_extensions_catalogue/orion_mongo_sink.md
 */
public class OrionMongoSink extends OrionMongoBaseSink {

    /**
     * Constructor.
     */
    public OrionMongoSink() {
        super();
    } // OrionMongoSink

    @Override
    void persistBatch(Batch batch) throws Exception {
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
            MongoDBAggregator aggregator = getAggregator(rowAttrPersistence);
            aggregator.initialize(subBatch.get(0));

            for (CygnusEvent cygnusEvent : subBatch) {
                aggregator.aggregate(cygnusEvent);
            } // for
            
            // persist the fieldValues
            persistAggregation(aggregator);
            batch.setPersisted(destination);
        } // for
    } // persistBatch
    
    /**
     * Class for aggregating batches.
     */
    private abstract class MongoDBAggregator {
        
        // string containing the data fieldValues
        protected ArrayList<Document> aggregation;

        protected String service;
        protected String servicePath;
        protected String entity;
        protected String attribute;
        protected String dbName;
        protected String collectionName;
        
        public MongoDBAggregator() {
            aggregation = new ArrayList<Document>();
        } // MongoDBAggregator
        
        public ArrayList<Document> getAggregation() {
            return aggregation;
        } // getAggregation
        
        public String getDbName() {
            return dbName;
        } // getDbName
        
        public String getCollectionName() {
            return collectionName;
        } // getCollectionName
        
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            service = cygnusEvent.getService();
            servicePath = cygnusEvent.getServicePath();
            entity = cygnusEvent.getEntity();
            attribute = cygnusEvent.getAttribute();
            dbName = buildDbName(service);
            collectionName = buildCollectionName(dbName, "/" + servicePath, entity, attribute, false, null, null,
                    service);
        } // initialize
        
        public abstract void aggregate(CygnusEvent cygnusEvent) throws Exception;
        
    } // MongoDBAggregator
    
    /**
     * Class for aggregating batches in row mode.
     */
    private class RowAggregator extends MongoDBAggregator {

        @Override
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
        } // initialize
        
        @Override
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();

            // get the event body
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
            
            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                Document doc = createDoc(recvTimeTs, entityId, entityType, attrName, attrType, attrValue);
                aggregation.add(doc);
            } // for
        } // aggregate
        
        private Document createDoc(long recvTimeTs, String entityId, String entityType, String attrName,
                String attrType, String attrValue) {
            Document doc = new Document("recvTime", new Date(recvTimeTs * 1000));
        
            switch (dataModel) {
                case DMBYSERVICEPATH:
                    doc.append("entityId", entityId)
                            .append("entityType", entityType)
                            .append("attrName", attrName)
                            .append("attrType", attrType)
                            .append("attrValue", attrValue);
                    break;
                case DMBYENTITY:
                    doc.append("attrName", attrName)
                            .append("attrType", attrType)
                            .append("attrValue", attrValue);
                    break;
                case DMBYATTRIBUTE:
                    doc.append("attrType", attrType)
                            .append("attrValue", attrValue);
                    break;
                default:
                    return null; // this will never be reached
            } // switch
            
            return doc;
        } // createDoc

    } // RowAggregator
    
    /**
     * Class for aggregating batches in column mode.
     */
    private class ColumnAggregator extends MongoDBAggregator {

        @Override
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
        } // initialize
        
        @Override
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();

            // get the event body
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
            
            Document doc = createDoc(recvTimeTs, entityId, entityType);
            
            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                doc.append(attrName, attrValue);
            } // for
            
            aggregation.add(doc);
        } // aggregate
        
        private Document createDoc(long recvTimeTs, String entityId, String entityType) {
            Document doc = new Document("recvTime", new Date(recvTimeTs * 1000));

            switch (dataModel) {
                case DMBYSERVICEPATH:
                    doc.append("entityId", entityId).append("entityType", entityType);
                    break;
                case DMBYENTITY:
                    break;
                case DMBYATTRIBUTE:
                    return null; // this will never be reached
                default:
                    return null; // this will never be reached
            } // switch
            
            return doc;
        } // createDoc
        
    } // ColumnAggregator
    
    private MongoDBAggregator getAggregator(boolean rowAttrPersistence) {
        if (rowAttrPersistence) {
            return new RowAggregator();
        } else {
            return new ColumnAggregator();
        } // if else
    } // getAggregator
    
    private void persistAggregation(MongoDBAggregator aggregator) throws Exception {
        ArrayList<Document> aggregation = aggregator.getAggregation();
        String dbName = aggregator.getDbName();
        String collectionName = aggregator.getCollectionName();
        LOGGER.info("[" + this.getName() + "] Persisting data at OrionMongoSink. Database: "
                + dbName + ", Collection: " + collectionName + ", Data: " + aggregation.toString());
        backend.createDatabase(dbName);
        backend.createCollection(dbName, collectionName);
        backend.insertContextDataRaw(dbName, collectionName, aggregation);
    } // persistAggregation

} // OrionMongoSink
