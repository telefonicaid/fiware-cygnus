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

package com.telefonica.iot.cygnus.backends.ckan;

import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.backends.http.HttpBackend;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import org.json.simple.JSONObject;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONArray;

/**
 * Interface for those backends implementing the persistence in CKAN.
 *
 * @author fermin, frb
 */
public class CKANBackendImpl extends HttpBackend implements CKANBackend {

    private static final CygnusLogger LOGGER = new CygnusLogger(CKANBackendImpl.class);
    private static final int RECORDSPERPAGE = 100;
    private final String orionUrl;
    private final String apiKey;
    private final String viewer;
    private CKANCache cache;

    /**
     * Constructor.
     * @param apiKey
     * @param ckanHost
     * @param ckanPort
     * @param orionUrl
     * @param ssl
     * @param maxConns
     * @param maxConnsPerRoute
     * @param ckanViewer
     */
    public CKANBackendImpl(String apiKey, String ckanHost, String ckanPort, String orionUrl,
            boolean ssl, int maxConns, int maxConnsPerRoute, String ckanViewer) {
        super(ckanHost, ckanPort, ssl, false, null, null, null, null, maxConns, maxConnsPerRoute);
        
        // this class attributes
        this.apiKey = apiKey;
        this.orionUrl = orionUrl;
        this.viewer = ckanViewer;
        
        // create the cache
        cache = new CKANCache(ckanHost, ckanPort, ssl, apiKey, maxConns, maxConnsPerRoute);
    } // CKANBackendImpl

    @Override
    public void persist(String orgName, String pkgName, String resName, String records, boolean createEnabled)
        throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError {
        LOGGER.debug("Going to lookup for the resource id, the cache may be updated during the process (orgName="
                + orgName + ", pkgName=" + pkgName + ", resName=" + resName + ")");
        String resId = resourceLookupOrCreate(orgName, pkgName, resName, createEnabled);
        
        if (resId == null) {
            throw new CygnusPersistenceError("Cannot persist the data (orgName=" + orgName + ", pkgName=" + pkgName
                    + ", resName=" + resName + ")");
        } else {
            LOGGER.debug("Going to persist the data (orgName=" + orgName + ", pkgName=" + pkgName
                    + ", resName/resId=" + resName + "/" + resId + ")");
            insert(resId, records);
        } // if else
    } // persist
    
    private String resourceLookupOrCreate(String orgName, String pkgName, String resName, boolean createEnabled)
        throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError {
        if (!cache.isCachedOrg(orgName)) {
            LOGGER.debug("The organization was not cached nor existed in CKAN (orgName=" + orgName + ")");
            
            if (createEnabled) {
                String orgId = createOrganization(orgName);
                cache.addOrg(orgName);
                cache.setOrgId(orgName, orgId);
                String pkgId = createPackage(pkgName, orgId);
                cache.addPkg(orgName, pkgName);
                cache.setPkgId(orgName, pkgName, pkgId);
                String resId = createResource(resName, pkgId);
                cache.addRes(orgName, pkgName, resName);
                cache.setResId(orgName, pkgName, resName, resId);
                createDataStore(resId);
                createView(resId);
                return resId;
            } else {
                return null;
            } // if else
        } // if
        
        LOGGER.debug("The organization was cached (orgName=" + orgName + ")");
        
        if (!cache.isCachedPkg(orgName, pkgName)) {
            LOGGER.debug("The package was not cached nor existed in CKAN (orgName=" + orgName + ", pkgName="
                    + pkgName + ")");
            
            if (createEnabled) {
                String pkgId = createPackage(pkgName, cache.getOrgId(orgName));
                cache.addPkg(orgName, pkgName);
                cache.setPkgId(orgName, pkgName, pkgId);
                String resId = createResource(resName, pkgId);
                cache.addRes(orgName, pkgName, resName);
                cache.setResId(orgName, pkgName, resName, resId);
                createDataStore(resId);
                createView(resId);
                return resId;
            } else {
                return null;
            } // if else
        } // if
        
        LOGGER.debug("The package was cached (orgName=" + orgName + ", pkgName=" + pkgName + ")");
        
        if (!cache.isCachedRes(orgName, pkgName, resName)) {
            LOGGER.debug("The resource was not cached nor existed in CKAN (orgName=" + orgName + ", pkgName="
                    + pkgName + ", resName=" + resName + ")");
            
            if (createEnabled) {
                String resId = this.createResource(resName, cache.getPkgId(orgName, pkgName));
                cache.addRes(orgName, pkgName, resName);
                cache.setResId(orgName, pkgName, resName, resId);
                createDataStore(resId);
                createView(resId);
                return resId;
            } else {
                return null;
            } // if else
        } // if
        
        LOGGER.debug("The resource was cached (orgName=" + orgName + ", pkgName=" + pkgName + ", resName="
                + resName + ")");
        
        return cache.getResId(orgName, pkgName, resName);
    } // resourceLookupOrCreate

