/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of injector-server (FI-WARE project).
 *
 * injector-server is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * injector-server is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with injector-server. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * frb@tid.es
 */

package es.tid.cosmos.platform.injection.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

/**
 * FrontendPassword. Checks the user supplied password is found in the HUE frontend database.
 *
 * @author logc
 */
public class FrontendPassword implements PasswordAuthenticator {
    
    private static final String DJANGO_SEPARATOR = "$";
    private static final org.apache.log4j.Logger LOG = Logger.getLogger(FrontendPassword.class);
    private String frontendDbUrl;
    private String dbName;
    private String dbUserName;
    private String dbPassword;

    @Override
    public boolean authenticate(String username, String password, ServerSession session) {
        LOG.debug(String.format("received %s as username, %d chars as password", username, password.length()));
        boolean ans = false;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        
        try {
            connection = this.connect(this.frontendDbUrl, this.dbName, this.dbUserName, this.dbPassword);
            String sql = "SELECT password FROM auth_user WHERE username = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();
            String algorithm = "";
            String hash = "";
            String salt = "";
            
            while (resultSet.next()) {
                StringTokenizer algorithmSaltHash = new StringTokenizer(resultSet.getString(1), DJANGO_SEPARATOR);
                algorithm = algorithmSaltHash.nextToken();
                salt = algorithmSaltHash.nextToken();
                hash = algorithmSaltHash.nextToken();
            } // while
            
            if (algorithm.equals("sha1")) {
                ans = hash.equals(DigestUtils.shaHex(salt + password));
            } else if (algorithm.equals("md5")) {
                ans = hash.equals(DigestUtils.md5Hex(salt + password));
            } else {
                LOG.warn("Unknown algorithm found in DB: " + algorithm);
            } // if else if
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            return false;
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    LOG.error("could not close a result set", e);
                } // try catch
            } // if
            
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    LOG.error("could not close a database statement", e);
                } // try catch
            } // if
            
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOG.error("could not close a database connection", e);
                } // try catch
            } // if
        } // try catch finally
        
        return ans;
    } // authenticate

    /**
     * Connect to the platform frontend database with the configured credentials.
     *
     * @param url      the frontend database host URL
     * @param dbName   the frontend database name, if any
     * @param userName the username to connect to the frontend database
     * @param password the password to connect to the frontend database
     */
    private Connection connect(String url, String dbName, String userName, String password) throws SQLException {
        if (url == null) {
            throw new IllegalArgumentException("no database URL set up");
        } // if
        
        try {
            String driver = "";
            
            if (url.contains("sqlite")) {
                driver = "org.sqlite.JDBC";
            } else if (url.contains("mysql")) {
                driver = "com.mysql.jdbc.Driver";
            } // if else if
            
            if (driver.isEmpty()) {
                throw new SQLException("Missing driver");
            } // if
            
            Class.forName(driver).newInstance();
            return DriverManager.getConnection(url + dbName, userName, password);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new SQLException(e);
        } // try catch
    } // connect

    /**
     * Set all instance variables required to connect to the platform frontend database.
     *
     * @param url      the frontend database host URL
     * @param dbName   the frontend database name, if any
     * @param userName the username to connect to the frontend database
     * @param password the password to connect to the frontend database
     */
    public void setFrontendCredentials(String url, String dbName, String userName, String password) {
        this.frontendDbUrl = url;
        this.dbName = dbName;
        this.dbUserName = userName;
        this.dbPassword = password;
    } // setFrontendCredentials
    
} // FrontendPassword