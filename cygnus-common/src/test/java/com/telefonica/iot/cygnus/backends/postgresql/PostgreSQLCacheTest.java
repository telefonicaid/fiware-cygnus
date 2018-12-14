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

package com.telefonica.iot.cygnus.backends.postgresql;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import java.util.ArrayList;
import java.util.HashMap;
import static org.junit.Assert.assertEquals;
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

    
    @Test
    public void testSchemaAndTableInEmptyCache() {
        PSQLcache.setCache(emptyCache);
        System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableInEmptyCache]")
                + "-------- Testing if a new tableName and new schemaName are in an empty cache");
        
        try {
            assertEquals(true, ((!PSQLcache.isSchemaInCache(schemaName)) 
                    && (!PSQLcache.isTableInCachedSchema(schemaName, tableName))));
            System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableInEmptyCache]")
                    + "-  OK  - TableName and SchemaName weren't found in the empty Cache.");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableInEmptyCache]")
                    + "- FAIL - TableName and SchemaName were found in the empty Cache.");
        } // try catch
        
    } // testSchemaAndTableInEmptyCache
    
    @Test
    public void testSchemaAndTableWithBothValuesCached() {
        tableList.add(tableName);
        cacheWithSchemaAndTableName.put(schemaName, tableList);
        PSQLcache.setCache(cacheWithSchemaAndTableName);
        System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableWithBothValuesCached]")
                + "-------- Testing if a tableName and a schemaName are in a cache with both values cached");
        
        try {
            assertEquals(true, ((PSQLcache.isSchemaInCache(schemaName)) 
                    && (PSQLcache.isTableInCachedSchema(schemaName, tableName))));
            System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableWithBothValuesCached]")
                    + "-  OK  - TableName and SchemaName were found in the Cache.");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableWithBothValuesCached]")
                    + "- FAIL - TableName and SchemaName weren't found in the empty Cache.");
        } // try catch
        
    } // testSchemaAndTableWithBothValuesCached
    
    @Test
    public void testSchemaAndTableWithSchemaCached() {
        cacheOnlyWithSchema.put(schemaName, emptyList);
        PSQLcache.setCache(cacheOnlyWithSchema);
        System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableWithSchemaCached]")
                + "-------- Testing if a new tableName and a schemaName are in a cache with cached schemaName");
        
        try {
            assertEquals(true, ((PSQLcache.isSchemaInCache(schemaName)) 
                    && (!PSQLcache.isTableInCachedSchema(schemaName, tableName))));
            System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableWithSchemaCached]")
                    + "-  OK  - SchemaName was found in the cache but not TableName.");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[PostgreSQLCache.testSchemaAndTableWithSchemaCached]")
                    + "- FAIL - SchemaName and tableName weren't found in the cache.");
        } // try catch
        
    } // testSchemaAndTableWithSchemaCached
    
    //@Test
    public void testPersistSchemaAndTableInCache() {
        PSQLcache.setCache(emptyCache);
        PSQLcache.persistSchemaInCache(schemaName);
        PSQLcache.persistTableInCache(schemaName, tableName);
        System.out.println(getTestTraceHead("[PostgreSQLCache.testPersistSchemaAndTableInCache]")
                + "-------- Persisting a new tableName and new schemaName into an empty cache");
        
        try {
            assertEquals(cacheWithSchemaAndTableName, PSQLcache.getCache());
            System.out.println(getTestTraceHead("[PostgreSQLCache.testPersistSchemaAndTableInCache]")
                    + "-  OK  - Schema and Table were persisted in Cache.");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[PostgreSQLCache.testPersistSchemaAndTableInCache]")
                    + "- FAIL - Schema and Table weren't persisted in Cache.");
        } // try catch
        
    } // testPersistSchemaAndTableInCache
    
    //@Test
    public void testPersistOnlyTableInCache() {
        cacheOnlyWithSchema.put(schemaName, emptyList);
        PSQLcache.setCache(cacheOnlyWithSchema);
        PSQLcache.persistTableInCache(schemaName, tableName);
        System.out.println(getTestTraceHead("[PostgreSQLCache.testPersistOnlyTableInCache]")
                + "-------- Persisting a new tableName into a schema already created in cache");
        
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
        tableList.add(tableName);
        cacheWithSchemaAndTableName.put(schemaName, tableList);
        PSQLcache.setCache(cacheWithSchemaAndTableName);
        PSQLcache.persistSchemaInCache(schemaName);
        PSQLcache.persistTableInCache(schemaName, tableName);
        System.out.println(getTestTraceHead("[PostgreSQLCache.testIfPersistDuplicateTableNames]")
                + "-------- Persisting a tableName and schema into a cache with both values already cached.");
        
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