    /**
     * Insert records in the datastore.
     * @param resId The resource in which datastore the record is going to be inserted
     * @param records Records to be inserted in Json format
     * @throws Exception
     */
    private void insert(String resId, String records) throws CygnusRuntimeError, CygnusPersistenceError {
        String jsonString = "{ \"resource_id\": \"" + resId
                    + "\", \"records\": [ " + records + " ], "
                    + "\"method\": \"insert\", "
                    + "\"force\": \"true\" }";
        
        // create the CKAN request URL
        String urlPath = "/api/3/action/datastore_upsert";

        // do the CKAN request
        JsonResponse res = doCKANRequest("POST", urlPath, jsonString);

        // check the status
        if (res.getStatusCode() == 200) {
            LOGGER.debug("Successful insert (resource/datastore id=" + resId + ")");
        } else {
            throw new CygnusPersistenceError("Could not insert (resId=" + resId + ", statusCode="
                    + res.getStatusCode() + ")");
        } // if else
    } // insert

    /**
     * Creates an organization in CKAN.
     * @param orgName Organization to be created
     * @throws Exception
     * @return The organization id
     */
    private String createOrganization(String orgName) throws CygnusRuntimeError, CygnusPersistenceError {
        // create the CKAN request JSON
        String jsonString = "{ \"name\": \"" + orgName + "\"}";

        // create the CKAN request URL
        String urlPath = "/api/3/action/organization_create";

        // do the CKAN request
        JsonResponse res = doCKANRequest("POST", urlPath, jsonString);

        // check the status
        if (res.getStatusCode() == 200) {
            String orgId = ((JSONObject) res.getJsonObject().get("result")).get("id").toString();
            LOGGER.debug("Successful organization creation (orgName/OrgId=" + orgName + "/" + orgId + ")");
            return orgId;
        } else {
            throw new CygnusPersistenceError("Could not create the orgnaization (orgName=" + orgName
                    + ", statusCode=" + res.getStatusCode() + ")");
        } // if else
    } // createOrganization

    /**
     * Creates a dataset/package within a given organization in CKAN.
     * @param pkgName Package to be created
     * @param orgId Organization the package belongs to
     * @return A package identifier if the package was created or an exception if something went wrong
     * @throws Exception
     */
    private String createPackage(String pkgName, String orgId) throws CygnusRuntimeError, CygnusPersistenceError {
        // create the CKAN request JSON
        String jsonString = "{ \"name\": \"" + pkgName + "\", " + "\"owner_org\": \"" + orgId + "\" }";

        // create the CKAN request URL
        String urlPath = "/api/3/action/package_create";

        // do the CKAN request
        JsonResponse res = doCKANRequest("POST", urlPath, jsonString);

        // check the status
        if (res.getStatusCode() == 200) {
            String packageId = ((JSONObject) res.getJsonObject().get("result")).get("id").toString();
            LOGGER.debug("Successful package creation (pkgName/pkgId=" + pkgName + "/" + packageId + ")");
            return packageId;
        /*
        This is not deleted if in the future we try to activate deleted elements again

        } else if (res.getStatusCode() == 409) {
            logger.debug("The package exists but its state is \"deleted\", activating it (pkgName="
                    + pkgName + ")");
            String packageId = activateElementState(httpClient, pkgName, "dataset");

            if (packageId != null) {
                logger.debug("Successful package activation (pkgId=" + packageId + ")");
                return packageId;
            } else {
                throw new CygnusRuntimeError("Could not activate the package (pkgId=" + pkgName + ")");
            } // if else
        */
        } else {
            throw new CygnusPersistenceError("Could not create the package (orgId=" + orgId
                    + ", pkgName=" + pkgName + ", statusCode=" + res.getStatusCode() + ")");
        } // if else
    } // createPackage

