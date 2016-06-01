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
public final class CommonConstants {
    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private CommonConstants() {
    } // CommonConstants
    
    // Http header names
    public static final String HTTP_HEADER_CONTENT_TYPE = "content-type";
    
    // Http headers propably used by Flume events as well
    public static final String HEADER_FIWARE_SERVICE      = "fiware-service";
    public static final String HEADER_FIWARE_SERVICE_PATH = "fiware-servicepath";
    public static final String HEADER_CORRELATOR_ID       = "fiware-correlator";
    
    // Used by CKANBackendImpl... TBD: should not be here!!
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
    public static final int SERVICE_HEADER_MAX_LEN      = 50;
    public static final int SERVICE_PATH_HEADER_MAX_LEN = 50;
    
    // Others
    public static final String EMPTY_MD = "[]";
    
    // log4j specific constants
    public static final String LOG4J_CORR = "correlatorId";
    public static final String LOG4J_TRANS = "transactionId";
    public static final String LOG4J_SVC = "service";
    public static final String LOG4J_SUBSVC = "subservice";
    public static final String LOG4J_COMP = "agent";
    
    // encoding
    public static final String INTERNAL_CONCATENATOR = "=";
    public static final String CONCATENATOR = "x0000";
    
} // CommonConstants
