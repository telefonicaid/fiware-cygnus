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

import java.sql.Timestamp;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;

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

    // this map implements the f(NGSIentity, Org) = CKANresourceId function
    private HashMap<OrgEntityPair, String> resourceIds;

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
     */
    public CKANBackendImpl(String apiKey, String ckanHost, String ckanPort, String defaultDataset) {
        logger = Logger.getLogger(CKANBackendImpl.class);
        this.apiKey = apiKey;
        this.ckanHost = ckanHost;
        this.ckanPort = ckanPort;
        this.defaultDataset = defaultDataset;
        resourceIds = new HashMap<OrgEntityPair, String>();
        packagesIds = new HashMap<String, String>();

    } // CKANBackendImpl

    @Override
    public void initOrg(DefaultHttpClient httpClient, String organization) throws Exception {

        if (packagesIds.containsKey(organization)) {
            // Organization already initialized, nothing to do
            logger.info("organiation "  + organization + " already initialized");
            return;
        } // if

        // do CKAN request
        CKANResponse res = doCKANRequest(httpClient, "GET",
                "http://" + ckanHost + ":" + ckanPort + "/api/3/action/organization_show?id=" + organization);

        // check the status
        if (res.getStatusCode() == 200) {

            // organization exists
            JSONObject result = (JSONObject) res.getJsonObject().get("result");
            String orgId = result.get("id").toString();
            logger.info("organization found - organization ID: " + orgId);

            // check if organization contains the default package
            JSONArray packages = (JSONArray) result.get("packages");
            Iterator<JSONObject> iterator = packages.iterator();
            while (iterator.hasNext()) {
                JSONObject pkg = (JSONObject) iterator.next();
                String pkgName = (String) pkg.get("name");
                if (pkgName.equals(defaultDataset)) {
                    String packageId = pkg.get("id").toString();
                    logger.info("default package found - package ID: " + packageId);
                    populateResourcesMap((JSONArray) pkg.get("resources"), organization);
                    packagesIds.put(organization, packageId);
                    logger.info("added to packages map " + organization + " -> " + packageId);
                    return;
                } // if
            } // while

            // if we have reach this point, then organization doesn't include the default package and we need to
            // create it
            String packageId = createDefaultPackage(httpClient, orgId);
            packagesIds.put(organization, packageId);
            logger.info("added to packages map " + organization + " -> " + packageId);

        } else if (res.getStatusCode() == 404) {
            // organization doesn't exist, create it
            createOrganization(httpClient, organization);
        } else {
            logger.error("don't know how to treat response code " + res.getStatusCode());
        } // if else

    } // initOrg

    @Override
    public void persist(DefaultHttpClient httpClient, long recvTimeTs, String organization, String entity,
            String attrName, String attrType, String attrValue) throws Exception {

        // look for the resource id associated to the entity in the hashmap
        String resourceId;
        OrgEntityPair ek = new OrgEntityPair(organization, entity);
        logger.info("lookup in resources map " + ek);

        if (resourceIds.containsKey(ek)) {
            // persist the data in the datastore associated to the resource id (entity)
            resourceId = resourceIds.get(ek);
            logger.info("resolved " + ek + " -> " + resourceId);
        } else {
            // create the resource id and datastore, adding it to the resourceIds map
            resourceId = createResource(httpClient, entity, organization);
            createDataStore(httpClient, resourceId);
            resourceIds.put(ek, resourceId);
            logger.info("added to resources map " + ek + " -> " + resourceId);
        } // if else

        // persist the entity
        insert(httpClient, recvTimeTs, resourceId, attrName, attrType, attrValue);

    } // persist

    /**
     * Populates the entity-resource map of a given organization with the package information from the CKAN response.
     *
     * @param resources JSON vector from the CKAN response containing resource information.
     * @param organization
     */
    private void populateResourcesMap(JSONArray resources, String organization) {

        Iterator<JSONObject> iterator = resources.iterator();
        while (iterator.hasNext()) {
            JSONObject factObj = (JSONObject) iterator.next();
            String id = (String) factObj.get("id");
            String name = (String) factObj.get("name");
            OrgEntityPair ek = new OrgEntityPair(organization, name);
            resourceIds.put(ek, id);
            logger.info("added to resources map " + ek + " -> " + id);
        }

    } // populateResourcesMap

    /**
     * Insert record in datastore.
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param date timestamp.
     * @param resourceId the resource in which datastore the record is going to be inserted.
     * @param attrName attribute name.
     * @param attrType attribute type.
     * @param attrValue attribute value.
     * @throws Exception
     *
     */
    private void insert(DefaultHttpClient httpClient, long recvTimeTs, String resourceId, String attrName,
                        String attrType, String attrValue) throws Exception {

        // do CKAN request
        String jsonString = "{ \"resource_id\": \"" + resourceId
                + "\", \"records\": [ "
                + "{ \"" + Constants.RECV_TIME_TS + "\": \"" + recvTimeTs / 1000 + "\", "
                + "\"" + Constants.RECV_TIME + "\": \"" + new Timestamp(recvTimeTs).toString().replaceAll(" ", "T")
                + "\", " + "\"" + Constants.ATTR_NAME + "\": \"" + attrName + "\", "
                + "\"" + Constants.ATTR_TYPE + "\": \"" + attrType + "\", "
                + "\"" + Constants.ATTR_VALUE + "\": \"" + attrValue + "\" "
                + "}"
                + "], "
                + "\"method\": \"insert\", "
                + "\"force\": \"true\" }";
        CKANResponse res = doCKANRequest(httpClient, "POST",
                "http://" + ckanHost + ":" + ckanPort + "/api/3/action/datastore_upsert", jsonString);

        // check the status
        if (res.getStatusCode() == 200) {
            logger.info("successful insert in resource ID " + resourceId + " datastore");
        } else {
            logger.error("don't know how to treat response code " + res.getStatusCode());
        } // if else

    } // insert

    /**
     * Creates an organization in CKAN.
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param organization to create
     * @throws Exception
     */
    private void createOrganization(DefaultHttpClient httpClient, String organization) throws Exception {

        // do CKAN request
        String jsonString = "{ \"name\": \"" + organization + "\"}";
        CKANResponse res = doCKANRequest(httpClient, "POST",
                "http://" + ckanHost + ":" + ckanPort + "/api/3/action/organization_create", jsonString);

        // check the status
        if (res.getStatusCode() == 200) {
            String orgId = ((JSONObject) res.getJsonObject().get("result")).get("id").toString();
            logger.info("successful organization creation - organization ID: " + orgId);
            String packageId = createDefaultPackage(httpClient, orgId);
            packagesIds.put(organization, packageId);
            logger.info("added to packages map " + organization + " -> " + packageId);
        } else {
            logger.error("don't know how to treat response code " + res.getStatusCode());
        } // if else
    } // createOrganization

    /**
     * Creates the default dataset (package) for a given organization in CKAN.
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param orgId the owner organization for the package
     * @return packageId if the package was created or "" if it wasn't.
     * @throws Exception
     */
    private String createDefaultPackage(DefaultHttpClient httpClient, String orgId) throws Exception {

        // do CKAN request
        String jsonString = "{ \"name\": \"" + defaultDataset + "\", "
                + "\"owner_org\": \"" + orgId + "\" }";
        CKANResponse res = doCKANRequest(httpClient, "POST",
                "http://" + ckanHost + ":" + ckanPort + "/api/3/action/package_create", jsonString);

        // check the status
        if (res.getStatusCode() == 200) {
            String packageId = ((JSONObject) res.getJsonObject().get("result")).get("id").toString();
            logger.info("successful package creation - package ID: " + packageId);
            return packageId;
        } else {
            logger.error("don't know how to treat response code " + res.getStatusCode());
            return "";
        } // if else
    } // createDefaultPackage

    /**
     * Creates a resource within the default dataset of a given organization.
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param resourceName Resource to be created.
     * @param organization organization to which the default dataset belongs
     * @return resource ID if the resource was created or "" if it wasn't.
     * @throws Exception
     */
    private String createResource(DefaultHttpClient httpClient, String resourceName, String organization)
        throws Exception {

        // do CKAN request
        String jsonString = "{ \"name\": \"" + resourceName + "\", "
                + "\"url\": \"" + resourceName + "\", "
                + "\"package_id\": \"" + packagesIds.get(organization) + "\" }";
        CKANResponse res = doCKANRequest(httpClient, "POST",
                "http://" + ckanHost + ":" + ckanPort + "/api/3/action/resource_create", jsonString);

        // check the status
        if (res.getStatusCode() == 200) {
            String resourceId = ((JSONObject) res.getJsonObject().get("result")).get("id").toString();
            logger.info("successful resource creation - resource ID: " + resourceId);
            return resourceId;
        } else {
            logger.error("don't know how to treat response code " + res.getStatusCode());
            return "";
        } // if else
    } // createResource

    /**
     * Creates a datastore for a given resource.
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param resourceId identifies the resource which datastore is going to be created.
     * @throws Exception
     */
    private void createDataStore(DefaultHttpClient httpClient, String resourceId) throws Exception {

        // do CKAN request
        // CKAN types reference: http://docs.ckan.org/en/ckan-2.2/datastore.html#valid-types
        String jsonString = "{ \"resource_id\": \"" + resourceId
                + "\", \"fields\": [ "
                + "{ \"id\": \"" + Constants.RECV_TIME_TS + "\", \"type\": \"int\"},"
                + "{ \"id\": \"" + Constants.RECV_TIME + "\", \"type\": \"timestamp\"},"
                + "{ \"id\": \"" + Constants.ATTR_NAME + "\", \"type\": \"text\"},"
                + "{ \"id\": \"" + Constants.ATTR_TYPE + "\", \"type\": \"text\"},"
                + "{ \"id\": \"" + Constants.ATTR_VALUE + "\", \"type\": \"json\"}"
                + "], "
                + "\"force\": \"true\" }";
        CKANResponse res = doCKANRequest(httpClient, "POST",
                "http://" + ckanHost + ":" + ckanPort + "/api/3/action/datastore_create", jsonString);

        // check the status
        if (res.getStatusCode() == 200) {
            logger.info("successful datastore creation for resource ID: " + resourceId);
        } else {
            logger.error("don't know how to treat response code " + res.getStatusCode());
        } // if else
    } // createResource

    /**
     * Common method to perform HTTP request using the CKAN API without payload.
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param method HTTP method
     * @param url request URL.
     * @return CKANResponse associated to the request.
     * @throws Exception
     */
    private CKANResponse doCKANRequest(DefaultHttpClient httpClient, String method, String url) throws Exception {
        return doCKANRequest(httpClient, method, url, "");
    } // doCKANRequest

    /**
     * Common method to perform HTTP request using the CKAN API with payload.
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param method HTTP method.
     * @param url request URL.
     * @param payload request payload.
     * @return CKANResponse associated to the request.
     * @throws Exception
     */
    private CKANResponse doCKANRequest(DefaultHttpClient httpClient, String method, String url,
            String payload) throws Exception {
        // do the post
        HttpRequestBase request;
        if (method.equals("GET")) {
            request = new HttpGet(url);
        } else if (method.equals("POST")) {
            HttpPost r = new HttpPost(url);
            // payload (optional)
            if (!payload.equals("")) {
                logger.debug("request payload: " + payload);
                r.setEntity(new StringEntity(payload, ContentType.create("application/json")));
            }
            request = r;
        } else {
            throw new Exception("HTTP method not supported: " + method);
        } // if else

        // headers
        request.addHeader("Authorization", apiKey);

        // execute the request
        logger.info("CKAN operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String res = reader.readLine();
        request.releaseConnection();
        long l = response.getEntity().getContentLength();
        logger.info("CKAN response (" + l + " bytes): " + response.getStatusLine().toString());

        // get the JSON encapsulated in the response
        logger.debug("response payload: " + res);
        JSONParser j = new JSONParser();
        JSONObject o = (JSONObject) j.parse(res);

        // return result
        return new CKANResponse(o, response.getStatusLine().getStatusCode());

    } // doCKANRequest

    /**
     * Class to store the <org, entity> pair, uses as key in the resourceId hashmap in the CKANBackendImpl class.
     */
    class OrgEntityPair {

        private String entity;
        private String org;

        /**
         * Class constructor.
         * @param org
         * @param entity
         */
        public OrgEntityPair(String org, String entity) {
            this.org = org;
            this.entity = entity;
        } // OrgEntityPair

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
            return (obj instanceof OrgEntityPair
                    && ((OrgEntityPair) obj).entity.equals(this.entity)
                    && ((OrgEntityPair) obj).org.equals(this.org));
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

    } // OrgEntityPair

} // CKANBackendImpl
