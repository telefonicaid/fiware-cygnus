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
import java.util.Map;

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
    void persist(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        // get some header values
        Long recvTimeTs = new Long(eventHeaders.get("timestamp"));
        String fiwareService = eventHeaders.get(Constants.HEADER_SERVICE);
        String fiwareServicePath = eventHeaders.get(Constants.HEADER_SERVICE_PATH);
        String[] destinations = eventHeaders.get(Constants.DESTINATION).split(",");

        // human readable version of the reception time
        String recvTime = Utils.getHumanReadable(recvTimeTs);

        // create the database for this fiwareService if not yet existing... the cost of trying to create it is the same
        // than checking if it exits and then creating it
        String dbName = buildDbName(fiwareService);
        backend.createDatabase(dbName);
        
        // collection name container
        String collectionName = null;

        // create the collection at this stage, if the data model is collection-per-service-path
        if (dataModel == DataModel.COLLECTIONPERSERVICEPATH) {
            collectionName = buildCollectionName(fiwareServicePath, null, null) + ".aggr";
            backend.createCollection(dbName, collectionName);
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
                collectionName = buildCollectionName(fiwareServicePath, destinations[i], null) + ".aggr";
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
                    collectionName = buildCollectionName(fiwareServicePath, destinations[i], attrName) + ".aggr";
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
    } // persist
    
} // OrionSTHSink
