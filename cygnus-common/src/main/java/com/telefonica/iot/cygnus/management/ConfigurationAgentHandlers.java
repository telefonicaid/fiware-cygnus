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

import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

/**
 *
 * @author frb
 */
public final class ConfigurationAgentHandlers {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(ConfigurationAgentHandlers.class);
    
    /**
     * Constructor. Private since utility classes should not have a public or default constructor.
     */
    private ConfigurationAgentHandlers() {
    } // ConfigurationAgentHandlers
    
    /**
     * Handles GET /admin/configuration/agent.
     * @param request
     * @param response
     * @param v1
     * @throws IOException
     */
    public static void get(HttpServletRequest request, HttpServletResponse response, boolean v1) throws IOException {
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
    } // get
    
    /**
     * Handles POST /admin/configuration/agent.
     * @param request
     * @param response
     * @param v1
     * @throws IOException
     */
    public static void post(HttpServletRequest request, HttpServletResponse response, boolean v1) throws IOException {
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
    } // post
    
    /**
     * Handles PUT /admin/configuration/agent.
     * @param request
     * @param response
     * @param v1
     * @throws IOException
     */
    public static void put(HttpServletRequest request, HttpServletResponse response, boolean v1) throws IOException {
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
    } // put
    
    /**
     * Handles DELETE configuration/agent.
     * @param request
     * @param response
     * @param v1
     * @throws IOException
     */
    public static void delete(HttpServletRequest request, HttpServletResponse response, boolean v1) throws IOException {
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
    } // delete
    
} // ConfigurationAgentHandlers
