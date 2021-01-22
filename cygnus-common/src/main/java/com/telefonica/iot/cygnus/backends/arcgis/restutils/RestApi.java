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

package com.telefonica.iot.cygnus.backends.arcgis.restutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.http.HttpMethod;
import com.telefonica.iot.cygnus.backends.arcgis.http.HttpResponse;
import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 * @author dmartinez
 *
 */
public class RestApi  {
    private static final CygnusLogger LOGGER = new CygnusLogger(RestApi.class);
    
    // Response tags
    private static final String DETAILS_RESPONSE_TAG = "details";
    private static final String DESCRIPTION_RESPONSE_TAG = "description";
    private static final String MESSAGE_RESPONSE_TAG = "message";
    private static final String CODE_RESPONSE_TAG = "code";
    private static final String DELETE_RESULTS_RESPONSE_TAG = "deleteResults";
    private static final String UPDATE_RESULTS_RESPONSE_TAG = "updateResults";
    private static final String ADD_RESULTS_RESPONSE_TAG = "addResults";
    private static final String ERROR_RESPONSE_TAG = "error";
    
    private static final String LOCATION_HEADER = "Location";
    private static final String UTF_8_ENCODING = "UTF-8";

    /**
     * Añade los parámetros indicados a la url de entrada.
     * 
     * @param url
     *            Url base tipo http://servidor.com/carpeta/
     * @param params
     *            Map de parámetros para añadir a la url
     * @return Url con parámetros en linea
     */
    protected static String fullUrl(String url, Map<String, String> params) throws Exception {
        String result = url;
        String separator = "?";
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                try {
                    result += separator + param.getKey() + "="
                            + URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    result += separator + param.getKey() + "=" + param.getValue();
                } catch (Exception e) {
                    LOGGER.error("Error parsing URL param " + param.getKey() + " value "
                            + param.getValue());
                    throw e;
                }
                separator = "&";
            }
        }

        return result;
    }

    /**
     * Gets parameters to send with POST method.
     * 
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    protected static String getPostParameters(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            String key = entry.getKey();
            String value = entry.getValue();

            // Try to encode
            try {
                key = URLEncoder.encode(key, UTF_8_ENCODING);
                value = URLEncoder.encode(value, UTF_8_ENCODING);
            } catch (UnsupportedEncodingException e) {
            }

            result.append(key);
            result.append("=");
            result.append(value);
        }

        return result.toString();
    }

    /**
     * Comprueba el valor de los parametros pasados.
     * 
     * @param params
     * @return
     */
    protected static Map<String, String> checkParameters(Map<String, String> params) {
        if (params == null) {
            return new LinkedHashMap<>();
        } else {
            return params;
        }
    }

    /**
     * Petición genéria HTTP.
     * 
     * @param urlToRead
     * @param params
     * @param httpMethod
     * @param body
     * @return
     * @throws Exception
     */
    public static HttpResponse requestHTTP(String urlToRead, Map<String, String> params,
            HttpMethod httpMethod, String body) {

        HttpResponse httpResponse = new HttpResponse();
        StringBuilder result = new StringBuilder("");

        params = checkParameters(params);

        String errMsg = "";

        HttpURLConnection conn = null;
        BufferedReader rd = null;

        try {
            String strUrl = fullUrl(urlToRead, params);
            LOGGER.debug("requesting HTTP: " + strUrl);

            URL url = new java.net.URL(strUrl);
            conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod(httpMethod.toString());
 
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            conn.setDefaultHostnameVerifier(allHostsValid);

            // Si es necesario ponemos el body
            if (httpMethod != HttpMethod.GET) {
                conn.setDoOutput(true);
                OutputStream bodyStream = conn.getOutputStream();
                OutputStreamWriter bodyWriter = new OutputStreamWriter(bodyStream, UTF_8_ENCODING);

                bodyWriter.write(body);
                bodyWriter.flush();
                bodyWriter.close();
                bodyStream.close();

            }

            // Comprobamos si hay una redirección
            String location = conn.getHeaderField(LOCATION_HEADER);
            if (location != null) {
                conn = (HttpURLConnection) new URL(location).openConnection();
            }

            conn.connect();

            int responseCode = conn.getResponseCode();
            httpResponse.setResponseCode(responseCode);
            httpResponse.setResponseMessage(conn.getResponseMessage());

            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),
                    java.nio.charset.StandardCharsets.UTF_8.name()));
            String line;

            if (responseCode == 200) {
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                httpResponse.setBody(result.toString());
            } else {

                httpResponse.setBody(errMsg);

                throw new IOException(errMsg);
            }

        } catch (UnknownHostException e) {
            httpResponse.setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
            httpResponse.setResponseMessage(e.getClass() + "\n" + e.getMessage());
        } catch (IllegalArgumentException e) {
            httpResponse.setResponseMessage("Check url, it may have 'http:/' instead of 'http://' "
                    + e.getClass() + "\n" + e.getMessage());
        } catch (Exception e) {
            httpResponse.setResponseMessage(e.getClass() + "\n" + e.getMessage());
        } finally {
            LOGGER.debug("Disposing connection objects");
            if (rd != null) {
                try {
                    rd.close();
                } catch (IOException e) {
                    LOGGER.error("Error closing input stream.");
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }

        return httpResponse;
    }

    /**
     * Http GET request.
     * 
     * @param urlToRead
     * @param params
     * @return
     */
    public static HttpResponse httpGet(String urlToRead, Map<String, String> params) {
        return requestHTTP(urlToRead, params, HttpMethod.GET, "");
    }

    /**
     * Http POST request.
     * 
     * @param urlToRead
     * @param params
     * @param body
     * @return
     */
    public static HttpResponse httpPost(String urlToRead, Map<String, String> params, String body) {
        return requestHTTP(urlToRead, params, HttpMethod.POST, body);
    }

    /**
     * Http POST request.
     * 
     * @param urlToRead
     * @param params
     * @param body
     * @return
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse httpPost(String urlToRead, Map<String, String> params,
            Map<String, String> bodyParams) {
        bodyParams = checkParameters(bodyParams);
        return requestHTTP(urlToRead, params, HttpMethod.POST, getPostParameters(bodyParams));
    }

    /**
     * Http POST request.
     * 
     * @param urlToRead
     * @param params
     * @param body
     * @return
     */
    public static HttpResponse httpPost(String urlToRead, Map<String, String> bodyParams) {
        return httpPost(urlToRead, null, bodyParams);
    }

    /**
     * 
     * @param httpResponse
     * @return
     * @throws ArcgisException
     */
    protected static void checkHttpResponse(String httpResponse) throws ArcgisException {
        JsonParser jsonParser = new JsonParser();
        try {
            JsonElement json = jsonParser.parse(httpResponse);

            if (json.isJsonObject()) {
                // Single action response
                JsonElement node = getErrorJsonobject(json.getAsJsonObject());
                if (node != null) {
                    checkHttpSingleResponse(node);
                }
            } else {
                throw new JsonSyntaxException("Unexpected response format." + httpResponse);
            }
        } catch (JsonSyntaxException e) {
            throw new ArcgisException("Unexpected response format" + httpResponse);
        } catch (IllegalStateException | ClassCastException e) {
            throw new ArcgisException("checkHttpResponse, Unexpected exception" + e.getMessage()
                    + " \n\t" + httpResponse);
        } catch (NullPointerException e) {
            throw new ArcgisException(
                    "checkHttpResponse, Null Body recived from server." + httpResponse);
        }
    }

    /**
     * Checks if response result is successful or not.
     * 
     * @param response
     * @return
     */
    public static boolean checkHttpResponse(HttpResponse response) {
        boolean isSuccessful = false;

        if (!response.isResponseCodeError()) {
            try {
                checkHttpResponse(response.getBody());
                isSuccessful = true;
            } catch (ArcgisException e) {
                LOGGER.debug("Response has erros, " + e);
                response.setError(e);
            }
        } else {
            LOGGER.debug("Response is not successful, code: " + response.getResponseCode());
        }

        return isSuccessful;
    }

    /**
     * 
     * @param httpResponse
     * @return
     * @throws ArcgisException
     */
    protected static JsonElement getErrorJsonobject(JsonObject httpResponse)
            throws ArcgisException {
        JsonElement result = null;
        try {
            if (httpResponse.has(ERROR_RESPONSE_TAG)) {
                result = httpResponse;
            } else if (httpResponse.has(ADD_RESULTS_RESPONSE_TAG)) {
                result = httpResponse.get(ADD_RESULTS_RESPONSE_TAG);
            } else if (httpResponse.has(UPDATE_RESULTS_RESPONSE_TAG)) {
                result = httpResponse.get(UPDATE_RESULTS_RESPONSE_TAG);
            } else if (httpResponse.has(DELETE_RESULTS_RESPONSE_TAG)) {
                result = httpResponse.get(DELETE_RESULTS_RESPONSE_TAG);
            }
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ArcgisException(e);
        }

        return result;
    }

    /**
     * 
     * @param response
     * @throws ArcgisException
     */
    protected static void checkHttpSingleResponse(JsonElement response) throws ArcgisException {
        try {
            if (response.isJsonObject()) {
                // Single action response
                JsonObject node = response.getAsJsonObject();
                checkHttpSingleResponse(node);
            } else if (response.isJsonArray()) {
                // Multi action response
                JsonArray jsonArray = response.getAsJsonArray();
                // checks each action individually and returns first error found
                for (JsonElement jsonElement : jsonArray) {
                    checkHttpSingleResponse(jsonElement.getAsJsonObject());
                }
            } else {
                throw new ArcgisException("Unexpected JSON object type " + response.toString());
            }
        } catch (IllegalStateException e) {
            throw new ArcgisException("Error checking response, " + response.toString());
        }
    }

    /**
     * Check single-action responses.
     * 
     * @param node
     * @throws ArcgisException
     */
    protected static void checkHttpSingleResponse(JsonObject node) throws ArcgisException {
        int errorCode = -1;
        String message = "";
        String details = "";

        if (node.has(ERROR_RESPONSE_TAG)) {
            try {
                JsonObject errorNode = node.get(ERROR_RESPONSE_TAG).getAsJsonObject();
                errorCode = errorNode.get(CODE_RESPONSE_TAG).getAsInt();
                if (errorNode.has(MESSAGE_RESPONSE_TAG)) { // Single action errors
                    message = errorNode.get(MESSAGE_RESPONSE_TAG).getAsString();
                } else if (errorNode.has(DESCRIPTION_RESPONSE_TAG)) { // Multi action errors
                    message = errorNode.get(DESCRIPTION_RESPONSE_TAG).getAsString();
                }
                if (errorNode.has(DETAILS_RESPONSE_TAG)) {
                    details = errorNode.get(DETAILS_RESPONSE_TAG).getAsString();
                }
            } catch (Exception e) {
                throw new ArcgisException("Error checking single reponse, " + node.toString()
                        + " - " + e.getMessage());
            }

            throw new ArcgisException(errorCode,
                    "Response Error " + errorCode + "  " + message + "\n" + details);
        }
    }

}
