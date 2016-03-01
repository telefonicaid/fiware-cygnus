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

import com.telefonica.iot.cygnus.backends.mysql.MySQLBackendImpl;
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
 * @author frb
 * 
 * Detailed documentation can be found at:
 * https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/flume_extensions_catalogue/orion_mysql_sink.md
 */
public class OrionMySQLSink extends OrionSink {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(OrionMySQLSink.class);
    private String mysqlHost;
    private String mysqlPort;
    private String mysqlUsername;
    private String mysqlPassword;
    private boolean rowAttrPersistence;
    private MySQLBackendImpl persistenceBackend;
    
    /**
     * Constructor.
     */
    public OrionMySQLSink() {
        super();
    } // OrionMySQLSink
    
    /**
     * Gets the MySQL host. It is protected due to it is only required for testing purposes.
     * @return The MySQL host
     */
    protected String getMySQLHost() {
        return mysqlHost;
    } // getMySQLHost
    
    /**
     * Gets the MySQL port. It is protected due to it is only required for testing purposes.
     * @return The MySQL port
     */
    protected String getMySQLPort() {
        return mysqlPort;
    } // getMySQLPort
    
    /**
     * Gets the MySQL username. It is protected due to it is only required for testing purposes.
     * @return The MySQL username
     */
    protected String getMySQLUsername() {
        return mysqlUsername;
    } // getMySQLUsername
    
    /**
     * Gets the MySQL password. It is protected due to it is only required for testing purposes.
     * @return The MySQL password
     */
    protected String getMySQLPassword() {
        return mysqlPassword;
    } // getMySQLPassword
    
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
    protected MySQLBackendImpl getPersistenceBackend() {
        return persistenceBackend;
    } // getPersistenceBackend
    
    /**
     * Sets the persistence backend. It is protected due to it is only required for testing purposes.
     * @param persistenceBackend
     */
    protected void setPersistenceBackend(MySQLBackendImpl persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    } // setPersistenceBackend
    
    @Override
    public void configure(Context context) {
        mysqlHost = context.getString("mysql_host", "localhost");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mysql_host=" + mysqlHost + ")");
        mysqlPort = context.getString("mysql_port", "3306");
        int intPort = Integer.parseInt(mysqlPort);
        
        if ((intPort <= 0) || (intPort > 65535)) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (mysql_port=" + mysqlPort + ") "
                    + "must be between 0 and 65535");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (mysql_port=" + mysqlPort + ")");
        }  // if else
        
        mysqlUsername = context.getString("mysql_username", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mysql_username=" + mysqlUsername + ")");
        // FIXME: mysqlPassword should be read as a SHA1 and decoded here
        mysqlPassword = context.getString("mysql_password", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mysql_password=" + mysqlPassword + ")");
        rowAttrPersistence = context.getString("attr_persistence", "row").equals("row");
        String persistence = context.getString("attr_persistence", "row");
        
        if (persistence.equals("row") || persistence.equals("column")) {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_persistence="
                + persistence + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (attr_persistence="
                + persistence + ") must be 'row' or 'column'");
        }  // if else
        
        super.configure(context);
    } // configure

    @Override
    public void start() {
        try {
            persistenceBackend = new MySQLBackendImpl(mysqlHost, mysqlPort, mysqlUsername, mysqlPassword);
            LOGGER.debug("[" + this.getName() + "] MySQL persistence backend created");
        } catch (Exception e) {
            LOGGER.error("Error while creating the MySQL persistence backend. Details="
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
            MySQLAggregator aggregator = getAggregator(rowAttrPersistence);
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
    private abstract class MySQLAggregator {
        
        // string containing the data fieldValues
        protected String aggregation;

        protected String service;
        protected String servicePath;
        protected String entity;
        protected String attribute;
        protected String dbName;
        protected String tableName;
        protected String typedFieldNames;
        protected String fieldNames;
        
        public MySQLAggregator() {
            aggregation = "";
        } // MySQLAggregator
        
        public String getAggregation() {
            return aggregation;
        } // getAggregation
        
        public String getDbName(boolean enableLowercase) {
            if (enableLowercase) {
                return dbName.toLowerCase();
            } else {
                return dbName;
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
            dbName = buildDbName();
            tableName = buildTableName();
        } // initialize
        
        private String buildDbName() throws Exception {
            String name = service;

            if (name.length() > Constants.MAX_NAME_LEN) {
                throw new CygnusBadConfiguration("Building database name '" + name
                        + "' and its length is greater than " + Constants.MAX_NAME_LEN);
            } // if

            return name;
        } // buildDbName
        
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
        
    } // MySQLAggregator
    
    /**
     * Class for aggregating batches in row mode.
     */
    private class RowAggregator extends MySQLAggregator {
        
        @Override
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
            typedFieldNames = "("
                    + Constants.RECV_TIME_TS + " long,"
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
    private class ColumnAggregator extends MySQLAggregator {

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
    
    private MySQLAggregator getAggregator(boolean rowAttrPersistence) {
        if (rowAttrPersistence) {
            return new RowAggregator();
        } else {
            return new ColumnAggregator();
        } // if else
    } // getAggregator
    
    private void persistAggregation(MySQLAggregator aggregator) throws Exception {
        String typedFieldNames = aggregator.getTypedFieldNames();
        String fieldNames = aggregator.getFieldNames();
        String fieldValues = aggregator.getAggregation();
        String dbName = aggregator.getDbName(enableLowercase);
        String tableName = aggregator.getTableName(enableLowercase);
        
        LOGGER.info("[" + this.getName() + "] Persisting data at OrionMySQLSink. Database ("
                + dbName + "), Table (" + tableName + "), Fields (" + fieldNames + "), Values ("
                + fieldValues + ")");
        
        // creating the database and the table has only sense if working in row mode, in column node
        // everything must be provisioned in advance
        if (aggregator instanceof RowAggregator) {
            persistenceBackend.createDatabase(dbName);
            persistenceBackend.createTable(dbName, tableName, typedFieldNames);
        } // if
        
        persistenceBackend.insertContextData(dbName, tableName, fieldNames, fieldValues);
    } // persistAggregation

} // OrionMySQLSink