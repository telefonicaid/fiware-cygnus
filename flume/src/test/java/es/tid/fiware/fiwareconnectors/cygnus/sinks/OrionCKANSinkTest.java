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

import static org.mockito.Mockito.*; // this is required by "when" like functions
import static org.junit.Assert.*; // this is required by "fail" like assertions
import es.tid.fiware.fiwareconnectors.cygnus.backends.ckan.CKANBackend;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest;
import es.tid.fiware.fiwareconnectors.cygnus.http.HttpClientFactory;
import es.tid.fiware.fiwareconnectors.cygnus.sinks.OrionSink.TimeHelper;
import es.tid.fiware.fiwareconnectors.cygnus.utils.TestUtils;
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
    private HttpClientFactory mockHttpClientFactory;
    @Mock
    private CKANBackend mockCKANBackend;
    @Mock
    private TimeHelper mockTimeHelper;
    
    // instance to be tested
    private OrionCKANSink sink;
    
    // other instances
    private Context context;
    private NotifyContextRequest notifyContextRequest;
    
    // constants
    private final String ckanHost = "localhost";
    private final String ckanPort = "3306";
    private final String apiKey = "xyzwxyzwxyzw";
    private final String dataset = "data";
    private final long ts = 123456789;
    private final String iso8601date = "20140513T16:48:13";
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
            +         "<entityId type=\"Room\" isPattern=\"false\">"
            +           "<id>Room1</id>"
            +         "</entityId>"
            +         "<contextAttributeList>"
            +           "<contextAttribute>"
            +             "<name>temperature</name>"
            +             "<type>centigrade</type>"
            +             "<value>26.5</value>"
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
        sink = new OrionCKANSink();
        sink.setHttpClientFactory(mockHttpClientFactory);
        sink.setPersistenceBackend(mockCKANBackend);
        sink.setTimeHelper(mockTimeHelper);
        
        // set up other instances
        context = new Context();
        context.put("ckan_host", ckanHost);
        context.put("ckan_port", ckanPort);
        context.put("api_key", apiKey);
        context.put("dataset", dataset);
        notifyContextRequest = TestUtils.createXMLNotifyContextRequest(notifyXMLSimple);
        
        // set up the behaviour of the mocked classes
        when(mockHttpClientFactory.getHttpClient(true)).thenReturn(null);
        when(mockHttpClientFactory.getHttpClient(false)).thenReturn(null);
        when(mockTimeHelper.getTime()).thenReturn(ts);
        when(mockTimeHelper.getTimeString()).thenReturn(iso8601date);
        doNothing().doThrow(new Exception()).when(mockCKANBackend).init(null);
        doNothing().doThrow(new Exception()).when(mockCKANBackend).persist(
                null, null, entityId, attrName, attrType, attrValue);
    } // setUp
    
    /**
     * Test the constructor, of class OrionMySQLSink.
     */
    @Test
    public void testCKANSink() {
        System.out.println("CKANSink");
        assertTrue(sink.timeHelper != null);
    } // testCKANSink

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
        assertEquals(dataset, sink.getDataset());
    } // testConfigure

    /**
     * Test of start method, of class OrionCKANSink.
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
     * Test of processContextResponses method, of class OrionCKANSink.
     */
    @Test
    public void testProcessContextResponses() throws Exception {
        System.out.println("processContextResponses");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persist("FIXME", notifyContextRequest.getContextResponses());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testProcessContextResponses
    
} // OrionCKANSinkTest