    /**
     * Creates a resource within a given package in CKAN.
     * @param resName Resource to be created
     * @param pkgId Package the resource belongs to
     * @return A resource identifier if the resource was created or an exception if something went wrong
     * @throws Exception
     */
    private String createResource(String resName, String pkgId) throws CygnusRuntimeError, CygnusPersistenceError {
        // create the CKAN request JSON
        String jsonString = "{ \"name\": \"" + resName + "\", "
                + "\"url\": \"none\", "
                + "\"format\": \"\", "
                + "\"package_id\": \"" + pkgId + "\" }";

        // create the CKAN request URL
        String urlPath = "/api/3/action/resource_create";

        // do the CKAN request
        JsonResponse res = doCKANRequest("POST", urlPath, jsonString);

        // check the status
        if (res.getStatusCode() == 200) {
            String resourceId = ((JSONObject) res.getJsonObject().get("result")).get("id").toString();
            LOGGER.debug("Successful resource creation (resName/resId=" + resName + "/" + resourceId
                    + ")");
            return resourceId;
        } else {
            throw new CygnusPersistenceError("Could not create the resource (pkgId=" + pkgId
                    + ", resName=" + resName + ", statusCode=" + res.getStatusCode() + ")");
        } // if else
    } // createResource

    /**
     * Creates a datastore for a given resource in CKAN.
     * @param resId Identifies the resource whose datastore is going to be created.
     * @throws Exception
     */
    private void createDataStore(String resId) throws CygnusRuntimeError, CygnusPersistenceError {
        // create the CKAN request JSON
        // CKAN types reference: http://docs.ckan.org/en/ckan-2.2/datastore.html#valid-types
        String jsonString = "{ \"resource_id\": \"" + resId
                + "\", \"fields\": [ "
                + "{ \"id\": \"" + CommonConstants.RECV_TIME_TS + "\", \"type\": \"int\"},"
                + "{ \"id\": \"" + CommonConstants.RECV_TIME + "\", \"type\": \"timestamp\"},"
                + "{ \"id\": \"" + CommonConstants.FIWARE_SERVICE_PATH + "\", \"type\": \"text\"},"
                + "{ \"id\": \"" + CommonConstants.ENTITY_ID + "\", \"type\": \"text\"},"
                + "{ \"id\": \"" + CommonConstants.ENTITY_TYPE + "\", \"type\": \"text\"},"
                + "{ \"id\": \"" + CommonConstants.ATTR_NAME + "\", \"type\": \"text\"},"
                + "{ \"id\": \"" + CommonConstants.ATTR_TYPE + "\", \"type\": \"text\"},"
                + "{ \"id\": \"" + CommonConstants.ATTR_VALUE + "\", \"type\": \"json\"},"
                + "{ \"id\": \"" + CommonConstants.ATTR_MD + "\", \"type\": \"json\"}"
                + "], "
                + "\"force\": \"true\" }";

        // create the CKAN request URL
        String urlPath = "/api/3/action/datastore_create";

        // do the CKAN request
        JsonResponse res = doCKANRequest("POST", urlPath, jsonString);

        // check the status
        if (res.getStatusCode() == 200) {
            LOGGER.debug("Successful datastore creation (resourceId=" + resId + ")");
        } else {
            throw new CygnusPersistenceError("Could not create the datastore (resId=" + resId
                    + ", statusCode=" + res.getStatusCode() + ")");
        } // if else
    } // createResource
    
