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
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package es.tid.fiware.fiwareconnectors.cygnus.backends.hdfs;

import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusPersistenceError;
import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusRuntimeError;
import java.io.IOException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author frb
 * 
 * HDFS persistence based on the HttpFS service (TCP/14000). HttpFS is an alternative implementation of the WebHDFS
 * API which hides the cluster details by forwarding directly to the Master node instead of to the Data node.
 */
public class HDFSBackendImpl extends HDFSBackend {
    
    private Logger logger;
    
    /**
     * 
     * @param cosmosHost
     * @param cosmosPort
     * @param cosmosDefaultUsername
     * @param cosmosDefaultPassword
     */
    public HDFSBackendImpl(String[] cosmosHost, String cosmosPort, String cosmosDefaultUsername,
            String cosmosDefaultPassword, String hiveHost, String hivePort, boolean krb5, String krb5User,
            String krb5Password, String krb5LoginConfFile, String krb5ConfFile) {
        super(cosmosHost, cosmosPort, cosmosDefaultUsername, cosmosDefaultPassword, hiveHost, hivePort, krb5,
                krb5User, krb5Password, krb5LoginConfFile, krb5ConfFile);
        logger = Logger.getLogger(HDFSBackendImpl.class);
    } // HDFSBackendImpl
   
    @Override
    public void createDir(String username, String dirPath) throws Exception {
        String relativeURL = "/webhdfs/v1/user/" + username + "/" + dirPath + "?op=mkdirs&user.name=" + username;
        HttpResponse response = doHDFSRequest("PUT", relativeURL, true, null, null);

        // check the status
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new CygnusPersistenceError("The " + dirPath + " directory could not be created in HDFS. "
                    + "HttpFS response: " + response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase());
        } // if
    } // createDir
    
    @Override
    public void createFile(String username, String filePath, String data)
        throws Exception {
        String relativeURL = "/webhdfs/v1/user/" + username + "/" + filePath + "?op=create&user.name=" + username;
        HttpResponse response = doHDFSRequest("PUT", relativeURL, true, null, null);
        
        // check the status
        if (response.getStatusLine().getStatusCode() != 307) {
            throw new CygnusPersistenceError("The " + filePath + " file could not be created in HDFS. "
                    + "HttpFS response: " + response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase());
        } // if
        
        // get the redirection location
        Header header = response.getHeaders("Location")[0];
        String absoluteURL = header.getValue();

        // do second step
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Content-Type", "application/octet-stream"));
        response = doHDFSRequest("PUT", absoluteURL, false, headers, new StringEntity(data + "\n"));
    
        // check the status
        if (response.getStatusLine().getStatusCode() != 201) {
            throw new CygnusPersistenceError(filePath + " file created in HDFS, but could not write the "
                    + "data. HttpFS response: " + response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase());
        } // if
    } // createFile
    
    @Override
    public void append(String username, String filePath, String data) throws Exception {
        String relativeURL = "/webhdfs/v1/user/" + username + "/" + filePath + "?op=append&user.name=" + username;
        HttpResponse response = doHDFSRequest("POST", relativeURL, true, null, null);

        // check the status
        if (response.getStatusLine().getStatusCode() != 307) {
            throw new CygnusPersistenceError("The " + filePath + " file seems to not exist in HDFS. "
                    + "HttpFS response: " + response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase());
        } // if

        // get the redirection location
        Header header = response.getHeaders("Location")[0];
        String absoluteURL = header.getValue();

        // do second step
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Content-Type", "application/octet-stream"));
        response = doHDFSRequest("POST", absoluteURL, false, headers, new StringEntity(data + "\n"));
        
        // check the status
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new CygnusPersistenceError(filePath + " file exists in HDFS, but could not write the "
                    + "data. HttpFS response: " + response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase());
        } // if
    } // append
    
    @Override
    public boolean exists(String username, String filePath) throws Exception {
        String relativeURL = "/webhdfs/v1/user/" + username + "/" + filePath + "?op=getfilestatus&user.name="
                + username;
        HttpResponse response = doHDFSRequest("GET", relativeURL, true, null, null);

        // check the status
        return (response.getStatusLine().getStatusCode() == 200);
    } // exists
    
    /**
     * Does a HDFS request given a HTTP client, a method and a relative URL (the final URL will be composed by using
     * this relative URL and the active HDFS endpoint).
     * @param method
     * @param relativeURL
     * @return
     * @throws Exception
     */
    private HttpResponse doHDFSRequest(String method, String url, boolean relative, ArrayList<Header> headers,
            StringEntity entity) throws Exception {
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_SERVICE_UNAVAILABLE,
                "Service unavailable");
        
        if (relative) {
            // iterate on the hosts
            for (String host : cosmosHost) {
                // create the HttpFS URL
                String effectiveURL = "http://" + host + ":" + cosmosPort + url;
                
                try {
                    if (krb5) {
                        response = doPrivilegedHDFSRequest(method, effectiveURL, headers, entity);
                    } else {
                        response = doHDFSRequest(method, effectiveURL, headers, entity);
                    } // if else
                } catch (Exception e) {
                    logger.debug("The used HDFS endpoint is not active, trying another one (host=" + host + ")");
                    continue;
                } // try catch
                
                int status = response.getStatusLine().getStatusCode();

                if (status != 200 && status != 307 && status != 404 && status != 201) {
                    logger.debug("The used HDFS endpoint is not active, trying another one (host=" + host + ")");
                    continue;
                } // if
                
                // place the current host in the first place (if not yet placed), since it is currently working
                if (!cosmosHost.getFirst().equals(host)) {
                    cosmosHost.remove(host);
                    cosmosHost.add(0, host);
                    logger.debug("Placing the host in the first place of the list (host=" + host + ")");
                } // if
                
                break;
            } // for
        } else {
            if (krb5) {
                response = doPrivilegedHDFSRequest(method, url, headers, entity);
            } else {
                response = doHDFSRequest(method, url, headers, entity);
            } // if else
        } // if else
        
        return response;
    } // doHDFSRequest
        
    private HttpResponse doHDFSRequest(String method, String url, ArrayList<Header> headers, StringEntity entity)
        throws Exception {
        HttpResponse response = null;
        HttpRequestBase request = null;

        if (method.equals("PUT")) {
            HttpPut req = new HttpPut(url);

            if (entity != null) {
                req.setEntity(entity);
            } // if

            request = req;
        } else if (method.equals("POST")) {
            HttpPost req = new HttpPost(url);

            if (entity != null) {
                req.setEntity(entity);
            } // if

            request = req;
        } else if (method.equals("GET")) {
            request = new HttpGet(url);
        } else {
            throw new CygnusRuntimeError("HTTP method not supported: " + method);
        } // if else

        if (headers != null) {
            for (Header header : headers) {
                request.setHeader(header);
            } // for
        } // if

        logger.debug("HDFS request: " + request.toString());

        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch

        request.releaseConnection();
        logger.debug("HDFS response: " + response.getStatusLine().toString());
        return response;
    } // doHDFSRequest
    
    // from here on, consider this link:
    // http://stackoverflow.com/questions/21629132/httpclient-set-credentials-for-kerberos-authentication
    private HttpResponse doPrivilegedHDFSRequest(String method, String url, ArrayList<Header> headers,
            StringEntity entity) throws Exception {
        try {
            LoginContext loginContext = new LoginContext("cygnus_krb5_login",
                    new KerberosCallBackHandler(krb5User, krb5Password));
            loginContext.login();
            PrivilegedHDFSRequest req = new PrivilegedHDFSRequest(method, url, headers, entity);
            return (HttpResponse) Subject.doAs(loginContext.getSubject(), req);
        } catch (LoginException e) {
            logger.error(e.getMessage());
            return null;
        } // try catch
    } // doPrivilegedHDFSRequest
    
    /**
     * PrivilegedHDFSRequest class.
     */
    private class PrivilegedHDFSRequest implements PrivilegedAction {
        
        private Logger logger;
        private String method;
        private String url;
        private ArrayList<Header> headers;
        private StringEntity entity;
               
        /**
         * Constructor.
         * @param mrthod
         * @param url
         * @param headers
         * @param entity
         */
        public PrivilegedHDFSRequest(String method, String url, ArrayList<Header> headers, StringEntity entity) {
            this.logger = Logger.getLogger(PrivilegedHDFSRequest.class);
            this.method = method;
            this.url = url;
            this.headers = headers;
            this.entity = entity;
        } // PrivilegedHDFSRequest

        @Override
        public Object run() {
            try {
                Subject current = Subject.getSubject(AccessController.getContext());
                Set<Principal> principals = current.getPrincipals();
                
                for (Principal next : principals) {
                    logger.info("DOAS Principal: " + next.getName());
                } // for

                return doHDFSRequest(method, url, headers, entity);
            } catch (Exception e) {
                logger.error(e.getMessage());
                return null;
            } // try catch
        } // run
        
    } // PrivilegedHDFSRequest
    
    /**
     * KerberosCallBackHandler class.
     */
    private class KerberosCallBackHandler implements CallbackHandler {

        private final String user;
        private final String password;

        public KerberosCallBackHandler(String user, String password) {
            this.user = user;
            this.password = password;
        } // KerberosCallBackHandler

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    NameCallback nc = (NameCallback) callback;
                    nc.setName(user);
                } else if (callback instanceof PasswordCallback) {
                    PasswordCallback pc = (PasswordCallback) callback;
                    pc.setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(callback, "Unknown Callback");
                } // if else if
            } // for
        } // handle
        
    } // KerberosCallBackHandler
    
} // HDFSBackendImpl
