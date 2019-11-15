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

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import static com.telefonica.iot.cygnus.sinks.NGSIMongoBaseSink.LOGGER;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import java.util.ArrayList;
import java.util.Date;
import org.bson.Document;
import org.apache.flume.Context;

/**
 * @author frb
 * @author xdelox
 * 
 * Detailed documentation can be found at:
 * https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/flume_extensions_catalogue/ngsi_mongo_sink.md
 */
public class NGSIMongoSink extends NGSIMongoBaseSink {
    
    private long collectionsSize;
    private long maxDocuments;

    private boolean rowAttrPersistence;
    private String attrMetadataStore;
    /**
     * Constructor.
     */
    public NGSIMongoSink() {
        super();
    } // NGSIMongoSink
    
    public boolean getRowAttrPersistence() {
        return rowAttrPersistence;
    }
    
    @Override
    public void configure(Context context) {
        collectionsSize = context.getLong("collections_size", 0L);
        
        if ((collectionsSize > 0) && (collectionsSize < 4096)) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (collections_size="
                    + collectionsSize + ") -- Must be greater than or equal to 4096");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (collections_size=" + collectionsSize + ")");
        }  // if else
       
        maxDocuments = context.getLong("max_documents", 0L);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (max_documents=" + maxDocuments + ")");
        
        String attrPersistenceStr = context.getString("attr_persistence", "row");
        
        if (attrPersistenceStr.equals("row") || attrPersistenceStr.equals("column")) {
            rowAttrPersistence = attrPersistenceStr.equals("row");
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_persistence="
                + attrPersistenceStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (attr_persistence="
                + attrPersistenceStr + ") must be 'row' or 'column'");
        }  // if else
        
        attrMetadataStore = context.getString("attr_metadata_store", "false");

        if (attrMetadataStore.equals("true") || attrMetadataStore.equals("false")) {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_metadata_store="
                    + attrMetadataStore + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (attr_metadata_store="
                    + attrMetadataStore + ") must be 'true' or 'false'");
        } // if else 

