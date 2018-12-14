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

import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.sinks.Enums.DataModel;
import com.telefonica.iot.cygnus.sinks.NGSISink.Accumulator;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.lifecycle.LifecycleState;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NGSISinkTest {
    
    private final String originalCEStr = ""
            + "{"
            +   "\"attributes\" : ["
            +     "{"
            +       "\"name\" : \"temperature\","
            +       "\"type\" : \"centigrade\","
            +       "\"value\" : \"26.5\""
            +     "}"
            +   "],"
            +   "\"type\" : \"Room\","
            +   "\"isPattern\" : \"false\","
            +   "\"id\" : \"Room1\""
            + "}";
    private final String mappedCEStr = ""
            + "{"
            +   "\"attributes\" : ["
            +     "{"
            +       "\"name\" : \"new_temperature\","
            +       "\"type\" : \"new_centigrade\","
            +       "\"value\" : \"26.5\""
            +     "}"
            +   "],"
            +   "\"type\" : \"new_Room\","
            +   "\"isPattern\" : \"false\","
            +   "\"id\" : \"new_Room1\""
            + "}";
    private final String timestamp = "1234567890";
    private final String correlatorId = "1234567891";
    private final String originalService = "vehicles";
    private final String originalServicePath = "/4wheels";
    private final String mappedService = "new_vehicles";
    private final String mappedServicePath = "/new_4wheels";
    private final String originalEntity = "Room1_Room";
    private final String originalAttribute = "temperature";
    private final String mappedEntity = "new_Room1_new_Room";
    private final String mappedAttribute = "new_temperature";
    
    /**
     * This class is used to test once and only once the common functionality shared by all the real extending sinks.
     */
    private class NGSISinkImpl extends NGSISink {

        @Override
        void persistBatch(NGSIBatch batch) throws CygnusPersistenceError {
            throw new UnsupportedOperationException("Not supported yet.");
        } // persistBatch

        @Override
        public void capRecords(NGSIBatch batch, long size) throws CygnusCappingError {
            throw new UnsupportedOperationException("Not supported yet.");
        } // capRecords

        @Override
        public void expirateRecords(long time) throws CygnusExpiratingError {
            throw new UnsupportedOperationException("Not supported yet.");
        } // expirateRecords
        
    } // NGSISinkImpl
    
    /**
     * Constructor.
     */
    public NGSISinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSISinkTest
    
    /**
     * [CygnusSink.start] -------- The sink starts properly.
     */
    @Test
    public void testStart() {
        System.out.println(getTestTraceHead("[NGSISink.start]") + "-------- The sink starts properly");
        NGSISinkImpl sink = new NGSISinkImpl();
        sink.configure(createContext(null, null, null, null, null, null, null, null, null, null, null));
        sink.setChannel(new MemoryChannel());
        sink.start();
        LifecycleState state = sink.getLifecycleState();
        
        try {
            assertEquals(LifecycleState.START, state);
            System.out.println(getTestTraceHead("[NGSISink.start]")
                    + "-  OK  - The sink started properly, the lifecycle state is '" + state.toString() + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.start]")
                    + "- FAIL - The sink did not start properly, the lifecycle state is '" + state.toString() + "'");
        } // try catch
    } // testStart
    
    /**
     * [CygnusSink.configure] -------- When not configured, the default values are used for non mandatory
     * parameters.
     */
    @Test
    public void testConfigureNotMandatoryParameters() {
        System.out.println(getTestTraceHead("[NGSISink.configure]")
                + "-------- When not configured, the default values are used for non mandatory parameters");
        NGSISinkImpl sink = new NGSISinkImpl();
        sink.configure(createContext(null, null, null, null, null, null, null, null, null, null, null));
        
        try {
            assertEquals("5000", sink.getBatchRetryIntervals());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The default configuration value for 'batch_retry_intervals' is '5000'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The default configuration value for "
                    + "'batch_retry_intervals' is '" + sink.getBatchRetryIntervals() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(1, sink.getBatchSize());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The default configuration value for 'batch_size' is '1'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The default configuration value for 'batch_size' is '" + sink.getBatchSize() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(30, sink.getBatchTimeout());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The default configuration value for 'batch_timeout' is '30'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The default configuration value for 'batch_timeout' is '"
                    + sink.getBatchTimeout() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(10, sink.getBatchTTL());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The default configuration value for 'batch_ttl' is '10'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The default configuration value for 'batch_ttl' is '" + sink.getBatchTTL() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(DataModel.DMBYENTITY, sink.getDataModel());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The default configuration value for 'data_model' is 'dm-by-entity'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The default configuration value for "
                    + "'data_model' is '" + sink.getDataModel() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(false, sink.getEnableGrouping());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The default configuration value for 'enable_grouping' is 'false'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The default configuration value for "
                    + "'enable_grouping' is '" + sink.getEnableGrouping() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(false, sink.getEnableLowerCase());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The default configuration value for 'enable_lowercase' is 'false'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The default configuration value for "
                    + "'enable_lowercase' is '" + sink.getEnableLowerCase() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(false, sink.getEnableEncoding());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The default configuration value for 'enable_encoding' is 'false'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The default configuration value for "
                    + "'enable_encoding' is '" + sink.getEnableEncoding() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(false, sink.getEnableNameMappings());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The default configuration value for 'enable_name_mapping' is 'false'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The default configuration value for "
                    + "'enable_name_mapping' is '" + sink.getEnableNameMappings() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(-1, sink.getPersistencePolicyMaxRecords());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The default configuration value for 'persistence_poilicy.max_records' is '-1'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The default configuration value for "
                    + "'persistence_poilicy.max_records' is '" + sink.getPersistencePolicyMaxRecords() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(-1, sink.getPersistencePolicyExpirationTime());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The default configuration value for 'persistence_poilicy.expiration_time' is '-1'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The default configuration value for "
                    + "'persistence_poilicy.expiration_time' is '" + sink.getPersistencePolicyExpirationTime() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(3600, sink.getPersistencePolicyCheckingTime());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The default configuration value for 'persistence_poilicy.checking_time' is '3600000'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The default configuration value for "
                    + "'persistence_poilicy.checking_time' is '" + sink.getPersistencePolicyCheckingTime() + "'");
            throw e;
        } // try catch
    } // testConfigureNotMandatoryParameters
    
    /**
     * [CygnusSink.configure] -------- When not configured, the default values are used for non mandatory
     * parameters.
     */
    @Test
    public void testConfigureModifyNotMandatoryParameters() {
        System.out.println(getTestTraceHead("[NGSISink.configure]")
                + "-------- When configured, non mandatory parameters get the appropiate value");
        NGSISinkImpl sink = new NGSISinkImpl();
        String maxRecords = "5";
        sink.configure(createContext(null, null, null, null, null, null, null, null, maxRecords, null, null));
        
        try {
            assertEquals(5, sink.getPersistencePolicyMaxRecords());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The configuration value for 'persistence_policy.max_records' is '5'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The configuration value for 'persistence_policy.max_records' is '"
                    + sink.getPersistencePolicyMaxRecords() + "'");
            throw e;
        } // try catch
        
        String expirationTime = "60";
        sink.configure(createContext(null, null, null, null, null, null, null, null, null, expirationTime, null));
        
        try {
            assertEquals(60, sink.getPersistencePolicyExpirationTime());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The configuration value for 'persistence_policy.expiration_time' is '60'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The configuration value for 'persistence_policy.expiration_time' is '"
                    + sink.getPersistencePolicyExpirationTime() + "'");
            throw e;
        } // try catch
        
        String checkingTime = "7200";
        sink.configure(createContext(null, null, null, null, null, null, null, null, null, null,
                checkingTime));
        
        try {
            assertEquals(7200, sink.getPersistencePolicyCheckingTime());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - The configuration value for 'persistence_policy.checking_time' is '7200'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - The configuration value for 'persistence_policy.checking_time' is '"
                    + sink.getPersistencePolicyCheckingTime() + "'");
            throw e;
        } // try catch
    } // testConfigureModifyNotMandatoryParameters
    
    /**
     * [CygnusSink.configure] -------- The configuration becomes invalid upon out-of-the-limits configured values for
     * parameters having a discrete set of accepted values, or numerical values having upper or lower limits.
     */
    @Test
    public void testConfigureInvalidConfiguration() {
        System.out.println(getTestTraceHead("[NGSISink.configure]")
                + "-------- The configuration becomes invalid upon out-of-the-limits configured values for parameters "
                + "having a discrete set of accepted values, or numerical values having upper or lower limits");
        NGSISinkImpl sink = new NGSISinkImpl();
        String configuredBatchSize = "0";
        sink.configure(createContext(null, configuredBatchSize, null, null, null, null, null, null, null, null, null));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - A wrong configuration 'batch_size='"
                    + configuredBatchSize + "' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - A wrong configuration 'batch_size='"
                    + configuredBatchSize + "' has not been detected");
            throw e;
        } // try catch
        
        sink = new NGSISinkImpl();
        String configuredBatchTimeout = "0";
        sink.configure(createContext(null, null, configuredBatchTimeout, null, null, null, null, null, null, null,
                null));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - A wrong configuration 'batch_timeout='"
                    + configuredBatchTimeout + "' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - A wrong configuration 'batch_timeout='"
                    + configuredBatchTimeout + "' has not been detected");
            throw e;
        } // try catch
        
        sink = new NGSISinkImpl();
        String configuredBatchTTL = "-2";
        sink.configure(createContext(null, null, null, configuredBatchTTL, null, null, null, null, null, null, null));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - A wrong configuration 'batch_ttl='" + configuredBatchTTL + "' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - A wrong configuration 'batch_ttl='" + configuredBatchTTL + "' has not been detected");
            throw e;
        } // try catch
        
        sink = new NGSISinkImpl();
        String dataModel = "dm-by-other";
        sink.configure(createContext(null, null, null, null, dataModel, null, null, null, null, null, null));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - A wrong configuration 'data_model='" + dataModel + "' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - A wrong configuration 'data_model='" + dataModel + "' has not been detected");
            throw e;
        } // try catch
        
        sink = new NGSISinkImpl();
        String configuredEnableGrouping = "falso";
        sink.configure(createContext(null, null, null, null, null, configuredEnableGrouping, null, null, null, null,
                null));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - A wrong configuration 'enable_grouping='"
                    + configuredEnableGrouping + "' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - A wrong configuration 'enable_grouping='"
                    + configuredEnableGrouping + "' has not been detected");
            throw e;
        } // try catch
        
        sink = new NGSISinkImpl();
        String configuredEnableLowercase = "verdadero";
        sink.configure(createContext(null, null, null, null, null, null, configuredEnableLowercase, null, null, null,
                null));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - A wrong configuration 'enable_lowercase='"
                    + configuredEnableLowercase + "' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - A wrong configuration 'enable_lowercase='"
                    + configuredEnableLowercase + "' has not been detected");
            throw e;
        } // try catch
        
        sink = new NGSISinkImpl();
        String configuredEnableNameMappings = "verdadero";
        sink.configure(createContext(null, null, null, null, null, null, null, configuredEnableNameMappings,
                null, null, null));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - A wrong configuration 'enable_name_mappings='"
                    + configuredEnableNameMappings + "' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - A wrong configuration 'enable_name_mappings='"
                    + configuredEnableNameMappings + "' has not been detected");
            throw e;
        } // try catch
        
        sink = new NGSISinkImpl();
        String batchRetryIntervals = "1000,2000,-3000";
        sink.configure(createContext(batchRetryIntervals, null, null, null, null, null, null, null, null, null, null));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - A wrong configuration 'batch_retry_intervals='"
                    + batchRetryIntervals + "' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - A wrong configuration 'batch_retry_intervals='"
                    + batchRetryIntervals + "' has not been detected");
            throw e;
        } // try catch
        
        sink = new NGSISinkImpl();
        String checkingTime = "-1000";
        sink.configure(createContext(null, null, null, null, null, null, null, null, null, null,
                checkingTime));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "-  OK  - A wrong configuration 'persistence_policy.checking_time='"
                    + checkingTime + "' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.configure]")
                    + "- FAIL - A wrong configuration 'persistence_policy.checking_time='"
                    + checkingTime + "' has not been detected");
            throw e;
        } // try catch // try catch
    } // testConfigureInvalidConfiguration
    
    /**
     * [NGSISink.Accumulator.accumulate] -------- When data model is by service, a notification is successfully
     * accumulated.
     * @throws java.lang.Exception
     */
    @Test
    public void testAccumulateDMByService() throws Exception {
        System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                + "-------- When data model is by service, a notification is successfully accumulated");
        NGSISinkImpl sink = new NGSISinkImpl();
        String dataModel = "dm-by-service";
        sink.configure(createContext(null, null, null, null, dataModel, null, null, null, null, null, null));
        Accumulator acc = sink.new Accumulator();
        acc.initialize(new Date().getTime());
        Map<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
        headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        ContextElement originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
        ContextElement mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, mappedCE);
        acc.accumulate(event);
        NGSIBatch batch = acc.getBatch();
        batch.startIterator();
        batch.hasNext();
        String destination = batch.getNextDestination();
        int numEvents = batch.getNumEvents();
        ArrayList<NGSIEvent> events = batch.getNextEvents();
        
        try {
            assertEquals(correlatorId, acc.getAccTransactionIds());
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Correlator ID successfully accumulated");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Correlator ID unsuccessfully accumulated");
            throw e;
        } // try catch
        
        try {
            assertEquals(1, numEvents);
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated events is 1");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated events is not 1");
            throw e;
        } // try catch

        try {
            assertTrue(destination.equals(originalService));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated destination is '" + originalService + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated destination is not '" + originalService + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(originalService, events.get(0).getServiceForNaming(false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service is '" + originalService + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service is not '" + originalService + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(originalServicePath, events.get(0).getServicePathForNaming(false, false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service path is '" + originalServicePath + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service path is not '" + originalServicePath + "'");
            throw e;
        } // try catch
