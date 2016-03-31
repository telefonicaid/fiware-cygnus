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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {

    /**
     * [Utils.getTimeInstant] -------- A time instant is obtained when passing a valid ISO 8601 timestamp without
     * miliseconds.
     */
    @Test
    public void testGetTimeInstantISO8601TimestampWithoutMiliseconds() {
        System.out.println("[Utils.getTimeInstant] -------- A time instant is obtained when passing a valid ISO 8601 "
                + "timestamp without miliseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01T00:00:01Z");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = Utils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println("[Utils.getTimeInstant] -  OK  - Time instant obtained for '"
                    + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println("[Utils.getTimeInstant] - FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantISO8601TimestampWithoutMiliseconds
    
    /**
     * [Utils.getTimeInstant] -------- A time instant is obtained when passing a valid ISO 8601 timestamp with
     * miliseconds.
     */
    @Test
    public void testGetTimeInstantISO8601TimestampWithMiliseconds() {
        System.out.println("[Utils.getTimeInstant] -------- A time instant is obtained when passing a valid ISO 8601 "
                + "timestamp with miliseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01T00:00:01.123Z");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = Utils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println("[Utils.getTimeInstant] -  OK  - Time instant obtained for '"
                    + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println("[Utils.getTimeInstant] - FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantISO8601TimestampWithMiliseconds
    
    /**
     * [Utils.getTimeInstant] -------- A time instant is obtained when passing a valid ISO 8601 timestamp with
     * microseconds.
     */
    @Test
    public void testGetTimeInstantISO8601TimestampWithMicroseconds() {
        System.out.println("[Utils.getTimeInstant] -------- A time instant is obtained when passing a valid ISO 8601 "
                + "timestamp with microseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01T00:00:01.123456Z");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = Utils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println("[Utils.getTimeInstant] -  OK  - Time instant obtained for '"
                    + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println("[Utils.getTimeInstant] - FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantISO8601TimestampWithMicroseconds
    
    /**
     * [Utils.getTimeInstant] -------- A time instant is obtained when passing a valid SQL timestamp without
     * miliseconds.
     */
    @Test
    public void testGetTimeInstantSQLTimestampWithoutMiliseconds() {
        System.out.println("[Utils.getTimeInstant] -------- A time instant is obtained when passing a valid SQL "
                + "timestamp without miliseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01 00:00:01");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = Utils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println("[Utils.getTimeInstant] -  OK  - Time instant obtained for '"
                    + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println("[Utils.getTimeInstant] - FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantSQLTimestampWithoutMiliseconds
    
    /**
     * [Utils.getTimeInstant] -------- A time instant is obtained when passing a valid SQL timestamp with miliseconds.
     */
    @Test
    public void testGetTimeInstantSQLTimestampWithMiliseconds() {
        System.out.println("[Utils.getTimeInstant] -------- A time instant is obtained when passing a valid SQL "
                + "timestamp with miliseconds");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", "TimeInstant");
        metadataJson.put("type", "SQL timestamp");
        metadataJson.put("value", "2017-01-01 00:00:01.123");
        JSONArray metadatasJson = new JSONArray();
        metadatasJson.add(metadataJson);
        String metadatasStr = metadatasJson.toJSONString();
        Long timeInstant = Utils.getTimeInstant(metadatasStr);

        try {
            assertTrue(timeInstant != null);
            System.out.println("[Utils.getTimeInstant] -  OK  - Time instant obtained for '"
                    + metadatasJson.toJSONString() + "' is '" + timeInstant + "'");
        } catch (AssertionError e) {
            System.out.println("[Utils.getTimeInstant] - FAIL - Time instant obtained is 'null'");
            throw e;
        } // try catch
    } // testGetTimeInstantSQLTimestampWithMiliseconds
    
} // UtilsTest
