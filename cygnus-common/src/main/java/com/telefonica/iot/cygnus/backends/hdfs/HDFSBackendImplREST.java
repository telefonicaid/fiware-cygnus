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

package com.telefonica.iot.cygnus.backends.hdfs;

import com.telefonica.iot.cygnus.backends.hive.HiveBackendImpl;
import com.telefonica.iot.cygnus.backends.http.HttpBackend;
import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import java.util.ArrayList;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

/**
 *
 * @author frb
 * 
 * HDFS persistence based on the HttpFS service (TCP/14000). HttpFS is an alternative implementation of the WebHDFS
 * API which hides the cluster details by forwarding directly to the Master node instead of to the Data node.
 */
public class HDFSBackendImplREST extends HttpBackend implements HDFSBackend {
    
    private final String hdfsUser;
    private final String hdfsPassword;
    private final String hiveServerVersion;
    private final String hiveHost;
    private final String hivePort;
    private final boolean serviceAsNamespace;
    private static final CygnusLogger LOGGER = new CygnusLogger(HDFSBackendImplREST.class);
    private static final String BASE_URL = "/webhdfs/v1/user/";
    private ArrayList<Header> headers;
    
    /**
     * 
     * @param hdfsHosts
     * @param hdfsPort
     * @param hdfsUser
     * @param hdfsPassword
     * @param oauth2Token
     * @param hiveServerVersion
     * @param hiveHost
     * @param hivePort
     * @param krb5
     * @param krb5User
     * @param krb5Password
     * @param krb5LoginConfFile
     * @param krb5ConfFile
     * @param serviceAsNamespace
     */
    public HDFSBackendImplREST(String[] hdfsHosts, String hdfsPort, String hdfsUser, String hdfsPassword,
            String oauth2Token, String hiveServerVersion, String hiveHost, String hivePort, boolean krb5,
            String krb5User, String krb5Password, String krb5LoginConfFile, String krb5ConfFile,
            boolean serviceAsNamespace) {
        super(hdfsHosts, hdfsPort, false, krb5, krb5User, krb5Password, krb5LoginConfFile, krb5ConfFile);
        this.hdfsUser = hdfsUser;
        this.hdfsPassword = hdfsPassword;
        this.hiveServerVersion = hiveServerVersion;
        this.hiveHost = hiveHost;
        this.hivePort = hivePort;
        this.serviceAsNamespace = serviceAsNamespace;
        
        // add the OAuth2 token as a the unique header that will be sent
        if (oauth2Token != null && oauth2Token.length() > 0) {
            headers = new ArrayList<Header>();
            headers.add(new BasicHeader("X-Auth-Token", oauth2Token));
        } else {
            headers = null;
        } // if else
    } // HDFSBackendImplREST
   
    @Override
    public void createDir(String dirPath) throws Exception {
        String relativeURL = BASE_URL + (serviceAsNamespace ? "" : (hdfsUser + "/")) + dirPath
                + "?op=mkdirs&user.name=" + hdfsUser;
        JsonResponse response = doRequest("PUT", relativeURL, true, headers, null);

        // check the status
        if (response.getStatusCode() != 200) {
            throw new CygnusPersistenceError("The /user/" + (serviceAsNamespace ? "" : (hdfsUser + "/"))
                    + dirPath + " directory could not be created in HDFS. Server response: "
                    + response.getStatusCode() + " " + response.getReasonPhrase());
        } // if
    } // createDir
    
    @Override
    public void createFile(String filePath, String data)
        throws Exception {
        String relativeURL = BASE_URL + (serviceAsNamespace ? "" : (hdfsUser + "/")) + filePath
                + "?op=create&user.name=" + hdfsUser;
        JsonResponse response = doRequest("PUT", relativeURL, true, headers, null);
        
        // check the status
        if (response.getStatusCode() != 307) {
            throw new CygnusPersistenceError("The /user/" + (serviceAsNamespace ? "" : (hdfsUser + "/"))
                    + filePath + " file could not be created in HDFS. Server response: "
                    + response.getStatusCode() + " " + response.getReasonPhrase());
        } // if
        
        // get the redirection location
        Header header = response.getLocationHeader();
        String absoluteURL = header.getValue();

        // do second step
        if (headers == null) {
            headers = new ArrayList<Header>();
        } // if
        
        headers.add(new BasicHeader("Content-Type", "application/octet-stream"));
        response = doRequest("PUT", absoluteURL, false, headers, new StringEntity(data + "\n"));
    
        // check the status
        if (response.getStatusCode() != 201) {
            throw new CygnusPersistenceError("/user/" + (serviceAsNamespace ? "" : (hdfsUser + "/"))
                    + filePath + " file created in HDFS, but could not write the data. Server response: "
                    + response.getStatusCode() + " " + response.getReasonPhrase());
        } // if
    } // createFile
    
    @Override
    public void append(String filePath, String data) throws Exception {
        String relativeURL = BASE_URL + (serviceAsNamespace ? "" : (hdfsUser + "/")) + filePath
                + "?op=append&user.name=" + hdfsUser;
        JsonResponse response = doRequest("POST", relativeURL, true, headers, null);

        // check the status
        if (response.getStatusCode() != 307) {
            throw new CygnusPersistenceError("The /user/" + (serviceAsNamespace ? "" : (hdfsUser + "/"))
                    + filePath + " file seems to not exist in HDFS. Server response: "
                    + response.getStatusCode() + " " + response.getReasonPhrase());
        } // if

        // get the redirection location
        Header header = response.getLocationHeader();
        String absoluteURL = header.getValue();

        // do second step
        if (headers == null) {
            headers = new ArrayList<Header>();
        } // if

        headers.add(new BasicHeader("Content-Type", "application/octet-stream"));
        response = doRequest("POST", absoluteURL, false, headers, new StringEntity(data + "\n"));
        
        // check the status
        if (response.getStatusCode() != 200) {
            throw new CygnusPersistenceError("/user/" + (serviceAsNamespace ? "" : (hdfsUser + "/"))
                    + filePath + " file exists in HDFS, but could not write the data. Server response: "
                    + response.getStatusCode() + " " + response.getReasonPhrase());
        } // if
    } // append
    
    @Override
    public boolean exists(String filePath) throws Exception {
        String relativeURL = BASE_URL + (serviceAsNamespace ? "" : (hdfsUser + "/")) + filePath
                + "?op=getfilestatus&user.name=" + hdfsUser;
        JsonResponse response = doRequest("GET", relativeURL, true, headers, null);

        // check the status
        return (response.getStatusCode() == 200);
    } // exists

} // HDFSBackendImplREST
