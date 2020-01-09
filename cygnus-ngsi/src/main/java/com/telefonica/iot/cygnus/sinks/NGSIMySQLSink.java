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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.flume.Context;

import com.telefonica.iot.cygnus.backends.mysql.MySQLBackendImpl;
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
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.utils.NGSICharsets;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;

/**
 *
 * @author frb
 * 
 * Detailed documentation can be found at:
 * https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/flume_extensions_catalogue/ngsi_mysql_sink.md
 */
public class NGSIMySQLSink extends NGSISink {
    
    private static final String DEFAULT_ROW_ATTR_PERSISTENCE = "row";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_USER_NAME = "root";
    private static final int DEFAULT_MAX_POOL_SIZE = 3;
    private static final String DEFAULT_ATTR_NATIVE_TYPES = "false";

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIMySQLSink.class);
    private String mysqlHost;
    private String mysqlPort;
    private String mysqlUsername;
    private String mysqlPassword;
    private int maxPoolSize;
    private boolean rowAttrPersistence;
    private MySQLBackendImpl persistenceBackend;
    private boolean attrNativeTypes;

    /**
     * Constructor.
     */
    public NGSIMySQLSink() {
        super();
    } // NGSIMySQLSink
    
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


   /**
     * Returns if the attribute value will be native or stringfy. It will be stringfy due to backward compatibility
     * purposes.
     * @return True if the attribute value will be native, false otherwise
     */
    protected boolean getNativeAttrTypes() {
        return attrNativeTypes;
    } // attrNativeTypes
    
    @Override
    public void configure(Context context) {
        mysqlHost = context.getString("mysql_host", DEFAULT_HOST);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mysql_host=" + mysqlHost + ")");
        mysqlPort = context.getString("mysql_port", DEFAULT_PORT);
        int intPort = Integer.parseInt(mysqlPort);
        
        if ((intPort <= 0) || (intPort > 65535)) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (mysql_port=" + mysqlPort + ") "
                    + "must be between 0 and 65535");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (mysql_port=" + mysqlPort + ")");
        }  // if else
        
        mysqlUsername = context.getString("mysql_username", DEFAULT_USER_NAME);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mysql_username=" + mysqlUsername + ")");
        // FIXME: mysqlPassword should be read encrypted and decoded here
        mysqlPassword = context.getString("mysql_password", DEFAULT_PASSWORD);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mysql_password=" + mysqlPassword + ")");
        
        maxPoolSize = context.getInteger("mysql_maxPoolSize", DEFAULT_MAX_POOL_SIZE);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mysql_maxPoolSize=" + maxPoolSize + ")");
        
        rowAttrPersistence = context.getString("attr_persistence", DEFAULT_ROW_ATTR_PERSISTENCE).equals("row");
        String persistence = context.getString("attr_persistence", DEFAULT_ROW_ATTR_PERSISTENCE);
        
        if (persistence.equals("row") || persistence.equals("column")) {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_persistence="
                + persistence + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (attr_persistence="
                + persistence + ") must be 'row' or 'column'");
        }  // if else

        String attrNativeTypesStr = context.getString("attr_native_types", DEFAULT_ATTR_NATIVE_TYPES);
        if (attrNativeTypesStr.equals("true") || attrNativeTypesStr.equals("false")) {
            attrNativeTypes = Boolean.valueOf(attrNativeTypesStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_native_types=" + attrNativeTypesStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (attr_native_types="
                + attrNativeTypesStr + ") -- Must be 'true' or 'false'");
        } // if else

        super.configure(context);
    } // configure

    @Override
    public void start() {
        try {
            persistenceBackend = new MySQLBackendImpl(mysqlHost, mysqlPort, mysqlUsername, mysqlPassword, maxPoolSize);
            LOGGER.debug("[" + this.getName() + "] MySQL persistence backend created");
        } catch (Exception e) {
            LOGGER.error("Error while creating the MySQL persistence backend. Details="
                    + e.getMessage());
        } // try catch
        
        super.start();
    } // start

    @Override
    public void stop() {
        super.stop();
        if (persistenceBackend != null) persistenceBackend.close();
    } // stop
    
    @Override
    void persistBatch(NGSIBatch batch)
        throws CygnusBadConfiguration, CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
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
            
            // Get an aggregator for this destination and initialize it
            MySQLAggregator aggregator = getAggregator(rowAttrPersistence);
            aggregator.initialize(events.get(0));

            for (NGSIEvent event : events) {
                aggregator.aggregate(event);
            } // for
            
            // Persist the aggregation
            persistAggregation(aggregator);
            batch.setNextPersisted(true);
        } // for
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
            String entity = event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding);
            String entityType = event.getEntityTypeForNaming(enableGrouping, enableNameMappings);
            String attribute = event.getAttributeForNaming(enableNameMappings);
            
            try {
                String dbName = buildDbName(service);
                String tableName = buildTableName(servicePathForNaming, entity, entityType, attribute);
                LOGGER.debug("[" + this.getName() + "] Capping resource (maxRecords=" + maxRecords + ",dbName="
                        + dbName + ", tableName=" + tableName + ")");
                persistenceBackend.capRecords(dbName, tableName, maxRecords);
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
    } // expirateRecords
    
    /**
     * Class for aggregating.
     */
    private abstract class MySQLAggregator {
        
        // object containing the aggregted data
        private LinkedHashMap<String, ArrayList<String>> aggregation;

        private String service;
        private String servicePathForData;
        private String servicePathForNaming;
        private String entityForNaming;
        private String entityType;
        private String attribute;
        private String dbName;
        private String tableName;
        
        MySQLAggregator() {
            aggregation = new LinkedHashMap<>();
        } // MySQLAggregator
        
        protected LinkedHashMap<String, ArrayList<String>> getAggregation() {
            return aggregation;
        } //getAggregation

        @SuppressWarnings("unused")
        protected void setAggregation(LinkedHashMap<String, ArrayList<String>> aggregation) {
            this.aggregation = aggregation;
        } //setAggregation


        @SuppressWarnings("unused")
        protected String getService() {
            return service;
        } //getService


        @SuppressWarnings("unused")
        protected void setService(String service) {
            this.service = service;
        } //setService

        protected String getServicePathForData() {
            return servicePathForData;
        } //getServicePathForData


        @SuppressWarnings("unused")
        protected void setServicePathForData(String servicePathForData) {
            this.servicePathForData = servicePathForData;
        } //setServicePathForData


        @SuppressWarnings("unused")
        protected String getServicePathForNaming() {
            return servicePathForNaming;
        } //getServicePathForNaming


        @SuppressWarnings("unused")
        protected void setServicePathForNaming(String servicePathForNaming) {
            this.servicePathForNaming = servicePathForNaming;
        } //setServicePathForNaming


        @SuppressWarnings("unused")
        protected String getTableName() {
            return tableName;
        } //getTableName

        @SuppressWarnings("unused")
        protected void setTableName(String tableName) {
            this.tableName = tableName;
        } //setTableName



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
        
        public String getValuesForInsert() {
            String valuesForInsert = "";
            int numEvents = aggregation.get(NGSIConstants.FIWARE_SERVICE_PATH).size();
            
            for (int i = 0; i < numEvents; i++) {
                if (i == 0) {
                    valuesForInsert += "(";
                } else {
                    valuesForInsert += ",(";
                } // if else
                
                boolean first = true;
                Iterator<String> it = aggregation.keySet().iterator();
            
                while (it.hasNext()) {
                    String entry = (String) it.next();
                    ArrayList<String> values = (ArrayList<String>) aggregation.get(entry);
                    String value = values.get(i);
                    if (attrNativeTypes) {
                        LOGGER.debug("[" + getName() + "] aggregation entry = "  + entry );
                        if (value == null || value.equals("")) {
                            value = "NULL";
                        } else {
                            value = "'" + value + "'";
                        }
                        LOGGER.debug("[" + getName() + "] native value = "  + value );
                    } else {
                        value = "'" + value + "'";
                    }

                    if (first) {
                        valuesForInsert += value;
                        first = false;
                    } else {
                        valuesForInsert += "," + value;
                    } // if else
                } // while

                valuesForInsert += ")";
            } // for
            
            return valuesForInsert;
        } // getValuesForInsert
        
        public String getFieldsForCreate() {
            String fieldsForCreate = "(";
            boolean first = true;
            Iterator<String> it = aggregation.keySet().iterator();
            
            while (it.hasNext()) {
                if (first) {
                    fieldsForCreate += (String) it.next() + " text";
                    first = false;
                } else {
                    fieldsForCreate += "," + (String) it.next() + " text";
                } // if else
            } // while
            
            return fieldsForCreate + ")";
        } // getFieldsForCreate
        
        public String getFieldsForInsert() {
            String fieldsForInsert = "(";
            boolean first = true;
            Iterator<String> it = aggregation.keySet().iterator();
            
            while (it.hasNext()) {
                if (first) {
                    fieldsForInsert += (String) it.next();
                    first = false;
                } else {
                    fieldsForInsert += "," + (String) it.next();
                } // if else
            } // while
            
            return fieldsForInsert + ")";
        } // getFieldsForInsert
        
        public void initialize(NGSIEvent event) throws CygnusBadConfiguration {
            service = event.getServiceForNaming(enableNameMappings);
            servicePathForData = event.getServicePathForData();
            servicePathForNaming = event.getServicePathForNaming(enableGrouping, enableNameMappings);
            entityForNaming = event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding);
            entityType = event.getEntityTypeForNaming(enableGrouping, enableNameMappings);
            attribute = event.getAttributeForNaming(enableNameMappings);
            dbName = buildDbName(service);
            tableName = buildTableName(servicePathForNaming, entityForNaming, entityType, attribute);
        } // initialize
        
        public abstract void aggregate(NGSIEvent cygnusEvent);
        
    } // MySQLAggregator
    
    /**
     * Class for aggregating batches in row mode.
     */
    private class RowAggregator extends MySQLAggregator {
        
        @Override
        public void initialize(NGSIEvent cygnusEvent) throws CygnusBadConfiguration {
            super.initialize(cygnusEvent);
            LinkedHashMap<String, ArrayList<String>> aggregation = getAggregation();
            aggregation.put(NGSIConstants.RECV_TIME_TS, new ArrayList<String>());
            aggregation.put(NGSIConstants.RECV_TIME, new ArrayList<String>());
            aggregation.put(NGSIConstants.FIWARE_SERVICE_PATH, new ArrayList<String>());
            aggregation.put(NGSIConstants.ENTITY_ID, new ArrayList<String>());
            aggregation.put(NGSIConstants.ENTITY_TYPE, new ArrayList<String>());
            aggregation.put(NGSIConstants.ATTR_NAME, new ArrayList<String>());
            aggregation.put(NGSIConstants.ATTR_TYPE, new ArrayList<String>());
            aggregation.put(NGSIConstants.ATTR_VALUE, new ArrayList<String>());
            aggregation.put(NGSIConstants.ATTR_MD, new ArrayList<String>());
        } // initialize
        
        @Override
        public void aggregate(NGSIEvent event) {
            // get the getRecvTimeTs headers
            long recvTimeTs = event.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, false);

            // get the getRecvTimeTs body
            ContextElement contextElement = event.getContextElement();
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
                
                // aggregate the attribute information
                LinkedHashMap<String, ArrayList<String>> aggregation = getAggregation();
                aggregation.get(NGSIConstants.RECV_TIME_TS).add(Long.toString(recvTimeTs));
                aggregation.get(NGSIConstants.RECV_TIME).add(recvTime);
                aggregation.get(NGSIConstants.FIWARE_SERVICE_PATH).add(getServicePathForData());
                aggregation.get(NGSIConstants.ENTITY_ID).add(entityId);
                aggregation.get(NGSIConstants.ENTITY_TYPE).add(entityType);
                aggregation.get(NGSIConstants.ATTR_NAME).add(attrName);
                aggregation.get(NGSIConstants.ATTR_TYPE).add(attrType);
                aggregation.get(NGSIConstants.ATTR_VALUE).add(attrValue);
                aggregation.get(NGSIConstants.ATTR_MD).add(attrMetadata);
            } // for
        } // aggregate

    } // RowAggregator
    
    /**
     * Class for aggregating batches in column mode.
     */
    private class ColumnAggregator extends MySQLAggregator {

        @Override
        public void initialize(NGSIEvent cygnusEvent) throws CygnusBadConfiguration {
            super.initialize(cygnusEvent);
            
            // particular initialization
            LinkedHashMap<String, ArrayList<String>> aggregation = getAggregation();
            aggregation.put(NGSIConstants.RECV_TIME, new ArrayList<String>());
            aggregation.put(NGSIConstants.FIWARE_SERVICE_PATH, new ArrayList<String>());
            aggregation.put(NGSIConstants.ENTITY_ID, new ArrayList<String>());
            aggregation.put(NGSIConstants.ENTITY_TYPE, new ArrayList<String>());
            
            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = cygnusEvent.getContextElement().getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                return;
            } // if
            
            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                aggregation.put(attrName, new ArrayList<String>());
                aggregation.put(attrName + "_md", new ArrayList<String>());
            } // for
        } // initialize
        
        @Override
        public void aggregate(NGSIEvent event) {
            // Number of previous values
            int numPreviousValues = getAggregation().get(NGSIConstants.FIWARE_SERVICE_PATH).size();
            
            // Get the event headers
            long recvTimeTs = event.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, false);

            // get the event body
            ContextElement contextElement = event.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");
            
            // Iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            LinkedHashMap<String, ArrayList<String>> aggregation = getAggregation();
            aggregation.get(NGSIConstants.RECV_TIME).add(recvTime);
            aggregation.get(NGSIConstants.FIWARE_SERVICE_PATH).add(getServicePathForData());
            aggregation.get(NGSIConstants.ENTITY_ID).add(entityId);
            aggregation.get(NGSIConstants.ENTITY_TYPE).add(entityType);
            
            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                
                // Check if the attribute already exists in the form of 2 columns (one for metadata); if not existing,
                // add an empty value for all previous rows
                if (aggregation.containsKey(attrName)) {
                    aggregation.get(attrName).add(attrValue);
                    aggregation.get(attrName + "_md").add(attrMetadata);
                } else {
                    ArrayList<String> values = new ArrayList<>(Collections.nCopies(numPreviousValues, ""));
                    values.add(attrValue);
                    aggregation.put(attrName, values);
                    ArrayList<String> valuesMd = new ArrayList<>(Collections.nCopies(numPreviousValues, ""));
                    valuesMd.add(attrMetadata);
                    aggregation.put(attrName + "_md", valuesMd);
                } // if else
            } // for
            
            // Iterate on all the aggregations, checking for not updated attributes; add an empty value if missing
            for (String key : aggregation.keySet()) {
                ArrayList<String> values = aggregation.get(key);
                
                if (values.size() == numPreviousValues) {
                    values.add("");
                } // if
            } // for
        } // aggregate
        
    } // ColumnAggregator
    
    private MySQLAggregator getAggregator(boolean rowAttrPersistence) {
        if (rowAttrPersistence) {
            return new RowAggregator();
        } else {
            return new ColumnAggregator();
        } // if else
    } // getAggregator
    
    private void persistAggregation(MySQLAggregator aggregator)
        throws CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
        String fieldsForCreate = aggregator.getFieldsForCreate();
        String fieldsForInsert = aggregator.getFieldsForInsert();
        String valuesForInsert = aggregator.getValuesForInsert();
        String dbName = aggregator.getDbName(enableLowercase);
        String tableName = aggregator.getTableName(enableLowercase);
        
        LOGGER.info("[" + this.getName() + "] Persisting data at NGSIMySQLSink. Database ("
                + dbName + "), Table (" + tableName + "), Fields (" + fieldsForInsert + "), Values ("
                + valuesForInsert + ")");
        
        // creating the database and the table has only sense if working in row mode, in column node
        // everything must be provisioned in advance
        if (aggregator instanceof RowAggregator) {
            persistenceBackend.createDatabase(dbName);
            persistenceBackend.createTable(dbName, tableName, fieldsForCreate);
        } // if

        if (valuesForInsert.equals("")) {
            LOGGER.debug("[" + this.getName() + "] no values for insert");
        } else {
            persistenceBackend.insertContextData(dbName, tableName, fieldsForInsert, valuesForInsert);
        }
    } // persistAggregation
    
    /**
     * Creates a MySQL DB name given the FIWARE service.
     * @param service
     * @return The MySQL DB name
     * @throws CygnusBadConfiguration
     */
    protected String buildDbName(String service) throws CygnusBadConfiguration {
        String name;
        
        if (enableEncoding) {
            name = NGSICharsets.encodeMySQL(service);
        } else {
            name = NGSIUtils.encode(service, false, true);
        } // if else

        if (name.length() > NGSIConstants.MYSQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building database name '" + name
                    + "' and its length is greater than " + NGSIConstants.MYSQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildDbName

    /**
     * Creates a MySQL table name given the FIWARE service path, the entity and the attribute.
     * @param servicePath
     * @param entity
     * @param attribute
     * @return The MySQL table name
     * @throws CygnusBadConfiguration
     */
    protected String buildTableName(String servicePath, String entity, String entityType, String attribute)
            throws CygnusBadConfiguration {
        String name;

        if (enableEncoding) {
            switch (dataModel) {
                case DMBYSERVICEPATH:
                    name = NGSICharsets.encodeMySQL(servicePath);
                    break;
                case DMBYENTITY:
                    name = NGSICharsets.encodeMySQL(servicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodeMySQL(entity);
                    break;
                case DMBYENTITYTYPE:
                    name = NGSICharsets.encodeMySQL(servicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodeMySQL(entityType);
                    break;
                case DMBYATTRIBUTE:
                    name = NGSICharsets.encodeMySQL(servicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodeMySQL(entity)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodeMySQL(attribute);
                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use dm-by-service-path, dm-by-entity or dm-by-attribute");
            } // switch
        } else {
            switch (dataModel) {
                case DMBYSERVICEPATH:
                    if (servicePath.equals("/")) {
                        throw new CygnusBadConfiguration("Default service path '/' cannot be used with "
                                + "dm-by-service-path data model");
                    } // if
                    
                    name = NGSIUtils.encode(servicePath, true, false);
                    break;
                case DMBYENTITY:
                    String truncatedServicePath = NGSIUtils.encode(servicePath, true, false);
                    name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '_')
                            + NGSIUtils.encode(entity, false, true);
                    break;
                case DMBYENTITYTYPE:
                    truncatedServicePath = NGSIUtils.encode(servicePath, true, false);
                    name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '_')
                            + NGSIUtils.encode(entityType, false, true);
                    break;
                case DMBYATTRIBUTE:
                    truncatedServicePath = NGSIUtils.encode(servicePath, true, false);
                    name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '_')
                            + NGSIUtils.encode(entity, false, true)
                            + '_' + NGSIUtils.encode(attribute, false, true);
                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use DMBYSERVICEPATH, DMBYENTITY, DMBYENTITYTYPE or DMBYATTRIBUTE");
            } // switch
        } // if else

        if (name.length() > NGSIConstants.MYSQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building table name '" + name
                    + "' and its length is greater than " + NGSIConstants.MYSQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildTableName

} // NGSIMySQLSink
