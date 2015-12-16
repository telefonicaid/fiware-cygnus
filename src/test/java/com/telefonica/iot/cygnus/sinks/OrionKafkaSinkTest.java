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
import com.telefonica.iot.cygnus.utils.TestUtils;
import java.util.ArrayList;
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
    
    // other inmutable instances
    private NotifyContextRequest singleNotifyContextRequest;
    private NotifyContextRequest multipleNotifyContextRequest;
    
    // context constants
    private final int brokerPort = 9092;
    private final String brokerList = "localhost:" + brokerPort;
    private final int zookeeperPort = 2181;
    private final String zookeeperEndpoint = "localhost:" + zookeeperPort;
    
    // batches constants
    private final Long recvTimeTs = 123456789L;
    private final String normalService = "vehicles";
    private final String abnormalService =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongservname";
    private final String normalDefaultServicePath = "4wheels";
    private final String rootServicePath = "";
    private final String abnormalDefaultServicePath =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongservpathname";
    private final String normalGroupedServicePath = "cars";
    private final String abnormalGroupedServicePath =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongservpathname";
    private final String normalDefaultDestination = "car1_car";
    private final String abnormalDefaultDestination =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongdestname";
    private final String normalGroupedDestination = "my_cars";
    private final String abnormalGroupedDestination =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongdestname";
    
    // notification constants
    private final String singleContextElementNotification = ""
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
    private final String multipleContextElementNotification = ""
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
            + "        },\n"
            + "        {\n"
            + "            \"contextElement\" : {\n"
            + "                \"attributes\" : [\n"
            + "                    {\n"
            + "                        \"name\" : \"speed\",\n"
            + "                        \"type\" : \"float\",\n"
            + "                        \"value\" : \"115.8\"\n"
            + "                    }\n"
            + "                ],\n"
            + "                \"type\" : \"car\",\n"
            + "                \"isPattern\" : \"false\",\n"
            + "                \"id\" : \"car2\"\n"
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
        
        // set up other immutable instances
        singleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(singleContextElementNotification);
        multipleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(multipleContextElementNotification);

        // set up the behaviour of the mocked classes
        when(mockKafkaBackend.send(null)).thenReturn(null, null, null);
        when(mockTopicAPI.topicExists(null, null)).thenReturn(false, false, false);
        doNothing().doNothing().doNothing().when(mockTopicAPI).createTopic(null, null, null);
        
        // setup the testing purpose Zookeeper server
        zkServer = new TestingServer(zookeeperPort);
    } // setUp
    
    /**
     * Shutdowns all necessary testing classes.
     * @throws java.lang.Exception
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
        String topicType = "topic-by-destination";
        Context context = createContext(topicType);
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
        String topicType = "topic-by-destination";
        Context context = createContext(topicType);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        sink.start();
        assertTrue(sink.getPersistenceBackend() != null);
        assertEquals(LifecycleState.START, sink.getLifecycleState());
    } // testStart
    
    /**
     * Test of persistBatch method, of class OrionKafkaSink. Null batches are tested.
     */
    @Test
    public void testPersistNullBatches() {
        System.out.println("Testing OrionKafkaSink.persistBatch (null batches)");
        String topicType = "topic-by-destination";
        Context context = createContext(topicType);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(null);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersistNullBatches
    
    /**
     * Test of persistOne method, of class OrionKafkaSink. Topic types are tested.
     */
    @Test
    public void testPersistTopicTypes() {
        // common objects
        Batch groupedBatch = createBatch(recvTimeTs, normalService, normalGroupedServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        System.out.println("Testing OrionKafkaSink.persist (topic-by-service)");
        String topicType = "topic-by-service";
        Context context = createContext(topicType);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionKafkaSink.persist (topic-by-service-path)");
        topicType = "topic-by-servicePath";
        context = createContext(topicType);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionKafkaSink.persist (topic-by-destination)");
        topicType = "topic-by-destination";
        context = createContext(topicType);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersistTopicTypes
    
    /**
     * Test of persistBatch method, of class OrionKafkaSink. Special resources length is tested.
     * @throws java.lang.Exception
     */
    @Test
    public void testPersistResourceLengths() throws Exception {
        System.out.println("Testing OrionKafkaSink.persistBatch (normal resource lengths)");
        String topicType = "topic-by-destination";
        Context context = createContext(topicType);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        Batch groupedBatch = createBatch(recvTimeTs, normalService, normalGroupedServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionKafkaSink.persistBatch (too long service name)");
        topicType = "topic-by-service";
        context = createContext(topicType);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        groupedBatch = createBatch(recvTimeTs, abnormalService, normalGroupedServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(groupedBatch);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionKafkaSink.persistBatch (too long servicePath name)");
        topicType = "topic-by-service-path";
        context = createContext(topicType);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        groupedBatch = createBatch(recvTimeTs, normalService, abnormalGroupedServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(groupedBatch);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionKAfkaSink.persistBatch (too long destination name)");
        topicType = "topic-by-destination";
        context = createContext(topicType);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        groupedBatch = createBatch(recvTimeTs, normalService, normalGroupedServicePath, abnormalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(groupedBatch);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
    } // testPersistResourceLengths
    
    /**
     * Test of persistBatch method, of class OrionKafkaSink. Special service and service-path are tested.
     * @throws java.lang.Exception
     */
    @Test
    public void testPersistServiceServicePath() throws Exception {
        // common objects
        String topicType = "topic-by-destination";
        Context context = createContext(topicType);
        
        System.out.println("Testing OrionKafkaSink.persistBatch (\"root\" servicePath name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        Batch groupedBatch = createBatch(recvTimeTs, normalService, rootServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionKafkaSink.persistBatch (multiple destinations and "
                + "fiware-servicePaths)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        groupedBatch = createBatch(recvTimeTs, normalService, normalGroupedServicePath, normalGroupedDestination,
                multipleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersistServiceServicePath
    
    private Batch createBatch(long recvTimeTs, String service, String servicePath, String destination,
            NotifyContextRequest.ContextElement contextElement) {
        CygnusEvent groupedEvent = new CygnusEvent(recvTimeTs, service, servicePath, destination,
            contextElement);
        ArrayList<CygnusEvent> groupedBatchEvents = new ArrayList<CygnusEvent>();
        groupedBatchEvents.add(groupedEvent);
        Batch batch = new Batch();
        batch.addEvents(destination, groupedBatchEvents);
        return batch;
    } // createBatch
    
    private Context createContext(String topicType) {
        Context context = new Context();
        context.put("topic_type", topicType);
        context.put("broker_list", brokerList);
        context.put("zookeeper_endpoint", zookeeperEndpoint);
        return context;
    } // createContext
    
} // OrionKafkaSinkTest
