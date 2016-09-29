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
    
    public int isSchemaTableInCache(String schemaName, String tableName) {
        boolean isSchemaInCache = false;
        boolean isTableInSchema = false;
        
        if (!cache.isEmpty()) {
            
            for (HashMap.Entry<String,ArrayList<String>> entry : cache.entrySet()) {
                
                String schema = entry.getKey();
                ArrayList<String> tables = entry.getValue();
                
                LOGGER.info("Checking if the schema (" + schemaName + ") is equals to (" + schema + ")");
                if (schema.equals(schemaName)) {
                    isSchemaInCache = true;
                } // if
                
                LOGGER.info("Checking if the table (" + tables + ") contains (" + tableName + ")");
                if (tables.contains(tableName)) {
                    isTableInSchema = true;
                } // if
                
            } // for
            
        } else {
            LOGGER.info("Empty Cache. Returning 2");
            return 2;
        } // if else
        
        if (isSchemaInCache && isTableInSchema) {
            LOGGER.info("Schema & table in Cache: Returning 0");
            return 0;
        } else if (isSchemaInCache && !isTableInSchema) {
            LOGGER.info("Schema in Cache but not the tableName: Returning 1");
            return 1;
        } else {
            LOGGER.info("Schema & table not in Cache: Returning 2");
            return 2;
        } // if else if
        
    } // isSchemaTableInCache
    
    public void persistInCache(String schemaName, String tableName, int code) {    
        ArrayList<String> tableNames;
        if (code == 1) {
            tableNames = cache.get(schemaName);
        } else {
            tableNames = new ArrayList<String>();
        } // if else
        
        tableNames.add(tableName);
        cache.put(schemaName, tableNames);
    } // persistInCache
    
} // PostgreSQLCache
