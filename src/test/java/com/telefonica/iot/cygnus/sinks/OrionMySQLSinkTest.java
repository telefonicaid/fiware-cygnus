/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.sinks;

import static org.junit.Assert.*; // this is required by "fail" like assertions
import static org.mockito.Mockito.*; // this is required by "when" like functions
import com.telefonica.iot.cygnus.backends.mysql.MySQLBackend;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.sinks.OrionMySQLSink.TableType;
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
    private NotifyContextRequest singleNotifyContextRequest;
    private NotifyContextRequest multipleNotifyContextRequest;
    
    // mocks
    @Mock
    private MySQLBackend mockMySQLBackend;

    // configuration constants
    private final String mysqlHost = "localhost";
    private final String mysqlPort = "3306";
    private final String mysqlUsername = "user1";
    private final String mysqlPassword = "pass1234";
    private final String attrPersistence = "row";
    private final String enableGrouping = "true";
    private final String tableType = "table-by-destination";
    
    // other constants
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
    private static final String ENTITYNAME = "car1";
    private static final String ENTITYTYPE = "car";
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
        sink = new OrionMySQLSink();
        sink.setPersistenceBackend(mockMySQLBackend);
        
        // set up other instances
        context = new Context();
        context.put("mysql_host", mysqlHost);
        context.put("mysql_port", mysqlPort);
        context.put("mysql_username", mysqlUsername);
        context.put("mysql_password", mysqlPassword);
        context.put("attr_persistence", attrPersistence);
        context.put("enable_grouping", enableGrouping);
        context.put("table_type", tableType);
        singleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(singleContextElementNotification);
        multipleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(multipleContextElementNotification);
        
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
        assertEquals(enableGrouping, sink.getEnableGrouping() ? "true" : "false");
        assertEquals(TableType.valueOf(tableType.replaceAll("-", "").toUpperCase()), sink.getTableType());
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
     * Test of persistOne method, of class OrionMySQLSink.
     * @throws java.lang.Exception
     */
    @Test
    public void testProcessContextResponses() throws Exception {
        System.out.println("Testing OrionMySQLSinkTest.processContextResponses (normal resource lengths)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, Long.toString(recvTimeTs));
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, singleServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, singleDestinationName);
        
        try {
            sink.persistOne(headers, singleNotifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionMySQLSinkTest.processContextResponses (too long service name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, Long.toString(recvTimeTs));
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, abnormalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, singleServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, singleDestinationName);
        
        try {
            sink.persistOne(headers, singleNotifyContextRequest);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionMySQLSinkTest.processContextResponses (too long servicePath name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, Long.toString(recvTimeTs));
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, abnormalServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, singleDestinationName);
        
        try {
            sink.persistOne(headers, singleNotifyContextRequest);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionMySQLSinkTest.processContextResponses (too long destination name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, Long.toString(recvTimeTs));
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, singleServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, abnormalDestinationName);
        
        try {
            sink.persistOne(headers, singleNotifyContextRequest);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionMySQLSinkTest.processContextResponses (\"root\" servicePath name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, Long.toString(recvTimeTs));
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, rootServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, singleDestinationName);
        
        try {
            sink.persistOne(headers, singleNotifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionMySQLSinkTest.processContextResponses (multiple destinations and "
                + "fiware-servicePaths)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, Long.toString(recvTimeTs));
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, multipleServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, multipleDestinationName);
        
        try {
            sink.persistOne(headers, multipleNotifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testProcessContextResponses
    
} // OrionMySQLSinkTest