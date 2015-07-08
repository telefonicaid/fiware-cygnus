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
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.util.ArrayList;
import java.util.Map;
import org.apache.flume.Context;
import org.apache.kafka.clients.producer.KafkaProducer;

/**
 *
 * @author frb
 */
public class OrionKafkaSink extends OrionSink {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(OrionKafkaSink.class);
    private KafkaProducer persistenceBackend;
    
    @Override
    public void configure(Context cntxt) {
        throw new UnsupportedOperationException("Not supported yet.");
    } // configure
    
    @Override
    public void start() {
        try {
            // create the persistence backend
            persistenceBackend = new KafkaProducer();
            LOGGER.debug("[" + this.getName() + "] Kafka persistence backend (KafkaProducer) created");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } // try catch // try catch
        
        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

    @Override
    void persist(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        // get some header values
        Long recvTimeTs = new Long(eventHeaders.get("timestamp"));
        String fiwareService = eventHeaders.get(Constants.HEADER_SERVICE);
        String[] fiwareServicePaths = eventHeaders.get(Constants.HEADER_SERVICE_PATH).split(",");
        String[] destinations = eventHeaders.get(Constants.DESTINATION).split(",");
        
        // human readable version of the reception time
        String recvTime = Utils.getHumanReadable(recvTimeTs, true);
        
        // iterate on the contextResponses
        ArrayList contextResponses = notification.getContextResponses();
        
        for (int i = 0; i < contextResponses.size(); i++) {
            // get the i-th contextElement
            NotifyContextRequest.ContextElementResponse contextElementResponse = (NotifyContextRequest.ContextElementResponse) contextResponses.get(i);
            NotifyContextRequest.ContextElement contextElement = contextElementResponse.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + this.getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");
            
            
            // iterate on all this entity's attributes, if there are attributes
            ArrayList<NotifyContextRequest.ContextAttribute> contextAttributes = contextElement.getAttributes();
            
            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                continue;
            } // if
            
            // this is used for storing the attribute's names and values in a Json-like way when dealing with a per
            // column attributes persistence; in that case the persistence is not done attribute per attribute, but
            // persisting all of them at the same time
            String columnLine = "{\"" + Constants.RECV_TIME + "\":\"" + recvTime + "\",";
            
            // this is used for storing the attribute's names needed by Hive in order to create the table when dealing
            // with a per column attributes persistence; in that case the Hive table creation is not done using
            // standard 8-fields but a variable number of them
            String hiveFields = Constants.RECV_TIME + " string";

            for (NotifyContextRequest.ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + this.getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                
                if (rowAttrPersistence) {
                    // create a Json document to be persisted
                    String rowLine = "{"
                            + "\"" + Constants.RECV_TIME_TS + "\":\"" + recvTimeTs / 1000 + "\","
                            + "\"" + Constants.RECV_TIME + "\":\"" + recvTime + "\","
                            + "\"" + Constants.ENTITY_ID + "\":\"" + entityId + "\","
                            + "\"" + Constants.ENTITY_TYPE + "\":\"" + entityType + "\","
                            + "\"" + Constants.ATTR_NAME + "\":\"" + attrName + "\","
                            + "\"" + Constants.ATTR_TYPE + "\":\"" + attrType + "\","
                            + "\"" + Constants.ATTR_VALUE + "\":" + attrValue + ","
                            + "\"" + Constants.ATTR_MD + "\":" + attrMetadata
                            + "}";
                    LOGGER.info("[" + this.getName() + "] Persisting data at OrionHDFSSink. HDFS file ("
                            + hdfsFile + "), Data (" + rowLine + ")");
                    
                    // if the fileName exists, append the Json document to it; otherwise, create it with initial content
                    // and mark as existing (this avoids checking if the fileName exists each time a Json document is
                    // going to be persisted)
                    if (fileExists) {
                        persistenceBackend.append(hdfsFile, rowLine);
                    } else {
                        persistenceBackend.createDir(hdfsFolder);
                        persistenceBackend.createFile(hdfsFile, rowLine);
                        persistenceBackend.provisionHiveTable(hdfsFolder);
                        fileExists = true;
                    } // if else
                } else {
                    columnLine += "\"" + attrName + "\":" + attrValue + ", \"" + attrName + "_md\":" + attrMetadata
                            + ",";
                    hiveFields += "," + attrName + " string," + attrName + "_md array<string>";
                } // if else
            } // for
                 
            // if the attribute persistence mode is per column, now is the time to insert a new row containing full
            // attribute list
            if (!rowAttrPersistence) {
                // insert a new row containing full attribute list
                columnLine = columnLine.subSequence(0, columnLine.length() - 1) + "}";
                LOGGER.info("[" + this.getName() + "] Persisting data at OrionHDFSSink. HDFS file (" + hdfsFile
                        + "), Data (" + columnLine + ")");
                
                if (fileExists) {
                    persistenceBackend.append(hdfsFile, columnLine);
                } else {
                    persistenceBackend.createDir(hdfsFolder);
                    persistenceBackend.createFile(hdfsFile, columnLine);
                    persistenceBackend.provisionHiveTable(hdfsFolder, hiveFields);
                    fileExists = true;
                } // if else
            } // if
        } // for
    } // persist

} // OrionKafkaSink
