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

package es.tid.fiware.fiwareconnectors.cygnus.nodes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import es.tid.fiware.fiwareconnectors.cygnus.channels.CygnusChannel;
import es.tid.fiware.fiwareconnectors.cygnus.channels.CygnusFileChannel;
import es.tid.fiware.fiwareconnectors.cygnus.channels.CygnusMemoryChannel;
import es.tid.fiware.fiwareconnectors.cygnus.http.JettyServer;
import es.tid.fiware.fiwareconnectors.cygnus.management.ManagementInterface;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.flume.Channel;
import org.apache.flume.Constants;
import org.apache.flume.SinkRunner;
import org.apache.flume.SourceRunner;
import org.apache.flume.lifecycle.LifecycleAware;
import org.apache.flume.lifecycle.LifecycleState;
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
    private static JettyServer server;
    private static ImmutableMap<String, SourceRunner> sourcesRef;
    private static ImmutableMap<String, Channel> channelsRef;
    private static ImmutableMap<String, SinkRunner> sinksRef;
    
    /**
     * Constructor.
     */
    public CygnusApplication() {
        super();
    } // CygnusApplication
    
    /**
     * Constructor.
     * @param components
     */
    public CygnusApplication(List<LifecycleAware> components) {
        super(components);
    } // CygnusApplication
    
    @Override
    @Subscribe
    public synchronized void handleConfigurationEvent(MaterializedConfiguration conf) {
        sourcesRef = conf.getSourceRunners();
        channelsRef = conf.getChannels();
        sinksRef = conf.getSinkRunners();
        super.handleConfigurationEvent(conf);
    } // handleConfigurationEvent
   
    /**
     * Main application to be run when this CygnusApplication is invoked. The only differences with the original one
     * are the CygnusApplication is used instead of the Application one, and the Management Interface port option in
     * the command line.
     * @param args
     */
    public static void main(String[] args) {
        try {
            logger = Logger.getLogger(CygnusApplication.class);
            
            Options options = new Options();

            Option option = new Option("n", "name", true, "the name of this agent");
            option.setRequired(true);
            options.addOption(option);

            option = new Option("f", "conf-file", true, "specify a conf file");
            option.setRequired(true);
            options.addOption(option);

            option = new Option(null, "no-reload-conf", false, "do not reload " + "conf file if changed");
            options.addOption(option);

            option = new Option("h", "help", false, "display help text");
            options.addOption(option);
            
            option = new Option("p", "mgmt-if-port", true, "the management interface port");
            option.setRequired(false);
            options.addOption(option);

            CommandLineParser parser = new GnuParser();
            CommandLine commandLine = parser.parse(options, args);

            File configurationFile = new File(commandLine.getOptionValue('f'));
            String agentName = commandLine.getOptionValue('n');
            boolean reload = !commandLine.hasOption("no-reload-conf");

            if (commandLine.hasOption('h')) {
                new HelpFormatter().printHelp("cygnus-flume-ng agent", options, true);
                return;
            } // if
            
            int mgmtIfPort = 8081; // default value
            
            if (commandLine.hasOption('p')) {
                mgmtIfPort = new Integer(commandLine.getOptionValue('p'));
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
                logger.debug("no-reload-conf was not set, thus the configuration file will be polled each 30 seconds");
                EventBus eventBus = new EventBus(agentName + "-event-bus");
                PollingPropertiesFileConfigurationProvider configurationProvider =
                        new PollingPropertiesFileConfigurationProvider(agentName, configurationFile, eventBus, 30);
                components.add(configurationProvider);
                application = new CygnusApplication(components);
                eventBus.register(application);
            } else {
                logger.debug("no-reload-conf was set, thus the configuration file will only be read this time");
                PropertiesFileConfigurationProvider configurationProvider =
                        new PropertiesFileConfigurationProvider(agentName, configurationFile);
                application = new CygnusApplication();
                application.handleConfigurationEvent(configurationProvider.getConfiguration());
            } // if else
            
            // start the Cygnus application, including the management interface
            logger.info("Starting a Jetty server listening on port " + mgmtIfPort + " (Management Interface)");
            server = new JettyServer(mgmtIfPort, new ManagementInterface(sourcesRef, channelsRef, sinksRef));
            server.start();
            logger.info("Starting Cygnus application");
            application.start();

            // create a hook "listening" for shutdown interrupts (runtime.exit(int), crtl+c, etc)
            Runtime.getRuntime().addShutdownHook(new AgentShutdownHook("agent-shutdown-hook"));
        } catch (IllegalArgumentException e) {
            logger.error("A fatal error occurred while running. Exception follows.", e);
        } catch (ParseException e) {
            logger.error("A fatal error occurred while running. Exception follows.", e);
        } // try catch
         // try catch
    } // main
    
    /**
     * Implements a thread that starts when the Cygnus applications exits (runtime.exit(int), ctrl+c, etc).
     */
    private static class AgentShutdownHook extends Thread {
        
        /**
         * Constructor.
         * @param name
         */
        public AgentShutdownHook(String name) {
            super(name);
        } // AgentShutdownHook
        
        @Override
        public void run() {
            try {
                System.out.println("Starting an ordered shutdown of Cygnus");
                
                // stop the sources
                System.out.println("Stopping sources");
                stopSources();
                
                // wait until the channels are empty; if at least one of them has a single event, Cygnus cannot stop
                while (true) {
                    Iterator it = channelsRef.keySet().iterator();
                    boolean emptyChannels = true;

                    while (it.hasNext()) {
                        String channelName = (String) it.next();
                        Channel channel = channelsRef.get(channelName);
                        CygnusChannel cygnusChannel;

                        if (channel instanceof CygnusMemoryChannel) {
                            cygnusChannel = (CygnusMemoryChannel) channel;
                        } else if (channel instanceof CygnusFileChannel) {
                            cygnusChannel = (CygnusFileChannel) channel;
                        } else {
                            continue;
                        } // if else
                        
                        int numEvents = cygnusChannel.getNumEvents();
                        
                        if (numEvents != 0) {
                            System.out.println("There are " + numEvents + " events within " + channelName
                                    + ", Cygnus cannnot shutdown yet");
                            emptyChannels = false;
                            break;
                        } // if
                    } // while

                    if (emptyChannels) {
                        System.out.println("All the channels are empty");
                        break;
                    } else {
                        System.out.println("Waiting 5 seconds");
                        Thread.sleep(5000);
                    } // if else
                } // while

                // stop the channels
                System.out.println("Stopping channels");
                stopChannels();
                
                // stop the sinks
                System.out.println("Stopping sinks");
                stopSinks();
            } catch (InterruptedException e) {
                System.err.println("There was some problem while shutting down Cygnus. Details=" + e.getMessage());
            } // try catch
        } // run
        
        /**
         * Stops the sources.
         */
        private void stopSources() {
            for (String sourceName : sourcesRef.keySet()) {
                SourceRunner source = sourcesRef.get(sourceName);
                LifecycleState state = source.getLifecycleState();
                System.out.println("Stopping " + sourceName + " (lyfecycle state=" + state.toString() + ")");
                source.stop();
            } // for
        } // stopSources
        
        /**
         * Stops the channels.
         */
        private void stopChannels() {
            for (String channelName : channelsRef.keySet()) {
                Channel channel = channelsRef.get(channelName);
                LifecycleState state = channel.getLifecycleState();
                System.out.println("Stopping " + channelName + " (lyfecycle state=" + state.toString() + ")");
                channel.stop();
            } // for
        } // stopChannels
        
        /**
         * Stops the sinks.
         */
        private void stopSinks() {
            for (String sinkName : sinksRef.keySet()) {
                SinkRunner sink = sinksRef.get(sinkName);
                LifecycleState state = sink.getLifecycleState();
                System.out.println("Stopping " + sinkName + " (lyfecycle state=" + state.toString() + ")");
                sink.stop();
            } // for
        } // stopSinks
                
    } // AgentShutdownHook
    
} // CygnusApplication
