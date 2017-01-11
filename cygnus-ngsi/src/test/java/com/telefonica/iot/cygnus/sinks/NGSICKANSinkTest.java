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

import static org.junit.Assert.*; // this is required by "fail" like assertions
import com.telefonica.iot.cygnus.sinks.Enums.DataModel;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Test;

/**
 *
 * @author fgalan
 */
@RunWith(MockitoJUnitRunner.class)
public class NGSICKANSinkTest {
    
    /**
     * Constructor.
     */
    public NGSICKANSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSICKANSinkTest

    /**
     * [NGSICKANSink.configure] -------- When not configured, not mandatory parameters get default values.
     */
    @Test
    public void testConfigureDefaults() {
        System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                + "-------- When not configured, not mandatory parameters get default values");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        
        try {
            assertTrue(sink.getRowAttrPersistence());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'attr_persistence=row' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'attr_persistence=row' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals("nokey", sink.getAPIKey());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'api_key=nokey' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'api_key=nokey' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(500, sink.getBackendMaxConns());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'backend.max_conns=500' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'backend.max_conns=500' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(100, sink.getBackendMaxConnsPerRoute());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'backend.max_conns_per_route=100' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'backend.max_conns_per_route=100' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(1, sink.getBatchSize());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'batch_size=1' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'batch_size=1' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(30, sink.getBatchTimeout());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'batch_timeout=30' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'batch_timeout=30' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(10, sink.getBatchTTL());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'batch_ttl=30' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'batch_ttl=30' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(DataModel.DMBYENTITY, sink.getDataModel());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'data_model=dm-by-entity' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'data_model=dm-by-entity' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getEnableEncoding());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'enable_encoding=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'enable_encoding=false' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getEnableGrouping());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'enable_grouping=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'enable_grouping=false' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(sink.getEnableLowerCase());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'enable_lowercase=true' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'enable_lowercase=true' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals("localhost", sink.getCKANHost());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'ckan_host=localhost' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'ckan_host=localhost' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals("80", sink.getCKANPort());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'ckan_port=80' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'ckan_port=80' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getSSL());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'ssl=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'ssl=false' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals("recline_grid_view", sink.getCKANViewer());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'ckan_viewer=recline_grid_view' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'ckan_viewer=recline_grid_view' not configured by default");
            throw e;
        } // try catch
    } // testConfigureDefaults
    
    /**
     * [NGSICKANSink.configure] -------- Parameters get the configured value.
     */
    @Test
    public void testConfigureGetConfiguration() {
        System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                + "-------- Parameters gets the configured value");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = "25";
        String backendMaxConnsPerRoute = "3";
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "falso";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = "recline_view";
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        
        try {
            assertEquals(25, sink.getBackendMaxConns());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'backend.max_conns=25' was configured");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'backend.max_conns=25' was not configured");
            throw e;
        } // try catch
        
        try {
            assertEquals(3, sink.getBackendMaxConnsPerRoute());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'backend.max_conns_per_route=3' was configured");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'backend.max_conns_per_route=3' was not configured");
            throw e;
        } // try catch
        
        try {
            assertEquals("recline_view", sink.getCKANViewer());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'ckan_viewer=recline_view' was configured");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'ckan_viewer=recline_view' was not configured");
            throw e;
        } // try catch
    } // testConfigureGetConfiguration
    
    /**
     * [NGSICKANSink.configure] -------- enable_encoding can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableEncoding() {
        System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                + "-------- enable_encoding can only be 'true' or 'false'");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "falso";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'enable_encoding=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'enable_encoding=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableEncoding
    
    /**
     * [NGSICKANSink.configure] -------- enable_lowercase can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableLowercase() {
        System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                + "-------- enable_lowercase can only be 'true' or 'false'");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = "falso";
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'enable_lowercase=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'enable_lowercase=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableLowercase
    
    /**
     * [NGSICKANSink.configure] -------- enable_grouping can only be 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableGrouping() {
        System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                + "-------- enable_grouping can only be 'true' or 'false'");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = "falso";
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'enable_grouping=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'enable_grouping=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableGrouping
    
    /**
     * [NGSICKANSink.configure] -------- data_model can only be 'dm-by-entity'.
     */
    // TBD: check for dataModel values in NGSIMySQLSink and uncomment this test.
    // @Test
    public void testConfigureDataModel() {
        System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                + "-------- data_model can only be 'dm-by-entity'");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'data_model=dm-by-service' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'data_model=dm-by-service' was not detected");
            throw e;
        } // try catch
    } // testConfigureDataModel
    
    /**
     * [NGSICKANSink.configure] -------- attr_persistence can only be 'row' or 'column'.
     */
    @Test
    public void testConfigureAttrPersistence() {
        System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                + "-------- attr_persistence can only be 'row' or 'column'");
        String apiKey = null; // default
        String attrPersistence = "fila";
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "-  OK  - 'attr_persistence=fila' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.configure]")
                    + "- FAIL - 'attr_persistence=fila' was not detected");
            throw e;
        } // try catch
    } // testConfigureAttrPersistence
    
    /**
     * [NGSICKANSink.buildOrgName] -------- When no encoding, the org name is equals to the encoding of the
     * notified/defaulted service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildOrgNameNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                + "-------- When no encoding, the org name is equals to the encoding of the notified/defaulted "
                + "service");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String service = "someService";
        
        try {
            String builtOrgName = sink.buildOrgName(service);
            String expectedOrgName = "someservice";
        
            try {
                assertEquals(expectedOrgName, builtOrgName);
                System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                        + "-  OK  - '" + expectedOrgName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                        + "- FAIL - '" + expectedOrgName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildOrgNameNoEncoding
    
    /**
     * [NGSICKANSink.buildOrgName] -------- When encoding, the org name is equals to the encoding of the
     * notified/defaulted service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildOrgNameEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                + "-------- When encoding, the org name is equals to the encoding of the notified/defaulted service");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "true";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String service = "someService";
        
        try {
            String builtOrgName = sink.buildOrgName(service);
            String expectedOrgName = "somex0053ervice";
        
            try {
                assertEquals(expectedOrgName, builtOrgName);
                System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                        + "-  OK  - '" + expectedOrgName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                        + "- FAIL - '" + expectedOrgName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildOrgNameEncoding
    
    /**
     * [NGSICKANSink.buildPkgName] -------- When no encoding and when using a notified/defaulted non root service path,
     * the pkg name is equals to the encoding of the concatenation of the notified/defaulted service and the
     * notified/defaulted service path.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildPkgNameNonRootServicePathNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                + "-------- When no encoding and when using a notified/defaulted non root service path, the pkg name "
                + "is equals to the encoding of the concatenation of the notified/defaulted service and the "
                + "notified/defaulted service path");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String service = "someService";
        String servicePath = "/someServicePath";
        
        try {
            String builtPkgName = sink.buildPkgName(service, servicePath);
            String expectedPkgName = "someservice_someservicepath";
        
            try {
                assertEquals(expectedPkgName, builtPkgName);
                System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                        + "-  OK  - '" + expectedPkgName + "' is equals to the encoding of "
                        + "<service>xffff<servicePath>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                        + "- FAIL - '" + expectedPkgName + "' is not equals to the encoding of "
                        + "<service>xffff<servicePath>");
                throw e;
            } // try catch // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildPkgNameNonRootServicePathNoEncoding
    
    /**
     * [NGSICKANSink.buildPkgName] -------- When encoding and when using a notified/defaulted non root service path,
     * the pkg name is equals to the encoding of the concatenation of the notified/defaulted service and the
     * notified/defaulted service path.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildPkgNameNonRootServicePathEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                + "-------- When encoding and when using a notified/defaulted non root service path, the pkg name is "
                + "equals to the encoding of the concatenation of the notified/defaulted service and the "
                + "notified/defaulted service path");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "true";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String service = "someService";
        String servicePath = "/someServicePath";
        
        try {
            String builtPkgName = sink.buildPkgName(service, servicePath);
            String expectedPkgName = "somex0053ervicexffffx002fsomex0053ervicex0050ath";
        
            try {
                assertEquals(expectedPkgName, builtPkgName);
                System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                        + "-  OK  - '" + expectedPkgName + "' is equals to the encoding of "
                        + "<service>xffff<servicePath>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                        + "- FAIL - '" + expectedPkgName + "' is not equals to the encoding of "
                        + "<service>xffff<servicePath>");
                throw e;
            } // try catch // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildPkgNameNonRootServicePathEncoding
    
    /**
     * [NGSICKANSink.buildPkgName] -------- When no encoding and when using a notified/defaulted root service path, the
     * pkg name is equals to the encoding of the concatenation of the notified/defaulted service and the
     * notified/defaulted service path.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildPkgNameRootServicePathNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                + "-------- When no encoding and when using a notified/defaulted root service path, the pkg name is "
                + "equals to the encoding of the concatenation of the notified/defaulted service and the "
                + "notified/defaulted service path");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String service = "someService";
        String servicePath = "/";
        
        try {
            String builtPkgName = sink.buildPkgName(service, servicePath);
            String expectedPkgName = "someservice";
        
            try {
                assertEquals(expectedPkgName, builtPkgName);
                System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                        + "-  OK  - '" + expectedPkgName + "' is equals to the encoding of "
                        + "<service>xffff<servicePath>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                        + "- FAIL - '" + expectedPkgName + "' is not equals to the encoding of "
                        + "<service>xffff<servicePath>");
                throw e;
            } // try catch // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildPkgNameRootServicePathNoEncoding
    
    /**
     * [NGSICKANSink.buildPkgName] -------- When encoding and when using a notified/defaulted root service path, the
     * pkg name is equals to the encoding of the concatenation of the notified/defaulted service and the
     * notified/defaulted service path.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildPkgNameRootServicePathEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                + "-------- When encoding and when using a notified/defaulted root service path, the pkg name is "
                + "equals to the encoding of the concatenation of the notified/defaulted service and the "
                + "notified/defaulted service path");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "true";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String service = "someService";
        String servicePath = "/";
        
        try {
            String builtPkgName = sink.buildPkgName(service, servicePath);
            String expectedPkgName = "somex0053ervicexffffx002f";
        
            try {
                assertEquals(expectedPkgName, builtPkgName);
                System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                        + "-  OK  - '" + expectedPkgName + "' is equals to the encoding of "
                        + "<service>xffff<servicePath>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                        + "- FAIL - '" + expectedPkgName + "' is not equals to the encoding of "
                        + "<service>xffff<servicePath>");
                throw e;
            } // try catch // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildPkgNameRootServicePathEncoding
    
    /**
     * [NGSICKANSink.buildResName] -------- When no encoding, the CKAN resource name is the encoding of the
     * concatenation of the notified \<entity_id\> and \<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildResourceNameNoEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                + "-------- When no encoding, the CKAN resource name is the encoding of the concatenation of the "
                + "notified <entityId> and <entityType>");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String entity = "someId=someType";
        
        try {
            String builtResName = sink.buildResName(entity);
            String expecetedResName = "someid_sometype";
        
            try {
                assertEquals(expecetedResName, builtResName);
                System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                        + "-  OK  - '" + builtResName + "' is equals to the encoding of <entityId> and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                        + "- FAIL - '" + builtResName + "' is not equals to the encoding of <entityId> and "
                        + "<entityType>");
                throw e;
            } // try catch // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildResourceNameNoEncoding
    
    /**
     * [NGSICKANSink.buildResName] -------- When encoding, the CKAN resource name is the encoding of the concatenation
     * of the notified \<entity_id\> and \<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildResourceNameEncoding() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                + "-------- When encoding, the CKAN resource name is the encoding of the concatenation of the "
                + "notified <entityId> and <entityType>");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = "true";
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String entity = "someId=someType";
        
        try {
            String builtResName = sink.buildResName(entity);
            String expecetedResName = "somex0049dxffffsomex0054ype";
        
            try {
                assertEquals(expecetedResName, builtResName);
                System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                        + "-  OK  - '" + builtResName + "' is equals to the encoding of <entityId> and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                        + "- FAIL - '" + builtResName + "' is not equals to the encoding of <entityId> and "
                        + "<entityType>");
                throw e;
            } // try catch // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildResourceNameEncoding
    
    /**
     * [NGSICKANSink.buildOrgName] -------- An organization name length greater than 100 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildOrganizationNameLength() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                + "-------- An organization name length greater than 100 characters is detected");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String service = "veryLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "ooooogService";
        
        try {
            sink.buildOrgName(service);
            System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                    + "- FAIL - An organization name length greater than 100 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                    + "-  OK  - An organization name length greater than 100 characters has been detected");
        } // try catch
    } // testBuildOrganizationNameLength
    
    /**
     * [NGSICKANSink.buildPkgName] -------- A package name length greater than 100 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildPackageNameLength() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                + "-------- A resource name length greater than 100 characters is detected");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String service = "veryLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "ooooogService";
        String servicePath = "veryLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "ooooooooogServicePath";
        
        try {
            sink.buildPkgName(service, servicePath);
            System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                    + "- FAIL - A package name length greater than 100 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                    + "-  OK  - A package name length greater than 100 characters has been detected");
        } // try catch
    } // testBuildPackageNameLength
    
    /**
     * [NGSICKANSink.buildResName] -------- A resource name length greater than 100 characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildResourceNameLength() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                + "-------- A resource name length greater than 100 characters is detected");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; // default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String entity = "veryLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "ooooogEntity";
        
        try {
            sink.buildResName(entity);
            System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                    + "- FAIL - A resource name length greater than 100 characters has not been detected");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                    + "-  OK  - A resource name length greater than 100 characters has been detected");
        } // try catch
    } // testBuildResourceNameLength
    
    private Context createContext(String apiKey, String attrPersistence, String backendMaxConns,
            String backendMaxConnsPerRoute, String batchSize, String batchTime, String batchTTL, String dataModel,
            String enableEncoding, String enableGrouping, String enableLowercase, String host, String port,
            String ssl, String viewer) {
        Context context = new Context();
        context.put("api_key", apiKey);
        context.put("attr_persistence", attrPersistence);
        context.put("backend.max_conns", backendMaxConns);
        context.put("backend.max_conns_per_route", backendMaxConnsPerRoute);
        context.put("batch_size", batchSize);
        context.put("batch_time", batchTime);
        context.put("batch_ttl", batchTTL);
        context.put("ckan_host", host);
        context.put("ckan_port", port);
        context.put("ckan_viewer", viewer);
        context.put("data_model", dataModel);
        context.put("enable_encoding", enableEncoding);
        context.put("enable_grouping", enableGrouping);
        context.put("enable_lowercase", enableLowercase);
        context.put("ssl", ssl);
        return context;
    } // createContext
    
} // NGSICKANSinkTest
