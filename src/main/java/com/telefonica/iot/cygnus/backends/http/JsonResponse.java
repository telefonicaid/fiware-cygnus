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

import org.apache.http.Header;
import org.json.simple.JSONObject;

/**
 * Helper class encapsulating response code and JSON payload for CKAN response.
 *
 * @author fermin
 */
public class JsonResponse {

    private final JSONObject jsonObject;
    private final int statusCode;
    private final String reasonPhrase;
    private final Header locationHeader;

    /**
     * Constructor.
     * @param jsonObject
     * @param statusCode
     * @param reasonPhrase
     * @param locationHeader
     */
    public JsonResponse(JSONObject jsonObject, int statusCode, String reasonPhrase, Header locationHeader) {
        this.jsonObject = jsonObject;
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.locationHeader = locationHeader;
    } // JsonResponse

    /**
     * Gets the Json object.
     * @return jsonObject
     */
    public JSONObject getJsonObject() {
        return jsonObject;
    } // getJsonObject

    /**
     * Gets the status code.
     * @return statusCode
     */
    public int getStatusCode() {
        return statusCode;
    } // getStatusCode
    
    /**
     * Gets the reason phrase.
     * @return reasonPhrase
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    } // getReasonPhrase
    
    /**
     * Gets the location header.
     * @return locationHeader
     */
    public Header getLocationHeader() {
        return locationHeader;
    } // getLocationHeader

} // JsonResponse
