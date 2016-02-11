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
import org.json.simple.JSONArray;
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
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println("404 - " + method + " " + uri + " Not found");
            } // if else
        } else if (method.equals("POST")) {
            if (uri.equals("/v1/groupingrules")) {
                handlePostGroupingRules(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println("404 - " + method + " " + uri + " Not found");
            } // if else
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("404 - " + method + " " + uri + " Not found");
        } // if else
    } // handle
    
    private void handleGetVersion(HttpServletResponse response) throws IOException {
        response.setContentType("json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{\"version\":\"" + Utils.getCygnusVersion()
                + "." + Utils.getLastCommit() + "\"}");
    } // handleGetVersion

    private void handleGetStats(HttpServletResponse response) throws IOException {
        response.setContentType("json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String jsonStr = "{\"sources\":[";
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

        jsonStr += "]}";
        response.getWriter().println(jsonStr);
    } // handleGetStats
    
    private void handleGetGroupingRules(HttpServletResponse response) throws IOException {
        String configStr = readGroupingRules();
        response.setContentType("json;charset=utf-8");
        
        if (configStr.startsWith("404")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        } // if else
        
        response.getWriter().println(configStr);
    } // handleGetGroupingRules
    
    private void handlePostGroupingRules(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("json;charset=utf-8");
        
        // read the grouing rules we want to modify
        String configStr = readGroupingRules();
        
        // check if there was an error while reading the grouping rules
        if (configStr.startsWith("404")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(configStr);
            LOGGER.error("Grouping rules not found... but they are supposed to be there!!!");
            return;
        } // if

        // there was no error, and the syntax of the already configured grouping rules should be OK...
        // thus read the new rule wanted to be added
        BufferedReader reader = request.getReader();
        String ruleStr = "";
        String line;

        while ((line = reader.readLine()) != null) {
            ruleStr += line;
        } // while
        
        reader.close();
        LOGGER.debug("Grouping rule to be added: " + ruleStr);

        // check the Json syntax of the new rule
        JSONParser jsonParser = new JSONParser();
        JSONObject rule;

        try {
            rule = (JSONObject) jsonParser.parse(ruleStr);
        } catch (ParseException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("400 - Parse error, invalid Json syntax. Details: " + e.getMessage());
            LOGGER.error("Parse error, invalid Json syntax. Details: " + e.getMessage());
            return;
        } // try catch

        // check if the rule is valid (it could be a valid Json document,
        // but not a Json document describing a rule)
        int err = isValid(rule);
        
        if (err > 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
            switch (err) {
                case 1:
                    response.getWriter().println("400 - Invalid grouping rule, some field is missing");
                    LOGGER.error("Invalid grouping rule, some field is missing");
                    return;
                case 2:
                    response.getWriter().println("400 - Invalid grouping rule, the id is not numeric or it is missing");
                    LOGGER.error("Invalid grouping rule, the id is not numeric or it is missing");
                    return;
                case 3:
                    response.getWriter().println("400 - Invalid grouping rule, some field is empty");
                    LOGGER.error("Invalid grouping rule, some field is empty");
                    return;
                default:
                    response.getWriter().println("400 - Invalid grouping rule");
                    LOGGER.error("Invalid grouping rule");
                    return;
            } // swtich
        } // if
        
        // add the rule to the already existent configuration... the easiest way is parsing the configuration
        JSONObject config;
        
        try {
            config = (JSONObject) jsonParser.parse(configStr);
        } catch (ParseException e) {
            LOGGER.error("Grouping rules syntax is wrong... but it is supposed to be OK!!! Details: "
                    + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("500 - Internal server error");
            return;
        } // try catch
        
        JSONArray rules = (JSONArray) config.get("grouping_rules");
        rules.add(rule);
        LOGGER.debug("Grouping rules after adding the new rule: " + rules.toJSONString());
        
        // write the configuration
        PrintWriter writer = new PrintWriter(new FileWriter(groupingRulesConfFile));
        writer.println("{\"grouping_rules\":" + rules.toJSONString() + "}");
        writer.flush();
        writer.close();
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{\"success\":\"true\"}");
    } // handlePostGroupingRules
    
    private String readGroupingRules() throws IOException {
        if (!configurationFile.exists()) {
            return "404 - Configuration file for Cygnus not found. Details: "
                    + configurationFile.toString();
        } // if
        
        groupingRulesConfFile = null;
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
        
        if (groupingRulesConfFile == null) {
            return "404 - Missing configuration file for Grouping Rules";
        } // if
        
        if (!new File(groupingRulesConfFile).exists()) {
            return "404 - Configuration file for Grouing Rules not found. Details: "
                    + groupingRulesConfFile;
        } // if
        
        String jsonStr = "";
        reader = new BufferedReader(new FileReader(groupingRulesConfFile));
        
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("#")) {
                jsonStr += line;
            } // if
        } // while
        
        reader.close();
        
        return jsonStr;
    } // readGroupingRules
    
    private int isValid(JSONObject jsonRule) {
        // check if the rule contains all the required fields
        if (!jsonRule.containsKey("id")
                || !jsonRule.containsKey("fields")
                || !jsonRule.containsKey("regex")
                || !jsonRule.containsKey("destination")
                || !jsonRule.containsKey("fiware_service_path")) {
            return 1;
        } // if
        
        // check if the id is numeric
        try {
            Long l = (Long) jsonRule.get("id");
        } catch (Exception e) {
            return 2;
        } // catch
        
        // check if the rule has any empty field
        if (((JSONArray) jsonRule.get("fields")).size() == 0
                || ((String) jsonRule.get("regex")).length() == 0
                || ((String) jsonRule.get("destination")).length() == 0
                || ((String) jsonRule.get("fiware_service_path")).length() == 0) {
            return 3;
        } // if
        
        return 0;
    } // isValid
    
} // ManagementInterface
