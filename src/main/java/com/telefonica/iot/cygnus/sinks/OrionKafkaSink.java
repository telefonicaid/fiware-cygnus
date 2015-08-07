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
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import org.apache.flume.Context;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 *
 * @author frb
 */
public class OrionKafkaSink extends OrionSink {
    
    /**
     * Available topic types.
     */
    public enum TopicType { TOPICBYENTITYID, TOPICBYENTITYTYPE, TOPICBYSERVICEPATH, TOPICBYSERVICE }
    
    private static final CygnusLogger LOGGER = new CygnusLogger(OrionKafkaSink.class);
    private KafkaProducer persistenceBackend;
    private TopicType topicType;
    private String brokerList;
    
    @Override
    public void configure(Context context) {
        String topicTypeStr = context.getString("topic_type", "topic-by-entity-id");
        topicType = TopicType.valueOf(topicTypeStr.replaceAll("-", "").toUpperCase());
        LOGGER.debug("[" + this.getName() + "] Reading configuration (topic_type=" + topicTypeStr + ")");
        brokerList = context.getString("broker_list", "localhost:9092");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (broker_list=" + brokerList + ")");
    } // configure
    
    @Override
    public void start() {
        try {
            // create the persistence backend
            Properties props = new Properties();
            props.put("metadata.broker.list", brokerList);
            props.put("serializer.class", "kafka.serializer.StringEncoder");
            props.put("producer.type", "sync");
            props.put("request.required.acks", "1");
            persistenceBackend = new KafkaProducer(props);
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
//        String[] destinations = eventHeaders.get(Constants.DESTINATION).split(",");
        
        // human readable version of the reception time
        String recvTime = Utils.getHumanReadable(recvTimeTs, true);

        // iterate on the contextResponses
        ArrayList contextResponses = notification.getContextResponses();
        
        for (int i = 0; i < contextResponses.size(); i++) {
            // get the i-th contextElement
            ContextElementResponse contextElementResponse = (ContextElementResponse) contextResponses.get(i);
            ContextElement contextElement = contextElementResponse.getContextElement();
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
            
            String columnLine = "{\"" + Constants.RECV_TIME + "\":\"" + recvTime + "\",";

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + this.getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                columnLine += "\"" + attrName + "\":" + attrValue + ", \"" + attrName + "_md\":" + attrMetadata
                        + ",";
            } // for
            
            // insert a new row containing full attribute list
            columnLine = columnLine.subSequence(0, columnLine.length() - 1) + "}";
            ProducerRecord record = null;
 
            switch (topicType) {
                case TOPICBYENTITYID:
                    LOGGER.info("[" + this.getName() + "] Persisting data at OrionKafkaSink. Topic ("
                            + entityId + "), Data (" + columnLine + ")");
                    record = new ProducerRecord(entityId, columnLine);
                    break;
                case TOPICBYENTITYTYPE:
                    LOGGER.info("[" + this.getName() + "] Persisting data at OrionKafkaSink. Topic ("
                            + entityType + "), Data (" + columnLine + ")");
                    record = new ProducerRecord(entityType, columnLine);
                    break;
                case TOPICBYSERVICEPATH:
                    LOGGER.info("[" + this.getName() + "] Persisting data at OrionKafkaSink. Topic ("
                            + fiwareServicePaths[i] + "), Data (" + columnLine + ")");
                    record = new ProducerRecord(fiwareServicePaths[i], columnLine);
                    break;
                case TOPICBYSERVICE:
                    LOGGER.info("[" + this.getName() + "] Persisting data at OrionKafkaSink. Topic ("
                            + fiwareService + "), Data (" + columnLine + ")");
                    record = new ProducerRecord(fiwareService, columnLine);
                    break;
                default:
                    break;
            } // switch
            
            persistenceBackend.send(record);
        } // for
    } // persist

} // OrionKafkaSink
