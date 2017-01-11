/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
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

import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.NGSICharsets;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
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
        void persistBatch(NGSIBatch batch) throws CygnusPersistenceError {
            throw new UnsupportedOperationException("Not supported yet.");
        } // persistBatch

        @Override
        public void capRecords(NGSIBatch batch, long maxRecords) throws CygnusCappingError {
            throw new UnsupportedOperationException("Not supported yet.");
        } // capRecords

        @Override
        public void expirateRecords(long expirationTime) throws CygnusExpiratingError {
            throw new UnsupportedOperationException("Not supported yet.");
        } // expirateRecords
        
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
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                + "-------- Configured 'collection_prefix' cannot be 'system.'");
        String collectionPrefix = "system.";
        String dbPrefix = "sth_";
        String dataModel = null; // default
        String enableEncoding = null; // default
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        
        try {
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                    + "-  OK  - 'system.' value detected for 'collection_prefix'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                    + "- FAIL - 'system.' value not detected for 'collection_prefix'");
            throw e;
        } // try catch
    } // testConfigureCollectionPrefixIsNotSystem
    
    /**
     * [NGSIMongoSink.configure] -------- Configured 'collection_prefix' is encoded when having forbidden characters.
     */
    @Test
    public void testConfigureCollectionPrefixIsEncodedOldEncoding() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                + "-------- Configured 'collection_prefix' is encoded when having forbidden characters");
        String collectionPrefix = "this\\is/a$prefix.with-forbidden,chars:-.";
        String dbPrefix = "sth_";
        String dataModel = null; // default
        String enableEncoding = "false";
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String expectedCollectionPrefix = NGSIUtils.encodeSTHCollection(collectionPrefix);
        
        try {
            assertEquals(expectedCollectionPrefix, sink.collectionPrefix);
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                    + "-  OK  - 'collection_prefix=" + collectionPrefix
                    + "' correctly encoded as '" + expectedCollectionPrefix + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                    + "- FAIL - 'collection_prefix=" + collectionPrefix
                    + "' wrongly encoded as '" + expectedCollectionPrefix + "'");
            throw e;
        } // try catch // try catch
    } // testConfigureCollectionPrefixIsEncodedOldEncoding
    
    /**
     * [NGSIMongoSink.configure] -------- Configured 'collection_prefix' is encoded when having forbidden characters.
     */
    @Test
    public void testConfigureCollectionPrefixIsEncodedNewEncoding() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                + "-------- Configured 'collection_prefix' is encoded when having forbidden characters");
        String collectionPrefix = "this\\is/a$prefix.with-forbidden,chars:-.";
        String dbPrefix = "sth_";
        String dataModel = null; // default
        String enableEncoding = "true";
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String expectedCollectionPrefix = NGSICharsets.encodeMongoDBCollection(collectionPrefix);
        
        try {
            assertEquals(expectedCollectionPrefix, sink.collectionPrefix);
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                    + "-  OK  - 'collection_prefix=" + collectionPrefix
                    + "' correctly encoded as '" + expectedCollectionPrefix + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                    + "- FAIL - 'collection_prefix=" + collectionPrefix
                    + "' wrongly encoded as '" + expectedCollectionPrefix + "'");
            throw e;
        } // try catch // try catch
    } // testConfigureCollectionPrefixIsEncodedNewEncoding
    
    /**
     * [NGSIMongoSink.configure] -------- Configured 'db_prefix' is encoded when having forbidden characters.
     */
    @Test
    public void testConfigureDBPrefixIsEncodedOldEncoding() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                + "-------- Configured 'db_prefix' is encoded when having forbidden characters");
        String collectionPrefix = "sth_";
        String dbPrefix = "this\\is/a$prefix.with forbidden\"chars:-,";
        String dataModel = null; // defaulting
        String enableEncoding = "false";
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String expectedDbPrefix = NGSIUtils.encodeSTHDB(dbPrefix);
        
        try {
            assertEquals(expectedDbPrefix, sink.dbPrefix);
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                    + "-  OK  - 'db_prefix=" + dbPrefix + "' correctly encoded as '" + expectedDbPrefix + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                    + "- FAIL - 'db_prefix=" + dbPrefix + "' wrongly encoded as '" + expectedDbPrefix + "'");
            throw e;
        } // try catch // try catch
    } // testConfigureDBPrefixIsEncodedOldEncoding
    
    /**
     * [NGSIMongoSink.configure] -------- Configured 'db_prefix' is encoded when having forbidden characters.
     */
    @Test
    public void testConfigureDBPrefixIsEncodedNewEncoding() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                + "-------- Configured 'db_prefix' is encoded when having forbidden characters");
        String collectionPrefix = "sth_";
        String dbPrefix = "this\\is/a$prefix.with forbidden\"chars:-,";
        String dataModel = null; // defaulting
        String enableEncoding = "true";
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String expectedDbPrefix = NGSICharsets.encodeMongoDBDatabase(dbPrefix);
        
        try {
            assertEquals(expectedDbPrefix, sink.dbPrefix);
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                    + "-  OK  - 'db_prefix=" + dbPrefix + "' correctly encoded as '" + expectedDbPrefix + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.configure]")
                    + "- FAIL - 'db_prefix=" + dbPrefix + "' wrongly encoded as '" + expectedDbPrefix + "'");
            throw e;
        } // try catch // try catch
    } // testConfigureDBPrefixIsEncodedNewEncoding
    
    /**
     * [NGSIMongoBaseSink.buildCollectionName] -------- When / service-path is notified/defaulted and
     * data_model=dm-by-service-path, the MongoDB collection name is the concatenation of \<prefix\> and
     * \<service-path\>.
     */
    @Test
    public void testBuildCollectionNameDMByServicePathRootServicePathOldEncoding() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When / service-path is notified/defaulted and data_model=dm-by-service-path, "
                + "the MongoDB collection name is the concatenation of <prefix> and <service-path>");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-service-path";
        String enableEncoding = "false";
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String fiwareServicePath = "/";
        String entity = "someId=someType";
        String attribute = "someName";
        
        try {
            String collectionName = sink.buildCollectionName(fiwareServicePath, entity, attribute);
            String expectedCollectionName = "sth_/";
            
            try {
                assertEquals(expectedCollectionName, collectionName);
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "-  OK  - '" + collectionName + "' was created as collection name");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - '" + collectionName + "' was created as collection name instead of 'sth_/'");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - There was some problem when building the collection name");
            assertTrue(false);
        } // catch
    } // testBuildCollectionNameDMByServicePathRootServicePathOldEncoding
    
    /**
     * [NGSIMongoBaseSink.buildCollectionName] -------- When / service-path is notified/defaulted and
     * data_model=dm-by-service-path, the MongoDB collection name is the concatenation of \<prefix\> and
     * \<service-path\>.
     */
    @Test
    public void testBuildCollectionNameDMByServicePathRootServicePathNewEncoding() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When / service-path is notified/defaulted and data_model=dm-by-service-path, "
                + "the MongoDB collection name is the concatenation of <prefix> and <service-path>");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-service-path";
        String enableEncoding = "true";
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String fiwareServicePath = "/";
        String entity = "someId=someType";
        String attribute = "someName";
        
        try {
            String collectionName = sink.buildCollectionName(fiwareServicePath, entity, attribute);
            String expectedCollectionName = "sth_x002f";
            
            try {
                assertEquals(expectedCollectionName, collectionName);
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "-  OK  - '" + collectionName + "' was created as collection name");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - '" + collectionName + "' was created as collection name instead of 'sth_/'");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - There was some problem when building the collection name");
            assertTrue(false);
        } // catch
    } // testBuildCollectionNameDMByServicePathRootServicePathNewEncoding
    
    /**
     * [NGSIMongoBaseSink.buildCollectionName] -------- When / service-path is notified/defaulted and
     * data_model=dm-by-entity, the MongoDB collections name is the concatenation of the \<prefix\>, \<service-path\>,
     * \<entityId\> and \<entityType\>.
     */
    @Test
    public void testBuildCollectionNameDMByEntityRootServicePathOldEncoding() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When / service-path is notified/defaulted and data_model=dm-by-entity, the MongoDB"
                + "collections name is the concatenation of the <prefix>, <service-path>, <entityId> and <entityType>");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-entity";
        String enableEncoding = "false";
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String fiwareServicePath = "/";
        String entity = "someId=someType";
        String attribute = "someName";
        
        try {
            String collectionName = sink.buildCollectionName(fiwareServicePath, entity, attribute);
            String expectedCollectionName = "sth_/_someId_someType";
            
            try {
                assertEquals(expectedCollectionName, collectionName);
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "-  OK  - '" + collectionName + "' was created as collection name");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - '" + collectionName + "' was created as collection name instead of "
                        + "'sth_/_someId_someType'");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - There was some problem when building the collection name");
            assertTrue(false);
        } // catch
    } // testBuildCollectionNameDMByEntityRootServicePathOldEncoding
    
    /**
     * [NGSIMongoBaseSink.buildCollectionName] -------- When / service-path is notified/defaulted and
     * data_model=dm-by-entity, the MongoDB collections name is the concatenation of the \<prefix\>, \<service-path\>,
     * \<entityId\> and \<entityType\>.
     */
    @Test
    public void testBuildCollectionNameDMByEntityRootServicePathNewEncoding() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When / service-path is notified/defaulted and data_model=dm-by-entity, the MongoDB"
                + "collections name is the concatenation of the <prefix>, <service-path>, <entityId> and <entityType>");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-entity";
        String enableEncoding = "true";
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String fiwareServicePath = "/";
        String entity = "someId=someType";
        String attribute = "someName";
        
        try {
            String collectionName = sink.buildCollectionName(fiwareServicePath, entity, attribute);
            String expectedCollectionName = "sth_x002fxffffsomeIdxffffsomeType";
            
            try {
                assertEquals(expectedCollectionName, collectionName);
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "-  OK  - '" + collectionName + "' was created as collection name");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - '" + collectionName + "' was created as collection name instead of "
                        + "'sth_/_someId_someType'");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - There was some problem when building the collection name");
            assertTrue(false);
        } // catch
    } // testBuildCollectionNameDMByEntityRootServicePathNewEncoding
    
    /**
     * [NGSIMongoBaseSink.buildCollectionName] -------- When / service-path is notified/defaulted and
     * data_model=dm-by-attribute, the MongoDB collections name is the concatenation of \<prefix\>, \<service-path\>,
     * \<entityId\>, \<entityType\> and \<attrName\>.
     */
    @Test
    public void testBuildCollectionNameDMByAttributeRootServicePathOldEncoding() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When / service-path is notified/defaulted and data_model=dm-by-attribute, the "
                + "MongoDB collections name is the concatenation of <prefix>, <service-path>, <entityId>, <entityType> "
                + "and <attrName>");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-attribute";
        String enableEncoding = "false";
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String fiwareServicePath = "/";
        String entity = "someId=someType";
        String attribute = "someName";
        
        try {
            String collectionName = sink.buildCollectionName(fiwareServicePath, entity, attribute);
            String expectedCollectionName = "sth_/_someId_someType_someName";
            
            try {
                assertEquals(expectedCollectionName, collectionName);
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "-  OK  - '" + collectionName + "' was created as collection name");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - '" + collectionName + "' was created as collection name instead of "
                        + "'sth_/_someId_someType_someName_someType'");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - There was some problem when building the collection name");
            assertTrue(false);
        } // catch
    } // testBuildCollectionNameDMByEntityRootServicePathOldEncoding
    
    /**
     * [NGSIMongoBaseSink.buildCollectionName] -------- When / service-path is notified/defaulted and
     * data_model=dm-by-attribute, the MongoDB collections name is the concatenation of \<prefix\>, \<service-path\>,
     * \<entityId\>, \<entityType\> and \<attrName\>.
     */
    @Test
    public void testBuildCollectionNameDMByAttributeRootServicePathNewEncoding() {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When / service-path is notified/defaulted and data_model=dm-by-attribute, the "
                + "MongoDB collections name is the concatenation of <prefix>, <service-path>, <entityId>, <entityType> "
                + "and <attrName>");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-attribute";
        String enableEncoding = "true";
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String fiwareServicePath = "/";
        String entity = "someId=someType";
        String attribute = "someName";
        
        try {
            String collectionName = sink.buildCollectionName(fiwareServicePath, entity, attribute);
            String expectedCollectionName = "sth_x002fxffffsomeIdxffffsomeTypexffffsomeName";
            
            try {
                assertEquals(expectedCollectionName, collectionName);
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "-  OK  - '" + collectionName + "' was created as collection name");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - '" + collectionName + "' was created as collection name instead of "
                        + "'sth_/_someId_someType_someName_someType'");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                        + "- FAIL - There was some problem when building the collection name");
            assertTrue(false);
        } // catch
    } // testBuildCollectionNameDMByEntityRootServicePathNewEncoding
    
    /**
     * [NGSIMongoBaseSink.buildDbName] -------- A database name length greater than 113 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildDbNameLength() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildDbName]")
                + "-------- A database name length greater than 113 characters is detected");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = null; // default
        String enableEncoding = null; // default
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String service = "tooLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "oooooooooooooooooooongService";
        
        try {
            sink.buildDbName(service);
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildDbName]")
                    + "- FAIL - A database name length greater than 113 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildDbName]")
                    + "-  OK  - A database name length greater than 113 characters has been detected");
        } // try catch
    } // testBuildDbNameLength
    
    /**
     * [NGSIMongoBaseSink.buildCollectionName] -------- When data model is by service path, a collection name length
     * greater than 113 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildCollectionNameLengthDataModelByServicePath() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When data model is by service path, a collection name length greater than 113 characters "
                + "is detected");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-service-path";
        String enableEncoding = null; // default
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String servicePath = "/tooLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "oooooooooooooooooooooooooooongServicePath";
        String entity = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test
        
        try {
            sink.buildCollectionName(servicePath, entity, attribute);
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                    + "- FAIL - A collection name length greater than 113 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                    + "-  OK  - A collection name length greater than 113 characters has been detected");
        } // try catch
    } // testBuildCollectionNameLengthDataModelByServicePath
    
    /**
     * [NGSIMongoBaseSink.buildCollectionName] -------- When data model is by entity, a collection name length greater
     * than 113 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildCollectionNameLengthDataModelByEntity() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When data model is by entity, a collection name length greater than 113 characters is "
                + "detected");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-entity";
        String enableEncoding = null; // default
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String servicePath = "/tooLooooooooooooooooooooooooooooooooooooooooooooooooooooongServicePath";
        String entity = "tooLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooongEntity";
        String attribute = null; // irrelevant for this test
        
        try {
            sink.buildCollectionName(servicePath, entity, attribute);
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                    + "- FAIL - A collection name length greater than 113 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                    + "-  OK  - A collection name length greater than 113 characters has been detected");
        } // try catch
    } // testBuildCollectionNameLengthDataModelByEntity
    
    /**
     * [NGSIMongoBaseSink.buildCollectionName] -------- When data model is by attribute, a collection name length
     * greater than 113 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildCollectionNameLengthDataModelByAttribute() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                + "-------- When data model is by atribute, a collection name length greater than 113 characters is "
                + "detected");
        String collectionPrefix = "sth_";
        String dbPrefix = "sth_";
        String dataModel = "dm-by-attribute";
        String enableEncoding = null; // default
        NGSIMongoBaseSinkImpl sink = new NGSIMongoBaseSinkImpl();
        sink.configure(NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding));
        String servicePath = "/tooLoooooooooooooooooooooooooooooooooooongServicePath";
        String entity = "tooLoooooooooooooooooooooooooooooooooooooooooongEntity";
        String attribute = "tooLoooooooooooooooooooooooooooooooooooooooongAttribute";
        
        try {
            sink.buildCollectionName(servicePath, entity, attribute);
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                    + "- FAIL - A collection name length greater than 113 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIMongoBaseSink.buildCollectionName]")
                    + "-  OK  - A collection name length greater than 113 characters has been detected");
        } // try catch
    } // testBuildCollectionNameLengthDataModelByAttribute
    
} // NGSIMongoBaseSinkTest
