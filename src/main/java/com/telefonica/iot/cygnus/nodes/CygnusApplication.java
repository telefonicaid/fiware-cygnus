/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.nodes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.telefonica.iot.cygnus.channels.CygnusChannel;
import com.telefonica.iot.cygnus.channels.CygnusFileChannel;
import com.telefonica.iot.cygnus.channels.CygnusMemoryChannel;
import com.telefonica.iot.cygnus.http.JettyServer;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.management.ManagementInterface;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.apache.flume.lifecycle.LifecycleSupervisor;
import org.apache.flume.node.Application;
import org.apache.flume.node.MaterializedConfiguration;
import org.apache.flume.node.PollingPropertiesFileConfigurationProvider;
import org.apache.flume.node.PropertiesFileConfigurationProvider;

/**
 * CygnusApplication is an extension of the already existing org.apache.flume.node.Application. CygnusApplication
 * is closed in an ordered way, first the sources in order to no not receiving further notifications, then the
 * application waits until the channels are emptied by the sinks, finally the sinks are closed.
 * 
 * Java Reflection has been used in order to access the LifecycleSupervisor supervisor private variable since this
 * object allows to effectively stop the Cygnus agent components (if directly stoped from the components referecnes
 * then the lifecycle supervisor starts them again).
 * 
 * Cygnus agent components references are obtained only once, at handleConfigurationEvent method since it already
 * receives as an argument a MaterializedConfiguration object (if a new MaterializedConfiguration is gotten then new
 * instances of the components are started).
 * 
 * @author frb
 */
public class CygnusApplication extends Application {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(CygnusApplication.class);
    private static JettyServer mgmtIfServer;
    private static ImmutableMap<String, SourceRunner> sourcesRef;
    private static ImmutableMap<String, Channel> channelsRef;
    private static ImmutableMap<String, SinkRunner> sinksRef;
    private static LifecycleSupervisor supervisorRef;
    private static final int CHANNEL_CHECKING_INTERVAL = 5000;
    private static final int YAFS_CHECKING_INTERVAL = 1000;
    private static final int DEF_MGMT_IF_PORT = 8081;
    private static final int DEF_POLLING_INTERVAL = 30;
    
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
        
