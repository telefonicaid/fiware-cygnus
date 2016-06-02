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

import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.sinks.Enums.DataModel;
import static com.telefonica.iot.cygnus.sinks.NGSIMongoBaseSink.LOGGER;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import java.util.ArrayList;
import org.apache.flume.Context;

/**
 *
 * @author frb
 */
public class NGSISTHSink extends NGSIMongoBaseSink {
    
    protected final boolean[] resolutions = {false, false, false, false, false};

    /**
     * Constructor.
     */
    public NGSISTHSink() {
        super();
    } // NGSISTHSink
    
    @Override
    public void configure(Context context) {
        String resolutionsStr = context.getString("resolutions", "month,day,hour,minute,second");
        String[] resolutionsArray = resolutionsStr.split(",");
        
        for (String resolution : resolutionsArray) {
            if (resolution.trim().equals("month")) {
                resolutions[4] = true;
            } else if (resolution.trim().equals("day")) {
                resolutions[3] = true;
            } else if (resolution.trim().equals("hour")) {
                resolutions[2] = true;
            } else if (resolution.trim().equals("minute")) {
                resolutions[1] = true;
            } else if (resolution.trim().equals("second")) {
                resolutions[0] = true;
            } else {
                LOGGER.warn("[" + this.getName() + "] Unknown resolution " + resolution);
            } // if else
        } // for
        
        LOGGER.debug("[" + this.getName() + "] Reading configuration (resolutions=" + resolutionsStr + ")");
        super.configure(context);
    } // configure
    
    @Override
    public void persistBatch(NGSIBatch batch) throws Exception {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if
 
        // iterate on the destinations
        for (String destination : batch.getDestinations()) {
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the " + destination
                    + " destination");

            // get the sub-batch for this destination
            ArrayList<NGSIEvent> subBatch = batch.getEvents(destination);
            
            // iterate on the events within the sub-batch... events are not aggregated but directly persisted
            for (NGSIEvent cygnusEvent : subBatch) {
                persistOne(cygnusEvent);
            } // for
            
            // set the sub-batch as persisted
            batch.setPersisted(destination);
        } // for
    } // persistBatch
    
    private void persistOne(NGSIEvent event) throws Exception {
        // get some values from the event
        Long notifiedRecvTimeTs = event.getRecvTimeTs();
        String fiwareService = event.getService();
        String fiwareServicePath = event.getServicePath();
        String destination = event.getEntity();
        ContextElement contextElement = event.getContextElement();

        // human readable version of the reception time
        String notifiedRecvTime = CommonUtils.getHumanReadable(notifiedRecvTimeTs, true);

        // create the database for this fiwareService if not yet existing... the cost of trying to create it is the same
        // than checking if it exits and then creating it
        String dbName = enableLowercase ? buildDbName(fiwareService).toLowerCase() : buildDbName(fiwareService);
        backend.createDatabase(dbName);
        
        // collection name container
        String collectionName = null;

        // create the collection at this stage, if the data model is collection-per-service-path
        if (dataModel == DataModel.DMBYSERVICEPATH) {
            collectionName = enableLowercase
                    ? (buildCollectionName(dbName, fiwareServicePath, null, null, true, null, null,
                            fiwareService) + ".aggr").toLowerCase()
                    : buildCollectionName(dbName, fiwareServicePath, null, null, true, null, null,
                            fiwareService) + ".aggr";
            backend.createCollection(dbName, collectionName, dataExpiration);
        } // if
        
        String entityId = contextElement.getId();
        String entityType = contextElement.getType();
        LOGGER.debug("[" + this.getName() + "] Processing context element (id=" + entityId + ", type= "
                + entityType + ")");

        // create the collection at this stage, if the data model is collection-per-entity
        if (dataModel == DataModel.DMBYENTITY) {
            collectionName = enableLowercase
                    ? (buildCollectionName(dbName, fiwareServicePath, destination, null, true,
                            entityId, entityType, fiwareService) + ".aggr").toLowerCase()
                    : buildCollectionName(dbName, fiwareServicePath, destination, null, true,
                            entityId, entityType, fiwareService) + ".aggr";
            backend.createCollection(dbName, collectionName, dataExpiration);
        } // if

        // iterate on all this entity's attributes, if there are attributes
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
            
            // check if the attribute value is based on white spaces
            if (ignoreWhiteSpaces && attrValue.trim().length() == 0) {
                continue;
            } // if
            
            LOGGER.debug("[" + this.getName() + "] Processing context attribute (name=" + attrName + ", type="
                    + attrType + ")");

            // check if the metadata contains a TimeInstant value; use the notified reception time instead
            Long recvTimeTs;

            Long timeInstant = CommonUtils.getTimeInstant(attrMetadata);

            if (timeInstant != null) {
                recvTimeTs = timeInstant;
            } else {
                recvTimeTs = notifiedRecvTimeTs;
            } // if else

            // create the collection at this stage, if the data model is collection-per-attribute
            if (dataModel == DataModel.DMBYATTRIBUTE) {
                collectionName = enableLowercase
                        ? (buildCollectionName(dbName, fiwareServicePath, destination, attrName,
                                true, entityId, entityType, fiwareService) + ".aggr").toLowerCase()
                        : buildCollectionName(dbName, fiwareServicePath, destination, attrName,
                                true, entityId, entityType, fiwareService) + ".aggr";
                backend.createCollection(dbName, collectionName, dataExpiration);
            } // if

            // insert the data
            LOGGER.info("[" + this.getName() + "] Persisting data at OrionSTHSink. Database: " + dbName
                    + ", Collection: " + collectionName + ", Data: " + recvTimeTs + ","
                    + entityId + "," + entityType + "," + attrName + "," + attrType + "," + attrValue + ","
                    + attrMetadata);
            backend.insertContextDataAggregated(dbName, collectionName, recvTimeTs,
                    entityId, entityType, attrName, attrType, attrValue, attrMetadata, resolutions);
        } // for
    } // persistOne
    
} // NGSISTHSink
