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

import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusBadContextData;
import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusPersistenceError;
import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusRuntimeError;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import java.sql.SQLTimeoutException;

/**
 *
 * @author frb
 * 
 * MySQL related operations (database and table creation, context data insertion) when dealing with a MySQL
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
        Statement stmt = null;
        
        // get a connection to an empty database
        Connection con = getConnection("");
        
        try {            
            stmt = con.createStatement();
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        try {
            String query = "create database if not exists " + dbName;
            logger.debug("Executing MySQL query (" + query + ")");
            stmt.executeUpdate(query);
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        closeMySQLObjects(con, stmt);
    } // createDatabase
    
    /**
     * Creates a table, given its name, if not exists in the given database.
     * @param dbName
     * @param tableName
     * @throws Exception
     */
    public void createTable(String dbName, String tableName) throws Exception {
        Statement stmt = null;
        
        // get a connection to the given database
        Connection con = getConnection(dbName);
        
        try {
            stmt = con.createStatement();
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        try {
            String query = "create table if not exists " + tableName + " ("
                    + Constants.RECV_TIME_TS + " long, "
                    + Constants.RECV_TIME + " text, "
                    + Constants.ENTITY_ID + " text, "
                    + Constants.ENTITY_TYPE + " text, "
                    + Constants.ATTR_NAME + " text, "
                    + Constants.ATTR_TYPE + " text, "
                    + Constants.ATTR_VALUE + " text, "
                    + Constants.ATTR_MD + " text)";
            logger.debug("Executing MySQL query (" + query + ")");
            stmt.executeUpdate(query);
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        closeMySQLObjects(con, stmt);
    } // createTable
    
    /**
     * Inserts a new row in the given table within the given database representing a unique attribute change.
     * @param dbName
     * @param tableName
     * @param recvTimeTs
     * @param recvTime
     * @param entityId
     * @param entityType
     * @param attrName
     * @param attrType
     * @param attrValue
     * @param attrMd
     * @throws Exception
     */
    public void insertContextData(String dbName, String tableName, long recvTimeTs, String recvTime, String entityId,
            String entityType, String attrName, String attrType, String attrValue, String attrMd) throws Exception {
        Statement stmt = null;
        
        // get a connection to the given database
        Connection con = getConnection(dbName);
            
        try {
            stmt = con.createStatement();
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        try {
            String query = "insert into " + tableName + " values ('" + recvTimeTs + "', '" + recvTime + "', '"
                    + entityId + "', '" + entityType + "', '" + attrName + "', '" + attrType + "', '" + attrValue
                    + "', '" + attrMd + "')";
            logger.debug("Executing MySQL query (" + query + ")");
            stmt.executeUpdate(query);
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } catch (SQLException e) {
            throw new CygnusBadContextData(e.getMessage());
        } // try catch
        
        closeMySQLObjects(con, stmt);
    } // insertContextData
    
    /**
     * Inserts a new row in the given table within the given database representing full attribute list changes.
     * @param dbName
     * @param tableName
     * @param recvTime
     * @param attrs
     * @param mds
     * @throws Exception
     */
    public void insertContextData(String dbName, String tableName, String recvTime,
            Map<String, String> attrs, Map<String, String> mds) throws Exception {
        Statement stmt = null;
        String columnNames = null;
        String columnValues = null;
        
        // get a connection to the MySQL server and get a statement
        Connection con = getConnection(dbName);
        
        try {
            
            stmt = con.createStatement();

            // for query building purposes
            columnNames = Constants.RECV_TIME;
            columnValues = "'" + recvTime + "'";

            // iterate on the attrs in order to build the query
            Iterator it = attrs.keySet().iterator();

            while (it.hasNext()) {
                String attrName = (String) it.next();
                columnNames += "," + attrName;
                String attrValue = attrs.get(attrName);
                columnValues += ",'" + attrValue + "'";
            } // while

            // iterate on the mds in order to build the query
            it = mds.keySet().iterator();

            while (it.hasNext()) {
                String attrMdName = (String) it.next();
                columnNames += "," + attrMdName;
                String md = mds.get(attrMdName);
                columnValues += ",'" + md + "'";
            } // while
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
                
        try {
            // finish creating the query and execute it
            String query = "insert into " + tableName + " (" + columnNames + ") values (" + columnValues + ")";
            logger.debug("Executing MySQL query (" + query + ")");
            stmt.executeUpdate(query);
        } catch (SQLTimeoutException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } catch (SQLException e) {
            throw new CygnusBadContextData(e.getMessage());
        } // try catch
        
        closeMySQLObjects(con, stmt);
    } // insertContextData
    
    /**
     * Gets a connection to the MySQL server.
     * @return
     * @throws Exception
     */
    private Connection getConnection(String dbName) throws Exception {
        try {
            // dynamically load the MySQL JDBC driver
            Class.forName(driverName);

            // return a connection based on the MySQL JDBC driver
            logger.debug("Connecting to jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + dbName + "?user="
                    + mysqlUsername + "&password=XXXXXXXXXX");
            return DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + dbName,
                    mysqlUsername, mysqlPassword);
        } catch (Exception e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch
    } // getConnection
    
    /**
     * Close all the MySQL objects previously opened by doCreateTable and doQuery.
     * @param con
     * @param stmt
     * @return True if the MySQL objects have been closed, false otherwise.
     */
    private void closeMySQLObjects(Connection con, Statement stmt) throws Exception {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                throw new CygnusRuntimeError("The Hive connection could not be closed. Details="
                        + e.getMessage());
            } // try catch
        } // if

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                throw new CygnusRuntimeError("The Hive statement could not be closed. Details="
                        + e.getMessage());
            } // try catch
        } // if
    } // closeMySQLObjects
    
} // MySQLBackend