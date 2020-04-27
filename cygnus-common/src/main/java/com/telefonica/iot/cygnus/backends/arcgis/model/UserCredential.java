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
public class UserCredential extends Credential {

    private String user;
    private String password;
    private String referer;

    /**
     * 
     */
    private UserCredential() {
        super();
    }

    /**
     * @param token
     */
    public UserCredential(String user, String passwd) {
        super();
        this.user = user;
        this.password = passwd;
    }

    /**
     * Constructor.
     * 
     * @param user
     * @param passwd
     * @param token
     * @param expiration
     */
    public UserCredential(String user, String passwd, String token, LocalDateTime expiration) {
        this(user, passwd);
        this.setToken(token, expiration);
    }

    /**
     * Constructor.
     * 
     * @param user
     * @param passwd
     * @param token
     * @param expirationMillis
     */
    public UserCredential(String user, String passwd, String token, int expirationMillis) {
        this(user, passwd);
        this.setTokenMillis(token, expirationMillis);
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the referer
     */
    public String getReferer() {
        return referer;
    }

    /**
     * @param referer
     *            the referer to set
     */
    public void setReferer(String referer) {
        this.referer = referer;
    }

}
