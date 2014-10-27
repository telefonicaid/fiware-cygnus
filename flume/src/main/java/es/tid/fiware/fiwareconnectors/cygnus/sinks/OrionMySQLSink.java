/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * francisco.romerobueno@telefonica.com
 */

package es.tid.fiware.fiwareconnectors.cygnus.sinks;

import es.tid.fiware.fiwareconnectors.cygnus.backends.mysql.MySQLBackend;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextAttribute;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElement;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusBadConfiguration;
import es.tid.fiware.fiwareconnectors.cygnus.log.CygnusLogger;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Utils;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.flume.Context;
import org.apache.log4j.Logger;

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
    
    private Logger logger;
    private String mysqlHost;
    private String mysqlPort;
    private String mysqlUsername;
    private String mysqlPassword;
    private boolean rowAttrPersistence;
    private String namingPrefix;
    private MySQLBackend persistenceBackend;
    
    /**
     * Constructor.
     */
    public OrionMySQLSink() {
        super();
        logger = CygnusLogger.getLogger(OrionHDFSSink.class);
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
     * Returns if the naming prefix. It is protected due to it is only required for testing purposes.
     * @return The naming prefix
     */
    protected String getNamingPrefix() {
        return namingPrefix;
    } // getNamingPrefix
    
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
        logger.debug("[" + this.getName() + "] Reading configuration (mysql_host=" + mysqlHost + ")");
        mysqlPort = context.getString("mysql_port", "3306");
        logger.debug("[" + this.getName() + "] Reading configuration (mysql_port=" + mysqlPort + ")");
        mysqlUsername = context.getString("mysql_username", "opendata");
        logger.debug("[" + this.getName() + "] Reading configuration (mysql_username=" + mysqlUsername + ")");
        // FIXME: cosmosPassword should be read as a SHA1 and decoded here
        mysqlPassword = context.getString("mysql_password", "unknown");
        logger.debug("[" + this.getName() + "] Reading configuration (mysql_password=" + mysqlPassword + ")");
        rowAttrPersistence = context.getString("attr_persistence", "row").equals("row");
        logger.debug("[" + this.getName() + "] Reading configuration (attr_persistence="
                + (rowAttrPersistence ? "row" : "column") + ")");
        namingPrefix = context.getString("naming_prefix", "");
        
        if (namingPrefix.length() > Constants.NAMING_PREFIX_MAX_LEN) {
            logger.error("[" + this.getName() + "] Bad configuration (Naming prefix length is greater than "
                    + Constants.NAMING_PREFIX_MAX_LEN + ")");
            logger.info("[" + this.getName() + "] Exiting Cygnus");
            System.exit(-1);
        } // if
        
        logger.debug("[" + this.getName() + "] Reading configuration (naming_prefix=" + namingPrefix + ")");
    } // configure

    @Override
    public void start() {
        // create the persistence backend
        logger.debug("[" + this.getName() + "] MySQL persistence backend created");
        persistenceBackend = new MySQLBackend(mysqlHost, mysqlPort, mysqlUsername, mysqlPassword);
        super.start();
        logger.info("[" + this.getName() + "] Startup completed");
    } // start

    @Override
    void persist(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        // get some header values
        Long recvTimeTs = new Long(eventHeaders.get("timestamp")).longValue();
        String organization = eventHeaders.get(Constants.HEADER_SERVICE);
        String tableName = this.namingPrefix + eventHeaders.get(Constants.DESTINATION).replaceAll("-", "_");
        
        // human readable version of the reception time
        String recvTime = new Timestamp(recvTimeTs).toString().replaceAll(" ", "T");
        
        // FIXME: organization is given in order to support multi-tenancy... should be used instead of the current
        // cosmosUsername

        // create the database for this organization if not yet existing... the cost of trying to create it is the same
        // than checking if it exits and then creating it
        String dbName = namingPrefix + organization;
        
        if (dbName.length() > Constants.MYSQL_DB_NAME_MAX_LEN) {
            logger.error("[" + this.getName() + "] Bad configuration (A MySQL database name '" + dbName + "' has been "
                    + "built and its length is greater than" + Constants.MYSQL_DB_NAME_MAX_LEN + ". This database name "
                    + "generation is based on the concatenation of the 'naming_prefix' configuration parameter and the "
                    + "notified '" + Constants.HEADER_SERVICE + "' organization header, thus adjust them)");
            throw new CygnusBadConfiguration("The lenght of the MySQL database '" + dbName + "' is greater "
                    + "than " + Constants.MYSQL_DB_NAME_MAX_LEN);
        } // if
        
        // the database can be automatically created both in the per-column or per-row mode; anyway, it has no sense to
        // create it in the per-column mode because there will not be any table within the database
        if (rowAttrPersistence) {
            persistenceBackend.createDatabase(dbName);
        } // if
        
        // iterate in the contextResponses
        ArrayList contextResponses = notification.getContextResponses();
        
        for (int i = 0; i < contextResponses.size(); i++) {
            // get the i-th contextElement
            ContextElementResponse contextElementResponse = (ContextElementResponse) contextResponses.get(i);
            ContextElement contextElement = contextElementResponse.getContextElement();
            String entityId = Utils.encode(contextElement.getId());
            String entityType = Utils.encode(contextElement.getType());
            logger.debug("[" + this.getName() + "] Processing context element (id= + " + entityId + ", type= "
                    + entityType + ")");
            
            if (tableName.length() > Constants.MYSQL_DB_NAME_MAX_LEN) {
                logger.error("[" + this.getName() + "] Bad configuration (A MySQL table name '" + tableName + "' has "
                        + "been built and its length is greater than" + Constants.MYSQL_TABLE_NAME_MAX_LEN + ". This "
                        + "table name generation is based on the concatenation of the 'naming_prefix' configuration "
                        + "parameter, the notified entity identifier, a '_' character and the notified entity type, "
                        + "thus adjust them");
                throw new CygnusBadConfiguration("The length of the MySQL table '" + tableName + "' is "
                        + "greater than " + Constants.MYSQL_DB_NAME_MAX_LEN);
            } // if
            
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
                logger.warn("No attributes within the notified entity, nothing is done (id=" + entityId + ", type="
                        + entityType + ")");
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
                logger.debug("[" + this.getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                
                if (rowAttrPersistence) {
                    logger.info("[" + this.getName() + "] Persisting data at OrionMySQLSink. Database: " + dbName
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
                logger.info("[" + this.getName() + "] Persisting data at OrionMySQLSink. Database: " + dbName
                        + ", Table: " + tableName + ", Timestamp: " + recvTime + ", Data (attrs): " + attrs.toString()
                        + ", (metadata): " + mds.toString());
                persistenceBackend.insertContextData(dbName, tableName, recvTime, attrs, mds);
            } // if
        } // for
    } // persist
    
} // OrionMySQLSink