/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.backends.orion;

import com.telefonica.iot.cygnus.backends.http.JsonResponse;

/**
 *
 * @author pcoello25
 */
public interface OrionBackend {
    
    /**
     * Updates a context element given a list of statistical metadata for each entity's attribute.
     * 
     * @param entityId
     * @param entityType
     * @param allAttrStats
     * @throws Exception
     */
    // TBD: https://github.com/telefonicaid/fiware-cygnus/issues/304
    // void updateContext(String entityId, String entityType, ArrayList<OrionStats> allAttrStats) throws Exception;
    
    /**
     * Subscribe to Orion with a given port, host and string with subscription information.
     * 
     * @param cygnusSubscription 
     * @param xAuthToken 
     * @param token
     * @throws Exception
     * @return response
     */
    JsonResponse subscribeContext(String cygnusSubscription, boolean xAuthToken, 
            String token) throws Exception;
    
    /** 
     * Unsubscribe from Orion with a given subscription id.
     * 
     * @param subscriptionId 
     * @param xAuthToken 
     * @param token
     * @throws Exception
     * @return response
     */
    JsonResponse deleteSubscription(String subscriptionId, boolean xAuthToken, 
            String token) throws Exception;
    
} // StatsBackend
