/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * francisco.romerobueno@telefonica.com
 */

package es.tid.fiware.fiwareconnectors.cygnus.handlers;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.http.HTTPBadRequestException;
import org.apache.flume.source.http.HTTPSourceHandler;
import org.apache.http.MethodNotSupportedException;
import org.apache.log4j.Logger;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import org.slf4j.MDC;

/**
 *
 * @author frb
 * 
 * Custom HTTP handler for the default HTTP Flume source. It checks the method, notificationsTarget and headers are the
 * ones tipically sent by an instance of Orion Context Broker when notifying a context event. If everything is OK, a
 * Flume event is created in order the HTTP Flume source sends it to the Flume channel connecting the source with the
 * sink. This event contains both the context event data and a header specifying the content type (Json or XML).
 */
public class OrionRestHandler implements HTTPSourceHandler {
    
    private Logger logger;
    private String notificationsTarget;
    private String defaultOrg;
    private String eventsTTL;
    private long transactionCount;
    private long bootTimeSeconds;
    private long bootTimeMilliseconds;
    
    /**
     * Constructor. This can be used as a place where to initialize all that things we would like to do in the Flume
     * "initialization" class, which is unreachable by our code. As long as this class is instantiated almost at boot
     * time, it is the closest code to such real initialization.
     */
    public OrionRestHandler() {
        // init the logger
        logger = Logger.getLogger(OrionRestHandler.class);
        
        // init the transaction id
        transactionCount = 0;
        
        // store the boot time (not the exact boot time, but very accurate one)
        long bootTime = new Date().getTime();
        bootTimeSeconds = bootTime / 1000;
        bootTimeMilliseconds = bootTime % 1000;
        
        // print Cygnus version
        logger.info("Cygnus version (" + getCygnusVersion() + ")");
    } // OrionRestHandler
    
    /**
     * Gets the notifications target. It is protected due to it is only required for testing purposes.
     * @return The notifications target
     */
    protected String getNotificationTarget() {
        return notificationsTarget;
    } // getNotificationTarget

    @Override
    public void configure(Context context) {
        notificationsTarget = context.getString("notification_target", "notify");
        logger.debug("Reading configuration (notification_target=" + notificationsTarget + ")");

        if (notificationsTarget.charAt(0) != '/') {
            notificationsTarget = "/" + notificationsTarget;
        } // if
        
        defaultOrg = context.getString("default_organization", "default_org");
        
        if (defaultOrg.length() > Constants.ORG_MAX_LEN) {
            logger.error("Bad configuration (Default organization length greater than " + Constants.ORG_MAX_LEN + ")");
            logger.info("Exiting Cygnus");
            System.exit(-1);
        } // if
        
        logger.debug("Reading configuration (default_organization=" + defaultOrg + ")");
        eventsTTL = context.getString("events_ttl", "10");
        logger.debug("Reading configuration (events_ttl=" + eventsTTL + ")");
        
        logger.info("Startup completed");
    } // configure
            
