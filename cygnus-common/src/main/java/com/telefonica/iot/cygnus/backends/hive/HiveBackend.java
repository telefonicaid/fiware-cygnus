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
package com.telefonica.iot.cygnus.backends.hive;

/**
 *
 * @author frb
 */
public interface HiveBackend {
    
    /**
     * Creates a HiveQL database.
     * @param dbName
     * @return True if the database could be created, false otherwise.
     */
    boolean doCreateDatabase(String dbName);
    
    /**
     * Creates a HiveQL external table.
     * @param query
     * @return True if the table could be created, false otherwise.
     */
    boolean doCreateTable(String query);
    
    /**
     * Executes a HiveQL sentence.
     * @param query
     * @return True if the query succeded, false otherwise.
     */
    boolean doQuery(String query);
    
} // HiveBackend
