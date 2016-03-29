/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 * <p>
 * This file is part of fiware-cygnus (FI-WARE project).
 * <p>
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 * <p>
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.backends.cassandra.CassandraBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import org.apache.flume.Context;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Detailed documentation may soon be found at:
 * https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/design/OrionCassandraSink.md .
 *
 * @author jdegenhardt
 */
class OrionCassandraSink extends OrionSink {

    private static final CygnusLogger LOGGER = new CygnusLogger(OrionCassandraSink.class);
    private String[] cassandraHosts;
    private String cassandraPort;
    private String cassandraKeyspace;
    private String cassandraUsername;
    private String cassandraPassword;
    private boolean rowAttrPersistence;
    private CassandraBackendImpl persistenceBackend;

    /**
     * Constructor.
     */
    OrionCassandraSink() {
        super();
    } // OrionCassandraSink

    /**
     * Gets the Cassandra host(s). It is protected due to it is only required for testing purposes.
     *
     * @return The Cassandra host(s)
     */
    String[] getCassandraHosts() {
        return cassandraHosts;
    } // getCassandraHosts

    /**
     * Gets the Cassandra port. It is protected due to it is only required for testing purposes.
     *
     * @return The Cassandra port
     */
    String getCassandraPort() {
        return cassandraPort;
    } // getCassandraPort

    /**
     * Gets the Cassandra keyspace. It is protected due to it is only required for testing purposes.
     *
     * @return The Cassandra keyspace
     */
    protected String getCassandraKeyspace() {
        return cassandraKeyspace;
    } // getCassandraKeyspace

    /**
     * Gets the Cassandra username. It is protected due to it is only required for testing purposes.
     *
     * @return The Cassandra username
     */
    String getCassandraUsername() {
        return cassandraUsername;
    } // getCassandraUsername

    /**
     * Gets the Cassandra password. It is protected due to it is only required for testing purposes.
     *
     * @return The Cassandra password
     */
    String getCassandraPassword() {
        return cassandraPassword;
    } // getCassandraPassword

    /**
     * Returns if the attribute persistence is row-based. It is protected due to it is only required for testing
     * purposes.
     *
     * @return True if the attribute persistence is row-based, false otherwise
     */
    protected boolean getRowAttrPersistence() {
        return rowAttrPersistence;
    } // getRowAttrPersistence

    /**
     * Returns the persistence backend. It is protected due to it is only required for testing purposes.
     *
     * @return The persistence backend
     */
    protected CassandraBackendImpl getPersistenceBackend() {
        return persistenceBackend;
    } // getPersistenceBackend

    /**
     * Sets the persistence backend. It is protected due to it is only required for testing purposes.
     *
     * @param persistenceBackend the persistence backend that shall be used
     */
    protected void setPersistenceBackend(CassandraBackendImpl persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    } // setPersistenceBackend

