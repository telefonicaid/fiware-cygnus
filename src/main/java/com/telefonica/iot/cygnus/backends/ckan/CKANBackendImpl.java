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

import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.backends.http.HttpBackend;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import java.util.ArrayList;
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
    private final String orionUrl;
    private final String apiKey;
    private CKANCache cache;

    /**
     * Constructor.
     * @param apiKey
     * @param ckanHost
     * @param ckanPort
     * @param orionUrl
     * @param ssl
     */
    public CKANBackendImpl(String apiKey, String ckanHost, String ckanPort, String orionUrl,
            boolean ssl) {
        super(new String[]{ckanHost}, ckanPort, ssl, false, null, null, null, null);
        
        // this class attributes
        this.apiKey = apiKey;
        this.orionUrl = orionUrl;
        
        // create the cache
        cache = new CKANCache(new String[]{ckanHost}, ckanPort, ssl, apiKey);
    } // CKANBackendImpl

    @Override
    public void persist(String orgName, String pkgName, String resName, String records, boolean createEnabled)
        throws Exception {
        LOGGER.debug("Going to lookup for the resource id, the cache may be updated during the process (orgName="
                + orgName + ", pkgName=" + pkgName + ", resName=" + resName + ")");
        String resId = resourceLookupOrCreate(orgName, pkgName, resName, createEnabled);
        
        if (resId == null) {
            throw new CygnusRuntimeError("Cannot persist the data (orgName=" + orgName + ", pkgName=" + pkgName
                    + ", resName=" + resName + ")");
        } else {
            LOGGER.debug("Going to persist the data (orgName=" + orgName + ", pkgName=" + pkgName
                    + ", resName/resId=" + resName + "/" + resId + ")");
            insert(resId, records);
        } // if else
    } // persist
    
    private String resourceLookupOrCreate(String orgName, String pkgName, String resName, boolean createEnabled)
        throws Exception {
        if (!cache.isCachedOrg(orgName)) {
            LOGGER.debug("The organization was not cached nor existed in CKAN (orgName=" + orgName + ")");
            
            if (createEnabled) {
                String orgId = createOrganization(orgName);
                cache.addOrg(orgName);
                cache.setOrgId(orgName, orgId);
                String pkgId = createPackage(pkgName, orgId);
                cache.addPkg(orgName, pkgName);
                cache.setPkgId(pkgName, pkgId);
                String resId = createResource(resName, pkgId);
                cache.addRes(orgName, pkgName, resName);
                cache.setResId(resName, resId);
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
                cache.setPkgId(pkgName, pkgId);
                String resId = createResource(resName, pkgId);
                cache.addRes(orgName, pkgName, resName);
                cache.setResId(resName, resId);
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
                String resId = this.createResource(resName, cache.getPkgId(pkgName));
                cache.addRes(orgName, pkgName, resName);
                cache.setResId(resName, resId);
                createDataStore(resId);
                createView(resId);
                return resId;
            } else {
                return null;
            } // if else
        } // if
        
        LOGGER.debug("The resource was cached (orgName=" + orgName + ", pkgName=" + pkgName + ", resName="
                + resName + ")");
        
        return cache.getResId(resName);
    } // resourceLookupOrCreate

    /**
     * Insert records in the datastore.
     * @param resId The resource in which datastore the record is going to be inserted
     * @param records Records to be inserted in Json format
     * @throws Exception
     */
    private void insert(String resId, String records) throws Exception {
        String jsonString = "{ \"resource_id\": \"" + resId
                    + "\", \"records\": [ " + records + " ], "
                    + "\"method\": \"insert\", "
                    + "\"force\": \"true\" }";
        String urlPath;
        
        try {
            // create the CKAN request URL
            urlPath = "/api/3/action/datastore_upsert";
        
            // do the CKAN request
            JsonResponse res = doCKANRequest("POST", urlPath, jsonString);
            
            // check the status
            if (res.getStatusCode() == 200) {
                LOGGER.debug("Successful insert (resource/datastore id=" + resId + ")");
            } else {
                throw new CygnusRuntimeError("Don't know how to treat response code " + res.getStatusCode());
            } // if else
        } catch (Exception e) {
            if (e instanceof CygnusRuntimeError
                    || e instanceof CygnusPersistenceError
                    || e instanceof CygnusBadConfiguration) {
                throw e;
            } else {
                throw new CygnusRuntimeError(e.getMessage());
            } // if else
        } // try catch
    } // insert

    /**
     * Creates an organization in CKAN.
     * @param orgName Organization to be created
     * @throws Exception
     * @return The organization id
     */
    private String createOrganization(String orgName) throws Exception {
        try {
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
                throw new CygnusRuntimeError("Don't know how to treat the response code. Possibly the organization "
                        + "already exists in a deleted state (respCode=" + res.getStatusCode() + ", orgName="
                        + orgName + ")");
            } // if else if else
        } catch (Exception e) {
            if (e instanceof CygnusRuntimeError
                    || e instanceof CygnusPersistenceError
                    || e instanceof CygnusBadConfiguration) {
                throw e;
            } else {
                throw new CygnusRuntimeError(e.getMessage());
            } // if else
        } // try catch
    } // createOrganization

    /**
     * Creates a dataset/package within a given organization in CKAN.
     * @param pkgName Package to be created
     * @param orgId Organization the package belongs to
     * @return A package identifier if the package was created or an exception if something went wrong
     * @throws Exception
     */
    private String createPackage(String pkgName, String orgId) throws Exception {
        try {
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
                throw new CygnusRuntimeError("Don't know how to treat the response code. Possibly the package "
                        + "already exists in a deleted state (respCode=" + res.getStatusCode() + ", pkgName="
                        + pkgName + ")");
            } // if else if else
        } catch (Exception e) {
            if (e instanceof CygnusRuntimeError
                    || e instanceof CygnusPersistenceError
                    || e instanceof CygnusBadConfiguration) {
                throw e;
            } else {
                throw new CygnusRuntimeError(e.getMessage());
            } // if else
        } // try catch
    } // createPackage

    /**
     * Creates a resource within a given package in CKAN.
     * @param resourceName Resource to be created
     * @param pkgId Package the resource belongs to
     * @return A resource identifier if the resource was created or an exception if something went wrong
     * @throws Exception
     */
    private String createResource(String resourceName, String pkgId) throws Exception {
        try {
            // create the CKAN request JSON
            String jsonString = "{ \"name\": \"" + resourceName + "\", "
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
                LOGGER.debug("Successful resource creation (resName/resId=" + resourceName + "/" + resourceId
                        + ")");
                return resourceId;
            } else {
                throw new CygnusRuntimeError("Don't know how to treat the response code. Possibly the resource "
                        + "already exists (respCode=" + res.getStatusCode() + ", resourceName=" + resourceName + ")");
            } // if else if else
        } catch (Exception e) {
            if (e instanceof CygnusRuntimeError
                    || e instanceof CygnusPersistenceError
                    || e instanceof CygnusBadConfiguration) {
                throw e;
            } else {
                throw new CygnusRuntimeError(e.getMessage());
            } // if else
        } // try catch
    } // createResource

    /**
     * Creates a datastore for a given resource in CKAN.
     * @param resId Identifies the resource whose datastore is going to be created.
     * @throws Exception
     */
    private void createDataStore(String resourceId) throws Exception {
        try {
            // create the CKAN request JSON
            // CKAN types reference: http://docs.ckan.org/en/ckan-2.2/datastore.html#valid-types
            String jsonString = "{ \"resource_id\": \"" + resourceId
                    + "\", \"fields\": [ "
                    + "{ \"id\": \"" + Constants.RECV_TIME_TS + "\", \"type\": \"int\"},"
                    + "{ \"id\": \"" + Constants.RECV_TIME + "\", \"type\": \"timestamp\"},"
                    + "{ \"id\": \"" + Constants.FIWARE_SERVICE_PATH + "\", \"type\": \"text\"},"
                    + "{ \"id\": \"" + Constants.ENTITY_ID + "\", \"type\": \"text\"},"
                    + "{ \"id\": \"" + Constants.ENTITY_TYPE + "\", \"type\": \"text\"},"
                    + "{ \"id\": \"" + Constants.ATTR_NAME + "\", \"type\": \"text\"},"
                    + "{ \"id\": \"" + Constants.ATTR_TYPE + "\", \"type\": \"text\"},"
                    + "{ \"id\": \"" + Constants.ATTR_VALUE + "\", \"type\": \"json\"},"
                    + "{ \"id\": \"" + Constants.ATTR_MD + "\", \"type\": \"json\"}"
                    + "], "
                    + "\"force\": \"true\" }";
            
            // create the CKAN request URL
            String urlPath = "/api/3/action/datastore_create";
            
            // do the CKAN request
            JsonResponse res = doCKANRequest("POST", urlPath, jsonString);

            // check the status
            if (res.getStatusCode() == 200) {
                LOGGER.debug("Successful datastore creation (resourceId=" + resourceId + ")");
            } else {
                throw new CygnusRuntimeError("Don't know how to treat the response code. Possibly the datastore "
                        + "already exists (respCode=" + res.getStatusCode() + ", resourceId=" + resourceId + ")");
            } // if else if else
        } catch (Exception e) {
            if (e instanceof CygnusRuntimeError
                    || e instanceof CygnusPersistenceError
                    || e instanceof CygnusBadConfiguration) {
                throw e;
            } else {
                throw new CygnusRuntimeError(e.getMessage());
            } // if else
        } // try catch
    } // createResource
    
    /**
     * Creates a view for a given resource in CKAN.
     * @param resId Identifies the resource whose view is going to be created.
     * @throws Exception
     */
    private void createView(String resourceId) throws Exception {
        if (!existsView(resourceId)) {
            try {
                // create the CKAN request JSON
                String jsonString = "{ \"resource_id\": \"" + resourceId + "\","
                        + "\"view_type\": \"recline_grid_view\","
                        + "\"title\": \"Recline grid view\" }";

                // create the CKAN request URL
                String urlPath = "/api/3/action/resource_view_create";

                // do the CKAN request
                JsonResponse res = doCKANRequest("POST", urlPath, jsonString);

                // check the status
                if (res.getStatusCode() == 200) {
                    LOGGER.debug("Successful view creation (resourceId=" + resourceId + ")");
                } else {
                    throw new CygnusRuntimeError("Don't know how to treat the response code. Possibly the datastore "
                            + "already exists (respCode=" + res.getStatusCode() + ", resourceId=" + resourceId + ")");
                } // if else if else
            } catch (Exception e) {
                if (e instanceof CygnusRuntimeError
                        || e instanceof CygnusPersistenceError
                        || e instanceof CygnusBadConfiguration) {
                    throw e;
                } else {
                    throw new CygnusRuntimeError(e.getMessage());
                } // if else
            } // try catch
        } // if
    } // createView
    
    private boolean existsView(String resourceId) throws Exception {
        try {
            // create the CKAN request JSON
            String jsonString = "{ \"id\": \"" + resourceId + "\" }";

            // create the CKAN request URL
            String urlPath = "/api/3/action/resource_view_list";

            // do the CKAN request
            JsonResponse res = doCKANRequest("POST", urlPath, jsonString);

            // check the status
            if (res.getStatusCode() == 200) {
                LOGGER.debug("Successful view listing (resourceId=" + resourceId + ")");
                return (((JSONArray) res.getJsonObject().get("result")).size() > 0);
            } else {
                throw new CygnusRuntimeError("Don't know how to treat the response code. Possibly the datastore "
                        + "already exists (respCode=" + res.getStatusCode() + ", resourceId=" + resourceId + ")");
            } // if else if else
        } catch (Exception e) {
            if (e instanceof CygnusRuntimeError
                    || e instanceof CygnusPersistenceError
                    || e instanceof CygnusBadConfiguration) {
                throw e;
            } else {
                throw new CygnusRuntimeError(e.getMessage());
            } // if else
        } // try catch
    } // existsView
    
    /**
     * Sets the CKAN cache. This is protected since it is only used by the tests.
     * @param cache
     */
    protected void setCache(CKANCache cache) {
        this.cache = cache;
    } // setCache
    
    private JsonResponse doCKANRequest(String method, String urlPath, String jsonString) throws Exception {
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", apiKey));
        return doRequest(method, urlPath, true, headers, new StringEntity(jsonString));
    } // doCKANRequest

} // CKANBackendImpl
