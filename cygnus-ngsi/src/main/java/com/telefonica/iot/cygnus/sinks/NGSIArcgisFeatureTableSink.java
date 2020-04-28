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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.apache.flume.EventDeliveryException;
import org.json.JSONException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telefonica.iot.cygnus.backends.arcgis.NGSIArcgisFeatureTable;
import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.model.Feature;
import com.telefonica.iot.cygnus.backends.arcgis.model.GisAttributeType;
import com.telefonica.iot.cygnus.backends.arcgis.model.Point;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.ArcgisFeatureTable;
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
import com.telefonica.iot.cygnus.utils.NGSIConstants;

import jodd.util.URLDecoder;

/**
 *
 * @author dmartinez
 * 
 *         Detailed documentation can be found at:
 *         https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/flume_extensions_catalogue/ngsi_arcgis_sink.md
 */
public class NGSIArcgisFeatureTableSink extends NGSISink {

    private static final String DEFAULT_ARCGIS_SERVICE_URL = "localhost";
    private static final String DEFAULT_GETTOKEN_URL = "localhost";
    private static final String DEFAULT_USER_NAME = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final int DEFAULT_MAX_BATCH_SIZE = 10;
    private static final int DEFAULT_BATCH_TIMEOUT_SECS = 60;
    private static final String ARCGIS_INSTANCE_NAME = "arcgis";

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIArcgisFeatureTableSink.class);
    private String arcgisServicesUrl;
    private String getTokenUrl;
    private String userName;
    private String password;
    private int maxBatchSize;
    private long timeoutSecs;
    private static volatile Map<String, NGSIArcgisFeatureTable> arcgisPersistenceBackend;

    /**
     * Constructor.
     */
    public NGSIArcgisFeatureTableSink() {
        super();
    } // NGSIArcgisFeatureTableSink

    /**
     * Gets the Argis services url. It is protected due to it is only required for testing purposes.
     * 
     * @return The services url
     */
    protected String getrAcgisServicesUrl() {
        return arcgisServicesUrl;
    } // getrAcgisServicesUrl

    /**
     * Gets getToken's service url.
     * 
     * @return
     */
    public String getGetTokenUrl() {
        return getTokenUrl;
    }

    /**
     * Sets getToken service url.
     * 
     * @param getTokenUrl
     */
    public void setGetTokenUrl(String getTokenUrl) {
        this.getTokenUrl = getTokenUrl;
    }

    /**
     * Gets the username. It is protected due to it is only required for testing purposes.
     * 
     * @return The username
     */
    protected String getUsername() {
        return userName;
    } // getUsername

    /**
     * Gets the password. It is protected due to it is only required for testing purposes.
     * 
     * @return The password
     */
    protected String getPassword() {
        return password;
    } // getPassword

    /**
     * 
     */
    protected int featuresBatched() {
        int total = 0;
        for (Map.Entry<String, NGSIArcgisFeatureTable> entry : arcgisPersistenceBackend.entrySet()) {
            NGSIArcgisFeatureTable table = entry.getValue();
            if (table != null) {
                total += table.featuresBatched();
            }
        }

        return total;
    }

    /**
     * Returns the persistence backend. It is protected due to it is only required for testing purposes.
     * 
     * @return The persistence backend
     * @throws CygnusRuntimeError
     */
    protected ArcgisFeatureTable getPersistenceBackend(String featureServiceUrl) throws CygnusRuntimeError {

        if (arcgisPersistenceBackend.containsKey(featureServiceUrl)) {
            return arcgisPersistenceBackend.get(featureServiceUrl);
        } else {
            LOGGER.debug("Creating new persistenceBackend for Feature table: " + featureServiceUrl);
            LOGGER.debug("Token url: " + getGetTokenUrl());
            try {
                NGSIArcgisFeatureTable newTable = new NGSIArcgisFeatureTable(featureServiceUrl, getUsername(),
                        getPassword(), getGetTokenUrl(), timeoutSecs);
                newTable.setBatchAction(ArcgisFeatureTable.ADD_UPDATE_ACTION);
                newTable.setBatchSize(maxBatchSize);

                if (newTable.hasError() || !newTable.connected()) {
                    LOGGER.error("Error creating new persistence Backend. " + newTable.getErrorDesc());
                    throw new CygnusRuntimeError("[" + this.getName() + "Error creating Persistence backend: "
                            + newTable.getErrorCode() + " - " + newTable.getErrorDesc());
                } else {
                    arcgisPersistenceBackend.put(featureServiceUrl, newTable);
                    return newTable;
                }
            } catch (Throwable e) {
                String stackTrace = ExceptionUtils.getFullStackTrace(e);
                
                LOGGER.error("Error creating new persistence Backend. " +e.getClass().getSimpleName());
                LOGGER.debug(stackTrace);
                
                throw new CygnusRuntimeError("Error creating new persistence Backend. ", e.getClass().getName(),
                        e.getMessage());
            }
        }
    } // getPersistenceBackend

    /**
     * Sets the persistence backend. It is protected due to it is only required for testing purposes.
     * 
     * @param persistenceBackend
     */
    protected void setPersistenceBackend(String featureServiceUrl, NGSIArcgisFeatureTable persistenceBackend) {
        arcgisPersistenceBackend.put(featureServiceUrl, persistenceBackend);
    } // setPersistenceBackend

    @Override
    public void configure(Context context) {
        arcgisServicesUrl = context.getString("arcgis_service_url", DEFAULT_ARCGIS_SERVICE_URL);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (arcgis_service_url=" + arcgisServicesUrl + ")");

        getTokenUrl = context.getString("arcgis_gettoken_url", DEFAULT_GETTOKEN_URL);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (arcgis_fearcgis_gettoken_urlature_table_url="
                + getTokenUrl + ")");

        userName = context.getString("arcgis_username", DEFAULT_USER_NAME);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (arcgis_username=" + userName + ")");
        // FIXME: Password should be read encrypted and decoded here
        password = context.getString("arcgis_password", DEFAULT_PASSWORD);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (arcgis_password=" + "XXXXXXXX" + ")");

        maxBatchSize = context.getInteger("arcgis_maxBatchSize", DEFAULT_MAX_BATCH_SIZE);
        if (maxBatchSize <= 0 || maxBatchSize > 65535) {
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Invalid configuration (arcgis_maxBatchSize=" + maxBatchSize + ") "
                    + "must be an integer between 1 and 65535");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (arcgis_maxBatchSize=" + maxBatchSize + ")");
        }

        timeoutSecs = context.getInteger("arcgis_timeoutSecs", DEFAULT_BATCH_TIMEOUT_SECS);
        if (timeoutSecs <= 0 || timeoutSecs > 65535) {
            invalidConfiguration = true;
            LOGGER.error("[" + this.getName() + "] Invalid configuration (arcgis_timeoutSecs=" + timeoutSecs + ") "
                    + "must be an integer between 1 and 65535");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (arcgis_timeoutSecs=" + maxBatchSize + ")");
        }

        super.configure(context);
    } // configure

    @Override
    public void start() {
        try {
            createPersistenceBackend();
            LOGGER.debug("[" + this.getName() + "] Arcgis persistence backend created");
        } catch (Exception e) {
            LOGGER.error("Error while creating the Arcgis persistence backend. Details=" + e.getMessage());
        } // try catch

        super.start();
    } // start

    @Override
    public void stop() {
        super.stop();
        if (arcgisPersistenceBackend != null) {
            for (Map.Entry<String, NGSIArcgisFeatureTable> backend : arcgisPersistenceBackend.entrySet()) {
                backend.getValue().flushBatch();
            }
            arcgisPersistenceBackend.clear();
        }
    } // stop

    /**
     * Initialices a lazy singleton to share among instances on JVM.
     */
    private static synchronized void createPersistenceBackend() {
        if (arcgisPersistenceBackend == null) {
            arcgisPersistenceBackend = new ConcurrentHashMap<String, NGSIArcgisFeatureTable>();
        }
    }

    @Override
    void persistBatch(NGSIBatch batch)
            throws CygnusBadConfiguration, CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if

        try {
            // Iterate on the destinations
            batch.startIterator();

            while (batch.hasNext()) {
                String destination = batch.getNextDestination();
                LOGGER.debug(
                        "[" + this.getName() + "] Processing sub-batch regarding the " + destination + " destination");

                // Get the events within the current sub-batch
                ArrayList<NGSIEvent> events = batch.getNextEvents();

                // Get an aggregator for this destination and initialize it
                NGSIArcgisAggregator aggregator = new NGSIArcgisAggregator(getrAcgisServicesUrl(), enableNameMappings);
                
                for (NGSIEvent event : events) {
                    aggregator.aggregate(event);
                } // for

                // Persist the aggregation
                persistAggregation(aggregator);
                batch.setNextPersisted(true);

            } // while
        } catch (Exception e) {
            LOGGER.error("[" + this.getName() + "] Error persisting batch, " + e.getClass().getSimpleName() + "." + e.getMessage());
            throw new CygnusRuntimeError(e.getMessage());
        }
    } // persistBatch

    /*
     * (non-Javadoc)
     * 
     * @see com.telefonica.iot.cygnus.sinks.NGSISink#process()
     */
    @Override
    public Status process() throws EventDeliveryException {
        checkTimeouts();
        Status status = null;
        try {
            status = super.process();
        } catch (Throwable e) {
            LOGGER.error(e.getMessage() + "Stack trace: " + Arrays.toString(e.getStackTrace()));
        }
        return status;
    }

    /**
     * Flush if timeout.
     */
    protected void checkTimeouts() {
        boolean timeoutFound = false;
        for (Map.Entry<String, NGSIArcgisFeatureTable> entry : arcgisPersistenceBackend.entrySet()) {
            NGSIArcgisFeatureTable table = entry.getValue();
            if (table.hasTimeout()) {
                timeoutFound = true;
                LOGGER.info("[" + this.getName() + "] Feature table Timeout, flushing batch. " + entry.getKey());
                table.flushBatch();
            }
        }
        if (!timeoutFound) {
            LOGGER.debug("[" + this.getName() + "] No Feature table Timeouts found. Features in batch: "
                    + featuresBatched());
        }
    }

    @Override
    public void capRecords(NGSIBatch batch, long maxRecords) throws CygnusCappingError {
        // no capping
        LOGGER.warn("[" + this.getName() + "] Skipping capping records, just persisting it.");
        try {
            persistBatch(batch);
        } catch (CygnusBadConfiguration | CygnusPersistenceError | CygnusRuntimeError | CygnusBadContextData e) {
            LOGGER.error("[" + this.getName() + "] Error capping Records: " + e);
            throw new CygnusCappingError(e.getMessage());
        }
    } // capRecords

    @Override
    public void expirateRecords(long expirationTime) throws CygnusExpiratingError {
        LOGGER.debug("[" + this.getName() + "] Expirating records (time=" + expirationTime + ")");

    } // expirateRecords

    /**
     * Persist Aggregation.
     * 
     * @param aggregator
     * @throws CygnusRuntimeError
     */
    public void persistAggregation(NGSIArcgisAggregator aggregator) throws CygnusRuntimeError {
        try {
            List<ArcgisAggregatorDomain> aggregationList = aggregator.getListArcgisAggregatorDomain();
            LOGGER.debug("[" + this.getName() + "] persisting aggregation, "
                    + aggregator.getListArcgisAggregatorDomain().size() + " features.");
            for (ArcgisAggregatorDomain aggregation : aggregationList) {
                String featureTableUrl = aggregation.getFeatureTableUrl();

                boolean isNewFeatureTable = !arcgisPersistenceBackend.containsKey(featureTableUrl);
                LOGGER.debug("[" + this.getName() + "] persistAggregation - Feature table: " + featureTableUrl
                        + " is new: " + isNewFeatureTable);
                ArcgisFeatureTable featureTable = getPersistenceBackend(featureTableUrl);

                // If it's a new one, sets uniqueField value
                if (isNewFeatureTable) {
                    LOGGER.debug("[" + this.getName() + "] Created new backend for " + featureTableUrl
                            + " with uniqueField: " + aggregation.getUniqueField());
                    featureTable.setUniqueField(aggregation.getUniqueField());
                }

                featureTable.addToBatch(aggregation.getFeature());
                if (featureTable.hasError()){
                    throw new ArcgisException(featureTable.getErrorCode(), featureTable.getErrorDesc());
                }
            }
        } catch (CygnusRuntimeError e) {
            String stackTrace = ExceptionUtils.getFullStackTrace(e);
            LOGGER.debug(" PersistAggregation Error: " + stackTrace);
            throw (e);
        } catch (Exception e) {
            LOGGER.error("[" + this.getName() + "] Error persisting batch, " + e.getClass().getSimpleName() + " - "
                    + e.getMessage());
            throw new CygnusRuntimeError(e.getMessage());
        }
    } // persistAggregation

    /**
     * @author dmartinez
     *
     */
    public class NGSIArcgisAggregator {

        private List<ArcgisAggregatorDomain> listArcgisAggregatorDomain;
        private boolean enableNameMappings;
        private String argisServiceUrl;

        private NGSIArcgisAggregator() {
            listArcgisAggregatorDomain = new ArrayList<ArcgisAggregatorDomain>();
            enableNameMappings = true;
            argisServiceUrl = "";
        } // ArcgisAggregator

        /**
         * Constructor.
         * 
         * @param argisServiceUrl
         * @param enableNameMappings
         */
        public NGSIArcgisAggregator(String argisServiceUrl, boolean enableNameMappings) {
            this();
            this.enableNameMappings = enableNameMappings;
            this.argisServiceUrl = argisServiceUrl;
        } // ArcgisAggregator

        /**
         * @return the listArcgisAggregatorDomain
         */
        public List<ArcgisAggregatorDomain> getListArcgisAggregatorDomain() {
            return listArcgisAggregatorDomain;
        }

        /**
         * Determines if input sitring is quoted or not.
         * 
         * @param string
         * @return boolean
         */

        /**
         * 
         * @param event
         * @throws CygnusRuntimeError
         */
        public void aggregate(NGSIEvent event) throws CygnusRuntimeError {
            LOGGER.debug("[NGSIArcgisAggregator] aggregate - ContextElement ->" + event.getContextElement());
            LOGGER.debug("[NGSIArcgisAggregator] aggregate -  MappedCE ->" + event.getMappedCE());
            LOGGER.debug("[NGSIArcgisAggregator] aggregate - OriginalCE ->" + event.getOriginalCE());
            LOGGER.debug("[NGSIArcgisAggregator] aggregate - enableNameMappings status -> " + enableNameMappings);

            ContextElement contextElement = null;
            try {
                LOGGER.debug("[NGSIArcgisAggregator] aggregate - creating new aggregation object.");
                ArcgisAggregatorDomain aggregation = new ArcgisAggregatorDomain();
                LOGGER.debug("[NGSIArcgisAggregator] aggregate - aggregation created");
                Feature feature = aggregation.getFeature();
                LOGGER.debug("[NGSIArcgisAggregator] aggregate - Feature getted");
                String service = "";
                String subService = "";
                String featureTableUrl = "";

                LOGGER.debug("[NGSIArcgisAggregator] aggregate - Selecting context");
                // get the contextElement
                if (!enableNameMappings) {
                    LOGGER.debug("[NGSIArcgisAggregator] aggregate - no mappings");
                    contextElement = event.getContextElement();
                } else {
                    LOGGER.debug("[NGSIArcgisAggregator] aggregate - mappings");
                    contextElement = event.getMappedCE();
                }

                LOGGER.debug("[NGSIArcgisAggregator] aggregate - Selected context ->" + contextElement);

                // get the getRecvTimeTs headers
                Map<String, String> headers = event.getHeaders();

                for (Entry<String, String> entry : headers.entrySet()) {
                    LOGGER.debug("Header entry key --> " + entry.getKey().toString() + ", value --> "
                            + entry.getValue().toString());
                    if (entry.getKey() != null
                            && NGSIConstants.FLUME_HEADER_MAPPED_SERVICE.equals(entry.getKey().toString())) {
                        service = entry.getValue().toString();
                    } else if (entry.getKey() != null
                            && NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH.equals(entry.getKey().toString())) {
                        subService = entry.getValue().toString();
                    }
                } // for
                
                // Compose full url
                featureTableUrl = argisServiceUrl + "/" + service + "/" + subService;
                featureTableUrl = featureTableUrl.replaceAll("([^:])\\/\\/", "$1/");
                aggregation.setFeatureTableUrl(featureTableUrl);

                LOGGER.debug("[NGSIArcgisAggregator] aggregate - featureTableUrl ->" + featureTableUrl);

                // iterate on all this context element attributes, if there are
                // attributes
                ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

                String entityId = contextElement.getId();
                String entityType = contextElement.getType();

                // Set unique filed and it's value.
                aggregation.setUniqueField(entityType);
                feature.addAttribute(entityType, entityId);

                contextAttrToFeature(feature, contextAttributes);

                aggregation.setFeature(feature);
                listArcgisAggregatorDomain.add(aggregation);

            } catch (JSONException e) {
                LOGGER.error(
                        "[NGSIArcgisAggregator] aggregate - Error pharsing JSON BODY " + contextElement.toString());
                throw new CygnusRuntimeError(e.getMessage());
            } catch (Exception e) {
                LOGGER.error("[NGSIArcgisAggregator] aggregate - Unexpected Error" + e.getMessage()
                        + "\n contextElement: " + contextElement.toString());
                throw new CygnusRuntimeError(e.getMessage());
            }

        } // aggregate

        /**
         * 
         * @param feature
         * @param contextAttributes
         */
        protected void contextAttrToFeature(Feature feature, ArrayList<ContextAttribute> contextAttributes) {
            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.debug(
                        "[NGSIArcgisAggregator-contextAttrToFeature] contextAttributes NULL or empty, ignoring it. ");
            } else {
                for (ContextAttribute contextAttribute : contextAttributes) {
                    LOGGER.debug("[NGSIArcgisAggregator-contextAttrToFeature] Processing Attribute: "
                            + contextAttribute.toString());
                    String attrName = contextAttribute.getName();
                    String attrType = contextAttribute.getType();
                    // get attribute value as is, quoted or not
                    JsonElement attrValue = contextAttribute.getValue();

                    // TODO crop metadata values
                    LOGGER.debug("[NGSIArcgisAggregator] aggregate - Atribute (name --> " + attrName + ", type --> "
                            + attrType + ", value --> " + attrValue + ", metadata= " + "ignored " + ")");

                    // Get Feature attributes
                    LOGGER.debug("Proccessing attribute value:  " + attrName + " --> " + attrValue);

                    jsonElementToFeatureAttr(attrName, attrType, attrValue, feature);

                } // for
            } // if else
        }
    } // NGSIArcgisAggregator

    /**
     * Extracts Feature attributes from JSON.
     * 
     * @param attrName
     * @param attrType
     * @param attrValue
     * @param feature
     */
    protected void jsonElementToFeatureAttr(String attrName, String attrType, JsonElement attrValue, Feature feature) {

        // Get Feature attributes
        LOGGER.debug("Proccessing attribute value:  " + attrName + " --> " + attrValue);

        attrType = attrType.trim().toLowerCase();

        switch (attrType) {

            case "geo:json":
                try {
                    JsonObject location = attrValue.getAsJsonObject();
                    if (location.get("type").getAsString().equals("Point")) {
                        JsonArray coordinates = location.get("coordinates").getAsJsonArray();
                        double latitude = coordinates.get(0).getAsDouble();
                        double longitude = coordinates.get(1).getAsDouble();

                        Point point = new Point(latitude, longitude);
                        feature.setGeometry(point);

                    } else {
                        LOGGER.warn("Invalid geo:json type, only points allowed: " + location.toString());
                    }
                } catch (Exception e) {
                    LOGGER.error("Invalid geo:json format, (sikipped): " + attrValue.toString() + " - Error: "
                            + e.getMessage());
                }
                break;
            case "boolean":
                if (attrValue.isJsonPrimitive() && attrValue.getAsJsonPrimitive().isBoolean()) {
                    feature.addAttribute(attrName, attrValue.getAsBoolean());
                } else {
                    String strValue = attrValue.getAsString().toLowerCase().trim();
                    Boolean result = GisAttributeType.parseBoolean(strValue);
                    feature.addAttribute(attrName, result);
                }
                break;
            case "datetime":
                String dateStr = attrValue.toString();
                feature.addAttribute(attrName, parseFiwareDate(dateStr));
                break;

            default:
                // Verify if it is a string (it is into quotation marks)
                if (isQuoted(attrValue.toString())) {
                    // Insert unquoted
                    String strValue = URLDecoder.decode(unquote(attrValue.toString()));
                    feature.getAttributes().put(attrName, strValue);
                } else {
                    try {
                        // Try to insert as Integer
                        feature.addAttribute(attrName, Integer.parseInt(attrValue.toString()));
                    } catch (NumberFormatException e2) {
                        try {
                            // Try to insert as Double
                            feature.addAttribute(attrName, Double.parseDouble(attrValue.toString()));
                        } catch (NumberFormatException e3) {
                            // If all fails, insert as String
                            LOGGER.warn(
                                    "[NGSIArcgisAggregator] Unquoted String attribute: " + attrName + ":" + attrValue);
                            String strValue = URLDecoder.decode(attrValue.toString());
                            feature.addAttribute(attrName, strValue);
                        }
                    }
                }
        }

    }

    /**
     * Try to convert Fiware DateTime String to Date object, If it can't, input string will be returned.
     * 
     * @param dateStr
     * @return Date Object, or String.
     */
    private Object parseFiwareDate(String dateStr) {

        // Normalize date String
        dateStr = unquote(dateStr);
        dateStr = dateStr.replaceAll("Z[\\s]*$", "+00:00");
        dateStr = dateStr.replaceAll("([+-]{1}[0-9]{1,2})[\\s\\t\\r\\n]*$", "$1:00");

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        try {
            Date date = dateFormatter.parse(dateStr);
            return date;
        } catch (ParseException e) {
            LOGGER.error("[NGSIArcgisAggregator] Unexpected DateTime format: " + dateStr);
            return dateStr;
        }
    }

    /**
     * Determines if input string is quoted or not.
     * 
     * @param string
     * @return boolean
     */

    private boolean isQuoted(String string) {
        return string.matches("^[\\s]*\\\"(.*)\\\"[\\s]*$");
    }

    /**
     * Unquote input string.
     * 
     * @param Unquoted
     *            string
     * @return
     */
    private String unquote(String string) {
        return string.replaceAll("^[\\s]*\\\"(.*)\\\"[\\s]*$", "$1");
    }

    /**
     * 
     * @author PMO Santander Smart City – Ayuntamiento de Santander
     *
     */
    public class ArcgisAggregatorDomain {

        // string containing the data aggregation
        private Feature feature;

        private String featureTableUrl;

        private String uniqueField;

        /**
           * 
           */
        ArcgisAggregatorDomain() throws CygnusRuntimeError {

            LOGGER.debug("[ArcgisAggregatorDomain] - constructor init.");
            try {
                feature = Feature.createPointFeature(0, 0);
            } catch (Throwable e) {
                LOGGER.error(
                        "ArcgisAggregatorDomain - Unexpected error " + e.getClass().getName() + " - " + e.getMessage());
                throw new CygnusRuntimeError(e.getMessage());
            }
            featureTableUrl = "";
            uniqueField = "";
            LOGGER.debug("[ArcgisAggregatorDomain] - constructor end");
        } // ArcgisAggregatorDomain


        /**
         * @return the feature
         */
        public Feature getFeature() {
            return feature;
        }

        /**
         * @param feature
         *            the feature to set
         */
        public void setFeature(Feature feature) {
            this.feature = feature;
        }

        /**
         * @return the featureTableUrl
         */
        public String getFeatureTableUrl() {
            return featureTableUrl;
        }

        /**
         * @param featureTableUrl
         *            the featureTableUrl to set
         */
        public void setFeatureTableUrl(String featureTableUrl) {
            this.featureTableUrl = featureTableUrl;
        }

        /**
         * @return the uniqueField
         */
        public String getUniqueField() {
            return uniqueField;
        }

        /**
         * @param uniqueField
         *            the uniqueField to set
         */
        public void setUniqueField(String uniqueField) {
            this.uniqueField = uniqueField;
        }

    }

} // NGSIArcgisFeatureTableSink
