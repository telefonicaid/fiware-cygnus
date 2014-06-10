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

import es.tid.fiware.fiwareconnectors.cygnus.backends.hdfs.HDFSBackend;
import es.tid.fiware.fiwareconnectors.cygnus.backends.hdfs.HttpFSBackend;
import es.tid.fiware.fiwareconnectors.cygnus.backends.hdfs.WebHDFSBackend;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextAttribute;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElement;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import es.tid.fiware.fiwareconnectors.cygnus.hive.HiveClient;
import es.tid.fiware.fiwareconnectors.cygnus.http.HttpClientFactory;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.flume.Context;
import org.apache.log4j.Logger;

/**
 * 
 * @author frb
 * 
 * Custom HDFS sink for Orion Context Broker. There exists a default HDFS sink in Flume which serializes the data in
 * files, a file per event. This is not suitable for Orion, where the persisted files and its content must have specific
 * formats:
 *  - File names format: cygnus-<hdfs_user>-<hdfs_dataset>-<entity_id>-<entity_type>.txt
 *  - File lines format: {“ts”:”XXX”, “iso8601date”:”XXX”, “entityId”:”XXX”, “entityType”:”XXX”, “attrName”:”XXX”,
 *                       “attrType”:”XXX”, “attrValue":"XXX"|{...}|[...]}
 * 
 * As can be seen, a file is created per each entity, containing all the historical values this entity's attributes
 * have had.
 * 
 * It is important to note that certain degree of reliability is achieved by using a rolling back mechanism in the
 * channel, i.e. an event is not removed from the channel until it is not appropriately persisted.
 */
public class OrionHDFSSink extends OrionSink {

    private Logger logger;
    private String cosmosHost;
    private String cosmosPort;
    private String cosmosUsername;
    private String cosmosPassword;
    private String cosmosDataset;
    private String hdfsAPI;
    private boolean rowAttrPersistence;
    private String namingPrefix;
    private HDFSBackend persistenceBackend;
    private HttpClientFactory httpClientFactory;
    
    /**
     * Constructor.
     */
    public OrionHDFSSink() {
        super();
    } // OrionHDFSSink
    
    /**
     * Gets the Cosmos host. It is protected due to it is only required for testing purposes.
     * @return The Cosmos host
     */
    protected String getCosmosHost() {
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
     * Gets the Cosmos username. It is protected due to it is only required for testing purposes.
     * @return The Cosmos username
     */
    protected String getCosmosUsername() {
        return cosmosUsername;
    } // getCosmosUsername

    /**
     * Gets the Cosmos password. It is protected due to it is only required for testing purposes.
     * @return The Cosmos password
     */
    protected String getCosmosPassword() {
        return cosmosPassword;
    } // getCosmosPassword

    /**
     * Gets the Cosmos dataset. It is protected due to it is only required for testing purposes.
     * @return The Cosmos dataset
     */
    protected String getCosmosDataset() {
        return cosmosDataset;
    } // getCosmosDataset
    
    /**
     * Gets the HDFS API. It is protected due to it is only required for testing purposes.
     * @return The HDFS API
     */
    protected String getHDFSAPI() {
        return hdfsAPI;
    } // getHDFSAPI
    
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
        cosmosHost = context.getString("cosmos_host", "localhost");
        logger.debug("Reading cosmos_host=" + cosmosHost);
        cosmosPort = context.getString("cosmos_port", "14000");
        logger.debug("Reading cosmos_port=" + cosmosPort);
        cosmosUsername = context.getString("cosmos_username", "opendata");
        logger.debug("Reading cosmos_username=" + cosmosUsername);
        // FIXME: cosmosPassword should be read as a SHA1 and decoded here
        cosmosPassword = context.getString("cosmos_password", "unknown");
        logger.debug("Reading cosmos_password=" + cosmosPassword);
        cosmosDataset = context.getString("cosmos_dataset", "unknown");
        logger.debug("Reading cosmos_dataset=" + cosmosDataset);
        hdfsAPI = context.getString("hdfs_api", "httpfs");
        logger.debug("Reading hdfs_api=" + hdfsAPI);
        rowAttrPersistence = context.getString("attr_persistence", "row").equals("row");
        logger.debug("Reading attr_persistence=" + (rowAttrPersistence ? "row" : "column"));
        namingPrefix = context.getString("naming_prefix", "");
        logger.debug("Reading naming_prefix=" + namingPrefix);
    } // configure

