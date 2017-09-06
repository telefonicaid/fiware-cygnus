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

import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NGSIBatchTest {
    
    /**
     * Constructor.
     */
    public NGSIBatchTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSICKANSinkTest
    
    /**
     * [NGSIBatch.addEvent] -------- An event is added to a not existent subbatch.
     */
    @Test
    public void testAddEventNotExistentSubbatch() {
        System.out.println(getTestTraceHead("[NGSIBatch.addEvent]")
                + "-------- An event is added to a not existent subbatch");
        NGSIBatch batch = new NGSIBatch();
        String destination = "someDestination";
        String originalCEStr = ""; // not necessary a real one for this test
        String mappedCEStr = ""; // not necessary a real one for this test
        String service = "someService";
        String servicePath = "/someServicePath";
        String correlatorId = "12345";
        NGSIEvent event;
        
        try {
            event = NGSIUtilsForTests.createNGSIEvent(originalCEStr, mappedCEStr, service, servicePath, correlatorId);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIBatch.addEvent]")
                    + "- FAIL - There was some problem when creating the NGSI event");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        batch.addEvent(destination, event);
        batch.startIterator();
        
        try {
            assertTrue(batch.hasNext());
            System.out.println(getTestTraceHead("[NGSIBatch.addEvent]")
                    + "-  OK  - The batch has subbatches");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIBatch.addEvent]")
                    + "- FAIL - The batch has not subbatches");
            throw e;
        } // try catch
        
        try {
            assertEquals(event, batch.getNextEvents().get(0));
            System.out.println(getTestTraceHead("[NGSIBatch.addEvent]")
                    + "-  OK  - The event within the only subbatch is the added one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIBatch.addEvent]")
                    + "- FAIL - The event within the only subbatch is not the added one");
            throw e;
        } // try catch
    } // testAddEventNotExistentSubbatch
    
    /**
     * [NGSIBatch.addEvent] -------- An event is added to an already existent subbatch.
     */
    @Test
    public void testAddEventExistentSubbatch() {
        System.out.println(getTestTraceHead("[NGSIBatch.addEvent]")
                + "-------- An event is added to an already existent subbatch");
        NGSIBatch batch = new NGSIBatch();
        String destination = "someDestination";
        String originalCEStr = ""; // not necessary a real one for this test
        String mappedCEStr = ""; // not necessary a real one for this test
        String service = "someService";
        String servicePath = "/someServicePath";
        String correlatorId = "12345";
        NGSIEvent event1;
        
        try {
            event1 = NGSIUtilsForTests.createNGSIEvent(originalCEStr, mappedCEStr, service, servicePath, correlatorId);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIBatch.addEvent]")
                    + "- FAIL - There was some problem when creating the first NGSI event");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        batch.addEvent(destination, event1);
        NGSIEvent event2;
        
        try {
            event2 = NGSIUtilsForTests.createNGSIEvent(originalCEStr, mappedCEStr, service, servicePath, correlatorId);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIBatch.addEvent]")
                    + "- FAIL - There was some problem when creating the second NGSI event");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        batch.addEvent(destination, event2);
        batch.startIterator();
        
        try {
            assertTrue(batch.hasNext());
            System.out.println(getTestTraceHead("[NGSIBatch.addEvent]")
                    + "-  OK  - The batch has subbatches");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIBatch.addEvent]")
                    + "- FAIL - The batch has not subbatches");
            throw e;
        } // try catch
        
        try {
            assertEquals(event2, batch.getNextEvents().get(1));
            System.out.println(getTestTraceHead("[NGSIBatch.addEvent]")
                    + "-  OK  - The second event within the only subbatch is the secondly added one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIBatch.addEvent]")
                    + "- FAIL - The second event within the only subbatch is not the secondly added one");
            throw e;
        } // try catch
    } // testAddEventExistentSubbatch
    
} // NGSIBatchTest
