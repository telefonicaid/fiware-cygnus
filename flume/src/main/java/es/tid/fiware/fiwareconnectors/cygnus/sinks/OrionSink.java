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

package es.tid.fiware.fiwareconnectors.cygnus.sinks;

import com.google.gson.Gson;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import org.apache.flume.Channel;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Sink.Status;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author frb
 * 
 * Abstract class containing the common code to all the sinks persisting data comming from Orion Context Broker.
 * 
 * The common attributes are:
 *  - timeHelper, a wrapper of the Java timestamp methods, in order it can be mocked in the tests
 * The common methods are:
 *  - void stop()
 *  - Status process() throws EventDeliveryException
 *  - void persist(Event event) throws Exception
 * The non common parts, and therefore those that are sink dependant and must be implemented are:
 *  - void configure(Context context)
 *  - void start()
 *  - void persist(ArrayList contextResponses) throws Exception
 */
public abstract class OrionSink extends AbstractSink implements Configurable {

    private Logger logger;
    
    /**
     * Constructor.
     */
    public OrionSink() {
        // invoke the super class constructor
        super();
        
        // create a logger
        logger = Logger.getLogger(OrionSink.class);
    } // OrionSink
    
    @Override
    public void stop() {
        super.stop();
    } // stop

    @Override
    public Status process() throws EventDeliveryException {
        Status status = null;

        // start transaction
        Channel ch = getChannel();
        Transaction txn = ch.getTransaction();
        txn.begin();

        try {
            // get an event
            Event event = ch.take();
            
            if (event == null) {
                txn.commit();
                return Status.READY;
            } // if
            
            // persist the event
            logger.info("An event was taken from the channel, it must be persisted");
            persist(event);
            
            // specify the transaction has succeded
            txn.commit();
            status = Status.READY;
        } catch (Throwable t) {
            txn.rollback();
            logger.error(t.getMessage());
            status = Status.BACKOFF;

            // rethrow all errors
            if (t instanceof Error) {
                throw (Error) t;
            } // if
        } finally {
            // close the transaction
            txn.close();
        } // try catch finally

        return status;
    } // process
    
    /**
     * Given an event, it is preprocessed before it is persisted. Depending on the content type, it is appropriately
     * parsed (Json or XML) in order to obtain a NotifyContextRequest instance.
     * 
     * @param event A Flume event containing the data to be persisted and certain metadata (headers).
     * @throws Exception
     */
    private void persist(Event event) throws Exception {
        String eventData = new String(event.getBody());
        Map<String, String> eventHeaders = event.getHeaders();
        
        // parse the eventData
        NotifyContextRequest notification = null;
        
        if (eventHeaders.get(Constants.CONTENT_TYPE).contains("application/json")) {
            logger.debug("The content-type was application/json");
            Gson gson = new Gson();
            notification = gson.fromJson(eventData, NotifyContextRequest.class);
        } else if (eventHeaders.get(Constants.CONTENT_TYPE).contains("application/xml")) {
            logger.debug("The content-type was application/xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(eventData));
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            notification = new NotifyContextRequest(doc);
        } else {
            throw new Exception("Unrecognized content type (not Json nor XML)");
        } // if else if

        // process the event data
        ArrayList contextResponses = notification.getContextResponses();
        persist(eventHeaders.get(Constants.ORG_HEADER), new Long(eventHeaders.get(Constants.RECV_TIME_TS)).longValue(),
                contextResponses);
    } // persist
    
    /**
     * This is the method the classes extending this class must implement when dealing with persistence.
     * @param organization the organization/tenant to persist the data
     * @param recvTimeTs the reception time of the context information
     * @param contextResponses the context element responses to persist
     * @throws Exception
     */
    abstract void persist(String organization, long recvTimeTs, ArrayList contextResponses) throws Exception;
        
} // OrionSink