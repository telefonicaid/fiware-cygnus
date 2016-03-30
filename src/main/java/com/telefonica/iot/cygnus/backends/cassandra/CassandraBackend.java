/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 * <p>
 * This file is part of fiware-cygnus (FI-WARE project).
 * <p>
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 * <p>
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.backends.cassandra;

/**
 * Interface for those backends implementing the persistence in Cassandra.
 *
 * @author jdegenhardt
 */
public interface CassandraBackend {

    /**
     * Creates a schema, given its name, if not exists.
     * @param schemaName
     * @throws Exception
     */
    void createSchema(String schemaName) throws Exception;

    /**
     * Creates a table, given its name, if not exists in the given schema.
     * @param schemaName
     * @param tableName
     * @param fieldNames
     * @throws Exception
     */
    void createTable(String schemaName, String tableName, String fieldNames) throws Exception;

    /**
     * Insert already processed context data into the given table within the given database.
     * @param schemaName
     * @param tableName
     * @param fieldNames
     * @param fieldValues
     * @throws Exception
     */
    void insertContextData(String schemaName, String tableName, String fieldNames, String fieldValues) throws Exception;

} // CassandraBackend
