package es.tid.fiware.orionconnectors.cosmosinjector.experimental;

import es.tid.fiware.orionconnectors.cosmosinjector.http.BasicHttpServer;
import org.apache.flume.source.AbstractSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.Context;
import org.apache.log4j.Logger;

/**
 * 
 * @author frb
 * 
 * Custom Http source for Orion Context Broker. There exists a default Http source in Flume which receives data in a
 * given port and thus this must be considered as a training implementation.
 * 
 * DO NOT USE
 */
public class OrionSource extends AbstractSource implements Configurable, EventDrivenSource {
    
    private Logger logger;
    private int notificationsPort;
    private BasicHttpServer server;
    
    /**
     * Constructor.
     */
    public OrionSource() {
        // create a logger
        logger = Logger.getLogger(OrionSource.class);
    } // OrionSoruce
    
    @Override
    public void configure(Context context) {
        notificationsPort = context.getInteger("notifications_port", 12345);
    } // configure

    @Override
    public void start() {
        // start a basic Http server listening for connections in the given port
        server = new BasicHttpServer(notificationsPort, new OrionRestHandler(getChannelProcessor()));
        server.start();
        super.start();
    } // start

    @Override
    public void stop() {
        server.stop();
        super.stop();
    } // stop
    
} // OrionSource
