/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
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
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
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
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.flume.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
    private String[] hosts;
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
    private LinkedList<HDFSBackend> persistenceBackends;
    private HiveBackend hiveBackend;
    private String csvSeparator;
    private int maxConns;
    private int maxConnsPerRoute;

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
        return hosts;
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
     * @return True if the service is used as HDFS namespace, False otherwise.
     */
    protected boolean getServiceAsNamespace() {
        return serviceAsNamespace;
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
     * @return True if Kerberos is being used for authentication, otherwise False
     */
    protected boolean getEnableKrb5Auth() {
        return enableKrb5;
    } // getEnableKrb5Auth

    /**
     * Returns the persistence backends. It is protected due to it is only required for testing purposes.
     * @return The persistence backends
     */
    protected LinkedList<HDFSBackend> getPersistenceBackends() {
        return persistenceBackends;
    } // getPersistenceBackend

    /**
     * Sets the persistence backends. It is protected due to it is only required for testing purposes.
     * @param persistenceBackends
     */
    protected void setPersistenceBackend(LinkedList<HDFSBackend> persistenceBackends) {
        this.persistenceBackends = persistenceBackends;
    } // setPersistenceBackend
    
    protected String getCSVSeparator() {
        return csvSeparator;
    } // getCSVSeparator
    
    protected BackendImpl getBackendImpl() {
        return backendImpl;
    } // getBackendImpl
    
    protected int getBackendMaxConns() {
        return maxConns;
    } // getBackendMaxConns
    
    protected int getBackendMaxConnsPerRoute() {
        return maxConnsPerRoute;
    } // getBackendMaxConnsPerRoute

    @Override
    public void configure(Context context) {
        String hdfsHost = context.getString("hdfs_host", "localhost");
        hosts = hdfsHost.split(",");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (hdfs_host=" + Arrays.toString(hosts) + ")");
        
        port = context.getString("hdfs_port", "14000");
        
        try {
            int intPort = Integer.parseInt(port);
            
            if ((intPort >= 0) && (intPort <= 65535)) {
                LOGGER.debug("[" + this.getName() + "] Reading configuration (hdfs_port=" + port + ")");
            } else {
                LOGGER.warn("[" + this.getName() + "] Invalid configuration (hdfs_port=" + port
                        + ") -- Must be between 0 and 65535.");
            } // if else
        } catch (Exception e) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (hdfs_port=" + port
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
            LOGGER.warn("[" + this.getName() + "] No OAuth2 token provided. Cygnus can continue, but HDFS sink may "
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
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (file_format="
                    + fileFormatStr + ") -- Must be 'json-row', 'json-column', 'csv-row' or 'csv-column'");
        } // catch

        // Hive configuration
        String enableHiveStr = context.getString("hive", "false");
        
        if (enableHiveStr.equals("true") || enableHiveStr.equals("false")) {
            enableHive = Boolean.valueOf(enableHiveStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (enableHive="
                + enableHiveStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (enableHive="
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
                LOGGER.warn("[" + this.getName() + "] Invalid configuration (hive.port=" + hivePort
                    + ") -- Must be between 0 and 65535");
            }
            
        } catch (Exception e) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (hive.port=" + hivePort
                    + ") -- Must be a valid number between 0 and 65535");
        } // try catch

        hiveServerVersion = context.getString("hive.server_version", "2");
        
        if ((!((hiveServerVersion.equals("1"))) && (!(hiveServerVersion.equals("2"))))) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (hive.server_version=" + hiveServerVersion
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
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (hive.db_type="
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
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (krb5_auth="
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
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (service_as_namespace="
                + serviceAsNamespaceStr + ") -- Must be 'true' or 'false'");
        }  // if else
        
        String backendImplStr = context.getString("backend.impl", "rest");

        try {
            backendImpl = BackendImpl.valueOf(backendImplStr.toUpperCase());
            LOGGER.debug("[" + this.getName() + "] Reading configuration (backend.impl="
                        + backendImplStr + ")");
        } catch (Exception e) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (backend.impl="
                + backendImplStr + ") -- Must be 'rest' or 'binary'");
        }  // try catch
        
        maxConns = context.getInteger("backend.max_conns", 500);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (backend.max_conns=" + maxConns + ")");
        maxConnsPerRoute = context.getInteger("backend.max_conns_per_route", 100);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (backend.max_conns_per_route=" + maxConnsPerRoute
                + ")");
        
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
            
            // create the persistence backends
            persistenceBackends = new LinkedList<>();
            
            for (String host : hosts) {
                if (backendImpl == BackendImpl.BINARY) {
                    HDFSBackendImplBinary persistenceBackend = new HDFSBackendImplBinary(host, port, username, password,
                            oauth2Token, hiveServerVersion, hiveHost, hivePort, enableKrb5, krb5User, krb5Password,
                            krb5LoginConfFile, krb5ConfFile, serviceAsNamespace);
                    persistenceBackends.add(persistenceBackend);
                } else if (backendImpl == BackendImpl.REST) {
                    HDFSBackendImplREST persistenceBackend = new HDFSBackendImplREST(host, port, username, password,
                            oauth2Token, hiveServerVersion, hiveHost, hivePort, enableKrb5, krb5User, krb5Password,
                            krb5LoginConfFile, krb5ConfFile, serviceAsNamespace, maxConns, maxConnsPerRoute);
                    persistenceBackends.add(persistenceBackend);
                } else {
                    LOGGER.fatal("The configured backend implementation does not exist, Cygnus will exit. Details="
                            + backendImpl.toString());
                    System.exit(-1);
                } // if else
            } // for

            LOGGER.debug("[" + this.getName() + "] HDFS persistence backend created");
        } catch (Exception e) {
            LOGGER.error("Error while creating the HDFS persistence backend. Details="
                    + e.getMessage());
        } // try catch

        super.start();
    } // start

    @Override
    void persistBatch(NGSIBatch batch) throws CygnusBadConfiguration, CygnusBadContextData, CygnusPersistenceError {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if

        // Iterate on the destinations
        batch.startIterator();
        
        while (batch.hasNext()) {
            String destination = batch.getNextDestination();
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the "
                    + destination + " destination");

            // Get the events within the current sub-batch
            ArrayList<NGSIEvent> events = batch.getNextEvents();
            
            // Get the first event, it will give us useful information
            NGSIEvent firstEvent = events.get(0);
            String service = firstEvent.getServiceForData();
            String servicePath = firstEvent.getServicePathForData();

            // Get an aggregator for this entity and initialize it based on the first event
            HDFSAggregator aggregator = getAggregator(fileFormat);
            aggregator.initialize(firstEvent);

            for (NGSIEvent event : events) {
                aggregator.aggregate(event);
            } // for

            // Persist the aggregation
            persistAggregation(aggregator, service, servicePath);
            batch.setNextPersisted(true);

            // Persist the metadata aggregations only in CSV-like file formats
            if (fileFormat == FileFormat.CSVROW || fileFormat == FileFormat.CSVCOLUMN) {
                persistMDAggregations(aggregator);
            } // if

            // Create the Hive table
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
    
    @Override
    public void capRecords(NGSIBatch batch, long maxRecords) throws CygnusCappingError {
    } // capRecords

    @Override
    public void expirateRecords(long expirationTime) throws CygnusExpiratingError {
    } // expirateRecords

    /**
     * Class for aggregating aggregation.
     */
    private abstract class HDFSAggregator {

        // string containing the data aggregation
        protected String aggregation;
        // map containing the HDFS files holding the attribute metadata, one per attribute
        protected Map<String, String> mdAggregations;
        protected String service;
        protected String servicePathForData;
        protected String servicePathForNaming;
        protected String entityForNaming;
        protected String hdfsFolder;
        protected String hdfsFile;
        protected String hiveFields;

        public HDFSAggregator() {
            aggregation = "";
            mdAggregations = new HashMap<>();
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

        public void initialize(NGSIEvent event) throws CygnusBadConfiguration {
            service = event.getServiceForNaming(enableNameMappings);
            servicePathForData = event.getServicePathForData();
            servicePathForNaming = event.getServicePathForNaming(enableGrouping, enableNameMappings);
            entityForNaming = event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding);
            hdfsFolder = buildFolderPath(service, servicePathForNaming, entityForNaming);
            hdfsFile = buildFilePath(service, servicePathForNaming, entityForNaming);
        } // initialize

        public abstract void aggregate(NGSIEvent cygnusEvent) throws CygnusBadContextData;

    } // HDFSAggregator

    /**
     * Class for aggregating batches in JSON row mode.
     */
    private class JSONRowAggregator extends HDFSAggregator {

        @Override
        public void initialize(NGSIEvent cygnusEvent) throws CygnusBadConfiguration {
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
        public void aggregate(NGSIEvent event) throws CygnusBadContextData {
            // get the getRecvTimeTs headers
            long recvTimeTs = event.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the getRecvTimeTs body
            ContextElement contextElement = event.getContextElement();
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
                    + "\"" + NGSIConstants.FIWARE_SERVICE_PATH + "\":\"" + servicePathForData + "\","
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
        public void initialize(NGSIEvent cygnusEvent) throws CygnusBadConfiguration {
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
        public void aggregate(NGSIEvent event) {
            // get the getRecvTimeTs headers
            long recvTimeTs = event.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the getRecvTimeTs body
            ContextElement contextElement = event.getContextElement();
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
                    + "\"" + NGSIConstants.FIWARE_SERVICE_PATH + "\":\"" + servicePathForData + "\","
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
        public void initialize(NGSIEvent cygnusEvent) throws CygnusBadConfiguration {
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
        public void aggregate(NGSIEvent event) throws CygnusBadContextData {
            // get the getRecvTimeTs headers
            long recvTimeTs = event.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the getRecvTimeTs body
            ContextElement contextElement = event.getContextElement();
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
                String attrMdFileName = buildAttrMdFilePath(service, servicePathForNaming, entityForNaming, attrName,
                        attrType);
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
                    + servicePathForData + csvSeparator
                    + entityId + csvSeparator
                    + entityType + csvSeparator
                    + attrName + csvSeparator
                    + attrType + csvSeparator;
                if (attrValue != null) {
                    line += attrValue.replaceAll("\"", "") + csvSeparator;
                } else {
                    line += attrValue + csvSeparator;
                }
                line += printableAttrMdFileName;
                if (aggregation.isEmpty()) {
                    aggregation = line;
                } else {
                    aggregation += "\n" + line;
                } // if else
            } // for
        } // aggregate

        private String getCSVMetadata(String attrMetadata, long recvTimeTs) throws CygnusBadContextData {
            String csvMd = "";

            // metadata is in JSON format, decode it
            JSONParser jsonParser = new JSONParser();
            JSONArray attrMetadataJSON;
            
            try {
                attrMetadataJSON = (JSONArray) jsonParser.parse(attrMetadata);
            } catch (ParseException e) {
                throw new CygnusBadContextData("Metadata parsing error", "ParseException", e.getMessage());
            } // try catch

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
        public void initialize(NGSIEvent cygnusEvent) throws CygnusBadConfiguration {
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
                String attrMdFileName = buildAttrMdFilePath(service, servicePathForNaming, entityForNaming, attrName,
                        attrType);
                mdAggregations.put(attrMdFileName, new String());
                hiveFields += ",`" + NGSICharsets.encodeHive(attrName) + "` string,"
                        + "`" + NGSICharsets.encodeHive(attrName) + "_md_file` string";
            } // for
        } // initialize

        @Override
        public void aggregate(NGSIEvent event) throws CygnusBadContextData {
            // get the getRecvTimeTs headers
            long recvTimeTs = event.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, true);

            // get the getRecvTimeTs body
            ContextElement contextElement = event.getContextElement();
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

            String line = recvTime + csvSeparator + servicePathForData + csvSeparator + entityId + csvSeparator
                    + entityType;

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");

                // this has to be done notification by notification and not at initialization since in row mode not all
                // the notifications contain all the attributes
                String attrMdFileName = buildAttrMdFilePath(service, servicePathForNaming, entityForNaming, attrName,
                        attrType);
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
                if (attrValue != null) {
                    line += csvSeparator + attrValue.replaceAll("\"", "") + csvSeparator + printableAttrMdFileName;
                } else {
                    line += csvSeparator + attrValue + csvSeparator + printableAttrMdFileName;
                }
            } // for

            // now, aggregate the line
            if (aggregation.isEmpty()) {
                aggregation = line;
            } else {
                aggregation += "\n" + line;
            } // if else
        } // aggregate

        private String getCSVMetadata(String attrMetadata, long recvTimeTs) throws CygnusBadContextData {
            String csvMd = "";

            // metadata is in JSON format, decode it
            JSONParser jsonParser = new JSONParser();
            JSONArray attrMetadataJSON;
            
            try {
                attrMetadataJSON = (JSONArray) jsonParser.parse(attrMetadata);
            } catch (ParseException e) {
                throw new CygnusBadContextData("Metadata parsing error", "ParseException", e.getMessage());
            } // try catch

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

    private void persistAggregation(HDFSAggregator aggregator, String service, String servicePath)
        throws CygnusPersistenceError {
        String aggregation = aggregator.getAggregation();
        String hdfsFolder = aggregator.getFolder(enableLowercase);
        String hdfsFile = aggregator.getFile(enableLowercase);
        
        LOGGER.info("[" + this.getName() + "] Persisting data at NGSIHDFSSink. HDFS file ("
                + hdfsFile + "), Data (" + aggregation + ")");
        
        // Some variables related to persistence backends looping
        boolean persisted = false;
        long transactionRequestBytes = 0;
        long transactionResponseBytes = 0;

        // Iterate on all the available persistence backends
        for (HDFSBackend persistenceBackend: persistenceBackends) {
            if (persistenceBackend instanceof HDFSBackendImplREST) {
                ((HDFSBackendImplREST) persistenceBackend).startTransaction();
            } // if
            
            try {
                if (persistenceBackend.exists(hdfsFile)) {
                    persistenceBackend.append(hdfsFile, aggregation);
                } else {
                    persistenceBackend.createDir(hdfsFolder);
                    persistenceBackend.createFile(hdfsFile, aggregation);
                } // if else

                // Set the current persistence backend as the favourite one
                if (!persistenceBackends.getFirst().equals(persistenceBackend)) {
                    persistenceBackends.remove(persistenceBackend);
                    persistenceBackends.add(0, persistenceBackend);
                    LOGGER.debug("Placing the persistence backend (" + persistenceBackend.toString()
                            + ") in the first place of the list");
                } // if

                persisted = true;
                
                if (persistenceBackend instanceof HDFSBackendImplREST) {
                    ImmutablePair<Long, Long> bytes = ((HDFSBackendImplREST) persistenceBackend).finishTransaction();
                    transactionRequestBytes += bytes.left;
                    transactionResponseBytes += bytes.right;
                } // if
                
                break;
            } catch (CygnusPersistenceError | CygnusRuntimeError e) {
                if (persistenceBackend instanceof HDFSBackendImplREST) {
                    ImmutablePair<Long, Long> bytes = ((HDFSBackendImplREST) persistenceBackend).finishTransaction();
                    transactionRequestBytes += bytes.left;
                    transactionResponseBytes += bytes.right;
                } // if
                
                LOGGER.info("[" + this.getName() + "] There was some problem with the current endpoint, "
                        + "trying other one. Details: " + e.getMessage());
            } // try catch
        } // for
        
        // Add metrics after iterating on the persistence backends
        serviceMetrics.add(service, servicePath, 0, 0, 0, 0, 0, 0,
                transactionRequestBytes, transactionResponseBytes, 0);
        
        if (!persisted) {
            throw new CygnusPersistenceError("No endpoint was available");
        } // if
    } // persistAggregation

    private void persistMDAggregations(HDFSAggregator aggregator) throws CygnusPersistenceError {
        Set<String> attrMDFiles = aggregator.getAggregatedAttrMDFiles();

        for (String hdfsMDFile : attrMDFiles) {
            String hdfsMdFolder = hdfsMDFile.substring(0, hdfsMDFile.lastIndexOf("/"));
            String mdAggregation = aggregator.getMDAggregation(hdfsMDFile);

            LOGGER.info("[" + this.getName() + "] Persisting metadata at NGSIHDFSSink. HDFS file ("
                    + hdfsMDFile + "), Data (" + mdAggregation + ")");

            for (HDFSBackend persistenceBackend: persistenceBackends) {
                try {
                    if (persistenceBackend.exists(hdfsMDFile)) {
                        persistenceBackend.append(hdfsMDFile, mdAggregation);
                    } else {
                        persistenceBackend.createDir(hdfsMdFolder);
                        persistenceBackend.createFile(hdfsMDFile, mdAggregation);
                    } // if else
                    
                    if (!persistenceBackends.getFirst().equals(persistenceBackend)) {
                        persistenceBackends.remove(persistenceBackend);
                        persistenceBackends.add(0, persistenceBackend);
                        LOGGER.debug("Placing the persistence backend (" + persistenceBackend.toString()
                                + ") in the first place of the list");
                    } // if
                    
                    break;
                } catch (CygnusPersistenceError | CygnusRuntimeError e) {
                    LOGGER.info("[" + this.getName() + "] There was some problem with the current endpoint, "
                            + "trying other one. Details: " + e.getMessage());
                } // try catch
            } // for
        } // for
    } // persistMDAggregations

    private void provisionHiveTable(HDFSAggregator aggregator, String dbName) throws CygnusPersistenceError {
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
     * @throws com.telefonica.iot.cygnus.errors.CygnusBadConfiguration
     */
    protected String buildFolderPath(String service, String servicePath, String destination)
        throws CygnusBadConfiguration {
        String folderPath;
        
        if (enableEncoding) {
            folderPath = NGSICharsets.encodeHDFS(service, false) + NGSICharsets.encodeHDFS(servicePath, true)
                    + (servicePath.equals("/") ? "" : "/") + NGSICharsets.encodeHDFS(destination, false);
        } else {
            folderPath = NGSIUtils.encode(service, false, true) + NGSIUtils.encode(servicePath, false, false)
                    + (servicePath.equals("/") ? "" : "/") + NGSIUtils.encode(destination, false, true);
        } // if else
        
        if (folderPath.length() > NGSIConstants.HDFS_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("-, Building folder path name '" + folderPath + "' and its length is "
                    + "greater than " + NGSIConstants.HDFS_MAX_NAME_LEN);
        } // if
        
        return folderPath;
    } // buildFolderPath
    
    /**
     * Builds a HDFS file path.
     * @param service
     * @param servicePath
     * @param destination
     * @return The file path
     * @throws com.telefonica.iot.cygnus.errors.CygnusBadConfiguration
     */
    protected String buildFilePath(String service, String servicePath, String destination)
        throws CygnusBadConfiguration {
        String filePath;
        
        if (enableEncoding) {
            filePath = NGSICharsets.encodeHDFS(service, false) + NGSICharsets.encodeHDFS(servicePath, true)
                    + (servicePath.equals("/") ? "" : "/") + NGSICharsets.encodeHDFS(destination, false)
                    + "/" + NGSICharsets.encodeHDFS(destination, false) + ".txt";
        } else {
            filePath = NGSIUtils.encode(service, false, true) + NGSIUtils.encode(servicePath, false, false)
                    + (servicePath.equals("/") ? "" : "/") + NGSIUtils.encode(destination, false, true)
                    + "/" + NGSIUtils.encode(destination, false, true) + ".txt";
        } // if else
        
        if (filePath.length() > NGSIConstants.HDFS_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("-, Building file path name '" + filePath + "' and its length is "
                    + "greater than " + NGSIConstants.HDFS_MAX_NAME_LEN);
        } // if
        
        return filePath;
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
