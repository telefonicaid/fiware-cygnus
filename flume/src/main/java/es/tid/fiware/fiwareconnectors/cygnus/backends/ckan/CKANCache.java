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

import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusBadConfiguration;
import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusRuntimeError;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author frb
 */
public class CKANCache {
    
    private Logger logger;
    private CKANRequester requester;
    private HashMap<String, HashMap<String, ArrayList<String>>> tree; // this cache only contain human readable names
    private HashMap<String, String> orgMap; // this cache contains the translation from organization name to identifier
    private HashMap<String, String> pkgMap; // this cache contains the translation from package name to identifier
    private HashMap<String, String> resMap; // this cache contains the translation from resource name to identifier
    private String ckanVersion;
    
    /**
     * Constructor.
     */
    public CKANCache(CKANRequester ckanRequester) {
        logger = Logger.getLogger(CKANCache.class);
        this.requester = ckanRequester;
        tree = new HashMap<String, HashMap<String, ArrayList<String>>>();
        orgMap = new HashMap<String, String>();
        pkgMap = new HashMap<String, String>();
        resMap = new HashMap<String, String>();
        
        // get the CKAN version (just once)
        logger.debug("Going to get the CKAN version");
        
        try {
            ckanVersion = getCKANVersion();
        } catch (Exception e) {
            logger.error("Could not get the CKAN version. Details=" + e.getMessage());
        } // try catch
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
     * @param pkgName
     * @return
     */
    public String getPkgId(String pkgName) {
        return pkgMap.get(pkgName);
    } // getPkgId

    /**
     * Gets the resource id, given its name.
     * @param resName
     * @return
     */
    public String getResId(String resName) {
        return resMap.get(resName);
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
     * @param pkgName Package name
     * @param pkgId Package id
     */
    public void setPkgId(String pkgName, String pkgId) {
        pkgMap.put(pkgName, pkgId);
    } // setPkgId

    /**
     * Sets the resource id, given its name.
     * @param resName Resource name
     * @param resId Resource id
     */
    public void setResId(String resName, String resId) {
        resMap.put(resName, resId);
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
     * @throws Exception
     */
    public boolean isCachedOrg(String orgName) throws Exception {
        // check if the organization has already been cached
        if (tree.containsKey(orgName)) {
            logger.debug("Organization found in the cache (orgName=" + orgName + ")");
            return true;
        } // if
        
        logger.debug("Organization not found in the cache, querying CKAN for it (orgName=" + orgName + ")");
        
        // query CKAN for the organization information
        String ckanURL = "/api/3/action/organization_show?id=" + orgName;
        CKANResponse res = requester.doCKANRequest("GET", ckanURL);

        if (res.getStatusCode() == 200) {
            // the organization exists in CKAN
            JSONObject result = (JSONObject) res.getJsonObject().get("result");
                        
            // check if the organization is in "deleted" state
            String orgState = result.get("state").toString();
            
            if (orgState.equals("deleted")) {
                throw new CygnusBadConfiguration("The organization exists but it is in a deleted state (orgName="
                        + orgName + ")");
            } // if

            // put the organization in the tree and in the organization map
            String orgId = result.get("id").toString();
            tree.put(orgName, new HashMap<String, ArrayList<String>>());
            orgMap.put(orgName, orgId);
            logger.debug("Organization found in CKAN, now cached (orgName/orgId=" + orgName + "/" + orgId + ")");

            // get the packages and populate the packages map
            JSONArray packages = (JSONArray) result.get("packages");
            logger.debug("Going to populate the packages cache (orgName=" + orgName + ")");
            populatePackagesMap(packages, orgName);
            return true;
        } else if (res.getStatusCode() == 404) {
            return false;
        } else {
            throw new CygnusRuntimeError("Don't know how to treat response code " + res.getStatusCode() + ")");
        } // if else
    } // isCachedOrg
    
    /**
     * Checks if the package is cached. If not cached, CKAN is queried in order to update the cache.
     * This method assumes the given organization exists and it is cached.
     * @param orgName Organization name
     * @param pkgName Package name
     * @return True if the organization was cached, false otherwise
     * @throws Exception
     */
    public boolean isCachedPkg(String orgName, String pkgName) throws Exception {
        // check if the package has already been cached
        if (tree.get(orgName).containsKey(pkgName)) {
            logger.debug("Package found in the cache (orgName=" + orgName + ", pkgName=" + pkgName + ")");
            return true;
        } // if
        
        logger.debug("Package not found in the cache, querying CKAN for it (orgName=" + orgName + ", pkgName="
                + pkgName + ")");
        
        // query CKAN for the organization information
        String ckanURL = "/api/3/action/package_show?id=" + pkgName;
        CKANResponse res = requester.doCKANRequest("GET", ckanURL);

        if (res.getStatusCode() == 200) {
            // the package exists in CKAN
            JSONObject result = (JSONObject) res.getJsonObject().get("result");
                        
            // check if the package is in "deleted" state
            String pkgState = result.get("state").toString();
            
            if (pkgState.equals("deleted")) {
                throw new CygnusBadConfiguration("The package exists but it is in a deleted state (orgName=" + orgName
                        + ", pkgName=" + pkgName + ")");
            } // if

            // put the package in the tree and in the package map
            String pkgId = result.get("id").toString();
            tree.get(orgName).put(pkgName, new ArrayList<String>());
            orgMap.put(pkgName, pkgId);
            logger.debug("Package found in CKAN, now cached (orgName=" + orgName + ", pkgName/pkgId=" + pkgName + "/"
                    + pkgId + ")");

            // get the resource and populate the resource map
            JSONArray resources = (JSONArray) result.get("resources");
            logger.debug("Going to populate the resources cache (orgName=" + orgName + ", pkgName=" + pkgName + ")");
            populateResourcesMap(resources, orgName, pkgName, false);
            return true;
        } else if (res.getStatusCode() == 404) {
            return false;
        } else {
            throw new CygnusRuntimeError("Don't know how to treat response code " + res.getStatusCode() + ")");
        } // if else
    } // isCachedPkg
    
    /**
     * Checks if the resource is cached. If not cached, CKAN is queried in order to update the cache.
     * This method assumes the given organization and package exist and they are cached.
     * @param orgName Organization name
     * @param pkgName Package name
     * @param resName Resource name
     * @return True if the organization was cached, false otherwise
     * @throws Exception
     */
    public boolean isCachedRes(String orgName, String pkgName, String resName) throws Exception {
        // check if the resource has already been cached
        if (tree.get(orgName).get(pkgName).contains(resName)) {
            logger.debug("Resource found in the cache (orgName=" + orgName + ", pkgName=" + pkgName + ", resName="
                    + resName + ")");
            return true;
        } // if
        
        logger.debug("Resource not found in the cache, querying CKAN for the whole package containing it (orgName="
                + orgName + ", pkgName=" + pkgName + ", resName=" + resName + ")");
        
        // reached this point, we need to query CKAN about the resource, in order to know if it exists in CKAN
        // nevertheless, the CKAN API allows us to query for a certain resource by id, not by name...
        // the only solution seems to query for the whole package and check again
        // query CKAN for the organization information
        
        String ckanURL = "/api/3/action/package_show?id=" + pkgName;
        CKANResponse res = requester.doCKANRequest("GET", ckanURL);

        if (res.getStatusCode() == 200) {
            // the package exists in CKAN
            logger.debug("Package found in CKAN, going to update the cached resources (orgName=" + orgName
                    + ", pkgName=" + pkgName + ")");

            // there is no need to check if the package is in "deleted" state...

            // there is no need to put the package in the tree nor put it in the package map...

            // get the resource and populate the resource map
            JSONObject result = (JSONObject) res.getJsonObject().get("result");
            JSONArray resources = (JSONArray) result.get("resources");
            logger.debug("Going to populate the resources cache (orgName=" + orgName + ", pkgName=" + pkgName + ")");
            populateResourcesMap(resources, orgName, pkgName, true);
            return true;
        } else if (res.getStatusCode() == 404) {
            throw new CygnusRuntimeError("Unexpected package error when updating its resources... the package was "
                    + "supposed to exist!");
        } else {
            throw new CygnusRuntimeError("Don't know how to treat response code " + res.getStatusCode() + ")");
        } // if else
    } // isCachedRes

    /**
     * Populates the package map of a given orgName with the package information from the CKAN response.
     * @param packages JSON vector from the CKAN response containing package information
     * @param orgName Organization name
     * @throws Exception
     */
    private void populatePackagesMap(JSONArray packages, String orgName) throws Exception {
        // this check is for debuging purposes
        if (packages.size() == 0) {
            logger.debug("The pacakges list is empty, nothing to cache");
            return;
        } // if
        
        logger.debug("Packages to be populated: " + packages.toJSONString() + "(orgName=" + orgName + ")");

        // iterate on the packages
        Iterator<JSONObject> iterator = packages.iterator();
            
        while (iterator.hasNext()) {
            // get the package name
            JSONObject pkg = (JSONObject) iterator.next();
            String pkgName = (String) pkg.get("name");

            // check if the package is in "deleted" state
            String pkgState = pkg.get("state").toString();

            if (pkgState.equals("deleted")) {
                throw new CygnusBadConfiguration("The package exists but it is in a deleted state (orgName="
                        + orgName + ", pkgName=" + pkgName + ")");
            } // if
            
            // put the package in the tree and in the packages map
            String pkgId = pkg.get("id").toString();
            tree.get(orgName).put(pkgName, new ArrayList<String>());
            pkgMap.put(pkgName, pkgId);
            logger.debug("Package found in CKAN, now cached (orgName=" + orgName + " -> pkgName/pkgId=" + pkgName + "/"
                    + pkgId + ")");
            
            // get the resources
            JSONArray resources = null;

            // this piece of code tries to make the code compatible with CKAN 2.0, whose "organization_show"
            // method returns no resource lists for its packages! (not in CKAN 2.2)
            // more info --> https://github.com/telefonicaid/fiware-connectors/issues/153
            // if the resources list is null we must try to get it package by package
            if (ckanVersion.equals("2.0")) {
                logger.debug("CKAN version is 2.0, try to discover the resources for this package (pkgName="
                        + pkgName + ")");
                resources = discoverResources(pkgName);
            } else { // 2.2 or higher
                logger.debug("CKAN version is 2.2 (or higher), the resources list can be obtained from the "
                        + "organization information (pkgName=" + pkgName + ")");
                resources = (JSONArray) pkg.get("resources");
            } // if else

            // populate the resources map
            logger.debug("Going to populate the resources cache (orgName=" + orgName + ", pkgName=" + pkgName + ")");
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
        if (resources.size() == 0) {
            logger.debug("The resources list is empty, nothing to cache");
            return;
        } // if
        
        logger.debug("Resources to be populated: " + resources.toJSONString() + "(orgName=" + orgName + ", pkgName="
                + pkgName + ")");
        
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
            resMap.put(resourceName, resourceId);
            logger.debug("Resource found in CKAN, now cached (orgName=" + orgName + " -> pkgName=" + pkgName + " -> "
                    + "resourceName/resourceId=" + resourceName + "/" + resourceId + ")");
        } // while
    } // populateResourcesMap

    /**
     * This piece of code tries to make the code compatible with CKAN 2.0, whose "organization_show" method returns
     * no resource lists for its packages! (not in CKAN 2.2)
     * More info --> https://github.com/telefonicaid/fiware-connectors/issues/153
     * @param pkgName
     * @return The discovered resources for the given package.
     * @throws Exception
     */
    private JSONArray discoverResources(String pkgName) throws Exception {
        // query CKAN for the resources within the given package
        String urlPath = "/api/3/action/package_show?id=" + pkgName;
        CKANResponse res = requester.doCKANRequest("GET", urlPath);
        
        if (res.getStatusCode() == 200) {
            JSONObject result = (JSONObject) res.getJsonObject().get("result");
            JSONArray resources = (JSONArray) result.get("resources");
            logger.debug("Resources successfully discovered (pkgName=" + pkgName + ", numResources="
                    + resources.size() + ")");
            return resources;
        } else {
            throw new CygnusRuntimeError("Don't know how to treat response code " + res.getStatusCode() + ")");
        } // if else
    } // discoverResources
    
    /**
     * Gets the CKAN version.
     * @return The CKAN version
     * @throws Exception
     */
    private String getCKANVersion() throws Exception {
        String urlPath = "/api/util/status";
        CKANResponse res = requester.doCKANRequest("GET", urlPath);
        
        if (res.getStatusCode() == 200) {
            return res.getJsonObject().get("ckan_version").toString();
        } else {
            return null;
        } // if else
    } // getCKANVersion
    
    /**
     * Sets the organizations map. This is protected since it is only used by the tests.
     * @param orgMap
     */
    protected void setOrgMap(HashMap<String, String> orgMap) {
        this.orgMap = orgMap;
    } // setOrgMap

    /**
     * Sets the packages map. This is protected since it is only used by the tests.
     * @param requester
     */
    protected void setPkgMap(HashMap<String, String> pkgMap) {
        this.pkgMap = pkgMap;
    } // setPkgMap

    /**
     * Sets the resources map. This is protected since it is only used by the tests.
     * @param requester
     */
    protected void setResMap(HashMap<String, String> resMap) {
        this.resMap = resMap;
    } // setResMap
    
    /**
     * Sets the tree. This is protected since it is only used by the tests.
     * @param cache
     */
    protected void setTree(HashMap<String, HashMap<String, ArrayList<String>>> tree) {
        this.tree = tree;
    } // setTree
    
} // CKANCache
