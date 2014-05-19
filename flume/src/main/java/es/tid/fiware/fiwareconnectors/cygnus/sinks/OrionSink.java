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
import es.tid.fiware.fiwareconnectors.cygnus.http.HttpClientFactory;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
 *  - httpClientFactory, a HTTP clients generator
 *  - timeHelper, a wrapper of the Java timestamp methods, in order it can be mocked in the tests
 * The common methods are:
 *  - void stop()
 *  - Status process() throws EventDeliveryException
 *  - void persist(Event event) throws Exception
 * The non common parts, and therefore those that are sink dependant and must be implemented are:
 *  - void configure(Context context)
 *  - void start()
 *  - void processContextResponses(ArrayList contextResponses) throws Exception
 */
public abstract class OrionSink extends AbstractSink implements Configurable {

    private Logger logger;
    protected HttpClientFactory httpClientFactory;
    protected TimeHelper timeHelper;
    
    /**
     * Constructor.
     */
    public OrionSink() {
        // invoke the super class constructor
        super();
        
        // create a logger
        logger = Logger.getLogger(OrionSink.class);
        
        // create a Http clients factory (no SSL)
        httpClientFactory = new HttpClientFactory(false);
        
        // create the timer
        timeHelper = new TimeHelper();
    } // OrionSink
    
    @Override
    public void stop() {
        httpClientFactory = null;
        timeHelper = null;
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
        
        if (eventHeaders.get("content-type").contains("application/json")) {
            logger.debug("The content-type was application/json");
            Gson gson = new Gson();
            notification = gson.fromJson(eventData, NotifyContextRequest.class);
            logger.debug("Json parsed");
        } else if (eventHeaders.get("content-type").contains("application/xml")) {
            logger.debug("The content-type was application/xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(eventData));
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            notification = new NotifyContextRequest(doc);
            logger.debug("XML parsed");
        } else {
            throw new Exception("Unrecognized content type (not Json nor XML)");
        } // if else if

        // process the event data
        ArrayList contextResponses = notification.getContextResponses();
        logger.debug("num context responses "  + contextResponses.size());
        // FIXME: when dealing with multi-tenancy, the user's username owning the context data will be be given in the
        // notification; this username will be used to appropriately store the data in a user's specific space
        processContextResponses("FIXME", contextResponses);
    } // persist
    
    /**
     * This is the method the classes extending this class must implement when dealing with persistion.
     * @param contextResponses
     * @throws Exception
     */
    abstract void processContextResponses(String username, ArrayList contextResponses) throws Exception;
    
    /**
     * Class wrapping the time related operations, allowing that way those operation can be mocked.
     */
    public class TimeHelper {

        /**
         * Gets the current time in miliseconds.
         * @return The current time in miliseconds.
         */
        public long getTime() {
            return new Date().getTime() / 1000;
        } // getTime

        /**
         * Gets the current human readable time.
         * @return The human readable time.
         */
        public String getTimeString() {
            return new Timestamp(new Date().getTime()).toString().replaceAll(" ", "T");
        } // getTimeString

    } // TimeHelper
        
} // OrionSink