/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
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
import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImplBinary;
import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImplREST;
import com.telefonica.iot.cygnus.backends.hive.HiveBackend;
import com.telefonica.iot.cygnus.backends.hive.HiveBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.Enums.DataModel;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.utils.NGSICharsets;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.flume.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
/**
 *
 * @author frb
 *
 * Detailed documentation can be found at:
 * https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/flume_extensions_catalogue/ngsi_hdfs_sink.md
 */
public class NGSIHDFSSink extends NGSISink {

    /**
     * Available backend implementation.
     */
    public enum BackendImpl { BINARY, REST }

    /**
     * Available file-format implementation.
     */
    private enum FileFormat { JSONROW, JSONCOLUMN, CSVROW, CSVCOLUMN }
    
    /**
     * Available Hive database types.
     */
    public enum HiveDBType { DEFAULTDB, NAMESPACEDB }

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIHDFSSink.class);
    private String[] host;
    private String port;
    private String username;
    private String password;
    private FileFormat fileFormat;
    private String oauth2Token;
    private boolean enableHive;
    private String hiveServerVersion;
    private String hiveHost;
    private String hivePort;
    private boolean enableKrb5;
    private String krb5User;
    private String krb5Password;
    private String krb5LoginConfFile;
    private String krb5ConfFile;
    private boolean serviceAsNamespace;
    private BackendImpl backendImpl;
    private HiveDBType hiveDBType;
    private HDFSBackend persistenceBackend;
    private HiveBackend hiveBackend;
    private String csvSeparator;

    /**
     * Constructor.
     */
    public NGSIHDFSSink() {
        super();
    } // NGSIHDFSSink

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
     * Gets the password for the default Cosmos username. It is protected due to it is only required for testing
     * purposes.
     * @return The password for the default Cosmos username
     */
    protected String getHDFSPassword() {
        return password;
    } // getHDFSPassword

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

    protected boolean getEnableHive() {
        return enableHive;
    } // getEnableHive

    /**
     * Gets the Hive server version. It is protected due to it is only required for testing purposes.
     * @return The Hive server version
     */
    protected String getHiveServerVersion() {
        return hiveServerVersion;
    } // getHiveServerVersion

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
    protected String getEnableKrb5Auth() {
        return (enableKrb5 ? "true" : "false");
    } // getEnableKrb5Auth

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
    protected void setPersistenceBackend(HDFSBackendImplREST persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    } // setPersistenceBackend

    @Override
    public void configure(Context context) {
        String hdfsHost = context.getString("hdfs_host", "localhost");
        host = hdfsHost.split(",");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (hdfs_host=" + Arrays.toString(host) + ")");
        
        port = context.getString("hdfs_port", "14000");
        
        try {
            int intPort = Integer.parseInt(port);
            
            if ((intPort >= 0) && (intPort <= 65535)) {
                LOGGER.debug("[" + this.getName() + "] Reading configuration (hdfs_port=" + port + ")");
            } else {
                LOGGER.debug("[" + this.getName() + "] Invalid configuration (hdfs_port=" + port
                        + ") -- Must be between 0 and 65535.");
            } // if else
        } catch (Exception e) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (hdfs_port=" + port
                  + ") -- Must be a valid number between 0 and 65535.");
        } // try catch

        String hdfsUsername = context.getString("hdfs_username");

        if (hdfsUsername != null && hdfsUsername.length() > 0) {
            username = hdfsUsername;
            LOGGER.debug("[" + this.getName() + "] Reading configuration (hdfs_username=" + username + ")");
        } else {
            LOGGER.error("[" + this.getName() + "] No username provided. Cygnus can continue, but HDFS sink will not "
                    + "properly work!");
        } // if else

        csvSeparator = context.getString("csv_separator", ",");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (csvSeparator=" + csvSeparator + ")");

        oauth2Token = context.getString("oauth2_token");

        if (oauth2Token != null && oauth2Token.length() > 0) {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (oauth2_token=" + this.oauth2Token + ")");
        } else {
            LOGGER.error("[" + this.getName() + "] No OAuth2 token provided. Cygnus can continue, but HDFS sink may "
                    + "not properly work if WebHDFS service is protected with such an authentication and "
                    + "authorization mechanism!");
        } // if else

        password = context.getString("hdfs_password");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (hdfs_password=" + password + ")");

        String fileFormatStr = context.getString("file_format", "json-row");
            
        try {
            fileFormat = FileFormat.valueOf(fileFormatStr.replaceAll("-", "").toUpperCase());
            LOGGER.debug("[" + this.getName() + "] Reading configuration (file_format="
                   + fileFormatStr + ")");
        } catch (Exception e) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (file_format="
                    + fileFormatStr + ") -- Must be 'json-row', 'json-column', 'csv-row' or 'csv-column'");
        } // catch

        // Hive configuration
        String enableHiveStr = context.getString("hive", "true");
        
        if (enableHiveStr.equals("true") || enableHiveStr.equals("false")) {
            enableHive = Boolean.valueOf(enableHiveStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (enableHive="
                + enableHiveStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (enableHive="
                + enableHiveStr + ") -- Must be 'true' or 'false'");
        }  // if else

        hiveHost = context.getString("hive.host", "localhost");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (hive.host=" + hiveHost + ")");

        hivePort = context.getString("hive.port", "10000");
        
        try {
            int intHivePort = Integer.parseInt(hivePort);
            
            if ((intHivePort >= 0) && (intHivePort <= 65535)) {
                LOGGER.debug("[" + this.getName() + "] Reading configuration (hive.port=" + hivePort + ")");
            } else {
                invalidConfiguration = true;
                LOGGER.debug("[" + this.getName() + "] Invalid configuration (hive.port=" + hivePort
                    + ") -- Must be between 0 and 65535");
            }
            
        } catch (Exception e) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (hive.port=" + hivePort
                    + ") -- Must be a valid number between 0 and 65535");
        } // try catch

        hiveServerVersion = context.getString("hive.server_version", "2");
        
        if ((!((hiveServerVersion.equals("1"))) && (!(hiveServerVersion.equals("2"))))) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (hive.server_version=" + hiveServerVersion
                + ") -- Must be a valid number: '1' for HiveServer1 or '2' for HiveServer2");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (hive.server_version="
                    + hiveServerVersion + ")");
        } // if else

        String hiveDBTypeStr = context.getString("hive.db_type", "default-db");
        
        try {
            hiveDBType = HiveDBType.valueOf(hiveDBTypeStr.replaceAll("-", "").toUpperCase());
            LOGGER.debug("[" + this.getName() + "] Reading configuration (hive.db_type="
                    + hiveDBType + ")");
        } catch (Exception e) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (hive.db_type="
                + hiveDBTypeStr + ") -- Must be 'default-db' or 'namespace-db'");
        }  // try catch

        // Kerberos configuration
        String enableKrb5Str = context.getString("krb5_auth", "false");
        
        if (enableKrb5Str.equals("true") || enableKrb5Str.equals("false")) {
            enableKrb5 = Boolean.valueOf(enableKrb5Str);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (krb5_auth="
                + enableKrb5Str + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (krb5_auth="
                + enableKrb5Str + ") -- Must be 'true' or 'false'");
        }  // if else
        
        krb5User = context.getString("krb5_auth.krb5_user", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (krb5_user=" + krb5User + ")");
        krb5Password = context.getString("krb5_auth.krb5_password", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (krb5_password=" + krb5Password + ")");
        krb5LoginConfFile = context.getString("krb5_auth.krb5_login_conf_file", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (krb5_login_conf_file=" + krb5LoginConfFile
                + ")");
        krb5ConfFile = context.getString("krb5_auth.krb5_conf_file", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (krb5_conf_file=" + krb5ConfFile + ")");

        String serviceAsNamespaceStr = context.getString("service_as_namespace", "false");
        
        if (serviceAsNamespaceStr.equals("true") || serviceAsNamespaceStr.equals("false")) {
            serviceAsNamespace = Boolean.valueOf(serviceAsNamespaceStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (hive.db_type="
                + serviceAsNamespaceStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (service_as_namespace="
                + serviceAsNamespaceStr + ") -- Must be 'true' or 'false'");
        }  // if else
        
        String backendImplStr = context.getString("backend_impl", "rest");

        try {
            backendImpl = BackendImpl.valueOf(backendImplStr.toUpperCase());
            LOGGER.debug("[" + this.getName() + "] Reading configuration (backend_impl="
                        + backendImplStr + ")");
        } catch (Exception e) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (backend_impl="
                + backendImplStr + ") -- Must be 'rest' or 'binary'");
        }  // try catch
        
        super.configure(context);
        // Techdebt: allow this sink to work with all the data models
        dataModel = DataModel.DMBYENTITY;
    } // configure

    @Override
    public void start() {
        try {
            // create Hive backend
            hiveBackend = new HiveBackendImpl(hiveServerVersion, hiveHost, hivePort, username, password);
            LOGGER.debug("[" + this.getName() + "] Hive persistence backend created");

            // create the persistence backend
            if (backendImpl == BackendImpl.BINARY) {
                persistenceBackend = new HDFSBackendImplBinary(host, port, username, password, oauth2Token,
                        hiveServerVersion, hiveHost, hivePort, enableKrb5, krb5User, krb5Password, krb5LoginConfFile,
                        krb5ConfFile, serviceAsNamespace);
            } else if (backendImpl == BackendImpl.REST) {
                persistenceBackend = new HDFSBackendImplREST(host, port, username, password, oauth2Token,
                        hiveServerVersion, hiveHost, hivePort, enableKrb5, krb5User, krb5Password, krb5LoginConfFile,
                        krb5ConfFile, serviceAsNamespace);
            } else {
                LOGGER.fatal("The configured backend implementation does not exist, Cygnus will exit. Details="
                        + backendImpl.toString());
                System.exit(-1);
            } // if else

            LOGGER.debug("[" + this.getName() + "] HDFS persistence backend created");
        } catch (Exception e) {
            LOGGER.error("Error while creating the HDFS persistence backend. Details="
                    + e.getMessage());
        } // try catch

        super.start();
    } // start

    @Override
    void persistBatch(NGSIBatch batch) throws Exception {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if

        // iterate on the destinations, for each one a single create / append will be performed
        for (String destination : batch.getDestinations()) {
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the " + destination
                    + " destination");

            // get the sub-batch for this destination
            ArrayList<NGSIEvent> subBatch = batch.getEvents(destination);

            // get an aggregator for this destination and initialize it
            HDFSAggregator aggregator = getAggregator(fileFormat);
            aggregator.initialize(subBatch.get(0));

            for (NGSIEvent cygnusEvent : subBatch) {
                aggregator.aggregate(cygnusEvent);
            } // for

            // persist the aggregation
            persistAggregation(aggregator);
            batch.setPersisted(destination);

            // persist the metadata aggregations only in CSV-like file formats
            if (fileFormat == FileFormat.CSVROW || fileFormat == FileFormat.CSVCOLUMN) {
                persistMDAggregations(aggregator);
            } // if

            // create the Hive table
            if (enableHive) {
                if (hiveDBType == HiveDBType.NAMESPACEDB) {
                    if (serviceAsNamespace) {
                        hiveBackend.doCreateDatabase(aggregator.service);
                        provisionHiveTable(aggregator, aggregator.service);
                    } else {
                        hiveBackend.doCreateDatabase(username);
                        provisionHiveTable(aggregator, username);
                    } // if else
                } else {
                    provisionHiveTable(aggregator, "default");
                } // if else
            } // if
        } // for
    } // persistBatch

    /**
     * Class for aggregating aggregation.
     */
    private abstract class HDFSAggregator {

        // string containing the data aggregation
        protected String aggregation;
        // map containing the HDFS files holding the attribute metadata, one per attribute
        protected Map<String, String> mdAggregations;
        protected String service;
        protected String servicePath;
        protected String destination;
        //protected String firstLevel;
        //protected String secondLevel;
        //protected String thirdLevel;
        protected String hdfsFolder;
        protected String hdfsFile;
        protected String hiveFields;

        public HDFSAggregator() {
            aggregation = "";
            mdAggregations = new HashMap<String, String>();
        } // HDFSAggregator

        public String getAggregation() {
            return aggregation;
        } // getAggregation

        public Set<String> getAggregatedAttrMDFiles() {
            return mdAggregations.keySet();
        } // getAggregatedAttrMDFiles

        public String getMDAggregation(String attrMDFile) {
            return mdAggregations.get(attrMDFile);
        } // getMDAggregation

        public String getFolder(boolean enableLowercase) {
            if (enableLowercase) {
                return hdfsFolder.toLowerCase();
            } else {
                return hdfsFolder;
            } // if else
        } // getFolder

        public String getFile(boolean enableLowercase) {
            if (enableLowercase) {
                return hdfsFile.toLowerCase();
            } else {
                return hdfsFile;
            } // if else
        } // getFile

        public String getHiveFields() {
            return hiveFields;
        } // getHiveFields

        public void initialize(NGSIEvent cygnusEvent) throws Exception {
            service = cygnusEvent.getService();
            servicePath = cygnusEvent.getServicePath();
            destination = cygnusEvent.getEntity();
            //firstLevel = buildFirstLevel(service);
            //secondLevel = buildSecondLevel(servicePath);
            //thirdLevel = buildThirdLevel(destination);
            hdfsFolder = buildFolderPath(service, servicePath, destination);
            hdfsFile = buildFilePath(service, servicePath, destination);
            //hdfsFolder = firstLevel + (servicePath.equals("/") ? "" : secondLevel) + "/" + thirdLevel;
            //hdfsFile = hdfsFolder + "/" + thirdLevel + ".txt";
        } // initialize

        public abstract void aggregate(NGSIEvent cygnusEvent) throws Exception;

    } // HDFSAggregator

    /**
     * Class for aggregating batches in JSON row mode.
     */
    private class JSONRowAggregator extends HDFSAggregator {

        @Override
        public void initialize(NGSIEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
            hiveFields = NGSICharsets.encodeHive(NGSIConstants.RECV_TIME_TS) + " bigint,"
                    + NGSICharsets.encodeHive(NGSIConstants.RECV_TIME) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.FIWARE_SERVICE_PATH) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ENTITY_ID) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ENTITY_TYPE) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ATTR_NAME) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ATTR_TYPE) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ATTR_VALUE) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ATTR_MD)
                    + " array<struct<name:string,type:string,value:string>>";
        } // initialize

        @Override
        public void aggregate(NGSIEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the event body
            ContextElement contextElement = cygnusEvent.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");

                // create a line and aggregate it
                String line = "{"
                    + "\"" + NGSIConstants.RECV_TIME_TS + "\":\"" + recvTimeTs / 1000 + "\","
                    + "\"" + NGSIConstants.RECV_TIME + "\":\"" + recvTime + "\","
                    + "\"" + NGSIConstants.FIWARE_SERVICE_PATH + "\":\"" + servicePath + "\","
                    + "\"" + NGSIConstants.ENTITY_ID + "\":\"" + entityId + "\","
                    + "\"" + NGSIConstants.ENTITY_TYPE + "\":\"" + entityType + "\","
                    + "\"" + NGSIConstants.ATTR_NAME + "\":\"" + attrName + "\","
                    + "\"" + NGSIConstants.ATTR_TYPE + "\":\"" + attrType + "\","
                    + "\"" + NGSIConstants.ATTR_VALUE + "\":" + attrValue + ","
                    + "\"" + NGSIConstants.ATTR_MD + "\":" + attrMetadata
                    + "}";

                if (aggregation.isEmpty()) {
                    aggregation = line;
                } else {
                    aggregation += "\n" + line;
                } // if else
            } // for
        } // aggregate

    } // JSONRowAggregator

    /**
     * Class for aggregating batches in JSON column mode.
     */
    private class JSONColumnAggregator extends HDFSAggregator {

        @Override
        public void initialize(NGSIEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);

            // particular initialization
            hiveFields = NGSICharsets.encodeHive(NGSIConstants.RECV_TIME) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.FIWARE_SERVICE_PATH) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ENTITY_ID) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ENTITY_TYPE) + " string";

            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = cygnusEvent.getContextElement().getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                return;
            } // if

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                hiveFields += "," + NGSICharsets.encodeHive(attrName) + " string," + NGSICharsets.encodeHive(attrName)
                        + "_md array<struct<name:string,type:string,value:string>>";
            } // for
        } // initialize

        @Override
        public void aggregate(NGSIEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the event body
            ContextElement contextElement = cygnusEvent.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            String line = "{\"" + NGSIConstants.RECV_TIME + "\":\"" + recvTime + "\","
                    + "\"" + NGSIConstants.FIWARE_SERVICE_PATH + "\":\"" + servicePath + "\","
                    + "\"" + NGSIConstants.ENTITY_ID + "\":\"" + entityId + "\","
                    + "\"" + NGSIConstants.ENTITY_TYPE + "\":\"" + entityType + "\"";

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");

                // create part of the line with the current attribute (a.k.a. a column)
                line += ", \"" + attrName + "\":" + attrValue + ", \"" + attrName + "_md\":" + attrMetadata;
            } // for

            // now, aggregate the line
            if (aggregation.isEmpty()) {
                aggregation = line + "}";
            } else {
                aggregation += "\n" + line + "}";
            } // if else
        } // aggregate

    } // JSONColumnAggregator

    /**
     * Class for aggregating batches in CSV row mode.
     */
    private class CSVRowAggregator extends HDFSAggregator {

        @Override
        public void initialize(NGSIEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
            hiveFields = NGSICharsets.encodeHive(NGSIConstants.RECV_TIME_TS) + " bigint,"
                    + NGSICharsets.encodeHive(NGSIConstants.RECV_TIME) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.FIWARE_SERVICE_PATH) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ENTITY_ID) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ENTITY_TYPE) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ATTR_NAME) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ATTR_TYPE) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ATTR_VALUE) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ATTR_MD_FILE) + " string";
        } // initialize

        @Override
        public void aggregate(NGSIEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the event body
            ContextElement contextElement = cygnusEvent.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                // this has to be done notification by notification and not at initialization since in row mode not all
                // the notifications contain all the attributes
                String attrMdFileName = buildAttrMdFilePath(service, servicePath, destination, attrName, attrType);
                String printableAttrMdFileName = "hdfs:///user/" + username + "/" + attrMdFileName;
                String mdAggregation = mdAggregations.get(attrMdFileName);

                if (mdAggregation == null) {
                    mdAggregation = new String();
                } // if

                // aggregate the metadata
                String concatMdAggregation;

                if (mdAggregation.isEmpty()) {
                    concatMdAggregation = getCSVMetadata(attrMetadata, recvTimeTs);
                } else {
                    concatMdAggregation = mdAggregation.concat("\n" + getCSVMetadata(attrMetadata, recvTimeTs));
                } // if else

                mdAggregations.put(attrMdFileName, concatMdAggregation);

                // aggreagate the data
                String line = recvTimeTs / 1000 + csvSeparator
                    + recvTime + csvSeparator
                    + servicePath + csvSeparator
                    + entityId + csvSeparator
                    + entityType + csvSeparator
                    + attrName + csvSeparator
                    + attrType + csvSeparator
                    + attrValue.replaceAll("\"", "") + csvSeparator
                    + printableAttrMdFileName;
                if (aggregation.isEmpty()) {
                    aggregation = line;
                } else {
                    aggregation += "\n" + line;
                } // if else
            } // for
        } // aggregate

        private String getCSVMetadata(String attrMetadata, long recvTimeTs) throws Exception {
            String csvMd = "";

            // metadata is in JSON format, decode it
            JSONParser jsonParser = new JSONParser();
            JSONArray attrMetadataJSON = (JSONArray) jsonParser.parse(attrMetadata);

            // iterate on the metadata
            for (Object mdObject : attrMetadataJSON) {
                JSONObject mdJSONObject = (JSONObject) mdObject;
                String line = recvTimeTs + ","
                        + mdJSONObject.get("name") + ","
                        + mdJSONObject.get("type") + ","
                        + mdJSONObject.get("value");

                if (csvMd.isEmpty()) {
                    csvMd = line;
                } else {
                    csvMd += "\n" + line;
                } // if else
            } // for

            return csvMd;
        } // getCSVMetadata

    } // CSVRowAggregator

    /**
     * Class for aggregating aggregation in CSV column mode.
     */
    private class CSVColumnAggregator extends HDFSAggregator {

        @Override
        public void initialize(NGSIEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);

            // particular initialization
            hiveFields = NGSICharsets.encodeHive(NGSIConstants.RECV_TIME) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.FIWARE_SERVICE_PATH) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ENTITY_ID) + " string,"
                    + NGSICharsets.encodeHive(NGSIConstants.ENTITY_TYPE) + " string";

            // iterate on all this context element attributes; it is supposed all the entity's attributes are notified
            ArrayList<ContextAttribute> contextAttributes = cygnusEvent.getContextElement().getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                return;
            } // if

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrMdFileName = buildAttrMdFilePath(service, servicePath, destination, attrName, attrType);
                mdAggregations.put(attrMdFileName, new String());
                hiveFields += ",`" + NGSICharsets.encodeHive(attrName) + "` string,"
                        + "`" + NGSICharsets.encodeHive(attrName) + "_md_file` string";
            } // for
        } // initialize

        @Override
        public void aggregate(NGSIEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the event body
            ContextElement contextElement = cygnusEvent.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            String line = recvTime + csvSeparator + servicePath + csvSeparator + entityId + csvSeparator + entityType;

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");

                // this has to be done notification by notification and not at initialization since in row mode not all
                // the notifications contain all the attributes
                String attrMdFileName = buildAttrMdFilePath(service, servicePath, destination, attrName, attrType);
                String printableAttrMdFileName = "hdfs:///user/" + username + "/" + attrMdFileName;
                String mdAggregation = mdAggregations.get(attrMdFileName);

                if (mdAggregation == null) {
                    mdAggregation = new String();
                } // if

                // agregate the metadata
                String concatMdAggregation;

                if (mdAggregation.isEmpty()) {
                    concatMdAggregation = getCSVMetadata(attrMetadata, recvTimeTs);
                } else {
                    concatMdAggregation = mdAggregation.concat("\n" + getCSVMetadata(attrMetadata, recvTimeTs));
                } // if else

                mdAggregations.put(attrMdFileName, concatMdAggregation);

                // create part of the line with the current attribute (a.k.a. a column)
                line += csvSeparator + attrValue.replaceAll("\"", "") + csvSeparator + printableAttrMdFileName;
            } // for

            // now, aggregate the line
            if (aggregation.isEmpty()) {
                aggregation = line;
            } else {
                aggregation += "\n" + line;
            } // if else
        } // aggregate

        private String getCSVMetadata(String attrMetadata, long recvTimeTs) throws Exception {
            String csvMd = "";

            // metadata is in JSON format, decode it
            JSONParser jsonParser = new JSONParser();
            JSONArray attrMetadataJSON = (JSONArray) jsonParser.parse(attrMetadata);

            // iterate on the metadata
            for (Object mdObject : attrMetadataJSON) {
                JSONObject mdJSONObject = (JSONObject) mdObject;
                String line = recvTimeTs + ","
                        + mdJSONObject.get("name") + ","
                        + mdJSONObject.get("type") + ","
                        + mdJSONObject.get("value");

                if (csvMd.isEmpty()) {
                    csvMd = line;
                } else {
                    csvMd += "\n" + line;
                } // if else
            } // for

            return csvMd;
        } // getCSVMetadata

    } // CSVColumnAggregator

    private HDFSAggregator getAggregator(FileFormat fileFormat) {
        switch (fileFormat) {
            case JSONROW:
                return new JSONRowAggregator();
            case JSONCOLUMN:
                return new JSONColumnAggregator();
            case CSVROW:
                return new CSVRowAggregator();
            case CSVCOLUMN:
                return new CSVColumnAggregator();
            default:
                return null;
        } // switch
    } // getAggregator

    private void persistAggregation(HDFSAggregator aggregator) throws Exception {
        String aggregation = aggregator.getAggregation();
        String hdfsFolder = aggregator.getFolder(enableLowercase);
        String hdfsFile = aggregator.getFile(enableLowercase);

        LOGGER.info("[" + this.getName() + "] Persisting data at OrionHDFSSink. HDFS file ("
                + hdfsFile + "), Data (" + aggregation + ")");

        if (persistenceBackend.exists(hdfsFile)) {
            persistenceBackend.append(hdfsFile, aggregation);
        } else {
            persistenceBackend.createDir(hdfsFolder);
            persistenceBackend.createFile(hdfsFile, aggregation);
        } // if else
    } // persistAggregation

    private void persistMDAggregations(HDFSAggregator aggregator) throws Exception {
        Set<String> attrMDFiles = aggregator.getAggregatedAttrMDFiles();

        for (String hdfsMDFile : attrMDFiles) {
            String hdfsMdFolder = hdfsMDFile.substring(0, hdfsMDFile.lastIndexOf("/"));
            String mdAggregation = aggregator.getMDAggregation(hdfsMDFile);

            LOGGER.info("[" + this.getName() + "] Persisting metadata at OrionHDFSSink. HDFS file ("
                    + hdfsMDFile + "), Data (" + mdAggregation + ")");

            if (persistenceBackend.exists(hdfsMDFile)) {
                persistenceBackend.append(hdfsMDFile, mdAggregation);
            } else {
                persistenceBackend.createDir(hdfsMdFolder);
                persistenceBackend.createFile(hdfsMDFile, mdAggregation);
            } // if else
        } // for
    } // persistMDAggregations

    private void provisionHiveTable(HDFSAggregator aggregator, String dbName) throws Exception {
        String dirPath = aggregator.getFolder(enableLowercase);
        String fields = aggregator.getHiveFields();
        String tag;

        switch (fileFormat) {
            case JSONROW:
            case CSVROW:
                tag = "_row";
                break;
            case JSONCOLUMN:
            case CSVCOLUMN:
                tag = "_column";
                break;
            default:
                tag = "";
        } // switch

        // get the table name to be created
        // the replacement is necessary because Hive, due it is similar to MySQL, does not accept '-' in the table names
        String tableName = NGSICharsets.encodeHive((serviceAsNamespace ? "" : username + "_") + dirPath) + tag;
        LOGGER.info("Creating Hive external table '" + tableName + "' in database '"  + dbName + "'");

        // get a Hive client
        HiveBackendImpl hiveClient = new HiveBackendImpl(hiveServerVersion, hiveHost, hivePort, username, password);

        // create the query
        String query;

        switch (fileFormat) {
            case JSONCOLUMN:
            case JSONROW:
                query = "create external table if not exists " + dbName + "." + tableName + " (" + fields
                        + ") row format serde " + "'org.openx.data.jsonserde.JsonSerDe' with serdeproperties "
                        + "(\"dots.in.keys\" = \"true\") location '/user/"
                        + (serviceAsNamespace ? "" : (username + "/")) + dirPath + "'";
                break;
            case CSVCOLUMN:
            case CSVROW:
                query = "create external table if not exists " + dbName + "." + tableName + " (" + fields
                        + ") row format " + "delimited fields terminated by ',' location '/user/"
                        + (serviceAsNamespace ? "" : (username + "/")) + dirPath + "'";
                break;
            default:
                query = "";
        } // switch

        // execute the query
        LOGGER.debug("Doing Hive query: '" + query + "'");

        if (!hiveClient.doCreateTable(query)) {
            LOGGER.warn("The HiveQL external table could not be created, but Cygnus can continue working... "
                    + "Check your Hive/Shark installation");
        } // if
    } // provisionHive

    /**
     * Builds a HDFS folder path.
     * @param service
     * @param servicePath
     * @param destination
     * @return The HDFS folder path
     */
    protected String buildFolderPath(String service, String servicePath, String destination) {
        if (enableEncoding) {
            return NGSICharsets.encodeHDFS(service, false) + NGSICharsets.encodeHDFS(servicePath, true)
                    + (servicePath.equals("/") ? "" : "/") + NGSICharsets.encodeHDFS(destination, false);
        } else {
            return NGSIUtils.encode(service, false, true) + NGSIUtils.encode(servicePath, false, false)
                    + (servicePath.equals("/") ? "" : "/") + NGSIUtils.encode(destination, false, true);
        } // if else
    } // buildFolderPath
    
    /**
     * Builds a HDFS file path.
     * @param service
     * @param servicePath
     * @param destination
     * @return The file path
     */
    protected String buildFilePath(String service, String servicePath, String destination) {
        if (enableEncoding) {
            return NGSICharsets.encodeHDFS(service, false) + NGSICharsets.encodeHDFS(servicePath, true)
                    + (servicePath.equals("/") ? "" : "/") + NGSICharsets.encodeHDFS(destination, false)
                    + "/" + NGSICharsets.encodeHDFS(destination, false) + ".txt";
        } else {
            return NGSIUtils.encode(service, false, true) + NGSIUtils.encode(servicePath, false, false)
                    + (servicePath.equals("/") ? "" : "/") + NGSIUtils.encode(destination, false, true)
                    + "/" + NGSIUtils.encode(destination, false, true) + ".txt";
        } // if else
    } // buildFilePath
    
    /**
     * Builds an attribute metadata HDFS folder path.
     * @param service
     * @param servicePath
     * @param destination
     * @param attrName
     * @param attrType
     * @return The attribute metadata HDFS folder path
     */
    protected String buildAttrMdFolderPath(String service, String servicePath, String destination, String attrName,
            String attrType) {
        return NGSICharsets.encodeHDFS(service, false) + NGSICharsets.encodeHDFS(servicePath, true)
                + (servicePath.equals("/") ? "" : "/")
                + NGSICharsets.encodeHDFS(destination, false) + CommonConstants.CONCATENATOR
                + NGSICharsets.encodeHDFS(attrName, false) + CommonConstants.CONCATENATOR
                + NGSICharsets.encodeHDFS(attrType, false);
    } // buildAttrMdFolderPath
    
    /**
     * Builds an attribute metadata HDFS file path.
     * @param service
     * @param servicePath
     * @param destination
     * @param attrName
     * @param attrType
     * @return The attribute metadata HDFS file path
     */
    protected String buildAttrMdFilePath(String service, String servicePath, String destination, String attrName,
            String attrType) {
        return NGSICharsets.encodeHDFS(service, false) + NGSICharsets.encodeHDFS(servicePath, true)
                + (servicePath.equals("/") ? "" : "/")
                + NGSICharsets.encodeHDFS(destination, false) + CommonConstants.CONCATENATOR
                + NGSICharsets.encodeHDFS(attrName, false) + CommonConstants.CONCATENATOR
                + NGSICharsets.encodeHDFS(attrType, false) + "/"
                + NGSICharsets.encodeHDFS(destination, false) + CommonConstants.CONCATENATOR
                + NGSICharsets.encodeHDFS(attrName, false) + CommonConstants.CONCATENATOR
                + NGSICharsets.encodeHDFS(attrType, false) + ".txt";
    } // buildAttrMdFolderPath

} // NGSIHDFSSink
