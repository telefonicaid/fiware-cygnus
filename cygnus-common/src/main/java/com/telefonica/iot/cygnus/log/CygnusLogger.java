/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
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
package com.telefonica.iot.cygnus.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Wrapper class for log4j Logger class. Reference:
 * https://books.google.es/books?id=hZBimlxiyAcC&lpg=PA162&ots=QgJm9Y3WZ9&dq=log4j%20decorator
 *    &hl=es&pg=PA163#v=onepage&q=log4j%20decorator&f=false
 * 
 * @author frb
 */
public class CygnusLogger {
    
    private static String fqcn = CygnusLogger.class.getName();
    private Logger logger;
    
    /**
     * Constructor.
     * @param name
     */
    public CygnusLogger(String name) {
        this.logger = Logger.getLogger(name);
    } // CygnusLogger
    
    /**
     * Constructor.
     * @param clazz
     */
    public CygnusLogger(Class clazz) {
        this(clazz.getName());
    } // CygnusLogger
    
    /**
     * Traces a message with FATAL level.
     * @param msg
     */
    public void fatal(Object msg) {
        try {
            logger.log(fqcn, Level.FATAL, msg, null);
        } catch (Exception e) {
            traceAndExit(e);
        } // try catch
    } // fatal
    
    /**
     * Traces a message with ERROR level.
     * @param msg
     */
    public void error(Object msg) {
        try {
            logger.log(fqcn, Level.ERROR, msg, null);
        } catch (Exception e) {
            traceAndExit(e);
        } // try catch
    } // error
    
    /**
     * Traces a message with DEBUG level.
     * @param msg
     */
    public void debug(Object msg) {
        try {
            logger.log(fqcn, Level.DEBUG, msg, null);
        } catch (Exception e) {
            traceAndExit(e);
        } // try catch
    } // debug
    
    /**
     * Traces a message with INFO level.
     * @param msg
     */
    public void info(Object msg) {
        try {
            logger.log(fqcn, Level.INFO, msg, null);
        } catch (Exception e) {
            traceAndExit(e);
        } // try catch
    } // info
    
    /**
     * Traces a message with WARN level.
     * @param msg
     */
    public void warn(Object msg) {
        try {
            logger.log(fqcn, Level.WARN, msg, null);
        } catch (Exception e) {
            traceAndExit(e);
        } // try catch
    } // warn
    
    
    /**
     * Traces a message with TRACE level.
     * @param msg
     */
    public void trace(Object msg) {
        try {
            logger.log(fqcn, Level.TRACE, msg, null);
        } catch (Exception e) {
            traceAndExit(e);
        } // try catch
    } // trace
    
    
    private void traceAndExit(Exception e) {
        System.err.println("A problem with the logging system was found... shutting down Cygnus right now!"
                + " Details=" + e.getMessage());
        System.exit(-1);
    } // traceAndExit
    
} // CygnusLogger