        try {
            // get a reference to the supervisor, if not possible then Cygnus application cannot start
            getSupervisorRef();
        } catch (NoSuchFieldException e) {
            LOGGER.debug(e.getMessage());
            supervisorRef = null;
        } catch (IllegalAccessException e) {
            LOGGER.debug(e.getMessage());
            supervisorRef = null;
        } // try catch // try catch
    } // CygnusApplication
        
    /**
     * Gets a reference to the private variable "supervisor" within the super class "Application". This is achieved by
     * using Java Reflection.
     */
    private void getSupervisorRef() throws NoSuchFieldException, IllegalAccessException {
        // get a reference to the supervisor object, this will be needed when shutting down Cygnus in a certain order
        Field privateField = Application.class.getDeclaredField("supervisor");
        privateField.setAccessible(true);
        supervisorRef = (LifecycleSupervisor) privateField.get(this);
        privateField.setAccessible(false);
    } // getSupervisorRef
    
    /**
     * Stops and starts all the components when a configuration change event is generated. It also gets a reference to
     * all the Flume components.
     * 
     * @param conf
     */
    @Override
    @Subscribe
    public synchronized void handleConfigurationEvent(MaterializedConfiguration conf) {
        super.handleConfigurationEvent(conf);
        
        // get references to the different elements of the agent, this will be needed when shutting down Cygnus in a
        // certain order
        sourcesRef = conf.getSourceRunners();
        channelsRef = conf.getChannels();
        sinksRef = conf.getSinkRunners();
    } // handleConfigurationEvent

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
                new HelpFormatter().printHelp("cygnus-flume-ng agent", options, true);
                return;
            } // if
            
            int mgmtIfPort = DEF_MGMT_IF_PORT;
            
            if (commandLine.hasOption('p')) {
                mgmtIfPort = new Integer(commandLine.getOptionValue('p'));
            } // if
            
            int pollingInterval = DEF_POLLING_INTERVAL;
            
            if (commandLine.hasOption('t')) {
                pollingInterval = new Integer(commandLine.getOptionValue('t'));
            } // if
            
            // the following is to ensure that by default the agent will fail on startup if the file does not exist
            
            if (!configurationFile.exists()) {
                // if command line invocation, then need to fail fast
                if (System.getProperty(Constants.SYSPROP_CALLED_FROM_SERVICE) == null) {
                    String path = configurationFile.getPath();
                    
                    try {
                        path = configurationFile.getCanonicalPath();
                    } catch (IOException e) {
                        LOGGER.error("Failed to read canonical path for file: " + path + ". Details="
                                + e.getMessage());
                    } // try catch // try catch
                    
                    throw new ParseException("The specified configuration file does not exist: " + path);
                } // if
            } // if
            
            List<LifecycleAware> components = Lists.newArrayList();
            CygnusApplication application;

            if (reload) {
                LOGGER.debug("no-reload-conf was not set, thus the configuration file will be polled each 30 seconds");
                EventBus eventBus = new EventBus(agentName + "-event-bus");
                PollingPropertiesFileConfigurationProvider configurationProvider =
                        new PollingPropertiesFileConfigurationProvider(agentName, configurationFile, eventBus,
                                pollingInterval);
                components.add(configurationProvider);
                application = new CygnusApplication(components);
                eventBus.register(application);
            } else {
                LOGGER.debug("no-reload-conf was set, thus the configuration file will only be read this time");
                PropertiesFileConfigurationProvider configurationProvider =
                        new PropertiesFileConfigurationProvider(agentName, configurationFile);
                application = new CygnusApplication();
                application.handleConfigurationEvent(configurationProvider.getConfiguration());
            } // if else
                        
            // start the Cygnus application, including the management interface
            LOGGER.info("Starting a Jetty server listening on port " + mgmtIfPort + " (Management Interface)");
            mgmtIfServer = new JettyServer(mgmtIfPort, new ManagementInterface(sourcesRef, channelsRef, sinksRef));
            mgmtIfServer.start();
            LOGGER.info("Starting Cygnus application");
            application.start();

            // create a hook "listening" for shutdown interrupts (runtime.exit(int), crtl+c, etc)
            Runtime.getRuntime().addShutdownHook(new AgentShutdownHook("agent-shutdown-hook", supervisorRef));
            
            // start YAFS
            YAFS yafs = new YAFS();
            yafs.start();
        } catch (IllegalArgumentException e) {
            LOGGER.error("A fatal error occurred while running. Exception follows. Details=" + e.getMessage());
        } catch (ParseException e) {
            LOGGER.error("A fatal error occurred while running. Exception follows. Details=" + e.getMessage());
        } // try catch // try catch
    } // main
    
    /**
     * Implements a thread that starts when the Cygnus applications exits (runtime.exit(int), ctrl+c, etc).
     */
    private static class AgentShutdownHook extends Thread {
        
        private final LifecycleSupervisor supervisorRef;

        /**
         * Constructor.
         * @param name
         */
        public AgentShutdownHook(String name, LifecycleSupervisor supervisorRef) {
            super(name);
            this.supervisorRef = supervisorRef;
        } // AgentShutdownHook
        
        @Override
        public void run() {
            if (supervisorRef == null) {
                System.err.println("Cygnus cannot be shutdown in an ordered way since the supervisor variable at "
                        + "super class org.apache.flume.node.Application could not be accessed");
                return;
            } // if
            
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
                        Thread.sleep(CHANNEL_CHECKING_INTERVAL);
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
                supervisorRef.unsupervise(source);
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
                supervisorRef.unsupervise(channel);
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
                supervisorRef.unsupervise(sink);
            } // for
        } // stopSinks
                
    } // AgentShutdownHook
    
    /**
     * Yet Another Flume Supervisor. FIXME: this is a shortcut avoiding to extend the original LifecycleSupervisor
     * class from Apache Flume, which can be be hard to do. Nevertheless, a tech debt issue has been created regarding
     * this: https://github.com/telefonicaid/fiware-cygnus/issues/354
     */
    private static class YAFS extends Thread {
        
        private final Thread[] threadArray;
        
        /**
         * Constructor.
         */
        public YAFS() {
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
            threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        } // YAFS
        
        @Override
        public void run() {
            while (true) {
                for (Thread t: threadArray) {
                    // exit Cygnus if some thread (except for the main one) is found to be not alive or in a terminated
                    // state
                    if (!t.getName().equals("main") && (t.getState() == State.TERMINATED || !t.isAlive())) {
                        System.exit(-1);
                    } // if
                } // for
                
                try {
                    Thread.sleep(YAFS_CHECKING_INTERVAL);
                } catch (InterruptedException ex) {
                    System.exit(-1);
                }
            } // while
        } // run
        
    } // YAFS
    
} // CygnusApplication
