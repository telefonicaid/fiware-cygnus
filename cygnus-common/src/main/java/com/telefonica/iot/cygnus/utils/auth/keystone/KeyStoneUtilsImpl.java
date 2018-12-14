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
package com.telefonica.iot.cygnus.utils.auth.keystone;

import com.telefonica.iot.cygnus.backends.http.HttpBackend;
import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

/**
 * Keystone implementation Backend.
 * 
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 */
public class KeyStoneUtilsImpl extends HttpBackend implements KeyStoneUtils {

    private static final String STR_SCOPE_TAG = "<<scope>>";

    private static final String STR_NO_SERVICEPATH = "/";

    private static final String KEYSTONE_RELATIVE_URL = "/v3/auth/tokens";

    private static final CygnusLogger LOGGER = new CygnusLogger(KeyStoneUtilsImpl.class);

    // Request Header constats.
    private static final String HEADER_X_AUTH_TOKEN = "X-Auth-token";
    private static final String HEADER_FIWARE_SERVICEPATH = "Fiware-ServicePath";
    private static final String HEADER_FIWARE_SERVICE = "Fiware-Service";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE_VALUE = "application/json";
    private static final String HEADER_CONTENT_TYPE = "Content-type";

    // JSON body request constants...
    private static final String STR_PASSWORD_TAG = "<<passwd>>";
    private static final String STR_USER_TAG = "<<usuario>>";
    private static final String STR_SUBSERVICE_TAG = "<<subservicio>>";
    private static final String STR_SERVICE_TAG = "<<servicio>>";

    private static final String STR_JSON_REQUEST_TEMPLATE = "" + "{" + "\"auth\": {" + "    \"identity\": {"
            + "        \"methods\": [" + "            \"password\"" + "        ]," + "        \"password\": {"
            + "            \"user\": {" + "                \"domain\": {"
            + "                    \"name\": \"<<servicio>>\"" + "                },"
            + "                \"name\": \"<<usuario>>\"," + "                \"password\": \"<<passwd>>\""
            + "            }" + "        }" + "    }," + "    \"scope\": {"

            + STR_SCOPE_TAG

            + "        }" + "    }" + "}";

    private static final String STR_JSON_SERVICEPATH_SCOPE = "" + "        \"project\": {" + "            \"domain\": {"
            + "               \"name\": \"<<servicio>>\"" + "            },"
            + "            \"name\": \"<<subservicio>>\"}";

    private static final String STR_JSON_SERVICE_SCOPE = "" + "            \"domain\": {"
            + "               \"name\": \"<<servicio>>\"" + "            }";

    // token live time (minutes)
    private static final int INT_DEFAULT_LIVE_TIME = 50;

    // Token cache object
    private TokenCache tokenCache;

    /**
     * Constructor.
     * 
     * @param keyStoneHost
     *            authentication host
     * @param keyStonePort
     *            authentication port
     * @param ssl
     *            ssl connection indicator
     * @param maxConns
     *            max connections
     * @param maxConnsPerRoute
     *            max connections per route
     */
    public KeyStoneUtilsImpl(String keyStoneHost, String keyStonePort, boolean ssl, int maxConns,
            int maxConnsPerRoute) {
        super(keyStoneHost, keyStonePort, ssl, false, null, null, null, null, maxConns, maxConnsPerRoute);
        setAllHeaders(true);
        this.tokenCache = new TokenCache();
    }

