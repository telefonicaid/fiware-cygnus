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
import com.telefonica.iot.cygnus.backends.orion.OrionBackend;
import com.telefonica.iot.cygnus.backends.orion.OrionBackendImpl;
import com.telefonica.iot.cygnus.handlers.OrionRestHandler;
import com.telefonica.iot.cygnus.interceptors.GroupingRule;
import com.telefonica.iot.cygnus.interceptors.GroupingRules;
import com.telefonica.iot.cygnus.containers.CygnusSubscription;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.OrionSink;
import com.telefonica.iot.cygnus.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.logging.Level;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

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
    private static int numPoints = 0;
    private static String sourceRows = "";
    private static String channelRows = "";
    private static String sinkRows = "";
    private OrionBackendImpl subscriptionBackend;

    /**
     * Constructor.
     * @param configurationFile
     * @param sources
     * @param channels
     * @param sinks
     */
    public ManagementInterface(File configurationFile, ImmutableMap<String, SourceRunner> sources, ImmutableMap<String,
            Channel> channels, ImmutableMap<String, SinkRunner> sinks) {
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
        int port = request.getServerPort();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        LOGGER.info("Management interface request. Method: " + method + ", URI: " + uri);

        if (port == 8081) {
            if (method.equals("GET")) {
                if (uri.equals("/v1/version")) {
                    handleGetVersion(response);
                } else if (uri.equals("/v1/stats")) {
                    handleGetStats(response);
                } else if (uri.equals("/v1/groupingrules")) {
                    handleGetGroupingRules(response);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    response.getWriter().println(method + " " + uri + " Not implemented");
                } // if else
            } else if (method.equals("POST")) {
                if (uri.equals("/v1/groupingrules")) {
                    handlePostGroupingRules(request, response);
                } else if (uri.equals("/v1/subscriptions")) {
                    handlePostSubscription(request, response);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    response.getWriter().println(method + " " + uri + " Not implemented");
                } // if else
            } else if (method.equals("PUT")) {
                if (uri.equals("/v1/groupingrules")) {
                    handlePutGroupingRules(request, response);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    response.getWriter().println(method + " " + uri + " Not implemented");
                } // if else
            } else if (method.equals("DELETE")) {
                if (uri.equals("/v1/groupingrules")) {
                    handleDeleteGroupingRules(request, response);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    response.getWriter().println(method + " " + uri + " Not implemented");
                } // if else
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                response.getWriter().println(method + " " + uri + " Not implemented");
            } // if else
        } else if (port == 8082) {
            if (method.equals("GET")) {
                if (uri.equals("/")) {
                    handleGetGUI(response);
                } else if (uri.endsWith("/points")) {
                    handleGetPoints(response);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    response.getWriter().println(method + " " + uri + " Not implemented");
                } // if else
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                response.getWriter().println(method + " " + uri + " Not implemented");
            } // if else
        } else {
            LOGGER.debug("Attending a request in a non expected port!!");
        } // if else
    } // handle

    private void handleGetVersion(HttpServletResponse response) throws IOException {
        response.setContentType("json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{\"success\":\"true\",\"version\":\"" + Utils.getCygnusVersion()
                + "." + Utils.getLastCommit() + "\"}");
    } // handleGetVersion

    private void handleGetStats(HttpServletResponse response) throws IOException {
        response.setContentType("json;charset=utf-8");
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

            if (handler instanceof OrionRestHandler) {
                OrionRestHandler orh = (OrionRestHandler) handler;
                jsonStr += "\"setup_time\":\"" + Utils.getHumanReadable(orh.getBootTime(), true) + "\","
                        + "\"num_received_events\":" + orh.getNumReceivedEvents() + ","
                        + "\"num_processed_events\":" + orh.getNumProcessedEvents() + "}";
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
                jsonStr += "\"setup_time\":\"" + Utils.getHumanReadable(cc.getSetupTime(), true) + "\","
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

            if (sink instanceof OrionSink) {
                OrionSink os = (OrionSink) sink;
                jsonStr += "\"setup_time\":\"" + Utils.getHumanReadable(os.getSetupTime(), true) + "\","
                        + "\"num_processed_events\":" + os.getNumProcessedEvents() + ","
                        + "\"num_persisted_events\":" + os.getNumPersistedEvents() + "}";
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
        response.setContentType("json;charset=utf-8");

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

        GroupingRules groupingRules = new GroupingRules(groupingRulesConfFile);
        String rulesStr = groupingRules.toString(true);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{\"success\":\"true\"," + rulesStr + "}");
    } // handleGetGroupingRules

    private void handlePostGroupingRules(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("json;charset=utf-8");

        // read the new rule wanted to be added
        BufferedReader reader = request.getReader();
        String ruleStr = "";
        String line;

        while ((line = reader.readLine()) != null) {
            ruleStr += line;
        } // while

        reader.close();

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
        int err = GroupingRule.isValid(rule, true);

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

        GroupingRules groupingRules = new GroupingRules(groupingRulesConfFile);
        groupingRules.addRule(new GroupingRule(rule));
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

    private void handlePostSubscription(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("json;charset=utf-8");

        // read the new rule wanted to be added
        BufferedReader reader = request.getReader();
        String jsonStr = "";
        String line;

        while ((line = reader.readLine()) != null) {
            jsonStr += line;
        } // while

        reader.close();

        // Create a Gson for check JSON
        Gson gson = new Gson();
        CygnusSubscription cygnusSubscription;
        try {
            cygnusSubscription = gson.fromJson(jsonStr, CygnusSubscription.class);
        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, malformed Json. Check it for errors.\"");
            LOGGER.error("Parse error, malformed Json. Check it for errors.\"");
            return;
        } // try catch

        // check if the subscription and endpoint are valid
        int err = cygnusSubscription.isValid();

        if (err > 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            switch (err) {
                case 1: 
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"error\":\"Missing subscription or endpoint\"}");
                    LOGGER.warn("Missing subscription or endpoint");
                    return;
                case 2:
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"error\":\"Invalid subscription, some field is missing\"}");
                    LOGGER.warn("Invalid subscription, some field is missing");
                    return;
                case 3:
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"error\":\"Invalid subscription, some field is empty\"}");
                    LOGGER.warn("Invalid subscription, some field is empty");
                    return;
                case 4:
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"error\":\"Invalid subscription, some field is invalid\"}");
                    LOGGER.warn("Invalid subscription, some field is invalid");
                    return;
                default:
                    response.getWriter().println("{\"success\":\"false\","
                            + "\"error\":\"Invalid subscription\"}");
                    LOGGER.warn("Invalid subscription");
                    return;
            } // swtich

        } // if

        LOGGER.debug("Valid CygnusSubscription. Creating request to Orion.");

        // get host, port and ssl for request
        String host = cygnusSubscription.getOrionEndpoint().getHost();
        String port = cygnusSubscription.getOrionEndpoint().getPort();
        boolean ssl = Boolean.valueOf(cygnusSubscription.
                getOrionEndpoint().getSsl());
        boolean xAuthToken = false;
        String token = cygnusSubscription.getOrionEndpoint().getAuthToken();
        
        if (token != null) {
            xAuthToken = true;
        } // if

        // Create a subscriptionBackend for request
        subscriptionBackend = new OrionBackendImpl(host, port, ssl);
        
        // Get /subscription JSON from entire one
        JsonObject inputJson = new JsonParser().parse(jsonStr).getAsJsonObject();
        String subscriptionStr = inputJson.get("subscription").toString();
        
        try {
            JsonResponse orionResponse = 
                    subscriptionBackend.subscribeContext(subscriptionStr, xAuthToken, token);
            int status = orionResponse.getStatusCode();
            JSONObject orionJson = orionResponse.getJsonObject();
            
            if (status == 200) {
                response.getWriter().println("{\"success\":\"true\","
                            + "\"result\" : {" + orionJson.toJSONString() + "}");
                LOGGER.debug("Subscribed.");
            } else {
                response.getWriter().println("{\"success\":\"false\","
                            + "\"result\" : {" + orionJson.toJSONString() + "}");
            } // if else
            
        } catch (Exception ex) {
            Logger.getLogger(ManagementInterface.class.getName()).log(Level.SEVERE, null, ex);
        } // try catch

    } // handlePostSubscription

    private void handlePutGroupingRules(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("json;charset=utf-8");

        // read the new rule wanted to be added
        BufferedReader reader = request.getReader();
        String ruleStr = "";
        String line;

        while ((line = reader.readLine()) != null) {
            ruleStr += line;
        } // while

        reader.close();

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
        int err = GroupingRule.isValid(rule, true);

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

        GroupingRules groupingRules = new GroupingRules(groupingRulesConfFile);

        if (groupingRules.updateRule(id, new GroupingRule(rule))) {
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

    private void handleDeleteGroupingRules(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setContentType("json;charset=utf-8");

        // get the rule ID to be deleted
        long id = new Long(request.getParameter("id"));

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

        GroupingRules groupingRules = new GroupingRules(groupingRulesConfFile);

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
        response.setContentType("json;charset=utf-8");
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
            } // if
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
        response.setContentType("json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(
                "{\"source_points\":{\"columns\":[" + sourceColumns + "],\"rows\":[" + sourceRows + "]},"
                + "\"channel_points\":{\"columns\":[" + channelColumns + "],\"rows\":[" + channelRows + "]},"
                + "\"sink_points\":{\"columns\":[" + sinkColumns + "],\"rows\":[" + sinkRows + "]}}");
    } // handleGetPoints

} // ManagementInterface