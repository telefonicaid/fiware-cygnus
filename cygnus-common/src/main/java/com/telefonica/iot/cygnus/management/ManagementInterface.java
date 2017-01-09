/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.management;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.flume.Channel;
import org.apache.flume.SinkRunner;
import org.apache.flume.SourceRunner;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.json.simple.JSONObject;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.CommonConstants.LoggingLevels;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;
/**
 *
 * @author frb
 */
public class ManagementInterface extends AbstractHandler {

    private static final CygnusLogger LOGGER = new CygnusLogger(ManagementInterface.class);
    private final File configurationFile;
    private String groupingRulesConfFile;
    private String nameMappingsConfFile;
    private final ImmutableMap<String, SourceRunner> sources;
    private final ImmutableMap<String, Channel> channels;
    private final ImmutableMap<String, SinkRunner> sinks;
    private final int apiPort;
    private final int guiPort;
    private final String configurationPath;
    
    /**
     * Constructor.
     * @param configurationPath
     * @param configurationFile
     * @param sources
     * @param channels
     * @param sinks
     * @param apiPort
     * @param guiPort
     */
    public ManagementInterface(String configurationPath, File configurationFile, ImmutableMap<String,
            SourceRunner> sources, ImmutableMap<String, Channel> channels, ImmutableMap<String, SinkRunner> sinks,
            int apiPort, int guiPort) {
        this.configurationFile = configurationFile;

        try {
            this.groupingRulesConfFile = getGroupingRulesConfFile();
        } catch (IOException e) {
            this.groupingRulesConfFile = null;
            LOGGER.error("There was a problem while obtaining the grouping rules configuration file. Details: "
                    + e.getMessage());
        } // try catch
        
        try {
            this.nameMappingsConfFile = getNameMappingsConfFile();
        } catch (Exception e) {
            this.nameMappingsConfFile = null;
            LOGGER.error("There was a problem while obtainin the name mappings configuration file: Details: "
                    + e.getMessage());
        } // try catch
        
        this.sources = sources;
        this.channels = channels;
        this.sinks = sinks;
        this.apiPort = apiPort;
        this.guiPort = guiPort;
        this.configurationPath = configurationPath;
    } // ManagementInterface

    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
        throws IOException, ServletException {
        HttpConnection connection = HttpConnection.getCurrentConnection();

        if (connection != null) {
            Request baseRequest = (request instanceof Request) ? (Request) request : connection.getRequest();
            baseRequest.setHandled(true);
        } // if

        response.setContentType("text/html;charset=utf-8");
        int port = request.getLocalPort();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        LOGGER.info("Management interface request. Method: " + method + ", URI: " + uri);

        if (port == apiPort) {
            switch (method) {
                case "GET":
                    if (uri.equals("/v1/version")) {
                        handleGetVersion(response);
                    } else if (uri.equals("/v1/stats")) {
                        StatsHandlers.get(response, sources, channels, sinks);
                    } else if (uri.equals("/v1/groupingrules")) {
                        GroupingRulesHandlers.get(response, groupingRulesConfFile);
                    } else if (uri.equals("/admin/log")) {
                        handleGetAdminLog(request, response);
                    } else if (uri.equals("/v1/subscriptions")) {
                        SubscriptionsHandlers.get(request, response);
                    } else if (uri.startsWith("/admin/configuration/agent")) {
                        handleGetAdminConfigurationAgent(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/agent")) {
                        handleGetAdminConfigurationAgent(request, response, true);
                    } else if (uri.startsWith("/admin/configuration/instance")) {
                        handleGetAdminConfigurationInstance(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/instance")) {
                        handleGetAdminConfigurationInstance(request, response, true);
                    } else if (uri.startsWith("/v1/admin/log/loggers")) {
                        handleGetAdminLogLoggers(request, response);
                    } else if (uri.startsWith("/v1/admin/log/appenders")) {
                        handleGetAdminLogAppenders(request, response);
                    } else if (uri.startsWith("/v1/namemappings")) {
                        NameMappingsHandlers.get(request, response, nameMappingsConfFile);
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                        response.getWriter().println(method + " " + uri + " not implemented");
                    } // if else
                    
                    break;
                case "POST":
                    if (uri.equals("/v1/groupingrules")) {
                        GroupingRulesHandlers.post(request, response, groupingRulesConfFile);
                    } else if (uri.equals("/v1/subscriptions")) {
                        SubscriptionsHandlers.post(request, response);
                    } else if (uri.startsWith("/admin/configuration/agent")) {
                        handlePostAdminConfigurationAgent(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/agent")) {
                        handlePostAdminConfigurationAgent(request, response, true);
                    } else if (uri.startsWith("/admin/configuration/instance")) {
                        handlePostAdminConfigurationInstance(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/instance")) {
                        handlePostAdminConfigurationInstance(request, response, true);
                    } else if (uri.startsWith("/v1/admin/log/loggers")) {
                        handlePostAdminLogLoggers(request, response);
                    } else if (uri.startsWith("/v1/admin/log/appenders")) {
                        handlePostAdminLogAppenders(request, response);
                    } else if (uri.startsWith("/v1/namemappings")) {
                        NameMappingsHandlers.post(request, response, nameMappingsConfFile);
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                        response.getWriter().println(method + " " + uri + " not implemented");
                    } // if else
                    
                    break;
                case "PUT":
                    if (uri.equals("/v1/stats")) {
                        StatsHandlers.put(response, sources, channels, sinks);
                    } else if (uri.equals("/v1/groupingrules")) {
                        GroupingRulesHandlers.put(request, response, groupingRulesConfFile);
                    } else if (uri.equals("/admin/log")) {
                        handlePutAdminLog(request, response);
                    } else if (uri.startsWith("/admin/configuration/agent")) {
                        handlePutAdminConfigurationAgent(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/agent")) {
                        handlePutAdminConfigurationAgent(request, response, true);
                    } else if (uri.startsWith("/admin/configuration/instance")) {
                        handlePutAdminConfigurationInstance(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/instance")) {
                        handlePutAdminConfigurationInstance(request, response, true);
                    } else if (uri.startsWith("/v1/admin/log/loggers")) {
                        handlePutAdminLogLoggers(request, response);
                    } else if (uri.startsWith("/v1/admin/log/appenders")) {
                        handlePutAdminLogAppenders(request, response);
                    } else if (uri.startsWith("/v1/namemappings")) {
                        NameMappingsHandlers.put(request, response, nameMappingsConfFile);
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                        response.getWriter().println(method + " " + uri + " not implemented");
                    } // if else
                    
                    break;
                case "DELETE":
                    if (uri.equals("/v1/groupingrules")) {
                        GroupingRulesHandlers.delete(request, response, groupingRulesConfFile);
                    } else if (uri.equals("/v1/subscriptions")) {
                        SubscriptionsHandlers.delete(request, response);
                    } else if (uri.startsWith("/admin/configuration/agent")) {
                        handleDeleteAdminConfigurationAgent(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/agent")) {
                        handleDeleteAdminConfigurationAgent(request, response, true);
                    } else if (uri.startsWith("/admin/configuration/instance")) {
                        handleDeleteAdminConfigurationInstance(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/instance")) {
                        handleDeleteAdminConfigurationInstance(request, response, true);
                    } else if (uri.startsWith("/v1/admin/log/loggers")) {
                        handleDeleteAdminLogLoggers(request, response);
                    } else if (uri.startsWith("/v1/admin/log/appenders")) {
                        handleDeleteAdminLogAppenders(request, response);
                    } else if (uri.startsWith("/v1/namemappings")) {
                        NameMappingsHandlers.delete(request, response, nameMappingsConfFile);
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                        response.getWriter().println(method + " " + uri + " not implemented");
                    } // if else
                    
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    response.getWriter().println(method + " " + uri + " not implemented");
                    break; // if else
            }
        } else if (port == guiPort) {
            if (method.equals("GET")) {
                if (uri.equals("/")) {
                    GUIHandlers.getRoot(response);
                } else if (uri.endsWith("/points")) {
                    GUIHandlers.getPoints(response, channels);
                } else if (uri.equals("/stats")) { // this is order to avoid CORS access control
                    StatsHandlers.get(response, sources, channels, sinks);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    response.getWriter().println(method + " " + uri + " not implemented");
                } // if else
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                response.getWriter().println(method + " " + uri + " not implemented");
            } // if else
        } else {
            LOGGER.info("Attending a request in a non expected port: " + port);
        } // if else
    } // handle

    private void handleGetVersion(HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{\"success\":\"true\",\"version\":\"" + CommonUtils.getCygnusVersion()
                + "." + CommonUtils.getLastCommit() + "\"}");
    } // handleGetVersion
    
    /**
     * Handles GET /admin/log.
     * @param request
     * @param response
     * @throws IOException
     */
    protected void handleGetAdminLog(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        Level level = LogManager.getRootLogger().getLevel();
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{\"level\":\"" + level + "\"}");
        LOGGER.info("Cygnus logging level successfully obtained");
    } // handleGetAdminLog
    
    /**
     * Handles GET /admin/configuration/agent.
     * @param request
     * @param response
     * @param v1
     * @throws IOException
     */
    protected void handleGetAdminConfigurationAgent(HttpServletRequest request, HttpServletResponse response,
            boolean v1) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        boolean allParameters = false;
        
        String param = request.getParameter("param");
        String url = request.getRequestURI();
        String fileName = ManagementInterfaceUtils.getFileName(url);
        
        if (!(fileName.startsWith("agent_"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Agent file name must start with 'agent_'\"}");
            LOGGER.error("Agent file name must start with 'agent_'.");
            return;
        } // if
        
        if (param == null) {
            allParameters = true;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param_name). Check it for errors\"}");
            LOGGER.error("Parse error, empty parameter (param_name). Check it for errors.");
            return;
        } // if else
                
        String pathToFile;
        
        if (v1) {
            pathToFile = url.substring(29);
        } else {
            pathToFile = url.substring(26);
        } // if else
        
        File file = new File(pathToFile);
                
        if (file.exists()) {
            FileInputStream fileInputStream = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fileInputStream);

            JSONObject jsonObject = new JSONObject();
            
            if (allParameters) {
                jsonObject.put("agent", properties);
            } else {
                String property = properties.getProperty(param);
                
                if (property != null) {
                    jsonObject.put(param, properties.getProperty(param));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"result\":\"Param '" + param + "' not found in the agent\"}");
                    return;
                } // if else
                
            } // if else
            
            response.getWriter().println("{\"success\":\"true\",\"result\":" + jsonObject + "");
            LOGGER.debug(jsonObject);
            response.setStatus(HttpServletResponse.SC_OK);
            
        } else {
            response.getWriter().println("{\"success\":\"false\",\"result\":\"File not found in the path received\"}");
            LOGGER.debug("File not found in the path received");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // if else
    } // handleGetAdminParameters

    /**
     * Handles GET /admin/configuration/instance.
     * @param request
     * @param response
     * @param v1
     * @throws IOException
     */
    protected void handleGetAdminConfigurationInstance(HttpServletRequest request, HttpServletResponse response,
            boolean v1) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        boolean allParameters = false;
        
        String param = request.getParameter("param");
        String url = request.getRequestURI();
        String fileName = ManagementInterfaceUtils.getFileName(url);
        
        if (param == null) {
            allParameters = true;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param_name). Check it for errors.\"}");
            LOGGER.error("Parse error, empty parameter (param_name). Check it for errors.");
            return;
        } // if else
                
        String pathToFile;
        
        if (v1) {
            pathToFile = url.substring(32);
        } else {
            pathToFile = url.substring(29);
        } // if else
        
        if (!(pathToFile.endsWith("/usr/cygnus/conf/" + fileName))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\":\"Invalid path for a instance configuration file\"}");
            return;
        } // if
        
        File file = new File(pathToFile);
                
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fileInputStream);
            JSONObject jsonObject = new JSONObject();
            
            if (allParameters) {
                jsonObject.put("instance", properties);
            } else {
                String property = properties.getProperty(param);
                
                if (property != null) {
                    jsonObject.put(param, properties.getProperty(param));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"result\":\"Param '" + param + "' not found in the instance\"}");
                    return;
                } // if else
            } // if else
            
