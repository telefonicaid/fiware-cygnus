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
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.JsonUtils;
import java.io.BufferedReader;
import java.io.File;
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
            LOGGER.debug("Reading name mappings from " + nameMappingsConfFile + ", Json read: " + nameMappingsStr);
        } catch (IOException e) {
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
            LOGGER.debug("Name mappings Json parsed: " + nameMappings.toString());
        } catch (JsonIOException | JsonSyntaxException e) {
            LOGGER.error("Runtime error (" + e.getMessage() + ")");
            return null;
        } // try catch

        // Check if any of the mappings is not valid, e.g. some field is missing
        nameMappings.purge();
        LOGGER.debug("Name mappings Json purged: " + nameMappings.toString());
        
        // Pre-compile the regular expressions
        nameMappings.compilePatterns();
        LOGGER.debug("Regular expressions within name mappings Json were pre-compiled");
        
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
            
            LOGGER.debug("Payload rad from request: " + payload);
            return payload;
        } // try
    } // readPayload
    
    private static void saveNameMappings(String nameMappingsConfFile, NameMappings nameMappings) {
        try {
            JsonUtils.writeJsonFile(nameMappingsConfFile, nameMappings.toString());
            LOGGER.debug("Name mappings Json wrote");
        } catch (IOException e) {
            LOGGER.error("Runtime error (" + e.getMessage() + ")");
        } // try catch
    } // loadNameMappings

    /**
     * Handles GET /v1/namemappings.
     * @param request
     * @param response
     * @param nameMappingsConfFile
     * @throws IOException
     */
    public static void get(HttpServletRequest request, HttpServletResponse response,
            String nameMappingsConfFile) throws IOException {
        LOGGER.info("Request: GET /v1/namemappings");
        
        // Set the content type of the response
        response.setContentType("application/json; charset=utf-8");
        
        // Get the name mappings from the configuration file
        NameMappings nameMappings = loadNameMappings(nameMappingsConfFile);

        // Check if the name mappings file exits
        if (!new File(nameMappingsConfFile).exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Configuration file for Name Mappings not found. Details: "
                    + nameMappingsConfFile + "\"}";
            response.getWriter().println(responseStr);
            LOGGER.debug("Response: " + responseStr);
            return;
        } // if

        // Check if the name mappings are null
        if (nameMappings == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Configuration file for Name Mappings is empty. Details: "
                    + nameMappingsConfFile + "\"}";
            response.getWriter().println(responseStr);
            LOGGER.debug("Response: " + responseStr);
            return;
        } // if

        response.setStatus(HttpServletResponse.SC_OK);
        String responseStr = "{\"success\":\"true\",\"result\":" + nameMappings.toString() + "}";
        response.getWriter().println(responseStr);
        LOGGER.info("Response:" + responseStr);
    } // get
    
    /**
     * Handles POST /v1/namemappings.
     * @param request
     * @param response
     * @param nameMappingsConfFile
     * @throws IOException
     */
    public static void post(HttpServletRequest request, HttpServletResponse response,
            String nameMappingsConfFile) throws IOException {
        LOGGER.info("Request: POST /v1/namemappings");
        
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
        
        // Read the payload
        String payload = readPayload(request);

        if (payload == null || payload.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Given name mappings are null or empty\"}";
            response.getWriter().println(responseStr);
            LOGGER.debug("Response: " + responseStr);
            return;
        } // if

        // Do the POST
        NameMappings newNameMappings = parseNameMappings(payload);
        nameMappings.add(newNameMappings.getServiceMappings(), false);
        
        // Save the name mappings
        saveNameMappings(nameMappingsConfFile, nameMappings);
        
        // Response
        response.setStatus(HttpServletResponse.SC_OK);
        String responseStr = "{\"success\":\"true\",\"result\":" + nameMappings.toString() + "}";
        response.getWriter().println(responseStr);
        LOGGER.info("Response:" + responseStr);
    } // post
    
    /**
     * Handles PUT /v1/namemappings.
     * @param request
     * @param response
     * @param nameMappingsConfFile
     * @throws IOException
     */
    public static void put(HttpServletRequest request, HttpServletResponse response,
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
            LOGGER.debug("Response: " + responseStr);
            return;
        } // if
        
        // Read the payload
        String payload = readPayload(request);

        if (payload == null || payload.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Given name mappings are null or empty\"}";
            response.getWriter().println(responseStr);
            LOGGER.debug("Response: " + responseStr);
            return;
        } // if

        // Do the PUT
        NameMappings newNameMappings = parseNameMappings(payload);
        nameMappings.add(newNameMappings.getServiceMappings(), true);
        
        // Save the name mappings
        saveNameMappings(nameMappingsConfFile, nameMappings);
        
        // Response
        response.setStatus(HttpServletResponse.SC_OK);
        String responseStr = "{\"success\":\"true\",\"result\":" + nameMappings.toString() + "}";
        response.getWriter().println(responseStr);
        LOGGER.info("Response:" + responseStr);
    } // put
    
    /**
     * Handles DELETE /v1/namemappings.
     * @param request
     * @param response
     * @param nameMappingsConfFile
     * @throws IOException
     */
    public static void delete(HttpServletRequest request, HttpServletResponse response,
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
            LOGGER.debug("Response: " + responseStr);
            return;
        } // if
        
        // Read the payload
        String payload = readPayload(request);

        if (payload == null || payload.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String responseStr =
                    "{\"success\":\"false\","
                    + "\"error\":\"Given name mappings are null or empty\"}";
            response.getWriter().println(responseStr);
            LOGGER.debug("Response: " + responseStr);
            return;
        } // if

        // Do the DELETE
        NameMappings newNameMappings = parseNameMappings(payload);
        nameMappings.remove(newNameMappings.getServiceMappings());
        
        // Save the name mappings
        saveNameMappings(nameMappingsConfFile, nameMappings);
        
        // Response
        response.setStatus(HttpServletResponse.SC_OK);
        String responseStr = "{\"success\":\"true\",\"result\":" + nameMappings.toString() + "}";
        response.getWriter().println(responseStr);
        LOGGER.info("Response:" + responseStr);
    } // delete
    
} // NameMappingsHandlers
