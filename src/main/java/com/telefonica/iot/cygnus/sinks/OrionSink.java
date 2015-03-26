/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.sinks;

import com.google.gson.Gson;
import com.telefonica.iot.cygnus.channels.CygnusChannel;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequestSAXHandler;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.util.Map;
import com.telefonica.iot.cygnus.utils.Constants;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.flume.Channel;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Sink.Status;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.apache.log4j.MDC;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author frb
 * 
 * Abstract class containing the common code to all the sinks persisting data comming from Orion Context Broker.
 * 
 * The common attributes are:
 *  - there is no common attributes
 * The common methods are:
 *  - void stop()
 *  - Status process() throws EventDeliveryException
 *  - void persist(Event event) throws Exception
 * The non common parts, and therefore those that are sink dependant and must be implemented are:
 *  - void configure(Context context)
 *  - void start()
 *  - void persist(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception
 */
public abstract class OrionSink extends AbstractSink implements Configurable {

    private static final CygnusLogger LOGGER = new CygnusLogger(OrionSink.class);
    
    /**
     * Constructor.
     */
    public OrionSink() {
        // invoke the super class constructor
        super();
    } // OrionSink

    @Override
    public void stop() {
        super.stop();
    } // stop

    @Override
    public Status process() throws EventDeliveryException {
        Status status = null;
        Channel ch = null;
        Transaction txn = null;
        Event event = null;

        try {
            // get the channel
            ch = getChannel();
        } catch (Exception e) {
            LOGGER.error("Channel error (The channel could not be got. Details=" + e.getMessage() + ")");
            throw new EventDeliveryException(e);
        } // try catch // try catch

        try {
            // start a Flume transaction (it is not the same than a Cygnus transaction!)
            txn = ch.getTransaction();
            txn.begin();
        } catch (Exception e) {
            LOGGER.error("Channel error (The Flume transaction could not be started. Details=" + e.getMessage() + ")");
            throw new EventDeliveryException(e);
        } // try catch // try catch

        try {
            // get the event
            event = ch.take();

            if (event == null) {
                txn.commit();
                txn.close();
                return Status.READY;
            } // if
        } catch (Exception e) {
            LOGGER.error("Channel error (The event could not be got. Details=" + e.getMessage() + ")");
            throw new EventDeliveryException(e);
        } // try catch // try catch

        try {
            // set the transactionId in MDC
            MDC.put(Constants.HEADER_TRANSACTION_ID, event.getHeaders().get(Constants.HEADER_TRANSACTION_ID));
        } catch (Exception e) {
            LOGGER.error("Runtime error (" + e.getMessage() + ")");
        } // catch // catch

        LOGGER.info("Event got from the channel (id=" + event.hashCode() + ", headers="
                + event.getHeaders().toString() + ", bodyLength=" + event.getBody().length + ")");

        try {
            // persist the event
            persist(event);

            // the transaction has succeded
            txn.commit();
            status = Status.READY;
        } catch (Exception e) {
            // rollback only if the exception is about a persistence error
            if (e instanceof CygnusPersistenceError) {
                LOGGER.error(e.getMessage());

                // check the event HEADER_TTL
                int ttl;
                String ttlStr = event.getHeaders().get(Constants.HEADER_TTL);
                
                try {
                    ttl = Integer.parseInt(ttlStr);
                } catch (NumberFormatException nfe) {
                    ttl = 0;
                    LOGGER.error("Invalid TTL value (id=" + event.hashCode() + ", ttl=" + ttlStr
                          +  ", " + nfe.getMessage() + ")");
                } // try catch // try catch
                
                if (ttl == -1) {
                    txn.rollback();
                    ((CygnusChannel) ch).rollback();
                    status = Status.BACKOFF;
                    LOGGER.info("An event was put again in the channel (id=" + event.hashCode() + ", ttl=-1)");
                } else if (ttl == 0) {
                    LOGGER.warn("The event TTL has expired, it is no more re-injected in the channel (id="
                            + event.hashCode() + ", ttl=0)");
                    txn.commit();
                    status = Status.READY;
                } else {
                    ttl--;
                    String newTTLStr = Integer.toString(ttl);
                    event.getHeaders().put(Constants.HEADER_TTL, newTTLStr);
                    txn.rollback();
                    status = Status.BACKOFF;
                    LOGGER.info("An event was put again in the channel (id=" + event.hashCode() + ", ttl=" + ttl
                            + ")");
                } // if else
            } else {
                if (e instanceof CygnusRuntimeError) {
                    LOGGER.error(e.getMessage());
                } else if (e instanceof CygnusBadConfiguration) {
                    LOGGER.warn(e.getMessage());
                } else if (e instanceof CygnusBadContextData) {
                    LOGGER.warn(e.getMessage());
                } else {
                    LOGGER.warn(e.getMessage());
                } // if else if

                txn.commit();
                status = Status.READY;
            } // if else
        } finally {
            // close the transaction
            txn.close();
            LOGGER.info("Finishing transaction (" + MDC.get(Constants.HEADER_TRANSACTION_ID) + ")");
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

        if (eventHeaders.get(Constants.HEADER_CONTENT_TYPE).contains("application/json")) {
            Gson gson = new Gson();

            try {
                notification = gson.fromJson(eventData, NotifyContextRequest.class);
            } catch (Exception e) {
                throw new CygnusBadContextData(e.getMessage());
            } // try catch
        } else if (eventHeaders.get(Constants.HEADER_CONTENT_TYPE).contains("application/xml")) {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

            try {
                SAXParser saxParser = saxParserFactory.newSAXParser();
                NotifyContextRequestSAXHandler handler = new NotifyContextRequestSAXHandler();
                saxParser.parse(new InputSource(new StringReader(eventData)), handler);
                notification = handler.getNotifyContextRequest();
            } catch (ParserConfigurationException e) {
                throw new CygnusBadContextData(e.getMessage());
            } catch (SAXException e) {
                throw new CygnusBadContextData(e.getMessage());
            } catch (IOException e) {
                throw new CygnusBadContextData(e.getMessage());
            } // try catch
        } else {
            // this point should never be reached since the content type has been checked when receiving the
            // notification
            throw new Exception("Unrecognized content type (not Json nor XML)");
        } // if else if

        persist(eventHeaders, notification);
    } // persist

    /**
     * This is the method the classes extending this class must implement when dealing with persistence.
     * @param eventHeaders Event headers
     * @param notification Notification object (already parsed) regarding an event body
     * @throws Exception
     */
    abstract void persist(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception;

} // OrionSink
