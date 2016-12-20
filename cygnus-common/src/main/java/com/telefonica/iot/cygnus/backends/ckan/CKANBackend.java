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

package com.telefonica.iot.cygnus.backends.ckan;

/**
 * Interface for those backends implementing the persistence in CKAN.
 * 
 * @author fermin, frb
 */
public interface CKANBackend {

    /**
     * Persists new records within the given resource, within the given package, within the given organization.
     * @param orgName Organization name
     * @param pkgName Package/dataset name
     * @param resName Resource name
     * @param records Recods to be added to the resource
     * @param createEnabled
     * @throws Exception
     */
    void persist(String orgName, String pkgName, String resName, String records, boolean createEnabled)
        throws Exception;
    
    /**
     * Trucates the resource within the given package, within the given organization, by size.
     * @param orgName
     * @param pkgName
     * @param resName
     * @param size
     * @throws Exception
     */
    void truncateBySize(String orgName, String pkgName, String resName, long size) throws Exception;
    
    /**
     * Truncates the given resource within the given package, within the given organization, by time.
     * @param orgName
     * @param pkgName
     * @param resName
     * @param time
     * @throws Exception
     */
    void truncateByTime(String orgName, String pkgName, String resName, long time) throws Exception;
    
    /**
     * Trucates all the cached resources by time.
     * @param time
     * @throws Exception
     */
    void truncateCachedByTime(long time) throws Exception;
    
} // CKANBackend
