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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericColumnAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericRowAggregator;
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
import java.util.LinkedHashMap;

import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
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
            NGSIGenericAggregator aggregator = getAggregator(rowAttrPersistence);
            aggregator.setService(events.get(0).getServiceForNaming(enableNameMappings));
            aggregator.setServicePathForData(events.get(0).getServicePathForData());
            aggregator.setServicePathForNaming(events.get(0).getServicePathForNaming(enableGrouping, enableNameMappings));
            aggregator.setEntityForNaming(events.get(0).getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding));
            aggregator.setEntityType(events.get(0).getEntityTypeForNaming(enableGrouping, enableNameMappings));
            aggregator.setAttribute(events.get(0).getAttributeForNaming(enableNameMappings));
            aggregator.setDbName(buildDbName(aggregator.getService()));
            aggregator.setAttrMetadataStore(Boolean.valueOf(attrMetadataStore));
            aggregator.setCollectionName(buildCollectionName(aggregator.getServicePathForNaming(), aggregator.getEntityForNaming(), aggregator.getAttribute()));
            aggregator.setEnableNameMappings(enableNameMappings);
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
    
    protected NGSIGenericAggregator getAggregator(boolean rowAttrPersistence) {
        if (rowAttrPersistence) {
            return new NGSIGenericRowAggregator();
        } else {
            return new NGSIGenericColumnAggregator();
        } // if else
    } // getAggregator

    protected ArrayList<String> getKeysToCrop (boolean rowAttrPersistence){
        ArrayList<String> keysToCrop = new ArrayList<>();

        switch (dataModel) {
            case DMBYSERVICEPATH:
                if (rowAttrPersistence) {
                    keysToCrop.add(NGSIConstants.RECV_TIME_TS);
                    keysToCrop.add(NGSIConstants.RECV_TIME);
                    keysToCrop.add(NGSIConstants.FIWARE_SERVICE_PATH);
                } else {
                    keysToCrop.add(NGSIConstants.RECV_TIME);
                    keysToCrop.add(NGSIConstants.FIWARE_SERVICE_PATH);
                }
                break;
            case DMBYENTITY:
                if (rowAttrPersistence) {
                    keysToCrop.add(NGSIConstants.RECV_TIME_TS);
                    keysToCrop.add(NGSIConstants.RECV_TIME);
                    keysToCrop.add(NGSIConstants.FIWARE_SERVICE_PATH);
                    keysToCrop.add(NGSIConstants.ENTITY_ID);
                    keysToCrop.add(NGSIConstants.ENTITY_TYPE);
                } else {
                    keysToCrop.add(NGSIConstants.RECV_TIME);
                    keysToCrop.add(NGSIConstants.FIWARE_SERVICE_PATH);
                    keysToCrop.add(NGSIConstants.ENTITY_ID);
                    keysToCrop.add(NGSIConstants.ENTITY_TYPE);
                }
                break;
            case DMBYATTRIBUTE:
                if (rowAttrPersistence) {
                    keysToCrop.add(NGSIConstants.RECV_TIME);
                    keysToCrop.add(NGSIConstants.RECV_TIME_TS);
                    keysToCrop.add(NGSIConstants.FIWARE_SERVICE_PATH);
                    keysToCrop.add(NGSIConstants.ENTITY_ID);
                    keysToCrop.add(NGSIConstants.ENTITY_TYPE);
                    keysToCrop.add(NGSIConstants.ATTR_NAME);
                }
                break;
            default:
        }
        return keysToCrop;
    }

    protected BasicDBObject castDate (String key, BasicDBObject basicDBObject){
        try {
            LOGGER.debug("[" + this.getName() + "] Casting to Date att with key: " + key + " value: "
                         + basicDBObject.get(key));
            String str = basicDBObject.get(key).toString();
            Date date = new Date(CommonUtils.getTimeInstantFromString(str));
            basicDBObject.put(key, date);
        } catch (Exception e) {
            // Never reached
            LOGGER.error("[" + this.getName() + "] Casting to Date att with key: " + key + " value: "
                         + basicDBObject.get(key) + " Details=" + e.getMessage());
        }
        return basicDBObject;
    }
    
    private void persistAggregation(NGSIGenericAggregator aggregator) throws CygnusPersistenceError {
        ArrayList<String> keysToCrop = getKeysToCrop(rowAttrPersistence);
        LinkedHashMap<String, ArrayList<JsonElement>> cropedAggregation = NGSIUtils.cropLinkedHashMap(aggregator.getAggregationToPersist(), keysToCrop);
        ArrayList<JsonObject> jsonObjects = NGSIUtils.linkedHashMapToJsonList(cropedAggregation);
        ArrayList<Document> aggregation = new ArrayList<>();
        for (int i = 0 ; i < jsonObjects.size() ; i++) {
            BasicDBObject basicDBObject = BasicDBObject.parse(jsonObjects.get(i).toString());
            JsonElement attType;
            if (rowAttrPersistence) {
                attType = aggregator.getAggregation().get(NGSIConstants.ATTR_TYPE).get(i);
                if ( (attType != null &&
                      attType.isJsonPrimitive()) &&
                     (attType.getAsString().equals("ISO8601") ||
                      attType.getAsString().equals("DateTime")) ) {
                    basicDBObject = castDate(NGSIConstants.ATTR_VALUE, basicDBObject);
                }
            } else {
                for (String key : basicDBObject.keySet()) {
                    attType = aggregator.getAggregation().get(key + NGSIConstants.AUTOGENERATED_ATTR_TYPE).get(i);
                    if ( (attType != null &&
                          attType.isJsonPrimitive()) &&
                         (attType.getAsString().equals("ISO8601") ||
                          attType.getAsString().equals("DateTime")) ) {
                        basicDBObject = castDate(key, basicDBObject);
                    }
                }
            }
            aggregation.add(new Document(basicDBObject.toMap()));
            if (rowAttrPersistence) {
                Long timeInstant;
                if (aggregator.getAggregation().get(NGSIConstants.ATTR_MD).get(i).isJsonPrimitive()) {
                    timeInstant = CommonUtils.getTimeInstant(aggregator.getAggregation().get(NGSIConstants.ATTR_MD).get(i).getAsString());
                } else {
                    timeInstant = CommonUtils.getTimeInstant(aggregator.getAggregation().get(NGSIConstants.ATTR_MD).get(i).toString());
                }
                if (timeInstant != null) {
                    aggregation.get(i).append(NGSIConstants.RECV_TIME, new Date(timeInstant));
                } else {
                    aggregation.get(i).append(NGSIConstants.RECV_TIME, new Date(Long.parseLong(aggregator.getAggregation().get(NGSIConstants.RECV_TIME_TS).get(i).getAsString())));
                }
            } else {
                aggregation.get(i).append(NGSIConstants.RECV_TIME, new Date(Long.parseLong(aggregator.getAggregation().get(NGSIConstants.RECV_TIME_TS + "C").get(i).getAsString())));
            }
        }
        if (aggregation.isEmpty()) {
            return;
        } // if
        
        String dbName = aggregator.getDbName(enableLowercase);
        String collectionName = aggregator.getCollectionName(enableLowercase);
        LOGGER.info("[" + this.getName() + "] Persisting data at NGSIMongoSink. Database: "
                + dbName + ", Collection: " + collectionName + ", Data: " + aggregation.toString());
        
        try {
            // try insert without create database and collection before
            backend.insertContextDataRaw(dbName, collectionName, aggregation);
        } catch (Exception e) {
            try {
                // try insert without create collection before
                backend.createCollection(dbName, collectionName, collectionsSize, maxDocuments, dataExpiration);
                backend.insertContextDataRaw(dbName, collectionName, aggregation);
            } catch (Exception e) {
                try {
                    // insert creating database an collection before
                    backend.createDatabase(dbName);
                    backend.createCollection(dbName, collectionName, collectionsSize, maxDocuments, dataExpiration);
                    backend.insertContextDataRaw(dbName, collectionName, aggregation);
                } catch (Exception e) {
                    throw new CygnusPersistenceError("-, " + e.getMessage());
                } // try catch
            } // try catch
        } // try catch
    } // persistAggregation

} // NGSIMongoSink
