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
import com.telefonica.iot.cygnus.backends.sql.SQLQueryUtils;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author 
 */
@RunWith(MockitoJUnitRunner.class)
public class NGSIOracleSQLSinkTest {

    /**
     * Constructor.
     */
    public NGSIOracleSQLSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSIOracleSQLSinkTest

    /**
     * [NGSIOracleSQLSink.configure] -------- enable_encoding can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableEncoding() {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                + "-------- enable_encoding can only be 'true' or 'false'");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "falso";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                    + "-  OK  - 'enable_encoding=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                    + "- FAIL - 'enable_encoding=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableEncoding

    /**
     * [NGSIOracleSQLSink.configure] -------- enable_lowercase can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableLowercase() {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                + "-------- enable_lowercase can only be 'true' or 'false'");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableLowercase = "falso";
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                    + "-  OK  - 'enable_lowercase=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                    + "- FAIL - 'enable_lowercase=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableLowercase

    /**
     * [NGSIOracleSQLSink.configure] -------- data_model can only be 'dm-by-service-path' or 'dm-by-entity'.
     */
    // TBD: check for dataModel values in NGSIOracleSQLSink and uncomment this test.
    // @Test
    public void testConfigureDataModel() {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                + "-------- data_model can only be 'dm-by-service-path' or 'dm-by-entity'");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-service";
        String enableEncoding = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                    + "-  OK  - 'data_model=dm-by-service' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                    + "- FAIL - 'data_model=dm-by-service' was not detected");
            throw e;
        } // try catch
    } // testConfigureDataModel

    /**
     * [NGSIOracleSQLSink.configure] -------- attr_persistence can only be 'row' or 'column'.
     */
    @Test
    public void testConfigureAttrPersistence() {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                + "-------- attr_persistence can only be 'row' or 'column'");
        String attrPersistence = "fila";
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                    + "-  OK  - 'attr_persistence=fila' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                    + "- FAIL - 'attr_persistence=fila' was not detected");
            throw e;
        } // try catch
    } // testConfigureAttrPersistence

    /**
     * [NGSIOracleSQLSink.configure] -------- sqlOptions is null when it is not configured.
     */
    @Test
    public void testConfigureSQLOptionsIsNull() {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                + "-------- oracleOptions is null when oracle_options is not configured");
        String attrPersistence = null;
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));

        assertNull(sink.getOracleSQLOptions());
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                + "-  OK  - oracleOptions is null when it is not configured");
    } // testConfigureSQLOptionsIsNull

    /**
     * [NGSIOracleSQLSink.configure] -------- sqlOptions has value when it is configured.
     */
    @Test
    public void testConfigureSQLOptionsHasValue() {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                + "-------- oracleOptions has value when oracle_options is configured");
        String attrPersistence = null;
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        String sqlOptions = "sslmode=require";
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache, sqlOptions));

        assertEquals(sqlOptions, sink.getOracleSQLOptions());
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.configure]")
                + "-  OK  - oracleOptions has value when it is configured");
    } // testConfigureSQLOptionsHasValue

    /**
     * [NGSIOracleSQLSink.buildDBName] -------- The schema name is equals to the encoding of the notified/defaulted
     * service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildSchemaNameOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildSchemaNameOldEncoding]")
                + "-------- The schema name is equals to the encoding of the notified/defaulted service");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "false";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String service = "someService";
        String servicePath = "someServicePath";

        try {
            String builtSchemaName = sink.buildSchemaName(service, servicePath);
            String expectedDBName = "someService";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildSchemaNameOldEncoding]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildSchemaNameOldEncoding]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildSchemaNameOldEncoding]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildDBNameOldEncoding

    /**
     * [NGSIOracleSQLSink.buildDBName] -------- The schema name is equals to the encoding of the notified/defaulted
     * service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildSchemaNameNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildSchemaNameNewEncoding]")
                + "-------- The schema name is equals to the encoding of the notified/defaulted service");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "true";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String service = "someService";
        String servicePath = "someServicePath";

        try {
            String builtSchemaName = sink.buildSchemaName(service, servicePath);
            String expectedDBName = "somex0053ervice";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildSchemaNameNewEncoding]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildSchemaNameNewEncoding]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildSchemaNameNewEncoding]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildDBNameNewEncoding

    /**
     * [NGSIOracleSQLSink.testBuildDBNameOldEncodingDatabaseDataModel] -------- The DB name is equals to the encoding of the notified/defaulted
     * service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildDBNameOldEncodingDatabaseDataModel() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameOldEncodingDatabaseDataModel]")
                + "-------- The db name is equals to the encoding of the notified/defaulted service");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-database"; // default
        String enableEncoding = "false";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String service = "someService";

        try {
            String builtSchemaName = sink.buildDbName(service);
            String expectedDBName = "someService";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameOldEncodingDatabaseDataModel]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameOldEncodingDatabaseDataModel]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameOldEncodingDatabaseDataModel]")
                    + "- FAIL - There was some problem when building the Schema name");
            throw e;
        } // try catch
    } // testBuildDBNameOldEncodingDatabaseDataModel

    /**
     * [NGSIOracleSQLSink.testBuildDBNameOldEncodingEntityTypeDatabaseDataModel] -------- The DB name is equals to the encoding of the notified/defaulted
     * service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildDBNameOldEncodingEntityTypeDatabaseDataModel() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameOldEncodingEntityTypeDatabaseDataModel]")
                + "-------- The db name is equals to the encoding of the notified/defaulted service");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-type-database"; // default
        String enableEncoding = "false";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String service = "someService";

        try {
            String builtSchemaName = sink.buildDbName(service);
            String expectedDBName = "someService";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameOldEncodingEntityTypeDatabaseDataModel]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameOldEncodingEntityTypeDatabaseDataModel]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameOldEncodingEntityTypeDatabaseDataModel]")
                    + "- FAIL - There was some problem when building the Schema name");
            throw e;
        } // try catch
    } // testBuildDBNameOldEncodingEntityTypeDatabaseDataModel

    /**
     * [NGSIOracleSQLSink.testBuildDBNameOldEncoding] -------- The DB name is equals to the encoding of the notified/defaulted
     * service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildDBNameOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameOldEncoding]")
                + "-------- The db name is equals to the encoding of the notified/defaulted service");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "false";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String service = "someService";

        try {
            String builtSchemaName = sink.buildDbName(service);
            // The default vale for the DB name
            String expectedDBName = "xe";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameOldEncoding]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameOldEncoding]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameOldEncoding]")
                    + "- FAIL - There was some problem when building the Schema name");
            throw e;
        } // try catch          } // try catch
    } // testBuildDBNameOldEncoding

    /**
     * [NGSIOracleSQLSink.testBuildDBNameNewEncoding] -------- The DB name is equals to the encoding of the notified/defaulted
     * service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildDBNameNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameNewEncoding]")
                + "-------- The DB name is equals to the encoding of the notified/defaulted service");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "true";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String service = "someService";

        try {
            String builtSchemaName = sink.buildDbName(service);
            String expectedDBName = "xe";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameNewEncoding]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameNewEncoding]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameNewEncoding]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildDBNameNewEncoding


    /**
     * [NGSIOracleSQLSink.testBuildDBNameNewEncodingDatabaseDataModel] -------- The DB name is equals to the encoding of the notified/defaulted
     * service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildDBNameNewEncodingDatabaseDataModel() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameNewEncodingDatabaseDataModel]")
                + "-------- The DB name is equals to the encoding of the notified/defaulted service");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-database-schema"; // default
        String enableEncoding = "true";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String service = "someService";

        try {
            String builtSchemaName = sink.buildDbName(service);
            String expectedDBName = "somex0053ervice";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameNewEncodingDatabaseDataModel]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameNewEncodingDatabaseDataModel]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameNewEncodingDatabaseDataModel]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildDBNameNewEncodingDatabaseDataModel


    /**
     * [NGSIOracleSQLSink.testBuildDBNameNewEncodingEntityTypeDatabaseDataModel] -------- The DB name is equals to the encoding of the notified/defaulted
     * service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildDBNameNewEncodingEntityTypeDatabaseDataModel() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameNewEncodingEntityTypeDatabaseDataModel]")
                + "-------- The DB name is equals to the encoding of the notified/defaulted service");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-type-database-schema"; // default
        String enableEncoding = "true";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String service = "someService";

        try {
            String builtSchemaName = sink.buildDbName(service);
            String expectedDBName = "somex0053ervice";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameNewEncodingEntityTypeDatabaseDataModel]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameNewEncodingEntityTypeDatabaseDataModel]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testBuildDBNameNewEncodingEntityTypeDatabaseDataModel]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildDBNameNewEncodingEntityTypeDatabaseDataModel



    /**
     * [NGSIOracleSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-service-path' the OracleSQL table name is the encoding of <service-path>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByServicePathOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the OracleSQL table name is the encoding of <service-path>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-service-path";
        String enableEncoding = "false";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/somePath";
        String entity = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "somePath";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByServicePathOldEncoding

    /**
     * [NGSIOracleSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-service-path' the OracleSQL table name is the encoding of <service-path>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByServicePathNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the OracleSQL table name is the encoding of <service-path>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-service-path";
        String enableEncoding = "true";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/somePath";
        String entity = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "x002fsomex0050ath";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByServicePathNewEncoding

    /**
     * [NGSIOracleSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the OracleSQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByEntityOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the OracleSQL table name is the encoding of the concatenation of <service-path>, "
                + "<entityId> and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity";
        String enableEncoding = "false";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/somePath";
        String entityType = null; // irrelevant for this test
        String entity = "someId=someType";
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "somePath_someId_someType";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityOldEncoding

    /**
     * [NGSIOracleSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the OracleSQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByEntityNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the OracleSQL table name is the encoding of the concatenation of <service-path>, "
                + "<entityId> and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity";
        String enableEncoding = "true";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/somepath";
        String entity = "id=type";
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "x002fsomepathxffffidxfffftype";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityNewEncoding

    /**
     * [NGSIOracleSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the OracleSQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByEntityTypeOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-entity-type' the OracleSQL table name is the encoding of the concatenation of <service-path>, "
                + "<entityId> and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-type";
        String enableEncoding = "false";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/somePath";
        String entity = "someId=someType";
        String entityType = "someType"; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "somePath_someType";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityTypeOldEncoding

    /**
     * [NGSIOracleSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-entity-type' the OracleSQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByEntityTypeNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-entity-type' the OracleSQL table name is the encoding of the concatenation of <service-path>, "
                + "<entityId> and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-type";
        String enableEncoding = "true";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/somepath";
        String entity = "someId=someType"; // irrelevant for this test
        String entityType = "sometype";
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "x002fsomepathxffffsometype";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityTypeNewEncoding


    /**
     * [NGSIOracleSQLSink.buildTableName] -------- When a root service-path is notified/defaulted and
     * data_model is 'dm-by-service-path' the OracleSQL table name cannot be built.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByServicePathOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When a root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the OracleSQL table name cannot be built");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-service-path";
        String enableEncoding = "false";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/";
        String entity = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            sink.buildTableName(servicePath, entity, entityType, attribute);
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - The root service path was not detected as not valid");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "-  OK  - The root service path was detected as not valid");
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByServicePathOldEncoding

    /**
     * [NGSIOracleSQLSink.buildTableName] -------- When a root service-path is notified/defaulted and
     * data_model is 'dm-by-service-path' the OracleSQL table name is the encoding of \<service-path\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByServicePathNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When a root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the OracleSQL table name is the encoding of <service-path>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-service-path";
        String enableEncoding = "true";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/";
        String entity = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "x002f";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByServicePathNewEncoding

    /**
     * [NGSIOracleSQLSink.buildTableName] -------- When a root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the OracleSQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entityId\> and \<entityType\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByEntityOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When a root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the OracleSQL table name is the encoding of the concatenation of <service-path>, "
                + "<entityId> and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity";
        String enableEncoding = "false";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/";
        String entity = "someId=someType";
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "someId_someType";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByEntityOldencoding

    /**
     * [NGSIOracleSQLSink.buildTableName] -------- When a root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the OracleSQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entityId\> and \<entityType\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByEntityNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When a root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the OracleSQL table name is the encoding of the concatenation of <service-path>, "
                + "<entityId> and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity";
        String enableEncoding = "true";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/";
        String entity = "someid=sometype";
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "x002fxffffsomeidxffffsometype";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByEntityNewEncoding

    @Test
    public void testBuildTableNameRootServicePathDataModelByEntityTypeOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When a root service-path is notified/defaulted and data_model is "
                + "'dm-by-entity-type' the OracleSQL table name is the encoding of the concatenation of <service-path>, "
                + "<entityId> and <entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-type";
        String enableEncoding = "false";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/";
        String entity = "someId=someType";
        String entityType = "someType"; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "someType";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByEntityTypeOldEncoding

    /**
     * [NGSIOracleSQLSink.buildTableName] -------- When a root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the OracleSQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entityId\> and \<entityType\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByEntityTypeNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When a root service-path is notified/defaulted and data_model is "
                + "'dm-by-entity-type' and no encoding the OracleSQL table name is the concatenation of <service-path>, "
                + "<entityType>");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-type";
        String enableEncoding = "false";
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/";
        String entity = "someId";
        String entityType = "someType";
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(servicePath, entity, entityType, attribute);
            String expecetedTableName = "someType";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to " + expecetedTableName);
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to " + expecetedTableName);
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByEntityTypeNoEncoding
    /**
     * [NGSIOracleSQLSink.buildSchemaName] -------- A schema name length greater than 30 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildSchemaNameLength() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildSchemaName]")
                + "-------- A schema name length greater than 30 characters is detected");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String service = "tooLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooongService";
        String servicePath = "someServicePath";

        try {
            sink.buildSchemaName(service, servicePath);
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildSchemaName]")
                    + "- FAIL - A schema name length greater than 30 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildSchemaName]")
                    + "-  OK  - A schema name length greater than 30 characters has been detected");
        } // try catch
    } // testBuildSchemaNameLength

    /**
     * [NGSIOracleSQLSink.buildTableName] -------- When data model is by service path, a table name length greater
     * than 30 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByServicePath() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When data model is by service path, a table name length greater than 30 characters is "
                + "detected");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-service-path";
        String enableEncoding = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/tooLooooooooooooooooooooooooooooooooooooooooooooooooooooooongServicePath";
        String entity = null; // irrelevant for this test
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            sink.buildTableName(servicePath, entity, entityType, attribute);
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - A table name length greater than 30 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "-  OK  - A table name length greater than 30 characters has been detected");
        } // try catch
    } // testBuildTableNameLengthDataModelByServicePath

    /**
     * [NGSICartoDBSink.buildTableName] -------- When data model is by entity, a table name length greater than 30
     * characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByEntity() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When data model is by entity, a table name length greater than 30 characters is detected");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity";
        String enableEncoding = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/tooLooooooooooooooooooooongServicePath";
        String entity = "tooLooooooooooooooooooooooooooongEntity";
        String entityType = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            sink.buildTableName(servicePath, entity, entityType, attribute);
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - A table name length greater than 30 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "-  OK  - A table name length greater than 30 characters has been detected");
        } // try catch
    } // testBuildTableNameLengthDataModelByEntity

    /**
     * [NGSIOracleSQLSink.buildTableName] -------- When data model is by entity, a table name length greater than 30
     * characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByEntityType() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When data model is by entity, a table name length greater than 30 characters is detected");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-type";
        String enableEncoding = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/tooLooooooooooooooooooooongServicePath";
        String entity = "tooLooooooooooooooooooooooooooongEntity";
        String entityType = "tooLooooooooooooooooooooooooooongEntityType"; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            sink.buildTableName(servicePath, entity, entityType, attribute);
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - A table name length greater than 30 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "-  OK  - A table name length greater than 30 characters has been detected");
        } // try catch
    } // testBuildTableNameLengthDataModelByEntityType

    /**
     * [NGSICartoDBSink.buildTableName] -------- When data model is by attribute, a table name length greater than 30
     * characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByAttribute() throws Exception {
        System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                + "-------- When data model is by atribute, a table name length greater than 30 characters is "
                + "detected");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-attribute";
        String enableEncoding = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIOracleSQLSink sink = new NGSIOracleSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableLowercase, host, password, port, username, cache));
        String servicePath = "/tooLooooooooooooooongServicePath";
        String entity = "tooLooooooooooooooooooongEntity";
        String entityType = null; // irrelevant for this test
        String attribute = "tooLooooooooooooongAttribute";

        try {
            sink.buildTableName(servicePath, entity, entityType, attribute);
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "- FAIL - A table name length greater than 30 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIOracleSQLSink.buildTableName]")
                    + "-  OK  - A table name length greater than 30 characters has been detected");
        } // try catch
    } // testBuildTableNameLengthDataModelByAttribute


    private Context createContext(String attrPersistence, String batchSize, String batchTime, String batchTTL,
            String dataModel, String enableEncoding, String enableLowercase, String host,
            String password, String port, String username, String cache) {
        Context context = new Context();
        context.put("attr_persistence", attrPersistence);
        context.put("batch_size", batchSize);
        context.put("batch_time", batchTime);
        context.put("batch_ttl", batchTTL);
        context.put("data_model", dataModel);
        context.put("enable_encoding", enableEncoding);
        context.put("enable_lowercase", enableLowercase);
        context.put("oracle_host", host);
        context.put("oracle_password", password);
        context.put("oracle_port", port);
        context.put("oracle_username", username);
        context.put("backend.enable_cache", cache);
        return context;
    } // createContext

    private Context createContext(String attrPersistence, String batchSize, String batchTime, String batchTTL,
            String dataModel, String enableEncoding, String enableLowercase, String host,
            String password, String port, String username, String cache, String sqlOptions) {
        Context context = new Context();
        context.put("attr_persistence", attrPersistence);
        context.put("batch_size", batchSize);
        context.put("batch_time", batchTime);
        context.put("batch_ttl", batchTTL);
        context.put("data_model", dataModel);
        context.put("enable_encoding", enableEncoding);
        context.put("enable_lowercase", enableLowercase);
        context.put("oracle_host", host);
        context.put("oracle_password", password);
        context.put("oracle_port", port);
        context.put("oracle_username", username);
        context.put("backend.enable_cache", cache);
        context.put("oracle_options", sqlOptions);
        return context;
    } // createContext

    private Context createContextforNativeTypes(String attrPersistence, String batchSize, String batchTime, String batchTTL,
                                                String dataModel, String enableEncoding, String enableLowercase, String host,
                                                String password, String port, String username, String cache, String attrNativeTypes) {
        Context context = new Context();
        context.put("attr_persistence", attrPersistence);
        context.put("batch_size", batchSize);
        context.put("batch_time", batchTime);
        context.put("batch_ttl", batchTTL);
        context.put("data_model", dataModel);
        context.put("enable_encoding", enableEncoding);
        context.put("enable_lowercase", enableLowercase);
        context.put("oracle_host", host);
        context.put("oracle_password", password);
        context.put("oracle_port", port);
        context.put("oracle_username", username);
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
        NGSIOracleSQLSink ngsiOracleSQLSink = new NGSIOracleSQLSink();
        ngsiOracleSQLSink.configure(createContextforNativeTypes("column", null, null, null, null,  null, null, null, null, null, null, null, attr_native_types));
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
                NGSIGenericAggregator aggregator = ngsiOracleSQLSink.getAggregator(false);
                aggregator.setService(events.get(0).getServiceForNaming(false));
                aggregator.setServicePathForData(events.get(0).getServicePathForData());
                aggregator.setServicePathForNaming(events.get(0).getServicePathForNaming(false));
                aggregator.setEntityForNaming(events.get(0).getEntityForNaming(false, false));
                aggregator.setEntityType(events.get(0).getEntityTypeForNaming(false));
                aggregator.setAttribute(events.get(0).getAttributeForNaming(false));
                aggregator.setSchemeName(ngsiOracleSQLSink.buildSchemaName(aggregator.getService(), aggregator.getServicePathForNaming()));
                aggregator.setTableName(ngsiOracleSQLSink.buildTableName(aggregator.getServicePathForNaming(), aggregator.getEntityForNaming(), aggregator.getEntityType(), aggregator.getAttribute()));
                aggregator.setAttrNativeTypes(true);
                aggregator.setAttrMetadataStore(true);
                aggregator.setEnableNameMappings(true);
                aggregator.setLastDataMode("insert");
                aggregator.initialize(events.get(0));
                for (NGSIEvent event : events) {
                    aggregator.aggregate(event);
                }
                String correctBatch = "('someId','someType','somePath','2016-04-20 07:19:55.801',2,'[]',TRUE,'[]','2016-09-21T01:23:00.00Z','[]','{\"type\": \"Point\",\"coordinates\": [-0.036177,39.986159]}','[]','{\"String\": \"string\"}','[]','foo','[]','','[]',NULL,NULL,NULL,NULL),('someId','someType','somePath','2016-04-20 07:19:55.801',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'-3.7167, 40.3833','[{\"name\":\"location\",\"type\":\"string\",\"value\":\"WGS84\"}]','someValue2','[]')";
                String valuesForInsert = SQLQueryUtils.getValuesForInsert(aggregator.getAggregationToPersist(), aggregator.isAttrNativeTypes());
                if (valuesForInsert.equals(correctBatch)) {
                    System.out.println(getTestTraceHead("[NGSIOracleSQLSink.testNativeTypesColumnBatch]")
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

} // NGSIOracleSQLSinkTest
