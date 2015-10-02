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

import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.lifecycle.LifecycleState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class OrionMongoBaseSinkTest {
    
    // instance to be tested
    private OrionMongoSink sink;
    
    // other instances
    private Context context;
    
    // constants
    private final String mongoURI = "localhost:27017";
    private final String mongoUsername = "admin";
    private final String mongoPassword = "1a2b3c4d";
    private final String dataModel = "collection-per-entity";
    private final String dbPrefix = "test_";
    private final String collectionPrefix = "test_";
    private final String enableGrouping = "true";
    
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
        
        // set up other instances
        context = new Context();
        context.put("mongo_uri", mongoURI);
        context.put("mongo_username", mongoUsername);
        context.put("mongo_password", mongoPassword);
        context.put("data_model", dataModel);
        context.put("db_prefix", dbPrefix);
        context.put("collection_prefix", collectionPrefix);
        context.put("enable_grouping", enableGrouping);
    } // setUp
    
    /**
     * Test of configure method, of class OrionMongoSink.
     */
    @Test
    public void testConfigure() {
        System.out.println("Testing OrionMongosink.configure");
        sink.configure(context);
        assertEquals(mongoURI, sink.getMongoHosts());
        assertEquals(mongoUsername, sink.getUsername());
        assertEquals(mongoPassword, sink.getPassword());
        assertEquals(sink.getDataModel(dataModel), sink.getDataModel());
        assertEquals(dbPrefix, sink.getDbPrefix());
        assertEquals(collectionPrefix, sink.getCollectionPrefix());
        assertEquals(enableGrouping, sink.getEnableGrouping() ? "true" : "false");
    } // testConfigure

    /**
     * Test of start method, of class OrionMongoSink.
     */
    @Test
    public void testStart() {
        System.out.println("Testing OrionMongoBaseSink.start");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        sink.start();
        assertTrue(sink.getBackend() != null);
        assertEquals(LifecycleState.START, sink.getLifecycleState());
    } // testStart
    
} // OrionMongoBaseSinkTest
