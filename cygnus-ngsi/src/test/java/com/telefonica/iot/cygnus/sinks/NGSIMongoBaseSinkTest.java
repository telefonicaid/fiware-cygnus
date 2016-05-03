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
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NGSIMongoBaseSinkTest {
    
    /**
     * This class is used to test once and only once the common functionality shared by all the real extending sinks.
     */
    private class NGSIMongoBaseSinkImpl extends NGSIMongoBaseSink {

        @Override
        void persistBatch(NGSIBatch batch) throws Exception {
            throw new UnsupportedOperationException("Not supported yet.");
        } // persistBatch
        
    } // NGSIMongoBaseSinkImpl
    
    /**
     * Constructor.
     */
    public NGSIMongoBaseSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSIMongoBaseSinkTest
    
    /**
     * [NGSIMongoSink.configure] -------- Configured 'collection_prefix' cannot be 'system.'.
     */
    @Test
    public void testConfigureCollectionPrefixIsNotSystem() {
        System.out.println(getTestTraceHead("[OrionMongoSink.configure]")
                + "-------- Configured 'collection_prefix' cannot be 'system.'");
        String collectionPrefix = "system.";
        String dbPrefix = "sth_";
        String dataModel = null; // defaulting
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(CommonUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel));
        
        try {
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[OrionMongoSink.configure]")
                    + "-  OK  - 'system.' value detected for 'collection_prefix'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionMongoSink.configure]")
                    + "- FAIL - 'system.' value not detected for 'collection_prefix'");
            throw e;
        } // try catch
    } // testConfigureCollectionPrefixIsNotSystem
    
    /**
     * [NGSIMongoSink.configure] -------- Configured 'collection_prefix' only contains alphanumerics and underscores.
     */
    @Test
    public void testConfigureCollectionPrefixIsLegal() {
        System.out.println(getTestTraceHead("[OrionMongoSink.configure]")
                + "-------- Configured 'collection_prefix' only contains alphanumerics and underscores");
        String collectionPrefix = "this\\is/a$prefix.with-forbiden,chars:-.";
        String dbPrefix = "sth_";
        String dataModel = null; // defaulting
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(CommonUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel));
        
        try {
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[OrionMongoSink.configure]")
                    + "-  OK  - 'collection_prefix=" + collectionPrefix + "' correctly detected as invalid");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionMongoSink.configure]")
                    + "- FAIL - 'collection_prefix=" + collectionPrefix + "' not correctly detected as invalid");
            throw e;
        } // try catch
    } // testConfigureCollectionPrefixIsLegal
    
    /**
     * [NGSIMongoSink.configure] -------- Configured 'db_prefix' only contains alphanumercis and underscores.
     */
    @Test
    public void testConfigureDBPrefixIsLegal() {
        System.out.println(getTestTraceHead("[OrionMongoSink.configure]")
                + "-------- Configured 'db_prefix' only contains alphanumerics and underscores");
        String collectionPrefix = "sth_";
        String dbPrefix = "this\\is/a$prefix.with forbiden\"chars:-,";
        String dataModel = null; // defaulting
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(CommonUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel));
        
        try {
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[OrionMongoSink.configure]")
                    + "-  OK  - 'db_prefix=" + dbPrefix + "' correctly detected as invalid");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionMongoSink.configure]")
                    + "- FAIL - 'db_prefix=" + dbPrefix + "' not correctly detected as invalid");
            throw e;
        } // try catch
    } // testConfigureDBPrefixIsLegal
    
    /**
     * [NGSIMongoBaseSink.buildCollectionName] -------- When / service-path is notified/defaulted and
     * data_model=dm-by-service-path, the MongoDB collection name is <prefix>/.
     */
    @Test
    public void testBuildCollectionNameDMByServicePathRootServicePath() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When / service-path is notified/defaulted and data_model=dm-by-service-path, "
                + "the MongoDB collection name is <prefix>/");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-service-path";
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(CommonUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel));
        String fiwareService = "default";
        String fiwareServicePath = "/";
        String entityId = "someId";
        String entityType = "someType";
        String attrName = "someName";
        boolean isAggregated = false;
        
        
        try {
            String collectionName = sink.buildCollectionName(fiwareService, fiwareServicePath, entityId,
                    entityType, attrName, isAggregated);
            
            try {
                assertEquals("sth_/", collectionName);
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "-  OK  - '" + collectionName + "' was crated as collection name");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - '" + collectionName + "' was crated as collection name instead of 'sth_/'");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - There was some problem when building the collection name");
            assertTrue(false);
        } // catch
    } // testBuildCollectionNameDMByServicePathRootServicePath
    
    /**
     * [NGSIMongoBaseSink.buildCollectionName] -------- When / service-path is notified/defaulted and
     * data_model=dm-by-entity, the MongoDB collections name is <prefix>/;<entityId>;<entityType>.
     */
    @Test
    public void testBuildCollectionNameDMByEntityRootServicePath() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When / service-path is notified/defaulted and data_model=dm-by-entity, the MongoDB"
                + "collections name is <prefix>/;<entityId>;<entityType>");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-entity";
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(CommonUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel));
        String fiwareService = "default";
        String fiwareServicePath = "/";
        String entityId = "someId";
        String entityType = "someType";
        String attrName = "someName";
        boolean isAggregated = false;
        
        
        try {
            String collectionName = sink.buildCollectionName(fiwareService, fiwareServicePath, entityId,
                    entityType, attrName, isAggregated);
            
            try {
                assertEquals("sth_/;someId;someType", collectionName);
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "-  OK  - '" + collectionName + "' was crated as collection name");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - '" + collectionName + "' was crated as collection name instead of "
                        + "'sth_/;someId;someType'");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - There was some problem when building the collection name");
            assertTrue(false);
        } // catch
    } // testBuildCollectionNameDMByEntityRootServicePath
    
    /**
     * [NGSIMongoBaseSink.buildCollectionName] -------- When / service-path is notified/defaulted and
     * data_model=dm-by-attribute, the MongoDB collections name is
     * <prefix>/;<entityId>:<entityType>_;<attrName>.
     */
    @Test
    public void testBuildCollectionNameDMByAttributeRootServicePath() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When / service-path is notified/defaulted and data_model=dm-by-attribute, the "
                + "MongoDB collections name is <prefix>/;<entityId>;<entityType>;<attrName>");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-attribute";
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(CommonUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel));
        String fiwareService = "default";
        String fiwareServicePath = "/";
        String entityId = "someId";
        String entityType = "someType";
        String attrName = "someName";
        boolean isAggregated = false;
        
        try {
            String collectionName = sink.buildCollectionName(fiwareService, fiwareServicePath, entityId,
                    entityType, attrName, isAggregated);
            
            try {
                assertEquals("sth_/;someId;someType;someName", collectionName);
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "-  OK  - '" + collectionName + "' was crated as collection name");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - '" + collectionName + "' was crated as collection name instead of "
                        + "'sth_/;someId;someType;someName'");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - There was some problem when building the collection name");
            assertTrue(false);
        } // catch
    } // testBuildCollectionNameDMByEntityRootServicePath
    
} // NGSIMongoBaseSinkTest
