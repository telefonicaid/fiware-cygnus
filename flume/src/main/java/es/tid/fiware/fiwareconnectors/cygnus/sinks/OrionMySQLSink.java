/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * frb@tid.es
 */

package es.tid.fiware.fiwareconnectors.cygnus.sinks;

import es.tid.fiware.fiwareconnectors.cygnus.backends.mysql.MySQLBackend;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextAttribute;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElement;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Utils;
import java.util.ArrayList;
import org.apache.flume.Context;
import org.apache.log4j.Logger;

/**
 *
 * @author frb
 * 
 * Custom MySQL sink for Orion Context Broker. The MySQL design for this sink is:
 *  - There is a database per user, being its name:
 *    cygnus_<username>
 *  - Each entity has its data stored in a specific table, being its name:
 *    cygnus_<entity_id>_<entity_type>
 *  - Each event data is stored in the appropriate table as a new row, having each row the following fields:
 *    ts, iso8601date, entityId, entityType, attrName, attrType, attrValue
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
    
    /**
     * Sets the time helper. It is protected due to it is only required for testing purposes.
     * @param timeHelper
     */
    protected void setTimeHelper(TimeHelper timeHelper) {
        this.timeHelper = timeHelper;
    } // setTimeHelper
    
    @Override
    public void configure(Context context) {
        logger = Logger.getLogger(OrionHDFSSink.class);
        mysqlHost = context.getString("mysql_host", "localhost");
        logger.debug("Reading mysql_host=" + mysqlHost);
        mysqlPort = context.getString("mysql_port", "3306");
        logger.debug("Reading mysql_port=" + mysqlPort);
        mysqlUsername = context.getString("mysql_username", "opendata");
        logger.debug("Reading mysql_username=" + mysqlUsername);
        // FIXME: cosmosPassword should be read as a SHA1 and decoded here
        mysqlPassword = context.getString("mysql_password", "unknown");
        logger.debug("Reading mysql_password=" + mysqlPassword);
    } // configure

    @Override
    public void start() {
        // create the persistence backend
        logger.debug("Creating the MySQL persistence backend");
        persistenceBackend = new MySQLBackend(mysqlHost, mysqlPort, mysqlUsername, mysqlPassword);
        super.start();
    } // start

    @Override
    void persist(String username, ArrayList contextResponses) throws Exception {
        // FIXME: username is given in order to support multi-tenancy... should be used instead of the current
        // cosmosUsername
            
        // create the database for this user if not existing yet... the cost of trying to create it is the same than
        // checking if it exits and then creating it
        String dbName = "cygnus_" + mysqlUsername;
        persistenceBackend.createDatabase(dbName);
        
        // iterate in the contextResponses
        for (int i = 0; i < contextResponses.size(); i++) {
            // get the i-th contextElement
            ContextElementResponse contextElementResponse = (ContextElementResponse) contextResponses.get(i);
            ContextElement contextElement = contextElementResponse.getContextElement();
            String id = Utils.encode(contextElement.getId());
            String type = Utils.encode(contextElement.getType());
            
            // create the table for this entity if not existing yet... the cost of trying yo create it is the same than
            // checking if it exits and then creating it
            String tableName = "cygnus_" + id + "_" + type;
            persistenceBackend.createTable(dbName, tableName);
            
            // iterate on all this entity's attributes and insert a row per each updated one
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            for (int j = 0; j < contextAttributes.size(); j++) {
                // get the j-th contextAttribute
                ContextAttribute contextAttribute = contextAttributes.get(j);
                
                // insert a new row
                long ts = timeHelper.getTime();
                String iso8601date = timeHelper.getTimeString();
                logger.info("Persisting data. Database: " + dbName + ", Table: " + tableName + ", Row: " + ts + ","
                        + iso8601date + "," + contextElement.getId() + "," + contextElement.getType() + ","
                        + contextAttribute.getName() + "," + contextAttribute.getType() + ","
                        + contextAttribute.getContextValue());
                persistenceBackend.insertContextData(
                        dbName,
                        tableName,
                        ts,
                        iso8601date,
                        contextElement.getId(),
                        contextElement.getType(),
                        contextAttribute.getName(),
                        contextAttribute.getType(),
                        contextAttribute.getContextValue());
            } // for
        } // for
    } // persist
    
} // OrionMySQLSink