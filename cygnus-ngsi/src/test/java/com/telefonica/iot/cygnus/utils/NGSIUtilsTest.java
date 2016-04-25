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
package com.telefonica.iot.cygnus.utils;

import static com.telefonica.iot.cygnus.utils.TestUtils.getTestTraceHead;
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
public class NGSIUtilsTest {
    
    /**
     * Constructor.
     */
    public NGSIUtilsTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSIUtilsTest

    /**
     * [NGSIUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when passing
     * a valid ISO 8601 timestamp without miliseconds.
     */
    @Test
    public void testGetTimeInstantISO8601TimestampWithoutMiliseconds() {
        System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "ISO 8601 timestamp without miliseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01T00:00:01Z");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = NGSIUtils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println(getTestTraceHead("[Utils.getTimeInstant]") + "-  OK  - Time instant obtained for '"
                    + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                    + "- FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantISO8601TimestampWithoutMiliseconds
    
    /**
     * [NGSIUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when passing
     * a valid ISO 8601 timestamp with miliseconds.
     */
    @Test
    public void testGetTimeInstantISO8601TimestampWithMiliseconds() {
        System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "ISO 8601 timestamp with miliseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01T00:00:01.123Z");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = NGSIUtils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println(getTestTraceHead("[Utils.getTimeInstant]") + "-  OK  - Time instant obtained for '"
                    + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                    + "- FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantISO8601TimestampWithMiliseconds
    
    /**
     * [NGSIUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when
     * passing a valid ISO 8601 timestamp with microseconds.
     */
    @Test
    public void testGetTimeInstantISO8601TimestampWithMicroseconds() {
        System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "ISO 8601 timestamp with microseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01T00:00:01.123456Z");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = NGSIUtils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println(getTestTraceHead("[Utils.getTimeInstant]") + "-  OK  - Time instant obtained for '"
                    + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                    + "- FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantISO8601TimestampWithMicroseconds
    
    /**
     * [NGSIUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when
     * passing a valid SQL timestamp without miliseconds.
     */
    @Test
    public void testGetTimeInstantSQLTimestampWithoutMiliseconds() {
        System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "SQL timestamp without miliseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01 00:00:01");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = NGSIUtils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println(getTestTraceHead("[Utils.getTimeInstant]") + "-  OK  - Time instant obtained for '"
                    + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                    + "- FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantSQLTimestampWithoutMiliseconds
    
    /**
     * [NGSIUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when
     * passing a valid SQL timestamp with miliseconds.
     */
    @Test
    public void testGetTimeInstantSQLTimestampWithMiliseconds() {
        System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "SQL timestamp with miliseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01 00:00:01.123");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = NGSIUtils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                    + "-  OK  - Time instant obtained for '" + metadatasJson.toJSONString() + "' is '"
                    + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                    + "- FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantSQLTimestampWithMiliseconds
    
    /**
     * [NGSIUtils.getTimeInstant] -------- When getting a time instant, it is properly obtained when
     * passing a valid SQL timestamp with microseconds.
     */
    @Test
    public void testGetTimeInstantSQLTimestampWithMicroseconds() {
        System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                + "-------- When getting a time instant, it is properly obtained when passing a valid "
                + "SQL timestamp with microseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01 00:00:01.123456");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = NGSIUtils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                    + "-  OK  - Time instant obtained for '" + metadatasJson.toJSONString() + "' is '"
                    + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[Utils.getTimeInstant]")
                    + "- FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantSQLTimestampWithMicroseconds
    
    /**
     * [NGSIUtils.getLocation] -------- When getting a location, a CartoDB point is obtained when passing
     * an attribute of type 'geo:point'.
     */
    @Test
    public void testGetLocationType() {
        System.out.println(getTestTraceHead("[Utils.getLocation]")
                + "-------- When getting a location, a CartoDB point is obtained when passing an attribute "
                + "of type 'geo:point'");
        String attrMetadataStr = "[]";
        String attrValue = "-3.7167, 40.3833";
        String attrType = "geo:point";
        boolean flipCoordinates = false; // irrelevant for this test
        String location = NGSIUtils.getLocation(attrValue, attrType, attrMetadataStr, flipCoordinates);

        try {
            assertEquals("ST_SetSRID(ST_MakePoint(-3.7167,40.3833), 4326)", location);
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "-  OK  - Location '" + location + "' obtained for an attribute of type '" + attrType
                    + "' and value '" + attrValue + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "- FAIL - Location '" + location + "' obtained for an attribute of type '" + attrType
                    + "' and value '" + attrValue + "'");
            throw e;
        } // try catch
    } // testGetLocationType
    
    /**
     * [NGSIUtils.getLocation] -------- When getting a location, a CartoDB point is obtained when passing
     * an attribute of type 'geo:point'.
     */
    @Test
    public void testGetLocationMetadata() {
        System.out.println(getTestTraceHead("[Utils.getLocation]")
                + "-------- When getting a location, a CartoDB point is obtained when passing an attribute "
                + "of type 'geo:point'");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "location");
        metadataJson.put("type", "string");
        metadataJson.put("value", "WGS84");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String attrMetadataStr = metadatasJson.toJSONString();
        String attrValue = "-3.7167, 40.3833";
        String attrType = "coordinates"; // irrelevant for this test
        boolean flipCoordinates = false; // irrelevant for this test
        String location = NGSIUtils.getLocation(attrValue, attrType, attrMetadataStr, flipCoordinates);

        try {
            assertEquals("ST_SetSRID(ST_MakePoint(-3.7167,40.3833), 4326)", location);
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "-  OK  - Location '" + location + "' obtained for an attribute with metadata '"
                    + attrMetadataStr + "' and value '" + attrValue + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "- FAIL - Location '" + location + "' obtained for an attribute with metadata '"
                    + attrMetadataStr + "' and value '" + attrValue + "'");
            throw e;
        } // try catch
    } // testGetLocationMetadata
    
    /**
     * [NGSIUtils.getLocation] -------- When getting a location, the original attribute is returned when the
     * attribute type is not geo:point and there is no WGS84 location metadata.
     */
    @Test
    public void testGetLocationNoGeolocation() {
        System.out.println(getTestTraceHead("[Utils.getLocation]")
                + "-------- When getting a location, the original attribute is returned when the attribute "
                + "type is not geo:point and there is no WGS84 location metadata");
        String attrMetadataStr = "[]";
        String attrValue = "-3.7167, 40.3833";
        String attrType = "coordinates";
        boolean flipCoordinates = false; // irrelevant for this test
        String location = NGSIUtils.getLocation(attrValue, attrType, attrMetadataStr, flipCoordinates);

        try {
            assertEquals(attrValue, location);
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "-  OK  - Location '" + location + "' obtained for a not geolocated attribute");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "- FAIL - Location '" + location + "' obtained for a not geolocated attribute");
            throw e;
        } // try catch
    } // testGetLocationNoGeolocation
    
} // NGSIUtilsTest