    /**
     * Parameters.
     * 
     * @param token
     *            authentication token
     * @param fiwareService
     *            service name
     * @param fiwareServicePath
     *            subservice name
     * @return headers ArrayList
     */
    private ArrayList<Header> getHeaders(String token, String fiwareService, String fiwareServicePath) {
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE));
        headers.add(new BasicHeader(HEADER_ACCEPT, HEADER_CONTENT_TYPE_VALUE));
        headers.add(new BasicHeader(HEADER_FIWARE_SERVICE, fiwareService));
        if (fiwareServicePath != null) {
            headers.add(new BasicHeader(HEADER_FIWARE_SERVICEPATH, fiwareServicePath));
        }

        if (token != null) {
            headers.add(new BasicHeader(HEADER_X_AUTH_TOKEN, token));
        } // if

        return headers;
    }

    private String getAuthRequestJSON(String user, String password, String fiwareService, String fiwareServicePath) {
        // generate JSON Body
        String bodyJSON = STR_JSON_REQUEST_TEMPLATE;

        // compose template for required auth level
        if (fiwareServicePath.equals(STR_NO_SERVICEPATH)) {
            bodyJSON = bodyJSON.replaceAll(STR_SCOPE_TAG, STR_JSON_SERVICE_SCOPE);
        } else {
            bodyJSON = bodyJSON.replaceAll(STR_SCOPE_TAG, STR_JSON_SERVICEPATH_SCOPE);
        }

        bodyJSON = bodyJSON.replaceAll(STR_SERVICE_TAG, fiwareService);
        bodyJSON = bodyJSON.replaceAll(STR_SUBSERVICE_TAG, fiwareServicePath);
        bodyJSON = bodyJSON.replaceAll(STR_USER_TAG, user);
        bodyJSON = bodyJSON.replaceAll(STR_PASSWORD_TAG, password);

        return bodyJSON;
    }

    @Override
    public String updateSessionToken(String user, String password, String fiwareService, String fiwareServicePath)
            throws CygnusRuntimeError, CygnusPersistenceError {
        try {
            LOGGER.info("Requesting new token from Keystone Server.");
            // create the relative URL
            String relativeURL = KEYSTONE_RELATIVE_URL;

            // create the http headers
            ArrayList<Header> headers = getHeaders(null, fiwareService, fiwareServicePath);

            // generate JSON Body
            String bodyJSON = getAuthRequestJSON(user, password, fiwareService, fiwareServicePath);
            LOGGER.debug("Request bodyJSON: " + bodyJSON);

            StringEntity entity = new StringEntity(bodyJSON);

            // do the request
            JsonResponse response = doRequest("POST", relativeURL, true, headers, entity);

            // check the status
            if (response.getStatusCode() != 201) {
                LOGGER.error("Auth Error, HttpFS response:: " + response.getStatusCode() + " "
                        + response.getReasonPhrase() + "\n");
                LOGGER.error("response.toString :: " + response.getStatusCode() + " " + response.toString() + "\n");
                Header[] hders = response.getHeaders();
                String cabeceras = "";
                for (Header header : hders) {
                    cabeceras += header.getName() + "  " + header.getValue() + "\n";
                }
                LOGGER.error("response.headers :: " + response.getStatusCode() + "\n" + cabeceras + "\n");

                throw new CygnusRuntimeError("Auth Error, HttpFS response:: " + response.getStatusCode() + " "
                        + response.getReasonPhrase() + "\n");
            } else {
                String token = "";
                // Get Token of headers
                for (Header header : response.getHeaders()) {
                    if (header.getName().equals("X-Subject-Token")) {
                        token = header.getValue();
                    }
                }
                if (!token.equals("")) {
                    tokenCache.addToken(fiwareService, fiwareServicePath, token);
                    LOGGER.info("Token succesfully received.");
                    return token;
                } else {
                    LOGGER.error("Token not Found, HttpFS response:: " + response.getStatusCode() + " "
                            + response.getReasonPhrase());
                    throw new CygnusRuntimeError("Token not Found, HttpFS response:: " + response.getStatusCode() + " "
                            + response.getReasonPhrase());
                }
            } // if Check Status
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Unsupported encodding:: " + e.getMessage());
            throw new CygnusRuntimeError("Unsupported encodding:: " + e.getMessage());
        }
    }

    @Override
    public String getSessionToken(String user, String password, String fiwareService, String fiwareServicePath)
            throws CygnusRuntimeError, CygnusPersistenceError {

        try { // try in Cache
            return tokenCache.getToken(fiwareService, fiwareServicePath);

        } catch (CygnusExpiratingError e) {
            // If expired...

            if (tokenCache.isCached(fiwareService, STR_NO_SERVICEPATH)) {
                // Cached at service level
                return updateSessionToken(user, password, fiwareService, STR_NO_SERVICEPATH);
            } else {
                // Cached at SubService level
                return updateSessionToken(user, password, fiwareService, fiwareServicePath);
            }
        } catch (CygnusRuntimeError e) {
            // not cached or cache error

            try {
                // first try to recover token at service level.
                return updateSessionToken(user, password, fiwareService, STR_NO_SERVICEPATH);
            } catch (Exception e1) {
                // if not possible, at more restrictive sub-service level.
                return updateSessionToken(user, password, fiwareService, fiwareServicePath);
            }

        }
    }

    /**
     * LiveTime setter.
     * 
     * @param liveTime
     */
    public void setTokenTimeToLive(int liveTime) {
        tokenCache.setLiveTime(liveTime);
    }

    /**
     * Token Cache class.
     * 
     * @author PMO Santander Smart City – Ayuntamiento de Santander
     */
    private class TokenCache {
        /**
         * Token class.
         * 
         * @author PMO Santander Smart City – Ayuntamiento de Santander
         *
         */
        private class Token {

            private String fiwareService;
            private String fiwareServicePath;
            private Calendar expirationDate;
            private String token;
            private int tokenMinutesLive;

            /**
             * Default constructor.
             * 
             * @param fiwareService
             *            service name
             * @param fiwareServicePath
             *            subservice name
             * @param token
             *            auth token
             */
            Token(String fiwareService, String fiwareServicePath, String token, int liveTime) {
                super();
                this.fiwareService = fiwareService;
                this.fiwareServicePath = fiwareServicePath;
                this.token = token;
                this.tokenMinutesLive = liveTime; // Default live time = 50
                                                  // minutes
                this.renew();
            }

            /**
             * Gets token value.
             * 
             * @return token string.
             */
            public String getToken() {
                return token;
            }

            /**
             * Sets token value.
             * 
             * @param token
             *            auth token
             */
            public void setToken(String token) {
                this.token = token;
            }

            /**
             * @return returns the Fiware Service.
             */
            public String getFiwareService() {
                return fiwareService;
            }

            /**
             * @return returns the Fiware ServicePath.
             */
            public String getFiwareServicePath() {
                return fiwareServicePath;
            }

            /**
             * @return returns expiration date for the token.
             */
            public Calendar getExpirationDate() {
                return expirationDate;
            }

            /**
             * Gets token´s default live time.
             * 
             * @return Returns token´s default time to live in minutes
             */
            public int getTokenMinutesLive() {
                return tokenMinutesLive;
            }

            /**
             * Renews token expiration date.
             */
            public void renew() {
                this.expirationDate = (GregorianCalendar) GregorianCalendar.getInstance();
                this.expirationDate.add(GregorianCalendar.MINUTE, this.tokenMinutesLive);
            }

            /**
             * @return true if the token is valid.
             */
            public boolean isValid() {
                GregorianCalendar now = (GregorianCalendar) GregorianCalendar.getInstance();
                return this.expirationDate.compareTo(now) > 0;
            }

            /**
             * Generates an String id for the token.
             * 
             * @return String Index
             */
            public String getIndexKey() {
                return fiwareService + fiwareServicePath;
            }

        }

        private Map<String, Token> tokenCache;
        private int liveTime;

        /**
         * Default Constructor.
         */
        TokenCache() {
            super();
            this.tokenCache = new HashMap<String, Token>();
            this.liveTime = INT_DEFAULT_LIVE_TIME;
        }

        public int getLiveTime() {
            return liveTime;
        }

        public void setLiveTime(int liveTime) {
            this.liveTime = liveTime;
        }

        public boolean isCached(String fiwareService, String fiwareServicePath) {
            Token token = new Token(fiwareService, fiwareServicePath, "", liveTime);
            String key = token.getIndexKey();
            return tokenCache.containsKey(key);
        }

        void addToken(String fiwareService, String fiwareServicePath, String token) {
            Token tokenObj = new Token(fiwareService, fiwareServicePath, token, liveTime);
            String key = tokenObj.getIndexKey();

            // verify if already exists
            if (tokenCache.containsKey(key)) {
                tokenObj = tokenCache.get(key);
                // Renew live time & token string
                tokenObj.renew();
                tokenObj.setToken(token);
                LOGGER.info(" Token renew for " + key);
            } else {
                // add token to Cache
                tokenCache.put(key, tokenObj);

                LOGGER.info(" Token created for " + key);
            }
        }

        String getToken(String fiwareService, String fiwareServicePath)
                throws CygnusExpiratingError, CygnusRuntimeError {
            Token tokenObj = new Token(fiwareService, fiwareServicePath, "", liveTime);
            String key = "";

            // Check at Service and Subservice levels
            if (tokenCache.containsKey(tokenObj.getIndexKey())) {
                key = tokenObj.getIndexKey();
            } else if (tokenCache.containsKey(fiwareService + STR_NO_SERVICEPATH)) {
                tokenObj.fiwareServicePath = STR_NO_SERVICEPATH;
                key = tokenObj.getIndexKey();
            }

            // if exists
            if (!key.equals("")) {
                tokenObj = tokenCache.get(key);

                if (tokenObj.isValid()) {
                    LOGGER.info("Returning Token from cache " + key);
                    return tokenObj.getToken();
                } else {
                    // Token expired
                    // tokenCache.remove(key);

                    LOGGER.info("The token has expired for" + key);
                    throw new CygnusExpiratingError("The token has expired for" + key);
                }
            } else {
                // Not found in Cache
                LOGGER.info("Token not found in cache." + fiwareService + fiwareServicePath);
                throw new CygnusRuntimeError("Token not found in cache." + fiwareService + fiwareServicePath);
            }
        }
    }

}
