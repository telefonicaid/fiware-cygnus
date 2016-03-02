/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.http;

import com.telefonica.iot.cygnus.management.ManagementInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class JettyServerTest {
    
    // instance to be tested
    private JettyServer jettyServer;
    
    // other instances
    private ManagementInterface managementInterface;
    
    // constants
    private final int mgmtPort = 8081;
    private final int guiPort = 8082;
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        jettyServer = new JettyServer(mgmtPort, guiPort, managementInterface);
    } // setUp
    
    /**
     * Test of startServer method, of class JettyServer.
     */
    @Test
    public void testRun() {
        System.out.println("Testing JettyServer.testConfigure");
        
        try {
            jettyServer.start();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch
        
        try {
            System.out.println("Wait 5 seconds before checking the Jetty server is running");
            Thread.sleep(5000);
        } catch (Exception e) {
            fail(e.getMessage());
        } // try catch

        assertTrue(jettyServer.getServer().isRunning());
    } // testStartServer
    
} // JettyServerTest
