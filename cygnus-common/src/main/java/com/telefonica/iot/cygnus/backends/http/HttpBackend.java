/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
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
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author frb
 */
public abstract class HttpBackend {

    private final String host;
    private final String port;
    private final boolean ssl;
    private final boolean krb5;
    private final String krb5User;
    private final String krb5Password;
    private final HttpClientFactory httpClientFactory;
    private HttpClient httpClient;
    private long transactionRequestBytes;
    private long transactionResponseBytes;
    private static final CygnusLogger LOGGER = new CygnusLogger(HttpBackend.class);
    private static boolean allHeaders = false;

    /**
     * Constructor.
     * 
     * @param host
     * @param port
     * @param ssl
     * @param krb5
     * @param krb5User
     * @param krb5Password
     * @param krb5LoginConfFile
     * @param krb5ConfFile
     * @param maxConns
     * @param maxConnsPerRoute
     */
    public HttpBackend(String host, String port, boolean ssl, boolean krb5, String krb5User, String krb5Password,
            String krb5LoginConfFile, String krb5ConfFile, int maxConns, int maxConnsPerRoute) {
        this.host = host;
        this.port = port;
        this.ssl = ssl;
        this.krb5 = krb5;
        this.krb5User = krb5User;
        this.krb5Password = krb5Password;

        // create a Http clients factory and an initial connection
        httpClientFactory = new HttpClientFactory(ssl, krb5LoginConfFile, krb5ConfFile, maxConns, maxConnsPerRoute);
        httpClient = httpClientFactory.getHttpClient(ssl, krb5);
    } // HttpBackend

