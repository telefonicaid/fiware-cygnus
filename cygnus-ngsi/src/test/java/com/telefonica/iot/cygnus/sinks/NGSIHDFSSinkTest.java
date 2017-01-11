/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

import com.telefonica.iot.cygnus.sinks.NGSIHDFSSink.BackendImpl;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Assert;
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
     * [NGSIHDFSSink.configure] -------- When not configured, not mandatory parameters get default values.
     */
    @Test
    public void testConfigureDefaults() {
        System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                + "-------- When not configured, not mandatory parameters get default values");
        String backendImpl = null; // default value
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = null; // default value
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = null; // default value
        String krb5 = null; // default value
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
        
        try {
            assertEquals(BackendImpl.REST, sink.getBackendImpl());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'backend.impl=rest' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'backend.impl=rest' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(500, sink.getBackendMaxConns());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'backend.max_conns=500' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'backend.max_conns=500' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(100, sink.getBackendMaxConnsPerRoute());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'backend.max_conns_per_route=100' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'backend.max_conns_per_route=100' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(1, sink.getBatchSize());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'batch_size=1' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'batch_size=1' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(30, sink.getBatchTimeout());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'batch_timeout=30' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'batch_timeout=30' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(10, sink.getBatchTTL());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'batch_ttl=30' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'batch_ttl=30' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(",", sink.getCSVSeparator());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'csv_separator=,' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'csv_separator=,' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(Enums.DataModel.DMBYENTITY, sink.getDataModel());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'data_model=dm-by-entity' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'data_model=dm-by-entity' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getEnableEncoding());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'enable_encoding=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'enable_encoding=false' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getEnableGrouping());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'enable_grouping=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'enable_grouping=false' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getEnableLowerCase());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'enable_lowercase=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'enable_lowercase=false' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals("json-row", sink.getFileFormat());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'file_format=json-row' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'file_format=json-row' not configured by default");
            throw e;
        } // try catch
        
        try {
            String[] expectedHost = {"localhost"};
            Assert.assertArrayEquals(expectedHost, sink.getHDFSHosts());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'hdfs_host=localhost' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'hdfs_host=localhost' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals("14000", sink.getHDFSPort());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'hdfs_port=14000' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'hdfs_port=14000' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getEnableHive());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'enable_hive=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'enable_hive=false' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getEnableKrb5Auth());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'enable_krb5=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'enable_krb5=false' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getServiceAsNamespace());
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "-  OK  - 'service_as_namespace=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSink.configure]")
                    + "- FAIL - 'service_as_namespace=false' not configured by default");
            throw e;
        } // try catch
    } // testConfigureDefaults
    
    /**
     * [NGSIHDFSSinkTest.configure] -------- backend.max_conns gets the configured value.
     */
    @Test
    public void testConfigureMaxConns() {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                + "-------- backend.max_conns gets the configured value");
        String backendImpl = null; // default value
        String backendMaxConns = "25";
        String backendMaxConnsPerRoute = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = null;
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = null;
        String krb5 = null;
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
        
        try {
            assertEquals(25, sink.getBackendMaxConns());
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "-  OK  - 'backend.max_conns=25' was configured");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "- FAIL - 'backend.max_conns=25' was not configured");
            throw e;
        } // try catch
    } // testConfigureMaxConns
    
    /**
     * [NGSIHDFSSinkTest.configure] -------- backend.max_conns_per_route gets the configured value.
     */
    @Test
    public void testConfigureMaxConnsPerRoute() {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                + "-------- backend.max_conns_per_route gets the configured value");
        String backendImpl = null; // default value
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = "3";
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = null;
        String enableGrouping = null; // default value
        String enableLowercase = null; // default value
        String fileFormat = null; // default value
        String host = null; // default value
        String password = "mypassword";
        String port = null; // default value
        String username = "myuser";
        String hive = null;
        String krb5 = null;
        String token = "mytoken";
        String serviceAsNamespace  = null; // default value
        NGSIHDFSSink sink = new NGSIHDFSSink();
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
        
        try {
            assertEquals(3, sink.getBackendMaxConnsPerRoute());
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "-  OK  - 'backend.max_conns_per_route=3' was configured");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                    + "- FAIL - 'backend.max_conns_per_route=3' was not configured");
            throw e;
        } // try catch
    } // testConfigureMaxConnsPerRoute
    
    /**
     * [NGSIHDFSSinkTest.configure] -------- enable_encoding can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableEncoding() {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                + "-------- enable_encoding can only be 'true' or 'false'");
        String backendImpl = null; // default value
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
        
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
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
        
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
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
        
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
     * [NGSIHDFSSinkTest.configure] -------- backend.impl can only be 'rest' or 'binary'.
     */
    @Test
    public void testConfigureBackendImpl() {
        System.out.println(getTestTraceHead("[NGSIHDFSSinkTest.configure]")
                + "-------- backend.impl can only be 'rest' or 'binary'");
        String backendImpl = "api";
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
        
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
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
        
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
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
        
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
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
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
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
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
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
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
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
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
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
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
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
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
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
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
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
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
    
    /**
     * [NGSIHDFSSink.buildFolderPath] -------- A folder path length greater than 255 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildFolderPathLength() throws Exception {
        System.out.println(getTestTraceHead("[NGSIHDFSSink.buildFolderPath]")
                + "-------- A folder path length greater than 255 characters is detected");
        String backendImpl = null; // default value
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
        String service = "tooLoooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooogService";
        String servicePath = "/tooLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooongServicePath";
        String destination = "tooLoooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooogDestination";
        
        try {
            sink.buildFolderPath(service, servicePath, destination);
            System.out.println(getTestTraceHead("[NGSIHDFSSink.buildFolderPath]")
                    + "- FAIL - A folder path length greater than 255 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIHDFSSink.buildFolderPath]")
                    + "-  OK  - A folder path length greater than 255 characters has been detected");
        } // try catch
    } // testBuildFolderPathLength
    
    /**
     * [NGSIHDFSSink.buildFolderPath] -------- A folder path length greater than 255 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildFilePathLength() throws Exception {
        System.out.println(getTestTraceHead("[NGSIHDFSSink.buildFilePath]")
                + "-------- A file path length greater than 255 characters is detected");
        String backendImpl = null; // default value
        String backendMaxConns = null; // default value
        String backendMaxConnsPerRoute = null; // default value
        String batchSize = null; // default value
        String batchTime = null; // default value
        String batchTTL = null; // default value
        String csvSeparator = null; // default value
        String dataModel = null; // default value
        String enableEncoding = null; // default value
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
        sink.configure(createContext(backendImpl, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTime,
                batchTTL, csvSeparator, dataModel, enableEncoding, enableGrouping, enableLowercase, fileFormat, host,
                password, port, username, hive, krb5, token, serviceAsNamespace));
        String service = "tooLoooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooogService";
        String servicePath = "/tooLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooongServicePath";
        String destination = "tooLoooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooogDestination";
        
        try {
            sink.buildFilePath(service, servicePath, destination);
            System.out.println(getTestTraceHead("[NGSIHDFSSink.buildFilePath]")
                    + "- FAIL - A file path length greater than 255 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSIHDFSSink.buildFilePath]")
                    + "-  OK  - A file path length greater than 255 characters has been detected");
        } // try catch
    } // testBuildFilePathLength
    
    private Context createContext(String backendImpl, String backendMaxConns, String backendMaxConnsPerRoute,
            String batchSize, String batchTime, String batchTTL, String csvSeparator, String dataModel,
            String enableEncoding, String enableGrouping, String enableLowercase, String fileFormat, String host,
            String password, String port, String username, String hive, String krb5, String token,
            String serviceAsNamespace) {
        Context context = new Context();
        context.put("backend.impl", backendImpl);
        context.put("backend.max_conns", backendMaxConns);
        context.put("backend.max_conns_per_route", backendMaxConnsPerRoute);
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
