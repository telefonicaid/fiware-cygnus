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

import java.io.File;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

/**
 * InjectionServerMain is the main entry point to this application.
 *
 * @author logc
 * @since  CTP 2
 */
public final class InjectionServerMain {
    
    private static final String DEFAULT_EXTERNAL_CONFIGURATION = "file:///etc/cosmos/injection.properties";
    private static final String INTERNAL_CONFIGURATION = "/injection_server.prod.properties";
    private static final org.apache.log4j.Logger LOG = Logger.getLogger(InjectionServerMain.class);

    /*
     * Constructor.
     */
    private InjectionServerMain() {
    } // InjectionServerMain

    /**
     * 
     * @param args
     * @throws ConfigurationException
     */
    public static void main(String[] args) throws ConfigurationException {
        ServerCommandLine commandLine = new ServerCommandLine();
        
        try {
            commandLine.parse(args);
        } catch (ParseException e) {
            commandLine.printUsage();
            System.exit(1);
        } // try catch

        String externalConfiguration = DEFAULT_EXTERNAL_CONFIGURATION;
        
        if (commandLine.hasConfigFile()) {
            externalConfiguration = commandLine.getConfigFile();
        } // if

        Configuration config;
        
        try {
            config = new Configuration(new File(externalConfiguration).toURI().toURL());
        } catch (Exception ex) {
            config = new Configuration(InjectionServerMain.class.getResource(INTERNAL_CONFIGURATION));
        } // try catch

        try {
            InjectionServer server = new InjectionServer(config);
            server.setupSftpServer();
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            System.exit(1);
        } // try catch
    } // main

} // InjectionServerMain
