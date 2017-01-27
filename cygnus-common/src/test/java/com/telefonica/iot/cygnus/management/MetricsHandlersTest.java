/**
 * Copyright 2017 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.management;

import com.google.common.collect.ImmutableMap;
import com.telefonica.iot.cygnus.handlers.CygnusHandler;
import com.telefonica.iot.cygnus.metrics.CygnusMetrics;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import java.lang.reflect.Field;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.Source;
import org.apache.flume.SourceRunner;
import org.apache.flume.source.EventDrivenSourceRunner;
import org.apache.flume.source.http.HTTPSource;
import org.apache.flume.source.http.HTTPSourceHandler;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class MetricsHandlersTest {
    
    /**
     * Constructor.
     */
    public MetricsHandlersTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // MetricsHandlersTest
    
    /**
     * Dummy class for testing purposes.
     */
    private class CygnusHandlerImpl extends CygnusHandler implements HTTPSourceHandler {

        @Override
        public List<Event> getEvents(HttpServletRequest hsr) {
            throw new UnsupportedOperationException("Not supported yet.");
        } // getEvents

        @Override
        public void configure(Context cntxt) {
            throw new UnsupportedOperationException("Not supported yet.");
        } // configure
        
    } // CygnusHandlerImpl

    /**
     * [MetricsHandlers.mergeMetrics] -------- Metrics from two sources are merged.
     */
    @Test
    public void testMerge() {
        System.out.println(getTestTraceHead("[MetricsHandlers.mergeMetrics]")
                + " - Metrics from two sources are merged");
        
        // Names
        String service1 = "service1";
        String subservice11 = "subservice11";
        String service2 = "service2";
        String subservice21 = "subservice21";
        String subservice22 = "subservice22";
        
        // Sources
        String source1 = "source1";
        String source2 = "source2";
        
        // Source 1 organization and metrics
        // service1
        //    subservice11 -> 1000, 12000, 500, 5
        // service2
        //    subservice21 -> 8000, 340000, 3700, 90
        //    subservice22 -> 1500, 10000, 1000, 10
        CygnusMetrics source1Metrics = new CygnusMetrics();
        source1Metrics.add(service1, subservice11, 1000, 12000, 500, 5, 400.3, 1000, 25000, 0, 0);
        source1Metrics.add(service2, subservice21, 8000, 340000, 3700, 90, 430.1, 9000, 234000, 1200, 10);
        source1Metrics.add(service2, subservice22, 1500, 10000, 1000, 10, 501.2, 1500, 230000, 100, 1);
        
        // Source 2 organization and metrics
        // service1
        //    subservice11 -> 12500, 256700, 10500, 215
        // service2
        //    subservice22 -> 10, 100, 30, 0
        CygnusMetrics source2Metrics = new CygnusMetrics();
        source2Metrics.add(service1, subservice11, 12500, 256700, 10500, 215, 799.9, 50000, 1000000, 10000, 2000);
        source2Metrics.add(service2, subservice22, 10, 100, 30, 0, 569.5, 10, 1000, 500, 10);
        
        // Assign to all the sources a Cygnus handler containing metrics
        // Use Java reflection to hack SourceRunner class
        SourceRunner sr1;
        SourceRunner sr2;

        try {
            CygnusHandler ch1 = new CygnusHandlerImpl();
            ch1.setServiceMetrics(source1Metrics);
            sr1 = new EventDrivenSourceRunner();
            Source s1 = new HTTPSource();
            sr1.setSource(s1);
            Field f = s1.getClass().getDeclaredField("handler");
            f.setAccessible(true);
            f.set(s1, ch1);
            CygnusHandler ch2 = new CygnusHandlerImpl();
            ch2.setServiceMetrics(source2Metrics);
            sr2 = new EventDrivenSourceRunner();
            Source s2 = new HTTPSource();
            sr2.setSource(s2);
            f = s2.getClass().getDeclaredField("handler");
            f.setAccessible(true);
            f.set(s2, ch2);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            System.out.println(getTestTraceHead("[MetricsHandlers.mergeMetrics]")
                    + " - FAIL - Metrics merge was not correct ");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        // Create the list of sources
        ImmutableMap<String, SourceRunner> sources = ImmutableMap.of(source1, sr1, source2, sr2);
        
        // Do the merge for source's metrics
        CygnusMetrics mergedServiceMetrics = MetricsHandlers.mergeMetrics(sources, null);
        
        // Check the merge for subservice 11 within service 1
        CygnusMetrics.Metrics subservice11metrics =
                mergedServiceMetrics.getServiceSubserviceMetrics(service1, subservice11);
        
        try {
            assertEquals(13500, subservice11metrics.getIncomingTransactions());
            assertEquals(268700, subservice11metrics.getIncomingTransactionRequestSize());
            assertEquals(11000, subservice11metrics.getIncomingTransactionResponseSize());
            assertEquals(220, subservice11metrics.getIncomingTransactionErrors());
            assertEquals(1200.2, subservice11metrics.getServiceTime(), 0);
            assertEquals(51000, subservice11metrics.getOutgoingTransactions());
            assertEquals(1025000, subservice11metrics.getOutgoingTransactionRequestSize());
            assertEquals(10000, subservice11metrics.getOutgoingTransactionResponseSize());
            assertEquals(2000, subservice11metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for subservice 21 within service 2
        CygnusMetrics.Metrics subservice21metrics =
                mergedServiceMetrics.getServiceSubserviceMetrics(service2, subservice21);
        
        try {
            assertEquals(8000, subservice21metrics.getIncomingTransactions());
            assertEquals(340000, subservice21metrics.getIncomingTransactionRequestSize());
            assertEquals(3700, subservice21metrics.getIncomingTransactionResponseSize());
            assertEquals(90, subservice21metrics.getIncomingTransactionErrors());
            assertEquals(430.1, subservice21metrics.getServiceTime(), 0);
            assertEquals(9000, subservice21metrics.getOutgoingTransactions());
            assertEquals(234000, subservice21metrics.getOutgoingTransactionRequestSize());
            assertEquals(1200, subservice21metrics.getOutgoingTransactionResponseSize());
            assertEquals(10, subservice21metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for subservice 22 within service 2
        CygnusMetrics.Metrics subservice22metrics =
                mergedServiceMetrics.getServiceSubserviceMetrics(service2, subservice22);
        
        try {
            assertEquals(1510, subservice22metrics.getIncomingTransactions());
            assertEquals(10100, subservice22metrics.getIncomingTransactionRequestSize());
            assertEquals(1030, subservice22metrics.getIncomingTransactionResponseSize());
            assertEquals(10, subservice22metrics.getIncomingTransactionErrors());
            assertEquals(1070.7, subservice22metrics.getServiceTime(), 0);
            assertEquals(1510, subservice22metrics.getOutgoingTransactions());
            assertEquals(231000, subservice22metrics.getOutgoingTransactionRequestSize());
            assertEquals(600, subservice22metrics.getOutgoingTransactionResponseSize());
            assertEquals(11, subservice22metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for service 1 aggregated
        CygnusMetrics.Metrics service1metrics =
                mergedServiceMetrics.getServiceAggrMetrics(service1);
        
        try {
            assertEquals(13500, service1metrics.getIncomingTransactions());
            assertEquals(268700, service1metrics.getIncomingTransactionRequestSize());
            assertEquals(11000, service1metrics.getIncomingTransactionResponseSize());
            assertEquals(220, service1metrics.getIncomingTransactionErrors());
            assertEquals(1200.2, service1metrics.getServiceTime(), 0);
            assertEquals(51000, service1metrics.getOutgoingTransactions());
            assertEquals(1025000, service1metrics.getOutgoingTransactionRequestSize());
            assertEquals(10000, service1metrics.getOutgoingTransactionResponseSize());
            assertEquals(2000, service1metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for service 2 aggregated
        CygnusMetrics.Metrics service2metrics =
                mergedServiceMetrics.getServiceAggrMetrics(service2);
        
        try {
            assertEquals(9510, service2metrics.getIncomingTransactions());
            assertEquals(350100, service2metrics.getIncomingTransactionRequestSize());
            assertEquals(4730, service2metrics.getIncomingTransactionResponseSize());
            assertEquals(100, service2metrics.getIncomingTransactionErrors());
            assertEquals(1500.8, service2metrics.getServiceTime(), 0);
            assertEquals(10510, service2metrics.getOutgoingTransactions());
            assertEquals(465000, service2metrics.getOutgoingTransactionRequestSize());
            assertEquals(1800, service2metrics.getOutgoingTransactionResponseSize());
            assertEquals(21, service2metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for subservice 11 aggregated
        subservice11metrics =
                mergedServiceMetrics.getSubserviceAggrMetrics(subservice11);
        
        try {
            assertEquals(13500, subservice11metrics.getIncomingTransactions());
            assertEquals(268700, subservice11metrics.getIncomingTransactionRequestSize());
            assertEquals(11000, subservice11metrics.getIncomingTransactionResponseSize());
            assertEquals(220, subservice11metrics.getIncomingTransactionErrors());
            assertEquals(1200.2, subservice11metrics.getServiceTime(), 0);
            assertEquals(51000, subservice11metrics.getOutgoingTransactions());
            assertEquals(1025000, subservice11metrics.getOutgoingTransactionRequestSize());
            assertEquals(10000, subservice11metrics.getOutgoingTransactionResponseSize());
            assertEquals(2000, subservice11metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for subservice 21 aggregated
        subservice21metrics = mergedServiceMetrics.getSubserviceAggrMetrics(subservice21);
        
        try {
            assertEquals(8000, subservice21metrics.getIncomingTransactions());
            assertEquals(340000, subservice21metrics.getIncomingTransactionRequestSize());
            assertEquals(3700, subservice21metrics.getIncomingTransactionResponseSize());
            assertEquals(90, subservice21metrics.getIncomingTransactionErrors());
            assertEquals(430.1, subservice21metrics.getServiceTime(), 0);
            assertEquals(9000, subservice21metrics.getOutgoingTransactions());
            assertEquals(234000, subservice21metrics.getOutgoingTransactionRequestSize());
            assertEquals(1200, subservice21metrics.getOutgoingTransactionResponseSize());
            assertEquals(10, subservice21metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for subservice 22 aggregated
        subservice22metrics = mergedServiceMetrics.getSubserviceAggrMetrics(subservice22);
        
        try {
            assertEquals(1510, subservice22metrics.getIncomingTransactions());
            assertEquals(10100, subservice22metrics.getIncomingTransactionRequestSize());
            assertEquals(1030, subservice22metrics.getIncomingTransactionResponseSize());
            assertEquals(10, subservice22metrics.getIncomingTransactionErrors());
            assertEquals(1070.7, subservice22metrics.getServiceTime(), 0);
            assertEquals(1510, subservice22metrics.getOutgoingTransactions());
            assertEquals(231000, subservice22metrics.getOutgoingTransactionRequestSize());
            assertEquals(600, subservice22metrics.getOutgoingTransactionResponseSize());
            assertEquals(11, subservice22metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for all metrics
        CygnusMetrics.Metrics allMetrics = mergedServiceMetrics.getAllAggrMetrics();
        
        try {
            assertEquals(23010, allMetrics.getIncomingTransactions());
            assertEquals(618800, allMetrics.getIncomingTransactionRequestSize());
            assertEquals(15730, allMetrics.getIncomingTransactionResponseSize());
            assertEquals(320, allMetrics.getIncomingTransactionErrors());
            assertEquals(2701, allMetrics.getServiceTime(), 0);
            assertEquals(61510, allMetrics.getOutgoingTransactions());
            assertEquals(1490000, allMetrics.getOutgoingTransactionRequestSize());
            assertEquals(11800, allMetrics.getOutgoingTransactionResponseSize());
            assertEquals(2021, allMetrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.merge]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
    } // testMergeMetrics

    
} // MetricsHandlersTest
