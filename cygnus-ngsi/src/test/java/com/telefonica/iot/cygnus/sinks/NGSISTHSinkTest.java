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

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import java.util.Arrays;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NGSISTHSinkTest {
    
    /**
     * Constructor.
     */
    public NGSISTHSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSISTHSinkTest
    
    /**
     * [NGSIMongoSink.configure] -------- Non mandatory parameters get the default value when not configured.
     */
    @Test
    public void testConfigureDefaultValues() {
        System.out.println(getTestTraceHead("[NGSISTHSink.configure]")
                + "-------- Non mandatory parameters get the default value when not configured");
        String collectionPrefix = null; // default value
        String dbPrefix = null; // default value
        String dataModel = null; // default value
        String enableEncoding = null; // default value
        String resolutions = null; // default value
        NGSISTHSink sink = new NGSISTHSink();
        sink.configure(createContext(collectionPrefix, dbPrefix, dataModel, enableEncoding, resolutions));
        
        try {
            boolean[] expected = {true, true, true, true, true};
            assertTrue(Arrays.equals(expected, sink.resolutions));
            System.out.println(getTestTraceHead("[NGSISTHSink.configure]")
                    + "-  OK  - 'resolutions' gets the default value when not configured");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISTHSink.configure]")
                    + "- FAIL - 'resolutions' doesn't get the default value when not configured");
            throw e;
        } // try catch
    } // testConfigureCollectionPrefixIsNotSystem
    
    /**
     * [NGSIMongoSink.configure] -------- Non valid resolutions are detected.
     */
    @Test
    public void testConfigureInvalidResolutions() {
        System.out.println(getTestTraceHead("[NGSISTHSink.configure]")
                + "-------- Non valid resolutions are detected");
        String collectionPrefix = null; // default value
        String dbPrefix = null; // default value
        String dataModel = null; // default value
        String enableEncoding = null; // default value
        String resolutions = "month,week,second";
        NGSISTHSink sink = new NGSISTHSink();
        sink.configure(createContext(collectionPrefix, dbPrefix, dataModel, enableEncoding, resolutions));
        
        try {
            boolean[] expected = {true, false, false, false, true};
            assertTrue(Arrays.equals(expected, sink.resolutions));
            System.out.println(getTestTraceHead("[NGSISTHSink.configure]")
                    + "-  OK  - Invalid resolution 'week' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISTHSink.configure]")
                    + "- FAIL - Invalid resolution 'week' has not been detected");
            throw e;
        } // try catch
    } // testConfigureInvalidResolutions
    
    /**
     * [NGSIMongoSink.configure] -------- Empty 'resolutions' means no persistence for any resolution.
     */
    @Test
    public void testConfigureEmptyResolutions() {
        System.out.println(getTestTraceHead("[NGSISTHSink.configure]")
                + "-------- Empty 'resolutions' means no persistence for any resolution");
        String collectionPrefix = null; // default value
        String dbPrefix = null; // default value
        String dataModel = null; // default value
        String enableEncoding = null; // default value
        String resolutions = "";
        NGSISTHSink sink = new NGSISTHSink();
        sink.configure(createContext(collectionPrefix, dbPrefix, dataModel, enableEncoding, resolutions));
        
        try {
            boolean[] expected = {false, false, false, false, false};
            assertTrue(Arrays.equals(expected, sink.resolutions));
            System.out.println(getTestTraceHead("[NGSISTHSink.configure]")
                    + "-  OK  - Empty 'resolutions' detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISTHSink.configure]")
                    + "- FAIL - Empty 'resolutions' not detected");
            throw e;
        } // try catch
    } // testConfigureEmptyResolutions
    
    private Context createContext(String collectionPrefix, String dbPrefix, String dataModel, String enableEncoding,
            String resolutions) {
        Context context = NGSIUtilsForTests.createContextForMongoSTH(collectionPrefix, dbPrefix, dataModel,
                enableEncoding);
        context.put("resolutions", resolutions);
        return context;
    } // createContext
    
} // NGSISTHSinkTest
