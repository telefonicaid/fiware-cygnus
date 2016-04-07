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

import org.apache.flume.Context;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class OrionKafkaSinkTest {   
    
    String service = "service";
    String serviceUpper = "SERVICE";
    String servicePath = "/servicePath";
    String servicePathUpper = "/SERVICEPATH";
    String servicePathSlash = "/";
    String entity = "entityId_entityType";
    String attribute = "attributeName";
    String expectedTopic;
    
    @Test
    public void testTopicNameDmByService() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When a non "
                + "root service-path is notified/defaulted and data_model="
                + "dm-by-service, the Kafka topic name "
                + "is <service>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-service"));
        String topic = sink.buildTopicName(service,servicePath,entity,attribute);
        
        try {
            expectedTopic = "service";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation - "
                    + "Created topic is equal to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic + "' was created instead.");
            throw e;
        } // try catch
        
    } // testTopicNameDmByService
    
    @Test
    public void testTopicNameDmByServiceWithSlashServicePath() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When the "
                + "root service-path is notified/defaulted and "
                + "data_model=dm-by-service, the Kafka "
                + "topic name is <service>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-service-path"));
        String topic = sink.buildTopicName(service,servicePathSlash,entity,attribute);
        
        try {
            expectedTopic = "service";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic + "' was created instead.");
            throw e;
        } // try catch
        
    } // testTopicNameDmByServiceWithSlashServicePath
    
    @Test
    public void testTopicNameDmByServicePath() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When a non "
                + "root service-path is notified/defaulted "
                + "and data_model=dm-by-service-path, "
                + "the Kafka topic name is <service>_<service-path>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-service-path"));
        String topic = sink.buildTopicName(service,servicePath,entity,attribute);
        
        try {
            expectedTopic = "service_servicePath";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic + "' was created instead.");
            throw e;
        } // try catch
        
    } // testTopicNameDmByServicePath 
    
    @Test
    public void testTopicNameDmByServicePathWithSlashServicePath() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When the "
                + "root service-path is notified/defaulted and "
                + "data_model=dm-by-service-path, "
                + "the Kafka topic name is <service>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-service-path"));
        String topic = sink.buildTopicName(service,servicePathSlash,entity,attribute);
        
        try {
            expectedTopic = "service";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic + "' was created instead.");
            throw e;
        } // try catch
        
    } // testTopicNameDmByServicePathWithSlashServicePath    
    
    @Test
    public void testTopicNameDmByEntity() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When a non "
                + "root service-path is notified/defaulted "
                + "and data_model=dm-by-entity, the "
                + "Kafka topic name is <service>_<service-path>_<entityId>_<entityType>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-entity"));
        String topic = sink.buildTopicName(service,servicePath,entity,attribute);
        
        try {
            expectedTopic = "service_servicePath_entityId_entityType";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic + "' was created instead.");
            throw e;
        } // try catch
        
    } // testTopicNameDmByEntity
    
    @Test
    public void testTopicNameDmByEntityWithSlashServicePath() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When the "
                + "root service-path is notified/defaulted and data_model=dm-by-"
                + "entity, the Kafka topic name is "
                + "<service>_<entityId>_<entityType>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-entity"));
        String topic = sink.buildTopicName(service,servicePathSlash,entity,attribute);
        
        try {
            expectedTopic = "service_entityId_entityType";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic + "' was created instead.");
            throw e;
        } // try catch
        
    } // testTopicNameDmByEntityWithSlashServicePath
    
    @Test
    public void testTopicNameDmByAttribute() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When a non "
                + "root service-path is notified/defaulted and data_model=dm-by-"
                + "attribute, the Kafka topic name is "
                + "<service>_<service-path>_<entityId>_<entityType>_<attrName>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-attribute"));
        String topic = sink.buildTopicName(service,servicePath,entity,attribute);
        
        try {
            expectedTopic = "service_servicePath_entityId_entityType_attributeName";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic + "' was created instead.");
            throw e;
        } // try catch
        
    } // testTopicNameDmByAttribute
    
    @Test
    public void testTopicNameDmByAttributeWithSlashServicePath() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When the "
                + "root service-path is notified/defaulted "
                + "and data_model=dm-by-attribute, the "
                + "Kafka topic name is <service>_<entityId>_<entityType>_<attrName>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "dm-by-attribute"));
        String topic = sink.buildTopicName(service,servicePathSlash,entity,attribute);
        
        try {
            expectedTopic = "service_entityId_entityType_attributeName";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation  - "
                    + "Created topic is equals to " + expectedTopic);
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation - "
                    + "Wrong topic built, expected: '" + expectedTopic + "' but '" + topic + "' was created instead.");
            throw e;
        } // try catch
        
    } // testTopicNameDmByAttributeWithSlashServicePath
    
    private Context createContext(String lowerCase, String dataModel) {
        Context context = new Context();
        context.put("enable_lowercase", lowerCase);
        context.put("data_model", dataModel);
        return context;
    } // createContext
    
} // OrionKafkaSinkTest
