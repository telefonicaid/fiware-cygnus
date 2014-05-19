/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * frb@tid.es
 */

package es.tid.fiware.fiwareconnectors.cygnus.backends.mysql;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 * @author frb
 * 
 * MySQL related operations (database and table creation, row-like context data insertion) when dealing with a MySQL
 * persistence backend.
 */
public class MySQLBackend {
    
    private static String driverName = "com.mysql.jdbc.Driver";
    private String mysqlHost;
    private String mysqlPort;
    private String mysqlUsername;
    private String mysqlPassword;
    private Logger logger;
    
    /**
     * Constructor.
     * @param mysqlHost
     * @param mysqlPort
     * @param mysqlUsername
     * @param mysqlPassword
     */
    public MySQLBackend(String mysqlHost, String mysqlPort, String mysqlUsername, String mysqlPassword) {
        this.mysqlHost = mysqlHost;
        this.mysqlPort = mysqlPort;
        this.mysqlUsername = mysqlUsername;
        this.mysqlPassword = mysqlPassword;
        logger = Logger.getLogger(MySQLBackend.class);
    } // MySQLBackend
    
    /**
     * Creates a database, given its name, if not exists.
     * @param dbName
     * @throws Exception
     */
    public void createDatabase(String dbName) throws Exception {
        Connection con = null;
        Statement stmt = null;
        
        // create the "orion" database
        con = getConnection("");
        stmt = con.createStatement();
        logger.debug("Executing 'create database if not exists " + dbName + "'");
        stmt.executeUpdate("create database if not exists " + dbName);
        closeMySQLObjects(con, stmt);
    } // createDatabase
    
    /**
     * Creates a table, given its name, if not exists in the given database.
     * @param dbName
     * @param tableName
     * @throws Exception
     */
    public void createTable(String dbName, String tableName) throws Exception {
        Connection con = null;
        Statement stmt = null;
        
        // check if the given table name existsTable
        con = getConnection(dbName);
        stmt = con.createStatement();
        logger.debug("Executing 'create table if not exists " + tableName + " (ts, iso8601date, entityId, entityType, "
                + "attrName, attrType, attrValue)'");
        stmt.executeUpdate("create table if not exists " + tableName + " (ts long, iso8601date text, entityId text, "
                + "entityType text, attrName text, attrType text, attrValue text)");
        closeMySQLObjects(con, stmt);
    } // createTable
    
    /**
     * Inserts a new row in the given table within the given database.
     * @param dbName
     * @param tableName
     * @param ts
     * @param iso8601
     * @param entityId
     * @param entityType
     * @param attrName
     * @param attrType
     * @param attrValue
     * @throws Exception
     */
    public void insertContextData(String dbName, String tableName, long ts, String iso8601date, String entityId,
            String entityType, String attrName, String attrType, String attrValue) throws Exception {
        Connection con = null;
        Statement stmt = null;
        
        // check if the given table name existsTable
        con = getConnection(dbName);
        stmt = con.createStatement();
        stmt.executeUpdate("insert into " + tableName + " values ('" + ts + "', '" + iso8601date + "', '"
                + entityId + "', '" + entityType + "', '" + attrName + "', '" + attrType + "', '" + attrValue
                + "')");
        closeMySQLObjects(con, stmt);
    } // insertContextData
    
    /**
     * Gets a connection to the MySQL server.
     * @return
     * @throws Exception
     */
    private Connection getConnection(String dbName) throws Exception {
        // dynamically load the Hive JDBC driver
        Class.forName(driverName);

        // return a connection based on the Hive JDBC driver
        logger.debug("Connecting to jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + dbName + "?user="
                + mysqlUsername + "&password=XXXXXXXXXX");
        return DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + dbName,
                mysqlUsername, mysqlPassword);
    } // getConnection
    
    /**
     * Close all the Hive objects previously opened by doCreateTable and doQuery.
     * @param con
     * @param stmt
     * @return True if the Hive objects have been closed, false otherwise.
     */
    private boolean closeMySQLObjects(Connection con, Statement stmt) {
        boolean res = true;
        
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                res = false;
            } // try catch
        } // if

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                res = false;
            } // try catch
        } // if
        
        return res;
    } // closeMySQLObjects
    
} // MySQLBackend