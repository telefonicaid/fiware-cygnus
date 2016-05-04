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

package com.telefonica.iot.cygnus.utils;

/**
 *
 * @author fermin
 */
public final class NGSIConstants {
    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private NGSIConstants() {
    } // NGSIConstants
    
    // Flume header names added by NGSIRestHandler or NGSIGroupingInterceptor
    public static final String FLUME_HEADER_TRANSACTION_ID         = "transaction-id";
    public static final String FLUME_HEADER_GROUPED_SERVICE_PATHS  = "grouped-servicepaths";
    public static final String FLUME_HEADER_NOTIFIED_ENTITIES      = "notified-entities";
    public static final String FLUME_HEADER_GROUPED_ENTITIES       = "grouped-entities";
    public static final String FLUME_HEADER_TIMESTAMP              = "timestamp";

    // Common fields for sinks
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

    // NGSIRestHandler configuration parameter names
    public static final String PARAM_DEFAULT_SERVICE      = "default_service";
    public static final String PARAM_DEFAULT_SERVICE_PATH = "default_service_path";
    public static final String PARAM_NOTIFICATION_TARGET  = "notification_target";
    
    // NGSIHDFSSink specific constants
    public static final int MAX_NAME_LEN_HDFS = 255;
    
    // NGSISTHSink specific constants
    public static final int STH_MAX_NAMESPACE_SIZE_IN_BYTES = 113;
    public static final int STH_MIN_HASH_SIZE_IN_BYTES      = 20;
    public static final String STH_CONCAT_CHARS             = ";";
    
    // NGSIDynamoDBSink specific constants
    public static final String DYNAMO_DB_PRIMARY_KEY = "ID";
    
    // NGSIPostgreSQLSink specific constants
    // http://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS
    public static final int POSTGRESQL_MAX_ID_LEN = 63;
    
    // NGSICartoDBSink specific constants
    public static final String THE_GEOM = "the_geom";
    
} // NGSIConstants
