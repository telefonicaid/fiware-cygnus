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
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package es.tid.fiware.fiwareconnectors.cygnus.backends.ckan;

import java.util.Map;

/**
 * Interface for those backends implementing the persistence in CKAN.
 * 
 * @author fermin
 */
public interface CKANBackend {

    /**
     * Persist data in the CKAN datastore associated with the entity in a given organization (row mode).
     * 
     * @param recvTimeTs Reception time in milliseconds
     * @param recvTime Reception time (human readable)
     * @param orgName Organization name
     * @param pkgName Package/dataset name
     * @param resName Resource name
     * @param attrName Attribute name
     * @param attrType Attribute type
     * @param attrValue Attribute value
     * @param attrMd Attribute metadata string serialization
     * @throws Exception
     */
    void persist(long recvTimeTs, String recvTime, String orgName, String pkgName, String resName, String attrName,
            String attrType, String attrValue, String attrMd) throws Exception;

    /**
     * Persist data in the CKAN datastore associated with the entity in a given organization (column mode).
     *
     * @param recvTime Reception time (human readable)
     * @param orgName Organization name
     * @param pkgName Package/dataset name
     * @param resName Resource name
     * @param attrList Hashmap containing the attributes to persist
     * @param attrMdList Hashmap containing the metadata string serialization to persist
     * @throws Exception
     */
    void persist(String recvTime, String orgName, String pkgName, String resName, Map<String, String> attrList,
            Map<String, String> attrMdList) throws Exception;
    
} // CKANBackend
