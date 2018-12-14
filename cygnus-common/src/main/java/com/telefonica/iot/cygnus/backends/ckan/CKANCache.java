/**
 * Copyright 2015-1027 Telefonica Investigación y Desarrollo, S.A.U
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author frb
 */
public class CKANCache extends HttpBackend {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(CKANCache.class);
    private final String apiKey;
    private HashMap<String, HashMap<String, ArrayList<String>>> tree; // this cache only contain human readable names
    private HashMap<String, String> orgMap; // this cache contains the translation from organization name to identifier
    private HashMap<String, String> pkgMap; // this cache contains the translation from package name to identifier
    private HashMap<String, String> resMap; // this cache contains the translation from resource name to identifier
    private Iterator entries;
    private Entry nextEntry;
    
    /**
     * Constructor.
     * @param host
     * @param port
     * @param ssl
     * @param apiKey
     * @param maxConns
     * @param maxConnsPerRoute
     */
    public CKANCache(String host, String port, boolean ssl, String apiKey, int maxConns, int maxConnsPerRoute) {
        super(host, port, ssl, false, null, null, null, null, maxConns, maxConnsPerRoute);
        this.apiKey = apiKey;
        tree = new HashMap<>();
        orgMap = new HashMap<>();
        pkgMap = new HashMap<>();
        resMap = new HashMap<>();
        entries = null;
        nextEntry = null;
    } // CKANCache
    
    /**
     * Gets the organization id, given its name.
     * @param orgName
     * @return
     */
    public String getOrgId(String orgName) {
        return orgMap.get(orgName);
    } // getOrgId
    
    /**
     * Gets the package id, given its name.
     * @param orgName
     * @param pkgName
     * @return
     */
    public String getPkgId(String orgName, String pkgName) {
        return pkgMap.get(orgName + "_" + pkgName);
    } // getPkgId

    /**
     * Gets the resource id, given its name.
     * @param orgName
     * @param pkgName
     * @param resName
     * @return
     */
    public String getResId(String orgName, String pkgName, String resName) {
        return resMap.get(orgName + "_" + pkgName + "_" + resName);
    } // getResId

    /**
     * Sets the organization id, given its name.
     * @param orgName Organization name
     * @param orgId Organization id
     */
    public void setOrgId(String orgName, String orgId) {
        orgMap.put(orgName, orgId);
    } // setOrgId
    
    /**
     * Sets the package id, given its name.
     * @param orgName
     * @param pkgName Package name
     * @param pkgId Package id
     */
    public void setPkgId(String orgName, String pkgName, String pkgId) {
        pkgMap.put(orgName + "_" + pkgName, pkgId);
    } // setPkgId

    /**
     * Sets the resource id, given its name.
     * @param orgName
     * @param pkgName
     * @param resName Resource name
     * @param resId Resource id
     */
    public void setResId(String orgName, String pkgName, String resName, String resId) {
        resMap.put(orgName + "_" + pkgName + "_" + resName, resId);
    } // setResId
    
    /**
     * Adds an organization to the tree.
     * @param orgName
     */
    public void addOrg(String orgName) {
        tree.put(orgName, new HashMap<String, ArrayList<String>>());
    } // addOrg
    
    /**
     * Adds a package to the tree within a given package.
     * @param orgName
     * @param pkgName
     */
    public void addPkg(String orgName, String pkgName) {
        tree.get(orgName).put(pkgName, new ArrayList<String>());
    } // addPkg
    
    /**
     * Adds a resource to the tree within a given package within a given organization.
     * @param orgName
     * @param pkgName
     * @param resName
     */
    public void addRes(String orgName, String pkgName, String resName) {
        tree.get(orgName).get(pkgName).add(resName);
    } // addRes
    
