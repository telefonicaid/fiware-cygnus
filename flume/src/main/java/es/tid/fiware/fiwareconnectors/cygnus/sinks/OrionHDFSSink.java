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

import es.tid.fiware.fiwareconnectors.cygnus.backends.hdfs.HDFSBackend;
import es.tid.fiware.fiwareconnectors.cygnus.backends.hdfs.HDFSBackendImpl;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextAttribute;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElement;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import es.tid.fiware.fiwareconnectors.cygnus.http.HttpClientFactory;
import es.tid.fiware.fiwareconnectors.cygnus.log.CygnusLogger;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Utils;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.apache.flume.Context;
import org.apache.log4j.Logger;

/**
 * 
 * @author frb
 * 
 * Custom HDFS sink for Orion Context Broker. There exists a default HDFS sink in Flume which serializes the data in
 * files, a file per event. This is not suitable for Orion, where the persisted files and its content must have specific
 * formats:
 *  - Row-like persistence:
 *    -- File names format: hdfs:///user/<default_username>/<organization>/<entityDescriptor>/<entityDescriptor>.txt
 *    -- File lines format: {"recvTimeTs":"XXX", "recvTime":"XXX", "entityId":"XXX", "entityType":"XXX",
 *                          "attrName":"XXX", "attrType":"XXX", "attrValue":"XXX"|{...}|[...],
 *                          "attrMd":[{"attrName":"XXX", "entityType":"XXX", "value":"XXX"}...]}
 * - Column-like persistence:
 *    -- File names format: hdfs:///user/<default_username>/<organization>/<entityDescriptor>/<entityDescriptor>.txt
 *    -- File lines format: {"recvTime":"XXX", "<attr_name_1>":<attr_value_1>, "<attr_name_1>_md":<attr_md_1>,...,
 *                          <attr_name_N>":<attr_value_N>, "<attr_name_N>_md":<attr_md_N>}
 * 
 * Being <entityDescriptor>=<prefix_name><entity_id>-<entity_type>
 * 
 * As can be seen, in both persistence modes a fileName is created per each entity, containing all the historical values
 * this entity's attributes have had.
 * 
 * It is important to note that certain degree of reliability is achieved by using a rolling back mechanism in the
 * channel, i.e. an event is not removed from the channel until it is not appropriately persisted.
 * 
 * In addition, Hive tables are created for each entity taking the data from:
 * 
 * hdfs:///user/<default_username>/<organization>/<entityDescriptor>/
 * 
 * The Hive tables have the following attrName:
 *  - Row-like persistence:
 *    -- Table names format: <default_username>_<organization>_<entitydescriptor>_row
 *    -- Column types: recvTimeTs string, recvType string, entityId string, entityType string, attrName string,
 *                     attrType string, attrValue string, attrMd array<string>
 * - Column-like persistence:
 *    -- Table names format: <default_username>_<organization>_<entitydescriptor>_column
 *    -- Column types: recvTime string, <attr_name_1> string, <attr_name_1>_md array<string>,...,
 *                     <attr_name_N> string, <attr_name_N>_md array<string>
 * 
 */
public class OrionHDFSSink extends OrionSink {

    private Logger logger;
    private String[] cosmosHost;
    private String cosmosPort;
    private String cosmosDefaultUsername;
    private String cosmosDefaultPassword;
    private String hdfsAPI;
    private boolean rowAttrPersistence;
    private String namingPrefix;
    private String hiveHost;
    private String hivePort;
    private HDFSBackend persistenceBackend;
    private HttpClientFactory httpClientFactory;
    
    /**
     * Constructor.
     */
    public OrionHDFSSink() {
        super();
        logger = CygnusLogger.getLogger(OrionHDFSSink.class);
    } // OrionHDFSSink
    
    /**
     * Gets the Cosmos host. It is protected due to it is only required for testing purposes.
     * @return The Cosmos host
     */
    protected String[] getCosmosHost() {
        return cosmosHost;
    } // getCosmosHost
    
