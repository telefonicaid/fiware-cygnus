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
import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackend.FileFormat;
import static com.telefonica.iot.cygnus.backends.hdfs.HDFSBackend.FileFormat.CSVCOLUMN;
import static com.telefonica.iot.cygnus.backends.hdfs.HDFSBackend.FileFormat.CSVROW;
import static com.telefonica.iot.cygnus.backends.hdfs.HDFSBackend.FileFormat.JSONCOLUMN;
import static com.telefonica.iot.cygnus.backends.hdfs.HDFSBackend.FileFormat.JSONROW;
import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImplBinary;
import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImplREST;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
 * https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/design/OrionHDFSSink.md
 */
public class OrionHDFSSink extends OrionSink {

    /**
     * Available backend implementation.
     */
    public enum BackendImpl { BINARY, REST }

    private static final CygnusLogger LOGGER = new CygnusLogger(OrionHDFSSink.class);
    private String[] host;
    private String port;
    private String username;
    private String password;
    private FileFormat fileFormat;
    private String oauth2Token;
    private String hiveServerVersion;
    private String hiveHost;
    private String hivePort;
    private boolean krb5;
    private String krb5User;
    private String krb5Password;
    private String krb5LoginConfFile;
    private String krb5ConfFile;
    private boolean serviceAsNamespace;
    private BackendImpl backendImpl;
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
    protected void setPersistenceBackend(HDFSBackendImplREST persistenceBackend) {
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
        
        password = context.getString("hdfs_password");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (hdfs_password=" + password + ")");

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

        hiveServerVersion = context.getString("hive_server_version", "2");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (hive_server_version=" + hiveServerVersion + ")");
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
        String backendImplStr = context.getString("backend_impl", "rest");
        backendImpl = BackendImpl.valueOf(backendImplStr.toUpperCase());
        LOGGER.debug("[" + this.getName() + "] Reading configuration (backend_impl=" + backendImplStr + ")");
        super.configure(context);
    } // configure

