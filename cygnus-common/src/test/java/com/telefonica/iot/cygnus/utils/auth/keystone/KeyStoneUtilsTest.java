/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.utils.auth.keystone;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.PropertyUtils;

/**
 * 
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 *
 */
public class KeyStoneUtilsTest {

    private static final CygnusLogger LOGGER = new CygnusLogger(KeyStoneUtilsTest.class);

    // instance to be tested
    private KeyStoneUtilsImpl keyStoneUtils;

    private final PropertyUtils property = new PropertyUtils("./src/test/resources/login.properties");;

    // constants
    private final String host = property.getProperty("orionHostKey");
    private final String port = property.getProperty("orionPortKey");
    private final String user = property.getProperty("orionUsername");
    private final String password = property.getProperty("orionPassword");
    private final String fiwareService = property.getProperty("orionFiware");
    private final String fiwareServicePath = property.getProperty("orionFiwarePath");
    private final String noServicePath = "/";

    private final String user2 = property.getProperty("orionUsername2");
    private final String password2 = property.getProperty("orionPassword2");
    private final String fiwareService2 = property.getProperty("orionFiware2");
    private final String fiwareServicePath2 = property.getProperty("orionFiwarePath2");

    private final boolean ssl = true;
    private final int maxConns = 50;
    private final int maxConnsPerRoute = 50;

    /**
     * Sets up tests by creating a unique instance of the tested class, and by
     * defining the behaviour of the mocked classes.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        keyStoneUtils = new KeyStoneUtilsImpl(host, port, ssl, maxConns, maxConnsPerRoute);
    } // setUp

    /**
     * Test of getting session token from keystone server.
     */
    @Test
    public void testServiceLevelTokenOK() {
        System.out.println("Testing KeyStoneBackendImpl.serviceLevelToken");
        String token = "";

        System.out.println("------ Setting token TTL to 1 minute ");
        keyStoneUtils.setTokenTimeToLive(1);

        try {
            System.out.println("------ Getign token for " + fiwareService + fiwareServicePath);
            token = keyStoneUtils.getSessionToken(user, password, fiwareService, fiwareServicePath);
            System.out.println("Returned token:  " + token);

            System.out.println("------ Waiting token to expire...");
            Thread.sleep(61000);

            System.out.println("------ Getign token for " + fiwareService + fiwareServicePath);
            token = keyStoneUtils.getSessionToken(user, password, fiwareService, fiwareServicePath);
            System.out.println("Returned token:  " + token);

            assertTrue(!token.equals(""));

        } catch (CygnusRuntimeError e) {
            System.out.println(e.getMessage());
            fail(e.getMessage());
        } catch (CygnusPersistenceError e) {
            System.out.println(e.getMessage());
            fail(e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            fail(e.getMessage());
        }
    }

    /**
     * Test of getting session token from keystone server.
     */
    @Test
    public void testServiceLevelTokenNoAuth() {
        System.out.println("Testing KeyStoneBackendImpl.serviceLevelToken");
        String token = "";

        System.out.println("------ Setting token TTL to 1 minute ");
        keyStoneUtils.setTokenTimeToLive(1);

        try {

            System.out.println("------ Getign token for " + fiwareService2 + fiwareServicePath2);
            token = keyStoneUtils.getSessionToken(user2, password2, fiwareService2, fiwareServicePath2);
            System.out.println("Returned token:  " + token);

            System.out.println("------ Waiting token to expire...");
            Thread.sleep(61000);

            System.out.println("------ Getign token for " + fiwareService2 + fiwareServicePath2);
            token = keyStoneUtils.getSessionToken(user2, password2 + "fail", fiwareService2, fiwareServicePath2);
            System.out.println("Returned token:  " + token);

            fail(token);

        } catch (CygnusRuntimeError e) {
            System.out.println(e.getMessage());
            Assert.assertNotNull(e.getMessage());
        } catch (CygnusPersistenceError e) {
            System.out.println(e.getMessage());
            Assert.assertNotNull(e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            Assert.assertNotNull(e.getMessage());
        }

    }

    /**
     * Test of getting session token from keystone server.
     */
    @Test
    public void testGetSessionToken() {
        LOGGER.debug("Testing KeyStoneBackendImpl.getSessionToken");
        String token = "";

        try {
            LOGGER.debug("------ Getign token for " + fiwareService + fiwareServicePath);
            token = keyStoneUtils.getSessionToken(user, password, fiwareService, fiwareServicePath);
            LOGGER.debug("Returned token:  " + token);

            LOGGER.debug("------ Getign token for " + fiwareService2 + fiwareServicePath2);
            token = keyStoneUtils.getSessionToken(user2, password2, fiwareService2, fiwareServicePath2);
            LOGGER.debug("Returned token:  " + token);

            LOGGER.debug("------ Retry get token from cache:  " + fiwareService2 + fiwareServicePath2);
            token = keyStoneUtils.getSessionToken(user2, password2, fiwareService2, fiwareServicePath2);
            LOGGER.debug("Returned token:  " + token);

            /* Uncomment for testing exceeded token live time */
            LOGGER.debug("------ Sleeping time (if enabled in test) ....");
            // TimeUnit.MINUTES.sleep(1);

            LOGGER.debug("------ Retry token forced update: " + fiwareService2 + fiwareServicePath2);
            token = keyStoneUtils.updateSessionToken(user2, password2, fiwareService2, fiwareServicePath2);
            LOGGER.debug("Returned token:  " + token);

            LOGGER.debug("------ Getign token for " + fiwareService + fiwareServicePath);
            token = keyStoneUtils.getSessionToken(user, password, fiwareService, fiwareServicePath);
            LOGGER.debug("Returned token:  " + token);

            LOGGER.debug("------ Getign token for " + fiwareService2 + fiwareServicePath2);
            token = keyStoneUtils.getSessionToken(user2, password2, fiwareService2, fiwareServicePath2);
            LOGGER.debug("Returned token:  " + token);

            LOGGER.debug("------ Getign token for " + fiwareService + noServicePath);
            token = keyStoneUtils.getSessionToken(user, password, fiwareService, noServicePath);
            LOGGER.debug("Returned token:  " + token);

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            fail(e.getMessage());
        } finally {
            // The token is not empty
            assertTrue(!token.equals(""));
        } // try catch finally

    } // testGetSessionToken

}
