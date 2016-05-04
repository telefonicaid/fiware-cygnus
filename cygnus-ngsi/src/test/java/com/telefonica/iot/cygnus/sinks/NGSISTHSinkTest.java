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

import com.telefonica.iot.cygnus.utils.CommonUtilsForTests;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class NGSISTHSinkTest {
    
    /**
     * Constructor.
     */
    public NGSISTHSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSISTHSinkTest
    
    /**
     * [NGSISTHSink.configure] -------- Configured 'collection_prefix' cannot be 'system.'.
     */
    @Test
    public void testConfigureCollectionPrefixIsNotSystem() {
        System.out.println(getTestTraceHead("[OrionSTHSink.configure)")
                + "-------- Configured 'collection_prefix' cannot be 'system.'");
        String collectionPrefix = "system.";
        String dbPrefix = "sth_";
        String dataModel = null; // defaulting
        NGSISTHSink sink = new NGSISTHSink();
        sink.configure(CommonUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel));
        
        try {
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[OrionSTHSink.configure]")
                    + "-  OK  - 'system.' value detected for 'collection_prefix'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionSTHSink.configure]")
                    + "- FAIL - 'system.' value not detected for 'collection_prefix'");
        } // try catch
    } // testConfigureCollectionPrefixIsNotSystem
    
    /**
     * [NGSISTHSink.configure] -------- Configured 'collection_prefix' is encoded when having forbiden characters.
     */
    @Test
    public void testConfigureCollectionPrefixIsEncoded() {
        System.out.println(getTestTraceHead("[OrionSTHSink.configure]")
                + "-------- Configured 'collection_prefix' is encoded when having forbiden characters");
        String collectionPrefix = "this\\is/a$prefix.with-forbiden,chars:-.";
        String dbPrefix = "sth_";
        String dataModel = null; // defaulting
        NGSISTHSink sink = new NGSISTHSink();
        sink.configure(CommonUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel));
        String encodedCollectionPrefix = NGSIUtils.encodeSTHCollection(collectionPrefix);
        
        try {
            assertTrue(sink.collectionPrefix.equals(encodedCollectionPrefix));
            System.out.println(getTestTraceHead("[OrionSTHSink.configure]")
                    + "-  OK  - 'collection_prefix=" + collectionPrefix
                    + "' correctly encoded as '" + encodedCollectionPrefix + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionSTHSink.configure]")
                    + "- FAIL - 'collection_prefix=" + collectionPrefix
                    + "' wrongly encoded as '" + encodedCollectionPrefix + "'");
        } // try catch
    } // testConfigureCollectionPrefixIsEncoded
    
    /**
     * [NGSISTHSink.configure] -------- Configured 'db_prefix' is encoded when having forbiden characters.
     */
    @Test
    public void testConfigureDBPrefixIsEncoded() {
        System.out.println(getTestTraceHead("[OrionSTHSink.configure]")
                + "-------- Configured 'db_prefix' is encoded when having forbiden characters");
        String collectionPrefix = "sth_";
        String dbPrefix = "this\\is/a$prefix.with forbiden\"chars:-.";
        String dataModel = null; // defaulting
        NGSISTHSink sink = new NGSISTHSink();
        sink.configure(CommonUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel));
        String encodedDbPrefix = NGSIUtils.encodeSTHDB(dbPrefix);
        
        try {
            assertTrue(sink.dbPrefix.equals(encodedDbPrefix));
            System.out.println(getTestTraceHead("[OrionSTHSink.configure]")
                    + "-  OK  - 'db_prefix=" + dbPrefix + "' correctly encoded as '" + encodedDbPrefix + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionSTHSink.configure]")
                    + "- FAIL - 'db_prefix=" + dbPrefix + "' wrongly encoded as '" + encodedDbPrefix + "'");
        } // try catch
    } // testConfigureDBPrefixIsEncoded
    
} // NGSISTHSinkTest
