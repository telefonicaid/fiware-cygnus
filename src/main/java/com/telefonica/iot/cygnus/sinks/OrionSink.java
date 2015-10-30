/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.sinks;

import com.google.gson.Gson;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.flume.Channel;
import org.apache.flume.Context;
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
 
 Abstract class containing the common code to all the sinks persisting data comming from Orion Context Broker.
 
 The common attributes are:
  - there is no common attributes
 The common methods are:
  - void stop()
  - Status process() throws EventDeliveryException
  - void persistOne(Event event) throws Exception
 The non common parts, and therefore those that are sink dependant and must be implemented are:
  - void configure(Context context)
  - void start()
  - void persistOne(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception
 */
public abstract class OrionSink extends AbstractSink implements Configurable {

    // logger
    private static final CygnusLogger LOGGER = new CygnusLogger(OrionSink.class);
    // general parameters for all the sinks
    protected boolean enableGrouping;
    protected int batchSize;
    protected int batchTimeout;
    // accumulator utility
    private final Accumulator accumulator;
    
    /**
     * Constructor.
     */
    public OrionSink() {
        super();
        accumulator = new Accumulator();
        accumulator.initializeBatching(new Date().getTime());
    } // OrionSink
    
    /**
     * Gets if the grouping feature is enabled.
     * @return True if the grouping feature is enabled, false otherwise.
     */
    public boolean getEnableGrouping() {
        return enableGrouping;
    } // getEnableGrouping
    
    @Override
    public void configure(Context context) {
        enableGrouping = context.getBoolean("enable_grouping", false);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_grouping="
                + (enableGrouping ? "true" : "false") + ")");
        batchSize = context.getInteger("batch_size", 1);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (batch_size="
                + batchSize + ")");
        batchTimeout = context.getInteger("batch_timeout", 30);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (batch_timeout="
                + batchTimeout + ")");
    } // configure

    @Override
    public void stop() {
        super.stop();
    } // stop

    @Override
    public Status process() throws EventDeliveryException {
        // get the channel
        Channel ch = null;
        
        try {
            ch = getChannel();
        } catch (Exception e) {
            LOGGER.error("Channel error (The channel could not be got. Details=" + e.getMessage() + ")");
            throw new EventDeliveryException(e);
        } // try catch

        // start a Flume transaction (it is not the same than a Cygnus transaction!)
        Transaction txn = null;
        
        try {
            txn = ch.getTransaction();
            txn.begin();
        } catch (Exception e) {
            LOGGER.error("Channel error (The Flume transaction could not be started. Details=" + e.getMessage() + ")");
            throw new EventDeliveryException(e);
        } // try catch
        
        // TBD: remove the special case of batchSize=1 when all the sinks have migrated to persistBatch
        if (batchSize == 1) {
            // get an event
            Event event = null;

            try {
                event = ch.take();
            } catch (Exception e) {
                LOGGER.error("Channel error (The event could not be got. Details=" + e.getMessage() + ")");
                throw new EventDeliveryException(e);
            } // try catch

            // check if the event is null
            if (event == null) {
                txn.commit();
                txn.close();
                return Status.BACKOFF; // slow down the sink since no defaultBatch are available
            } // if

            // set the transactionId in MDC
            try {
                MDC.put(Constants.HEADER_TRANSACTION_ID, event.getHeaders().get(Constants.HEADER_TRANSACTION_ID));
            } catch (Exception e) {
                LOGGER.error("Runtime error (" + e.getMessage() + ")");
            } // catch // catch

            LOGGER.debug("Event got from the channel (id=" + event.hashCode() + ", headers="
                    + event.getHeaders().toString() + ", bodyLength=" + event.getBody().length + ")");

            // parse the event
            NotifyContextRequest notification = null;
            
            try {
                notification = parseEventBody(event);
            } catch (Exception e) {
                LOGGER.debug("There was some problem when parsing the notifed context element. Details="
                        + e.getMessage());
            } // try catch
            
            try {
                persistOne(event.getHeaders(), notification);
                LOGGER.info("Finishing transaction (" + MDC.get(Constants.HEADER_TRANSACTION_ID) + ")");
                txn.commit();
                txn.close();
                return Status.READY;
            } catch (Exception e) {
                LOGGER.debug(Arrays.toString(e.getStackTrace()));
            
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
                        txn.commit();
                        txn.close();
                        LOGGER.info("An event was put again in the channel (id=" + event.hashCode() + ", ttl=-1)");
                        return Status.BACKOFF;
                    } else if (ttl == 0) {
                        LOGGER.warn("The event TTL has expired, it is no more re-injected in the channel (id="
                                + event.hashCode() + ", ttl=0)");
                        txn.commit();
                        txn.close();
                        return Status.READY;
                    } else {
                        ttl--;
                        String newTTLStr = Integer.toString(ttl);
                        event.getHeaders().put(Constants.HEADER_TTL, newTTLStr);
                        txn.rollback();
                        LOGGER.info("An event was put again in the channel (id=" + event.hashCode() + ", ttl=" + ttl
                                + ")");
                        return Status.BACKOFF;
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
                    
                    LOGGER.info("Finishing transaction (" + MDC.get(Constants.HEADER_TRANSACTION_ID) + ")");
                    txn.commit();
                    txn.close();
                    return Status.READY;
                } // if else
            } // try catch
        } else {
            // get and process as many events as the batch size
            int currentIndex;
            
            for (currentIndex = accumulator.getAccIndex(); currentIndex < batchSize; currentIndex++) {
                // check if the batch accumulation timeout has been reached
                if ((new Date().getTime() - accumulator.getAccStartDate()) > (batchTimeout * 1000)) {
                    LOGGER.info("Batch accumulation time reached, the batch will be processed as it is");
                    break;
                } // if

                // get an event
                Event event = null;

                try {
                    event = ch.take();
                } catch (Exception e) {
                    LOGGER.error("Channel error (The event could not be got. Details=" + e.getMessage() + ")");
                    throw new EventDeliveryException(e);
                } // try catch

                // check if the event is null
                if (event == null) {
                    accumulator.setAccIndex(currentIndex);
                    txn.commit();
                    txn.close();
                    return Status.BACKOFF; // slow down the sink since no events are available
                } // if

                // set the transactionId in MDC
                try {
                    MDC.put(Constants.HEADER_TRANSACTION_ID, event.getHeaders().get(Constants.HEADER_TRANSACTION_ID));
                } catch (Exception e) {
                    LOGGER.error("Runtime error (" + e.getMessage() + ")");
                } // catch // catch

                // parse the event and accumulate it
                try {
                    LOGGER.debug("Event got from the channel (id=" + event.hashCode() + ", headers="
                            + event.getHeaders().toString() + ", bodyLength=" + event.getBody().length + ")");
                    NotifyContextRequest notification = parseEventBody(event);
                    accumulator.accumulate(event.getHeaders(), notification);
                } catch (Exception e) {
                    LOGGER.debug("There was some problem when parsing the notifed context element. Details="
                            + e.getMessage());
                } // try catch
            } // for
            
            // save the current index for next run of the process() method
            accumulator.setAccIndex(currentIndex);
            
            try {
                if (accumulator.getAccIndex() != 0) {
                    persistBatch(accumulator.getDefaultBatch(), accumulator.getGroupedBatch());
                } // if
                
                LOGGER.info("Finishing transaction (" + accumulator.getAccTransactionIds() + ")");
                accumulator.initializeBatching(new Date().getTime());
                txn.commit();
                txn.close();
                return Status.READY;
            } catch (Exception e) {
                LOGGER.debug(Arrays.toString(e.getStackTrace()));

                // rollback only if the exception is about a persistence error
                if (e instanceof CygnusPersistenceError) {
                    LOGGER.error(e.getMessage());
                    doBatchRollback();
                    LOGGER.info("Finishing transaction (" + accumulator.getAccTransactionIds() + ")");
                    accumulator.initializeBatching(new Date().getTime());
                    txn.commit();
                    txn.close();
                    return Status.BACKOFF; // slow down the sink since there are problems with the persistence backend
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
                    
                    LOGGER.info("Finishing transaction (" + accumulator.getAccTransactionIds() + ")");
                    accumulator.initializeBatching(new Date().getTime());
                    txn.commit();
                    txn.close();
                    return Status.READY;
                } // if else
            } // try catch
        } // if else
    } // process

    /**
     * Given an event, it is parsed before it is persisted. Depending on the content type, it is appropriately
     * parsed (Json or XML) in order to obtain a NotifyContextRequest instance.
     * 
     * @param event A Flume event containing the data to be persistedDestinations and certain metadata (headers).
     * @throws Exception
     */
    private NotifyContextRequest parseEventBody(Event event) throws Exception {
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
        
        return notification;
    } // parseEventBody
    
    // TBD: this class must be private once all the sinks migrate to persistsBatch
    /**
     * Utility class for batch-like event accumulation purposes.
     */
    protected class Accumulator {
        
        // eventsPerDestination of events with default service path and destination
        private Batch defaultBatch;
        // eventsPerDestination of events with grouped service path and destination
        private Batch groupedBatch;
        // list of Flume events taken from the channel, it is necessary for an hipothetical massive decreasing of the
        // ttl when rollbacking
        private ArrayList<Event> flumeEvents;
        private long accStartDate;
        private int accIndex;
        private String accTransactionIds;
        
        public long getAccStartDate() {
            return accStartDate;
        } // getAccStartDate
        
        public int getAccIndex() {
            return accIndex;
        } // getAccIndex
        
        public void setAccIndex(int accIndex) {
            this.accIndex = accIndex;
        } // setAccIndex
        
        public Batch getDefaultBatch() {
            return defaultBatch;
        } // getDefaultBatch
        
        public Batch getGroupedBatch() {
            return groupedBatch;
        } // getGroupedBatch
        
        public String getAccTransactionIds() {
            return accTransactionIds;
        } // getAccTransactionIds
        
        public void accumulate(Map<String, String> headers, NotifyContextRequest notification) {
            Long recvTimeTs = new Long(headers.get(Constants.HEADER_TIMESTAMP));
            String service = headers.get(Constants.HEADER_NOTIFIED_SERVICE);
            String[] defaultServicePaths = headers.get(Constants.HEADER_DEFAULT_SERVICE_PATHS).split(",");
            String[] defaultDestinations = headers.get(Constants.HEADER_DEFAULT_DESTINATIONS).split(",");
            String transactionId = headers.get(Constants.HEADER_TRANSACTION_ID);
            
            if (accTransactionIds.isEmpty()) {
                accTransactionIds = transactionId;
            } else {
                accTransactionIds += "," + transactionId;
            } // if else

            for (int i = 0; i < defaultDestinations.length; i++) {
                String destination = defaultDestinations[i];
                ArrayList<CygnusEvent> list = defaultBatch.getEvents(destination);

                if (list == null) {
                    list = new ArrayList<CygnusEvent>();
                    defaultBatch.addEvents(destination, list);
                } // if

                CygnusEvent cygnusEvent = new CygnusEvent(
                        recvTimeTs, service, defaultServicePaths[i], destination,
                        notification.getContextResponses().get(i).getContextElement());
                list.add(cygnusEvent);
            } // for

            String[] groupedServicePaths = headers.get(Constants.HEADER_GROUPED_SERVICE_PATHS).split(",");
            String[] groupedDestinations = headers.get(Constants.HEADER_GROUPED_DESTINATIONS).split(",");

            for (int i = 0; i < groupedDestinations.length; i++) {
                String destination = defaultDestinations[i];
                ArrayList<CygnusEvent> list = groupedBatch.getEvents(destination);

                if (list == null) {
                    list = new ArrayList<CygnusEvent>();
                    groupedBatch.addEvents(destination, list);
                } // if

                CygnusEvent cygnusEvent = new CygnusEvent(
                        recvTimeTs, service, groupedServicePaths[i], destination,
                        notification.getContextResponses().get(i).getContextElement());
                list.add(cygnusEvent);
            } // for
        } // accumulate

        public void initializeBatching(long startDateMs) {
            // what happens if Cygnus falls down while accumulating the batch?
            // TBD: https://github.com/telefonicaid/fiware-cygnus/issues/562
            defaultBatch = new Batch();
            groupedBatch = new Batch();
            accStartDate = startDateMs;
            accIndex = 0;
            accTransactionIds = "";
        } // initializeBatching
        
    } // Accumulator
    
    private void doBatchRollback() {
        // TBD: https://github.com/telefonicaid/fiware-cygnus/issues/563
    } // doBatchRollback

    // TDB: to be removed once all the sinks migrate to persistBatch method
    /**
     * This is the method the classes extending this class must implement when dealing with a single event to be
     * persisted.
     * @param eventHeaders Event headers
     * @param notification Notification object (already parsed) regarding an event body
     * @throws Exception
     */
    abstract void persistOne(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception;
    
    /**
     * This is the method the classes extending this class must implement when dealing with a batch of events to be
     * persisted.
     * @param defaultEvents
     * @param groupedEvents
     * @throws Exception
     */
    abstract void persistBatch(Batch defaultEvents, Batch groupedEvents) throws Exception;
    
} // OrionSink
