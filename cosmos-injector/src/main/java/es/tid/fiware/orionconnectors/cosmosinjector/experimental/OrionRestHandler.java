package es.tid.fiware.orionconnectors.cosmosinjector.experimental;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.flume.ChannelException;
import org.apache.flume.Event;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.event.EventBuilder;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author frb
 * 
 * Custom HTTP handler for the custom Orion Source for Flume. It checks the method, target and headers are the ones
 * tipically sent by an instance of Orion Context Broker when notifying a context event. If everything is OK, a Flume
 * event is created and sent to the Flume channel connecting the source with the sink. This event contains both the
 * context event data and a header specifying the content type (Json or XML).
 * 
 * DO NOT USE
 * 
 */
public class OrionRestHandler implements HttpRequestHandler  {
    
    private ChannelProcessor channel;

    /**
     * Constructor.
     * 
     * @param channel Flume channel where to send data events.
     */
    public OrionRestHandler(ChannelProcessor channel) {
        super();
        this.channel = channel;
    } // OrionRestHandler

    @Override
    public void handle(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {
        // check the method
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);

        if (!method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        } // if

        // check the target
        String target = request.getRequestLine().getUri();
        
        if (!target.equals("/notify")) {
            throw new HttpException(target + " target not supported");
        } // if
        
        // check the headers looking for not supported user agents and content type
        org.apache.http.Header[] headers = request.getAllHeaders();
        String contentType = null;
        
        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];
            
            if (header.getName().equals("User-Agent")) {
                if (!header.getValue().equals("orion/0.9.0")) {
                    throw new HttpException(header.getValue() + " user agent not supported");
                } // if
            } else if (header.getName().equals("Content-Type")) {
                if (!header.getValue().contains("application/json") && !header.getValue().contains("application/xml")) {
                    throw new HttpException(header.getValue() + " content type not supported");
                } else {
                    contentType = header.getValue();
                } // if else if
            } // if else if
        } // for

        // check if the request has a valid content
        if (!(request instanceof HttpEntityEnclosingRequest)) {
            throw new HttpException("No content in the request");
        } // if
        
        // get the data content
        HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
        
        // create the appropiate headers
        Map<String, String> eventHeaders = new HashMap<String, String>();
        eventHeaders.put("content-type", contentType);
        
        // send the data to the sink via the channel
        Event event = EventBuilder.withBody(EntityUtils.toByteArray(entity), eventHeaders);
        
        try {
            channel.processEvent(event);
        } catch (ChannelException e) {
            throw e;
        } // try catch
    } // handle

} // OrionRestHandler
