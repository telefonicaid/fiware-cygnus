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
package com.telefonica.iot.cygnus.backends.mongo;

import com.telefonica.iot.cygnus.sinks.Enums.DataModel;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class MongoBackendImplTest {
    
    /**
     * Constructor.
     */
    public MongoBackendImplTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // MongoBackendImplTest
    
    /**
     * [MongoBackendImpl.getRange] -------- Given a resolution, its related range is correctly returned.
     */
    @Test
    public void testGetRange() {
        System.out.println(getTestTraceHead("[MongoBackendImpl.getRange]")
                + "-------- Given a resolution, its related range is correctly returned");
        MongoBackendImpl backend = new MongoBackendImpl(null, null, null, null);
        
        
        try {
            assertEquals("minute", backend.getRange(MongoBackendImpl.Resolution.SECOND));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getRange]")
                    + "-  OK  - The related range for 'SECOND' resolution is 'minute'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getRange]")
                    + "- FAIL - The related range for 'SECOND' resolution is not 'minute'");
            throw e;
        } // try catch
        
        try {
            assertEquals("hour", backend.getRange(MongoBackendImpl.Resolution.MINUTE));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getRange]")
                    + "-  OK  - The related range for 'MINUTE' resolution is 'hour'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getRange]")
                    + "- FAIL - The related range for 'MINUTE' resolution is not 'hour'");
            throw e;
        } // try catch
        
        try {
            assertEquals("day", backend.getRange(MongoBackendImpl.Resolution.HOUR));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getRange]")
                    + "-  OK  - The related range for 'HOUR' resolution is 'day'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getRange]")
                    + "- FAIL - The related range for 'HOUR' resolution is not 'day'");
            throw e;
        } // try catch
        
        try {
            assertEquals("month", backend.getRange(MongoBackendImpl.Resolution.DAY));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getRange]")
                    + "-  OK  - The related range for 'DAY' resolution is 'month'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getRange]")
                    + "- FAIL - The related range for 'DAY' resolution is not 'month'");
            throw e;
        } // try catch
        
        try {
            assertEquals("year", backend.getRange(MongoBackendImpl.Resolution.MONTH));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getRange]")
                    + "-  OK  - The related range for 'MONTH' resolution is 'year'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getRange]")
                    + "- FAIL - The related range for 'MONTH' resolution is not 'year'");
            throw e;
        } // try catch
    } // testGetRange
    
    /**
     * [MongoBackendImpl.getOrigin] -------- Given a calendar and a resolution, its related origin is correctly
     * returned.
     */
    @Test
    public void testGetOrigin() {
        System.out.println(getTestTraceHead("[MongoBackendImpl.getOrigin]")
                + "-------- Given a calendar and a resolution, its related origin is correctly returned");
        MongoBackendImpl backend = new MongoBackendImpl(null, null, null, null);
        GregorianCalendar calendar = new GregorianCalendar(2017, 4, 5, 11, 46, 13);
        
        try {
            GregorianCalendar gc = new GregorianCalendar(2017, 4, 5, 11, 46, 0);
            gc.setTimeZone(TimeZone.getTimeZone("UTC"));
            assertEquals(gc.getTime(), backend.getOrigin(calendar, MongoBackendImpl.Resolution.SECOND));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOrigin]")
                    + "-  OK  - The related origin for '2017-04-05T11:46:13' and 'SECOND' resolution is "
                    + "'2017-04-05T11:46:00'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOrigin]")
                    + "- FAIL - The related origin for '2017-04-05T11:46:13' and 'SECOND' resolution is not "
                    + "'2017-04-05T11:46:00'");
            throw e;
        } // try catch
        
        try {
            GregorianCalendar gc = new GregorianCalendar(2017, 4, 5, 11, 0, 0);
            gc.setTimeZone(TimeZone.getTimeZone("UTC"));
            assertEquals(gc.getTime(), backend.getOrigin(calendar, MongoBackendImpl.Resolution.MINUTE));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOrigin]")
                    + "-  OK  - The related origin for '2017-04-05T11:46:13' and 'MINUTE' resolution is "
                    + "'2017-04-05T11:00:00'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getRange]")
                    + "- FAIL - The related origin for '2017-04-05T11:46:13' and 'MINUTE' resolution is not "
                    + "'2017-04-05T11:00:00'");
            throw e;
        } // try catch
        
        try {
            GregorianCalendar gc = new GregorianCalendar(2017, 4, 5, 0, 0, 0);
            gc.setTimeZone(TimeZone.getTimeZone("UTC"));
            assertEquals(gc.getTime(), backend.getOrigin(calendar, MongoBackendImpl.Resolution.HOUR));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOrigin]")
                    + "-  OK  - The related origin for '2017-04-05T11:46:13' and 'HOUR' resolution is "
                    + "'2017-04-05T00:00:00'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOrigin]")
                    + "- FAIL - The related origin for '2017-04-05T11:46:13' and 'HOUR' resolution is not "
                    + "'2017-04-05T00:00:00'");
            throw e;
        } // try catch
        
        try {
            GregorianCalendar gc = new GregorianCalendar(2017, 4, 1, 0, 0, 0);
            gc.setTimeZone(TimeZone.getTimeZone("UTC"));
            assertEquals(gc.getTime(), backend.getOrigin(calendar, MongoBackendImpl.Resolution.DAY));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOrigin]")
                    + "-  OK  - The related origin for '2017-04-05T11:46:13' and 'DAY' resolution is "
                    + "'2017-04-01T00:00:00'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOrigin]")
                    + "- FAIL - The related origin for '2017-04-05T11:46:13' and 'DAY' resolution is not "
                    + "'2017-04-01T00:00:00'");
            throw e;
        } // try catch
        
        try {
            // The month has to be zero since GregorianCalendar builds dates considering months start by zero
            GregorianCalendar gc = new GregorianCalendar(2017, 0, 1, 0, 0, 0);
            gc.setTimeZone(TimeZone.getTimeZone("UTC"));
            assertEquals(gc.getTime(), backend.getOrigin(calendar, MongoBackendImpl.Resolution.MONTH));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOrigin]")
                    + "-  OK  - The related origin for '2017-04-05T11:46:13' and 'MONTH' resolution is "
                    + "'2017-01-01T00:00:00'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOrigin]")
                    + "- FAIL - The related origin for '2017-04-05T11:46:13' and 'MONTH' resolution is not "
                    + "'2017-01-01T00:00:00'");
            throw e;
        } // try catch
    } // testGetOrigin
    
    /**
     * [MongoBackendImpl.getOffset] -------- Given a calendar and a resolution, its related offset is correctly
     * returned.
     */
    @Test
    public void testGetOffset() {
        System.out.println(getTestTraceHead("[MongoBackendImpl.getOffset]")
                + "-------- Given a calendar and a resolution, its related offset is correctly returned");
        MongoBackendImpl backend = new MongoBackendImpl(null, null, null, null);
        GregorianCalendar calendar = new GregorianCalendar(2017, 3, 5, 11, 46, 13); // month 3 is April
        
        try {
            assertEquals(13, backend.getOffset(calendar, MongoBackendImpl.Resolution.SECOND));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOffset]")
                    + "-  OK  - The related offset for '2017-04-05T11:46:13' and 'SECOND' resolution is '13'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOffset]")
                    + "- FAIL - The related offset for '2017-04-05T11:46:13' and 'SECOND' resolution is '13'");
            throw e;
        } // try catch
        
        try {
            assertEquals(46, backend.getOffset(calendar, MongoBackendImpl.Resolution.MINUTE));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOffset]")
                    + "-  OK  - The related offset for '2017-04-05T11:46:13' and 'MINUTE' resolution is '46'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOffset]")
                    + "- FAIL - The related offset for '2017-04-05T11:46:13' and 'MINUTE' resolution is '46'");
            throw e;
        } // try catch
        
        try {
            assertEquals(11, backend.getOffset(calendar, MongoBackendImpl.Resolution.HOUR));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOffset]")
                    + "-  OK  - The related offset for '2017-04-05T11:46:13' and 'HOUR' resolution is '11'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOffset]")
                    + "- FAIL - The related offset for '2017-04-05T11:46:13' and 'HOUR' resolution is '11'");
            throw e;
        } // try catch
        
        try {
            assertEquals(5, backend.getOffset(calendar, MongoBackendImpl.Resolution.DAY));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOffset]")
                    + "-  OK  - The related offset for '2017-04-05T11:46:13' and 'DAY' resolution is '5'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOffset]")
                    + "- FAIL - The related offset for '2017-04-05T11:46:13' and 'DAY' resolution is '5'");
            throw e;
        } // try catch
        
        try {
            assertEquals(4, backend.getOffset(calendar, MongoBackendImpl.Resolution.MONTH));
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOffset]")
                    + "-  OK  - The related offset for '2017-04-05T11:46:13' and 'MONTH' resolution is '4'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.getOffset]")
                    + "- FAIL - The related offset for '2017-04-05T11:46:13' and 'MONTH' resolution is '4'");
            throw e;
        } // try catch
    } // testGetOffset
    
    /**
     * [MongoBackendImpl.buildQueryForInsertAggregated] -------- Given a calendar, a resolution, an entity ID and type
     * and a attribute name, a query for insert if built
     * returned.
     */
    @Test
    public void testBuildQueryForInsertAggregated() {
        System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                + "-------- Given a calendar, a resolution, an entity ID and type and a attribute name, a query "
                + "for insert if built");
        String entityId = "someId";
        String entityType = "someType";
        String attrName = "someName";
        GregorianCalendar calendar = new GregorianCalendar(2017, 3, 5, 11, 46, 13); // month 3 is April
        MongoBackendImpl backend = new MongoBackendImpl(null, null, null, DataModel.DMBYSERVICEPATH);
        String queryForInsertAggregated = "{ \"_id\" : { \"entityId\" : \"someId\" , \"entityType\" : \"someType\" , "
                + "\"attrName\" : \"someName\" , \"origin\" : { \"$date\" : \"2017-04-05T11:46:00.000Z\"} , "
                + "\"resolution\" : \"second\" , \"range\" : \"minute\"} , \"points.offset\" : 13}";

        try {
            assertEquals(queryForInsertAggregated, backend.buildQueryForInsertAggregated(calendar, entityId,
                    entityType, attrName, MongoBackendImpl.Resolution.SECOND).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'SECOND' resolution and 'DMBYENTITY' "
                    + "data mode is '" + queryForInsertAggregated + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'SECOND' resolution and 'DMBYENTITY' "
                    + "data mode is not '" + queryForInsertAggregated + "'");
            throw e;
        } // try catch
        
        queryForInsertAggregated = "{ \"_id\" : { \"entityId\" : \"someId\" , \"entityType\" : \"someType\" , "
                + "\"attrName\" : \"someName\" , \"origin\" : { \"$date\" : \"2017-04-05T11:00:00.000Z\"} , "
                + "\"resolution\" : \"minute\" , \"range\" : \"hour\"} , \"points.offset\" : 46}";
        
        try {
            assertEquals(queryForInsertAggregated, backend.buildQueryForInsertAggregated(calendar, entityId,
                    entityType, attrName, MongoBackendImpl.Resolution.MINUTE).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'MINUTE' resolution and 'DMBYENTITY' "
                    + "data mode is '" + queryForInsertAggregated + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'MINUTE' resolution and 'DMBYENTITY' "
                    + "data mode is not '" + queryForInsertAggregated + "'");
            throw e;
        } // try catch
        
        queryForInsertAggregated = "{ \"_id\" : { \"entityId\" : \"someId\" , \"entityType\" : \"someType\" , "
                + "\"attrName\" : \"someName\" , \"origin\" : { \"$date\" : \"2017-04-05T00:00:00.000Z\"} , "
                + "\"resolution\" : \"hour\" , \"range\" : \"day\"} , \"points.offset\" : 11}";
        
        try {
            assertEquals(queryForInsertAggregated, backend.buildQueryForInsertAggregated(calendar, entityId,
                    entityType, attrName, MongoBackendImpl.Resolution.HOUR).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'HOUR' resolution and 'DMBYENTITY' "
                    + "data mode is '" + queryForInsertAggregated + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'HOUR' resolution and 'DMBYENTITY' "
                    + "data mode is not '" + queryForInsertAggregated + "'");
            throw e;
        } // try catch
        
        queryForInsertAggregated = "{ \"_id\" : { \"entityId\" : \"someId\" , \"entityType\" : \"someType\" , "
                + "\"attrName\" : \"someName\" , \"origin\" : { \"$date\" : \"2017-04-01T00:00:00.000Z\"} , "
                + "\"resolution\" : \"day\" , \"range\" : \"month\"} , \"points.offset\" : 5}";
        
        try {
            assertEquals(queryForInsertAggregated, backend.buildQueryForInsertAggregated(calendar, entityId,
                    entityType, attrName, MongoBackendImpl.Resolution.DAY).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'DAY' resolution and 'DMBYENTITY' "
                    + "data mode is '" + queryForInsertAggregated + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'DAY' resolution and 'DMBYENTITY' "
                    + "data mode is not '" + queryForInsertAggregated + "'");
            throw e;
        } // try catch
        
        queryForInsertAggregated = "{ \"_id\" : { \"entityId\" : \"someId\" , \"entityType\" : \"someType\" , "
                + "\"attrName\" : \"someName\" , \"origin\" : { \"$date\" : \"2017-01-01T00:00:00.000Z\"} , "
                + "\"resolution\" : \"month\" , \"range\" : \"year\"} , \"points.offset\" : 4}";
        
        try {
            assertEquals(queryForInsertAggregated, backend.buildQueryForInsertAggregated(calendar, entityId,
                    entityType, attrName, MongoBackendImpl.Resolution.MONTH).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'MONTH' resolution and 'DMBYENTITY' "
                    + "data mode is '" + queryForInsertAggregated + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'MONTH' resolution and 'DMBYENTITY' "
                    + "data mode is not '" + queryForInsertAggregated + "'");
            throw e;
        } // try catch
        
        backend = new MongoBackendImpl(null, null, null, DataModel.DMBYENTITY);
        queryForInsertAggregated = "{ \"_id\" : { \"attrName\" : \"someName\" , "
                + "\"origin\" : { \"$date\" : \"2017-04-05T11:46:00.000Z\"} , \"resolution\" : \"second\" , "
                + "\"range\" : \"minute\"} , \"points.offset\" : 13}";

        try {
            assertEquals(queryForInsertAggregated, backend.buildQueryForInsertAggregated(calendar, entityId,
                    entityType, attrName, MongoBackendImpl.Resolution.SECOND).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'SECOND' resolution and 'DMBYENTITY' "
                    + "data mode is '" + queryForInsertAggregated + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'SECOND' resolution and 'DMBYENTITY' "
                    + "data mode is not '" + queryForInsertAggregated + "'");
            throw e;
        } // try catch
        
        queryForInsertAggregated = "{ \"_id\" : { \"attrName\" : \"someName\" , "
                + "\"origin\" : { \"$date\" : \"2017-04-05T11:00:00.000Z\"} , \"resolution\" : \"minute\" , "
                + "\"range\" : \"hour\"} , \"points.offset\" : 46}";
        
        try {
            assertEquals(queryForInsertAggregated, backend.buildQueryForInsertAggregated(calendar, entityId,
                    entityType, attrName, MongoBackendImpl.Resolution.MINUTE).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'MINUTE' resolution and 'DMBYENTITY' "
                    + "data mode is '" + queryForInsertAggregated + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'MINUTE' resolution and 'DMBYENTITY' "
                    + "data mode is not '" + queryForInsertAggregated + "'");
            throw e;
        } // try catch
        
        queryForInsertAggregated = "{ \"_id\" : { \"attrName\" : \"someName\" , "
                + "\"origin\" : { \"$date\" : \"2017-04-05T00:00:00.000Z\"} , \"resolution\" : \"hour\" , "
                + "\"range\" : \"day\"} , \"points.offset\" : 11}";
        
        try {
            assertEquals(queryForInsertAggregated, backend.buildQueryForInsertAggregated(calendar, entityId,
                    entityType, attrName, MongoBackendImpl.Resolution.HOUR).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'HOUR' resolution and 'DMBYENTITY' "
                    + "data mode is '" + queryForInsertAggregated + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'HOUR' resolution and 'DMBYENTITY' "
                    + "data mode is not '" + queryForInsertAggregated + "'");
            throw e;
        } // try catch
        
        queryForInsertAggregated = "{ \"_id\" : { \"attrName\" : \"someName\" , "
                + "\"origin\" : { \"$date\" : \"2017-04-01T00:00:00.000Z\"} , \"resolution\" : \"day\" , "
                + "\"range\" : \"month\"} , \"points.offset\" : 5}";
        
        try {
            assertEquals(queryForInsertAggregated, backend.buildQueryForInsertAggregated(calendar, entityId,
                    entityType, attrName, MongoBackendImpl.Resolution.DAY).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'DAY' resolution and 'DMBYENTITY' "
                    + "data mode is '" + queryForInsertAggregated + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'DAY' resolution and 'DMBYENTITY' "
                    + "data mode is not '" + queryForInsertAggregated + "'");
            throw e;
        } // try catch
        
        queryForInsertAggregated = "{ \"_id\" : { \"attrName\" : \"someName\" , "
                + "\"origin\" : { \"$date\" : \"2017-01-01T00:00:00.000Z\"} , \"resolution\" : \"month\" , "
                + "\"range\" : \"year\"} , \"points.offset\" : 4}";
        
        try {
            assertEquals(queryForInsertAggregated, backend.buildQueryForInsertAggregated(calendar, entityId,
                    entityType, attrName, MongoBackendImpl.Resolution.MONTH).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'MONTH' resolution and 'DMBYENTITY' "
                    + "data mode is '" + queryForInsertAggregated + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildQueryForInsertAggregated]")
                    + "-  OK  - The related query for '2017-04-05T11:46:13', 'MONTH' resolution and 'DMBYENTITY' "
                    + "data mode is not '" + queryForInsertAggregated + "'");
            throw e;
        } // try catch
    } // testBuildQueryForInsertAggregated
    
    /**
     * [MongoBackendImpl.buildUpdateForUpdate] -------- Given a calendar, an attribute type and pre-aggregated numerical
     * values, an aggregation update is created.
     */
    @Test
    public void testBuildUpdateForUpdateNumerical() {
        System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                + "-------- Given a calendar, an attribute type and pre-aggregated numerical values, an aggregation "
                + "update is create");
        String attrType= "someType";
        double max = 10;
        double min = 0;
        double sum = 20;
        double sum2 = 200;
        int numSamples = 2;
        GregorianCalendar calendar = new GregorianCalendar(2017, 3, 5, 11, 46, 13); // month 3 is April
        MongoBackendImpl backend = new MongoBackendImpl(null, null, null, null);
        String updateForUpdate = "{ \"$set\" : { \"attrType\" : \"someType\"} , "
                + "\"$inc\" : { \"points.$.samples\" : 2 , \"points.$.sum\" : 20.0 , \"points.$.sum2\" : 200.0} , "
                + "\"$min\" : { \"points.$.min\" : 0.0} , \"$max\" : { \"points.$.max\" : 10.0}}";

        try {
            assertEquals(updateForUpdate, backend.buildUpdateForUpdate(attrType, calendar, max, min, sum, sum2,
                    numSamples).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                    + "-  OK  - The related update for '2017-04-05T11:46:13' and given preaggregation is '"
                    + updateForUpdate + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                    + "-  OK  - The related update for '2017-04-05T11:46:13' and given preaggregation is not '"
                    + updateForUpdate + "'");
            throw e;
        } // try catch
    } // testBuildUpdateForUpdateNumerical
    
    /**
     * [MongoBackendImpl.buildUpdateForUpdate] -------- Given a calendar, an attribute type and pre-aggregated string
     * values, an aggregation update is created.
     */
    @Test
    public void testBuildUpdateForUpdateString() {
        System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                + "-------- Given a calendar, an attribute type and pre-aggregated string values, an aggregation "
                + "update is create");
        String attrType= "someType";
        String value = "someString";
        int count = 2;
        GregorianCalendar calendar = new GregorianCalendar(2017, 3, 5, 11, 46, 13); // month 3 is April
        MongoBackendImpl backend = new MongoBackendImpl(null, null, null, null);
        String updateForUpdate = "{ \"$set\" : { \"attrType\" : \"someType\"} , "
                + "\"$inc\" : { \"points.13.samples\" : 2 , \"points.13.occur.someString\" : 2}}";

        try {
            assertEquals(updateForUpdate, backend.buildUpdateForUpdate(attrType, MongoBackendImpl.Resolution.SECOND,
                    calendar, value, count).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                    + "-  OK  - The related update for '2017-04-05T11:46:13', resolution 'SECOND' and given "
                    + "preaggregation is '" + updateForUpdate + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                    + "-  OK  - The related update for '2017-04-05T11:46:13', resolution 'SECOND' and given "
                    + "preaggregation is not '" + updateForUpdate + "'");
            throw e;
        } // try catch
        
        updateForUpdate = "{ \"$set\" : { \"attrType\" : \"someType\"} , "
                + "\"$inc\" : { \"points.46.samples\" : 2 , \"points.46.occur.someString\" : 2}}";

        try {
            assertEquals(updateForUpdate, backend.buildUpdateForUpdate(attrType, MongoBackendImpl.Resolution.MINUTE,
                    calendar, value, count).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                    + "-  OK  - The related update for '2017-04-05T11:46:13', resolution 'MINUTE' and given "
                    + "preaggregation is '" + updateForUpdate + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                    + "-  OK  - The related update for '2017-04-05T11:46:13', resolution 'MINUTE' and given "
                    + "preaggregation is not '" + updateForUpdate + "'");
            throw e;
        } // try catch
        
        updateForUpdate = "{ \"$set\" : { \"attrType\" : \"someType\"} , "
                + "\"$inc\" : { \"points.11.samples\" : 2 , \"points.11.occur.someString\" : 2}}";

        try {
            assertEquals(updateForUpdate, backend.buildUpdateForUpdate(attrType, MongoBackendImpl.Resolution.HOUR,
                    calendar, value, count).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                    + "-  OK  - The related update for '2017-04-05T11:46:13', resolution 'HOUR' and given "
                    + "preaggregation is '" + updateForUpdate + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                    + "-  OK  - The related update for '2017-04-05T11:46:13', resolution 'HOUR' and given "
                    + "preaggregation is not '" + updateForUpdate + "'");
            throw e;
        } // try catch
        
        // Offsets for days start by 0, this the fifth day is 4
        updateForUpdate = "{ \"$set\" : { \"attrType\" : \"someType\"} , "
                + "\"$inc\" : { \"points.4.samples\" : 2 , \"points.4.occur.someString\" : 2}}";

        try {
            assertEquals(updateForUpdate, backend.buildUpdateForUpdate(attrType, MongoBackendImpl.Resolution.DAY,
                    calendar, value, count).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                    + "-  OK  - The related update for '2017-04-05T11:46:13', resolution 'DAY' and given "
                    + "preaggregation is '" + updateForUpdate + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                    + "-  OK  - The related update for '2017-04-05T11:46:13', resolution 'DAY' and given "
                    + "preaggregation is not '" + updateForUpdate + "'");
            throw e;
        } // try catch
        
        // Offsets for months start by 0, thus April is 3
        updateForUpdate = "{ \"$set\" : { \"attrType\" : \"someType\"} , "
                + "\"$inc\" : { \"points.3.samples\" : 2 , \"points.3.occur.someString\" : 2}}";

        try {
            assertEquals(updateForUpdate, backend.buildUpdateForUpdate(attrType, MongoBackendImpl.Resolution.MONTH,
                    calendar, value, count).toString());
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                    + "-  OK  - The related update for '2017-04-05T11:46:13', resolution 'MONTH' and given "
                    + "preaggregation is '" + updateForUpdate + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MongoBackendImpl.buildUpdateForUpdate]")
                    + "-  OK  - The related update for '2017-04-05T11:46:13', resolution 'MONTH' and given "
                    + "preaggregation is not '" + updateForUpdate + "'");
            throw e;
        } // try catch
    } // testBuildUpdateForUpdateString
    
} // MongoBackendImplTest
