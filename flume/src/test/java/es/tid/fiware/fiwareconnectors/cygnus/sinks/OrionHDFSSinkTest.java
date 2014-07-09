/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * frb@tid.es
 */

package es.tid.fiware.fiwareconnectors.cygnus.sinks;

import static org.junit.Assert.*; // this is required by "fail" like assertions
import static org.mockito.Mockito.*; // this is required by "when" like functions
import es.tid.fiware.fiwareconnectors.cygnus.backends.hdfs.HDFSBackend;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest;
import es.tid.fiware.fiwareconnectors.cygnus.http.HttpClientFactory;
import es.tid.fiware.fiwareconnectors.cygnus.utils.TestUtils;
import java.util.Date;
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
    private HDFSBackend mockWebHDFSBackend;
    
    // instance to be tested
    private OrionHDFSSink sink;
    
    // other instances
    private Context context;
    private NotifyContextRequest notifyContextRequest;
    
    // constants
    private final String cosmosHost = "localhost";
    private final String cosmosPort = "3306";
    private final String cosmosDefaultUsername = "user1";
    private final String cosmosDefaultPassword = "pass1234";
    private final String hdfsAPI = "httpfs";
    private final String hivePort = "10000";
    private final long ts = 123456789;
    private final String recvTime = "20140513T16:48:13";
    private final String entityId = "room1";
    private final String entityType = "room";
    private final String attrName = "temperature";
    private final String attrType = "degrees";
    private final String attrValue = "26.5";
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
        sink.setHttpClientFactory(mockHttpClientFactory);
        sink.setPersistenceBackend(mockWebHDFSBackend);
        
        // set up other instances
        context = new Context();
        context.put("cosmos_host", cosmosHost);
        context.put("cosmos_port", cosmosPort);
        context.put("cosmos_default_username", cosmosDefaultUsername);
        context.put("cosmos_default_password", cosmosDefaultPassword);
        context.put("hdfs_api", hdfsAPI);
        context.put("hive_port", hivePort);
        notifyContextRequest = TestUtils.createXMLNotifyContextRequest(notifyXMLSimple);
        
        // set up the behaviour of the mocked classes
        when(mockHttpClientFactory.getHttpClient(true)).thenReturn(null);
        when(mockHttpClientFactory.getHttpClient(false)).thenReturn(null);
        when(mockWebHDFSBackend.exists(null, null, null)).thenReturn(true);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).createDir(null, null, attrName);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).createFile(null, null, attrName, attrName);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).append(null, null, attrName, attrName);
    } // setUp

    /**
     * Test of configure method, of class OrionHDFSSink.
     */
    @Test
    public void testConfigure() {
        System.out.println("configure");
        sink.configure(context);
        assertEquals(cosmosHost, sink.getCosmosHost());
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
        assertTrue(sink.getHttpClientFactory() != null);
        assertTrue(sink.getPersistenceBackend() != null);
        assertEquals(LifecycleState.START, sink.getLifecycleState());
    } // testStart

    /**
     * Test of persist method, of class OrionHDFSSink.
     */
    @Test
    public void testProcessContextResponses() throws Exception {
        System.out.println("processContextResponses");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persist("FIXME", new Date().getTime(), notifyContextRequest.getContextResponses());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testProcessContextResponses
    
} // OrionHDFSSinkTest
