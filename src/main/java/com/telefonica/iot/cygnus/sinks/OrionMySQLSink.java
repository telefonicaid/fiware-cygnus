/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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

import com.telefonica.iot.cygnus.backends.mysql.MySQLBackend;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.flume.Context;
import org.apache.flume.Event;

/**
 *
 * @author frb
 * 
 * Custom MySQL sink for Orion Context Broker. The MySQL design for this sink is:
 *  - There is a database per user, being its attrName:
 *    cygnus_<username>
 *  - Each entity has its data stored in a specific table, being its attrName:
 *    cygnus_<entity_id>_<entity_type>
 *  - Each event data is stored in the appropriate table as a new row, having each row the following fields:
 *    recvTimeTs, recvTime, entityId, entityType, attrName, attrType, attrValue
 * 
 * As can be seen, a table is created per each entity, containing all the historical values this entity's attributes
 * have had.
 * 
 * It is important to note that certain degree of reliability is achieved by using a rolling back mechanism in the
 * channel, i.e. an event is not removed from the channel until it is not appropriately persisted.
 */
public class OrionMySQLSink extends OrionSink {
    
    /**
     * Available table types.
     */
    public enum TableType { TABLEBYDESTINATION, TABLEBYSERVICEPATH }
    
    private static final CygnusLogger LOGGER = new CygnusLogger(OrionMySQLSink.class);
    private String mysqlHost;
    private String mysqlPort;
    private String mysqlUsername;
    private String mysqlPassword;
    private TableType tableType;
    private boolean rowAttrPersistence;
    private MySQLBackend persistenceBackend;
    
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
     * Returns the table type. It is protected due to it is only required for testing purposes.
     * @return The table type
     */
    protected TableType getTableType() {
        return tableType;
    } // getTableType

    /**
     * Returns the persistence backend. It is protected due to it is only required for testing purposes.
     * @return The persistence backend
     */
    protected MySQLBackend getPersistenceBackend() {
        return persistenceBackend;
    } // getPersistenceBackend
    
    /**
     * Sets the persistence backend. It is protected due to it is only required for testing purposes.
     * @param persistenceBackend
     */
    protected void setPersistenceBackend(MySQLBackend persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    } // setPersistenceBackend
    
