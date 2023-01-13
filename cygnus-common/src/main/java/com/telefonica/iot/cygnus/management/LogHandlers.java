/**
 * Copyright 2017 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.management;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.PatternLayout.Builder;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Configuration;

/**
 *
 * @author frb
 */
public final class LogHandlers {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(LogHandlers.class);
    
    /**
     * Constructor. Private since utility classes should not have a public or default constructor.
     */
    private LogHandlers() {
    } // LogHandlers
    
    public static void getLoggers(HttpServletRequest request, HttpServletResponse response, String configurationPath)
        throws IOException {
        response.setContentType("application/json; charset=utf-8");
        String transientVar = request.getParameter("transient");
        String loggerName = request.getParameter("name");
        boolean allLoggers = true;

        if (loggerName != null) {
            allLoggers = false;
        } // if

        String pathToFile = configurationPath + "/log4j.properties";
        File file = new File(pathToFile);
        String param = "flume.root.logger";

        if ((transientVar == null) || (transientVar.equals("true"))) {
            String loggersJson = "[";
            boolean firstTime = true;
            LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
            Map<String,LoggerConfig> loggers = logContext.getConfiguration().getLoggers();

            if (allLoggers) {
                Iterator<Map.Entry<String, LoggerConfig>> iterator = loggers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = iterator.next();
                    String loggName = entry.getKey().toString();
                    Logger logger = LogManager.getLogger(loggName);
                    Level level = logger.getLevel();

                    if (!firstTime) {
                        loggersJson += ",";
                    }  // if

                    if (level != null) {
                        loggersJson += "{\"name\":\"" + loggName + "\",\"level\":\"" + level.toString() + "\"}";
                    } else {
                        loggersJson += "{\"name\":\"" + loggName + "\",\"level\":null}";
                    }
                    firstTime = false;
                } // while

                loggersJson += "]";
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("{\"success\":\"true\",\"loggers\":" + loggersJson + "}");
                LOGGER.debug("Log4j loggers successfully obtained");
            } else {
                boolean loggerFound = false;

                Iterator<Map.Entry<String, LoggerConfig>> iterator = loggers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = iterator.next();
                    String loggName = entry.getKey().toString();
                    Logger log = LogManager.getLogger(loggName);
                    if (log.getName().equals(loggerName)) {
                        loggersJson += "{\"name\":\"" + log.getName() + "\"}";
                        loggerFound = true;
                    } // if

                } // while

                loggersJson += "]";

                if (loggerFound) {
                    LogManager.getLogger(loggerName);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println("{\"success\":\"true\",\"logger\":" + loggersJson + "}");
                    LOGGER.debug("Log4j logger successfully obtained");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\",\"result\":\"Logger name not found\"}");
                    LOGGER.debug("Logger name not found");
                } // try catch
            } // if else
        } else if (transientVar.equals("false")) {
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(fileInputStream);
                String loggersJson = "[";
                ArrayList<String> loggerNames = ManagementInterfaceUtils.getLoggersFromProperties(properties);

                if (allLoggers) {

                    for (String name : loggerNames) {
                        String propertyName = "log4j.logger." + name;
                        String level = properties.getProperty(propertyName);
                        loggersJson += "{\"name\":\"" + name + "\",\"level\":\""
                            + level + "\"}";

                        if (!(loggerNames.get(loggerNames.size() - 1).equals(name))) {
                            loggersJson += ",";
                        } // if
                    } // for

                    loggersJson += "]";
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println("{\"success\":\"true\",\"loggers\":\"" + loggersJson + "\"}");
                    LOGGER.debug("Logger list: " + loggersJson);
                } else {
                    boolean loggerFound = false;

                    for (String name : loggerNames) {
                        if (name.equals(loggerName)) {
                            String propertyName = "log4j.logger." + name;
                            String level = properties.getProperty(propertyName);
                            loggersJson += "{\"name\":\"" + loggerName + "\",\"level\":\""
                                + level + "\"}]";
                            loggerFound = true;
                        } // if
                    } // for

                    if (loggerFound) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().println("{\"success\":\"true\",\"logger\":\"" + loggersJson + "\"}");
                        LOGGER.debug("Logger list: " + loggersJson);
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\",\"result\":\"Logger name not found\"}");
                        LOGGER.debug("Logger name not found");
                    } // if else
                } // if else
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"success\":\"false\","
                        + "\"result\":\"File not found in the path received\"}");
                LOGGER.debug("File not found in the path received");
            } // if else
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\",\"result\":\"Invalid 'transient' parameter found\"}");
            LOGGER.debug("Invalid 'transient' parameter found");
        } // if else if
    } // getLoggers
    
    public static void postLoggers(HttpServletRequest request, HttpServletResponse response, String configurationPath)
        throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        // read the new rule wanted to be added
        String jsonStr;
        
        try (BufferedReader reader = request.getReader()) {
            jsonStr = "";
            String line;
            
            while ((line = reader.readLine()) != null) {
                jsonStr += line;
            } // while
        } // try
        
        if (!jsonStr.isEmpty()) {
            JsonObject jsonLogger = new JsonParser().parse(jsonStr).getAsJsonObject();
            
            try {
                JsonObject logger = jsonLogger.get("logger").getAsJsonObject();
                String name = logger.get("name").getAsString();
                String level = logger.get("level").getAsString();

                try {
                    CommonConstants.LoggingLevels.valueOf(level);
                    String isTransient = request.getParameter("transient");
                    String pathToFile = configurationPath + "/log4j.properties";
                    File file = new File(pathToFile);

                    if ((isTransient == null) || (isTransient.equals("true"))) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\","
                            + "\"result\":\"POST appenders in transient mode is not implemented\"}");
                        LOGGER.debug("POST appenders in transient mode is not implemented");

                    } else if (isTransient.equals("false")) {

                        if (file.exists()) {
                            FileInputStream fileInputStream = new FileInputStream(file);
                            Properties properties = new Properties();
                            properties.load(fileInputStream);
                            Map<String, String> descriptions = ManagementInterfaceUtils.readLogDescriptions(file);
                            ArrayList<String> loggerNames =
                                    ManagementInterfaceUtils.getLoggersFromProperties(properties);
                            boolean loggerFound = false;

                            for (String loggerName: loggerNames) {

                                if (name.equals(loggerName)) {
                                    loggerFound = true;
                                } // if

                            } // for

                            if (loggerFound) {
                                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                response.getWriter().println("{\"success\":\"false\","
                                    + "\"result\":\"Logger '" + name + "' already exist\"}");
                                LOGGER.debug("Logger '" + name + "' already exist");
                            } else {
                                String propertyName = "log4j.logger." + name;
                                properties.put(propertyName, level);
                                ManagementInterfaceUtils.orderedLogPrinting(properties, descriptions, file);
                                response.setStatus(HttpServletResponse.SC_OK);
                                response.getWriter().println("{\"success\":\"true\","
                                    + "\"result\":\"Logger '" + name + "' put\"}");
                                LOGGER.debug("Logger '" + name + "' put");
                            } // if else

                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.getWriter().println("{\"success\":\"false\","
                                    + "\"result\":\"File not found in the path received\"}");
                            LOGGER.debug("File not found in the path received");
                        } // if else

                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\","
                                + "\"result\":{\"Invalid 'transient' parameter found\"}}");
                        LOGGER.debug("Invalid 'transient' parameter found");
                    } // if else if

                } catch (Exception e)  {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\",\"result\":{\"Invalid logging level\"}}");
                    LOGGER.debug("Invalid logging level");
                } // try catch
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"success\":\"false\",\"result\":\"Invalid input JSON\"}");
                LOGGER.debug("Invalid input JSON");
            } // try catch
            
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\",\"result\":\"Missing input JSON\"}");
            LOGGER.debug("Missing input JSON");
        } // if else
    } // postLoggers

    public static void setLevel(Logger logger, Level level) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        LoggerConfig loggerConfig = config.getLoggerConfig(logger.getName());
        LoggerConfig specificConfig = loggerConfig;

        // We need a specific configuration for this logger,
        // otherwise we would change the level of all other loggers
        // having the original configuration as parent as well

        if (!loggerConfig.getName().equals(logger.getName())) {
            specificConfig = new LoggerConfig(logger.getName(), level, true);
            specificConfig.setParent(loggerConfig);
            config.addLogger(logger.getName(), specificConfig);
        }
        specificConfig.setLevel(level);
        ctx.updateLoggers();
    }

    public static void putLoggers(HttpServletRequest request, HttpServletResponse response, String configurationPath)
        throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        // read the new rule wanted to be added
        String jsonStr;
        
        try (BufferedReader reader = request.getReader()) {
            jsonStr = "";
            String line;
            
            while ((line = reader.readLine()) != null) {
                jsonStr += line;
            } // while
        } // try
        
        if (!jsonStr.isEmpty()) {
            JsonObject jsonLogger = new JsonParser().parse(jsonStr).getAsJsonObject();
            
            try {
                JsonObject logger = jsonLogger.get("logger").getAsJsonObject();
                String name = logger.get("name").getAsString();
                String level = logger.get("level").getAsString();

                try {
                    CommonConstants.LoggingLevels.valueOf(level);
                    String isTransient = request.getParameter("transient");
                    String pathToFile = configurationPath + "/log4j.properties";
                    File file = new File(pathToFile);

                    if ((isTransient == null) || (isTransient.equals("true"))) {
                        LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
                        Map<String,LoggerConfig> loggers = logContext.getConfiguration().getLoggers();
                        boolean loggerFound = false;

                        Iterator<Map.Entry<String, LoggerConfig>> iterator = loggers.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry entry = iterator.next();
                            String loggerName = entry.getKey().toString();
                            Logger currentLogger = LogManager.getLogger(loggerName);

                            if (loggerName.equals(name)) {
                                loggerFound = true;
                            } // if
                        } // while

                        if (loggerFound) {
                            Logger loggerUpdated = LogManager.getLogger(name);

                            setLevel(loggerUpdated, Level.toLevel(level));
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().println("{\"success\":\"true\","
                                + "\"result\":\"Logger '" + name + "' updated succesfully\"}");
                            LOGGER.debug("Logger '" + name + "' updated succesfully");
                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.getWriter().println("{\"success\":\"false\","
                                + "\"result\":\"Loggers addition is not implemented\"}");
                            LOGGER.debug("Loggers addition is not implemented");
                        } // if else

                    } else if (isTransient.equals("false")) {
                        if (file.exists()) {
                            FileInputStream fileInputStream = new FileInputStream(file);
                            Properties properties = new Properties();
                            properties.load(fileInputStream);
                            boolean loggerFound = false;
                            String prop = "log4j.logger." + name;

                            if (properties.containsKey(prop)) {
                                System.out.println(properties.containsKey(prop));
                                loggerFound = true;
                            } // if

                            Map<String, String> descriptions = ManagementInterfaceUtils.readLogDescriptions(file);
                            String propertyName = "log4j.logger." + name;
                            properties.put(propertyName, level);
                            ManagementInterfaceUtils.orderedLogPrinting(properties, descriptions, file);

                            if (loggerFound) {
                                response.setStatus(HttpServletResponse.SC_OK);
                                response.getWriter().println("{\"success\":\"true\","
                                    + "\"result\":\"Logger '" + name + "' updated succesfully\"}");
                                LOGGER.debug("Logger '" + name + "' updated succesfully");
                            } else {
                                response.setStatus(HttpServletResponse.SC_OK);
                                response.getWriter().println("{\"success\":\"true\","
                                    + "\"result\":\"Logger '" + name + "' put\"}");
                                LOGGER.debug("Logger '" + name + "' put.");
                            } // if else
                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.getWriter().println("{\"success\":\"false\","
                                    + "\"result\":\"File not found in the path received\"}");
                            LOGGER.debug("File not found in the path received");
                        } // if else
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\","
                                + "\"result\":\"Invalid 'transient' parameter found\"}");
                        LOGGER.debug("Invalid 'transient' parameter found");
                    } // if else if
                } catch (Exception e)  {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\","
                        + "\"result\":\"Invalid logging level\"}");
                    LOGGER.debug("Invalid logging level");
                } // try catch
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"success\":\"false\","
                        + "\"result\":\"Invalid input JSON\"}");
                LOGGER.debug("Invalid input JSON");
            } // try catch
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\":\"Missing input JSON\"}");
            LOGGER.debug("Missing input JSON");
        } // if else
    } // putLoggers

    public static void deleteLoggers(HttpServletRequest request, HttpServletResponse response, String configurationPath)
        throws IOException {
        response.setContentType("application/json; charset=utf-8");

        String isTransient = request.getParameter("transient");
        String loggerName = request.getParameter("name");
        boolean allLoggers = true;

        if (loggerName != null) {
            allLoggers = false;
        } // if

        String pathToFile = configurationPath + "/log4j.properties";
        File file = new File(pathToFile);

        if ((isTransient == null) || (isTransient.equals("true"))) {
            LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
            Map<String,LoggerConfig> loggers = logContext.getConfiguration().getLoggers();

            if (allLoggers) {
                loggers.forEach((loggName, config) -> {
                    setLevel(LogManager.getLogger(loggName), Level.OFF);
                }); // while

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("{\"success\":\"true\",\"result\":\"Loggers removed succesfully\"}");
                LOGGER.debug("Log4j loggers removed succesfully");
                
            } else {

                ArrayList<Logger> loggerNames = new ArrayList<>();

                loggers.forEach((loggName, config) -> {
                    loggerNames.add(LogManager.getLogger(loggName));
                }); // while

                if (loggerNames.contains(LogManager.getLogger(loggerName))) {
                    setLevel(LogManager.getLogger(loggerName),Level.OFF);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println("{\"success\":\"true\",\"result\":\"Logger '" + loggerName
                            + "' removed succesfully\"}");
                    LOGGER.debug("Log4j logger removed succesfully");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\",\"result\":\"Logger name not found\"}");
                    LOGGER.debug("Logger name not found");
                } // if else
                
            } // if else if
            
        } else if (isTransient.equals("false")) {
            
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(fileInputStream);
                Map<String, String> descriptions = ManagementInterfaceUtils.readLogDescriptions(file);
                ArrayList<String> loggerNames = ManagementInterfaceUtils.getLoggersFromProperties(properties);
                boolean hasAppenders = false;

                if (allLoggers) {
                        
                    for (String property: properties.stringPropertyNames()) {
                            
                        if (property.startsWith("log4j.logger")) {
                            properties.remove(property);
                            hasAppenders = true;
                        } // if
                                                    
                    } // for

                    ManagementInterfaceUtils.orderedLogPrinting(properties, descriptions, file);
                    
                    if (hasAppenders) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().println("{\"success\":\"true\",\"result\":\"Loggers removed "
                                + "succesfully\"}");
                        LOGGER.debug("Loggers removed succesfully");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\",\"result\":\"No log4j loggers found\"}");
                        LOGGER.debug("No log4j loggers found");
                    } // if else
                    
                } else {
                    boolean loggerFound = false;

                    for (String name : loggerNames) {

                        if (name.equals(loggerName)) {
                            String loggName = "log4j.logger." + name;
                            loggerFound = true;
                            
                            for (String property: properties.stringPropertyNames()) {
                            
                                if (property.startsWith(loggName)) {
                                    properties.remove(property);
                                } // if
                            
                            } // for
                   
                        } // if

                    } // for

                    if (loggerFound) {
                        ManagementInterfaceUtils.orderedLogPrinting(properties, descriptions, file);
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().println("{\"success\":\"true\",\"result\":\" Logger '" + loggerName
                                + "' removed succesfully\"}");
                        LOGGER.debug("Logger '" + loggerName + "' removed succesfully");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\",\"result\":\"Logger name not found\"}");
                        LOGGER.debug("Logger name not found");
                    } // if else

                } // if else

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"success\":\"false\","
                        + "\"result\":\"File not found in the path received\"}");
                LOGGER.debug("File not found in the path received");
            } // if else
            
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\":\"Invalid 'transient' parameter found\"}");
            LOGGER.debug("Invalid 'transient' parameter found");
        } // if else if
    } // deleteLoggers
    
    public static void getAppenders(HttpServletRequest request, HttpServletResponse response, String configurationPath)
        throws IOException {
        response.setContentType("application/json; charset=utf-8");
        String transientVar = request.getParameter("transient");
        String appenderName = request.getParameter("name");
        boolean allAppenders = true;

        if (appenderName != null) {
            allAppenders = false;
        } // if

        String pathToFile = configurationPath + "/log4j.properties";
        File file = new File(pathToFile);

        if ((transientVar == null) || (transientVar.equals("true"))) {
            String appendersJson;

            if (allAppenders) {
                LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
                Map<String,Appender> appendersMap = logContext.getConfiguration().getAppenders();
                Enumeration<Appender> appenders = Collections.enumeration(appendersMap.values());
                appendersJson = ManagementInterfaceUtils.getStringAppender(appenders);

                if (appendersJson.equals("[]")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\",\"result\":\"No log4j appenders found\"}");
                    LOGGER.debug("No log4j appenders found");
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println("{\"success\":\"true\",\"appender\":" + appendersJson + "}");
                    LOGGER.debug("Log4j appenders successfully obtained");
                } // if else

            } else {

                try {
                    LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
                    Appender app = logContext.getConfiguration().getAppender(appenderName);
                    String name = app.getName();
                    PatternLayout layout = (PatternLayout) app.getLayout();
                    String layoutStr = layout.getConversionPattern();
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println("{\"success\":\"true\",\"appender\":[{\"name\":\"" + name
                            + "\",\"layout\":\"" + layoutStr + "\",\"active\":\"true\"}]}");
                    LOGGER.debug("Log4j appenders successfully obtained");
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\",\"result\":\"Appender name not found\"}");
                    LOGGER.debug("Appender name not found");
                } // try catch
            } // if else
        } else if (transientVar.equals("false")) {
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(fileInputStream);
                String param = "flume.root.logger";
                String rootProperty = properties.getProperty(param);
                String[] rootLogger = rootProperty.split(",");
                String active = rootLogger[1];
                String appenderJson = "[";
                ArrayList<String> appenderNames = ManagementInterfaceUtils.getAppendersFromProperties(properties);

                if (allAppenders) {

                    for (String name : appenderNames) {
                        boolean isActive = false;

                        if (name.equals(active)) {
                            isActive = true;
                        } // if

                        String layoutName = "log4j.appender." + name + ".layout."
                            + "ConversionPattern";
                        String layout = properties.getProperty(layoutName);
                        appenderJson += "{\"name\":\"" + name + "\",\"layout\":\""
                            + layout + "\",\"active\":\"" + Boolean.toString(isActive) + "\"}";

                        if (!(appenderNames.get(appenderNames.size() - 1).equals(name))) {
                            appenderJson += ", ";
                        } // if
                    } // for

                    appenderJson += "]";

                    if (appenderJson.equals("[]")) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\",\"result\":\"No log4j appenders found\"}");
                        LOGGER.debug("No log4j appenders found");
                    } else {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().println("{\"success\":\"true\",\"appenders\":" + appenderJson + "}");
                        LOGGER.debug("Appender list: " + appenderJson);
                    } // if else
                } else {
                    boolean appenderFound = false;

                    for (String name : appenderNames) {
                        if (name.equals(appenderName)) {
                            String layoutName = "log4j.appender." + name + ".layout."
                                + "ConversionPattern";
                            String layout = properties.getProperty(layoutName);
                            appenderJson += "{\"name\":\"" + name + "\",\"layout\":\""
                                + layout + "\",\"active\":\"";
                            if (name.equals(active)) {
                                appenderJson += "true\"}";
                            } else {
                                appenderJson += "false\"}";
                            }
                            appenderFound = true;
                        } // if
                    } // for

                    if (appenderFound) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().println("{\"success\":\"true\",\"appender\":\"" + appenderJson + "\"}");
                        LOGGER.debug("Appender list: " + appenderJson);
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\",\"result\":\"Appender name not found\"}");
                        LOGGER.debug("Appender name not found");
                    } // if else
                } // if else
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"success\":\"false\","
                        + "\"result\":\"File not found in the path received\"}");
                LOGGER.debug("File not found in the path received");
            } // if else
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\":\"Invalid 'transient' parameter found\"}");
            LOGGER.debug("Invalid 'transient' parameter found");
        } // if else if
    } // getAppenders
    
    public static void postAppenders(HttpServletRequest request, HttpServletResponse response, String configurationPath)
        throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        String jsonStr;
        
        try (
            // read the new rule wanted to be added
            BufferedReader reader = request.getReader()) {
            jsonStr = "";
            String line;
            
            while ((line = reader.readLine()) != null) {
                jsonStr += line;
            } // while
        } // try

        if (!jsonStr.isEmpty()) {
            JsonObject jsonAppender = new JsonParser().parse(jsonStr).getAsJsonObject();
            try {
                JsonObject appender = jsonAppender.get("appender").getAsJsonObject();
                String name = appender.get("name").getAsString();
                JsonObject layout = jsonAppender.get("pattern").getAsJsonObject();
                String pattern = layout.get("ConversionPattern").getAsString();
                String isTransient = request.getParameter("transient");
                String pathToFile = configurationPath + "/log4j.properties";
                File file = new File(pathToFile);

                if ((isTransient == null) || (isTransient.equals("true"))) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\","
                        + "\"result\":\"POST appenders in transient mode is not implemented\"}");
                    LOGGER.debug("POST appenders in transient mode is not implemented");

                } else if (isTransient.equals("false")) {

                    if (file.exists()) {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        Properties properties = new Properties();
                        properties.load(fileInputStream);
                        String classs = appender.get("class").getAsString();
                        String layoutStr = layout.get("layout").getAsString();
                        Map<String, String> descriptions = ManagementInterfaceUtils.readLogDescriptions(file);
                        boolean appenderFound = false;
                        ArrayList<String> appenderNames =
                                ManagementInterfaceUtils.getAppendersFromProperties(properties);

                        for (String app : appenderNames) {
                            if (app.equals(name)) {
                                appenderFound = true;
                            } // if
                        } // for

                        if (appenderFound) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.getWriter().println("{\"success\":\"false\","
                                + "\"result\":\"Appender '" + name + "' already exist\"}");
                            LOGGER.debug("Appender '" + name + "' already exist");
                        } else {
                            String propertyName = "log4j.appender." + name;
                            String propertyLayout = "log4j.appender." + name + ".layout";
                            String propertyPattern = "log4j.appender." + name + ".layout.ConversionPattern";
                            properties.put(propertyName, classs);
                            properties.put(propertyLayout, layoutStr);
                            properties.put(propertyPattern, pattern);
                            String comments;

                            try {
                                comments = jsonAppender.get("comments").getAsString();
                                descriptions.put("log4j.appender." + name , comments);
                            } catch (Exception e) {
                                comments = "# Values for appender '" + name + "' \n";
                                descriptions.put("log4j.appender." + name , comments);
                            } // try catch

                            ManagementInterfaceUtils.orderedLogPrinting(properties, descriptions, file);

                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().println("{\"success\":\"true\","
                                + "\"result\":\"Appender '" + name + "' posted\"}");
                            LOGGER.debug("Appender '" + name + "' posted.");
                        } // if else
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\","
                                + "\"result\":\"File not found in the path received\"}");
                        LOGGER.debug("File not found in the path received");
                    } // if else

                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"result\":\"Invalid 'transient' parameter found\"}");
                    LOGGER.debug("Invalid 'transient' parameter found");
                } // if else if
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"success\":\"false\",\"result\":\"Invalid input JSON\"}");
                LOGGER.debug("Invalid input JSON");
            } // try catch
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\",\"result\":\"Missing input JSON\"}");
            LOGGER.debug("Missing input JSON.");
        } // if else
    } // postAppenders
    
    public static void putAppenders(HttpServletRequest request, HttpServletResponse response, String configurationPath)
        throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        // read the new appender wanted to be added
        String jsonStr;
        
        try (BufferedReader reader = request.getReader()) {
            jsonStr = "";
            String line;
            
            while ((line = reader.readLine()) != null) {
                jsonStr += line;
            } // while
        } // try
        
        if (!jsonStr.isEmpty()) {
            JsonObject jsonAppender = new JsonParser().parse(jsonStr).getAsJsonObject();
            try {
                JsonObject appender = jsonAppender.get("appender").getAsJsonObject();
                String name = appender.get("name").getAsString();
                JsonObject layout = jsonAppender.get("pattern").getAsJsonObject();
                String pattern = layout.get("ConversionPattern").getAsString();
                String isTransient = request.getParameter("transient");
                String pathToFile = configurationPath + "/log4j.properties";
                File file = new File(pathToFile);
                
                if ((isTransient == null) || (isTransient.equals("true"))) {
                    LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
                    Map<String,Appender> currentAppendersMap = logContext.getConfiguration().getAppenders();
                    Enumeration<Appender> currentAppenders = Collections.enumeration(currentAppendersMap.values());
                    boolean appenderFound = false;

                    while (currentAppenders.hasMoreElements()) {
                        Appender currentApp = currentAppenders.nextElement();
                        String appenderName = currentApp.getName();

                        if (appenderName.equals(name)) {
                            appenderFound = true;
                        } // if
                    } // while

                    PatternLayout p = PatternLayout.createDefaultLayout();
                    PatternLayout.Builder pb = p.newBuilder();
                    PatternLayout patternLayout = pb.withPattern(pattern).build();

                    if (appenderFound) {
                        ConsoleAppender newAppender = ConsoleAppender.createDefaultAppenderForLayout(patternLayout);
                        logContext.getConfiguration().addAppender(newAppender);
                        logContext.getRootLogger().addAppender(logContext.getConfiguration().getAppender(newAppender.getName()));
                        logContext.updateLoggers();

                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().println("{\"success\":\"true\","
                            + "\"result\":\"Appender '" + name + "' updated succesfully\"}");
                        LOGGER.debug("Appender '" + name + "' updated succesfully");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\","
                            + "\"result\":\"Appenders addition is not implemented\"}");
                        LOGGER.debug("Appenders addition is not implemented");
                    } // if else

                } else if (isTransient.equals("false")) {

                    if (file.exists()) {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        Properties properties = new Properties();
                        properties.load(fileInputStream);
                        String classs = appender.get("class").getAsString();
                        String layoutStr = layout.get("layout").getAsString();
                        Map<String, String> descriptions = ManagementInterfaceUtils.readLogDescriptions(file);
                        boolean isUpdate = false;

                        if (properties.contains("log4j.appender." + name)) {
                            isUpdate = true;
                        } // if

                        String propertyName = "log4j.appender." + name;
                        String propertyLayout = "log4j.appender." + name + ".layout";
                        String propertyPattern = "log4j.appender." + name + ".layout.ConversionPattern";
                        properties.put(propertyName, classs);
                        properties.put(propertyLayout, layoutStr);
                        properties.put(propertyPattern, pattern);
                        String comments;

                        try {
                            comments = jsonAppender.get("comments").getAsString();
                            descriptions.put("log4j.appender." + name , comments);
                        } catch (Exception e) {
                            comments = "# Values for appender '" + name + "' \n";
                            descriptions.put("log4j.appender." + name , comments);
                        } // try catch

                        ManagementInterfaceUtils.orderedLogPrinting(properties, descriptions, file);

                        if (isUpdate) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().println("{\"success\":\"true\","
                                + "\"result\":\"Appender '" + name + "' succesfully updated\"}");
                            LOGGER.debug("Appender '" + name + "' succesfully updated.");
                        } else {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().println("{\"success\":\"true\","
                                + "\"result\":\"Appender '" + name + "' put\"}");
                            LOGGER.debug("Appender '" + name + "' put.");
                        } // if else

                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\","
                                + "\"result\":\"File not found in the path received\"}");
                        LOGGER.debug("File not found in the path received");
                    } // if else

                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"result\":\"Invalid 'transient' parameter found\"}");
                    LOGGER.debug("Invalid 'transient' parameter found");
                } // if else if
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"success\":\"false\","
                        + "\"result\":\"Invalid input JSON\"}");
                LOGGER.debug("Invalid input JSON");
            } // try catch
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\",\"result\":\"Missing input JSON\"}");
            LOGGER.debug("Missing input JSON");
        } // if else
    } // putAppenders
    
    public static void deleteAppenders(HttpServletRequest request, HttpServletResponse response, String configurationPath)
        throws IOException {
        response.setContentType("application/json; charset=utf-8");

        String isTransient = request.getParameter("transient");
        String appenderName = request.getParameter("name");
        boolean allAppenders = true;

        if (appenderName != null) {
            allAppenders = false;
        } // if

        String pathToFile = configurationPath + "/log4j.properties";
        File file = new File(pathToFile);

        if ((isTransient == null) || (isTransient.equals("true"))) {

            if (allAppenders) {
                LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
                Map<String,Appender> appenders = logContext.getConfiguration().getAppenders();
                Configuration config = logContext.getConfiguration();
                appenders.forEach((name, appender) -> {
                        config.getRootLogger().removeAppender(name);
                });
                logContext.updateLoggers();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("{\"success\":\"true\",\"result\":\"Appenders removed succesfully\"}}");
                LOGGER.debug("Log4j appenders removed succesfully");
            } else {

                try {
                    // Check if appender already exists
                    LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
                    Configuration config = logContext.getConfiguration();
                    config.getRootLogger().removeAppender(appenderName);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println("{\"success\":\"true\",\"result\":\"Appender '" + appenderName
                            + "'removed succesfully\"}");
                    LOGGER.debug("Log4j appender removed succesfully");
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\",\"result\":\"Appender name not found\"}");
                    LOGGER.debug("Appender name not found");
                } // try catch
            } // if else
        } else if (isTransient.equals("false")) {
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(fileInputStream);
                Map<String, String> descriptions = ManagementInterfaceUtils.readLogDescriptions(file);
                ArrayList<String> appenderNames = ManagementInterfaceUtils.getAppendersFromProperties(properties);
                boolean hasAppenders = false;

                if (allAppenders) {
                    for (String property: properties.stringPropertyNames()) {
                        if (property.startsWith("log4j.appender")) {
                            properties.remove(property);
                            hasAppenders = true;
                        } // if
                    } // for

                    ManagementInterfaceUtils.orderedLogPrinting(properties, descriptions, file);
                    
                    if (hasAppenders) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().println("{\"success\":\"true\",\"result\":\"Appenders removed "
                                + "succesfully\"}");
                        LOGGER.debug("Appenders removed succesfully");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\",\"result\":\"No log4j appenders found\"}");
                        LOGGER.debug("No log4j appenders found");
                    } // if else
                } else {
                    boolean appenderFound = false;

                    for (String name : appenderNames) {
                        if (name.equals(appenderName)) {
                            String appName = "log4j.appender." + name;
                            appenderFound = true;
                            
                            for (String property: properties.stringPropertyNames()) {
                                if (property.startsWith(appName)) {
                                    properties.remove(property);
                                } // if
                            } // for
                        } // if
                    } // for

                    if (appenderFound) {
                        ManagementInterfaceUtils.orderedLogPrinting(properties, descriptions, file);
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().println("{\"success\":\"true\",\"result\":\" Appender '" + appenderName
                                + "' removed succesfully\"}");
                        LOGGER.debug("Appender '" + appenderName + "' removed succesfully");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\",\"result\":\"Appender name not found\"}");
                        LOGGER.debug("Appender name not found");
                    } // if else

                } // if else

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"success\":\"false\","
                        + "\"result\":\"File not found in the path received\"}");
                LOGGER.debug("File not found in the path received");
            } // if else
            
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\":{\"Invalid 'transient' parameter found\"}}");
            LOGGER.debug("Invalid 'transient' parameter found");
        } // if else if
    } // deleteAppenders
    
    /**
     * Handles GET /admin/log.
     * @param request
     * @param response
     * @throws IOException
     */
    public static void getLogLevel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        Level level = LogManager.getRootLogger().getLevel();
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{\"level\":\"" + level + "\"}");
        LOGGER.info("Cygnus logging level successfully obtained");
    } // getLogLevel
    
    /**
     * Handles PUT /admin/log.
     * @param request
     * @param response
     * @throws IOException
     */
    public static void putLogLevel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        // get the parameters to be updated
        String logLevel = request.getParameter("level");
        
        try {
            CommonConstants.LoggingLevels.valueOf(logLevel.toUpperCase());


            setLevel(LogManager.getRootLogger(), Level.toLevel(logLevel.toUpperCase()));

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("{\"success\":\"log4j logging level updated to "
                    + logLevel.toUpperCase() + "\" }");
            LOGGER.debug("log4j logging level updated to " + logLevel.toUpperCase());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"error\":\"Invalid log level\"}");
            LOGGER.error("Invalid log level '" + logLevel + "'");
        } // try catch
    } // putLogLevel
    
} // LogHandlers
