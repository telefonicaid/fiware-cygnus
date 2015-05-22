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
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.TestUtils;
import java.util.HashMap;
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
    
    // other instances
    private Context context;
    private NotifyContextRequest singleNotifyContextRequest;
    private NotifyContextRequest multipleNotifyContextRequest;
    
    // constants
    private final String ckanHost = "localhost";
    private final String ckanPort = "3306";
    private final String apiKey = "xyzwxyzwxyzw";
    private final long recvTimeTs = 123456789;
    private final String recvTime = "20140513T16:48:13";
    private final String normalServiceName = "vehicles";
    private final String abnormalServiceName =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongservname";
    private final String singleServicePathName = "4wheels";
    private final String multipleServicePathName = "4wheelsSport,4wheelsUrban";
    private final String abnormalServicePathName =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongservpathname";
    private final String rootServicePathName = "";
    private final String singleDestinationName = "car1-car";
    private final String multipleDestinationName = "sport1,urban1";
    private final String abnormalDestinationName =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongdestname";
    private static final String ATTRNAME = "speed";
    private static final String ATTRTYPE = "float";
    private static final String ATTRVALUE = "112.9";
    private static final String ATTRMD =
            "{\"name\":\"measureTime\", \"type\":\"timestamp\", \"value\":\"20140513T16:47:59\"}";
    private static final HashMap<String, String> ATTRLIST;
    private static final HashMap<String, String> ATTRMDLIST;
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
    
    static {
        ATTRLIST = new HashMap<String, String>();
        ATTRLIST.put(ATTRNAME, ATTRVALUE);
        ATTRMDLIST = new HashMap<String, String>();
        ATTRMDLIST.put(ATTRNAME + "_md", ATTRMD);
    } // static

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
        
        // set up other instances
        context = new Context();
        context.put("ckan_host", ckanHost);
        context.put("ckan_port", ckanPort);
        context.put("api_key", apiKey);
        singleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(singleContextElementNotification);
        multipleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(multipleContextElementNotification);
        
        // set up the behaviour of the mocked classes
        doNothing().doThrow(new Exception()).when(mockCKANBackend).persist(recvTimeTs, recvTime, normalServiceName,
                singleServicePathName, singleDestinationName, ATTRNAME, ATTRTYPE, ATTRVALUE, ATTRMD);
        doNothing().doThrow(new Exception()).when(mockCKANBackend).persist(recvTime, normalServiceName,
                singleServicePathName, singleDestinationName, ATTRLIST, ATTRMDLIST);
    } // setUp

    /**
     * Test of configure method, of class OrionCKANSink.
     */
    @Test
    public void testConfigure() {
        System.out.println("configure");
        sink.configure(context);
        assertEquals(ckanHost, sink.getCKANHost());
        assertEquals(ckanPort, sink.getCKANPort());
        assertEquals(apiKey, sink.getAPIKey());
    } // testConfigure

    /**
     * Test of start method, of class OrionCKANSink.
     */
    @Test
    public void testStart() {
        System.out.println("Testing OrionCKANSinkTest.start");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        sink.start();
        assertTrue(sink.getPersistenceBackend() != null);
        assertEquals(LifecycleState.START, sink.getLifecycleState());
    } // testStart

    /**
     * Test of processContextResponses method, of class OrionCKANSink.
     * @throws java.lang.Exception
     */
    @Test
    public void testProcessContextResponses() throws Exception {
        System.out.println("Testing OrionCKANSinkTest.processContextResponses (normal resource lengths)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("timestamp", Long.toString(recvTimeTs));
        headers.put(Constants.HEADER_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_SERVICE_PATH, singleServicePathName);
        headers.put(Constants.DESTINATION, singleDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionCKANSinkTest.processContextResponses (too long service name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put("timestamp", Long.toString(recvTimeTs));
        headers.put(Constants.HEADER_SERVICE, abnormalServiceName);
        headers.put(Constants.HEADER_SERVICE_PATH, singleServicePathName);
        headers.put(Constants.DESTINATION, singleDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionCKANSinkTest.processContextResponses (too long servicePath name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put("timestamp", Long.toString(recvTimeTs));
        headers.put(Constants.HEADER_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_SERVICE_PATH, abnormalServicePathName);
        headers.put(Constants.DESTINATION, singleDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionCKANSinkTest.processContextResponses (too long destination name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put("timestamp", Long.toString(recvTimeTs));
        headers.put(Constants.HEADER_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_SERVICE_PATH, singleServicePathName);
        headers.put(Constants.DESTINATION, abnormalDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionCKANSinkTest.processContextResponses (\"root\" servicePath name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put("timestamp", Long.toString(recvTimeTs));
        headers.put(Constants.HEADER_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_SERVICE_PATH, rootServicePathName);
        headers.put(Constants.DESTINATION, singleDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionCKANSinkTest.processContextResponses (multiple destinations and "
                + "fiware-servicePaths)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put("timestamp", Long.toString(recvTimeTs));
        headers.put(Constants.HEADER_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_SERVICE_PATH, multipleServicePathName);
        headers.put(Constants.DESTINATION, multipleDestinationName);
        
        try {
            sink.persist(headers, multipleNotifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testProcessContextResponses
    
} // OrionCKANSinkTest
