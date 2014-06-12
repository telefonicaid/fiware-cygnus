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

package es.tid.fiware.fiwareconnectors.cygnus.backends.hdfs;

import es.tid.fiware.fiwareconnectors.cygnus.hive.HiveClient;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

/**
 * Interface for those backends implementing the persistence in HDFS.
 * 
 * @author frb
 */
public abstract class HDFSBackend {
    
    protected String cosmosHost;
    protected String cosmosPort;
    protected String cosmosUsername;
    protected String cosmosPassword;
    protected String cosmosDataset;
    private Logger logger;
    
    /**
     * 
     * @param cosmosHost
     * @param cosmosPort
     * @param cosmosUsername
     * @param cosmosDataset
     */
    public HDFSBackend(String cosmosHost, String cosmosPort, String cosmosUsername, String cosmosPassword,
            String cosmosDataset) {
        this.cosmosHost = cosmosHost;
        this.cosmosPort = cosmosPort;
        this.cosmosUsername = cosmosUsername;
        this.cosmosPassword = cosmosPassword;
        this.cosmosDataset = cosmosDataset;
        logger = Logger.getLogger(HDFSBackend.class);
    } // HDFSBackend

    /**
     * Provision the necessary Hive external tables.
     * @throws Exception
     */
    public void provisionHive() throws Exception {
        // FIXME: this is only valid for the row-like persistence!!!
        
        logger.info("Creating Hive external table " + cosmosUsername + "_"
                + cosmosDataset.replaceAll("/", "_"));
        HiveClient hiveClient = new HiveClient(cosmosHost, "10000", cosmosUsername, cosmosPassword);

        String fields = "("
                + Constants.RECV_TIME_TS + " bigint, "
                + Constants.RECV_TIME + " string, "
                + Constants.ENTITY_ID + " string, "
                + Constants.ENTITY_TYPE + " string, "
                + Constants.ATTR_NAME + " string, "
                + Constants.ATTR_TYPE + " string, "
                + Constants.ATTR_VALUE + " string, "
                + Constants.ATTR_MD + " string"
                + ")";

        String query = "create external table " + cosmosUsername + "_"
                + cosmosDataset.replaceAll("/", "_") + " " + fields + "  row format serde "
                + "'org.openx.data.jsonserde.JsonSerDe' location '/user/" + cosmosUsername + "/" + cosmosDataset
                + "'";

        if (!hiveClient.doCreateTable(query)) {
            logger.warn("The HiveQL external table could not be created, but Cygnus can continue working... "
                    + "Check your Hive/Shark installation");
        } // if
    } // provisionHive
    
    /**
     * Creates a directory in HDFS.
     * 
     * @param httpClient HTTP client for accessing the backend server.
     * @param dirPath Directory to be created.
     */
    public abstract void createDir(DefaultHttpClient httpClient, String dirPath) throws Exception;
    
    /**
     * Creates a file in HDFS with initial content.
     * 
     * @param httpClient HTTP client for accessing the backend server.
     * @param filePath File to be created.
     * @param data Data to be written in the created file.
     */
    public abstract void createFile(DefaultHttpClient httpClient, String filePath, String data) throws Exception;
    
    /**
     * Appends data to an existent file in HDFS.
     * 
     * @param httpClient HTTP client for accessing the backend server.
     * @param filePath File where to be append the data.
     * @param data Data to be appended in the file.
     */
    public abstract void append(DefaultHttpClient httpClient, String filePath, String data) throws Exception;
    
    /**
     * Checks if the file exists in HDFS.
     * 
     * @param httpClient HTTP client for accessing the backend server.
     * @param filePath File that must be checked.
     */
    public abstract boolean exists(DefaultHttpClient httpClient, String filePath) throws Exception;
    
} // HDFSBackend
