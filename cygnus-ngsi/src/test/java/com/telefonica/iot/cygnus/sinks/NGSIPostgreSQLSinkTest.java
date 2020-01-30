/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
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

import com.google.gson.JsonPrimitive;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericAggregator;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;

import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hermanjunge
 */
@RunWith(MockitoJUnitRunner.class)
public class NGSIPostgreSQLSinkTest {

    /**
     * Constructor.
     */
    public NGSIPostgreSQLSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSIPostgreSQLSinkTest

    /**
     * [NGSIPostgreSQLSink.configure] -------- enable_encoding can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableEncoding() {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                + "-------- enable_encoding can only be 'true' or 'false'");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "falso";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                    + "-  OK  - 'enable_encoding=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                    + "- FAIL - 'enable_encoding=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableEncoding
    
    /**
     * [NGSIPostgreSQLSink.configure] -------- enable_lowercase can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableLowercase() {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                + "-------- enable_lowercase can only be 'true' or 'false'");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = "falso";
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                    + "-  OK  - 'enable_lowercase=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                    + "- FAIL - 'enable_lowercase=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableLowercase
    
    /**
     * [NGSIPostgreSQLSink.configure] -------- enable_grouping can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableGrouping() {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                + "-------- enable_grouping can only be 'true' or 'false'");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = "falso";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                    + "-  OK  - 'enable_grouping=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                    + "- FAIL - 'enable_grouping=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableGrouping
    
    /**
     * [NGSIPostgreSQLSink.configure] -------- data_model can only be 'dm-by-service-path' or 'dm-by-entity'.
     */
    // TBD: check for dataModel values in NGSIMySQLSink and uncomment this test.
    // @Test
    public void testConfigureDataModel() {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                + "-------- data_model can only be 'dm-by-service-path' or 'dm-by-entity'");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-service";
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                    + "-  OK  - 'data_model=dm-by-service' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                    + "- FAIL - 'data_model=dm-by-service' was not detected");
            throw e;
        } // try catch
    } // testConfigureDataModel
    
    /**
     * [NGSIPostgreSQLSink.configure] -------- attr_persistence can only be 'row' or 'column'.
     */
    @Test
    public void testConfigureAttrPersistence() {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                + "-------- attr_persistence can only be 'row' or 'column'");
        String attrPersistence = "fila";
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                    + "-  OK  - 'attr_persistence=fila' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                    + "- FAIL - 'attr_persistence=fila' was not detected");
            throw e;
        } // try catch
    } // testConfigureAttrPersistence
    
    /**
     * [NGSIPostgreSQLSink.buildDBName] -------- The schema name is equals to the encoding of the notified/defaulted
     * service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildDBNameOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildDBName]")
                + "-------- The schema name is equals to the encoding of the notified/defaulted service");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "false";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String service = "someService";
        
        try {
            String builtSchemaName = sink.buildSchemaName(service);
            String expectedDBName = "someService";
        
            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildDBName]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildDBName]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildDBName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildDBNameOldEncoding
    
    /**
     * [NGSIPostgreSQLSink.buildDBName] -------- The schema name is equals to the encoding of the notified/defaulted
     * service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildDBNameNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildDBName]")
                + "-------- The schema name is equals to the encoding of the notified/defaulted service");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "true";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String service = "someService";
        
        try {
            String builtSchemaName = sink.buildSchemaName(service);
            String expectedDBName = "somex0053ervice";
        
            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildDBName]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildDBName]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildDBName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildDBNameNewEncoding
    
    /**
     * [NGSIPostgreSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-service-path' the MySQL table name is the encoding of <service-path>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByServicePathOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the MySQL table name is the encoding of <service-path>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-service-path";
        String enableEncoding = "false";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String servicePath = "/somePath";
        String entity = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test
        
        try {
            String builtTableName = sink.buildTableName(servicePath, entity, attribute);
            String expecetedTableName = "somePath";
        
            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByServicePathOldEncoding
    
    /**
     * [NGSIPostgreSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-service-path' the MySQL table name is the encoding of <service-path>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByServicePathNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the MySQL table name is the encoding of <service-path>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-service-path";
        String enableEncoding = "true";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String servicePath = "/somePath";
        String entity = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test
        
        try {
            String builtTableName = sink.buildTableName(servicePath, entity, attribute);
            String expecetedTableName = "x002fsomex0050ath";
        
            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByServicePathNewEncoding
    
    /**
     * [NGSIPostgreSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByEntityOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the MySQL table name is the encoding of the concatenation of <service-path>, "
                + "<entityId> and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity";
        String enableEncoding = "false";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String servicePath = "/somePath";
        String entity = "someId=someType";
        String attribute = null; // irrelevant for this test
        
        try {
            String builtTableName = sink.buildTableName(servicePath, entity, attribute);
            String expecetedTableName = "somePath_someId_someType";
        
            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityOldEncoding
    
    /**
     * [NGSIPostgreSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByEntityNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the MySQL table name is the encoding of the concatenation of <service-path>, "
                + "<entityId> and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity";
        String enableEncoding = "true";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String servicePath = "/somePath";
        String entity = "someId=someType";
        String attribute = null; // irrelevant for this test
        
        try {
            String builtTableName = sink.buildTableName(servicePath, entity, attribute);
            String expecetedTableName = "x002fsomex0050athxffffsomex0049dxffffsomex0054ype";
        
            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityNewEncoding
    
    /**
     * [NGSIPostgreSQLSink.buildTableName] -------- When a root service-path is notified/defaulted and
     * data_model is 'dm-by-service-path' the MySQL table name cannot be built.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByServicePathOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                + "-------- When a root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the MySQL table name cannot be built");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-service-path";
        String enableEncoding = "false";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String servicePath = "/";
        String entity = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test
        
        try {
            sink.buildTableName(servicePath, entity, attribute);
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "- FAIL - The root service path was not detected as not valid");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "-  OK  - The root service path was detected as not valid");
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByServicePathOldEncoding
    
    /**
     * [NGSIPostgreSQLSink.buildTableName] -------- When a root service-path is notified/defaulted and
     * data_model is 'dm-by-service-path' the MySQL table name is the encoding of \<service-path\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByServicePathNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                + "-------- When a root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the MySQL table name is the encoding of <service-path>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-service-path";
        String enableEncoding = "true";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String servicePath = "/";
        String entity = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test
        
        try {
            String builtTableName = sink.buildTableName(servicePath, entity, attribute);
            String expecetedTableName = "x002f";
        
            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByServicePathNewEncoding
    
    /**
     * [NGSIPostgreSQLSink.buildTableName] -------- When a root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entityId\> and \<entityType\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByEntityOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                + "-------- When a root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the MySQL table name is the encoding of the concatenation of <service-path>, "
                + "<entityId> and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity";
        String enableEncoding = "false";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String servicePath = "/";
        String entity = "someId=someType";
        String attribute = null; // irrelevant for this test
        
        try {
            String builtTableName = sink.buildTableName(servicePath, entity, attribute);
            String expecetedTableName = "someId_someType";
        
            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByEntityOldencoding
    
    /**
     * [NGSIPostgreSQLSink.buildTableName] -------- When a root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entityId\> and \<entityType\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByEntityNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                + "-------- When a root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the MySQL table name is the encoding of the concatenation of <service-path>, "
                + "<entityId> and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity";
        String enableEncoding = "true";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String servicePath = "/";
        String entity = "someId=someType";
        String attribute = null; // irrelevant for this test
        
        try {
            String builtTableName = sink.buildTableName(servicePath, entity, attribute);
            String expecetedTableName = "x002fxffffsomex0049dxffffsomex0054ype";
        
            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByEntityNewEncoding
    
    /**
     * [NGSIPostgreSQLSink.buildSchemaName] -------- A schema name length greater than 63 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildSchemaNameLength() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildSchemaName]")
                + "-------- A schema name length greater than 63 characters is detected");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String service = "tooLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooongService";
        
        try {
            sink.buildSchemaName(service);
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildSchemaName]")
                    + "- FAIL - A schema name length greater than 63 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildSchemaName]")
                    + "-  OK  - A schema name length greater than 63 characters has been detected");
        } // try catch
    } // testBuildSchemaNameLength
    
    /**
     * [NGSIPostgreSQLSink.buildTableName] -------- When data model is by service path, a table name length greater
     * than 63 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByServicePath() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                + "-------- When data model is by service path, a table name length greater than 63 characters is "
                + "detected");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-service-path";
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String servicePath = "/tooLooooooooooooooooooooooooooooooooooooooooooooooooooooooongServicePath";
        String entity = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test
        
        try {
            sink.buildTableName(servicePath, entity, attribute);
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "- FAIL - A table name length greater than 63 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "-  OK  - A table name length greater than 63 characters has been detected");
        } // try catch
    } // testBuildTableNameLengthDataModelByServicePath
    
    /**
     * [NGSICartoDBSink.buildTableName] -------- When data model is by entity, a table name length greater than 63
     * characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByEntity() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                + "-------- When data model is by entity, a table name length greater than 63 characters is detected");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity";
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String servicePath = "/tooLooooooooooooooooooooongServicePath";
        String entity = "tooLooooooooooooooooooooooooooongEntity";
        String attribute = null; // irrelevant for this test
        
        try {
            sink.buildTableName(servicePath, entity, attribute);
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "- FAIL - A table name length greater than 63 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "-  OK  - A table name length greater than 63 characters has been detected");
        } // try catch
    } // testBuildTableNameLengthDataModelByEntity
    
    /**
     * [NGSICartoDBSink.buildTableName] -------- When data model is by attribute, a table name length greater than 63
     * characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByAttribute() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                + "-------- When data model is by atribute, a table name length greater than 63 characters is "
                + "detected");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-attribute";
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String servicePath = "/tooLooooooooooooooongServicePath";
        String entity = "tooLooooooooooooooooooongEntity";
        String attribute = "tooLooooooooooooongAttribute";
        
        try {
            sink.buildTableName(servicePath, entity, attribute);
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "- FAIL - A table name length greater than 63 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                    + "-  OK  - A table name length greater than 63 characters has been detected");
        } // try catch
    } // testBuildTableNameLengthDataModelByAttribute
    
    /**
     * [NGSIPostgreSQLSink.configure] -------- cache can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureCache() {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                + "-------- cache can only be 'true' or 'false'");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null;
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = "falso";
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                    + "-  OK  - 'enable_cache=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                    + "- FAIL - 'enable_cache=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableEncoding

    @Test
    public void testNativeTypeColumn() throws CygnusBadConfiguration {
        String attr_native_types = "true"; // default
        NGSIPostgreSQLSink ngsiPostgreSQLSink = new NGSIPostgreSQLSink();
        ngsiPostgreSQLSink.configure(createContextforNativeTypes(null, null, null, null, null, null, null, null, null, null, null, null, null, attr_native_types));
        // Create a NGSIEvent
        String timestamp = "1461136795801";
        String correlatorId = "123456789";
        String transactionId = "123456789";
        String originalService = "someService";
        String originalServicePath = "somePath";
        String mappedService = "newService";
        String mappedServicePath = "newPath";
        Map<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
        headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
        headers.put(NGSIConstants.FLUME_HEADER_TRANSACTION_ID, transactionId);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);

        NGSIPostgreSQLSink.ColumnAggregator columnAggregator = ngsiPostgreSQLSink.new ColumnAggregator(false, false,false,false);
        NotifyContextRequest.ContextElement contextElement = createContextElementForNativeTypes();
        NGSIEvent ngsiEvent = new NGSIEvent(headers, contextElement.toString().getBytes(), contextElement, null);
        columnAggregator.initialize(ngsiEvent);
        columnAggregator.aggregate(ngsiEvent);
        if (columnAggregator.getValuesForInsert().contains("2,'[]'")  &&
                columnAggregator.getValuesForInsert().contains("TRUE,'[]'")  &&
                columnAggregator.getValuesForInsert().contains("'2016-09-21T01:23:00.00Z'")  &&
                columnAggregator.getValuesForInsert().contains("{\"type\": \"Point\",\"coordinates\": [-0.036177,39.986159]}")  &&
                columnAggregator.getValuesForInsert().contains("{\"String\": \"string\"}")  &&
                columnAggregator.getValuesForInsert().contains("foo")) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.testNativeTypesColumn]")
                    + "-  OK  - NativeTypesOK");
            assertTrue(true);
        } else {
            System.out.println(columnAggregator.getValuesForInsert());
            assertFalse(true);
        }
    }

    @Test
    public void testNativeTypeRow() throws CygnusBadConfiguration {
        String attr_native_types = "true"; // default
        NGSIPostgreSQLSink ngsiPostgreSQLSink = new NGSIPostgreSQLSink();
        ngsiPostgreSQLSink.configure(createContextforNativeTypes(null, null, null, null, null, null, null, null, null, null, null, null, null, attr_native_types));
        // Create a NGSIEvent
        String timestamp = "1461136795801";
        String correlatorId = "123456789";
        String transactionId = "123456789";
        String originalService = "someService";
        String originalServicePath = "somePath";
        String mappedService = "newService";
        String mappedServicePath = "newPath";
        Map<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
        headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
        headers.put(NGSIConstants.FLUME_HEADER_TRANSACTION_ID, transactionId);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        NGSIPostgreSQLSink.RowAggregator rowAggregator = ngsiPostgreSQLSink.new RowAggregator(false, false,false,false);
        NotifyContextRequest.ContextElement contextElement = createContextElementForNativeTypes();
        NGSIEvent ngsiEvent = new NGSIEvent(headers, contextElement.toString().getBytes(), contextElement, null);
        rowAggregator.initialize(ngsiEvent);
        rowAggregator.aggregate(ngsiEvent);
        if (rowAggregator.getValuesForInsert().contains("'2','[]'")  &&
                rowAggregator.getValuesForInsert().contains("'true','[]'")  &&
                rowAggregator.getValuesForInsert().contains("'2016-09-21T01:23:00.00Z'")  &&
                rowAggregator.getValuesForInsert().contains("{\"type\": \"Point\",\"coordinates\": [-0.036177,39.986159]}")  &&
                rowAggregator.getValuesForInsert().contains("{\"String\": \"string\"}")  &&
                rowAggregator.getValuesForInsert().contains("foo")) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.testNativeTypesRow]")
                    + "-  OK  - NativeTypesOK");
            assertTrue(true);
        } else {
            assertFalse(true);
        }
    }

    private Context createContext(String attrPersistence, String batchSize, String batchTime, String batchTTL,
            String dataModel, String enableEncoding, String enableGrouping, String enableLowercase, String host,
            String password, String port, String username, String cache) {
        Context context = new Context();
        context.put("attr_persistence", attrPersistence);
        context.put("batch_size", batchSize);
        context.put("batch_time", batchTime);
        context.put("batch_ttl", batchTTL);
        context.put("data_model", dataModel);
        context.put("enable_encoding", enableEncoding);
        context.put("enable_grouping", enableGrouping);
        context.put("enable_lowercase", enableLowercase);
        context.put("mysql_host", host);
        context.put("mysql_password", password);
        context.put("mysql_port", port);
        context.put("mysql_username", username);
        context.put("backend.enable_cache", cache);
        return context;
    } // createContext

    private Context createContextforNativeTypes(String attrPersistence, String batchSize, String batchTime, String batchTTL,
                                                String dataModel, String enableEncoding, String enableGrouping, String enableLowercase, String host,
                                                String password, String port, String username, String cache, String attrNativeTypes) {
        Context context = new Context();
        context.put("attr_persistence", attrPersistence);
        context.put("batch_size", batchSize);
        context.put("batch_time", batchTime);
        context.put("batch_ttl", batchTTL);
        context.put("data_model", dataModel);
        context.put("enable_encoding", enableEncoding);
        context.put("enable_grouping", enableGrouping);
        context.put("enable_lowercase", enableLowercase);
        context.put("mysql_host", host);
        context.put("mysql_password", password);
        context.put("mysql_port", port);
        context.put("mysql_username", username);
        context.put("backend.enable_cache", cache);
        context.put("attr_native_types", attrNativeTypes);
        return context;
    } // createContextforNativeTypes

    private NotifyContextRequest.ContextElement createContextElementForNativeTypes() {
        NotifyContextRequest notifyContextRequest = new NotifyContextRequest();
        NotifyContextRequest.ContextMetadata contextMetadata = new NotifyContextRequest.ContextMetadata();
        contextMetadata.setName("someString");
        contextMetadata.setType("string");
        ArrayList<NotifyContextRequest.ContextMetadata> metadata = new ArrayList<>();
        metadata.add(contextMetadata);
        NotifyContextRequest.ContextAttribute contextAttribute1 = new NotifyContextRequest.ContextAttribute();
        contextAttribute1.setName("someNumber");
        contextAttribute1.setType("number");
        contextAttribute1.setContextValue(new JsonPrimitive(2));
        contextAttribute1.setContextMetadata(null);
        NotifyContextRequest.ContextAttribute contextAttribute2 = new NotifyContextRequest.ContextAttribute();
        contextAttribute2.setName("somneBoolean");
        contextAttribute2.setType("Boolean");
        contextAttribute2.setContextValue(new JsonPrimitive(true));
        contextAttribute2.setContextMetadata(null);
        NotifyContextRequest.ContextAttribute contextAttribute3 = new NotifyContextRequest.ContextAttribute();
        contextAttribute3.setName("someDate");
        contextAttribute3.setType("DateTime");
        contextAttribute3.setContextValue(new JsonPrimitive("2016-09-21T01:23:00.00Z"));
        contextAttribute3.setContextMetadata(null);
        NotifyContextRequest.ContextAttribute contextAttribute4 = new NotifyContextRequest.ContextAttribute();
        contextAttribute4.setName("someGeoJson");
        contextAttribute4.setType("geo:json");
        contextAttribute4.setContextValue(new JsonPrimitive("{\"type\": \"Point\",\"coordinates\": [-0.036177,39.986159]}"));
        contextAttribute4.setContextMetadata(null);
        NotifyContextRequest.ContextAttribute contextAttribute5 = new NotifyContextRequest.ContextAttribute();
        contextAttribute5.setName("someJson");
        contextAttribute5.setType("json");
        contextAttribute5.setContextValue(new JsonPrimitive("{\"String\": \"string\"}"));
        contextAttribute5.setContextMetadata(null);
        NotifyContextRequest.ContextAttribute contextAttribute6 = new NotifyContextRequest.ContextAttribute();
        contextAttribute6.setName("someString");
        contextAttribute6.setType("string");
        contextAttribute6.setContextValue(new JsonPrimitive("foo"));
        contextAttribute6.setContextMetadata(null);
        NotifyContextRequest.ContextAttribute contextAttribute7 = new NotifyContextRequest.ContextAttribute();
        contextAttribute7.setName("someString2");
        contextAttribute7.setType("string");
        contextAttribute7.setContextValue(new JsonPrimitive(""));
        contextAttribute7.setContextMetadata(null);
        ArrayList<NotifyContextRequest.ContextAttribute> attributes = new ArrayList<>();
        attributes.add(contextAttribute1);
        attributes.add(contextAttribute2);
        attributes.add(contextAttribute3);
        attributes.add(contextAttribute4);
        attributes.add(contextAttribute5);
        attributes.add(contextAttribute6);
        attributes.add(contextAttribute7);
        NotifyContextRequest.ContextElement contextElement = new NotifyContextRequest.ContextElement();
        contextElement.setId("someId");
        contextElement.setType("someType");
        contextElement.setIsPattern("false");
        contextElement.setAttributes(attributes);
        return contextElement;
    } // createContextElementForNativeTypes

    @Test
    public void testNativeTypeColumnBatch() throws CygnusBadConfiguration{
        String attr_native_types = "true"; // default
        NGSIPostgreSQLSink ngsiPostgreSQLSink = new NGSIPostgreSQLSink();
        ngsiPostgreSQLSink.configure(createContextforNativeTypes("column", null, null, null, null, null, null, null, null, null, null, null, null, attr_native_types));
        // Create a NGSIEvent
        String timestamp = "1461136795801";
        String correlatorId = "123456789";
        String transactionId = "123456789";
        String originalService = "someService";
        String originalServicePath = "somePath";
        String mappedService = "newService";
        String mappedServicePath = "newPath";
        String destination = "someDestination";
        Map<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
        headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
        headers.put(NGSIConstants.FLUME_HEADER_TRANSACTION_ID, transactionId);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        NotifyContextRequest.ContextElement contextElement = createContextElementForNativeTypes();
        NotifyContextRequest.ContextElement contextElement2 = createContextElement();
        NGSIEvent ngsiEvent = new NGSIEvent(headers, contextElement.toString().getBytes(), contextElement, null);
        NGSIEvent ngsiEvent2 = new NGSIEvent(headers, contextElement2.toString().getBytes(), contextElement2, null);
        NGSIBatch batch = new NGSIBatch();
        batch.addEvent(destination, ngsiEvent);
        batch.addEvent(destination, ngsiEvent2);
        try {
            batch.startIterator();
            while (batch.hasNext()) {
                destination = batch.getNextDestination();
                ArrayList<NGSIEvent> events = batch.getNextEvents();
                NGSIGenericAggregator aggregator = ngsiPostgreSQLSink.getAggregator(false);
                aggregator.initialize(events.get(0));
                for (NGSIEvent event : events) {
                    aggregator.aggregate(event);
                } // for
                String correctBatch = "('2016-04-20 07:19:55.801','somePath','someId','someType',2,'[]',TRUE,'[]','2016-09-21T01:23:00.00Z','[]',ST_GeomFromGeoJSON('\"{\"type\": \"Point\",\"coordinates\": [-0.036177,39.986159]}\"'),'[]','{\"String\": \"string\"}','[]','foo','[]','','[]',NULL,NULL,NULL,NULL),('2016-04-20 07:19:55.801','somePath','someId','someType',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,ST_SetSRID(ST_MakePoint(\"-3.7167::double precision , 40.3833\"::double precision ), 4326),'[{\"name\":\"location\",\"type\":\"string\",\"value\":\"WGS84\"}]','someValue2','[]')";
                if (aggregator.getValuesForInsert().equals(correctBatch)) {
                    System.out.println(getTestTraceHead("[NGSIPostgreSQL.testNativeTypesColumnBatch]")
                            + "-  OK  - NativeTypesOK");
                    assertTrue(true);
                } else {
                    assertFalse(true);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            assertFalse(true);
        }
    }

    private NotifyContextRequest.ContextElement createContextElement() {
        NotifyContextRequest notifyContextRequest = new NotifyContextRequest();
        NotifyContextRequest.ContextMetadata contextMetadata = new NotifyContextRequest.ContextMetadata();
        contextMetadata.setName("location");
        contextMetadata.setType("string");
        contextMetadata.setContextMetadata(new JsonPrimitive("WGS84"));
        ArrayList<NotifyContextRequest.ContextMetadata> metadata = new ArrayList<>();
        metadata.add(contextMetadata);
        NotifyContextRequest.ContextAttribute contextAttribute1 = new NotifyContextRequest.ContextAttribute();
        contextAttribute1.setName("someName1");
        contextAttribute1.setType("geo:point");
        contextAttribute1.setContextValue(new JsonPrimitive("-3.7167, 40.3833"));
        contextAttribute1.setContextMetadata(metadata);
        NotifyContextRequest.ContextAttribute contextAttribute2 = new NotifyContextRequest.ContextAttribute();
        contextAttribute2.setName("someName2");
        contextAttribute2.setType("someType2");
        contextAttribute2.setContextValue(new JsonPrimitive("someValue2"));
        contextAttribute2.setContextMetadata(null);
        ArrayList<NotifyContextRequest.ContextAttribute> attributes = new ArrayList<>();
        attributes.add(contextAttribute1);
        attributes.add(contextAttribute2);
        NotifyContextRequest.ContextElement contextElement = new NotifyContextRequest.ContextElement();
        contextElement.setId("someId");
        contextElement.setType("someType");
        contextElement.setIsPattern("false");
        contextElement.setAttributes(attributes);
        return contextElement;
    } // createContextElement

} // NGSIPostgreSQLSinkTest
