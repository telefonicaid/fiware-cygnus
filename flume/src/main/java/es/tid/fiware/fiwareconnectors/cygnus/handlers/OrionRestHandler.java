/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * frb@tid.es
 */

package es.tid.fiware.fiwareconnectors.cygnus.handlers;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.http.HTTPBadRequestException;
import org.apache.flume.source.http.HTTPSourceHandler;
import org.apache.http.MethodNotSupportedException;
import org.apache.log4j.Logger;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import java.util.Date;

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
    private Pattern orionVersionRegexPattern;
    private String notificationsTarget;
    private String defaultOrg;
    
    /**
     * Gets the Orion version regex pattern. It is protected due to it is only required for testing purposes.
     * @return The Orion version regex pattern
     */
    protected Pattern getOrionVersionRegexPattern() {
        return orionVersionRegexPattern;
    } // getOrionVersionRegexPattern
    
    /**
     * Gets the notifications target. It is protected due to it is only required for testing purposes.
     * @return The notifications target
     */
    protected String getNotificationTarget() {
        return notificationsTarget;
    } // getNotificationTarget

    @Override
    public void configure(Context context) {
        logger = Logger.getLogger(OrionRestHandler.class);
        orionVersionRegexPattern = Pattern.compile("orion/" + context.getString("orion_version", "*"));
        notificationsTarget = context.getString("notification_target", "notify");
        defaultOrg = context.getString("default_organization", "default_org");
        
        if (notificationsTarget.charAt(0) != '/') {
            notificationsTarget = "/" + notificationsTarget;
        } // if
    } // configure
            
    @Override
    public List<Event> getEvents(javax.servlet.http.HttpServletRequest request) throws Exception {
        // reception time in milliseconds
        long recvTimeTs = new Date().getTime();
        
        // check the method
        String method = request.getMethod().toUpperCase(Locale.ENGLISH);
        
        if (!method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        } // if

        // check the notificationsTarget
        String target = request.getRequestURI();
        
        if (!target.equals(notificationsTarget)) {
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
                Matcher matcher = orionVersionRegexPattern.matcher(headerValue);
                
                if (!matcher.matches()) {
                    throw new HTTPBadRequestException(headerValue + " user agent not supported");
                } // if
            } else if (headerName.equals(Constants.CONTENT_TYPE)) {
                if (!headerValue.contains("application/json") && !headerValue.contains("application/xml")) {
                    throw new HTTPBadRequestException(headerValue + " content type not supported");
                } else {
                    contentType = headerValue;
                } // if else if
            } else if (headerName.equals(Constants.ORG_HEADER)) {
                organization = headerValue;
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
            throw new HTTPBadRequestException("No content in the request");
        } // if

        // replace all the appearances of "contextValue" with "value" in order Orion versions under 0.10.0 may work
        data = data.replaceAll("contextValue", "value").replaceAll(">[ ]*<", "><");
        logger.debug("Received data: " + data);
        
        // create the appropiate headers
        Map<String, String> eventHeaders = new HashMap<String, String>();
        eventHeaders.put(Constants.RECV_TIME_TS, new Long(recvTimeTs).toString());
        eventHeaders.put(Constants.CONTENT_TYPE, contentType);
        eventHeaders.put(Constants.ORG_HEADER, organization == null ? defaultOrg : organization);
        
        // create the event list containing only one event
        ArrayList<Event> eventList = new ArrayList<Event>();
        eventList.add(EventBuilder.withBody(data.getBytes(), eventHeaders));
        return eventList;
    } // getEvents
 
} // OrionRestHandler