    /**
     * Gets the Cosmos port. It is protected due to it is only required for testing purposes.
     * @return The Cosmos port
     */
    protected String getCosmosPort() {
        return cosmosPort;
    } // getCosmosPort

    /**
     * Gets the default Cosmos username. It is protected due to it is only required for testing purposes.
     * @return The default Cosmos username
     */
    protected String getCosmosDefaultUsername() {
        return cosmosDefaultUsername;
    } // getCosmosDefaultUsername
    
    /**
     * Gets the Cosmos password for the default username. It is protected due to it is only required for testing
     * purposes.
     * @return The Cosmos password for the detault Cosmos username
     */
    protected String getCosmosDefaultPassword() {
        return cosmosDefaultPassword;
    } // getCosmosDefaultPassword
    
    /**
     * Gets the HDFS API. It is protected due to it is only required for testing purposes.
     * @return The HDFS API
     */
    protected String getHDFSAPI() {
        return hdfsAPI;
    } // getHDFSAPI
    
    /**
     * Gets the Hive port. It is protected due to it is only required for testing purposes.
     * @return The Hive port
     */
    protected String getHivePort() {
        return hivePort;
    } // getHivePort
    
    /**
     * Gets the Http client factory. It is protected due to it is only required for testing purposes.
     * @return The Http client factory
     */
    protected HttpClientFactory getHttpClientFactory() {
        return httpClientFactory;
    } // getHttpClientFactory
    
    /**
     * Returns the persistence backend. It is protected due to it is only required for testing purposes.
     * @return The persistence backend
     */
    protected HDFSBackend getPersistenceBackend() {
        return persistenceBackend;
    } // getPersistenceBackend
    
