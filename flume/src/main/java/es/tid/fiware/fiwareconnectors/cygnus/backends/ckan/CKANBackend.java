/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * frb@tid.es
 */

package es.tid.fiware.fiwareconnectors.cygnus.backends.ckan;

import org.apache.http.impl.client.DefaultHttpClient;
import java.util.Map;

/**
 * Interface for those backends implementing the persistence in CKAN.
 * 
 * @author fermin
 */
public interface CKANBackend {

    /**
     * Prepares an organization for use, creating it if it doesn't previously exist
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param organization to initialize
     * @throws Exception
     */
    void initOrg(DefaultHttpClient httpClient, String organization) throws Exception;

    /**
     * Persist data in the CKAN datastore associated with the entity in a given organization (row mode)
     * 
     * @param httpClient HTTP client for accessing the backend server.
     * @param recvTimeTs reception time in milliseconds.
     * @param recvTime reception time (human readable)
     * @param organization organization.
     * @param entity entity string (including ID and type).
     * @param attrName attribute name.
     * @param attrType attribute type.
     * @param attrValue attribute value.
     * @param attrMd attribute metadata string serialization
     * @throws Exception
     */
    void persist(DefaultHttpClient httpClient, long recvTimeTs, String recvTime, String organization, String entity,
                 String attrName, String attrType, String attrValue, String attrMd) throws Exception;

    /**
     * Persist data in the CKAN datastore associated with the entity in a given organization (column mode)
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param recvTime reception time (human readable)
     * @param organization organization.
     * @param entity entity string (including ID and type).
     * @param attrList hashmap containing the attributes to persist
     * @param attrMdList hashmap containing the metadata string serialization to persist
     * @throws Exception
     */
    void persist(DefaultHttpClient httpClient, String recvTime, String organization, String entity,
                 Map<String, String> attrList, Map<String, String> attrMdList) throws Exception;

    
} // CKANBackend
