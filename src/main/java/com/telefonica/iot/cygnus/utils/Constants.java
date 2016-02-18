/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.utils;

/**
 *
 * @author fermin
 */
public final class Constants {
    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private Constants() {
    } // Constants

    // HTTP header names
    public static final String HTTP_HEADER_FIWARE_SERVICE      = "fiware-service";
    public static final String HTTP_HEADER_FIWARE_SERVICE_PATH = "fiware-servicepath";
    
    // Flume header names
    public static final String FLUME_HEADER_NOTIFIED_SERVICE_PATHS = "notified-servicepaths";
    public static final String FLUME_HEADER_GROUPED_SERVICE_PATHS  = "grouped-servicepaths";
    public static final String FLUME_HEADER_NOTIFIED_ENTITIES      = "notified-entities";
    public static final String FLUME_HEADER_GROUPED_ENTITIES       = "grouped-entities";
    public static final String FLUME_HEADER_TRANSACTION_ID         = "transactionId";
    public static final String FLUME_HEADER_TTL                    = "ttl";
    public static final String FLUME_HEADER_TIMESTAMP              = "timestamp";
    
    // Both HTTP and Flume header names
    public static final String HEADER_CONTENT_TYPE   = "content-type";
    public static final String HEADER_USER_AGENT     = "user-agent";

    // Common fields for sinks/backends
    public static final String RECV_TIME_TS        = "recvTimeTs";
    public static final String RECV_TIME           = "recvTime";
    public static final String FIWARE_SERVICE_PATH = "fiwareServicePath";
    public static final String ENTITY_ID           = "entityId";
    public static final String ENTITY_TYPE         = "entityType";
    public static final String ATTR_NAME           = "attrName";
    public static final String ATTR_TYPE           = "attrType";
    public static final String ATTR_VALUE          = "attrValue";
    public static final String ATTR_MD             = "attrMd";
    public static final String ATTR_MD_FILE        = "attrMdFile";

    // Maximum values
    public static final int MAX_CONNS                   = 500;
    public static final int MAX_CONNS_PER_ROUTE         = 100;
    public static final int MAX_NAME_LEN                = 64;
    public static final int MAX_NAME_LEN_HDFS           = 255;
    public static final int SERVICE_HEADER_MAX_LEN      = 50;
    public static final int SERVICE_PATH_HEADER_MAX_LEN = 50;
    
    // Others
    public static final String EMPTY_MD = "[]";

    // Configuration parameter names
    public static final String PARAM_DEFAULT_SERVICE      = "default_service";
    public static final String PARAM_DEFAULT_SERVICE_PATH = "default_service_path";
    public static final String PARAM_NOTIFICATION_TARGET  = "notification_target";
    public static final String PARAM_EVENTS_TTL           = "events_ttl";
    
    // OrionSTHSink specific headers
    public static final int STH_MAX_NAMESPACE_SIZE_IN_BYTES = 113;
    public static final int STH_MIN_HASH_SIZE_IN_BYTES      = 20;
    
    // OrionDynamoDB specific headers
    public static final String DYNAMO_DB_PRIMARY_KEY = "ID";
    
    // L4J specific constants
    public static final String LOG4J_SVC = "service";
    public static final String LOG4J_SUBSVC = "subservice";
    
} // Constants
