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

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.telefonica.iot.cygnus.containers.NameMappings;
import com.telefonica.iot.cygnus.containers.NameMappings.ServiceMapping;
import com.telefonica.iot.cygnus.containers.NameMappings.ServicePathMapping;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.JsonUtils;
import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author frb
 */
public final class NameMappingsHandlers {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(NameMappingsHandlers.class);
    
    /**
     * Constructor. Utility classes should not have a public or default constructor.
     */
    private NameMappingsHandlers() {
    } // NameMappingsHandlers
    
    private static NameMappings loadNameMappings(String nameMappingsConfFile) {
        // Read the Json string from the configuration file
        String nameMappingsStr;

        try {
            nameMappingsStr = JsonUtils.readJsonFile(nameMappingsConfFile);
            LOGGER.debug("Reading name mappings, Json read: " + nameMappingsStr);
        } catch (Exception e) {
            LOGGER.error("Runtime error (" + e.getMessage() + ")");
            return null;
        } // try catch
        
        return parseNameMappings(nameMappingsStr);
    } // loadNameMappings

    private static NameMappings parseNameMappings(String jsonStr) {
        // Result to be returned
        NameMappings nameMappings;
        
        // Check if the Json is empty
        if (jsonStr == null || jsonStr.isEmpty()) {
            LOGGER.debug("No Json to parse");
            return null;
        } // if

        // Parse the Json string
        Gson gson = new Gson();

        try {
            nameMappings = gson.fromJson(jsonStr, NameMappings.class);
            LOGGER.debug("Json parsed");
        } catch (JsonIOException | JsonSyntaxException e) {
            LOGGER.error("Runtime error (" + e.getMessage() + ")");
            return null;
        } // try catch

        // Check if any of the mappings is not valid, e.g. some field is missing
        nameMappings.purge();
        LOGGER.debug("Json purged");
        
        // Pre-compile the regular expressions
        nameMappings.compilePatterns();
        LOGGER.debug("Regular expressions with Json pre-compiled");
        
        return nameMappings;
    } // parseNameMappings
    
    private static String readPayload(HttpServletRequest request) throws IOException {
        try (
            BufferedReader reader = request.getReader()) {
            String payload = "";
            String line;

            while ((line = reader.readLine()) != null) {
                payload += line;
            } // while
            
            return payload;
        } // try
    } // readPayload

    /**
     * Handles GET /v1/namemappings.
     * @param request
     * @param response
     * @param nameMappingsConfFile
     * @throws IOException
     */
    public static void handleGetNameMappings(HttpServletRequest request, HttpServletResponse response,
            String nameMappingsConfFile) throws IOException {
        LOGGER.debug("Request: GET /v1/namemappings");
        
        // Set the content type of the response
        response.setContentType("application/json; charset=utf-8");
        
        // Get the name mappings from the configuration file
        NameMappings nameMappings = loadNameMappings(nameMappingsConfFile);

        // Check if the name mappings are null
        if (nameMappings == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Configuration file for Name Mappings not found. Details: "
                    + nameMappingsConfFile + "\"}";
            response.getWriter().println(responseStr);
            LOGGER.debug("Response: " + responseStr);
            return;
        } // if
        
        response.setStatus(HttpServletResponse.SC_OK);
        String responseStr = "{\"success\":\"true\",\"result\":" + nameMappings.toString() + "}";
        response.getWriter().println(responseStr);
        LOGGER.debug("Response:" + responseStr);
    } // handleGetNameMappings
    
    /**
     * Handles POST /v1/namemappings/servicemapping.
     * @param request
     * @param response
     * @param nameMappingsConfFile
     * @throws IOException
     */
    public static void handlePostServiceMapping(HttpServletRequest request, HttpServletResponse response,
            String nameMappingsConfFile) throws IOException {
        LOGGER.debug("Request: POST /v1/namemappings/servicemapping");
        
        // Set the content type of the response
        response.setContentType("application/json; charset=utf-8");
        
        // Get the name mappings from the configuration file
        NameMappings nameMappings = loadNameMappings(nameMappingsConfFile);

        // Check if the name mappings are null
        if (nameMappings == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Configuration file for Name Mappings not found. Details: "
                    + nameMappingsConfFile + "\"}";
            response.getWriter().println(responseStr);
            LOGGER.debug("Response: " + responseStr);
            return;
        } // if
        
        String payload = readPayload(request);

        if (payload == null || payload.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Given service mapping is null or empty\"}";
            response.getWriter().println(responseStr);
            LOGGER.debug("Response: " + responseStr);
            return;
        } // if

        Gson gson = new Gson();
        ServiceMapping serviceMapping;

        try {
            serviceMapping = gson.fromJson(payload, ServiceMapping.class);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Given service mapping has errors. Details: " + e.getMessage() + "\"}";
            response.getWriter().println(responseStr);
            LOGGER.debug("Response: " + responseStr);
            return;
        } // try catch

        nameMappings.addServiceMapping(serviceMapping);
        response.setStatus(HttpServletResponse.SC_OK);
        String responseStr = "{\"success\":\"true\",\"result\":" + nameMappings.toString() + "}";
        response.getWriter().println(responseStr);
        LOGGER.debug("Response:" + responseStr);
    } // handlePostServiceMapping
    
