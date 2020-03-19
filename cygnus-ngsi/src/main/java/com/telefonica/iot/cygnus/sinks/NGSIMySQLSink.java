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

import com.telefonica.iot.cygnus.aggregation.NGSIGenericAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericColumnAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericRowAggregator;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSICharsets;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import org.apache.flume.Context;

import com.telefonica.iot.cygnus.backends.mysql.MySQLBackendImpl;
import com.telefonica.iot.cygnus.backends.sql.SQLBackend;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;

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
    private static final String MYSQL_DRIVER_NAME = "com.mysql.jdbc.Driver";
    private static final String MYSQL_INSTANCE_NAME = "mysql";

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIMySQLSink.class);
    private String mysqlHost;
    private String mysqlPort;
    private String mysqlUsername;
    private String mysqlPassword;
    private int maxPoolSize;
    private boolean rowAttrPersistence;
    private SQLBackend persistenceBackend;
    private boolean attrNativeTypes;
    private boolean attrMetadataStore;

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
    protected SQLBackend getPersistenceBackend() {
        return persistenceBackend;
    } // getPersistenceBackend
    
    /**
     * Sets the persistence backend. It is protected due to it is only required for testing purposes.
     * @param persistenceBackend
     */
    protected void setPersistenceBackend(SQLBackend persistenceBackend) {
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

        String attrMetadataStoreSrt = context.getString("attr_metadata_store", "true");

        if (attrMetadataStoreSrt.equals("true") || attrMetadataStoreSrt.equals("false")) {
            attrMetadataStore = Boolean.parseBoolean(attrMetadataStoreSrt);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_metadata_store="
                    + attrMetadataStore + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (attr_metadata_store="
                    + attrNativeTypesStr + ") -- Must be 'true' or 'false'");
        } // if else

        super.configure(context);
    } // configure

    @Override
    public void start() {
        try {
            //persistenceBackend = new MySQLBackendImpl(mysqlHost, mysqlPort, mysqlUsername, mysqlPassword, maxPoolSize);
            persistenceBackend = new SQLBackend(mysqlHost, mysqlPort, mysqlUsername, mysqlPassword, maxPoolSize, MYSQL_INSTANCE_NAME, MYSQL_DRIVER_NAME);
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
            NGSIGenericAggregator aggregator = getAggregator(rowAttrPersistence);
            aggregator.setService(events.get(0).getServiceForNaming(enableNameMappings));
            aggregator.setServicePathForData(events.get(0).getServicePathForData());
            aggregator.setServicePathForNaming(events.get(0).getServicePathForNaming(enableGrouping, enableNameMappings));
            aggregator.setEntityForNaming(events.get(0).getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding));
            aggregator.setEntityType(events.get(0).getEntityTypeForNaming(enableGrouping, enableNameMappings));
            aggregator.setAttribute(events.get(0).getAttributeForNaming(enableNameMappings));
            aggregator.setDbName(buildDbName(aggregator.getService()));
            aggregator.setTableName(buildTableName(aggregator.getServicePathForNaming(), aggregator.getEntityForNaming(), aggregator.getEntityType(), aggregator.getAttribute()));
            aggregator.setAttrNativeTypes(attrNativeTypes);
            aggregator.setAttrMetadataStore(attrMetadataStore);
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
    
    protected NGSIGenericAggregator getAggregator(boolean rowAttrPersistence) {
        if (rowAttrPersistence) {
            return new NGSIGenericRowAggregator();
        } else {
            return new NGSIGenericColumnAggregator();
        } // if else
    } // getAggregator
    
    private void persistAggregation(NGSIGenericAggregator aggregator)
        throws CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
        String fieldsForCreate = NGSIUtils.getFieldsForCreate(aggregator.getAggregationToPersist());
        String fieldsForInsert = NGSIUtils.getFieldsForInsert(aggregator.getAggregationToPersist());
        String valuesForInsert = NGSIUtils.getValuesForInsert(aggregator.getAggregationToPersist(), aggregator.isAttrNativeTypes());
        String dbName = aggregator.getDbName(enableLowercase);
        String tableName = aggregator.getTableName(enableLowercase);
        
        LOGGER.info("[" + this.getName() + "] Persisting data at NGSIMySQLSink. Database ("
                + dbName + "), Table (" + tableName + "), Fields (" + fieldsForInsert + "), Values ("
                + valuesForInsert + ")");
        
        // creating the database and the table has only sense if working in row mode, in column node
        // everything must be provisioned in advance
        if (aggregator instanceof NGSIGenericRowAggregator) {
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
