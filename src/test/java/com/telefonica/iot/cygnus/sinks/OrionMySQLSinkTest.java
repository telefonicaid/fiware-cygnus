/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
 *
 * cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.sinks.OrionMySQLSink;
import static org.junit.Assert.*; // this is required by "fail" like assertions
import static org.mockito.Mockito.*; // this is required by "when" like functions
import com.telefonica.iot.cygnus.backends.mysql.MySQLBackend;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.TestUtils;
import java.util.HashMap;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.lifecycle.LifecycleState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class OrionMySQLSinkTest {

    // instance to be tested
    private OrionMySQLSink sink;
    
    // other instances
    private Context context;
    private NotifyContextRequest notifyContextRequest;
    
    // mocks
    @Mock
    private MySQLBackend mockMySQLBackend;

    // constants
    private final String mysqlHost = "localhost";
    private final String mysqlPort = "3306";
    private final String mysqlUsername = "user1";
    private final String mysqlPassword = "pass1234";
    private final String attrPersistence = "row";
    private final long recvTimeTs = 123456789;
    private final String recvTime = "20140513T16:48:13";
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
    private static final String ENTITYNAME = "room1";
    private static final String ENTITYTYPE = "room";
    private static final String ATTRNAME = "temperature";
    private static final String ATTRTYPE = "degrees";
    private static final String ATTRVALUE = "26.5";
    private static final String ATTRMD =
            "{\"name\":\"measureTime\", \"type\":\"timestamp\", \"value\":\"20140513T16:47:59\"}";
    private static final HashMap<String, String> ATTRLIST;
    private static final HashMap<String, String> ATTRMDLIST;
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
        sink = new OrionMySQLSink();
        sink.setPersistenceBackend(mockMySQLBackend);
        
        // set up other instances
        context = new Context();
        context.put("mysql_host", mysqlHost);
        context.put("mysql_port", mysqlPort);
        context.put("mysql_username", mysqlUsername);
        context.put("mysql_password", mysqlPassword);
        context.put("attr_persistence", attrPersistence);
        notifyContextRequest = TestUtils.createXMLNotifyContextRequest(notifyXMLSimple);
        
        // set up the behaviour of the mocked classes
        doNothing().doThrow(new Exception()).when(mockMySQLBackend).createDatabase(null);
        doNothing().doThrow(new Exception()).when(mockMySQLBackend).createTable(null, null);
        doNothing().doThrow(new Exception()).when(mockMySQLBackend).insertContextData(null, null, recvTimeTs, recvTime,
                ENTITYNAME, ENTITYTYPE, ATTRNAME, ATTRTYPE, ATTRVALUE, ATTRMD);
        doNothing().doThrow(new Exception()).when(mockMySQLBackend).insertContextData(null, null, recvTime, ATTRLIST,
                ATTRMDLIST);
    } // setUp
    
    /**
     * Test of configure method, of class OrionMySQLSink.
     */
    @Test
    public void testConfigure() {
        System.out.println("configure");
        sink.configure(context);
        assertEquals(mysqlHost, sink.getMySQLHost());
        assertEquals(mysqlPort, sink.getMySQLPort());
        assertEquals(mysqlUsername, sink.getMySQLUsername());
        assertEquals(mysqlPassword, sink.getMySQLPassword());
        assertEquals(attrPersistence, sink.getRowAttrPersistence() ? "row" : "column");
    } // testConfigure

    /**
     * Test of start method, of class OrionMySQLSink.
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
     * Test of persist method, of class OrionMySQLSink.
     */
    @Test
    public void testProcessContextResponses() throws Exception {
        System.out.println("Testing OrionMySQLSinkTest.processContextResponses (normal resource lengths)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("timestamp", new Long(recvTimeTs).toString());
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
        
        System.out.println("Testing OrionMySQLSinkTest.processContextResponses (too long service name)");
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
        
        System.out.println("Testing OrionMySQLSinkTest.processContextResponses (too long servicePath name)");
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
        
        System.out.println("Testing OrionMySQLSinkTest.processContextResponses (too long destination name)");
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
        
        System.out.println("Testing OrionMySQLSinkTest.processContextResponses (\"root\" servicePath name)");
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
    
} // OrionMySQLSinkTest