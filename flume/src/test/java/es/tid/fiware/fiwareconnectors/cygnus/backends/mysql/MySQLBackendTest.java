/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * francisco.romerobueno@telefonica.com
 */

package es.tid.fiware.fiwareconnectors.cygnus.backends.mysql;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class MySQLBackendTest {
    
    // instance to be tested
    private MySQLBackend backend;
    
    // FIXME: the Connection object return by getConnection must be mocked. In order to pass mocked connections to the
    // MySQLBackend, the connection object must be another argument of the different methods, e.g.:
    // void createDatabase(Connection connection, String dbName) instead of void createDatabase(String dbName)
    
    // constants
    private final String host = "localhost";
    private final String port = "3306";
    private final String user = "root";
    private final String password = "12345abcde";
    
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
    } // setUp
    
    /**
     * Test of createDatabase method, of class MySQLBackend.
     */
    @Test
    public void testCreateDatabase() {
        System.out.println("Testing MySQLBackend.createDatabase");
    } // testCreateDatabase
    
    /**
     * Test of createTable method, of class MySQLBackend.
     */
    @Test
    public void testCreateTable() {
        System.out.println("Testing MySQLBackend.createTable");
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
