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

import com.telefonica.iot.cygnus.backends.postgresql.PostgreSQLBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.util.ArrayList;
import org.apache.flume.Context;

/**
 *
 * @author hermanjunge
 *
 * Detailed documentation can be found at:
 * https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/design/OrionPostgreSQLSink.md
 */
public class OrionPostgreSQLSink extends OrionSink {

    private static final CygnusLogger LOGGER = new CygnusLogger(OrionPostgreSQLSink.class);
    private String postgresqlHost;
    private String postgresqlPort;
    private String postgresqlDatabase;
    private String postgresqlUsername;
    private String postgresqlPassword;
    private boolean rowAttrPersistence;
    private PostgreSQLBackendImpl persistenceBackend;

    /**
     * Constructor.
     */
    public OrionPostgreSQLSink() {
        super();
    } // OrionPostgreSQLSink

    /**
     * Gets the PostgreSQL host. It is protected due to it is only required for testing purposes.
     * @return The PostgreSQL host
     */
    protected String getPostgreSQLHost() {
        return postgresqlHost;
    } // getPostgreSQLHost

    /**
     * Gets the PostgreSQL port. It is protected due to it is only required for testing purposes.
     * @return The PostgreSQL port
     */
    protected String getPostgreSQLPort() {
        return postgresqlPort;
    } // getPostgreSQLPort

    /**
     * Gets the PostgreSQL database. It is protected due to it is only required for testing purposes.
     * @return The PostgreSQL database
     */
    protected String getPostgreSQLDatabase() {
        return postgresqlDatabase;
    } // getPostgreSQLDatabase

    /**
     * Gets the PostgreSQL username. It is protected due to it is only required for testing purposes.
     * @return The PostgreSQL username
     */
    protected String getPostgreSQLUsername() {
        return postgresqlUsername;
    } // getPostgreSQLUsername

    /**
     * Gets the PostgreSQL password. It is protected due to it is only required for testing purposes.
     * @return The PostgreSQL password
     */
    protected String getPostgreSQLPassword() {
        return postgresqlPassword;
    } // getPostgreSQLPassword

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
    protected PostgreSQLBackendImpl getPersistenceBackend() {
        return persistenceBackend;
    } // getPersistenceBackend

    /**
     * Sets the persistence backend. It is protected due to it is only required for testing purposes.
     * @param persistenceBackend
     */
    protected void setPersistenceBackend(PostgreSQLBackendImpl persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    } // setPersistenceBackend

    @Override
    public void configure(Context context) {
        postgresqlHost = context.getString("postgresql_host", "localhost");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgresql_host=" + postgresqlHost + ")");
        postgresqlPort = context.getString("postgresql_port", "3306");
        int intPort = Integer.parseInt(postgresqlPort);

