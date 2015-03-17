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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;



/**
 * Extending log4j Logger class is extremely not recommended.
 * 
 * Cygnus logger is a utility class related to log4j logging system. It adds certain useful features such as:
 *    - Printing the name of the component trying to log.
 *    - Checking for the logging system availability.
 *    - Ordering the shutdown (if this behaviour was configured) of Cygnus if the above check fails.
 * 
 * @author frb
 */
public class CygnusLogger implements Logger {
    
    private final Logger logger;
    private final boolean traceAndExit;
    
    /**
     * Constructor.
     * @param clazz FQCN owning the logging facility
     * @param traceAndExit True if the whole application must exit when an error with the logging system appears.
     * Otherwise false.
     */
    public CygnusLogger(Class clazz, boolean traceAndExit) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.traceAndExit = traceAndExit;
    } // CygnusLogger
    
    /**
     * Constructor.
     * @param logger Logging facility
     * @param traceAndExit True if the whole application must exit when an error with the logging system appears.
     * Otherwise false.
     */
    public CygnusLogger(Logger logger, boolean traceAndExit) {
        this.logger = logger;
        this.traceAndExit = traceAndExit;
    } // CygnusLogger
    
    @Override
    public void info(String message) {
        try {
            logger.info(message);
        } catch (Exception e) {
            if (traceAndExit) {
                traceAndExit(e);
            } // if
        } // catch // catch
    } // info
    
    @Override
    public void error(String message) {
        try {
            logger.error(message);
        } catch (Exception e) {
            if (traceAndExit) {
                traceAndExit(e);
            } // if
        } // catch // catch
    } // error
    
    /**
     * Traces a log with FATAL level.
     * @param message
     */
    public void fatal(String message) {
        try {
            logger.error(MarkerFactory.getMarker("FATAL"), message);
        } catch (Exception e) {
            if (traceAndExit) {
                traceAndExit(e);
            } // if
        } // catch // catch
    } // fatal

    @Override
    public void warn(String message) {
        try {
            logger.warn(message);
        } catch (Exception e) {
            if (traceAndExit) {
                traceAndExit(e);
            } // if
        } // catch // catch
    } // warn
 
    @Override
    public void debug(String message) {
        try {
            logger.debug(message);
        } catch (Exception e) {
            if (traceAndExit) {
                traceAndExit(e);
            } // if
        } // catch // catch
    } // debug
    
    private void traceAndExit(Exception e) {
        System.err.println("A problem with the logging system was found... shutting down Cygnus right now!"
                + " Details=" + e.getMessage());
        System.exit(-1);
    } // traceAndExit

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isTraceEnabled() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void trace(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void trace(String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void trace(String string, Object o, Object o1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void trace(String string, Object... os) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void trace(String string, Throwable thrwbl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void trace(Marker marker, String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void trace(Marker marker, String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void trace(Marker marker, String string, Object o, Object o1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void trace(Marker marker, String string, Object... os) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void trace(Marker marker, String string, Throwable thrwbl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDebugEnabled() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void debug(String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void debug(String string, Object o, Object o1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void debug(String string, Object... os) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void debug(String string, Throwable thrwbl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void debug(Marker marker, String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void debug(Marker marker, String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void debug(Marker marker, String string, Object o, Object o1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void debug(Marker marker, String string, Object... os) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void debug(Marker marker, String string, Throwable thrwbl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isInfoEnabled() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void info(String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void info(String string, Object o, Object o1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void info(String string, Object... os) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void info(String string, Throwable thrwbl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void info(Marker marker, String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void info(Marker marker, String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void info(Marker marker, String string, Object o, Object o1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void info(Marker marker, String string, Object... os) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void info(Marker marker, String string, Throwable thrwbl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isWarnEnabled() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void warn(String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void warn(String string, Object... os) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void warn(String string, Object o, Object o1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void warn(String string, Throwable thrwbl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void warn(Marker marker, String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void warn(Marker marker, String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void warn(Marker marker, String string, Object o, Object o1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void warn(Marker marker, String string, Object... os) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void warn(Marker marker, String string, Throwable thrwbl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isErrorEnabled() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void error(String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void error(String string, Object o, Object o1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void error(String string, Object... os) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void error(String string, Throwable thrwbl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void error(Marker marker, String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void error(Marker marker, String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void error(Marker marker, String string, Object o, Object o1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void error(Marker marker, String string, Object... os) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void error(Marker marker, String string, Throwable thrwbl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
   
} // CygnusLogger