    /**
     * Handles POST /v1/namemappings/servicepathmapping.
     * @param request
     * @param response
     * @param nameMappingsConfFile
     * @throws IOException
     */
    public static void handlePostServicePathMapping(HttpServletRequest request, HttpServletResponse response,
            String nameMappingsConfFile) throws IOException {
        LOGGER.debug("Request: POST /v1/namemappings/servicepathmapping");
        
        // Set the content type of the response
        response.setContentType("application/json; charset=utf-8");
        
        // Get the name mappings from the configuration file
        NameMappings nameMappings = loadNameMappings(nameMappingsConfFile);

        // Check if the name mappings are null
        if (nameMappings == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Configuration file for Name Mappings not found. Details: "
                    + nameMappingsConfFile + "\"}";
            response.getWriter().println(responseStr);
            LOGGER.debug("Response: " + responseStr);
            return;
        } // if
        
        String payload = readPayload(request);

        if (payload == null || payload.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Given service path mapping is null or empty\"}";
            response.getWriter().println(responseStr);
            LOGGER.debug("Response: " + responseStr);
            return;
        } // if

        Gson gson = new Gson();
        ServicePathMapping servicePathMapping;

        try {
            servicePathMapping = gson.fromJson(payload, ServicePathMapping.class);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Given service path mapping has errors. Details: " + e.getMessage() + "\"}";
            response.getWriter().println(responseStr);
            LOGGER.debug("Response: " + responseStr);
            return;
        } // try catch

        for (ServiceMapping serviceMapping : nameMappings.getServiceMappings()) {
            if (serviceMapping.getOriginalService().equals(request)) {
                
            }
        } // for
        
        response.setStatus(HttpServletResponse.SC_OK);
        String responseStr = "{\"success\":\"true\",\"result\":" + nameMappings.toString() + "}";
        response.getWriter().println(responseStr);
        LOGGER.debug("Response:" + responseStr);
    } // handlePostServicePathMapping
    
    /**
     * Handles POST /v1/namemappings/enttymapping.
     * @param request
     * @param response
     * @param nameMappingsConfFile
     * @throws IOException
     */
    public static void handlePostEntityMapping(HttpServletRequest request, HttpServletResponse response,
            String nameMappingsConfFile) throws IOException {
        
    } // handlePostEntityMapping
    
    /**
     * Handles POST /v1/namemappings/attributemapping.
     * @param request
     * @param response
     * @param nameMappingsConfFile
     * @throws IOException
     */
    public static void handlePostAttributeMapping(HttpServletRequest request, HttpServletResponse response,
            String nameMappingsConfFile) throws IOException {
        
    } // handlePostAttributeMapping
    
    /**
     * Handles PUT /v1/namemappings.
     * @param request
     * @param response
     * @param nameMappingsConfFile
     * @throws IOException
     */
    public static void handlePutNameMappings(HttpServletRequest request, HttpServletResponse response,
            String nameMappingsConfFile) throws IOException {
        LOGGER.info("Request: PUT /v1/namemappings");
        
        // Set the content type of the response
        response.setContentType("application/json; charset=utf-8");
        
        // Get the name mappings from the configuration file
        NameMappings nameMappings = loadNameMappings(nameMappingsConfFile);

        // Check if the name mappings are null
        if (nameMappings == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Configuration file for Name Mappings not found. Details: "
                    + nameMappingsConfFile + "\"}";
            response.getWriter().println(responseStr);
            LOGGER.info("Response: " + responseStr);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            String responseStr = "{\"success\":\"true\",\"result\":" + nameMappings.toString() + "}";
            response.getWriter().println(responseStr);
            LOGGER.info("Response:" + responseStr);
        } // if else
    } // handlePutNameMappings
    
    /**
     * Handles DELETE /v1/namemappings.
     * @param request
     * @param response
     * @param nameMappingsConfFile
     * @throws IOException
     */
    public static void handleDeleteNameMappings(HttpServletRequest request, HttpServletResponse response,
            String nameMappingsConfFile) throws IOException {
        LOGGER.info("Request: DELETE /v1/namemappings");
        
        // Set the content type of the response
        response.setContentType("application/json; charset=utf-8");
        
        // Get the name mappings from the configuration file
        NameMappings nameMappings = loadNameMappings(nameMappingsConfFile);

        // Check if the name mappings are null
        if (nameMappings == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Configuration file for Name Mappings not found. Details: "
                    + nameMappingsConfFile + "\"}";
            response.getWriter().println(responseStr);
            LOGGER.info("Response: " + responseStr);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            String responseStr = "{\"success\":\"true\",\"result\":" + nameMappings.toString() + "}";
            response.getWriter().println(responseStr);
            LOGGER.info("Response:" + responseStr);
        } // if else
    } // handleDeleteNameMappings
    
} // NameMappingsHandlers
