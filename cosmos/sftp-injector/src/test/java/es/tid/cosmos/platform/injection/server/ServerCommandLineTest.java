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
import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 
 * @author sortega
 */
public class ServerCommandLineTest extends BaseSftpTest {

    private static final org.apache.log4j.Logger LOGGER = Logger.getLogger(ServerCommandLine.class);
    private ServerCommandLine instance;

    /**
     * Constructor.
     */
    public ServerCommandLineTest() {
        super(LOGGER);
    } // ServerCommandLineTest

    /**
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        this.instance = new ServerCommandLine();
    } // setUp

    /**
     * 
     * @throws Exception
     */
    @Test
    public void emptyCommandLine() throws Exception {
        this.instance.parse(new String[] {});
        assertFalse(this.instance.hasConfigFile());
    } // emptyCommandLine

    /**
     * 
     * @throws Exception
     */
    @Test
    public void externalShortConfigCommandLine() throws Exception {
        this.instance.parse(new String[] {"-c", "/tmp/test.properties"});
        assertTrue(this.instance.hasConfigFile());
    } // externalShortConfigCommandLine

    /**
     * 
     * @throws Exception
     */
    @Test
    public void externalLongConfigCommandLine() throws Exception {
        this.instance.parse(new String[] {"--config", "/tmp/test.properties"});
        assertTrue(this.instance.hasConfigFile());
    } // externalLongConfigCommandLine

    /**
     * 
     * @throws Exception
     */
    @Test(expected=ParseException.class)
    public void unexpectedOptionsCommandLine() throws Exception {
        this.instance.parse(new String[] {"-x"});
    } // unexpectedOptionsCommandLine
    
} // ServerCommandLineTest
