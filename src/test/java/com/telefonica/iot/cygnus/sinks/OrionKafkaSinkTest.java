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

import static com.telefonica.iot.cygnus.utils.TestUtils.getTestTraceHead;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author pcoello25
 */
public class OrionKafkaSinkTest {
    
    private final String service = "service";
    private final String servicePath = "/servicePath";
    private final String servicePathSlash = "/";
    private final String entity = "entityId_entityType";
    private final String attribute = "attributeName";
    private String expectedTopic;
    
    /**
     * Constructor.
     */
    public OrionKafkaSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // OrionKafkaSinkTest
    
    /**
     * 
     * @throws Exception
     */
    @Test
    public void testbuildTopicNameDmByService() throws Exception {
        System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-------- When a non "
                + "root service-path is notified/defaulted and data_model=dm-by-service, the Kafka topic "
                + "name is <service>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-service"));
        String topic = sink.buildTopicName(service, servicePath, entity, attribute);
        
        try {
            expectedTopic = "service";
            assertEquals(expectedTopic, topic);
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-  OK  - "
                    + "Created topic is equal to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "- FAIL - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic
                    + "' was created instead.");
            throw e;
        } // try catch
    } // testBuildTopicNameDmByService
    
    /**
     * 
     * @throws Exception
     */
    @Test
    public void testBuildTopicNameDmByServiceWithSlashServicePath() throws Exception {
        System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-------- When the "
                + "root service-path is notified/defaulted and data_model=dm-by-service, the Kafka "
                + "topic name is <service>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-service-path"));
        String topic = sink.buildTopicName(service, servicePathSlash, entity, attribute);
        
        try {
            expectedTopic = "service";
            assertEquals(expectedTopic, topic);
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-  OK  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "- FAIL - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic
                    + "' was created instead.");
            throw e;
        } // try catch
    } // testBuildTopicNameDmByServiceWithSlashServicePath
    
    /**
     * 
     * @throws Exception
     */
    @Test
    public void testBuildTopicNameDmByServicePath() throws Exception {
        System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-------- When a non "
                + "root service-path is notified/defaulted and data_model=dm-by-service-path, "
                + "the Kafka topic name is <service>_<service-path>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-service-path"));
        String topic = sink.buildTopicName(service, servicePath, entity, attribute);
        
        try {
            expectedTopic = "service_servicePath";
            assertEquals(expectedTopic, topic);
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-  OK  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "- FAIL - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic
                    + "' was created instead.");
            throw e;
        } // try catch
    } // testBuildTopicNameDmByServicePath
    
    /**
     * 
     * @throws Exception
     */
    @Test
    public void testBuildTopicNameDmByServicePathWithSlashServicePath() throws Exception {
        System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-------- When the "
                + "root service-path is notified/defaulted and data_model=dm-by-service-path, "
                + "the Kafka topic name is <service>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-service-path"));
        String topic = sink.buildTopicName(service, servicePathSlash, entity, attribute);
        
        try {
            expectedTopic = "service";
            assertEquals(expectedTopic, topic);
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-  OK  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "- FAIL - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic
                    + "' was created instead.");
            throw e;
        } // try catch
    } // testBuildTopicNameDmByServicePathWithSlashServicePath
    
    /**
     * 
     * @throws Exception
     */
    @Test
    public void testBuildTopicNameDmByEntity() throws Exception {
        System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-------- When a non "
                + "root service-path is notified/defaulted and data_model=dm-by-entity, the "
                + "Kafka topic name is <service>_<service-path>_<entityId>_<entityType>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-entity"));
        String topic = sink.buildTopicName(service, servicePath, entity, attribute);
        
        try {
            expectedTopic = "service_servicePath_entityId_entityType";
            assertEquals(expectedTopic, topic);
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-  OK  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]")  + "- FAIL - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic
                    + "' was created instead.");
            throw e;
        } // try catch
    } // testBuildTopicNameDmByEntity
    
    /**
     * 
     * @throws Exception
     */
    @Test
    public void testBuildTopicNameDmByEntityWithSlashServicePath() throws Exception {
        System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-------- When the "
                + "root service-path is notified/defaulted and data_model=dm-by-entity, the Kafka topic "
                + "name is <service>_<entityId>_<entityType>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-entity"));
        String topic = sink.buildTopicName(service, servicePathSlash, entity, attribute);
        
        try {
            expectedTopic = "service_entityId_entityType";
            assertEquals(expectedTopic, topic);
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-  OK  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "- FAIL - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic
                    + "' was created instead.");
            throw e;
        } // try catch
    } // testBuildTopicNameDmByEntityWithSlashServicePath
    
    /**
     * 
     * @throws Exception
     */
    @Test
    public void testBuildTopicNameDmByAttribute() throws Exception {
        System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-------- When a non "
                + "root service-path is notified/defaulted and data_model=dm-by-attribute, the Kafka "
                + "topic name is <service>_<service-path>_<entityId>_<entityType>_<attrName>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-attribute"));
        String topic = sink.buildTopicName(service, servicePath, entity, attribute);
        
        try {
            expectedTopic = "service_servicePath_entityId_entityType_attributeName";
            assertEquals(expectedTopic, topic);
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-  OK  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "- FAIL - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic + "' was created instead.");
            throw e;
        } // try catch
    } // testBuildTopicNameDmByAttribute
    
    /**
     * 
     * @throws Exception
     */
    @Test
    public void testBuildTopicNameDmByAttributeWithSlashServicePath() throws Exception {
        System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-------- When the "
                + "root service-path is notified/defaulted and data_model=dm-by-attribute, the "
                + "Kafka topic name is <service>_<entityId>_<entityType>_<attrName>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-attribute"));
        String topic = sink.buildTopicName(service, servicePathSlash, entity, attribute);
        
        try {
            expectedTopic = "service_entityId_entityType_attributeName";
            assertEquals(expectedTopic, topic);
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "-  OK  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionKafkaSink.buildTopicName]") + "- FAIL - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic + "' was created instead.");
            throw e;
        } // try catch
    } // testBuildTopicNameDmByAttributeWithSlashServicePath
    
    private Context createContext(String lowerCase, String dataModel) {
        Context context = new Context();
        context.put("enable_lowercase", lowerCase);
        context.put("data_model", dataModel);
        return context;
    } // createContext
    
} // OrionKafkaSinkTest
