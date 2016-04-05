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

import com.telefonica.iot.cygnus.sinks.OrionSink.DataModel;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.lifecycle.LifecycleState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class OrionSinkTest {
    
    /**
     * This class is used to test once and only once the common functionality shared by all the real extending sinks.
     */
    private class OrionSinkImpl extends OrionSink {

        @Override
        void persistBatch(Batch batch) throws Exception {
            throw new UnsupportedOperationException("Not supported yet.");
        } // persistBatch
        
    } // OrionSinkImpl
    
    /**
     * [OrionSink] -------- The sink starts properly.
     */
    @Test
    public void testStart() {
        System.out.println("[OrionSink] -------- The sink starts properly");
        OrionSinkImpl sink = new OrionSinkImpl();
        sink.configure(createContext(null, null, null, null, null, null)); // default configuration
        sink.setChannel(new MemoryChannel());
        sink.start();
        LifecycleState state = sink.getLifecycleState();
        
        try {
            assertEquals(LifecycleState.START, state);
            System.out.println("[OrionSink] -  OK  - The sink started properly, the lifecycle state is '"
                    + state.toString() + "'");
        } catch (AssertionError e) {
            System.out.println("[OrionSink] - FAIL - The sink did not start properly, the lifecycle state "
                    + "is '" + state.toString() + "'");
        } // try catch
    } // testStart
    
    /**
     * [OrionSink.configure] -------- When not configured, the default values are used for non mandatory
     * parameters.
     */
    @Test
    public void testConfigureNotMandatoryParameters() {
        System.out.println("[OrionSink.configure] -------- When not configured, the default values are used "
                + "for non mandatory parameters");
        OrionSinkImpl sink = new OrionSinkImpl();
        sink.configure(createContext(null, null, null, null, null, null)); // default configuration
        
        try {
            assertEquals(1, sink.getBatchSize());
            System.out.println("[OrionSink.configure] -  OK  - The default configuration value for "
                    + "'batch_size' is '1'");
        } catch (AssertionError e) {
            System.out.println("[OrionSink.configure] - FAIL - The default configuration value for "
                    + "'batch_size' is '" + sink.getBatchSize() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(30, sink.getBatchTimeout());
            System.out.println("[OrionSink.configure] -  OK  - The default configuration value for "
                    + "'batch_timeout' is '30'");
        } catch (AssertionError e) {
            System.out.println("[OrionSink.configure] - FAIL - The default configuration value for "
                    + "'batch_timeout' is '" + sink.getBatchTimeout() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(10, sink.getBatchTTL());
            System.out.println("[OrionSink.configure] -  OK  - The default configuration value for "
                    + "'batch_ttl' is '10'");
        } catch (AssertionError e) {
            System.out.println("[OrionSink.configure] - FAIL - The default configuration value for "
                    + "'batch_ttl' is '" + sink.getBatchTTL() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(DataModel.DMBYENTITY, sink.getDataModel());
            System.out.println("[OrionSink.configure] -  OK  - The default configuration value for "
                    + "'data_model' is 'dm-by-entity'");
        } catch (AssertionError e) {
            System.out.println("[OrionSink.configure] - FAIL - The default configuration value for "
                    + "'data_model' is '" + sink.getDataModel() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(false, sink.getEnableGrouping());
            System.out.println("[OrionSink.configure] -  OK  - The default configuration value for "
                    + "'enable_grouping' is 'false'");
        } catch (AssertionError e) {
            System.out.println("[OrionSink.configure] - FAIL - The default configuration value for "
                    + "'enable_grouping' is '" + sink.getEnableGrouping() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals(false, sink.getEnableLowerCase());
            System.out.println("[OrionSink.configure] -  OK  - The default configuration value for "
                    + "'enable_lowercase' is 'false'");
        } catch (AssertionError e) {
            System.out.println("[OrionSink.configure] - FAIL - The default configuration value for "
                    + "'enable_lowercase' is '" + sink.getEnableLowerCase() + "'");
            throw e;
        } // try catch
    } // testConfigureNotMandatoryParameters
    
    /**
     * [OrionSink.configure] -------- The configuration becomes invalid upon out-of-the-limits configured values
     * for parameters having a discrete set of accepted values, or numerical values having upper or lower limits.
     */
    @Test
    public void testConfigureInvalidConfiguration() {
        System.out.println("[OrionSink.configure] -------- The configuration becomes invalid upon out-of-the-limits "
                + "configured values for parameters having a discrete set of accepted values, or numerical values "
                + "having upper or lower limits");
        OrionSinkImpl sink = new OrionSinkImpl();
        String configuredBatchSize = "0";
        sink.configure(createContext(configuredBatchSize, null, null, null, null, null));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println("[OrionSink.configure] -  OK  - A wrong configuration 'batch_size='"
                    + configuredBatchSize + "' has been detected");
        } catch (AssertionError e) {
            System.out.println("[OrionSink.configure] - FAIL - A wrong configuration 'batch_size='"
                    + configuredBatchSize + "' has not been detected");
            throw e;
        } // try catch
        
        sink = new OrionSinkImpl();
        String configuredBatchTimeout = "0";
        sink.configure(createContext(null, configuredBatchTimeout, null, null, null, null));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println("[OrionSink.configure] -  OK  - A wrong configuration 'batch_timeout='"
                    + configuredBatchTimeout + "' has been detected");
        } catch (AssertionError e) {
            System.out.println("[OrionSink.configure] - FAIL - A wrong configuration 'batch_timeout='"
                    + configuredBatchTimeout + "' has not been detected");
            throw e;
        } // try catch
        
        sink = new OrionSinkImpl();
        String configuredBatchTTL = "-2";
        sink.configure(createContext(null, null, configuredBatchTTL, null, null, null));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println("[OrionSink.configure] -  OK  - A wrong configuration 'batch_ttl='"
                    + configuredBatchTTL + "' has been detected");
        } catch (AssertionError e) {
            System.out.println("[OrionSink.configure] - FAIL - A wrong configuration 'batch_ttl='"
                    + configuredBatchTTL + "' has not been detected");
            throw e;
        } // try catch
        
        sink = new OrionSinkImpl();
        String dataModel = "dm-by-other";
        sink.configure(createContext(null, null, null, dataModel, null, null));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println("[OrionSink.configure] -  OK  - A wrong configuration 'data_model='"
                    + dataModel + "' has been detected");
        } catch (AssertionError e) {
            System.out.println("[OrionSink.configure] - FAIL - A wrong configuration 'data_model='"
                    + dataModel + "' has not been detected");
            throw e;
        } // try catch
        
        sink = new OrionSinkImpl();
        String configuredEnableGrouping = "falso";
        sink.configure(createContext(null, null, null, null, configuredEnableGrouping, null));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println("[OrionSink.configure] -  OK  - A wrong configuration 'enable_grouping='"
                    + configuredEnableGrouping + "' has been detected");
        } catch (AssertionError e) {
            System.out.println("[OrionSink.configure] - FAIL - A wrong configuration 'enable_grouping='"
                    + configuredEnableGrouping + "' has not been detected");
            throw e;
        } // try catch
        
        sink = new OrionSinkImpl();
        String configuredEnableLowercase = "verdadero";
        sink.configure(createContext(null, null, null, null, null, configuredEnableLowercase));
        
        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println("[OrionSink.configure] -  OK  - A wrong configuration 'enable_lowercase='"
                    + configuredEnableLowercase + "' has been detected");
        } catch (AssertionError e) {
            System.out.println("[OrionSink.configure] - FAIL - A wrong configuration 'enable_lowercase='"
                    + configuredEnableLowercase + "' has not been detected");
            throw e;
        } // try catch
    } // testConfigureInvalidConfiguration
    
    private Context createContext(String batchSize, String batchTimeout, String batchTTL, String dataModel,
            String enableGrouping, String enableLowercase) {
        Context context = new Context();
        context.put("batch_size", batchSize);
        context.put("batch_timeout", batchTimeout);
        context.put("batch_ttl", batchTTL);
        context.put("data_model", dataModel);
        context.put("enable_grouping", enableGrouping);
        context.put("enable_lowercase", enableLowercase);
        return context;
    } // createContext
    
} // OrionSinkTest
