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

import com.telefonica.iot.cygnus.backends.ckan.CKANBackend;
import static org.mockito.Mockito.*; // this is required by "when" like functions
import static org.junit.Assert.*; // this is required by "fail" like assertions
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.utils.TestUtils;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.lifecycle.LifecycleState;
import org.junit.Test;

/**
 *
 * @author fgalan
 */
@RunWith(MockitoJUnitRunner.class)
public class OrionCKANSinkTest {
    
    // mocks
    @Mock
    private CKANBackend mockCKANBackend;
    
    // instance to be tested
    private OrionCKANSink sink;
    
    // other inmutable instances
    private NotifyContextRequest singleNotifyContextRequest;
    private NotifyContextRequest multipleNotifyContextRequest;
    
    // context constants
    private final String ckanHost = "localhost";
    private final String ckanPort = "3306";
    private final String apiKey = "xyzwxyzwxyzw";
    private final String orionURL = "http://localhost:1026";
    private final String ssl = "true";
    private String enableGrouping = "true";
    private String attrPersistence = "row";
    
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
        sink = new OrionCKANSink();
        sink.setPersistenceBackend(mockCKANBackend);
        
        // set up other immutable instances
        singleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(singleContextElementNotification);
        multipleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(multipleContextElementNotification);

        // set up the behaviour of the mocked classes
        doNothing().doThrow(new Exception()).when(mockCKANBackend).persist(null, null, null, null, true);
    } // setUp

    /**
     * Test of configure method, of class OrionCKANSink.
     */
    @Test
    public void testConfigure() {
        System.out.println("Testing OrionCKANSink.configure");
        attrPersistence = "row";
        Context context = createContext(attrPersistence, enableGrouping);
        sink.configure(context);
        assertEquals(ckanHost, sink.getCKANHost());
        assertEquals(ckanPort, sink.getCKANPort());
        assertEquals(apiKey, sink.getAPIKey());
        assertEquals(apiKey, sink.getAPIKey());
        assertEquals(attrPersistence, sink.getRowAttrPersistence() ? "row" : "column");
        assertEquals(ssl, sink.getSSL() ? "true" : "false");
    } // testConfigure

    /**
     * Test of start method, of class OrionCKANSink.
     */
    @Test
    public void testStart() {
        System.out.println("Testing OrionCKANSink.start");
        attrPersistence = "row";
        enableGrouping = "true";
        Context context = createContext(attrPersistence, enableGrouping);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        sink.start();
        assertTrue(sink.getPersistenceBackend() != null);
        assertEquals(LifecycleState.START, sink.getLifecycleState());
    } // testStart
    
    /**
     * Test of persistBatch method, of class OrionCKANSink. Null batches are tested.
     */
    @Test
    public void testPersistNullBatches() {
        System.out.println("Testing OrionCKANSinkTest.persistBatch (null batches)");
        attrPersistence = "row";
        enableGrouping = "true";
        Context context = createContext(attrPersistence, enableGrouping);
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
     * Test of persistBatch method, of class OrionCKANSink. Attribute persistence modes are tested.
     */
    @Test
    public void testPersistAttrPersistence() {
        // common objects
        Batch defaultBatch = createBatch(recvTimeTs, normalService, normalDefaultServicePath, normalDefaultDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        Batch groupedBatch = createBatch(recvTimeTs, normalService, normalGroupedServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        System.out.println("Testing OrionCKANSinkTest.persistBatch (row persistence, enable grouping)");
        attrPersistence = "row";
        enableGrouping = "true";
        Context context = createContext(attrPersistence, enableGrouping);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionCKANSinkTest.persistBatch (row persistence, disable grouping)");
        attrPersistence = "row";
        enableGrouping = "false";
        context = createContext(attrPersistence, enableGrouping);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionCKANSinkTest.persistBatch (column attr persistence, enable grouping)");
        attrPersistence = "column";
        enableGrouping = "true";
        context = createContext(attrPersistence, enableGrouping);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionCKANSinkTest.persistBatch (column attr persistence, disable grouping)");
        attrPersistence = "column";
        enableGrouping = "false";
        context = createContext(attrPersistence, enableGrouping);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersistAttrPersistence
    
    /**
     * Test of persistBatch method, of class OrionCKANSink. Attribute persistence modes are tested.
     */
    @Test
    public void testPersistResourceLengths() {
        // common objects
        attrPersistence = "row";
        enableGrouping = "true";
        Context context = createContext(attrPersistence, enableGrouping);
        
        System.out.println("Testing OrionCKANSink.persisBatch (normal resource lengths)");
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
        
        System.out.println("Testing OrionCKANSink.persistBatch (too long service name)");
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
        
        System.out.println("Testing OrionCKANSink.persistBatch (too long servicePath name)");
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
        
        System.out.println("Testing OrionCKANSink.persistBatch (too long destination name)");
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
     * Test of persistBatch method, of class OrionCKANSink. Special service and service-path are tested.
     */
    @Test
    public void testPersistServiceServicePath() {
        // common objects
        attrPersistence = "row";
        enableGrouping = "true";
        Context context = createContext(attrPersistence, enableGrouping);
        
        System.out.println("Testing OrionCKANSink.persistBatch (\"root\" servicePath name)");
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
        
        System.out.println("Testing OrionCKANSink.persistBatch (multiple destinations and "
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
        CygnusEvent groupedEvent = new CygnusEvent(recvTimeTs, service, servicePath, destination, null,
            contextElement);
        Batch batch = new Batch();
        batch.addEvent(destination, groupedEvent);
        return batch;
    } // createBatch
    
    private Context createContext(String attrPersistence, String enableGrouping) {
        Context context = new Context();
        context.put("ckan_host", ckanHost);
        context.put("ckan_port", ckanPort);
        context.put("api_key", apiKey);
        context.put("enable_grouping", enableGrouping);
        context.put("orion_url", orionURL);
        context.put("attr_persistence", attrPersistence);
        context.put("ssl", ssl);
        return context;
    } // createContext
    
} // OrionCKANSinkTest
