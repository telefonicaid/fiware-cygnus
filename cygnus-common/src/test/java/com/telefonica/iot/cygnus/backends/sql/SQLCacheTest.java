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

// import org.apache.log4j.Level;
// import org.apache.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.Test;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import static org.junit.Assert.assertTrue;

public class SQLCacheTest {

    /**
     * Constructor.
     */
    public SQLCacheTest() {
        //LogManager.getRootLogger().setLevel(Level.FATAL);
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.FATAL);
        ctx.updateLoggers();
    } // SQLCacheTest
    
    /**
     * [SQLCache.addDataBase] -------- A dataBase is added if not existing in the cache.
     */
    @Test
    public void testAdddataBaseNotExisting() {
        System.out.println(getTestTraceHead("[SQLCache.addDataBase]")
                + "-------- A dataBase is added if not existing in the cache");
        SQLCache cache = new SQLCache();
        String dataBase = "dataBase";
        boolean added = cache.addDataBase(dataBase);
        
        try {
            assertTrue(added);
            System.out.println(getTestTraceHead("[SQLCache.addDataBase]")
                    + "-  OK  - The dataBase was added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.addDataBase]")
                    + "- FAIL - The dataBase was not added");
            throw e;
        } // try catch
    } // testAdddataBaseNotExisting
    
    /**
     * [SQLCache.addDataBase] -------- A dataBase is not added if already existing in the cacbe.
     */
    @Test
    public void testAdddataBaseExisting() {
        System.out.println(getTestTraceHead("[SQLCache.addDataBase]")
                + "-------- A dataBase is not added if already existing in the cache");
        SQLCache cache = new SQLCache();
        String dataBase = "dataBase";
        boolean added1 = cache.addDataBase(dataBase);
        boolean added2 = cache.addDataBase(dataBase);
        
        try {
            assertTrue(added1 && !added2);
            System.out.println(getTestTraceHead("[SQLCache.addDataBase]")
                    + "-  OK  - The dataBase was not added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.addDataBase]")
                    + "- FAIL - The dataBase was added");
            throw e;
        } // try catch
    } // testAdddataBaseExisting
    
    /**
     * [SQLCache.addTable] -------- A table is added if not existing in the cache.
     */
    @Test
    public void testAddTableNotExisting() {
        System.out.println(getTestTraceHead("[SQLCache.addTable]")
                + "-------- A table is added if not existing in the cache");
        SQLCache cache = new SQLCache();
        String dataBase = "dataBase";
        String tableName = "tablename";
        boolean added1 = cache.addDataBase(dataBase);
        boolean added2 = cache.addTable(dataBase, tableName);
        
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
                + "-------- A dataBase is not added if already existing in the cache");
        SQLCache cache = new SQLCache();
        String dataBase = "dataBase";
        String tableName = "tablename";
        boolean added1 = cache.addDataBase(dataBase);
        boolean added2 = cache.addTable(dataBase, tableName);
        boolean added3 = cache.addTable(dataBase, tableName);
        
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
     * [SQLCache.addTable] -------- A table is not added if the dataBase does not exist in the cache.
     */
    @Test
    public void testAddTableNotExistingdataBase() {
        System.out.println(getTestTraceHead("[SQLCache.addTable]")
                + "-------- A table is not added if the dataBase does not exist in the cache");
        SQLCache cache = new SQLCache();
        String dataBase = "dataBase";
        String tableName = "tablename";
        boolean added1 = cache.addTable(dataBase, tableName);
        
        try {
            assertTrue(!added1);
            System.out.println(getTestTraceHead("[SQLCache.addTable]")
                    + "-  OK  - The table was not added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.addTable]")
                    + "- FAIL - The table was added");
            throw e;
        } // try catch
    } // testAddTableNotExistingdataBase
    
    /**
     * [SQLCache.isCachedDataBase] -------- A cached dataBase is checked.
     */
    @Test
    public void testIsCacheddataBaseExists() {
        System.out.println(getTestTraceHead("[SQLCache.isCachedDataBase]")
                + "-------- A cached dataBase is checked");
        SQLCache cache = new SQLCache();
        String dataBase = "dataBase";
        cache.addDataBase(dataBase);
        
        try {
            assertTrue(cache.isCachedDataBase(dataBase));
            System.out.println(getTestTraceHead("[SQLCache.isCachedDataBase]")
                    + "-  OK  - The dataBase was cached");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.isCachedDataBase]")
                    + "- FAIL - The dataBase was not cached");
            throw e;
        } // try catch
    } // testIsCacheddataBaseExists
    
    /**
     * [SQLCache.isCachedDataBase] -------- A not cached dataBase is checked.
     */
    @Test
    public void testIsCacheddataBaseNotExists() {
        System.out.println(getTestTraceHead("[SQLCache.isCachedDataBase]")
                + "-------- A not cached dataBase is checked");
        SQLCache cache = new SQLCache();
        String dataBase = "dataBase";
        
        try {
            assertTrue(!cache.isCachedDataBase(dataBase));
            System.out.println(getTestTraceHead("[SQLCache.isCachedDataBase]")
                    + "-  OK  - The dataBase was not cached");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.isCachedDataBase]")
                    + "- FAIL - The dataBase was cached");
            throw e;
        } // try catch
    } // testIsCacheddataBaseNotExists
    
    /**
     * [SQLCache.isCachedTable] -------- A cached table is checked.
     */
    @Test
    public void testIsCachedTableExists() {
        System.out.println(getTestTraceHead("[SQLCache.isCachedTable]")
                + "-------- A cached table is checked");
        SQLCache cache = new SQLCache();
        String dataBase = "dataBase";
        String tableName = "tablename";
        cache.addDataBase(dataBase);
        cache.addTable(dataBase, tableName);
        
        try {
            assertTrue(cache.isCachedTable(dataBase, tableName));
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
        String dataBase = "dataBase";
        String tableName = "tablename";
        cache.addDataBase(dataBase);
        
        try {
            assertTrue(!cache.isCachedTable(dataBase, tableName));
            System.out.println(getTestTraceHead("[SQLCache.isCachedTable]")
                    + "-  OK  - The table was not cached");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.isCachedTable]")
                    + "- FAIL - The table was cached");
            throw e;
        } // try catch
    } // testIsCachedTableNotExists
    
    /**
     * [SQLCache.dataBase_iteration_methods] -------- dataBase iteration methods work.
     */
    @Test
    public void testdataBaseIterationMethods() {
        System.out.println(getTestTraceHead("[SQLCache.dataBase_iteration_methods]")
                + "-------- DataBase iteration methods work");
        SQLCache cache = new SQLCache();
        String dataBase1 = "dataBaseName1";
        String tableName11 = "tablename11";
        String tableName12 = "tablename12";
        String dataBase2 = "dataBaseName2";
        String tableName21 = "tablename21";
        String tableName22 = "tablename22";
        cache.addDataBase(dataBase1);
        cache.addTable(dataBase1, tableName11);
        cache.addTable(dataBase1, tableName12);
        cache.addDataBase(dataBase2);
        cache.addTable(dataBase2, tableName21);
        cache.addTable(dataBase2, tableName22);
        
        try {
            cache.startDataBaseIterator();
            assertTrue(cache.hasNextDataBase());
            String nextDataBase1 = cache.nextDataBase();
            assertTrue(cache.hasNextDataBase());
            String nextDataBase2 = cache.nextDataBase();
            assertTrue(!cache.hasNextDataBase());
            assertTrue(!nextDataBase1.equals(nextDataBase2) && (dataBase1.equals("dataBaseName1") || dataBase1.equals("dataBaseName2"))
                    && (dataBase2.equals("dataBaseName1") || dataBase2.equals("dataBaseName2")));
            System.out.println(getTestTraceHead("[SQLCache.dataBase_iteration_methods]")
                    + "-  OK  - dataBase iteration methods work");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[SQLCache.dataBase_iteration_methods]")
                    + "- FAIL - dataBase iteration methods don't work");
            throw e;
        } // try catch
    } // testdataBaseIterationMethods
    
    /**
     * [SQLCache.table_iteration_methods] -------- Table iteration methods work.
     */
    @Test
    public void testTableIterationMethods() {
        System.out.println(getTestTraceHead("[SQLCache.table_iteration_methods]")
                + "-------- Table iteration methods work");
        SQLCache cache = new SQLCache();
        String dataBase1 = "dataBase";
        String tableName11 = "tablename1";
        String tableName12 = "tablename2";
        cache.addDataBase(dataBase1);
        cache.addTable(dataBase1, tableName11);
        cache.addTable(dataBase1, tableName12);
        
        try {
            cache.startDataBaseIterator();
            cache.hasNextDataBase();
            String dataBase = cache.nextDataBase();
            cache.startTableIterator(dataBase);
            cache.hasNextTable(dataBase);
            String table1 = cache.nextTable(dataBase);
            cache.hasNextTable(dataBase);
            String table2 = cache.nextTable(dataBase);
            
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