    @Override
    public void configure(Context context) {
        cassandraHosts = context.getString("cassandra_host", "localhost").split(";");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (cassandra_hosts="
                + Arrays.toString(cassandraHosts) + ")");
        cassandraPort = context.getString("cassandra_port", "5432");
        int intPort = Integer.parseInt(cassandraPort);

        if ((intPort <= 0) || (intPort > 65535)) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (cassandra_port=" + cassandraPort + ")"
                    + " -- Must be between 0 and 65535");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (cassandra_port=" + cassandraPort + ")");
        }  // if else

        cassandraKeyspace = context.getString("cassandra_database", "postgres");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (cassandra_database=" + cassandraKeyspace + ")");
        cassandraUsername = context.getString("cassandra_username", "cassandra");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (cassandra_username=" + cassandraUsername + ")");
        // FIXME: cassandraPassword should be read as a SHA1 and decoded here
        cassandraPassword = context.getString("cassandra_password", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (cassandra_password=" + cassandraPassword + ")");
        rowAttrPersistence = context.getString("attr_persistence", "row").equals("row");
        String persistence = context.getString("attr_persistence", "row");

        if (persistence.equals("row") || persistence.equals("column")) {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_persistence="
                    + persistence + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (attr_persistence="
                    + persistence + ") -- Must be 'row' or 'column'");
        }  // if else

        super.configure(context);

        // CKAN requires all the names written in lower case
        enableLowercase = true;
    } // configure

    @Override
    public void start() {
        try {
            LOGGER.debug("[" + this.getName() + "] Cassandra persistence backend created");
            persistenceBackend = new CassandraBackendImpl(cassandraUsername, cassandraPassword, cassandraHosts);
        } catch (Exception e) {
            LOGGER.error("Error while creating the Cassandra persistence backend. Details="
                    + e.getMessage());
        } // try catch

        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

    @Override
    void persistBatch(Batch batch) throws Exception {
        // FIXME anpassen
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
            CassandraAggregator aggregator = getAggregator(rowAttrPersistence);
            aggregator.initialize(subBatch.get(0));

            for (CygnusEvent cygnusEvent : subBatch) {
                aggregator.aggregate(cygnusEvent);
            } // for

            // persist the fieldValues
            persistAggregation(aggregator);
            batch.setPersisted(destination);
        } // for
    } // persistBatch

    @org.jetbrains.annotations.Contract("true -> !null; false -> !null")
    private CassandraAggregator getAggregator(boolean rowAttrPersistence) {
        if (rowAttrPersistence) {
            return new RowAggregator();
        } else {
            return new ColumnAggregator();
        } // if else
    } // getAggregator

    private void persistAggregation(CassandraAggregator aggregator) throws Exception {
        String typedFieldNames = aggregator.getTypedFieldNames();
        String fieldNames = aggregator.getFieldNames();
        String fieldValues = aggregator.getAggregation();
        String keyspaceName = aggregator.getKeyspaceName(enableLowercase);
        String tableName = aggregator.getTableName(enableLowercase);

        LOGGER.info("[" + this.getName() + "] Persisting data at OrionCassandraSink. Keyspace ("
                + keyspaceName + "), Table (" + tableName + "), Fields (" + fieldNames + "), Values ("
                + fieldValues + ")");

        // creating the database and the table has only sense if working in row mode, in column node
        // everything must be provisioned in advance
        if (aggregator instanceof RowAggregator) {
            persistenceBackend.createKeyspace(keyspaceName);
            persistenceBackend.createTable(keyspaceName, tableName, typedFieldNames);
        } // if

        persistenceBackend.insertContextData(keyspaceName, tableName, fieldNames, fieldValues);
    } // persistAggregation

    /**
     * Class for aggregating fieldValues.
     */
    private abstract class CassandraAggregator {

        // string containing the data fieldValues
        private String aggregation;

        private String service;
        private String servicePath;
        private String entity;
        private String attribute;
        private String keyspaceName;
        private String tableName;
        private String typedFieldNames;
        private String fieldNames;

        CassandraAggregator() {
            aggregation = "";
        } // CassandraAggregator

        public String getAggregation() {
            return aggregation;
        } // getAggregation

        void addToAggregation(String aggregationAddition) {
            this.aggregation += aggregationAddition;
        } // addToAggregation

        String getServicePath() {
            return servicePath;
        } // getServicePath

        String getKeyspaceName(boolean enableLowercase) {
            if (enableLowercase) {
                return keyspaceName.toLowerCase();
            } else {
                return keyspaceName;
            } // if else
        } // getDbName

        String getTableName(boolean enableLowercase) {
            if (enableLowercase) {
                return tableName.toLowerCase();
            } else {
                return tableName;
            } // if else
        } // getTableName

        String getTypedFieldNames() {
            return typedFieldNames;
        } // getTypedFieldNames

        void setTypedFieldNames(String typedFieldNames) {
            this.typedFieldNames = typedFieldNames;
        } // setTypedFieldNames

        void addToTypedFieldNames(String typedFieldNamesAddition) {
            this.typedFieldNames += typedFieldNamesAddition;
        } // AddTotTypedFieldNames

        public String getFieldNames() {
            return fieldNames;
        } // getFieldNames

        public void setFieldNames(String fieldNames) {
            this.fieldNames = fieldNames;
        } // setFieldNames

        void addToFieldNames(String fieldNamesAddition) {
            this.fieldNames += fieldNamesAddition;
        } // addToFieldNames

        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            service = cygnusEvent.getService();
            servicePath = cygnusEvent.getServicePath();
            entity = cygnusEvent.getEntity();
            attribute = cygnusEvent.getAttribute();
            keyspaceName = buildKeyspaceName();
            tableName = buildTableName();
        } // initialize

        private String buildKeyspaceName() throws Exception {
            String name = service;

            if (name.length() > Constants.MAX_NAME_LEN) {
                throw new CygnusBadConfiguration("Building keyspace name '" + name
                        + "' and its length is greater than " + Constants.MAX_NAME_LEN);
            } // if

            return name;
        } // buildKeyspaceName

        private String buildTableName() throws Exception {
            String name;

            //noinspection Duplicates
            switch (dataModel) {
                case DMBYSERVICEPATH:
                    name = servicePath;
                    break;
                case DMBYENTITY:
                    name = servicePath + '_' + entity;
                    break;
                case DMBYATTRIBUTE:
                    name = servicePath + '_' + entity + '_' + attribute;
                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use DMBYSERVICEPATH, DMBYENTITY or DMBYATTRIBUTE");
            } // switch

            if (name.length() > Constants.MAX_NAME_LEN) {
                throw new CygnusBadConfiguration("Building table name '" + name
                        + "' and its length is greater than " + Constants.MAX_NAME_LEN);
            } // if

            return name;
        } // buildTableName

        public abstract void aggregate(CygnusEvent cygnusEvent) throws Exception;

    } // CassandraAggregator

    /**
     * Class for aggregating batches in row mode.
     */
    private class RowAggregator extends CassandraAggregator {

        @Override
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
            setTypedFieldNames("("
                    + Constants.RECV_TIME_TS + " bigint,"
                    + Constants.RECV_TIME + " text,"
                    + Constants.FIWARE_SERVICE_PATH + " text,"
                    + Constants.ENTITY_ID + " text,"
                    + Constants.ENTITY_TYPE + " text,"
                    + Constants.ATTR_NAME + " text,"
                    + Constants.ATTR_TYPE + " text,"
                    + Constants.ATTR_VALUE + " text,"
                    + Constants.ATTR_MD + " text"
                    + ")");
            setFieldNames("("
                    + Constants.RECV_TIME_TS + ","
                    + Constants.RECV_TIME + ","
                    + Constants.FIWARE_SERVICE_PATH + ","
                    + Constants.ENTITY_ID + ","
                    + Constants.ENTITY_TYPE + ","
                    + Constants.ATTR_NAME + ","
                    + Constants.ATTR_TYPE + ","
                    + Constants.ATTR_VALUE + ","
                    + Constants.ATTR_MD
                    + ")");
        } // initialize

        @Override
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = Utils.getHumanReadable(recvTimeTs, true);

            // get the event body
            ContextElement contextElement = cygnusEvent.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");

                // create a column and aggregate it
                String row = "('"
                        + recvTimeTs + "','"
                        + recvTime + "','"
                        + getServicePath() + "','"
                        + entityId + "','"
                        + entityType + "','"
                        + attrName + "','"
                        + attrType + "','"
                        + attrValue + "','"
                        + attrMetadata
                        + "')";

                if (getAggregation().isEmpty()) {
                    addToAggregation(row);
                } else {
                    addToAggregation("," + row);
                } // if else
            } // for
        } // aggregate

    } // RowAggregator

    /**
     * Class for aggregating batches in column mode.
     */
    private class ColumnAggregator extends CassandraAggregator {

        @Override
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);

            // particulat initialization
            setTypedFieldNames("(" + Constants.RECV_TIME + " text,"
                    + Constants.FIWARE_SERVICE_PATH + " text,"
                    + Constants.ENTITY_ID + " text,"
                    + Constants.ENTITY_TYPE + " text");
            setFieldNames("(" + Constants.RECV_TIME + ","
                    + Constants.FIWARE_SERVICE_PATH + ","
                    + Constants.ENTITY_ID + ","
                    + Constants.ENTITY_TYPE);

            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = cygnusEvent.getContextElement().getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                return;
            } // if

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                addToTypedFieldNames("," + attrName + " text," + attrName + "_md text");
                addToFieldNames("," + attrName + "," + attrName + "_md");
            } // for

            addToTypedFieldNames(")");
            addToFieldNames(")");
        } // initialize

        @Override
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = Utils.getHumanReadable(recvTimeTs, true);

            // get the event body
            ContextElement contextElement = cygnusEvent.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            String column = "('" + recvTime + "','" + getServicePath() + "','" + entityId + "','" + entityType + "'";

            //noinspection Duplicates
            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");

                // create part of the column with the current attribute (a.k.a. a column)
                column += ",'" + attrValue + "','" + attrMetadata + "'";
            } // for

            // now, aggregate the column
            if (getAggregation().isEmpty()) {
                addToAggregation(column + ")");
            } else {
                addToAggregation("," + column + ")");
            } // if else
        } // aggregate

    } // ColumnAggregator

} // OrionCassandraSink