/*        
        try {
            assertEquals(null, events.get(0).getEntityForNaming(false, false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is 'null'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not 'null'");
            throw e;
        } // try catch
        
        try {
            assertEquals(null, events.get(0).getAttributeForNaming(false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is 'null'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not 'null'");
            throw e;
        } // try catch
*/
    } // testAccumulateDMByService
    
    /**
     * [NGSISink.Accumulator.accumulate] -------- When data model is by service path, a notification is successfully
     * accumulated.
     * @throws java.lang.Exception
     */
    @Test
    public void testAccumulateDMByServicePath() throws Exception {
        System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                + "-------- When data model is by service path, a notification is successfully accumulated");
        NGSISinkImpl sink = new NGSISinkImpl();
        String dataModel = "dm-by-service-path";
        sink.configure(createContext(null, null, null, null, dataModel, null, null, null, null, null, null));
        Accumulator acc = sink.new Accumulator();
        acc.initialize(new Date().getTime());
        Map<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
        headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        ContextElement originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
        ContextElement mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, mappedCE);
        acc.accumulate(event);
        NGSIBatch batch = acc.getBatch();
        batch.startIterator();
        batch.hasNext();
        String destination = batch.getNextDestination();
        int numEvents = batch.getNumEvents();
        ArrayList<NGSIEvent> events = batch.getNextEvents();
        
        try {
            assertEquals(correlatorId, acc.getAccTransactionIds());
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Notification successfully accumulated");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Notification unsuccessfully accumulated");
            throw e;
        } // try catch
        
        try {
            assertEquals(1, numEvents);
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated events is 1");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated events is not 1");
            throw e;
        } // try catch
        
        try {
            assertTrue(destination.equals(originalService + "_" + originalServicePath));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated destination is '" + originalService + "_" + originalServicePath + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated destination is not '" + originalService + "_" + originalServicePath + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(originalService, events.get(0).getServiceForNaming(false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service is '" + originalService + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service is not '" + originalService + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(originalServicePath, events.get(0).getServicePathForNaming(false, false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service path is '" + originalServicePath + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service path is not '" + originalServicePath + "'");
            throw e;
        } // try catch
