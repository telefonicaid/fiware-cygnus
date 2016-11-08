/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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
    
} // TestUtils