    /**
     * Creates a view for a given resource in CKAN.
     * @param resId Identifies the resource whose view is going to be created.
     * @throws Exception
     */
    private void createView(String resId) throws CygnusRuntimeError, CygnusPersistenceError {
        if (!existsView(resId)) {
            // create the CKAN request JSON
            String jsonString = "{ \"resource_id\": \"" + resId + "\","
                    + "\"view_type\": \"" + viewer + "\","
                    + "\"title\": \"Recline grid view\" }";

            // create the CKAN request URL
            String urlPath = "/api/3/action/resource_view_create";

            // do the CKAN request
            JsonResponse res = doCKANRequest("POST", urlPath, jsonString);

            // check the status
            if (res.getStatusCode() == 200) {
                LOGGER.debug("Successful view creation (resourceId=" + resId + ")");
            } else {
                throw new CygnusPersistenceError("Could not create the datastore (resId=" + resId
                    + ", statusCode=" + res.getStatusCode() + ")");
            } // if else
        } // if
    } // createView
    
    private boolean existsView(String resId) throws CygnusRuntimeError, CygnusPersistenceError {
        // create the CKAN request JSON
        String jsonString = "{ \"id\": \"" + resId + "\" }";

        // create the CKAN request URL
        String urlPath = "/api/3/action/resource_view_list";

        // do the CKAN request
        JsonResponse res = doCKANRequest("POST", urlPath, jsonString);

        // check the status
        if (res.getStatusCode() == 200) {
            LOGGER.debug("Successful view listing (resourceId=" + resId + ")");
            return (((JSONArray) res.getJsonObject().get("result")).size() > 0);
        } else {
            throw new CygnusPersistenceError("Could not check if the view exists (resId=" + resId
                    + ", statusCode=" + res.getStatusCode() + ")");
        } // if else
    } // existsView

    private JSONObject getRecords(String resId, String filters, int offset, int limit)
        throws CygnusRuntimeError, CygnusPersistenceError {
        // create the CKAN request JSON
        String jsonString = "{\"id\": \"" + resId + "\",\"sort\":\"_id\",\"offset\":" + offset
                + ",\"limit\":" + limit;

        if (filters == null || filters.isEmpty()) {
            jsonString += "}";
        } else {
            jsonString += ",\"filters\":\"" + filters + "\"}";
        } // if else

        // create the CKAN request URL
        String urlPath = "/api/3/action/datastore_search";

        // do the CKAN request
        JsonResponse res = doCKANRequest("POST", urlPath, jsonString);

        // check the status
        if (res.getStatusCode() == 200) {
            LOGGER.debug("Successful search (resourceId=" + resId + ")");
            return res.getJsonObject();
        } else {
            throw new CygnusPersistenceError("Could not search for the records (resId=" + resId
                    + ", statusCode=" + res.getStatusCode() + ")");
        } // if else
    } // getRecords
    
    private void deleteRecords(String resId, String filters) throws CygnusRuntimeError, CygnusPersistenceError {
        // create the CKAN request JSON
        String jsonString = "{\"id\": \"" + resId + "\",\"force\":\"true\"";

        if (filters == null || filters.isEmpty()) {
            jsonString += "}";
        } else {
            jsonString += ",\"filters\":" + filters + "}";
        } // if else

        // create the CKAN request URL
        String urlPath = "/api/3/action/datastore_delete";

        // do the CKAN request
        JsonResponse res = doCKANRequest("POST", urlPath, jsonString);

        // check the status
        if (res.getStatusCode() == 200) {
            LOGGER.debug("Successful deletion (resourceId=" + resId + ")");
        } else {
            throw new CygnusPersistenceError("Could not delete the records (resId=" + resId
                    + ", statusCode=" + res.getStatusCode() + ")");
        } // if else
    } // deleteRecords
    