    /**
     * Sets the Http client factory. It is protected due to it is only required for testing purposes.
     * @param httpClientFactory
     */
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    } // setHttpClientFactory
    
    /**
     * Sets the persistence backend. It is protected due to it is only required for testing purposes.
     * @param persistenceBackend
     */
    protected void setPersistenceBackend(HDFSBackend persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    } // setPersistenceBackend
       
    @Override
    public void configure(Context context) {
        cosmosHost = context.getString("cosmos_host", "localhost").split(",");
        logger.debug("[" + this.getName() + "] Reading configuration (cosmos_host=" + Arrays.toString(cosmosHost)
                + ")");
        cosmosPort = context.getString("cosmos_port", "14000");
        logger.debug("[" + this.getName() + "] Reading configuration (cosmos_port=" + cosmosPort + ")");
        cosmosDefaultUsername = context.getString("cosmos_default_username", "defaultCygnus");
        logger.debug("[" + this.getName() + "] Reading configuration (cosmos_default_username=" + cosmosDefaultUsername
                + ")");
        // FIXME: cosmosPassword should be read as a SHA1 and decoded here
        cosmosDefaultPassword = context.getString("cosmos_default_password", "");
        logger.debug("[" + this.getName() + "] Reading configuration (cosmos_default_password=" + cosmosDefaultPassword
                + ")");
        hdfsAPI = context.getString("hdfs_api", "httpfs");
        
        if (!hdfsAPI.equals("webhdfs") && !hdfsAPI.equals("httpfs")) {
            logger.error("[" + this.getName() + "] Bad configuration (Unrecognized HDFS API " + hdfsAPI + ")");
            logger.info("[" + this.getName() + "] Exiting Cygnus");
            System.exit(-1);
        } else {
            logger.debug("[" + this.getName() + "] Reading configuration (hdfs_api=" + hdfsAPI + ")");
        } // if else
        
        rowAttrPersistence = context.getString("attr_persistence", "row").equals("row");
        logger.debug("[" + this.getName() + "] Reading configuration (attr_persistence="
                + (rowAttrPersistence ? "row" : "column") + ")");
        namingPrefix = context.getString("naming_prefix", "");
        
        if (namingPrefix.length() > Constants.NAMING_PREFIX_MAX_LEN) {
            logger.error("[" + this.getName() + "] Bad configuration (Naming prefix length is greater than "
                    + Constants.NAMING_PREFIX_MAX_LEN
                    + ")");
            logger.info("[" + this.getName() + "] Exiting Cygnus");
            System.exit(-1);
        } // if
        
        logger.debug("[" + this.getName() + "] Reading configuration (naming_prefix=" + namingPrefix + ")");
        hiveHost = context.getString("hive_host", "localhost");
        logger.debug("[" + this.getName() + "] Reading configuration (hive_host=" + hiveHost + ")");
        hivePort = context.getString("hive_port", "10000");
        logger.debug("[" + this.getName() + "] Reading configuration (hive_port=" + hivePort + ")");
    } // configure

    @Override
    public void start() {
        // create a Http clients factory (no SSL)
        httpClientFactory = new HttpClientFactory(false);
        
        try {
            // create the persistence backend
            if (hdfsAPI.equals("httpfs")) {
                persistenceBackend = new HDFSBackendImpl(cosmosHost, cosmosPort, cosmosDefaultUsername,
                        cosmosDefaultPassword, hiveHost, hivePort);
                logger.debug("[" + this.getName() + "] HttpFS persistence backend created");
            } else if (hdfsAPI.equals("webhdfs")) {
                persistenceBackend = new HDFSBackendImpl(cosmosHost, cosmosPort, cosmosDefaultUsername,
                        cosmosDefaultPassword, hiveHost, hivePort);
                logger.debug("[" + this.getName() + "] WebHDFS persistence backend created");
            } else {
                // this point should never be reached since the HDFS API has been checked while configuring the sink
                logger.error("[" + this.getName() + "] Bad configuration (Unrecognized HDFS API " + hdfsAPI + ")");
                logger.info("[" + this.getName() + "] Exiting Cygnus");
                System.exit(-1);
            } // if else if
        } catch (Exception e) {
            logger.error(e.getMessage());
        } // try catch
        
        super.start();
        logger.info("[" + this.getName() + "] Startup completed");
    } // start

    @Override
    void persist(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        // get some header values
        Long recvTimeTs = new Long(eventHeaders.get("timestamp")).longValue();
        String organization = eventHeaders.get(Constants.ORG_HEADER);
        String fileName = this.namingPrefix + eventHeaders.get(Constants.DESTINATION);
        
        // human readable version of the reception time
        String recvTime = new Timestamp(recvTimeTs).toString().replaceAll(" ", "T");
        
        // iterate in the contextResponses
        ArrayList contextResponses = notification.getContextResponses();
        
        for (int i = 0; i < contextResponses.size(); i++) {
            // get the i-th contextElement
            ContextElementResponse contextElementResponse = (ContextElementResponse) contextResponses.get(i);
            ContextElement contextElement = contextElementResponse.getContextElement();
            String entityId = Utils.encode(contextElement.getId());
            String entityType = Utils.encode(contextElement.getType());
            logger.debug("[" + this.getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");
            
            // check if the fileName exists in HDFS right now, i.e. when its attrName has been got
            boolean fileExists = false;
            
            // FIXME: current version of the notification only provides the organization, being null the username
            if (persistenceBackend.exists(httpClientFactory.getHttpClient(false), null, organization + "/"
                    + fileName + "/" + fileName + ".txt")) {
                fileExists = true;
            } // if
            
            // iterate on all this entity's attributes and write a rowLine per each updated one
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();
            
            // this is used for storing the attribute's names and values in a Json-like way when dealing with a per
            // column attributes persistence; in that case the persistence is not done attribute per attribute, but
            // persisting all of them at the same time
            String columnLine = "{\"" + Constants.RECV_TIME + "\":\"" + recvTime + "\",";
            
            // this is used for storing the attribute's names needed by Hive in order to create the table when dealing
            // with a per column attributes persistence; in that case the Hive table creation is not done using
            // standard 8-fields but a variable number of them
            String hiveFields = Constants.RECV_TIME + " string";

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMetadata = contextAttribute.getContextMetadata();
                logger.debug("[" + this.getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                
                if (rowAttrPersistence) {
                    // create a Json document to be persisted
                    String rowLine = "{"
                            + "\"" + Constants.RECV_TIME_TS + "\":\"" + recvTimeTs / 1000 + "\","
                            + "\"" + Constants.RECV_TIME + "\":\"" + recvTime + "\","
                            + "\"" + Constants.ENTITY_ID + "\":\"" + entityId + "\","
                            + "\"" + Constants.ENTITY_TYPE + "\":\"" + entityType + "\","
                            + "\"" + Constants.ATTR_NAME + "\":\"" + attrName + "\","
                            + "\"" + Constants.ATTR_TYPE + "\":\"" + attrType + "\","
                            + "\"" + Constants.ATTR_VALUE + "\":" + attrValue + ","
                            + "\"" + Constants.ATTR_MD + "\":" + attrMetadata
                            + "}";
                    logger.info("[" + this.getName() + "] Persisting data at OrionHDFSSink. HDFS file ("
                            + fileName + "), Data (" + rowLine + ")");
                    
                    // if the fileName exists, append the Json document to it; otherwise, create it with initial content and
                    // mark as existing (this avoids checking if the fileName exists each time a Json document is going to
                    // be persisted)
                    if (fileExists) {
                        // FIXME: current version of the notification only provides the organization, being null the
                        // username
                        persistenceBackend.append(httpClientFactory.getHttpClient(false), null, organization + "/"
                                + fileName + "/" + fileName + ".txt", rowLine);
                    } else {
                        // having in mind the HDFS structure:
                        // hdfs:///user/<username>/<organization>/<entityDescriptor>/<entityDescriptor>.txt
                        //
                        // 1. create the entity folder if not yet existing
                        // FIXME: current version of the notification only provides the organization, being null the
                        // username
                        persistenceBackend.createDir(httpClientFactory.getHttpClient(false), null, organization + "/"
                                + fileName);
                        // 2. create the entity fileName
                        // FIXME: current version of the notification only provides the organization, being null the
                        // username
                        persistenceBackend.createFile(httpClientFactory.getHttpClient(false), null, organization + "/"
                                + fileName + "/" + fileName + ".txt", rowLine);
                        // 3. create the 8-fields standard Hive table
                        persistenceBackend.provisionHiveTable(organization, fileName);
                        fileExists = true;
                    } // if else
                } else {
                    columnLine += "\"" + attrName + "\":" + attrValue + ", \"" + attrName + "_md\":" + attrMetadata
                            + ",";
                    hiveFields += "," + attrName + " string," + attrName + "_md array<string>";
                } // if else
            } // for
                 
            // if the attribute persistence mode is per column, now is the time to insert a new row containing full
            // attribute list
            if (!rowAttrPersistence) {
                // insert a new row containing full attribute list
                columnLine = columnLine.subSequence(0, columnLine.length() - 1) + "}";
                logger.info("[" + this.getName() + "] Persisting data at OrionHDFSSink. HDFS file (" + fileName
                        + "), Data (" + columnLine + ")");
                
                if (fileExists) {
                    // FIXME: current version of the notification only provides the organization, being null the
                    // username
                    persistenceBackend.append(httpClientFactory.getHttpClient(false), null, organization + "/"
                            + fileName + "/" + fileName + ".txt", columnLine);
                } else {
                    // having in mind the HDFS structure:
                    // hdfs:///user/<username>/<organization>/<entityDescriptor>/<entityDescriptor>.txt
                    //
                    // 1. create the entity folder if not yet existing
                    // FIXME: current version of the notification only provides the organization, being null the
                    // username
                    persistenceBackend.createDir(httpClientFactory.getHttpClient(false), null, organization + "/"
                            + fileName);
                    // 2. create the entity fileName
                    // FIXME: current version of the notification only provides the organization, being null the
                    // username
                    persistenceBackend.createFile(httpClientFactory.getHttpClient(false), null, organization + "/"
                            + fileName + "/" + fileName + ".txt", columnLine);
                    // 3. create the Hive table with a variable number of fields
                    persistenceBackend.provisionHiveTable(organization, fileName, hiveFields);
                    fileExists = true;
                } // if else
            } // if
        } // for
    } // persist
    
} // OrionHDFSSink
