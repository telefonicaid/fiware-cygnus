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
    private Iterator destinationEntries;
    private Entry nextDestinationEntry;
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
     * Adds a destination name to the cache.
     * @param destination
     * @return True if the destination is added, false otherwise.
     */
    public boolean addDestination(String destination) {
        if (hierarchy.containsKey(destination)) {
            LOGGER.debug("'" + destination + "' not added to the cache, since already existing");
            return false;
        } else {
            hierarchy.put(destination, new ArrayList<String>());
            LOGGER.debug("'" + destination + "' added to the cache");
            return true;
        } // if else
    } // addDestination
    
    /**
     * Adds a table name within in a destination name to the cache.
     * @param destination
     * @param tableName
     * @return True if the table is added, false otherwise
     */
    public boolean addTable(String destination, String tableName) {
        ArrayList<String> tables = hierarchy.get(destination);
        
        if (tables != null) {
            if (tables.contains(tableName)) {
                LOGGER.debug("'" + tableName + "' not added to the cache, since already existing");
                return false;
            } else {
                tables.add(tableName);
                LOGGER.debug("'" + tableName + "' added to the cache");
                return true;
            } // if else
        } else {
            LOGGER.debug("'" + destination + "' was not added to the cache, since database/scheme did not exist");
            return false;
        } // if else
    } // addTable
    
    /**
     * Gets if a destination name is cached.
     * @param destination
     * @return True if the destination name is cached, false otherwise.
     */
    public boolean isCachedDestination(String destination) {
        return hierarchy.containsKey(destination);
    } // isCachedDestination
    
    /**
     * Gets if a table name within a destination name is cached.
     * @param destination
     * @param tableName
     * @return True if the table name is cached, false otherwise.
     */
    public boolean isCachedTable(String destination, String tableName) {
        ArrayList<String> tables = hierarchy.get(destination);
        
        if (tables == null) {
            return false;
        } else {
            return tables.contains(tableName);
        } // if else
    } // isCachedTable
    
    /**
     * Starts an interator for all the destination objects.
     */
    public void startDestinationIterator() {
        destinationEntries = hierarchy.entrySet().iterator();
    } // startDestinationIterator
    
    /**
     * Checks if there is a next destination for iteration.
     * @return True if there is a next destination for iteration, false otherwise.
     */
    public boolean hasNextDestination() {
        if (destinationEntries.hasNext()) {
            nextDestinationEntry = (Entry) destinationEntries.next();
            return true;
        } else {
            return false;
        } // if else
    } // hasNextDestination
    
    /**
     * Gets the next destination for iteration.
     * @return The next destination for iteration.
     */
    public String nextDestination() {
        return (String) nextDestinationEntry.getKey();
    } // nextDestination
    
    /**
     * Starts an iterator for all the tables within the given destination.
     * @param destination
     */
    public void startTableIterator(String destination) {
        tableEntries.put(destination, hierarchy.get(destination).iterator());
    } // startTableIterator
    
    /**
     * Checks if there is a next table for iteration within the given destination.
     * @param destination
     * @return True if there is a next table for iteration, false otherwise.
     */
    public boolean hasNextTable(String destination) {
        Iterator it = tableEntries.get(destination);
        
        if (it.hasNext()) {
            nextTableEntry.put(destination, (String) it.next());
            return true;
        } else {
            return false;
        } // if else
    } // hasNextTable
    
    /**
     * Gets the next table for iteration.
     * @param destination
     * @return The next table for iteration.
     */
    public String nextTable(String destination) {
        return (String) nextTableEntry.get(destination);
    } // nextTable
    
} // SQLCache
