/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.arcgis.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;

/**
 * @author dmartinez
 *
 */
public class HttpResponse {
    private int responseCode;
    private String responseMessage;
    private String body;
    private Map<String, List<String>> headers;
    private ArcgisException responseError;

    /**
     * Default Constructor.
     */
    public HttpResponse() {
        headers = new HashMap<String, List<String>>();
        responseError = null;
    }

    /**
     * Resposecode.
     */
    public HttpResponse(int responseCode) {
        this();
        this.responseCode = responseCode;
    }

    /**
     * Resposecode.
     */
    public HttpResponse(int responseCode, String body) {
        this(responseCode);
        this.body = body;
    }

    /**
     * @return the responseCode
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * @param responseCode
     *            the responseCode to set
     */
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body
     *            the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * @return the headers
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * @param headers
     *            the headers to set
     */
    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    /**
     * @return the responseMessage
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * @param responseMessage
     *            the responseMessage to set
     */
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * Full toString conversion.
     */
    public String toFullString() {
        String json = "{ \"responseCode\" : " + responseCode + ", \"responseMessage\": \""
                + responseMessage + ", \"body\": \"" + body + "\"}";
        return json;
    }

    /**
     * is it an error response?
     * 
     * @return
     */
    public boolean hasError() {
        if (responseCode >= 300) {
            return true;
        } else {
            return this.responseError != null;
        }
    }

    /**
     * is it a success response?
     * 
     * @return
     */
    public boolean isSuccessful() {

        if (responseCode >= 300) {
            return false;
        } else {
            return this.responseError == null;
        }
    }

    /**
     * Resumed toString conversion.
     */
    public String toString() {
        String json = "{ \"responseCode\" : " + responseCode + ", \"responseMessage\": \""
                + responseMessage + "\"}";
        return json;
    }

    /**
     * Set error data from exception.
     * @param exception
     */
    public void setError(ArcgisException exception) {
        this.responseError = exception;
    }

    /**
     * Returns true if responseCode is an error code.
     * @return
     */
    public boolean isResponseCodeError() {
        return responseCode >= 300 || responseCode < 200;
    }

    /**
     * Retruns error code.
     * @return
     */
    public int getErrorCode() {
        int result = -1;
        if (responseError != null) {
            result = responseError.getCode();
        } else if (isResponseCodeError()) {
            result = responseCode;
        }
        return result;
    }

    /**
     * Returns error message.
     * @return
     */
    public String getErrorMessage() {
        String result = "";
        if (responseError != null) {
            result = responseError.getMessage();
        } else if (isResponseCodeError()) {
            result = responseMessage;
        }
        return result;
    }
}
