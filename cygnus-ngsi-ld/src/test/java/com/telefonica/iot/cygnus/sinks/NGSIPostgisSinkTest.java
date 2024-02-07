/**
 * Copyright 2019 Telefonica
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
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 *
 * @author anmunoz
 */
@RunWith(MockitoJUnitRunner.class)
public class NGSIPostgisSinkTest {

    /**
     * Constructor.
     */
    public NGSIPostgisSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSIPostgisSinkTest

    /**
     * [NGSIPostgisSink.configure] -------- enable_encoding can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableEncoding() {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
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
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                    + "-  OK  - 'enable_encoding=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                    + "- FAIL - 'enable_encoding=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableEncoding

    /**
     * [NGSIPostgisSink.configure] -------- enable_lowercase can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableLowercase() {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
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
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                    + "-  OK  - 'enable_lowercase=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                    + "- FAIL - 'enable_lowercase=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableLowercase


    /**
     * [NGSIPostgisSink.configure] -------- data_model can only be 'dm-by-service-path' or 'dm-by-entity'.
     */
    // TBD: check for dataModel values in NGSIPostgisSink and uncomment this test.
    // @Test
    public void testConfigureDataModel() {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
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
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                    + "-  OK  - 'data_model=dm-by-service' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                    + "- FAIL - 'data_model=dm-by-service' was not detected");
            throw e;
        } // try catch
    } // testConfigureDataModel

    /**
     * [NGSIPostgisSink.configure] -------- attr_persistence can only be 'row' or 'column'.
     */
    @Test
    public void testConfigureAttrPersistence() {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                + "-------- attr_persistence can only be 'column'");
        String attrPersistence = "columna";
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
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                    + "-  OK  - 'attr_persistence=column' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                    + "- FAIL - 'attr_persistence=column' was not detected");
            throw e;
        } // try catch
    } // testConfigureAttrPersistence

    /**
     * [NGSIPostgisSink.configure] -------- sqlOptions is null when it is not configured.
     */
    @Test
    public void testConfigureSQLOptionsIsNull() {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                + "-------- postgisOptions is null when postgis_options is not configured");
        String attrPersistence = null;
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
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));

        assertNull(sink.getPostgisOptions());
        System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                + "-  OK  - postgisOptions is null when it is not configured");
    } // testConfigureSQLOptionsIsNull

    /**
     * [NGSIPostgisSink.configure] -------- sqlOptions has value when it is onfigured.
     */
    @Test
    public void testConfigureSQLOptionsHasValue() {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                + "-------- postgisOptions has value when postgis_options is configured");
        String attrPersistence = null;
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
        String sqlOptions = "sslmode=require";
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache, sqlOptions));

        assertEquals(sqlOptions, sink.getPostgisOptions());
        System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                + "-  OK  - postgisOptions has value when it is configured");
    } // testConfigureSQLOptionsIsNull

    /**
     * [NGSIPostgisSink.buildDBName] -------- The schema name is equals to the encoding of the notified/defaulted
     * service.
     * @throws Exception
     */
    @Test
    public void testBuildSchemaNameOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.buildSchemaName]")
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
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String service = "someService";
        String servicePath = "someServicePath";

        try {
            String builtSchemaName = sink.buildSchemaName(service);
            String expectedDBName = "someService";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIPostgisSink.buildSchemaName]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgisSink.buildSchemaName]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgisSink.buildSchemaName]")
                    + "- FAIL - There was some problem when building the schema name");
            throw e;
        } // try catch
    } // testBuildDBNameOldEncoding

    /**
     * [NGSIPostgisSink.testBuildDBNameOldEncodingDatabaseDataModel] -------- The DB name is equals to the encoding of the notified/defaulted
     * service.
     * @throws Exception
     */
    @Test
    public void testBuildDBNameOldEncodingDatabaseDataModel() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameOldEncodingDatabaseDataModel]")
                + "-------- The db name is equals to the encoding of the notified/defaulted service");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-database"; // default
        String enableEncoding = "false";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String service = "someService";

        try {
            String builtSchemaName = sink.buildDBName(service);
            String expectedDBName = "someService";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameOldEncodingDatabaseDataModel]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameOldEncodingDatabaseDataModel]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameOldEncodingDatabaseDataModel]")
                    + "- FAIL - There was some problem when building the Schema name");
            throw e;
        } // try catch
    } // testBuildDBNameOldEncodingDatabaseDataModel

    /**
     * [NGSIPostgisSink.testBuildDBNameOldEncoding] -------- The DB name is equals to the encoding of the notified/defaulted
     * service.
     * @throws Exception
     */
    @Test
    public void testBuildDBNameOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameOldEncoding]")
                + "-------- The db name is equals to the encoding of the notified/defaulted service");
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
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String service = "someService";

        try {
            String builtSchemaName = sink.buildDBName(service);
            // The default vale for the DB name
            String expectedDBName = "postgres";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameOldEncoding]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameOldEncoding]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameOldEncoding]")
                    + "- FAIL - There was some problem when building the Schema name");
            throw e;
        } // try catch
    } // testBuildDBNameOldEncoding

    /**
     * [NGSIPostgisSink.buildDBName] -------- The schema name is equals to the encoding of the notified/defaulted
     * service.
     * @throws Exception
     */
    @Test
    public void testBuildSchemaNameNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildSchemaNameNewEncoding]")
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
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String service = "someService";
        String servicePath = "someServicePath";

        try {
            String builtSchemaName = sink.buildSchemaName(service);
            String expectedDBName = "somex0053ervice";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildSchemaNameNewEncoding]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildSchemaNameNewEncoding]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildSchemaNameNewEncoding]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildSchemaNameNewEncoding

    /**
     * [NGSIPostgisSink.testBuildDBNameNewEncoding] -------- The DB name is equals to the encoding of the notified/defaulted
     * service.
     * @throws Exception
     */
    @Test
    public void testBuildDBNameNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameNewEncoding]")
                + "-------- The DB name is equals to the encoding of the notified/defaulted service");
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
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String service = "someService";

        try {
            String builtSchemaName = sink.buildDBName(service);
            String expectedDBName = "postgres";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameNewEncoding]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameNewEncoding]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameNewEncoding]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildDBNameNewEncoding

    /**
     * [NGSIPostgisSink.testBuildDBNameNewEncodingDatabaseDataModel] -------- The DB name is equals to the encoding of the notified/defaulted
     * service.
     * @throws Exception
     */
    @Test
    public void testBuildDBNameNewEncodingDatabaseDataModel() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameNewEncodingDatabaseDataModel]")
                + "-------- The DB name is equals to the encoding of the notified/defaulted service");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-database-schema"; // default
        String enableEncoding = "true";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String service = "someService";

        try {
            String builtSchemaName = sink.buildDBName(service);
            String expectedDBName = "somex0053ervice";

            try {
                assertEquals(expectedDBName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameNewEncodingDatabaseDataModel]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameNewEncodingDatabaseDataModel]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIPostgisSink.testBuildDBNameNewEncodingDatabaseDataModel]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildDBNameNewEncodingDatabaseDataModel


    /**
     * [NGSIPostgisSink.buildSchemaName] -------- A schema name length greater than 63 characters is detected.
     * @throws Exception
     */
    @Test
    public void testBuildSchemaNameLength() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.buildSchemaName]")
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
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String service = "tooLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooongService";
        String servicePath = "someServicePath";
        try {
            sink.buildSchemaName(service);
            System.out.println(getTestTraceHead("[NGSIPostgisSink.buildSchemaName]")
                    + "- FAIL - A schema name length greater than 63 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIPostgisSink.buildSchemaName]")
                    + "-  OK  - A schema name length greater than 63 characters has been detected");
        } // try catch
    } // testBuildSchemaNameLength


    /**
     * [NGSIPostgisSink.buildTableName] -------- When data model is by entity, a table name length greater than 63
            * characters is detected.
     * @throws Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByEntity() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.buildTableName]")
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
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String entity = "tooLooooooooooooooooooooooooooooooooooooooooooooooooongEntity";
        String entityType = null; // irrelevant for this test

        try {
            sink.buildTableName(entity, entityType);
            System.out.println(getTestTraceHead("[NGSIPostgisSink.buildTableName]")
                    + "- FAIL - A table name length greater than 63 characters has not been detected");
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
            System.out.println(getTestTraceHead("[NGSIPostgisSink.buildTableName]")
                    + "-  OK  - A table name length greater than 63 characters has been detected");
        } // try catch
    } // testBuildTableNameLengthDataModelByEntity

    /**
     * [NGSIPostgisSink.buildTableName] -------- When data model is by entity, a table name length greater than 63
     * characters is detected.
     * @throws Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByEntityType() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.buildTableName]")
                + "-------- When data model is by entity, a table name length greater than 63 characters is detected");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "dm-by-entity-type";
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String password = null; // default
        String port = null; // default
        String username = null; // default
        String cache = null; // default
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String servicePath = "/tooLooooooooooooooooooooongServicePath";
        String entity = "tooLooooooooooooooooooooooooooongEntity";
        String entityType = "tooLooooooooooooooooooooooooooongEntityType"; // irrelevant for this test
        String attribute = null; // irrelevant for this test

        try {
            sink.buildTableName(entity, entityType);
            System.out.println(getTestTraceHead("[NGSIPostgisSink.buildTableName]")
                    + "- FAIL - A table name length greater than 63 characters has not been detected");
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
            System.out.println(getTestTraceHead("[NGSIPostgisSink.buildTableName]")
                    + "-  OK  - A table name length greater than 63 characters has been detected");
        } // try catch
    } // testBuildTableNameLengthDataModelByEntityType



    /**
     * [NGSIPostgisSink.configure] -------- cache can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureCache() {
        System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
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
        NGSIPostgisSink sink = new NGSIPostgisSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                    + "-  OK  - 'enable_cache=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIPostgisSink.configure]")
                    + "- FAIL - 'enable_cache=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableEncoding


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
        context.put("postgis_host", host);
        context.put("postgis_password", password);
        context.put("postgis_port", port);
        context.put("postgis_username", username);
        context.put("backend.enable_cache", cache);
        return context;
    } // createContext

    private Context createContext(String attrPersistence, String batchSize, String batchTime, String batchTTL,
            String dataModel, String enableEncoding, String enableGrouping, String enableLowercase, String host,
            String password, String port, String username, String cache, String sqlOptions) {
        Context context = new Context();
        context.put("attr_persistence", attrPersistence);
        context.put("batch_size", batchSize);
        context.put("batch_time", batchTime);
        context.put("batch_ttl", batchTTL);
        context.put("data_model", dataModel);
        context.put("enable_encoding", enableEncoding);
        context.put("enable_grouping", enableGrouping);
        context.put("enable_lowercase", enableLowercase);
        context.put("postgis_host", host);
        context.put("postgis_password", password);
        context.put("postgis_port", port);
        context.put("postgis_username", username);
        context.put("backend.enable_cache", cache);
        context.put("postgis_options", sqlOptions);
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
        context.put("postgis_host", host);
        context.put("postgis_password", password);
        context.put("postgis_port", port);
        context.put("postgis_username", username);
        context.put("backend.enable_cache", cache);
        context.put("attr_native_types", attrNativeTypes);
        return context;
    } // createContext



} // NGSIPostgisSinkTest