    @Override
    public void start() {
        try {
            // create the persistence backend
            if (backendImpl == BackendImpl.BINARY) {
                persistenceBackend = new HDFSBackendImplBinary(host, port, username, password, oauth2Token,
                        hiveServerVersion, hiveHost, hivePort, krb5, krb5User, krb5Password, krb5LoginConfFile,
                        krb5ConfFile, serviceAsNamespace);
            } else if (backendImpl == BackendImpl.REST) {
                persistenceBackend = new HDFSBackendImplREST(host, port, username, password, oauth2Token,
                        hiveServerVersion, hiveHost, hivePort, krb5, krb5User, krb5Password, krb5LoginConfFile,
                        krb5ConfFile, serviceAsNamespace);
            } else {
                LOGGER.fatal("The configured backend implementation does not exist, Cygnus will exit. Details="
                        + backendImpl.toString());
                System.exit(-1);
            } // if else
            
            LOGGER.debug("[" + this.getName() + "] HDFS persistence backend created");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } // try catch // try catch
        
        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

    // TBD: to be removed once all the sinks have been migrated to persistBatch method
    @Override
    void persistOne(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        Accumulator accumulator = new Accumulator();
        accumulator.initializeBatching(new Date().getTime());
        accumulator.accumulate(eventHeaders, notification);
        persistBatch(accumulator.getDefaultBatch(), accumulator.getGroupedBatch());
    } // persistOne
    
    @Override
    void persistBatch(Batch defaultBatch, Batch groupedBatch) throws Exception {
        // select batch depending on the enable grouping parameter
        Batch batch = (enableGrouping ? groupedBatch : defaultBatch);
        
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if
 
        // iterate on the destinations, for each one a single create / append will be performed
        for (String destination : batch.getDestinations()) {
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the " + destination
                    + " destination");

            // get the sub-batch for this destination
            ArrayList<CygnusEvent> subBatch = batch.getEvents(destination);
            
            // get an aggregator for this destination and initialize it
            HDFSAggregator aggregator = getAggregator(fileFormat);
            aggregator.initialize(subBatch.get(0));

            for (CygnusEvent cygnusEvent : subBatch) {
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
            createHiveTable(aggregator);
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
        protected String firstLevel;
        protected String secondLevel;
        protected String thirdLevel;
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
        
        public String getFolder() {
            return hdfsFolder;
        } // getFolder
        
        public String getFile() {
            return hdfsFile;
        } // getFile
        
        public String getHiveFields() {
            return hiveFields;
        } // getHiveFields
        
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            service = cygnusEvent.getService();
            servicePath = cygnusEvent.getServicePath();
            destination = cygnusEvent.getDestination();
            firstLevel = buildFirstLevel(service);
            secondLevel = buildSecondLevel(servicePath);
            thirdLevel = buildThirdLevel(destination);
            hdfsFolder = firstLevel + "/" + secondLevel + "/" + thirdLevel;
            hdfsFile = hdfsFolder + "/" + thirdLevel + ".txt";
        } // initialize
        
        public abstract void aggregate(CygnusEvent cygnusEvent) throws Exception;
        
    } // HDFSAggregator
    
    /**
     * Class for aggregating batches in JSON row mode.
     */
    private class JSONRowAggregator extends HDFSAggregator {
        
        @Override
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
            hiveFields = Constants.RECV_TIME_TS + " bigint, "
                    + Constants.RECV_TIME + " string, "
                    + Constants.ENTITY_ID + " string, "
                    + Constants.ENTITY_TYPE + " string, "
                    + Constants.ATTR_NAME + " string, "
                    + Constants.ATTR_TYPE + " string, "
                    + Constants.ATTR_VALUE + " string, "
                    + Constants.ATTR_MD + " array<struct<name:string,type:string,value:string>>";
        } // initialize
        
        @Override
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = Utils.getHumanReadable(recvTimeTs, true);

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
                    + "\"" + Constants.RECV_TIME_TS + "\":\"" + recvTimeTs / 1000 + "\","
                    + "\"" + Constants.RECV_TIME + "\":\"" + recvTime + "\","
                    + "\"" + Constants.HEADER_NOTIFIED_SERVICE_PATH + "\":\"" + servicePath + "\","
                    + "\"" + Constants.ENTITY_ID + "\":\"" + entityId + "\","
                    + "\"" + Constants.ENTITY_TYPE + "\":\"" + entityType + "\","
                    + "\"" + Constants.ATTR_NAME + "\":\"" + attrName + "\","
                    + "\"" + Constants.ATTR_TYPE + "\":\"" + attrType + "\","
                    + "\"" + Constants.ATTR_VALUE + "\":" + attrValue + ","
                    + "\"" + Constants.ATTR_MD + "\":" + attrMetadata
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
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
            
            // particular initialization
            hiveFields = Constants.RECV_TIME + " string";
            
            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = cygnusEvent.getContextElement().getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                return;
            } // if
            
            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                hiveFields += "," + attrName + " string," + attrName
                        + "_md array<struct<name:string,type:string,value:string>>";
            } // for
        } // initialize
        
        @Override
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = Utils.getHumanReadable(recvTimeTs, true);

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
            
            String line = "{\"" + Constants.RECV_TIME + "\":\"" + recvTime + "\","
                    + "\"" + Constants.HEADER_NOTIFIED_SERVICE_PATH + "\":\"" + servicePath + "\","
                    + "\"" + Constants.ENTITY_ID + "\":\"" + entityId + "\","
                    + "\"" + Constants.ENTITY_TYPE + "\":\"" + entityType + "\"";
            
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
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
            hiveFields = Constants.RECV_TIME_TS + " bigint, "
                    + Constants.RECV_TIME + " string, "
                    + Constants.ENTITY_ID + " string, "
                    + Constants.ENTITY_TYPE + " string, "
                    + Constants.ATTR_NAME + " string, "
                    + Constants.ATTR_TYPE + " string, "
                    + Constants.ATTR_VALUE + " string, "
                    + Constants.ATTR_MD_FILE + " string";
        } // initialize

        @Override
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = Utils.getHumanReadable(recvTimeTs, true);

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
                String thirdLevelMd = buildThirdLevelMd(destination, attrName, attrType);
                String attrMdFolder = firstLevel + "/" + secondLevel + "/" + thirdLevelMd;
                String attrMdFileName = attrMdFolder + "/" + thirdLevelMd + ".txt";
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
                String line = recvTimeTs / 1000 + ","
                    + recvTime + ","
                    + servicePath + ","
                    + entityId + ","
                    + entityType + ","
                    + attrName + ","
                    + attrType + ","
                    + attrValue.replaceAll("\"", "") + ","
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
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            super.initialize(cygnusEvent);
            
            // particular initialization
            hiveFields = Constants.RECV_TIME + " string";
            
            // iterate on all this context element attributes; it is supposed all the entity's attributes are notified
            ArrayList<ContextAttribute> contextAttributes = cygnusEvent.getContextElement().getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                return;
            } // if
            
            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String thirdLevelMd = buildThirdLevelMd(destination, attrName, attrType);
                String attrMdFolder = firstLevel + "/" + secondLevel + "/" + thirdLevelMd;
                String attrMdFileName = attrMdFolder + "/" + thirdLevelMd + ".txt";
                mdAggregations.put(attrMdFileName, new String());
                hiveFields += "," + attrName + " string," + attrName + "_md_file string";
            } // for
        } // initialize

        @Override
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            String recvTime = Utils.getHumanReadable(recvTimeTs, true);

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
            
            String line = recvTime + "," + servicePath + "," + entityId + "," + entityType;
            
            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                
                // this has to be done notification by notification and not at initialization since in row mode not all
                // the notifications contain all the attributes
                String thirdLevelMd = buildThirdLevelMd(destination, attrName, attrType);
                String attrMdFolder = firstLevel + "/" + secondLevel + "/" + thirdLevelMd;
                String attrMdFileName = attrMdFolder + "/" + thirdLevelMd + ".txt";
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
                line += "," + attrValue.replaceAll("\"", "") + "," + printableAttrMdFileName;
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
        String hdfsFolder = aggregator.getFolder();
        String hdfsFile = aggregator.getFile();
        
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
    
    private void createHiveTable(HDFSAggregator aggregator) throws Exception {
        persistenceBackend.provisionHiveTable(fileFormat, aggregator.getFolder(), aggregator.getHiveFields());
    } // createHiveTable
    
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

} // OrionHDFSSink
