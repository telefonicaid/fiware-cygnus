/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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

import com.google.gson.JsonPrimitive;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextMetadata;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.sinks.NGSICartoDBSink.CartoDBAggregator;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author frb
 */
public class NGSICartoDBSinkTest {
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    /**
     * Constructor.
     */
    public NGSICartoDBSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSICartoDBSinkTest
    
    /**
     * [NGSICartoDBSink.configure] -------- When not configured, not mandatory parameters get default values.
     */
    @Test
    public void testConfigureDefaults() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- When not configured, not mandatory parameters get default values");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null;
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null;
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        try {
            assertEquals(500, sink.getBackendMaxConns());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'backend.max_conns=500' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'backend.max_conns=500' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(100, sink.getBackendMaxConnsPerRoute());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'backend.max_conns_per_route=100' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'backend.max_conns_per_route=100' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(1, sink.getBatchSize());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'batch_size=1' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'batch_size=1' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(30, sink.getBatchTimeout());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'batch_timeout=30' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'batch_timeout=30' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(10, sink.getBatchTTL());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'batch_ttl=30' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'batch_ttl=30' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(Enums.DataModel.DMBYENTITY, sink.getDataModel());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'data_model=dm-by-entity' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'data_model=dm-by-entity' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getEnableGrouping());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'enable_grouping=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'enable_grouping=false' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(sink.getEnableLowerCase());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'enable_lowercase=true' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'enable_lowercase=true' not configured by default");
            throw e;
        } // try catch

        try {
            assertTrue(sink.getEnableRawHistoric());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'enable_raw=true' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'enable_raw=true' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getEnableRawSnapshot());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'enable_raw_snapshot=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'enable_raw_snapshot=false' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getEnableDistanceHistoric());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'enable_distance=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'enable_distance=false' not configured by default");
            throw e;
        } // try catch
    } // testConfigureDefaults
    
    /**
     * [NGSICartoDBSink.configure] -------- backend.max_conns gets the configured value.
     */
    @Test
    public void testConfigureMaxConns() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- backend.max_conns gets the configured value");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = "25";
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null;
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        try {
            assertEquals(25, sink.getBackendMaxConns());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'backend.max_conns=25' was configured");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'backend.max_conns=25' was not configured");
            throw e;
        } // try catch
    } // testConfigureMaxConns
    
    /**
     * [NGSICartoDBSink.configure] -------- backend.max_conns_per_route gets the configured value.
     */
    @Test
    public void testConfigureMaxConnsPerRoute() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- backend.max_conns_per_route gets the configured value");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = "3";
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null;
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        try {
            assertEquals(3, sink.getBackendMaxConnsPerRoute());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'backend.max_conns_per_route=3' was configured");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'backend.max_conns_per_route=3' was not configured");
            throw e;
        } // try catch
    } // testConfigureMaxConnsPerRoute
    
    /**
     * [NGSICartoDBSink.configure] -------- Independently of the configured value, enable_lowercase is always 'true'
     * by default.
     */
    @Test
    public void testConfigureEnableLowercaseAlwaysTrue() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- Independently of the configured value, enable_lowercase is always 'true' by default");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = "false";
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        try {
            assertTrue(sink.enableLowercase);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'enable_lowercase=false' was configured, nevertheless it is always true by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'enable_lowercase=false' was configured and it is really false");
            throw e;
        } // try catch
    } // testConfigureEnableLowercaseAlwaysTrue
    
    /**
     * [NGSICartoDBSink.configure] -------- Configured 'swap_coordinates' cannot be different than 'true' or 'false'.
     */
    @Test
    public void testConfigureSwapCoordinatesOK() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- Configured 'swap_coordinates' cannot be different than 'true' or 'false'");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = "falso"; // wrong value
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        try {
            assertTrue(sink.enableLowercase);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'swap_coordinates=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'swap_coordinates=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureSwapCoordinatesOK
    
    /**
     * [NGSICartoDBSink.configure] -------- Configured 'enable_raw_historic' cannot be different than 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableRawHistoricOK() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- Configured 'enable_raw' cannot be different than 'true' or 'false'");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = "falso"; // wrong value
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default value
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        try {
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'enable_raw=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'enable_raw=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableRawHistoricOK
    
    /**
     * [NGSICartoDBSink.configure] -------- Configured 'enable_distance_historic' cannot be different than 'true' or
     * 'false'.
     */
    @Test
    public void testConfigureEnableDistanceHistoricOK() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- Configured 'enable_distance' cannot be different than 'true' or 'false'");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = "falso"; // wrong value
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        try {
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'enable_distance=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'enable_distance=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableDistanceHistoricOK
    
    /**
     * [NGSICartoDBSink.configure] -------- Configured 'enable_raw_snapshot' cannot be different than 'true' or 'false'.
     */
    @Test
    public void testConfigureEnableRawSnapshotOK() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- Configured 'enable_raw_snapshot' cannot be different than 'true' or 'false'");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default value
        String enableRawSnapshot = "falso"; // wrong value
        String swapCoordinates = null; // default value
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        try {
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'enable_raw_snapshot=falso' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'enable_raw_snapshot=falso' was not detected");
            throw e;
        } // try catch
    } // testConfigureEnableRawSnapshotOK
    
    /**
     * [NGSICartoDBSink.configure] -------- Configured 'keys_conf_file' cannot be empty.
     */
    @Test
    public void testConfigureKeysConfFileOK() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- Configured 'keys_conf_file' cannot be empty");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default_one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = null; // empty file
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        try {
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - Empty 'keys_conf_file' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - Empty 'keys_conf_file' was not detected");
            throw e;
        } // try catch
    } // testConfigureKeysConfFileOK
    
    /**
     * [NGSICartoDBSink.start] -------- When started, a CartoDB backend is created from a valid keys file.
     */
    @Test
    public void testStartBackendCreated() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                + "-------- When started, a CartoDB backend is created from a valid keys file");
        File file;
        
        try {
            file = folder.newFile("keys.conf");
            PrintWriter out = new PrintWriter(file);
            out.println("{\"cartodb_keys\":[{\"username\":\"frb\",\"endpoint\":\"http://frb.com\","
                    + "\"key\":\"xxx\",\"type\":\"personal\"}]}");
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - There was some problem when mocking the keys file");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = file.getAbsolutePath();
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        sink.setChannel(new MemoryChannel());
        sink.start();
        
        try {
            assertTrue(sink.getBackends() != null);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "-  OK  - A CartoDB backend has been created");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - A CartoDB backend has not been created");
            throw e;
        } // try catch
    } // testStartBackendCreated
    
    /**
     * [NGSICartoDBSink.start] -------- Username field must appear within an entry in the keys file.
     */
    @Test
    public void testStartUsernameNotNullKeysFile() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                + "-------- Username field must appear within an entry in the keys file");
        File file;
        
        try {
            file = folder.newFile("keys.conf");
            PrintWriter out = new PrintWriter(file);
            out.println("{\"cartodb_keys\":[{\"endpoint\":\"http://frb.com\",\"key\":\"xxx\"}]}");
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - There was some problem when mocking the keys file");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = file.getAbsolutePath();
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        sink.setChannel(new MemoryChannel());
        sink.start();
        
        try {
            assertEquals(null, sink.getBackends());
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "-  OK  - Null username has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - Null username has not been detected");
            throw e;
        } // try catch
    } // testStartUsernameNotNullKeysFile
    
    /**
     * [NGSICartoDBSink.start] -------- Username within an entry in the keys file cannot be empty.
     */
    @Test
    public void testStartUsernameNotEmptyKeysFile() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                + "-------- Username within an entry in the keys file cannot be empty");
        File file;
        
        try {
            file = folder.newFile("keys.conf");
            PrintWriter out = new PrintWriter(file);
            out.println("{\"cartodb_keys\":[{\"username\":\"\",\"endpoint\":\"http://frb.com\",\"key\":\"xxx\"}]}");
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - There was some problem when mocking the keys file");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = file.getAbsolutePath();
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        sink.setChannel(new MemoryChannel());
        sink.start();
        
        try {
            assertEquals(null, sink.getBackends());
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "-  OK  - Empty username has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - Empty username has not been detected");
            throw e;
        } // try catch
    } // testStartUsernameNotEmptyKeysFile
    
    /**
     * [NGSICartoDBSink.configure] -------- Configured `data_model` cannot be different than `dm-by-service-path`
     * or `dm-by-entity`.
     */
    @Test
    public void testConfigureDataModelOK() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- Configured `data_model` cannot be different than `dm-by-service-path` or `dm-by-entity`");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-service";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        try {
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'data_model=dm-by-service' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'data_model=dm-by-service' was not detected");
            throw e;
        } // try catch
        
        dataModel = "dm-by-attribute";
        sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        try {
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - 'data_model=dm-by-attribute' was detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - 'data_model=dm-by-attribute' was not detected");
            throw e;
        } // try catch
    } // testConfigureDataModelOK
    
    /**
     * [NGSICartoDBSink.start] -------- Endpoint field must appear within an entry in the keys file.
     */
    @Test
    public void testStartEndpointNotNullKeysFile() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                + "-------- Endpoint field must appear within an entry in the keys file");
        File file;
        
        try {
            file = folder.newFile("keys.conf");
            PrintWriter out = new PrintWriter(file);
            out.println("{\"cartodb_keys\":[{\"username\":\"frb\",\"key\":\"xxx\"}]}");
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - There was some problem when mocking the keys file");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableLowercase = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = file.getAbsolutePath();
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        sink.setChannel(new MemoryChannel());
        sink.start();
        
        try {
            assertEquals(null, sink.getBackends());
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "-  OK  - Null endpoint has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - Null endpoint has not been detected");
            throw e;
        } // try catch
    } // testStartEndpointNotNullKeysFile
    
    /**
     * [NGSICartoDBSink.start] -------- Endpoint within an entry in the keys file cannot be empty.
     */
    @Test
    public void testStartEndpointNotEmptyKeysFile() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                + "-------- Endpoint within an entry in the keys file cannot be empty");
        File file;
        
        try {
            file = folder.newFile("keys.conf");
            PrintWriter out = new PrintWriter(file);
            out.println("{\"cartodb_keys\":[{\"username\":\"frb\",\"endpoint\":\"\",\"key\":\"xxx\"}]}");
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - There was some problem when mocking the keys file");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = file.getAbsolutePath();
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        sink.setChannel(new MemoryChannel());
        sink.start();
        
        try {
            assertEquals(null, sink.getBackends());
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "-  OK  - Empty endpoint has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - Empty endpoint has not been detected");
            throw e;
        } // try catch
    } // testStartEndpointNotEmptyKeysFile
    
    /**
     * [NGSICartoDBSink.start] -------- Endpoint within an entry in the keys file must be a URI using the http or
     * https schema.
     */
    @Test
    public void testStartEndpointSchemaOKKeysFile() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                + "-------- Endpoint within an entry in the keys file must be a URI using the http or https schema");
        File file;
        
        try {
            file = folder.newFile("keys.conf");
            PrintWriter out = new PrintWriter(file);
            out.println("{\"cartodb_keys\":[{\"username\":\"frb\",\"endpoint\":\"htt://frb.com\",\"key\":\"xxx\"}]}");
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - There was some problem when mocking the keys file");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableLowercase = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = file.getAbsolutePath();
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        sink.setChannel(new MemoryChannel());
        sink.start();
        
        try {
            assertEquals(null, sink.getBackends());
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "-  OK  - Invalid 'htt' schema in the endpoint has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - Invalid 'htt' schema in the endpoint has not been detected");
            throw e;
        } // try catch
    } // testStartEndpointSchemaOKKeysFile
    
    /**
     * [NGSICartoDBSink.start] -------- Key field must appear within an entry in the keys file.
     */
    @Test
    public void testStartKeyNotNullKeysFile() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                + "-------- Key field must appear within an entry in the keys file");
        File file;
        
        try {
            file = folder.newFile("keys.conf");
            PrintWriter out = new PrintWriter(file);
            out.println("{\"cartodb_keys\":[{\"username\":\"frb\",\"endpoint\":\"http://frb.com\"}]}");
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - There was some problem when mocking the keys file");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = file.getAbsolutePath();
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        sink.setChannel(new MemoryChannel());
        sink.start();
        
        try {
            assertEquals(null, sink.getBackends());
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "-  OK  - Null key has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - Null key has not been detected");
            throw e;
        } // try catch
    } // testStartKeyNotNullKeysFile
    
    /**
     * [NGSICartoDBSink.start] -------- Key within an entry in the keys file cannot be empty.
     */
    @Test
    public void testStartKeyNotEmptyKeysFile() {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                + "-------- Key within an entry in the keys file cannot be empty");
        File file;
        
        try {
            file = folder.newFile("keys.conf");
            PrintWriter out = new PrintWriter(file);
            out.println("{\"cartodb_keys\":[{\"username\":\"frb\",\"endpoint\":\"http://frb.com\",\"key\":\"\"}]}");
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - There was some problem when mocking the keys file");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = file.getAbsolutePath();
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        sink.setChannel(new MemoryChannel());
        sink.start();
        
        try {
            assertEquals(null, sink.getBackends());
            assertTrue(sink.invalidConfiguration);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "-  OK  - Empty key has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.start]")
                    + "- FAIL - Empty key has not been detected");
            throw e;
        } // try catch
    } // testStartKeyNotEmptyKeysFile
    
    /**
     * [NGSICartoDBSink.buildSchemaName] -------- The schema name is equals to the encoding of the notified/defaulted
     * service.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildSchemaName() throws Exception {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.buildSchemaName]")
                + "-------- The schema name is equals to the encoding of the notified/defaulted service");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null; // default one
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        String service = "someService";
        
        try {
            String builtSchemaName = sink.buildSchemaName(service);
            String expectedSchemaName = "somex0053ervice";
        
            try {
                assertEquals(expectedSchemaName, builtSchemaName);
                System.out.println(getTestTraceHead("[NGSICartoDBSink.buildSchemaName]")
                        + "-  OK  - '" + builtSchemaName + "' is equals to the encoding of <service>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICartoDBSink.buildSchemaName]")
                        + "- FAIL - '" + builtSchemaName + "' is not equals to the encoding of <service>");
                throw e;
            } // try catch
        } catch (CygnusBadConfiguration e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.buildSchemaName]")
                    + "- FAIL - There was some problem when building the schema name");
            throw e;
        } // try catch
    } // testBuildSchemaName

    /**
     * [NGSICartoDBSink.buildTableName] -------- When a non root service-path is notified/defaulted and
     * data_model is 'dm-by-service-path' the CartoDB table name is the encoding of <service-path>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByServicePath() throws Exception {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the CartoDB table name is the encoding of <service-path>");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-service-path";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        String servicePath = "/somePath";
        String entity = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test
        
        try {
            String builtTableName = sink.buildTableName(servicePath, entity, attribute);
            String expecetedTableName = "x002fsomex0050ath";
        
            try {
                assertEquals(expecetedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of <service-path>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of <service-path>");
                throw e;
            } // try catch
        } catch (CygnusBadConfiguration e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByServicePath
    
    /**
     * [NGSICartoDBSink.buildTableName] -------- When a non root service-path is notified/defaulted
     * and data_model is 'dm-by-entity' the CartoDB table name is the lower case of
     * x002f\<service-path\>xffff\<entity_id\>xffff\<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameNonRootServicePathDataModelByEntity() throws Exception {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                + "-------- When a non root service-path is notified/defaulted and data_model is 'dm-by-entity' "
                + "the CartoDB table name is the lower case of x002f<servicePath>xffff<entityId>xffff<entityType>");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-entity";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        String servicePath = "/somePath";
        String entity = "someId=someType"; // using the internal concatenator
        String attribute = null; // irrelevant for this test
        
        try {
            String builtTableName = sink.buildTableName(servicePath, entity, attribute);
        
            try {
                assertEquals("x002fsomex0050athxffffsomex0049dxffffsomex0054ype", builtTableName);
                System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the lower case of "
                        + "x002f<servicePath>xffff<entityId>xffff<entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the lower case of "
                        + "x002f<servicePath>xffff<entityId>xffff<entityType>");
                throw e;
            } // try catch
        } catch (CygnusBadConfiguration e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameNonRootServicePathDataModelByEntity
    
    /**
     * [NGSICartoDBSink.buildTableName] -------- When a root service-path is notified/defaulted and
     * data_model is 'dm-by-service-path' the CartoDB table name is x002f.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByServicePath() throws Exception {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                + "-------- When a root service-path is notified/defaulted and data_model is "
                + "'dm-by-service-path' the CartoDB table name is x002f");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-service-path";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        String servicePath = "/";
        String entity = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test
        
        try {
            String builtTableName = sink.buildTableName(servicePath, entity, attribute);
            
            try {
                assertEquals("x002f", builtTableName);
                System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to x002f");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to x002f");
                throw e;
            } // try catch
        } catch (CygnusBadConfiguration e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByServicePath
    
    /**
     * [NGSICartoDBSink.buildTableName] -------- When a root service-path is notified/defaulted
     * and data_model is 'dm-by-entity' the CartoDB table name is the encoding of the concatenation of
     * \<service-path\>, \<entity_id\> and \<entity_type\>.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameRootServicePathDataModelByEntity() throws Exception {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                + "-------- When a non service-path is notified/defaulted and data_model is 'dm-by-entity' "
                + "the CartoDB table name is the encoding of the concatenation of <service-path>, <entityId> and"
                + "<entityType>");
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-entity";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        String servicePath = "/";
        String entity = "someId=someType";
        String attribute = null; // irrelevant for this test
        
        try {
            String builtTableName = sink.buildTableName(servicePath, entity, attribute);
            String expectedTableName = "x002fxffffsomex0049dxffffsomex0054ype";
        
            try {
                assertEquals(expectedTableName, builtTableName);
                System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                        + "-  OK  - '" + builtTableName + "' is equals to the encoding of the concatenation of "
                        + "<service-path>, <entityId> and <entityType>");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                        + "- FAIL - '" + builtTableName + "' is not equals to the encoding of the concatenation of "
                        + "<service-path>, <entityId> and <entityType>");
                throw e;
            } // try catch
        } catch (CygnusBadConfiguration e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                    + "- FAIL - There was some problem when building the table name");
            throw e;
        } // try catch
    } // testBuildTableNameRootServicePathDataModelByEntity

    /**
     * [CartoDBAggregator.initialize] -------- When initializing through an initial geolocated event, a table
     * name is created.
     * @throws java.lang.Exception
     */
    @Test
    public void testInitializeBuildTable() throws Exception {
        System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                + "-------- When initializing through an initial geolocated event, a table name is created");
        
        // Create a NGSICartoDBSink
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-entity";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        // Create a CartoDBAggregator
        CartoDBAggregator aggregator = sink.new CartoDBAggregator();
        
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
        ContextElement originalCE = createContextElement();
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, null);
        
        try {
            aggregator.initialize(event);
        
            try {
                assertTrue(aggregator.getTableName() != null);
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "-  OK  - A table name has been created");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "- FAIL - A table name has not been created");
                throw e;
            } // try catch
        } catch (CygnusBadConfiguration e) {
            System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                    + "- FAIL - There was some problem when initializing CartoDBAggregator");
            throw e;
        } // try catch
    } // testInitializeBuildTable
    
    /**
     * [CartoDBAggregator.initialize] -------- When initializing through an initial geolocated event, the
     * aggregation fields string contains a field and a metadata field for each attribute in the event except
     * for the geolocation attribute, which is added as a specific field ('the_geom').
     * @throws java.lang.Exception
     */
    @Test
    public void testInitializeFieldsOK() throws Exception {
        System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                + "-------- When initializing through an initial geolocated event, the aggregation fields "
                + "string contains a field and a metadata field for each attribute in the event except for "
                + "the geolocation attribute, which is added as a specific field ('the_geom')");
        
        // Create a NGSICartoDBSink
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-entity";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        // Create a CartoDBAggregator
        CartoDBAggregator aggregator = sink.new CartoDBAggregator();
        
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
        ContextElement originalCE = createContextElement();
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, null);
        
        try {
            aggregator.initialize(event);
            String fields = aggregator.getFields();
        
            try {
                assertTrue(!fields.contains("somename1") && !fields.contains("somname1_md"));
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "-  OK  - 'somename1' and 'somename1_md' are not in the fields '" + fields + "'");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "- FAIL - 'somename1' and 'somename1_md' are in the fields '" + fields + "'");
                throw e;
            } // try catch
            
            try {
                assertTrue(fields.contains("somename2") && fields.contains("somename2_md"));
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "-  OK  - 'somename2' and 'somename2_md' are in the fields '" + fields + "'");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "- FAIL - 'somename2' and 'somename2_md' are not in the fields '" + fields + "'");
                throw e;
            } // try catch
            
            try {
                assertTrue(fields.contains(NGSIConstants.CARTO_DB_THE_GEOM));
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "-  OK  - '" + NGSIConstants.CARTO_DB_THE_GEOM + "' is in the fields '" + fields + "'");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "- FAIL - '" + NGSIConstants.CARTO_DB_THE_GEOM + "' is not in the fields '" + fields + "'");
                throw e;
            } // try catch
        } catch (CygnusBadConfiguration e) {
            System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                    + "- FAIL - There was some problem when initializing CartoDBAggregator");
            throw e;
        } // try catch
    } // testInitializeFieldsOK
    
    /**
     * [CartoDBAggregator.initialize] -------- When initializing through an initial geolocated event,
     * the aggregation fields string is lower case, starts with '(' and finishes with ')'.
     * @throws java.lang.Exception
     */
    @Test
    public void testInitializeFieldsStringOK() throws Exception {
        System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                + "-------- When initializing through an initial geolocated event, the aggregation fields "
                + "string is lower case, starts with '(' and finishes with ')'");
        
        // Create a NGSICartoDBSink
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-entity";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        // Create a CartoDBAggregator
        CartoDBAggregator aggregator = sink.new CartoDBAggregator();
        
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
        ContextElement originalCE = createContextElement();
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, null);
        
        try {
            aggregator.initialize(event);
            String fields = aggregator.getFields();
        
            try {
                assertEquals(fields, fields.toLowerCase());
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "-  OK  - '" + fields + "' is lower case");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "- FAIL - '" + fields + "' is not lower case");
                throw e;
            } // try catch
            
            try {
                assertTrue(fields.startsWith("("));
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "-  OK  - '" + fields + "' starts with '('");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "- FAIL - '" + fields + "' does not start with '('");
                throw e;
            } // try catch
            
            try {
                assertTrue(fields.endsWith(")"));
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "-  OK  - '" + fields + "' ends with ')'");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "- FAIL - '" + fields + "' does not end with ')'");
                throw e;
            } // try catch
        } catch (CygnusBadConfiguration e) {
            System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                    + "- FAIL - There was some problem when initializing CartoDBAggregator");
            throw e;
        } // try catch
    } // testInitializeFieldsStringOK
    
    /**
     * [CartoDBAggregator.aggregate] -------- When aggregating a single geolocated event, the aggregation
     * values string contains a value and a metadata value for each attribute in the event except for the
     * geolocation attribute, which is added as a specific value (a point).
     * @throws java.lang.Exception
     */
    @Test
    public void testAggregateValuesOK() throws Exception {
        System.out.println(getTestTraceHead("[CartoDBAggregator.aggregate]")
                + "-------- When aggregating a single geolocated event, the aggregation values string "
                + "contains a value and a metadata value for each attribute in the event except for the "
                + "geolocation attribute, which is added as a specific value (a point)");
        
        // Create a NGSICartoDBSink
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-entity";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        // Create a CartoDBAggregator
        CartoDBAggregator aggregator = sink.new CartoDBAggregator();
        
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
        ContextElement originalCE = createContextElement();
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, null);
        
        try {
            aggregator.initialize(event);
            aggregator.aggregate(event);
            String rows = aggregator.getRows();
            
            try {
                assertTrue(!rows.contains("'-3.7167, 40.3833'")
                        && !rows.contains("'{\"name\":\"location\",\"type\":\"string\",\"value\":\"WGS84\"}'"));
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "-  OK  - '-3.7167, 40.3833' and "
                        + "'{\"name\":\"location\",\"type\":\"string\",\"value\":\"WGS84\"}' "
                        + "are not in the rows '" + rows + "'");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "- FAIL - '-3.7167, 40.3833' and "
                        + "'{\"name\":\"location\",\"type\":\"string\",\"value\":\"WGS84\"}' "
                        + "are in the rows '" + rows + "'");
                throw e;
            } // try catch
            
            try {
                assertTrue(rows.contains("'someValue2'") && rows.contains("'[]'"));
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "-  OK  - 'someValue2' and '[]' are in the rows '" + rows + "'");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "- FAIL - 'someValue2' and '[]' are not in the rows '" + rows + "'");
                throw e;
            } // try catch
            
            try {
                assertTrue(rows.contains("ST_SetSRID(ST_MakePoint(-3.7167::double precision , 40.3833::double precision ), 4326)"));
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "-  OK  - 'ST_SetSRID(ST_MakePoint(-3.7167:double precision , 40.3833::double precision ), 4326)' is in the rows '" + rows
                        + "'");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                        + "- FAIL - 'ST_SetSRID(ST_MakePoint(-3.7167::double precision , 40.3833::double precision ), 4326)' is not in the rows '"
                        + rows + "'");
                throw e;
            } // try catch
        } catch (CygnusBadConfiguration e) {
            System.out.println(getTestTraceHead("[CartoDBAggregator.initialize]")
                    + "- FAIL - There was some problem when initializing CartoDBAggregator");
            throw e;
        } // try catch
    } // testAggregateValuesOK
    
    /**
     * [CartoDBAggregator.aggregate] -------- When aggregating a single geolocated event, the aggregation
     * values string starts with '(' and finishes with ')'.
     * @throws java.lang.Exception
     */
    @Test
    public void testAggregateValuesStringOK() throws Exception {
        System.out.println(getTestTraceHead("[CartoDBAggregator.aggregate]")
                + "-------- When aggregating a single geolocated event, the aggregation values string starts "
                + "with '(' and finishes with ')'");
        
        // Create a NGSICartoDBSink
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-entity";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default one
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        // Create a CartoDBAggregator
        CartoDBAggregator aggregator = sink.new CartoDBAggregator();
        
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
        ContextElement originalCE = createContextElement();
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, null);
        
        try {
            aggregator.initialize(event);
            aggregator.aggregate(event);
            String rows = aggregator.getRows();
            
            try {
                assertTrue(rows.startsWith("("));
                System.out.println(getTestTraceHead("[CartoDBAggregator.aggregate]")
                        + "-  OK  - '" + rows + "' starts with '('");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CartoDBAggregator.aggregate]")
                        + "- FAIL - '" + rows + "' does not start with '('");
                throw e;
            } // try catch
            
            try {
                assertTrue(rows.endsWith(")"));
                System.out.println(getTestTraceHead("[CartoDBAggregator.aggregate]")
                        + "-  OK  - '" + rows + "' ends with ')'");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CartoDBAggregator.aggregate]")
                        + "- FAIL - '" + rows + "' does not end with ')'");
                throw e;
            } // try catch
        } catch (CygnusBadConfiguration e) {
            System.out.println(getTestTraceHead("[CartoDBAggregator.aggregate]")
                    + "- FAIL - There was some problem when aggregating in CartoDBAggregator");
            throw e;
        } // try catch
    } // testAggregateValuesStringOK
    
    /**
     * [CartoDBAggregator.aggregate] -------- When aggregating a single geolocated event, if swap_coordinates=true
     * then the_geom field contains a point with exchanged latitude and longitude.
     * @throws java.lang.Exception
     */
    @Test
    public void testAggregateCoordinatesAreSwapped() throws Exception {
        System.out.println(getTestTraceHead("[CartoDBAggregator.aggregate]")
                + "-------- When aggregating a single geolocated event, if swap_coordinates=true then the_geom "
                + "field contains a point with exchanged latitude and longitude.");
        
        // Create a NGSICartoDBSink
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-entity";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default one
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = "true";
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        
        // Create a CartoDBAggregator
        CartoDBAggregator aggregator = sink.new CartoDBAggregator();
        
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
        ContextElement originalCE = createContextElement();
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, null);
        
        try {
            aggregator.initialize(event);
            aggregator.aggregate(event);
            String rows = aggregator.getRows();
            
            try {
                assertTrue(rows.contains("40.3833::double precision , -3.7167::double precision"));
                System.out.println(getTestTraceHead("[CartoDBAggregator.aggregate]")
                        + "-  OK  - '" + rows + "' contains the coordinates '-3.7167, 40.3833' swapped");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CartoDBAggregator.aggregate]")
                        + "- FAIL - '" + rows + "' done not contain the coordinates '-3.7167, 40.3833' swapped");
                throw e;
            } // try catch
        } catch (CygnusBadConfiguration e) {
            System.out.println(getTestTraceHead("[CartoDBAggregator.aggregate]")
                    + "- FAIL - There was some problem when aggregating in CartoDBAggregator");
            throw e;
        } // try catch
    } // testAggregateCoordinatesAreSwapped
    
    /**
     * [NGSICartoDBSink.buildTableName] -------- When data model is by service path, a table name length greater than 63
     * characters is detected.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildTableNameLengthDataModelByServicePath() throws Exception {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                + "-------- When data model is by service path, a table name length greater than 63 characters is "
                + "detected");
        // Create a NGSICartoDBSink
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-service-path";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        String servicePath = "/tooLooooooooooooooooooooooooooooooooooooooooooooooooooooooongServicePath";
        String entity = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test
        
        try {
            sink.buildTableName(servicePath, entity, attribute);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                    + "- FAIL - A table name length greater than 63 characters has not been detected");
            assertTrue(false);
        } catch (CygnusBadConfiguration e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
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
        System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                + "-------- When data model is by entity, a table name length greater than 63 characters is detected");
        // Create a NGSICartoDBSink
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-entity";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        String servicePath = "/tooLoooooooooooooooooooooooooooongServicePath";
        String entity = "tooLoooooooooooooooooooooooooooongEntity";
        String attribute = null; // irrelevant for this test
        
        try {
            sink.buildTableName(servicePath, entity, attribute);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                    + "- FAIL - A table name length greater than 63 characters has not been detected");
            assertTrue(false);
        } catch (CygnusBadConfiguration e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
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
        System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                + "-------- When data model is by atribute, a table name length greater than 63 characters is "
                + "detected");
        // Create a NGSICartoDBSink
        String apiKey = "1234567890abcdef";
        String backendMaxConns = null;
        String backendMaxConnsPerRoute = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = "dm-by-attribute";
        String enableDistanceHistoric = null; // default one
        String enableGrouping = null;
        String enableLowercase = null; // default
        String enableRawHistoric = null; // default one
        String enableRawSnapshot = null; // defatul one
        String swapCoordinates = null; // default
        String keysConfFile = "/keys.conf";
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(createContext(apiKey, backendMaxConns, backendMaxConnsPerRoute, batchSize, batchTimeout,
                batchTTL, dataModel, enableDistanceHistoric, enableGrouping, enableLowercase, enableRawHistoric,
                enableRawSnapshot, swapCoordinates, keysConfFile));
        String servicePath = "/tooLooooooooooooooongServicePath";
        String entity = "tooLooooooooooooooooooongEntity";
        String attribute = "tooLooooooooooooongAttribute";
        
        try {
            sink.buildTableName(servicePath, entity, attribute);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                    + "- FAIL - A table name length greater than 63 characters has not been detected");
            assertTrue(false);
        } catch (CygnusBadConfiguration e) {
            assertTrue(true);
            System.out.println(getTestTraceHead("[NGSICartoDBSink.buildTableName]")
                    + "-  OK  - A table name length greater than 63 characters has been detected");
        } // try catch
    } // testBuildTableNameLengthDataModelByAttribute
    
    /**
     * [NGSICartoDBSink.configure] -------- When enable_raw and enable_raw_historic are configured, the second one
     * value is used. This must be removed once enable_raw parameter is removed (currently it is just deprecated).
     * @throws java.lang.Exception
     */
    @Test
    public void testConfigureEnableRawAndEnableRawHistoric() throws Exception {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- When enable_raw and enable_raw_historic are configured, the second one value is used");
        // Create a NGSICartoDBSink
        Context context = new Context();
        context.put("enable_raw", "false");
        context.put("enable_raw_historic", "true");
        context.put("keys_conf_file", ""); // any value except for null
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(context);
        
        try {
            assertTrue(sink.getEnableRawHistoric());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - Both 'enable_raw' and 'enable_raw_historic' where configured, but "
                    + "'enable_raw_historic' value is used");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - Both 'enable_raw' and 'enable_raw_historic' where configured, but "
                    + "'enable_raw' value is used");
            throw e;
        } // try catch
    } // testConfigureEnableRawAndEnableRawHistoric
    
    /**
     * [NGSICartoDBSink.configure] -------- When enable_distance and enable_distance_historic are configured, the second
     * one value is used. This must be removed once enable_raw parameter is removed (currently it is just deprecated).
     * @throws java.lang.Exception
     */
    @Test
    public void testConfigureEnableDistanceAndEnableDistanceHistoric() throws Exception {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- When enable_distance and enable_distance_historic are configured, the second one value "
                + "is used");
        // Create a NGSICartoDBSink
        Context context = new Context();
        context.put("enable_distance", "true");
        context.put("enable_distance_historic", "false");
        context.put("keys_conf_file", ""); // any value except for null
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(context);
        
        try {
            assertTrue(!sink.getEnableDistanceHistoric());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - Both 'enable_distance' and 'enable_distance_historic' where configured, but "
                    + "'enable_distance_historic' value is used");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - Both 'enable_distance' and 'enable_distnace_historic' where configured, but "
                    + "'enable_distance' value is used");
            throw e;
        } // try catch
    } // testConfigureEnableDistanceAndEnableDistanceHistoric
    
    /**
     * [NGSICartoDBSink.configure] -------- Only enable_raw configuration works. This must be removed once
     * enable_raw parameter is removed (currently it is just deprecated).
     * @throws java.lang.Exception
     */
    @Test
    public void testConfigureEnableRawOnly() throws Exception {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- Only enable_raw configuration works");
        // Create a NGSICartoDBSink
        Context context = new Context();
        context.put("enable_raw", "false");
        context.put("keys_conf_file", ""); // any value except for null
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(context);
        
        try {
            assertTrue(!sink.getEnableRawHistoric());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - Only 'enable_raw' was configured and worked");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - Only 'enable_raw' was configured and did not work");
            throw e;
        } // try catch
    } // testConfigureEnableRawOnly
    
    /**
     * [NGSICartoDBSink.configure] -------- Only enable_distance configuration works. This must be removed
     * once enable_raw parameter is removed (currently it is just deprecated).
     * @throws java.lang.Exception
     */
    @Test
    public void testConfigureEnableDistanceOnly() throws Exception {
        System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                + "-------- Only enable_distance configuration works");
        // Create a NGSICartoDBSink
        Context context = new Context();
        context.put("enable_distance", "true");
        context.put("keys_conf_file", ""); // any value except for null
        NGSICartoDBSink sink = new NGSICartoDBSink();
        sink.configure(context);
        
        try {
            assertTrue(sink.getEnableDistanceHistoric());
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "-  OK  - Only 'enable_distance' was configured and worked");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICartoDBSink.configure]")
                    + "- FAIL - Only 'enable_distance' was configured and did not work");
            throw e;
        } // try catch
    } // testConfigureEnableDistanceOnly
    
    private Context createContext(String apiKey, String backendMaxConns, String backendMaxConnsPerRoute,
            String batchSize, String batchTimeout, String batchTTL, String dataModel, String enableDistanceHistoric,
            String enableGrouping, String enableLowercase, String enableRawHistoric, String enableRawSnapshot,
            String swapCoordinates, String keysConfFile) {
        Context context = new Context();
        context.put("api_key", apiKey);
        context.put("backend.max_conns", backendMaxConns);
        context.put("backend.max_conns_per_route", backendMaxConnsPerRoute);
        context.put("batch_size", batchSize);
        context.put("batch_timeout", batchTimeout);
        context.put("batch_ttl", batchTTL);
        context.put("data_model", dataModel);
        context.put("enable_distance_historic", enableDistanceHistoric);
        context.put("enable_grouping", enableGrouping);
        context.put("enable_lowercase", enableLowercase);
        context.put("enable_raw_historic", enableRawHistoric);
        context.put("enable_raw_snapshot", enableRawSnapshot);
        context.put("swap_coordinates", swapCoordinates);
        context.put("keys_conf_file", keysConfFile);
        return context;
    } // createContext
    
    private ContextElement createContextElement() {
        NotifyContextRequest notifyContextRequest = new NotifyContextRequest();
        ContextMetadata contextMetadata = new ContextMetadata();
        contextMetadata.setName("location");
        contextMetadata.setType("string");
        contextMetadata.setContextMetadata(new JsonPrimitive("WGS84"));
        ArrayList<ContextMetadata> metadata = new ArrayList<>();
        metadata.add(contextMetadata);
        ContextAttribute contextAttribute1 = new ContextAttribute();
        contextAttribute1.setName("someName1");
        contextAttribute1.setType("someType1");
        contextAttribute1.setContextValue(new JsonPrimitive("-3.7167, 40.3833"));
        contextAttribute1.setContextMetadata(metadata);
        ContextAttribute contextAttribute2 = new ContextAttribute();
        contextAttribute2.setName("someName2");
        contextAttribute2.setType("someType2");
        contextAttribute2.setContextValue(new JsonPrimitive("someValue2"));
        contextAttribute2.setContextMetadata(null);
        ArrayList<ContextAttribute> attributes = new ArrayList<>();
        attributes.add(contextAttribute1);
        attributes.add(contextAttribute2);
        ContextElement contextElement = new ContextElement();
        contextElement.setId("someId");
        contextElement.setType("someType");
        contextElement.setIsPattern("false");
        contextElement.setAttributes(attributes);
        return contextElement;
    } // createContextElement
    
} // NGSICartoDBSinkTest
