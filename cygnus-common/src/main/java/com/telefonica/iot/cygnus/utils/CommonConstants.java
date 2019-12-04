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
public final class CommonConstants {
    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private CommonConstants() {
    } // CommonConstants
    
    // Not applicable
    public static final String NA = "N/A";
    
    // Http header names
    public static final String HTTP_HEADER_CONTENT_TYPE = "content-type";
    
    // Http headers probably used by Flume events as well... TBD: should not be here!!
    public static final String HEADER_FIWARE_SERVICE      = "fiware-service";
    public static final String HEADER_FIWARE_SERVICE_PATH = "fiware-servicepath";
    public static final String HEADER_CORRELATOR_ID       = "fiware-correlator";
    public static final String HEADER_NGSI_VERSION        = "ngsiv2-attrsformat";
    
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
    public static final String CONCATENATOR = "xffff";
    public static final String OLD_CONCATENATOR = "_";
    
    // Header for API uses
    public static final String CYGNUS_IPR_HEADER = "#####\n"
            + "# Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U\n"
            + "# \n"
            + "# This file is part of fiware-cygnus (FIWARE project).\n"
            + "# \n"
            + "# fiware-cygnus is free software: you can redistribute it and/or modify it under "
            + "the terms of the GNU Affero General\n"
            + "# Public License as published by the Free Software Foundation, either version 3 "
            + "of the License, or (at your option) any\n"
            + "# later version.\n"
            + "# fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT "
            + "ANY WARRANTY; without even the implied\n"
            + "# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU "
            + "Affero General Public License for more\n"
            + "# details.\n"
            + "# \n"
            + "# You should have received a copy of the GNU Affero General Public License "
            + "along with fiware-cygnus. If not, see\n"
            + "# http://www.gnu.org/licenses/.\n"
            + "# \n"
            + "# For those usages not covered by the GNU Affero General Public License please "
            + "contact with iot_support at tid dot es";

    /**
     * Enumeration for ManagementInterface.
     * 
     * @author fermin
     *
     */
    public enum LoggingLevels { FATAL, ERROR, WARN, INFO, DEBUG, ALL, OFF }
    
} // CommonConstants