    @Override
    public void configure(Context context) {
        mysqlHost = context.getString("mysql_host", "localhost");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mysql_host=" + mysqlHost + ")");
        mysqlPort = context.getString("mysql_port", "3306");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mysql_port=" + mysqlPort + ")");
        mysqlUsername = context.getString("mysql_username", "opendata");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mysql_username=" + mysqlUsername + ")");
        // FIXME: mysqlPassword should be read as a SHA1 and decoded here
        mysqlPassword = context.getString("mysql_password", "unknown");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mysql_password=" + mysqlPassword + ")");
        String tableTypeStr = context.getString("table_type", "table-per-destination");
        tableType = TableType.valueOf(tableTypeStr.replaceAll("-", "").toUpperCase());
        rowAttrPersistence = context.getString("attr_persistence", "row").equals("row");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_persistence="
                + (rowAttrPersistence ? "row" : "column") + ")");
        super.configure(context);
    } // configure

    @Override
    public void start() {
        // create the persistence backend
        LOGGER.debug("[" + this.getName() + "] MySQL persistence backend created");
        persistenceBackend = new MySQLBackend(mysqlHost, mysqlPort, mysqlUsername, mysqlPassword);
        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

    @Override
    void persistOne(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        // get some header values
        Long recvTimeTs = new Long(eventHeaders.get(Constants.HEADER_TIMESTAMP));
        String fiwareService = eventHeaders.get(Constants.HEADER_NOTIFIED_SERVICE);
        String[] servicePaths;
        String[] destinations;
        
        if (enableGrouping) {
            servicePaths = eventHeaders.get(Constants.HEADER_GROUPED_SERVICE_PATHS).split(",");
            destinations = eventHeaders.get(Constants.HEADER_GROUPED_DESTINATIONS).split(",");
        } else {
            servicePaths = eventHeaders.get(Constants.HEADER_DEFAULT_SERVICE_PATHS).split(",");
            destinations = eventHeaders.get(Constants.HEADER_DEFAULT_DESTINATIONS).split(",");
        } // if else

        // human readable version of the reception time
        String recvTime = Utils.getHumanReadable(recvTimeTs, false);

        // create the database for this fiwareService if not yet existing... the cost of trying to create it is the same
        // than checking if it exits and then creating it
        String dbName = buildDbName(fiwareService);
        
        // the database can be automatically created both in the per-column or per-row mode; anyway, it has no sense to
        // create it in the per-column mode because there will not be any table within the database
        if (rowAttrPersistence) {
            persistenceBackend.createDatabase(dbName);
        } // if
        
        // iterate on the contextResponses
        ArrayList contextResponses = notification.getContextResponses();
        
        for (int i = 0; i < contextResponses.size(); i++) {
            // get the i-th contextElement
            ContextElementResponse contextElementResponse = (ContextElementResponse) contextResponses.get(i);
            ContextElement contextElement = contextElementResponse.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + this.getName() + "] Processing context element (id=" + entityId + ", type= "
                    + entityType + ")");
            
            // build the table name
            String tableName = buildTableName(servicePaths[i], destinations[i], tableType);
            
            // if the attribute persistence is based in rows, create the table where the data will be persisted, since
            // these tables are fixed 7-field row ones; otherwise, the size of the table is unknown and cannot be
            // created in execution time, it must be previously provisioned
            if (rowAttrPersistence) {
                // create the table for this entity if not existing yet... the cost of trying yo create it is the same
                // than checking if it exits and then creating it
                persistenceBackend.createTable(dbName, tableName);
            } // if
            
            // iterate on all this entity's attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();
            
            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                continue;
            } // if
            
            // this is used for storing the attribute's names and values when dealing with a per column attributes
            // persistence; in that case the persistence is not done attribute per attribute, but persisting all of them
            // at the same time
            HashMap<String, String> attrs = new HashMap<String, String>();
            
            // this is used for storing the attribute's names (sufixed with "-md") and metadata when dealing with a per
            // column attributes persistence; in that case the persistence is not done attribute per attribute, but
            // persisting all of them at the same time
            HashMap<String, String> mds = new HashMap<String, String>();

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + this.getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                
                if (rowAttrPersistence) {
                    LOGGER.info("[" + this.getName() + "] Persisting data at OrionMySQLSink. Database: " + dbName
                            + ", Table: " + tableName + ", Data: " + recvTimeTs / 1000 + "," + recvTime + ","
                            + entityId + "," + entityType + "," + attrName + "," + entityType + "," + attrValue + ","
                            + attrMetadata);
                    persistenceBackend.insertContextData(dbName, tableName, recvTimeTs / 1000, recvTime,
                            entityId, entityType, attrName, attrType, attrValue, attrMetadata);
                } else {
                    attrs.put(attrName, attrValue);
                    mds.put(attrName + "_md", attrMetadata);
                } // if else
            } // for
            
            // if the attribute persistence mode is per column, now is the time to insert a new row containing full
            // attribute list of attrName-values.
            if (!rowAttrPersistence) {
                LOGGER.info("[" + this.getName() + "] Persisting data at OrionMySQLSink. Database: " + dbName
                        + ", Table: " + tableName + ", Timestamp: " + recvTime + ", Data (attrs): " + attrs.toString()
                        + ", (metadata): " + mds.toString());
                persistenceBackend.insertContextData(dbName, tableName, recvTime, attrs, mds);
            } // if
        } // for
    } // persistOne
    
    /**
     * Builds a database name given a fiwareService. It throws an exception if the naming conventions are violated.
     * @param fiwareService
     * @return
     * @throws Exception
     */
    private String buildDbName(String fiwareService) throws Exception {
        String dbName = fiwareService;
        
        if (dbName.length() > Constants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building dbName=fiwareService (" + dbName + ") and its length is greater "
                    + "than " + Constants.MAX_NAME_LEN);
        } // if
        
        return dbName;
    } // buildDbName
    
    /**
     * Builds a package name given a fiwareServicePath and a destination. It throws an exception if the naming
     * conventions are violated.
     * @param fiwareServicePath
     * @param destination
     * @return
     * @throws Exception
     */
    private String buildTableName(String fiwareServicePath, String destination, TableType tableType) throws Exception {
        String tableName;
        
        switch(tableType) {
            case TABLEBYDESTINATION:
                if (fiwareServicePath.length() == 0) {
                    tableName = destination;
                } else {
                    tableName = fiwareServicePath + '_' + destination;
                } // if else

                break;
            case TABLEBYSERVICEPATH:
                tableName = fiwareServicePath;
                break;
            default:
                throw new CygnusBadConfiguration("Unknown table type (" + tableType.toString() + " in OrionMySQLSink, "
                        + "cannot build the table name. Please, use TABLEBYSERVICEPATH or TABLEBYDESTINATION");
        } // switch
                
        if (tableName.length() > Constants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building tableName=fiwareServicePath + '_' + destination (" + tableName
                    + ") and its length is greater than " + Constants.MAX_NAME_LEN);
        } // if
        
        return tableName;
    } // buildTableName
    
    @Override
    void persistBatch(Batch defaultBatch, Batch groupedBatch) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    } // persistBatch

} // OrionMySQLSink