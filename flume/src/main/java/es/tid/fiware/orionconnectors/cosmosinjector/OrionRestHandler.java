/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of cosmos-injector (FI-WARE project).
 *
 * cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with [PROJECT NAME]. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * frb@tid.es
 */

package es.tid.fiware.orionconnectors.cosmosinjector;

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
    private Pattern pattern;
    private String notificationsTarget;

    @Override
    public void configure(Context context) {
        logger = Logger.getLogger(OrionRestHandler.class);
        pattern = Pattern.compile("orion/" + context.getString("orion_version", "*"));
        notificationsTarget = context.getString("notification_target", "notify");
        
        if (notificationsTarget.charAt(0) != '/') {
            notificationsTarget = "/" + notificationsTarget;
        } // if
    } // configure
            
    @Override
    public List<Event> getEvents(javax.servlet.http.HttpServletRequest request) throws Exception {
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
        
        // check the headers looking for not supported user agents and content type
        Enumeration headerNames = request.getHeaderNames();
        String contentType = null;
        
        while (headerNames.hasMoreElements()) {
            String headerName = ((String) headerNames.nextElement()).toLowerCase(Locale.ENGLISH);
            String headerValue = request.getHeader(headerName).toLowerCase(Locale.ENGLISH);
            
            if (headerName.equals("user-agent")) {
                Matcher matcher = pattern.matcher(headerValue);
                
                if (!matcher.matches()) {
                    throw new HTTPBadRequestException(headerValue + " user agent not supported");
                } // if
            } else if (headerName.equals("content-type")) {
                if (!headerValue.contains("application/json") && !headerValue.contains("application/xml")) {
                    throw new HTTPBadRequestException(headerValue + " content type not supported");
                } else {
                    contentType = headerValue;
                } // if else if
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

	// replace all the appearances of "contextValue" with "value" in order Orion versions under 0.10.0
	// may work
	data = data.replaceAll("contextValue", "value");
        
        // create the appropiate headers
        Map<String, String> eventHeaders = new HashMap<String, String>();
        eventHeaders.put("content-type", contentType);
        
        // create the event list containing only one event
        ArrayList<Event> eventList = new ArrayList<Event>();
        eventList.add(EventBuilder.withBody(data.getBytes(), eventHeaders));
        return eventList;
    } // getEvents
}
