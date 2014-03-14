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

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * InjectionServerTest.
 *
 * @author logc
 */
public class InjectionServerTest extends BaseSftpTest {

    private static final org.apache.log4j.Logger LOGGER = Logger.getLogger(InjectionServer.class);
    private InjectionServer instance;

    /**
     * Constructor.
     */
    public InjectionServerTest() {
        super(LOGGER);
    } // InjectionServerTest

    /**
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        Configuration configuration = new Configuration(InjectionServerMain.class.getResource(
                "/injection_server.dev.properties"));
        this.instance = new InjectionServer(configuration);
    } // setUp

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testSetupSftpServer() throws Exception {
        assertTrue(this.instance instanceof InjectionServer);
        this.instance.setupSftpServer();
    } // testSetupSftpServer
    
} // InjectionServerTest