    /**
     * Sets the http client.
     * 
     * @param httpClient
     */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    } // setHttpClient

    /**
     * Sets the all headers.
     * 
     * @param allHeaders
     */
    public static void setAllHeaders(boolean allHeaders) {
        HttpBackend.allHeaders = allHeaders;
    } // setAllHeaders

    /**
     * Does a Http request given a method, a relative URL (the final URL will be
     * composed by using this relative URL and the active Http endpoint), a list
     * of headers and the payload.
     * 
     * @param method
     * @param url
     * @param relative
     * @param headers
     * @param entity
     * @return A Http httpRes
     * @throws CygnusRuntimeError
     * @throws CygnusPersistenceError
     */
    public JsonResponse doRequest(String method, String url, boolean relative, ArrayList<Header> headers,
            StringEntity entity) throws CygnusRuntimeError, CygnusPersistenceError {
        if (entity != null) {
            transactionRequestBytes += entity.getContentLength();
        } // if

        JsonResponse response;

        if (relative) {
            // create the HttpFS URL
            String effectiveURL = (ssl ? "https://" : "http://") + host + ":" + port + url;

            if (krb5) {
                response = doPrivilegedRequest(method, effectiveURL, headers, entity);
            } else {
                response = doRequest(method, effectiveURL, headers, entity);
            } // if else

            return response;
        } else {
            if (krb5) {
                return doPrivilegedRequest(method, url, headers, entity);
            } else {
                return doRequest(method, url, headers, entity);
            } // if else
        } // if else
    } // doRequest

    /**
     * Does a Http request given a method, a relative URL, a list of headers and
     * the payload Protected method due to it's used by the tests.
     * 
     * @param method
     * @param url
     * @param headers
     * @param entity
     * @return The result of the request
     * @throws CygnusRuntimeError
     * @throws CygnusPersistenceError
     */

    protected JsonResponse doRequest(String method, String url, ArrayList<Header> headers, StringEntity entity)
            throws CygnusRuntimeError, CygnusPersistenceError {
        HttpResponse httpRes = null;
        HttpRequestBase request;

        switch (method) {

            case "PUT":
                HttpPut reqPut = new HttpPut(url);

                if (entity != null) {
                    reqPut.setEntity(entity);
                } // if
    
                request = reqPut;
                break;
            case "POST":
                HttpPost reqPost = new HttpPost(url);
    
                if (entity != null) {
                    reqPost.setEntity(entity);
                } // if
    
                request = reqPost;
                break;
            case "GET":
                request = new HttpGet(url);
                break;
            case "DELETE":
                request = new HttpDelete(url);
                break;
            default:
                throw new CygnusRuntimeError("Http '" + method + "' method not supported");
        } // switch

        if (headers != null) {
            for (Header header : headers) {
                request.setHeader(header);
            } // for
        } // if

        LOGGER.debug("Http request: " + request.toString());

        try {
            httpRes = httpClient.execute(request);
        } catch (IOException e) {
            request.releaseConnection();
            throw new CygnusPersistenceError("Request error", "IOException", e.getMessage());
        } // try catch

        JsonResponse response = createJsonResponse(httpRes);
        request.releaseConnection();
        return response;
    } // doRequest

    // from here on, consider this link:
    // http://stackoverflow.com/questions/21629132/httpclient-set-credentials-for-kerberos-authentication
    private JsonResponse doPrivilegedRequest(String method, String url, ArrayList<Header> headers, StringEntity entity)
            throws CygnusRuntimeError {
        try {
            LoginContext loginContext = new LoginContext("cygnus_krb5_login",
                    new KerberosCallbackHandler(krb5User, krb5Password));
            loginContext.login();
            PrivilegedRequest req = new PrivilegedRequest(method, url, headers, entity);
            return createJsonResponse((HttpResponse) Subject.doAs(loginContext.getSubject(), req));
        } catch (LoginException e) {
            throw new CygnusRuntimeError("Privileged request error", "LoginException", e.getMessage());
        } // try catch
    } // doPrivilegedRequest

    /**
     * PrivilegedRequest class.
     */
    private class PrivilegedRequest implements PrivilegedAction<Object> {

        private final Logger logger;
        private final String method;
        private final String url;
        private final ArrayList<Header> headers;
        private final StringEntity entity;

        /**
         * Constructor.
         * 
         * @param mrthod
         * @param url
         * @param headers
         * @param entity
         */
        PrivilegedRequest(String method, String url, ArrayList<Header> headers, StringEntity entity) {
            this.logger = Logger.getLogger(PrivilegedRequest.class);
            this.method = method;
            this.url = url;
            this.headers = headers;
            this.entity = entity;
        } // PrivilegedRequest

        @Override
        public Object run() {
            Subject current = Subject.getSubject(AccessController.getContext());
            Set<Principal> principals = current.getPrincipals();

            for (Principal next : principals) {
                logger.info("DOAS Principal: " + next.getName());
            } // for

            try {
                return doRequest(method, url, headers, entity);
            } catch (CygnusRuntimeError | CygnusPersistenceError e) {
                logger.error(e.getMessage());
                return null;
            } // try catch
        } // run

    } // PrivilegedRequest

    /**
     * Creates a JsonResponse object based on the given HttpResponse. It is
     * protected for testing purposes.
     * 
     * @param httpRes
     * @return A JsonResponse object
     * @throws CygnusRuntimeError
     */
    @SuppressWarnings("unchecked")
    protected JsonResponse createJsonResponse(HttpResponse httpRes) throws CygnusRuntimeError {
        if (httpRes == null) {
            return null;
        } // if

        // get the location header
        Header locationHeader = null;
        Header[] headers = httpRes.getHeaders("Location");
        // return all headers
        Header[] headersAll = null;
        if (allHeaders) {
            headersAll = httpRes.getAllHeaders();
        }

        if (headers.length > 0) {
            locationHeader = headers[0];
        } // if

        if (httpRes.getHeaders("Content-Type").length == 0) {
            return new JsonResponse(null, httpRes.getStatusLine().getStatusCode(),
                    httpRes.getStatusLine().getReasonPhrase(), locationHeader, headersAll);
        } // if

        if (!httpRes.getHeaders("Content-Type")[0].getValue().contains("application/json")) {
            return new JsonResponse(null, httpRes.getStatusLine().getStatusCode(),
                    httpRes.getStatusLine().getReasonPhrase(), locationHeader, headersAll);
        } // if

        LOGGER.debug("Http response status line: " + httpRes.getStatusLine().toString());

        // parse the httpRes payload
        JSONObject jsonPayload = null;
        HttpEntity entity = httpRes.getEntity();

        if (entity != null) {
            BufferedReader reader;

            try {
                reader = new BufferedReader(new InputStreamReader(httpRes.getEntity().getContent()));
            } catch (IOException e) {
                throw new CygnusRuntimeError("Response handling error", "IOException", e.getMessage());
            } catch (IllegalStateException e) {
                throw new CygnusRuntimeError("Response handling error", "IllegalStateException", e.getMessage());
            } // try catch

            String res = "";
            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    res += line;
                } // while
            } catch (IOException e) {
                throw new CygnusRuntimeError("Response handling error", "IOException", e.getMessage());
            } // try catch

            transactionResponseBytes += res.length();

            try {
                reader.close();
            } catch (IOException e) {
                throw new CygnusRuntimeError("Response handling error", "IOException", e.getMessage());
            } // try catch

            LOGGER.debug("Http response payload: " + res);

            if (!res.isEmpty()) {
                JSONParser jsonParser = new JSONParser();

                if (res.startsWith("[")) {
                    Object object;

                    try {
                        object = jsonParser.parse(res);
                    } catch (ParseException e) {
                        throw new CygnusRuntimeError("Response handling error", "ParseException", e.getMessage());
                    } // try catch

                    jsonPayload = new JSONObject();
                    jsonPayload.put("result", (JSONArray) object);
                } else {
                    try {
                        jsonPayload = (JSONObject) jsonParser.parse(res);
                    } catch (ParseException e) {
                        throw new CygnusRuntimeError("Response handling error", "ParseException", e.getMessage());
                    } // try catch
                } // if else
            } // if
        } // if

        // return the result
        return new JsonResponse(jsonPayload, httpRes.getStatusLine().getStatusCode(),
                httpRes.getStatusLine().getReasonPhrase(), locationHeader, headersAll);
    } // createJsonResponse

    /**
     * Starts a transaction. Basically, this means the byte counters are
     * reseted.
     */
    public void startTransaction() {
        transactionRequestBytes = 0;
        transactionResponseBytes = 0;
    } // startTransaction

    /**
     * Finishes a transaction. Basically, this means the the bytes counters are
     * retrieved.
     * 
     * @return
     */
    public ImmutablePair<Long, Long> finishTransaction() {
        return new ImmutablePair<Long, Long>(transactionRequestBytes, transactionResponseBytes);
    } // finishTransaction

} // HttpBackend
