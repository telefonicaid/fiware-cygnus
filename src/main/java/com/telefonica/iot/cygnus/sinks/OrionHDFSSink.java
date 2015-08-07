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
import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImpl.FileFormat;
import static com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImpl.FileFormat.CSVCOLUMN;
import static com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImpl.FileFormat.CSVROW;
import static com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImpl.FileFormat.JSONCOLUMN;
import static com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImpl.FileFormat.JSONROW;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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
    protected String[] getHDFSHosts() {
        return host;
    } // getHDFSHosts
    
    /**
     * Gets the Cosmos port. It is protected due to it is only required for testing purposes.
     * @return The Cosmos port
     */
    protected String getHDFSPort() {
        return port;
    } // getHDFSPort

    /**
     * Gets the default Cosmos username. It is protected due to it is only required for testing purposes.
     * @return The default Cosmos username
     */
    protected String getHDFSUsername() {
        return username;
    } // getHDFSUsername
    
    /**
     * Gets the OAuth2 token used for authentication and authorization. It is protected due to it is only required
     * for testing purposes.
     * @return The Cosmos oauth2Token for the detault Cosmos username
     */
    protected String getOAuth2Token() {
        return oauth2Token;
    } // getOAuth2Token
    
    /**
     * Returns if the service is used as HDFS namespace. It is protected due to it is only required for testing
     * purposes.
     * @return "true" if the service is used as HDFS namespace, "false" otherwise.
     */
    protected String getServiceAsNamespace() {
        return (serviceAsNamespace ? "true" : "false");
    } // getServiceAsNamespace
    
    /**
     * Gets the file format. It is protected due to it is only required for testing purposes.
     * @return The file format
     */
    protected String getFileFormat() {
        switch (fileFormat) {
            case JSONROW:
                return "json-row";
            case JSONCOLUMN:
                return "json-column";
            case CSVROW:
                return "csv-row";
            case CSVCOLUMN:
                return "csv-column";
            default:
                return "";
        } // switch;
    } // getFileFormat
    
    /**
     * Gets the Hive host. It is protected due to it is only required for testing purposes.
     * @return The Hive port
     */
    protected String getHiveHost() {
        return hiveHost;
    } // getHiveHost
    
    /**
     * Gets the Hive port. It is protected due to it is only required for testing purposes.
     * @return The Hive port
     */
    protected String getHivePort() {
        return hivePort;
    } // getHivePort

    /**
     * Returns if Kerberos is being used for authenticacion. It is protected due to it is only required for testing
     * purposes.
     * @return "true" if Kerberos is being used for authentication, otherwise "false"
     */
    protected String getKrb5Auth() {
        return (krb5 ? "true" : "false");
    } // getKrb5Auth
    
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
        
        boolean rowAttrPersistenceConfigured = context.getParameters().containsKey("attr_persistence");
        boolean fileFormatConfigured = context.getParameters().containsKey("file_format");
        
        if (fileFormatConfigured) {
            fileFormat = FileFormat.valueOf(context.getString("file_format").replaceAll("-", "").toUpperCase());
            LOGGER.debug("[" + this.getName() + "] Reading configuration (file_format=" + fileFormat + ")");
        } else if (rowAttrPersistenceConfigured) {
            boolean rowAttrPersistence = context.getString("attr_persistence").equals("row");
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_persistence="
                + (rowAttrPersistence ? "row" : "column") + ") -- DEPRECATED, converting to file_format="
                + (rowAttrPersistence ? "json-row" : "json-column"));
            fileFormat = (rowAttrPersistence ? FileFormat.JSONROW : FileFormat.JSONCOLUMN);
        } else {
            fileFormat = FileFormat.JSONROW;
            LOGGER.debug("[" + this.getName() + "] Defaulting to file_format=json-row");
        } // if else if

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
            
            // check if the file exists in HDFS
            boolean dataFileExists = persistenceBackend.exists(hdfsFile);
            
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
                columnLine = "{\"" + Constants.RECV_TIME + "\":\"" + recvTime + "\"";
            } else if (fileFormat == FileFormat.CSVCOLUMN) {
                columnLine = recvTime;
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
                
                switch (fileFormat) {
                    case JSONROW:
                        // create a row and persist it right now
                        String rowLine = createRow(fileFormat, recvTimeTs, recvTime, entityId, entityType, attrName,
                                attrType, attrValue, attrMetadata);
                        persistData(rowLine, hdfsFolder, hdfsFile, dataFileExists);
                        
                        // Hive table is only created once the HDFS file is created
                        if (!dataFileExists) {
                            persistenceBackend.provisionHiveTable(fileFormat, hdfsFolder, "_row");
                            dataFileExists = true;
                        } // if
                        
                        break;
                    case JSONCOLUMN:
                        // "accumulate" the data for future persistence
                        columnLine += createColumn(fileFormat, attrName, attrValue, attrMetadata);
                        hiveFields += "," + attrName + " string," + attrName
                                + "_md array<struct<name:string,type:string,value:string>>";
                        break;
                    case CSVROW:
                        // build some metadata related stuff
                        String thirdLevelMd = buildThirdLevelMd(destinations[i], attrName, attrType);
                        String attrMdFolder = firstLevel + "/" + secondLevel + "/" + thirdLevelMd;
                        String attrMdFileName = attrMdFolder + "/" + thirdLevelMd + ".txt";
                        String printableAttrMdFileName = "hdfs:///user/" + this.username + attrMdFileName;
                        
                        // create a row and persist it right now
                        rowLine = createRow(fileFormat, recvTimeTs, recvTime, entityId, entityType, attrName,
                                attrType, attrValue.replaceAll("\"", ""), printableAttrMdFileName);
                        persistData(rowLine, hdfsFolder, hdfsFile, dataFileExists);
                        
                        // metadata is persisted in a separated HDFS file
                        boolean mdFileExists = persistenceBackend.exists(attrMdFileName);
                        persistCSVMetadata(attrMetadata, recvTimeTs, attrMdFolder, attrMdFileName, mdFileExists);
                        
                        // Hive table is only created once the HDFS file is created
                        if (!dataFileExists) {
                            persistenceBackend.provisionHiveTable(fileFormat, hdfsFolder, "_row");
                            dataFileExists = true;
                        } // if
                        
                        break;
                    case CSVCOLUMN:
                        // build some metadata related stuff
                        thirdLevelMd = buildThirdLevelMd(destinations[i], attrName, attrType);
                        attrMdFolder = firstLevel + "/" + secondLevel + "/" + thirdLevelMd;
                        attrMdFileName = attrMdFolder + "/" + thirdLevelMd + ".txt";
                        printableAttrMdFileName = "hdfs:///user/" + this.username + attrMdFileName;
                        
                        // "accumulate" the data for future persistence
                        columnLine += createColumn(fileFormat, attrName, attrValue.replaceAll("\"", ""),
                                printableAttrMdFileName);
                        hiveFields += "," + attrName + " string," + attrName + "_md_file string";
                        
                        // metadata is persisted in a separated HDFS file
                        mdFileExists = persistenceBackend.exists(attrMdFileName);
                        persistCSVMetadata(attrMetadata, recvTimeTs, attrMdFolder, attrMdFileName, mdFileExists);
                        break;
                    default:
                        break;
                } // switch
            } // for
                 
            // if the attribute persistence mode is per column, now is the time to insert a new row containing full
            // attribute list
            switch (fileFormat) {
                case JSONCOLUMN:
                    persistData(columnLine + "}", hdfsFolder, hdfsFile, dataFileExists);
                    
                    // Hive table is only created once the file is created
                    if (!dataFileExists) {
                        persistenceBackend.provisionHiveTable(fileFormat, hdfsFolder, hiveFields, "_column");
                    } // if
                    
                    break;
                case CSVCOLUMN:
                    persistData(columnLine, hdfsFolder, hdfsFile, dataFileExists);
                    
                    // Hive table is only created once the file is created
                    if (!dataFileExists) {
                        persistenceBackend.provisionHiveTable(fileFormat, hdfsFolder, hiveFields, "_column");
                    } // if
                    
                    break;
                default:
                    break;
            } // switch
        } // for
    } // persist

    /**
     * Persists String-based data (row or column like, JSON or CSV format) in the given HDFS file within the given
     * HDFS folder. In any of the HDFS elements exists, it is created.
     * @param data
     * @param hdfsFolder
     * @param hdfsFile
     * @param hdfsFileExists
     * @throws Exception
     */
    private void persistData(String data, String hdfsFolder, String hdfsFile, boolean hdfsFileExists)
        throws Exception {
        LOGGER.info("[" + this.getName() + "] Persisting data at OrionHDFSSink. HDFS file ("
                + hdfsFile + "), Data (" + data + ")");

        if (hdfsFileExists) {
            persistenceBackend.append(hdfsFile, data);
        } else {
            persistenceBackend.createDir(hdfsFolder);
            persistenceBackend.createFile(hdfsFile, data);
        } // if else
    } // persistData

    /**
     * Persists String-based metadata in CSV format in the given HDFS file within the given HDFS folder. In any of the
     * HDFS elements exists, it is created.
     * @param attrMetadata
     * @param recvTimeTs
     * @param hdfsFolder
     * @param hdfsFile
     * @param hdfsFileExists
     * @throws Exception
     */
    private void persistCSVMetadata(String attrMetadata, long recvTimeTs, String hdfsFolder, String hdfsFile,
            boolean hdfsFileExists) throws Exception {
        // this should never occur, but just in case...
        if (attrMetadata == null || attrMetadata.length() == 0) {
            return;
        } // if
        
        if (attrMetadata.equals("[]")) {
            if (!hdfsFileExists) {
                // create an empty file for metadata
                persistenceBackend.createDir(hdfsFolder);
                persistenceBackend.createFile(hdfsFile, "");
            } // if
            
            return;
        } // if
        
        // metadata is in JSON format, decode it
        JSONParser jsonParser = new JSONParser();
        JSONArray attrMetadataJSON = (JSONArray) jsonParser.parse(attrMetadata);

        // iterate on the metadata
        for (Object mdObject : attrMetadataJSON) {
            JSONObject mdJSONObject = (JSONObject) mdObject;
            String mdCSV = recvTimeTs + "," + mdJSONObject.get("name") + "," + mdJSONObject.get("type") + ","
                    + mdJSONObject.get("value");
            LOGGER.info("[" + this.getName() + "] Persisting metadadata at OrionHDFSSink. HDFS file (" + hdfsFile
                + "), Data (" + mdCSV + ")");

            if (hdfsFileExists) {
                persistenceBackend.append(hdfsFile, mdCSV);
            } else {
                persistenceBackend.createDir(hdfsFolder);
                persistenceBackend.createFile(hdfsFile, mdCSV);
                hdfsFileExists = true;
            } // if else
        } // for
    } // persistCSVMetadata
    
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
    
    /**
     * Builds the third level of a HDFS path given a destination. It throws an exception if the naming conventions are
     * violated.
     * @param destination
     * @return
     * @throws Exception
     */
    private String buildThirdLevelMd(String destination, String attrName, String attrType) throws Exception {
        String thirdLevelMd = destination + "_" + attrName + "_" + attrType;
        
        if (thirdLevelMd.length() > Constants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building thirdLevelMd=" + thirdLevelMd + " and its length is "
                    + "greater than " + Constants.MAX_NAME_LEN);
        } // if

        return thirdLevelMd;
    } // buildThirdLevelMd
    
    /**
     * Creates a String-based row in the given format.
     * @param fileFormat
     * @param recvTimeTs
     * @param recvTime
     * @param entityId
     * @param entityType
     * @param attrName
     * @param attrType
     * @param attrValue
     * @param attrMetadata
     * @return A string-based row
     */
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
            return recvTimeTs / 1000 + ","
                    + recvTime + ","
                    + entityId + ","
                    + entityType + ","
                    + attrName + ","
                    + attrType + ","
                    + attrValue + ","
                    + attrMetadata;
        } else {
            return "";
        } // if else
    } // createRow

    /**
     * Creates a String-based column in the given format.
     * @param fileFormat
     * @param attrName
     * @param attrValue
     * @param attrMetadata
     * @return A String-based column
     */
    private String createColumn(FileFormat fileFormat, String attrName, String attrValue, String attrMetadata) {
        if (fileFormat == FileFormat.JSONCOLUMN) {
            return ", \"" + attrName + "\":" + attrValue + ", \"" + attrName + "_md\":" + attrMetadata;
        } else if (fileFormat == FileFormat.CSVCOLUMN) {
            return "," + attrValue + "," + attrMetadata;
        } else {
            return "";
        } // if else
    } // createJSONColumn

} // OrionHDFSSink
