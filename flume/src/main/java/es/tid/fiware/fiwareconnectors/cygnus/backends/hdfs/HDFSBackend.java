/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * francisco.romerobueno@telefonica.com
 */

package es.tid.fiware.fiwareconnectors.cygnus.backends.hdfs;

import es.tid.fiware.fiwareconnectors.cygnus.hive.HiveClient;
import es.tid.fiware.fiwareconnectors.cygnus.http.HttpClientFactory;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Utils;
import java.util.Arrays;
import java.util.LinkedList;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

/**
 * Interface for those backends implementing the persistence in HDFS.
 * 
 * @author frb
 */
public abstract class HDFSBackend {
    
    protected LinkedList<String> cosmosHost; // a linked list is used because the order is important
    protected String cosmosPort;
    protected String cosmosDefaultUsername;
    protected String cosmosDefaultPassword;
    protected String hiveHost;
    protected String hivePort;
    protected HttpClientFactory httpClientFactory;
    protected HttpClient httpClient;
    private Logger logger;
    
    /**
     * 
     * @param cosmosHost
     * @param cosmosPort
     * @param cosmosUsername
     * @param cosmosDefaultUsername
     * @param cosmosDefaultPassword
     * @param hivePort
     */
    public HDFSBackend(String[] cosmosHost, String cosmosPort, String cosmosDefaultUsername,
            String cosmosDefaultPassword, String hiveHost, String hivePort) {
        // this class attributes
        this.cosmosHost = new LinkedList(Arrays.asList(cosmosHost));
        this.cosmosPort = cosmosPort;
        this.cosmosDefaultPassword = cosmosDefaultPassword;
        this.cosmosDefaultUsername = cosmosDefaultUsername;
        this.hiveHost = hiveHost;
        this.hivePort = hivePort;

        // create a Http clients factory (no SSL) and an initial connection (no SSL)
        httpClientFactory = new HttpClientFactory(false);
        httpClient = httpClientFactory.getHttpClient(false);

        // logger
        logger = Logger.getLogger(HDFSBackend.class);
    } // HDFSBackend
    
    /**
     * Sets the http client. This is protected since it is only used by the tests.
     * @param httpClient
     */
    protected void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    } // setHttpClient

    /**
     * Provisions a Hive external table (row mode).
     * @param username
     * @param dirPath
     * @throws Exception
     */
    public void provisionHiveTable(String username, String dirPath) throws Exception {
        // get the table name to be created
        // the replacement is necessary because Hive, due it is similar to MySQL, does not accept '-' in the table names
        String tableName = Utils.encodeHive(username + "_" + dirPath) + "_row";
        logger.info("Creating Hive external table=" + tableName);
        
        // get a Hive client
        HiveClient hiveClient = new HiveClient(hiveHost, hivePort, cosmosDefaultUsername, cosmosDefaultPassword);

        // create the standard 8-fields
        String fields = "("
                + Constants.RECV_TIME_TS + " bigint, "
                + Constants.RECV_TIME + " string, "
                + Constants.ENTITY_ID + " string, "
                + Constants.ENTITY_TYPE + " string, "
                + Constants.ATTR_NAME + " string, "
                + Constants.ATTR_TYPE + " string, "
                + Constants.ATTR_VALUE + " string, "
                + Constants.ATTR_MD + " array<string>"
                + ")";

        // create the query
        
        String query = "create external table " + tableName + " " + fields + " row format serde "
                + "'org.openx.data.jsonserde.JsonSerDe' location '/user/" + username + "/" + dirPath + "'";

        // execute the query
        if (!hiveClient.doCreateTable(query)) {
            logger.warn("The HiveQL external table could not be created, but Cygnus can continue working... "
                    + "Check your Hive/Shark installation");
        } // if
    } // provisionHiveTable
    
    /**
     * Provisions a Hive external table (column mode).
     * @param username
     * @param dirPath
     * @param fields
     * @throws Exception
     */
    public void provisionHiveTable(String username, String dirPath, String fields) throws Exception {
        // get the table name to be created
        // the replacement is necessary because Hive, due it is similar to MySQL, does not accept '-' in the table names
        String tableName = Utils.encodeHive(username + "_" + dirPath) + "_column";
        logger.info("Creating Hive external table=" + tableName);
        
        // get a Hive client
        HiveClient hiveClient = new HiveClient(hiveHost, hivePort, cosmosDefaultUsername, cosmosDefaultPassword);
        
        // create the query
        String query = "create external table " + tableName + " (" + fields + ") row format serde "
                + "'org.openx.data.jsonserde.JsonSerDe' location '/user/" + username + "/" + dirPath + "'";

        // execute the query
        if (!hiveClient.doCreateTable(query)) {
            logger.warn("The HiveQL external table could not be created, but Cygnus can continue working... "
                    + "Check your Hive/Shark installation");
        } // if
    } // provisionHiveTable
    
    /**
     * Creates a directory in HDFS such as hdfs:///user/<username>/<organization>/<dirPath>/. If username is null, the
     * default one is used. If organization is null, the default one is used.
     * 
     * @param username Cosmos username
     * @param dirPath Directory to be created
     */
    public abstract void createDir(String username, String dirPath) throws Exception;
    
    /**
     * Creates a file in HDFS with initial content such as hdfs:///user/<username>/<organization>/<filePath>. If
     * username is null, the default one is used. If organization is null, the default one is used.
     * 
     * @param username Cosmos username
     * @param filePath File to be created
     * @param data Data to be written in the created file
     */
    public abstract void createFile(String username, String filePath, String data)
        throws Exception;
    /**
     * Appends data to an existent file in HDFS.
     * 
     * @param username Cosmos username
     * @param filePath File to be created
     * @param data Data to be appended in the file
     */
    public abstract void append(String username, String filePath, String data)
        throws Exception;
    /**
     * Checks if the file exists in HDFS.
     * 
     * @param username Cosmos username
     * @param filePath File that must be checked
     */
    public abstract boolean exists(String username, String filePath) throws Exception;
    
} // HDFSBackend
