/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.Enums.DataModel;
import com.telefonica.iot.cygnus.channels.CygnusChannel;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYATTRIBUTE;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYENTITY;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYENTITYTYPE;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYENTITYDATABASE;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYENTITYTYPEDATABASE;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYENTITYDATABASESCHEMA;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYENTITYTYPEDATABASESCHEMA;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYSERVICE;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYSERVICEPATH;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYFIXEDENTITYTYPE;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYFIXEDENTITYTYPEDATABASE;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYFIXEDENTITYTYPEDATABASESCHEMA;
import java.util.Map;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.management.PatternTypeAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Sink.Status;
import org.apache.flume.Transaction;
import org.apache.flume.ChannelException;
import org.apache.flume.ChannelFullException;
import org.apache.flume.conf.Configurable;
import org.apache.logging.log4j.ThreadContext;

/**
 *
 * @author frb

 Abstract class containing the common code to all the sinks persisting data comming from Orion Context Broker.

 The common attributes are:
  - there is no common attributes
 The common methods are:
  - void stop()
  - Status process() throws EventDeliveryException
  - void persistOne(Event getRecvTimeTs) throws Exception
 The non common parts, and therefore those that are sink dependant and must be implemented are:
  - void configure(Context context)
  - void start()
  - void persistOne(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception
 */
public abstract class NGSISink extends CygnusSink implements Configurable {

    // Logger
    private static final CygnusLogger LOGGER = new CygnusLogger(NGSISink.class);
    // General parameters for all the sinks
    protected DataModel dataModel;
    protected int batchSize;
    protected int batchTimeout;
    protected int batchTTL;
    protected int[] batchRetryIntervals;
    protected boolean enableLowercase;
    protected boolean invalidConfiguration;
    protected boolean enableEncoding;
    protected boolean enableNameMappings;
    private long persistencePolicyMaxRecords;
    private long persistencePolicyExpirationTime;
    private long persistencePolicyCheckingTime;
    // Accumulator utility
    private final Accumulator accumulator;
    // Rollback queues
    private ArrayList<Accumulator> rollbackedAccumulations;
    // Rollback queues
    private int rollbackedAccumulationsIndex;
    // Expiration thread
    private ExpirationTimeChecker expirationTimeChecker;

    // Rollback Metrics
    private int num_rollback_by_channel_exception;
    private int num_rollback_by_exception;

    /**
     * Constructor.
     */
    public NGSISink() {
        super();

        // Configuration is supposed to be valid
        invalidConfiguration = false;

        // Create the accumulator utility
        accumulator = new Accumulator();

        // Create the rollbacking queue
        rollbackedAccumulations = new ArrayList<>();

        num_rollback_by_channel_exception = 0;
        num_rollback_by_exception = 0;

    } // NGSISink
    
    protected String getBatchRetryIntervals() {
        return Arrays.toString(batchRetryIntervals).replaceAll("\\[", "").replaceAll("\\]", "");
    } // getBatchRetryIntervals
    
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
     * Gets if lower case is enabled.
     * @return True is lower case is enabled, false otherwise.
     */
    protected boolean getEnableLowerCase() {
        return enableLowercase;
    } // getEnableLowerCase
    
    /**
     * Gets if the encoding is enabled.
     * @return True is the encoding is enabled, false otherwise.
     */
    protected boolean getEnableEncoding() {
        return enableEncoding;
    } // getEnableEncoding
    
    protected boolean getEnableNameMappings() {
        return enableNameMappings;
    } // getEnableNameMappings
    
    /**
     * Gets true if the configuration is invalid, false otherwise. It is protected due to it is only
     * required for testing purposes.
     * @return
     */
    protected boolean getInvalidConfiguration() {
        return invalidConfiguration;
    } // getInvalidConfiguration
    
    protected ArrayList<Accumulator> getRollbackedAccumulations() {
        return rollbackedAccumulations;
    } // getRollbackedAccumulations
    
    protected void setRollbackedAccumulations(ArrayList<Accumulator> rollbackedAccumulations) {
        this.rollbackedAccumulations = rollbackedAccumulations;
    } // setRollbackedAccumulations
    
    protected long getPersistencePolicyMaxRecords() {
        return persistencePolicyMaxRecords;
    } // getPersistencePolicyMaxRecords
    
    protected long getPersistencePolicyExpirationTime() {
        return persistencePolicyExpirationTime;
    } // getPersistencePolicyExpirationTime
    
    protected long getPersistencePolicyCheckingTime() {
        return persistencePolicyCheckingTime;
    } // getPersistencePolicyCheckingTime

