/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
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
    public static final String FLUME_HEADER_TRANSACTION_ID        = "transaction-id";
    public static final String FLUME_HEADER_GROUPED_SERVICE_PATH  = "grouped-servicepath"; // 'TODO': remove
    public static final String FLUME_HEADER_NOTIFIED_ENTITY       = "notified-entity"; // 'TODO': remove
    public static final String FLUME_HEADER_GROUPED_ENTITY        = "grouped-entity"; // 'TODO': remove
    public static final String FLUME_HEADER_GROUPED_ENTITY_TYPE   = "grouped-entity-type"; // 'TODO': remove
    public static final String FLUME_HEADER_TIMESTAMP             = "timestamp";
    public static final String FLUME_HEADER_MAPPED_SERVICE        = "mapped-fiware-service";
    public static final String FLUME_HEADER_MAPPED_SERVICE_PATH   = "mapped-fiware-servicepath";

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
    
    // FIWARE service and FIWARE service path specific constants
    public static final int SERVICE_HEADER_MAX_LEN      = 50;
    public static final int SERVICE_PATH_HEADER_MAX_LEN = 50;

    // NGSIRestHandler specific constants
    public static final String PARAM_DEFAULT_SERVICE      = "default_service";
    public static final String PARAM_DEFAULT_SERVICE_PATH = "default_service_path";
    public static final String PARAM_NOTIFICATION_TARGET  = "notification_target";
    
    //NGSICKANSink specific constants
    // http://docs.ckan.org/en/latest/api/#ckan.logic.action.create.organization_create
    // http://docs.ckan.org/en/latest/api/#ckan.logic.action.create.package_create
    public static final int CKAN_MAX_NAME_LEN = 100;
    public static final int CKAN_MIN_NAME_LEN = 2;
    
    // NGSICartoDBSink specific constants
    // http://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS
    public static final int    CARTO_DB_MAX_NAME_LEN = 63;
    public static final String CARTO_DB_THE_GEOM = "the_geom";
    
    // NGSIDynamoDBSink specific constants
    // http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Limits.html#limits-naming-rules
    public static final int    DYNAMO_DB_MIN_NAME_LEN = 3;
    public static final int    DYNAMO_DB_MAX_NAME_LEN = 255;
    public static final String DYNAMO_DB_PRIMARY_KEY  = "ID";
    
    // NGSIHDFSSink specific constants
    public static final int HDFS_MAX_NAME_LEN = 255;
    
    // NGSIMongoSink/NGSISTHSink specific constants
    // https://docs.mongodb.com/manual/reference/limits/#naming-restrictions
    public static final int MONGO_DB_MAX_NAMESPACE_SIZE_IN_BYTES = 113;
    public static final int MONGO_DB_MIN_HASH_SIZE_IN_BYTES      = 20;
    
    //NGSIMySQLSink specific constants
    // http://dev.mysql.com/doc/refman/5.7/en/identifiers.html
    public static final int MYSQL_MAX_NAME_LEN = 64;
    
    // NGSIPostgreSQLSink specific constants
    // http://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS
    public static final int POSTGRESQL_MAX_NAME_LEN = 63;
    
} // NGSIConstants
