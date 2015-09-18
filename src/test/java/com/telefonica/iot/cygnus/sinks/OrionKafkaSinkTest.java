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
import com.telefonica.iot.cygnus.sinks.OrionKafkaSink.TopicAPI;
import com.telefonica.iot.cygnus.sinks.OrionKafkaSink.TopicType;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.TestUtils;
import java.util.HashMap;
import org.apache.curator.test.TestingServer;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.lifecycle.LifecycleState;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class OrionKafkaSinkTest {
    
    // mocks
    @Mock
    private KafkaProducer mockKafkaBackend;
    @Mock
    private TopicAPI mockTopicAPI;
    
    // not a mock, but a testing purpose Zookepper server by Curator
    private TestingServer zkServer;
    
    // instance to be tested
    private OrionKafkaSink sink;
    
    // other instances
    private Context context;
    private NotifyContextRequest notifyContextRequest;
    
    // context constants
    private final String topicType = "topic-by-destination";
    private final int brokerPort = 9092;
    private final String brokerList = "localhost:" + brokerPort;
    private final int zookeeperPort = 2181;
    private final String zookeeperEndpoint = "localhost:" + zookeeperPort;
    
    // header contants
    private final String timestamp = "123456789";
    private final String service = "vehicles";
    private final String servicePath = "4wheels";
    private final String destination = "car1_car";
    
    // notification constants
    private final String notification = ""
            + "{\n"
            + "    \"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\",\n"
            + "    \"originator\" : \"localhost\",\n"
            + "    \"contextResponses\" : [\n"
            + "        {\n"
            + "            \"contextElement\" : {\n"
            + "                \"attributes\" : [\n"
            + "                    {\n"
            + "                        \"name\" : \"speed\",\n"
            + "                        \"type\" : \"float\",\n"
            + "                        \"value\" : \"112.9\"\n"
            + "                    }\n"
            + "                ],\n"
            + "                \"type\" : \"car\",\n"
            + "                \"isPattern\" : \"false\",\n"
            + "                \"id\" : \"car1\"\n"
            + "            },\n"
            + "            \"statusCode\" : {\n"
            + "                \"code\" : \"200\",\n"
            + "                \"reasonPhrase\" : \"OK\"\n"
            + "            }\n"
            + "        }\n"
            + "    ]\n"
            + "}";
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        sink = new OrionKafkaSink();
        sink.setPersistenceBackend(mockKafkaBackend);
        sink.setTopicAPI(mockTopicAPI);
        
        // set up other instances
        context = new Context();
        context.put("topic_type", topicType);
        context.put("broker_list", brokerList);
        context.put("zookeeper_endpoint", zookeeperEndpoint);
        notifyContextRequest = TestUtils.createJsonNotifyContextRequest(notification);

        // set up the behaviour of the mocked classes
        when(mockKafkaBackend.send(null)).thenReturn(null, null, null);
        when(mockTopicAPI.topicExists(null, null)).thenReturn(false, false, false);
        doNothing().doNothing().doNothing().when(mockTopicAPI).createTopic(null, null, null);
        
        // setup the testing purpose Zookeeper server
        zkServer = new TestingServer(zookeeperPort);
    } // setUp
    
    /**
     * Shutdowns all necessary testing classes.
     * @throws IOException
     */
    @After
    public void shutdown() throws Exception {
        zkServer.stop();
    } // shutdown

    /**
     * Test of configure method, of class OrionKafkaSink.
     */
    @Test
    public void testConfigure() {
        System.out.println("Testing OrionKafkaSink.configure");
        sink.configure(context);
        assertEquals(TopicType.valueOf(topicType.replaceAll("-", "").toUpperCase()), sink.getTopicType());
        assertEquals(brokerList, sink.getBrokerList());
        assertEquals(zookeeperEndpoint, sink.getZookeeperEndpoint());
    } // testConfigure
    
    /**
     * Test of start method, of class OrionKafkaSink.
     */
    @Test
    public void testStart() {
        System.out.println("Testing OrionKafkaSink.start");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        sink.start();
        assertTrue(sink.getPersistenceBackend() != null);
        assertEquals(LifecycleState.START, sink.getLifecycleState());
    } // testStart
    
    /**
     * Test of persist method, of class OrionKafkaSink. Topic types are tested.
     */
    @Test
    public void testPersistTopicTypes() {
        System.out.println("Testing OrionKafkaSink.persist (topic-per-service)");
        context.put("topic_type", "topic-by-service");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_SERVICE, service);
        headers.put(Constants.HEADER_SERVICE_PATH, servicePath);
        headers.put(Constants.DESTINATION, destination);
        
        try {
            sink.persist(headers, notifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionKafkaSink.persist (topic-per-service-path)");
        context.put("topic_type", "topic-by-service-path");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_SERVICE, service);
        headers.put(Constants.HEADER_SERVICE_PATH, servicePath);
        headers.put(Constants.DESTINATION, destination);
        
        try {
            sink.persist(headers, notifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionKafkaSink.persist (topic-per-destination)");
        context.put("topic_type", "topic-by-destination");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_SERVICE, service);
        headers.put(Constants.HEADER_SERVICE_PATH, servicePath);
        headers.put(Constants.DESTINATION, destination);
        
        try {
            sink.persist(headers, notifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersistTopicTypes
    
} // OrionKafkaSinkTest