    @Override
    public void configure(Context context) {
        String dataModelStr = context.getString("data_model", "dm-by-entity");

        try {
            dataModel = DataModel.valueOf(dataModelStr.replaceAll("-", "").toUpperCase());
            LOGGER.debug("[" + this.getName() + "] Reading configuration (data_model="
                    + dataModelStr + ")");
        } catch (Exception e) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (data_model="
                    + dataModelStr + ")");
        } // catch
        
        String enableLowercaseStr = context.getString("enable_lowercase", "false");
        
        if (enableLowercaseStr.equals("true") || enableLowercaseStr.equals("false")) {
            enableLowercase = Boolean.valueOf(enableLowercaseStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_lowercase="
                + enableLowercaseStr + ")");
        }  else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (enable_lowercase="
                + enableLowercaseStr + ") -- Must be 'true' or 'false'");
        }  // if else

        batchSize = context.getInteger("batch_size", 1);

        if (batchSize <= 0) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (batch_size="
                    + batchSize + ") -- Must be greater than 0");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (batch_size="
                    + batchSize + ")");
        } // if else

        batchTimeout = context.getInteger("batch_timeout", 30);

        if (batchTimeout <= 0) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (batch_timeout="
                    + batchTimeout + ") -- Must be greater than 0");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (batch_timeout="
                    + batchTimeout + ")");
        } // if

        batchTTL = context.getInteger("batch_ttl", 10);
        
        if (batchTTL < -1) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (batch_ttl="
                    + batchTTL + ") -- Must be greater than -2");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (batch_ttl="
                    + batchTTL + ")");
        } // if else
        
        String enableEncodingStr = context.getString("enable_encoding", "false");
        
        if (enableEncodingStr.equals("true") || enableEncodingStr.equals("false")) {
            enableEncoding = Boolean.valueOf(enableEncodingStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_encoding="
                + enableEncodingStr + ")");
        }  else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (enable_encoding="
                + enableEncodingStr + ") -- Must be 'true' or 'false'");
        }  // if else
        
        String enableNameMappingsStr = context.getString("enable_name_mappings", "false");
        
        if (enableNameMappingsStr.equals("true") || enableNameMappingsStr.equals("false")) {
            enableNameMappings = Boolean.valueOf(enableNameMappingsStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (enable_name_mappings="
                + enableNameMappingsStr + ")");
        }  else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (enable_name_mappings="
                + enableNameMappingsStr + ") -- Must be 'true' or 'false'");
        }  // if else
        
        String batchRetryIntervalsStr = context.getString("batch_retry_intervals", "5000");
        String[] batchRetryIntervalsSplit = batchRetryIntervalsStr.split(",");
        batchRetryIntervals = new int[batchRetryIntervalsSplit.length];
        boolean allOK = true;
        
        for (int i = 0; i < batchRetryIntervalsSplit.length; i++) {
            String batchRetryIntervalStr = batchRetryIntervalsSplit[i];
            int batchRetryInterval = new Integer(batchRetryIntervalStr);
            
            if (batchRetryInterval <= 0) {
                invalidConfiguration = true;
                LOGGER.warn("[" + this.getName() + "] Invalid configuration (batch_retry_intervals="
                        + batchRetryIntervalStr + ") -- Members must be greater than 0");
                allOK = false;
                break;
            } else {
                batchRetryIntervals[i] = batchRetryInterval;
            } // if else
        } // for
        
        if (allOK) {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (batch_retry_intervals="
                    + batchRetryIntervalsStr + ")");
        } // if
        
        persistencePolicyMaxRecords = context.getInteger("persistence_policy.max_records", -1);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (persistence_policy.max_records="
                    + persistencePolicyMaxRecords + ")");
        persistencePolicyExpirationTime = context.getInteger("persistence_policy.expiration_time", -1);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (persistence_policy.expiration_time="
                    + persistencePolicyExpirationTime + ")");
        persistencePolicyCheckingTime = context.getInteger("persistence_policy.checking_time", 3600);
        
        if (persistencePolicyCheckingTime <= 0) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (persistence_policy.checking_time="
                    + persistencePolicyCheckingTime + ") -- Must be greater than 0");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (persistence_policy.checking_time="
                    + persistencePolicyCheckingTime + ")");
        } // if else
    } // configure

    @Override
    public void start() {
        super.start();
        ThreadContext.put(CommonConstants.LOG4J_COMP, CommonConstants.DEF_AGENT_NAME);

        if (invalidConfiguration) {
            LOGGER.info("[" + this.getName() + "] Startup completed. Nevertheless, there are errors "
                    + "in the configuration, thus this sink will not run the expected logic");
        } else {
            // The accumulator must be initialized once read the configuration
            accumulator.initialize(new Date().getTime());
            // Crate and start the expiration time checker thread... this has to be created here in order to have a not
            // null name for the sink (i.e. after configuration)
            expirationTimeChecker = new ExpirationTimeChecker(this.getName());
            expirationTimeChecker.start();
            
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
            processRollbackedBatches();
            return processNewBatches();
        } // if else
    } // process

    private Status processRollbackedBatches() {
        // Get a rollbacked accumulation
        Accumulator rollbackedAccumulation = getRollbackedAccumulationForRetry();

        if (rollbackedAccumulation == null) {
            setMDCToNA();
            return Status.READY; // No rollbacked batch was ready for retry, so we are ready to process new batches
        } // if

        NGSIBatch batch = rollbackedAccumulation.getBatch();
        NGSIBatch rollbackBatch = new NGSIBatch();
        StringBuffer transactionIds = new StringBuffer();
        batch.startIterator();
        while (batch.hasNext()) {
            String destination = batch.getNextDestination();
            ArrayList<NGSIEvent> events = batch.getNextEvents();
            for (NGSIEvent event : events) {
                NGSIBatch batchToPersist = new NGSIBatch();
                batchToPersist.addEvent(destination, event);
                transactionIds.append(event.getHeaders().get(CommonConstants.HEADER_CORRELATOR_ID)).append(", ");
                //}
                // force to persist a batch with just one element
                try {
                    persistBatch(batchToPersist);
                    updateServiceMetrics(batchToPersist, false);
                    if (persistencePolicyMaxRecords > -1) {
                        try {
                            capRecords(batchToPersist, persistencePolicyMaxRecords);
                        } catch (CygnusCappingError e) {
                            LOGGER.error(e.getMessage() + "Stack trace: " + Arrays.toString(e.getStackTrace()) + " Sink: " + this.getName() + " Destination: " + destination );
                        } // try
                    } // if
                    numPersistedEvents += batchToPersist.getNumEvents();
                    LOGGER.info("Finishing internal transaction (" + transactionIds + ")" + " Sink: " + this.getName() + " Destination: " + destination);
                } catch (CygnusBadConfiguration | CygnusBadContextData | CygnusRuntimeError e) {
                    updateServiceMetrics(batchToPersist, true); // do not try again, is just one event
                    LOGGER.error(e.getMessage() + "Stack trace: " + Arrays.toString(e.getStackTrace()) + " Sink: " + this.getName() + " Destination: " + destination);
                } catch (Exception e) {
                    updateServiceMetrics(batchToPersist, true);
                    LOGGER.error(e.getMessage() + "Stack trace: " + Arrays.toString(e.getStackTrace()) + " Sink: " + this.getName() + " Destination: " + destination);
                    rollbackBatch.addEvent(destination, event); // there is just one event
                } finally {
                    batch.setNextPersisted(true);
                }
            } // for (NGSIEvent event : events)
        } // while (batch.hasNext())
        if (rollbackBatch.getNumEvents() > 0) {
            Accumulator rollbackAccumulator = new Accumulator();
            rollbackAccumulator.initialize(rollbackedAccumulation.getAccStartDate());
            rollbackAccumulator.setTTL(rollbackedAccumulation.ttl);
            rollbackAccumulator.setLastRetry(rollbackedAccumulation.lastRetry);
            rollbackBatch.startIterator();
            while (rollbackBatch.hasNext()) {
                for (NGSIEvent event : rollbackBatch.getNextEvents()) {
                    rollbackAccumulator.accumulate(event);
                }
            }
            doRollbackAgain(rollbackAccumulator);
            if (rollbackedAccumulations.size() > rollbackedAccumulationsIndex) {
                rollbackedAccumulations.set(rollbackedAccumulationsIndex, rollbackAccumulator);
            }
            setMDCToNA();
            return Status.BACKOFF;
        }
        rollbackedAccumulations.remove(rollbackedAccumulationsIndex);
        numPersistedEvents += rollbackedAccumulation.getBatch().getNumEvents();
        setMDCToNA();
        return Status.READY;
    } // processRollbackedBatches
    
    /**
     * Gets a rollbacked accumulation for retry.
     * @return A rollbacked accumulation for retry.
     */
    protected Accumulator getRollbackedAccumulationForRetry() {
        Accumulator rollbackedAccumulation = null;

        for (int i = 0 ; i < rollbackedAccumulations.size() ; i++) {

            Accumulator rollbackedAcc = rollbackedAccumulations.get(i);
            rollbackedAccumulation = rollbackedAcc;
            
            // Check the last retry
            int retryIntervalIndex = batchTTL - rollbackedAccumulation.ttl;
            
            if (retryIntervalIndex >= batchRetryIntervals.length) {
                retryIntervalIndex = batchRetryIntervals.length - 1;
            } // if
            
            if (rollbackedAccumulation.getLastRetry() + batchRetryIntervals[retryIntervalIndex]
                    <= new Date().getTime()) {
                rollbackedAccumulationsIndex = i;
                break;
            } // if
            
            rollbackedAccumulation = null;
        } // for
        
        return rollbackedAccumulation;
    } // getRollbackedAccumulationForRetry
    
    /**
     * Rollbacks the accumulation once more.
     * @param rollbackedAccumulation
     */
    protected void doRollbackAgain(Accumulator rollbackedAccumulation) {
        if (rollbackedAccumulation.getTTL() == -1) {
            rollbackedAccumulation.setLastRetry(new Date().getTime());
            LOGGER.info("Rollbacking again (" + rollbackedAccumulation.getAccTransactionIds() + "), "
                    + "infinite batch TTL" + " Sink: " + this.getName());
        } else if (rollbackedAccumulation.getTTL() > 1) {
            rollbackedAccumulation.setLastRetry(new Date().getTime());
            rollbackedAccumulation.setTTL(rollbackedAccumulation.getTTL() - 1);
            LOGGER.info("Rollbacking again (" + rollbackedAccumulation.getAccTransactionIds() + "), "
                        + "this was retry #" + (batchTTL - rollbackedAccumulation.getTTL()) + " Sink: " + this.getName());
        } else {
            rollbackedAccumulations.remove(rollbackedAccumulationsIndex);
            if (!rollbackedAccumulation.getAccTransactionIds().isEmpty()) {
                LOGGER.info("Finishing internal transaction ("
                        + rollbackedAccumulation.getAccTransactionIds() + "), this was retry #" + batchTTL + " Sink: " + this.getName());
            } // if
        } // if else
    } // doRollbackAgain


    private Status processNewBatches() {
        // Get the channel
        Channel ch = getChannel();

        double channelUsage = 0;
        
        // Start a Flume transaction (it is not the same than a Cygnus transaction!)
        Transaction txn = ch.getTransaction();
        try {
            txn.begin();

            CygnusChannel cygnusch = (CygnusChannel) ch;
            channelUsage = cygnusch.getUsage();
            LOGGER.debug("Channel usage: " + channelUsage + "% in sink " + this.getName());
            if (channelUsage > NGSIConstants.HIGH_CHANNEL_PERCENT_USAGE) {
                LOGGER.warn("High Channel usage: " + channelUsage + "% in sink " + this.getName());
            }

            // Get and process as many events as the batch size
            int currentIndex;

            for (currentIndex = accumulator.getAccIndex(); currentIndex < batchSize; currentIndex++) {
                // Check if the batch accumulation timeout has been reached
                if ((new Date().getTime() - accumulator.getAccStartDate()) > (batchTimeout * 1000)) {
                    LOGGER.debug("Batch accumulation time reached, the batch will be processed as it is");
                    break;
                } // if

                // Get an event
                Event event = ch.take();
            
                // Check if the event is null
                if (event == null) {
                    accumulator.setAccIndex(currentIndex);
                    txn.commit();
                    // to-do: this must be uncomment once multiple transaction and correlation IDs are traced in logs
                    //setMDCToNA();
                    return Status.BACKOFF; // Slow down the sink since no events are available
                } // if
            
                // Cast the event to a NGSI event
                NGSIEvent ngsiEvent;
            
                if (event instanceof NGSIEvent) {
                    // Event comes from memory... everything is already in memory
                    ngsiEvent = (NGSIEvent)event;
                } else {
                    // Event comes from file... original and mapped context elements must be re-created
                    String[] contextElementsStr = (new String(event.getBody())).split(CommonConstants.CONCATENATOR);
                    Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Pattern.class, new PatternTypeAdapter())
                        .create();
                    ContextElement originalCE = null;
                    ContextElement mappedCE = null;
                
                    if (contextElementsStr.length == 1) {
                        originalCE = gson.fromJson(contextElementsStr[0], ContextElement.class);
                    } else if (contextElementsStr.length == 2) {
                        originalCE = gson.fromJson(contextElementsStr[0], ContextElement.class);
                        mappedCE = gson.fromJson(contextElementsStr[1], ContextElement.class);
                    } // if else
                
                    // Re-create the NGSI event
                    ngsiEvent = new NGSIEvent(event.getHeaders(), event.getBody(), originalCE, mappedCE);
                    LOGGER.debug("Re-creating NGSI event from raw bytes in file channel, original context element: "
                                 + (originalCE == null ? null : originalCE.toString()) + ", mapped context element: "
                                 + (mappedCE == null ? null : mappedCE.toString()));
                } // if else

                // Set the correlation ID, transaction ID, service and service path in MDC
                ThreadContext.put(CommonConstants.LOG4J_CORR,
                        ngsiEvent.getHeaders().get(CommonConstants.HEADER_CORRELATOR_ID));
                ThreadContext.put(CommonConstants.LOG4J_TRANS,
                        ngsiEvent.getHeaders().get(NGSIConstants.FLUME_HEADER_TRANSACTION_ID));
                // One batch is able to handle/process several events from different srv/subsrvs (#1983)
                ThreadContext.put(CommonConstants.LOG4J_SVC, CommonConstants.NA);
                ThreadContext.put(CommonConstants.LOG4J_SUBSVC, CommonConstants.NA);

                // Accumulate the event
                accumulator.accumulate(ngsiEvent);
                numProcessedEvents++;
            } // for

            // Save the current index for next run of the process() method
            accumulator.setAccIndex(currentIndex);

            // Persist the accumulation

            if (accumulator.getAccIndex() != 0) {
                LOGGER.debug("Batch completed");
                NGSIBatch batch = accumulator.getBatch();
                NGSIBatch rollbackBatch = new NGSIBatch();
                StringBuffer transactionIds = new StringBuffer();
                batch.startIterator();
                while (batch.hasNext()) {
                    NGSIBatch batchToPersist = new NGSIBatch();
                    String destination = batch.getNextDestination();
                    ArrayList<NGSIEvent> events = batch.getNextEvents();
                    for (NGSIEvent event : events) {
                        batchToPersist.addEvent(destination, event);
                        transactionIds.append(event.getHeaders().get(CommonConstants.HEADER_CORRELATOR_ID)).append(", ");
                    }
                    try {
                        persistBatch(batchToPersist);
                        updateServiceMetrics(batchToPersist, false);
                        if (persistencePolicyMaxRecords > -1) {
                            try {
                                capRecords(batchToPersist, persistencePolicyMaxRecords);
                            } catch (CygnusCappingError e) {
                                LOGGER.error(e.getMessage() + " Sink: " + this.getName() + " Destination: " + destination + " Stack trace: " + Arrays.toString(e.getStackTrace()));
                            } // try
                        } // if
                        numPersistedEvents += batchToPersist.getNumEvents();
                        LOGGER.info("Finishing internal transaction (" + transactionIds + ")" + " Sink: " + this.getName() + " Destination: " + destination );
                    } catch (CygnusBadConfiguration | CygnusBadContextData | CygnusRuntimeError e) {
                        updateServiceMetrics(batchToPersist, true);
                        LOGGER.error(e.getMessage() + " Sink: " + this.getName() + " Destination: " + destination + " Stack trace: " + Arrays.toString(e.getStackTrace()));
                        if (batchToPersist.getNumEvents() > 1) {
                            // Maybe there are other events int batch that could finally get inserted
                            for (NGSIEvent event : batchToPersist.getNextEvents()) {
                                rollbackBatch.addEvent(destination, event);
                            }
                        }
                    } catch (Exception e) {
                        updateServiceMetrics(batchToPersist, true);
                        LOGGER.error(e.getMessage() + " Sink: " + this.getName() + " Destination: " + destination + " Stack trace: " + Arrays.toString(e.getStackTrace()));
                        for (NGSIEvent event : batchToPersist.getNextEvents()) {
                            rollbackBatch.addEvent(destination, event);
                        }
                    } finally {
                        batch.setNextPersisted(true);
                    }
                } // while (batch.hasNext())
                if (rollbackBatch.getNumEvents() > 0) {
                    Accumulator rollbackAccumulator = new Accumulator();
                    rollbackAccumulator.initialize(accumulator.getAccStartDate());
                    rollbackBatch.startIterator();
                    while (rollbackBatch.hasNext()) {
                        for (NGSIEvent event : rollbackBatch.getNextEvents()) {
                            rollbackAccumulator.accumulate(event);
                        }
                    }
                    doRollback(rollbackAccumulator.clone());
                    accumulator.initialize(new Date().getTime());
                    txn.commit();
                    setMDCToNA();
                    return Status.BACKOFF;
                }

            } // if
            accumulator.initialize(new Date().getTime());
            txn.commit();
        } catch (ChannelException ex) {
            LOGGER.info("Rollback transaction by ChannelException  (" + ex.getMessage() + ")  Sink: " +
                        this.getName());
            num_rollback_by_channel_exception++;
            if (num_rollback_by_channel_exception >= NGSIConstants.ROLLBACK_CHANNEL_EXCEPTION_THRESHOLD) {
                LOGGER.warn("Rollback (" + num_rollback_by_channel_exception +
                            " times) transaction by ChannelException  (" + ex.getMessage() + ")  Sink: " +
                        this.getName());
                num_rollback_by_channel_exception = 0;
            }
            txn.rollback();
        } catch (Exception ex) {
            LOGGER.info("Rollback transaction by Exception  (" + ex.getMessage() + ")  Sink: " +
                        this.getName());
            num_rollback_by_exception++;
            if (num_rollback_by_exception >= NGSIConstants.ROLLBACK_EXCEPTION_THRESHOLD) {
                LOGGER.warn("Rollback (" + num_rollback_by_exception +
                            " times) transaction by Exception  (" + ex.getMessage() + ")  Sink: " +
                        this.getName());
                num_rollback_by_exception = 0;
            }
            txn.rollback();
        } finally {
            txn.close();
        }
        setMDCToNA();
        return Status.READY;
    } // processNewBatches
    
    /**
     * Sets some MDC logging fields to 'N/A' for this thread. Value for the component field is inherited from main
     * thread (CygnusApplication.java).
     */
    private void setMDCToNA() {
        ThreadContext.put(CommonConstants.LOG4J_CORR, CommonConstants.NA);
        ThreadContext.put(CommonConstants.LOG4J_TRANS, CommonConstants.NA);
        ThreadContext.put(CommonConstants.LOG4J_SVC, CommonConstants.NA);
        ThreadContext.put(CommonConstants.LOG4J_SUBSVC, CommonConstants.NA);
        ThreadContext.put(CommonConstants.LOG4J_COMP, CommonConstants.DEF_AGENT_NAME);
    } // setMDCToNA

    /**
     * Rollbacks the accumulator for the first time.
     * @param accumulator Accumulator to be rollbacked
     */
    protected void doRollback(Accumulator accumulator) {
        if (accumulator.getTTL() == -1) {
            accumulator.setLastRetry(new Date().getTime());
            rollbackedAccumulations.add(accumulator);
            LOGGER.info("Rollbacking (" + accumulator.getAccTransactionIds() + "), "
                    + "infinite batch TTL" + " Sink: " + this.getName());
        } else if (accumulator.getTTL() > 0) {
            accumulator.setLastRetry(new Date().getTime());
            rollbackedAccumulations.add(accumulator);
            LOGGER.info("Rollbacking (" + accumulator.getAccTransactionIds() + "), "
                    + batchTTL + " retries will be done" + " Sink: " + this.getName());
        } else {
            if (!accumulator.getAccTransactionIds().isEmpty()) {
                LOGGER.info("Finishing internal transaction ("
                        + accumulator.getAccTransactionIds() + "), 0 retries will be done" + " Sink: " + this.getName());
            } // if
        } // if else
    } // doRollback
    
    private void updateServiceMetrics(NGSIBatch batch, boolean error) {
        batch.startIterator();
        
        while (batch.hasNext()) {
            ArrayList<NGSIEvent> events = batch.getNextEvents();
            NGSIEvent event = events.get(0);
            String service = event.getServiceForData();
            String servicePath = event.getServicePathForData();
            long time = (new Date().getTime() - event.getRecvTimeTs()) * events.size();
            serviceMetrics.add(service, servicePath, 0, 0, 0, 0, time, events.size(), 0, 0, error ? events.size() : 0);
        } // while
    } // updateServiceMetrics

    /**
     * Utility class for batch-like getRecvTimeTs accumulation purposes.
     */
    protected class Accumulator implements Cloneable {

        // accumulated events
        private NGSIBatch batch;
        private long accStartDate;
        private int accIndex;
        private String accTransactionIds;
        private int ttl;
        private long lastRetry;

        /**
         * Constructor.
         */
        public Accumulator() {
            batch = new NGSIBatch();
            accStartDate = 0;
            accIndex = 0;
            accTransactionIds = null;
            ttl = batchTTL;
            lastRetry = 0;
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

        public NGSIBatch getBatch() {
            return batch;
        } // getBatch

        public String getAccTransactionIds() {
            return accTransactionIds;
        } // getAccTransactionIds
        
        public long getLastRetry() {
            return lastRetry;
        } // getLastRetry
        
        public void setLastRetry(long lastRetry) {
            this.lastRetry = lastRetry;
        } // setLastRetry
        
        public int getTTL() {
            return ttl;
        } // getTTL
        
        public void setTTL(int ttl) {
            this.ttl = ttl;
        } // setTTL

        /**
         * Accumulates an getRecvTimeTs given its headers and context data.
         * @param event
         */
        public void accumulate(NGSIEvent event) {
            String transactionId = event.getHeaders().get(CommonConstants.HEADER_CORRELATOR_ID);

            if (accTransactionIds.isEmpty()) {
                accTransactionIds = transactionId;
            } else {
                accTransactionIds += "," + transactionId;
            } // if else

            switch (dataModel) {
                case DMBYSERVICE:
                    accumulateByService(event);
                    break;
                case DMBYSERVICEPATH:
                    accumulateByServicePath(event);
                    break;
                case DMBYENTITYDATABASE:
                case DMBYENTITYDATABASESCHEMA:
                case DMBYENTITY:
                    accumulateByEntity(event);
                    break;
                case DMBYENTITYTYPEDATABASE:
                case DMBYENTITYTYPEDATABASESCHEMA:
                case DMBYENTITYTYPE:
                    accumulateByEntityType(event);
                    break;
                case DMBYATTRIBUTE:
                    accumulateByAttribute(event);
                    break;
                case DMBYENTITYID:
                    accumulateByEntityId(event);
                    break;
                case DMBYFIXEDENTITYTYPE:
                case DMBYFIXEDENTITYTYPEDATABASE:
                case DMBYFIXEDENTITYTYPEDATABASESCHEMA:
                    accumulateByFixedEntityType(event);
                    break;
                default:
                    LOGGER.error("Unknown data model. Details=" + dataModel.toString() + " Sink: " + this.getClass().getName());
            } // switch
        } // accumulate

        private void accumulateByService(NGSIEvent event) {
            Map<String, String> headers = event.getHeaders();
            String destination;
            
            if (enableNameMappings) {
                destination = headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE);
            } else {
                destination = headers.get(CommonConstants.HEADER_FIWARE_SERVICE);
            } // if else
            
            batch.addEvent(destination, event);
        } // accumulateByService

        private void accumulateByServicePath(NGSIEvent event) {
            Map<String, String> headers = event.getHeaders();
            String destination;

            if (enableNameMappings) {
                destination = headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE) + "_"
                        + headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH);
            } else {
                destination = headers.get(CommonConstants.HEADER_FIWARE_SERVICE) + "_"
                        + headers.get(CommonConstants.HEADER_FIWARE_SERVICE_PATH);
            } // if else

            batch.addEvent(destination, event);
        } // accumulateByServicePath

        private void accumulateByEntity(NGSIEvent event) {
            Map<String, String> headers = event.getHeaders();
            ContextElement originalCE = event.getOriginalCE();
            ContextElement mappedCE = event.getMappedCE();
            String destination;

            if (enableNameMappings) {
                destination = headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE) + "_"
                        + headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH) + "_"
                        + mappedCE.getId() + "_" + mappedCE.getType();
            } else {
                destination = headers.get(CommonConstants.HEADER_FIWARE_SERVICE) + "_"
                        + headers.get(CommonConstants.HEADER_FIWARE_SERVICE_PATH) + "_"
                        + originalCE.getId() + "_" + originalCE.getType();
            } // if else

            batch.addEvent(destination, event);
        } // accumulateByEntity

        private void accumulateByEntityType(NGSIEvent event) {
            Map<String, String> headers = event.getHeaders();
            ContextElement originalCE = event.getOriginalCE();
            ContextElement mappedCE = event.getMappedCE();
            String destination;

            if (enableNameMappings) {
                destination = headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE) + "_"
                        + headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH) + "_"
                        + mappedCE.getType();
            } else {
                destination = headers.get(CommonConstants.HEADER_FIWARE_SERVICE) + "_"
                        + headers.get(CommonConstants.HEADER_FIWARE_SERVICE_PATH) + "_"
                        + originalCE.getType();
            } // if else

            batch.addEvent(destination, event);
        } // accumulateByEntityType

        private void accumulateByEntityId(NGSIEvent event) {
            Map<String, String> headers = event.getHeaders();
            ContextElement originalCE = event.getOriginalCE();
            ContextElement mappedCE = event.getMappedCE();
            String destination;

            if (enableNameMappings) {
                destination = headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE) + "_"
                        + headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH) + "_"
                        + mappedCE.getId() + "_" + mappedCE.getType();
            } else {
                destination = headers.get(CommonConstants.HEADER_FIWARE_SERVICE) + "_"
                        + headers.get(CommonConstants.HEADER_FIWARE_SERVICE_PATH) + "_"
                        + originalCE.getId() + "_" + originalCE.getType();
            } // if else

            batch.addEvent(destination, event);
        } // accumulateByServicePath

        private void accumulateByFixedEntityType(NGSIEvent event) {
            Map<String, String> headers = event.getHeaders();
            ContextElement originalCE = event.getOriginalCE();
            ContextElement mappedCE = event.getMappedCE();
            String destination;

            if (enableNameMappings) {
                destination = headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE) + "_" + mappedCE.getType();
            } else {
                destination = headers.get(CommonConstants.HEADER_FIWARE_SERVICE) + "_" + originalCE.getType();
            } // if else

            batch.addEvent(destination, event);
        } // accumulateByEntityIdType

        private void accumulateByAttribute(NGSIEvent event) {
            Map<String, String> headers = event.getHeaders();
            ContextElement originalCE = event.getOriginalCE();
            ContextElement mappedCE = event.getMappedCE();
            String destination;

            if (enableNameMappings) {
                destination = headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE) + "_"
                        + headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH) + "_"
                        + mappedCE.getId() + "_" + mappedCE.getType();
                ArrayList<ContextAttribute> attrs = mappedCE.getAttributes();

                for (ContextAttribute attr : attrs) {
                    ContextElement filteredOriginalCE = originalCE.filter(attr.getName());
                    ContextElement filteredMappedCE = mappedCE.filter(attr.getName());
                    event.setOriginalCE(filteredOriginalCE);
                    event.setMappedCE(filteredMappedCE);
                    batch.addEvent(destination + "_" + attr.getName(), event);
                } // for
            } else {
                destination = headers.get(CommonConstants.HEADER_FIWARE_SERVICE) + "_"
                        + headers.get(CommonConstants.HEADER_FIWARE_SERVICE_PATH) + "_"
                        + originalCE.getId() + "_" + originalCE.getType();
                ArrayList<ContextAttribute> attrs = originalCE.getAttributes();

                for (ContextAttribute attr : attrs) {
                    ContextElement filteredOriginalCE = originalCE.filter(attr.getName());
                    ContextElement filteredMappedCE = mappedCE.filter(attr.getName()); // not really necessary...
                    event.setOriginalCE(filteredOriginalCE);
                    event.setMappedCE(filteredMappedCE);
                    batch.addEvent(destination + "_" + attr.getName(), event);
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
            batch = new NGSIBatch();
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
     * Class for checking about expired records.
     */
    private class ExpirationTimeChecker extends Thread {
        
        private final String sinkName;
        
        /**
         * Constructor.
         * @param sinkName
         */
        public ExpirationTimeChecker(String sinkName) {
            this.sinkName = sinkName;
        } // ExpirationTimeChecker

        @Override
        public void run() {
            while (true) {
                long timeBefore = 0;
                long timeAfter = 0;
                
                if (persistencePolicyExpirationTime > -1) {
                    timeBefore = new Date().getTime();
                    
                    try {
                        LOGGER.debug("[" + sinkName + "] Expirating records");
                        expirateRecords(persistencePolicyExpirationTime);
                    } catch (CygnusExpiratingError e) {
                        LOGGER.error("[" + sinkName + "] Error while expirating records. Details: "
                                + e.getMessage());
                    } // try catch
                    
                    timeAfter = new Date().getTime();
                } // if
                
                long timeExpent = timeAfter - timeBefore;
                long sleepTime = (persistencePolicyCheckingTime * 1000) - timeExpent;
                
                if (sleepTime <= 0) {
                    sleepTime = 1000; // sleep at least 1 second
                } // if
                
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    LOGGER.error("[" + sinkName + "] Error while sleeping. Details: " + e.getMessage());
                } // try
            } // while
        } // run

    } // ExpirationTimeChecker
    
    /**
     * This is the method the classes extending this class must implement when dealing with a batch of events to be
     * persisted.
     * @param batch
     * @throws Exception
     */
    abstract void persistBatch(NGSIBatch batch) throws CygnusBadConfiguration, CygnusBadContextData,
            CygnusRuntimeError, CygnusPersistenceError;
    
    /**
     * This is the method the classes extending this class must implement when dealing with size-based capping.
     * @param batch
     * @param maxRecords
     * @throws EventDeliveryException
     */
    abstract void capRecords(NGSIBatch batch, long maxRecords) throws CygnusCappingError;
    
    /**
     * This is the method the classes extending this class must implement when dealing with time-based expiration.
     * @param expirationTime
     * @throws Exception
     */
    abstract void expirateRecords(long expirationTime) throws CygnusExpiratingError;

} // NGSISink
