/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
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

import com.telefonica.iot.cygnus.backends.dynamo.DynamoDBBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.sinks.OrionDynamoDBSink.TableType;
import com.telefonica.iot.cygnus.utils.TestUtils;
import java.util.ArrayList;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.lifecycle.LifecycleState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class OrionDynamoDBSinkTest {
    
    // mocks
    @Mock
    private DynamoDBBackendImpl mockDynamoDBBackend;
    
    // instance to be tested
    private OrionDynamoDBSink sink;
    
    // other inmutable instances
    private NotifyContextRequest singleNotifyContextRequest;
    private NotifyContextRequest multipleNotifyContextRequest;
    
    // context constants
    private final String accessKeyId = "xxxx";
    private final String secretAccessKey = "xxxx";
    private final String region = "eu-west-1";
    private final String attrPersistence = "row";
    private final String enableGrouping = "true";
    private final String tableType = "table-by-destination";
    
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
        sink = new OrionDynamoDBSink();
        sink.setPersistenceBackend(mockDynamoDBBackend);
        
        // set up other immutable instances
        singleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(singleContextElementNotification);
        multipleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(multipleContextElementNotification);
        
        // set up the behaviour of the mocked classes
        doNothing().doThrow(new Exception()).when(mockDynamoDBBackend).createTable(null, null);
        doNothing().doThrow(new Exception()).when(mockDynamoDBBackend).putItems(null, null);
    } // setUp
    
    /**
     * Test of configure method, of class OrionDynamoDBSink.
     */
    @Test
    public void testConfigure() {
        System.out.println("Testing OrionDynamoDBSink.configure");
        Context context = createContext();
        sink.configure(context);
        assertEquals(accessKeyId, sink.getAccessKeyId());
        assertEquals(secretAccessKey, sink.getSecretAccessKey());
        assertEquals(region, sink.getRegion());
        assertEquals(attrPersistence, sink.getRowAttrPersistence() ? "row" : "column");
        assertEquals(enableGrouping, sink.getEnableGrouping() ? "true" : "false");
        assertEquals(TableType.valueOf(tableType.replaceAll("-", "").toUpperCase()), sink.getTableType());
    } // testConfigure

    /**
     * Test of start method, of class OrionDynamoDBSink.
     */
    @Test
    public void testStart() {
        System.out.println("Testing OrionDynamoDBSink.start");
        Context context = createContext();
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        sink.start();
        assertTrue(sink.getPersistenceBackend() != null);
        assertEquals(LifecycleState.START, sink.getLifecycleState());
    } // testStart
    
    /**
     * Test of persistBatch method, of class OrionDynamoDBSink. Null batches are tested.
     */
    @Test
    public void testPersistNullBatches() {
        System.out.println("Testing OrionDynamoDBSink.persist (null batches)");
        Context context = createContext();
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(null, null);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersistNullBatches

    /**
     * Test of persistBath method, of class OrionDynamoDBSink. Special resources length are tested.
     * @throws java.lang.Exception
     */
    @Test
    public void testPersistResourceLengths() throws Exception {
        // common objects
        Context context = createContext();
        
        System.out.println("Testing OrionDynamoDBSink.persistBatch (normal resource lengths)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        Batch defaultBatch = createBatch(recvTimeTs, normalService, normalDefaultServicePath, normalDefaultDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        Batch groupedBatch = createBatch(recvTimeTs, normalService, normalGroupedServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(defaultBatch, groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionDynamoDBSink.persistBatch (too long service name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        defaultBatch = createBatch(recvTimeTs, abnormalService, normalDefaultServicePath, normalDefaultDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        groupedBatch = createBatch(recvTimeTs, abnormalService, normalGroupedServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(defaultBatch, groupedBatch);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionDynamoDBSink.persistBatch (too long servicePath name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        defaultBatch = createBatch(recvTimeTs, normalService, abnormalDefaultServicePath, normalDefaultDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        groupedBatch = createBatch(recvTimeTs, normalService, abnormalGroupedServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(defaultBatch, groupedBatch);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionDynamoDBSink.persistBatch (too long destination name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        defaultBatch = createBatch(recvTimeTs, normalService, normalDefaultServicePath, abnormalDefaultDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        groupedBatch = createBatch(recvTimeTs, normalService, normalGroupedServicePath, abnormalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(defaultBatch, groupedBatch);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
    } // testPersistResourceLengths
    
    /**
     * Test of persistBatch method, of class OrionDynamoDBSink. Special service and service-path are tested.
     * @throws java.lang.Exception
     */
    @Test
    public void testPersistServiceServicePath() throws Exception {
        // common objects
        Context context = createContext();
        
        System.out.println("Testing OrionDynamoDBSink.persistBatch (\"root\" servicePath name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        Batch defaultBatch = createBatch(recvTimeTs, normalService, rootServicePath, normalDefaultDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        Batch groupedBatch = createBatch(recvTimeTs, normalService, rootServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(defaultBatch, groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally

        System.out.println("Testing OrionDynamoDBSink.persistBatch (multiple destinations and "
                + "fiware-servicePaths)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        defaultBatch = createBatch(recvTimeTs, normalService, normalDefaultServicePath, normalDefaultDestination,
                multipleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        groupedBatch = createBatch(recvTimeTs, normalService, normalGroupedServicePath, normalGroupedDestination,
                multipleNotifyContextRequest.getContextResponses().get(0).getContextElement());

        try {
            sink.persistBatch(defaultBatch, groupedBatch);
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
    
    private Context createContext() {
        Context context = new Context();
        context.put("access_key_id", accessKeyId);
        context.put("secret_access_key", secretAccessKey);
        context.put("region", region);
        context.put("attr_persistence", attrPersistence);
        context.put("enable_grouping", enableGrouping);
        context.put("table_type", tableType);
        return context;
    } // createContext
    
} // OrionDynamoDBSinkTest
