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
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import java.util.ArrayList;
import java.util.Date;
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
    
    private static final CygnusLogger LOGGER = new CygnusLogger(OrionKafkaSink.class);
    private KafkaProducer<String, String> persistenceBackend;
    private String brokerList;
    private String zookeeperEndpoint;
    private TopicAPI topicAPI;
    private ZkClient zookeeperClient;
    private int partitions;
    private int replicationFactor;
    
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
     * Sets the name API. It is protected since it is used by the tests.
     * @param topicAPI The name API to be set
     */
    protected void setTopicAPI(TopicAPI topicAPI) {
        this.topicAPI = topicAPI;
    } // setTopicAPI
    
    @Override
    public void configure(Context context) {
        brokerList = context.getString("broker_list", "localhost:9092");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (broker_list=" + brokerList + ")");
        zookeeperEndpoint = context.getString("zookeeper_endpoint", "localhost:2181");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (zookeeper_endpoint="
                + zookeeperEndpoint + ")");
        partitions = context.getInteger("partitions", 1);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (partitions=" + partitions + ")");
        replicationFactor = context.getInteger("replication_factor", 1);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (replication factor="
                + replicationFactor + ")");
        super.configure(context);
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
        
        // creat the name API
        topicAPI = new TopicAPI();
        
        // create the Zookeeper client
        zookeeperClient = new ZkClient(zookeeperEndpoint, 10000, 10000, ZKStringSerializer$.MODULE$);
        
        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

    @Override
    void persistOne(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        Accumulator accumulator = new Accumulator();
        accumulator.initialize(new Date().getTime());
        accumulator.accumulate(eventHeaders, notification);
        persistBatch(accumulator.getBatch());
    } // persitOne
    
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
            KafkaAggregator aggregator = new KafkaAggregator();
            aggregator.initialize(subBatch.get(0));

            for (CygnusEvent cygnusEvent : subBatch) {
                aggregator.aggregate(cygnusEvent);
            } // for
            
            // persist the aggregation
            persistAggregation(aggregator);
            batch.setPersisted(destination);
        } // for
    } // persistBatch
    
    /**
     * Class for aggregating aggregation.
     */
    private class KafkaAggregator {
        
        // string containing the data aggregation
        protected String aggregation;
        protected String service;
        protected String servicePath;
        protected String entity;
        protected String attribute;
        protected String topic;
        
        public KafkaAggregator() {
            aggregation = "";
        } // KafkaAggregator
        
        public String getAggregation() {
            return aggregation;
        } // getAggregation
        
        public String getService() {
            return service;
        } // getService
        
        public String getServicePath() {
            return servicePath;
        } // servicePath
        
        public String getTopic() {
            return topic;
        } // getTopic
        
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            service = cygnusEvent.getService();
            servicePath = cygnusEvent.getServicePath();
            entity = cygnusEvent.getEntity();
            attribute = cygnusEvent.getAttribute();
            topic = buildTopicName();
        } // initialize
        
        private String buildTopicName() throws Exception {
            String name;
            
            switch (dataModel) {
                case DMBYSERVICE:
                    name = service;
                    break;
                case DMBYSERVICEPATH:
                    name = servicePath;
                    break;
                case DMBYENTITY:
                    name = entity;
                    break;
                case DMBYATTRIBUTE:
                    name = attribute;
                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use DMBYSERVICEPATH, DMBYENTITY or DMBYATTRIBUTE");
            } // switch
            
            if (name.length() > Constants.MAX_NAME_LEN) {
                throw new CygnusBadConfiguration("Building topic name '" + name
                        + "' and its length is greater than " + Constants.MAX_NAME_LEN);
            } // if

            return name;
        } // buildTopic
        
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();

            // get the event body
            ContextElement contextElement = cygnusEvent.getContextElement();
            
            if (aggregation.isEmpty()) {
                aggregation = buildMessage(contextElement, service, servicePath, recvTimeTs);
            } else {
                aggregation += "\n" + buildMessage(contextElement, service, servicePath, recvTimeTs);
            } // if else
        } // aggregate

    } // KafkaAggregator
    
    private void persistAggregation(KafkaAggregator aggregator) throws Exception {
        String aggregation = aggregator.getAggregation();
        String destination = aggregator.getTopic();
        
        // build the message/record to be sent to Kafka
        ProducerRecord<String, String> record;
        String topicName = buildTopicName(destination);

        if (!topicAPI.topicExists(zookeeperClient, topicName)) {
            LOGGER.info("[" + this.getName() + "] Creating topic at OrionKafkaSink. "
                    + "Topic: " + topicName + " , partitions: " + partitions + " , "
                    + "replication factor: " + replicationFactor);
            topicAPI.createTopic(zookeeperClient, topicName, partitions, replicationFactor, new Properties());
        } // if

        LOGGER.info("[" + this.getName() + "] Persisting data at OrionKafkaSink. Topic ("
                + topicName + "), Data (" + aggregation + ")");
        record = new ProducerRecord<String, String>(topicName, aggregation);
        persistenceBackend.send(record);
    } // persistAggregation

    private String buildMessage(ContextElement contextElement, String fiwareService,
            String fiwareServicePath, long recvTimeTs) {
        String message = "{\"headers\":[{\"fiware-service\":\"" + fiwareService + "\"},"
                + "{\"fiware-servicePath\":\"" + fiwareServicePath + "\"},"
                + "{\"timestamp\":" + recvTimeTs + "}" + "],\"body\":";
        Gson gson = new Gson();
        String contextElementResponseStr = gson.toJson(contextElement);
        message += contextElementResponseStr + "}";
        return message;
    } // buildMessage

    private String buildTopicName(String topic) throws Exception {
        if (topic.length() > Constants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building topic " + topic + " and its length is greater "
                    + "than " + Constants.MAX_NAME_LEN);
        } // if
        
        return topic;
    } // buildTopicFromDestination
    
    /**
     * API for dealing with topics existence check and creation. It is needed since static methods from AdminUtils
     * cannot be tested with Mockito; however, this class can be mocked.
     */
    public class TopicAPI {
        
        /**
         * Returns true if the given name exists, false otherwise.
         * @param zookeeperClient
         * @param topic
         * @return True if the given name exists, false otherwise
         */
        public boolean topicExists(ZkClient zookeeperClient, String topic) {
            return AdminUtils.topicExists(zookeeperClient, topic);
        } // topicExists
        
        /**
         * Creates the given name with given properties.
         * @param zookeeperClient
         * @param topic
         * @param props
         * @param partitions
         * @param replicationFactor
         */
        public void createTopic(ZkClient zookeeperClient, String topic, int partitions, int replicationFactor, Properties props) {
            AdminUtils.createTopic(zookeeperClient, topic, partitions, replicationFactor, props);
        } // createTopic
        
    } // TopicAPI

} // OrionKafkaSink
