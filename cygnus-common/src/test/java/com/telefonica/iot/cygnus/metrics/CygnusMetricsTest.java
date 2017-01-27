/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.telefonica.iot.cygnus.metrics;

import com.telefonica.iot.cygnus.metrics.CygnusMetrics.Metrics;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class CygnusMetricsTest {
    
    /**
     * Constructor.
     */
    public CygnusMetricsTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // CygnusMetricsTest

    /**
     * [CygnusMetrics.add] -------- Values are added to the metrics.
     */
    @Test
    public void testAdd() {
        System.out.println(getTestTraceHead("[CygnusMetrics.add]") + " - Values are added to the metrics");
        
        // Names
        String service1 = "service1";
        String subservice11 = "subservice11";
        String service2 = "service2";
        String subservice21 = "subservice21";
        String subservice22 = "subservice22";
        
        // Metrics
        CygnusMetrics metrics = new CygnusMetrics();
        metrics.add(service1, subservice11, 1000, 12000, 500, 5, 400.3, 1000, 25000, 0, 0);
        metrics.add(service2, subservice21, 8000, 340000, 3700, 90, 430.1, 9000, 234000, 1200, 10);
        metrics.add(service2, subservice22, 1500, 10000, 1000, 10, 501.2, 1500, 230000, 100, 1);
        
        // Check the merge for subservice 11 within service 1
        Metrics subservice11metrics = metrics.getServiceSubserviceMetrics(service1, subservice11);
        
        try {
            assertEquals(1000, subservice11metrics.getIncomingTransactions());
            assertEquals(12000, subservice11metrics.getIncomingTransactionRequestSize());
            assertEquals(500, subservice11metrics.getIncomingTransactionResponseSize());
            assertEquals(5, subservice11metrics.getIncomingTransactionErrors());
            assertEquals(400.3, subservice11metrics.getServiceTime(), 0);
            assertEquals(1000, subservice11metrics.getOutgoingTransactions());
            assertEquals(25000, subservice11metrics.getOutgoingTransactionRequestSize());
            assertEquals(0, subservice11metrics.getOutgoingTransactionResponseSize());
            assertEquals(0, subservice11metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for subservice 21 within service 2
        Metrics subservice21metrics = metrics.getServiceSubserviceMetrics(service2, subservice21);
        
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
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for subservice 22 within service 2
        Metrics subservice22metrics = metrics.getServiceSubserviceMetrics(service2, subservice22);
        
        try {
            assertEquals(1500, subservice22metrics.getIncomingTransactions());
            assertEquals(10000, subservice22metrics.getIncomingTransactionRequestSize());
            assertEquals(1000, subservice22metrics.getIncomingTransactionResponseSize());
            assertEquals(10, subservice22metrics.getIncomingTransactionErrors());
            assertEquals(501.2, subservice22metrics.getServiceTime(), 0);
            assertEquals(1500, subservice22metrics.getOutgoingTransactions());
            assertEquals(230000, subservice22metrics.getOutgoingTransactionRequestSize());
            assertEquals(100, subservice22metrics.getOutgoingTransactionResponseSize());
            assertEquals(1, subservice22metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for service 1 aggregated
        Metrics service1metrics = metrics.getServiceAggrMetrics(service1);
        
        try {
            assertEquals(1000, service1metrics.getIncomingTransactions());
            assertEquals(12000, service1metrics.getIncomingTransactionRequestSize());
            assertEquals(500, service1metrics.getIncomingTransactionResponseSize());
            assertEquals(5, service1metrics.getIncomingTransactionErrors());
            assertEquals(400.3, service1metrics.getServiceTime(), 0);
            assertEquals(1000, service1metrics.getOutgoingTransactions());
            assertEquals(25000, service1metrics.getOutgoingTransactionRequestSize());
            assertEquals(0, service1metrics.getOutgoingTransactionResponseSize());
            assertEquals(0, service1metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for service 2 aggregated
        Metrics service2metrics = metrics.getServiceAggrMetrics(service2);
        
        try {
            assertEquals(9500, service2metrics.getIncomingTransactions());
            assertEquals(350000, service2metrics.getIncomingTransactionRequestSize());
            assertEquals(4700, service2metrics.getIncomingTransactionResponseSize());
            assertEquals(100, service2metrics.getIncomingTransactionErrors());
            assertEquals(931.3, service2metrics.getServiceTime(), 0);
            assertEquals(10500, service2metrics.getOutgoingTransactions());
            assertEquals(464000, service2metrics.getOutgoingTransactionRequestSize());
            assertEquals(1300, service2metrics.getOutgoingTransactionResponseSize());
            assertEquals(11, service2metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for subservice 11 aggregated
        subservice11metrics =
                metrics.getSubserviceAggrMetrics(subservice11);
        
        try {
            assertEquals(1000, subservice11metrics.getIncomingTransactions());
            assertEquals(12000, subservice11metrics.getIncomingTransactionRequestSize());
            assertEquals(500, subservice11metrics.getIncomingTransactionResponseSize());
            assertEquals(5, subservice11metrics.getIncomingTransactionErrors());
            assertEquals(400.3, subservice11metrics.getServiceTime(), 0);
            assertEquals(1000, subservice11metrics.getOutgoingTransactions());
            assertEquals(25000, subservice11metrics.getOutgoingTransactionRequestSize());
            assertEquals(0, subservice11metrics.getOutgoingTransactionResponseSize());
            assertEquals(0, subservice11metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for subservice 21 aggregated
        subservice21metrics = metrics.getSubserviceAggrMetrics(subservice21);
        
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
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for subservice 22 aggregated
        subservice22metrics = metrics.getSubserviceAggrMetrics(subservice22);
        
        try {
            assertEquals(1500, subservice22metrics.getIncomingTransactions());
            assertEquals(10000, subservice22metrics.getIncomingTransactionRequestSize());
            assertEquals(1000, subservice22metrics.getIncomingTransactionResponseSize());
            assertEquals(10, subservice22metrics.getIncomingTransactionErrors());
            assertEquals(501.2, subservice22metrics.getServiceTime(), 0);
            assertEquals(1500, subservice22metrics.getOutgoingTransactions());
            assertEquals(230000, subservice22metrics.getOutgoingTransactionRequestSize());
            assertEquals(100, subservice22metrics.getOutgoingTransactionResponseSize());
            assertEquals(1, subservice22metrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
        
        // Check the merge for all metrics
        Metrics allMetrics = metrics.getAllAggrMetrics();
        
        try {
            assertEquals(10500, allMetrics.getIncomingTransactions());
            assertEquals(362000, allMetrics.getIncomingTransactionRequestSize());
            assertEquals(5200, allMetrics.getIncomingTransactionResponseSize());
            assertEquals(105, allMetrics.getIncomingTransactionErrors());
            assertEquals(1331.6, allMetrics.getServiceTime(), 0.001);
            assertEquals(11500, allMetrics.getOutgoingTransactions());
            assertEquals(489000, allMetrics.getOutgoingTransactionRequestSize());
            assertEquals(1300, allMetrics.getOutgoingTransactionResponseSize());
            assertEquals(11, allMetrics.getOutgoingTransactionErrors());
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " -  OK  - Metrics merge was correct");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.add]")
                    + " - FAIL - Metrics merge was not corect ");
            throw e;
        } // try catch
    } // testAdd
    
    /**
     * [CygnusMetrics.merge] -------- Two metrics are merged.
     */
    @Test
    public void testMerge() {
        System.out.println(getTestTraceHead("[CygnusMetrics.merge]") + " - Two metrics are merged");
        
        // Names
        String service1 = "service1";
        String subservice11 = "subservice11";
        String service2 = "service2";
        String subservice21 = "subservice21";
        String subservice22 = "subservice22";
        
        // First metrics
        CygnusMetrics metrics1 = new CygnusMetrics();
        metrics1.add(service1, subservice11, 1000, 12000, 500, 5, 400.3, 1000, 25000, 0, 0);
        metrics1.add(service2, subservice21, 8000, 340000, 3700, 90, 430.1, 9000, 234000, 1200, 10);
        metrics1.add(service2, subservice22, 1500, 10000, 1000, 10, 501.2, 1500, 230000, 100, 1);
        
        // Second metrics
        CygnusMetrics metrics2 = new CygnusMetrics();
        metrics2.add(service1, subservice11, 12500, 256700, 10500, 215, 799.9, 50000, 1000000, 10000, 2000);
        metrics2.add(service2, subservice22, 10, 100, 30, 0, 569.5, 10, 1000, 500, 10);

        // Merge second metrics in first one
        metrics1.merge(metrics2);
        
        // Check the merge for subservice 11 within service 1
        Metrics subservice11metrics = metrics1.getServiceSubserviceMetrics(service1, subservice11);
        
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
        Metrics subservice21metrics = metrics1.getServiceSubserviceMetrics(service2, subservice21);
        
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
        Metrics subservice22metrics = metrics1.getServiceSubserviceMetrics(service2, subservice22);
        
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
        Metrics service1metrics = metrics1.getServiceAggrMetrics(service1);
        
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
        Metrics service2metrics = metrics1.getServiceAggrMetrics(service2);
        
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
                metrics1.getSubserviceAggrMetrics(subservice11);
        
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
        subservice21metrics = metrics1.getSubserviceAggrMetrics(subservice21);
        
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
        subservice22metrics = metrics1.getSubserviceAggrMetrics(subservice22);
        
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
        Metrics allMetrics = metrics1.getAllAggrMetrics();
        
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
    
    /**
     * [CygnusMetrics.toJsonString] -------- Metrics are printed as Json string.
     */
    @Test
    public void testToJsonString() {
        System.out.println(getTestTraceHead("[CygnusMetrics.toJsonString]")
                + " - Metrics are printed as Json string");
        
        // Names
        String service1 = "service1";
        String subservice11 = "subservice11";
        String service2 = "service2";
        String subservice21 = "subservice21";
        String subservice22 = "subservice22";
        
        // Source 1 organization and metrics
        CygnusMetrics metrics = new CygnusMetrics();
        metrics.add(service1, subservice11, 1000, 12000, 500, 5, 400.3, 1000, 25000, 0, 0);
        metrics.add(service2, subservice21, 8000, 340000, 3700, 90, 430.1, 9000, 234000, 1200, 10);
        metrics.add(service2, subservice22, 1500, 10000, 1000, 10, 501.2, 1500, 230000, 100, 1);
        
        // To Json string
        String jsonStr = metrics.toJsonString();
        String expectedJsonStr = "{\"services\":{"
                + "\"service2\":{"
                + "\"subservs\":{"
                + "\"subservice22\":{"
                + "\"incomingTransactions\":1500,"
                + "\"incomingTransactionRequestSize\":10000,"
                + "\"incomingTransactionResponseSize\":1000,"
                + "\"incomingTransactionErrors\":10,"
                + "\"serviceTime\":0.33413333333333334,"
                + "\"outgoingTransactions\":1500,"
                + "\"outgoingTransactionRequestSize\":230000,"
                + "\"outgoingTransactionResponseSize\":100,"
                + "\"outgoingTransactionErrors\":1},"
                + "\"subservice21\":{"
                + "\"incomingTransactions\":8000,"
                + "\"incomingTransactionRequestSize\":340000,"
                + "\"incomingTransactionResponseSize\":3700,"
                + "\"incomingTransactionErrors\":90,"
                + "\"serviceTime\":0.047788888888888895,"
                + "\"outgoingTransactions\":9000,"
                + "\"outgoingTransactionRequestSize\":234000,"
                + "\"outgoingTransactionResponseSize\":1200,"
                + "\"outgoingTransactionErrors\":10}},"
                + "\"sum\":{"
                + "\"incomingTransactions\":9500,"
                + "\"incomingTransactionRequestSize\":350000,"
                + "\"incomingTransactionResponseSize\":4700,"
                + "\"incomingTransactionErrors\":100,"
                + "\"serviceTime\":0.08869523809523809,"
                + "\"outgoingTransactions\":10500,"
                + "\"outgoingTransactionRequestSize\":464000,"
                + "\"outgoingTransactionResponseSize\":1300,"
                + "\"outgoingTransactionErrors\":11}},"
                + "\"service1\":{"
                + "\"subservs\":{"
                + "\"subservice11\":{"
                + "\"incomingTransactions\":1000,"
                + "\"incomingTransactionRequestSize\":12000,"
                + "\"incomingTransactionResponseSize\":500,"
                + "\"incomingTransactionErrors\":5,"
                + "\"serviceTime\":0.4003,"
                + "\"outgoingTransactions\":1000,"
                + "\"outgoingTransactionRequestSize\":25000,"
                + "\"outgoingTransactionResponseSize\":0,"
                + "\"outgoingTransactionErrors\":0}},"
                + "\"sum\":{"
                + "\"incomingTransactions\":1000,"
                + "\"incomingTransactionRequestSize\":12000,"
                + "\"incomingTransactionResponseSize\":500,"
                + "\"incomingTransactionErrors\":5,"
                + "\"serviceTime\":0.4003,"
                + "\"outgoingTransactions\":1000,"
                + "\"outgoingTransactionRequestSize\":25000,"
                + "\"outgoingTransactionResponseSize\":0,"
                + "\"outgoingTransactionErrors\":0}}},"
                + "\"sum\": {"
                + "\"subservs\":{"
                + "\"subservice11\":{"
                + "\"incomingTransactions\":1000,"
                + "\"incomingTransactionRequestSize\":12000,"
                + "\"incomingTransactionResponseSize\":500,"
                + "\"incomingTransactionErrors\":5,"
                + "\"serviceTime\":0.4003,"
                + "\"outgoingTransactions\":1000,"
                + "\"outgoingTransactionRequestSize\":25000,"
                + "\"outgoingTransactionResponseSize\":0,"
                + "\"outgoingTransactionErrors\":0},"
                + "\"subservice22\":{"
                + "\"incomingTransactions\":1500,"
                + "\"incomingTransactionRequestSize\":10000,"
                + "\"incomingTransactionResponseSize\":1000,"
                + "\"incomingTransactionErrors\":10,"
                + "\"serviceTime\":0.33413333333333334,"
                + "\"outgoingTransactions\":1500,"
                + "\"outgoingTransactionRequestSize\":230000,"
                + "\"outgoingTransactionResponseSize\":100,"
                + "\"outgoingTransactionErrors\":1},"
                + "\"subservice21\":{"
                + "\"incomingTransactions\":8000,"
                + "\"incomingTransactionRequestSize\":340000,"
                + "\"incomingTransactionResponseSize\":3700,"
                + "\"incomingTransactionErrors\":90,"
                + "\"serviceTime\":0.047788888888888895,"
                + "\"outgoingTransactions\":9000,"
                + "\"outgoingTransactionRequestSize\":234000,"
                + "\"outgoingTransactionResponseSize\":1200,"
                + "\"outgoingTransactionErrors\":10}},"
                + "\"sum\":{"
                + "\"incomingTransactions\":10500,"
                + "\"incomingTransactionRequestSize\":362000,"
                + "\"incomingTransactionResponseSize\":5200,"
                + "\"incomingTransactionErrors\":105,"
                + "\"serviceTime\":0.1157913043478261,"
                + "\"outgoingTransactions\":11500,"
                + "\"outgoingTransactionRequestSize\":489000,"
                + "\"outgoingTransactionResponseSize\":1300,"
                + "\"outgoingTransactionErrors\":11}}}";
        
        try {
            assertEquals(expectedJsonStr, jsonStr);
            System.out.println(getTestTraceHead("[CygnusMetrics.toJsonString]")
                    + " -  OK  - Metrics were successfully printed as Json string");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.toJsonString]")
                    + " - FAIL - Metrics were not successfully printed as Json string");
            throw e;
        } // try catch
    } // testToJsonString
    
} // CygnusMetricsTest