            response.getWriter().println("{\"success\":\"true\",\"result\":\"" + jsonObject + "\"}");
            LOGGER.debug(jsonObject);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.getWriter().println("{\"success\":\"false\",\"result\":\"File not found in the path received\"}");
            LOGGER.debug("File not found in the path received. Details: " +  e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // if else
    } // handleGetAdminConfigurationInstance
    
    private void handleGetAdminLogLoggers(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
            Enumeration<Logger> loggers = LogManager.getLoggerRepository().getCurrentLoggers();

            if (allLoggers) {
                while (loggers.hasMoreElements()) {

                    Logger logger = loggers.nextElement();
                    String loggName = logger.getName();
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

                while (loggers.hasMoreElements()) {

                    Logger log = loggers.nextElement();
                    if (log.getName().equals(loggerName)) {
                        loggersJson += "{\"name\":\"" + log.getName() + "\"}";
                        loggerFound = true;
                    } // if

                } // while

                loggersJson += "]";

                if (loggerFound) {
                    LogManager.getLoggerRepository().getLogger(loggerName);
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
    } // handleGetAdminLogLoggers

    private void handleGetAdminLogAppenders(HttpServletRequest request, HttpServletResponse response)
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
                Enumeration appenders = LogManager.getRootLogger().getAllAppenders();
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
                    Appender app = LogManager.getRootLogger().getAppender(appenderName);
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
    } // handleGetAdminLogAppenders
    
    /**
     * Handles POST /admin/configuration/agent.
     * @param request
     * @param response
     * @param v1
     * @throws IOException
     */
    protected void handlePostAdminConfigurationAgent(HttpServletRequest request, HttpServletResponse response,
            boolean v1) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        String param = request.getParameter("param");
        String newValue = request.getParameter("value");
        String url = request.getRequestURI();
        String fileName = ManagementInterfaceUtils.getFileName(url);
        
        if (!(fileName.startsWith("agent_"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Agent file name must start with 'agent_'\"}");
            LOGGER.error("Agent file name must start with 'agent_'.");
            return;
        } // if
                
        if (param == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (param). Check it for errors\"}");
            LOGGER.error("Parse error, missing parameter (param). Check it for errors.");
            return;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param). Check it for errors\"}");
            LOGGER.error("Parse error, empty parameter (param). Check it for errors.");
            return;
        } // if else
        
