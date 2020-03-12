/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

import com.google.gson.JsonPrimitive;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericColumnAggregator;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;

import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericAggregator;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author frb
 */
public class NGSIMySQLSinkTest {

    /**
     * Constructor.
     */
    public NGSIMySQLSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSIMySQLSinkTest

    /**
     * [NGSIMySQLSink.configure] -------- enable_encoding can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableEncoding() {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
                    + "-  OK  - 'enable_encoding=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
                    + "- FAIL - 'enable_encoding=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableEncoding

    /**
     * [NGSIMySQLSink.configure] -------- enable_lowercase can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableLowercase() {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
                    + "-  OK  - 'enable_lowercase=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
                    + "- FAIL - 'enable_lowercase=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableLowercase

    /**
     * [NGSIMySQLSink.configure] -------- enable_grouping can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableGrouping() {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
                    + "-  OK  - 'enable_grouping=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
                    + "- FAIL - 'enable_grouping=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableGrouping

    /**
     * [NGSIMySQLSink.configure] -------- data_model can only be 'dm-by-service-path' or 'dm-by-entity'.
     */
    // TBD: check for dataModel values in NGSIMySQLSink and uncomment this test.
    // @Test
    public void testConfigureDataModel() {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
                    + "-  OK  - 'data_model=dm-by-service' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
                    + "- FAIL - 'data_model=dm-by-service' was not detected");
            throw e;
        } // try catch
    } // testConfigureDataModel

    /**
     * [NGSIMySQLSink.configure] -------- attr_persistence can only be 'row' or 'column'.
     */
    @Test
    public void testConfigureAttrPersistence() {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
                    + "-  OK  - 'attr_persistence=fila' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.configure]")
                    + "- FAIL - 'attr_persistence=fila' was not detected");
            throw e;
        } // try catch
    } // testConfigureAttrPersistence

    /**
     * [NGSIMySQLSink.buildDBName] -------- When no encoding, the DB name is equals to the encoding of the
     * notified/defaulted service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildDBNameNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildDBName]")
                + "-------- When no encoding, the DB name is equals to the encoding of the notified/defaulted service");
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String service = "someService";

        try {
            String builtSchemaName = sink.buildDbName(service);
            String expectedDBName = "someService";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildDBName]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildDBName]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildDBName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildDBNameNoEncoding

    /**
     * [NGSIMySQLSink.buildDBName] -------- When encoding, the DB name is equals to the encoding of the
     * notified/defaulted service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildDBNameEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildDBName]")
                + "-------- When encoding, the DB name is equals to the encoding of the notified/defaulted service");
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String service = "someService";

        try {
            String builtSchemaName = sink.buildDbName(service);
            String expectedDBName = "someService";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildDBName]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildDBName]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildDBName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildDBNameEncoding

    /**
     * [NGSIMySQLSink.buildTableName] -------- When no encoding and when a non root service-path is notified/defaulted
     * and data_model is 'dm-by-service-path' the MySQL table name is the encoding of <service-path>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByServicePathNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When no encoding and when a non root service-path is notified/defaulted and data_model is "
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/somePath";
        String entity = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "somePath";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByServicePathNoEncoding

    /**
     * [NGSIMySQLSink.buildTableName] -------- When encoding and when a non root service-path is notified/defaulted and
     * data_model is 'dm-by-service-path' the MySQL table name is the encoding of <service-path>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByServicePathEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When encoding and when a non root service-path is notified/defaulted and data_model is "
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/somePath";
        String entity = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "x002fsomePath";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByServicePathEncoding

    /**
     * [NGSIMySQLSink.buildTableName] -------- When no encoding and when a non root service-path is notified/defaulted
     * and data_model is 'dm-by-entity' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByEntityNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When no encoding and when a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-entity' the MySQL table name is the encoding of the concatenation of <service-path>, "
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/somePath";
        String entity = "someId=someType";
        String attribute = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "somePath_someId_someType";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityNoEncoding
    /**
     * [NGSIMySQLSink.buildTableName] -------- When no encoding and when a non root service-path is notified/defaulted
     * and data_model is 'dm-by-entity-type' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByEntityTypeNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When no encoding and when a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-entity-type' the MySQL table name is the encoding of the concatenation of <service-path>, "
                + "<entityId> and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-type";
        String enableEncoding = "false";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/somePath";
        String entity = "someId";
        String attribute = null; // irrelevant for this test
        String entityType = "someType"; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "somePath_someType";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityTypeNoEncoding

    /**
     * [NGSIMySQLSink.buildTableName] -------- When encoding and when a non root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByEntityEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When encoding and when a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-entity' the MySQL table name is the encoding of the concatenation of <service-path>, "
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/somePath";
        String entity = "someId=someType";
        String attribute = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "x002fsomePathxffffsomeIdxffffsomeType";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityEncoding
    /**
     * [NGSIMySQLSink.buildTableName] -------- When encoding and when a non root service-path is notified/defaulted and
     * data_model is 'dm-by-entity-type' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByEntityTypeEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When encoding and when a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-entity-type' the MySQL table name is the encoding of the concatenation of <service-path>, "
                + "<entityId> and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-type";
        String enableEncoding = "true";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/somePath";
        String entity = "someId";
        String attribute = null; // irrelevant for this test
        String entityType = "someType"; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "x002fsomePathxffffsomeType";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityTypeEncoding

    /**
     * [NGSIMySQLSink.buildTableName] -------- When no encoding and when a root service-path is notified/defaulted and
     * data_model is 'dm-by-service-path' the MySQL table name is the encoding of \<service-path\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByServicePathNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When no encoding and when a root service-path is notified/defaulted and data_model is "
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/";
        String entity = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            sink.buildTableName(servicePath, entity, entityType, attribute);
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - The table name was built when data_model='dm-by-service-path' and using the root "
                    + "service path");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "-  OK  - The table name was not built when data_model='dm-by-service-path' and using the root "
                    + "service path");
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByServicePathNoEncoding

    /**
     * [NGSIMySQLSink.buildTableName] -------- When encoding and when a root service-path is notified/defaulted and
     * data_model is 'dm-by-service-path' the MySQL table name is the encoding of \<service-path\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByServicePathEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When encoding and when a root service-path is notified/defaulted and data_model is "
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/";
        String entity = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "x002f";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByServicePathEncoding

    /**
     * [NGSIMySQLSink.buildTableName] -------- When no encoding and when a root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entityId\> and \<entityType\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByEntityNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When no encoding and when a root service-path is notified/defaulted and data_model is "
                + "'dm-by-entity' the MySQL table name is the encoding of the concatenation of <service-path>, "
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/";
        String entity = "someId=someType";
        String attribute = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "someId_someType";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByEntityNoEncoding

    /**
     * [NGSIMySQLSink.buildTableName] -------- When encoding and when a root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entityId\> and \<entityType\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByEntityEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When encoding and when a root service-path is notified/defaulted and data_model is "
                + "'dm-by-entity' the MySQL table name is the encoding of the concatenation of <service-path>, "
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/";
        String entity = "someId=someType";
        String attribute = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "x002fxffffsomeIdxffffsomeType";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByEntityEncoding

    /**
     * [NGSIMySQLSink.buildTableName] -------- When encoding and when a root service-path is notified/defaulted and
     * data_model is 'dm-by-entity-type' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entityId\> and \<entityType\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathModelByEntityType() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When encoding and when a root service-path is notified/defaulted and data_model is "
                + "'dm-by-entity-type' the MySQL table name is the encoding of the concatenation of <service-path>, "
                + "and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-type";
        String enableEncoding = "true";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/";
        String entity = "someId";
        String entityType = "someType";
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "x002fxffffsomeType";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathModelByEntityType

    /**
     * [NGSIMySQLSink.buildTableName] -------- When encoding and when a root service-path is notified/defaulted and
     * data_model is 'dm-by-entity-type' the MySQL table name is the concatenation of \<service-path\>,
     * \<entityId\> and \<entityType\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathModelByEntityTypeNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When encoding and when a root service-path is notified/defaulted and data_model is "
                + "'dm-by-entity-type' the MySQL table name is the concatenation of <service-path>, "
                + "and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-type";
        String enableEncoding = "false";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/";
        String entity = "someId";
        String entityType = "someType";
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "someType";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to " + expecetedTableName);
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to " + expecetedTableName);
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathModelByEntityTypeNoEncoding

    /**
     * [NGSIMySQLSink.buildDbName] -------- A database name length greater than 64 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildDbNameLength() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildDbName]")
                + "-------- A database name length greater than 64 characters is detected");
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String service = "tooLoooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooongService";

        try {
            sink.buildDbName(service);
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildDbName]")
                    + "- FAIL - A database name length greater than 64 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildDbName]")
                    + "-  OK  - A database name length greater than 64 characters has been detected");
        } // try catch
    } // testBuildDbNameLength

    /**
     * [NGSIMySQLSink.buildTableName] -------- When data model is by service path, a table name length greater than 64
     * characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByServicePath() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When data model is by service path, a table name length greater than 64 characters is "
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/tooLooooooooooooooooooooooooooooooooooooooooooooooooooooooongServicePath";
        String entity = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            sink.buildTableName(servicePath, entity, entityType, attribute);
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - A table name length greater than 64 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "-  OK  - A table name length greater than 64 characters has been detected");
        } // try catch
    } // testBuildTableNameLengthDataModelByServicePath

    /**
     * [NGSICartoDBSink.buildTableName] -------- When data model is by entity, a table name length greater than 64
     * characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByEntity() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When data model is by entity, a table name length greater than 64 characters is detected");
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/tooLooooooooooooooooooooongServicePath";
        String entity = "tooLooooooooooooooooooooooooooongEntity";
        String attribute = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test

        try {
            sink.buildTableName(servicePath, entity, entityType, attribute);
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - A table name length greater than 64 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "-  OK  - A table name length greater than 64 characters has been detected");
        } // try catch
    } // testBuildTableNameLengthDataModelByEntity

    /**
     * [NGSICartoDBSink.buildTableName] -------- When data model is by attribute, a table name length greater than 63
     * characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByAttribute() throws Exception {
        System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                + "-------- When data model is by atribute, a table name length greater than 64 characters is "
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
        NGSIMySQLSink sink = new NGSIMySQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username));
        String servicePath = "/tooLooooooooooooooongServicePath";
        String entity = "tooLooooooooooooooooooongEntity";
        String attribute = "tooLooooooooooooongAttribute";
        String entityType = null; // irrelevant for this test

        try {
            sink.buildTableName(servicePath, entity, entityType, attribute);
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "- FAIL - A table name length greater than 64 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIMySQLSink.buildTableName]")
                    + "-  OK  - A table name length greater than 64 characters has been detected");
        } // try catch
    } // testBuildTableNameLengthDataModelByAttribute

    private Context createContext(String attrPersistence, String batchSize, String batchTime, String batchTTL,
                                  String dataModel, String enableEncoding, String enableGrouping, String enableLowercase, String host,
                                  String password, String port, String username) {
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
    } // createContext

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
        contextAttribute1.setType("someType1");
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

    @Test
    public void testNativeTypeColumnBatch() throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError, CygnusBadContextData {
        String attr_native_types = "true"; // default
        NGSIMySQLSink ngsiMySQLSink = new NGSIMySQLSink();
        ngsiMySQLSink.configure(createContextforNativeTypes("column", null, null, null, null, null, null, null, null, null, null, null, null, attr_native_types));
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
                NGSIGenericAggregator aggregator = ngsiMySQLSink.getAggregator(false);
                aggregator.setService(events.get(0).getServiceForNaming(false));
                aggregator.setServicePathForData(events.get(0).getServicePathForData());
                aggregator.setServicePathForNaming(events.get(0).getServicePathForNaming(false, false));
                aggregator.setEntityForNaming(events.get(0).getEntityForNaming(false, false, false));
                aggregator.setEntityType(events.get(0).getEntityTypeForNaming(false, false));
                aggregator.setAttribute(events.get(0).getAttributeForNaming(false));
                aggregator.setDbName(ngsiMySQLSink.buildDbName(aggregator.getService()));
                aggregator.setTableName(ngsiMySQLSink.buildTableName(aggregator.getServicePathForNaming(), aggregator.getEntityForNaming(), aggregator.getEntityType(), aggregator.getAttribute()));
                aggregator.setAttrNativeTypes(true);
                aggregator.setAttrMetadataStore(true);
                aggregator.initialize(events.get(0));
                for (NGSIEvent event : events) {
                    aggregator.aggregate(event);
                }
                String correctBatch = "('2016-04-20 07:19:55.801','somePath','someId','someType',2,'[]',TRUE,'[]','2016-09-21T01:23:00.00Z','[]','{\"type\": \"Point\",\"coordinates\": [-0.036177,39.986159]}','[]','{\"String\": \"string\"}','[]','foo','[]','','[]',NULL,NULL,NULL,NULL),('2016-04-20 07:19:55.801','somePath','someId','someType',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'-3.7167, 40.3833','[{\"name\":\"location\",\"type\":\"string\",\"value\":\"WGS84\"}]','someValue2','[]')";
                String valuesForInsert = NGSIUtils.getValuesForInsert(aggregator.getAggregationToPersist(), aggregator.isAttrNativeTypes());
                if (valuesForInsert.equals(correctBatch)) {
                    System.out.println(getTestTraceHead("[NGSIMySQKSink.testNativeTypesColumnBatch]")
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

    @Test
    public void testNativeTypeRowBatch() throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError, CygnusBadContextData {
        String attr_native_types = "true"; // default
        NGSIMySQLSink ngsiMySQLSink = new NGSIMySQLSink();
        ngsiMySQLSink.configure(createContextforNativeTypes("row", null, null, null, null, null, null, null, null, null, null, null, null, attr_native_types));
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
                NGSIGenericAggregator aggregator = ngsiMySQLSink.getAggregator(true);
                aggregator.setService(events.get(0).getServiceForNaming(false));
                aggregator.setServicePathForData(events.get(0).getServicePathForData());
                aggregator.setServicePathForNaming(events.get(0).getServicePathForNaming(false, false));
                aggregator.setEntityForNaming(events.get(0).getEntityForNaming(false, false, false));
                aggregator.setEntityType(events.get(0).getEntityTypeForNaming(false, false));
                aggregator.setAttribute(events.get(0).getAttributeForNaming(false));
                aggregator.setDbName(ngsiMySQLSink.buildDbName(aggregator.getService()));
                aggregator.setTableName(ngsiMySQLSink.buildTableName(aggregator.getServicePathForNaming(), aggregator.getEntityForNaming(), aggregator.getEntityType(), aggregator.getAttribute()));
                aggregator.setAttrNativeTypes(false);
                aggregator.setAttrMetadataStore(true);
                aggregator.initialize(events.get(0));
                for (NGSIEvent event : events) {
                    aggregator.aggregate(event);
                } // for
                String correctBatch = "('1461136795801','2016-04-20 07:19:55.801','somePath','someId','someType','someNumber','number','2','[]'),('1461136795801','2016-04-20 07:19:55.801','somePath','someId','someType','somneBoolean','Boolean','true','[]'),('1461136795801','2016-04-20 07:19:55.801','somePath','someId','someType','someDate','DateTime','2016-09-21T01:23:00.00Z','[]'),('1461136795801','2016-04-20 07:19:55.801','somePath','someId','someType','someGeoJson','geo:json','{\"type\": \"Point\",\"coordinates\": [-0.036177,39.986159]}','[]'),('1461136795801','2016-04-20 07:19:55.801','somePath','someId','someType','someJson','json','{\"String\": \"string\"}','[]'),('1461136795801','2016-04-20 07:19:55.801','somePath','someId','someType','someString','string','foo','[]'),('1461136795801','2016-04-20 07:19:55.801','somePath','someId','someType','someString2','string','','[]'),('1461136795801','2016-04-20 07:19:55.801','somePath','someId','someType','someName1','someType1','-3.7167, 40.3833','[{\"name\":\"location\",\"type\":\"string\",\"value\":\"WGS84\"}]'),('1461136795801','2016-04-20 07:19:55.801','somePath','someId','someType','someName2','someType2','someValue2','[]')";
                String valuesForInsert = NGSIUtils.getValuesForInsert(aggregator.getAggregationToPersist(), aggregator.isAttrNativeTypes());
                if (valuesForInsert.equals(correctBatch)) {
                    System.out.println(getTestTraceHead("[NGSIMySQKSink.testNativeTypesRowBatch]")
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

} // NGSIMySQLSinkTest