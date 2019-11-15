/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
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

import com.telefonica.iot.cygnus.backends.ckan.CKANBackendImpl;
import com.telefonica.iot.cygnus.backends.ckan.CKANBackend;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.Enums.DataModel;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.utils.NGSICharsets;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import java.util.ArrayList;
import java.util.Locale;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.flume.Context;

/**
 *
 * @author fermin
 *
 * CKAN sink for Orion Context Broker.
 *
 */
public class NGSICKANSink extends NGSISink {

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
        
        // Techdebt: allow this sink to work with all the data models
        dataModel = DataModel.DMBYENTITY;
    
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

    @Override
    void persistBatch(NGSIBatch batch) throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError {
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
            ArrayList<NGSIEvent> events = batch.getNextEvents();
            
            // Get the first event, it will give us useful information
            NGSIEvent firstEvent = events.get(0);
            String service = firstEvent.getServiceForData();
            String servicePath = firstEvent.getServicePathForData();

            // Get an aggregator for this entity and initialize it based on the first event
            CKANAggregator aggregator = getAggregator(this.rowAttrPersistence);
            aggregator.initialize(firstEvent);

            for (NGSIEvent event : events) {
                aggregator.aggregate(event);
            } // for

            // Persist the aggregation
            persistAggregation(aggregator, service, servicePath);
            batch.setNextPersisted(true);
        } // while
    } // persistBatch

    @Override
    public void capRecords(NGSIBatch batch, long maxRecords) throws CygnusCappingError {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if

        // Iterate on the destinations
        batch.startIterator();
        
        while (batch.hasNext()) {
            // Get the events within the current sub-batch
            ArrayList<NGSIEvent> events = batch.getNextEvents();

            // Get a representative from the current destination sub-batch
            NGSIEvent event = events.get(0);
            
            // Do the capping
            String service = event.getServiceForNaming(enableNameMappings);
            String servicePathForNaming = event.getServicePathForNaming(enableGrouping, enableNameMappings);
            String entityForNaming = event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding);

            try {
                String orgName = buildOrgName(service);
                String pkgName = buildPkgName(service, servicePathForNaming);
                String resName = buildResName(entityForNaming);
                LOGGER.debug("[" + this.getName() + "] Capping resource (maxRecords=" + maxRecords + ",orgName="
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

    /**
     * Class for aggregating fieldValues.
     */
    private abstract class CKANAggregator {

        // string containing the data records
        protected String records;

        protected String service;
        protected String servicePathForData;
        protected String servicePathForNaming;
        protected String entityForNaming;
        protected String orgName;
        protected String pkgName;
        protected String resName;
        protected String resId;

        public CKANAggregator() {
            records = "";
        } // CKANAggregator

        public String getAggregation() {
            return records;
        } // getAggregation

        public String getOrgName(boolean enableLowercase) {
            if (enableLowercase) {
                return orgName.toLowerCase();
            } else {
                return orgName;
            } // if else
        } // getOrgName

        public String getPkgName(boolean enableLowercase) {
            if (enableLowercase) {
                return pkgName.toLowerCase();
            } else {
                return pkgName;
            } // if else
        } // getPkgName

        public String getResName(boolean enableLowercase) {
            if (enableLowercase) {
                return resName.toLowerCase();
            } else {
                return resName;
            } // if else
        } // getResName

        public void initialize(NGSIEvent event) throws CygnusBadConfiguration {
            service = event.getServiceForNaming(enableNameMappings);
            servicePathForData = event.getServicePathForData();
            servicePathForNaming = event.getServicePathForNaming(enableGrouping, enableNameMappings);
            entityForNaming = event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding);
            orgName = buildOrgName(service);
            pkgName = buildPkgName(service, servicePathForNaming);
            resName = buildResName(entityForNaming);
        } // initialize

        public abstract void aggregate(NGSIEvent cygnusEvent);

    } // CKANAggregator

    /**
     * Class for aggregating batches in row mode.
     */
    private class RowAggregator extends CKANAggregator {

        @Override
        public void initialize(NGSIEvent event) throws CygnusBadConfiguration {
            super.initialize(event);
        } // initialize

        @Override
        public void aggregate(NGSIEvent event) {
            // get the getRecvTimeTs headers
            long recvTimeTs = event.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the getRecvTimeTs body
            NotifyContextRequest.ContextElement contextElement = event.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

            // iterate on all this context element attributes, if there are attributes
            ArrayList<NotifyContextRequest.ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            for (NotifyContextRequest.ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");

                // create a column and aggregate it
                String record = "{\"" + NGSIConstants.RECV_TIME_TS + "\": \"" + recvTimeTs / 1000 + "\","
                    + "\"" + NGSIConstants.RECV_TIME + "\": \"" + recvTime + "\","
                    + "\"" + NGSIConstants.FIWARE_SERVICE_PATH + "\": \"" + servicePathForData + "\","
                    + "\"" + NGSIConstants.ENTITY_ID + "\": \"" + entityId + "\","
                    + "\"" + NGSIConstants.ENTITY_TYPE + "\": \"" + entityType + "\","
                    + "\"" + NGSIConstants.ATTR_NAME + "\": \"" + attrName + "\","
                    + "\"" + NGSIConstants.ATTR_TYPE + "\": \"" + attrType + "\""
                    + (isSpecialValue(attrValue) ? "" : ",\"" + NGSIConstants.ATTR_VALUE + "\": " + attrValue)
                    + (isSpecialMetadata(attrMetadata) ? "" : ",\"" + NGSIConstants.ATTR_MD + "\": " + attrMetadata)
                    + "}";

                if (records.isEmpty()) {
                    records += record;
                } else {
                    records += "," + record;
                } // if else
            } // for
        } // aggregate

    } // RowAggregator

    /**
     * Class for aggregating batches in column mode.
     */
    private class ColumnAggregator extends CKANAggregator {

        @Override
        public void initialize(NGSIEvent event) throws CygnusBadConfiguration {
            super.initialize(event);
        } // initialize

        @Override
        public void aggregate(NGSIEvent event) {
            // get the getRecvTimeTs headers
            long recvTimeTs = event.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the getRecvTimeTs body
            NotifyContextRequest.ContextElement contextElement = event.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

            // iterate on all this context element attributes, if there are attributes
            ArrayList<NotifyContextRequest.ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            String record = "{\"" + NGSIConstants.RECV_TIME + "\": \"" + recvTime + "\","
                    + "\"" + NGSIConstants.FIWARE_SERVICE_PATH + "\": \"" + servicePathForData + "\","
                    + "\"" + NGSIConstants.ENTITY_ID + "\": \"" + entityId + "\","
                    + "\"" + NGSIConstants.ENTITY_TYPE + "\": \"" + entityType + "\"";

            for (NotifyContextRequest.ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");

                // create part of the column with the current attribute (a.k.a. a column)
                record += (isSpecialValue(attrValue) ? "" : ",\"" + attrName + "\": " + attrValue)
                        + (isSpecialMetadata(attrMetadata) ? "" : ",\"" + attrName + "_md\": " + attrMetadata);
            } // for

            // now, aggregate the column
            if (records.isEmpty()) {
                records += record + "}";
            } else {
                records += "," + record + "}";
            } // if else
        } // aggregate

    } // ColumnAggregator

    private CKANAggregator getAggregator(boolean rowAttrPersistence) {
        if (rowAttrPersistence) {
            return new RowAggregator();
        } else {
            return new ColumnAggregator();
        } // if else
    } // getAggregator

    private void persistAggregation(CKANAggregator aggregator, String service, String servicePath)
        throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError {
        String aggregation = aggregator.getAggregation();
        String orgName = aggregator.getOrgName(enableLowercase);
        String pkgName = aggregator.getPkgName(enableLowercase);
        String resName = aggregator.getResName(enableLowercase);

        LOGGER.info("[" + this.getName() + "] Persisting data at NGSICKANSink (orgName=" + orgName
                + ", pkgName=" + pkgName + ", resName=" + resName + ", data=" + aggregation + ")");

        ((CKANBackendImpl) persistenceBackend).startTransaction();
        
        // Do try-catch only for metrics gathering purposes... after that, re-throw
        try {
            if (aggregator instanceof RowAggregator) {
                persistenceBackend.persist(orgName, pkgName, resName, aggregation, true);
            } else {
                persistenceBackend.persist(orgName, pkgName, resName, aggregation, false);
            } // if else
            
            ImmutablePair<Long, Long> bytes = ((CKANBackendImpl) persistenceBackend).finishTransaction();
            serviceMetrics.add(service, servicePath, 0, 0, 0, 0, 0, 0, bytes.left, bytes.right, 0);
        } catch (CygnusBadConfiguration | CygnusRuntimeError | CygnusPersistenceError e) {
            ImmutablePair<Long, Long> bytes = ((CKANBackendImpl) persistenceBackend).finishTransaction();
            serviceMetrics.add(service, servicePath, 0, 0, 0, 0, 0, 0, bytes.left, bytes.right, 0);
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
            
        return orgName;
    } // buildOrgName

    /**
     * Builds a package name given a fiwareService and a fiwareServicePath. It throws an exception if the naming
     * conventions are violated.
     * @param fiwareService
     * @param fiwareServicePath
     * @return Package name
     * @throws CygnusBadConfiguration
     */
    public String buildPkgName(String fiwareService, String fiwareServicePath) throws CygnusBadConfiguration {
        String pkgName;
        
        if (enableEncoding) {
            pkgName = NGSICharsets.encodeCKAN(fiwareService)
                    + CommonConstants.CONCATENATOR
                    + NGSICharsets.encodeCKAN(fiwareServicePath);
        } else {
            if (fiwareServicePath.equals("/")) {
                pkgName = NGSIUtils.encode(fiwareService, false, true).toLowerCase(Locale.ENGLISH);
            } else {
                pkgName = (NGSIUtils.encode(fiwareService, false, true)
                        + NGSIUtils.encode(fiwareServicePath, false, true)).toLowerCase(Locale.ENGLISH);
            } // if else
        } // if else

        if (pkgName.length() > NGSIConstants.CKAN_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building package name '" + pkgName + "' and its length is "
                    + "greater than " + NGSIConstants.CKAN_MAX_NAME_LEN);
        } else if (pkgName.length() < NGSIConstants.CKAN_MIN_NAME_LEN) {
            throw new CygnusBadConfiguration("Building package name '" + pkgName + "' and its length is "
                    + "lower than " + NGSIConstants.CKAN_MIN_NAME_LEN);
        } // if else if

        return pkgName;
    } // buildPkgName

    /**
     * Builds a resource name given a entity. It throws an exception if the naming conventions are violated.
     * @param entity
     * @return Resource name
     * @throws CygnusBadConfiguration
     */
    public String buildResName(String entity) throws CygnusBadConfiguration {
        String resName;
        
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

        return resName;
    } // buildResName
    
    private boolean isSpecialValue(String value) {
        return value == null || value.equals(("\"\"")) || value.equals("{}") || value.equals("[]");
    } // isSpecialValue
    
    private boolean isSpecialMetadata(String value) {
        return value == null || value.equals("[]");
    } // isSpecialMetadata

} // NGSICKANSink