        if (newValue == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (value). Check it for errors\"}");
            LOGGER.error("Parse error, missing parameter (value). Check it for errors.");
            return;
        } // if else
                
        String pathToFile;
        
        if (v1) {
            pathToFile = url.substring(29);
        } else {
            pathToFile = url.substring(26);
        } // if
                
        File file = new File(pathToFile);
                
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            JSONObject jsonObject = new JSONObject();
            
            for (Object key: properties.keySet()) {
                String name = (String) key;
                
                if (name.equals(param)) {
                    jsonObject.put("agent", properties);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println("{\"success\":\"false\",\"result\":" + jsonObject + "}");
                    LOGGER.debug(jsonObject);
                    return;
                } // if
            } // for
            
            properties.put(param, newValue);
            jsonObject.put("agent", properties);
            ManagementInterfaceUtils.orderedPrinting(properties, file);
            response.getWriter().println("{\"success\":\"true\",\"result\":" + jsonObject + "}");
            LOGGER.debug(jsonObject);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.getWriter().println("{\"success\":\"false\",\"result\":\"File not found in the path received\"}");
            LOGGER.debug("File not found in the path received. Details: " +  e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // if else
    } // handlePostAdminConfigurationAgent
    
    /**
     * Handles POST /admin/configuration/instance.
     * @param request
     * @param response
     * @param v1
     * @throws IOException
     */
    protected void handlePostAdminConfigurationInstance(HttpServletRequest request, HttpServletResponse response,
            boolean v1) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        String param = request.getParameter("param");
        String newValue = request.getParameter("value");
        String url = request.getRequestURI();
        String fileName = ManagementInterfaceUtils.getFileName(url);
                
        if (param == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (param). Check it for errors\"}");
            LOGGER.error("Parse error, missing parameter (param). Check it for errors.");
            return;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param). Check it for errors\"}");
            LOGGER.error("Parse error, empty parameter (param). Check it for errors.");
            return;
        } // if else
        
        if (newValue == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (value). Check it for errors\"}");
            LOGGER.error("Parse error, missing parameter (value). Check it for errors.");
            return;
        } // if else
                
        String pathToFile;
        
        if (v1) {
            pathToFile = url.substring(32);
        } else {
            pathToFile = url.substring(29);
        } // if
        
        if (!(pathToFile.endsWith("/usr/cygnus/conf/" + fileName))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\":\"Invalid path for a instance configuration file\"}");
            return;
        } // if
                
        File file = new File(pathToFile);
        
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            JSONObject jsonObject = new JSONObject();
            Map<String, String> descriptions = ManagementInterfaceUtils.readDescriptions(file);
            
            for (Object key: properties.keySet()) {
                String name = (String) key;
                
                if (name.equals(param)) {
                    jsonObject.put("instance", properties);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println("{\"success\":\"false\",\"result\":" + jsonObject + "}");
                    LOGGER.debug(jsonObject);
                    return;
                } // if
            } // for
            
            properties.put(param, newValue);
            jsonObject.put("instance", properties);
            ManagementInterfaceUtils.instancePrinting(properties, file, descriptions);
            response.getWriter().println("{\"success\":\"true\",\"result\":" + jsonObject + "}");
            LOGGER.debug(jsonObject);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.getWriter().println("{\"success\":\"false\",\"result\":\"File not found in the path received\"}");
            LOGGER.debug("File not found in the path received. Details: " +  e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // if else
    } // handlePostAdminConfigurationInstance
    
    private void handlePostAdminLogLoggers(HttpServletRequest request, HttpServletResponse response)
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
       
    } // handlePostAdminLogLoggers
    
    private void handlePostAdminLogAppenders(HttpServletRequest request, HttpServletResponse response)
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
    } // handlePostAdminLogAppender
    
    /**
     * Handles DELETE configuration/agent.
     * @param request
     * @param response
     * @param v1
     * @throws IOException
     */
    protected void handleDeleteAdminConfigurationAgent(HttpServletRequest request, HttpServletResponse response,
            boolean v1) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        String param = request.getParameter("param");
        String url = request.getRequestURI();
        String fileName = ManagementInterfaceUtils.getFileName(url);
        
        if (!(fileName.startsWith("agent_"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Agent file name must start with 'agent_'\"}");
            LOGGER.error("Agent file name must start with 'agent_'.");
            return;
        } // if
                
        if (param == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (param). Check it for errors\"}");
            LOGGER.error("Parse error, missing parameter (param). Check it for errors.");
            return;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param). Check it for errors\"}");
            LOGGER.error("Parse error, empty parameter (param). Check it for errors.");
            return;
        } // if else
                
        String pathToFile;
        
        if (v1) {
            pathToFile = url.substring(29);
        } else {
            pathToFile = url.substring(26);
        } // if
                
        File file = new File(pathToFile);
                
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            JSONObject jsonObject = new JSONObject();
            boolean paramExists = false;
            
            for (Object key: properties.keySet()) {
                String name = (String) key;
                
                if (name.equals(param)) {
                    paramExists = true;
                } // if
                
            } // for
            
            properties.remove(param);
            jsonObject.put("agent", properties);
            ManagementInterfaceUtils.orderedPrinting(properties, file);
            response.setStatus(HttpServletResponse.SC_OK);
            
            if (paramExists) {
                response.getWriter().println("{\"success\":\"true\",\"result\":" + jsonObject + "}");
            } else {
                response.getWriter().println("{\"success\":\"false\",\"result\":" + jsonObject + "}");
            } // if else
            
            LOGGER.debug(jsonObject);
            
        } catch (Exception e) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\":\"File not found in the path received\"}");
            LOGGER.debug("File not found in the path received. Details: " +  e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // try catch
    } // handleDeleteAdminConfigurationAgent
    
    /**
     * Handles DELETE configuration/instance.
     * @param request
     * @param response
     * @param v1
     * @throws IOException
     */
    protected void handleDeleteAdminConfigurationInstance(HttpServletRequest request, HttpServletResponse response,
            boolean v1) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        String param = request.getParameter("param");
        String url = request.getRequestURI();
                
        if (param == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (param). Check it for errors\"}");
            LOGGER.error("Parse error, missing parameter (param). Check it for errors.");
            return;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param). Check it for errors\"}");
            LOGGER.error("Parse error, empty parameter (param). Check it for errors.");
            return;
        } // if else
                
        String pathToFile;
        
        if (v1) {
            pathToFile = url.substring(32);
        } else {
            pathToFile = url.substring(29);
        } // if
                
        File file = new File(pathToFile);
                
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            Map<String, String> descriptions = ManagementInterfaceUtils.readDescriptions(file);
            JSONObject jsonObject = new JSONObject();
            boolean paramExists = false;
            
            for (Object key: properties.keySet()) {
                String name = (String) key;
                
                if (name.equals(param)) {
                    paramExists = true;
                } // if
            } // for
            
            properties.remove(param);
            jsonObject.put("agent", properties);
            ManagementInterfaceUtils.instancePrinting(properties, file, descriptions);
            response.setStatus(HttpServletResponse.SC_OK);
            
            if (paramExists) {
                response.getWriter().println("{\"success\":\"true\","
                        + "\"result\" : " + jsonObject + "}");
            } else {
                response.getWriter().println("{\"success\":\"false\","
                        + "\"result\" : " + jsonObject + "}");
            } // if else
            
            LOGGER.debug(jsonObject);
        } catch (Exception e) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\":\"File not found in the path received\"}");
            LOGGER.debug("File not found in the path received. Details: " +  e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // try catch
    } // handleDeleteAdminConfigurationAgent
    
    private void handleDeleteAdminLogAppenders(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
                LogManager.getRootLogger().removeAllAppenders();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("{\"success\":\"true\",\"result\":\"Appenders removed succesfully\"}}");
                LOGGER.debug("Log4j appenders removed succesfully");
            } else {

                try {
                    // Check if appender already exists
                    Appender delete = LogManager.getRootLogger().getAppender(appenderName);
                    LogManager.getRootLogger().removeAppender(delete);
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
    } // handleDeleteAdminLogAppenders
    
    /**
     * Handles PUT /admin/log.
     * @param request
     * @param response
     * @throws IOException
     */
    protected void handlePutAdminLog(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        // get the parameters to be updated
        String logLevel = request.getParameter("level");
        
        try {
            LoggingLevels.valueOf(logLevel.toUpperCase());
            LogManager.getRootLogger().setLevel(Level.toLevel(logLevel.toUpperCase()));
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("{\"success\":\"log4j logging level updated to "
                    + logLevel.toUpperCase() + "\" }");
            LOGGER.debug("log4j logging level updated to " + logLevel.toUpperCase());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"error\":\"Invalid log level\"}");
            LOGGER.error("Invalid log level '" + logLevel + "'");
        } // try catch
        
    } // handlePutAdminLog
    
    /**
     * Handles PUT /admin/configuration/agent.
     * @param request
     * @param response
     * @param v1
     * @throws IOException
     */
    protected void handlePutAdminConfigurationAgent(HttpServletRequest request, HttpServletResponse response,
            boolean v1) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        String param = request.getParameter("param");
        String newValue = request.getParameter("value");
        String url = request.getRequestURI();
        String fileName = ManagementInterfaceUtils.getFileName(url);
        
        if (!(fileName.startsWith("agent_"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Agent file name must start with 'agent_'\"}");
            LOGGER.error("Agent file name must start with 'agent_'.");
            return;
        } // if
                
        if (param == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (param). Check it for errors\"}");
            LOGGER.error("Parse error, missing parameter (param). Check it for errors.");
            return;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param). Check it for errors\"}");
            LOGGER.error("Parse error, empty parameter (param). Check it for errors.");
            return;
        } // if else
        
        if (newValue == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (value). Check it for errors\"}");
            LOGGER.error("Parse error, missing parameter (value). Check it for errors.");
            return;
        } // if else
                
        String pathToFile;
        
        if (v1) {
            pathToFile = url.substring(29);
        } else {
            pathToFile = url.substring(26);
        } // if
                
        File file = new File(pathToFile);
                
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            properties.put(param, newValue);
                        
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("agent", properties);
            
            ManagementInterfaceUtils.orderedPrinting(properties, file);
                               
            response.getWriter().println("{\"success\":\"true\",\"result\" : " + jsonObject + "}");
            LOGGER.debug(jsonObject);
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\":\"File not found in the path received\"}");
            LOGGER.debug("File not found in the path received. Details: " +  e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // if else
        
    } // handlePutAdminConfigurationAgent
    
    /**
     * Handles PUT /admin/configuration/instance.
     * @param request
     * @param response
     * @param v1
     * @throws IOException
     */
    protected void handlePutAdminConfigurationInstance(HttpServletRequest request, HttpServletResponse response,
            boolean v1) throws IOException {
        response.setContentType("json;charset=utf-8");
        
        String param = request.getParameter("param");
        String newValue = request.getParameter("value");
        String url = request.getRequestURI();
        String fileName = ManagementInterfaceUtils.getFileName(url);
                
        if (param == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (param). Check it for errors\"}");
            LOGGER.error("Parse error, missing parameter (param). Check it for errors.");
            return;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param). Check it for errors\"}");
            LOGGER.error("Parse error, empty parameter (param). Check it for errors.");
            return;
        } // if else
        
        if (newValue == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (value). Check it for errors\"}");
            LOGGER.error("Parse error, missing parameter (value). Check it for errors.");
            return;
        } // if else
                
        String pathToFile;
        
        if (v1) {
            pathToFile = url.substring(32);
        } else {
            pathToFile = url.substring(29);
        } // if
        
        if (!(pathToFile.endsWith("/usr/cygnus/conf/" + fileName))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"result\":{\"Invalid path for a instance configuration file\"}");
            return;
        } // if
                
        File file = new File(pathToFile);
                
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            Map<String, String> descriptions = ManagementInterfaceUtils.readDescriptions(file);
            properties.put(param, newValue);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("instance", properties);
            ManagementInterfaceUtils.instancePrinting(properties, file, descriptions);
            response.getWriter().println("{\"success\":\"true\",\"result\":" + jsonObject + "}");
            LOGGER.debug(jsonObject);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\":\"File not found in the path received\"}");
            LOGGER.debug("File not found in the path received. Details: " +  e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // try catch
    } // handlePutAdminConfigurationInstance
    
    private void handlePutAdminLogAppenders(HttpServletRequest request, HttpServletResponse response)
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
                    Enumeration<Appender> currentAppenders = LogManager.getRootLogger().getAllAppenders();
                    boolean appenderFound = false;

                    while (currentAppenders.hasMoreElements()) {
                        Appender currentApp = currentAppenders.nextElement();
                        String appenderName = currentApp.getName();

                        if (appenderName.equals(name)) {
                            appenderFound = true;
                        } // if
                    } // while

                    PatternLayout patternLayout = new PatternLayout(pattern);

                    if (appenderFound) {
                        Appender appUpdated = LogManager.getRootLogger().getAppender(name);
                        appUpdated.setLayout(patternLayout);
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
    } // handlePutAdminLogAppender
    
    private void handlePutAdminLogLoggers(HttpServletRequest request, HttpServletResponse response)
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
                        Enumeration<Logger> currentLoggers = LogManager.getLoggerRepository().getCurrentLoggers();
                        boolean loggerFound = false;

                        while (currentLoggers.hasMoreElements()) {
                            Logger currentLogger = currentLoggers.nextElement();
                            String loggerName = currentLogger.getName();

                            if (loggerName.equals(name)) {
                                loggerFound = true;
                            } // if
                        } // while

                        if (loggerFound) {
                            Logger loggerUpdated = LogManager.getLoggerRepository().getLogger(name);
                            loggerUpdated.setLevel(Level.toLevel(level));
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
    } // handlePutAdminLogLoggers
    
    private void handleDeleteAdminLogLoggers(HttpServletRequest request, HttpServletResponse response)
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
            Enumeration<Logger> loggers = LogManager.getLoggerRepository().getCurrentLoggers();

            if (allLoggers) {
                
                while (loggers.hasMoreElements()) {
                    loggers.nextElement().setLevel(Level.OFF);
                } // while
                
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("{\"success\":\"true\",\"result\":\"Loggers removed succesfully\"}");
                LOGGER.debug("Log4j loggers removed succesfully");
                
            } else {

                ArrayList<Logger> loggerNames = new ArrayList<>();

                while (loggers.hasMoreElements()) {
                    loggerNames.add(loggers.nextElement());
                } // while

                if (loggerNames.contains(LogManager.getLogger(loggerName))) {
                    LogManager.getLogger(loggerName).setLevel(Level.OFF);
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
    } // handleDeleteAdminLogLoggers

    private String getGroupingRulesConfFile() throws IOException {
        if (!configurationFile.exists()) {
            return "404 - Configuration file for Cygnus not found. Details: "
                    + configurationFile.toString();
        } // if

        String grConfFile = null;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(configurationFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    if (line.contains("grouping_rules_conf_file")) {
                        String[] splits = line.split("=");
                        grConfFile = splits[1].replaceAll(" ", "");
                        break;
                    } // if
                } // if
            } // while
        }
        return grConfFile;
    } // getGroupingRulesConfFile
    
    private String getNameMappingsConfFile() throws IOException {
        if (!configurationFile.exists()) {
            return "404 - Configuration file for Cygnus not found. Details: "
                    + configurationFile.toString();
        } // if
        
        String nmConfFile = null;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(configurationFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    if (line.contains("name_mappings_conf_file")) {
                        String[] splits = line.split("=");
                        nmConfFile = splits[1].replaceAll(" ", "");
                        break;
                    } // if
                } // if
            } // while
        } // try
        
        return nmConfFile;
    } // getGroupingRulesConfFile

} // ManagementInterface