/*        
        try {
            assertEquals(null, events.get(0).getEntityForNaming(false, false, false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is 'null'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not 'null'");
            throw e;
        } // try catch
        
        try {
            assertEquals(null, events.get(0).getAttributeForNaming(false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is 'null'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not 'null'");
            throw e;
        } // try catch
*/
    } // testAccumulateDMByServicePath
    
    /**
     * [NGSISink.Accumulator.accumulate] -------- When data model is by entity, a notification is successfully
     * accumulated.
     * @throws java.lang.Exception
     */
    @Test
    public void testAccumulateDMByEntity() throws Exception {
        System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                + "-------- When data model is by entity, a notification is successfully accumulated");
        NGSISinkImpl sink = new NGSISinkImpl();
        String dataModel = "dm-by-entity";
        sink.configure(createContext(null, null, null, null, dataModel, null, null, null, null, null, null));
        Accumulator acc = sink.new Accumulator();
        acc.initialize(new Date().getTime());
        Map<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
        headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        ContextElement originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
        ContextElement mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, mappedCE);
        acc.accumulate(event);
        NGSIBatch batch = acc.getBatch();
        batch.startIterator();
        batch.hasNext();
        String destination = batch.getNextDestination();
        int numEvents = batch.getNumEvents();
        ArrayList<NGSIEvent> events = batch.getNextEvents();
        
        try {
            assertEquals(correlatorId, acc.getAccTransactionIds());
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Notification successfully accumulated");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Notification unsuccessfully accumulated");
            throw e;
        } // try catch
        
        try {
            assertEquals(1, numEvents);
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated events is 1");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated events is not 1");
            throw e;
        } // try catch
        
        try {
            assertTrue(destination.equals(originalService + "_" + originalServicePath + "_" + originalEntity));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated destination is '" + originalService + "_" + originalServicePath + "_"
                    + originalEntity + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated destination is not '" + originalService + "_" + originalServicePath + "_"
                    + originalEntity + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(originalService, events.get(0).getServiceForNaming(false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service is '" + originalService + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service is not '" + originalService + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(originalServicePath, events.get(0).getServicePathForNaming(false, false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service path is '" + originalServicePath + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service path is not '" + originalServicePath + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(originalEntity, events.get(0).getEntityForNaming(false, false, false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is '" + originalEntity + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not '" + originalEntity + "'");
            throw e;
        } // try catch
/*        
        try {
            assertEquals(null, events.get(0).getAttributeForNaming(false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is 'null'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not 'null'");
            throw e;
        } // try catch
*/
    } // testAccumulateDMByEntity
    
    /**
     * [NGSISink.Accumulator.accumulate] -------- When data model is by attribute, a notification is successfully
     * accumulated.
     * @throws java.lang.Exception
     */
    @Test
    public void testAccumulateDMByAttribute() throws Exception {
        System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                + "-------- When data model is by attribute, a notification is successfully accumulated");
        NGSISinkImpl sink = new NGSISinkImpl();
        String dataModel = "dm-by-attribute";
        sink.configure(createContext(null, null, null, null, dataModel, null, null, null, null, null, null));
        Accumulator acc = sink.new Accumulator();
        acc.initialize(new Date().getTime());
        Map<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
        headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        ContextElement originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
        ContextElement mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, mappedCE);
        acc.accumulate(event);
        NGSIBatch batch = acc.getBatch();
        batch.startIterator();
        batch.hasNext();
        String destination = batch.getNextDestination();
        int numEvents = batch.getNumEvents();
        ArrayList<NGSIEvent> events = batch.getNextEvents();
        
        try {
            assertEquals(correlatorId, acc.getAccTransactionIds());
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Notification successfully accumulated");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Notification unsuccessfully accumulated");
            throw e;
        } // try catch
        
        try {
            assertEquals(1, numEvents);
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated events is 1");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated events is not 1");
            throw e;
        } // try catch
        
        try {
            assertTrue(destination.equals(originalService + "_" + originalServicePath + "_"
                    + originalEntity + "_" + originalAttribute));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated destination is '" + originalService + "_" + originalServicePath + "_"
                    + originalEntity + "_" + originalAttribute + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated destination is not '" + originalService + "_" + originalServicePath + "_"
                    + originalEntity + "_" + originalAttribute + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(originalService, events.get(0).getServiceForNaming(false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service is '" + originalService + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service is not '" + originalService + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(originalServicePath, events.get(0).getServicePathForNaming(false, false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service path is '" + originalServicePath + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service path is not '" + originalServicePath + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(originalEntity, events.get(0).getEntityForNaming(false, false, false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is '" + originalEntity + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not '" + originalEntity + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(originalAttribute, events.get(0).getAttributeForNaming(false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is '" + originalAttribute + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not '" + originalAttribute + "'");
            throw e;
        } // try catch
    } // testAccumulateDMByAttribute
    
    /**
     * [NGSISink.Accumulator.accumulate] -------- When data model is by service and name mappings are enabled, a
     * notification is successfully accumulated.
     * @throws java.lang.Exception
     */
    @Test
    public void testAccumulateDMByServiceNameMappings() throws Exception {
        System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                + "-------- When data model is by service and name mappings are enabled, a notification is"
                + "successfully accumulated");
        NGSISinkImpl sink = new NGSISinkImpl();
        String dataModel = "dm-by-service";
        String enableNameMappings = "true";
        sink.configure(createContext(null, null, null, null, dataModel, null, null, enableNameMappings, null, null, null));
        Accumulator acc = sink.new Accumulator();
        acc.initialize(new Date().getTime());
        Map<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
        headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        ContextElement originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
        ContextElement mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, mappedCE);
        acc.accumulate(event);
        NGSIBatch batch = acc.getBatch();
        batch.startIterator();
        batch.hasNext();
        String destination = batch.getNextDestination();
        int numEvents = batch.getNumEvents();
        ArrayList<NGSIEvent> events = batch.getNextEvents();
        
        try {
            assertEquals(correlatorId, acc.getAccTransactionIds());
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Correlator ID successfully accumulated");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Correlator ID unsuccessfully accumulated");
            throw e;
        } // try catch
        
        try {
            assertEquals(1, numEvents);
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated events is 1");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated events is not 1");
            throw e;
        } // try catch
        
        try {
            assertTrue(destination.equals(mappedService));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated destination is '" + mappedService + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated destination is not '" + mappedService + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(mappedService, events.get(0).getServiceForNaming(true));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service is '" + mappedService + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service is not '" + mappedService + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(mappedServicePath, events.get(0).getServicePathForNaming(false, true));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service path is '" + mappedServicePath + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service path is not '" + mappedServicePath + "'");
            throw e;
        } // try catch
