/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.mysql;

import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;

/**
 * Interface for those backends implementing the persistence in MySQL.
 * 
 * @author frb
 */
public interface MySQLBackend {
    
    /**
     * Creates a database, given its name, if not exists.
     * @param dbName
     * @throws com.telefonica.iot.cygnus.errors.CygnusRuntimeError
     * @throws com.telefonica.iot.cygnus.errors.CygnusPersistenceError
     */
    void createDatabase(String dbName) throws CygnusRuntimeError, CygnusPersistenceError;
    
    /**
     * Creates a table, given its name, if not exists in the given database.
     * @param dbName
     * @param tableName
     * @param fieldNames
     * @throws com.telefonica.iot.cygnus.errors.CygnusRuntimeError
     * @throws com.telefonica.iot.cygnus.errors.CygnusPersistenceError
     */
    void createTable(String dbName, String tableName, String fieldNames)
        throws CygnusRuntimeError, CygnusPersistenceError;
    
    /**
     * Insert already processed context data into the given table within the given database.
     * @param dbName
     * @param tableName
     * @param fieldNames
     * @param fieldValues
     * @throws com.telefonica.iot.cygnus.errors.CygnusBadContextData
     * @throws com.telefonica.iot.cygnus.errors.CygnusRuntimeError
     * @throws com.telefonica.iot.cygnus.errors.CygnusPersistenceError
     */
    void insertContextData(String dbName, String tableName, String fieldNames, String fieldValues)
        throws CygnusBadContextData, CygnusRuntimeError, CygnusPersistenceError;
    
    /**
     * Caps records from the given table within the given database according to the given maximum number.
     * @param dbName
     * @param tableName
     * @param maxRecords
     * @throws com.telefonica.iot.cygnus.errors.CygnusRuntimeError
     * @throws com.telefonica.iot.cygnus.errors.CygnusPersistenceError
     */
    void capRecords(String dbName, String tableName, long maxRecords) throws CygnusRuntimeError, CygnusPersistenceError;
    
    /**
     * Expirates records within all the cached tables based on the expiration time.
     * @param expirationTime
     * @throws com.telefonica.iot.cygnus.errors.CygnusRuntimeError
     * @throws com.telefonica.iot.cygnus.errors.CygnusPersistenceError
     */
    void expirateRecordsCache(long expirationTime) throws CygnusRuntimeError, CygnusPersistenceError;
    
} // MySQLBackend