    @Override
    public void capRecords(String orgName, String pkgName, String resName, long maxRecords)
        throws CygnusRuntimeError, CygnusPersistenceError {
        // Get the resource ID by querying the cache
        String resId = cache.getResId(orgName, pkgName, resName);
        
        // Create the filters for a datastore deletion
        String filters = "";
        
        // Get the record pages, some variables
        int offset = 0;
        long toBeDeleted = 0;
        long alreadyDeleted = 0;
        
        do {
            // Get the number of records to be deleted
            JSONObject result = (JSONObject) getRecords(resId, null, offset, RECORDSPERPAGE).get("result");
            long total = (Long) result.get("total");
            toBeDeleted = total - maxRecords;
            
            if (toBeDeleted < 0) {
                break;
            } // if
            
            // Get how much records within the current page must be deleted
            long remaining = toBeDeleted - alreadyDeleted;
            long toBeDeletedNow = (remaining > RECORDSPERPAGE ? RECORDSPERPAGE : remaining);
            
            // Get the records to be deleted from the current page and get their ID
            JSONArray records = (JSONArray) result.get("records");

            for (int i = 0; i < toBeDeletedNow; i++) {
                long id = (Long) ((JSONObject) records.get(i)).get("_id");

                if (filters.isEmpty()) {
                    filters += "{\"_id\":[" + id;
                } else {
                    filters += "," + id;
                } // if else
            } // for
            
            // Updates
            alreadyDeleted += toBeDeletedNow;
            offset += RECORDSPERPAGE;
        } while (alreadyDeleted < toBeDeleted);
        
        if (filters.isEmpty()) {
            LOGGER.debug("No records to be deleted");
        } else {
            filters += "]}";
            LOGGER.debug("Records must be deleted (resId=" + resId + ", filters=" + filters + ")");
            deleteRecords(resId, filters);
        } // if else
    } // capRecords
    
    @Override
    public void expirateRecords(String orgName, String pkgName, String resName, long expirationTime)
        throws CygnusRuntimeError, CygnusPersistenceError {
        throw new UnsupportedOperationException("Not supported yet.");
    } // expirateRecords

    @Override
    public void expirateRecordsCache(long expirationTime) throws CygnusRuntimeError, CygnusPersistenceError {
        // Iterate on the cached resource IDs
        cache.startResIterator();
        
        while (cache.hasNextRes()) {
            // Get the next resource ID
            String resId = cache.getNextResId();
            
            // Create the filters for a datastore deletion
            String filters = "";

            // Get the record pages, some variables
            int offset = 0;
            boolean morePages = true;

            do {
                // Get the records within the current page
                JSONObject result = (JSONObject) getRecords(resId, null, offset, RECORDSPERPAGE).get("result");
                JSONArray records = (JSONArray) result.get("records");

                try {
                    for (Object recordObj : records) {
                        JSONObject record = (JSONObject) recordObj;
                        long id = (Long) record.get("_id");
                        String recvTime = (String) record.get("recvTime");
                        long recordTime = CommonUtils.getMilliseconds(recvTime);
                        long currentTime = new Date().getTime();

                        if (recordTime < (currentTime - (expirationTime * 1000))) {
                            if (filters.isEmpty()) {
                                filters += "{\"_id\":[" + id;
                            } else {
                                filters += "," + id;
                            } // if else
                        } else {
                            // Since records are sorted by _id, once the first not expirated record is found the loop
                            // can finish
                            morePages = false;
                            break;
                        } // if else
                    } // for
                } catch (ParseException e) {
                    throw new CygnusRuntimeError("Data expiration error", "ParseException", e.getMessage());
                } // try catch

                if (records.isEmpty()) {
                    morePages = false;
                } else {
                    offset += RECORDSPERPAGE;
                } // if else
            } while (morePages);
            
            if (filters.isEmpty()) {
                LOGGER.debug("No records to be deleted");
            } else {
                filters += "]}";
                LOGGER.debug("Records to be deleted, resId=" + resId + ", filters=" + filters);
                deleteRecords(resId, filters);
            } // if else
        } // while
    } // expirateRecordsCache
    
    /**
     * Sets the CKAN cache. This is protected since it is only used by the tests.
     * @param cache
     */
    protected void setCache(CKANCache cache) {
        this.cache = cache;
    } // setCache
    
    private JsonResponse doCKANRequest(String method, String urlPath, String jsonString) throws CygnusRuntimeError,
        CygnusPersistenceError {
        ArrayList<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Authorization", apiKey));
        headers.add(new BasicHeader("Content-Type", "application/json; charset=utf-8"));
        return doRequest(method, urlPath, true, headers, new StringEntity(jsonString, "UTF-8"));
    } // doCKANRequest

} // CKANBackendImpl
