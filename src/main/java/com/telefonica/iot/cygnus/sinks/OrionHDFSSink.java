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

import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackend;
import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.apache.flume.Context;

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

    private static final CygnusLogger LOGGER = new CygnusLogger(OrionHDFSSink.class);
    private String[] cosmosHost;
    private String cosmosPort;
    private String cosmosDefaultUsername;
    private String cosmosDefaultPassword;
    private String hdfsAPI;
    private boolean rowAttrPersistence;
    private String hiveHost;
    private String hivePort;
    private boolean krb5;
    private String krb5User;
    private String krb5Password;
    private String krb5LoginConfFile;
    private String krb5ConfFile;
    private HDFSBackend persistenceBackend;
    
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
     * Returns the persistence backend. It is protected due to it is only required for testing purposes.
     * @return The persistence backend
     */
    protected HDFSBackend getPersistenceBackend() {
        return persistenceBackend;
    } // getPersistenceBackend
    
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
        LOGGER.debug("[" + this.getName() + "] Reading configuration (cosmos_host=" + Arrays.toString(cosmosHost)
                + ")");
        cosmosPort = context.getString("cosmos_port", "14000");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (cosmos_port=" + cosmosPort + ")");
        cosmosDefaultUsername = context.getString("cosmos_default_username", "defaultCygnus");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (cosmos_default_username="
                + cosmosDefaultUsername + ")");
        // FIXME: cosmosPassword should be read as a SHA1 and decoded here
        cosmosDefaultPassword = context.getString("cosmos_default_password", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (cosmos_default_password="
                + cosmosDefaultPassword + ")");
        hdfsAPI = context.getString("hdfs_api", "httpfs");
        
        if (!hdfsAPI.equals("webhdfs") && !hdfsAPI.equals("httpfs")) {
            LOGGER.error("[" + this.getName() + "] Bad configuration (Unrecognized HDFS API " + hdfsAPI + ")");
            LOGGER.info("[" + this.getName() + "] Exiting Cygnus");
            System.exit(-1);
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (hdfs_api=" + hdfsAPI + ")");
        } // if else
        
        rowAttrPersistence = context.getString("attr_persistence", "row").equals("row");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_persistence="
                + (rowAttrPersistence ? "row" : "column") + ")");
        hiveHost = context.getString("hive_host", "localhost");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (hive_host=" + hiveHost + ")");
        hivePort = context.getString("hive_port", "10000");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (hive_port=" + hivePort + ")");
        krb5 = context.getBoolean("krb5_auth", false);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (krb5_auth=" + (krb5 ? "true" : "false")
                + ")");
        krb5User = context.getString("krb5_auth.krb5_user", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (krb5_user=" + krb5User + ")");
        krb5Password = context.getString("krb5_auth.krb5_password", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (krb5_password=" + krb5Password + ")");
        krb5LoginConfFile = context.getString("krb5_auth.krb5_login_conf_file", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (krb5_login_conf_file=" + krb5LoginConfFile
                + ")");
        krb5ConfFile = context.getString("krb5_auth.krb5_conf_file", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (krb5_conf_file=" + krb5ConfFile + ")");
    } // configure

    @Override
    public void start() {
        try {
            // create the persistence backend
            if (hdfsAPI.equals("httpfs")) {
                persistenceBackend = new HDFSBackendImpl(cosmosHost, cosmosPort, cosmosDefaultUsername,
                        cosmosDefaultPassword, hiveHost, hivePort, krb5, krb5User, krb5Password, krb5LoginConfFile,
                        krb5ConfFile);
                LOGGER.debug("[" + this.getName() + "] HttpFS persistence backend created");
            } else if (hdfsAPI.equals("webhdfs")) {
                persistenceBackend = new HDFSBackendImpl(cosmosHost, cosmosPort, cosmosDefaultUsername,
                        cosmosDefaultPassword, hiveHost, hivePort, krb5, krb5User, krb5Password, krb5LoginConfFile,
                        krb5ConfFile);
                LOGGER.debug("[" + this.getName() + "] WebHDFS persistence backend created");
            } else {
                // this point should never be reached since the HDFS API has been checked while configuring the sink
                LOGGER.error("[" + this.getName() + "] Bad configuration (Unrecognized HDFS API " + hdfsAPI
                        + ")");
                LOGGER.info("[" + this.getName() + "] Exiting Cygnus");
                System.exit(-1);
            } // if else if
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } // try catch // try catch
        
        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

    @Override
    void persist(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        // get some header values
        Long recvTimeTs = new Long(eventHeaders.get("timestamp"));
        String fiwareService = eventHeaders.get(Constants.HEADER_SERVICE);
        String fiwareServicePath = eventHeaders.get(Constants.HEADER_SERVICE_PATH);
        String[] destinations = eventHeaders.get(Constants.DESTINATION).split(",");
        
        // human readable version of the reception time
        String recvTime = Utils.getHumanReadable(recvTimeTs);
        
        // iterate on the contextResponses
        ArrayList contextResponses = notification.getContextResponses();
        
        for (int i = 0; i < contextResponses.size(); i++) {
            // get the i-th contextElement
            ContextElementResponse contextElementResponse = (ContextElementResponse) contextResponses.get(i);
            ContextElement contextElement = contextElementResponse.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + this.getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");
            
            // build the effective HDFS stuff
            String firstLevel = buildFirstLevel(fiwareService);
            String secondLevel = buildSecondLevel(fiwareServicePath);
            String thirdLevel = buildThirdLevel(destinations[i]);
            String hdfsFolder = firstLevel + "/" + secondLevel + "/" + thirdLevel;
            String hdfsFile = hdfsFolder + "/" + thirdLevel + ".txt";
            
            // check if the fileName exists in HDFS right now, i.e. when its attrName has been got
            boolean fileExists = false;
            
            if (persistenceBackend.exists(cosmosDefaultUsername, hdfsFile)) {
                fileExists = true;
            } // if
            
            // iterate on all this entity's attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();
            
            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                continue;
            } // if
            
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
                LOGGER.debug("[" + this.getName() + "] Processing context attribute (name=" + attrName + ", type="
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
                    LOGGER.info("[" + this.getName() + "] Persisting data at OrionHDFSSink. HDFS file ("
                            + hdfsFile + "), Data (" + rowLine + ")");
                    
                    // if the fileName exists, append the Json document to it; otherwise, create it with initial content
                    // and mark as existing (this avoids checking if the fileName exists each time a Json document is
                    // going to be persisted)
                    if (fileExists) {
                        persistenceBackend.append(cosmosDefaultUsername, hdfsFile, rowLine);
                    } else {
                        persistenceBackend.createDir(cosmosDefaultUsername, hdfsFolder);
                        persistenceBackend.createFile(cosmosDefaultUsername, hdfsFile, rowLine);
                        persistenceBackend.provisionHiveTable(cosmosDefaultUsername, hdfsFolder);
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
                LOGGER.info("[" + this.getName() + "] Persisting data at OrionHDFSSink. HDFS file (" + hdfsFile
                        + "), Data (" + columnLine + ")");
                
                if (fileExists) {
                    persistenceBackend.append(cosmosDefaultUsername, hdfsFile, columnLine);
                } else {
                    persistenceBackend.createDir(cosmosDefaultUsername, hdfsFolder);
                    persistenceBackend.createFile(cosmosDefaultUsername, hdfsFile, columnLine);
                    persistenceBackend.provisionHiveTable(cosmosDefaultUsername, hdfsFolder, hiveFields);
                    fileExists = true;
                } // if else
            } // if
        } // for
    } // persist
    
    /**
     * Builds the first level of a HDFS path given a fiwareService. It throws an exception if the naming conventions are
     * violated.
     * @param fiwareService
     * @return
     * @throws Exception
     */
    private String buildFirstLevel(String fiwareService) throws Exception {
        String firstLevel = fiwareService;
        
        if (firstLevel.length() > Constants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building firstLevel=fiwareService (fiwareService=" + fiwareService + ") "
                    + "and its length is greater than " + Constants.MAX_NAME_LEN);
        } // if
        
        return firstLevel;
    } // buildFirstLevel
    
    /**
     * Builds the second level of a HDFS path given given a fiwareService and a destination. It throws an exception if
     * the naming conventions are violated.
     * @param fiwareService
     * @param destination
     * @return
     * @throws Exception
     */
    private String buildSecondLevel(String fiwareServicePath) throws Exception {
        String secondLevel = fiwareServicePath;
        
        if (secondLevel.length() > Constants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building secondLevel=fiwareServicePath (" + fiwareServicePath + ") and "
                    + "its length is greater than " + Constants.MAX_NAME_LEN);
        } // if
        
        return secondLevel;
    } // buildSecondLevel

    /**
     * Builds the third level of a HDFS path given a destination. It throws an exception if the naming conventions are
     * violated.
     * @param destination
     * @return
     * @throws Exception
     */
    private String buildThirdLevel(String destination) throws Exception {
        String thirdLevel = destination;
        
        if (thirdLevel.length() > Constants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building thirdLevel=destination (" + destination + ") and its length is "
                    + "greater than " + Constants.MAX_NAME_LEN);
        } // if

        return thirdLevel;
    } // buildThirdLevel
    
} // OrionHDFSSink
