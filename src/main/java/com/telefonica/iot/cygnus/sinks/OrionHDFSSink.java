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
    
    /**
     * Supported file formats when writting to HDFS.
     */
    private enum FileFormat { JSONROW, CSVROW, JSONCOLUMN, CSVCOLUMN };

    private static final CygnusLogger LOGGER = new CygnusLogger(OrionHDFSSink.class);
    private String[] host;
    private String port;
    private String username;
    private FileFormat fileFormat;
    private String oauth2Token;
    private String hiveHost;
    private String hivePort;
    private boolean krb5;
    private String krb5User;
    private String krb5Password;
    private String krb5LoginConfFile;
    private String krb5ConfFile;
    private boolean serviceAsNamespace;
    private HDFSBackendImpl persistenceBackend;
    
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
        return host;
    } // getCosmosHost
    
    /**
     * Gets the Cosmos port. It is protected due to it is only required for testing purposes.
     * @return The Cosmos port
     */
    protected String getCosmosPort() {
        return port;
    } // getCosmosPort

    /**
     * Gets the default Cosmos username. It is protected due to it is only required for testing purposes.
     * @return The default Cosmos username
     */
    protected String getCosmosDefaultUsername() {
        return username;
    } // getCosmosDefaultUsername
    
    /**
     * Gets the OAuth2 token used for authentication and authorization. It is protected due to it is only required
     * for testing purposes.
     * @return The Cosmos oauth2Token for the detault Cosmos username
     */
    protected String getOAuth2Token() {
        return oauth2Token;
    } // getOAuth2Token
    
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
    protected void setPersistenceBackend(HDFSBackendImpl persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    } // setPersistenceBackend
       
    @Override
    public void configure(Context context) {
        String cosmosHost = context.getString("cosmos_host");
        String hdfsHost = context.getString("hdfs_host");
        
        if (hdfsHost != null && hdfsHost.length() > 0) {
            host = hdfsHost.split(",");
            LOGGER.debug("[" + this.getName() + "] Reading configuration (hdfs_host=" + Arrays.toString(host) + ")");
        } else if (cosmosHost != null && cosmosHost.length() > 0) {
            host = cosmosHost.split(",");
            LOGGER.debug("[" + this.getName() + "] Reading configuration (cosmos_host=" + Arrays.toString(host) + ")"
                    + " -- DEPRECATED, use hdfs_host instead");
        } else {
            host = new String[]{"localhost"};
            LOGGER.debug("[" + this.getName() + "] Defaulting to hdfs_host=localhost");
        } // if else
        
        String cosmosPort = context.getString("cosmos_port");
        String hdfsPort = context.getString("hdfs_port");
        
        if (hdfsPort != null && hdfsPort.length() > 0) {
            port = hdfsPort;
            LOGGER.debug("[" + this.getName() + "] Reading configuration (hdfs_port=" + port + ")");
        } else if (cosmosPort != null && cosmosPort.length() > 0) {
            port = cosmosPort;
            LOGGER.debug("[" + this.getName() + "] Reading configuration (cosmos_port=" + port + ")"
                    + " -- DEPRECATED, use hdfs_port instead");
        } else {
            port = "14000";
            LOGGER.debug("[" + this.getName() + "] Defaulting to hdfs_port=14000");
        }
        
        String cosmosDefaultUsername = context.getString("cosmos_default_username");
        String hdfsUsername = context.getString("hdfs_username");
        
        if (hdfsUsername != null && hdfsUsername.length() > 0) {
            username = hdfsUsername;
            LOGGER.debug("[" + this.getName() + "] Reading configuration (hdfs_username=" + username + ")");
        } else if (cosmosDefaultUsername != null && cosmosDefaultUsername.length() > 0) {
            username = cosmosDefaultUsername;
            LOGGER.debug("[" + this.getName() + "] Reading configuration (cosmos_default_username=" + username + ")"
                    + " -- DEPRECATED, use hdfs_username instead");
        } else {
            LOGGER.error("[" + this.getName() + "] No username provided. Cygnus can continue, but HDFS sink will not "
                    + "properly work!");
        } // if else
        
        oauth2Token = context.getString("oauth2_token");
        
        if (oauth2Token != null && oauth2Token.length() > 0) {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (oauth2_token=" + this.oauth2Token + ")");
        } else {
            LOGGER.error("[" + this.getName() + "] No OAuth2 token provided. Cygnus can continue, but HDFS sink may "
                    + "not properly work if WebHDFS service is protected with such an authentication and "
                    + "authorization mechanism!");
        } // if else
        
        fileFormat = FileFormat.valueOf(context.getString("file_format", "row").replaceAll("-", "").toUpperCase());
        LOGGER.debug("[" + this.getName() + "] Reading configuration (file_format=" + fileFormat + ")");
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
        serviceAsNamespace = context.getBoolean("service_as_namespace", false);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (service_as_namespace=" + serviceAsNamespace
                + ")");
    } // configure

    @Override
    public void start() {
        try {
            // create the persistence backend
            persistenceBackend = new HDFSBackendImpl(host, port, username, oauth2Token, hiveHost, hivePort, krb5,
                    krb5User, krb5Password, krb5LoginConfFile, krb5ConfFile, serviceAsNamespace);
            LOGGER.debug("[" + this.getName() + "] HDFS persistence backend created");
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
        String[] fiwareServicePaths = eventHeaders.get(Constants.HEADER_SERVICE_PATH).split(",");
        String[] destinations = eventHeaders.get(Constants.DESTINATION).split(",");
        
        // human readable version of the reception time
        String recvTime = Utils.getHumanReadable(recvTimeTs, true);
        
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
            String secondLevel = buildSecondLevel(fiwareServicePaths[i]);
            String thirdLevel = buildThirdLevel(destinations[i]);
            String hdfsFolder = firstLevel + "/" + secondLevel + "/" + thirdLevel;
            String hdfsFile = hdfsFolder + "/" + thirdLevel + ".txt";
            
            // check if the fileName exists in HDFS right now, i.e. when its attrName has been got
            boolean fileExists = false;
            
            if (persistenceBackend.exists(hdfsFile)) {
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
            String columnLine;
            
            if (fileFormat == FileFormat.JSONCOLUMN) {
                columnLine = "{\"" + Constants.RECV_TIME + "\":\"" + recvTime + "\",";
            } else if (fileFormat == FileFormat.CSVCOLUMN) {
                columnLine = recvTime + " ";
            } else {
                columnLine = "";
            } // if else
            
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
                
                if (fileFormat == FileFormat.JSONROW || fileFormat == FileFormat.CSVROW) {
                    String rowLine = createRow(fileFormat, recvTimeTs, recvTime, entityId, entityType, attrName,
                            attrType, attrValue, attrMetadata);
                    
                    LOGGER.info("[" + this.getName() + "] Persisting data at OrionHDFSSink. HDFS file ("
                            + hdfsFile + "), Data (" + rowLine + ")");
                    
                    // if the fileName exists, append the Json document to it; otherwise, create it with initial content
                    // and mark as existing (this avoids checking if the fileName exists each time a Json document is
                    // going to be persisted)
                    if (fileExists) {
                        persistenceBackend.append(hdfsFile, rowLine);
                    } else {
                        persistenceBackend.createDir(hdfsFolder);
                        persistenceBackend.createFile(hdfsFile, rowLine);
                        persistenceBackend.provisionHiveTable(hdfsFolder);
                        fileExists = true;
                    } // if else
                } else if (fileFormat == FileFormat.JSONCOLUMN || fileFormat == FileFormat.CSVCOLUMN) {
                    columnLine += createColumn(fileFormat, attrName, attrValue, attrMetadata);
                    hiveFields += "," + attrName + " string," + attrName + "_md array<string>";
                } // if else
            } // for
                 
            // if the attribute persistence mode is per column, now is the time to insert a new row containing full
            // attribute list
            if (fileFormat == FileFormat.JSONCOLUMN || fileFormat == FileFormat.CSVCOLUMN) {
                // replace/remove the last character of the string (',' is replaced by '}' for JSON,
                // ' ' is removed for CSV)
                columnLine = truncateColumn(fileFormat, columnLine);
                
                // insert a new row containing full attribute list
                LOGGER.info("[" + this.getName() + "] Persisting data at OrionHDFSSink. HDFS file (" + hdfsFile
                        + "), Data (" + columnLine + ")");
                
                if (fileExists) {
                    persistenceBackend.append(hdfsFile, columnLine);
                } else {
                    persistenceBackend.createDir(hdfsFolder);
                    persistenceBackend.createFile(hdfsFile, columnLine);
                    persistenceBackend.provisionHiveTable(hdfsFolder, hiveFields);
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
    
    private String createRow(FileFormat fileFormat, long recvTimeTs, String recvTime, String entityId,
            String entityType, String attrName, String attrType, String attrValue, String attrMetadata) {
        if (fileFormat == FileFormat.JSONROW) {
            return "{"
                    + "\"" + Constants.RECV_TIME_TS + "\":\"" + recvTimeTs / 1000 + "\","
                    + "\"" + Constants.RECV_TIME + "\":\"" + recvTime + "\","
                    + "\"" + Constants.ENTITY_ID + "\":\"" + entityId + "\","
                    + "\"" + Constants.ENTITY_TYPE + "\":\"" + entityType + "\","
                    + "\"" + Constants.ATTR_NAME + "\":\"" + attrName + "\","
                    + "\"" + Constants.ATTR_TYPE + "\":\"" + attrType + "\","
                    + "\"" + Constants.ATTR_VALUE + "\":" + attrValue + ","
                    + "\"" + Constants.ATTR_MD + "\":" + attrMetadata
                    + "}";
        } else if (fileFormat == FileFormat.CSVROW) {
            return recvTimeTs / 1000 + " "
                    + recvTime + " "
                    + entityId + " "
                    + entityType + " "
                    + attrName + " "
                    + attrType + " "
                    + attrValue + " "
                    + attrMetadata;
        } else {
            return "";
        } // if else
    } // createRow

    private String createColumn(FileFormat fileFormat, String attrName, String attrValue, String attrMetadata) {
        if (fileFormat == FileFormat.JSONCOLUMN) {
            return "\"" + attrName + "\":" + attrValue + ", \"" + attrName + "_md\":" + attrMetadata + ",";
        } else if (fileFormat == FileFormat.CSVCOLUMN) {
            return attrValue + " " + attrMetadata + " ";
        } else {
            return "";
        } // if else
    } // createJSONColumn
    
    private String truncateColumn(FileFormat fileFormat, String columnLine) {
        if (fileFormat == FileFormat.JSONCOLUMN) {
            return columnLine.substring(0, columnLine.length() - 1) + "}";
        } else if (fileFormat == FileFormat.CSVCOLUMN) {
            return columnLine.substring(0, columnLine.length() - 1);
        } else {
            return columnLine;
        } // if else
    } // truncateColumn

} // OrionHDFSSink
