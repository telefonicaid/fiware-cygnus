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
package com.telefonica.iot.cygnus.management;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.channels.CygnusChannel;
import com.telefonica.iot.cygnus.backends.orion.OrionBackendImpl;
import com.telefonica.iot.cygnus.containers.CygnusSubscriptionV1;
import com.telefonica.iot.cygnus.containers.CygnusSubscriptionV2;
import com.telefonica.iot.cygnus.containers.OrionEndpoint;
import com.telefonica.iot.cygnus.handlers.CygnusHandler;
import com.telefonica.iot.cygnus.interceptors.CygnusGroupingRule;
import com.telefonica.iot.cygnus.interceptors.CygnusGroupingRules;
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
import org.apache.flume.Sink;
import org.apache.flume.SinkProcessor;
import org.apache.flume.SinkRunner;
import org.apache.flume.Source;
import org.apache.flume.SourceRunner;
import org.apache.flume.source.http.HTTPSourceHandler;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
                    } else if (uri.equals("/v1/subscriptions")) {
                        SubscriptionsHandlers.get(request, response);
                    } else if (uri.startsWith("/admin/configuration/agent")) {
                        ConfigurationAgentHandlers.get(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/agent")) {
                        ConfigurationAgentHandlers.get(request, response, true);
                    } else if (uri.startsWith("/admin/configuration/instance")) {
                        ConfigurationInstanceHandlers.get(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/instance")) {
                        ConfigurationInstanceHandlers.get(request, response, true);
                    } else if (uri.equals("/admin/log")) {
                        LogHandlers.getLogLevel(request, response);
                    } else if (uri.startsWith("/v1/admin/log/loggers")) {
                        LogHandlers.getLoggers(request, response, configurationPath);
                    } else if (uri.startsWith("/v1/admin/log/appenders")) {
                        LogHandlers.getAppenders(request, response, configurationPath);
                    } else if (uri.startsWith("/v1/admin/metrics") || uri.startsWith("/admin/metrics")) {
                        MetricsHandlers.get(request, response, sources, sinks);
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
                        ConfigurationAgentHandlers.post(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/agent")) {
                        ConfigurationAgentHandlers.post(request, response, true);
                    } else if (uri.startsWith("/admin/configuration/instance")) {
                        ConfigurationInstanceHandlers.post(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/instance")) {
                        ConfigurationInstanceHandlers.post(request, response, true);
                    } else if (uri.startsWith("/v1/admin/log/loggers")) {
                        LogHandlers.postLoggers(request, response, configurationPath);
                    } else if (uri.startsWith("/v1/admin/log/appenders")) {
                        LogHandlers.postAppenders(request, response, configurationPath);
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
                    } else if (uri.startsWith("/admin/configuration/agent")) {
                        ConfigurationAgentHandlers.put(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/agent")) {
                        ConfigurationAgentHandlers.put(request, response, true);
                    } else if (uri.startsWith("/admin/configuration/instance")) {
                        ConfigurationInstanceHandlers.put(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/instance")) {
                        ConfigurationInstanceHandlers.put(request, response, true);
                    } else if (uri.equals("/admin/log")) {
                        LogHandlers.putLogLevel(request, response);
                    } else if (uri.startsWith("/v1/admin/log/loggers")) {
                        LogHandlers.putLoggers(request, response, configurationPath);
                    } else if (uri.startsWith("/v1/admin/log/appenders")) {
                        LogHandlers.putAppenders(request, response, configurationPath);
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
                        ConfigurationAgentHandlers.delete(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/agent")) {
                        ConfigurationAgentHandlers.delete(request, response, true);
                    } else if (uri.startsWith("/admin/configuration/instance")) {
                        ConfigurationInstanceHandlers.delete(request, response, false);
                    } else if (uri.startsWith("/v1/admin/configuration/instance")) {
                        ConfigurationInstanceHandlers.delete(request, response, true);
                    } else if (uri.startsWith("/v1/admin/log/loggers")) {
                        LogHandlers.deleteLoggers(request, response, configurationPath);
                    } else if (uri.startsWith("/v1/admin/log/appenders")) {
                        LogHandlers.deleteAppenders(request, response, configurationPath);
                    } else if (uri.startsWith("/v1/admin/metrics") || uri.startsWith("/admin/metrics")) {
                        MetricsHandlers.delete(response, sources, sinks);
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

    private void handleGetStats(HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String jsonStr = "{\"success\":\"true\",\"stats\":{\"sources\":[";
        boolean first = true;

        for (String key : sources.keySet()) {
            if (first) {
                first = false;
            } else {
                jsonStr += ",";
            } // if else

            Source source;
            HTTPSourceHandler handler;

            try {
                SourceRunner sr = sources.get(key);
                source = sr.getSource();
                Field f = source.getClass().getDeclaredField("handler");
                f.setAccessible(true);
                handler = (HTTPSourceHandler) f.get(source);
            } catch (IllegalArgumentException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } catch (IllegalAccessException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } catch (NoSuchFieldException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } catch (SecurityException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } // try catch

            jsonStr += "{\"name\":\"" + source.getName() + "\","
                    + "\"status\":\"" + source.getLifecycleState().toString() + "\",";

            if (handler instanceof CygnusHandler) {
                CygnusHandler ch = (CygnusHandler) handler;
                jsonStr += "\"setup_time\":\"" + CommonUtils.getHumanReadable(ch.getBootTime(), true) + "\","
                        + "\"num_received_events\":" + ch.getNumReceivedEvents() + ","
                        + "\"num_processed_events\":" + ch.getNumProcessedEvents() + "}";
            } else {
                jsonStr += "\"setup_time\":\"unknown\","
                        + "\"num_received_events\":-1,"
                        + "\"num_processed_events\":-1}";
            } // if else
        } // for

        jsonStr += "],\"channels\":[";
        first = true;

        for (String key : channels.keySet()) {
            if (first) {
                first = false;
            } else {
                jsonStr += ",";
            } // if else

            Channel channel = channels.get(key);
            jsonStr += "{\"name\":\"" + channel.getName() + "\","
                    + "\"status\":\"" + channel.getLifecycleState().toString() + "\",";

            if (channel instanceof CygnusChannel) {
                CygnusChannel cc = (CygnusChannel) channel;
                jsonStr += "\"setup_time\":\"" + CommonUtils.getHumanReadable(cc.getSetupTime(), true) + "\","
                        + "\"num_events\":" + cc.getNumEvents() + ","
                        + "\"num_puts_ok\":" + cc.getNumPutsOK() + ","
                        + "\"num_puts_failed\":" + cc.getNumPutsFail() + ","
                        + "\"num_takes_ok\":" + cc.getNumTakesOK() + ","
                        + "\"num_takes_failed\":" + cc.getNumTakesFail() + "}";
            } else {
                jsonStr += "\"setup_time\":\"unknown\","
                        + "\"num_events\":-1,"
                        + "\"num_puts_ok\":-1,"
                        + "\"num_puts_failed\":-1,"
                        + "\"num_takes_ok\":-1,"
                        + "\"num_takes_failed\":-1}";
            } // if else
        } // for

        jsonStr += "],\"sinks\":[";
        first = true;

        for (String key : sinks.keySet()) {
            if (first) {
                first = false;
            } else {
                jsonStr += ",";
            } // if else

            Sink sink;

            try {
                SinkRunner sr = sinks.get(key);
                SinkProcessor sp = sr.getPolicy();
                Field f = sp.getClass().getDeclaredField("sink");
                f.setAccessible(true);
                sink = (Sink) f.get(sp);
            } catch (IllegalArgumentException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } catch (IllegalAccessException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } catch (NoSuchFieldException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } catch (SecurityException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } // try catch

            jsonStr += "{\"name\":\"" + sink.getName() + "\","
                    + "\"status\":\"" + sink.getLifecycleState().toString() + "\",";

            if (sink instanceof CygnusSink) {
                CygnusSink cs = (CygnusSink) sink;
                jsonStr += "\"setup_time\":\"" + CommonUtils.getHumanReadable(cs.getSetupTime(), true) + "\","
                        + "\"num_processed_events\":" + cs.getNumProcessedEvents() + ","
                        + "\"num_persisted_events\":" + cs.getNumPersistedEvents() + "}";
            } else {
                jsonStr += "\"setup_time\":\"unknown\","
                        + "\"num_processed_events\":-1,"
                        + "\"num_persisted_events\":-1}";
            } // if else
        } // for

        jsonStr += "]}}";
        response.getWriter().println(jsonStr);
    } // handleGetStats

    private void handleGetGroupingRules(HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");

        if (groupingRulesConfFile == null) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Missing configuration file for Grouping Rules\"}");
            LOGGER.error("Missing configuration file for Grouping Rules");
            return;
        } // if

        if (!new File(groupingRulesConfFile).exists()) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Configuration file for Grouing Rules not found. Details: "
                    + groupingRulesConfFile + "\"}");
            LOGGER.error("Configuration file for Grouing Rules not found. Details: " + groupingRulesConfFile);
            return;
        } // if

        CygnusGroupingRules groupingRules = new CygnusGroupingRules(groupingRulesConfFile);
        String rulesStr = groupingRules.toString(true);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{\"success\":\"true\"," + rulesStr + "}");
    } // handleGetGroupingRules
    
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
        
        // set the given header to the response or create it
        response.setHeader(CommonConstants.HEADER_CORRELATOR_ID, ManagementInterfaceUtils.setCorrelator(request));

        // check the Json syntax of the new rule
        JSONParser jsonParser = new JSONParser();
        JSONObject rule;

        try {
            rule = (JSONObject) jsonParser.parse(ruleStr);
        } catch (ParseException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, invalid Json syntax. Details: "
                    + e.getMessage() + "\"}");
            LOGGER.error("Parse error, invalid Json syntax. Details: " + e.getMessage());
            return;
        } // try catch

        // check if the rule is valid (it could be a valid Json document,
        // but not a Json document describing a rule)
        int err = CygnusGroupingRule.isValid(rule, true);

        if (err > 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            switch (err) {
                case 1:
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"error\":\"Invalid grouping rule, some field is missing\"}");
                    LOGGER.warn("Invalid grouping rule, some field is missing");
                    return;
                case 2:
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"error\":\"Invalid grouping rule, some field is empty\"}");
                    LOGGER.warn("Invalid grouping rule, some field is empty");
                    return;
                case 3:
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"error\":\"Invalid grouping rule, some field is not allowed\"}");
                    LOGGER.warn("Invalid grouping rule, some field is not allowed");
                    return;
                default:
                    response.getWriter().println("{\"success\":\"false\",\"error\":\"Invalid grouping rule\"}");
                    LOGGER.warn("Invalid grouping rule");
                    return;
            } // swtich
        } // if

        if (groupingRulesConfFile == null) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Missing configuration file for Grouping Rules\"}");
            LOGGER.error("Missing configuration file for Grouping Rules");
            return;
        } // if

        if (!new File(groupingRulesConfFile).exists()) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Configuration file for Grouing Rules not found. Details: "
                    + groupingRulesConfFile + "\"}");
            LOGGER.error("Configuration file for Grouing Rules not found. Details: " + groupingRulesConfFile);
            return;
        } // if

        CygnusGroupingRules groupingRules = new CygnusGroupingRules(groupingRulesConfFile);
        groupingRules.addRule(new CygnusGroupingRule(rule));
        String rulesStr = groupingRules.toString(false);

        // write the configuration
        PrintWriter writer = new PrintWriter(new FileWriter(groupingRulesConfFile));
        writer.println(rulesStr);
        writer.flush();
        writer.close();
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{\"success\":\"true\"}");
        LOGGER.debug("Rule added. Grouping rules after adding the new rule: " + rulesStr);
    } // handlePostGroupingRules

    protected void handlePostSubscription(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
        // read the new rule wanted to be added
        BufferedReader reader = request.getReader();
        String jsonStr = "";
        String line;
        
        while ((line = reader.readLine()) != null) {
            jsonStr += line;
        } // while

        reader.close();
                
        String ngsiVersion = request.getParameter("ngsi_version");
        String fiwareService = request.getHeader("Fiware-Service");
        String fiwareServicePath = request.getHeader("Fiware-ServicePath");
        
        if ((ngsiVersion == null) || (ngsiVersion.equals(""))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, wrong parameter (ngsi_version). Check it for errors\"}");
            LOGGER.error("Parse error, wrong parameter (ngsi_version). Check it for errors.");
            return;
        } // if
        
        if (!((ngsiVersion.equals("1")) || (ngsiVersion.equals("2")))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, invalid parameter (ngsi_version): "
                    + "Must be 1 or 2. Check it for errors\"}");
            LOGGER.error("Parse error, invalid parameter (ngsi_version): "
                    + "Must be 1 or 2. Check it for errors.");
            return;
        } // if
        
        // set the given header to the response or create it
        response.setHeader(CommonConstants.HEADER_CORRELATOR_ID, ManagementInterfaceUtils.setCorrelator(request));

        // Create a Gson object parsing the Json string
        Gson gson = new Gson();
        CygnusSubscriptionV1 cygnusSubscriptionv1;
        CygnusSubscriptionV2 cygnusSubscriptionv2;
        
        if (ngsiVersion.equals("1")) {
            try {
                cygnusSubscriptionv1 = gson.fromJson(jsonStr, CygnusSubscriptionV1.class);
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (JsonSyntaxException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, malformed Json. Check it for errors\"");
                LOGGER.error("Parse error, malformed Json. Check it for errors.\"");
                return;
            } // try catch

            // check if the subscription and endpoint are valid
            int err = cygnusSubscriptionv1.isValid();

            if (err > 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
                try {
                    manageErrorMsg(err, response);
                    return;
                } catch (Exception e) {
                    Logger.getLogger(e.getMessage());
                } // try catch
            } // if
             
            LOGGER.debug("Valid CygnusSubscription. Creating request to Orion.");

            // get host, port and ssl for request
            String host = cygnusSubscriptionv1.getOrionEndpoint().getHost();
            String port = cygnusSubscriptionv1.getOrionEndpoint().getPort();
            boolean ssl = Boolean.valueOf(cygnusSubscriptionv1.
                    getOrionEndpoint().getSsl());
            String token = cygnusSubscriptionv1.getOrionEndpoint().getAuthToken();
            
            // Create a orionBackend for request
            orionBackend = new OrionBackendImpl(host, port, ssl, MAX_CONNS, MAX_CONNS_PER_ROUTE);
        
            // Get /subscription JSON from entire one
            JsonObject inputJson = new JsonParser().parse(jsonStr).getAsJsonObject();
            String subscriptionStr = inputJson.get("subscription").toString();
        
            JsonResponse orionResponse;
            int status;
            JSONObject orionJson;
            
            try {
                orionResponse = orionBackend.subscribeContextV1(subscriptionStr, token, fiwareService,
                        fiwareServicePath);
                status = orionResponse.getStatusCode();
                orionJson = orionResponse.getJsonObject();
                
                if (orionJson.containsKey("orionError")) {
                    JSONObject error = (JSONObject) orionJson.get("orionError");
                    status = Integer.parseInt(error.get("code").toString());
                } // if
                
                if (status == 200) {
                    response.getWriter().println("{\"success\":\"true\",\"result\":{" + orionJson.toJSONString() + "}");
                    LOGGER.debug("Subscribed.");
                } else {
                    response.getWriter().println("{\"success\":\"false\",\"result\":{" + orionJson.toJSONString() + "}");
                } // if else
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            } // try catch
        } else if (ngsiVersion.equals("2")) {
            try {
                cygnusSubscriptionv2 = gson.fromJson(jsonStr, CygnusSubscriptionV2.class);
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (JsonSyntaxException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, malformed Json. Check it for errors\"");
                LOGGER.error("Parse error, malformed Json. Check it for errors.\"");
                return;
            } // try catch
            
            // check if the subscription and endpoint are valid
            int err = cygnusSubscriptionv2.isValid();

            if (err > 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
                try {
                    manageErrorMsg(err, response);
                    return;
                } catch (Exception e) {
                    Logger.getLogger(e.getMessage());
                } // try catch
            } // if
             
            LOGGER.debug("Valid CygnusSubscriptionV2. Creating request to Orion.");

            // get host, port and ssl for request
            String host = cygnusSubscriptionv2.getOrionEndpoint().getHost();
            String port = cygnusSubscriptionv2.getOrionEndpoint().getPort();
            boolean ssl = Boolean.valueOf(cygnusSubscriptionv2.
                    getOrionEndpoint().getSsl());
            String token = cygnusSubscriptionv2.getOrionEndpoint().getAuthToken();
            
            // Create a orionBackend for request
            orionBackend = new OrionBackendImpl(host, port, ssl, MAX_CONNS, MAX_CONNS_PER_ROUTE);
        
            // Get /subscription JSON from entire one
            JsonObject inputJson = new JsonParser().parse(jsonStr).getAsJsonObject();
            String subscriptionStr = inputJson.get("subscription").toString();
        
            JsonResponse orionResponse;
            int status;
            JSONObject orionJson;
            
            try {
                orionResponse = orionBackend.subscribeContextV2(subscriptionStr, token, fiwareService,
                        fiwareServicePath);
                status = orionResponse.getStatusCode();
                
                if (status == 201) {
                    String location = orionResponse.getLocationHeader().getValue();
                    response.getWriter().println("{\"success\":\"true\","
                            + "\"result\":{\"SubscriptionID\":\"" + location.substring(18) + "\"}");
                    LOGGER.debug("Subscribed.");
                } else {
                    orionJson = orionResponse.getJsonObject();
                    response.getWriter().println("{\"success\":\"false\",\"result\":{" + orionJson.toString() + "}");
                } // if else
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            } // try catch
        } // if else if
    } // handlePostSubscription
    
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
    protected void handlePostAdminConfigurationInstance (HttpServletRequest request, HttpServletResponse response,
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
    
    private void handlePostAdminLogLoggers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        // read the new rule wanted to be added
        BufferedReader reader = request.getReader();
        String jsonStr = "";
        String line;
        
        while ((line = reader.readLine()) != null) {
            jsonStr += line;
        } // while

        reader.close();
        
        if (!jsonStr.isEmpty()) {
            JsonObject jsonLogger = new JsonParser().parse(jsonStr).getAsJsonObject(); 
            
            try {
                JsonObject logger = jsonLogger.get("logger").getAsJsonObject();
                String name = logger.get("name").getAsString();
                String level = logger.get("level").getAsString();

                try {
                    CommonConstants.LoggingLevels.valueOf(level);
                    String transient_ = request.getParameter("transient");
                    String pathToFile = configurationPath + "/log4j.properties";
                    File file = new File(pathToFile);

                    if ((transient_ == null) || (transient_.equals("true"))) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("{\"success\":\"false\","
                            + "\"result\":\"POST appenders in transient mode is not implemented\"}");
                        LOGGER.debug("POST appenders in transient mode is not implemented");                       

                    } else if (transient_.equals("false")) {

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
    
    private void handlePostAdminLogAppenders(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        // read the new rule wanted to be added
        BufferedReader reader = request.getReader();
        String jsonStr = "";
        String line;
        
        while ((line = reader.readLine()) != null) {
            jsonStr += line;
        } // while

        reader.close();
        if (!jsonStr.isEmpty()) {
            JsonObject jsonAppender = new JsonParser().parse(jsonStr).getAsJsonObject();
            try {
                JsonObject appender = jsonAppender.get("appender").getAsJsonObject();
                String name = appender.get("name").getAsString();
                JsonObject layout = jsonAppender.get("pattern").getAsJsonObject();
                String pattern = layout.get("ConversionPattern").getAsString();
                String transient_ = request.getParameter("transient");
                String pathToFile = configurationPath + "/log4j.properties";
                File file = new File(pathToFile);

                if ((transient_ == null) || (transient_.equals("true"))) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\","
                        + "\"result\":\"POST appenders in transient mode is not implemented\"}");
                    LOGGER.debug("POST appenders in transient mode is not implemented");

                } else if (transient_.equals("false")) {

                    if (file.exists()) {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        Properties properties = new Properties();
                        properties.load(fileInputStream);
                        String class_ = appender.get("class").getAsString();
                        String layoutStr = layout.get("layout").getAsString();
                        Map<String, String> descriptions = ManagementInterfaceUtils.readLogDescriptions(file);
                        boolean appenderFound = false;
                        ArrayList<String> appenderNames = ManagementInterfaceUtils.getAppendersFromProperties(properties);

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
                            properties.put(propertyName, class_);
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
                }// if else if
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"success\":\"false\",\"result\":\"Invalid input JSON\"}");
                LOGGER.debug("Invalid input JSON");
            }
            
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\",\"result\":\"Missing input JSON\"}");
            LOGGER.debug("Missing input JSON.");
        } // if else
        
    } // handlePostAdminLogAppender  
    
    protected void handleDeleteSubscription(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        String subscriptionId = request.getParameter("subscription_id");
        String ngsiVersion = request.getParameter("ngsi_version");
        String fiwareService = request.getHeader("Fiware-Service");
        String fiwareServicePath = request.getHeader("Fiware-ServicePath");
        
        if ((subscriptionId == null) || (subscriptionId.equals(""))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, wrong parameter (subscription_id). Check it for errors\"}");
            LOGGER.error("Parse error, wrong parameter (subscription_id). Check it for errors.");
            return;
        } // if
        
        if ((ngsiVersion == null) || (ngsiVersion.equals(""))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, wrong parameter (ngsi_version). Check it for errors\"}");
            LOGGER.error("Parse error, wrong parameter (ngsi_version). Check it for errors.");
            return;
        } // if
        
        if (!((ngsiVersion.equals("1")) || (ngsiVersion.equals("2")))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, invalid parameter (ngsi_version): "
                    + "Must be 1 or 2. Check it for errors.\"}");
            LOGGER.error("Parse error, invalid parameter (ngsi_version): "
                    + "Must be 1 or 2. Check it for errors.");
            return;
        } // if
                
        LOGGER.debug("Subscription id = " + subscriptionId);
         
         // read the new rule wanted to be added
        BufferedReader reader = request.getReader();
        String endpointStr = "";
        String line;

        while ((line = reader.readLine()) != null) {
            endpointStr += line;
        } // while

        reader.close();
        
        // set the given header to the response or create it
        response.setHeader(CommonConstants.HEADER_CORRELATOR_ID, ManagementInterfaceUtils.setCorrelator(request));
        
        // Create a Gson object parsing the Json string
        Gson gson = new Gson();
        OrionEndpoint endpoint;
        
        try {
            endpoint = gson.fromJson(endpointStr, OrionEndpoint.class);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, malformed Json. Check it for errors\"");
            LOGGER.error("Parse error, malformed Json. Check it for errors.\"");
            return;
        } // try catch
                       
        // check if the endpoint are valid
        int err;
        
        if (endpoint != null) {
            err = endpoint.isValid();
        } else {
            // missing entire endpoint -> missing endpoint (code nº21)
            err = 21;
        } // if else
        
        if (err > 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            try {
                manageErrorMsg(err, response);
                return;
            } catch (Exception e) {
                Logger.getLogger(e.getMessage());
            } // try catch
        } // if
        
        LOGGER.debug("Valid Endpoint. Creating request to Orion.");
        
        // get host, port and ssl for request
        String host = endpoint.getHost();
        String port = endpoint.getPort();
        boolean ssl = Boolean.valueOf(endpoint.getSsl());
        String token = endpoint.getAuthToken();

        // Create a orionBackend for request
        orionBackend = new OrionBackendImpl(host, port, ssl, MAX_CONNS, MAX_CONNS_PER_ROUTE);
        
        try {
            
            JsonResponse orionResponse = null;
            int status = -1;
            JSONObject orionJson = new JSONObject();
            
            if (ngsiVersion.equals("1")) {
                orionResponse = orionBackend.
                    deleteSubscriptionV1(subscriptionId, token, fiwareService, fiwareServicePath);
                if (orionResponse != null) {
                    orionJson = orionResponse.getJsonObject();
                    JSONObject statusCode = (JSONObject) orionJson.get("statusCode");
                    String code = (String) statusCode.get("code");
                    status = Integer.parseInt(code);
                } // if
            } else if (ngsiVersion.equals("2")) {
                orionResponse = orionBackend.
                    deleteSubscriptionV2(subscriptionId, token, fiwareService, fiwareServicePath);
                if (orionResponse != null) {
                    orionJson = orionResponse.getJsonObject();
                    status = orionResponse.getStatusCode();
                } // if
            } // if else
            
            LOGGER.debug("Status code obtained: " + status);
            
            if ((status == 204) || (status == 200)) {
                response.getWriter().println("{\"success\":\"true\",\"result\":\" Subscription deleted\"}");
                LOGGER.debug("Subscription deleted succesfully.");
            } else {
                response.getWriter().println("{\"success\":\"false\",\"result\":" + orionJson.toJSONString() + "}");
            } // if else
            
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            response.getWriter().println("{\"success\":\"false\",\"result\":" + e.getMessage() + "}");
        } // try catch
        
    } // handleDeleteSubscription
    
    protected void handleDeleteAdminConfigurationAgent (HttpServletRequest request, HttpServletResponse response,
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
    
    protected void handleDeleteAdminConfigurationInstance (HttpServletRequest request, HttpServletResponse response,
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

        String transient_ = request.getParameter("transient");
        String appenderName = request.getParameter("name");
        boolean allAppenders = true;

        if (appenderName != null) {
            allAppenders = false;
        } // if

        String pathToFile = configurationPath + "/log4j.properties";
        File file = new File(pathToFile);

        if ((transient_ == null) || (transient_.equals("true"))) {

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
                    response.getWriter().println("{\"success\":\"true\",\"result\":\"Appender '"+ appenderName 
                            +"'removed succesfully\"}");
                    LOGGER.debug("Log4j appender removed succesfully");
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\",\"result\":\"Appender name not found\"}");
                    LOGGER.debug("Appender name not found");
                } // try catch

            } // if else

        } else if (transient_.equals("false")){
            
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
        }// if else if

    } // handleDeleteAdminLogAppenders        
    
    private void handlePutStats(HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        for (String key : sources.keySet()) {
            Source source;
            HTTPSourceHandler handler;
            
            try {
                SourceRunner sr = sources.get(key);
                source = sr.getSource();
                Field f = source.getClass().getDeclaredField("handler");
                f.setAccessible(true);
                handler = (HTTPSourceHandler) f.get(source);
            } catch (IllegalArgumentException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } catch (IllegalAccessException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } catch (NoSuchFieldException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } catch (SecurityException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } // try catch
            
            if (handler instanceof CygnusHandler) {
                CygnusHandler ch = (CygnusHandler) handler;
                ch.setNumProcessedEvents(0);
                ch.setNumReceivedEvents(0);
            } // if
        } // for

        for (String key : channels.keySet()) {
            Channel channel = channels.get(key);
            
            if (channel instanceof CygnusChannel) {
                CygnusChannel cc = (CygnusChannel) channel;
                cc.setNumPutsOK(0);
                cc.setNumPutsFail(0);
                cc.setNumTakesOK(0);
                cc.setNumTakesFail(0);
            } // if
        } // for

        for (String key : sinks.keySet()) {
            Sink sink;
            
            try {
                SinkRunner sr = sinks.get(key);
                SinkProcessor sp = sr.getPolicy();
                Field f = sp.getClass().getDeclaredField("sink");
                f.setAccessible(true);
                sink = (Sink) f.get(sp);
            } catch (IllegalArgumentException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } catch (IllegalAccessException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } catch (NoSuchFieldException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } catch (SecurityException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } // try catch

            if (sink instanceof CygnusSink) {
                CygnusSink cs = (CygnusSink) sink;
                cs.setNumProcessedEvents(0);
                cs.setNumPersistedEvents(0);
            } // if
        } // for
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{\"success\":\"true\"}");
        LOGGER.debug("Statistics reseted");
    } // handlePutStats

    private void handlePutGroupingRules(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");

        // read the new rule wanted to be added
        BufferedReader reader = request.getReader();
        String ruleStr = "";
        String line;

        while ((line = reader.readLine()) != null) {
            ruleStr += line;
        } // while

        reader.close();
        
        // set the given header to the response or create it
        response.setHeader(CommonConstants.HEADER_CORRELATOR_ID, ManagementInterfaceUtils.setCorrelator(request));

        // get the rule ID to be updated
        long id = new Long(request.getParameter("id"));

        // check the Json syntax of the new rule
        JSONParser jsonParser = new JSONParser();
        JSONObject rule;

        try {
            rule = (JSONObject) jsonParser.parse(ruleStr);
        } catch (ParseException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, invalid Json syntax. Details: " + e.getMessage() + "\"}");
            LOGGER.error("Parse error, invalid Json syntax. Details: " + e.getMessage());
            return;
        } // try catch

        // check if the rule is valid (it could be a valid Json document,
        // but not a Json document describing a rule)
        int err = CygnusGroupingRule.isValid(rule, true);

        if (err > 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            switch (err) {
                case 1:
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"error\":\"Invalid grouping rule, some field is missing\"}");
                    LOGGER.warn("Invalid grouping rule, some field is missing");
                    return;
                case 2:
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"error\":\"Invalid grouping rule, some field is empty\"}");
                    LOGGER.warn("Invalid grouping rule, some field is empty");
                    return;
                case 3:
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"error\":\"Invalid grouping rule, some field is not allowed\"}");
                    LOGGER.warn("Invalid grouping rule, some field is not allowed");
                    return;
                default:
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"error\":\"Invalid grouping rule\"}");
                    LOGGER.warn("Invalid grouping rule");
                    return;
            } // swtich
        } // if

        if (groupingRulesConfFile == null) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Missing configuration file for Grouping Rules\"}");
            LOGGER.error("Missing configuration file for Grouping Rules");
            return;
        } // if

        if (!new File(groupingRulesConfFile).exists()) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Configuration file for Grouing Rules not found. Details: "
                    + groupingRulesConfFile + "\"}");
            LOGGER.error("Configuration file for Grouing Rules not found. Details: " + groupingRulesConfFile);
            return;
        } // if

        CygnusGroupingRules groupingRules = new CygnusGroupingRules(groupingRulesConfFile);

        if (groupingRules.updateRule(id, new CygnusGroupingRule(rule))) {
            PrintWriter writer = new PrintWriter(new FileWriter(groupingRulesConfFile));
            writer.println(groupingRules.toString(false));
            writer.flush();
            writer.close();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("{\"success\":\"true\"}");
            LOGGER.debug("Rule updated. Details: id=" + id);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"The specified rule ID does not exist. Details: " + id + "\"}");
            LOGGER.error("The specified rule ID does not exist. Details: id=" + id);
        } // if else
    } // handlePutGroupingRules
    
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
    protected void handlePutAdminConfigurationInstance (HttpServletRequest request, HttpServletResponse response,
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
    
    private void handlePutAdminLogAppenders(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        // read the new appender wanted to be added
        BufferedReader reader = request.getReader();
        String jsonStr = "";
        String line;
        
        while ((line = reader.readLine()) != null) {
            jsonStr += line;
        } // while

        reader.close();
        if (!jsonStr.isEmpty()) {
            JsonObject jsonAppender = new JsonParser().parse(jsonStr).getAsJsonObject(); 
            try {
                JsonObject appender = jsonAppender.get("appender").getAsJsonObject();
                String name = appender.get("name").getAsString();
                JsonObject layout = jsonAppender.get("pattern").getAsJsonObject();
                String pattern = layout.get("ConversionPattern").getAsString();
                String transient_ = request.getParameter("transient");
                String pathToFile = configurationPath + "/log4j.properties";
                File file = new File(pathToFile);
                
                if ((transient_ == null) || (transient_.equals("true"))) {
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

                } else if (transient_.equals("false")) {

                    if (file.exists()) {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        Properties properties = new Properties();
                        properties.load(fileInputStream);
                        String class_ = appender.get("class").getAsString();
                        String layoutStr = layout.get("layout").getAsString();
                        Map<String, String> descriptions = ManagementInterfaceUtils.readLogDescriptions(file);
                        boolean isUpdate = false;

                        if (properties.contains("log4j.appender." + name)) {
                            isUpdate = true;
                        } // if

                        String propertyName = "log4j.appender." + name;
                        String propertyLayout = "log4j.appender." + name + ".layout";
                        String propertyPattern = "log4j.appender." + name + ".layout.ConversionPattern";
                        properties.put(propertyName, class_);
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
                }// if else if
                
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
    
        private void handlePutAdminLogLoggers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        // read the new rule wanted to be added
        BufferedReader reader = request.getReader();
        String jsonStr = "";
        String line;
        
        while ((line = reader.readLine()) != null) {
            jsonStr += line;
        } // while

        reader.close();
        
        if (!jsonStr.isEmpty()) {
            JsonObject jsonLogger = new JsonParser().parse(jsonStr).getAsJsonObject(); 
            try {
                JsonObject logger = jsonLogger.get("logger").getAsJsonObject();
                String name = logger.get("name").getAsString();
                String level = logger.get("level").getAsString();

                try {
                    CommonConstants.LoggingLevels.valueOf(level);
                    String transient_ = request.getParameter("transient");
                    String pathToFile = configurationPath + "/log4j.properties";
                    File file = new File(pathToFile);

                    if ((transient_ == null) || (transient_.equals("true"))) {
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

                    } else if (transient_.equals("false")){

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
                    }// if else if

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
            }
            
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\":\"Missing input JSON\"}");
            LOGGER.debug("Missing input JSON");
        }
        
    } // handlePutAdminLogLoggers
    
    private void handleDeleteGroupingRules(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setContentType("application/json; charset=utf-8");

        // get the rule ID to be deleted
        long id = new Long(request.getParameter("id"));
        
        // set the given header to the response or create it
        response.setHeader(CommonConstants.HEADER_CORRELATOR_ID, ManagementInterfaceUtils.setCorrelator(request));

        if (groupingRulesConfFile == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Missing configuration file for Grouping Rules\"}");
            LOGGER.error("Missing configuration file for Grouping Rules");
            return;
        } // if

        if (!new File(groupingRulesConfFile).exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Configuration file for Grouing Rules not found. Details: "
                    + groupingRulesConfFile + "\"}");
            LOGGER.error("Configuration file for Grouing Rules not found. Details: " + groupingRulesConfFile);
            return;
        } // if

        CygnusGroupingRules groupingRules = new CygnusGroupingRules(groupingRulesConfFile);

        if (groupingRules.deleteRule(id)) {
            PrintWriter writer = new PrintWriter(new FileWriter(groupingRulesConfFile));
            writer.println(groupingRules.toString(false));
            writer.flush();
            writer.close();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("{\"success\":\"true\"}");
            LOGGER.debug("Rule deleted. Details: id=" + id);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"The specified rule ID does not exist. Details: " + id + "\"}");
            LOGGER.error("The specified rule ID does not exist. Details: id=" + id);
        } // if else
    } // handleDeleteGroupingRules
    
    private void handleDeleteAdminLogLoggers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");

        String transient_ = request.getParameter("transient");
        String loggerName = request.getParameter("name");
        boolean allLoggers = true;

        if (loggerName != null) {
            allLoggers = false;
        } // if

        String pathToFile = configurationPath + "/log4j.properties";
        File file = new File(pathToFile);

        if ((transient_ == null) || (transient_.equals("true"))) {
            Enumeration<Logger> loggers = LogManager.getLoggerRepository().getCurrentLoggers();

            if (allLoggers) {
                
                while (loggers.hasMoreElements()) {
                    loggers.nextElement().setLevel(Level.OFF);
                } // while
                
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("{\"success\":\"true\",\"result\":\"Loggers removed succesfully\"}");
                LOGGER.debug("Log4j loggers removed succesfully");
                
            } else {

                ArrayList<Logger> loggerNames = new ArrayList<Logger>();

                while (loggers.hasMoreElements()) {
                    loggerNames.add(loggers.nextElement());
                } // while 

                if (loggerNames.contains(LogManager.getLogger(loggerName))) {
                    LogManager.getLogger(loggerName).setLevel(Level.OFF);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println("{\"success\":\"true\",\"result\":\"Logger '"+ loggerName 
                        +"' removed succesfully\"}");
                    LOGGER.debug("Log4j logger removed succesfully");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("{\"success\":\"false\",\"result\":\"Logger name not found\"}");
                    LOGGER.debug("Logger name not found");
                } // if else
                
            } // if else if
            
        } else if (transient_.equals("false")) {
            
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
        }// if else if

    } // handleDeleteAdminLogLoggers

} // ManagementInterface
