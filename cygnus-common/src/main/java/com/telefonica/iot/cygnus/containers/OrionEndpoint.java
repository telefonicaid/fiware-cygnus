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
package com.telefonica.iot.cygnus.containers;

/**
 *
 * @author pcoello25, frb
 */
public class OrionEndpoint {
    
    private String host;
    private String port;
    private String ssl;
    private String authToken;
        
    /**
     * Constructor.
     */
    public OrionEndpoint() {
    } // endpoint

    public String getHost() {
        return host;
    } // gethost

    public String getPort() {
        return port;
    } // getport

    public boolean getSsl() {
        return ssl.equals("true");
    } // getSsl

    public String getAuthToken() {
        return authToken;
    } // getAuthToken
    
    /**
     * Checks if this endpoint is valid (i.e. it contains all the rquired fields, with valid content).
     * @throws Exception
     */
    public void validate() throws Exception {
        if (host == null) {
            throw new Exception("Invalid Orion endpoint, missing host");
        } else if (host.isEmpty()) {
            throw new Exception("Invalid Orion endpoint, empty host");
        } // if else
        
        if (port == null) {
            throw new Exception("Invalid Orion endpoint, missing port");
        } else if (port.isEmpty()) {
            throw new Exception("Invalid Orion endpoint, empty port");
        } // if else
        
        if (ssl == null) {
            throw new Exception("Invalid Orion endpoint, missing ssl flag");
        } else if (!ssl.equals("true") && !ssl.equals("false")) {
            throw new Exception("Invalid Orion endpoint, ssl flag must be 'true' or 'false'");
        }
        
        if (authToken == null) {
            throw new Exception("Invalid Orion endpoint, missing X-Auth-Token");
        } else if (authToken.isEmpty()) {
            throw new Exception("Invalid Orion endpoint, empty X-Auth-Token");
        } // if else
    } // validate

} // OrionEndpoint
