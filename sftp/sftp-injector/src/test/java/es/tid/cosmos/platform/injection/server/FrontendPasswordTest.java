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
import java.sql.SQLException;
import java.sql.Statement;
import com.google.common.io.Files;
//import org.apache.hadoop.thirdparty.guava.common.io.Files;
import org.apache.log4j.Logger;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * FrontendPasswordTest.
 *
 * @author logc
 */
public class FrontendPasswordTest extends BaseSftpTest {

    private static final org.apache.log4j.Logger LOGGER = Logger.getLogger(FrontendPassword.class);
    private static final org.apache.log4j.Logger TEST_LOGGER = Logger.getLogger(FrontendPasswordTest.class);
    private FrontendPassword instance;
    private String fileName;
    private String frontendDbUrl;
    private Connection connection;
    private File tempDir;

    /**
     * Constructor.
     */
    public FrontendPasswordTest() {
        super(LOGGER);
    } // FrontendPasswordTest

    /**
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        this.instance = new FrontendPassword();
        this.tempDir = Files.createTempDir();
        this.fileName = "test.db";
        this.frontendDbUrl = "jdbc:sqlite:" + this.tempDir.toString() + fileName;
        Class.forName("org.sqlite.JDBC").newInstance();
        this.connection = DriverManager.getConnection(this.frontendDbUrl);
        Statement stat = this.connection.createStatement();
        stat.executeUpdate("DROP TABLE IF EXISTS auth_user");
        String createStatement = "CREATE TABLE \"auth_user\" (\"username\" varchar(30) NOT NULL UNIQUE,"
                + "\"password\" varchar(128) NOT NULL);";
        stat.executeUpdate(createStatement);
        String usernameColContent1 = "test";
        String passwordColContent1 = "sha1$a49dc$b234ed692454a6164983c3fae6d8b4ca0f69b219";
        String usernameColContent2 = "testMd5";
        String passwordColContent2 = "md5$a49dc$de03e38fd7240af348801f09ab2ed616";
        String usernameColContent3 = "testSenselessContent";
        String passwordColContent3 = "colt45$a49dc$de03e38fd7240af348801f09ab2ed616";
        this.insertIntoTestDb(usernameColContent1, passwordColContent1);
        this.insertIntoTestDb(usernameColContent2, passwordColContent2);
        this.insertIntoTestDb(usernameColContent3, passwordColContent3);
    } // setUp

    /**
     * 
     * @param usernameColContent
     * @param passwordColContent
     * @throws SQLException
     */
    private void insertIntoTestDb(String usernameColContent, String passwordColContent) throws SQLException {
        String sql = "INSERT INTO auth_user(username, password) VALUES (?, ?)";
        PreparedStatement insertion = this.connection.prepareStatement(sql);
        insertion.setString(1, usernameColContent);
        insertion.setString(2, passwordColContent);
        insertion.execute();
    } // insertIntoTestDb

    /**
     * 
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        this.deleteDbFile();
    } // tearDown

    /**
     * 
     */
    private void deleteDbFile() {
        boolean deleted = false;
        File f = new File(this.tempDir.toString() + "/" + this.fileName);
        
        if (f.exists() && f.canWrite() && !f.isDirectory()) {
            deleted = f.delete();
        } // if
        
        if (!deleted) {
            TEST_LOGGER.error("test DB at " + this.tempDir.toString() + "/" + this .fileName + " could not be deleted");
        } // if
    } // deleteDbFile

    /**
     * The PasswordAuthenticator interface requires us to catch any exceptions thrown by the underlying authentication,
     * including SQL databases not found, etc. Here we show that the exception is caught and we do not allow
     * authentication if the credentials are correct but the database is missing.
     *
     * @throws Exception
     */
    @Test
    public void testWithoutDb() throws Exception {
        this.instance.setFrontendCredentials(this.frontendDbUrl, "", "", "");
        this.deleteDbFile();
        assertFalse(this.instance.authenticate("who_cares", "whatever", null));
    } // testWithoutDb

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticate() throws Exception {
        this.instance.setFrontendCredentials(this.frontendDbUrl, "", "", "");
        assertFalse(this.instance.authenticate("test", "fake_password", null));
        assertTrue(this.instance.authenticate("test", "test", null));
        assertFalse(this.instance.authenticate("testMd5", "very_fake", null));
        assertTrue(this.instance.authenticate("testMd5", "test", null));
    } // testAuthenticate

    /**
     * Show that if the content found in the database does not make sense, we do not allow authentication. Note that one
     * user does not exist and the other has 'colt45' stored as its password hashing algorithm.
     *
     * @throws Exception
     */
    @Test
    public void testDefaultAuthenticationIsFalse() throws Exception {
        this.instance.setFrontendCredentials(this.frontendDbUrl, "", "", "");
        assertFalse(this.instance.authenticate("anonymous", "we_are_legion", null));
        assertFalse(this.instance.authenticate("testSenselessContent", "whatever", null));
    } // testDefaultAuthenticationIsFalse

    /**
     * Show that if the database URL is wrongly configured, we catch the exception and do not allow to authenticate.
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticationWhenDbConfigWrong() throws Exception {
        this.instance.setFrontendCredentials("jdbc:superdb:cosmos.db", "", "",
                "");
        assertFalse(this.instance.authenticate("test", "test", null));
    } // testAuthenticationWhenDbConfigWrong
    
} // FrontendPasswordTest
