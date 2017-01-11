/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.orion;

import com.telefonica.iot.cygnus.backends.http.HttpBackend;
import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.util.ArrayList;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

/**
 *
 * @author pcoello25
 */
public class OrionBackendImpl extends HttpBackend implements OrionBackend {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(OrionBackendImpl.class);
    
    /**
     * Constructor.
     * @param orionHost
     * @param orionPort
     * @param ssl
     * @param maxConns
     * @param maxConnsPerRoute
     */
    public OrionBackendImpl(String orionHost, String orionPort, boolean ssl, int maxConns, int maxConnsPerRoute) {
        super(orionHost, orionPort, ssl, false, null, null, null, null, maxConns, maxConnsPerRoute);
    } // StatsBackendImpl

    @Override
    public JsonResponse subscribeContextV1(String subscription, String token, String fiwareService, 
            String fiwareServicePath) throws Exception {
        
        // create the relative URL
        String relativeURL = "/v1/subscribeContext";
        
        // create the http header
        ArrayList<Header> headers = getHeaders(token, fiwareService, fiwareServicePath);
        
        // create an entity for request
        StringEntity entity = new StringEntity(subscription);
        
        // do the request
        JsonResponse response = doRequest("POST", relativeURL, true, headers, entity);
        
        // check status code from response
        return response;
    } // subscribeContext
    
    @Override
    public JsonResponse subscribeContextV2(String subscription, String token, String fiwareService, 
            String fiwareServicePath) throws Exception {
        
        // create the relative URL
        String relativeURL = "/v2/subscriptions";
        
        // create the http header
        ArrayList<Header> headers = getHeaders(token, fiwareService, fiwareServicePath);
        
        // create an entity for request
        StringEntity entity = new StringEntity(subscription);
        
        // do the request
        JsonResponse response = doRequest("POST", relativeURL, true, headers, entity);
        
        // check status code from response
        return response;
    } // subscribeContext
    
    @Override
    public JsonResponse deleteSubscriptionV1(String subscriptionId, String token, String fiwareService, 
            String fiwareServicePath) throws Exception { 
        
        // create the http header
        ArrayList<Header> headers = getHeaders(token, fiwareService, fiwareServicePath);
                
        String relativeURL = "/v1/unsubscribeContext";
        String subscriptionStr = "{\n" 
                + "\"subscriptionId\": \"" + subscriptionId + "\"\n"
                + "}";
        StringEntity subscriptionEnt = new StringEntity(subscriptionStr);
        
        // do the request
        JsonResponse response = doRequest("POST", relativeURL, true, headers, subscriptionEnt);
        
        return response;   
    } // deleteSubscriptionV1
    
    @Override
    public JsonResponse getSubscriptionsByIdV2(String token, String subscriptionId, String fiwareService, 
            String fiwareServicePath) throws Exception {
        
        // create the http header
        ArrayList<Header> headers = getHeaders(token, fiwareService, fiwareServicePath);
        
        String relativeURL = "/v2/subscriptions/" + subscriptionId;
        JsonResponse response = doRequest("GET", relativeURL, true, headers, null);
        
        return response;
    } // getSubscriptionsV2byId
    
    @Override
    public JsonResponse getSubscriptionsV2(String token, String subscriptionId, String fiwareService, 
            String fiwareServicePath) throws Exception {
        
    // create the http header
        ArrayList<Header> headers = getHeaders(token, fiwareService, fiwareServicePath);
        
        String relativeURL = "/v2/subscriptions";
        JsonResponse response = doRequest("GET", relativeURL, true, headers, null);
        
        return response;
    } // getSubscriptionsV2
    
    @Override
    public JsonResponse deleteSubscriptionV2(String subscriptionId, String token, String fiwareService, 
            String fiwareServicePath) throws Exception {
        // create the http header
        ArrayList<Header> headers = getHeaders(token, fiwareService, fiwareServicePath);
        
        String relativeURL = "/v2/subscriptions/" + subscriptionId;
        JsonResponse response = doRequest("DELETE", relativeURL, true, headers, null);
        
        return response;
        
    } // deleteSubscriptionV2
    
    private ArrayList<Header> getHeaders (String token, String fiwareService, String fiwareServicePath) {
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Content-type", "application/json"));
        headers.add(new BasicHeader("Accept", "application/json"));
        headers.add(new BasicHeader("Fiware-Service", fiwareService));
        headers.add(new BasicHeader("Fiware-ServicePath", fiwareServicePath));
        
        if (token != null) {
            headers.add(new BasicHeader("X-Auth-token", token));
        } // if
        
        return headers;
    }
    
    // TBD: https://github.com/telefonicaid/fiware-cygnus/issues/304
    /**
    @Override
    public void updateContext(String entityId, String entityType, ArrayList<OrionStats> allAttrStats)
        throws Exception {
        // create the relative URL
        String relativeURL = "/v1/updateContext";
        
        // create the http headers
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Content-Type", "application/json"));
        headers.add(new BasicHeader("Accept", "application/json"));

        // create the Json-based payload
        String jsonStr = ""
                + "{"
                + "   \"contextElements\": ["
                + "      {"
                + "         \"type\": \"" + entityType + "\","
                + "         \"isPattern\": \"false\","
                + "         \"id\": \"" + entityId + "\","
                + "         \"attributes\": [";
        
        if (allAttrStats != null) {
            for (int i = 0; i < allAttrStats.size(); i++) {
                OrionStats attrStats = allAttrStats.get(i);
                jsonStr += ""
                        + "             {"
                        + "                \"name\": \"" + attrStats.getAttrName() + "\","
                        + "                \"type\": \"" + attrStats.getAttrType() + "\","
                        + "                \"metadatas\":";
                jsonStr += attrStats.toNGSIString();
                jsonStr += ""
                        + "             }";

                if (i != (allAttrStats.size() - 1)) {
                    jsonStr += ",";
                } // if
            } // for
        } // if
        
        jsonStr += ""
                + "         ]"
                + "      }"
                + "   ],"
                + "   \"updateAction\": \"UPDATE\""
                + "}";
        StringEntity entity = new StringEntity(jsonStr);
        
        // do the request
        JsonResponse response = doRequest("POST", relativeURL, true, headers, entity);

        // check the status
        if (response.getStatusCode() != 200) {
            throw new CygnusPersistenceError("The context could not be updated. HttpFS response: "
                    + response.getStatusCode() + " " + response.getReasonPhrase());
        } // if
    } // updateContext
    **/
} // StatsBackendImpl
