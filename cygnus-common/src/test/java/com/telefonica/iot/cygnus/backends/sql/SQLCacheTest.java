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
package com.telefonica.iot.cygnus.backends.sql;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Test;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import static org.junit.Assert.assertTrue;

public class SQLCacheTest {

    /**
     * Constructor.
     */
    public SQLCacheTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // SQLCacheTest
    
    /**
     * [SQLCache.addDestination] -------- A destination is added if not existing in the cache.
     */
    @Test
    public void testAdddestinationNotExisting() {
        System.out.println(getTestTraceHead("[SQLCache.addDestination]")
                + "-------- A destination is added if not existing in the cache");
        SQLCache cache = new SQLCache();
        String destination = "destination";
        boolean added = cache.addDestination(destination);
        
        try {
            assertTrue(added);
            System.out.println(getTestTraceHead("[SQLCache.addDestination]")
                    + "-  OK  - The destination was added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.addDestination]")
                    + "- FAIL - The destination was not added");
            throw e;
        } // try catch
    } // testAdddestinationNotExisting
    
    /**
     * [SQLCache.addDestination] -------- A destination is not added if already existing in the cacbe.
     */
    @Test
    public void testAdddestinationExisting() {
        System.out.println(getTestTraceHead("[SQLCache.addDestination]")
                + "-------- A destination is not added if already existing in the cache");
        SQLCache cache = new SQLCache();
        String destination = "destination";
        boolean added1 = cache.addDestination(destination);
        boolean added2 = cache.addDestination(destination);
        
        try {
            assertTrue(added1 && !added2);
            System.out.println(getTestTraceHead("[SQLCache.addDestination]")
                    + "-  OK  - The destination was not added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.addDestination]")
                    + "- FAIL - The destination was added");
            throw e;
        } // try catch
    } // testAdddestinationExisting
    
    /**
     * [SQLCache.addTable] -------- A table is added if not existing in the cache.
     */
    @Test
    public void testAddTableNotExisting() {
        System.out.println(getTestTraceHead("[SQLCache.addTable]")
                + "-------- A table is added if not existing in the cache");
        SQLCache cache = new SQLCache();
        String destination = "destination";
        String tableName = "tablename";
        boolean added1 = cache.addDestination(destination);
        boolean added2 = cache.addTable(destination, tableName);
        
        try {
            assertTrue(added1 && added2);
            System.out.println(getTestTraceHead("[SQLCache.addTable]")
                    + "-  OK  - The table was added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.addTable]")
                    + "- FAIL - The table was not added");
            throw e;
        } // try catch
    } // testAddTableNotExisting
    
    /**
     * [SQLCache.addTable] -------- A table is not added if already existing in the cache.
     */
    @Test
    public void testAddTableExisting() {
        System.out.println(getTestTraceHead("[SQLCache.addTable]")
                + "-------- A destination is not added if already existing in the cache");
        SQLCache cache = new SQLCache();
        String destination = "destination";
        String tableName = "tablename";
        boolean added1 = cache.addDestination(destination);
        boolean added2 = cache.addTable(destination, tableName);
        boolean added3 = cache.addTable(destination, tableName);
        
        try {
            assertTrue(added1 && added2 && !added3);
            System.out.println(getTestTraceHead("[SQLCache.addTable]")
                    + "-  OK  - The table was not added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.addTable]")
                    + "- FAIL - The table was added");
            throw e;
        } // try catch
    } // testAddTableExisting
    
    /**
     * [SQLCache.addTable] -------- A table is not added if the destination does not exist in the cache.
     */
    @Test
    public void testAddTableNotExistingdestination() {
        System.out.println(getTestTraceHead("[SQLCache.addTable]")
                + "-------- A table is not added if the destination does not exist in the cache");
        SQLCache cache = new SQLCache();
        String destination = "destination";
        String tableName = "tablename";
        boolean added1 = cache.addTable(destination, tableName);
        
        try {
            assertTrue(!added1);
            System.out.println(getTestTraceHead("[SQLCache.addTable]")
                    + "-  OK  - The table was not added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.addTable]")
                    + "- FAIL - The table was added");
            throw e;
        } // try catch
    } // testAddTableNotExistingdestination
    
    /**
     * [SQLCache.isCachedDestination] -------- A cached destination is checked.
     */
    @Test
    public void testIsCacheddestinationExists() {
        System.out.println(getTestTraceHead("[SQLCache.isCachedDestination]")
                + "-------- A cached destination is checked");
        SQLCache cache = new SQLCache();
        String destination = "destination";
        cache.addDestination(destination);
        
        try {
            assertTrue(cache.isCachedDestination(destination));
            System.out.println(getTestTraceHead("[SQLCache.isCachedDestination]")
                    + "-  OK  - The destination was cached");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.isCachedDestination]")
                    + "- FAIL - The destination was not cached");
            throw e;
        } // try catch
    } // testIsCacheddestinationExists
    
    /**
     * [SQLCache.isCachedDestination] -------- A not cached destination is checked.
     */
    @Test
    public void testIsCacheddestinationNotExists() {
        System.out.println(getTestTraceHead("[SQLCache.isCachedDestination]")
                + "-------- A not cached destination is checked");
        SQLCache cache = new SQLCache();
        String destination = "destination";
        
        try {
            assertTrue(!cache.isCachedDestination(destination));
            System.out.println(getTestTraceHead("[SQLCache.isCachedDestination]")
                    + "-  OK  - The destination was not cached");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.isCachedDestination]")
                    + "- FAIL - The destination was cached");
            throw e;
        } // try catch
    } // testIsCacheddestinationNotExists
    
    /**
     * [SQLCache.isCachedTable] -------- A cached table is checked.
     */
    @Test
    public void testIsCachedTableExists() {
        System.out.println(getTestTraceHead("[SQLCache.isCachedTable]")
                + "-------- A cached table is checked");
        SQLCache cache = new SQLCache();
        String destination = "destination";
        String tableName = "tablename";
        cache.addDestination(destination);
        cache.addTable(destination, tableName);
        
        try {
            assertTrue(cache.isCachedTable(destination, tableName));
            System.out.println(getTestTraceHead("[SQLCache.isCachedTable]")
                    + "-  OK  - The table was cached");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.isCachedTable]")
                    + "- FAIL - The table was not cached");
            throw e;
        } // try catch
    } // testIsCachedTableExists
    
    /**
     * [SQLCache.isCachedTable] -------- A not cached table is checked.
     */
    @Test
    public void testIsCachedTableNotExists() {
        System.out.println(getTestTraceHead("[SQLCache.isCachedTable]")
                + "-------- A not cached table is checked");
        SQLCache cache = new SQLCache();
        String destination = "destination";
        String tableName = "tablename";
        cache.addDestination(destination);
        
        try {
            assertTrue(!cache.isCachedTable(destination, tableName));
            System.out.println(getTestTraceHead("[SQLCache.isCachedTable]")
                    + "-  OK  - The table was not cached");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.isCachedTable]")
                    + "- FAIL - The table was cached");
            throw e;
        } // try catch
    } // testIsCachedTableNotExists
    
    /**
     * [SQLCache.destination_iteration_methods] -------- destination iteration methods work.
     */
    @Test
    public void testdestinationIterationMethods() {
        System.out.println(getTestTraceHead("[SQLCache.destination_iteration_methods]")
                + "-------- Destination iteration methods work");
        SQLCache cache = new SQLCache();
        String destination1 = "destinationName1";
        String tableName11 = "tablename11";
        String tableName12 = "tablename12";
        String destination2 = "destinationName2";
        String tableName21 = "tablename21";
        String tableName22 = "tablename22";
        cache.addDestination(destination1);
        cache.addTable(destination1, tableName11);
        cache.addTable(destination1, tableName12);
        cache.addDestination(destination2);
        cache.addTable(destination2, tableName21);
        cache.addTable(destination2, tableName22);
        
        try {
            cache.startDestinationIterator();
            assertTrue(cache.hasNextDestination());
            String nextDestination1 = cache.nextDestination();
            assertTrue(cache.hasNextDestination());
            String nextDestination2 = cache.nextDestination();
            assertTrue(!cache.hasNextDestination());
            assertTrue(!nextDestination1.equals(nextDestination2) && (destination1.equals("destinationName1") || destination1.equals("destinationName2"))
                    && (destination2.equals("destinationName1") || destination2.equals("destinationName2")));
            System.out.println(getTestTraceHead("[SQLCache.destination_iteration_methods]")
                    + "-  OK  - destination iteration methods work");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.destination_iteration_methods]")
                    + "- FAIL - destination iteration methods don't work");
            throw e;
        } // try catch
    } // testdestinationIterationMethods
    
    /**
     * [SQLCache.table_iteration_methods] -------- Table iteration methods work.
     */
    @Test
    public void testTableIterationMethods() {
        System.out.println(getTestTraceHead("[SQLCache.table_iteration_methods]")
                + "-------- Table iteration methods work");
        SQLCache cache = new SQLCache();
        String destination1 = "destination";
        String tableName11 = "tablename1";
        String tableName12 = "tablename2";
        cache.addDestination(destination1);
        cache.addTable(destination1, tableName11);
        cache.addTable(destination1, tableName12);
        
        try {
            cache.startDestinationIterator();
            cache.hasNextDestination();
            String destination = cache.nextDestination();
            cache.startTableIterator(destination);
            cache.hasNextTable(destination);
            String table1 = cache.nextTable(destination);
            cache.hasNextTable(destination);
            String table2 = cache.nextTable(destination);
            
            assertTrue(!table1.equals(table2) && (table1.equals("tablename1") || table1.equals("tablename2"))
                    && (table2.equals("tablename1") || table2.equals("tablename2")));
            System.out.println(getTestTraceHead("[SQLCache.table_iteration_methods]")
                    + "-  OK  - Table iteration methods work");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.table_iteration_methods]")
                    + "- FAIL - Table iteration methods don't work");
            throw e;
        } // try catch
    } // testTableIterationMethods
    
} // SQLCacheTest
