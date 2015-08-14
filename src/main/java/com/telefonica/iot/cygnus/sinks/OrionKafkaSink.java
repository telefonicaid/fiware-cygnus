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

import com.google.gson.Gson;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.apache.flume.Context;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 *
 * @author frb
 */
public class OrionKafkaSink extends OrionSink {
    
    /**
     * Available topic types.
     */
    public enum TopicType { TOPICBYDESTINATION, TOPICBYSERVICEPATH, TOPICBYSERVICE }
    
    private static final CygnusLogger LOGGER = new CygnusLogger(OrionKafkaSink.class);
    private KafkaProducer<String, String> persistenceBackend;
    private TopicType topicType;
    private String brokerList;
    private String zookeeperEndpoint;
    private TopicAPI topicAPI;
    private ZkClient zookeeperClient;
    
    /**
     * Gets the topic type.
     * @return The topic type
     */
    public TopicType getTopicType() {
        return topicType;
    } // getTopicType
    
    /**
     * Gets the broker list.
     * @return The broker list
     */
    public String getBrokerList() {
        return brokerList;
    } // getBrokerList
    
    /**
     * Gets the Zookeeper endpoint.
     * @return The Zookeeper endpoint
     */
    public String getZookeeperEndpoint() {
        return zookeeperEndpoint;
    } // getZookeeperEndpoint
    
    /**
     * Gets the persistence backend.
     * @return The persistence backend
     */
    public KafkaProducer getPersistenceBackend() {
        return persistenceBackend;
    } // getPersistenceBackend
    
    /**
     * Sets the persistence backend. It is protected since it is used by the tests.
     * @param persistenceBackend The persistence backend to be set
     */
    protected void setPersistenceBackend(KafkaProducer persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    } // setPersistenceBackend
    
    /**
     * Sets the topic API. It is protected since it is used by the tests.
     * @param topicAPI The topic API to be set
     */
    protected void setTopicAPI(TopicAPI topicAPI) {
        this.topicAPI = topicAPI;
    } // setTopicAPI
    
    @Override
    public void configure(Context context) {
        String topicTypeStr = context.getString("topic_type", "topic-by-destination");
        topicType = TopicType.valueOf(topicTypeStr.replaceAll("-", "").toUpperCase());
        LOGGER.debug("[" + this.getName() + "] Reading configuration (topic_type=" + topicTypeStr + ")");
        brokerList = context.getString("broker_list", "localhost:9092");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (broker_list=" + brokerList + ")");
        zookeeperEndpoint = context.getString("zookeeper_endpoint", "localhost:2181");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (zookeeper_endpoint="
                + zookeeperEndpoint + ")");
    } // configure
    
    @Override
    public void start() {
        // create the persistence backend
        try {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            persistenceBackend = new KafkaProducer<String, String>(props);
            LOGGER.debug("[" + this.getName() + "] Kafka persistence backend (KafkaProducer) created");
        } catch (Exception e) {
            LOGGER.error("Error while creating the Kafka persistence backend (KafkaProducer). Details="
                    + e.getMessage());
        } // try catch // try catch
        
        // creat the topic API
        topicAPI = new TopicAPI();
        
        // create the Zookeeper client
        zookeeperClient = new ZkClient(zookeeperEndpoint, 10000, 10000, ZKStringSerializer$.MODULE$);
        
        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

    @Override
    void persist(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        // get some header values
        String fiwareService = eventHeaders.get(Constants.HEADER_SERVICE);
        String[] fiwareServicePaths = eventHeaders.get(Constants.HEADER_SERVICE_PATH).split(",");
        String[] destinations = eventHeaders.get(Constants.DESTINATION).split(",");

        // iterate on the contextResponses
        ArrayList contextResponses = notification.getContextResponses();
        
        for (int i = 0; i < contextResponses.size(); i++) {
            // get the i-th contextElement
            ContextElementResponse contextElementResponse = (ContextElementResponse) contextResponses.get(i);
            Gson gson = new Gson();
            String contextElementResponseStr = gson.toJson(contextElementResponse);
            ProducerRecord<String, String> record;
            
            switch (topicType) {
                case TOPICBYDESTINATION:
                    if (!topicAPI.topicExists(zookeeperClient, destinations[i])) {
                        LOGGER.info("[" + this.getName() + "] Creating topic " + destinations[i]
                                + " at OrionKafkaSink");
                        topicAPI.createTopic(zookeeperClient, destinations[i], new Properties());
                    } // if
                    
                    LOGGER.info("[" + this.getName() + "] Persisting data at OrionKafkaSink. Topic ("
                            + destinations[i] + "), Data (" + contextElementResponseStr + ")");
                    record = new ProducerRecord<String, String>(destinations[i], contextElementResponseStr);
                    break;
                case TOPICBYSERVICEPATH:
                    if (!topicAPI.topicExists(zookeeperClient, fiwareServicePaths[i])) {
                        LOGGER.info("[" + this.getName() + "] Creating topic " + fiwareServicePaths[i]
                                + " at OrionKafkaSink");
                        topicAPI.createTopic(zookeeperClient, fiwareServicePaths[i], new Properties());
                    } // if
                    
                    LOGGER.info("[" + this.getName() + "] Persisting data at OrionKafkaSink. Topic ("
                            + fiwareServicePaths[i] + "), Data (" + contextElementResponseStr + ")");
                    record = new ProducerRecord<String, String>(fiwareServicePaths[i], contextElementResponseStr);
                    break;
                case TOPICBYSERVICE:
                    if (!topicAPI.topicExists(zookeeperClient, fiwareService)) {
                        LOGGER.info("[" + this.getName() + "] Creating topic " + fiwareService
                                + " at OrionKafkaSink");
                        topicAPI.createTopic(zookeeperClient, fiwareService, new Properties());
                    } // if
                    
                    LOGGER.info("[" + this.getName() + "] Persisting data at OrionKafkaSink. Topic ("
                            + fiwareService + "), Data (" + contextElementResponseStr + ")");
                    record = new ProducerRecord<String, String>(fiwareService, contextElementResponseStr);
                    break;
                default:
                    record = null;
                    break;
            } // switch
            
            if (record != null) {
                persistenceBackend.send(record);
            } // if
        } // for
    } // persist
    
    /**
     * API for dealing with topics existence check and creation. It is needed since static methods from AdminUtils
     * cannot be tested with Mockito; however, this class can be mocked.
     */
    public class TopicAPI {
        
        public boolean topicExists(ZkClient zookeeperClient, String topic) {
            return AdminUtils.topicExists(zookeeperClient, topic);
        } // topicExists
        
        public void createTopic(ZkClient zookeeperClient, String topic, Properties props) {
            AdminUtils.createTopic(zookeeperClient, topic, 1, 1, props);
        } // createTopic
        
    } // TopicAPI

} // OrionKafkaSink
