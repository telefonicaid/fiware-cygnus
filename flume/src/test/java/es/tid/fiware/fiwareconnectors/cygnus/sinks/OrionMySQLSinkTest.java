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
import es.tid.fiware.fiwareconnectors.cygnus.backends.mysql.MySQLBackend;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest;
import es.tid.fiware.fiwareconnectors.cygnus.utils.TestUtils;
import java.util.Date;
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
    private final String namingPrefix = "cygnus_";
    private final long ts = 123456789;
    private final String recvTime = "20140513T16:48:13";
    private final String entityId = "Room1";
    private final String entityType = "Room";
    private final String attrName = "temperature";
    private final String attrType = "degrees";
    private final String attrValue = "26.5";
    private final String attrMd = "{\"name\":\"measureTime\", \"type\":\"timestamp\", \"value\":\"20140513T16:47:59\"}";
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
    private final String dbName = "cygnus_user1";
    private final String tableName = "cygnus_Room1_Room";

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
        context.put("naming_prefix", namingPrefix);
        notifyContextRequest = TestUtils.createXMLNotifyContextRequest(notifyXMLSimple);
        
        // set up the behaviour of the mocked classes
        doNothing().doThrow(new Exception()).when(mockMySQLBackend).createDatabase(dbName);
        doNothing().doThrow(new Exception()).when(mockMySQLBackend).createTable(dbName, tableName);
        doNothing().doThrow(new Exception()).when(mockMySQLBackend).insertContextData(dbName, tableName, ts,
                recvTime, entityId, entityType, attrName, attrType, attrValue, attrMd);
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
        assertEquals(namingPrefix, sink.getNamingPrefix());
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
    
} // OrionMySQLSinkTest