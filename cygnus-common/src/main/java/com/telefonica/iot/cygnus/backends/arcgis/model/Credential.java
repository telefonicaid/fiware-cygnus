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

package com.telefonica.iot.cygnus.backends.arcgis.model;

import java.time.LocalDateTime;

import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 * @author dmartinez
 *
 */
public abstract class Credential  {
    private static final CygnusLogger LOGGER = new CygnusLogger(Credential.class);
    private String token;
    private LocalDateTime expirationTime;

    /**
     * Default constructor.
     */
    public Credential() {
        token = "";
        expirationTime = LocalDateTime.now().minusMinutes(1);
    }

    /**
     * @param token
     */
    public Credential(String token, LocalDateTime expirationTime) {
        super();
        setToken(token, expirationTime);
    }

    /**
     * @param token
     */
    public Credential(String token, int expirationMillis) {
        super();
        this.setTokenMillis(token, expirationMillis);
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * token the token to set.
     * 
     * @param token
     * @param expirationTime
     */
    public void setToken(String token, LocalDateTime expirationTime) {
        this.token = token;
        this.expirationTime = expirationTime;
    }

    /**
     * 
     * @param token
     * @param expirationMillis
     */
    public void setTokenMillis(String token, int expirationMillis) {
        LocalDateTime expiration = LocalDateTime.now().plusNanos(expirationMillis * 1000000);
        this.setToken(token, expiration);
    }

    /**
     * token the token to set.
     * 
     * @param token
     * @param expirationMins
     */
    public void setToken(String token, int expirationMins) {
        this.token = token;
        this.expirationTime = LocalDateTime.now().plusMinutes(expirationMins - 1);
    }

    /**
     * ¿ha caducado la credencial?
     * 
     * @return
     */
    public boolean isExpired() {
        LOGGER.debug("Checking Credential expiration time: " + expirationTime);
        return LocalDateTime.now().isAfter(expirationTime) || "".equals(token);
    }

    /**
     * @return the expirationTime
     */
    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    /**
     * @param expirationTime
     *            the expirationTime to set
     */
    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

}
