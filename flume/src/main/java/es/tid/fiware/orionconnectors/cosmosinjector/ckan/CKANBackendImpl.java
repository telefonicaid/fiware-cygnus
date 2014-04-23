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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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

    // this hashmap implements the f(NGSIentity) = CKANresourceId function
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
            // package exists, populate resourcesId hashmap
            JSONObject o = responseJsonData(res);
            packageId = ((JSONObject)o.get("result")).get("id").toString();
            logger.info("package found - package ID: " + packageId);
            // TBD
        } else if (response.getStatusLine().getStatusCode() == 404) {
            // package doesn't exist, create it
            createPackage(httpClient, dataset);
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
            // TBD
        } else if (resourceInPackage()) {
            // if the resource id (entity) is not found in the dataset, then create it
            // TBD
        } else {
            // TBD
        } // if else

    } // persist

    /**
     * Creates a dataset (package) in CKAN.
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param packageName Package name to be created.
     */
    private void createPackage(DefaultHttpClient httpClient, String packageName) throws Exception {

        // build URL
        String url = "http://" + ckanHost + ":" + ckanPort + "/api/3/action/package_create";

        // do the get
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
     * Creates a resource within a dataset and its associated datastore
     *
     * @param httpClient HTTP client for accessing the backend server.
     * @param packageId package ID in which the resource will be created.
     * @param resourceName File to be created.
     * @return resource ID
     */
    private String createResource(DefaultHttpClient httpClient, String packageId, String resourceName) throws Exception {
        return "";
    } // createResource

    /**
     *
     * @return
     */
    private boolean resourceInPackage() {
        return true;
    }

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
