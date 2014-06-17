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
import es.tid.fiware.fiwareconnectors.cygnus.http.HttpClientFactory;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Utils;
import java.sql.Timestamp;
import java.util.ArrayList;

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
 *    -- File names format: <prefix_name><entity_id>-<entity_type>.txt
 *    -- File lines format: {"recvTimeTs":"XXX", "recvTime":"XXX", "entityId":"XXX", "entityType":"XXX",
 *                          "attrName":"XXX", "attrType":"XXX", "attrValue":"XXX"|{...}|[...],
 *                          "attrMd":[{"name":"XXX", "type":"XXX", "value":"XXX"}...]}
 * - Column-like persistence:
 *    -- File names format: <prefix_name><entity_id>-<entity_type>.txt
 *    -- File lines format: {"recvTime":"XXX", "<attr_name_1>":<attr_value_1>, ..., <attr_name_N>":<attr_value_N>,
 *                          "<attr_name_1>-md":<attr_md_1>, ..., "<attr_name_N>-md":<attr_md_N>}
 * 
 * As can be seen, in both persistence modes a file is created per each entity, containing all the historical values
 * this entity's attributes have had.
 * 
 * It is important to note that certain degree of reliability is achieved by using a rolling back mechanism in the
 * channel, i.e. an event is not removed from the channel until it is not appropriately persisted.
 */
public class OrionHDFSSink extends OrionSink {

