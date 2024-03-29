/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.telefonica.iot.cygnus.containers.NotifyContextRequestLD;
import com.telefonica.iot.cygnus.interceptors.NGSILDEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.management.PatternTypeAdapter;
import java.io.BufferedReader;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.source.http.HTTPBadRequestException;
import org.apache.flume.source.http.HTTPSourceHandler;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import org.slf4j.MDC;
import org.json.JSONObject;


/**
 *
 * @author anmunoz
 * 
 * Custom HTTP handler for the default HTTP Flume source. It checks the method, notificationTarget and headers are the
 * ones tipically sent by an instance of Orion Context Broker when notifying a context event. If everything is OK, a
 * Flume event is created in order the HTTP Flume source sends it to the Flume channel connecting the source with the
 * sink. This event contains both the context event data and a header specifying the content type (Json).
 */
public class NGSIRestHandler extends CygnusHandler implements HTTPSourceHandler {

    // LOGGER
    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIRestHandler.class);

    // configuration parameters
    private boolean invalidConfiguration;
    private String notificationTarget;
    private String defaultService;

    // shared variables, making them static all the instances of this class will share them
    private static final Object LOCK = new Object();

    /**
     * Constructor. This can be used as a place where to initialize all that things we would like to do in the Flume
     * "initialization" class, which is unreachable by our code. As long as this class is instantiated almost at boot
     * time, it is the closest code to such real initialization.
     */
    public NGSIRestHandler() {
        // initially, the configuration is meant to be valid
        invalidConfiguration = false;
    } // NGSIRestHandler

    /**
     * Gets the notifications target. It is protected due to it is only required for testing purposes.
     *
     * @return The notifications target
     */
    protected String getNotificationTarget() {
        return notificationTarget;
    } // getNotificationTarget

    /**
     * Gets the default service. It is protected due to it is only required for testing purposes.
     *
     * @return
     */
    protected String getDefaultService() {
        return defaultService;
    } // getDefaultService


    /**
     * Gets true if the configuration is invalid, false otherwise. It is protected due to it is only
     * required for testing purposes.
     *
     * @return
     */
    protected boolean getInvalidConfiguration() {
        return invalidConfiguration;
    } // getInvalidConfiguration

    @Override
    public void configure(Context context) {
        notificationTarget = context.getString(NGSIConstants.PARAM_NOTIFICATION_TARGET, "/notify");

        if (notificationTarget.startsWith("/")) {
            LOGGER.debug("[NGSIRestHandler] Reading configuration (" + NGSIConstants.PARAM_NOTIFICATION_TARGET + "="
                    + notificationTarget + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.error("[NGSIRestHandler] Bad configuration (" + NGSIConstants.PARAM_NOTIFICATION_TARGET + "="
                    + notificationTarget + ") -- Must start with '/'");
        } // if else

        defaultService = context.getString(NGSIConstants.PARAM_DEFAULT_SERVICE, "default");

        if (defaultService.length() > NGSIConstants.SERVICE_HEADER_MAX_LEN) {
            invalidConfiguration = true;
            LOGGER.error("[NGSIRestHandler] Bad configuration ('" + NGSIConstants.PARAM_DEFAULT_SERVICE
                    + "' parameter length greater than " + NGSIConstants.SERVICE_HEADER_MAX_LEN + ")");
        } else if (CommonUtils.isMAdeOfAlphaNumericsOrUnderscores(defaultService)) {
            LOGGER.debug("[NGSIRestHandler] Reading configuration (" + NGSIConstants.PARAM_DEFAULT_SERVICE + "="
                    + defaultService + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.error("[NGSIRestHandler] Bad configuration ('" + NGSIConstants.PARAM_DEFAULT_SERVICE
                    + "' parameter can only contain alphanumerics or underscores)");
        } // if else

        LOGGER.info("[NGSIRestHandler] Startup completed");
    } // configure

    @Override
    public List<Event> getEvents(javax.servlet.http.HttpServletRequest request) throws Exception {
        // Set some MDC logging fields to 'N/A' for this thread
        // Value for the component field is inherited from main thread (CygnusApplication.java)
        org.apache.log4j.MDC.put(CommonConstants.LOG4J_CORR, CommonConstants.NA);
        org.apache.log4j.MDC.put(CommonConstants.LOG4J_TRANS, CommonConstants.NA);
        org.apache.log4j.MDC.put(CommonConstants.LOG4J_SVC, CommonConstants.NA);
        org.apache.log4j.MDC.put(CommonConstants.LOG4J_SUBSVC, CommonConstants.NA);

        // Result
        ArrayList<Event> ngsiEvents = new ArrayList<>();
        // Update the counters
        numReceivedEvents++;
        // Check the headers looking for not supported content type and/or invalid FIWARE service and service path
        Enumeration headerNames = request.getHeaderNames();
        String corrId = null;
        String contentType = null;
        String service = defaultService;
        String servicePath = "";
        String link = "";

        while (headerNames.hasMoreElements()) {
            String headerName = ((String) headerNames.nextElement()).toLowerCase(Locale.ENGLISH);
            String headerValue = request.getHeader(headerName);
            LOGGER.info("[NGSIRestHandler] Header " + headerName + " received with value " + headerValue);

            switch (headerName) {
                case CommonConstants.HEADER_CORRELATOR_ID:
                    corrId = headerValue;
                    break;
                case CommonConstants.HTTP_HEADER_CONTENT_TYPE:
                    if (wrongContentType(headerValue)) {
                        LOGGER.warn("[NGSIRestHandler] Bad HTTP notification (" + headerValue
                                + " content type not supported)");
                        throw new HTTPBadRequestException(headerValue
                                + " content type not supported");
                    } else {
                        contentType = headerValue;
                    } // if else

                    break;
                case CommonConstants.HEADER_FIWARE_SERVICE:
                    if (wrongServiceHeaderLength(headerValue)) {
                        LOGGER.warn("[NGSIRestHandler] Bad HTTP notification ('"
                                + CommonConstants.HEADER_FIWARE_SERVICE
                                + "' header length greater than "
                                + NGSIConstants.SERVICE_HEADER_MAX_LEN + ")");
                        throw new HTTPBadRequestException(
                                "'" + CommonConstants.HEADER_FIWARE_SERVICE
                                        + "' header length greater than "
                                        + NGSIConstants.SERVICE_HEADER_MAX_LEN + ")");
                    } else {
                        service = headerValue;
                    } // if else

                    break;
                case "link":
                    link = headerValue.split(";")[0].replaceAll("<", "").replaceAll(">", "");
                    break;
                default:
                    LOGGER.debug("[NGSIRestHandler] Unnecessary header");
            } // switch
        } // while

        // Get a service and servicePath and store it in the log4j Mapped Diagnostic Context (MDC)
        MDC.put(CommonConstants.LOG4J_SVC, service == null ? defaultService : service);

        // If the configuration is invalid, nothing has to be done but to return null
        if (invalidConfiguration) {
            LOGGER.info("[NGSIRestHandler] info test control+2 ");

            serviceMetrics.add(service, servicePath, 1, request.getContentLength(), 0, 0, 0, 0, 0, 0, 0);
            LOGGER.debug("[NGSIRestHandler] Invalid configuration, thus returning an empty list of Flume events");
            return new ArrayList<>();
        } // if

        // Check the method
        String method = request.getMethod().toUpperCase(Locale.ENGLISH);

        if (!method.equals("POST")) {
            serviceMetrics.add(service, servicePath, 1, request.getContentLength(), 0, 1, 0, 0, 0, 0, 0);
            LOGGER.warn("[NGSIRestHandler] Bad HTTP notification (" + method + " method not supported)");
            // It would be more precise to use 405 Method Not Allowed (along with the explanatory "Allow" header
            // in the response. However, we are limited to the ones provided by Flume
            // (see https://flume.apache.org/releases/content/1.9.0/apidocs/org/apache/flume/FlumeException.html)
            // so we HTTPBadRequestException for 400 Bad Request instead
            throw new HTTPBadRequestException(method + " method not supported");
        } // if

        // Check the notificationTarget
        String target = request.getRequestURI();

        if (!target.equals(notificationTarget)) {
            serviceMetrics.add(service, servicePath, 1, request.getContentLength(), 0, 1, 0, 0, 0, 0, 0);
            LOGGER.warn("[NGSIRestHandler] Bad HTTP notification (" + target + " target not supported)");
            throw new HTTPBadRequestException(target + " target not supported");
        } // if

        // Check if received content type is null
        if (contentType == null) {
            serviceMetrics.add(service, servicePath, 1, request.getContentLength(), 0, 1, 0, 0, 0, 0, 0);
            LOGGER.warn("[NGSIRestHandler] Missing content type. Required 'application/json; charset=utf-8'");
            throw new HTTPBadRequestException("Missing content type. Required 'application/json; charset=utf-8'");
        } // if

        // Get an internal transaction ID.
        String transId = CommonUtils.generateUniqueId(null, null);

        // Get also a correlator ID if not sent in the notification. Id correlator ID is not notified
        // then correlator ID and transaction ID must have the same value.
        corrId = CommonUtils.generateUniqueId(corrId, transId);
        // Store both of them in the log4j Mapped Diagnostic Context (MDC), this way it will be accessible
        // by the whole source code.
        MDC.put(CommonConstants.LOG4J_CORR, corrId);
        MDC.put(CommonConstants.LOG4J_TRANS, transId);
        LOGGER.info("[NGSIRestHandler] Starting internal transaction (" + transId + ")");
        // Get the data content
        String data = "";
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                data += line;
            } // while
        } // try

        if (data.length() == 0) {
            serviceMetrics.add(service, servicePath, 1, request.getContentLength(), 0, 1, 0, 0, 0, 0, 0);
            LOGGER.warn("[NGSIRestHandler] Bad HTTP notification (No content in the request)");
            throw new HTTPBadRequestException("No content in the request");
        } // if

        LOGGER.info("[NGSIRestHandler] Received data (" + data + ")");

        // Parse the original data into a NotifyContextRequest object
        JSONObject content = new JSONObject(data);
        NotifyContextRequestLD notifyContextRequestLD = null;
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(Pattern.class, new PatternTypeAdapter())
            .create();

        try {
            notifyContextRequestLD = new NotifyContextRequestLD(content);
            LOGGER.debug("[NGSIRestHandler] Parsed NotifyContextRequest: " + notifyContextRequestLD.toString());

        } catch (JsonSyntaxException e) {
            serviceMetrics.add(service, servicePath, 1, request.getContentLength(), 0, 1, 0, 0, 0, 0, 0);
            LOGGER.error("[NGSIRestHandler] Runtime error (" + e.getMessage() + ")");
            return null;
        } // try catch

        // Split the notified service path and check if it matches the number of notified context responses
        String[] servicePaths = servicePath.split(",");

        if ( servicePaths.length != notifyContextRequestLD.getContextResponses().size()) {
            serviceMetrics.add(service, servicePath, 1, request.getContentLength(), 0, 1, 0, 0, 0, 0, 0);
            LOGGER.warn("[NGSIRestHandler] Bad HTTP notification ('"
                    + CommonConstants.HEADER_FIWARE_SERVICE_PATH
                    + "' header value does not match the number of notified context responses");
            throw new HTTPBadRequestException(
                    "'" + CommonConstants.HEADER_FIWARE_SERVICE_PATH
                            + "' header value does not match the number of notified context responses");
        } // if

        // Iterate on the NotifyContextRequest object in order to create an event per ContextElement
        String ids = "";

        for (int i = 0; i < notifyContextRequestLD.getContextResponses().size(); i++) {
            NotifyContextRequestLD.ContextElementResponse lData = notifyContextRequestLD.getContextResponses().get(i);
            // NotifyContextRequestLD.ContextElementResponse cer = notifyContextRequestLD.getContextResponses().get(i);
            LOGGER.debug("[NGSIRestHandler] NGSI event created for ContextElementResponse: ");//  + cer.toString());

            // Create the appropiate headers
            Map<String, String> headers = new HashMap<>();
            headers.put(CommonConstants.HEADER_FIWARE_SERVICE, service);
            LOGGER.debug("[NGSIRestHandler] Header added to NGSI event ("
                    + CommonConstants.HEADER_FIWARE_SERVICE + ": " + service + ")");
            headers.put(CommonConstants.HEADER_CORRELATOR_ID, corrId);
            LOGGER.debug("[NGSIRestHandler] Header added to NGSI event ("
                    + CommonConstants.HEADER_CORRELATOR_ID + ": " + corrId + ")");
            headers.put(NGSIConstants.FLUME_HEADER_TRANSACTION_ID, transId);
            LOGGER.debug("[NGSIRestHandler] Header added to NGSI event ("
                    + NGSIConstants.FLUME_HEADER_TRANSACTION_ID + ": " + transId + ")");
            headers.put("link", link);
            LOGGER.debug("[NGSIRestHandler] Header added to NGSI event ("
                    + "Link" + ": " + link + ")");
            if (!"".contentEquals(link)) {
                notifyContextRequestLD.setContext(link);
            }
            // Create the NGSI event and add it to the list
            NGSILDEvent ngsiLdEvent = new NGSILDEvent(
                    // Headers
                    headers,
                    // Bytes version of the notified ContextElement
                    (lData.toString() + CommonConstants.CONCATENATOR).getBytes(),
                    // Object version of the notified ContextElement
                    lData.getContextElement()
                    // Will be set with the mapped object version of the notified ContextElement, by
                    // NGSINameMappingsInterceptor (if configured). Currently, null

            );
            ngsiEvents.add(ngsiLdEvent);

            if (ids.isEmpty()) {
                ids += ngsiLdEvent.hashCode();
            } else {
                ids += "," + ngsiLdEvent.hashCode();
            } // if else
        } // for

        // Return the NGSIEvent list
        serviceMetrics.add(service, servicePath, 1, request.getContentLength(), 0, 0, 0, 0, 0, 0, 0);
        LOGGER.debug("[NGSIRestHandler] NGSI events put in the channel, ids=" + ids);
        numProcessedEvents++;
        return ngsiEvents;

    }
    
    /**
     * Checks is the give Content-Type header value is wrong or not. It is protected since it is used by the tests.
     * @param headerValue
     * @return True is the header value length is wrong, otherwise false
     */
    protected boolean wrongContentType(String headerValue) {
        if (headerValue.toLowerCase(Locale.ENGLISH).contains("application/json; charset=utf-8") ||
                headerValue.toLowerCase(Locale.ENGLISH).contains("application/ld+json") ||
                headerValue.toLowerCase(Locale.ENGLISH).contains("application/json")){
        return false;
        }else {
            return true;
        }
    } // wrongContentType
    
    /**
     * Checks if the given FIWARE service header value length is wrong or not. It is protected since it is used by the
     * tests.
     * @param headerValue
     * @return True is the header value length is wrong, otherwise false
     */
    protected boolean wrongServiceHeaderLength(String headerValue) {
        return headerValue.length() > NGSIConstants.SERVICE_HEADER_MAX_LEN;
    } // wrongServiceHeaderLength


} // NGSIRestHandler
