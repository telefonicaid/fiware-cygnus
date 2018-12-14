/**
 * Copyright 2017 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.handlers;

import com.telefonica.iot.cygnus.metrics.CygnusMetrics;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class CygnusHandlerTest {
    
    /**
     * Constructor.
     */
    public CygnusHandlerTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // CygnusHandlerTest
    
    /**
     * Dummy class for testing purposes.
     */
    private class CygnusHandlerImpl extends CygnusHandler {
    } // CygnusHandlerImpl
    
    /**
     * [CygnusHandler.getServiceMetrics] -------- Not null metrics are retrieved.
     */
    @Test
    public void testGetServiceMetrics() {
        System.out.println(getTestTraceHead("[CygnusHandler.getServiceMetrics]")
                + " - Not null metrics are retrieved");
        
        CygnusHandlerImpl ch = new CygnusHandlerImpl();
        CygnusMetrics metrics = ch.getServiceMetrics();
        
        try {
            assertTrue(metrics != null);
            System.out.println(getTestTraceHead("[CygnusMetrics.getServiceMetrics]")
                    + " -  OK  - Not null metrics were retrieved");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.getServiceMetrics]")
                    + " - FAIL - Null metrics were retrieved");
            throw e;
        } // try catch
    } // testGetServiceMetrics
    
    /**
     * [CygnusHandler.setServiceMetrics] -------- Given metrics are set.
     */
    @Test
    public void testSetServiceMetrics() {
        System.out.println(getTestTraceHead("[CygnusHandler.setServiceMetrics]")
                + " - Given metrics are set");
        
        CygnusHandlerImpl ch = new CygnusHandlerImpl();
        CygnusMetrics metrics = new CygnusMetrics();
        ch.setServiceMetrics(metrics);
        
        try {
            assertEquals(metrics, ch.getServiceMetrics());
            System.out.println(getTestTraceHead("[CygnusMetrics.setServiceMetrics]")
                    + " -  OK  - Metrics were set");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusMetrics.setServiceMetrics]")
                    + " - FAIL - Metrics were not set");
            throw e;
        } // try catch
    } // testGetServiceMetrics
    
} // CygnusHandlerTest
