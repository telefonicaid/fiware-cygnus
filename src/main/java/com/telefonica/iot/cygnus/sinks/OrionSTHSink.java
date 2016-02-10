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
import static com.telefonica.iot.cygnus.sinks.OrionMongoBaseSink.LOGGER;
import com.telefonica.iot.cygnus.utils.Utils;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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
        Accumulator accumulator = new Accumulator();
        accumulator.initialize(new Date().getTime());
        accumulator.accumulate(eventHeaders, notification);
        persistBatch(accumulator.getBatch());
    } // persistOne
    
    @Override
    public void persistBatch(Batch batch) throws Exception {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if
 
        // iterate on the destinations
        for (String destination : batch.getDestinations()) {
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the " + destination
                    + " destination");

            // get the sub-batch for this destination
            ArrayList<CygnusEvent> subBatch = batch.getEvents(destination);
            
            // iterate on the events within the sub-batch... events are not aggregated but directly persisted
            for (CygnusEvent cygnusEvent : subBatch) {
                persistOne(cygnusEvent);
            } // for
            
            // set the sub-batch as persisted
            batch.setPersisted(destination);
        } // for
    } // persistBatch
    
    private void persistOne(CygnusEvent event) throws Exception {
        // get some values from the event
        Long notifiedRecvTimeTs = event.getRecvTimeTs();
        String fiwareService = event.getService();
        String fiwareServicePath = "/" + event.getServicePath(); // this sink uses the removed initial slash
        String destination = event.getEntity();
        ContextElement contextElement = event.getContextElement();

        // human readable version of the reception time
        String notifiedRecvTime = Utils.getHumanReadable(notifiedRecvTimeTs, true);

        // create the database for this fiwareService if not yet existing... the cost of trying to create it is the same
        // than checking if it exits and then creating it
        String dbName = buildDbName(fiwareService);
        backend.createDatabase(dbName);
        
        // collection name container
        String collectionName = null;

        // create the collection at this stage, if the data model is collection-per-service-path
        if (dataModel == DataModel.DMBYSERVICEPATH) {
            collectionName = buildCollectionName(dbName, fiwareServicePath, null, null, true, null, null,
                    fiwareService) + ".aggr";
            backend.createCollection(dbName, collectionName);
        } // if
        
        String entityId = contextElement.getId();
        String entityType = contextElement.getType();
        LOGGER.debug("[" + this.getName() + "] Processing context element (id=" + entityId + ", type= "
                + entityType + ")");

        // create the collection at this stage, if the data model is collection-per-entity
        if (dataModel == DataModel.DMBYENTITY) {
            collectionName = buildCollectionName(dbName, fiwareServicePath, destination, null, true,
                    entityId, entityType, fiwareService) + ".aggr";
            backend.createCollection(dbName, collectionName);
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
            LOGGER.debug("[" + this.getName() + "] Processing context attribute (name=" + attrName + ", type="
                    + attrType + ")");

            // check if the attribute is not numerical
            if (!Utils.isANumber(attrValue)) {
                LOGGER.debug("[" + this.getName() + "] Context attribute discarded since it is not numerical");
                continue;
            } // if

            // check if the metadata contains a TimeInstant value; use the notified reception time instead
            Long recvTimeTs;
            String recvTime;

            Long timeInstant = getTimeInstant(attrMetadata);

            if (timeInstant != null) {
                recvTimeTs = timeInstant;
                recvTime = Utils.getHumanReadable(timeInstant, true);
            } else {
                recvTimeTs = notifiedRecvTimeTs;
                recvTime = notifiedRecvTime;
            } // if else

            // create the collection at this stage, if the data model is collection-per-attribute
            if (dataModel == DataModel.DMBYATTRIBUTE) {
                collectionName = buildCollectionName(dbName, fiwareServicePath, destination, attrName,
                        true, entityId, entityType, fiwareService) + ".aggr";
                backend.createCollection(dbName, collectionName);
            } // if

            // insert the data
            LOGGER.info("[" + this.getName() + "] Persisting data at OrionSTHSink. Database: " + dbName
                    + ", Collection: " + collectionName + ", Data: " + recvTimeTs / 1000 + "," + recvTime + ","
                    + entityId + "," + entityType + "," + attrName + "," + entityType + "," + attrValue + ","
                    + attrMetadata);
            backend.insertContextDataAggregated(dbName, collectionName, recvTimeTs / 1000, recvTime,
                    entityId, entityType, attrName, attrType, attrValue, attrMetadata);
        } // for
    } // persistOne
    
    private Long getTimeInstant(String metadata) throws Exception {
        Long res = null;
        JSONParser parser = new JSONParser();
        JSONArray mds = (JSONArray) parser.parse(metadata);
        
        for (Object mdObject : mds) {
            JSONObject md = (JSONObject) mdObject;
            String mdName = (String) md.get("name");
            
            if (mdName.equals("TimeInstant")) {
                String mdValue = (String) md.get("value");
                res = new Long(mdValue);
                break;
            } // if
        } // for
        
        return res;
    } // getTimeInstant
    
} // OrionSTHSink
