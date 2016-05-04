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
import com.telefonica.iot.cygnus.log.CygnusLogger;
import org.apache.flume.Context;

import java.util.*;

/**
 *
 * @author cardiealb
 *
 * Detailed documentation can be found at:
 * https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/flume_extensions_catalogue/orion_hdfs_sink.md
 */
public class TwitterHDFSSink extends TwitterSink {

    /**
     * Available backend implementation.
     */
    public enum BackendImpl { BINARY, REST }

    private static final CygnusLogger LOGGER = new CygnusLogger(TwitterHDFSSink.class);
    private String[] host;
    private String port;
    private String username;
    private String password;
    private String hdfs_folder;
    private String hdfs_file;
    private String oauth2Token;
    private boolean enableKrb5;
    private String krb5User;
    private String krb5Password;
    private String krb5LoginConfFile;
    private String krb5ConfFile;
    private BackendImpl backendImpl;
    private HDFSBackend persistenceBackend;

    /**
     * Constructor.
     */
    public TwitterHDFSSink() {
        super();
    } // TwitterHDFSSink

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


    protected boolean getEnableHive() {
        return false;
    } // getEnableHive

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

        String hdfsFolder = context.getString("hdfs_folder");
        if (hdfsFolder != null && hdfsFolder.length() > 0) {
            hdfs_folder = hdfsFolder;
            LOGGER.debug("[" + this.getName() + "] Reading configuration (hdfs_folder=" + hdfs_folder + ")");
        } else {
            LOGGER.error("[" + this.getName() + "] No folder provided. Cygnus can continue, but HDFS sink will not "
                    + "properly work!");
        } // if else

        String hdfsFile = context.getString("hdfs_file");
        if (hdfsFile != null && hdfsFile.length() > 0) {
            hdfs_file = hdfsFile;
            LOGGER.debug("[" + this.getName() + "] Reading configuration (hdfs_file=" + hdfs_file + ")");
        } else {
            LOGGER.error("[" + this.getName() + "] No filename provided. Cygnus can continue, but HDFS sink will not "
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

    } // configure

    @Override
    public void start() {
        try {
            // create the persistence backend
            if (backendImpl == BackendImpl.BINARY) {
                persistenceBackend = new HDFSBackendImplBinary(host, port, username, password, oauth2Token,
                        "", "", "", enableKrb5, krb5User, krb5Password, krb5LoginConfFile,
                        krb5ConfFile, false);
            } else if (backendImpl == BackendImpl.REST) {
                persistenceBackend = new HDFSBackendImplREST(host, port, username, password, oauth2Token,
                        "", "", "", enableKrb5, krb5User, krb5Password, krb5LoginConfFile,
                        krb5ConfFile, false);
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
    void persistBatch(TwitterBatch batch) throws Exception {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if

        // get an aggregator for this destination and initialize it
        HDFSAggregator aggregator = new JSONRowAggregator();

        ArrayList<TwitterEvent> subBatch = batch.getBatch();
        aggregator.initialize(hdfs_folder, hdfs_file);
        for (TwitterEvent cygnusEvent : subBatch) {
            aggregator.aggregate(cygnusEvent);
        } // for

        // persist the aggregation
        persistAggregation(aggregator);


    } // persistBatch

    /**
     * Class for aggregating aggregation.
     */
    private abstract class HDFSAggregator {

        // string containing the data aggregation
        protected String aggregation;
        protected String hdfsFolder;
        protected String hdfsFile;

        public HDFSAggregator() {
            aggregation = "";
        } // HDFSAggregator

        public String getAggregation() {
            return aggregation;
        } // getAggregation

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

        public void initialize(String hdfs_folder, String hdfs_file) throws Exception {
            hdfsFolder = hdfs_folder;
            hdfsFile = hdfsFolder + "/" + hdfs_file;
        } // initialize

        public abstract void aggregate(TwitterEvent cygnusEvent) throws Exception;

    } // HDFSAggregator

    /**
     * Class for aggregating batches in JSON row mode.
     */
    private class JSONRowAggregator extends HDFSAggregator {

        @Override
        public void initialize(String hdfs_folder, String hdfs_file) throws Exception {
            super.initialize(hdfs_folder, hdfs_file);
        } // initialize

        @Override
        public void aggregate(TwitterEvent cygnusEvent) throws Exception {

            // get the event data (tweet)
            String eventData = cygnusEvent.getEventData();

            if (eventData == null || eventData.isEmpty()) {
                LOGGER.warn("No event data (tweet), nothing is done ");
                return;
            } // if

            aggregation += tweet2row(eventData);
        } // aggregate

        private String tweet2row(String eventData) {
            return eventData.replaceAll("\n", "") + "\n";
        }

    } // JSONRowAggregator

    private void persistAggregation(HDFSAggregator aggregator) throws Exception {
        String aggregation = aggregator.getAggregation();
        String hdfsFolder = aggregator.getFolder(enableLowercase);
        String hdfsFile = aggregator.getFile(enableLowercase);

        LOGGER.info("[" + this.getName() + "] Persisting data at TwitterHDFSSink. HDFS file ("
                + hdfsFile + "), Data (" + aggregation + ")");

        if (persistenceBackend.exists(hdfsFile)) {
            persistenceBackend.append(hdfsFile, aggregation);
        } else {
            persistenceBackend.createDir(hdfsFolder);
            persistenceBackend.createFile(hdfsFile, aggregation);
        } // if else
    } // persistAggregation

} // TwitterHDFSSink
