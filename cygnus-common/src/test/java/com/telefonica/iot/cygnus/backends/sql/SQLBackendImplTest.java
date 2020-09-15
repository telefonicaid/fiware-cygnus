/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

import com.telefonica.iot.cygnus.backends.sql.SQLBackendImpl.SQLDriver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SQLBackendImplTest {

    // instance to be tested
    private SQLBackendImpl backend;

    // mocks
    @Mock
    private SQLDriver mockDriverDbCreate;
    @Mock
    private SQLDriver mockDriverTableCreate;
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;

    // constants
    private final int maxPoolSize = 2;
    private final String host = "localhost";
    private final String port = "3306";
    private final String user = "root";
    private final String password = "";
    private final String dbName1 = "db1";
    private final String dbName2 = "db2";
    private final String tableName1 = "table1";
    private final String tableName2 = "table2";
    private final String fieldNames1 = "(a INT, b INT)";
    private final String fieldNamesInsert1 = "(a, b)";
    private final String fieldValues1 = "(1,2)";
    private final String fieldNames2 = "c text, d text";
    private static final String MYSQL_DRIVER_NAME = "com.mysql.jdbc.Driver";
    private static final String MYSQL_INSTANCE_NAME = "mysql";
    private final Boolean persistErrors = true;

    // True: real test, False: Mock test
    private final boolean runRealTest = false;

    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        backend = new SQLBackendImpl(host, port, user, password, maxPoolSize, MYSQL_INSTANCE_NAME, MYSQL_DRIVER_NAME, null, persistErrors);

        // set up the behaviour of the mocked classes
        when(mockDriverDbCreate.getConnection(Mockito.anyString())).thenReturn(mockConnection);
        when(mockDriverDbCreate.isConnectionCreated(Mockito.anyString())).thenReturn(true);
        when(mockDriverDbCreate.numConnectionsCreated()).thenReturn(1);
        when(mockDriverTableCreate.getConnection(Mockito.anyString())).thenReturn(mockConnection);
        when(mockDriverTableCreate.isConnectionCreated(Mockito.anyString())).thenReturn(true, true, true, true, true);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeUpdate(Mockito.anyString())).thenReturn(1);
    } // setUp

    /**
     * Test of createDatabase method, of class SQLBackendImpl.
     */
    @Test
    public void testCreateDatabase() {
        System.out.println("Testing SQLBackendImpl.createDatabase (first database creation)");

        try {
            if (!runRealTest) backend.setDriver(mockDriverDbCreate);

            backend.createDestination(dbName1);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            // the empty database name is used to create new databases
            assertTrue(backend.getDriver().isConnectionCreated(""));
        } // try catch finally

        System.out.println("Testing SQLBackendImpl.createDatabase (second database creation)");

        try {
            // once created a database, the empty database name must be within the connections map; this empty
            // database name has associated a default connection that will be used for new database creations
            assertTrue(backend.getDriver().isConnectionCreated(""));
            backend.createDestination(dbName2);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            // despite the number of databases we create, the default connections asscoaited to the empty database name
            // must be the unique element within the map
            assertTrue(backend.getDriver().numConnectionsCreated() == 1);
        } // try catch finally
    } // testCreateDatabase

    /**
     * Test of createTable method, of class SQLBackendImpl.
     */
    @Test
    public void testCreateTable() {
        System.out.println("Testing SQLBackendImpl.createTable (within first database)");

        try {
            if (!runRealTest) backend.setDriver(mockDriverTableCreate);
            backend.createTable(dbName1, tableName1, fieldNames1);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(backend.getDriver().isConnectionCreated(""));
            assertTrue(backend.getDriver().isConnectionCreated(dbName1));
        } // try catch finally

        System.out.println("Testing SQLBackendImpl.createTable (within second database");

        try {
            backend.createDestination(dbName2);
            backend.createTable(dbName2, tableName2, fieldNames2);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(backend.getDriver().isConnectionCreated(""));
            assertTrue(backend.getDriver().isConnectionCreated(dbName1));
            assertTrue(backend.getDriver().isConnectionCreated(dbName2));
        } // try catch finally
    } // testCreateTable

    /**
     * Test of insertContextData method, of class SQLBackendImpl.
     */
    @Test
    public void testInsertContextData() {
        System.out.println("Testing SQLBackendImpl.insertContextData");
        try {
            if (!runRealTest) backend.setDriver(mockDriverTableCreate);

            backend.insertContextData(dbName1, tableName1, fieldNamesInsert1, fieldValues1);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(backend.getDriver().isConnectionCreated(""));
            assertTrue(backend.getDriver().isConnectionCreated(dbName1));
        } // try catch finally

        System.out.println("Testing SQLBackendImpl.createTable (within second database");

        try {
            backend.createDestination(dbName2);
            backend.createTable(dbName2, tableName2, fieldNames2);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(backend.getDriver().isConnectionCreated(""));
            assertTrue(backend.getDriver().isConnectionCreated(dbName1));
            assertTrue(backend.getDriver().isConnectionCreated(dbName2));
        } // try catch finally
    } // testInsertContextData

    @Test
    public void testJDBCUrlMySQL() {
        System.out.println("Testing SQLBackendImpl.SQLDriver.generateJDBCUrl (sqlInstance:mysql)");
        String sqlHost = "localhost";
        String sqlPort = "3306";
        String sqlInstance = "mysql";
        String sqlDriverName = "com.mysql.jdbc.Driver";
        String destination = "dest";

        SQLBackendImpl backend = new SQLBackendImpl(sqlHost, sqlPort, user, password, maxPoolSize, sqlInstance, sqlDriverName, null, persistErrors);
        SQLBackendImpl.SQLDriver driver = backend.getDriver();

        assertEquals(driver.generateJDBCUrl(destination), "jdbc:mysql://localhost:3306/dest");
    } // testJDBCUrlMySQL

    @Test
    public void testJDBCUrlPostgreSQL() {
        System.out.println("Testing SQLBackendImpl.SQLDriver.generateJDBCUrl (sqlInstance:postgresql)");
        String sqlHost = "localhost";
        String sqlPort = "5432";
        String sqlInstance = "postgresql";
        String sqlDriverName = "org.postgresql.Driver";
        String destination = "dest";
        String defaultDataBase = "default";

        SQLBackendImpl backend = new SQLBackendImpl(sqlHost, sqlPort, user, password, maxPoolSize, sqlInstance, sqlDriverName, defaultDataBase, persistErrors);
        SQLBackendImpl.SQLDriver driver = backend.getDriver();

        assertEquals(driver.generateJDBCUrl(destination), "jdbc:postgresql://localhost:5432/default");
    } // testJDBCUrlPostgreSQL

    @Test
    public void testJDBCUrlMySQLWithOptions() {
        System.out.println("Testing SQLBackendImpl.SQLDriver.generateJDBCUrl (sqlInstance:mysql, options:useSSL=true&requireSSL=false)");
        String sqlHost = "localhost";
        String sqlPort = "3306";
        String sqlInstance = "mysql";
        String sqlDriverName = "com.mysql.jdbc.Driver";
        String destination = "dest";
        String sqlOptions = "useSSL=true&requireSSL=false";

        SQLBackendImpl backend = new SQLBackendImpl(sqlHost, sqlPort, user, password, maxPoolSize, sqlInstance, sqlDriverName, null, sqlOptions, persistErrors);
        SQLBackendImpl.SQLDriver driver = backend.getDriver();

        assertEquals(driver.generateJDBCUrl(destination), "jdbc:mysql://localhost:3306/dest?useSSL=true&requireSSL=false");

        System.out.println("Testing SQLBackendImpl.SQLDriver.generateJDBCUrl (sqlInstance:mysql, options:<white spaces>)");
        sqlOptions = " \t";

        backend = new SQLBackendImpl(sqlHost, sqlPort, user, password, maxPoolSize, sqlInstance, sqlDriverName, null, sqlOptions, persistErrors);
        driver = backend.getDriver();

        assertEquals(driver.generateJDBCUrl(destination), "jdbc:mysql://localhost:3306/dest");

        System.out.println("Testing SQLBackendImpl.SQLDriver.generateJDBCUrl (sqlInstance:mysql, options:<empty>)");
        sqlOptions = "";

        backend = new SQLBackendImpl(sqlHost, sqlPort, user, password, maxPoolSize, sqlInstance, sqlDriverName, null, sqlOptions, persistErrors);
        driver = backend.getDriver();

        assertEquals(driver.generateJDBCUrl(destination), "jdbc:mysql://localhost:3306/dest");

        System.out.println("Testing SQLBackendImpl.SQLDriver.generateJDBCUrl (sqlInstance:mysql, options:<null>)");
        sqlOptions = null;

        backend = new SQLBackendImpl(sqlHost, sqlPort, user, password, maxPoolSize, sqlInstance, sqlDriverName, null, sqlOptions, persistErrors);
        driver = backend.getDriver();

        assertEquals(driver.generateJDBCUrl(destination), "jdbc:mysql://localhost:3306/dest");
    } // testJDBCUrlMySQLWithOptions

    @Test
    public void testJDBCUrlPostgreSQLWithOptions() {
        System.out.println("Testing SQLBackendImpl.SQLDriver.generateJDBCUrl (sqlInstance:postgresql, options:sslmode=require)");
        String sqlHost = "localhost";
        String sqlPort = "5432";
        String sqlInstance = "postgresql";
        String sqlDriverName = "org.postgresql.Driver";
        String destination = "dest";
        String defaultDataBase = "default";
        String sqlOptions = "sslmode=require";

        SQLBackendImpl backend = new SQLBackendImpl(sqlHost, sqlPort, user, password, maxPoolSize, sqlInstance, sqlDriverName, defaultDataBase, sqlOptions, persistsErrors);
        SQLBackendImpl.SQLDriver driver = backend.getDriver();

        assertEquals(driver.generateJDBCUrl(destination), "jdbc:postgresql://localhost:5432/default?sslmode=require");

        System.out.println("Testing SQLBackendImpl.SQLDriver.generateJDBCUrl (sqlInstance:postgresql, options:<white spaces)");
        sqlOptions = " \t";

        backend = new SQLBackendImpl(sqlHost, sqlPort, user, password, maxPoolSize, sqlInstance, sqlDriverName, defaultDataBase, sqlOptions, persistErrors);
        driver = backend.getDriver();

        assertEquals(driver.generateJDBCUrl(destination), "jdbc:postgresql://localhost:5432/default");

        System.out.println("Testing SQLBackendImpl.SQLDriver.generateJDBCUrl (sqlInstance:postgresql, options:<empty>)");
        sqlOptions = "";

        backend = new SQLBackendImpl(sqlHost, sqlPort, user, password, maxPoolSize, sqlInstance, sqlDriverName, defaultDataBase, sqlOptions, persistErrors);
        driver = backend.getDriver();

        assertEquals(driver.generateJDBCUrl(destination), "jdbc:postgresql://localhost:5432/default");

        System.out.println("Testing SQLBackendImpl.SQLDriver.generateJDBCUrl (sqlInstance:postgresql, options:<null>)");
        sqlOptions = null;

        backend = new SQLBackendImpl(sqlHost, sqlPort, user, password, maxPoolSize, sqlInstance, sqlDriverName, defaultDataBase, sqlOptions, persistErrors);
        driver = backend.getDriver();

        assertEquals(driver.generateJDBCUrl(destination), "jdbc:postgresql://localhost:5432/default");
    } // testJDBCUrlPostgreSQLWithOptions

} // SQLBackendImplTest
