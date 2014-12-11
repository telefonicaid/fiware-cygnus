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
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package es.tid.fiware.fiwareconnectors.cygnus.nodes;

import es.tid.fiware.fiwareconnectors.cygnus.http.JettyServer;
import es.tid.fiware.fiwareconnectors.cygnus.management.ManagementInterface;
import org.apache.flume.node.Application;
import org.apache.log4j.Logger;

/**
 *
 * @author frb
 */
public class CygnusApplication extends Application {
    
    private Logger logger;
    private JettyServer server;
    
    public CygnusApplication() {
        super();
        logger = Logger.getLogger(CygnusApplication.class);
    } // CygnusApplication
    
    @Override
    public synchronized void start() {
        super.start();
        logger.info("Starting a Jetty server listening on port 8081 (Management Interface)");
        server = new JettyServer(8081, new ManagementInterface());
        server.start();
    } // start
    
} // CygnusApplication
