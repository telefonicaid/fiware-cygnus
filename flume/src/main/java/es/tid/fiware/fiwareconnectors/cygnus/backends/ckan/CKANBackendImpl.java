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
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * francisco.romerobueno@telefonica.com
 */

package es.tid.fiware.fiwareconnectors.cygnus.backends.ckan;

import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusBadConfiguration;
import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusPersistenceError;
import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusRuntimeError;
import es.tid.fiware.fiwareconnectors.cygnus.http.HttpClientFactory;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.http.client.HttpClient;

/**
 * Interface for those backends implementing the persistence in CKAN.
 *
 * @author fermin
 */
public class CKANBackendImpl implements CKANBackend {

    private Logger logger;
    private String apiKey;
    private String ckanHost;
    private String ckanPort;
    private String defaultDataset;
    private String orionUrl;
    private boolean ssl;
    private HttpClientFactory httpClientFactory;
    private HttpClient httpClient;

    // this map implements the f(NGSIentity, Org) = CKANresourceId function
    private HashMap<OrgResourcePair, String> resourceIds;

    // this map implements the f(Org) = defaultPackageId function
    private HashMap<String, String> packagesIds;

    // Note that the usage of maps as "cache" for organizations, packages/datasets and resources assumes
    // that once we discover/create some of the elements no other actor is going to delete it or change
    // in a way the cache becomes invalid. An improved version of this backend will implement mechanism
    // to manage "cache fails" and cache updates in that case

    /**
     * Constructor.
     * @param apiKey
     * @param ckanHost
     * @param ckanPort
     * @param defaultDataset
     * @param orionUrl
     * @param ssl
     */
    public CKANBackendImpl(String apiKey, String ckanHost, String ckanPort, String defaultDataset, String orionUrl,
            boolean ssl) {
        // this class attributes
        this.apiKey = apiKey;
        this.ckanHost = ckanHost;
        this.ckanPort = ckanPort;
        this.defaultDataset = defaultDataset;
        this.orionUrl = orionUrl;
        this.ssl = ssl;
        resourceIds = new HashMap<OrgResourcePair, String>();
        packagesIds = new HashMap<String, String>();
        
        // create a Http clients factory and an initial connection
        httpClientFactory = new HttpClientFactory(ssl);
        httpClient = httpClientFactory.getHttpClient(ssl);
        
        // logger
        logger = Logger.getLogger(CKANBackendImpl.class);
    } // CKANBackendImpl