/*        
        try {
            assertEquals(null, events.get(0).getEntityForNaming(false, true, false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is 'null'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not 'null'");
            throw e;
        } // try catch
        
        try {
            assertEquals(null, events.get(0).getAttributeForNaming(true));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is 'null'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not 'null'");
            throw e;
        } // try catch
*/
    } // testAccumulateDMByServiceNameMappings
    
    /**
     * [NGSISink.Accumulator.accumulate] -------- When data model is by service path and name mappings are enabled, a
     * notification is successfully accumulated.
     * @throws java.lang.Exception
     */
    @Test
    public void testAccumulateDMByServicePathNameMappings() throws Exception {
        System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                + "-------- When data model is by service path and name mappings are enabled, a notification is"
                + "successfully accumulated");
        NGSISinkImpl sink = new NGSISinkImpl();
        String dataModel = "dm-by-service-path";
        String enableNameMappings = "true";
        sink.configure(createContext(null, null, null, null, dataModel, null, null, enableNameMappings, null, null,
                null));
        Accumulator acc = sink.new Accumulator();
        acc.initialize(new Date().getTime());
        Map<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
        headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        ContextElement originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
        ContextElement mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, mappedCE);
        acc.accumulate(event);
        NGSIBatch batch = acc.getBatch();
        batch.startIterator();
        batch.hasNext();
        String destination = batch.getNextDestination();
        int numEvents = batch.getNumEvents();
        ArrayList<NGSIEvent> events = batch.getNextEvents();
        
        try {
            assertEquals(correlatorId, acc.getAccTransactionIds());
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Notification successfully accumulated");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Notification unsuccessfully accumulated");
            throw e;
        } // try catch
        
        try {
            assertEquals(1, numEvents);
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated events is 1");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated events is not 1");
            throw e;
        } // try catch
        
        try {
            assertTrue(destination.equals(mappedService + "_" + mappedServicePath));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated destination is '" + mappedService + "_" + mappedServicePath + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated destination is not '" + mappedService + "_" + mappedServicePath + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(mappedService, events.get(0).getServiceForNaming(true));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service is '" + mappedService + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service is not '" + mappedService + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(mappedServicePath, events.get(0).getServicePathForNaming(false, true));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service path is '" + mappedServicePath + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service path is not '" + mappedServicePath + "'");
            throw e;
        } // try catch
