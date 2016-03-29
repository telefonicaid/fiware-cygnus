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
@SuppressWarnings("WeakerAccess")
public interface CassandraBackend {

    /**
     * Creates a keyspace, given its name, if not exists.
     *
     * @param keyspaceName name of the cassandra keyspace that shall get created
     */
    void createKeyspace(String keyspaceName) throws IllegalArgumentException;

    /**
     * Creates a table, given its name, if not exists in the given keyspace.
     *
     * @param keyspaceName cassandra keyspace in what the table shall get created
     * @param tableName    name of the table that shall get created
     * @param fieldNames   names of the fields that shall get created in the table
     */
    void createTable(String keyspaceName, String tableName, String fieldNames) throws IllegalArgumentException;

    /**
     * Insert already processed context data into the given table within the given keyspace.
     *
     * @param keyspaceName cassandra keyspace in what the context shall get inserted
     * @param tableName    name of the table in what the context shall get inserted
     * @param fieldNames   name of the fields in what the context shall get inserted
     * @param fieldValues  values that shall get inserted
     */
    void insertContextData(String keyspaceName,
                           String tableName,
                           String fieldNames,
                           String fieldValues) throws IllegalArgumentException;

} // CassandraBackend
