/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
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

import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author cardiealb

    Abstract class containing the common code to all the sinks persisting data comming from Twitter.

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
public abstract class TwitterSink extends CygnusSink implements Configurable {

    // logger
    private static final CygnusLogger LOGGER = new CygnusLogger(TwitterSink.class);
    // general parameters for all the sinks
    protected int batchSize;
    protected int batchTimeout;
    protected int batchTTL;
    protected boolean enableLowercase;
    protected boolean invalidConfiguration;
    // accumulator utility
    private final Accumulator accumulator;
    // rollback queues
    private final ArrayList<Accumulator> rollbackedAccumulations;

    /**
     * Constructor.
     */
    public TwitterSink() {
        super();

        // configuration is supposed to be valid
        invalidConfiguration = false;

        // create the accumulator utility
        accumulator = new Accumulator();

        // crete the rollbacking queue
        rollbackedAccumulations = new ArrayList<Accumulator>();
    } // TwitterSink

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
     * Gets true if the configuration is invalid, false otherwise. It is protected due to it is only
     * required for testing purposes.
     * @return
     */
    /**
     * Gets if lower case is enabled.
     * @return True is lower case is enabled, false otherwise.
     */
    protected boolean getEnableLowerCase() {
        return enableLowercase;
    } // getEnableLowerCase

    protected boolean getInvalidConfiguration() {
        return invalidConfiguration;
    } // getInvalidConfiguration

    @Override
    public void configure(Context context) {

        String enableLowercaseStr = context.getString("enable_lowercase", "false");

        if (enableLowercaseStr.equals("true") || enableLowercaseStr.equals("false")) {
            enableLowercase = Boolean.valueOf(enableLowercaseStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_lowercase="
                    + enableLowercaseStr + ")");
        } else {
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
            numPersistedEvents += rollbackedAccumulation.getBatch().size();
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

            // parse the event and accumulate it
            try {
                LOGGER.debug("Event got from the channel (id=" + event.hashCode() + ", headers="
                        + event.getHeaders().toString() + ", bodyLength=" + event.getBody().length + ")");
                String eventData = parseEventBody(event);
                accumulator.accumulate(eventData);
                numProcessedEvents++;
            } catch (Exception e) {
                LOGGER.debug("There was some problem when parsing event data element. Details="
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
            numPersistedEvents += accumulator.getBatch().size();
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
    private String parseEventBody(Event event) throws Exception {
        String eventData = new String(event.getBody());

        return eventData;
    } // parseEventBody

    /**
     * Utility class for batch-like event accumulation purposes.
     */
    private class Accumulator implements Cloneable {

        // accumulated events
        private ArrayList<TwitterEvent> batch;
        private long accStartDate;
        private int accIndex;
        private String accTransactionIds;
        private int ttl;

        /**
         * Constructor.
         */
        public Accumulator() {
            batch = new ArrayList<TwitterEvent>();
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

        public ArrayList<TwitterEvent> getBatch() {
            return batch;
        } // getBatch

        public String getAccTransactionIds() {
            return accTransactionIds;
        } // getAccTransactionIds

        /**
         * Accumulates an event given it event data.

         * @param eventData
         */
        public void accumulate(String eventData) {


            TwitterEvent cygnusEvent = new TwitterEvent(eventData);
            batch.add(cygnusEvent);

        } // accumulate

        /**
         * Initialize the batch.
         * @param startDateMs
         */
        public void initialize(long startDateMs) {
            // what happens if Cygnus falls down while accumulating the batch?
            // TBD: https://github.com/telefonicaid/fiware-cygnus/issues/562
            batch = new ArrayList<TwitterEvent>();
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
    abstract void persistBatch(ArrayList<TwitterEvent> batch) throws Exception;

} // TwitterSink