    @Override
    public void initOrg(String orgName) throws Exception {
        // check if the organization has already been initialized
        if (packagesIds.containsKey(orgName)) {
            logger.debug("Organization found in the cache, thus it is already initialized (orgName=" + orgName + ")");
            return;
        } else {
            logger.debug("Organization not found in the cache, querying CKAN for it (orgName=" + orgName + ")");
        } // if else

        // query CKAN for the organization information
        String ckanURL = (ssl ? "https://" : "http://") + ckanHost + ":" + ckanPort
                + "/api/3/action/organization_show?id=" + orgName;
        CKANResponse res = doCKANRequest("GET", ckanURL);

        if (res.getStatusCode() == 200) {
            // orgName exists within CKAN
            JSONObject result = (JSONObject) res.getJsonObject().get("result");
            
            // check if the organization is in "deleted" state
            String orgState = result.get("state").toString();
            
            if (orgState.equals("deleted")) {
                throw new CygnusBadConfiguration("The organization exists but it is in a deleted state (orgName="
                        + orgName + ")");
            } // if
            
            String orgId = result.get("id").toString();
            logger.debug("Organization found (orgName=" + orgName + ", orgId=" + orgId + ")");

            // create the effective default package and check its validity
            String effectiveDefaultDataset = orgName + "_" + defaultDataset;
            
            if (effectiveDefaultDataset.length() > Constants.CKAN_PKG_MAX_LEN) {
                logger.error("Bad configuration (A CKAN package/dataset name '" + effectiveDefaultDataset + "' has "
                        + "been built and its length is greater than " + Constants.CKAN_PKG_MAX_LEN + ". This package "
                        + "name generation is based on the concatenation of the notified '" + Constants.HEADER_SERVICE
                        + "' organization header, the character '_' and the 'default_dataset' configuration parameter, "
                        + "thus adjust them)");
                throw new Exception("The lenght of the CKAN package/dataset '" + effectiveDefaultDataset
                        + "' is greater than " + Constants.CKAN_PKG_MAX_LEN);
            } // if
            
            // check if orgName contains the default package
            JSONArray packages = (JSONArray) result.get("packages");
            logger.debug("Going to iterate on the packages looking for the default one (orgName=" + orgName
                    + ", defaultPkgName=" + effectiveDefaultDataset + ")");
            Iterator<JSONObject> iterator = packages.iterator();
            
            while (iterator.hasNext()) {
                JSONObject pkg = (JSONObject) iterator.next();
                String pkgName = (String) pkg.get("name");
                
                if (pkgName.equals(effectiveDefaultDataset)) {
                    // check if the organization is in "deleted" state
                    String pkgState = pkg.get("state").toString();

                    if (pkgState.equals("deleted")) {
                        throw new CygnusBadConfiguration("The package exists but it is in a deleted state (orgName="
                                + orgName + ", pkgName=" + pkgName + ")");
                    } // if
                    
                    String pkgId = pkg.get("id").toString();
                    logger.debug("Default package found (orgName=" + orgName + ", defaultPkgName="
                            + effectiveDefaultDataset + ", defaultPkgId= " + pkgId + ")");
                    JSONArray resources = null;
                    
                    // this piece of code tries to make the code compatible with CKAN 2.0, whose "organization_show"
                    // method returns no resource lists for its packages! (not in CKAN 2.2)
                    // more info --> https://github.com/telefonicaid/fiware-connectors/issues/153
                    // if the resources list is null we must try to get it package by package
                    logger.debug("Going to get the CKAN version");
                    String ckanVersion = getCKANVersion();
                    
                    if (ckanVersion.equals("2.0")) {
                        logger.debug("CKAN version is 2.0, try to discover the resources for this package (pkgName="
                                + pkgName + ")");
                        resources = discoverResources(pkgName);
                    } else { // 2.2 or higher
                        logger.debug("CKAN version is 2.2 (or higher), the resources list can be obtained from the "
                                + "organization information (pkgName=" + pkgName + ")");
                        resources = (JSONArray) pkg.get("resources");
                    } // if else
                    
                    logger.debug("Going to populate the resources cache (orgName=" + orgName + ")");
                    populateResourcesMap(resources, orgName);
                    packagesIds.put(orgName, pkgId);
                    logger.debug("Default package added to pckages map (orgName=" + orgName + " -> defaultPkgId="
                            + pkgId + ")");
                    return;
                } // if
                // it seems the other packages are not of interest for Cygnus
            } // while

            // if we have reach this point, then orgName doesn't include the default package; thus create is
            logger.debug("Default package not found, going to create it (orgName=" + orgName + ", defaultPkgName="
                    + effectiveDefaultDataset + ")");
            String packageId = createPackage(effectiveDefaultDataset, orgId);
            packagesIds.put(orgName, packageId);
            logger.debug("Default package added to pckages map (orgName=" + orgName + " -> defaultPkgId="
                            + packageId + ")");
        } else if (res.getStatusCode() == 404) {
            // orgName doesn't exist in CKAN, create it
            createOrganization(orgName);
        } else {
            throw new CygnusRuntimeError("Don't know how to treat response code " + res.getStatusCode() + ")");
        } // if else
    } // initOrg
    
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
        String ckanURL = (ssl ? "https://" : "http://") + ckanHost + ":" + ckanPort + "/api/3/action/package_show?id="
                + pkgName;
        CKANResponse res = doCKANRequest("GET", ckanURL);
        
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
     * Populates the entity-resource map of a given orgName with the package information from the CKAN response.
     * @param resources JSON vector from the CKAN response containing resource information.
     * @param orgName
     */
    private void populateResourcesMap(JSONArray resources, String orgName) {
        if (resources.size() == 0) {
            logger.debug("The resources list is empty, nothing to cache");
            return;
        } // if
        
        Iterator<JSONObject> iterator = resources.iterator();
        
        while (iterator.hasNext()) {
            JSONObject factObj = (JSONObject) iterator.next();
            String resourceId = (String) factObj.get("id");
            String resourceName = (String) factObj.get("name");
            OrgResourcePair orgResourcePair = new OrgResourcePair(orgName, resourceName);
            resourceIds.put(orgResourcePair, resourceId);
            logger.debug("Resource added to resources map (<orgName,resourceName>=" + orgResourcePair
                    + " -> resourceId=" + resourceId + ")");
        } // while
    } // populateResourcesMap

