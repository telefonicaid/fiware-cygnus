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

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NGSIUtilsTest {
    
    /**
     * Constructor.
     */
    public NGSIUtilsTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSIUtilsTest
    
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
