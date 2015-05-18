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

import com.telefonica.iot.cygnus.sinks.OrionHDFSSink;
import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImpl;
import static org.junit.Assert.*; // this is required by "fail" like assertions
import static org.mockito.Mockito.*; // this is required by "when" like functions
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.backends.http.HttpClientFactory;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.TestUtils;
import java.util.HashMap;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.lifecycle.LifecycleState;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class OrionHDFSSinkTest {
    
    // mocks
    @Mock
    private HttpClientFactory mockHttpClientFactory;
    @Mock
    private HDFSBackendImpl mockWebHDFSBackend;
    
    // instance to be tested
    private OrionHDFSSink sink;
    
    // other instances
    private Context context;
    private NotifyContextRequest notifyContextRequest;
    
    // constants
    private final String[] cosmosHost = {"localhost"};
    private final String cosmosPort = "14000";
    private final String cosmosDefaultUsername = "user1";
    private final String cosmosDefaultPassword = "pass1234";
    private final String hdfsAPI = "httpfs";
    private final String hivePort = "10000";
    private final long recvTimeTs = 123456789;
    private final String normalServiceName = "rooms";
    private final String abnormalServiceName =
            "toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongorgname";
    private final String normalServicePathName = "numeric-rooms";
    private final String abnormalServicePathName =
            "toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongpkgname";
    private final String rootServicePathName = "";
    private final String normalDestinationName = "room1-room";
    private final String abnormalDestinationName =
            "toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongresname";
    private static final String ATTRNAME = "temperature";
    private final String notifyXMLSimple = ""
            + "<notifyContextRequest>"
            +   "<subscriptionId>51c0ac9ed714fb3b37d7d5a8</subscriptionId>"
            +   "<originator>localhost</originator>"
            +   "<contextResponseList>"
            +     "<contextElementResponse>"
            +       "<contextElement>"
            +         "<entityId type=\"AType\" isPattern=\"false\">"
            +           "<id>Entity</id>"
            +         "</entityId>"
            +         "<contextAttributeList>"
            +           "<contextAttribute>"
            +             "<name>attribute</name>"
            +             "<type>attributeType</type>"
            +             "<contextValue>foo</contextValue>"
            +           "</contextAttribute>"
            +         "</contextAttributeList>"
            +       "</contextElement>"
            +       "<statusCode>"
            +         "<code>200</code>"
            +         "<reasonPhrase>OK</reasonPhrase>"
            +       "</statusCode>"
            +     "</contextElementResponse>"
            +   "</contextResponseList>"
            + "</notifyContextRequest>";

    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        sink = new OrionHDFSSink();
        sink.setPersistenceBackend(mockWebHDFSBackend);
        
        // set up other instances
        context = new Context();
        context.put("cosmos_host", cosmosHost[0]);
        context.put("cosmos_port", cosmosPort);
        context.put("cosmos_default_username", cosmosDefaultUsername);
        context.put("cosmos_default_password", cosmosDefaultPassword);
        context.put("hdfs_api", hdfsAPI);
        context.put("hive_port", hivePort);
        notifyContextRequest = TestUtils.createXMLNotifyContextRequest(notifyXMLSimple);
        
        // set up the behaviour of the mocked classes
        when(mockHttpClientFactory.getHttpClient(true, false)).thenReturn(null);
        when(mockHttpClientFactory.getHttpClient(false, false)).thenReturn(null);
        when(mockWebHDFSBackend.exists(null)).thenReturn(true);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).createDir(ATTRNAME);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).createFile(ATTRNAME, ATTRNAME);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).append(ATTRNAME, ATTRNAME);
    } // setUp

    /**
     * Test of configure method, of class OrionHDFSSink.
     */
    @Test
    public void testConfigure() {
        System.out.println("configure");
        sink.configure(context);
        assertEquals(cosmosHost[0], sink.getCosmosHost()[0]);
        assertEquals(cosmosPort, sink.getCosmosPort());
        assertEquals(cosmosDefaultUsername, sink.getCosmosDefaultUsername());
        assertEquals(cosmosDefaultPassword, sink.getCosmosDefaultPassword());
        assertEquals(hdfsAPI, sink.getHDFSAPI());
        assertEquals(hivePort, sink.getHivePort());
    } // testConfigure

    /**
     * Test of start method, of class OrionHDFSSink.
     */
    @Test
    public void testStart() {
        System.out.println("start");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        sink.start();
        assertTrue(sink.getPersistenceBackend() != null);
        assertEquals(LifecycleState.START, sink.getLifecycleState());
    } // testStart

    /**
     * Test of persist method, of class OrionHDFSSink.
     */
    @Test
    public void testProcessContextResponses() throws Exception {
        System.out.println("Testing OrionHDFSSinkTest.processContextResponses (normal resource lengths)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("timestamp", "123456789");
        headers.put(Constants.HEADER_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_SERVICE_PATH, normalServicePathName);
        headers.put(Constants.DESTINATION, normalDestinationName);
        
        try {
            sink.persist(headers, notifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionHDFSSinkTest.processContextResponses (too long service name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put("timestamp", new Long(recvTimeTs).toString());
        headers.put(Constants.HEADER_SERVICE, abnormalServiceName);
        headers.put(Constants.HEADER_SERVICE_PATH, normalServicePathName);
        headers.put(Constants.DESTINATION, normalDestinationName);
        
        try {
            sink.persist(headers, notifyContextRequest);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionHDFSSinkTest.processContextResponses (too long servicePath name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put("timestamp", new Long(recvTimeTs).toString());
        headers.put(Constants.HEADER_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_SERVICE_PATH, abnormalServicePathName);
        headers.put(Constants.DESTINATION, normalDestinationName);
        
        try {
            sink.persist(headers, notifyContextRequest);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionHDFSSinkTest.processContextResponses (too long destination name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put("timestamp", new Long(recvTimeTs).toString());
        headers.put(Constants.HEADER_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_SERVICE_PATH, normalServicePathName);
        headers.put(Constants.DESTINATION, abnormalDestinationName);
        
        try {
            sink.persist(headers, notifyContextRequest);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionHDFSSinkTest.processContextResponses (\"root\" servicePath name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put("timestamp", new Long(recvTimeTs).toString());
        headers.put(Constants.HEADER_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_SERVICE_PATH, rootServicePathName);
        headers.put(Constants.DESTINATION, normalDestinationName);
        
        try {
            sink.persist(headers, notifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testProcessContextResponses
    
} // OrionHDFSSinkTest
