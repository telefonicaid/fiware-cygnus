/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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

package es.tid.fiware.fiwareconnectors.cygnus.backends.ckan;

import org.json.simple.JSONObject;

/**
 * Helper class encapsulating response code and JSON payload for CKAN response.
 *
 * @author fermin
 */
public class CKANResponse {

    private final JSONObject jsonObject;
    private final int statusCode;

    /**
     * Constructor.
     * @param jsonObject
     * @param statusCode
     */
    public CKANResponse(JSONObject jsonObject, int statusCode) {
        this.jsonObject = jsonObject;
        this.statusCode = statusCode;
    } // CKANResponse

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

} // CKANResponse