    private Logger logger;
    private String cosmosHost;
    private String cosmosPort;
    private String cosmosDefaultUsername;
    private String cosmosDefaultPassword;
    private String hdfsAPI;
    private boolean rowAttrPersistence;
    private String namingPrefix;
    private String hivePort;
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
        logger = Logger.getLogger(OrionHDFSSink.class);
        cosmosHost = context.getString("cosmos_host", "localhost");
        logger.debug("Reading cosmos_host=" + cosmosHost);
        cosmosPort = context.getString("cosmos_port", "14000");
        logger.debug("Reading cosmos_port=" + cosmosPort);
        cosmosDefaultUsername = context.getString("cosmos_default_username", "defaultCygnus");
        logger.debug("Reading cosmos_default_username=" + cosmosDefaultUsername);
        // FIXME: cosmosPassword should be read as a SHA1 and decoded here
        cosmosDefaultPassword = context.getString("cosmos_default_password", "");
        logger.debug("Reading cosmos_default_password=" + cosmosDefaultPassword);
        hdfsAPI = context.getString("hdfs_api", "httpfs");
        logger.debug("Reading hdfs_api=" + hdfsAPI);
        rowAttrPersistence = context.getString("attr_persistence", "row").equals("row");
        logger.debug("Reading attr_persistence=" + (rowAttrPersistence ? "row" : "column"));
        namingPrefix = context.getString("naming_prefix", "");
        logger.debug("Reading naming_prefix=" + namingPrefix);
        hivePort = context.getString("hive_port", "10000");
        logger.debug("Reading hive_port=" + hivePort);
    } // configure

    @Override
    public void start() {
        // create a Http clients factory (no SSL)
        httpClientFactory = new HttpClientFactory(false);
        
        try {
            // create the persistence backend
            if (hdfsAPI.equals("httpfs")) {
                persistenceBackend = new HttpFSBackend(cosmosHost, cosmosPort, cosmosDefaultUsername,
                        cosmosDefaultPassword, hivePort);
                logger.debug("HttpFS persistence backend created");
            } else if (hdfsAPI.equals("webhdfs")) {
                persistenceBackend = new WebHDFSBackend(cosmosHost, cosmosPort, cosmosDefaultUsername,
                        cosmosDefaultPassword, hivePort);
                logger.debug("WebHDFS persistence backend created");
            } else {
                logger.error("Unrecognized HDFS API. The sink can start, but the data is not going to be persisted!");
            } // if else if

            if (persistenceBackend != null) {
                // create the HDFS dataset
//                logger.info("Creating /user/" + cosmosUsername + "/" + cosmosDataset);
//                persistenceBackend.createDir(httpClientFactory.getHttpClient(false), "");
                
                // provision the Hive external table for the above dataset if running in the "row" mode; otherwise, the
                // table creation must be delayed until the sink knows the structure of the table
                if (rowAttrPersistence) {
                    persistenceBackend.provisionHiveTable();
                } // if
            } // if
        } catch (Exception e) {
            logger.error(e.getMessage());
        } // try catch
        
        super.start();
    } // start

    @Override
    void persist(String organization, long recvTimeTs, ArrayList contextResponses) throws Exception {
        // human readable version of the reception time
        String recvTime = new Timestamp(recvTimeTs).toString().replaceAll(" ", "T");
        
        // iterate in the contextResponses
        for (int i = 0; i < contextResponses.size(); i++) {
            // get the i-th contextElement
            ContextElementResponse contextElementResponse = (ContextElementResponse) contextResponses.get(i);
            ContextElement contextElement = contextElementResponse.getContextElement();
            
            // get the name of the file
            String entityDescriptor = this.namingPrefix + Utils.encode(contextElement.getId()) + "-"
                    + Utils.encode(contextElement.getType());
            
            // having in mind the HDFS structure (hdfs:///user/<username>/<organization>/<entity>/<entity>.txt), create
            // the entity folder if not yet existing; this will create the username and the organization if not yet
            // existing as well
            // FIXME: current version of the notification only provides the organization, being null the username
            persistenceBackend.createDir(httpClientFactory.getHttpClient(false), null, organization + "/"
                    + entityDescriptor);
            
            // check if the file exists in HDFS right now, i.e. when its name has been got
            boolean fileExists = false;
            
            // FIXME: current version of the notification only provides the organization, being null the username
            if (persistenceBackend.exists(httpClientFactory.getHttpClient(false), null, organization + "/"
                    + entityDescriptor + "/" + entityDescriptor + ".txt")) {
                fileExists = true;
            } // if
            
            // iterate on all this entity's attributes and write a rowLine per each updated one
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();
            
            // this is used for storing the attribute's names and values when dealing with a per column attributes
            // persistence; in that case the persistence is not done attribute per attribute, but persisting all of them
            // at the same time
            String columnLine = "{\"" + Constants.RECV_TIME + "\":\"" + recvTime + "\",";

            for (int j = 0; j < contextAttributes.size(); j++) {
                // get the j-th contextAttribute
                ContextAttribute contextAttribute = contextAttributes.get(j);
                
                if (rowAttrPersistence) {
                    // create a Json document to be persisted
                    String rowLine = "{"
                            + "\"" + Constants.RECV_TIME_TS + "\":\"" + recvTimeTs / 1000 + "\","
                            + "\"" + Constants.RECV_TIME + "\":\"" + recvTime + "\","
                            + "\"" + Constants.ENTITY_ID + "\":\"" + contextElement.getId() + "\","
                            + "\"" + Constants.ENTITY_TYPE + "\":\"" + contextElement.getType() + "\","
                            + "\"" + Constants.ATTR_NAME + "\":\"" + contextAttribute.getName() + "\","
                            + "\"" + Constants.ATTR_TYPE + "\":\"" + contextAttribute.getType() + "\","
                            + "\"" + Constants.ATTR_VALUE + "\":" + contextAttribute.getContextValue(true) + ","
                            + "\"" + Constants.ATTR_MD + "\":" + contextAttribute.getContextMetadata()
                            + "}";
                    logger.info("Persisting data. File: " + entityDescriptor + ", Data: " + rowLine);
                    
                    // if the file exists, append the Json document to it; otherwise, create it with initial content and
                    // mark as existing (this avoids checking if the file exists each time a Json document is going to
                    // be persisted)
                    if (fileExists) {
                        // FIXME: current version of the notification only provides the organization, being null the
                        // username
                        persistenceBackend.append(httpClientFactory.getHttpClient(false), null, organization + "/"
                                + entityDescriptor + "/" + entityDescriptor + ".txt", rowLine);
                    } else {
                        // FIXME: current version of the notification only provides the organization, being null the
                        // username
                        persistenceBackend.createFile(httpClientFactory.getHttpClient(false), null, organization + "/"
                                + entityDescriptor + "/" + entityDescriptor + ".txt", rowLine);
                        fileExists = true;
                    } // if else
                } else {
                    columnLine += "\"" + contextAttribute.getName() + "\":" + contextAttribute.getContextValue(true)
                            + ", \"" + contextAttribute.getName() + "_md\":" + contextAttribute.getContextMetadata()
                            + ",";
                } // if else
            } // for
                 
            // if the attribute persistence mode is per column, now is the time to insert a new row containing full
            // attribute list of name-values.
            if (!rowAttrPersistence) {
                columnLine = columnLine.subSequence(0, columnLine.length() - 1) + "}";
                logger.info("Persisting data. File: " + entityDescriptor + ", Data: " + columnLine);
                
                if (fileExists) {
                    // FIXME: current version of the notification only provides the organization, being null the
                    // username
                    persistenceBackend.append(httpClientFactory.getHttpClient(false), null, organization + "/"
                            + entityDescriptor + "/" + entityDescriptor + ".txt", columnLine);
                } else {
                    // FIXME: current version of the notification only provides the organization, being null the
                    // username
                    persistenceBackend.createFile(httpClientFactory.getHttpClient(false), null, organization + "/"
                            + entityDescriptor + "/" + entityDescriptor + ".txt", columnLine);
                    fileExists = true;
                } // if else
            } // if
        } // for
    } // persist
    
} // OrionHDFSSink
