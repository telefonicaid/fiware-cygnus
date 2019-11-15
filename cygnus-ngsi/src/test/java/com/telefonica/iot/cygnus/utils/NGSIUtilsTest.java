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
import org.apache.commons.lang3.tuple.ImmutablePair;
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
     * [NGSIUtils.getGeometry] -------- When getting a geometry, a CartoDB point is obtained when passing
     * an attribute of type 'geo:point'.
     */
    @Test
    public void testGetGeometryGeopoint() {
        System.out.println(getTestTraceHead("[Utils.getLocation]")
                + "-------- When getting a geometry, a CartoDB point is obtained when passing an attribute "
                + "of type 'geo:point'");
        String attrMetadataStr = "[]";
        String attrValue = "-3.7167, 40.3833";
        String attrType = "geo:point";
        boolean swapCoordinates = false; // irrelevant for this test
        ImmutablePair<String, Boolean> geometry = NGSIUtils.getGeometry(
                attrValue, attrType, attrMetadataStr, swapCoordinates);

        try {
            assertEquals("ST_SetSRID(ST_MakePoint(-3.7167::double precision , 40.3833::double precision ), 4326)", geometry.getLeft());
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "-  OK  - Geometry '" + geometry.getLeft() + "' obtained for an attribute of type '" + attrType
                    + "' and value '" + attrValue + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "- FAIL - Geometry '" + geometry.getLeft() + "' obtained for an attribute of type '" + attrType
                    + "' and value '" + attrValue + "'");
            throw e;
        } // try catch // try catch
    } // testGetGeometryGeopoint
    
    /**
     * [NGSIUtils.getGeometry] -------- When getting a geometry, a CartoDB point is obtained when passing
     * an attribute with 'geometry' metadata.
     */
    @Test
    public void testGetGeometryMetadata() {
        System.out.println(getTestTraceHead("[Utils.getLocation]")
                + "-------- When getting a geometry, a CartoDB point is obtained when passing an attribute "
                + "with 'location' metadata");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "location");
        metadataJson.put("type", "string");
        metadataJson.put("value", "WGS84");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String attrMetadataStr = metadatasJson.toJSONString();
        String attrValue = "-3.7167, 40.3833";
        String attrType = "coordinates"; // irrelevant for this test
        boolean swapCoordinates = false; // irrelevant for this test
        ImmutablePair<String, Boolean> geometry = NGSIUtils.getGeometry(
                attrValue, attrType, attrMetadataStr, swapCoordinates);

        try {
            assertEquals("ST_SetSRID(ST_MakePoint(-3.7167::double precision , 40.3833::double precision ), 4326)", geometry.getLeft());
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "-  OK  - Geometry '" + geometry.getLeft() + "' obtained for an attribute with metadata '"
                    + attrMetadataStr + "' and value '" + attrValue + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "- FAIL - Geometry '" + geometry.getLeft() + "' obtained for an attribute with metadata '"
                    + attrMetadataStr + "' and value '" + attrValue + "'");
            throw e;
        } // try catch // try catch
    } // testGetGeometryMetadata
    
    /**
     * [NGSIUtils.getGeometry] -------- When getting a geometry, the original attribute is returned when the
     * attribute type is not geo:point and there is no WGS84 geometry metadata.
     */
    @Test
    public void testGetGeometryNoGeolocation() {
        System.out.println(getTestTraceHead("[Utils.getLocation]")
                + "-------- When getting a geometry, the original attribute is returned when the attribute "
                + "type is not geo:point and there is no WGS84 location metadata");
        String attrMetadataStr = "[]";
        String attrValue = "-3.7167, 40.3833";
        String attrType = "coordinates";
        boolean swapCoordinates = false; // irrelevant for this test
        ImmutablePair<String, Boolean> geometry = NGSIUtils.getGeometry(
                attrValue, attrType, attrMetadataStr, swapCoordinates);

        try {
            assertEquals(attrValue, geometry.getLeft());
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "-  OK  - Geometry '" + geometry.getLeft() + "' obtained for a not geolocated attribute");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "- FAIL - Geometry '" + geometry.getLeft() + "' obtained for a not geolocated attribute");
            throw e;
        } // try catch // try catch
    } // testGetGeometryNoGeolocation
    
    /**
     * [NGSIUtils.getGeometry] -------- When getting a geometry, a CartoDB geometry is obtained when passing
     * an attribute of type 'geo:json'.
     */
    @Test
    public void testGetGeometryGeojson() {
        System.out.println(getTestTraceHead("[Utils.getLocation]")
                + "-------- When getting a geometry, a CartoDB geometry is obtained when passing an attribute "
                + "of type 'geo:json'");
        String attrMetadataStr = "[]";
        String attrValue = "{\"coordinates\": [-3.7167, 40.3833], \"type\": \"Point\"}";
        String attrType = "geo:json";
        boolean swapCoordinates = false; // irrelevant for this test
        ImmutablePair<String, Boolean> geometry = NGSIUtils.getGeometry(
                attrValue, attrType, attrMetadataStr, swapCoordinates);

        try {
            assertEquals("ST_GeomFromGeoJSON('{\"coordinates\": [-3.7167, 40.3833], \"type\": \"Point\"}')", geometry.getLeft());
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "-  OK  - Geometry '" + geometry.getLeft() + "' obtained for an attribute of type '" + attrType
                    + "' and value '" + attrValue + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[Utils.getLocation]")
                    + "- FAIL - Geometry '" + geometry.getLeft() + "' obtained for an attribute of type '" + attrType
                    + "' and value '" + attrValue + "'");
            throw e;
        } // try catch // try catch
    } // testGetGeometryGeojson

} // NGSIUtilsTest
