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
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
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

    // this map implements the f(NGSIentity, Org) = CKANresourceId function
    private HashMap<OrgResourcePair, String> resourceIds;

    // this map implements the f(Org) = defaultPackageId function
    private HashMap<String, String> packagesIds;

    // Note that the usage of maps as "cache" for organizations, packages/datasets and resources assumes
    // that once we discover/create some of the elements no other actor is going to delete it or change
    // in a way the cache becomes invalid. An improved version of this backend will implement mechanism
    // to manage "cache fails" and cache updates in that case

    /**
     * @param apiKey
     * @param ckanHost
     * @param ckanPort
     * @param defaultDataset
     * @param orionUrl
     */
    public CKANBackendImpl(String apiKey, String ckanHost, String ckanPort, String defaultDataset, String orionUrl) {
        logger = Logger.getLogger(CKANBackendImpl.class);
        this.apiKey = apiKey;
        this.ckanHost = ckanHost;
        this.ckanPort = ckanPort;
        this.defaultDataset = defaultDataset;
        this.orionUrl = orionUrl;
        resourceIds = new HashMap<OrgResourcePair, String>();
        packagesIds = new HashMap<String, String>();
    } // CKANBackendImpl

    @Override
    public void initOrg(DefaultHttpClient httpClient, String orgName) throws Exception {
        // check if the organization has already been initialized
        if (packagesIds.containsKey(orgName)) {
            logger.debug("Organization '"  + orgName + "' already initialized");
            return;
        } else {
            logger.debug("The organization id must be found by querying CKAN");
        } // if else

        // do CKAN request
        String ckanURL = "http://" + ckanHost + ":" + ckanPort + "/api/3/action/organization_show?id=" + orgName;
        CKANResponse res = doCKANRequest(httpClient, "GET", ckanURL);

        // check the status
        if (res.getStatusCode() == 200) {
            // orgName exists
            JSONObject result = (JSONObject) res.getJsonObject().get("result");
            String orgId = result.get("id").toString();
            logger.debug("Organization found (Organization name=" + orgName + ", organization id=" + orgId + ")");

            // check if orgName contains the default package
            String effectiveDefaultDataset = orgName + "_" + defaultDataset;
            
            if (effectiveDefaultDataset.length() > Constants.CKAN_PKG_MAX_LEN) {
                logger.error("Bad configuration (A CKAN package/dataset name '" + effectiveDefaultDataset + "' has "
                        + "been built and its length is greater than " + Constants.CKAN_PKG_MAX_LEN + ". This package "
                        + "name generation is based on the concatenation of the notified '" + Constants.ORG_HEADER
                        + "' organization header, the character '_' and the 'default_dataset' configuration parameter, "
                        + "thus adjust them)");
                throw new Exception("The lenght of the CKAN package/dataset '" + effectiveDefaultDataset
                        + "' is greater than " + Constants.CKAN_PKG_MAX_LEN);
            } // if
            
            JSONArray packages = (JSONArray) result.get("packages");
            Iterator<JSONObject> iterator = packages.iterator();
            
            while (iterator.hasNext()) {
                JSONObject pkg = (JSONObject) iterator.next();
                String pkgName = (String) pkg.get("name");
                
                if (pkgName.equals(effectiveDefaultDataset)) {
                    String packageId = pkg.get("id").toString();
                    logger.debug("Default package found (Package id= " + packageId + ")");
                    populateResourcesMap((JSONArray) pkg.get("resources"), orgName);
                    packagesIds.put(orgName, packageId);
                    logger.debug("Package added to map (" + orgName + " -> " + packageId + ")");
                    return;
                } // if
            } // while

            // if we have reach this point, then orgName doesn't include the default package and we need to
            // create it
            String packageId = createPackage(httpClient, effectiveDefaultDataset, orgId);
            packagesIds.put(orgName, packageId);
            logger.debug("Package added to map (" + orgName + " -> " + packageId + ")");
        } else if (res.getStatusCode() == 404) {
            // orgName doesn't exist, create it
            createOrganization(httpClient, orgName);
        } else {
            logger.error("Runtime error (Don't know how to treat response code " + res.getStatusCode() + ")");
        } // if else
    } // initOrg
    
    /**
     * Populates the entity-resource map of a given organization with the package information from the CKAN response.
     * @param resources JSON vector from the CKAN response containing resource information.
     * @param organization
     */
    private void populateResourcesMap(JSONArray resources, String organization) {
        Iterator<JSONObject> iterator = resources.iterator();
        
        while (iterator.hasNext()) {
            JSONObject factObj = (JSONObject) iterator.next();
            String id = (String) factObj.get("id");
            String name = (String) factObj.get("name");
            OrgResourcePair orgResourcePair = new OrgResourcePair(organization, name);
            resourceIds.put(orgResourcePair, id);
            logger.debug("Resource id added to resources map (" + orgResourcePair + " -> " + id + ")");
        } // while
    } // populateResourcesMap

    @Override
    public void persist(DefaultHttpClient httpClient, long recvTimeTs, String recvTime, String orgName,
            String resourceName, String attrName, String attrType, String attrValue, String attrMd) throws Exception {
        // get resource ID
        String resourceId = resourceLookupAndCreate(httpClient, orgName, resourceName, true);

        // persist the resourceName
        insert(httpClient, recvTimeTs, recvTime, resourceId, attrName, attrType, attrValue, attrMd);
    } // persist

    @Override
    public void persist(DefaultHttpClient httpClient, String recvTime, String orgName, String resourceName,
                 Map<String, String> attrList, Map<String, String> attrMdList) throws Exception {
        // get resource ID
        String resourceId = resourceLookupAndCreate(httpClient, orgName, resourceName, false);

        if (resourceId == null) {
            logger.error("Runtime error (Cannot persist <" + orgName + "," + resourceName + ">)");
        } else {
            // persist the resourceName
            insert(httpClient, recvTime, resourceId, attrList, attrMdList);
        } // if else
    } // persist

    /**
     * looks the ID of a resource identified by orgName and resourceName, creating it if not found and the
     * createResource param is true.
     * @param httpClient
     * @param orgName
     * @param resourceName
     * @param createResource
     * @return
     * @throws Exception
     */
    private String resourceLookupAndCreate(DefaultHttpClient httpClient, String orgName, String resourceName,
            boolean createResource) throws Exception {
        try {
            // look for the resource id associated to the resourceName in the hashmap
            String resourceId;
            OrgResourcePair orgResourcePair = new OrgResourcePair(orgName, resourceName);

            // check if the resource id can be got from the map
            if (resourceIds.containsKey(orgResourcePair)) {
                resourceId = resourceIds.get(orgResourcePair);
                logger.debug("Resource id found in the map (" + orgResourcePair + " -> " + resourceId + ")");
            } else {
                logger.debug("Resource id not found in the map");

                if (createResource) {
                    // create the resource id and datastore, adding it to the resourceIds map
                    resourceId = createResource(httpClient, resourceName, orgName);

                    if (resourceId == null) {
                        logger.error("Configuration error (The resource id did not exist and could not be created. The "
                                + "resource/datastore pre-provision in column mode failed");
                        return null;
                    } // if

                    createDataStore(httpClient, resourceId);
                    resourceIds.put(orgResourcePair, resourceId);
                    logger.debug("Resource id added to resources map (" + orgResourcePair + " -> " + resourceId + ")");
                } else {
                    logger.error("Configuration error (The resource id did not exist and could not be created. The "
                            + "resource/datastore pre-provision in column mode failed");
                    return null;
                } // if else
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
     * Insert record in datastore (row mode).
     * @param httpClient HTTP client for accessing the backend server.
     * @param recvTimeTs timestamp.
     * @param recvTime timestamp (human readable)
     * @param resourceId the resource in which datastore the record is going to be inserted.
     * @param attrName attribute name.
     * @param attrType attribute type.
     * @param attrValue attribute value.
     * @throws Exception
     */
    private void insert(DefaultHttpClient httpClient, long recvTimeTs, String recvTime, String resourceId,
            String attrName, String attrType, String attrValue, String attrMd) throws Exception {
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
            ckanURL = "http://" + ckanHost + ":" + ckanPort + "/api/3/action/datastore_upsert";
        
            // do the CKAN request
            CKANResponse res = doCKANRequest(httpClient, "POST", ckanURL, jsonString);

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
     * @param httpClient HTTP client for accessing the backend server.
     * @param recvTime timestamp (human readable)
     * @param resourceId the resource in which datastore the record is going to be inserted.
     * @param attrList map with the attributes to persist
     * @throws Exception
     */
    private void insert(DefaultHttpClient httpClient, String recvTime, String resourceId,
                        Map<String, String> attrList, Map<String, String> attrMdList) throws Exception {
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
            ckanURL = "http://" + ckanHost + ":" + ckanPort + "/api/3/action/datastore_upsert";
        
            // do the CKAN request
            CKANResponse res = doCKANRequest(httpClient, "POST", ckanURL, jsonString);

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
     * Creates an organization in CKAN.
     * @param httpClient HTTP client for accessing the backend server.
     * @param organization to create
     * @throws Exception
     */
    private void createOrganization(DefaultHttpClient httpClient, String organization) throws Exception {
        try {
            // create the CKAN request JSON
            String jsonString = "{ \"name\": \"" + organization + "\"}";
            
            // create the CKAN request URL
            String ckanURL = "http://" + ckanHost + ":" + ckanPort + "/api/3/action/organization_create";
            
            // do the CKAN request
            CKANResponse res = doCKANRequest(httpClient, "POST", ckanURL, jsonString);

            // check the status
            if (res.getStatusCode() == 200) {
                String orgId = ((JSONObject) res.getJsonObject().get("result")).get("id").toString();
                logger.debug("Successful organization creation (" + orgId + ")");

                // create the package/dataset
                String packageName = organization + "_" + defaultDataset;

                if (packageName.length() > Constants.CKAN_PKG_MAX_LEN) {
                    logger.error("Bad configuration (A CKAN package/dataset name '" + packageName + "' has been built "
                            + "and its length is greater than " + Constants.CKAN_PKG_MAX_LEN + ". This package name "
                            + "generation is based on the concatenation of the notified '" + Constants.ORG_HEADER
                            + "' organization header, the character '_' and the 'default_dataset' configuration "
                            + "parameter, thus adjust them)");
                    throw new CygnusBadConfiguration("The lenght of the CKAN package/dataset '" + packageName
                            + "' is greater than " + Constants.CKAN_PKG_MAX_LEN);
                } // if

                String packageId = createPackage(httpClient, packageName, orgId);
                packagesIds.put(organization, packageId);
                logger.debug("Package added to map (" + organization + " -> " + packageId + ")");
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
    } // createOrganization

    /**
     * Creates a dataset/package within a given organization in CKAN.
     * @param httpClient HTTP client for accessing the backend server.
     * @param pkg package to create
     * @param orgId the owner organization for the package
     * @return packageId if the package was created or "" if it wasn't.
     * @throws Exception
     */
    private String createPackage(DefaultHttpClient httpClient, String pkg, String orgId) throws Exception {
        try {
            // create the CKAN request JSON
            String jsonString = "{ \"name\": \"" + pkg + "\", " + "\"owner_org\": \"" + orgId + "\" }";
            
            // create the CKAN request URL
            String ckanURL = "http://" + ckanHost + ":" + ckanPort + "/api/3/action/package_create";
            
            // do the CKAN request
            CKANResponse res = doCKANRequest(httpClient, "POST", ckanURL, jsonString);

            // check the status
            if (res.getStatusCode() == 200) {
                String packageId = ((JSONObject) res.getJsonObject().get("result")).get("id").toString();
                logger.debug("Successful package creation (package id=" + packageId + ")");
                return packageId;
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
    } // createDefaultPackage

    /**
     * Creates a resource within the default dataset of a given orgName.
     * @param httpClient HTTP client for accessing the backend server.
     * @param resourceName Resource to be created.
     * @param orgName orgName to which the default dataset belongs
     * @return resource ID if the resource was created or "" if it wasn't.
     * @throws Exception
     */
    private String createResource(DefaultHttpClient httpClient, String resourceName, String orgName)
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
            String ckanURL = "http://" + ckanHost + ":" + ckanPort + "/api/3/action/resource_create";
            
            // do the CKAN request
            CKANResponse res = doCKANRequest(httpClient, "POST", ckanURL, jsonString);

            // check the status
            if (res.getStatusCode() == 200) {
                String resourceId = ((JSONObject) res.getJsonObject().get("result")).get("id").toString();
                logger.debug("Successful resource creation (resource id=" + resourceId + ")");
                return resourceId;
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
    } // createResource

    /**
     * Creates a datastore for a given resource.
     * @param httpClient HTTP client for accessing the backend server.
     * @param resourceId identifies the resource which datastore is going to be created.
     * @throws Exception
     */
    private void createDataStore(DefaultHttpClient httpClient, String resourceId) throws Exception {
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
            String ckanURL = "http://" + ckanHost + ":" + ckanPort + "/api/3/action/datastore_create";
            
            // do the CKAN request
            CKANResponse res = doCKANRequest(httpClient, "POST", ckanURL, jsonString);

            // check the status
            if (res.getStatusCode() == 200) {
                logger.debug("Successful datastore creation (resource id=" + resourceId + ")");
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
    } // createResource

    /**
     * Common method to perform HTTP request using the CKAN API without payload.
     * @param httpClient HTTP client for accessing the backend server.
     * @param method HTTP method
     * @param jsonURL request URL.
     * @return CKANResponse associated to the request.
     * @throws Exception
     */
    private CKANResponse doCKANRequest(DefaultHttpClient httpClient, String method, String url) throws Exception {
        return doCKANRequest(httpClient, method, url, "");
    } // doCKANRequest

    /**
     * Common method to perform HTTP request using the CKAN API with payload.
     * @param httpClient HTTP client for accessing the backend server.
     * @param method HTTP method.
     * @param jsonURL request URL.
     * @param payload request payload.
     * @return CKANResponse associated to the request.
     * @throws Exception
     */
    private CKANResponse doCKANRequest(DefaultHttpClient httpClient, String method, String url, String payload)
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
