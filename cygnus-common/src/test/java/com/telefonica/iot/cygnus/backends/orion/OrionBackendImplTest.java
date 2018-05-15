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

package com.telefonica.iot.cygnus.backends.orion;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.PropertyUtils;
import com.telefonica.iot.cygnus.utils.auth.keystone.KeyStoneUtils;
import com.telefonica.iot.cygnus.utils.auth.keystone.KeyStoneUtilsImpl;

/**
 *
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 */
public class OrionBackendImplTest {

    private static final CygnusLogger LOGGER = new CygnusLogger(OrionBackendImplTest.class);

    // instance to be tested
    private OrionBackend backend;
    private KeyStoneUtils keyStoneUtils;

    private final PropertyUtils propertyUtils = new PropertyUtils("./src/test/resources/login.properties");;
    
    // constants
    private final String orionHost = propertyUtils.getProperty("orionHost");
    private final String orionPort = propertyUtils.getProperty("orionPort");
    private final String orionHostKey = propertyUtils.getProperty("orionHostKey");
    private final String orionPortKey = propertyUtils.getProperty("orionPortKey");
    private final String orionUsername = propertyUtils.getProperty("orionUsername");
    private final String orionPassword = propertyUtils.getProperty("orionPassword");
    private final String orionFiware = propertyUtils.getProperty("orionFiware");
    private final String orionFiwarePath = propertyUtils.getProperty("orionFiwarePath");
    
    private static String token = "";
    private final List<String> listBodyJSON = new ArrayList<String>() {
        {
            add("{\"id\": \"Car1\", \"type\": \"Car\", \"speed\": { \"type\":\"Float\", \"value\": 98 } }");
            add("{\"id\": \"Car2\", \"type\": \"Car\", \"speed\": { \"type\":\"Float\", \"value\": 98 } }");
            add("{\"id\": \"Car1\", \"type\": \"Car\", \"speed\": { \"type\":\"Float\", \"value\": 105 }, "
                    + "\"speedMax\": { \"type\": \"Float\",\"value\": 300 } }");
        }
    };
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
        keyStoneUtils = new KeyStoneUtilsImpl(orionHostKey, orionPortKey, ssl, maxConns, maxConnsPerRoute);
        backend = new OrionBackendImpl(orionHost, orionPort, ssl, maxConns, maxConnsPerRoute);
        token = keyStoneUtils.getSessionToken(orionUsername, orionPassword, orionFiware, orionFiwarePath);

    } // setUp

    /**
     * Test of testInsertDatos method, of class OrionBackendImpl.
     */
    @Test
    public void testInsertDatos() {
        LOGGER.debug("Testing OrionBackend.updateRemoteContext");
        try {
            for (String bodyJSON : listBodyJSON) {
                backend.updateRemoteContext(bodyJSON, token, orionFiware, orionFiwarePath);
            }
            assertTrue(true);
        } catch (Exception e) {
            fail(e.getMessage());
            LOGGER.error("Error of test --> " + e.getMessage());
        } finally {
            LOGGER.debug("Fin del test");
        } // try catch finally

    } // testInsertDatos

} // OrionBackendImplTest
