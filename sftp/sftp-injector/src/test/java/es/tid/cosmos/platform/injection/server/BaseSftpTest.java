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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;

/**
 * Suppress logging during SFTP tests execution. All warnings issued to console are correctly caused by the tests, but
 * they clutter the output terminal.
 *
 * @author logc
 */
public class BaseSftpTest {

    private final Logger logger;

    /**
     * Constructor.
     * 
     * @param logger
     */
    public BaseSftpTest(Logger logger) {
        this.logger = logger;
    } // BaseSftpTest

    /**
     * Disables logging.
     */
    @Before
    public void disableLogging() {
        this.logger.setLevel(Level.OFF);
    } // disableLogging
    
} // BaseSftpTest
