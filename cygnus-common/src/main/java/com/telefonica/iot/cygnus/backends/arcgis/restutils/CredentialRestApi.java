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

import java.net.MalformedURLException;
import java.net.URL;

import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.model.Credential;
import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 * @author dmartinez
 *
 */
public abstract class CredentialRestApi extends RestApi {
    private static final CygnusLogger LOGGER = new CygnusLogger(CredentialRestApi.class);

    protected URL tokenGenUrl;

    protected Credential credential;
    protected String referer;

    protected int connectionTimeout = 0;
    protected int readTimeout = 0;

    /**
     * @param tokenGenUrl
     * @param credential
     * @param referer
     * @param expirationMins
     */
    public CredentialRestApi(URL tokenGenUrl, Credential credential, String referer,
                             int connectionTimeout, int readTimeout) {
        super();
        this.tokenGenUrl = tokenGenUrl;
        this.credential = credential;
        this.referer = referer;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * @param tokenGenUrl
     * @param credential
     * @param referer
     * @param expirationMins
     * @throws ArcgisException
     */
    public CredentialRestApi(String tokenGenUrl, Credential credential, String referer,
                             int connectionTimeout, int readTimeout)
            throws ArcgisException {
        super();

        this.credential = credential;
        this.referer = referer;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        try {
            if (tokenGenUrl != null && !"".equals(tokenGenUrl.trim())) {
                this.tokenGenUrl = new URL(tokenGenUrl);
            } else {
                this.tokenGenUrl = null;
            }
        } catch (MalformedURLException e) {
            throw new ArcgisException("Can´t parse token generation url, MalformedURLException.");
        } catch (Exception e) {
            throw new ArcgisException("CredentialRestApi, Unexpected Exception " + e.toString());
        }
    }

    /**
     * Obtiene las credenciales de acceso, refresca el token si es necesario.
     * 
     * @return Credential
     * @throws ArcgisException
     */
    public Credential getCredential() throws ArcgisException {
        LOGGER.debug("------------------ getCredential() " + "\t tokenGenUrl: " + tokenGenUrl
                + "\t credential: " + credential + "\t credential.isExpired(): "
                + (credential != null ? credential.isExpired() : null));
        if (tokenGenUrl != null && (credential == null || credential.isExpired())) {
            LOGGER.debug("Creating/Refreshing token.");
            credential = RestAuthentication.createToken(credential, tokenGenUrl, referer, this.connectionTimeout, this.readTimeout);
        }

        return credential;
    }

    /**
     * Obtiene las credenciales de acceso, refresca el token si es necesario.
     * 
     * @param credential
     * @throws ArcgisException
     */
    protected void setCredential(Credential credential) throws ArcgisException {
        this.credential = credential;
    }

    /**
     * @return the tokenGenUrl
     */
    public String getTokenGenUrl() {
        if (tokenGenUrl == null) {
            return "";
        } else {
            return tokenGenUrl.toString();
        }
    }

    /**
     * @param tokenGenUrl
     *            the tokenGenUrl to set
     */
    public void setTokenGenUrl(URL tokenGenUrl) {
        this.tokenGenUrl = tokenGenUrl;
    }

}
