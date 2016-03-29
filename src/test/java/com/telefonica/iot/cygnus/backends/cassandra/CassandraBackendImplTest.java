/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
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

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.telefonica.iot.cygnus.backends.cassandra.CassandraBackendImpl.CassandraDriver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * @author jdegenhardt
 */
@SuppressWarnings({"FieldCanBeLocal", "Duplicates"})
@RunWith(MockitoJUnitRunner.class)
public class CassandraBackendImplTest {

    // constants
    private final String[] hosts = {"localhost"};
    private final String keyspace = "my_keyspace";
    private final String user = "root";
    private final String password = "12345abcde";
    private final String tableName1 = "table1";
    private final String fieldNames1 = "a text, b text";
    // instance to be tested
    private CassandraBackendImpl backend;
    // mocks
    @Mock
    private CassandraDriver mockDriver;
    @Mock
    private Session mockSession;
    @Mock
    private Statement mockStatement;

    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        backend = new CassandraBackendImpl(user, password, hosts);

        // set up the behaviour of the mocked classes
        when(mockDriver.getSession(Mockito.anyString())).thenReturn(mockSession);
        when(mockDriver.isSessionCreated(Mockito.anyString())).thenReturn(true);
        when(mockDriver.numConnectionsCreated()).thenReturn(1);
        when(mockDriver.isSessionCreated(Mockito.anyString())).thenReturn(true, true, true, true, true);
        when(mockSession.execute(mockStatement)).thenReturn(null);
    } // setUp

    /**
     * Test of createKeyspace method, of class CassandraBackendImpl.
     */
    @Test
    public void testCreateSchema() {
        System.out.println("Testing CassandraBackend.createKeyspace (first keyspace creation");

        try {
            backend.setDriver(mockDriver);
            backend.createKeyspace(keyspace);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(backend.getDriver().isSessionCreated(keyspace));
        } // try catch finally
    } // testCreateSchema

    /**
     * Test of createTable method, of class CassandraBackendImpl.
     */
    @Test
    public void testCreateTable() {
        System.out.println("Testing CassandraBackend.createTable (within first schema");

        try {
            backend.setDriver(mockDriver);
            backend.createKeyspace(keyspace);
            backend.createTable(keyspace, tableName1, fieldNames1);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(backend.getDriver().isSessionCreated(keyspace));
        } // try catch finally
    } // testCreateTable

    /**
     * Test of insertContextData method, of class CassandraBackendImpl.
     */
    @Test
    public void testInsertContextData() {
        System.out.println("Testing CassandraBackend.insertContextData");
    } // testInsertContextData
} // CassandraBackendImplTest
