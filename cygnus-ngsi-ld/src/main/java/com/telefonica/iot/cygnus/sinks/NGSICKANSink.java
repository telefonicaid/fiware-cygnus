/**
 * Copyright 2020 Telefonica Investigación y Desarrollo, S.A.U
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telefonica.iot.cygnus.backends.ckan.CKANBackendImpl;
import com.telefonica.iot.cygnus.backends.ckan.CKANBackend;
import com.telefonica.iot.cygnus.containers.NotifyContextRequestLD;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSILDEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.utils.NGSICharsets;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import com.google.gson.*;

import java.util.*;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.flume.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author anmunoz
 *
 * CKAN sink for Orion-LD Context Broker.
 *
 */
public class NGSICKANSink extends NGSILDSink {

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSICKANSink.class);
    private String apiKey;
    private String ckanHost;
    private String ckanPort;
    private String orionUrl;
    private boolean rowAttrPersistence;
    private boolean ssl;
    private int backendMaxConns;
    private int backendMaxConnsPerRoute;
    private String ckanViewer;
    private CKANBackend persistenceBackend;

    /**
     * Constructor.
     */
    public NGSICKANSink() {
        super();
    } // NGSICKANSink

    /**
     * Gets the CKAN host. It is protected due to it is only required for testing purposes.
     * @return The KCAN host
     */
    protected String getCKANHost() {
        return ckanHost;
    } // getCKANHost

    /**
     * Gets the CKAN port. It is protected due to it is only required for testing purposes.
     * @return The CKAN port
     */
    protected String getCKANPort() {
        return ckanPort;
    } // getCKANPort

    /**
     * Gets the CKAN API key. It is protected due to it is only required for testing purposes.
     * @return The CKAN API key
     */
    protected String getAPIKey() {
        return apiKey;
    } // getAPIKey

    /**
     * Returns if the attribute persistence is row-based. It is protected due to it is only required for testing
     * purposes.
     * @return True if the attribute persistence is row-based, false otherwise
     */
    protected boolean getRowAttrPersistence() {
        return rowAttrPersistence;
    } // getRowAttrPersistence

    /**
     * Returns the persistence backend. It is protected due to it is only required for testing purposes.
     * @return The persistence backend
     */
    protected CKANBackend getPersistenceBackend() {
        return persistenceBackend;
    } // getPersistenceBackend

    /**
     * Sets the persistence backend. It is protected due to it is only required for testing purposes.
     * @param persistenceBackend
     */
    protected void setPersistenceBackend(CKANBackend persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    } // setPersistenceBackend

    /**
     * Gets if the connections with CKAN is SSL-enabled. It is protected due to it is only required for testing
     * purposes.
     * @return True if the connection is SSL-enabled, false otherwise
     */
    protected boolean getSSL() {
        return this.ssl;
    } // getSSL
    
    /**
     * Gets the maximum number of Http connections allowed in the backend. It is protected due to it is only required
     * for testing purposes.
     * @return The maximum number of Http connections allowed in the backend
     */
    protected int getBackendMaxConns() {
        return backendMaxConns;
    } // getBackendMaxConns
    
    /**
     * Gets the maximum number of Http connections per route allowed in the backend. It is protected due to it is only
     * required for testing purposes.
     * @return The maximum number of Http connections per route allowed in the backend
     */
    protected int getBackendMaxConnsPerRoute() {
        return backendMaxConnsPerRoute;
    } // getBackendMaxConnsPerRoute
    
    /**
     * Gets the CKAN Viewer. It is protected for testing purposes.
     * @return The CKAN viewer
     */
    protected String getCKANViewer() {
        return ckanViewer;
    } // getCKANViewer

    @Override
    public void configure(Context context) {
        apiKey = context.getString("api_key", "nokey");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (api_key=" + apiKey + ")");
        ckanHost = context.getString("ckan_host", "localhost");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (ckan_host=" + ckanHost + ")");
        ckanPort = context.getString("ckan_port", "80");
        int intPort = Integer.parseInt(ckanPort);

        if ((intPort <= 0) || (intPort > 65535)) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (ckan_port=" + ckanPort + ")"
                    + " -- Must be between 0 and 65535");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (ckan_port=" + ckanPort + ")");
        }  // if else

        orionUrl = context.getString("orion_url", "http://localhost:1026");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (orion_url=" + orionUrl + ")");
        String attrPersistenceStr = context.getString("attr_persistence", "row");
        
        if (attrPersistenceStr.equals("row") || attrPersistenceStr.equals("column")) {
            rowAttrPersistence = attrPersistenceStr.equals("row");
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_persistence="
                + attrPersistenceStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (attr_persistence="
                + attrPersistenceStr + ") -- Must be 'row' or 'column'");
        }  // if else

        String sslStr = context.getString("ssl", "false");
        
        if (sslStr.equals("true") || sslStr.equals("false")) {
            ssl = Boolean.valueOf(sslStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (ssl="
                + sslStr + ")");
        } else  {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (ssl="
                + sslStr + ") -- Must be 'true' or 'false'");
        }  // if else
        
        backendMaxConns = context.getInteger("backend.max_conns", 500);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (backend.max_conns=" + backendMaxConns + ")");
        backendMaxConnsPerRoute = context.getInteger("backend.max_conns_per_route", 100);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (backend.max_conns_per_route="
                + backendMaxConnsPerRoute + ")");
        ckanViewer = context.getString("ckan_viewer", "recline_grid_view");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (ckan_viewer=" + ckanViewer + ")");

        super.configure(context);
        // CKAN requires all the names written in lower case
        enableLowercase = true;
    } // configure

    @Override
    public void start() {
        try {
            persistenceBackend = new CKANBackendImpl(apiKey, ckanHost, ckanPort, orionUrl, ssl, backendMaxConns,
                    backendMaxConnsPerRoute, ckanViewer);
            LOGGER.debug("[" + this.getName() + "] CKAN persistence backend created");
        } catch (Exception e) {
            LOGGER.error("Error while creating the CKAN persistence backend. Details="
                    + e.getMessage());
        } // try catch

        super.start();
    } // start

    /**
     * Class for aggregating fieldValues.
     */
    private abstract class CKANAggregator {
        /**
         * The Aggregation of processed entityes.
         */
        protected LinkedHashMap<String, ArrayList<JsonElement>> aggregation;

        // string containing the data fieldValues
        protected String service;
        protected String entityForNaming;
        protected String entityTypeForNaming;
        protected String attributeForNaming;
        protected String attribute;
        protected String orgName;
        protected String pkgName;
        protected String resName;

        /**
         * Gets aggregation.
         *
         * @return the aggregation
         */
        public LinkedHashMap<String, ArrayList<JsonElement>> getAggregation() {
            if (aggregation == null) {
                return new LinkedHashMap<>();
            } else {
                return aggregation;
            }
        } //getAggregation

        /**
         * Gets aggregation to persist. This means that the returned aggregation will not have metadata
         * in case that attrMetadataStore is set to false. Also, added fields for processing purposes
         * will be removed from the aggregation (like attrType on Column mode).
         *
         * @return the aggregation to persist
         */
        public LinkedHashMap<String, ArrayList<JsonElement>> getAggregationToPersist() {
            if (aggregation == null) {
                return new LinkedHashMap<>();
            } else {
                return linkedHashMapWithoutDefaultFields(aggregation);
            }
        } //getAggregationToPersist

        /**
         * Sets aggregation.
         *
         * @param aggregation the aggregation
         */
        public void setAggregation(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
            this.aggregation = aggregation;
        } //setAggregation

        public void setService(String service) {
            this.service = service;
        }

        public void setEntityForNaming(String entityForNaming) {
            this.entityForNaming = entityForNaming;
        }

        public void setEntityTypeForNaming(String entityTypeForNaming) {
            this.entityTypeForNaming = entityTypeForNaming;
        }
        /**
         * Gets attribute.
         *
         * @return the attribute
         */
        public String getAttribute() {
            return attribute;
        } //getAttribute

        /**
         * Sets attribute.
         *
         * @param attribute the attribute
         */
        public void setAttribute(String attribute) {
            this.attribute = attribute;
        } //setAttribute

        public void setAttributeForNaming(String attributeForNaming) {
            this.attributeForNaming = attributeForNaming;
        }

        public void setOrgName(String orgName) {
            this.orgName = orgName;
        }

        public void setPkgName(String pkgName) {
            this.pkgName = pkgName;
        }

        public void setResName(String resName) {
            this.resName = resName;
        }




        protected String tableName;
        protected String typedFieldNames;
        protected String fieldNames;


        public String getOrgName (boolean enableLowercase) {
            if (enableLowercase) {
                return orgName.toLowerCase();
            } else {
                return orgName;
            } // if else
        } //

        public String getPkgName (boolean enableLowercase) {
            if (enableLowercase) {
                return pkgName.toLowerCase();
            } else {
                return pkgName;
            } // if else
        } //

        public String getResName (boolean enableLowercase) {
            if (enableLowercase) {
                return resName.toLowerCase();
            } else {
                return resName;
            } // if else
        } //

        public String getTableName(boolean enableLowercase) {
            if (enableLowercase) {
                return tableName.toLowerCase();
            } else {
                return tableName;
            } // if else
        } // getTableName

        public String getTypedFieldNames() {
            return typedFieldNames;
        } // getTypedFieldNames

        public String getFieldNames() {
            return fieldNames;
        } // getFieldNames

        public void initialize(NGSILDEvent event) throws CygnusBadConfiguration {
            service = event.getServiceForNaming();
            entityForNaming = event.getEntityForNaming(enableGrouping, enableEncoding);
            entityTypeForNaming = event.getEntityTypeForNaming(enableEncoding);
            attributeForNaming = event.getAttributeForNaming();

        } // initialize

        public abstract void aggregate(NGSILDEvent cygnusEvent);

    } // CKANAggregator

    /**
     * Class for aggregating batches in row mode.
    */
    private class RowAggregator extends NGSICKANSink.CKANAggregator {

        @Override
        public void initialize(NGSILDEvent cygnusEvent) throws CygnusBadConfiguration {
            super.initialize(cygnusEvent);
            typedFieldNames = "("
                    + NGSIConstants.RECV_TIME_TS + " bigint,"
                    + NGSIConstants.RECV_TIME + " text,"
                    + NGSIConstants.FIWARE_SERVICE_PATH + " text,"
                    + NGSIConstants.ENTITY_ID + " text,"
                    + NGSIConstants.ENTITY_TYPE + " text,"
                    + NGSIConstants.ATTR_NAME + " text,"
                    + NGSIConstants.ATTR_TYPE + " text,"
                    + NGSIConstants.ATTR_VALUE + " text,"
                    + NGSIConstants.ATTR_MD + " text"
                    + ")";
            fieldNames = "("
                    + NGSIConstants.RECV_TIME_TS + ","
                    + NGSIConstants.RECV_TIME + ","
                    + NGSIConstants.FIWARE_SERVICE_PATH + ","
                    + NGSIConstants.ENTITY_ID + ","
                    + NGSIConstants.ENTITY_TYPE + ","
                    + NGSIConstants.ATTR_NAME + ","
                    + NGSIConstants.ATTR_TYPE + ","
                    + NGSIConstants.ATTR_VALUE + ","
                    + NGSIConstants.ATTR_MD
                    + ")";
        } // initialize

        @Override
        public void aggregate(NGSILDEvent event) {
            // get the getRecvTimeTs headers
            long recvTimeTs = event.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the getRecvTimeTs body
            NotifyContextRequestLD.ContextElement contextElement = event.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

        } // aggregate

    } // RowAggregator

    /**
     * Class for aggregating batches in column mode.
    */
    private class ColumnAggregator extends NGSICKANSink.CKANAggregator {

        @Override
        public void initialize(NGSILDEvent cygnusEvent) throws CygnusBadConfiguration {
            super.initialize(cygnusEvent);
            LinkedHashMap<String, ArrayList<JsonElement>> aggregation = getAggregation();
            aggregation.put(NGSIConstants.RECV_TIME_TS+"C", new ArrayList<JsonElement>());
            aggregation.put(NGSIConstants.RECV_TIME, new ArrayList<JsonElement>());
            aggregation.put(NGSIConstants.ENTITY_ID, new ArrayList<JsonElement>());
            aggregation.put(NGSIConstants.ENTITY_TYPE, new ArrayList<JsonElement>());

            // iterate on all this context element attributes, if there are attributes
            Map<String, Object> contextAttributes = cygnusEvent.getContextElement().getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                return;
            } // if

            // iterate on all this context element attributes, if there are attributes
            for (Map.Entry<String, Object> entry : contextAttributes.entrySet()) {
                {
                    String x = entry.getKey();
                    String attrName = x;
                    String subAttrName = "";
                    aggregation.put(attrName, new ArrayList<JsonElement>());
                    aggregation.put(attrName + NGSIConstants.AUTOGENERATED_ATTR_TYPE, new ArrayList<JsonElement>());
                    if (isValid(entry.getValue().toString())) {
                        JsonObject y = (JsonObject) entry.getValue();
                        for (Map.Entry<String, JsonElement> entry2 : y.entrySet()) {
                            String x2 = entry2.getKey();
                            if (!"type".contentEquals(x2) && !"value".contentEquals(x2)
                                    && !"object".contentEquals(x2)) {
                                subAttrName = x2;
                                aggregation.put(attrName + "_" + subAttrName, new ArrayList<JsonElement>());
                                aggregation.put(attrName + "_" + subAttrName+ NGSIConstants.AUTOGENERATED_ATTR_TYPE, new ArrayList<JsonElement>());
                            }
                        }
                    }
                }
            }
            setAggregation(aggregation);
    } // initialize

        @Override
        public void aggregate(NGSILDEvent cygnusEvent) {
            // get the getRecvTimeTs headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the getRecvTimeTs body
            NotifyContextRequestLD.ContextElement contextElement = cygnusEvent.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

            // iterate on all this context element attributes, if there are attributes
            Map<String, Object> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            LinkedHashMap<String, ArrayList<JsonElement>> aggregation = getAggregation();
            aggregation.get(NGSIConstants.RECV_TIME_TS+"C").add(new JsonPrimitive(Long.toString(recvTimeTs)));
            aggregation.get(NGSIConstants.RECV_TIME).add(new JsonPrimitive(recvTime));
            aggregation.get(NGSIConstants.ENTITY_ID).add(new JsonPrimitive(entityId));
            aggregation.get(NGSIConstants.ENTITY_TYPE).add(new JsonPrimitive(entityType));

            for (Map.Entry<String, Object> entry : contextAttributes.entrySet()) {
                String x = entry.getKey();
                String attrName =x;
                JsonElement attrValue = null;
                String attrType ="";
                JsonElement subAttrName= null;
                String subAttrType="";
                if (isValid(entry.getValue().toString())){
                    JsonObject y = (JsonObject) entry.getValue();
                    attrType = y.get("type").getAsString();
                    if ("Relationship".contentEquals(attrType)){
                        attrValue = new JsonPrimitive(y.get("object").getAsString());
                    }else if ("Property".contentEquals(attrType)){
                        attrValue = new JsonPrimitive(y.get("value").getAsString());
                    }else if ("GeoProperty".contentEquals(attrType)){
                        attrValue = new JsonPrimitive(y.get("value").toString());
                    }
                    if (aggregation.containsKey(attrName)) {
                        aggregation.get(attrName).add(attrValue);
                        aggregation.get(attrName + NGSIConstants.AUTOGENERATED_ATTR_TYPE).add(new JsonPrimitive(attrType));                    }
                    for (Map.Entry<String, JsonElement> entry2 : y.entrySet()) {
                        String x2 = entry2.getKey();
                        Object y2 = entry2.getValue();
                        if ("observedAt".contentEquals(x2)){
                            aggregation.get(attrName+"_"+x2).add(attrValue);
                            aggregation.get(attrName+"_"+x2+ NGSIConstants.AUTOGENERATED_ATTR_TYPE).add(new JsonPrimitive(attrType));
                        }
                        if (!"observedAt".contentEquals(x2) && !"type".contentEquals(x2) && !"value".contentEquals(x2) && !"object".contentEquals(x2)) {
                            if (entry2.getValue().isJsonObject()){
                                JsonObject subAttrJson = entry2.getValue().getAsJsonObject();
                                subAttrType = subAttrJson.get("type").getAsString();
                                if ("Relationship".contentEquals(subAttrType)){
                                    subAttrName = new JsonPrimitive(subAttrJson.get("object").getAsString());
                                }else if ("Property".contentEquals(subAttrType)){
                                    subAttrName = new JsonPrimitive(subAttrJson.get("value").getAsString());
                                }else if ("GeoProperty".contentEquals(subAttrType)){
                                    subAttrName = new JsonPrimitive(subAttrJson.get("value").toString());
                                }
                                if (aggregation.containsKey(attrName+"_"+x2)) {
                                    aggregation.get(attrName+"_"+x2).add(subAttrName);
                                    aggregation.get(attrName +"_"+x2+ NGSIConstants.AUTOGENERATED_ATTR_TYPE).add(new JsonPrimitive(subAttrType));
                                }
                            }

                        }
                    }
                }else {
                    attrValue= new JsonPrimitive( entry.getValue().toString());
                    aggregation.get(attrName).add(attrValue);
                }

            }

            // for
            setAggregation(aggregation);

        } // aggregate

    } // ColumnAggregator

    private NGSICKANSink.CKANAggregator getAggregator(boolean rowAttrPersistence) {
        if (rowAttrPersistence) {
            return new NGSICKANSink.RowAggregator();
        } else {
            return new NGSICKANSink.ColumnAggregator();
        } // if else
    } // getAggregator


    @Override
    void persistBatch(NGSILDBatch batch) throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if

        // Iterate on the destinations
        batch.startIterator();
        
        while (batch.hasNext()) {
            String destination = batch.getNextDestination();
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the "
                    + destination + " destination");

            // Get the events within the current sub-batch
            ArrayList<NGSILDEvent> events = batch.getNextEvents();

            // Get the first event, it will give us useful information
            NGSILDEvent firstEvent = events.get(0);
            String service = firstEvent.getServiceForData();
            String entityForNaming = firstEvent.getEntityForNaming(enableGrouping, enableEncoding);

            // Get an aggregator for this entity and initialize it based on the first event
            CKANAggregator aggregator = getAggregator(rowAttrPersistence);
            aggregator.setService(events.get(0).getServiceForNaming());
            aggregator.setEntityForNaming(events.get(0).getEntityForNaming(enableGrouping, enableEncoding));
            aggregator.setEntityTypeForNaming(events.get(0).getEntityTypeForNaming(enableGrouping));
            aggregator.setOrgName(buildOrgName(service));
            aggregator.setPkgName(buildPkgName(service, events.get(0).getContextElement().getId()));
            aggregator.setResName(buildResName(entityForNaming, events.get(0).getContextElement().getId()));
            aggregator.setAttribute(events.get(0).getAttributeForNaming());
            aggregator.initialize(events.get(0));

            for (NGSILDEvent event : events) {
                aggregator.aggregate(event);
            }

            // Persist the aggregation
            persistAggregation(aggregator, service);
            batch.setNextPersisted(true);
        } // while
    } // persistBatch

    @Override
    public void capRecords(NGSILDBatch batch, long maxRecords) throws CygnusCappingError {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if

        // Iterate on the destinations
        batch.startIterator();
        
        while (batch.hasNext()) {
            // Get the events within the current sub-batch
            ArrayList<NGSILDEvent> events = batch.getNextEvents();

            // Get a representative from the current destination sub-batch
            NGSILDEvent event = events.get(0);
            
            // Do the capping
            String service = event.getServiceForNaming();
            String entityForNaming = event.getEntityForNaming(enableGrouping, enableEncoding);

            try {
                String orgName = buildOrgName(service);
                String pkgName = buildPkgName(service, events.get(0).getContextElement().getId());
                String resName = buildResName(entityForNaming, events.get(0).getContextElement().getId());
                LOGGER.info("[" + this.getName() + "] Capping resource (maxRecords=" + maxRecords + ",orgName="
                        + orgName + ", pkgName=" + pkgName + ", resName=" + resName + ")");
                persistenceBackend.capRecords(orgName, pkgName, resName, maxRecords);
            } catch (CygnusBadConfiguration e) {
                throw new CygnusCappingError("Data capping error", "CygnusBadConfiguration", e.getMessage());
            } catch (CygnusRuntimeError e) {
                throw new CygnusCappingError("Data capping error", "CygnusRuntimeError", e.getMessage());
            } catch (CygnusPersistenceError e) {
                throw new CygnusCappingError("Data capping error", "CygnusPersistenceError", e.getMessage());
            } // try catch
        } // while
    } // capRecords

    @Override
    public void expirateRecords(long expirationTime) throws CygnusExpiratingError {
        LOGGER.debug("[" + this.getName() + "] Expirating records (time=" + expirationTime + ")");
        
        try {
            persistenceBackend.expirateRecordsCache(expirationTime);
        } catch (CygnusRuntimeError e) {
            throw new CygnusExpiratingError("Data expiration error", "CygnusRuntimeError", e.getMessage());
        } catch (CygnusPersistenceError e) {
            throw new CygnusExpiratingError("Data expiration error", "CygnusPersistenceError", e.getMessage());
        } // try catch
    } // truncateByTime


    private void persistAggregation(CKANAggregator aggregator, String service)
        throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError {

        ArrayList<JsonObject> jsonObjects = linkedHashMapToJsonListWithOutEmptyMD(aggregator.getAggregationToPersist());
        String aggregation = "";
        for (JsonObject jsonObject : jsonObjects) {
            if (aggregation.isEmpty()) {
                aggregation = jsonObject.toString();
            } else {
                aggregation += "," + jsonObject;
            }
        }

        String orgName = aggregator.getOrgName(enableLowercase);
        String pkgName = aggregator.getPkgName(enableLowercase);
        String resName = aggregator.getResName(enableLowercase);

        LOGGER.info("[" + this.getName() + "] Persisting data at NGSICKANSink (orgName=" + orgName
                + ", pkgName=" + pkgName + ", resName=" + resName + ", data=(" + aggregation + ")");

        ((CKANBackendImpl) persistenceBackend).startTransaction();

        // Do try-catch only for metrics gathering purposes... after that, re-throw
        try {
            if (aggregator instanceof RowAggregator) {
                persistenceBackend.persist(orgName, pkgName, resName, aggregation, true);
            } else {
                persistenceBackend.persist(orgName, pkgName, resName, aggregation, false);
            } // if else
            
            ImmutablePair<Long, Long> bytes = ((CKANBackendImpl) persistenceBackend).finishTransaction();
            serviceMetrics.add(service, "", 0, 0, 0, 0, 0, 0, bytes.left, bytes.right, 0);
        } catch (CygnusBadConfiguration | CygnusRuntimeError | CygnusPersistenceError e) {
            ImmutablePair<Long, Long> bytes = ((CKANBackendImpl) persistenceBackend).finishTransaction();
            serviceMetrics.add(service, "", 0, 0, 0, 0, 0, 0, bytes.left, bytes.right, 0);
            throw e;
        } // catch
    } // persistAggregation

    /**
     * Builds an organization name given a fiwareService. It throws an exception if the naming conventions are violated.
     * @param fiwareService
     * @return Organization name
     * @throws CygnusBadConfiguration
     */
    public String buildOrgName(String fiwareService) throws CygnusBadConfiguration {
        String orgName;

        switch(dataModel) {
            case DMBYENTITYID:
                //FIXME
                //note that if we enable encode() and/or encodeCKAN() in this datamodel we could have problems, although it need to be analyzed in deep
                orgName=fiwareService;
                break;
            case DMBYENTITY:
                if (enableEncoding) {
                    orgName = NGSICharsets.encodeCKAN(fiwareService);
                } else {
                    orgName = NGSIUtils.encode(fiwareService, false, true).toLowerCase(Locale.ENGLISH);
                } // if else

                if (orgName.length() > NGSIConstants.CKAN_MAX_NAME_LEN) {
                    throw new CygnusBadConfiguration("Building organization name '" + orgName + "' and its length is "
                        + "greater than " + NGSIConstants.CKAN_MAX_NAME_LEN);
                } else if (orgName.length() < NGSIConstants.CKAN_MIN_NAME_LEN) {
                    throw new CygnusBadConfiguration("Building organization name '" + orgName + "' and its length is "
                        + "lower than " + NGSIConstants.CKAN_MIN_NAME_LEN);
                } // if else if
                break;
            default:
                throw new CygnusBadConfiguration("Not supported Data Model for CKAN Sink: " + dataModel);
        }

        return orgName;
    } // buildOrgName

    /**
     * Builds a package name given a fiwareService and a fiwareServicePath. It throws an exception if the naming
     * conventions are violated.
     * @param fiwareService
     * @return Package name
     * @throws CygnusBadConfiguration
     */
    public String buildPkgName(String fiwareService, String entityId) throws CygnusBadConfiguration {
        String pkgName;

        switch(dataModel) {
            case DMBYENTITYID:
                //FIXME
                //note that if we enable encode() and/or encodeCKAN() in this datamodel we could have problems, although it need to be analyzed in deep
                pkgName=entityId;
                break;
            case DMBYENTITY:
                if (enableEncoding) {
                    pkgName = NGSICharsets.encodeCKAN(fiwareService);

                } else {
                    pkgName = NGSIUtils.encode(fiwareService, false, true).toLowerCase(Locale.ENGLISH);
                } // if else
                if (pkgName.length() > NGSIConstants.CKAN_MAX_NAME_LEN) {
                    throw new CygnusBadConfiguration("Building package name '" + pkgName + "' and its length is "
                            + "greater than " + NGSIConstants.CKAN_MAX_NAME_LEN);
                } else if (pkgName.length() < NGSIConstants.CKAN_MIN_NAME_LEN) {
                    throw new CygnusBadConfiguration("Building package name '" + pkgName + "' and its length is "
                            + "lower than " + NGSIConstants.CKAN_MIN_NAME_LEN);
                } // if else if
                break;
            default:
                throw new CygnusBadConfiguration("Not supported Data Model for CKAN Sink: " + dataModel);
        }

        return pkgName;
    } // buildPkgName

    /**
     * Builds a resource name given a entity. It throws an exception if the naming conventions are violated.
     * @param entity
     * @return Resource name
     * @throws CygnusBadConfiguration
     */
    public String buildResName(String entity, String entityId) throws CygnusBadConfiguration {
        String resName;
        switch(dataModel) {
            case DMBYENTITYID:
                //FIXME
                //note that if we enable encode() and/or encodeCKAN() in this datamodel we could have problems, although it need to be analyzed in deep
            	resName=entityId;
                break;
            case DMBYENTITY:
                if (enableEncoding) {
                    resName = NGSICharsets.encodeCKAN(entity);
                } else {
                    resName = NGSIUtils.encode(entity, false, true).toLowerCase(Locale.ENGLISH);
                } // if else

                if (resName.length() > NGSIConstants.CKAN_MAX_NAME_LEN) {
                    throw new CygnusBadConfiguration("Building resource name '" + resName + "' and its length is "
                            + "greater than " + NGSIConstants.CKAN_MAX_NAME_LEN);
                } else if (resName.length() < NGSIConstants.CKAN_MIN_NAME_LEN) {
                    throw new CygnusBadConfiguration("Building resource name '" + resName + "' and its length is "
                            + "lower than " + NGSIConstants.CKAN_MIN_NAME_LEN);
                } // if else if
                break;
            default:
                throw new CygnusBadConfiguration("Not supported Data Model for CKAN Sink: " + dataModel);
        }

        return resName;
    } // buildResName


    public boolean isValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Linked hash map to json list with out empty md array list.
     *
     * @param aggregation the aggregation
     * @return an ArrayList of JsonObjects wich contain all attributes on a LinkedHashMap, this method also removes empty medatada fields.
     */
    public static ArrayList<JsonObject> linkedHashMapToJsonListWithOutEmptyMD(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        ArrayList<JsonObject> jsonStrings = new ArrayList<>();
        int numEvents = collectionSizeOnLinkedHashMap(aggregation);
        for (int i = 0; i < numEvents; i++) {
            Iterator<String> it = aggregation.keySet().iterator();
            JsonObject jsonObject = new JsonObject();
            while (it.hasNext()) {
                String entry = (String) it.next();
                ArrayList<JsonElement> values = (ArrayList<JsonElement>) aggregation.get(entry);
                if (values.get(i) != null) {
                    if (entry.contains("_md") || entry.contains("Md")) {
                        if (!values.get(i).toString().contains("[]"))
                            jsonObject.add(entry, values.get(i));
                    } else {
                        jsonObject.add(entry, values.get(i));
                    }
                }
            }
            jsonStrings.add(jsonObject);
        }
        return jsonStrings;
    }

    /**
     * Collection size on linked hash map int.
     *
     * @param aggregation the aggregation
     * @return the number of attributes contained on the aggregation object.
     */
    public static int collectionSizeOnLinkedHashMap(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        ArrayList<ArrayList<JsonElement>> list = new ArrayList<>(aggregation.values());
        return list.get(0).size();
    }

    /**
     * Linked hash map without default fields linked hash map.
     *
     * @param aggregation       the aggregation
     * @return the linked hash map without metadata objects (if attrMetadataStore is set to true)
     * also, removes "_type" and "RECV_TIME_TSC" keys from the object
     */
    public static LinkedHashMap<String, ArrayList<JsonElement>> linkedHashMapWithoutDefaultFields(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        ArrayList<String> keysToCrop = new ArrayList<>();
        Iterator<String> it = aggregation.keySet().iterator();
        while (it.hasNext()) {
            String entry = (String) it.next();
            if ((entry.equals(NGSIConstants.RECV_TIME_TS+"C")|| entry.contains(NGSIConstants.AUTOGENERATED_ATTR_TYPE)))  {
                keysToCrop.add(entry);
            }
        }
        return cropLinkedHashMap(aggregation, keysToCrop);
    }

    /**
     * Crop linked hash map linked hash map.
     *
     * @param aggregation the aggregation
     * @param keysToCrop  the keys to crop
     * @return removes all keys on list keysToCrop from the aggregation object.
     */
    public static LinkedHashMap<String, ArrayList<JsonElement>> cropLinkedHashMap(LinkedHashMap<String, ArrayList<JsonElement>> aggregation, ArrayList<String> keysToCrop) {
        LinkedHashMap<String, ArrayList<JsonElement>> cropedLinkedHashMap = (LinkedHashMap<String, ArrayList<JsonElement>>) aggregation.clone();
        for (String key : keysToCrop) {
            cropedLinkedHashMap.remove(key);
        }
        return cropedLinkedHashMap;
    }


} // NGSICKANSink
