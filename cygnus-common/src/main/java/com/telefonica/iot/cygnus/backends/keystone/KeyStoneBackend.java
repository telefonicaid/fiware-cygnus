package com.telefonica.iot.cygnus.backends.keystone;

import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;

/**
 * Keystone Authentication Backend.
 * 
 * @author dmartinez
 */
public interface KeyStoneBackend {

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