        if ((intPort <= 0) || (intPort > 65535)) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (postgresql_port=" + postgresqlPort + ")"
                    + " -- Must be between 0 and 65535");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (postgresql_port=" + postgresqlPort + ")");
        }  // if else

        postgresqlDatabase = context.getString("postgresql_database", "postgres");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgresql_database=" + postgresqlDatabase + ")");
        postgresqlUsername = context.getString("postgresql_username", "opendata");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgresql_username=" + postgresqlUsername + ")");
        // FIXME: postgresqlPassword should be read as a SHA1 and decoded here
        postgresqlPassword = context.getString("postgresql_password", "unknown");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (postgresql_password=" + postgresqlPassword + ")");
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
            LOGGER.debug("[" + this.getName() + "] PostgreSQL persistence backend created");
            persistenceBackend = new PostgreSQLBackendImpl(postgresqlHost, postgresqlPort, postgresqlDatabase,
                    postgresqlUsername, postgresqlPassword);
        } catch (Exception e) {
            LOGGER.error("Error while creating the PostgreSQL persistence backend. Details="
                    + e.getMessage());
        } // try catch

        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
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
            PostgreSQLAggregator aggregator = getAggregator(rowAttrPersistence);
            aggregator.initialize(subBatch.get(0));

            for (CygnusEvent cygnusEvent : subBatch) {
                aggregator.aggregate(cygnusEvent);
            } // for

            // persist the fieldValues
            persistAggregation(aggregator);
            batch.setPersisted(destination);
        } // for
    } // persistBatch

    /**
     * Class for aggregating fieldValues.
     */
    private abstract class PostgreSQLAggregator {

        // string containing the data fieldValues
        protected String aggregation;

        protected String service;
        protected String servicePath;
        protected String entity;
        protected String attribute;
        protected String schemaName;
        protected String tableName;
        protected String typedFieldNames;
        protected String fieldNames;

        public PostgreSQLAggregator() {
            aggregation = "";
        } // PostgreSQLAggregator

        public String getAggregation() {
            return aggregation;
        } // getAggregation

        public String getSchemaName(boolean enableLowercase) {
            if (enableLowercase) {
                return schemaName.toLowerCase();
            } else {
                return schemaName;
            } // if else
        } // getDbName

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

        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            service = cygnusEvent.getService();
            servicePath = cygnusEvent.getServicePath();
            entity = cygnusEvent.getEntity();
            attribute = cygnusEvent.getAttribute();
            schemaName = buildSchemaName();
            tableName = buildTableName();
        } // initialize

        private String buildSchemaName() throws Exception {
            String name = service;

            if (name.length() > Constants.MAX_NAME_LEN) {
                throw new CygnusBadConfiguration("Building schema name '" + name
                        + "' and its length is greater than " + Constants.MAX_NAME_LEN);
            } // if

            return name;
        } // buildSchemaName

        private String buildTableName() throws Exception {
            String name;

            switch(dataModel) {
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

    } // PostgreSQLAggregator

    /**
     * Class for aggregating batches in row mode.
     */
    private class RowAggregator extends PostgreSQLAggregator {

        @Override
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
            typedFieldNames = "("
                    + Constants.RECV_TIME_TS + " bigint,"
                    + Constants.RECV_TIME + " text,"
                    + Constants.FIWARE_SERVICE_PATH + " text,"
                    + Constants.ENTITY_ID + " text,"
                    + Constants.ENTITY_TYPE + " text,"
                    + Constants.ATTR_NAME + " text,"
                    + Constants.ATTR_TYPE + " text,"
                    + Constants.ATTR_VALUE + " text,"
                    + Constants.ATTR_MD + " text"
                    + ")";
            fieldNames = "("
                    + Constants.RECV_TIME_TS + ","
                    + Constants.RECV_TIME + ","
                    + Constants.FIWARE_SERVICE_PATH + ","
                    + Constants.ENTITY_ID + ","
                    + Constants.ENTITY_TYPE + ","
                    + Constants.ATTR_NAME + ","
                    + Constants.ATTR_TYPE + ","
                    + Constants.ATTR_VALUE + ","
                    + Constants.ATTR_MD
                    + ")";
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
                    + servicePath + "','"
                    + entityId + "','"
                    + entityType + "','"
                    + attrName + "','"
                    + attrType + "','"
                    + attrValue + "','"
                    + attrMetadata
                    + "')";

                if (aggregation.isEmpty()) {
                    aggregation += row;
                } else {
                    aggregation += "," + row;
                } // if else
            } // for
        } // aggregate

    } // RowAggregator

    /**
     * Class for aggregating batches in column mode.
     */
    private class ColumnAggregator extends PostgreSQLAggregator {

        @Override
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);

            // particulat initialization
            typedFieldNames = "(" + Constants.RECV_TIME + " text,"
                    + Constants.FIWARE_SERVICE_PATH + " text,"
                    + Constants.ENTITY_ID + " text,"
                    + Constants.ENTITY_TYPE + " text";
            fieldNames = "(" + Constants.RECV_TIME + ","
                    + Constants.FIWARE_SERVICE_PATH + ","
                    + Constants.ENTITY_ID + ","
                    + Constants.ENTITY_TYPE;

            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = cygnusEvent.getContextElement().getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                return;
            } // if

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                typedFieldNames += "," + attrName + " text," + attrName + "_md text";
                fieldNames += "," + attrName + "," + attrName + "_md";
            } // for

            typedFieldNames += ")";
            fieldNames += ")";
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

            String column = "('" + recvTime + "','" + servicePath + "','" + entityId + "','" + entityType + "'";

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");

                // create part of the column with the current attribute (a.k.a. a column)
                column += ",'" + attrValue + "','"  + attrMetadata + "'";
            } // for

            // now, aggregate the column
            if (aggregation.isEmpty()) {
                aggregation += column + ")";
            } else {
                aggregation += "," + column + ")";
            } // if else
        } // aggregate

    } // ColumnAggregator

    private PostgreSQLAggregator getAggregator(boolean rowAttrPersistence) {
        if (rowAttrPersistence) {
            return new RowAggregator();
        } else {
            return new ColumnAggregator();
        } // if else
    } // getAggregator

    private void persistAggregation(PostgreSQLAggregator aggregator) throws Exception {
        String typedFieldNames = aggregator.getTypedFieldNames();
        String fieldNames = aggregator.getFieldNames();
        String fieldValues = aggregator.getAggregation();
        String schemaName = aggregator.getSchemaName(enableLowercase);
        String tableName = aggregator.getTableName(enableLowercase);

        LOGGER.info("[" + this.getName() + "] Persisting data at OrionPostgreSQLSink. Schema ("
                + schemaName + "), Table (" + tableName + "), Fields (" + fieldNames + "), Values ("
                + fieldValues + ")");

        // creating the database and the table has only sense if working in row mode, in column node
        // everything must be provisioned in advance
        if (aggregator instanceof RowAggregator) {
            persistenceBackend.createSchema(schemaName);
            persistenceBackend.createTable(schemaName, tableName, typedFieldNames);
        } // if

        persistenceBackend.insertContextData(schemaName, tableName, fieldNames, fieldValues);
    } // persistAggregation

} // OrionPostgreSQLSink