    /**
     * Checks if the organization is cached. If not cached, CKAN is queried in order to update the cache.
     * @param orgName Organization name
     * @return True if the organization was cached, false otherwise
     * @throws CygnusBadConfiguration
     * @throws CygnusRuntimeError
     * @throws CygnusPersistenceError
     */
    public boolean isCachedOrg(String orgName)
        throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError {
        // check if the organization has already been cached
        if (tree.containsKey(orgName)) {
            LOGGER.debug("Organization found in the cache (orgName=" + orgName + ")");
            return true;
        } // if
        
        LOGGER.debug("Organization not found in the cache, querying CKAN for it (orgName=" + orgName + ")");
        
        // query CKAN for the organization information
        String ckanURL = "/api/3/action/organization_show?id=" + orgName + "&include_datasets=true";
        ArrayList<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Authorization", apiKey));
        JsonResponse res = doRequest("GET", ckanURL, true, headers, null);

        switch (res.getStatusCode()) {
            case 200:
                // the organization exists in CKAN
                JSONObject result = (JSONObject) res.getJsonObject().get("result");
                
                // check if the organization is in "deleted" state
                String orgState = result.get("state").toString();
                
                if (orgState.equals("deleted")) {
                    throw new CygnusBadConfiguration("The organization '" + orgName + "' exists but it is in a "
                            + "deleted state");
                } // if
                
                // put the organization in the tree and in the organization map
                String orgId = result.get("id").toString();
                tree.put(orgName, new HashMap<String, ArrayList<String>>());
                orgMap.put(orgName, orgId);
                LOGGER.debug("Organization found in CKAN, now cached (orgName/orgId=" + orgName + "/" + orgId + ")");
                
                // get the packages and populate the packages map
                JSONArray packages = (JSONArray) result.get("packages");
                LOGGER.debug("Going to populate the packages cache (orgName=" + orgName + ")");
                populatePackagesMap(packages, orgName);
                return true;
            case 404:
                return false;
            default:
                throw new CygnusPersistenceError("Could not check if the organization exists ("
                        + "orgName=" + orgName + ", statusCode=" + res.getStatusCode() + ")");
        } // switch
    } // isCachedOrg
    
    /**
     * Checks if the package is cached. If not cached, CKAN is queried in order to update the cache.
     * This method assumes the given organization exists and it is cached.
     * @param orgName Organization name
     * @param pkgName Package name
     * @return True if the organization was cached, false otherwise
     * @throws CygnusBadConfiguration
     * @throws CygnusRuntimeError
     * @throws CygnusPersistenceError
     */
    public boolean isCachedPkg(String orgName, String pkgName)
        throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError {
        // check if the package has already been cached
        if (tree.get(orgName).containsKey(pkgName)) {
            LOGGER.debug("Package found in the cache (orgName=" + orgName + ", pkgName=" + pkgName + ")");
            return true;
        } // if
        
        LOGGER.debug("Package not found in the cache, querying CKAN for it (orgName=" + orgName + ", pkgName="
                + pkgName + ")");
        
        // query CKAN for the package information
        String ckanURL = "/api/3/action/package_show?id=" + pkgName;
        ArrayList<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Authorization", apiKey));
        JsonResponse res = doRequest("GET", ckanURL, true, headers, null);

        switch (res.getStatusCode()) {
            case 200:
                // the package exists in CKAN
                JSONObject result = (JSONObject) res.getJsonObject().get("result");
                
                // check if the package is in "deleted" state
                String pkgState = result.get("state").toString();
                
                if (pkgState.equals("deleted")) {
                    throw new CygnusBadConfiguration("The package '" + pkgName + "' exists but it is in a "
                            + "deleted state");
                } // if
                
                // put the package in the tree and in the package map
                String pkgId = result.get("id").toString();
                tree.get(orgName).put(pkgName, new ArrayList<String>());
                orgMap.put(pkgName, pkgId);
                LOGGER.debug("Package found in CKAN, now cached (orgName=" + orgName + ", pkgName/pkgId=" + pkgName
                        + "/" + pkgId + ")");
                
                // get the resource and populate the resource map
                JSONArray resources = (JSONArray) result.get("resources");
                LOGGER.debug("Going to populate the resources cache (orgName=" + orgName + ", pkgName=" + pkgName
                        + ")");
                populateResourcesMap(resources, orgName, pkgName, false);
                return true;
            case 404:
                return false;
            default:
                throw new CygnusPersistenceError("Could not check if the package exists ("
                        + "orgName=" + orgName + ", pkgName=" + pkgName + ", statusCode=" + res.getStatusCode() + ")");
        } // switch
    } // isCachedPkg
    
