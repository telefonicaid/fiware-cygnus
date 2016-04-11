/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
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
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.util.Map;
import com.telefonica.iot.cygnus.utils.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Sink.Status;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.apache.log4j.MDC;

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

    /**
     * Available data models for all the sinks.
     */
    public enum DataModel { DMBYSERVICE, DMBYSERVICEPATH, DMBYENTITY, DMBYATTRIBUTE }

    // logger
    private static final CygnusLogger LOGGER = new CygnusLogger(OrionSink.class);
    // general parameters for all the sinks
    protected DataModel dataModel;
    protected boolean enableGrouping;
    protected int batchSize;
    protected int batchTimeout;
    protected int batchTTL;
    protected boolean enableLowercase;
    protected boolean invalidConfiguration;
    // accumulator utility
    private final Accumulator accumulator;
    // rollback queues
    private final ArrayList<Accumulator> rollbackedAccumulations;
    // statistics
    private final long setupTime;
    private long numProcessedEvents;
    private long numPersistedEvents;

    /**
     * Constructor.
     */
    public OrionSink() {
        super();

        // configuration is supposed to be valid
        invalidConfiguration = false;

        // create the accumulator utility
        accumulator = new Accumulator();

        // crete the rollbacking queue
        rollbackedAccumulations = new ArrayList<Accumulator>();

        // initialize the statistics
        setupTime = new Date().getTime();
        numProcessedEvents = 0;
        numPersistedEvents = 0;
    } // OrionSink
    
    /**
     * Gets the batch size.
     * @return The batch size.
     */
    protected int getBatchSize() {
        return batchSize;
    } // getBatchSize
    
    /**
     * Gets the batch timeout.
     * @return The batch timeout.
     */
    protected int getBatchTimeout() {
        return batchTimeout;
    } // getBatchTimeout
    
    /**
     * Gets the batch TTL.
     * @return The batch TTL.
     */
    protected int getBatchTTL() {
        return batchTTL;
    } // getBatchTTL
    
    /**
     * Gets the data model.
     * @return The data model
     */
    protected DataModel getDataModel() {
        return dataModel;
    } // getDataModel

    /**
     * Gets if the grouping feature is enabled.
     * @return True if the grouping feature is enabled, false otherwise.
     */
    protected boolean getEnableGrouping() {
        return enableGrouping;
    } // getEnableGrouping

    /**
     * Gets if lower case is enabled.
     * @return True is lower case is enabled, false otherwise.
     */
    protected boolean getEnableLowerCase() {
        return enableLowercase;
    } // getEnableLowerCase

    /**
     * Gets the setup time.
     * @return The setup time (in miliseconds)
     */
    public long getSetupTime() {
        return setupTime;
    } // getSetupTime

    /**
     * Gets the number of processed events.
     * @return The number of processed events
     */
    public long getNumProcessedEvents() {
        return numProcessedEvents;
    } // getNumProcessedEvents

    /**
     * Gets the number of persisted events.
     * @return The number of persisted events.
     */
    public long getNumPersistedEvents() {
        return numPersistedEvents;
    } // getNumPersistedEvents
    
    /**
     * Sets the number of processed events.
     * @param n The number of processed events to be set
     */
    public void setNumProcessedEvents(long n) {
        numProcessedEvents = n;
    } // setNumProcessedEvents
    
    /**
     * Sets the number of persisted events.
     * @param n The number of persisted events to be set
     */
    public void setNumPersistedEvents(long n) {
        numPersistedEvents = n;
    } // setNumPersistedEvents
    
    /**
     * Gets true if the configuration is invalid, false otherwise. It is protected due to it is only
     * required for testing purposes.
     * @return
     */
    protected boolean getInvalidConfiguration() {
        return invalidConfiguration;
    } // getInvalidConfiguration

    @Override
    public void configure(Context context) {
        String dataModelStr = context.getString("data_model", "dm-by-entity");

        try {
            dataModel = DataModel.valueOf(dataModelStr.replaceAll("-", "").toUpperCase());
            LOGGER.debug("[" + this.getName() + "] Reading configuration (data_model="
                    + dataModelStr + ")");
        } catch (Exception e) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (data_model="
                    + dataModelStr + ")");
        } // catch

        String enableGroupingStr = context.getString("enable_grouping", "false");
        
        if (enableGroupingStr.equals("true") || enableGroupingStr.equals("false")) {
            enableGrouping = Boolean.valueOf(enableGroupingStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_grouping="
                + enableGroupingStr + ")");
        }  else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (enable_grouping="
                + enableGroupingStr + ") -- Must be 'true' or 'false'");
        }  // if else
        
        String enableLowercaseStr = context.getString("enable_lowercase", "false");
        
        if (enableLowercaseStr.equals("true") || enableLowercaseStr.equals("false")) {
            enableLowercase = Boolean.valueOf(enableLowercaseStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_lowercase="
                + enableLowercaseStr + ")");
        }  else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (enable_lowercase="
                + enableLowercaseStr + ") -- Must be 'true' or 'false'");
        }  // if else

        batchSize = context.getInteger("batch_size", 1);

        if (batchSize <= 0) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (batch_size="
                    + batchSize + ") -- Must be greater than 0");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (batch_size="
                    + batchSize + ")");
        } // if else

        batchTimeout = context.getInteger("batch_timeout", 30);

        if (batchTimeout <= 0) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (batch_timeout="
                    + batchTimeout + ") -- Must be greater than 0");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (batch_timeout="
                    + batchTimeout + ")");
        } // if

        batchTTL = context.getInteger("batch_ttl", 10);
        
        if (batchTTL < -1) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (batch_ttl="
                    + batchTTL + ") -- Must be greater than -2");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (batch_ttl="
                    + batchTTL + ")");
        } // if else
    } // configure

    @Override
    public void start() {
        super.start();

        if (invalidConfiguration) {
            LOGGER.info("[" + this.getName() + "] Startup completed. Nevertheless, there are errors "
                    + "in the configuration, thus this sink will not run the expected logic");
        } else {
            // the accumulator must be initialized once read the configuration
            accumulator.initialize(new Date().getTime());
            LOGGER.info("[" + this.getName() + "] Startup completed");
        } // if else
    } // start

    @Override
    public void stop() {
        super.stop();
    } // stop

    @Override
    public Status process() throws EventDeliveryException {
        if (invalidConfiguration) {
            return Status.BACKOFF;
        } else if (rollbackedAccumulations.isEmpty()) {
            return processNewBatches();
        } else {
            return processRollbackedBatches();
        } // if else
    } // process

    private Status processRollbackedBatches() throws EventDeliveryException {
        Accumulator rollbackedAccumulation;

        // get a rollbacked accumulation
        if (rollbackedAccumulations.isEmpty()) {
            return Status.BACKOFF;
        } else {
            rollbackedAccumulation = rollbackedAccumulations.get(0);
        } // if else

        // try persisting the rollbacked accumulation
        try {
            persistBatch(rollbackedAccumulation.getBatch());
            LOGGER.info("Finishing internal transaction (" + rollbackedAccumulation.getAccTransactionIds() + ")");
            rollbackedAccumulations.remove(0);
            numPersistedEvents += rollbackedAccumulation.getBatch().getNumEvents();
            return Status.READY;
        } catch (Exception e) {
            LOGGER.debug(Arrays.toString(e.getStackTrace()));

            // rollback only if the exception is about a persistence error
            if (e instanceof CygnusPersistenceError) {
                LOGGER.error(e.getMessage());
                
                if (rollbackedAccumulation.ttl == -1) {
                    LOGGER.info("Rollbacking again (" + rollbackedAccumulation.getAccTransactionIds() + "), "
                            + "infinite batch TTL");
                } else if (rollbackedAccumulation.ttl > 0) {
                    rollbackedAccumulation.ttl--;
                    LOGGER.info("Rollbacking again (" + rollbackedAccumulation.getAccTransactionIds() + "), "
                            + "batch TTL=" + rollbackedAccumulation.ttl);
                } else {
                    rollbackedAccumulations.remove(0);
                    LOGGER.info("TTL exhausted, finishing internal transaction ("
                            + rollbackedAccumulation.getAccTransactionIds() + ")");
                } // if else
                
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

                return Status.READY;
            } // if else
        } // try catch
    } // processRollbackedBatches

    private Status processNewBatches() throws EventDeliveryException {
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

            // set the correlation ID, transaction ID, service and service path in MDC
            try {
                MDC.put(Constants.LOG4J_CORR,
                        event.getHeaders().get(Constants.HEADER_CORRELATOR_ID));
                MDC.put(Constants.LOG4J_TRANS,
                        event.getHeaders().get(Constants.FLUME_HEADER_TRANSACTION_ID));
                MDC.put(Constants.LOG4J_SVC,
                        event.getHeaders().get(Constants.HEADER_FIWARE_SERVICE));
                MDC.put(Constants.LOG4J_SUBSVC,
                        event.getHeaders().get(Constants.HEADER_FIWARE_SERVICE_PATH));
            } catch (Exception e) {
                LOGGER.error("Runtime error (" + e.getMessage() + ")");
            } // catch

            // parse the event and accumulate it
            try {
                LOGGER.debug("Event got from the channel (id=" + event.hashCode() + ", headers="
                        + event.getHeaders().toString() + ", bodyLength=" + event.getBody().length + ")");
                NotifyContextRequest notification = parseEventBody(event);
                accumulator.accumulate(event.getHeaders(), notification);
                numProcessedEvents++;
            } catch (Exception e) {
                LOGGER.debug("There was some problem when parsing the notifed context element. Details="
                        + e.getMessage());
            } // try catch
        } // for

        // save the current index for next run of the process() method
        accumulator.setAccIndex(currentIndex);

        try {
            if (accumulator.getAccIndex() != 0) {
                LOGGER.info("Batch completed, persisting it");
                persistBatch(accumulator.getBatch());
            } // if

            LOGGER.info("Finishing internal transaction (" + accumulator.getAccTransactionIds() + ")");
            numPersistedEvents += accumulator.getBatch().getNumEvents();
            accumulator.initialize(new Date().getTime());
            txn.commit();
            txn.close();
            return Status.READY;
        } catch (Exception e) {
            LOGGER.debug(Arrays.toString(e.getStackTrace()));

            // rollback only if the exception is about a persistence error
            if (e instanceof CygnusPersistenceError) {
                LOGGER.error(e.getMessage());
                
                if (accumulator.ttl == -1) {
                    rollbackedAccumulations.add(accumulator.clone());
                    LOGGER.info("Rollbacking again (" + accumulator.getAccTransactionIds() + "), "
                            + "infinite batch TTL");
                } else if (accumulator.ttl > 0) {
                    accumulator.ttl--;
                    rollbackedAccumulations.add(accumulator.clone());
                    LOGGER.info("Rollbacking again (" + accumulator.getAccTransactionIds() + "), "
                            + "batch TTL=" + accumulator.ttl);
                } else {
                    LOGGER.info("TTL exhausted, finishing internal transaction ("
                            + accumulator.getAccTransactionIds() + ")");
                } // if else
                
                accumulator.initialize(new Date().getTime());
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

                accumulator.initialize(new Date().getTime());
                txn.commit();
                txn.close();
                return Status.READY;
            } // if else
        } // try catch
    } // processNewBatches

    /**
     * Given an event, it is parsed before it is persisted. Depending on the content type, it is appropriately
     * parsed (Json or XML) in order to obtain a NotifyContextRequest instance.
     *
     * @param event A Flume event containing the data to be persistedDestinations and certain metadata (headers).
     * @throws Exception
     */
    private NotifyContextRequest parseEventBody(Event event) throws Exception {
        String eventData = new String(event.getBody());

        // parse the event body as a Json document
        NotifyContextRequest notification = null;
        Gson gson = new Gson();

        try {
            notification = gson.fromJson(eventData, NotifyContextRequest.class);
        } catch (Exception e) {
            throw new CygnusBadContextData(e.getMessage());
        } // try catch

        return notification;
    } // parseEventBody

    /**
     * Utility class for batch-like event accumulation purposes.
     */
    private class Accumulator implements Cloneable {

        // accumulated events
        private Batch batch;
        private long accStartDate;
        private int accIndex;
        private String accTransactionIds;
        private int ttl;

        /**
         * Constructor.
         */
        public Accumulator() {
            batch = new Batch();
            accStartDate = 0;
            accIndex = 0;
            accTransactionIds = null;
            ttl = batchTTL;
        } // Accumulator

        public long getAccStartDate() {
            return accStartDate;
        } // getAccStartDate

        public int getAccIndex() {
            return accIndex;
        } // getAccIndex

        public void setAccIndex(int accIndex) {
            this.accIndex = accIndex;
        } // setAccIndex

        public Batch getBatch() {
            return batch;
        } // getBatch

        public String getAccTransactionIds() {
            return accTransactionIds;
        } // getAccTransactionIds

        /**
         * Accumulates an event given its headers and context data.
         * @param headers
         * @param notification
         */
        public void accumulate(Map<String, String> headers, NotifyContextRequest notification) {
            String transactionId = headers.get(Constants.HEADER_CORRELATOR_ID);

            if (accTransactionIds.isEmpty()) {
                accTransactionIds = transactionId;
            } else {
                accTransactionIds += "," + transactionId;
            } // if else

            switch (dataModel) {
                case DMBYSERVICE:
                    accumulateByService(headers, notification);
                    break;
                case DMBYSERVICEPATH:
                    accumulateByServicePath(headers, notification);
                    break;
                case DMBYENTITY:
                    accumulateByEntity(headers, notification);
                    break;
                case DMBYATTRIBUTE:
                    accumulateByAttribute(headers, notification);
                    break;
                default:
                    LOGGER.error("Unknown data model. Details=" + dataModel.toString());
            } // switch
        } // accumulate

        private void accumulateByService(Map<String, String> headers, NotifyContextRequest notification) {
            Long recvTimeTs = new Long(headers.get(Constants.FLUME_HEADER_TIMESTAMP));
            String service = headers.get(Constants.HEADER_FIWARE_SERVICE);
            String destination = service;

            if (!enableGrouping) {
                String[] notifiedServicePaths = headers.get(Constants.HEADER_FIWARE_SERVICE_PATH).split(",");

                for (int i = 0; i < notifiedServicePaths.length; i++) {
                    CygnusEvent cygnusEvent = new CygnusEvent(
                            recvTimeTs, destination, notifiedServicePaths[i], null, null,
                            notification.getContextResponses().get(i).getContextElement());
                    batch.addEvent(destination, cygnusEvent);
                } // for
            } else {
                String[] groupedServicePaths = headers.get(Constants.FLUME_HEADER_GROUPED_SERVICE_PATHS).split(",");

                for (int i = 0; i < groupedServicePaths.length; i++) {
                    CygnusEvent cygnusEvent = new CygnusEvent(
                            recvTimeTs, destination, groupedServicePaths[i], null, null,
                            notification.getContextResponses().get(i).getContextElement());
                    batch.addEvent(destination, cygnusEvent);
                } // for
            } // if else
        } // accumulateByService

        private void accumulateByServicePath(Map<String, String> headers, NotifyContextRequest notification) {
            Long recvTimeTs = new Long(headers.get(Constants.FLUME_HEADER_TIMESTAMP));
            String service = headers.get(Constants.HEADER_FIWARE_SERVICE);

            if (!enableGrouping) {
                String[] notifiedServicePaths = headers.get(Constants.HEADER_FIWARE_SERVICE_PATH).split(",");

                for (int i = 0; i < notifiedServicePaths.length; i++) {
                    String destination = service + "_" + notifiedServicePaths[i];
                    CygnusEvent cygnusEvent = new CygnusEvent(
                            recvTimeTs, service, notifiedServicePaths[i], null, null,
                            notification.getContextResponses().get(i).getContextElement());
                    batch.addEvent(destination, cygnusEvent);
                } // for
            } else {
                String[] groupedServicePaths = headers.get(Constants.FLUME_HEADER_GROUPED_SERVICE_PATHS).split(",");

                for (int i = 0; i < groupedServicePaths.length; i++) {
                    String destination = service + "_" + groupedServicePaths[i];
                    CygnusEvent cygnusEvent = new CygnusEvent(
                            recvTimeTs, service, groupedServicePaths[i], null, null,
                            notification.getContextResponses().get(i).getContextElement());
                    batch.addEvent(destination, cygnusEvent);
                } // for
            } // if else
        } // accumulateByServicePath

        private void accumulateByEntity(Map<String, String> headers, NotifyContextRequest notification) {
            Long recvTimeTs = new Long(headers.get(Constants.FLUME_HEADER_TIMESTAMP));
            String service = headers.get(Constants.HEADER_FIWARE_SERVICE);

            if (!enableGrouping) {
                String[] notifiedServicePaths = headers.get(Constants.HEADER_FIWARE_SERVICE_PATH).split(",");
                String[] notifiedEntities = headers.get(Constants.FLUME_HEADER_NOTIFIED_ENTITIES).split(",");

                for (int i = 0; i < notifiedEntities.length; i++) {
                    String destination = service + "_" + notifiedServicePaths[i] + "_" + notifiedEntities[i];
                    CygnusEvent cygnusEvent = new CygnusEvent(
                            recvTimeTs, service, notifiedServicePaths[i], notifiedEntities[i], null,
                            notification.getContextResponses().get(i).getContextElement());
                    batch.addEvent(destination, cygnusEvent);
                } // for
            } else {
                String[] groupedServicePaths = headers.get(Constants.FLUME_HEADER_GROUPED_SERVICE_PATHS).split(",");
                String[] groupedEntities = headers.get(Constants.FLUME_HEADER_GROUPED_ENTITIES).split(",");

                for (int i = 0; i < groupedEntities.length; i++) {
                    String destination = service + "_" + groupedServicePaths[i] + "_" + groupedEntities[i];
                    CygnusEvent cygnusEvent = new CygnusEvent(
                            recvTimeTs, service, groupedServicePaths[i], groupedEntities[i], null,
                            notification.getContextResponses().get(i).getContextElement());
                    batch.addEvent(destination, cygnusEvent);
                } // for
            } // if else
        } // accumulateByEntity

        private void accumulateByAttribute(Map<String, String> headers, NotifyContextRequest notification) {
            Long recvTimeTs = new Long(headers.get(Constants.FLUME_HEADER_TIMESTAMP));
            String service = headers.get(Constants.HEADER_FIWARE_SERVICE);
            ArrayList<ContextElementResponse> contextElementResponses = notification.getContextResponses();

            if (!enableGrouping) {
                String[] notifiedServicePaths = headers.get(Constants.HEADER_FIWARE_SERVICE_PATH).split(",");
                String[] notifiedEntities = headers.get(Constants.FLUME_HEADER_NOTIFIED_ENTITIES).split(",");

                for (int i = 0; i < contextElementResponses.size(); i++) {
                    ContextElement contextElement = contextElementResponses.get(i).getContextElement();
                    ArrayList<ContextAttribute> attrs = contextElement.getAttributes();

                    for (ContextAttribute attr : attrs) {
                        String destination = service + "_" + notifiedServicePaths[i] + "_" + notifiedEntities[i]
                                + "_" + attr.getName();
                        CygnusEvent cygnusEvent = new CygnusEvent(
                                recvTimeTs, service, notifiedServicePaths[i], notifiedEntities[i],
                                attr.getName(), contextElement.filter(destination));
                        batch.addEvent(destination, cygnusEvent);
                    } // for
                } // for
            } else {
                String[] groupedServicePaths = headers.get(Constants.FLUME_HEADER_GROUPED_SERVICE_PATHS).split(",");
                String[] groupedEntities = headers.get(Constants.FLUME_HEADER_GROUPED_ENTITIES).split(",");

                for (int i = 0; i < contextElementResponses.size(); i++) {
                    ContextElement contextElement = contextElementResponses.get(i).getContextElement();
                    ArrayList<ContextAttribute> attrs = contextElement.getAttributes();

                    for (ContextAttribute attr : attrs) {
                        String destination = service + "_" + groupedServicePaths[i] + "_" + groupedEntities[i]
                                + "_" + attr.getName();
                        CygnusEvent cygnusEvent = new CygnusEvent(
                                recvTimeTs, service, groupedServicePaths[i], groupedEntities[i],
                                attr.getName(), contextElement);
                        batch.addEvent(destination, cygnusEvent);
                    } // for
                } // for
            } // if else
        } // accumulateByAttribute

        /**
         * Initialize the batch.
         * @param startDateMs
         */
        public void initialize(long startDateMs) {
            // what happens if Cygnus falls down while accumulating the batch?
            // TBD: https://github.com/telefonicaid/fiware-cygnus/issues/562
            batch = new Batch();
            accStartDate = startDateMs;
            accIndex = 0;
            accTransactionIds = "";
            ttl = batchTTL;
        } // initialize

        @Override
        public Accumulator clone() {
            try {
                Accumulator acc = (Accumulator) super.clone();
                return acc;
            } catch (CloneNotSupportedException ce) {
                return null;
            } // clone
        } // clone

    } // Accumulator

    /**
     * This is the method the classes extending this class must implement when dealing with a batch of events to be
     * persisted.
     * @param batch
     * @throws Exception
     */
    abstract void persistBatch(Batch batch) throws Exception;

} // OrionSink
