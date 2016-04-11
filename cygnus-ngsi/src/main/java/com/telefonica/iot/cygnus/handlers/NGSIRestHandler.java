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

package com.telefonica.iot.cygnus.handlers;

import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.io.BufferedReader;
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
import org.apache.http.MethodNotSupportedException;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import java.util.Date;
import org.apache.flume.event.EventBuilder;
import org.slf4j.MDC;
import java.util.UUID;

/**
 *
 * @author frb
 * 
 * Custom HTTP handler for the default HTTP Flume source. It checks the method, notificationTarget and headers are the
 * ones tipically sent by an instance of Orion Context Broker when notifying a context event. If everything is OK, a
 * Flume event is created in order the HTTP Flume source sends it to the Flume channel connecting the source with the
 * sink. This event contains both the context event data and a header specifying the content type (Json).
 */
public class NGSIRestHandler implements HTTPSourceHandler {
    
    // LOGGER
    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIRestHandler.class);
    
    // configuration parameters
    private boolean invalidConfiguration;
    private String notificationTarget;
    private String defaultService;
    private String defaultServicePath;
    
    // shared variables, making them static all the instances of this class will share them
    private static final Object LOCK = new Object();
    private static long transactionCount = 0;
    private static final long BOOTTIME = new Date().getTime();
    private static final long BOOTTIMESECONDS = BOOTTIME / 1000;
    private static long bootTimeMiliseconds = BOOTTIME % 1000;
    private static long numReceivedEvents = 0;
    private static long numProcessedEvents = 0;
    
    /**
     * Constructor. This can be used as a place where to initialize all that things we would like to do in the Flume
     * "initialization" class, which is unreachable by our code. As long as this class is instantiated almost at boot
     * time, it is the closest code to such real initialization.
     */
    public NGSIRestHandler() {
        // initially, the configuration is meant to be valid
        invalidConfiguration = false;
        
        // print Cygnus version
        LOGGER.info("Cygnus version (" + NGSIUtils.getCygnusVersion() + "." + NGSIUtils.getLastCommit() + ")");
    } // NGSIRestHandler
    
    /**
     * Gets the setup time.
     * @return The setup time
     */
    public long getBootTime() {
        return BOOTTIME;
    } // getBootTime
    
    /**
     * Gets the number of received events.
     * @return The number of received events
     */
    public long getNumReceivedEvents() {
        return numReceivedEvents;
    } // getNumReceivedEvents
        
    /**
     * Gets the number of processed events.
     * @return The number of processed events
     */
    public long getNumProcessedEvents() {
        return numProcessedEvents;
    } // getNumProcessedEvents
    
    /**
     * Sets the number of received events.
     * @param n The number of received events to be set
     */
    public void setNumReceivedEvents(long n) {
        numReceivedEvents = n;
    } // setNumReceivedEvents
    
    /**
     * Sets the number of processed events.
     * @param n The number of processed events to be set
     */
    public void setNumProcessedEvents(long n) {
        numProcessedEvents = n;
    } // setNumProcessedEvents
    
    /**
     * Gets the notifications target. It is protected due to it is only required for testing purposes.
     * @return The notifications target
     */
    protected String getNotificationTarget() {
        return notificationTarget;
    } // getNotificationTarget
    
    /**
     * Gets the default service. It is protected due to it is only required for testing purposes.
     * @return
     */
    protected String getDefaultService() {
        return defaultService;
    } // getDefaultService
    
    /**
     * Gets the default service path. It is protected due to it is only required for testing purposes.
     * @return
     */
    protected String getDefaultServicePath() {
        return defaultServicePath;
    } // getDefaultServicePath
    
    /**
     * Gets true if the configuration is invalid, false otherwise. It is protected due to it is only
     * required for testing purposes.
     * @return
     */
    protected boolean getInvalidConfiguration() {
        return invalidConfiguration;
    } // getInvalidConfiguration

    @Override
    public void configure(Context context) {
        notificationTarget = context.getString(NGSIConstants.PARAM_NOTIFICATION_TARGET, "/notify");
        
        if (notificationTarget.startsWith("/")) {
            LOGGER.debug("Reading configuration (" + NGSIConstants.PARAM_NOTIFICATION_TARGET + "="
                    + notificationTarget + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.error("Bad configuration (" + NGSIConstants.PARAM_NOTIFICATION_TARGET + "="
                    + notificationTarget + ") -- Must start with '/'");
        } // if else
        
        defaultService = context.getString(NGSIConstants.PARAM_DEFAULT_SERVICE, "default");
        
        if (defaultService.length() > NGSIConstants.SERVICE_HEADER_MAX_LEN) {
            invalidConfiguration = true;
            LOGGER.error("Bad configuration ('" + NGSIConstants.PARAM_DEFAULT_SERVICE
                    + "' parameter length greater than " + NGSIConstants.SERVICE_HEADER_MAX_LEN + ")");
        } else {
            LOGGER.debug("Reading configuration (" + NGSIConstants.PARAM_DEFAULT_SERVICE + "="
                    + defaultService + ")");
        } // if else
        
        defaultServicePath = context.getString(NGSIConstants.PARAM_DEFAULT_SERVICE_PATH, "/");
        
        if (defaultServicePath.length() > NGSIConstants.SERVICE_PATH_HEADER_MAX_LEN) {
            invalidConfiguration = true;
            LOGGER.error("Bad configuration ('" + NGSIConstants.PARAM_DEFAULT_SERVICE_PATH
                    + "' parameter length greater " + "than " + NGSIConstants.SERVICE_PATH_HEADER_MAX_LEN + ")");
        } else if (!defaultServicePath.startsWith("/")) {
            invalidConfiguration = true;
            LOGGER.error("Bad configuration ('" + NGSIConstants.PARAM_DEFAULT_SERVICE_PATH
                    + "' must start with '/')");
        } else {
            LOGGER.debug("Reading configuration (" + NGSIConstants.PARAM_DEFAULT_SERVICE_PATH + "="
                    + defaultServicePath + ")");
        } // if else
        
        LOGGER.info("Startup completed");
    } // configure
            
    @Override
    public List<Event> getEvents(javax.servlet.http.HttpServletRequest request) throws Exception {
        // if the configuration is invalid, nothing has to be done but to return null
        if (invalidConfiguration) {
            LOGGER.debug("Invalid configuration, thus returning an empty list of Flume events");
            return new ArrayList<Event>();
        } // if
        
        numReceivedEvents++;
        
        // check the method
        String method = request.getMethod().toUpperCase(Locale.ENGLISH);
        
        if (!method.equals("POST")) {
            LOGGER.warn("Bad HTTP notification (" + method + " method not supported)");
            throw new MethodNotSupportedException(method + " method not supported");
        } // if

        // check the notificationTarget
        String target = request.getRequestURI();
        
        if (!target.equals(notificationTarget)) {
            LOGGER.warn("Bad HTTP notification (" + target + " target not supported)");
            throw new HTTPBadRequestException(target + " target not supported");
        } // if
        
        // check the headers looking for not supported user agents, content type and tenant/organization
        Enumeration headerNames = request.getHeaderNames();
        String corrId = null;
        String contentType = null;
        String service = null;
        String servicePath = null;
        
        while (headerNames.hasMoreElements()) {
            String headerName = ((String) headerNames.nextElement()).toLowerCase(Locale.ENGLISH);
            String headerValue = request.getHeader(headerName);
            LOGGER.debug("Header " + headerName + " received with value " + headerValue);
            
            if (headerName.equals(NGSIConstants.HEADER_CORRELATOR_ID)) {
                corrId = headerValue;
            } else if (headerName.equals(NGSIConstants.HTTP_HEADER_CONTENT_TYPE)) {
                if (!headerValue.contains("application/json")) {
                    LOGGER.warn("Bad HTTP notification (" + headerValue + " content type not supported)");
                    throw new HTTPBadRequestException(headerValue + " content type not supported");
                } else {
                    contentType = headerValue;
                } // if else
            } else if (headerName.equals(NGSIConstants.HEADER_FIWARE_SERVICE)) {
                if (headerValue.length() > NGSIConstants.SERVICE_HEADER_MAX_LEN) {
                    LOGGER.warn("Bad HTTP notification ('fiware-service' header length greater than "
                            + NGSIConstants.SERVICE_HEADER_MAX_LEN + ")");
                    throw new HTTPBadRequestException("'fiware-service' header length greater than "
                            + NGSIConstants.SERVICE_HEADER_MAX_LEN + ")");
                } else {
                    service = headerValue;
                } // if else
            } else if (headerName.equals(NGSIConstants.HEADER_FIWARE_SERVICE_PATH)) {
                if (headerValue.length() > NGSIConstants.SERVICE_PATH_HEADER_MAX_LEN) {
                    LOGGER.warn("Bad HTTP notification ('fiware-servicePath' header length greater than "
                            + NGSIConstants.SERVICE_PATH_HEADER_MAX_LEN + ")");
                    throw new HTTPBadRequestException("'fiware-servicePath' header length greater than "
                            + NGSIConstants.SERVICE_PATH_HEADER_MAX_LEN + ")");
                } else if (!headerValue.startsWith("/")) {
                    LOGGER.warn("Bad HTTP notification ('fiware-servicePath' heacder value must start with '/'");
                    throw new HTTPBadRequestException("'fiware-servicePath' header value must start with '/'");
                } else {
                    servicePath = headerValue;
                } // if else
            } // if else if
        } // while
        
        // check if received content type is null
        if (contentType == null) {
            LOGGER.warn("Missing content type. Required application/json.");
            throw new HTTPBadRequestException("Missing content type. Required application/json.");
        } // if
        
        // get a service and servicePath and store it in the log4j Mapped Diagnostic Context (MDC)
        MDC.put(NGSIConstants.LOG4J_SVC, service == null ? defaultService : service);
        MDC.put(NGSIConstants.LOG4J_SUBSVC, servicePath == null ? defaultServicePath : servicePath);
        
        // Get an internal transaction ID.
        String transId = generateUniqueId(null, null);
        
        // Get also a correlator ID if not sent in the notification. Id correlator ID is not notified
        // then correlator ID and transaction ID must have the same value.
        corrId = generateUniqueId(corrId, transId);
        
        // Store both of them in the log4j Mapped Diagnostic Context (MDC), this way it will be accessible
        // by the whole source code.
        MDC.put(NGSIConstants.LOG4J_CORR, corrId);
        MDC.put(NGSIConstants.LOG4J_TRANS, transId);
        LOGGER.info("Starting internal transaction (" + transId + ")");
        
        // get the data content
        String data = "";
        String line;
        BufferedReader reader = request.getReader();
        
        while ((line = reader.readLine()) != null) {
            data += line;
        } // while
                
        if (data.length() == 0) {
            LOGGER.warn("Bad HTTP notification (No content in the request)");
            throw new HTTPBadRequestException("No content in the request");
        } // if

        LOGGER.info("Received data (" + data + ")");
        
        // create the appropiate headers
        Map<String, String> eventHeaders = new HashMap<String, String>();
        eventHeaders.put(NGSIConstants.HEADER_FIWARE_SERVICE, service == null ? defaultService : service);
        LOGGER.debug("Adding flume event header (name=" + NGSIConstants.HEADER_FIWARE_SERVICE
                + ", value=" + (service == null ? defaultService : service) + ")");
        eventHeaders.put(NGSIConstants.HEADER_FIWARE_SERVICE_PATH, servicePath == null
                ? defaultServicePath : servicePath);
        LOGGER.debug("Adding flume event header (name=" + NGSIConstants.HEADER_FIWARE_SERVICE_PATH
                + ", value=" + (servicePath == null ? defaultServicePath : servicePath) + ")");
        eventHeaders.put(NGSIConstants.HEADER_CORRELATOR_ID, corrId);
        LOGGER.debug("Adding flume event header (name=" + NGSIConstants.HEADER_CORRELATOR_ID
                + ", value=" + corrId + ")");
        eventHeaders.put(NGSIConstants.FLUME_HEADER_TRANSACTION_ID, transId);
        LOGGER.debug("Adding flume event header (name=" + NGSIConstants.FLUME_HEADER_TRANSACTION_ID
                + ", value=" + transId + ")");
        
        // create the event list containing only one event
        ArrayList<Event> eventList = new ArrayList<Event>();
        Event event = EventBuilder.withBody(data.getBytes(), eventHeaders);
        eventList.add(event);
        LOGGER.debug("Event put in the channel, id=" + event.hashCode());
        numProcessedEvents++;
        return eventList;
    } // getEvents
    
    /**
     * Generates a new unique identifier. The format for this notifiedId is:
     * bootTimeSecond-bootTimeMilliseconds-transactionCount%10000000000
     * @param notifiedId An alrady existent identifier, most probably a notified one
     * @param transactionId An already existent internal identifier
     * @return A new unique identifier
     */
    protected String generateUniqueId(String notifiedId, String transactionId) {
        if (notifiedId != null) {
            return notifiedId;
        } else if (transactionId != null) {
            return transactionId;
        } else {
            // Check this SOF link: A bug doesn't allow to uuid be thread-safe
            // For that reason we use LOCK in order to avoid problems with treads
            // http://stackoverflow.com/questions/7212635/is-java-util-uuid-thread-safe
            synchronized (LOCK) {
                return UUID.randomUUID().toString();
            } // synchronized
        } // else
    } // generateUniqueId
 
} // NGSIRestHandler
