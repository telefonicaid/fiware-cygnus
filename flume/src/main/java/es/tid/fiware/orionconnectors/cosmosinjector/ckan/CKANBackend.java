/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of cosmos-injector (FI-WARE project).
 *
 * cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with [PROJECT NAME]. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * frb@tid.es
 */

package es.tid.fiware.orionconnectors.cosmosinjector.ckan;

import org.apache.http.impl.client.DefaultHttpClient;
import java.util.Date;

/**
 * Interface for those backends implementing the persistence in CKAN.
 * 
 * @author fermin
 */
public interface CKANBackend {

    /**
     * This methods prepares the CKAN backend. In particular, it check that the dataset exists (creating
     * it otherwise) and populates the entity to resource id map based on its content.
     *
     * @param httpClient HTTP client for accessing the backend server.
     */
    void init(DefaultHttpClient httpClient) throws Exception;

    /**
     * Persist data in the CKAN datastore associated with the entity.
     * 
     * @param httpClient HTTP client for accessing the backend server.
     * @param date timestamp date.
     * @param entity entity string (including ID and type)
     * @param attrName attribute name
     * @param attrType attribute type
     * @param attrValue attribute value
     */
    void persist(DefaultHttpClient httpClient, Date date, String entity, String attrName, String attrType,
                 String attrValue) throws Exception;

    
} // CKANBackend
