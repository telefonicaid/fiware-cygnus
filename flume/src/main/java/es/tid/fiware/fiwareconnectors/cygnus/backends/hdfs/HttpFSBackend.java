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
 * HDFS persistence based on the HttpFS service (TCP/14000). HttpFS is an alternative implemenatation of the WebHDFS
 * API which hides the cluster details by forwarding directly to the Master node instead of to the Data node.
 */
public class HttpFSBackend extends HDFSBackend {
    
    private Logger logger;
    
    /**
     * 
     * @param cosmosHost
     * @param cosmosPort
     * @param cosmosUsername
     * @param cosmosDataset
     */
    public HttpFSBackend(String cosmosHost, String cosmosPort, String cosmosUsername, String cosmosDataset) {
        super(cosmosHost, cosmosPort, cosmosUsername, cosmosDataset);
        logger = Logger.getLogger(HttpFSBackend.class);
        this.cosmosHost = cosmosHost;
        this.cosmosPort = cosmosPort;
        this.cosmosUsername = cosmosUsername;
        this.cosmosDataset = cosmosDataset;
    } // HttpFSBackend
   
    @Override
    public void createDir(DefaultHttpClient httpClient, String dirPath) throws Exception {
        // create the HttpFS URL
        String url = "http://" + cosmosHost + ":" + cosmosPort + "/webhdfs/v1/user/" + cosmosUsername + "/"
                + cosmosDataset + "/" + dirPath + "?op=mkdirs&user.name=" + cosmosUsername;
        
        // do the put
        HttpPut request = new HttpPut(url);
        logger.info("HttpFS operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        request.releaseConnection();
        logger.info("HttpFS response: " + response.getStatusLine().toString());
        
        // check the status
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new Exception("The " + dirPath + " directory could not be created in HDFS. HttpFS response: "
                    + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        } // if
    } // createDir
    
    @Override
    public void createFile(DefaultHttpClient httpClient, String filePath, String data) throws Exception {
        // create the HttpFS URL
        String url = "http://" + cosmosHost + ":" + cosmosPort + "/webhdfs/v1/user/" + cosmosUsername + "/"
                + cosmosDataset + "/" + filePath + "?op=create&user.name=" + cosmosUsername;
        
        // do the put (first step)
        HttpPut request = new HttpPut(url);
        logger.info("HttpFS operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        request.releaseConnection();
        logger.info("HttpFS response: " + response.getStatusLine().toString());
        
        // check the status
        if (response.getStatusLine().getStatusCode() != 307) {
            throw new Exception("The " + filePath + " file could not be created in HDFS. HttpFS response: "
                    + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        } // if

        // do the put (second step); the URL remains the same
        request = new HttpPut(url + "&data=true");
        request.setHeader("Content-Type", "application/octet-stream");
        request.setEntity(new StringEntity(data + "\n"));
        logger.info("HttpFS operation: " + request.toString());
        response = httpClient.execute(request);
        request.releaseConnection();
        logger.info("HttpFS response: " + response.getStatusLine().toString());
        
        // check the status
        if (response.getStatusLine().getStatusCode() != 201) {
            throw new Exception(filePath + " file created in HDFS, but could not write the data. HttpFS response: "
                    + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        } // if
    } // createFile
    
    @Override
    public void append(DefaultHttpClient httpClient, String filePath, String data) throws Exception {
        // create the HttpFS URL
        String url = "http://" + cosmosHost + ":" + cosmosPort + "/webhdfs/v1/user/" + cosmosUsername + "/"
                + cosmosDataset + "/" + filePath + "?op=append&user.name=" + cosmosUsername;
        
        // do the post (first step)
        HttpPost request = new HttpPost(url);
        logger.info("HttpFS operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        request.releaseConnection();
        logger.info("HttpFS response: " + response.getStatusLine().toString());

        // check the status
        if (response.getStatusLine().getStatusCode() != 307) {
            throw new Exception("The " + filePath + " file seems to not exist in HDFS. HttpFS response: "
                    + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        } // if
                
        // do the post (second step); the URL remains the same
        request = new HttpPost(url + "&data=true");
        request.setHeader("Content-Type", "application/octet-stream");
        request.setEntity(new StringEntity(data + "\n"));
        logger.info("HttpFS operation: " + request.toString());
        response = httpClient.execute(request);
        request.releaseConnection();
        logger.info("HttpFS response: " + response.getStatusLine().toString());

        // check the status
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new Exception(filePath + " file exists in HDFS, but could not write the data. HttpFS response: "
                    + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        } // if
    } // append
    
    @Override
    public boolean exists(DefaultHttpClient httpClient, String filePath) throws Exception {
        // create the HttpFS URL
        String url = "http://" + cosmosHost + ":" + cosmosPort + "/webhdfs/v1/user/" + cosmosUsername + "/"
                + cosmosDataset + "/" + filePath + "?op=getfilestatus&user.name=" + cosmosUsername;
        
        // do the get
        HttpGet request = new HttpGet(url);
        logger.info("HttpFS operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        request.releaseConnection();
        logger.info("HttpFS response: " + response.getStatusLine().toString());
        
        // check the status
        if (response.getStatusLine().getStatusCode() == 200) {
            return true;
        } else {
            return false;
        } // if else
    } // exists
    
} // HttpFSBackend
