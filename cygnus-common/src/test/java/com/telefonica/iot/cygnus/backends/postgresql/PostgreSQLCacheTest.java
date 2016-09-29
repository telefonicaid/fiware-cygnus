/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.postgresql;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import java.util.ArrayList;
import java.util.HashMap;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author pcoello25
 */
@RunWith(MockitoJUnitRunner.class)
public class PostgreSQLCacheTest {   
    
    private final PostgreSQLCache PSQLcache = new PostgreSQLCache();
    private final String schemaName = "aSchemaName";
    private final String tableName = "aTableName";
    
    private final ArrayList<String> emptyList = new ArrayList<String>();
    private final ArrayList<String> tableList = new ArrayList<String>();
     
    private final HashMap<String, ArrayList<String>> emptyCache = new HashMap<String, ArrayList<String>>();
    private final HashMap<String, ArrayList<String>> cacheWithSchemaAndTableName = 
            new HashMap<String, ArrayList<String>>();
    private final HashMap<String, ArrayList<String>> cacheOnlyWithSchema = new HashMap<String, ArrayList<String>>();
    
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
                
        // Used in test 'testSchemaAndTableInEmptyCache'
        cacheOnlyWithSchema.put(schemaName, emptyList);
        
        // Used in test 'testSchemaAndTableInEmptyCache'
        tableList.add(tableName);
        cacheWithSchemaAndTableName.put(schemaName, tableList);
    }
    
    @Test
    public void testSchemaAndTableInEmptyCache() {
        PSQLcache.setCache(emptyCache);
        System.out.println(getTestTraceHead("[PostgreSQLCache.isSchemaTableInCache]")
                + "-------- Adding a new tableName and new schemaName in an empty cache");
        
        try {
            assertEquals(2, PSQLcache.isSchemaTableInCache(schemaName, tableName));
            System.out.println(getTestTraceHead("[PostgreSQLCache.isSchemaTableInCache]")
                    + "-  OK  - TableName and SchemaName weren't found in the empty Cache.");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[PostgreSQLCache.isSchemaTableInCache]")
                    + "- FAIL - TableName and SchemaName were found in the empty Cache.");
        } // try catch
        
    } // testSchemaAndTableInEmptyCache
    
    @Test
    public void testSchemaAndTableWithBothValuesCached() {
        PSQLcache.setCache(cacheWithSchemaAndTableName);
        System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableWithBothValuesCached]")
                + "-------- Adding a new tableName and new schemaName in a cache with both values cached");
        
        try {
            assertEquals(0, PSQLcache.isSchemaTableInCache(schemaName, tableName));
            System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableWithBothValuesCached]")
                    + "-  OK  - TableName and SchemaName were found in the Cache.");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableWithBothValuesCached]")
                    + "- FAIL - TableName and SchemaName weren't found in the empty Cache.");
        } // try catch
        
    } // testSchemaAndTableWithBothValuesCached
    
    @Test
    public void testSchemaAndTableWithSchemaCached() {
        PSQLcache.setCache(cacheOnlyWithSchema);
        System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableWithSchemaCached]")
                + "-------- Adding a new tableName and new schemaName in a cache with the schemaName");
        
        try {
            assertEquals(1, PSQLcache.isSchemaTableInCache(schemaName, tableName));
            System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableWithSchemaCached]")
                    + "-  OK  - SchemaName was found in the cache but not TableName.");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableWithSchemaCached]")
                    + "- FAIL - SchemaName and tableName weren't found in the cache.");
        } // try catch
        
    } // testSchemaAndTableWithSchemaCached
    
    @Test
    public void testPersistSchemaAndTableInCache() {
        PSQLcache.setCache(emptyCache);
        PSQLcache.persistInCache(schemaName, tableName, 2);
        System.out.println(getTestTraceHead("[PostgreSQLCache.testPersistSchemaAndTableInCache]")
                + "-------- Adding a new tableName and new schemaName in a cache with the schemaName");
        
        try {
            assertEquals(cacheWithSchemaAndTableName, PSQLcache.getCache());
            System.out.println(getTestTraceHead("[PostgreSQLCache.testPersistSchemaAndTableInCache]")
                    + "-  OK  - Schema and Table were persisted in Cache.");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[PostgreSQLCache.testPersistSchemaAndTableInCache]")
                    + "- FAIL - Schema and Table weren't persisted in Cache.");
        } // try catch
        
    } // testPersistSchemaAndTableInCache
    
    @Test
    public void testPersistOnlyTableInCache() {
        PSQLcache.setCache(cacheOnlyWithSchema);
        PSQLcache.persistInCache(schemaName, tableName, 1);
        System.out.println(getTestTraceHead("[PostgreSQLCache.testPersistOnlyTableInCache]")
                + "-------- Adding a new tableName to a schema already created in cache");
        
        try {
            assertEquals(cacheWithSchemaAndTableName, PSQLcache.getCache());
            System.out.println(getTestTraceHead("[PostgreSQLCache.testPersistOnlyTableInCache]")
                    + "-  OK  - Table was persisted in the cached schema.");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[PostgreSQLCache.testPersistOnlyTableInCache]")
                    + "- FAIL - Table wasn't persisted in the cached schema.");
        } // try catch
        
    } // testPersistOnlyTableInCache
    
    @Test
    public void testIfPersistDuplicateTableNames() {
        PSQLcache.setCache(cacheWithSchemaAndTableName);
        PSQLcache.persistInCache(schemaName, tableName, 2);
        System.out.println(getTestTraceHead("[PostgreSQLCache.testIfPersistDuplicateTableNames]")
                + "-------- Adding a tableName and schema to a cache with both values already cached.");
        
        try {
            assertEquals(cacheWithSchemaAndTableName, PSQLcache.getCache());
            System.out.println(getTestTraceHead("[PostgreSQLCache.testIfPersistDuplicateTableNames]")
                    + "-  OK  - Values weren't duplicated.");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[PostgreSQLCache.testIfPersistDuplicateTableNames]")
                    + "- FAIL - TableName was duplicated in the cache.");
        } // try catch
        
    } // testIfPersistDuplicateTableNames

} // PostgreSQLCacheTest
