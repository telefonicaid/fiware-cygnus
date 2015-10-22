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

import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import static com.telefonica.iot.cygnus.sinks.OrionMongoBaseSink.LOGGER;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.flume.Event;

/**
 *
 * @author frb
 */
public class OrionSTHSink extends OrionMongoBaseSink {

    /**
     * Constructor.
     */
    public OrionSTHSink() {
        super();
    } // OrionSTHSink
    
    @Override
    void persistOne(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        // get some header values; they are not null nor empty thanks to OrionRESTHandler
        Long recvTimeTs = new Long(eventHeaders.get(Constants.HEADER_TIMESTAMP));
        String fiwareService = eventHeaders.get(Constants.HEADER_NOTIFIED_SERVICE);
        String[] servicePaths;
        String[] destinations;
        
        if (enableGrouping) {
            servicePaths = eventHeaders.get(Constants.HEADER_GROUPED_SERVICE_PATHS).split(",");
            destinations = eventHeaders.get(Constants.HEADER_GROUPED_DESTINATIONS).split(",");
        } else {
            servicePaths = eventHeaders.get(Constants.HEADER_DEFAULT_SERVICE_PATHS).split(",");
            destinations = eventHeaders.get(Constants.HEADER_DEFAULT_DESTINATIONS).split(",");
        } // if else
        
        for (int i = 0; i < servicePaths.length; i++) {
            servicePaths[i] = "/" + servicePaths[i]; // this sink uses the removed initial slash
        } // for

        // human readable version of the reception time
        String recvTime = Utils.getHumanReadable(recvTimeTs, true);

        // create the database for this fiwareService if not yet existing... the cost of trying to create it is the same
        // than checking if it exits and then creating it
        String dbName = buildDbName(fiwareService);
        backend.createDatabase(dbName);
        
        // collection name container
        String collectionName = null;

        // create the collection at this stage, if the data model is collection-per-service-path
        if (dataModel == DataModel.COLLECTIONPERSERVICEPATH) {
            for (String fiwareServicePath : servicePaths) {
                collectionName = buildCollectionName(dbName, fiwareServicePath, null, null, true, null, null,
                        fiwareService) + ".aggr";
                backend.createCollection(dbName, collectionName);
            } // for
        } // if
        
        // iterate on the contextResponses
        ArrayList contextResponses = notification.getContextResponses();
        
        for (int i = 0; i < contextResponses.size(); i++) {
            NotifyContextRequest.ContextElementResponse contextElementResponse;
            contextElementResponse = (NotifyContextRequest.ContextElementResponse) contextResponses.get(i);
            NotifyContextRequest.ContextElement contextElement = contextElementResponse.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + this.getName() + "] Processing context element (id=" + entityId + ", type= "
                    + entityType + ")");
            
            // create the collection at this stage, if the data model is collection-per-entity
            if (dataModel == DataModel.COLLECTIONPERENTITY) {
                collectionName = buildCollectionName(dbName, servicePaths[i], destinations[i], null, true,
                        entityId, entityType, fiwareService) + ".aggr";
                backend.createCollection(dbName, collectionName);
            } // if
            
            // iterate on all this entity's attributes, if there are attributes
            ArrayList<NotifyContextRequest.ContextAttribute> contextAttributes = contextElement.getAttributes();
            
            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                continue;
            } // if

            for (NotifyContextRequest.ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + this.getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                
                if (!Utils.isANumber(attrValue)) {
                    LOGGER.debug("[" + this.getName() + "] Context attribute discarded since it is not numerical");
                    continue;
                } // if
                
                // create the collection at this stage, if the data model is collection-per-attribute
                if (dataModel == DataModel.COLLECTIONPERATTRIBUTE) {
                    collectionName = buildCollectionName(dbName, servicePaths[i], destinations[i], attrName,
                            true, entityId, entityType, fiwareService) + ".aggr";
                    backend.createCollection(dbName, collectionName);
                } // if

                LOGGER.info("[" + this.getName() + "] Persisting data at OrionSTHSink. Database: " + dbName
                        + ", Collection: " + collectionName + ", Data: " + recvTimeTs / 1000 + "," + recvTime + ","
                        + entityId + "," + entityType + "," + attrName + "," + entityType + "," + attrValue + ","
                        + attrMetadata);
                backend.insertContextDataAggregated(dbName, collectionName, recvTimeTs / 1000, recvTime,
                        entityId, entityType, attrName, attrType, attrValue, attrMetadata);
            } // for
        } // for
    } // persistOne
    
    @Override
    void persistBatch(Batch defaultBatch, Batch groupedBatch) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    } // persistBatch
    
} // OrionSTHSink
