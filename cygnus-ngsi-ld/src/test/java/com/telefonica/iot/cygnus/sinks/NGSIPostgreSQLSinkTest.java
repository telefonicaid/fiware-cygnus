/**
 * Copyright 2020 Telefonica Investigación y Desarrollo, S.A.U
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

import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author anmunoz
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
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.configure]")
                    + "-  OK  - 'enable_encoding=false' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.configure]")
                    + "- FAIL - 'enable_encoding=false' was not detected");
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
                    + "-  OK  - 'enable_lowercase=false' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.configure]")
                    + "- FAIL - 'enable_lowercase=false' was not detected");
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
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.configure]")
                    + "-  OK  - 'enable_grouping=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.configure]")
                    + "- FAIL - 'enable_grouping=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableGrouping

    /**
     * [NGSIPostgreSQLSink.configure] -------- data_model can only be 'dm-by-service-path' or 'dm-by-entity'.
     */
    @Test
    public void testConfigureDataModel() {
        System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.configure]")
                + "-------- data_model can only be 'dm-by-entity' or 'dm-by-entity-type'");
        String attrPersistence = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = "entity";
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
            System.out.println(sink.getDataModel()+"test");
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.configure]")
                    + "-  OK  - 'data_model=entity' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.configure]")
                    + "- FAIL - 'data_model=entity' was not detected");
            throw e;
        } // try catch
    } // testConfigureDataModel

    /**
     * [NGSIPostgreSQLSink.configure] -------- attr_persistence can only be 'row' or 'column'.
     */
    @Test
    public void testConfigureAttrPersistence() {
        System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.configure]")
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
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));

        try {
            System.out.println(sink.getInvalidConfiguration());
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.configure]")
                    + "-  OK  - 'attr_persistence=column' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.configure]")
                    + "- FAIL - 'attr_persistence=column' was not detected");
            throw e;
        } // try catch
    } // testConfigureAttrPersistence

    /**
     * [NGSILDPostgreSQLSink.buildDBName] -------- The schema name is equals to the encoding of the notified/defaulted
     * service.
     * @throws Exception
     */
    @Test
    public void testBuildDBNameOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildDBName]")
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
                System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildDBName]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildDBName]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildDBName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildDBNameOldEncoding

    /**
     * [NGSIPostgreSQLSink.buildDBName] -------- The schema name is equals to the encoding of the notified/defaulted
     * service.
     * @throws Exception
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
                System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildDBName]")
                        + "-  OK  - '" + expectedDBName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildDBName]")
                        + "- FAIL - '" + expectedDBName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildDBName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildDBNameNewEncoding


    /**
     * [NGSILDPostgreSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws Exception
     */
    @Test
    public void testBuildTableNameDataModelByEntityOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
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
        String entity = "urn:ngsi-ld:Vehicle:V123";
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(entity, attribute);
            System.out.println(builtTableName);
            String expecetedTableName = "urn_ngsi_ld_Vehicle_V123";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityOldEncoding

    /**
     * [NGSIPostgreSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws Exception
     */
    @Test
    public void testBuildTableNameDataModelByEntityNewEncoding() throws Exception {
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
        String entity = "urn:ngsi-ld:Vehicle:V123";
        String attribute = null; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(entity, attribute);
            String expecetedTableName = "urnx003angsix002dldx003ax0056ehiclex003ax0056123";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityNewEncoding


    /**
     * [NGSILDPostgreSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws Exception
     */
    @Test
    public void testBuildTableNameDataModelByEntityTypeOldEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the MySQL table name is the encoding of the concatenation of <service-path>, "
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
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String entity = "urn:ngsi-ld:Vehicle:V123";
        String entityType = "Vehicle"; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(entity, entityType);
            System.out.println(builtTableName);
            String expecetedTableName = "Vehicle";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityOldEncoding


    /**
     * [NGSIPostgreSQLSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-entity' the MySQL table name is the encoding of the concatenation of \<service-path\>,
     * \<entity_id\> and \<entity_type\>.
     * @throws Exception
     */
    @Test
    public void testBuildTableNameDataModelByEntityTypeNewEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the MySQL table name is the encoding of the concatenation of <service-path>, "
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
        String cache = null; // default
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String entity = "urn:ngsi-ld:Vehicle:V123";
        String entityType = "Vehicle"; // irrelevant for this test

        try {
            String builtTableName = sink.buildTableName(entity, entityType);
            String expecetedTableName = "x0056ehicle";

            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>, <entityId> "
                        + "and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>, "
                        + "<entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSILDPostgreSQLSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntityNewEncoding

    /**
     * [NGSIPostgreSQLSink.buildSchemaName] -------- A schema name length greater than 63 characters is detected.
     * @throws Exception
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
     * [NGSICartoDBSink.buildTableName] -------- When data model is by entity, a table name length greater than 63
     * characters is detected.
     * @throws Exception
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
        String entity = "/tooLooooooooooooooooooooongServicePathtooLooooooooooooooooooooooooooongEntity";
        String entityType = null; // irrelevant for this test

        try {
            sink.buildTableName(entity, entityType);
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
     * @throws Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByAttribute() throws Exception {
        System.out.println(getTestTraceHead("[NGSIPostgreSQLSink.buildTableName]")
                + "-------- When data model is by entity type, a table name length greater than 63 characters is "
                + "detected");
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
        NGSIPostgreSQLSink sink = new NGSIPostgreSQLSink();
        sink.configure(createContext(attrPersistence, batchSize, batchTime, batchTTL, dataModel, enableEncoding,
                enableGrouping, enableLowercase, host, password, port, username, cache));
        String entity = "tooLooooooooooooooooooongEntitytooLooooooooooooongAttribute";
        String entityType = "tooLooooooooooooooooooongEntitytooLooooooooooooongAttributetooLooooooooooooooooooongEntitytooLooooooooooooongAttribute";
        
        try {
            sink.buildTableName(entity, entityType);
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

} // NGSIPostgreSQLSinkTest
