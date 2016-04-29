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
        NGSIMongoSink sink = new NGSIMongoSink();
        sink.configure(CommonUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel));
        String dbName = "sth_default";
        String fiwareService = "default";
        String fiwareServicePath = "/";
        String entity = "someId_someType";
        String attribute = "someName_someType";
        boolean isAggregated = false;
        String entityId = "someId";
        String entityType = "someType";
        
        try {
            String collectionName = sink.buildCollectionName(dbName, fiwareServicePath, entity, attribute,
                    isAggregated, entityId, entityType, fiwareService);
            
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
     * data_model=dm-by-entity, the MongoDB collections name is <prefix>/_<entityId>_<entityType>.
     */
    @Test
    public void testBuildCollectionNameDMByEntityRootServicePath() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When / service-path is notified/defaulted and data_model=dm-by-entity, the MongoDB"
                + "collections name is <prefix>/_<entityId>_<entityType>");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-entity";
        NGSIMongoSink sink = new NGSIMongoSink();
        sink.configure(CommonUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel));
        String dbName = "sth_default";
        String fiwareService = "default";
        String fiwareServicePath = "/";
        String entity = "someId_someType";
        String attribute = "someName_someType";
        boolean isAggregated = false;
        String entityId = "someId";
        String entityType = "someType";
        
        try {
            String collectionName = sink.buildCollectionName(dbName, fiwareServicePath, entity, attribute,
                    isAggregated, entityId, entityType, fiwareService);
            
            try {
                assertEquals("sth_/_someId_someType", collectionName);
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "-  OK  - '" + collectionName + "' was crated as collection name");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - '" + collectionName + "' was crated as collection name instead of "
                        + "'sth_/_someId_someType'");
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
     * <prefix>/_<entityId>_<entityType>_<attrName>_<attrType>.
     */
    @Test
    public void testBuildCollectionNameDMByAttributeRootServicePath() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When / service-path is notified/defaulted and data_model=dm-by-attribute, the "
                + "MongoDB collections name is <prefix>/_<entityId>_<entityType>_<attrName>_<attrType>");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-attribute";
        NGSIMongoSink sink = new NGSIMongoSink();
        sink.configure(CommonUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel));
        String dbName = "sth_default";
        String fiwareService = "default";
        String fiwareServicePath = "/";
        String entity = "someId_someType";
        String attribute = "someName_someType";
        boolean isAggregated = false;
        String entityId = "someId";
        String entityType = "someType";
        
        try {
            String collectionName = sink.buildCollectionName(dbName, fiwareServicePath, entity, attribute,
                    isAggregated, entityId, entityType, fiwareService);
            
            try {
                assertEquals("sth_/_someId_someType_someName_someType", collectionName);
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "-  OK  - '" + collectionName + "' was crated as collection name");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - '" + collectionName + "' was crated as collection name instead of "
                        + "'sth_/_someId_someType_someName_someType'");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - There was some problem when building the collection name");
            assertTrue(false);
        } // catch
    } // testBuildCollectionNameDMByEntityRootServicePath
    
} // NGSIMongoBaseSinkTest