    @Override
    public void persist(long recvTimeTs, String recvTime, String orgName, String resourceName, String attrName,
                String attrType, String attrValue, String attrMd) throws Exception {
        // try to get the resource identifier from the cache
        String resourceId = resourceLookupAndCreate(orgName, resourceName, true, true);

        if (resourceId == null) {
            throw new CygnusRuntimeError("Cannot persist the data (orgName=" + orgName + ", resourceName="
                    + resourceName + ")");
        } else {
            // persist the resourceName
            insert(recvTimeTs, recvTime, resourceId, attrName, attrType, attrValue, attrMd);
        } // if else
    } // persist

    @Override
    public void persist(String recvTime, String orgName, String resourceName, Map<String, String> attrList,
                Map<String, String> attrMdList) throws Exception {
        // try to get the resource identifier from the cache
        String resourceId = resourceLookupAndCreate(orgName, resourceName, false, true);

        if (resourceId == null) {
            throw new CygnusRuntimeError("Cannot persist the data (orgName=" + orgName + ", resourceName="
                    + resourceName + ")");
        } else {
            // persist the resourceName
            insert(recvTime, resourceId, attrList, attrMdList);
        } // if else
    } // persist

    /**
     * looks the ID of a resource identified by orgName and resourceName, creating it if not found and the
     * createResource param is true.
     * @param orgName
     * @param resourceName
     * @param createResource True if running in row-like mode (where resources can be created on the fly), false if
     * running in column-like mode (where the resources must be preprovisioned).
     * @param purgeCache True if the cache has to be purged when an error is found, false otherwise.
     * @return
     * @throws Exception
     */
    private String resourceLookupAndCreate(String orgName, String resourceName, boolean createResource,
            boolean purge) throws Exception {
        try {
            // look for the resource resourceId associated to the resourceName in the hashmap
            String resourceId;
            OrgResourcePair orgResourcePair = new OrgResourcePair(orgName, resourceName);

            // check if the resource can be obtained from the map
            if (resourceIds.containsKey(orgResourcePair)) {
                resourceId = resourceIds.get(orgResourcePair);
                logger.debug("Resource id found in the map (<orgName,resourceName>=" + orgResourcePair
                        + " -> resourceId=" + resourceId + ")");
            } else if (createResource) {
                logger.debug("Resource id not found in the map, going to create it (orgName=" + orgName
                        + ", resourceName=" + resourceName + ")");

                // create the resource resourceId and datastore, adding it to the resourceIds map
                resourceId = createResource(resourceName, orgName);

                if (resourceId == null) {
                    throw new CygnusBadConfiguration("The resource id did not exist and could not be created. The "
                            + "resource/datastore pre-provision in column mode failed");
                } // if

                createDataStore(resourceId);
                resourceIds.put(orgResourcePair, resourceId);
                logger.debug("Resource id added to resources map (<orgName,resourceName>=" + orgResourcePair
                        + " -> resourceId=" + resourceId + ")");
            } else if (purge) {
                // Reached this point, it could be that the resource was created after the organization was cached.
                // Purge this cache entry and reload the organization again (observe purge=false, i.e. after reloading
                // the organization the same behaviour is definitely considered an error).
                logger.debug("Going to purge the cache (orgName=" + orgName + ")");
                purgeCache(orgName);
                initOrg(orgName);
                return resourceLookupAndCreate(orgName, resourceName, createResource, false);
            } else {
                throw new CygnusBadConfiguration("The resource id did not exist and could not be created. The "
                        + "resource/datastore pre-provision in column mode failed");
            } // if else

            return resourceId;
        } catch (Exception e) {
            if (e instanceof CygnusRuntimeError
                    || e instanceof CygnusPersistenceError
                    || e instanceof CygnusBadConfiguration) {
                throw e;
            } else {
                throw new CygnusRuntimeError(e.getMessage());
            } // if else
        } // try catch
    } // resourceLookupAndCreate
    
