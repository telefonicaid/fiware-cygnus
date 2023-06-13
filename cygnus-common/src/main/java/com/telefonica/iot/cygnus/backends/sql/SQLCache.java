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

import com.telefonica.iot.cygnus.log.CygnusLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class SQLCache {

    private static final CygnusLogger LOGGER = new CygnusLogger(SQLCache.class);
    private final HashMap<String, ArrayList<String>> hierarchy;
    private Iterator dataBaseEntries;
    private Entry nextDataBaseEntry;
    private final HashMap<String, Iterator> tableEntries;
    private final HashMap<String, String> nextTableEntry;

    /**
     * Constructor.
     */
    public SQLCache() {
        hierarchy = new HashMap<>();
        tableEntries = new HashMap<>();
        nextTableEntry = new HashMap<>();
    } // SQLCache
    
    /**
     * Adds a dataBase name to the cache.
     * @param dataBase
     * @return True if the dataBase is added, false otherwise.
     */
    public boolean addDataBase(String dataBase) {
        if (hierarchy.containsKey(dataBase)) {
            LOGGER.debug("'" + dataBase + "' not added to the database cache, since already existing (total: " +
                         hierarchy.size() + ")");
            return false;
        } else {
            hierarchy.put(dataBase, new ArrayList<String>());
            LOGGER.debug("'" + dataBase + "' added to the database cache(total: " + hierarchy.size() + ")");
            return true;
        } // if else
    } // addDataBase
    
    /**
     * Adds a table name within in a dataBase name to the cache.
     * @param dataBase
     * @param tableName
     * @return True if the table is added, false otherwise
     */
    public boolean addTable(String dataBase, String tableName) {
        ArrayList<String> tables = hierarchy.get(dataBase);
        
        if (tables != null) {
            if (tables.contains(tableName)) {
                LOGGER.debug("'" + tableName + "' not added to the tables cache, since already existing (total: " +
                             tables.size() + ")");
                return false;
            } else {
                tables.add(tableName);
                LOGGER.debug("'" + tableName + "' added to the tables cache (total: " + tables.size() + ")");
                return true;
            } // if else
        } else {
            LOGGER.debug("'" + tableName + "' was not added to the tables cache, since database/scheme" +
                         "'" + dataBase + "' did not exist (total: " + hierarchy.size() + ")");
            return false;
        } // if else
    } // addTable
    
    /**
     * Gets if a dataBase name is cached.
     * @param dataBase
     * @return True if the dataBase name is cached, false otherwise.
     */
    public boolean isCachedDataBase(String dataBase) {
        return hierarchy.containsKey(dataBase);
    } // isCachedDataBase
    
    /**
     * Gets if a table name within a dataBase name is cached.
     * @param dataBase
     * @param tableName
     * @return True if the table name is cached, false otherwise.
     */
    public boolean isCachedTable(String dataBase, String tableName) {
        ArrayList<String> tables = hierarchy.get(dataBase);
        
        if (tables == null) {
            return false;
        } else {
            return tables.contains(tableName);
        } // if else
    } // isCachedTable
    
    /**
     * Starts an interator for all the dataBase objects.
     */
    public void startDataBaseIterator() {
        dataBaseEntries = hierarchy.entrySet().iterator();
    } // startDataBaseIterator
    
    /**
     * Checks if there is a next dataBase for iteration.
     * @return True if there is a next dataBase for iteration, false otherwise.
     */
    public boolean hasNextDataBase() {
        if (dataBaseEntries.hasNext()) {
            nextDataBaseEntry = (Entry) dataBaseEntries.next();
            return true;
        } else {
            return false;
        } // if else
    } // hasNextDataBase
    
    /**
     * Gets the next dataBase for iteration.
     * @return The next dataBase for iteration.
     */
    public String nextDataBase() {
        return (String) nextDataBaseEntry.getKey();
    } // nextDataBase
    
    /**
     * Starts an iterator for all the tables within the given dataBase.
     * @param dataBase
     */
    public void startTableIterator(String dataBase) {
        tableEntries.put(dataBase, hierarchy.get(dataBase).iterator());
    } // startTableIterator
    
    /**
     * Checks if there is a next table for iteration within the given dataBase.
     * @param dataBase
     * @return True if there is a next table for iteration, false otherwise.
     */
    public boolean hasNextTable(String dataBase) {
        Iterator it = tableEntries.get(dataBase);
        
        if (it.hasNext()) {
            nextTableEntry.put(dataBase, (String) it.next());
            return true;
        } else {
            return false;
        } // if else
    } // hasNextTable
    
    /**
     * Gets the next table for iteration.
     * @param dataBase
     * @return The next table for iteration.
     */
    public String nextTable(String dataBase) {
        return (String) nextTableEntry.get(dataBase);
    } // nextTable
    
} // SQLCache
