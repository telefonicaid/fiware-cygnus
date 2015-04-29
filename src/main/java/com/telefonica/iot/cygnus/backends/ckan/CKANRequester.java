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

package com.telefonica.iot.cygnus.backends.ckan;

import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author frb
 */
public class CKANRequester {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(CKANRequester.class);
    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseURL;
    
    /**
     * Constructor.
     * @param httpClient Http client
     * @param ckanHost
     * @param ckanPort
     * @param ssl
     * @param apiKey CKAN API key
     */
    public CKANRequester(HttpClient httpClient, String ckanHost, String ckanPort, boolean ssl, String apiKey) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        baseURL = (ssl ? "https://" : "http://") + ckanHost + ":" + ckanPort;
    } // CKANRequester
    
    /**
     * Common method to perform HTTP request using the CKAN API without payload.
     * @param method HTTP method
     * @param urlPath URL path to be added to the base URL
     * @return CKANResponse associated to the request
     * @throws Exception
     */
    public CKANResponse doCKANRequest(String method, String urlPath) throws Exception {
        return doCKANRequest(method, urlPath, "");
    } // doCKANRequest

    /**
     * Common method to perform HTTP request using the CKAN API with payload.
     * @param method HTTP method
     * @param urlPath URL path to be added to the base URL
     * @param payload Request payload
     * @return CKANResponse associated to the request
     * @throws Exception
     */
    public CKANResponse doCKANRequest(String method, String urlPath, String payload)
        throws Exception {
        // build the final URL
        String url = baseURL + urlPath;
        
        HttpRequestBase request = null;
        HttpResponse response = null;
        
        try {
            // do the post
            if (method.equals("GET")) {
                request = new HttpGet(url);
            } else if (method.equals("POST")) {
                HttpPost r = new HttpPost(url);

                // payload (optional)
                if (!payload.equals("")) {
                    LOGGER.debug("request payload: " + payload);
                    r.setEntity(new StringEntity(payload, ContentType.create("application/json")));
                } // if
                
                request = r;
            } else {
                throw new CygnusRuntimeError("HTTP method not supported: " + method);
            } // if else

            // headers
            request.addHeader("Authorization", apiKey);

            // execute the request
            LOGGER.debug("CKAN operation: " + request.toString());
        } catch (Exception e) {
            if (e instanceof CygnusRuntimeError
                    || e instanceof CygnusPersistenceError
                    || e instanceof CygnusBadConfiguration) {
                throw e;
            } else {
                throw new CygnusRuntimeError(e.getMessage());
            } // if else
        } // try catch
        
        try {
            response = httpClient.execute(request);
        } catch (Exception e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch
        
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String res = reader.readLine();
            request.releaseConnection();
            long l = response.getEntity().getContentLength();
            LOGGER.debug("CKAN response (" + l + " bytes): " + response.getStatusLine().toString());

            // get the JSON encapsulated in the response
            LOGGER.debug("response payload: " + res);
            JSONParser j = new JSONParser();
            JSONObject o = (JSONObject) j.parse(res);

            // return result
            return new CKANResponse(o, response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            if (e instanceof CygnusRuntimeError
                    || e instanceof CygnusPersistenceError
                    || e instanceof CygnusBadConfiguration) {
                throw e;
            } else {
                throw new CygnusRuntimeError(e.getMessage());
            } // if else
        } // try catch
    } // doCKANRequest
    
} // CKANRequester
