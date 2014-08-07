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

import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusPersistenceError;
import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusRuntimeError;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
     * @param cosmosDefaultUsername
     * @param cosmosDefaultPassword
     */
    public HttpFSBackend(String cosmosHost, String cosmosPort, String cosmosDefaultUsername,
            String cosmosDefaultPassword,  String hivePort) {
        super(cosmosHost, cosmosPort, cosmosDefaultUsername, cosmosDefaultPassword, hivePort);
        logger = Logger.getLogger(HttpFSBackend.class);
        this.cosmosHost = cosmosHost;
        this.cosmosPort = cosmosPort;
        this.cosmosDefaultUsername = cosmosDefaultUsername;
        this.cosmosDefaultPassword = cosmosDefaultPassword;
    } // HttpFSBackend
   
    @Override
    public void createDir(DefaultHttpClient httpClient, String username, String dirPath) throws Exception {
        HttpPut request = null;
        HttpResponse response = null;
        
        try {
            // check the username
            if (username == null) {
                username = this.cosmosDefaultUsername;
            } // if

            // create the HttpFS URL
            String url = "http://" + cosmosHost + ":" + cosmosPort + "/webhdfs/v1/user/" + username + "/" + dirPath
                    + "?op=mkdirs&user.name=" + username;

            // do the put
            request = new HttpPut(url);
            logger.debug("HttpFS operation: " + request.toString());
            response = null;
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch
        
        try {
            request.releaseConnection();
            logger.debug("HttpFS response: " + response.getStatusLine().toString());

            // check the status
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new CygnusPersistenceError("The " + dirPath + " directory could not be created in HDFS. "
                        + "HttpFS response: " + response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase());
            } // if
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
    } // createDir
    
    @Override
    public void createFile(DefaultHttpClient httpClient, String username, String filePath, String data)
        throws Exception {
        HttpPut request = null;
        HttpResponse response = null;
        String url = null;
        
        try {
            // check the username
            if (username == null) {
                username = this.cosmosDefaultUsername;
            } // if

            // create the HttpFS URL
            url = "http://" + cosmosHost + ":" + cosmosPort + "/webhdfs/v1/user/" + username + "/" + filePath
                    + "?op=create&user.name=" + username;

            // do the put (first step)
            request = new HttpPut(url);
            logger.debug("HttpFS operation: " + request.toString());
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch

        try {
            request.releaseConnection();
            logger.debug("HttpFS response: " + response.getStatusLine().toString());

            // check the status
            if (response.getStatusLine().getStatusCode() != 307) {
                throw new CygnusPersistenceError("The " + filePath + " file could not be created in HDFS. "
                        + "HttpFS response: " + response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase());
            } // if

            // do the put (second step); the URL remains the same
            request = new HttpPut(url + "&data=true");
            request.setHeader("Content-Type", "application/octet-stream");
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        try {
            request.setEntity(new StringEntity(data + "\n"));
        } catch (UnsupportedEncodingException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch
            
        logger.debug("HttpFS operation: " + request.toString());
            
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch
            
        try {
            request.releaseConnection();
            logger.debug("HttpFS response: " + response.getStatusLine().toString());

            // check the status
            if (response.getStatusLine().getStatusCode() != 201) {
                throw new CygnusPersistenceError(filePath + " file created in HDFS, but could not write the "
                        + "data. HttpFS response: " + response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase());
            } // if
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // catch
    } // createFile
    
    @Override
    public void append(DefaultHttpClient httpClient, String username, String filePath, String data) throws Exception {
        HttpPost request = null;
        HttpResponse response = null;
        String url = null;
        
        try {
            // check the username
            if (username == null) {
                username = this.cosmosDefaultUsername;
            } // if

            // create the HttpFS URL
            url = "http://" + cosmosHost + ":" + cosmosPort + "/webhdfs/v1/user/" + username + "/" + filePath
                    + "?op=append&user.name=" + username;

            // do the post (first step)
            request = new HttpPost(url);
            logger.debug("HttpFS operation: " + request.toString());
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
                    
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch
        
        try {
            request.releaseConnection();
            logger.debug("HttpFS response: " + response.getStatusLine().toString());

            // check the status
            if (response.getStatusLine().getStatusCode() != 307) {
                throw new CygnusPersistenceError("The " + filePath + " file seems to not exist in HDFS. "
                        + "HttpFS response: " + response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase());
            } // if

            // do the post (second step); the URL remains the same
            request = new HttpPost(url + "&data=true");
            request.setHeader("Content-Type", "application/octet-stream");
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        try {
            request.setEntity(new StringEntity(data + "\n"));
        } catch (UnsupportedEncodingException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch
        
        logger.debug("HttpFS operation: " + request.toString());
    
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch
        
        try {
            request.releaseConnection();
            logger.debug("HttpFS response: " + response.getStatusLine().toString());

            // check the status
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new CygnusPersistenceError(filePath + " file exists in HDFS, but could not write the "
                        + "data. HttpFS response: " + response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase());
            } // if
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // catch
    } // append
    
    @Override
    public boolean exists(DefaultHttpClient httpClient, String username, String filePath) throws Exception {
        HttpResponse response = null;
        HttpGet request = null;
        
        try {
            // check the username
            if (username == null) {
                username = this.cosmosDefaultUsername;
            } // if

            // create the HttpFS URL
            String url = "http://" + cosmosHost + ":" + cosmosPort + "/webhdfs/v1/user/" + username + "/" + filePath
                    + "?op=getfilestatus&user.name=" + username;

            // do the get
            request = new HttpGet(url);
            logger.debug("HttpFS operation: " + request.toString());
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
        
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch
        
        try {
            request.releaseConnection();
            logger.debug("HttpFS response: " + response.getStatusLine().toString());

            // check the status
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            } else {
                return false;
            } // if else
        } catch (Exception e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // catch
    } // exists
    
} // HttpFSBackend