    @Override
    public void start() {
        // create a Http clients factory (no SSL)
        httpClientFactory = new HttpClientFactory(false);
        
        // create the persistence backend
        if (hdfsAPI.equals("httpfs")) {
            persistenceBackend = new HttpFSBackend(cosmosHost, cosmosPort, cosmosUsername, cosmosDataset);
            logger.debug("HttpFS persistence backend created");
        } else if (hdfsAPI.equals("webhdfs")) {
            persistenceBackend = new WebHDFSBackend(cosmosHost, cosmosPort, cosmosUsername, cosmosDataset);
            logger.debug("WebHDFS persistence backend created");
        } else {
            logger.error("Unrecognized HDFS API. The sink can start, but the data is not going to be persisted!");
        } // if else if
        
        try {
            // FIXME: this could be moved to a "provision" method within the persistence backend, as OrionMySQLSink does
            // create (if not exists) the /user/myuser/mydataset folder and the related HiveQL external table
            if (persistenceBackend != null) {
                logger.info("Creating /user/" + cosmosUsername + "/" + cosmosDataset);
                persistenceBackend.createDir(httpClientFactory.getHttpClient(false), "");
                logger.info("Creating Hive external table " + cosmosUsername + "_"
                        + cosmosDataset.replaceAll("/", "_"));
                HiveClient hiveClient = new HiveClient(cosmosHost, "10000", cosmosUsername, cosmosPassword);
                String query = "create external table " + cosmosUsername + "_"
                        + cosmosDataset.replaceAll("/", "_") + " (ts bigint, iso8601date string, entityId string, "
                        + "entityType string, attrName string, attrType string, attrValue string) row format serde "
                        + "'org.openx.data.jsonserde.JsonSerDe' location '/user/" + cosmosUsername + "/" + cosmosDataset
                        + "'";
                
                if (!hiveClient.doCreateTable(query)) {
                    logger.warn("The HiveQL external table could not be created, but Cygnus can continue working... "
                            + "Check your Hive/Shark installation");
                } // if
            } // if
        } catch (Exception e) {
            logger.error(e.getMessage());
        } // try catch
        
        super.start();
    } // start

    @Override
    void persist(String organization, ArrayList contextResponses) throws Exception {
        // FIXME: organization is given in order to support multi-tenancy... should be used instead of the current
        // cosmosUsername
        
        // reception time FIXME: should be moved to the handler
        long ts = timeHelper.getTime();
        String iso8601date = timeHelper.getTimeString();
        
        // unlike the MySQL sink, the database has not to be created since this concept is represented by the HDFS user,
        // which userspace is already created under /user/myusername
        
        // iterate in the contextResponses
        for (int i = 0; i < contextResponses.size(); i++) {
            // get the i-th contextElement
            ContextElementResponse contextElementResponse = (ContextElementResponse) contextResponses.get(i);
            ContextElement contextElement = contextElementResponse.getContextElement();
            
            // get the name of the file
            String fileName = this.namingPrefix + Utils.encode(contextElement.getId()) + "-"
                    + Utils.encode(contextElement.getType()) + ".txt";
            
            // check if the file exists in HDFS right now, i.e. when its name has been got
            boolean fileExists = false;
            
            if (persistenceBackend.exists(httpClientFactory.getHttpClient(false), fileName)) {
                fileExists = true;
            } // if
            
            // iterate on all this entity's attributes and write a rowLine per each updated one
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();
            
            // this is used for storing the attribute's names and values when dealing with a per column attributes
            // persistence; in that case the persistence is not done attribute per attribute, but persisting all of them
            // at the same time
            String columnLine = "{\"recv_time\":\"" + iso8601date + "\",";

            for (int j = 0; j < contextAttributes.size(); j++) {
                // get the j-th contextAttribute
                ContextAttribute contextAttribute = contextAttributes.get(j);
                
                if (rowAttrPersistence) {
                    // create a Json document to be persisted
                    String rowLine = "{"
                            + "\"recv_time_ts\":\"" + timeHelper.getTime() + "\","
                            + "\"recv_time\":\"" + timeHelper.getTimeString() + "\","
                            + "\"entityId\":\"" + contextElement.getId() + "\","
                            + "\"entityType\":\"" + contextElement.getType() + "\","
                            + "\"attrName\":\"" + contextAttribute.getName() + "\","
                            + "\"attrType\":\"" + contextAttribute.getType() + "\","
                            + "\"attrValue\":" + contextAttribute.getContextValue(true)
                            + "}";
                    logger.info("Persisting data. File: " + fileName + ", Data: " + rowLine);
                    
                    // if the file exists, append the Json document to it; otherwise, create it with initial content and
                    // mark as existing (this avoids checking if the file exists each time a Json document is going to
                    // be persisted)
                    if (fileExists) {
                        persistenceBackend.append(httpClientFactory.getHttpClient(false), fileName, rowLine);
                    } else {
                        persistenceBackend.createFile(httpClientFactory.getHttpClient(false), fileName, rowLine);
                        fileExists = true;
                    } // if else
                } else {
                    columnLine += "\"" + contextAttribute.getName() + "\":" + contextAttribute.getContextValue(true)
                            + ",";
                } // if else
            } // for
                 
            // if the attribute persistence mode is per column, now is the time to insert a new row containing full
            // attribute list of name-values.
            if (!rowAttrPersistence) {
                columnLine = columnLine.subSequence(0, columnLine.length() - 1) + "}";
                logger.info("Persisting data. File: " + fileName + ", Data: " + columnLine);
                
                if (fileExists) {
                    persistenceBackend.append(httpClientFactory.getHttpClient(false), fileName, columnLine);
                } else {
                    persistenceBackend.createFile(httpClientFactory.getHttpClient(false), fileName, columnLine);
                    fileExists = true;
                } // if else
            } // if
        } // for
    } // persist
    
} // OrionHDFSSink
