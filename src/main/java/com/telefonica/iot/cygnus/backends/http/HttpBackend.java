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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author frb
 */
public abstract class HttpBackend {
    
    private final LinkedList<String> hosts;
    private final String port;
    private final boolean ssl;
    private final boolean krb5;
    private final String krb5User;
    private final String krb5Password;
    private final HttpClientFactory httpClientFactory;
    private HttpClient httpClient;
    private static final CygnusLogger LOGGER = new CygnusLogger(HttpBackend.class);
    
    /**
     * Constructor.
     * @param hosts
     * @param port
     * @param ssl
     * @param krb5
     * @param krb5User
     * @param krb5Password
     * @param krb5LoginConfFile
     * @param krb5ConfFile
     */
    public HttpBackend(String[] hosts, String port, boolean ssl, boolean krb5, String krb5User, String krb5Password,
            String krb5LoginConfFile, String krb5ConfFile) {
        this.hosts = new LinkedList(Arrays.asList(hosts));
        this.port = port;
        this.ssl = ssl;
        this.krb5 = krb5;
        this.krb5User = krb5User;
        this.krb5Password = krb5Password;
        
        // create a Http clients factory and an initial connection
        httpClientFactory = new HttpClientFactory(ssl, krb5LoginConfFile, krb5ConfFile);
        httpClient = httpClientFactory.getHttpClient(ssl, krb5);
    } // HttpBackend
    
    /**
     * Sets the http client.
     * @param httpClient
     */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    } // setHttpClient
    
    /**
     * Does a Http request given a method, a relative URL (the final URL will be composed by using this relative URL
     * and the active Http endpoint), a list of headers and the payload.
     * @param method
     * @param url
     * @param relative
     * @param headers
     * @param entity
     * @return A Http httpRes
     * @throws Exception
     */
    public JsonResponse doRequest(String method, String url, boolean relative, ArrayList<Header> headers,
            StringEntity entity) throws Exception {
        // default httpRes
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse("{}");
        JsonResponse response = new JsonResponse(null, 503, "Service unavailable", null);
        
        if (relative) {
            // iterate on the hosts
            for (String host : hosts) {
                // create the HttpFS URL
                String effectiveURL = (ssl ? "https://" : "http://") + host + ":" + port + url;
                
                try {
                    if (krb5) {
                        response = doPrivilegedRequest(method, effectiveURL, headers, entity);
                    } else {
                        response = doRequest(method, effectiveURL, headers, entity);
                    } // if else
                } catch (Exception e) {
                    LOGGER.debug("There was a problem when performing the request (details=" + e.getMessage() + "). "
                            + "Most probably the used Http endpoint is not active, trying another one (host="
                            + host + ")");
                    continue;
                } // try catch // try catch
                
                int status = response.getStatusCode();

                if (status != 200 && status != 307 && status != 404 && status != 201) {
                    LOGGER.debug("The used Http endpoint is not active, trying another one (host=" + host + ")");
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
        
    private JsonResponse doRequest(String method, String url, ArrayList<Header> headers, StringEntity entity)
        throws Exception {
        HttpResponse httpRes = null;
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

        LOGGER.debug("Http request: " + request.toString());

        try {
            httpRes = httpClient.execute(request);
        } catch (IOException e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch

        JsonResponse response = createJsonResponse(httpRes);
        request.releaseConnection();
        return response;
    } // doRequest
    
    // from here on, consider this link:
    // http://stackoverflow.com/questions/21629132/httpclient-set-credentials-for-kerberos-authentication
    private JsonResponse doPrivilegedRequest(String method, String url, ArrayList<Header> headers,
            StringEntity entity) throws Exception {
        try {
            LoginContext loginContext = new LoginContext("cygnus_krb5_login",
                    new KerberosCallbackHandler(krb5User, krb5Password));
            loginContext.login();
            PrivilegedRequest req = new PrivilegedRequest(method, url, headers, entity);
            return createJsonResponse((HttpResponse) Subject.doAs(loginContext.getSubject(), req));
        } catch (LoginException e) {
            LOGGER.error(e.getMessage());
            return null;
        } // try catch // try catch
    } // doPrivilegedRequest
    
    /**
     * PrivilegedRequest class.
     */
    private class PrivilegedRequest implements PrivilegedAction {
        
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
        public PrivilegedRequest(String method, String url, ArrayList<Header> headers, StringEntity entity) {
            this.logger = Logger.getLogger(PrivilegedRequest.class);
            this.method = method;
            this.url = url;
            this.headers = headers;
            this.entity = entity;
        } // PrivilegedRequest

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
        
    } // PrivilegedRequest
    
    private JsonResponse createJsonResponse(HttpResponse httpRes) throws Exception {
        try {
            LOGGER.debug("Http response: " + httpRes.getStatusLine().toString());
            
            // parse the httpRes payload
            JSONObject jsonPayload = null;
            HttpEntity entity = httpRes.getEntity();
            
            if (entity != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpRes.getEntity().getContent()));
                String res = reader.readLine();
                LOGGER.debug("response payload: " + res);
                
                if (res != null) {
                    JSONParser jsonParser = new JSONParser();
                    jsonPayload = (JSONObject) jsonParser.parse(res);
                } // if
            } // if

            // get the location header
            Header locationHeader = null;
            Header[] headers = httpRes.getHeaders("Location");
            
            if (headers.length > 0) {
                locationHeader = headers[0];
            } // if
            
            // return the result
            return new JsonResponse(jsonPayload, httpRes.getStatusLine().getStatusCode(),
                    httpRes.getStatusLine().getReasonPhrase(), locationHeader);
        } catch (IOException e) {
            throw new CygnusRuntimeError(e.getMessage());
        } catch (IllegalStateException e) {
            throw new CygnusRuntimeError(e.getMessage());
        } // try catch
    } // createJsonResponse

} // HttpBackend
