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

import com.telefonica.iot.cygnus.backends.ckan.CKANBackendImpl;
import com.telefonica.iot.cygnus.backends.ckan.CKANBackend;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.util.ArrayList;
import java.util.Locale;
import org.apache.flume.Context;

/**
 *
 * @author fermin
 *
 * CKAN sink for Orion Context Broker.
 *
 */
public class OrionCKANSink extends OrionSink {

    private static final CygnusLogger LOGGER = new CygnusLogger(OrionCKANSink.class);
    private String apiKey;
    private String ckanHost;
    private String ckanPort;
    private String orionUrl;
    private boolean rowAttrPersistence;
    private boolean ssl;
    private CKANBackend persistenceBackend;

    /**
     * Constructor.
     */
    public OrionCKANSink() {
        super();
    } // OrionCKANSink

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
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (ckan_port=" + ckanPort + ")"
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
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (attr_persistence="
                + attrPersistenceStr + ") -- Must be 'row' or 'column'");
        }  // if else

        String sslStr = context.getString("ssl", "false");
        
        if (sslStr.equals("true") || sslStr.equals("false")) {
            ssl = Boolean.valueOf(sslStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (ssl="
                + sslStr + ")");
        } else  {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (ssl="
                + sslStr + ") -- Must be 'true' or 'false'");
        }  // if else
        
        super.configure(context);
        
        // Techdebt: allow this sink to work with all the data models
        dataModel = DataModel.DMBYENTITY;
    
        // CKAN requires all the names written in lower case
        enableLowercase = true;
    } // configure

    @Override
    public void start() {
        try {
            persistenceBackend = new CKANBackendImpl(apiKey, ckanHost, ckanPort, orionUrl, ssl);
            LOGGER.debug("[" + this.getName() + "] CKAN persistence backend created");
        } catch (Exception e) {
            LOGGER.error("Error while creating the CKAN persistence backend. Details="
                    + e.getMessage());
        } // try catch

        super.start();
    } // start

    @Override
    void persistBatch(Batch batch) throws Exception {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if

        // iterate on the destinations, for each one a single create / append will be performed
        for (String destination : batch.getDestinations()) {
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the " + destination
                    + " destination");

            // get the sub-batch for this destination
            ArrayList<CygnusEvent> subBatch = batch.getEvents(destination);

            // get an aggregator for this destination and initialize it
            CKANAggregator aggregator = getAggregator(this.rowAttrPersistence);
            aggregator.initialize(subBatch.get(0));

            for (CygnusEvent cygnusEvent : subBatch) {
                aggregator.aggregate(cygnusEvent);
            } // for

            // persist the aggregation
            persistAggregation(aggregator);
            batch.setPersisted(destination);
        } // for
    } // persistBatch

    /**
     * Class for aggregating fieldValues.
     */
    private abstract class CKANAggregator {

        // string containing the data records
        protected String records;

        protected String service;
        protected String servicePath;
        protected String destination;
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

        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            service = cygnusEvent.getService();
            servicePath = cygnusEvent.getServicePath();
            destination = cygnusEvent.getEntity();
            orgName = buildOrgName(service);
            pkgName = buildPkgName(service, servicePath);
            resName = buildResName(destination);
        } // initialize

        public abstract void aggregate(CygnusEvent cygnusEvent) throws Exception;

    } // CKANAggregator

    /**
     * Class for aggregating batches in row mode.
     */
    private class RowAggregator extends CKANAggregator {

        @Override
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
        } // initialize

        @Override
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = Utils.getHumanReadable(recvTimeTs, true);

            // get the event body
            NotifyContextRequest.ContextElement contextElement = cygnusEvent.getContextElement();
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
                String record = "{\"" + Constants.RECV_TIME_TS + "\": \"" + recvTimeTs / 1000 + "\","
                    + "\"" + Constants.RECV_TIME + "\": \"" + recvTime + "\","
                    + "\"" + Constants.FIWARE_SERVICE_PATH + "\": \"" + servicePath + "\","
                    + "\"" + Constants.ENTITY_ID + "\": \"" + entityId + "\","
                    + "\"" + Constants.ENTITY_TYPE + "\": \"" + entityType + "\","
                    + "\"" + Constants.ATTR_NAME + "\": \"" + attrName + "\","
                    + "\"" + Constants.ATTR_TYPE + "\": \"" + attrType + "\","
                    + "\"" + Constants.ATTR_VALUE + "\": " + attrValue;

                // metadata is an special case, because CKAN doesn't support empty array, e.g. "[ ]"
                // (http://stackoverflow.com/questions/24207065/inserting-empty-arrays-in-json-type-fields-in-datastore)
                if (!attrMetadata.equals(Constants.EMPTY_MD)) {
                    record += ",\"" + Constants.ATTR_MD + "\": " + attrMetadata + "}";
                } else {
                    record += "}";
                } // if else

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
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
        } // initialize

        @Override
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = Utils.getHumanReadable(recvTimeTs, true);

            // get the event body
            NotifyContextRequest.ContextElement contextElement = cygnusEvent.getContextElement();
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

            String record = "{\"" + Constants.RECV_TIME + "\": \"" + recvTime + "\","
                    + "\"" + Constants.FIWARE_SERVICE_PATH + "\": \"" + servicePath + "\","
                    + "\"" + Constants.ENTITY_ID + "\": \"" + entityId + "\","
                    + "\"" + Constants.ENTITY_TYPE + "\": \"" + entityType + "\"";

            for (NotifyContextRequest.ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");

                // create part of the column with the current attribute (a.k.a. a column)
                record += ",\"" + attrName + "\": " + attrValue;

                // metadata is an special case, because CKAN doesn't support empty array, e.g. "[ ]"
                // (http://stackoverflow.com/questions/24207065/inserting-empty-arrays-in-json-type-fields-in-datastore)
                if (!attrMetadata.equals(Constants.EMPTY_MD)) {
                    record += ",\"" + attrName + "_md\": " + attrMetadata;
                } // if
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

    private void persistAggregation(CKANAggregator aggregator) throws Exception {
        String aggregation = aggregator.getAggregation();
        String orgName = aggregator.getOrgName(enableLowercase);
        String pkgName = aggregator.getPkgName(enableLowercase);
        String resName = aggregator.getResName(enableLowercase);

        LOGGER.info("[" + this.getName() + "] Persisting data at OrionCKANSink (orgName=" + orgName
                + ", pkgName=" + pkgName + ", resName=" + resName + ", data=" + aggregation + ")");

        if (aggregator instanceof RowAggregator) {
            persistenceBackend.persist(orgName, pkgName, resName, aggregation, true);
        } else {
            persistenceBackend.persist(orgName, pkgName, resName, aggregation, false);
        } // if else
    } // persistAggregation

    /**
     * Builds an organization name given a fiwareService. It throws an exception if the naming conventions are violated.
     * @param fiwareService
     * @return
     * @throws Exception
     */
    private String buildOrgName(String fiwareService) throws Exception {
        String orgName = Utils.encode(fiwareService);

        if (orgName.length() > Constants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building orgName=fiwareService (" + orgName + ") and its length is "
                    + "greater than " + Constants.MAX_NAME_LEN);
        } // if

        return orgName;
    } // buildOrgName

    /**
     * Builds a package name given a fiwareService and a fiwareServicePath. It throws an exception if the naming
     * conventions are violated.
     * @param fiwareService
     * @param fiwareServicePath
     * @return
     * @throws Exception
     */
    private String buildPkgName(String fiwareService, String fiwareServicePath) throws Exception {
        String pkgName;

        if (fiwareServicePath.equals("/")) {
            pkgName = Utils.encode(fiwareService);
        } else {
            pkgName = Utils.encode(fiwareService) + "_" + Utils.encode(fiwareServicePath);
        } // if else

        if (pkgName.length() > Constants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building pkgName=fiwareService + '_' + fiwareServicePath (" + pkgName
                    + ") and its length is greater than " + Constants.MAX_NAME_LEN);
        } // if

        return pkgName;
    } // buildPkgName

    /**
     * Builds a resource name given a destination. It throws an exception if the naming conventions are violated.
     * @param destination
     * @return
     * @throws Exception
     */
    private String buildResName(String destination) throws Exception {
        String resName = Utils.encode(destination);

        if (resName.length() > Constants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building resName=destination (" + resName + ") and its length is greater "
                    + "than " + Constants.MAX_NAME_LEN);
        } // if

        return resName;
    } // buildResName

} // OrionCKANSink
