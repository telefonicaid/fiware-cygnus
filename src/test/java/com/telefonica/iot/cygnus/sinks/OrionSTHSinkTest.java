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

import com.telefonica.iot.cygnus.backends.mongo.MongoBackend;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.TestUtils;
import java.util.HashMap;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
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
public class OrionSTHSinkTest {
    
    @Mock
    private MongoBackend mockMongoBackend;
    
    // instance to be tested
    private OrionMongoSink sink;
    
    // other instances
    private Context context;
    private NotifyContextRequest notifyContextRequest;
    
    // constants
    private final String mongoURI = "localhost:27017";
    private final String mongoUsername = "admin";
    private final String mongoPassword = "1a2b3c4d";
    private final String dataModel = "collection-per-entity";
    private final String dbPrefix = "test_";
    private final String collectionPrefix = "test_";
    private final String dbName = "db-name";
    private final String collectionName = "collection-name";
    private final long recvTimeTs = 1429535775;
    private final String recvTime = "2015-04-20T12:13:22.41";
    private final String entityId = "car1";
    private final String entityType = "car";
    private final String attrName = "speed";
    private final String attrType = "kmh";
    private final String attrValue = "112.9";
    private final String attrMd = "[]";
    private final String serviceHeader = "vehicles";
    private final String servicePathHeader = "4wheels";
    private final String destination = "4wheelsCars";
    private final String timestamp = "123456789";
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
        sink = new OrionMongoSink();
        sink.setBackend(mockMongoBackend);
        
        // set up other instances
        context = new Context();
        context.put("mongo_uri", mongoURI);
        context.put("mongo_username", mongoUsername);
        context.put("mongo_password", mongoPassword);
        context.put("data_model", dataModel);
        context.put("db_prefix", dbPrefix);
        context.put("collection_prefix", collectionPrefix);
        notifyContextRequest = TestUtils.createXMLNotifyContextRequest(notifyXMLSimple);
        
        // set up the behaviour of the mocked classes
        doNothing().doThrow(new Exception()).when(mockMongoBackend).createDatabase(dbName);
        doNothing().doThrow(new Exception()).when(mockMongoBackend).createCollection(dbName, collectionName);
        doNothing().doThrow(new Exception()).when(mockMongoBackend).insertContextDataAggregated(dbName, collectionName,
                recvTimeTs, recvTime, entityId, entityType, attrName, attrType, attrValue, attrMd);
    } // setUp
    
    /**
     * Test of persist method, of class OrionMongoSink.
     */
    @Test
    public void testProcessContextResponses() {
        System.out.println("Testing OrionSTHSink.processContextResponses");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_SERVICE, serviceHeader);
        headers.put(Constants.HEADER_SERVICE_PATH, servicePathHeader);
        headers.put(Constants.DESTINATION, destination);
        
        try {
            sink.persist(headers, notifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testProcessContextResponses
    
} // OrionSTHSinkTest
