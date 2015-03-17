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

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import es.tid.fiware.fiwareconnectors.cygnus.http.JettyServer;
import es.tid.fiware.fiwareconnectors.cygnus.management.ManagementInterface;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.flume.Constants;
import org.apache.flume.lifecycle.LifecycleAware;
import org.apache.flume.node.Application;
import org.apache.flume.node.MaterializedConfiguration;
import org.apache.flume.node.PollingPropertiesFileConfigurationProvider;
import org.apache.flume.node.PropertiesFileConfigurationProvider;
import org.apache.log4j.Logger;

/**
 *
 * @author frb
 */
public class CygnusApplication extends Application {
    
    private static Logger logger;
    private int mgmtIfPort;
    private JettyServer server;
    
    /**
     * Constructor.
     */
    public CygnusApplication(int mgmtIfPort) {
        super();
        this.mgmtIfPort = mgmtIfPort;
        logger = Logger.getLogger(CygnusApplication.class);
    } // CygnusApplication
    
    /**
     * Constructor.
     * @param components
     */
    public CygnusApplication(List<LifecycleAware> components, int mgmtIfPort) {
        super(components);
        this.mgmtIfPort = mgmtIfPort;
        logger = Logger.getLogger(CygnusApplication.class);
    } // CygnusApplication

    @Override
    @Subscribe
    public synchronized void handleConfigurationEvent(MaterializedConfiguration conf) {
        super.handleConfigurationEvent(conf);
        startManagementInterface(conf);
    } // handleConfigurationEvent
    
    /**
     * Starts a Management Interface instance based on a Jetty server.
     * @param conf
     */
    private void startManagementInterface(MaterializedConfiguration conf) {
        logger.info("Starting a Jetty server listening on port " + mgmtIfPort + " (Management Interface)");
        server = new JettyServer(mgmtIfPort, new ManagementInterface(conf.getSourceRunners(), conf.getChannels(),
                conf.getSinkRunners()));
        server.start();
    } // startManagementInterface
   
    /**
     * Main application to be run when this CygnusApplication is invoked. The only differences with the original one
     * are the CygnusApplication is used instead of the Application one, and the Management Interface port option in
     * the command line.
     * @param args
     */
    public static void main(String[] args) {
        try {
            Options options = new Options();

            Option option = new Option("n", "name", true, "the name of this agent");
            option.setRequired(true);
            options.addOption(option);

            option = new Option("f", "conf-file", true, "specify a conf file");
            option.setRequired(true);
            options.addOption(option);

            option = new Option(null, "no-reload-conf", false, "do not reload conf file if changed");
            options.addOption(option);

            option = new Option("h", "help", false, "display help text");
            options.addOption(option);
            
            option = new Option("p", "mgmt-if-port", true, "the management interface port");
            option.setRequired(false);
            options.addOption(option);
            
            option = new Option("t", "polling-interval", true, "polling interval");
            option.setRequired(false);
            options.addOption(option);

            CommandLineParser parser = new GnuParser();
            CommandLine commandLine = parser.parse(options, args);

            File configurationFile = new File(commandLine.getOptionValue('f'));
            String agentName = commandLine.getOptionValue('n');
            boolean reload = !commandLine.hasOption("no-reload-conf");

            if (commandLine.hasOption('h')) {
                new HelpFormatter().printHelp("flume-ng agent", options, true);
                return;
            } // if
            
            int mgmtIfPort = 8081; // default value
            
            if (commandLine.hasOption('p')) {
                mgmtIfPort = new Integer(commandLine.getOptionValue('p')).intValue();
            } // if
            
            int pollingTime = 30; // default value
            
            if (commandLine.hasOption('t')) {
                pollingTime = new Integer(commandLine.getOptionValue('t'));
            } // if
            
            // the following is to ensure that by default the agent will fail on startup if the file does not exist
            
            if (!configurationFile.exists()) {
                // if command line invocation, then need to fail fast
                if (System.getProperty(Constants.SYSPROP_CALLED_FROM_SERVICE) == null) {
                    String path = configurationFile.getPath();
                    
                    try {
                        path = configurationFile.getCanonicalPath();
                    } catch (IOException ex) {
                        logger.error("Failed to read canonical path for file: " + path, ex);
                    } // try catch
                    
                    throw new ParseException("The specified configuration file does not exist: " + path);
                } // if
            } // if
            
            List<LifecycleAware> components = Lists.newArrayList();
            CygnusApplication application;

            if (reload) {
                EventBus eventBus = new EventBus(agentName + "-event-bus");
                PollingPropertiesFileConfigurationProvider configurationProvider =
                        new PollingPropertiesFileConfigurationProvider(agentName, configurationFile, eventBus,
                                pollingTime);
                components.add(configurationProvider);
                application = new CygnusApplication(components, mgmtIfPort);
                eventBus.register(application);
            } else {
                PropertiesFileConfigurationProvider configurationProvider =
                        new PropertiesFileConfigurationProvider(agentName, configurationFile);
                application = new CygnusApplication(mgmtIfPort);
                application.handleConfigurationEvent(configurationProvider.getConfiguration());
            } // if else
            
            application.start();

            final CygnusApplication appReference = application;
            Runtime.getRuntime().addShutdownHook(new Thread("agent-shutdown-hook") {
                @Override
                public void run() {
                    appReference.stop();
                } // run
            });
        } catch (Exception e) {
            logger.error("A fatal error occurred while running. Exception follows.", e);
        } // try catch
    } // main
    
} // CygnusApplication