    /**
     * Purges the cache regarding an organization, given its name.
     * @param orgName
     * @throws Exception
     */
    private void purgeCache(String orgName) throws Exception {
        packagesIds.remove(orgName);
        
        Iterator it = resourceIds.keySet().iterator();
        
        while (it.hasNext()) {
            OrgResourcePair pair = (OrgResourcePair) it.next();
            
            if (pair.getOrg().equals("orgName")) {
                it.remove(); // Map.remove(objectKey) cannot be used inside a loop!
            } // if
        } // while
    } // purgeCache

    /**
     * Insert record in datastore (row mode).
     * @param recvTimeTs timestamp.
     * @param recvTime timestamp (human readable)
     * @param resourceId the resource in which datastore the record is going to be inserted.
     * @param attrName attribute resourceName.
     * @param attrType attribute type.
     * @param attrValue attribute value.
     * @throws Exception
     */
    private void insert(long recvTimeTs, String recvTime, String resourceId, String attrName, String attrType,
            String attrValue, String attrMd) throws Exception {
        String ckanURL = null;
        String jsonString = null;
        
        try {
            // create the CKAN request JSON
            String records = "\"" + Constants.RECV_TIME_TS + "\": \"" + recvTimeTs / 1000 + "\", "
                    + "\"" + Constants.RECV_TIME + "\": \"" + recvTime + "\", "
                    + "\"" + Constants.ATTR_NAME + "\": \"" + attrName + "\", "
                    + "\"" + Constants.ATTR_TYPE + "\": \"" + attrType + "\", "
                    + "\"" + Constants.ATTR_VALUE + "\": " + attrValue;

            // metadata is an special case, because CKAN doesn't support empty array, e.g. "[ ]"
            // (http://stackoverflow.com/questions/24207065/inserting-empty-arrays-in-json-type-fields-in-datastore)
            if (!attrMd.equals(Constants.EMPTY_MD)) {
                records += ", \"" + Constants.ATTR_MD + "\": " + attrMd;
            } // if

            jsonString = "{ \"resource_id\": \"" + resourceId
                    + "\", \"records\": [ { " + records + " } ], "
                    + "\"method\": \"insert\", "
                    + "\"force\": \"true\" }";
            
            // create the CKAN request URL
            ckanURL = (ssl ? "https://" : "http://") + ckanHost + ":" + ckanPort + "/api/3/action/datastore_upsert";
        
            // do the CKAN request
            CKANResponse res = doCKANRequest("POST", ckanURL, jsonString);

            // check the status
            if (res.getStatusCode() == 200) {
                logger.debug("Successful insert (resource/datastore id=" + resourceId + ")");
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
     * Insert record in datastore (column mode).
     * @param recvTime timestamp (human readable)
     * @param resourceId the resource in which datastore the record is going to be inserted.
     * @param attrList map with the attributes to persist
     * @throws Exception
     */
    private void insert(String recvTime, String resourceId, Map<String, String> attrList,
            Map<String, String> attrMdList) throws Exception {
        String ckanURL = null;
        String jsonString = null;
        
        try {
            // create the CKAN request JSON
            String records = "\"" + Constants.RECV_TIME + "\": \"" + recvTime + "\"";

            // iterate on the attribute and metadata maps in order to build the query
            Iterator it = attrList.keySet().iterator();

            while (it.hasNext()) {
                String attrName = (String) it.next();
                String attrValue = attrList.get(attrName);
                records += ", \"" + attrName + "\": " + attrValue;
            } // while

            it = attrMdList.keySet().iterator();

            while (it.hasNext()) {
                String attrName = (String) it.next();
                String attrMd = attrMdList.get(attrName);

                // metadata is an special case, because CKAN doesn't support empty array, e.g. "[ ]"
                // (http://stackoverflow.com/questions/24207065/inserting-empty-arrays-in-json-type-fields-in-datastore)
                if (!attrMd.equals(Constants.EMPTY_MD)) {
                    records += ", \"" + attrName + "\": " + attrMd;
                } // if
            } // while

            jsonString = "{ \"resource_id\": \"" + resourceId
                    + "\", \"records\": [ { " + records + " } ], "
                    + "\"method\": \"insert\", "
                    + "\"force\": \"true\" }";
            
            // create the CKAN request URL
            ckanURL = (ssl ? "https://" : "http://") + ckanHost + ":" + ckanPort + "/api/3/action/datastore_upsert";
        
            // do the CKAN request
            CKANResponse res = doCKANRequest("POST", ckanURL, jsonString);

            // check the status
            if (res.getStatusCode() == 200) {
                logger.debug("Successful insert (resource/datastore id=" + resourceId + ")");
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
     * Creates an orgName in CKAN.
     * @param orgName to create
     * @throws Exception
     */
    private void createOrganization(String orgName) throws Exception {
        try {
            // create the CKAN request JSON
            String jsonString = "{ \"name\": \"" + orgName + "\"}";
            
            // create the CKAN request URL
            String ckanURL = (ssl ? "https://" : "http://") + ckanHost + ":" + ckanPort
                    + "/api/3/action/organization_create";
            
            // do the CKAN request
            CKANResponse res = doCKANRequest("POST", ckanURL, jsonString);

            // check the status
            if (res.getStatusCode() == 200) {
                String orgId = ((JSONObject) res.getJsonObject().get("result")).get("id").toString();
                logger.debug("Successful organization creation (orgName=" + orgName + ", orgId=" + orgId + ")");

                // create the package/dataset
                String packageName = orgName + "_" + defaultDataset;

                if (packageName.length() > Constants.CKAN_PKG_MAX_LEN) {
                    logger.error("Bad configuration (A CKAN package/dataset name '" + packageName + "' has been built "
                            + "and its length is greater than " + Constants.CKAN_PKG_MAX_LEN + ". This package name "
                            + "generation is based on the concatenation of the notified '" + Constants.HEADER_SERVICE
                            + "' organization header, the character '_' and the 'default_dataset' configuration "
                            + "parameter, thus adjust them)");
                    throw new CygnusBadConfiguration("The lenght of the CKAN package/dataset '" + packageName
                            + "' is greater than " + Constants.CKAN_PKG_MAX_LEN);
                } // if

                String packageId = createPackage(packageName, orgId);
                packagesIds.put(orgName, packageId);
                logger.debug("Package added to packages map (orgName=" + orgName + " -> packageId=" + packageId + ")");
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
     * @param pkgName package to create
     * @param orgId the owner organization for the package
     * @return pkgId if the package was created or "" if it wasn't.
     * @throws Exception
     */
    private String createPackage(String pkgName, String orgId) throws Exception {
        try {
            String jsonString = "{ \"name\": \"" + pkgName + "\", " + "\"owner_org\": \"" + orgId + "\" }";
            String ckanURL = (ssl ? "https://" : "http://") + ckanHost + ":" + ckanPort
                    + "/api/3/action/package_create";
            CKANResponse res = doCKANRequest("POST", ckanURL, jsonString);

            // check the status
            if (res.getStatusCode() == 200) {
                String packageId = ((JSONObject) res.getJsonObject().get("result")).get("id").toString();
                logger.debug("Successful package creation (pkgId=" + packageId + ")");
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
    } // createDefaultPackage

    /**
     * Creates a resource within the default dataset of a given orgName.
     * @param resourceName Resource to be created.
     * @param orgName orgName to which the default dataset belongs
     * @return resource ID if the resource was created or "" if it wasn't.
     * @throws Exception
     */
    private String createResource(String resourceName, String orgName)
        throws Exception {
        try {
            // create the CKAN request JSON; compose the resource URL with the one corresponding to the NGSI10
            // convenience operation to get entity information in Orion
            StringTokenizer st = new StringTokenizer(resourceName, "-");
            String jsonURL = orionUrl + "/ngsi10/contextEntitites/" + st.nextElement();
            String jsonString = "{ \"name\": \"" + resourceName + "\", "
                    + "\"url\": \"" + jsonURL + "\", "
                    + "\"package_id\": \"" + packagesIds.get(orgName) + "\" }";
            
            // create the CKAN request URL
            String ckanURL = (ssl ? "https://" : "http://") + ckanHost + ":" + ckanPort
                    + "/api/3/action/resource_create";
            
            // do the CKAN request
            CKANResponse res = doCKANRequest("POST", ckanURL, jsonString);

            // check the status
            if (res.getStatusCode() == 200) {
                String resourceId = ((JSONObject) res.getJsonObject().get("result")).get("id").toString();
                logger.debug("Successful resource creation (resource id=" + resourceId + ")");
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
     * Creates a datastore for a given resource.
     * @param resourceId identifies the resource which datastore is going to be created.
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
                    + "{ \"id\": \"" + Constants.ATTR_NAME + "\", \"type\": \"text\"},"
                    + "{ \"id\": \"" + Constants.ATTR_TYPE + "\", \"type\": \"text\"},"
                    + "{ \"id\": \"" + Constants.ATTR_VALUE + "\", \"type\": \"json\"},"
                    + "{ \"id\": \"" + Constants.ATTR_MD + "\", \"type\": \"json\"}"
                    + "], "
                    + "\"force\": \"true\" }";
            
            // create the CKAN request URL
            String ckanURL = (ssl ? "https://" : "http://") + ckanHost + ":" + ckanPort
                    + "/api/3/action/datastore_create";
            
            // do the CKAN request
            CKANResponse res = doCKANRequest("POST", ckanURL, jsonString);

            // check the status
            if (res.getStatusCode() == 200) {
                logger.debug("Successful datastore creation (resourceId=" + resourceId + ")");
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
     * Activates an element given its resourceName and type, by changing its status from "deleted" to "activate".
     * Its resourceId is returned.
     * @param elementName
     * @param elementType
     * @return The element resourceId if it could be activated, otherwise null.
     * @throws Exception
     */
    private String activateElementState(String elementName, String elementType)
        throws Exception {
        String jsonString = "{\"state\":\"active\"}";
        String ckanURL = (ssl ? "https://" : "http://") + ckanHost + ":" + ckanPort + "/api/rest/" + elementType + "/"
                + elementName;
        CKANResponse res = doCKANRequest("POST", ckanURL, jsonString);
                
        if (res.getStatusCode() == 200) {
            return res.getJsonObject().get("id").toString();
        } else {
            return null;
        } // if else
    } // activateElementState
    
    private String getCKANVersion() throws Exception {
        String ckanURL = (ssl ? "https://" : "http://") + ckanHost + ":" + ckanPort + "/api/util/status";
        CKANResponse res = doCKANRequest("GET", ckanURL);
        
        if (res.getStatusCode() == 200) {
            return res.getJsonObject().get("ckan_version").toString();
        } else {
            return null;
        } // if else
    } // getCKANVersion

    /**
     * Common method to perform HTTP request using the CKAN API without payload.
     * @param method HTTP method
     * @param jsonURL request URL.
     * @return CKANResponse associated to the request.
     * @throws Exception
     */
    private CKANResponse doCKANRequest(String method, String url) throws Exception {
        return doCKANRequest(method, url, "");
    } // doCKANRequest

    /**
     * Common method to perform HTTP request using the CKAN API with payload.
     * @param method HTTP method.
     * @param jsonURL request URL.
     * @param payload request payload.
     * @return CKANResponse associated to the request.
     * @throws Exception
     */
    private CKANResponse doCKANRequest(String method, String url, String payload)
        throws Exception {
        HttpRequestBase request = null;
        HttpResponse response = null;
        
        try {
            // do the post
            if (method.equals("GET")) {
                request = new HttpGet(url);
            } else if (method.equals("POST")) {
                HttpPost r = new HttpPost(url);

                // payload (optional)
                if (!payload.equals("")) {
                    logger.debug("request payload: " + payload);
                    r.setEntity(new StringEntity(payload, ContentType.create("application/json")));
                } // if
                
                request = r;
            } else {
                throw new CygnusRuntimeError("HTTP method not supported: " + method);
            } // if else

            // headers
            request.addHeader("Authorization", apiKey);

            // execute the request
            logger.debug("CKAN operation: " + request.toString());
        } catch (Exception e) {
            if (e instanceof CygnusRuntimeError
                    || e instanceof CygnusPersistenceError
                    || e instanceof CygnusBadConfiguration) {
                throw e;
            } else {
                throw new CygnusRuntimeError(e.getMessage());
            } // if else
        } // try catch
        
        try {
            response = httpClient.execute(request);
        } catch (Exception e) {
            throw new CygnusPersistenceError(e.getMessage());
        } // try catch
        
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String res = reader.readLine();
            request.releaseConnection();
            long l = response.getEntity().getContentLength();
            logger.debug("CKAN response (" + l + " bytes): " + response.getStatusLine().toString());

            // get the JSON encapsulated in the response
            logger.debug("response payload: " + res);
            JSONParser j = new JSONParser();
            JSONObject o = (JSONObject) j.parse(res);

            // return result
            return new CKANResponse(o, response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            if (e instanceof CygnusRuntimeError
                    || e instanceof CygnusPersistenceError
                    || e instanceof CygnusBadConfiguration) {
                throw e;
            } else {
                throw new CygnusRuntimeError(e.getMessage());
            } // if else
        } // try catch
    } // doCKANRequest
    
    /**
     * Sets the http client. This is protected since it is only used by the tests.
     * @param httpClient
     */
    protected void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    } // setHttpClient

    /**
     * Class to store the <org, entity> pair, uses as key in the resourceId hashmap in the CKANBackendImpl class.
     */
    class OrgResourcePair {

        private String entity;
        private String org;

        /**
         * Class constructor.
         * @param org
         * @param entity
         */
        public OrgResourcePair(String org, String entity) {
            this.org = org;
            this.entity = entity;
        } // OrgResourcePair

        /**
         * @return entity
         */
        public String getEntity() {
            return entity;
        } // getEntity

        /**
         * @return org
         */
        public String getOrg() {
            return org;
        } // gettOrg

        /**
         * @param obj
         * @return true if obj is equals to the object
         */
        @Override
        public boolean equals(Object obj) {
            return (obj instanceof OrgResourcePair
                    && ((OrgResourcePair) obj).entity.equals(this.entity)
                    && ((OrgResourcePair) obj).org.equals(this.org));
        } // equals

        /**
         * Gets a hashcode for the object based on the following algorithm:
         * http://stackoverflow.com/questions/113511/hash-code-implementation.
         * @return hashcode for the object
         */
        @Override
        public int hashCode() {
            return org.hashCode() + 37 * entity.hashCode();
        } // hashCode

        /**
         * Serializes the object's attributes as a string.
         * @return String
         */
        @Override
        public String toString() {
            return "<" + org + "," + entity + ">";
        } // toString

    } // OrgResourcePair

} // CKANBackendImpl
