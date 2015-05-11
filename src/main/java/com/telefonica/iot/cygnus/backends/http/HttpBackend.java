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
package com.telefonica.iot.cygnus.backends.http;

import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.io.IOException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author frb
 */
public abstract class HttpBackend {
    
    private final LinkedList<String> hosts;
    private final String port;
    private final boolean krb5;
    private final String krb5User;
    private final String krb5Password;
    private final String krb5LoginConfFile;
    private final String krb5ConfFile;
    private final HttpClientFactory httpClientFactory;
    private HttpClient httpClient;
    private static final CygnusLogger LOGGER = new CygnusLogger(HttpBackend.class);
    
    /**
     * Constructor.
     * @param hosts
     * @param port
     * @param krb5
     * @param krb5User
     * @param krb5Password
     * @param krb5LoginConfFile
     * @param krb5ConfFile
     */
    public HttpBackend(String[] hosts, String port, boolean krb5, String krb5User, String krb5Password,
            String krb5LoginConfFile, String krb5ConfFile) {
        this.hosts = new LinkedList(Arrays.asList(hosts));
        this.port = port;
        this.krb5 = krb5;
        this.krb5User = krb5User;
        this.krb5Password = krb5Password;
        this.krb5LoginConfFile = krb5LoginConfFile;
        this.krb5ConfFile = krb5ConfFile;
        
        // create a Http clients factory (no SSL) and an initial connection (no SSL)
        httpClientFactory = new HttpClientFactory(false, krb5LoginConfFile, krb5ConfFile);
        httpClient = httpClientFactory.getHttpClient(false, krb5);
    } // HttpBackend
    
    /**
     * Gets the list of hosts.
     * @return The list of hosts
     */
    public LinkedList<String> getHosts() {
        return hosts;
    } // getHosts
    
    /**
     * Gets the port.
     * @return The port
     */
    public String getPort() {
        return port;
    } // getPort
    
    /**
     * Sets the http client.
     * @param httpClient
     */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    } // setHttpClient
    
    /**
     * Does a HDFS request given a HTTP client, a method and a relative URL (the final URL will be composed by using
     * this relative URL and the active HDFS endpoint).
     * @param method
     * @param url
     * @param relative
     * @param headers
     * @param entity
     * @return
     * @throws Exception
     */
    public HttpResponse doRequest(String method, String url, boolean relative, ArrayList<Header> headers,
            StringEntity entity) throws Exception {
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_SERVICE_UNAVAILABLE,
                "Service unavailable");
        
        if (relative) {
            // iterate on the hosts
            for (String host : hosts) {
                // create the HttpFS URL
                String effectiveURL = "http://" + host + ":" + port + url;
                
                try {
                    if (krb5) {
                        response = doPrivilegedRequest(method, effectiveURL, headers, entity);
                    } else {
                        response = doRequest(method, effectiveURL, headers, entity);
                    } // if else
                } catch (Exception e) {
                    LOGGER.debug("The used HDFS endpoint is not active, trying another one (host=" + host + ")");
                    continue;
                } // try catch // try catch
                
                int status = response.getStatusLine().getStatusCode();

                if (status != 200 && status != 307 && status != 404 && status != 201) {
                    LOGGER.debug("The used HDFS endpoint is not active, trying another one (host=" + host + ")");
                    continue;
                } // if
                
                // place the current host in the first place (if not yet placed), since it is currently working
                if (!hosts.getFirst().equals(host)) {
                    hosts.remove(host);
                    hosts.add(0, host);
                    LOGGER.debug("Placing the host in the first place of the list (host=" + host + ")");
                } // if
                
                break;
            } // for
        } else {
            if (krb5) {
                response = doPrivilegedRequest(method, url, headers, entity);
            } else {
                response = doRequest(method, url, headers, entity);
            } // if else
        } // if else
        
        return response;
    } // doRequest
        
    private HttpResponse doRequest(String method, String url, ArrayList<Header> headers, StringEntity entity)
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

        LOGGER.debug("HDFS request: " + request.toString());

        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch

        request.releaseConnection();
        LOGGER.debug("HDFS response: " + response.getStatusLine().toString());
        return response;
    } // doRequest
    
    // from here on, consider this link:
    // http://stackoverflow.com/questions/21629132/httpclient-set-credentials-for-kerberos-authentication
    private HttpResponse doPrivilegedRequest(String method, String url, ArrayList<Header> headers,
            StringEntity entity) throws Exception {
        try {
            LoginContext loginContext = new LoginContext("cygnus_krb5_login",
                    new KerberosCallbackHandler(krb5User, krb5Password));
            loginContext.login();
            PrivilegedHDFSRequest req = new PrivilegedHDFSRequest(method, url, headers, entity);
            return (HttpResponse) Subject.doAs(loginContext.getSubject(), req);
        } catch (LoginException e) {
            LOGGER.error(e.getMessage());
            return null;
        } // try catch // try catch
    } // doPrivilegedRequest
    
    /**
     * PrivilegedHDFSRequest class.
     */
    private class PrivilegedHDFSRequest implements PrivilegedAction {
        
        private final Logger logger;
        private final String method;
        private final String url;
        private final ArrayList<Header> headers;
        private final StringEntity entity;
               
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

                return doRequest(method, url, headers, entity);
            } catch (Exception e) {
                logger.error(e.getMessage());
                return null;
            } // try catch
        } // run
        
    } // PrivilegedHDFSRequest
    
} // HttpBackend
