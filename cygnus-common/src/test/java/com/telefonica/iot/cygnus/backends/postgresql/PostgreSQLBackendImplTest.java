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

package com.telefonica.iot.cygnus.backends.postgresql;

import com.telefonica.iot.cygnus.backends.postgresql.PostgreSQLBackendImpl.PostgreSQLDriver;
import java.sql.Connection;
import java.sql.Statement;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class PostgreSQLBackendImplTest {

    // instance to be tested
    private PostgreSQLBackendImpl backend;

    // mocks
    @Mock
    private PostgreSQLDriver mockDriverSchemaCreate;
    @Mock
    private PostgreSQLDriver mockDriverTableCreate;
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;

    // constants
    private final String host = "localhost";
    private final String port = "5432";
    private final String database = "my-database";
    private final String user = "root";
    private final String password = "12345abcde";
    private final String schemaName1 = "db1";
    private final String schemaName2 = "db2";
    private final String tableName1 = "table1";
    private final String tableName2 = "table2";
    private final String fieldNames1 = "a text, b text";
    private final String fieldNames2 = "c text, d text";

    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        backend = new PostgreSQLBackendImpl(host, port, database, user, password, 3);

        // set up the behaviour of the mocked classes
        when(mockDriverSchemaCreate.getConnection(Mockito.anyString())).thenReturn(mockConnection);
        when(mockDriverSchemaCreate.isConnectionCreated(Mockito.anyString())).thenReturn(true);
        when(mockDriverSchemaCreate.numConnectionsCreated()).thenReturn(1);
        when(mockDriverTableCreate.getConnection(Mockito.anyString())).thenReturn(mockConnection);
        when(mockDriverTableCreate.isConnectionCreated(Mockito.anyString())).thenReturn(true, true, true, true, true);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeUpdate(Mockito.anyString())).thenReturn(1);
    } // setUp

    /**
     * Test of createSchema method, of class PostgreSQLBackendImpl.
     */
    @Test
    public void testCreateSchema() {
        System.out.println("Testing PostgreSQLBackend.createSchema (first schema creation");

        try {
            backend.setDriver(mockDriverSchemaCreate);
            backend.createSchema(schemaName1);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(backend.getDriver().isConnectionCreated(""));
        } // try catch finally

        System.out.println("Testing PostgreSQLBackend.createSchema (second schema creation");

        try {
            assertTrue(backend.getDriver().isConnectionCreated(""));
            backend.createSchema(schemaName2);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            // despite the number of schemas we create, the default connections associated to the empty database name
            // must be the unique element within the map
            assertTrue(backend.getDriver().numConnectionsCreated() == 1);
        } // try catch finally
    } // testCreateSchema

    /**
     * Test of createTable method, of class PostgreSQLBackendImpl.
     */
    @Test
    public void testCreateTable() {
        System.out.println("Testing PostgreSQLBackend.createTable (within first schema");

        try {
            backend.setDriver(mockDriverTableCreate);
            backend.createSchema(schemaName1);
            backend.createTable(schemaName1, tableName1, fieldNames1);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(backend.getDriver().isConnectionCreated(""));
            assertTrue(backend.getDriver().isConnectionCreated(schemaName1));
        } // try catch finally

        System.out.println("Testing PostgreSQLBackend.createTable (within second schema");

        try {
            backend.createSchema(schemaName2);
            backend.createTable(schemaName2, tableName2, fieldNames2);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(backend.getDriver().isConnectionCreated(""));
            assertTrue(backend.getDriver().isConnectionCreated(schemaName1));
            assertTrue(backend.getDriver().isConnectionCreated(schemaName2));
        } // try catch finally
    } // testCreateTable

    /**
     * Test of insertContextData method, of class PostgreSQLBackendImpl.
     */
    @Test
    public void testInsertContextData() {
        System.out.println("Testing PostgreSQLBackend.insertContextData");
    } // testInsertContextData
} // PostgreSQLBackendImplTest
