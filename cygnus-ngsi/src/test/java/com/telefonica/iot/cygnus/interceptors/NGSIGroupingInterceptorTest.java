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
package com.telefonica.iot.cygnus.interceptors;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import java.util.Map;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NGSIGroupingInterceptorTest {
    
    /**
     * Constructor.
     */
    public NGSIGroupingInterceptorTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSIGroupingInterceptorTest
    
    /**
     * [GroupingInterceptor.Builder.configure] -------- Not mandatory parameters get the default value on configure().
     */
    @Test
    public void testBuilderConfigureDefaultValues() {
        System.out.println(getTestTraceHead("[GroupingInterceptor.Builder.configure]")
                + "-------- Not mandatory parameters get the default value on configure()");
        NGSIGroupingInterceptor.Builder builder = new NGSIGroupingInterceptor.Builder();
        String groupingRulesConfFile = "/grouping_rules.conf";
        String enableEncoding = null; // default value
        Context context = createBuilderContext(enableEncoding, groupingRulesConfFile);
        builder.configure(context);
        
        try {
            assertTrue(!builder.getEnableNewEncoding());
            System.out.println(getTestTraceHead("[GroupingInterceptor.Builder.configure]")
                    + "-  OK  - 'enable_new_encoding' is configured to 'false' by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.Builder.configure]")
                    + "- FAIL - 'enable_new_encoding' is not configured to 'false' by default");
            throw e;
        } // try catch
    } // testBuilderConfigureDefaultValues
    
    /**
     * [GroupingInterceptor.Builder.configure] -------- When configured, enable_new_encoding must be 'true' or 'false'.
     */
    @Test
    public void testBuilderConfigureEnableNewEncodingOK() {
        System.out.println(getTestTraceHead("[GroupingInterceptor.Builder.configure]")
                + "-------- When configured, enable_new_encoding must be 'true' or 'false'");
        NGSIGroupingInterceptor.Builder builder = new NGSIGroupingInterceptor.Builder();
        String groupingRulesConfFile = "/grouping_rules.conf";
        String enableEncoding = "falso"; // wrong value
        Context context = createBuilderContext(enableEncoding, groupingRulesConfFile);
        builder.configure(context);
        
        try {
            assertTrue(builder.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[GroupingInterceptor.Builder.configure]")
                    + "-  OK  - 'enable_new_encoding=falso' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.Builder.configure]")
                    + "- FAIL - 'enable_new_encoding=falso' has not been detected");
            throw e;
        } // try catch
    } // testBuilderConfigureEnableNewEncodingOK
    
    /**
     * [GroupingInterceptor.Builder.configure] -------- Configured 'grouping_rules_conf_file' cannot be empty.
     */
    @Test
    public void testBuilderConfigureGroupingRulesConfFileNotEmpty() {
        System.out.println(getTestTraceHead("[GroupingInterceptor.Builder.configure]")
                + "-------- Configured 'grouping_rules_conf_file' cannot be empty");
        NGSIGroupingInterceptor.Builder builder = new NGSIGroupingInterceptor.Builder();
        String groupingRulesConfFile = ""; // wrong value
        String enableEncoding = null; // default value
        Context context = createBuilderContext(enableEncoding, groupingRulesConfFile);
        builder.configure(context);
        
        try {
            assertTrue(builder.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[GroupingInterceptor.Builder.configure]")
                    + "-  OK  - Empty 'grouping_rules_conf_file' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.Builder.configure]")
                    + "- FAIL - Empty 'grouping_rules_conf_file' has not been detected");
            throw e;
        } // try catch
    } // testBuilderConfigureGroupingRulesConfFileNotEmpty
    
    /**
     * [GroupingInterceptor.Builder.configure] -------- Configured 'grouping_rules_conf_file' cannot be null.
     */
    @Test
    public void testBuilderConfigureGroupingRulesConfFileNotNull() {
        System.out.println(getTestTraceHead("[GroupingInterceptor.Builder.configure]")
                + "-------- Configured 'grouping_rules_conf_file' cannot be null");
        NGSIGroupingInterceptor.Builder builder = new NGSIGroupingInterceptor.Builder();
        String groupingRulesConfFile = null; // wrong value
        String enableEncoding = null; // default value
        Context context = createBuilderContext(enableEncoding, groupingRulesConfFile);
        builder.configure(context);
        
        try {
            assertTrue(builder.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[GroupingInterceptor.Builder.configure]")
                    + "-  OK  - Null 'grouping_rules_conf_file' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.Builder.configure]")
                    + "- FAIL - Null 'grouping_rules_conf_file' has not been detected");
            throw e;
        } // try catch
    } // testBuilderConfigureGroupingRulesConfFileNotNull
    
    /**
     * [NGSIGroupingInterceptor.getEvents] -------- When a Flume event is put in the channel, it contains
     * fiware-service, fiware-servicepath, fiware-correlator, transaction-id, notified-entities, grouped-servicepath
     * and grouped-entities headers.
     */
    @Test
    public void testGetEventsHeadersInFlumeEvent() {
        System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                + "-------- When a Flume event is put in the channel, it contains fiware-service, fiware-servicepath, "
                + "fiware-correlator, transaction-id, notified-entities, grouped-servicepaths and grouped-entities "
                + "headers");
        NGSIGroupingInterceptor groupingInterceptor = new NGSIGroupingInterceptor("", false, false);
        groupingInterceptor.initialize();
        String originalCEStr = ""
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
        String service = "default";
        String servicePath = "/default";
        String correlatorID = "12345";
        NGSIEvent originalEvent;
        
        try {
            originalEvent = NGSIUtilsForTests.createNGSIEvent(originalCEStr, null, service, servicePath, correlatorID);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "- FAIL - There was a problem when creating the NGSIEvent");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        Map<String, String> interceptedEventHeaders = groupingInterceptor.intercept(originalEvent).getHeaders();

        try {
            assertTrue(interceptedEventHeaders.containsKey("fiware-service"));
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "-  OK  - The generated Flume event contains 'fiware-service'");
        } catch (AssertionError e1) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "- FAIL - The generated Flume event does not contain 'fiware-service'");
            throw e1;
        } // try catch

        try {
            assertTrue(interceptedEventHeaders.containsKey("fiware-servicepath"));
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "-  OK  - The generated Flume event contains 'fiware-servicepath'");
        } catch (AssertionError e2) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "- FAIL - The generated Flume event does not contain 'fiware-servicepath'");
            throw e2;
        } // try catch

        try {
            assertTrue(interceptedEventHeaders.containsKey("fiware-correlator"));
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "-  OK  - The generated Flume event contains 'fiware-correlator'");
        } catch (AssertionError e3) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "- FAIL - The generated Flume event does not contain 'fiware-correlator'");
            throw e3;
        } // try catch

        try {
            assertTrue(interceptedEventHeaders.containsKey("transaction-id"));
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "-  OK  - The generated Flume event contains 'transaction-id'");
        } catch (AssertionError e4) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "- FAIL - The generated Flume event does not contain 'transaction-id'");
            throw e4;
        } // try catch
        
        try {
            assertTrue(interceptedEventHeaders.containsKey("notified-entity"));
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "-  OK  - The generated Flume event contains 'notified-entity'");
        } catch (AssertionError e5) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "- FAIL - The generated Flume event does not contain 'notified-entity'");
            throw e5;
        } // try catch
        
        try {
            assertTrue(interceptedEventHeaders.containsKey("grouped-servicepath"));
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "-  OK  - The generated Flume event contains 'grouped-servicepath'");
        } catch (AssertionError e6) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "- FAIL - The generated Flume event does not contain 'grouped-servicepath'");
            throw e6;
        } // try catch
        
        try {
            assertTrue(interceptedEventHeaders.containsKey("grouped-entity"));
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "-  OK  - The generated Flume event contains 'grouped-entity'");
        } catch (AssertionError e7) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "- FAIL - The generated Flume event does not contain 'grouped-entity'");
            throw e7;
        } // try catch
    } // testGetEventsHeadersInFlumeEvent
    
    private Context createBuilderContext(String enableEncoding, String groupingRulesConfFile) {
        Context context = new Context();
        context.put("enable_encoding", enableEncoding);
        context.put("grouping_rules_conf_file", groupingRulesConfFile);
        return context;
    } // createBuilderContext

} // NGSIGroupingInterceptorTest
