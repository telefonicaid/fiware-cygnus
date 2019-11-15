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
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().println(method + " " + uri + " not found");
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
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().println(method + " " + uri + " not found");
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
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().println(method + " " + uri + " not found");
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
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().println(method + " " + uri + " not found");
                    } // if else
                    
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().println(method + " " + uri + " not found");
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
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().println(method + " " + uri + " not found");
                } // if else
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println(method + " " + uri + " not found");
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
    } // getNameMappingsConfFile

} // ManagementInterface
