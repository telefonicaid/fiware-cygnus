/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package es.tid.fiware.fiwareconnectors.cygnus.http;

import es.tid.fiware.fiwareconnectors.cygnus.log.CygnusLogger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.slf4j.LoggerFactory;

/**
 *
 * @author frb
 */
public class JettyServer extends Thread {
    
    private final CygnusLogger logger;
    private final int port;
    private final AbstractHandler handler;
    private final Server server;
    
    /**
     * Constructor.
     * @param port
     * @param handler
     */
    public JettyServer(int port, AbstractHandler handler) {
        logger = new CygnusLogger(LoggerFactory.getLogger(JettyServer.class), true);
        this.port = port;
        this.handler = handler;
        server = new Server(port);
        server.setHandler(handler);
    } // JettyServer
    
    /**
     * Gets the server. It is protected because it is only going to be used in the tests.
     * @return
     */
    protected Server getServer() {
        return server;
    } // getServer
    
    @Override
    public void run() {
        try {
            server.start();
            server.join();
        } catch (Exception ex) {
            logger.fatal("Fatal error running the Management Interface. Details=" + ex.getMessage());
        }
    } // run
    
} // JettyServer
