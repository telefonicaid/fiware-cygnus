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
package com.telefonica.iot.cygnus.utils;

import java.util.HashMap;

import org.apache.flume.Context;

import com.google.gson.Gson;
import com.telefonica.iot.cygnus.containers.NameMappings;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;

/**
 *
 * @author frb
 */
public final class NGSIUtilsForTests {
    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private NGSIUtilsForTests() {
    } // NGSIUtilsForTests
    
    /**
     * Creates a Flume context for Mongo/STH sinks.
     * @param collectionPrefix
     * @param dbPrefix
     * @param dataModel
     * @param enableEncoding
     * @return A Flume context for Mongo/STH sinks.
     */
    public static Context createContextForMongoSTH(String collectionPrefix, String dbPrefix, String dataModel,
            String enableEncoding) {
        Context context = new Context();
        context.put("attr_persistence", "row");
        context.put("batch_size", "100");
        context.put("batch_timeout", "30");
        context.put("batch_ttl", "10");
        context.put("collection_prefix", collectionPrefix);
        context.put("collection_size", "0");
        context.put("data_expiration", "0");
        context.put("data_model", dataModel);
        context.put("db_prefix", dbPrefix);
        context.put("enable_encoding", enableEncoding);
        context.put("enable_grouping", "false");
        context.put("enable_lowercase", "false");
        context.put("max_documents", "0");
        context.put("mongo_hosts", "localhost:27017");
        context.put("mongo_password", "");
        context.put("mongo_username", "");
        return context;
    } // createContextForMongoSTH
    
    
    /**
     * Creates a Flume context for Orion sinks.
     * 
     * @param orionHost
     * @param orionPort
     * @param orionHostKey
     * @param orionPortKey
     * @param orionUsername
     * @param orionPassword
     * @param orionFiware
     * @param orionFiwarePath
     * @return
     */
    public static Context createContextForOrion(String orionHost, String orionPort,
            String orionHostKey, String orionPortKey,
            String orionUsername, String orionPassword, String orionFiware, String orionFiwarePath) {
        Context context = new Context();
        context.put("orion_host", orionHost);
        context.put("orion_port", orionPort);
        context.put("keystone_host", orionHostKey);
        context.put("keystone_port", orionPortKey);
        context.put("orion_username", orionUsername);
        context.put("orion_password", orionPassword);
        context.put("orion_fiware", orionFiware);
        context.put("orion_fiware_path", orionFiwarePath);
        return context;
    } // createContextForOrion

    /**
     * Creates a Flume context for Arcgis sinks.
     * 
     * @param url
     * @param username
     * @param password
     * @return
     */
    public static Context createContextForArcGis(String url, String username,
            String password) {
        Context context = new Context();
        context.put("arcgis_url", url);
        context.put("arcgis_username", username);
        context.put("arcgis_password", password);
        return context;
    } // createContextForArcGis
	
    /**
     * Creates a Json-based NotifyContextRequest given the string representation of such Json.
     * @param jsonStr
     * @return The Json-based NotifyContextRequest
     * @throws java.lang.Exception
     */
    public static NotifyContextRequest createJsonNotifyContextRequest(String jsonStr) throws Exception {
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, NotifyContextRequest.class);
    } // createJsonNotifyContextRequest

    /**
     * Creates a Json-based StatusCode given the string representation of such Json.
     * @param jsonStr
     * @return The Json-based StatusCode
     * @throws java.lang.Exception
     */
    public static NotifyContextRequest.StatusCode createJsonStatusCode(String jsonStr) throws Exception {
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, NotifyContextRequest.StatusCode.class);
    } // createJsonStatusCode

    /**
     * Creates a NGSIEvent as NGSIRestHandler would create it (not intercepted).
     * @param originalCEStr
     * @param mappedCEStr
     * @param service
     * @param servicePath
     * @param correlatorID
     * @return A NGSIEvent as NGSIRestHandler would create it (not intercepted)
     * @throws java.lang.Exception
     */
    public static NGSIEvent createNGSIEvent(String originalCEStr, String mappedCEStr, String service,
            String servicePath, String correlatorID) throws Exception {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, service);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, servicePath);
        headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorID);
        headers.put(NGSIConstants.FLUME_HEADER_TRANSACTION_ID, correlatorID);
        NotifyContextRequest.ContextElement originalCE = createJsonContextElement(originalCEStr);
        NotifyContextRequest.ContextElement mappedCE = createJsonContextElement(mappedCEStr);
        return new NGSIEvent(headers, (originalCEStr + CommonConstants.CONCATENATOR).getBytes(), originalCE, mappedCE);
    } // createNGSIEvent

    /**
     * Creates a Json-based NameMappings given the string representation of such Json.
     * @param jsonStr
     * @return The Json-based NameMappings
     * @throws java.lang.Exception
     */
    public static NameMappings createJsonNameMappings(String jsonStr) throws Exception {
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, NameMappings.class);
    } // createJsonNameMappings

    /**
     * Creates a NGSIEvent as NGSINameMappings would create it.
     * @param originalCEStr
     * @param mappedCEStr
     * @return A NGSIEvent as NGSINameMappings would create it
     * @throws java.lang.Exception
     */
    public static NGSIEvent createInterceptedNGSIEvent(String originalCEStr, String mappedCEStr) throws Exception {
        HashMap<String, String> headers = new HashMap<>();
        NotifyContextRequest.ContextElement originalCE = createJsonContextElement(originalCEStr);
        NotifyContextRequest.ContextElement mappedCE = createJsonContextElement(mappedCEStr);
        return new NGSIEvent(headers, originalCE == null ? null : originalCE.toString().getBytes(), originalCE,
                mappedCE);
    } // createInterceptedNGSIEvent

    
    /**
     * Creates a Json-based ContextElement given the string representation of such Json.
     * @param jsonStr
     * @return The Json-based ContextElement
     * @throws java.lang.Exception
     */
    public static NotifyContextRequest.ContextElement createJsonContextElement(String jsonStr) throws Exception {
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, NotifyContextRequest.ContextElement.class);
    } // createJsonContextElement
    
} // NGSIUtilsForTests
