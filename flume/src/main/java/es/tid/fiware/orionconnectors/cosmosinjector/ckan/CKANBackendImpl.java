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

package es.tid.fiware.orionconnectors.cosmosinjector.ckan;

import org.apache.http.HttpResponse;
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
import java.util.Date;
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
    private String dataset;
    private String packageId;

    // this map implements the f(NGSIentity) = CKANresourceId function
    private HashMap<String, String> resourceIds;

    public CKANBackendImpl(String apiKey, String ckanHost, String ckanPort, String dataset) {
        logger = Logger.getLogger(CKANBackendImpl.class);
        this.apiKey = apiKey;
        this.ckanHost = ckanHost;
        this.ckanPort = ckanPort;
        this.dataset = dataset;
        resourceIds = new HashMap<String, String>();

    } // CKANBackendImpl

    @Override
    public void init(DefaultHttpClient httpClient) throws Exception {

        // Get package information
        String url = "http://" + ckanHost + ":" + ckanPort + "/api/3/action/package_show?id=" + dataset;

        // do the get
        HttpGet request = new HttpGet(url);
        logger.info("CKAN operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String res = reader.readLine();
        request.releaseConnection();
        long l = response.getEntity().getContentLength();
        logger.info("CKAN response (" + l + " bytes): " + response.getStatusLine().toString());
        logger.debug("payload: " + res);

        // check the status
        if (response.getStatusLine().getStatusCode() == 200) {
            // package exists, populate resourcesId map
            JSONObject o = responseJsonData(res);
            JSONObject result = (JSONObject)o.get("result");
            packageId = result.get("id").toString();
            logger.info("package found - package ID: " + packageId);
            populateResourcesMap((JSONArray)result.get("resources"));
        } else if (response.getStatusLine().getStatusCode() == 404) {
            // package doesn't exist, create it
            createPackage(httpClient);
        } else {
            logger.error("don't know how to treat response code " + response.getStatusLine().getStatusCode());
        } // if else

    } // init

    @Override
    public void persist(DefaultHttpClient httpClient, Date date, String entity, String attrName,
                        String attrType, String attrValue) throws Exception {

        // look for the resource id associated to the entity in the hashmap
        if (resourceIds.containsKey(entity)) {
            // persist the data in the datastore associated to the resource id (entity)
            String resourceId = resourceIds.get(entity);
            logger.info("resolved " + entity + " -> " + resourceId);
            insert(httpClient, date, resourceId, attrName, attrType, attrValue);
        } else if (resourceInPackage(entity)) {
            // if the resource id (entity) is not found in the dataset, look for it in the package.
            // Although if only one instance of CKAN sink is running and no other out-of-band resource creation
            // is in place this check will always return false, it is safer to do it.
            // TBD
        } else {
            // create the resource id and datastore, adding it to the resourceIds map
            String resourceId = createResource(httpClient, entity);
            createDataStore(httpClient, resourceId);
            resourceIds.put(entity, resourceId);
            insert(httpClient, date, resourceId, attrName, attrType, attrValue);
        } // if else

    } // persist

    /**
     * Populates the entity-resource map with the package information from the CKAN response
     *
     * @param resources JSON vector from the CKAN response containing resource information.
     */
    private void populateResourcesMap(JSONArray resources) {

        Iterator<JSONObject> iterator = resources.iterator();
        while (iterator.hasNext()) {
            JSONObject factObj = (JSONObject) iterator.next();
            String id = (String) factObj.get("id");
            String name = (String) factObj.get("name");
            resourceIds.put(name, id);
            logger.info("added to map " + name + " -> " + id);
        }

    } // populateResourcesMap

    /**
     * Insert record in datastore
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param date timestamp.
     * @param resourceId the resource in which datastore the record is going to be inserted.
     * @param attrName attribute name
     * @param attrType attribute type
     * @param attrValue attribute value
     *
     */
    private void insert(DefaultHttpClient httpClient, Date date, String resourceId, String attrName,
                        String attrType, String attrValue) throws Exception {
        // build URL
        String url = "http://" + ckanHost + ":" + ckanPort + "/api/3/action/datastore_upsert";

        // do the post
        HttpPost request = new HttpPost(url);
        request.addHeader("Authorization", apiKey);
        // FIXME: maybe timestamp is more appropiated for ts/iso8601 (see
        // http://docs.ckan.org/en/ckan-2.2/datastore.html#valid-types)
        String jsonString = "{ \"resource_id\": \"" + resourceId +
                "\", \"records\": [ " +
                   "{ \"ts\": \"" + date.getTime() / 1000 + "\", " +
                   "\"iso8601date\": \"" + new Timestamp(date.getTime()).toString().replaceAll(" ", "T") + "\", " +
                   "\"attrName\": \"" + attrName + "\", " +
                   "\"attrType\": \"" + attrType + "\", " +
                   "\"attrValue\": \"" + attrValue + "\" " +
                   "}" +
                "], " +
                "\"method\": \"insert\", " +
                "\"force\": \"true\" }";
        logger.debug("JSONString: " + jsonString);
        request.setEntity(new StringEntity(jsonString, ContentType.create("application/json")));
        logger.info("CKAN operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String res = reader.readLine();
        request.releaseConnection();
        long l = response.getEntity().getContentLength();
        logger.info("CKAN response (" + l + " bytes): " + response.getStatusLine().toString());
        logger.debug("payload: " + res);

        // check the status
        if (response.getStatusLine().getStatusCode() == 200) {
            logger.info("successful insert in resource ID " + resourceId + " datastore");
        } else {
            logger.error("don't know how to treat response code " + response.getStatusLine().getStatusCode());
        } // if else

    } // insert

    /**
     * Creates the dataset (package) in CKAN.
     *
     * @param httpClient HTTP client for accessing the backend server.
     */
    private void createPackage(DefaultHttpClient httpClient) throws Exception {

        // build URL
        String url = "http://" + ckanHost + ":" + ckanPort + "/api/3/action/package_create";

        // do the post
        HttpPost request = new HttpPost(url);
        request.addHeader("Authorization", apiKey);
        request.setEntity(new StringEntity("{ \"name\": \"" + dataset + "\"}", ContentType.create("application/json")));
        logger.info("CKAN operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String res = reader.readLine();
        request.releaseConnection();
        long l = response.getEntity().getContentLength();
        logger.info("CKAN response (" + l + " bytes): " + response.getStatusLine().toString());
        logger.debug("payload: " + res);

        // check the status
        if (response.getStatusLine().getStatusCode() == 200) {
            JSONObject o = responseJsonData(res);
            packageId = ((JSONObject)o.get("result")).get("id").toString();
            logger.info("successful package creation - package ID: " + packageId);
        } else {
            logger.error("don't know how to treat response code " + response.getStatusLine().getStatusCode());
        } // if else
    } // createPackage

    /**
     * Creates a resource within the dataset
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param resourceName File to be created.
     * @return resource ID if the resource was created or "" if it wasn't
     */
    private String createResource(DefaultHttpClient httpClient, String resourceName) throws Exception {
        // build URL
        String url = "http://" + ckanHost + ":" + ckanPort + "/api/3/action/resource_create";

        // do the post
        HttpPost request = new HttpPost(url);
        request.addHeader("Authorization", apiKey);
        // URL is mandatory in CKAN (we use the name of the resource also in this field)
        String jsonString = "{ \"name\": \"" + resourceName + "\", " +
                "\"url\": \"" + resourceName + "\", " +
                "\"package_id\": \""+ packageId +"\" }";
        logger.debug("JSONString: " + jsonString);
        request.setEntity(new StringEntity(jsonString, ContentType.create("application/json")));
        logger.info("CKAN operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String res = reader.readLine();
        request.releaseConnection();
        long l = response.getEntity().getContentLength();
        logger.info("CKAN response (" + l + " bytes): " + response.getStatusLine().toString());
        logger.debug("payload: " + res);

        // check the status
        if (response.getStatusLine().getStatusCode() == 200) {
            JSONObject o = responseJsonData(res);
            String resourceId = ((JSONObject)o.get("result")).get("id").toString();
            logger.info("successful resource creation - resource ID: " + resourceId);
            return resourceId;
        } else {
            logger.error("don't know how to treat response code " + response.getStatusLine().getStatusCode());
            return "";
        } // if else
    } // createResource

    /**
     * Creates a datastore for a given resource
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param resourceId identifies the resource which datastore is going to be created.
     */
    private void createDataStore(DefaultHttpClient httpClient, String resourceId) throws Exception {
        // build URL
        String url = "http://" + ckanHost + ":" + ckanPort + "/api/3/action/datastore_create";

        // do the post
        HttpPost request = new HttpPost(url);
        request.addHeader("Authorization", apiKey);
        // FIXME: maybe timestamp is more appropiated for ts/iso8601 (see
        // CKAN types reference: http://docs.ckan.org/en/ckan-2.2/datastore.html#valid-types
        String jsonString = "{ \"resource_id\": \"" + resourceId +
                "\", \"fields\": [ " +
                   "{ \"id\": \"ts\", \"type\": \"int\"}," +
                   "{ \"id\": \"iso8601date\", \"type\": \"timestamp\"}," +
                   "{ \"id\": \"attrName\", \"type\": \"text\"}," +
                   "{ \"id\": \"attrType\", \"type\": \"text\"}," +
                   "{ \"id\": \"attrValue\", \"type\": \"json\"}" +
                "], " +
                "\"force\": \"true\" }";
        logger.debug("JSONString: " + jsonString);
        request.setEntity(new StringEntity(jsonString, ContentType.create("application/json")));
        logger.info("CKAN operation: " + request.toString());
        HttpResponse response = httpClient.execute(request);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String res = reader.readLine();
        request.releaseConnection();
        long l = response.getEntity().getContentLength();
        logger.info("CKAN response (" + l + " bytes): " + response.getStatusLine().toString());
        logger.debug("payload: " + res);

        // check the status
        if (response.getStatusLine().getStatusCode() == 200) {
            logger.info("successful datastore creation for resource ID: " + resourceId);
        } else {
            logger.error("don't know how to treat response code " + response.getStatusLine().getStatusCode());
        } // if else
    } // createResource

    /**
     *
     * @return
     */
    private boolean resourceInPackage(String entity) {
        return false;
    } // resourceInPackage

    /**
     * Helper method to get the JSON Object encapsulated in a HTTP response
     *
     * @param s String containing the HTTP response
     */
    private JSONObject responseJsonData(String s) throws Exception {
       JSONParser j = new JSONParser();
       return (JSONObject)j.parse(s);
    }

} // CKANBackendImpl