    /**
     * Checks if the resource is cached. If not cached, CKAN is queried in order to update the cache.
     * This method assumes the given organization and package exist and they are cached.
     * @param orgName Organization name
     * @param pkgName Package name
     * @param resName Resource name
     * @return True if the organization was cached, false otherwise
     * @throws CygnusBadConfiguration
     * @throws CygnusRuntimeError
     * @throws CygnusPersistenceError
     */
    public boolean isCachedRes(String orgName, String pkgName, String resName)
        throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError {
        // check if the resource has already been cached
        if (tree.get(orgName).get(pkgName).contains(resName)) {
            LOGGER.debug("Resource found in the cache (orgName=" + orgName + ", pkgName=" + pkgName + ", resName="
                    + resName + ")");
            return true;
        } // if
        
        LOGGER.debug("Resource not found in the cache, querying CKAN for the whole package containing it "
                + "(orgName=" + orgName + ", pkgName=" + pkgName + ", resName=" + resName + ")");
        
        // reached this point, we need to query CKAN about the resource, in order to know if it exists in CKAN
        // nevertheless, the CKAN API allows us to query for a certain resource by id, not by name...
        // the only solution seems to query for the whole package and check again
        // query CKAN for the organization information
        
        String ckanURL = "/api/3/action/package_show?id=" + pkgName;
        ArrayList<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Authorization", apiKey));
        JsonResponse res = doRequest("GET", ckanURL, true, headers, null);

        switch (res.getStatusCode()) {
            case 200:
                // the package exists in CKAN
                LOGGER.debug("Package found in CKAN, going to update the cached resources (orgName=" + orgName
                        + ", pkgName=" + pkgName + ")");
                
                // there is no need to check if the package is in "deleted" state...
                
                // there is no need to put the package in the tree nor put it in the package map...
                
                // get the resource and populate the resource map
                JSONObject result = (JSONObject) res.getJsonObject().get("result");
                JSONArray resources = (JSONArray) result.get("resources");
                
                if (resources.isEmpty()) {
                    return false;
                } else {
                    LOGGER.debug("Going to populate the resources cache (orgName=" + orgName + ", pkgName=" + pkgName
                            + ")");
                    populateResourcesMap(resources, orgName, pkgName, true);
                    
                    // check if the resource is within the resources cache, once populated
                    if (tree.get(orgName).get(pkgName).contains(resName)) {
                        LOGGER.debug("Resource found in the cache, once queried CKAN " + "(orgName=" + orgName
                                + ", pkgName=" + pkgName + ", resName=" + resName + ")");
                        return true;
                    } else {
                        LOGGER.debug("Resource not found in the cache, once queried CKAN " + "(orgName=" + orgName
                                + ", pkgName=" + pkgName + ", resName=" + resName + ")");
                        return false;
                    } // if else
                } // if else
            case 404:
                return false;
            default:
                throw new CygnusPersistenceError("Could not check if the resource exists ("
                        + "orgName=" + orgName + ", pkgName=" + pkgName + ", resName=" + resName
                        + ", statusCode=" + res.getStatusCode() + ")");
        } // switch
    } // isCachedRes

    /**
     * Populates the package map of a given orgName with the package information from the CKAN response.
     * @param packages JSON vector from the CKAN response containing package information
     * @param orgName Organization name
     * @throws CygnusBadConfiguration
     * @throws CygnusRuntimeError
     * @throws CygnusPersistenceError
     */
    private void populatePackagesMap(JSONArray packages, String orgName)
        throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError {
        // this check is for debuging purposes
        if (packages == null || packages.isEmpty()) {
            LOGGER.debug("The pacakges list is empty, nothing to cache");
            return;
        } // if
        
        LOGGER.debug("Packages to be populated: " + packages.toJSONString() + "(orgName=" + orgName + ")");

        // iterate on the packages
        Iterator<JSONObject> iterator = packages.iterator();
            
        while (iterator.hasNext()) {
            // get the package name
            JSONObject pkg = (JSONObject) iterator.next();
            String pkgName = (String) pkg.get("name");

            // check if the package is in "deleted" state
            String pkgState = pkg.get("state").toString();

            if (pkgState.equals("deleted")) {
                throw new CygnusBadConfiguration("The package '" + pkgName + "' exists but it is in a deleted state");
            } // if
            
            // put the package in the tree and in the packages map
            String pkgId = pkg.get("id").toString();
            tree.get(orgName).put(pkgName, new ArrayList<String>());
            this.setPkgId(orgName, pkgName, pkgId);
            LOGGER.debug("Package found in CKAN, now cached (orgName=" + orgName + " -> pkgName/pkgId=" + pkgName
                    + "/" + pkgId + ")");
            
            // from CKAN 2.4, the organization_show method does not return the per-package list of resources
            JSONArray resources = getResources(pkgName);

            // populate the resources map
            LOGGER.debug("Going to populate the resources cache (orgName=" + orgName + ", pkgName=" + pkgName
                    + ")");
            populateResourcesMap(resources, orgName, pkgName, false);
        } // while
    } // populatePackagesMap
    
