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

package es.tid.fiware.fiwareconnectors.cygnus.log;

import org.apache.log4j.Logger;

/**
 *
 * @author frb
 */
public class CygnusLogger extends Logger {
    
    /**
     * Constructor.
     * @param name
     */
    public CygnusLogger(String name) {
        super(name);
    } // CygnusLogger
    
    /**
     * Traces a message with INFO level, belonging a component.
     * @param componentName
     * @param message
     */
    public void info(String componentName, String message) {
        info("[" + componentName + "] " + message);
    } // info
    
    /**
     * Traces a message with ERROR level, belonging a component.
     * @param componentName
     * @param message
     */
    public void error(String componentName, String message) {
        error("[" + componentName + "] " + message);
    } // error
    
    /**
     * Traces a message with FATAL level, belonging a component.
     * @param componentName
     * @param message
     */
    public void fatal(String componentName, String message) {
        fatal("[" + componentName + "] " + message);
    } // fatal
    
    /**
     * Traces a message with WARN level, belonging a component.
     * @param componentName
     * @param message
     */
    public void warn(String componentName, String message) {
        warn("[" + componentName + "] " + message);
    } // warn
    
    /**
     * Traces a message with DEBUG level, belonging a component.
     * @param componentName
     * @param message
     */
    public void debug(String componentName, String message) {
        debug("[" + componentName + "] " + message);
    } // debug
    
} // CygnusLogger
