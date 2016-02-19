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
import com.telefonica.iot.cygnus.channels.CygnusChannel;
import com.telefonica.iot.cygnus.handlers.OrionRestHandler;
import com.telefonica.iot.cygnus.interceptors.GroupingRule;
import com.telefonica.iot.cygnus.interceptors.GroupingRules;
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
        String uri = request.getRequestURI();
        String method = request.getMethod();
        LOGGER.info("Management interface request. Method: " + method + ", URI: " + uri);
        
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
                jsonStr += "\"setup_time\":\"" + Utils.getHumanReadable(orh.getSetupTime(), true) + "\","
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

} // ManagementInterface
