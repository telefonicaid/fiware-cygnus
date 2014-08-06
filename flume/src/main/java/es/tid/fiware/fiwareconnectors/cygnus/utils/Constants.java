/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * francisco.romerobueno@telefonica.com
 */

package es.tid.fiware.fiwareconnectors.cygnus.utils;

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

    // HTTP headers
    public static final String ORG_HEADER = "fiware-service";
    public static final String CONTENT_TYPE = "content-type";
    public static final String USER_AGENT   = "user-agent";
    public static final int ORG_MAX_LEN = 32;

    // Common fields for sinks/backends
    public static final String RECV_TIME_TS = "recvTimeTs";
    public static final String RECV_TIME    = "recvTime";
    public static final String ENTITY_ID    = "entityId";
    public static final String ENTITY_TYPE  = "entityType";
    public static final String ATTR_NAME    = "attrName";
    public static final String ATTR_TYPE    = "attrType";
    public static final String ATTR_VALUE   = "attrValue";
    public static final String ATTR_MD      = "attrMd";
    public static final int NAMING_PREFIX_MAX_LEN = 32;

    // Logging
    public static final String TRANSACTION_ID = "transactionId";
    
    // HTTP client factory
    public static final int MAX_CONNS = 500;
    public static final int MAX_CONNS_PER_ROUTE = 100;
    
    // MySQL
    public static final int MYSQL_DB_NAME_MAX_LEN = 64;
    public static final int MYSQL_TABLE_NAME_MAX_LEN = 64;
    
    // CKAN
    public static final int CKAN_PKG_MAX_LEN = 100;
    public static final int CKAN_RESOURCE_MAX_LEN = 100;
    
    // Others
    public static final String EMPTY_MD = "[]";

} // Constants
