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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * 
 * ServerCommandLine.
 * 
 * @author sortega
 */
public class ServerCommandLine {
    
    private static final String SHORT_CONFIG_FILE = "c";
    private static final String LONG_CONFIG_FILE = "config";
    private final GnuParser parser;
    private final Options options;
    private CommandLine commandLine;

    /**
     * Constructor.
     */
    public ServerCommandLine() {
        this.parser = new GnuParser();
        this.options = new Options().addOption(SHORT_CONFIG_FILE, LONG_CONFIG_FILE, true, "Configuration file");
    } // ServerCommandLine

    /**
     * Parses the command line.
     * 
     * @param args
     * @throws ParseException
     */
    public void parse(String[] args) throws ParseException {
        this.commandLine = this.parser.parse(this.options, args.clone());
    } // parse

    /**
     * Prints the usage of the application.
     */
    public void printUsage() {
        new HelpFormatter().printHelp("injection-server", this.options);
    } // printUsage

    /**
     * Checks if command line has a configuration file or not.
     * 
     * @return True if the command line has a configuration file. False otherwise.
     */
    public boolean hasConfigFile() {
        return this.commandLine.hasOption(SHORT_CONFIG_FILE);
    } // hasConfigFile

    /**
     * Gets the configuration file parameter from the command line.
     * 
     * @return The configuration file name within the command line.
     */
    public String getConfigFile() {
        return this.commandLine.getOptionValue(SHORT_CONFIG_FILE);
    } // getConfigFile
    
} // SeverCommandLine
