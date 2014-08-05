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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

/**
 *
 * @author frb
 * 
 * HDFS persistence based on the Hadoop's standard WebHDFS service (TCP/50070). Most of data-related operations within
 * WebHDFS require two steps. The firsts one, when the namenode is accessed at TCP/50070 port in order to find the
 * datanode hosting the data block to be read or write; a temporary redirection is sent back containing the URL for the
 * datanoed. The second step occurs when the specific datanode is accessed according the redirection (at TCP/50075 port)
 * in order to perform the final I/O operation.
 */
public class WebHDFSBackend extends HDFSBackend {
    
    private Logger logger;
    
    /**
     * 
     * @param cosmosHost
     * @param cosmosPort
     * @param cosmosUsername
     * @param cosmosPassword
     * @param cosmosDefaultUsername
     */
    public WebHDFSBackend(String cosmosHost, String cosmosPort, String cosmosPassword, String cosmosDataset,
            String hivePort) {
        super(cosmosHost, cosmosPort, cosmosPassword, cosmosDataset, hivePort);
        logger = Logger.getLogger(HttpFSBackend.class);
        this.cosmosHost = cosmosHost;
        this.cosmosPort = cosmosPort;
        this.cosmosDefaultUsername = cosmosDataset;
    } // WebHDFSBackend
    
    @Override
    public void createDir(DefaultHttpClient httpClient, String username, String dirPath) throws Exception {
        // check the username
        if (username == null) {
            username = this.cosmosDefaultUsername;
        } // if
        
        // create the HttpFS URL
        String url = "http://" + cosmosHost + ":" + cosmosPort + "/webhdfs/v1/user/" + username + "/" + dirPath
                + "?op=mkdirs&user.name=" + username;
        
        // do the put
        HttpPut request = new HttpPut(url);
        logger.debug("WebHDFS operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        request.releaseConnection();
        logger.debug("WebHDFS response: " + response.getStatusLine().toString());
        
        // check the status
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new Exception("The " + dirPath + " directory could not be created in HDFS. WebHDFS response: "
                    + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        } // if
    } // createDir
    
    @Override
    public void createFile(DefaultHttpClient httpClient, String username, String filePath, String data)
        throws Exception {
        // check the username
        if (username == null) {
            username = this.cosmosDefaultUsername;
        } // if
        
        // create the HttpFS URL
        String url = "http://" + cosmosHost + ":" + cosmosPort + "/webhdfs/v1/user/" + username + "/" + filePath
                + "?op=create&user.name=" + username;
        
        // do the put (first step)
        HttpPut request = new HttpPut(url);
        logger.debug("WebHDFS operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        request.releaseConnection();
        logger.debug("WebHDFS response: " + response.getStatusLine().toString());
        
        // check the status
        if (response.getStatusLine().getStatusCode() != 307) {
            throw new Exception("The " + filePath + " file could not be created in HDFS. WebHDFS response: "
                    + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        } // if
        
        // get the redirection location
        Header header = response.getHeaders("Location")[0];
        String location = header.getValue();

        // do the post (second step) using the redirection location
        request = new HttpPut(location);
        request.setHeader("Content-Type", "application/octet-stream");
        request.setEntity(new StringEntity(data + "\n"));
        logger.debug("WebHDFS operation: " + request.toString());
        response = httpClient.execute(request);
        request.releaseConnection();
        logger.debug("WebHDFS response: " + response.getStatusLine().toString());
        
        // check the status
        if (response.getStatusLine().getStatusCode() != 201) {
            throw new Exception(filePath + " file created in HDFS, but could not write the data. WebHDFS response: "
                    + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        } // if
    } // createFile
    
    @Override
    public void append(DefaultHttpClient httpClient, String username, String filePath, String data) throws Exception {
        // check the username
        if (username == null) {
            username = this.cosmosDefaultUsername;
        } // if
        
        // create the HttpFS URL
        String url = "http://" + cosmosHost + ":" + cosmosPort + "/webhdfs/v1/user/" + username + "/" + filePath
                + "?op=append&user.name=" + username;
        
        // do the post (first step)
        HttpPost request = new HttpPost(url);
        logger.debug("WebHDFS operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        request.releaseConnection();
        logger.debug("WebHDFS response: " + response.getStatusLine().toString());

        // check the status
        if (response.getStatusLine().getStatusCode() != 307) {
            throw new Exception("The " + filePath + " file seems to not exist in HDFS. WebHDFS response: "
                    + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        } // if

        // get the redirection location
        Header header = response.getHeaders("Location")[0];
        String location = header.getValue();
        
        // do the post (second step) using the redirection location
        request = new HttpPost(location);
        request.setHeader("Content-Type", "application/octet-stream");
        request.setEntity(new StringEntity(data + "\n"));
        logger.debug("WebHDFS operation: " + request.toString());
        response = httpClient.execute(request);
        request.releaseConnection();
        logger.debug("WebHDFS response: " + response.getStatusLine().toString());

        // check the status
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new Exception(filePath + " file exists in HDFS, but could not write the data. WebHDFS response: "
                    + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        } // if
    } // append
    
    @Override
    public boolean exists(DefaultHttpClient httpClient, String username, String filePath) throws Exception {
        // check the username
        if (username == null) {
            username = this.cosmosDefaultUsername;
        } // if
        
        // create the HttpFS URL
        String url = "http://" + cosmosHost + ":" + cosmosPort + "/webhdfs/v1/user/" + username + "/" + filePath
                + "?op=getfilestatus&user.name=" + username;
        
        // do the get
        HttpGet request = new HttpGet(url);
        logger.debug("WebHDFS operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        request.releaseConnection();
        logger.debug("WebHDFS response: " + response.getStatusLine().toString());
        
        // check the status
        if (response.getStatusLine().getStatusCode() == 200) {
            return true;
        } else {
            return false;
        } // if else
    } // exists
    
} // WebHDFSBackend