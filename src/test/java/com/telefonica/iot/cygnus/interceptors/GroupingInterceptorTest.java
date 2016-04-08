/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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

import com.telefonica.iot.cygnus.utils.TestUtils;
import static com.telefonica.iot.cygnus.utils.TestUtils.getTestTraceHead;
import java.util.Map;
import org.apache.flume.Event;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class GroupingInterceptorTest {
    
    /**
     * [GroupingInterceptor.getEvents] -------- When a Flume event is put in the channel, it contains fiware-service,
     * fiware-servicepath, fiware-correlator, transaction-id, notified-entities, grouped-servicepath and
     * grouped-entities headers.
     */
    @Test
    public void testGetEventsHeadersInFlumeEvent() {
        System.out.println("[GroupingInterceptor.intercept] -------- When a Flume event is put in the channel, it "
                + "contains fiware-service, fiware-servicepath, fiware-correlator, transaction-id, notified-entities, "
                + "grouped-servicepaths and grouped-entities headers");
        GroupingInterceptor groupingInterceptor = new GroupingInterceptor("");
        groupingInterceptor.initialize();
        Event originalEvent = TestUtils.createEvent();
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
            assertTrue(interceptedEventHeaders.containsKey("notified-entities"));
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "-  OK  - The generated Flume event contains 'notified-entities'");
        } catch (AssertionError e5) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "- FAIL - The generated Flume event does not contain 'notified-entities'");
            throw e5;
        } // try catch
        
        try {
            assertTrue(interceptedEventHeaders.containsKey("grouped-servicepaths"));
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "-  OK  - The generated Flume event contains 'grouped-servicepaths'");
        } catch (AssertionError e6) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "- FAIL - The generated Flume event does not contain 'grouped-servicepaths'");
            throw e6;
        } // try catch
        
        try {
            assertTrue(interceptedEventHeaders.containsKey("grouped-entities"));
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "-  OK  - The generated Flume event contains 'grouped-entities'");
        } catch (AssertionError e7) {
            System.out.println(getTestTraceHead("[GroupingInterceptor.intercept]")
                    + "- FAIL - The generated Flume event does not contain 'grouped-entities'");
            throw e7;
        } // try catch
    } // testGetEventsHeadersInFlumeEvent
    
} // GroupingInterceptorTest