/*        
        try {
            assertEquals(null, events.get(0).getEntityForNaming(false, true));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is 'null'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not 'null'");
            throw e;
        } // try catch
        
        try {
            assertEquals(null, events.get(0).getAttributeForNaming(true));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is 'null'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not 'null'");
            throw e;
        } // try catch
*/
    } // testAccumulateDMByServicePathNameMappings
    
    /**
     * [NGSISink.Accumulator.accumulate] -------- When data model is by entity and name mappings are enabled, a
     * notification is successfully accumulated.
     * @throws java.lang.Exception
     */
    @Test
    public void testAccumulateDMByEntityNameMappings() throws Exception {
        System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                + "-------- When data model is by entity and name mappings are enabled, a notification is"
                + "successfully accumulated");
        NGSISinkImpl sink = new NGSISinkImpl();
        String dataModel = "dm-by-entity";
        String enableNameMappings = "true";
        sink.configure(createContext(null, null, null, null, dataModel, null, null, enableNameMappings, null, null,
                null));
        Accumulator acc = sink.new Accumulator();
        acc.initialize(new Date().getTime());
        Map<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
        headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        ContextElement originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
        ContextElement mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, mappedCE);
        acc.accumulate(event);
        NGSIBatch batch = acc.getBatch();
        batch.startIterator();
        batch.hasNext();
        String destination = batch.getNextDestination();
        int numEvents = batch.getNumEvents();
        ArrayList<NGSIEvent> events = batch.getNextEvents();
        
        try {
            assertEquals(correlatorId, acc.getAccTransactionIds());
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Notification successfully accumulated");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Notification unsuccessfully accumulated");
            throw e;
        } // try catch
        
        try {
            assertEquals(1, numEvents);
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated events is 1");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated events is not 1");
            throw e;
        } // try catch
        
        try {
            assertTrue(destination.equals(mappedService + "_" + mappedServicePath + "_" + mappedEntity));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated destination is '" + mappedService + "_" + mappedServicePath + "_"
                    + mappedEntity + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated destination is not '" + mappedService + "_" + mappedServicePath + "_"
                    + mappedEntity + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(mappedService, events.get(0).getServiceForNaming(true));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service is '" + mappedService + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service is not '" + mappedService + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(mappedServicePath, events.get(0).getServicePathForNaming(false, true));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service path is '" + mappedServicePath + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service path is not '" + mappedServicePath + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(mappedEntity, events.get(0).getEntityForNaming(false, true, false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is '" + mappedEntity + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not '" + mappedEntity + "'");
            throw e;
        } // try catch
