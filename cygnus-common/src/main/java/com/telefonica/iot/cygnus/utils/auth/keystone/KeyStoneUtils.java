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

import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;

/**
 * Keystone Authentication Backend.
 * 
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 */
public interface KeyStoneUtils {

    /**
     * Gets KeyStone session token given user and password using tokens cache.
     * 
     * @param user
     *            user for authentication
     * @param password
     *            password for authentication
     * @param fiwareService
     *            Fiware service name
     * @param fiwareServicePath
     *            Fiware subservice name
     * @return return value
     * @throws Exception
     *             general exception
     */
    String getSessionToken(String user, String password, String fiwareService, String fiwareServicePath)
            throws CygnusRuntimeError, CygnusPersistenceError;

    /**
     * Forces token update skipping cache.
     * 
     * @param user
     *            user for authentication
     * @param password
     *            password for authentication
     * @param fiwareService
     *            Fiware service name
     * @param fiwareServicePath
     *            Fiware subservice name
     * @return return value
     * @throws CygnusRuntimeError
     *             Runtime error
     * @throws CygnusPersistenceError
     *             Error during persistence process
     */
    String updateSessionToken(String user, String password, String fiwareService, String fiwareServicePath)
            throws CygnusRuntimeError, CygnusPersistenceError;

}
