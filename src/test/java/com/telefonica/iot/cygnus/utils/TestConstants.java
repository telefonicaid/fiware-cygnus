/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
 *
 * cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.utils;

/**
 *
 * @author fermin
 */
public final class TestConstants {

    /**
     * Constructor.
     */
    private TestConstants() {
    } // TestConstants

    // HTTP header names
    public static final String HEADER_SERVICE = "fiware-service";
    public static final String HEADER_SERVICE_PATH = "fiware-servicepath";
    public static final String HEADER_CONTENT_TYPE = "content-type";
    public static final String HEADER_USER_AGENT   = "user-agent";

    // Common fields for sinks/backends
    public static final String RECV_TIME_TS = "recvTimeTs";
    public static final String RECV_TIME    = "recvTime";
    public static final String ENTITY_ID    = "entityId";
    public static final String ENTITY_TYPE  = "entityType";
    public static final String ATTR_NAME    = "attrName";
    public static final String ATTR_TYPE    = "attrType";
    public static final String ATTR_VALUE   = "attrValue";
    public static final String ATTR_MD      = "attrMd";
    
    // Configuration parameter names
    public static final String PARAM_DEFAULT_SERVICE = "default_service";
    public static final String PARAM_DEFAULT_SERVICE_PATH = "default_service_path";
    public static final String PARAM_NOTIFICATION_TARGET = "notification_target";
    public static final String PARAM_EVENTS_TTL = "events_ttl";

} // TestConstants

