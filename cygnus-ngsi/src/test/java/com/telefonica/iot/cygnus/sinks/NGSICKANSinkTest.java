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

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericColumnAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericRowAggregator;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.sinks.Enums.DataModel;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;

import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.apache.flume.Context;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.FATAL);
        ctx.updateLoggers();
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
        String entityId = ""; //defalut
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String service = "someService";
        String servicePath = "/someServicePath";
        
        try {
            String builtPkgName = sink.buildPkgName(service, servicePath, entityId);
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
        String entityId = ""; //defalut
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String service = "someService";
        String servicePath = "/someServicePath";
        
        try {
            String builtPkgName = sink.buildPkgName(service, servicePath, entityId);
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
        String entityId = ""; //defalut
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String service = "someService";
        String servicePath = "/";
        
        try {
            String builtPkgName = sink.buildPkgName(service, servicePath, entityId);
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
        String entityId = ""; //defalut
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String service = "someService";
        String servicePath = "/";
        
        try {
            String builtPkgName = sink.buildPkgName(service, servicePath, entityId);
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
        String entityId = ""; //defalut
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String entity = "someId=someType";
        
        try {
            String builtResName = sink.buildResName(entity, entityId);
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
        String entityId = ""; //defalut
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String entity = "someId=someType";
        
        try {
            String builtResName = sink.buildResName(entity, entityId);
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
        String entityId = ""; //defalut
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String service = "veryLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "ooooogService";
        String servicePath = "veryLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "ooooooooogServicePath";
        
        try {
            sink.buildPkgName(service, servicePath, entityId);
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
        String entityId = ""; //defalut
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        String entity = "veryLooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "ooooogEntity";
        
        try {
            sink.buildResName(entity, entityId);
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

    private Context createContextforNativeTypes(String backendImpl, String backendMaxConns, String backendMaxConnsPerRoute,
                                                String batchSize, String batchTime, String batchTTL, String csvSeparator, String dataModel,
                                                String enableEncoding, String enableGrouping, String enableLowercase, String fileFormat, String host,
                                                String password, String port, String username, String hive, String krb5, String token,
                                                String serviceAsNamespace, String attrNativeTypes, String metadata) {
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
        context.put("attr_native_types", attrNativeTypes);
        context.put("attr_metadata_store", metadata);
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

    public NGSIBatch prepaireBatch() {
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
        return batch;
    }

    @Test
    public void testNativeTypeColumnBatch() throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError, CygnusBadContextData {
        NGSICKANSink ngsickanSink= new NGSICKANSink();
        ngsickanSink.configure(createContextforNativeTypes(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null));
        NGSIBatch batch = prepaireBatch();
        String destination = "someDestination";
        String entityId = ""; //default
        try {
            batch.startIterator();
            NGSIGenericAggregator aggregator = new NGSIGenericColumnAggregator();
            while (batch.hasNext()) {
                destination = batch.getNextDestination();
                ArrayList<NGSIEvent> events = batch.getNextEvents();
                aggregator.setService(events.get(0).getServiceForNaming(false));
                aggregator.setServicePathForData(events.get(0).getServicePathForData());
                aggregator.setServicePathForNaming(events.get(0).getServicePathForNaming(false, false));
                aggregator.setEntityForNaming(events.get(0).getEntityForNaming(false, false, false));
                aggregator.setEntityType(events.get(0).getEntityTypeForNaming(false, false));
                aggregator.setAttribute(events.get(0).getAttributeForNaming(false));
                aggregator.setEnableUTCRecvTime(true);
                aggregator.setOrgName(ngsickanSink.buildOrgName(aggregator.getService()));
                aggregator.setPkgName(ngsickanSink.buildPkgName(aggregator.getService(), aggregator.getServicePathForNaming(), entityId));
                aggregator.setResName(ngsickanSink.buildResName(aggregator.getEntityForNaming(), entityId));
                aggregator.initialize(events.get(0));
                aggregator.setAttrMetadataStore(true);
                for (NGSIEvent event : events) {
                    aggregator.aggregate(event);
                } // for
            }
            ArrayList<JsonObject> jsonObjects = NGSIUtils.linkedHashMapToJsonListWithOutEmptyMD(aggregator.getAggregationToPersist());
            String aggregation = "";
            for (JsonObject jsonObject : jsonObjects) {
                if (aggregation.isEmpty()) {
                    aggregation = jsonObject.toString();
                } else {
                    aggregation += "," + jsonObject;
                }
            }
            System.out.println(aggregation);
            String correctBatch = "{\"recvTime\":\"2016-04-20T07:19:55.801Z\",\"fiwareServicePath\":\"somePath\",\"entityId\":\"someId\",\"entityType\":\"someType\",\"someNumber\":2,\"somneBoolean\":true,\"someDate\":\"2016-09-21T01:23:00.00Z\",\"someGeoJson\":\"{\\\"type\\\": \\\"Point\\\",\\\"coordinates\\\": [-0.036177,39.986159]}\",\"someJson\":\"{\\\"String\\\": \\\"string\\\"}\",\"someString\":\"foo\",\"someString2\":\"\"},{\"recvTime\":\"2016-04-20T07:19:55.801Z\",\"fiwareServicePath\":\"somePath\",\"entityId\":\"someId\",\"entityType\":\"someType\",\"someName1\":\"-3.7167, 40.3833\",\"someName1_md\":[{\"name\":\"location\",\"type\":\"string\",\"value\":\"WGS84\"}],\"someName2\":\"someValue2\"}";
            assertEquals(aggregation, correctBatch);
        } catch (Exception e) {
            fail();
        }
    } // testNativeTypeColumnBatch

    /**
     * [NGSICKANSink.buildOrgName] -------- When encoding, the org name is equals to the encoding of the
     * notified/defaulted service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildOrgNamePkgByEntity() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                + "-------- When confOrganization, the org name is equals to subservice/fiwareServicePath");
        String apiKey = null; // default
        String attrPersistence = null; // default
        String backendMaxConns = null; // default
        String backendMaxConnsPerRoute = null; // default
        String batchSize = null; // default
        String batchTime = null; // default
        String batchTTL = null; // default
        String dataModel = null; // default
        String enableEncoding = null; //default
        String enableGrouping = null; // default
        String enableLowercase = null; // default
        String host = null; // default
        String port = null; // default
        String ssl = null; // default
        String viewer = null; // default
        String entityId = "entityId";
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        sink.dataModel=DataModel.DMBYENTITYID;
        
        String subService = "someSubService";
        
        try {
            String builtOrgName = sink.buildOrgName(subService); 
            String expectedOrgName = "someSubService";
        
            try {
                assertEquals(expectedOrgName, builtOrgName);
                System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                        + "-  OK  - '" + expectedOrgName + "' is equals to the subservice");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                        + "- FAIL - '" + expectedOrgName + "' is not equals to the subservice");
                throw e;
            } // try catch // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.buildOrgName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildOrgNamePkgByEntity
    
    /**
     * [NGSICKANSink.buildPkgName] -------- When confOrganization, the pkgName is equals to entityId
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildPkgNamePkgByEntity() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                + "-------- When confOrganization, the pkgName is equals to entityId");
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
        String entityId = "entityId";
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        sink.dataModel=DataModel.DMBYENTITYID;
        String service = "someService";
        String servicePath = "/someServicePath";
        
        try {
            String builtPkgName = sink.buildPkgName(service, servicePath, entityId);
            String expectedPkgName = "entityId";
        
            try {
                assertEquals(expectedPkgName, builtPkgName);
                System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                        + "-  OK  - '" + expectedPkgName + "' is equals to the entityId");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                        + "- FAIL - '" + expectedPkgName + "' is not equals to the entityId");
                throw e;
            } // try catch // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildPkgNamePkgByEntity
    
    /**
     * [NGSICKANSink.buildPkgName] -------- When confOrganization, the resource name is equals to entityId
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildResourceNamePkgByEntity() throws Exception {
        System.out.println(getTestTraceHead("[NGSICKANSink.buildPkgName]")
                + "-------- When confOrganization, the resourceName is equals to entityId");
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
        String entityId = "entityId";
        NGSICKANSink sink = new NGSICKANSink();
        sink.configure(createContext(apiKey, attrPersistence, backendMaxConns, backendMaxConnsPerRoute, batchSize,
                batchTime, batchTTL, dataModel, enableEncoding, enableGrouping, enableLowercase, host, port, ssl,
                viewer));
        sink.dataModel=DataModel.DMBYENTITYID;
        String service = "someService";
        String servicePath = "/someServicePath";
        
        try {
            String builtPkgName = sink.buildPkgName(service, servicePath, entityId);
            String expectedPkgName = "entityId";
        
            try {
                assertEquals(expectedPkgName, builtPkgName);
                System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                        + "-  OK  - '" + expectedPkgName + "' is equals to the entityId");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                        + "- FAIL - '" + expectedPkgName + "' is not equals to the entityId");
                throw e;
            } // try catch // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSICKANSink.buildResName]")
                    + "- FAIL - There was some problem when building the DB name");
            throw e;
        } // try catch
    } // testBuildResourceNamePkgByEntity

    @Test
    public void testNativeTypeRowBatch() throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError, CygnusBadContextData {
        NGSICKANSink ngsickanSink= new NGSICKANSink();
        ngsickanSink.configure(createContextforNativeTypes(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null));
        NGSIBatch batch = prepaireBatch();
        String destination = "someDestination";
        String entityId = "";
        try {
            batch.startIterator();
            NGSIGenericAggregator aggregator = new NGSIGenericRowAggregator();
            while (batch.hasNext()) {
                destination = batch.getNextDestination();
                ArrayList<NGSIEvent> events = batch.getNextEvents();
                aggregator.setService(events.get(0).getServiceForNaming(false));
                aggregator.setServicePathForData(events.get(0).getServicePathForData());
                aggregator.setServicePathForNaming(events.get(0).getServicePathForNaming(false, false));
                aggregator.setEntityForNaming(events.get(0).getEntityForNaming(false, false, false));
                aggregator.setEntityType(events.get(0).getEntityTypeForNaming(false, false));
                aggregator.setAttribute(events.get(0).getAttributeForNaming(false));
                aggregator.setEnableUTCRecvTime(true);
                aggregator.setOrgName(ngsickanSink.buildOrgName(aggregator.getService()));
                aggregator.setPkgName(ngsickanSink.buildPkgName(aggregator.getService(), aggregator.getServicePathForNaming(), entityId));
                aggregator.setResName(ngsickanSink.buildResName(aggregator.getEntityForNaming(), entityId));
                aggregator.initialize(events.get(0));
                aggregator.setAttrMetadataStore(true);
                for (NGSIEvent event : events) {
                    aggregator.aggregate(event);
                } // for
            }
            ArrayList<JsonObject> jsonObjects = NGSIUtils.linkedHashMapToJsonListWithOutEmptyMD(aggregator.getAggregationToPersist());
            String aggregation = "";
            for (JsonObject jsonObject : jsonObjects) {
                if (aggregation.isEmpty()) {
                    aggregation = jsonObject.toString();
                } else {
                    aggregation += "," + jsonObject;
                }
            }
            System.out.println(aggregation);
            String correctBatch = "{\"recvTimeTs\":\"1461136795801\",\"recvTime\":\"2016-04-20T07:19:55.801Z\",\"fiwareServicePath\":\"somePath\",\"entityId\":\"someId\",\"entityType\":\"someType\",\"attrName\":\"someNumber\",\"attrType\":\"number\",\"attrValue\":2},{\"recvTimeTs\":\"1461136795801\",\"recvTime\":\"2016-04-20T07:19:55.801Z\",\"fiwareServicePath\":\"somePath\",\"entityId\":\"someId\",\"entityType\":\"someType\",\"attrName\":\"somneBoolean\",\"attrType\":\"Boolean\",\"attrValue\":true},{\"recvTimeTs\":\"1461136795801\",\"recvTime\":\"2016-04-20T07:19:55.801Z\",\"fiwareServicePath\":\"somePath\",\"entityId\":\"someId\",\"entityType\":\"someType\",\"attrName\":\"someDate\",\"attrType\":\"DateTime\",\"attrValue\":\"2016-09-21T01:23:00.00Z\"},{\"recvTimeTs\":\"1461136795801\",\"recvTime\":\"2016-04-20T07:19:55.801Z\",\"fiwareServicePath\":\"somePath\",\"entityId\":\"someId\",\"entityType\":\"someType\",\"attrName\":\"someGeoJson\",\"attrType\":\"geo:json\",\"attrValue\":\"{\\\"type\\\": \\\"Point\\\",\\\"coordinates\\\": [-0.036177,39.986159]}\"},{\"recvTimeTs\":\"1461136795801\",\"recvTime\":\"2016-04-20T07:19:55.801Z\",\"fiwareServicePath\":\"somePath\",\"entityId\":\"someId\",\"entityType\":\"someType\",\"attrName\":\"someJson\",\"attrType\":\"json\",\"attrValue\":\"{\\\"String\\\": \\\"string\\\"}\"},{\"recvTimeTs\":\"1461136795801\",\"recvTime\":\"2016-04-20T07:19:55.801Z\",\"fiwareServicePath\":\"somePath\",\"entityId\":\"someId\",\"entityType\":\"someType\",\"attrName\":\"someString\",\"attrType\":\"string\",\"attrValue\":\"foo\"},{\"recvTimeTs\":\"1461136795801\",\"recvTime\":\"2016-04-20T07:19:55.801Z\",\"fiwareServicePath\":\"somePath\",\"entityId\":\"someId\",\"entityType\":\"someType\",\"attrName\":\"someString2\",\"attrType\":\"string\",\"attrValue\":\"\"},{\"recvTimeTs\":\"1461136795801\",\"recvTime\":\"2016-04-20T07:19:55.801Z\",\"fiwareServicePath\":\"somePath\",\"entityId\":\"someId\",\"entityType\":\"someType\",\"attrName\":\"someName1\",\"attrType\":\"someType1\",\"attrValue\":\"-3.7167, 40.3833\",\"attrMd\":[{\"name\":\"location\",\"type\":\"string\",\"value\":\"WGS84\"}]},{\"recvTimeTs\":\"1461136795801\",\"recvTime\":\"2016-04-20T07:19:55.801Z\",\"fiwareServicePath\":\"somePath\",\"entityId\":\"someId\",\"entityType\":\"someType\",\"attrName\":\"someName2\",\"attrType\":\"someType2\",\"attrValue\":\"someValue2\"}";
            assertEquals(correctBatch, aggregation);
        } catch (Exception e) {
            fail();
        }
    } // testNativeTypeRowBatch


} // NGSICKANSinkTest
