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
package com.telefonica.iot.cygnus.utils;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import java.text.ParseException;
import java.util.logging.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class CommonUtilsTest {
    
    /**
     * Constructor.
     */
    public CommonUtilsTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // CommonUtilsTest

    /**
     * [CommonUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when passing
     * a valid ISO 8601 timestamp without miliseconds.
     */
    @Test
    public void testGetTimeInstantISO8601TimestampWithoutMiliseconds() {
        System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "ISO 8601 timestamp without miliseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01T00:00:01Z");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = CommonUtils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]") + "-  OK  - Time instant obtained for '"
                    + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                    + "- FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantISO8601TimestampWithoutMiliseconds
    
    /**
     * [CommonUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when passing
     * a valid ISO 8601 timestamp with miliseconds.
     */
    @Test
    public void testGetTimeInstantISO8601TimestampWithMiliseconds() {
        System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "ISO 8601 timestamp with miliseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01T00:00:01.123Z");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = CommonUtils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]") + "-  OK  - Time instant obtained for '"
                    + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                    + "- FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantISO8601TimestampWithMiliseconds
    
    /**
     * [CommonUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when
     * passing a valid ISO 8601 timestamp with microseconds.
     */
    @Test
    public void testGetTimeInstantISO8601TimestampWithMicroseconds() {
        System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "ISO 8601 timestamp with microseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01T00:00:01.123456Z");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = CommonUtils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]") + "-  OK  - Time instant obtained for '"
                    + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                    + "- FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantISO8601TimestampWithMicroseconds

    /**
     * [CommonUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when
     * passing a valid SQL timestamp without miliseconds.
     */
    @Test
    public void testGetTimeInstantSQLTimestampWithoutMiliseconds() {
        System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "SQL timestamp without miliseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01 00:00:01");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = CommonUtils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]") + "-  OK  - Time instant obtained for '"
                    + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                    + "- FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantSQLTimestampWithoutMiliseconds
    
    /**
     * [CommonUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when
     * passing a valid SQL timestamp with miliseconds.
     */
    @Test
    public void testGetTimeInstantSQLTimestampWithMiliseconds() {
        System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "SQL timestamp with miliseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01 00:00:01.123");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = CommonUtils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                    + "-  OK  - Time instant obtained for '" + metadatasJson.toJSONString() + "' is '"
                    + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                    + "- FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantSQLTimestampWithMiliseconds
    
    /**
     * [CommonUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when
     * passing a valid SQL timestamp with microseconds.
     */
    @Test
    public void testGetTimeInstantSQLTimestampWithMicroseconds() {
        System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "SQL timestamp with microseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01 00:00:01.123456");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = CommonUtils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                    + "-  OK  - Time instant obtained for '" + metadatasJson.toJSONString() + "' is '"
                    + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                    + "- FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantSQLTimestampWithMicroseconds

    /**
     * [CommonUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when passing
     * a valid ISO 8601 timestamp with offset without miliseconds.
     */
    @Test
    public void testGetTimeInstantISO8601TimestampWithOffsetWithoutMiliseconds() {
        System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "ISO 8601 timestamp with offset without miliseconds");
        String offsets[] = {"+09:00", "+0900", "+09", "-09:00", "-0900", "-09"};
        for (String offset : offsets) {
            JSONObject metadataJson = new JSONObject();
            metadataJson.put("name", "TimeInstant");
            metadataJson.put("type", "ISO8601");
            metadataJson.put("value", "2017-01-01T00:00:01" + offset);
            JSONArray metadatasJson = new JSONArray();
            metadatasJson.add(metadataJson);
            String metadatasStr = metadatasJson.toJSONString();
            Long timeInstant = CommonUtils.getTimeInstant(metadatasStr);

            try {
                assertTrue(timeInstant != null);
                System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]") + "-  OK  - Time instant obtained for '"
                        + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                        + "- FAIL - Time instant obtained is 'null'");
                throw e;
            } // try catch
        } // for
    } // testGetTimeInstantISO8601TimestampWithOffsetWithoutMiliseconds

    /**
     * [CommonUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when passing
     * a valid ISO 8601 timestamp with offset with miliseconds.
     */
    @Test
    public void testGetTimeInstantISO8601TimestampWithOffsetWithMiliseconds() {
        System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "ISO 8601 timestamp with offset and miliseconds");
        String offsets[] = {"+11:00", "+1100", "+11", "-11:00", "-1100", "-11"};
        for (String offset : offsets) {
            JSONObject metadataJson = new JSONObject();
            metadataJson.put("name", "TimeInstant");
            metadataJson.put("type", "ISO8601");
            metadataJson.put("value", "2017-01-01T00:00:01.123456" + offset);
            JSONArray metadatasJson = new JSONArray();
            metadatasJson.add(metadataJson);
            String metadatasStr = metadatasJson.toJSONString();
            Long timeInstant = CommonUtils.getTimeInstant(metadatasStr);

            try {
                assertTrue(timeInstant != null);
                System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]") + "-  OK  - Time instant obtained for '"
                        + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[CommonUtils.getTimeInstant]")
                        + "- FAIL - Time instant obtained is 'null'");
                throw e;
            } // try catch
        }
    } // testGetTimeInstantISO8601TimestampWithOffsetWithMiliseconds

    
    /**
     * [CommonUtils.getMilliseconds] -------- Milliseconds are obtained when passing a valid timestamp.
     */
    @Test
    public void testGetMilliseconds() {
        System.out.println(getTestTraceHead("[CommonUtils.getMilliseconds]")
                + "-------- Milliseconds are obtained when passing a valid timestamp");
        String timestamp = "2017-01-10T17:08:00.0Z";
        Long milliseconds;
        
        try {
            milliseconds = CommonUtils.getMilliseconds(timestamp);
        } catch (ParseException e) {
            System.out.println(getTestTraceHead("[CommonUtils.getMilliseconds]")
                    + "- FAIL - There was some problem while getting the milliseconds");
            throw new AssertionError(e.getMessage());
        } // try catch

        try {
            assertEquals(timestamp, CommonUtils.getHumanReadable(milliseconds, true));
            System.out.println(getTestTraceHead("[CommonUtils.getMilliseconds]")
                    + "-  OK  - Milliseconds obtained");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CommonUtils.getMilliseconds]")
                    + "- FAIL - Milliseconds were not obtained");
            throw e;
        } // try catch
    } // testGetMilliseconds

} // NGSIUtilsTest
