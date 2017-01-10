/**
 * Copyright 2017 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.backends.mysql;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class MySQLCacheTest {
    
    /**
     * Constructor.
     */
    public MySQLCacheTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // MySQLCacheTest
    
    /**
     * [MySQLCache.addDb] -------- A database is added if not existing in the cache.
     */
    @Test
    public void testAddDbNotExisting() {
        System.out.println(getTestTraceHead("[MySQLCache.addDb]")
                + "-------- A database is added if not existing in the cache");
        MySQLCache cache = new MySQLCache();
        String dbName = "dbname";
        boolean added = cache.addDb(dbName);
        
        try {
            assertTrue(added);
            System.out.println(getTestTraceHead("[MySQLCache.addDb]")
                    + "-  OK  - The database was added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MySQLCache.addDb]")
                    + "- FAIL - The database was not added");
            throw e;
        } // try catch
    } // testAddDbNotExisting
    
    /**
     * [MySQLCache.addDb] -------- A database is not added if already existing in the cacbe.
     */
    @Test
    public void testAddDbExisting() {
        System.out.println(getTestTraceHead("[MySQLCache.addDb]")
                + "-------- A database is not added if already existing in the cache");
        MySQLCache cache = new MySQLCache();
        String dbName = "dbname";
        boolean added1 = cache.addDb(dbName);
        boolean added2 = cache.addDb(dbName);
        
        try {
            assertTrue(added1 && !added2);
            System.out.println(getTestTraceHead("[MySQLCache.addDb]")
                    + "-  OK  - The database was not added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MySQLCache.addDb]")
                    + "- FAIL - The database was added");
            throw e;
        } // try catch
    } // testAddDbExisting
    
    /**
     * [MySQLCache.addTable] -------- A table is added if not existing in the cache.
     */
    @Test
    public void testAddTableNotExisting() {
        System.out.println(getTestTraceHead("[MySQLCache.addTable]")
                + "-------- A table is added if not existing in the cache");
        MySQLCache cache = new MySQLCache();
        String dbName = "dbname";
        String tableName = "tablename";
        boolean added1 = cache.addDb(dbName);
        boolean added2 = cache.addTable(dbName, tableName);
        
        try {
            assertTrue(added1 && added2);
            System.out.println(getTestTraceHead("[MySQLCache.addTable]")
                    + "-  OK  - The table was added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MySQLCache.addTable]")
                    + "- FAIL - The table was not added");
            throw e;
        } // try catch
    } // testAddTableNotExisting
    
    /**
     * [MySQLCache.addTable] -------- A table is not added if already existing in the cache.
     */
    @Test
    public void testAddTableExisting() {
        System.out.println(getTestTraceHead("[MySQLCache.addTable]")
                + "-------- A database is not added if already existing in the cache");
        MySQLCache cache = new MySQLCache();
        String dbName = "dbname";
        String tableName = "tablename";
        boolean added1 = cache.addDb(dbName);
        boolean added2 = cache.addTable(dbName, tableName);
        boolean added3 = cache.addTable(dbName, tableName);
        
        try {
            assertTrue(added1 && added2 && !added3);
            System.out.println(getTestTraceHead("[MySQLCache.addTable]")
                    + "-  OK  - The table was not added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MySQLCache.addTable]")
                    + "- FAIL - The table was added");
            throw e;
        } // try catch
    } // testAddTableExisting
    
    /**
     * [MySQLCache.addTable] -------- A table is not added if the database does not exist in the cache.
     */
    @Test
    public void testAddTableNotExistingDb() {
        System.out.println(getTestTraceHead("[MySQLCache.addTable]")
                + "-------- A table is not added if the database does not exist in the cache");
        MySQLCache cache = new MySQLCache();
        String dbName = "dbname";
        String tableName = "tablename";
        boolean added1 = cache.addTable(dbName, tableName);
        
        try {
            assertTrue(!added1);
            System.out.println(getTestTraceHead("[MySQLCache.addTable]")
                    + "-  OK  - The table was not added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MySQLCache.addTable]")
                    + "- FAIL - The table was added");
            throw e;
        } // try catch
    } // testAddTableNotExistingDb
    
    /**
     * [MySQLCache.isCachedDb] -------- A cached database is checked.
     */
    @Test
    public void testIsCachedDbExists() {
        System.out.println(getTestTraceHead("[MySQLCache.isCachedDb]")
                + "-------- A cached database is checked");
        MySQLCache cache = new MySQLCache();
        String dbName = "dbname";
        cache.addDb(dbName);
        
        try {
            assertTrue(cache.isCachedDb(dbName));
            System.out.println(getTestTraceHead("[MySQLCache.isCachedDb]")
                    + "-  OK  - The database was cached");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MySQLCache.isCachedDb]")
                    + "- FAIL - The database was not cached");
            throw e;
        } // try catch
    } // testIsCachedDbExists
    
    /**
     * [MySQLCache.isCachedDb] -------- A not cached database is checked.
     */
    @Test
    public void testIsCachedDbNotExists() {
        System.out.println(getTestTraceHead("[MySQLCache.isCachedDb]")
                + "-------- A not cached database is checked");
        MySQLCache cache = new MySQLCache();
        String dbName = "dbname";
        
        try {
            assertTrue(!cache.isCachedDb(dbName));
            System.out.println(getTestTraceHead("[MySQLCache.isCachedDb]")
                    + "-  OK  - The database was not cached");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MySQLCache.isCachedDb]")
                    + "- FAIL - The database was cached");
            throw e;
        } // try catch
    } // testIsCachedDbNotExists
    
    /**
     * [MySQLCache.isCachedTable] -------- A cached table is checked.
     */
    @Test
    public void testIsCachedTableExists() {
        System.out.println(getTestTraceHead("[MySQLCache.isCachedTable]")
                + "-------- A cached table is checked");
        MySQLCache cache = new MySQLCache();
        String dbName = "dbname";
        String tableName = "tablename";
        cache.addDb(dbName);
        cache.addTable(dbName, tableName);
        
        try {
            assertTrue(cache.isCachedTable(dbName, tableName));
            System.out.println(getTestTraceHead("[MySQLCache.isCachedTable]")
                    + "-  OK  - The table was cached");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MySQLCache.isCachedTable]")
                    + "- FAIL - The table was not cached");
            throw e;
        } // try catch
    } // testIsCachedTableExists
    
    /**
     * [MySQLCache.isCachedTable] -------- A not cached table is checked.
     */
    @Test
    public void testIsCachedTableNotExists() {
        System.out.println(getTestTraceHead("[MySQLCache.isCachedTable]")
                + "-------- A not cached table is checked");
        MySQLCache cache = new MySQLCache();
        String dbName = "dbname";
        String tableName = "tablename";
        cache.addDb(dbName);
        
        try {
            assertTrue(!cache.isCachedTable(dbName, tableName));
            System.out.println(getTestTraceHead("[MySQLCache.isCachedTable]")
                    + "-  OK  - The table was not cached");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[MySQLCache.isCachedTable]")
                    + "- FAIL - The table was cached");
            throw e;
        } // try catch
    } // testIsCachedTableNotExists
    
} // MySQLCacheTest
