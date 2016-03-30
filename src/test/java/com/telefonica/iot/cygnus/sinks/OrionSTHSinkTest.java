/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
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

import com.telefonica.iot.cygnus.utils.Utils;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.lifecycle.LifecycleState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class OrionSTHSinkTest {
    
    /**
     * [OrionSTHSink] -------- The sink starts properly.
     */
    @Test
    public void testStart() {
        System.out.println("[OrionSTHSink] -------- The sink starts properly");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        OrionSTHSink sink = new OrionSTHSink();
        sink.configure(createContext(collectionPrefix, dbPrefix));
        sink.setChannel(new MemoryChannel());
        sink.start();
        LifecycleState state = sink.getLifecycleState();
        
        try {
            assertEquals(LifecycleState.START, state);
            System.out.println("[OrionSTHSink] -  OK  - The sink started properly, the lifecycle state is '"
                    + state.toString() + "'");
        } catch (AssertionError e) {
            System.out.println("[OrionSTHSink] - FAIL - The sink did not start properly, the lifecycle state "
                    + "is '" + state.toString() + "'");
        } // try catch
    } // testStart
    
    /**
     * [OrionSTHSink] -------- Configured 'collection_prefix' cannot be 'system.'.
     */
    @Test
    public void testConfiguredCollectionPrefixIsNotSystem() {
        System.out.println("[OrionSTHSink] -------- Configured 'collection_prefix' cannot be 'system.'");
        String collectionPrefix = "system.";
        String dbPrefix = "sth_";
        OrionSTHSink sink = new OrionSTHSink();
        sink.configure(createContext(collectionPrefix, dbPrefix));
        
        try {
            assertTrue(sink.invalidConfiguration);
            System.out.println("[OrionSTHSink] -  OK  - 'system.' value detected for 'collection_prefix'");
        } catch (AssertionError e) {
            System.out.println("[OrionSTHSink] - FAIL - 'system.' value not detected for 'collection_prefix'");
        } // try catch
    } // testConfiguredCollectionPrefixIsNotSystem
    
    /**
     * [OrionSTHSink] -------- Configured 'collection_prefix' is encoded when having forbiden characters.
     */
    @Test
    public void testConfiguredCollectionPrefixIsEncoded() {
        System.out.println("[OrionSTHSink] -------- Configured 'collection_prefix' "
                + "is encoded when having forbiden characters");
        String collectionPrefix = "this\\is/a$prefix.with-forbiden,chars";
        String dbPrefix = "sth_";
        OrionSTHSink sink = new OrionSTHSink();
        sink.configure(createContext(collectionPrefix, dbPrefix));
        String encodedCollectionPrefix = Utils.encodeSTHCollection(collectionPrefix);
        
        try {
            assertTrue(sink.collectionPrefix.equals(encodedCollectionPrefix));
            System.out.println("[OrionSTHSink] -  OK  - 'collection_prefix=" + collectionPrefix
                    + "' correctly encoded as '" + encodedCollectionPrefix + "'");
        } catch (AssertionError e) {
            System.out.println("[OrionSTHSink] - FAIL - 'collection_prefix=" + collectionPrefix
                    + "' wrongly encoded as '" + encodedCollectionPrefix + "'");
        } // try catch
    } // testConfiguredCollectionPrefixIsEncoded
    
    /**
     * [OrionSTHSink] -------- Configured 'db_prefix' is encoded when having forbiden characters.
     */
    @Test
    public void testConfiguredDBPrefixIsEncoded() {
        System.out.println("[OrionSTHSink] -------- Configured 'db_prefix' "
                + "is encoded when having forbiden characters");
        String collectionPrefix = "sth_";
        String dbPrefix = "this\\is/a$prefix.with forbiden\"chars";
        OrionSTHSink sink = new OrionSTHSink();
        sink.configure(createContext(collectionPrefix, dbPrefix));
        String encodedDbPrefix = Utils.encodeSTHDB(dbPrefix);
        
        try {
            assertTrue(sink.dbPrefix.equals(encodedDbPrefix));
            System.out.println("[OrionSTHSink] -  OK  - 'db_prefix=" + dbPrefix
                    + "' correctly encoded as '" + encodedDbPrefix + "'");
        } catch (AssertionError e) {
            System.out.println("[OrionSTHSink] - FAIL - 'db_prefix=" + dbPrefix
                    + "' wrongly encoded as '" + encodedDbPrefix + "'");
        } // try catch
    } // testConfiguredDBPrefixIsEncoded
    
    private Context createContext(String collectionPrefix, String dbPrefix) {
        Context context = new Context();
        context.put("attr_persistence", "row");
        context.put("batch_size", "100");
        context.put("batch_timeout", "30");
        context.put("batch_ttl", "10");
        context.put("collection_prefix", collectionPrefix);
        context.put("collection_size", "0");
        context.put("data_expiration", "0");
        context.put("data_model", "dm-by-entity");
        context.put("db_prefix", dbPrefix);
        context.put("enable_grouping", "false");
        context.put("enable_lowercase", "false");
        context.put("max_documents", "0");
        context.put("mongo_hosts", "localhost:27017");
        context.put("mongo_password", "");
        context.put("mongo_username", "");
        context.put("should_hash", "false");
        return context;
    } // createContext
    
} // OrionSTHSinkTest
