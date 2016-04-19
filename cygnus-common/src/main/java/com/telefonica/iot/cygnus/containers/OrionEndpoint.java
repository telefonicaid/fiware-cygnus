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

package com.telefonica.iot.cygnus.containers;

import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 *
 * @author pcoello25
 */
public class OrionEndpoint {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(OrionEndpoint.class);
    private String host;
    private String port;
    private String ssl;
    private String xauthtoken;
        
    public OrionEndpoint() {
    } // endpoint

    public String getHost() {
        return host;
    } // gethost

    public String getPort() {
        return port;
    } // getport

    public String getSsl() {
        return ssl;
    } // getSsl

    public String getAuthToken() {
        return xauthtoken;
    } // getAuthToken

    public boolean hasAuthToken() {
        String token = this.getAuthToken();
        return (token != null);
    } // hasAuthToken
    
    public int isValid (boolean requiredToken) {
        
        // get host, port and ssl
        host = this.getHost();
        port = this.getPort();
        ssl = this.getSsl();
        boolean isValidSsl;
        
        // check if entire endpoint is missing        
        if ((host == null) && (port == null) && (ssl == null)) {
            LOGGER.debug("Missing endpoint in the request");
            return 21;
        } // if
        
        // check if endpoint contains ssl
        if (ssl == null) {
            LOGGER.debug("Field 'ssl' is missing in the endpoint");
            return 223;
        } else if ((ssl.equals("true") || ssl.equals("false")) ? 
                (isValidSsl=true):(isValidSsl=false));  
        // if else
        
        // check if endpoint contains host
        if (host == null) {
            LOGGER.debug("Field 'host' is missing in the endpoint");
            return 221;
        } // if
        
        // check if endpoint contains port 
        if (port == null) {
            LOGGER.debug("Field 'port' is missing in the endpoint");
            return 222;
        } // if
        
        // check if endpoint has an empty host
        if (host.length() == 0) {
            LOGGER.debug("Field 'host' is empty in the endpoint");
            return 231;
        } // if
        
        // check if endpoint has an empty port
        if (port.length() == 0) {
            LOGGER.debug("Field 'port' is empty in the endpoint");
            return 232;
        } // if
        
        // check if endpoint has an empty ssl
        if (ssl.length() == 0) {
            LOGGER.debug("Field 'ssl' is empty in the endpoint");
            return 233;
        } // if
        
        // check if endpoint contains invalid fields
        if (!(isValidSsl)) {
            LOGGER.debug("Field 'ssl' has an invalid value");
            return 24;
        } // if
                
        // API CHECK: authtoken neccesary 
        if (requiredToken && !hasAuthToken()) {
            LOGGER.debug("Auth-Token not given: Required for use API");
            return 5;
        }
        LOGGER.debug("Valid endpoint");
        return 0;
        
    } // isValid

} // OrionEndpoint