/*        
        try {
            assertEquals(null, events.get(0).getAttributeForNaming(true));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is 'null'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not 'null'");
            throw e;
        } // try catch
*/
    } // testAccumulateDMByEntityNameMappings
    
    /**
     * [NGSISink.Accumulator.accumulate] -------- When data model is by attribute and name mappings are enabled, a
     * notification is successfully accumulated.
     * @throws java.lang.Exception
     */
    @Test
    public void testAccumulateDMByAttributeNameMappings() throws Exception {
        System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                + "-------- When data model is by attribute and name mappings are enabled, a notification is"
                + "successfully accumulated");
        NGSISinkImpl sink = new NGSISinkImpl();
        String dataModel = "dm-by-attribute";
        String enableNameMappings = "true";
        sink.configure(createContext(null, null, null, null, dataModel, null, null, enableNameMappings, null, null,
                null));
        Accumulator acc = sink.new Accumulator();
        acc.initialize(new Date().getTime());
        Map<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
        headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        ContextElement originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
        ContextElement mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        NGSIEvent event = new NGSIEvent(headers, originalCE.toString().getBytes(), originalCE, mappedCE);
        acc.accumulate(event);
        NGSIBatch batch = acc.getBatch();
        batch.startIterator();
        batch.hasNext();
        String destination = batch.getNextDestination();
        int numEvents = batch.getNumEvents();
        ArrayList<NGSIEvent> events = batch.getNextEvents();
        
        try {
            assertEquals(correlatorId, acc.getAccTransactionIds());
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Notification successfully accumulated");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Notification unsuccessfully accumulated");
            throw e;
        } // try catch
        
        try {
            assertEquals(1, numEvents);
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated events is 1");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated events is not 1");
            throw e;
        } // try catch
        
        try {
            assertTrue(destination.equals(mappedService + "_" + mappedServicePath + "_"
                    + mappedEntity + "_" + mappedAttribute));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated destination is '" + mappedService + "_" + mappedServicePath + "_"
                    + mappedEntity + "_" + mappedAttribute + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated destination is not '" + mappedService + "_" + mappedServicePath + "_"
                    + mappedEntity + "_" + mappedAttribute + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(mappedService, events.get(0).getServiceForNaming(true));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service is '" + mappedService + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service is not '" + mappedService + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(mappedServicePath, events.get(0).getServicePathForNaming(false, true));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's service path is '" + mappedServicePath + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's service path is not '" + mappedServicePath + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(mappedEntity, events.get(0).getEntityForNaming(false, true, false));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is '" + mappedEntity + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not '" + mappedEntity + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(mappedAttribute, events.get(0).getAttributeForNaming(true));
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "-  OK  - Accumulated event's entity path is '" + mappedAttribute + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.Accumulator.accumulate]")
                    + "- FAIL - Accumulated event's entity path is not '" + mappedAttribute + "'");
            throw e;
        } // try catch
    } // testAccumulateDMByAttributeNameMappings
    
    /**
     * [NGSISink.getRollbackedAccumulationForRetry] -------- When there are no candidates for retrying, null is
     * returned.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetRollbackedAccumulationForRetryNoCandidates() throws Exception {
        System.out.println(getTestTraceHead("[NGSISink.getRollbackedAccumulationForRetry]")
                + "-------- When there are no candidates for retrying, null is returned");
        NGSISinkImpl sink = new NGSISinkImpl();
        sink.configure(createContext(null, null, null, null, null, null, null, null, null, null, null));
        ArrayList<Accumulator> rollbackedAccumulations = new ArrayList<>();
        sink.setRollbackedAccumulations(rollbackedAccumulations);
        
        try {
            assertEquals(null, sink.getRollbackedAccumulationForRetry());
            System.out.println(getTestTraceHead("[NGSISink.getRollbackedAccumulationForRetry]")
                    + "-  OK  - There is no candidate for retrying having an empty list");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.getRollbackedAccumulationForRetry]")
                    + "- FAIL - A candidate for retrying was found having an empty list");
            throw e;
        } // try catch
        
        rollbackedAccumulations = new ArrayList<>();
        Accumulator acc = sink.new Accumulator();
        // this accumulation has supposedly been retried 10 miliseconds ago, so it is not a candidate
        acc.setLastRetry(new Date().getTime() - 10);
        rollbackedAccumulations.add(acc);
        sink.setRollbackedAccumulations(rollbackedAccumulations);
        
        try {
            assertEquals(null, sink.getRollbackedAccumulationForRetry());
            System.out.println(getTestTraceHead("[NGSISink.getRollbackedAccumulationForRetry]")
                    + "-  OK  - There is no candidate for retrying having an empty list");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.getRollbackedAccumulationForRetry]")
                    + "- FAIL - A candidate for retrying was found having an empty list");
            throw e;
        } // try catch
    } // getRollbackedAccumulationForRetryNoCandidates
    
    /**
     * [NGSISink.getRollbackedAccumulationForRetry] -------- When there is a candidate for retrying, it is returned.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetRollbackedAccumulationForRetryCandidateOK() throws Exception {
        System.out.println(getTestTraceHead("[NGSISink.getRollbackedAccumulationForRetry]")
                + "-------- When there is a candidate for retrying, it is returned");
        NGSISinkImpl sink = new NGSISinkImpl();
        sink.configure(createContext(null, null, null, null, null, null, null, null, null, null, null));
        ArrayList<Accumulator> rollbackedAccumulations = new ArrayList<>();
        Accumulator acc = sink.new Accumulator();
        // this accumulation has supposedly been retried 10000 miliseconds ago, so it is a candidate
        acc.setLastRetry(new Date().getTime() - 10000);
        rollbackedAccumulations.add(acc);
        sink.setRollbackedAccumulations(rollbackedAccumulations);
        
        try {
            assertEquals(acc, sink.getRollbackedAccumulationForRetry());
            System.out.println(getTestTraceHead("[NGSISink.getRollbackedAccumulationForRetry]")
                    + "-  OK  - There is a candidate for retrying");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.getRollbackedAccumulationForRetry]")
                    + "- FAIL - There is no candidate for retrying");
            throw e;
        } // try catch
    } // getRollbackedAccumulationForRetryCandidateOK
    
    /**
     * [NGSISink.doRollback] -------- When rollbacking for the first time, the accumulator is added to the rollbacked
     * accumulations having the maximum TTL.
     * @throws java.lang.Exception
     */
    @Test
    public void testDoRollback() throws Exception {
        System.out.println(getTestTraceHead("[NGSISink.doRollback]")
                + "-------- When rollbacking for the first time, the accumulator is added to the rollbacked "
                + "accumulations having the maximum TTL");
        NGSISinkImpl sink = new NGSISinkImpl();
        sink.configure(createContext(null, null, null, null, null, null, null, null, null, null, null));
        Accumulator acc = sink.new Accumulator();
        sink.doRollback(acc);
        
        try {
            assertEquals(10, acc.getTTL());
            System.out.println(getTestTraceHead("[NGSISink.doRollback]")
                    + "-  OK  - Rollbacked accumulation TTL has the maximum value");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.doRollback]")
                    + "- FAIL - Rollbacked accumulation TTL has not the maximum value");
            throw e;
        } // try catch
        
        try {
            assertEquals(acc, sink.getRollbackedAccumulations().get(0));
            System.out.println(getTestTraceHead("[NGSISink.doRollback]")
                    + "-  OK  - The accumulation has been added to the rollback queue");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.doRollback]")
                    + "- FAIL - The accumulation has not been added to the rollback queue");
            throw e;
        } // try catch
    } // testDoRollback
    
    /**
     * [NGSISink.doRollbackAgain] -------- When rollbacking after the first time, the accumulator is added to the
     * rollbacked accumulations having a decreased TTL.
     * @throws java.lang.Exception
     */
    @Test
    public void testDoRollbackAgagin() throws Exception {
        System.out.println(getTestTraceHead("[NGSISink.doRollbackAgain]")
                + "-------- When rollbacking after the first time, the accumulator is added to the rollbacked "
                + "accumulations having a decreased TTL");
        NGSISinkImpl sink = new NGSISinkImpl();
        sink.configure(createContext(null, null, null, null, null, null, null, null, null, null, null));
        Accumulator acc = sink.new Accumulator();
        int ttl = 3;
        acc.setTTL(ttl);
        sink.doRollback(acc);
        sink.doRollbackAgain(acc);
        
        try {
            assertEquals(ttl - 1, acc.getTTL());
            System.out.println(getTestTraceHead("[NGSISink.doRollback]")
                    + "-  OK  - Rollbacked accumulation TTL has the maximum value");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.doRollback]")
                    + "- FAIL - Rollbacked accumulation TTL has not the maximum value");
            throw e;
        } // try catch
        
        try {
            assertEquals(acc, sink.getRollbackedAccumulations().get(0));
            System.out.println(getTestTraceHead("[NGSISink.doRollback]")
                    + "-  OK  - The accumulation has been added to the rollback queue");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISink.doRollback]")
                    + "- FAIL - The accumulation has not been added to the rollback queue");
            throw e;
        } // try catch
    } // testDoRollbackAgain
    
    private Context createContext(String batchRetryIntervals, String batchSize, String batchTimeout, String batchTTL,
            String dataModel, String enableGrouping, String enableLowercase, String enableNameMappings,
            String perisistencePolicyMaxRecords, String perisistencePolicyExpirationTime,
            String perisistencePolicyCheckingTime) {
        Context context = new Context();
        context.put("batch_retry_intervals", batchRetryIntervals);
        context.put("batch_size", batchSize);
        context.put("batch_timeout", batchTimeout);
        context.put("batch_ttl", batchTTL);
        context.put("data_model", dataModel);
        context.put("enable_grouping", enableGrouping);
        context.put("enable_lowercase", enableLowercase);
        context.put("enable_name_mappings", enableNameMappings);
        context.put("persistence_policy.max_records", perisistencePolicyMaxRecords);
        context.put("persistence_policy.expiration_time", perisistencePolicyExpirationTime);
        context.put("persistence_policy.checking_time", perisistencePolicyCheckingTime);
        return context;
    } // createContext
    
} // NGSISinkTest
