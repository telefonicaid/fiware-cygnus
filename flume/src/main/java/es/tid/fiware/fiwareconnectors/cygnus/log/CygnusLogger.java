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

package es.tid.fiware.fiwareconnectors.cygnus.log;

import org.apache.log4j.Logger;

/**
 * Cygnus logger basically extends log4j logger. It adds, nevertheless, certain useful features such as:
 *    - Printing the name of the component trying to log.
 *    - Checking for the logging system availability.
 *    - Ordering the shutdown of Cygnus if the above check fails.
 * 
 * @author frb
 */
public class CygnusLogger extends Logger {
    
    private final boolean shutdown;
    
    /**
     * Constructor.
     * @param name A name for the logger.
     * @param shutdown True if the whole application must exit when an error with the logging system appears. Otherwise
     * false.
     */
    public CygnusLogger(String name, boolean shutdown) {
        super(name);
        this.shutdown = shutdown;
    } // CygnusLogger
    
    /**
     * Traces a message with INFO level, belonging a component.
     * @param componentName Component commanding the log
     * @param message Message to be logged
     */
    public void info(String componentName, String message) {
        try {
            info("[" + componentName + "] " + message);
        } catch (Exception e) {
            if (shutdown) {
                traceAndExit(e);
            } // if
        } // catch
    } // info
    
    /**
     * Traces a message with ERROR level, belonging a component.
     * @param componentName Component commanding the log
     * @param message Message to be logged
     */
    public void error(String componentName, String message) {
        try {
            error("[" + componentName + "] " + message);
        } catch (Exception e) {
            if (shutdown) {
                traceAndExit(e);
            } // if
        } // catch
    } // error
    
    /**
     * Traces a message with FATAL level, belonging a component.
     * @param componentName Component commanding the log
     * @param message Message to be logged
     */
    public void fatal(String componentName, String message) {
        try {
            fatal("[" + componentName + "] " + message);
        } catch (Exception e) {
            if (shutdown) {
                traceAndExit(e);
            } // if
        } // catch
    } // fatal
    
    /**
     * Traces a message with WARN level, belonging a component.
     * @param componentName Component commanding the log
     * @param message Message to be logged
     */
    public void warn(String componentName, String message) {
        try {
            warn("[" + componentName + "] " + message);
        } catch (Exception e) {
            if (shutdown) {
                traceAndExit(e);
            } // if
        } // catch
    } // warn
    
    /**
     * Traces a message with DEBUG level, belonging a component.
     * @param componentName Component commanding the log
     * @param message Message to be logged
     */
    public void debug(String componentName, String message) {
        try {
            debug("[" + componentName + "] " + message);
        } catch (Exception e) {
            if (shutdown) {
                traceAndExit(e);
            } // if
        } // catch
    } // debug
    
    private void traceAndExit(Exception e) {
        System.err.println("A problem with the logging system was found... shutting down Cygnus right now!"
                + " Details=" + e.getMessage());
        System.exit(-1);
    } // traceAndExit
    
} // CygnusLogger
