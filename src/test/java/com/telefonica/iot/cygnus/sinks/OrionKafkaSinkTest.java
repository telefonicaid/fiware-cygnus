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
                + "dm-by-service and enable_grouping=false, the Kafka topic name "
                + "is <service>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "false", "dm-by-service"));
        String topic = sink.buildTopicName(service,servicePath,entity,attribute);
        
        try {
            expectedTopic = "service";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation "
                    + "[dm-by-service] Create topic is equals to <service>");
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation '"
                    + "[dm-by-service] Bad built topic: " + topic);
            throw e;
        } // try catch
        
    } // testTopicNamesDmByService
    
    @Test
    public void testTopicNameDmByServiceWithSlashServicePath() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When the "
                + "root service-path is notified/defaulted and "
                + "data_model=dm-by-service and enable_grouping=false, the Kafka "
                + "topic name is <service>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "false", "dm-by-service-path"));
        String topic = sink.buildTopicName(service,servicePathSlash,entity,attribute);
        
        try {
            expectedTopic = "service";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation "
                    + "[dm-by-service] Topic created is equals to <service>");
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation '"
                    + "[dm-by-service] Bad build topic: " + topic);
            throw e;
        } // try catch
        
    } // testTopicNamesDmByService
    
    @Test
    public void testTopicNameDmByServicePath() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When a non "
                + "root service-path is notified/defaulted "
                + "and data_model=dm-by-service-path and enable_grouping=false, "
                + "the Kafka topic name is <service>_<service-path>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "false", "dm-by-service-path"));
        String topic = sink.buildTopicName(service,servicePath,entity,attribute);
        
        try {
            expectedTopic = "service_servicePath";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation "
                    + "[dm-by-service-path] Created topic is equals to <service>_<servicePath>");
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation '"
                    + "[dm-by-service-path] Bad build topic: " + topic);
            throw e;
        } // try catch
        
    } // testTopicNamesDmByServicePath 
    
    @Test
    public void testTopicNameDmByServicePathWithSlashServicePath() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When the "
                + "root service-path is notified/defaulted and "
                + "data_model=dm-by-service-path and enable_grouping=false, "
                + "the Kafka topic name is <service>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "false", "dm-by-service-path"));
        String topic = sink.buildTopicName(service,servicePathSlash,entity,attribute);
        
        try {
            expectedTopic = "service";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation "
                    + "[dm-by-service-path] Created topic is equals to <service>_<servicePath>");
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation '"
                    + "[dm-by-service-path] Bad built topic: " + topic);
            throw e;
        } // try catch
        
    } // testTopicNamesDmByServicePath    
    
    @Test
    public void testTopicNameDmByEntity() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When a non "
                + "root service-path is notified/defaulted "
                + "and data_model=dm-by-entity and enable_grouping=false, the "
                + "Kafka topic name is <service>_<service-path>_<entityId>_<entityType>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "false", "dm-by-entity"));
        String topic = sink.buildTopicName(service,servicePath,entity,attribute);
        
        try {
            expectedTopic = "service_servicePath_entityId_entityType";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation "
                    + "[dm-by-entity] Create topic is equals to <service>_<servicePath>_<entityId>_<entityType>");
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation '"
                    + "[dm-by-entity] Bad built topic: " + topic);
            throw e;
        } // try catch
        
    } // testTopicNamesDmByEntity
    
    @Test
    public void testTopicNameDmByEntityWithSlashServicePath() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When a non "
                + "root service-path is notified/defaulted and data_model=dm-by-"
                + "entity and enable_grouping=false, the Kafka topic name is "
                + "<service>_<entityId>_<entityType>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "false", "dm-by-entity"));
        String topic = sink.buildTopicName(service,servicePathSlash,entity,attribute);
        
        try {
            expectedTopic = "service_entityId_entityType";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation "
                    + "[dm-by-entity] Created topid is equals to <service>_<entityId>_<entityType>");
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation '"
                    + "[dm-by-entity] Bad built topic: " + topic);
            throw e;
        } // try catch
        
    } // testTopicNamesDmByEntity
    
    @Test
    public void testTopicNameDmByAttribute() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When a non "
                + "root service-path is notified/defaulted and data_model=dm-by-"
                + "attribute and enable_grouping=false, the Kafka topic name is "
                + "<service>_<service-path>_<entityId>_<entityType>_<attrName>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "false", "dm-by-attribute"));
        String topic = sink.buildTopicName(service,servicePath,entity,attribute);
        
        try {
            expectedTopic = "service_servicePath_entityId_entityType_attributeName";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation "
                    + "[dm-by-attribute] Created topic is equals to <service>_<servicePath>_<entityId>_<entityType>_<attributeName>");
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation '"
                    + "[dm-by-attribute] Bad built topic: " + topic);
            throw e;
        } // try catch
        
    } // testTopicNamesDmByAttribute
    
    @Test
    public void testTopicNameDmByAttributeWithSlashServicePath() throws Exception {
        System.out.println("[OrionKafkaSink.buildTopicName ] -------- When a non "
                + "root service-path is notified/defaulted "
                + "and data_model=dm-by-attribute and enable_grouping=false, the "
                + "Kafka topic name is <service>_<entityId>_<entityType>_<attrName>");
        OrionKafkaSink sink = new OrionKafkaSink();
        sink.configure(createContext("false", "false", "dm-by-attribute"));
        String topic = sink.buildTopicName(service,servicePathSlash,entity,attribute);
        
        try {
            expectedTopic = "service_entityId_entityType_attributeName";
            assertEquals(expectedTopic, topic);
            System.out.println("[OrionKafkaSink.buildTopicName ] -  OK  - Succesful creation "
                    + "[dm-by-attribute] Created topic is equals to <service>_<entityId>_<entityType>_<attributeName>");
        } catch (AssertionError e) {
            System.out.println("[OrionKafkaSink.buildTopicName ] - FAIL - Failed creation '"
                    + "[dm-by-attribute] Bad built topic: " + topic);
            throw e;
        } // try catch
        
    } // testTopicNamesDmByAttribute
    
    private Context createContext(String lowerCase, String grouping, String dataModel) {
        Context context = new Context();
        context.put("enable_lowercase", lowerCase);
        context.put("enable_grouping", grouping);
        context.put("data_model", dataModel);
        return context;
    } // createContext
    
} // OrionKafkaSinkTest
