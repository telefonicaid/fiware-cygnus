/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.utils;

import com.google.gson.Gson;
import com.telefonica.iot.cygnus.containers.NameMappings;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import java.util.HashMap;

/**
 *
 * @author frb
 */
public final class TestUtils {
    
    /**
     * Constructor. It is private since utility clasess should not have a public or default constructor.
     */
    private TestUtils() {
    } // TestUtils
    
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
     * Creates a Json-based ContextElement given the string representation of such Json.
     * @param jsonStr
     * @return The Json-based ContextElement
     * @throws java.lang.Exception
     */
    public static ContextElement createJsonContextElement(String jsonStr) throws Exception {
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, ContextElement.class);
    } // createJsonNotifyContextRequest
    
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
        ContextElement originalCE = createJsonContextElement(originalCEStr);
        ContextElement mappedCE = createJsonContextElement(mappedCEStr);
        return new NGSIEvent(headers, (originalCE == null ? null : originalCE.toString().getBytes()),
                originalCE, mappedCE);
    } // createNGSIEvent
    
    /**
     * Creates a NGSIEvent as NGSINameMappings would create it.
     * @param originalCEStr
     * @param mappedCEStr
     * @return A NGSIEvent as NGSINameMappings would create it
     * @throws java.lang.Exception
     */
    public static NGSIEvent createInterceptedNGSIEvent(String originalCEStr, String mappedCEStr) throws Exception {
        HashMap<String, String> headers = new HashMap<>();
        ContextElement originalCE = createJsonContextElement(originalCEStr);
        ContextElement mappedCE = createJsonContextElement(mappedCEStr);
        return new NGSIEvent(headers, (originalCE == null ? null : originalCE.toString().getBytes()),
                originalCE, mappedCE);
    } // createInterceptedNGSIEvent
    
} // TestUtils
