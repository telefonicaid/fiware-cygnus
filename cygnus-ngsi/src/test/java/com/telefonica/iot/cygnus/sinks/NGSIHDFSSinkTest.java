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

import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NGSIHDFSSinkTest {
    
    /**
     * Constructor.
     */
    public NGSIHDFSSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSIHDFSSinkTest

    /**
     * [NGSIHDFSSinkTest.configure] -------- enable_encoding can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableEncoding() {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                + "-------- enable_encoding can only be 'true' or 'false'");
        String backendImpl = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = "falso";
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "-  OK  - 'enable_encoding=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "- FAIL - 'enable_encoding=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableEncoding
    
    /**
     * [NGSIHDFSSinkTest.configure] -------- enable_lowercase can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableLowercase() {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                + "-------- enable_lowercase can only be 'true' or 'false'");
        String backendImpl = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = "true";
        String enableGrouping = null; // default value
        String enableLowercase = "falso";
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "-  OK  - 'enable_lowercase=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "- FAIL - 'enable_lowercase=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableLowercase
    
    /**
     * [NGSIHDFSSinkTest.configure] -------- enable_grouping can only be 'true' or 'false'.
     */
    // TBD: check for enable_grouping values in NGSIHDFSSink and uncomment this test.
    //@Test
    public void testConfigureEnableGrouping() {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                + "-------- enable_grouping can only be 'true' or 'false'");
        String backendImpl = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = "true";
        String enableGrouping = "falso";
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "-  OK  - 'enable_grouping=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "- FAIL - 'enable_grouping=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableGrouping
    
    /**
     * [NGSIHDFSSinkTest.configure] -------- backend_impl can only be 'rest' or 'binary'.
     */
    @Test
    public void testConfigureBackendImpl() {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                + "-------- enable_grouping can only be 'true' or 'false'");
        String backendImpl = "api";
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = "true";
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "-  OK  - 'backend_impl=api' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "- FAIL - 'backend_impl=api' was not detected");
            throw e;
        } // try catch
    } // testConfigureBackendImpl
    
    /**
     * [NGSIHDFSSinkTest.configure] -------- data_model can only be 'dm-by-entity'.
     */
    // TBD: check for data_model values in NGSIHDFSSink and uncomment this test.
    //@Test
    public void testConfigureDataModel() {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                + "-------- data_model can only be 'dm-by-entity'");
        String backendImpl = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = "dm-by-service-path";
        String enableEncoding = "true";
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "-  OK  - 'data_model=dm-by-service-path' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "- FAIL - 'data_model=dm-by-service-path' was not detected");
            throw e;
        } // try catch
    } // testConfigureDataModel
    
    /**
     * [NGSIHDFSSinkTest.configure] -------- file_format can only be 'json-row', 'json-column', 'csv-row' or
     * 'csv-column'.
     */
    @Test
    public void testConfigureFileFormat() {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                + "-------- file_format can only be 'json-row', 'json-column', 'csv-row' or 'csv-column'");
        String backendImpl = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = "true";
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = "fila";
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "-  OK  - 'file_format=fila' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "- FAIL - 'file_format=fila' was not detected");
            throw e;
        } // try catch
    } // testConfigureFileFormat

    /**
     * [NGSIHDFSSinkTest.buildFolderPath] -------- When no encoding and when a non root service-path is
     * notified/defaulted the HDFS folder path is the encoding of \<service\>/\<service-path\>/\<entity\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildFolderPathNonRootServicePathNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                + "-------- When no encoding and when a non root service-path is notified/defaulted the HDFS folder "
                + "path is the encoding of <service>/<service-path>/<entity>");
        String backendImpl = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = "false";
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        String service = "someService";
        String servicePath = "/somePath";
        String entity = "someId=someType";
        
        try {
            String buildFolderPath = sink.buildFolderPath(service, servicePath, entity);
            String expectedFolderPath = "someService/somePath/someId_someType";
        
            try {
                assertEquals(expectedFolderPath, buildFolderPath);
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                        + "-  OK  - '" + buildFolderPath + "' is equals to "
                        + "<service>/<service-path>/<entity>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                        + "- FAIL - '" + buildFolderPath + "' is not equals to "
                        + "<service>/<service-path>/<entity>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildFolderPathNonRootServicePathNoEncoding
    
    /**
     * [NGSIHDFSSinkTest.buildFolderPath] -------- When encoding and when a non root service-path is notified/defaulted
     * the HDFS folder path is the encoding of \<service\>/\<service-path\>/\<entity\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildFolderPathNonRootServicePathEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                + "-------- When encoding and when a non root service-path is notified/defaulted the HDFS folder path "
                + "is the encoding of <service>/<service-path>/<entity>");
        String backendImpl = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = "true";
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        String service = "someService";
        String servicePath = "/somePath";
        String entity = "someId=someType";
        
        try {
            String buildFolderPath = sink.buildFolderPath(service, servicePath, entity);
            String expectedFolderPath = "someService/somePath/someIdxffffsomeType";
        
            try {
                assertEquals(expectedFolderPath, buildFolderPath);
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                        + "-  OK  - '" + buildFolderPath + "' is equals to the encoding of "
                        + "<service>/<service-path>/<entity>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                        + "- FAIL - '" + buildFolderPath + "' is not equals to the encoding of "
                        + "<service>/<service-path>/<entity>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildFolderPathNonRootServicePathEncoding

    /**
     * [NGSIHDFSSinkTest.buildTableName] -------- When no encoding and when a root service-path is notified/defaulted
     * the HDFS folder path is the encoding of \<service\>/\<entity\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildFolderPathRootServicePathNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                + "-------- When no encoding and when a root service-path is notified/defaulted the HDFS folder path "
                + "is the encoding of <service>/<entity>");
        String backendImpl = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = "false";
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        String service = "someService";
        String servicePath = "/";
        String entity = "someId=someType";
        
        try {
            String builtTableName = sink.buildFolderPath(service, servicePath, entity);
            String expecetedTableName = "someService/someId_someType";
        
            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of "
                        + "<service>/<entity>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of "
                        + "<service>/<entity>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildFolderPathRootServicePathNoEncoding
    
    /**
     * [NGSIHDFSSinkTest.buildTableName] -------- When encoding and when a root service-path is notified/defaulted the
     * HDFS folder path is the encoding of \<service\>/\<entity\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildFolderPathRootServicePathEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                + "-------- When encoding and when a root service-path is notified/defaulted the HDFS folder path is "
                + "the encoding of <service>/<entity>");
        String backendImpl = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = "true";
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        String service = "someService";
        String servicePath = "/";
        String entity = "someId=someType";
        
        try {
            String builtTableName = sink.buildFolderPath(service, servicePath, entity);
            String expecetedTableName = "someService/someIdxffffsomeType";
        
            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of "
                        + "<service>/<entity>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of "
                        + "<service>/<entity>");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFolderPath]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildFolderPathRootServicePathEncoding
    
    /**
     * [NGSIHDFSSinkTest.buildFilePath] -------- When no encoding and when a non root service-path is notified/defaulted
     * the HDFS file path is the encoding of \<service\>/\<service-path\>/\<entity\>/\<entity\>.txt.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildFilePathNonRootServicePathNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                + "-------- When no encoding and when a non root service-path is notified/defaulted the HDFS file path "
                + "is the encoding of <service>/<service-path>/<entity>/<entity>.txt");
        String backendImpl = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = "false";
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        String service = "someService";
        String servicePath = "/somePath";
        String entity = "someId=someType";
        
        try {
            String buildFolderPath = sink.buildFilePath(service, servicePath, entity);
            String expectedFolderPath = "someService/somePath/someId_someType/someId_someType.txt";
        
            try {
                assertEquals(expectedFolderPath, buildFolderPath);
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                        + "-  OK  - '" + buildFolderPath + "' is equals to "
                        + "<service>/<service-path>/<entity>/<entity>.txt");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                        + "- FAIL - '" + buildFolderPath + "' is not equals to "
                        + "<service>/<service-path>/<entity>/<entity>.txt");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildFilePathNonRootServicePathNoEncoding
    
    /**
     * [NGSIHDFSSinkTest.buildFilePath] -------- When encoding and when a non root service-path is notified/defaulted
     * the HDFS file path is the encoding of \<service\>/\<service-path\>/\<entity\>/\<entity\>.txt.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildFilePathNonRootServicePathEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                + "-------- When encoding and when a non root service-path is notified/defaulted the HDFS file path "
                + "is the encoding of <service>/<service-path>/<entity>/<entity>.txt");
        String backendImpl = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = "true";
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        String service = "someService";
        String servicePath = "/somePath";
        String entity = "someId=someType";
        
        try {
            String buildFolderPath = sink.buildFilePath(service, servicePath, entity);
            String expectedFolderPath = "someService/somePath/someIdxffffsomeType/someIdxffffsomeType.txt";
        
            try {
                assertEquals(expectedFolderPath, buildFolderPath);
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                        + "-  OK  - '" + buildFolderPath + "' is equals to the encoding of "
                        + "<service>/<service-path>/<entity>/<entity>.txt");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                        + "- FAIL - '" + buildFolderPath + "' is not equals to the encoding of "
                        + "<service>/<service-path>/<entity>/<entity>.txt");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildFilePathNonRootServicePathEncoding

    /**
     * [NGSIHDFSSinkTest.buildTableName] -------- When no encoding and when a root service-path is notified/defaulted
     * the HDFS file path is the encoding of \<service\>/\<entity\>/\<entity\>.txt.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildFilePathRootServicePathNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                + "-------- When no encoding and when a root service-path is notified/defaulted the HDFS file path is "
                + "the encoding of <service>/<entity>/<entity>.txt");
        String backendImpl = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = "false";
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        String service = "someService";
        String servicePath = "/";
        String entity = "someId=someType";
        
        try {
            String builtTableName = sink.buildFilePath(service, servicePath, entity);
            String expecetedTableName = "someService/someId_someType/someId_someType.txt";
        
            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                        + "-  OK  - '" + builtTableName + "' is equals to "
                        + "<service>/<entity>/<entity>.txt");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                        + "- FAIL - '" + builtTableName + "' is not equals to "
                        + "<service>/<entity>/<entity>.txt");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildFilePathRootServicePathNoEncoding
    
    /**
     * [NGSIHDFSSinkTest.buildTableName] -------- When encoding and when a root service-path is notified/defaulted the
     * HDFS file path is the encoding of \<service\>/\<entity\>/\<entity\>.txt.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildFilePathRootServicePathEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                + "-------- When encoding and when a root service-path is notified/defaulted the HDFS file path is the "
                + "encoding of <service>/<entity>/<entity>.txt");
        String backendImpl = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = "true";
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = "false";
        String krb5 = "false";
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, batchSize, batchTime, batchTTL, csvSeparator, dataModel,
                enableEncoding, enableGrouping, enableLowercase, fileFormat, host, password, port, username, hive, krb5,
                token, serviceAsNamespace));
        String service = "someService";
        String servicePath = "/";
        String entity = "someId=someType";
        
        try {
            String builtTableName = sink.buildFilePath(service, servicePath, entity);
            String expecetedTableName = "someService/someIdxffffsomeType/someIdxffffsomeType.txt";
        
            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of "
                        + "<service>/<entity>/<entity>.txt");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of "
                        + "<service>/<entity>/<entity>.txt");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.buildFilePath]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildFilePathRootServicePathEncoding
    
    private NGSIBatch createBatch(long recvTimeTs, String service, String servicePath, String destination,
            ContextElement contextElement) {
        NGSIEvent groupedEvent = new NGSIEvent(recvTimeTs, service, servicePath, destination, null,
            contextElement);
        NGSIBatch batch = new NGSIBatch();
        batch.addEvent(destination, groupedEvent);
        return batch;
    } // createBatch
    
    private Context createContext(String backendImpl, String batchSize, String batchTime, String batchTTL,
            String csvSeparator, String dataModel, String enableEncoding, String enableGrouping, String enableLowercase,
            String fileFormat, String host, String password, String port, String username, String hive, String krb5,
            String token, String serviceAsNamespace) {
        Context context = new Context();
        context.put("backend_impl", backendImpl);
        context.put("batchSize", batchSize);
        context.put("batchTime", batchTime);
        context.put("batchTTL", batchTTL);
        context.put("csv_separator", csvSeparator);
        context.put("data_model", dataModel);
        context.put("enable_encoding", enableEncoding);
        context.put("enable_grouping", enableGrouping);
        context.put("enable_grouping", enableLowercase);
        context.put("file_format", fileFormat);
        context.put("hdfs_host", host);
        context.put("hdfs_password", password);
        context.put("hdfs_port", port);
        context.put("hdfs_username", username);
        context.put("hive", hive);
        context.put("krb5_auth", krb5);
        context.put("oauth2_token", token);
        context.put("service_as_namespace", serviceAsNamespace);
        return context;
    } // createContext
    
} // NGSIHDFSSinkTest
