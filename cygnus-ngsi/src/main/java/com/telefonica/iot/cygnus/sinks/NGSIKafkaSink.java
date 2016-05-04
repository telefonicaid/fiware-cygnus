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

import com.google.gson.Gson;
import com.telefonica.iot.cygnus.backends.kafka.KafkaBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
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

        super.configure(context);
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
    void persistBatch(NGSIBatch batch) throws Exception {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if

        // iterate on the destinations, for each one a single create / append will be performed
        for (String destination : batch.getDestinations()) {
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the " + destination
                    + " destination");

            // get the sub-batch for this destination
            ArrayList<NGSIEvent> subBatch = batch.getEvents(destination);

            // get an aggregator for this destination and initialize it
            KafkaAggregator aggregator = new KafkaAggregator();
            aggregator.initialize(subBatch.get(0));

            for (NGSIEvent cygnusEvent : subBatch) {
                aggregator.aggregate(cygnusEvent);
            } // for

            // persist the aggregation
            persistAggregation(aggregator);
            batch.setPersisted(destination);
        } // for
    } // persistBatch
    
    protected String buildTopicName(String service, String servicePath,
        String entity, String attribute) throws Exception {
        String name;

        switch (dataModel) {
            case DMBYSERVICE:
                name = NGSIUtils.encode(service, false, true);
                break;
            case DMBYSERVICEPATH:
                if (servicePath.equals("/")) {
                    name = NGSIUtils.encode(service, false, true);
                    break;
                } else if (servicePath.startsWith("/")) {
                    name =  NGSIUtils.encode(service, false, true) + "_"
                            + NGSIUtils.encode(servicePath, true, false);
                    break;
                } else {
                    // Impossible to reach this case
                    throw new CygnusBadConfiguration("Service path must be"
                            + "'/' or must start with '/'");
                } // if else if

            case DMBYENTITY:
                if (servicePath.equals("/")) {
                    name = NGSIUtils.encode(service, false, true) + "_"
                            + NGSIUtils.encode(entity, false, true);
                    break;
                } else if (servicePath.startsWith("/")) {
                    name = NGSIUtils.encode(service, false, true) + "_"
                            + NGSIUtils.encode(servicePath, true, false) + "_"
                            + NGSIUtils.encode(entity, false, true);
                    break;
                } else {
                    // Impossible to reach this case
                    throw new CygnusBadConfiguration("Service path must be"
                            + "'/' or must start with '/'");
                } // if else if

            case DMBYATTRIBUTE:
                if (servicePath.equals("/")) {
                    name = NGSIUtils.encode(service, false, true) + "_"
                            + NGSIUtils.encode(entity, false, true) + "_"
                            + NGSIUtils.encode(attribute, false, true);
                    break;
                } else if (servicePath.startsWith("/")) {
                    name = NGSIUtils.encode(service, false, true) + "_"
                            + NGSIUtils.encode(servicePath, true, false) + "_"
                            + NGSIUtils.encode(entity, false, true) + "_"
                            + NGSIUtils.encode(attribute, false, true);
                    break;
                } else {
                    // Impossible to reach this case
                    throw new CygnusBadConfiguration("Service path must be"
                            + "'/' or must start with '/'");
                } // if else if

            default:
                throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                        + "'. Please, use DMBYSERVICEPATH, DMBYENTITY or DMBYATTRIBUTE");
        } // switch

        if (name.length() > CommonConstants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building topic name '" + name
                    + "' and its length is greater than " + CommonConstants.MAX_NAME_LEN);
        } // if

        return name;
    } // buildTopic

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
        } // getServicePath
        
        public String getEntity() {
            return entity;
        } // getEntity
        
        public String getAttribute() {
            return attribute;
        } // getAttribute

        public void initialize(NGSIEvent cygnusEvent) throws Exception {
            service = cygnusEvent.getService();
            servicePath = cygnusEvent.getServicePath();
            entity = cygnusEvent.getEntity();
            attribute = cygnusEvent.getAttribute();
        } // initialize

        public void aggregate(NGSIEvent cygnusEvent) throws Exception {
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
        String topicName = buildTopicName(aggregator.getService(),
                aggregator.getServicePath(), aggregator.getEntity(),
                aggregator.getAttribute()).toLowerCase();

        // build the message/record to be sent to Kafka
        ProducerRecord<String, String> record;

        if (!persistenceBackend.topicExists(topicName)) {
            LOGGER.info("[" + this.getName() + "] Creating topic at OrionKafkaSink. "
                    + "Topic: " + topicName + " , partitions: " + partitions + " , "
                    + "replication factor: " + replicationFactor);
            persistenceBackend.createTopic(topicName, partitions, replicationFactor);
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

} // NGSIKafkaSink
