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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.backends.orion.OrionBackendImpl;
import com.telefonica.iot.cygnus.containers.CygnusSubscriptionV1;
import com.telefonica.iot.cygnus.containers.CygnusSubscriptionV2;
import com.telefonica.iot.cygnus.containers.OrionEndpoint;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

/**
 *
 * @author frb
 */
public final class SubscriptionsHandlers {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(SubscriptionsHandlers.class);
    private static final int MAX_CONNS = 50;
    private static final int MAX_CONNS_PER_ROUTE = 10;
    
    /**
     * Constructor. Utility classes should not have a public or default constructor.
     */
    private SubscriptionsHandlers() {
    } // SubscriptionsHandlers
    
    private static String readPayload(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = request.getReader()) {
            String jsonStr = "";
            String line;
            
            while ((line = reader.readLine()) != null) {
                jsonStr += line;
            } // while
            
            return jsonStr;
        } // try
    } // readPayload
    
    /**
     * Handles GET /subscriptions.
     * @param request
     * @param response
     * @throws IOException
     */
    public static void get(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Let's assume everything will go OK...
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(CommonConstants.HEADER_CORRELATOR_ID, ManagementInterfaceUtils.setCorrelator(request));
        
        // Get the request parameters
        String ngsiVersion = request.getParameter("ngsi_version");
        String subscriptionID = request.getParameter("subscription_id");
        String fiwareService = request.getHeader("Fiware-Service");
        String fiwareServicePath = request.getHeader("Fiware-ServicePath");
        
        // Check ngsi_version parameter
        if (ngsiVersion == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, missing parameter (ngsi_version)\"}");
            LOGGER.error("Parse error, missing parameter (ngsi_version)");
            return;
        } else if (ngsiVersion.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, empty parameter (ngsi_version)\"}");
            LOGGER.error("Parse error, empty parameter (ngsi_version)");
            return;
        } // if else
        
        if (!ngsiVersion.equals("2")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, invalid parameter (ngsi_version), must be '2'\"}");
            LOGGER.error("Parse error, invalid parameter (ngsi_version), must be '2'");
            return;
        } // if
        
        // Check subscription_id parameter
        boolean getAllSubscriptions = false;
        
        if (subscriptionID == null) {
            getAllSubscriptions = true;
        } else if (subscriptionID.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, empty parameter (subscription_id)\"}");
            LOGGER.error("Parse error, empty parameter (subscription_id)");
            return;
        } // if else

        // Get the endpoint string and parse it
        String endpointStr = readPayload(request);
        Gson gson = new Gson();
        OrionEndpoint endpoint;
        
        try {
            endpoint = gson.fromJson(endpointStr, OrionEndpoint.class);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\",\"error\":\"Parse error, " + e.getMessage() + "\"}");
            LOGGER.error("Parse error, malformed Json");
            return;
        } // try catch
                       
        // Check if the endpoint is valid
        try {
            endpoint.validate();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\",\"error\":\"Validation error, "
                    + e.getMessage() + "\"}");
            LOGGER.error(e.getMessage());
            return;
        } // try catch

        // Create a orionBackend for request
        OrionBackendImpl orionBackend = new OrionBackendImpl(endpoint.getHost(), endpoint.getPort(), endpoint.getSsl(),
                MAX_CONNS, MAX_CONNS_PER_ROUTE);
        
        if (getAllSubscriptions) {
            LOGGER.debug("Valid Endpoint. Creating request to Orion (GET all subscriptions).");
            
            try {
                int status;
                JSONObject orionJson;

                JsonResponse orionResponse = orionBackend.
                        getSubscriptionsV2(endpoint.getAuthToken(), subscriptionID, fiwareService, fiwareServicePath);
                                
                if (orionResponse != null) {
                    orionJson = orionResponse.getJsonObject();
                    status = orionResponse.getStatusCode();
                } else {
                    response.getWriter().println("{\"success\":\"false\","
                        + "\"result\":\"There was some problem when handling the response\"}");
                    LOGGER.debug("There was some problem when handling the response");
                    return;
                } // if else

                LOGGER.debug("Status code obtained: " + status);

                if (status == 200) {
                    response.getWriter().println("{\"success\":\"true\","
                                + "\"result\":\"" + orionJson.toJSONString() + "\"}");
                    LOGGER.debug("Subscription received: " + orionJson.toJSONString());
                } else {
                    response.getWriter().println("{\"success\":\"false\","
                                + "\"result\":\"" + orionJson.toJSONString() + "\"}");
                } // if else
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                response.getWriter().println("{\"success\":\"false\",\"result\":\"" + e.getMessage() + "\"}");
            } // try catch
        } else {
            LOGGER.debug("Valid Endpoint. Creating request to Orion (GET subsription by id).");
            
            try {
                int status;
                JSONObject orionJson;

                JsonResponse orionResponse = orionBackend.getSubscriptionsByIdV2(endpoint.getAuthToken(),
                        subscriptionID, fiwareService, fiwareServicePath);

                if (orionResponse != null) {
                    orionJson = orionResponse.getJsonObject();
                    status = orionResponse.getStatusCode();
                } else {
                    response.getWriter().println("{\"success\":\"false\","
                        + "\"result\":\"There was some problem when handling the response\"}");
                    LOGGER.debug("There was some problem when handling the response");
                    return;
                } // if else

                LOGGER.debug("Status code obtained: " + status);

                if (status == 200) {
                    response.getWriter().println("{\"success\":\"true\","
                                + "\"result\":\"" + orionJson.toJSONString() + "\"}");
                    LOGGER.debug("Subscription received: " + orionJson.toJSONString());
                } else {
                    response.getWriter().println("{\"success\":\"false\","
                                + "\"result\":\"" + orionJson.toJSONString() + "\"}");
                } // if else
            
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                response.getWriter().println("{\"success\":\"false\",\"result\":\"" + e.getMessage() + "\"}");
            } // try catch
        } // if else
    } // get
    
    /**
     * Handles POST /v1/subscriptions.
     * @param request
     * @param response
     * @throws IOException
     */
    public static void post(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Let's assume everything will go OK...
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(CommonConstants.HEADER_CORRELATOR_ID, ManagementInterfaceUtils.setCorrelator(request));
        
        // Read the payload (must be done before the request parameters are read)
        String payload = readPayload(request);

        // Get the request parameters
        String ngsiVersion = request.getParameter("ngsi_version");
        String fiwareService = request.getHeader("Fiware-Service");
        String fiwareServicePath = request.getHeader("Fiware-ServicePath");
        
        // Check ngsi_version parameter
        if (ngsiVersion == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, missing parameter (ngsi_version)\"}");
            LOGGER.error("Parse error, missing parameter (ngsi_version)");
            return;
        } else if (ngsiVersion.equals("")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, empty parameter (ngsi_version)\"}");
            LOGGER.error("Parse error, empty parameter (ngsi_version)");
            return;
        } // if else
        
        if (!ngsiVersion.equals("1") && !ngsiVersion.equals("2")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, invalid parameter (ngsi_version), must be '1' or '2'\"}");
            LOGGER.error("Parse error, invalid parameter (ngsi_version), must be '1' or '2'");
            return;
        } // if
        
        // Do the POST
        switch (ngsiVersion) {
            case "1":
                postV1(payload, fiwareService, fiwareServicePath, response);
                break;
            case "2":
                postV2(payload, fiwareService, fiwareServicePath, response);
                break;
            default:
                LOGGER.error("You should never have reached this! NGSI version must be '1' or '2'");
        } // switch
    } // post
    
    private static void postV1(String payload, String fiwareService, String fiwareServicePath,
            HttpServletResponse response) throws IOException {
        // Parse the payload
        Gson gson = new Gson();
        CygnusSubscriptionV1 cygnusSubscriptionv1;

        try {
            cygnusSubscriptionv1 = gson.fromJson(payload, CygnusSubscriptionV1.class);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, " + e.getMessage() + "\"");
            LOGGER.error("Parse error, " + e.getMessage());
            return;
        } // try catch

        // Check if the Cygnus subscription (Orion endpoint + Orion subscription) is valid
        try {
            cygnusSubscriptionv1.validate();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, " + e.getMessage() + "\"");
            LOGGER.error("Parse error, " + e.getMessage() + "\"");
            return;
        } // try catch

        // Create a Orion backend
        OrionBackendImpl orionBackend = new OrionBackendImpl(
                cygnusSubscriptionv1.getOrionEndpoint().getHost(),
                cygnusSubscriptionv1.getOrionEndpoint().getPort(),
                cygnusSubscriptionv1.getOrionEndpoint().getSsl(), MAX_CONNS, MAX_CONNS_PER_ROUTE);

        // Do the POST
        String subscriptionStr = cygnusSubscriptionv1.getOrionSubscription().toString();
        JsonResponse orionResponse;

        // to-do: this must be replaced with especific error handling in orionBackend.subscribeContextV1(...)
        try {
            orionResponse = orionBackend.subscribeContextV1(
                    subscriptionStr, cygnusSubscriptionv1.getOrionEndpoint().getAuthToken(),
                    fiwareService, fiwareServicePath);
        } catch (Exception e) {
            throw new IOException(e);
        } // try catch
        
        int status = orionResponse.getStatusCode();
        JSONObject orionJson = orionResponse.getJsonObject();

        if (orionJson.containsKey("orionError")) {
            JSONObject error = (JSONObject) orionJson.get("orionError");
            status = Integer.parseInt(error.get("code").toString());
        } // if

        if (status == 200) {
            response.getWriter().println("{\"success\":\"true\",\"result\":{"
                    + orionJson.toJSONString() + "}");
            LOGGER.debug("Subscribed.");
        } else {
            response.getWriter().println("{\"success\":\"false\",\"result\":{"
                    + orionJson.toJSONString() + "}");
        } // if else
    } // postV1
    
    private static void postV2(String payload, String fiwareService, String fiwareServicePath,
            HttpServletResponse response) throws IOException {
        // Parse the payload
        Gson gson = new Gson();
        CygnusSubscriptionV2 cygnusSubscriptionv2;

        try {
            cygnusSubscriptionv2 = gson.fromJson(payload, CygnusSubscriptionV2.class);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, " + e.getMessage() + "\"");
            LOGGER.error("Parse error, " + e.getMessage());
            return;
        } // try catch

        // Check if the Cygnus subscription (Orion endpoint + Orion subscription) is valid
        try {
            cygnusSubscriptionv2.validate();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"success\":\"false\","
                    + "\"error\":\"Parse error, " + e.getMessage() + "\"");
            LOGGER.error("Parse error, " + e.getMessage() + "\"");
            return;
        } // try catch

        // Create a orionBackend for request
        OrionBackendImpl orionBackend = new OrionBackendImpl(
                cygnusSubscriptionv2.getOrionEndpoint().getHost(),
                cygnusSubscriptionv2.getOrionEndpoint().getPort(),
                cygnusSubscriptionv2.getOrionEndpoint().getSsl(), MAX_CONNS, MAX_CONNS_PER_ROUTE);

        // Do the POST
        String subscriptionStr = cygnusSubscriptionv2.getOrionSubscription().toString();
        JsonResponse orionResponse;
        
        // to-do: this must be replaced with especific error handling in orionBackend.subscribeContextV2(...)
        try {
            orionResponse = orionBackend.subscribeContextV2(subscriptionStr,
                 cygnusSubscriptionv2.getOrionEndpoint().getAuthToken(),
                fiwareService, fiwareServicePath);
        } catch (Exception e) {
            throw new IOException(e);
        } // try catch

        if (orionResponse.getStatusCode() == 201) {
            String location = orionResponse.getLocationHeader().getValue();
            response.getWriter().println("{\"success\":\"true\","
                    + "\"result\":{\"SubscriptionID\":\"" + location.substring(18) + "\"}");
            LOGGER.debug("Subscribed.");
        } else {
            JSONObject orionJson = orionResponse.getJsonObject();
            response.getWriter().println("{\"success\":\"false\",\"result\":{"
                    + orionJson.toString() + "}");
        } // if else
    } // postV2
    
    /**
     * Handles DELETE /v1/subscriptions.
     * @param request
     * @param response
     * @throws IOException
     */
    public static void delete(HttpServletRequest request, HttpServletResponse response) throws IOException {
/*
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
        String endpointStr;
        
        try (BufferedReader reader = request.getReader()) {
            endpointStr = "";
            String line;
            
            while ((line = reader.readLine()) != null) {
                endpointStr += line;
            } // while
        } // try
        
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
            err = endpoint.validate();
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
            
            JsonResponse orionResponse;
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
*/
    } // delete
    
    /**
     * Sets a given Orion backend.
     * @param orionBackend
     */
    protected void setOrionBackend(OrionBackendImpl orionBackend) {
//        this.orionBackend = orionBackend;
    } // setOrionBackend
    
} // SubscriptionsHandlers
