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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericColumnAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericRowAggregator;
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

import java.util.*;

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
    protected enum FileFormat { JSONROW, JSONCOLUMN, CSVROW, CSVCOLUMN }


    protected enum Periodicity { NONE, HOURLY, DAILY, MONTHLY, YEARLY }
    
    /**
     * Available Hive database types.
     */
    public enum HiveDBType { DEFAULTDB, NAMESPACEDB }

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIHDFSSink.class);
    private String[] hosts;
    private String port;
    private String username;
    private String password;
    protected FileFormat fileFormat;
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
    private boolean enableMetadataPersistance;
    private Periodicity periodicityOfFileSeparation;

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

        String periodicityOfFileSeparationStr = context.getString("periodicity_of_file_separation", "none");

        try {
            periodicityOfFileSeparation = Periodicity.valueOf(periodicityOfFileSeparationStr.toUpperCase());
            LOGGER.debug("[" + this.getName() + "] Reading configuration (periodicity_of_file_separation="
                    + periodicityOfFileSeparationStr + ")");
        } catch (Exception e) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (periodicity_of_file_separation="
                    + periodicityOfFileSeparationStr + ") -- Must be 'none', 'hourly', 'daily', 'monthly' or 'yearly'");
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

        // metadata configuration
        String enableMetadata = context.getString("attr_metadata_store", "true");

        if (enableMetadata.equals("true") || enableMetadata.equals("false")) {
            enableMetadataPersistance = Boolean.valueOf(enableMetadata);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (metadata="
                    + enableMetadata + ")");
        }

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
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Get an aggregator for this entity and initialize it based on the first event
            NGSIGenericAggregator aggregator = getAggregator(fileFormat);
            aggregator.setService(firstEvent.getServiceForNaming(enableNameMappings));
            aggregator.setServicePathForData(firstEvent.getServicePathForData());
            aggregator.setServicePathForNaming(firstEvent.getServicePathForNaming(enableGrouping, enableNameMappings));
            aggregator.setEntityForNaming(firstEvent.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding));
            aggregator.setAttrMetadataStore(enableMetadataPersistance);
            aggregator.setHdfsFolder(buildFolderPath(aggregator.getService(), aggregator.getServicePathForNaming(), aggregator.getEntityForNaming()));
            aggregator.setHdfsFile(buildFilePath(aggregator.getService(), aggregator.getServicePathForNaming(), aggregator.getEntityForNaming(), calendar));
            aggregator.setEnableNameMappings(enableNameMappings);
            aggregator.initialize(firstEvent);
            for (NGSIEvent event : events) {
                aggregator.aggregate(event);
            } // for

            // Persist the aggregation
            persistAggregation(aggregator, service, servicePath);
            batch.setNextPersisted(true);
        } // for
    } // persistBatch
    
    @Override
    public void capRecords(NGSIBatch batch, long maxRecords) throws CygnusCappingError {
    } // capRecords

    @Override
    public void expirateRecords(long expirationTime) throws CygnusExpiratingError {
    } // expirateRecords

    /**
     * Gets aggregator (Row or Column) where all events will be allocated.
     *
     * @param fileFormat the file format
     * @return an object NGSIGenericAggregator.
     */
    protected NGSIGenericAggregator getAggregator(FileFormat fileFormat) {
        switch (fileFormat) {
            case JSONROW:
                return new NGSIGenericRowAggregator();
            case JSONCOLUMN:
                return new NGSIGenericColumnAggregator();
            case CSVROW:
                return new NGSIGenericRowAggregator();
            case CSVCOLUMN:
                return new NGSIGenericColumnAggregator();
            default:
                return null;
        } // switch
    } // getAggregator

    /**
     * Gets hive fields.
     *
     * @param aggregation the aggregation
     * @return the hive fields to be stored.
     */
    protected String getHiveFields(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        Iterator<String> it = aggregation.keySet().iterator();
        String hiveFields = "";
        while (it.hasNext()) {
            String field = "";
            String entry = (String) it.next();
            if (entry.equals(NGSIConstants.RECV_TIME_TS)) {
                field = NGSICharsets.encodeHive(entry) + " bigint";
            } else {
                if (fileFormat == FileFormat.CSVCOLUMN) {
                    if (entry.equals(NGSIConstants.RECV_TIME) || entry.equals(NGSIConstants.FIWARE_SERVICE_PATH) || entry.equals(NGSIConstants.ENTITY_ID) || entry.equals(NGSIConstants.ENTITY_TYPE)) {
                        field += NGSICharsets.encodeHive(entry) + " string";
                    } else if (entry.contains("_md")) {
                        field = "`" + NGSICharsets.encodeHive(entry.substring(0, entry.length() - 3)) + "_md_file` string";
                    } else {
                        field = "`" + NGSICharsets.encodeHive(entry) + "` string";
                    }
                } else {
                    if (entry.contains("_md") || entry.contains("_MD") || entry.equals(NGSIConstants.ATTR_MD)) {
                        switch (fileFormat) {
                            case JSONROW:
                                field = NGSICharsets.encodeHive(entry) + " array<struct<name:string,type:string,value:string>>";
                                break;
                            case JSONCOLUMN: field = NGSICharsets.encodeHive(entry.substring(0, entry.length() - 3)) + " array<struct<name:string,type:string,value:string>>";
                                break;
                            case CSVROW:
                                field = NGSICharsets.encodeHive(entry) + " string";
                                break;
                        } // switch
                    } else {
                        field = NGSICharsets.encodeHive(entry) + " string";
                    }
                }
            }
            if (hiveFields.isEmpty()) {
                hiveFields = field;
            } else {
                hiveFields += "," + field;
            }
        }
        return hiveFields;
    } //getHiveFields

    /**
     * Json to persist string. Stores all objects on a LinkedHashMap into a json String. Every attribute will be a Json Object inside the output String.
     *
     * @param aggregation the aggregation
     * @return a Json String.
     */
    protected String jsonToPersist(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        String json = "";
        ArrayList<JsonObject> jsonObjects = NGSIUtils.linkedHashMapToJsonList(aggregation);
        for (JsonObject jsonObject : jsonObjects) {
            if (json.isEmpty()) {
                json = jsonObject.toString().replace("\\", "").replace("\"[]\"", "[]");
            } else {
                json += "\n" + jsonObject.toString().replace("\\", "").replace("\"[]\"", "[]");
            }
        }
        return json;
    } //jsonToPersist

    /**
     * Process csv fields ngsi generic aggregator. This function also stores all attributes on a CSV String into the aggregtator output object.
     *
     * @param genericAggregator the generic aggregator
     * @return the ngsi generic aggregator.
     * @throws CygnusBadContextData the cygnus bad context data
     */
    protected NGSIGenericAggregator processCSVFields (NGSIGenericAggregator genericAggregator) throws CygnusBadContextData {
        String aggregation = "";
        ArrayList<String> attributeNames = NGSIUtils.attributeNames(genericAggregator.getAggregation());
        int numEvents = NGSIUtils.collectionSizeOnLinkedHashMap(genericAggregator.getAggregation());
        for (int i = 0; i < numEvents; i++) {
            String line = "";
            long recvTimeTs;
            if (genericAggregator instanceof NGSIGenericRowAggregator) {
                line = genericAggregator.getAggregation().get(NGSIConstants.RECV_TIME_TS).get(i).toString() + csvSeparator;
                recvTimeTs = Long.parseLong(genericAggregator.getAggregation().get(NGSIConstants.RECV_TIME_TS).get(i).getAsString());
            } else {
                recvTimeTs = Long.parseLong(genericAggregator.getAggregation().get(NGSIConstants.RECV_TIME_TS+"C").get(i).getAsString());
            }
            line += genericAggregator.getAggregation().get(NGSIConstants.RECV_TIME).get(i).toString();
            line += csvSeparator + genericAggregator.getAggregation().get(NGSIConstants.FIWARE_SERVICE_PATH).get(i).toString();
            line += csvSeparator + genericAggregator.getAggregation().get(NGSIConstants.ENTITY_ID).get(i).toString();
            line += csvSeparator + genericAggregator.getAggregation().get(NGSIConstants.ENTITY_TYPE).get(i).toString();
            if (genericAggregator instanceof NGSIGenericRowAggregator) {
                line += csvSeparator + genericAggregator.getAggregation().get(NGSIConstants.ATTR_NAME).get(i).toString();
                line += csvSeparator + genericAggregator.getAggregation().get(NGSIConstants.ATTR_TYPE).get(i).toString();
                line += csvSeparator + genericAggregator.getAggregation().get(NGSIConstants.ATTR_VALUE).get(i).toString();
                JsonElement metadata = genericAggregator.getAggregation().get(NGSIConstants.ATTR_MD).get(i);
                if (genericAggregator.isAttrMetadataStore() && metadata != null && !metadata.toString().isEmpty() && !metadata.toString().contains("[]")) {
                    String attrMdFileName = buildAttrMdFilePath(genericAggregator.getService(), genericAggregator.getServicePathForNaming(), genericAggregator.getEntityForNaming(), genericAggregator.getAggregation().get(NGSIConstants.ATTR_NAME).get(i).toString(),
                            genericAggregator.getAggregation().get(NGSIConstants.ATTR_TYPE).get(i).toString());
                    String printableAttrMdFileName = "hdfs:///user/" + username + "/" + attrMdFileName;
                    line += csvSeparator + printableAttrMdFileName;
                    if (metadata.isJsonPrimitive()) {
                        genericAggregator.setMdAggregations(persistMetadata(attrMdFileName, genericAggregator.getMdAggregations(), metadata.getAsString(), recvTimeTs));
                    } else {
                        genericAggregator.setMdAggregations(persistMetadata(attrMdFileName, genericAggregator.getMdAggregations(), metadata.toString(), recvTimeTs));
                    }
                }else {
                    if (genericAggregator.isAttrMetadataStore())
                        line += csvSeparator + "NULL";
                }
            } else {
                for (String attributeName : attributeNames) {
                    JsonElement value = genericAggregator.getAggregation().get(attributeName).get(i);
                    if (value == null) {
                        line += csvSeparator + "NULL";
                    } else {
                        line += csvSeparator + value.toString();
                    }
                    if (genericAggregator.isAttrMetadataStore()) {
                        JsonElement metadata = genericAggregator.getAggregation().get(attributeName + "_md").get(i);
                        if (genericAggregator.isAttrMetadataStore() && metadata != null && !metadata.toString().isEmpty() && !metadata.toString().contains("[]")) {
                            String attrMdFileName = buildAttrMdFilePath(genericAggregator.getService(), genericAggregator.getServicePathForNaming(), genericAggregator.getEntityForNaming(), attributeName,
                                    genericAggregator.getAggregation().get(attributeName + NGSIConstants.AUTOGENERATED_ATTR_TYPE).get(i).toString());
                            String printableAttrMdFileName = "hdfs:///user/" + username + "/" + attrMdFileName;
                            line += csvSeparator + printableAttrMdFileName;
                            if (metadata.isJsonPrimitive()) {
                                genericAggregator.setMdAggregations(persistMetadata(attrMdFileName, genericAggregator.getMdAggregations(), metadata.getAsString(), recvTimeTs));
                            } else {
                                genericAggregator.setMdAggregations(persistMetadata(attrMdFileName, genericAggregator.getMdAggregations(), metadata.toString(), recvTimeTs));
                            }
                        } else {
                            if (genericAggregator.isAttrMetadataStore())
                                line += csvSeparator + "NULL";
                        }
                    }
                }
            }
            if (aggregation.isEmpty()) {
                aggregation = line;
            } else {
                aggregation += "\n" + line;
            }
        }
        genericAggregator.setCsvString(aggregation.replaceAll("\"", "").replace("\\", ""));
        return genericAggregator;
    } //processCSVFields

    /**
     * Persist metadata map.
     *
     * @param attrMdFileName the attr md file name
     * @param mdAggregations the md aggregations
     * @param attrMetadata   the attr metadata
     * @param recvTimeTs     the recv time ts
     * @return the map
     * @throws CygnusBadContextData the cygnus bad context data
     */
    protected Map<String, String> persistMetadata(String attrMdFileName, Map<String, String> mdAggregations, String attrMetadata, long recvTimeTs) throws CygnusBadContextData {
        Map<String, String> mdAggregationMap = mdAggregations;
        String mdAggregation = mdAggregationMap.get(attrMdFileName);
        if (mdAggregation == null) {
            mdAggregation = new String();
        }
        String concatMdAggregation;
        if (mdAggregation.isEmpty()) {
            concatMdAggregation = getCSVMetadata(attrMetadata, recvTimeTs);
        } else {
            concatMdAggregation = mdAggregation.concat("\n" + getCSVMetadata(attrMetadata, recvTimeTs));
        }
        mdAggregationMap.put(attrMdFileName, concatMdAggregation);
        return mdAggregationMap;
    } //persistMetadata

    /**
     * Gets csv metadata.
     *
     * @param attrMetadata the attr metadata
     * @param recvTimeTs   the recv time ts
     * @return the csv metadata String.
     * @throws CygnusBadContextData the cygnus bad context data
     */
    protected String getCSVMetadata(String attrMetadata, long recvTimeTs) throws CygnusBadContextData {
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

    protected void persistAggregation(NGSIGenericAggregator aggregator, String service, String servicePath)
            throws CygnusPersistenceError, CygnusBadContextData {
        NGSIGenericAggregator genericAggregator = processCSVFields(aggregator);
        genericAggregator.setHiveFields(getHiveFields(genericAggregator.getAggregationToPersist()));
        String aggregation = "";
        switch (fileFormat) {
            case JSONROW:
                aggregation = jsonToPersist(genericAggregator.getAggregationToPersist());
                break;
            case JSONCOLUMN:
                aggregation = jsonToPersist(genericAggregator.getAggregationToPersist());
                break;
            case CSVROW:
                aggregation = genericAggregator.getCsvString();
                break;
            case CSVCOLUMN:
                aggregation = genericAggregator.getCsvString();
                break;
        }
        String hdfsFolder = genericAggregator.getHdfsFolder(enableLowercase);
        String hdfsFile = genericAggregator.getHdfsFile(enableLowercase);
        
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

        // Persist the metadata aggregations only in CSV-like file formats
        if (genericAggregator.isAttrMetadataStore() && (fileFormat == FileFormat.CSVROW || fileFormat == FileFormat.CSVCOLUMN)) {
            persistMDAggregations(genericAggregator);
        } // if

        // Create the Hive table
        if (enableHive) {
            if (hiveDBType == HiveDBType.NAMESPACEDB) {
                if (serviceAsNamespace) {
                    hiveBackend.doCreateDatabase(genericAggregator.getService());
                    provisionHiveTable(genericAggregator, genericAggregator.getService());
                } else {
                    hiveBackend.doCreateDatabase(username);
                    provisionHiveTable(genericAggregator, username);
                } // if else
            } else {
                provisionHiveTable(genericAggregator, "default");
            } // if else
        } // if


    } // persistAggregation

    protected void persistMDAggregations(NGSIGenericAggregator aggregator) throws CygnusPersistenceError {
        Set<String> attrMDFiles = aggregator.getMdAggregations().keySet();

        for (String hdfsMDFile : attrMDFiles) {
            String hdfsMdFolder = hdfsMDFile.substring(0, hdfsMDFile.lastIndexOf("/"));
            String mdAggregation = aggregator.getMdAggregations().get(hdfsMDFile);

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
                    LOGGER.error("[" + this.getName() + "] There was some problem with the current endpoint, "
                            + "trying other one. Details: " + e.getMessage());
                } // try catch
            } // for
        } // for
    } // persistMDAggregations

    private void provisionHiveTable(NGSIGenericAggregator aggregator, String dbName) throws CygnusPersistenceError {
        String dirPath = aggregator.getHdfsFolder(enableLowercase);
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
    protected String buildFilePath(String service, String servicePath, String destination, GregorianCalendar calendar)
        throws CygnusBadConfiguration {
        String filePath;
        String separationPrefix = "";
        switch (periodicityOfFileSeparation) {
            case NONE:
                break;
            case HOURLY:
                separationPrefix = "_" +
                        ((String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)).length() ==  1) ? "0" +  String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) : String.valueOf(calendar.get(Calendar.HOUR_OF_DAY))) +
                        ((String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)).length() ==  1) ? "0" +  String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) : String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))) +
                        ((String.valueOf(calendar.get(Calendar.MONTH) + 1).length() ==  1) ? "0" +  String.valueOf(calendar.get(Calendar.MONTH) + 1) : String.valueOf(calendar.get(Calendar.MONTH) + 1)) +
                        String.valueOf(calendar.get(Calendar.YEAR));
                break;
            case DAILY:
                separationPrefix = "_" +
                        ((String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)).length() ==  1) ? "0" +  String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) : String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))) +
                        ((String.valueOf(calendar.get(Calendar.MONTH) + 1).length() ==  1) ? "0" +  String.valueOf(calendar.get(Calendar.MONTH) + 1) : String.valueOf(calendar.get(Calendar.MONTH) + 1)) +
                        String.valueOf(calendar.get(Calendar.YEAR));
                break;
            case MONTHLY:
                separationPrefix = "_" + ((String.valueOf(calendar.get(Calendar.MONTH) + 1).length() ==  1) ? "0" +  String.valueOf(calendar.get(Calendar.MONTH) + 1) : String.valueOf(calendar.get(Calendar.MONTH) + 1)) +
                        String.valueOf(calendar.get(Calendar.YEAR));
                break;
            case YEARLY:
                separationPrefix = "_" + String.valueOf(calendar.get(Calendar.YEAR));
                break;
        }
        if (enableEncoding) {
            filePath = NGSICharsets.encodeHDFS(service, false) + NGSICharsets.encodeHDFS(servicePath, true)
                    + (servicePath.equals("/") ? "" : "/") + NGSICharsets.encodeHDFS(destination, false)
                    + "/" + NGSICharsets.encodeHDFS(destination + separationPrefix, false) + ".txt";
        } else {
            filePath = NGSIUtils.encode(service, false, true) + NGSIUtils.encode(servicePath, false, false)
                    + (servicePath.equals("/") ? "" : "/") + NGSIUtils.encode(destination, false, true)
                    + "/" + NGSIUtils.encode(destination + separationPrefix, false, true) + ".txt";
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