    @Override
    public List<Event> getEvents(javax.servlet.http.HttpServletRequest request) throws Exception {
        // reception time in milliseconds
        long recvTimeTs = new Date().getTime();
        
        // get a transaction id and store it in the log4j Mapped Diagnostic Context (MDC); this way it will be
        // accessible by the whole source code
        String transId = generateTransId();
        MDC.put(Constants.TRANSACTION_ID, transId);
        logger.info("Starting transaction (" + transId + ")");
        
        // check the method
        String method = request.getMethod().toUpperCase(Locale.ENGLISH);
        
        if (!method.equals("POST")) {
            logger.warn("Bad HTTP notification (" + method + " method not supported)");
            throw new MethodNotSupportedException(method + " method not supported");
        } // if

        // check the notificationsTarget
        String target = request.getRequestURI();
        
        if (!target.equals(notificationsTarget)) {
            logger.warn("Bad HTTP notification (" + target + " target not supported)");
            throw new HTTPBadRequestException(target + " target not supported");
        } // if
        
        // check the headers looking for not supported user agents, content type and tenant/organization
        Enumeration headerNames = request.getHeaderNames();
        String contentType = null;
        String organization = null;
        
        while (headerNames.hasMoreElements()) {
            String headerName = ((String) headerNames.nextElement()).toLowerCase(Locale.ENGLISH);
            String headerValue = request.getHeader(headerName).toLowerCase(Locale.ENGLISH);
            
            if (headerName.equals(Constants.USER_AGENT)) {
                if (!headerValue.startsWith("orion")) {
                    logger.warn("Bad HTTP notification (" + headerValue + " user agent not supported)");
                    throw new HTTPBadRequestException(headerValue + " user agent not supported");
                } // if
            } else if (headerName.equals(Constants.CONTENT_TYPE)) {
                if (!headerValue.contains("application/json") && !headerValue.contains("application/xml")) {
                    logger.warn("Bad HTTP notification (" + headerValue + " content type not supported)");
                    throw new HTTPBadRequestException(headerValue + " content type not supported");
                } else {
                    contentType = headerValue;
                } // if else
            } else if (headerName.equals(Constants.ORG_HEADER)) {
                if (headerValue.length() > Constants.ORG_MAX_LEN) {
                    logger.warn("Bad HTTP notification (organization length greater than " + Constants.ORG_MAX_LEN
                            + ")");
                    throw new HTTPBadRequestException("organization length greater than " + Constants.ORG_MAX_LEN
                            + ")");
                } else {
                    organization = headerValue;
                } // if else
            } // if else if
        } // for

        // get the data content
        String data = "";
        String line;
        BufferedReader reader = request.getReader();
        
        while ((line = reader.readLine()) != null) {
            data += line;
        } // while
                
        if (data.length() == 0) {
            logger.warn("Bad HTTP notification (No content in the request)");
            throw new HTTPBadRequestException("No content in the request");
        } // if

        // data adaptation; two replacements:
        //   1. replace all the appearances of "contextValue" with "value" in order Orion versions under 0.10.0 may
        //      work (Json content type only)
        //   2. replace all the white lines between tags with nothing; the regex ">[ ]*<" means "all the white spaces
        //      between '>' and '<', e.g. "<tag1>1</tag1>      <tag2>2</tag2>" becomes "<tag1>1</tag1><tag2>2</tag2>"
        
        if (contentType.equals("application/json")) {
            data = data.replaceAll("contextValue", "value");
        } // if

        data = data.replaceAll(">[ ]*<", "><");
        logger.info("Received data (" + data + ")");
        
        // create the appropiate headers
        Map<String, String> eventHeaders = new HashMap<String, String>();
        eventHeaders.put(Constants.RECV_TIME_TS, new Long(recvTimeTs).toString());
        logger.debug("Adding flume event header (name=" + Constants.RECV_TIME_TS
                + ", value=" + new Long(recvTimeTs).toString() + ")");
        eventHeaders.put(Constants.CONTENT_TYPE, contentType);
        logger.debug("Adding flume event header (name=" + Constants.CONTENT_TYPE + ", value=" + contentType + ")");
        eventHeaders.put(Constants.ORG_HEADER, organization == null ? defaultOrg : organization);
        logger.debug("Adding flume event header (name=" + Constants.ORG_HEADER
                + ", value=" + organization == null ? defaultOrg : organization + ")");
        eventHeaders.put(Constants.TRANSACTION_ID, transId);
        logger.debug("Adding flume event header (name=" + Constants.TRANSACTION_ID + ", value=" + transId + ")");
        eventHeaders.put(Constants.TTL, eventsTTL);
        logger.debug("Adding flume event header (name=" + Constants.TTL + ", value=" + eventsTTL + ")");
        
        // create the event list containing only one event
        ArrayList<Event> eventList = new ArrayList<Event>();
        Event event = EventBuilder.withBody(data.getBytes(), eventHeaders);
        eventList.add(event);
        logger.info("Event put in the channel (id=" + event.hashCode() + ", ttl=" + eventsTTL +")");
        return eventList;
    } // getEvents
    
    /**
     * Generates a new unique transaction identifier. The format for this id is:
     * <bootTimeSeconds>-<bootTimeMilliseconds>-<transactionCount%10000000000>
     * @return A new unique transaction identifier
     */
    private String generateTransId() {
        long transCountTrunked = transactionCount % 10000000000L;
        String transId = bootTimeSeconds + "-" + bootTimeMilliseconds + "-" + String.format("%010d", transCountTrunked);
        
        // check if the transactionCount must be restarted
        if (transCountTrunked == 9999999999L) {
            transactionCount = 0;
            bootTimeMilliseconds = (bootTimeMilliseconds + 1) % 1000; // this could also overflow!
        } else {
            transactionCount++;
        } // if else
        
        return transId;
    } // generateTransId
    
    /**
     * Gets the Cygnus version from the pom.xml.
     * @return The Cygnus version
     */
    private String getCygnusVersion() {
        String path = "/pom.properties";
        InputStream stream = getClass().getResourceAsStream(path);
        
        if (stream == null) {
            logger.warn("The stream regarding pom.properties is NULL");
            return "UNKNOWN";
        } // if
        
        Properties props = new Properties();
        
        try {
            props.load(stream);
            stream.close();
            return (String) props.get("version");
        } catch (IOException e) {
            logger.warn("Cannot get the version from pom.properties stream (Details=" + e.getMessage() + ")");
            return "UNKNOWN";
        } // try catch
    } // getCygnusVersion
 
} // OrionRestHandler
