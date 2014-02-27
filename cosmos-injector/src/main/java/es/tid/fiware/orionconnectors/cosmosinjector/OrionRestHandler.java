package es.tid.fiware.orionconnectors.cosmosinjector;

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

/**
 *
 * @author frb
 * 
 * Custom HTTP handler for the default HTTP Flume source. It checks the method, target and headers are the ones
 * tipically sent by an instance of Orion Context Broker when notifying a context event. If everything is OK, a Flume
 * event is created in order the HTTP Flume source sends it to the Flume channel connecting the source with the sink.
 * This event contains both the context event data and a header specifying the content type (Json or XML).
 */
public class OrionRestHandler implements HTTPSourceHandler {

    @Override
    public void configure(Context context) {
        
    } // configure
            
    @Override
    public List<Event> getEvents(javax.servlet.http.HttpServletRequest request) throws Exception {
        // check the method
        String method = request.getMethod().toUpperCase(Locale.ENGLISH);

        if (!method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        } // if

        // check the target
        String target = request.getRequestURI();
        
        if (!target.equals("/notify")) {
            throw new HTTPBadRequestException(target + " target not supported");
        } // if
        
        // check the headers looking for not supported user agents and content type
        Enumeration headerNames = request.getHeaderNames();
        String contentType = null;
        
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            if (headerName.equals("User-Agent")) {
                if (!headerValue.equals("orion/0.9.0")) {
                    throw new HTTPBadRequestException(headerValue + " user agent not supported");
                } // if
            } else if (headerName.equals("Content-Type")) {
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
        
        // create the appropiate headers
        Map<String, String> eventHeaders = new HashMap<String, String>();
        eventHeaders.put("content-type", contentType);
        
        // create the event list containing only one event
        ArrayList<Event> eventList = new ArrayList<Event>();
        eventList.add(EventBuilder.withBody(data.getBytes(), eventHeaders));
        return eventList;
    } // getEvents
}
