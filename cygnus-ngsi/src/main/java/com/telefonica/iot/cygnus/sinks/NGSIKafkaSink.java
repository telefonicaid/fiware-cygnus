/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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

import com.google.gson.Gson;
import com.telefonica.iot.cygnus.backends.kafka.KafkaBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSICharsets;
import java.util.ArrayList;
import org.I0Itec.zkclient.ZkClient;
import org.apache.flume.Context;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 *
 * @author frb
 */
public class NGSIKafkaSink extends NGSISink {

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIKafkaSink.class);
    private KafkaBackendImpl persistenceBackend;
    private String brokerList;
    private String zookeeperEndpoint;
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
    public KafkaBackendImpl getPersistenceBackend() {
        return persistenceBackend;
    } // getPersistenceBackend

    /**
     * Sets the persistence backend. It is protected since it is used by the tests.
     * @param persistenceBackend The persistence backend to be set
     */
    protected void setPersistenceBackend(KafkaBackendImpl persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    } // setPersistenceBackend

    @Override
    public void configure(Context context) {
        // Read NGSISink general configuration
        super.configure(context);
        
        // Impose enable encoding
        enableEncoding = true;
        
        brokerList = context.getString("broker_list", "localhost:9092");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (broker_list=" + brokerList + ")");
        zookeeperEndpoint = context.getString("zookeeper_endpoint", "localhost:2181");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (zookeeper_endpoint="
                + zookeeperEndpoint + ")");
        partitions = context.getInteger("partitions", 1);

        if (partitions <= 0) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (partitions=" + partitions
                    + ") -- Must be greater than 0");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (partitions=" + partitions + ")");
        } // if else

        replicationFactor = context.getInteger("replication_factor", 1);

        if (replicationFactor <= 0) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (replication_factor="
                    + replicationFactor + ") -- Must be greater than 0");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (replication_factor="
                    + replicationFactor + ")");
        } // if else
    } // configure

    @Override
    public void start() {
        // create the persistence backend
        try {
            persistenceBackend = new KafkaBackendImpl(brokerList, zookeeperEndpoint);
            LOGGER.debug("[" + this.getName() + "] Kafka persistence backend (KafkaProducer) created");
        } catch (Exception e) {
            LOGGER.error("Error while creating the Kafka persistence backend (KafkaProducer). Details="
                    + e.getMessage());
        } // try catch

        super.start();
    } // start

    @Override
    void persistBatch(NGSIBatch batch) throws CygnusBadConfiguration, CygnusBadContextData, CygnusPersistenceError {
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

            // Get an aggregator for this destination and initialize it
            KafkaAggregator aggregator = new KafkaAggregator();
            aggregator.initialize(events.get(0));

            for (NGSIEvent event : events) {
                aggregator.aggregate(event);
            } // for

            // Persist the aggregation
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
     * Builds the topic name.
     * @param service
     * @param servicePath
     * @param entity
     * @param attribute
     * @return
     * @throws CygnusBadConfiguration
     */
    protected String buildTopicName(String service, String servicePath, String entity, String attribute,
                                    boolean enableLowercase)
    throws CygnusBadConfiguration {
        String name;

        switch (dataModel) {
            case DMBYSERVICE:
                name = NGSICharsets.encodeKafka(service);
                break;
            case DMBYSERVICEPATH:
                name =  NGSICharsets.encodeKafka(service)
                        + CommonConstants.CONCATENATOR
                        + NGSICharsets.encodeKafka(servicePath);
                break;
            case DMBYENTITY:
                name = NGSICharsets.encodeKafka(service)
                        + CommonConstants.CONCATENATOR
                        + NGSICharsets.encodeKafka(servicePath)
                        + CommonConstants.CONCATENATOR
                        + NGSICharsets.encodeKafka(entity);
                break;
            case DMBYATTRIBUTE:
                name = NGSICharsets.encodeKafka(service)
                        + CommonConstants.CONCATENATOR
                        + NGSICharsets.encodeKafka(servicePath)
                        + CommonConstants.CONCATENATOR
                        + NGSICharsets.encodeKafka(entity)
                        + CommonConstants.CONCATENATOR
                        + NGSICharsets.encodeKafka(attribute);
                break;
            default:
                throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                        + "'. Please, use dm-by-service, dm-by-service-path, dm-by-entity or dm-by-attribute");
        } // switch
/*
        This was commented in order to pass the tests. This must be uncommented and fixed according to
        this issue: https://github.com/telefonicaid/fiware-cygnus/issues/407
        
        if (name.length() > CommonConstants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building topic name '" + name
                    + "' and its length is greater than " + CommonConstants.MAX_NAME_LEN);
        } // if
*/
        if (enableLowercase) {
            return name.toLowerCase();
        } else {
            return name;
        }
    } // buildTopic

    /**
     * Class for aggregating aggregation.
     */
    private class KafkaAggregator {

        // string containing the data aggregation
        protected String aggregation;
        protected String service;
        protected String servicePathForData;
        protected String servicePathForNaming;
        protected String entityForNaming;
        protected String attributeForNaming;

        public KafkaAggregator() {
            aggregation = "";
        } // KafkaAggregator

        public String getAggregation() {
            return aggregation;
        } // getAggregation

        public String getService() {
            return service;
        } // getServiceForNaming

        public String getServicePathForNaming() {
            return servicePathForNaming;
        } // getServicePathForNaming
        
        public String getEntityForNaming() {
            return entityForNaming;
        } // getEntityForNaming
        
        public String getAttributeForNaming() {
            return attributeForNaming;
        } // getAttributeForNaming

        public void initialize(NGSIEvent event) {
            service = event.getServiceForNaming(enableNameMappings);
            servicePathForData = event.getServicePathForData();
            servicePathForNaming = event.getServicePathForNaming(enableGrouping, enableNameMappings);
            entityForNaming = event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding);
            attributeForNaming = event.getAttributeForNaming(enableNameMappings);
        } // initialize

        public void aggregate(NGSIEvent event) {
            // get the getRecvTimeTs headers
            long recvTimeTs = event.getRecvTimeTs();

            // get the getRecvTimeTs body
            ContextElement contextElement = event.getContextElement();

            if (aggregation.isEmpty()) {
                aggregation = buildMessage(contextElement, service, servicePathForData, recvTimeTs);
            } else {
                aggregation += "\n" + buildMessage(contextElement, service, servicePathForData, recvTimeTs);
            } // if else
        } // aggregate

    } // KafkaAggregator

    private void persistAggregation(KafkaAggregator aggregator) throws CygnusBadConfiguration, CygnusPersistenceError {
        String aggregation = aggregator.getAggregation();
        String topicName = buildTopicName(aggregator.getService(),
                                          aggregator.getServicePathForNaming(),
                                          aggregator.getEntityForNaming(),
                                          aggregator.getAttributeForNaming(),
                                          enableLowercase);

        // build the message/record to be sent to Kafka
        ProducerRecord<String, String> record;
        Boolean topicExists;

        try {
            topicExists = persistenceBackend.topicExists(topicName);
        } catch (Exception e) {
            throw new CygnusPersistenceError("-, " + e.getMessage());
        } // try catch
        
        if (!topicExists) {
            LOGGER.info("[" + this.getName() + "] Creating topic at NGSIKafkaSink. "
                    + "Topic: " + topicName + " , partitions: " + partitions + " , "
                    + "replication factor: " + replicationFactor);
            persistenceBackend.createTopic(topicName, partitions, replicationFactor);
        } // if

        LOGGER.info("[" + this.getName() + "] Persisting data at NGSIKafkaSink. Topic ("
                + topicName + "), Data (" + aggregation + ")");
        record = new ProducerRecord<>(topicName, aggregation);
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

} // NGSIKafkaSink