    /**
     * Populates the resourceName-resource map of a given orgName with the package information from the CKAN response.
     * @param resources JSON vector from the CKAN response containing resource information
     * @param orgName Organization name
     * @param pkgName Package name
     * @param checkExistence If true, checks if the queried resource already exists in the cache
     */
    private void populateResourcesMap(JSONArray resources, String orgName, String pkgName, boolean checkExistence) {
        // this check is for debuging purposes
        if (resources == null || resources.isEmpty()) {
            LOGGER.debug("The resources list is empty, nothing to cache");
            return;
        } // if
        
        LOGGER.debug("Resources to be populated: " + resources.toJSONString() + "(orgName=" + orgName
                + ", pkgName=" + pkgName + ")");
        
        // iterate on the resources
        Iterator<JSONObject> iterator = resources.iterator();
        
        while (iterator.hasNext()) {
            // get the resource name and id (resources cannot be in deleted state)
            JSONObject factObj = (JSONObject) iterator.next();
            String resourceName = (String) factObj.get("name");
            String resourceId = (String) factObj.get("id");

            // put the resource in the tree and in the resource map
            if (checkExistence) {
                if (tree.get(orgName).get(pkgName).contains(resourceName)) {
                    continue;
                } // if
            } // if
            
            tree.get(orgName).get(pkgName).add(resourceName);
            this.setResId(orgName, pkgName, resourceName, resourceId);
            LOGGER.debug("Resource found in CKAN, now cached (orgName=" + orgName + " -> pkgName=" + pkgName
                    + " -> " + "resourceName/resourceId=" + resourceName + "/" + resourceId + ")");
        } // while
    } // populateResourcesMap
    
    private JSONArray getResources(String pkgName) throws CygnusRuntimeError, CygnusPersistenceError {
        LOGGER.debug("Going to get the resources list from package " + pkgName);
        String ckanURL = "/api/3/action/package_show?id=" + pkgName;
        ArrayList<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Authorization", apiKey));
        JsonResponse res = doRequest("GET", ckanURL, true, headers, null);

        if (res.getStatusCode() == 200) {
            // get the resource and populate the resource map
            JSONObject result = (JSONObject) res.getJsonObject().get("result");
            JSONArray resources = (JSONArray) result.get("resources");
            return resources;
        } else {
            return null;
        } // if else
    } // getResources
    
    /**
     * Sets the organizations map. This is protected since it is only used by the tests.
     * @param orgMap
     */
    protected void setOrgMap(HashMap<String, String> orgMap) {
        this.orgMap = orgMap;
    } // setOrgMap

    /**
     * Sets the packages map. This is protected since it is only used by the tests.
     * @param pkgMap
     */
    protected void setPkgMap(HashMap<String, String> pkgMap) {
        this.pkgMap = pkgMap;
    } // setPkgMap

    /**
     * Sets the resources map. This is protected since it is only used by the tests.
     * @param resMap
     */
    protected void setResMap(HashMap<String, String> resMap) {
        this.resMap = resMap;
    } // setResMap
    
    /**
     * Sets the tree. This is protected since it is only used by the tests.
     * @param tree
     */
    protected void setTree(HashMap<String, HashMap<String, ArrayList<String>>> tree) {
        this.tree = tree;
    } // setTree
    
    /**
     * Starts an iterator for the resource IDs.
     */
    public void startResIterator() {
        entries = resMap.entrySet().iterator();
    } // getIterator

    /**
     * Return if there is another element to iterate.
     * @return True if there is another element to iterare, false otherwise. Internally, gets stores a pointer to the
     * next entry
     */
    public boolean hasNextRes() {
        if (entries.hasNext()) {
            nextEntry = (Entry) entries.next();
            return true;
        } else {
            return false;
        } // if else
    } // hasNextRes
    
    /**
     * Gets the next resource ID.
     * @return The next resource ID
     */
    public String getNextResId() {
        return (String) nextEntry.getValue();
    } // getNextResId
    
} // CKANCache
