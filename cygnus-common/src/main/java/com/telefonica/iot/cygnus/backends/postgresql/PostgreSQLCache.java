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

package com.telefonica.iot.cygnus.backends.postgresql;

import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author pcoello25
 */
public class PostgreSQLCache {
    
    private final CygnusLogger LOGGER = new CygnusLogger(PostgreSQLCache.class);
    private HashMap<String, ArrayList<String>> cache = new HashMap<String, ArrayList<String>>();
    
    public HashMap<String, ArrayList<String>> getCache() {
        return cache;
    } // getCache
    
    public void setCache(HashMap<String, ArrayList<String>> PSQLcache) {
        this.cache = PSQLcache;
    } // setCache
    
    public boolean isSchemaInCache (String schemaName) {    
        LOGGER.debug("Checking if the schema (" + schemaName + ") exists");
        if (!cache.isEmpty()) {
            
            for (HashMap.Entry<String,ArrayList<String>> entry : cache.entrySet()) {
                String schema = entry.getKey();
                
                if (schema.equals(schemaName)) {
                    LOGGER.debug("Schema (" + schemaName + ") exists in Cache");
                    return true;
                } // if
                
            } // for
            
            LOGGER.debug("Schema (" + schemaName + ") doesnt' exist in Cache");
            return false;
        } else {
            LOGGER.debug("Cache is empty");
            return false;
        } // if else

    } // isSchemaInCache
    
    public boolean isTableInCachedSchema (String schemaName, String tableName) {
        if (!cache.isEmpty()) {
            
            if (isSchemaInCache(schemaName)) {
                ArrayList<String> tables = cache.get(schemaName);
                
                LOGGER.info("Checking if the table (" + tableName + ") belongs to (" + schemaName + ")");
                
                if (tables.contains(tableName)) {
                    LOGGER.debug("Table (" + tableName + ") was found in the specified schema (" + schemaName + ")");
                    return true;
                } else {
                    LOGGER.debug("Table (" + tableName + ") wasn't found in the specified schema (" + schemaName + ")");
                    return false;
                } // if else
                
            } // if             
            
            LOGGER.debug("Schema (" + schemaName + ") wasn't found in Cache");
            return false;            
        } else {
            LOGGER.debug("Cache is empty");
            return false;
        }
    } // isTableInCachedSchema
    
    public void persistSchemaInCache(String schemaName) {
        cache.put(schemaName, new ArrayList<String>());
        LOGGER.debug("Schema (" + schemaName + ") added to cache");
    } // persistSchemaInCache
    
    public void persistTableInCache(String schemaName, String tableName) {
        
        for (HashMap.Entry<String,ArrayList<String>> entry : cache.entrySet()) {
                String schema = entry.getKey();
                
                if (schema.equals(schemaName)) {
                    ArrayList<String> tableNames = cache.get(schemaName);
                    tableNames.add(tableName);
                    cache.put(schemaName, tableNames);
                    LOGGER.debug("Table (" + tableName + ") added to schema (" + schemaName + ") in cache");
                } // if 
                
            } // for
        
    } // persistTableInCache
    
} // PostgreSQLCache
