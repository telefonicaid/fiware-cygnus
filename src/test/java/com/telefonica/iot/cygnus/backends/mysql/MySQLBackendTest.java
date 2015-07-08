/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.mysql;

import com.telefonica.iot.cygnus.backends.mysql.MySQLBackend.MySQLDriver;
import java.sql.Connection;
import java.sql.Statement;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class MySQLBackendTest {
    
    // instance to be tested
    private MySQLBackend backend;
    
    // mocks
    @Mock
    private MySQLDriver mockDriver;
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    
    // constants
    private final String host = "localhost";
    private final String port = "3306";
    private final String user = "root";
    private final String password = "12345abcde";
    private final String dbName1 = "db1";
    private final String dbName2 = "db2";
    private final String tableName1 = "table1";
    private final String tableName2 = "table2";
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        backend = new MySQLBackend(host, port, user, password);
        backend.setDriver(mockDriver);
        
        // set up the behaviour of the mocked classes
        when(mockDriver.getConnection(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeUpdate(Mockito.anyString())).thenReturn(1);
    } // setUp
    
    /**
     * Test of createDatabase method, of class MySQLBackend.
     */
    @Test
    public void testCreateDatabase() {
        System.out.println("Testing MySQLBackend.createDatabase (first database creation");
        
        try {
            backend.createDatabase(dbName1);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            // the empty database name is used to create new databases
            assertTrue(backend.getConnections().containsKey(""));
        } // try catch finally
        
        System.out.println("Testing MySQLBackend.createDatabase (second database creation");
        
        try {
            // once created a database, the empty database name must be within the connections map; this empty
            // database name has associated a default connection that will be used for new database creations
            assertTrue(backend.getConnections().containsKey(""));
            backend.createDatabase(dbName2);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            // despite the number of databases we create, the default connections asscoaited to the empty database name
            // must be the unique element within the map
            assertTrue(backend.getConnections().size() == 1);
        } // try catch finally
    } // testCreateDatabase
    
    /**
     * Test of createTable method, of class MySQLBackend.
     */
    @Test
    public void testCreateTable() {
        System.out.println("Testing MySQLBackend.createTable (within first database");
        
        try {
            backend.createDatabase(dbName1);
            backend.createTable(dbName1, tableName1);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(backend.getConnections().containsKey(""));
            assertTrue(backend.getConnections().containsKey(dbName1));
        } // try catch finally
        
        System.out.println("Testing MySQLBackend.createTable (within second database");
        
        try {
            backend.createDatabase(dbName2);
            backend.createTable(dbName2, tableName2);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(backend.getConnections().containsKey(""));
            assertTrue(backend.getConnections().containsKey(dbName1));
            assertTrue(backend.getConnections().containsKey(dbName2));
        } // try catch finally
    } // testCreateTable
    
    /**
     * Test of insertContextData method, of class MySQLBackend.
     */
    @Test
    public void testInsertContextData() {
        System.out.println("Testing MySQLBackend.insertContextData");
    } // testInsertContextData
    
    /**
     * Test of getConnection method, of class MySQLBackend.
     */
    @Test
    public void testGetConnection() {
        System.out.println("Testing MySQLBackend.getConnection");
    } // testGetConnection
    
    /**
     * Test of closeMySQLObjects method, of class MySQLBackend.
     */
    @Test
    public void testCloseMySQLObjects() {
        System.out.println("Testing MySQLBackend.closeMySQLObjects");
    } // testCloseMySQLObjects

} // MySQLBackendTest