        super.configure(context);
    } // configure

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
            
            // get an aggregator for this destination and initialize it
            MongoDBAggregator aggregator = getAggregator(rowAttrPersistence);
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
     * Class for aggregating batches.
     */
    private abstract class MongoDBAggregator {
        
        // string containing the data fieldValues
        protected ArrayList<Document> aggregation;

        protected String service;
        protected String servicePathForData;
        protected String servicePathForNaming;
        protected String entityForNaming;
        protected String attributeForNaming;
        protected String dbName;
        protected String collectionName;
        
        public MongoDBAggregator() {
            aggregation = new ArrayList<>();
        } // MongoDBAggregator
        
        public ArrayList<Document> getAggregation() {
            return aggregation;
        } // getAggregation
        
        public String getDbName(boolean enableLowercase) {
            if (enableLowercase) {
                return dbName.toLowerCase();
            } else {
                return dbName;
            } // if else
        } // getDbName
        
        public String getCollectionName(boolean enableLowercase) {
            if (enableLowercase) {
                return collectionName.toLowerCase();
            } else {
                return collectionName;
            } // if else
        } // getCollectionName
        
        public void initialize(NGSIEvent event) throws CygnusBadConfiguration {
            service = event.getServiceForNaming(enableNameMappings);
            servicePathForData = event.getServicePathForData();
            servicePathForNaming = event.getServicePathForNaming(enableGrouping, enableNameMappings);
            entityForNaming = event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding);
            attributeForNaming = event.getAttributeForNaming(enableNameMappings);
            dbName = buildDbName(service);
            collectionName = buildCollectionName(servicePathForNaming, entityForNaming, attributeForNaming);
        } // initialize
        
        public abstract void aggregate(NGSIEvent cygnusEvent);
        
    } // MongoDBAggregator
    
    /**
     * Class for aggregating batches in row mode.
     */
    private class RowAggregator extends MongoDBAggregator {

        @Override
        public void initialize(NGSIEvent cygnusEvent) throws CygnusBadConfiguration {
            super.initialize(cygnusEvent);
        } // initialize
        
        @Override
        public void aggregate(NGSIEvent cygnusEvent) {
            // get the event headers
            long notifiedRecvTimeTs = cygnusEvent.getRecvTimeTs();

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
                String attrMetadata = contextAttribute.getContextMetadata();
                
                // check if the attribute value is based on white spaces
                if (ignoreWhiteSpaces && attrValue != null && attrValue.trim().length() == 0) {
                    continue;
                } // if
                
                // check if the metadata contains a TimeInstant value; use the notified reception time instead
                Long recvTimeTs;

                Long timeInstant = CommonUtils.getTimeInstant(attrMetadata);

                if (timeInstant != null) {
                    recvTimeTs = timeInstant;
                } else {
                    recvTimeTs = notifiedRecvTimeTs;
                } // if else
                
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                
                Document doc;

                if(attrMetadataStore.equals("true")){
                    doc = createDocWithMetadata(recvTimeTs, entityId, entityType, attrName, attrType, attrValue, attrMetadata);
                } else {
                    doc = createDoc(recvTimeTs, entityId, entityType, attrName, attrType, attrValue);
                } // if else

                aggregation.add(doc);
            } // for
        } // aggregate
        
        private Document createDocWithMetadata(Long recvTimeTs, String entityId, String entityType, String attrName,
                String attrType, String attrValue, String attrMetadata) {
            Document doc = new Document("recvTime", new Date(recvTimeTs));

            switch (dataModel) {
            case DMBYSERVICEPATH:
                doc.append("entityId", entityId)
                        .append("entityType", entityType)
                        .append("attrName", attrName)
                        .append("attrType", attrType)
                        .append("attrValue", attrValue)
                        .append("attrMetadata", (DBObject)JSON.parse(attrMetadata));
                break;
            case DMBYENTITY:
                doc.append("attrName", attrName)
                        .append("attrType", attrType)
                        .append("attrValue", attrValue)
                        .append("attrMetadata", (DBObject)JSON.parse(attrMetadata));
                break;
            case DMBYATTRIBUTE:
                doc.append("attrType", attrType)
                        .append("attrValue", attrValue)
                        .append("attrMetadata", (DBObject)JSON.parse(attrMetadata));
                break;
            default:
                return null; // this will never be reached
            } // switch

            return doc;
        } // createDocWithMetadata
        
        private Document createDoc(long recvTimeTs, String entityId, String entityType, String attrName,
                String attrType, String attrValue) {
            Document doc = new Document("recvTime", new Date(recvTimeTs));
        
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
        public void initialize(NGSIEvent cygnusEvent) throws CygnusBadConfiguration {
            super.initialize(cygnusEvent);
        } // initialize
        
        @Override
        public void aggregate(NGSIEvent cygnusEvent) {
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
                
                // check if the attribute value is based on white spaces
                if (ignoreWhiteSpaces && attrValue != null && attrValue.trim().length() == 0) {
                    continue;
                } // if
                
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                doc.append(attrName, attrValue);
            } // for

            aggregation.add(doc);
        } // aggregate
        
        private Document createDoc(long recvTimeTs, String entityId, String entityType) {
            Document doc = new Document("recvTime", new Date(recvTimeTs));

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
    
    private void persistAggregation(MongoDBAggregator aggregator) throws CygnusPersistenceError {
        ArrayList<Document> aggregation = aggregator.getAggregation();
        
        if (aggregation.isEmpty()) {
            return;
        } // if
        
        String dbName = aggregator.getDbName(enableLowercase);
        String collectionName = aggregator.getCollectionName(enableLowercase);
        LOGGER.info("[" + this.getName() + "] Persisting data at NGSIMongoSink. Database: "
                + dbName + ", Collection: " + collectionName + ", Data: " + aggregation.toString());
        
        try {
            backend.createDatabase(dbName);
            backend.createCollection(dbName, collectionName, collectionsSize, maxDocuments, dataExpiration);
            backend.insertContextDataRaw(dbName, collectionName, aggregation);
        } catch (Exception e) {
            throw new CygnusPersistenceError("-, " + e.getMessage());
        } // try catch
    } // persistAggregation

} // NGSIMongoSink
