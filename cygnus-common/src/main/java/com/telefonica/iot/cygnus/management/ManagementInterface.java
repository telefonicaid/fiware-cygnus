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
import com.telefonica.iot.cygnus.sinks.CygnusSink;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.logging.Logger;
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
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.json.simple.JSONObject;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.CommonConstants.LoggingLevels;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.MDC;
/**
 *
 * @author frb
 */
public class ManagementInterface extends AbstractHandler {

    private static final CygnusLogger LOGGER = new CygnusLogger(ManagementInterface.class);
    private final File configurationFile;
    private String groupingRulesConfFile;
    private final ImmutableMap<String, SourceRunner> sources;
    private final ImmutableMap<String, Channel> channels;
    private final ImmutableMap<String, SinkRunner> sinks;
    private final int apiPort;
    private final int guiPort;
    private static int numPoints = 0;
    private static final String sourceRows = "";
    private static String channelRows = "";
    private static final String sinkRows = "";
    private OrionBackendImpl orionBackend;
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
            if (method.equals("GET")) {
                if (uri.equals("/v1/version")) {
                    handleGetVersion(response);
                } else if (uri.equals("/v1/stats")) {
                    handleGetStats(response);
                } else if (uri.equals("/v1/groupingrules")) {
                    handleGetGroupingRules(response);
                } else if (uri.equals("/admin/log")) {
                    handleGetAdminLog(request, response);
                } else if (uri.equals("/v1/subscriptions")) {
                    handleGetSubscriptions(request, response);
                } else if (uri.startsWith("/admin/configuration/agent")) {
                    handleGetAdminConfigurationAgent(request, response, false);
                } else if (uri.startsWith("/v1/admin/configuration/agent")) {
                    handleGetAdminConfigurationAgent(request, response, true);
                } else if (uri.startsWith("/admin/configuration/instance")) {
                    handleGetAdminConfigurationInstance(request, response, false);
                } else if (uri.startsWith("/v1/admin/configuration/instance")) {
                    handleGetAdminConfigurationInstance(request, response, true);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    response.getWriter().println(method + " " + uri + " Not implemented");
                } // if else
            } else if (method.equals("POST")) {
                if (uri.equals("/v1/groupingrules")) {
                    handlePostGroupingRules(request, response);
                } else if (uri.equals("/v1/subscriptions")) {
                    handlePostSubscription(request, response);
                } else if (uri.startsWith("/admin/configuration/agent")) {
                    handlePostAdminConfigurationAgent(request, response, false);
                } else if (uri.startsWith("/v1/admin/configuration/agent")) {
                    handlePostAdminConfigurationAgent(request, response, true);
                } else if (uri.startsWith("/admin/configuration/instance")) {
                    handlePostAdminConfigurationInstance(request, response, false);
                } else if (uri.startsWith("/v1/admin/configuration/instance")) {
                    handlePostAdminConfigurationInstance(request, response, true); 
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    response.getWriter().println(method + " " + uri + " Not implemented");
                } // if else
            } else if (method.equals("PUT")) {
                if (uri.equals("/v1/stats")) {
                    handlePutStats(response);
                } else if (uri.equals("/v1/groupingrules")) {
                    handlePutGroupingRules(request, response);
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
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    response.getWriter().println(method + " " + uri + " Not implemented");
                } // if else
            } else if (method.equals("DELETE")) {
                if (uri.equals("/v1/groupingrules")) {
                    handleDeleteGroupingRules(request, response);
                } else if (uri.equals("/v1/subscriptions")) {
                    handleDeleteSubscription(request, response);
                } else if (uri.startsWith("/admin/configuration/agent")) {
                    handleDeleteAdminConfigurationAgent(request, response, false);
                } else if (uri.startsWith("/v1/admin/configuration/agent")) {
                    handleDeleteAdminConfigurationAgent(request, response, true);
                } else if (uri.startsWith("/admin/configuration/instance")) {
                    handleDeleteAdminConfigurationInstance(request, response, false);
                } else if (uri.startsWith("/v1/admin/configuration/instance")) {
                    handleDeleteAdminConfigurationInstance(request, response, true);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    response.getWriter().println(method + " " + uri + " Not implemented");
                } // if else
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                response.getWriter().println(method + " " + uri + " Not implemented");
            } // if else
        } else if (port == guiPort) {
            if (method.equals("GET")) {
                if (uri.equals("/")) {
                    handleGetGUI(response);
                } else if (uri.endsWith("/points")) {
                    handleGetPoints(response);
                } else if (uri.equals("/stats")) { // this is order to avoid CORS access control
                    handleGetStats(response);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    response.getWriter().println(method + " " + uri + " Not implemented");
                } // if else
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                response.getWriter().println(method + " " + uri + " Not implemented");
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
    
    private void handleGetAdminLog(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        Level level = LogManager.getRootLogger().getLevel();
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{\"level\":\"" + level + "\"}");
        LOGGER.info("Cygnus logging level successfully obtained");
    } // handleGetAdminLog
    
    protected void handleGetSubscriptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        // flag for use get all or get one subscription
        boolean getAllSubscriptions = false;
        
        // get the parameters to be updated
        String ngsiVersion = request.getParameter("ngsi_version");
        String subscriptionID = request.getParameter("subscription_id");
        
        if (ngsiVersion == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (ngsi_version). Check it for errors.\"}");
            LOGGER.error("Parse error, missing parameter (ngsi_version). Check it for errors.");
            return;
        } else if (ngsiVersion.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (ngsi_version). Check it for errors.\"}");
            LOGGER.error("Parse error, empty parameter (ngsi_version). Check it for errors.");
            return;
        } // if else
        
        if (!((ngsiVersion.equals("1")) || (ngsiVersion.equals("2")))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, invalid parameter (ngsi_version): "
                + "Must be 1 or 2. Check it for errors.\"}");
            LOGGER.error("Parse error, invalid parameter (ngsi_version): "
                + "Must be 1 or 2. Check it for errors.");
            return;
        } // if
        
        if (ngsiVersion.equals("1")) {
            response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"GET /v1/subscriptions not implemented for NGSI version 1.\"}");
            LOGGER.error("GET /v1/subscriptions not implemented.");
            return;
        } // if
        
        if (subscriptionID == null) {
            getAllSubscriptions = true;
        } else if (subscriptionID.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (subscription_id). Check it for errors.\"}");
            LOGGER.error("Parse error, empty parameter (subscription_id). Check it for errors.");
            return;
        } // if else

        // set the given header to the response or create it
        response.setHeader(CommonConstants.HEADER_CORRELATOR_ID, 
                setCorrelator(request));
                
        BufferedReader reader = request.getReader();
        String endpointStr = "";
        String line;

        while ((line = reader.readLine()) != null) {
            endpointStr += line;
        } // while

        reader.close();
        
        // Create a Gson object parsing the Json string
        Gson gson = new Gson();
        OrionEndpoint endpoint;
        
        try {
            endpoint = gson.fromJson(endpointStr, OrionEndpoint.class);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, malformed Json. Check it for errors.\"}");
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
        
        if (getAllSubscriptions) {
            LOGGER.debug("Valid Endpoint. Creating request to Orion (GET all subscriptions).");
        } else {
            LOGGER.debug("Valid Endpoint. Creating request to Orion (GET subsription by id).");
        }
        
        // get host, port and ssl for request
        String host = endpoint.getHost();
        String port = endpoint.getPort();
        boolean ssl = Boolean.valueOf(endpoint.getSsl());
        String token = endpoint.getAuthToken();

        // Create a orionBackend for request
        orionBackend = new OrionBackendImpl(host, port, ssl);
        
        if (getAllSubscriptions)  {
            
            try { 
                int status;
                JSONObject orionJson = new JSONObject();

                JsonResponse orionResponse = orionBackend.
                        getSubscriptionsV2(token, subscriptionID);
                                
                if (orionResponse != null) {
                    orionJson = orionResponse.getJsonObject();
                    status = orionResponse.getStatusCode();
                } else {
                    response.getWriter().println("{\"success\":\"false\","
                        + "\"result\" : { \"There was some problem when handling the response\" }");
                    LOGGER.debug("There was some problem when handling the response");
                    return;
                } // if else

                LOGGER.debug("Status code obtained: " + status);

                if (status == 200) {
                    response.getWriter().println("{\"success\":\"true\","
                                + "\"result\" : {" + orionJson.toJSONString() + "}");
                    LOGGER.debug("Subscription received: " + orionJson.toJSONString());
                } else {
                    response.getWriter().println("{\"success\":\"false\","
                                + "\"result\" : {" + orionJson.toJSONString() + "}");
                } // if else
            
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                response.getWriter().println("{\"success\":\"false\","
                                + "\"result\" : {" + e.getMessage() + "}");
            } // try catch
            
        } else {
            
            try { 
                int status = -1;
                JSONObject orionJson = new JSONObject();

                JsonResponse orionResponse = orionBackend.
                        getSubscriptionsByIdV2(token, subscriptionID);

                if (orionResponse != null) {
                    orionJson = orionResponse.getJsonObject();
                    status = orionResponse.getStatusCode();
                } else {
                    response.getWriter().println("{\"success\":\"false\","
                        + "\"result\" : { \"There was some problem when handling the response\" }");
                    LOGGER.debug("There was some problem when handling the response");
                    return;
                } // if else

                LOGGER.debug("Status code obtained: " + status);

                if (status == 200) {
                    response.getWriter().println("{\"success\":\"true\","
                                + "\"result\" : {" + orionJson.toJSONString() + "}");
                    LOGGER.debug("Subscription received: " + orionJson.toJSONString());
                } else {
                    response.getWriter().println("{\"success\":\"false\","
                                + "\"result\" : {" + orionJson.toJSONString() + "}");
                } // if else
            
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                response.getWriter().println("{\"success\":\"false\","
                                + "\"result\" : {" + e.getMessage() + "}");
            } // try catch
            
        }
        
    } // handleGetSubscriptions
    
    protected void handleGetAdminConfigurationAgent(HttpServletRequest request, HttpServletResponse response, 
            boolean v1) throws IOException {
        
        response.setContentType("application/json; charset=utf-8");
        boolean allParameters = false;
        
        String param = request.getParameter("param");
        String url = request.getRequestURI();     
        String fileName = getFileName(url);
        
        if (!(fileName.startsWith("agent_"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Agent file name must start with 'agent_'.\"}");
            LOGGER.error("Agent file name must start with 'agent_'.");
            return;
        } // if
        
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
                        + "\"result\" : {\"Param '" + param + "' not found in the agent\"}"); 
                    return;
                } // if else
                
            } // if else
            
            response.getWriter().println("{\"success\":\"true\","
                    + "\"result\" : " + jsonObject + "");
            LOGGER.debug(jsonObject);
            response.setStatus(HttpServletResponse.SC_OK);
            
        } else {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\" : { \"File not found in the path received\" }");
            LOGGER.debug("File not found in the path received");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // if else    
        
    } // handleGetAdminParameters
    
    protected void handleGetAdminConfigurationInstance (HttpServletRequest request, HttpServletResponse response, 
            boolean v1) throws IOException {
        
        response.setContentType("application/json; charset=utf-8");
        boolean allParameters = false;
        
        String param = request.getParameter("param");
        String url = request.getRequestURI();     
        String fileName = getFileName(url);
        
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
                + "\"result\" : {\"Invalid path for a instance configuration file\"}"); 
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
                        + "\"result\" : {\"Param '" + param + "' not found in the instance\"}"); 
                    return;
                } // if else
                
            } // if else
            
            response.getWriter().println("{\"success\":\"true\","
                    + "\"result\" : " + jsonObject + "}");
            LOGGER.debug(jsonObject);
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\" : {\"File not found in the path received\" }");
            LOGGER.debug("File not found in the path received. Details: " +  e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // if else    
        
    } // handleGetAdminConfigurationInstance

    private void handlePostGroupingRules(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        response.setHeader(CommonConstants.HEADER_CORRELATOR_ID, 
                setCorrelator(request)); 

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
        
        if ((ngsiVersion == null) || (ngsiVersion.equals(""))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, wrong parameter (ngsi_version). Check it for errors.\"}");
            LOGGER.error("Parse error, wrong parameter (ngsi_version). Check it for errors.");
            return;
        } 
        
        if (!((ngsiVersion.equals("1")) || (ngsiVersion.equals("2")))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, invalid parameter (ngsi_version): "
                    + "Must be 1 or 2. Check it for errors.\"}");
            LOGGER.error("Parse error, invalid parameter (ngsi_version): "
                    + "Must be 1 or 2. Check it for errors.");
            return;
        }
        
        // set the given header to the response or create it
        response.setHeader(CommonConstants.HEADER_CORRELATOR_ID, 
                setCorrelator(request)); 

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
                    + "\"error\":\"Parse error, malformed Json. Check it for errors.\"");
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
                } // if
                 catch (Exception e) {
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
            orionBackend = new OrionBackendImpl(host, port, ssl);
        
            // Get /subscription JSON from entire one
            JsonObject inputJson = new JsonParser().parse(jsonStr).getAsJsonObject();
            String subscriptionStr = inputJson.get("subscription").toString();
        
            JsonResponse orionResponse = null;
            int status = -1;
            JSONObject orionJson = new JSONObject();
            
            try {
                orionResponse = orionBackend.subscribeContextV1(subscriptionStr, token);
                status = orionResponse.getStatusCode();
                orionJson = orionResponse.getJsonObject();
                
                if (status == 200) {
                    response.getWriter().println("{\"success\":\"true\","
                            + "\"result\" : {" + orionJson.toJSONString() + "}");
                    LOGGER.debug("Subscribed.");
                } else {
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"result\" : {" + orionJson.toJSONString() + "}");
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
                    + "\"error\":\"Parse error, malformed Json. Check it for errors.\"");
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
                } // if
                 catch (Exception e) {
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
            orionBackend = new OrionBackendImpl(host, port, ssl);
        
            // Get /subscription JSON from entire one
            JsonObject inputJson = new JsonParser().parse(jsonStr).getAsJsonObject();
            String subscriptionStr = inputJson.get("subscription").toString();
        
            JsonResponse orionResponse = null;
            int status = -1;
            JSONObject orionJson = new JSONObject();
            
            try {
                orionResponse = orionBackend.subscribeContextV2(subscriptionStr, token);   
                status = orionResponse.getStatusCode();
                
                if (status == 201) {
                    String location = orionResponse.getLocationHeader().getValue();
                    response.getWriter().println("{\"success\":\"true\","
                            + "\"result\" : { SubscriptionID = " + location.substring(18) + "}");
                    LOGGER.debug("Subscribed.");
                } else {
                    orionJson = orionResponse.getJsonObject();
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"result\" : {" + orionJson.toString()+ "}");
                } // if else
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            } // try catch
        } // if else if
        
    } // handlePostSubscription
    
    protected void handlePostAdminConfigurationAgent(HttpServletRequest request, HttpServletResponse response,
            boolean v1) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        String param = request.getParameter("param");
        String newValue = request.getParameter("value");
        String url = request.getRequestURI();
        String fileName = getFileName(url);
        
        if (!(fileName.startsWith("agent_"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Agent file name must start with 'agent_'.\"}");
            LOGGER.error("Agent file name must start with 'agent_'.");
            return;
        } // if
                
        if (param == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (param). Check it for errors.\"}");
            LOGGER.error("Parse error, missing parameter (param). Check it for errors.");
            return;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param). Check it for errors.\"}");
            LOGGER.error("Parse error, empty parameter (param). Check it for errors.");
            return;
        } // if else
        
        if (newValue == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (value). Check it for errors.\"}");
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
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"result\" : " + jsonObject + "}");
                    LOGGER.debug(jsonObject);
                    return;
                } // if
                
            } // for
            
            properties.put(param, newValue);                       
            jsonObject.put("agent", properties);                        
            orderedPrinting(properties, file);
                               
            response.getWriter().println("{\"success\":\"true\","
                    + "\"result\" : " + jsonObject + "}");
            LOGGER.debug(jsonObject);
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\" : { \"File not found in the path received\" }");
            LOGGER.debug("File not found in the path received. Details: " +  e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // if else    
        
    } // handlePostAdminConfigurationAgent
    
    protected void handlePostAdminConfigurationInstance (HttpServletRequest request, HttpServletResponse response,
            boolean v1) throws IOException {
        
        response.setContentType("application/json; charset=utf-8");
        
        String param = request.getParameter("param");
        String newValue = request.getParameter("value");
        String url = request.getRequestURI();
        String fileName = getFileName(url);
                
        if (param == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (param). Check it for errors.\"}");
            LOGGER.error("Parse error, missing parameter (param). Check it for errors.");
            return;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param). Check it for errors.\"}");
            LOGGER.error("Parse error, empty parameter (param). Check it for errors.");
            return;
        } // if else
        
        if (newValue == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (value). Check it for errors.\"}");
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
                + "\"result\" : {\"Invalid path for a instance configuration file\"}"); 
            return;
        } // if
                
        File file = new File(pathToFile);
        
        try {
            Properties properties = new Properties();           
            properties.load(new FileInputStream(file));
            JSONObject jsonObject = new JSONObject();          
            Map<String, String> descriptions = readDescriptions(file);
            
            for (Object key: properties.keySet()) {
                String name = (String) key;
                
                if (name.equals(param)) {
                    jsonObject.put("instance", properties);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"result\" : " + jsonObject + "}");
                    LOGGER.debug(jsonObject);
                    return;
                } // if
                
            } // for
            
            properties.put(param, newValue);                       
            jsonObject.put("instance", properties);                        
            instancePrinting(properties, file, descriptions);
                               
            response.getWriter().println("{\"success\":\"true\","
                    + "\"result\" : " + jsonObject + "}");
            LOGGER.debug(jsonObject);
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\" : {\"File not found in the path received\"}");
            LOGGER.debug("File not found in the path received. Details: " +  e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // if else    
        
    } // handlePostAdminConfigurationInstance
    
    protected void handleDeleteSubscription(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        String subscriptionId = request.getParameter("subscription_id");
        String ngsiVersion = request.getParameter("ngsi_version");
        
        if ((subscriptionId == null) || (subscriptionId.equals(""))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, wrong parameter (subscription_id). Check it for errors.\"}");
            LOGGER.error("Parse error, wrong parameter (subscription_id). Check it for errors.");
            return;
        } // if
        
        if ((ngsiVersion == null) || (ngsiVersion.equals(""))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, wrong parameter (ngsi_version). Check it for errors.\"}");
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
        response.setHeader(CommonConstants.HEADER_CORRELATOR_ID, 
                setCorrelator(request));
        
        // Create a Gson object parsing the Json string
        Gson gson = new Gson();
        OrionEndpoint endpoint;
        
        try {
            endpoint = gson.fromJson(endpointStr, OrionEndpoint.class);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, malformed Json. Check it for errors.\"");
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
        orionBackend = new OrionBackendImpl(host, port, ssl);
        
        try {
            
            JsonResponse orionResponse = null;
            int status = -1;
            JSONObject orionJson = new JSONObject();
            
            if (ngsiVersion.equals("1")) {
                orionResponse = orionBackend.
                    deleteSubscriptionV1(subscriptionId, token);
                if (orionResponse != null) {
                    orionJson = orionResponse.getJsonObject();
                    JSONObject statusCode = (JSONObject) orionJson.get("statusCode");
                    String code = (String) statusCode.get("code");
                    status = Integer.parseInt(code);
                } // if
            } else if (ngsiVersion.equals("2")) {
                orionResponse = orionBackend.
                    deleteSubscriptionV2(subscriptionId, token);
                if (orionResponse != null) {
                    orionJson = orionResponse.getJsonObject();
                    status = orionResponse.getStatusCode();
                } // if
            } // if else
            
            LOGGER.debug("Status code obtained: " + status);
            
            if ((status == 204) || (status == 200)) {
                response.getWriter().println("{\"success\":\"true\","
                            + "\"result\" : {\" Subscription deleted \"}");
                LOGGER.debug("Subscription deleted succesfully.");
            } else {
                response.getWriter().println("{\"success\":\"false\","
                            + "\"result\" : {" + orionJson.toJSONString() + "}");
            } // if else
            
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            response.getWriter().println("{\"success\":\"false\","
                            + "\"result\" : {" + e.getMessage() + "}");
        } // try catch
        
    } // handleDeleteSubscription
    
    protected void handleDeleteAdminConfigurationAgent (HttpServletRequest request, HttpServletResponse response, 
            boolean v1) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        String param = request.getParameter("param");
        String url = request.getRequestURI();
        String fileName = getFileName(url);
        
        if (!(fileName.startsWith("agent_"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Agent file name must start with 'agent_'.\"}");
            LOGGER.error("Agent file name must start with 'agent_'.");
            return;
        } // if
                
        if (param == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (param). Check it for errors.\"}");
            LOGGER.error("Parse error, missing parameter (param). Check it for errors.");
            return;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param). Check it for errors.\"}");
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
            orderedPrinting(properties, file);
            
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
                    + "\"result\" : { \"File not found in the path received\" }");
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
                + "\"error\":\"Parse error, missing parameter (param). Check it for errors.\"}");
            LOGGER.error("Parse error, missing parameter (param). Check it for errors.");
            return;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param). Check it for errors.\"}");
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
            Map<String,String> descriptions = readDescriptions(file);
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
            instancePrinting(properties, file, descriptions);  
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
                    + "\"result\" : { \"File not found in the path received\" }");
            LOGGER.debug("File not found in the path received. Details: " +  e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // try catch    
        
    } // handleDeleteAdminConfigurationAgent
    
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
        response.setHeader(CommonConstants.HEADER_CORRELATOR_ID, 
                setCorrelator(request)); 

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
    
    private void handlePutAdminLog(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        // get the parameters to be updated
        String logLevel = request.getParameter("level");
        
        try {
            LoggingLevels.valueOf(logLevel.toUpperCase());
            LogManager.getRootLogger().setLevel(Level.toLevel(logLevel.toUpperCase()));
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("{\"success\":\"log4j logging level updated to " 
                    + logLevel.toUpperCase() + "\" }");
            LOGGER.info("log4j logging level updated to " + logLevel.toUpperCase());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"error\":\"Invalid log level\"}");
            LOGGER.error("Invalid log level '" + logLevel + "'");
        } // try catch
        
    } // handlePutAdminLog
    
    protected void handlePutAdminConfigurationAgent(HttpServletRequest request, HttpServletResponse response,
            boolean v1) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        String param = request.getParameter("param");
        String newValue = request.getParameter("value");
        String url = request.getRequestURI();
        String fileName = getFileName(url);
        
        if (!(fileName.startsWith("agent_"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Agent file name must start with 'agent_'.\"}");
            LOGGER.error("Agent file name must start with 'agent_'.");
            return;
        } // if
                
        if (param == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (param). Check it for errors.\"}");
            LOGGER.error("Parse error, missing parameter (param). Check it for errors.");
            return;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param). Check it for errors.\"}");
            LOGGER.error("Parse error, empty parameter (param). Check it for errors.");
            return;
        } // if else
        
        if (newValue == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (value). Check it for errors.\"}");
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
            
            orderedPrinting(properties, file);
                               
            response.getWriter().println("{\"success\":\"true\","
                    + "\"result\" : " + jsonObject + "}");
            LOGGER.debug(jsonObject);
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\" : { \"File not found in the path received\" }");
            LOGGER.debug("File not found in the path received. Details: " +  e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // if else    
        
    } // handlePutAdminConfigurationAgent
    
    protected void handlePutAdminConfigurationInstance (HttpServletRequest request, HttpServletResponse response,
            boolean v1) throws IOException {
        response.setContentType("json;charset=utf-8");
        
        String param = request.getParameter("param");
        String newValue = request.getParameter("value");
        String url = request.getRequestURI();
        String fileName = getFileName(url);
                
        if (param == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (param). Check it for errors.\"}");
            LOGGER.error("Parse error, missing parameter (param). Check it for errors.");
            return;
        } else if (param.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, empty parameter (param). Check it for errors.\"}");
            LOGGER.error("Parse error, empty parameter (param). Check it for errors.");
            return;
        } // if else
        
        if (newValue == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                + "\"error\":\"Parse error, missing parameter (value). Check it for errors.\"}");
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
                + "\"result\" : {\"Invalid path for a instance configuration file\"}"); 
            return;
        } // if
                
        File file = new File(pathToFile);
                
        try {
            Properties properties = new Properties();           
            properties.load(new FileInputStream(file));
            Map<String, String> descriptions = readDescriptions(file);
            properties.put(param, newValue);                                   
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("instance", properties);            
            instancePrinting(properties, file, descriptions);                               
            response.getWriter().println("{\"success\":\"true\","
                    + "\"result\" : " + jsonObject + "}");
            LOGGER.debug(jsonObject);
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            response.getWriter().println("{\"success\":\"false\","
                    + "\"result\" : {\"File not found in the path received\"}");
            LOGGER.debug("File not found in the path received. Details: " +  e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } // try catch   
        
    } // handlePutAdminConfigurationInstance
    
    private void handleDeleteGroupingRules(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setContentType("application/json; charset=utf-8");

        // get the rule ID to be deleted
        long id = new Long(request.getParameter("id"));
        
        // set the given header to the response or create it
        response.setHeader(CommonConstants.HEADER_CORRELATOR_ID, 
            setCorrelator(request)); 

        if (groupingRulesConfFile == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Missing configuration file for Grouping Rules\"");
            LOGGER.error("Missing configuration file for Grouping Rules");
            return;
        } // if

        if (!new File(groupingRulesConfFile).exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Configuration file for Grouing Rules not found. Details: "
                    + groupingRulesConfFile + "\"");
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

    private String getGroupingRulesConfFile() throws IOException {
        if (!configurationFile.exists()) {
            return "404 - Configuration file for Cygnus not found. Details: "
                    + configurationFile.toString();
        } // if

        String groupingRulesConfFile = null;
        BufferedReader reader = new BufferedReader(new FileReader(configurationFile));
        String line;

        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("#")) {
                if (line.contains("grouping_rules_conf_file")) {
                    String[] splits = line.split("=");
                    groupingRulesConfFile = splits[1].replaceAll(" ", "");
                    break;
                } // if
            } // if
        } // while

        reader.close();
        return groupingRulesConfFile;
    } // getGroupingRulesConfFile

    private void handleGetGUI(HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String indexJSP = "";
        BufferedReader reader = new BufferedReader(new FileReader(
                "src/main/java/com/telefonica/iot/cygnus/management/index.html"));
        String line;

        while ((line = reader.readLine()) != null) {
            indexJSP += line + "\n";
        } // while

        response.getWriter().println(indexJSP);
    } // handleGetGUI

    private void handleGetPoints(HttpServletResponse response) throws IOException {
        // add a new source row
        String sourceColumns = "";

        // add a new channel row
        String channelColumns = "\"count\"";
        String point = "[" + numPoints;

        for (String key : channels.keySet()) {
            Channel channel = channels.get(key);
            channelColumns += ",\"" + channel.getName() + "\"";

            if (channel instanceof CygnusChannel) {
                CygnusChannel cygnusChannel = (CygnusChannel) channel;
                point += "," + cygnusChannel.getNumEvents();
            } // if12
        } // for

        point += "]";

        if (channelRows.length() == 0) {
            channelRows += point;
        } else {
            channelRows += "," + point;
        } // if else

        // add a new sink row
        String sinkColumns = "";

        // increase the points counter
        numPoints++;

        // return the points
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(
                "{\"source_points\":{\"columns\":[" + sourceColumns + "],\"rows\":[" + sourceRows + "]},"
                + "\"channel_points\":{\"columns\":[" + channelColumns + "],\"rows\":[" + channelRows + "]},"
                + "\"sink_points\":{\"columns\":[" + sinkColumns + "],\"rows\":[" + sinkRows + "]}}");
    } // handleGetPoints
    
    private void manageErrorMsg(int err, HttpServletResponse response) throws Exception{
                
        switch (err) {            
            // cases of missing endpoint or subscription
            case 11:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Missing subscription\"}");
                LOGGER.error("Missing subscription");
                return;
            case 21:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Missing endpoint\"}");
                LOGGER.error("Missing endpoint");
                return;
      
            case 1211:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'entities' is missing\"}");
                LOGGER.error("Invalid subscription, field 'entities' is missing");
                return;
            case 1212:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'entities' has missing fields\"}");
                LOGGER.error("Invalid subscription, field 'entities' has missing fields");
                return;
            case 1213:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'entities' has empty fields\"}");
                LOGGER.error("Invalid subscription, field 'entities' has empty fields");
                return;

            case 122:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'reference' is missing\"}");
                LOGGER.error("Invalid subscription, field 'reference' is missing");
                return;
            case 123:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'duration' is missing\"}");
                LOGGER.error("Invalid subscription, field 'duration' is missing");
                return;

            // cases for 'notifyConditions'
            case 1241:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'notifyConditions' is missing\"}");
                LOGGER.error("Invalid subscription, field 'notifyConditions' is missing");
                return;
            case 1242:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'notifyConditions' has missing fields\"}");
                LOGGER.error("Invalid subscription, field 'notifyConditions' has missing fields");
                return;
            case 1243:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'notifyConditions' has empty fields\"}");
                LOGGER.error("Invalid subscription, field 'notifyConditions' has empty fields");
                return;

            case 125:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'throttling' is missing\"}");
                LOGGER.error("Invalid subscription, field 'throttling' is missing");
                return;
            case 126:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'attributes' is missing\"}");
                LOGGER.error("Invalid subscription, field 'attributes' is missing");
                return;

            case 131:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'entities' is empty\"}");
                LOGGER.error("Invalid subscription, field 'entities' is empty");
                return;
            case 1311:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'condition' is missing\"}");
                LOGGER.error("Invalid subscription, field 'condition' is missing");
                return;
            case 1312:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'condition' has missing fields\"}");
                LOGGER.error("Invalid subscription, field 'condition' has missing fields");
                return;
            case 1313:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'condition' has empty fields\"}");
                LOGGER.error("Invalid subscription, field 'condition' has empty fields");
                return;
            case 132:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'reference' is empty\"}");
                LOGGER.error("Invalid subscription, field 'reference' is empty");
                return;
            case 133:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'duration' is empty\"}");
                LOGGER.error("Invalid subscription, field 'duration' is empty");
                return;
            case 134:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'notifyConditions' is empty\"}");
                LOGGER.error("Invalid subscription, field 'notifyConditions' is empty");
                return;
            case 135:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'throttling' is empty\"}");
                LOGGER.error("Invalid subscription, field 'throttling' is empty");
                return;
                
            case 141:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'description' is missing\"}");
                LOGGER.error("Invalid subscription, field 'description' is missing");
                return;               
            case 142:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'description' is empty\"}");
                LOGGER.error("Invalid subscription, field 'description' is empty");
                return;
                
            case 15111:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'subject' is missing\"}");
                LOGGER.error("Invalid subscription, field 'subject' is missing");
                return;                 
            case 15112:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'subject' is empty\"}");
                LOGGER.error("Invalid subscription, field 'subject' is empty");
                return;           
            case 1512:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'subject' has missing fields\"}");
                LOGGER.error("Invalid subscription, field 'subject' has missing fields");
                return;           
            case 1513:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'subject' has empty fields\"}");
                LOGGER.error("Invalid subscription, field 'subject' has empty fields");
                return; 
             
            case 1611:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'notification' is missing\"}");
                LOGGER.error("Invalid subscription, field 'notification' is missing");
                return; 
            case 1612:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'notification' has missing fields\"}");
                LOGGER.error("Invalid subscription, field 'notification' has missing fields");
                return;    
            case 1613:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'notification' has empty fields\"}");
                LOGGER.error("Invalid subscription, field 'notification' has empty fields");
                return;
                
            case 171:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'expires' is missing\"}");
                LOGGER.error("Invalid subscription, field 'expires' is missing");
                return;               
            case 172:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription, field 'expires' is empty\"}");
                LOGGER.error("Invalid subscription, field 'expires' is empty");
                return;
                
            // cases of missing fields in endpoint
            case 221:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid endpoint, field 'host' is missing\"}");
                LOGGER.error("Invalid endpoint, field 'host' is missing");
                return;
            case 222:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid endpoint, field 'port' is missing\"}");
                LOGGER.error("Invalid endpoint, field 'port' is missing");
                return;
            case 223:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid endpoint, field 'ssl' is missing\"}");
                LOGGER.error("Invalid endpoint, field 'ssl' is missing");
                return;

            // cases of empty fields in endpoint
            case 231:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid endpoint, field 'host' is empty\"}");
                LOGGER.error("Invalid endpoint, field 'host' is empty");
                return;
            case 232:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid endpoint, field 'port' is empty\"}");
                LOGGER.error("Invalid endpoint, field 'port' is empty");
                return;
            case 233:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid endpoint, field 'ssl' is empty\"}");
                LOGGER.error("Invalid endpoint, field 'ssl' is empty");
                return;

            // cases of invalid ssl in endpoint
            case 24:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid endpoint, field 'ssl' invalid\"}");
                LOGGER.error("Invalid endpoint, field 'ssl' invalid");
                return;
                
            // case for authtoken missing
            case 51:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Missing Auth-Token. Required for API methods\"}");
                LOGGER.error("Invalid endpoint, missing 'xAuthToken'");
                return;

            // case for authtoken empty
            case 52:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Empty Auth-Token. Required for API methods\"}");
                LOGGER.error("Invalid endpoint, empty 'xAuthToken'");
                return;
                
            default:
                response.getWriter().println("{\"success\":\"false\","
                        + "\"error\":\"Invalid subscription\"}");
                LOGGER.error("Invalid subscription");
            } // switch
        
    } // manageErrorMsg 
    
    private String setCorrelator (HttpServletRequest request) {
        // Get an internal transaction ID.
        String transId = CommonUtils.generateUniqueId(null, null);
        
        // Get also a correlator ID if not sent in the notification. Id correlator ID is not notified
        // then correlator ID and transaction ID must have the same value.
        String corrId = CommonUtils.generateUniqueId
               (request.getHeader(CommonConstants.HEADER_CORRELATOR_ID), transId);
        
        // set the given header to the response or create it
        MDC.put(CommonConstants.LOG4J_CORR, corrId);
        MDC.put(CommonConstants.LOG4J_TRANS, transId);
        return corrId;
    } // setCorrelator
    
    
    /**
     * SetOrionBackend: Sets a given orionBackend.
     * 
     * @param orionBackend
     */
    protected void setOrionBackend(OrionBackendImpl orionBackend) {
        this.orionBackend = orionBackend;
    } // setOrionBackend
    
    /** 
     * OrderedPrintWriter: Creates a writer with the original order's agent file.
     * 
     * @param printWriter
     * @param properties 
     */
    private void orderedPrinting(Properties properties, File file) throws FileNotFoundException {
                
        PrintWriter printWriter = new PrintWriter(file);
        String agentName = "";
        
        for (Object key : properties.keySet()) {
            String name = (String) key;
            String[] nameParts = name.split("\\.");
            agentName = nameParts[0];
            break;
        } // for
                
        ArrayList<String> sourceNames = null;
        ArrayList<String> channelNames = null;
        ArrayList<String> sinkNames=  null;
        
        for (Object key : properties.keySet()) {
            String name = (String) key;
            String value = (String) properties.getProperty(name);
            
            if (name.equals(agentName + ".sources")) {
                sourceNames = new ArrayList<String>(Arrays.asList(value.split("\\s+")));
                printWriter.println(name + " = " + value);
            } // if
            
            if (name.equals(agentName + ".channels")) {
                channelNames = new ArrayList<String>(Arrays.asList(value.split("\\s+")));
                printWriter.println(name + " = " + value);
            } // if
            
            if (name.equals(agentName + ".sinks")) {
                sinkNames = new ArrayList<String>(Arrays.asList(value.split("\\s+")));
                printWriter.println(name + " = " + value);
            } // if
            
        } // for
        
        printWriter.println();
        
        for (String sourceName : sourceNames) {
            
            for (Object key : properties.keySet()) {
                String name = (String) key;
                String value = (String) properties.getProperty(name);
                
                if (name.startsWith(agentName + ".sources." + sourceName)) {
                    printWriter.println(name + " = " + value);
                } // if
                
            } // for
            
            printWriter.println();

        } // for
        
        for (String channelName : channelNames) {
            
            for (Object key : properties.keySet()) {
                String name = (String) key;
                String value = (String) properties.getProperty(name);
                
                if (name.startsWith(agentName + ".channels." + channelName)) {
                    printWriter.println(name + " = " + value);
                } // if
                
            } // for
            
            printWriter.println();

        } // for
        
        for (String sinkName : sinkNames) {
            
            for (Object key : properties.keySet()) {
                String name = (String) key;
                String value = (String) properties.getProperty(name);
                
                if (name.startsWith(agentName + ".sinks." + sinkName)) {
                    printWriter.println(name + " = " + value);
                } // if
                
            } // for
            
            printWriter.println();

        } // for
        
        printWriter.close();
        
    } // orderedPrinting
    
    private void instancePrinting (Properties properties, File file, Map<String,String> descriptions) 
            throws FileNotFoundException {
                
        PrintWriter printWriter = new PrintWriter(file);      
        printWriter.println(CommonConstants.CYGNUS_IPR_HEADER);
        printWriter.println();
        
        for (Object key : properties.keySet()) {
            String name = (String) key;
            String value = (String) properties.getProperty(name);
            
            if (descriptions.containsKey(name)) {
                String description = (String) descriptions.get(name);
                printWriter.print(description);
            } // if 
            
            printWriter.println(name + "=" + value);
            printWriter.println();
        } // for
        
        printWriter.close();
        
    } // instancePrinting
    
    /** 
    * getFileName: Gets the name of the agent from the given path.
    * 
    * @param url 
    */
    private String getFileName (String url) {
        String[] pathElements = url.split("/");
        return pathElements[pathElements.length - 1];
    } // getFileName
    
    /**
     * 
     */
    private Map<String,String> readDescriptions(File file) throws IOException {
                
        // read the comments and the properties
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String header = "";
        String description = "";
        String line;
        Map<String,String> descriptions = new LinkedHashMap<String,String>();

        while ((line = reader.readLine()) != null) {
            
            if (line.startsWith("#")) {
                description += line + "\n";
            } // if
            
            if (line.isEmpty()) {
                description = "";
            } else if ((!(line.startsWith("#")) && (!(line.isEmpty())))) {
                String[] nameValue = line.split("=");
                String name = nameValue[0];
                descriptions.put(name, description);
                description = "";
            } // if
            
        } // while
        
        reader.close();
        return descriptions;
    } // readDescriptions
    
} // ManagementInterface
