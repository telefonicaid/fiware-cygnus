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

import java.io.UnsupportedEncodingException;

import org.json.JSONException;

import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.errors.CygnusBadAuthorization;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;

/**
 *
 * @author pcoello25
 */
public interface OrionBackend {

    /**
     * Subscribe to Orion with a given port, host and string with subscription
     * information (NGSIv1).
     * 
     * @param subscription
     * @param token
     * @param fiwareService
     * @param fiwareServicePath
     * @throws Exception
     * @return response
     */
    JsonResponse subscribeContextV1(String subscription, String token, String fiwareService, String fiwareServicePath)
            throws Exception;

    /**
     * Subscribe to Orion with a given port, host and string with subscription
     * information (NGSIv2).
     * 
     * @param subscription
     * @param token
     * @param fiwareService
     * @param fiwareServicePath
     * @throws Exception
     * @return response
     */
    JsonResponse subscribeContextV2(String subscription, String token, String fiwareService, String fiwareServicePath)
            throws Exception;

    /**
     * Unsubscribe from Orion with a given subscription id (NGSIv1).
     * 
     * @param subscriptionId
     * @param token
     * @param fiwareService
     * @param fiwareServicePath
     * @throws Exception
     * @return response
     */
    JsonResponse deleteSubscriptionV1(String subscriptionId, String token, String fiwareService,
            String fiwareServicePath) throws Exception;

    /**
     * Unsubscribe from Orion with a given subscription id (NGSIv2).
     * 
     * @param subscriptionId
     * @param token
     * @param fiwareService
     * @param fiwareServicePath
     * @throws Exception
     * @return response
     */
    JsonResponse deleteSubscriptionV2(String subscriptionId, String token, String fiwareService,
            String fiwareServicePath) throws Exception;

    /**
     * Gets a current subscription with a given subscription id (NGSIv2).
     * 
     * @param token
     * @param subscriptionId
     * @param fiwareService
     * @param fiwareServicePath
     * @throws Exception
     * @return response
     */
    JsonResponse getSubscriptionsByIdV2(String token, String subscriptionId, String fiwareService,
            String fiwareServicePath) throws Exception;

    /**
     * Gets all current subscriptions in the system (NGSIv2).
     * 
     * @param token
     * @param subscriptionId
     * @param fiwareService
     * @param fiwareServicePath
     * @throws Exception
     * @return response
     */
    JsonResponse getSubscriptionsV2(String token, String subscriptionId, String fiwareService, String fiwareServicePath)
            throws Exception;

    /**
     * Create or update entity in the system (NGSIv2).
     * 
     * @param bodyJSON
     * @param orionToken
     * @param fiwareService
     * @param fiwareServicePath
     * @throws CygnusRuntimeError
     * @throws CygnusPersistenceError
     * @throws UnsupportedEncodingException
     * @throws CygnusBadAuthorization
     * @throws JSONException
     */
    void updateRemoteContext(String bodyJSON, String orionToken, String fiwareService, String fiwareServicePath)
            throws CygnusRuntimeError, CygnusPersistenceError, UnsupportedEncodingException, CygnusBadAuthorization,
            JSONException;
} // StatsBackend
