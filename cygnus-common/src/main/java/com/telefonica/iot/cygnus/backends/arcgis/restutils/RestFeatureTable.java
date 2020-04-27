/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.arcgis.restutils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.http.HttpResponse;
import com.telefonica.iot.cygnus.backends.arcgis.model.Credential;
import com.telefonica.iot.cygnus.backends.arcgis.model.Feature;
import com.telefonica.iot.cygnus.backends.arcgis.model.Field;
import com.telefonica.iot.cygnus.backends.arcgis.model.pagination.ResultPage;
import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 * @author dmartinez
 *
 */
public class RestFeatureTable extends CredentialRestApi {
    private static final CygnusLogger LOGGER = new CygnusLogger(RestFeatureTable.class);

    protected URL serviceUrl;

    // Table info
    private String uniqueIdField = "";
    private Map<String, Field> tableAttributes = new HashMap<String, Field>();
    private Map<String, Boolean> uniqueAttributes = new HashMap<String, Boolean>();

    /**
     * 
     * @param serviceUrl
     * @param credential
     * @param referer
     * @param expirationMins
     */
    private RestFeatureTable(URL serviceUrl, Credential credential) {
        super((URL) null, credential, serviceUrl.toString());
        this.serviceUrl = serviceUrl;
    }


    /**
     * Constructor.
     * 
     * @param url
     * @param credential
     * @throws MalformedURLException
     */
    public RestFeatureTable(String url, Credential credential, String tokenGenUrl)
            throws ArcgisException {
        super(tokenGenUrl, credential, url);

        try {
            this.serviceUrl = new URL(url);
        } catch (MalformedURLException e) {
            LOGGER.debug("Error parsing url " + url);
            throw new ArcgisException("Error parsing url " + url);
        }
    }



    /**
     * @return the tableAttributes
     */
    public Map<String, Field> getTableAttributes() {
        return tableAttributes;
    }

    /**
     * @param tableAttributes
     *            the tableAttributes to set
     */
    public void setTableAttributes(Map<String, Field> tableAttributes) {
        this.tableAttributes = tableAttributes;
    }

    /**
     * @return the uniqueAttributes
     */
    public Map<String, Boolean> getUniqueAttributes() {
        return uniqueAttributes;
    }

    /**
     * @param uniqueAttributes
     *            the uniqueAttributes to set
     */
    public void setUniqueAttributes(Map<String, Boolean> uniqueAttributes) {
        this.uniqueAttributes = uniqueAttributes;
    }

    /**
     * @return the uniqueIdField
     */
    public String getUniqueIdField() {
        return uniqueIdField;
    }

    /**
     * @return the uniqueIdField
     */
    public boolean hasUniqueIdField() {
        return this.tableAttributes.containsKey(uniqueIdField);
    }

    /**
     * 
     * @param whereClause
     * @return
     * @throws ArcgisException
     */
    public List<Feature> getFeatureList(String whereClause) throws ArcgisException {
        List<Feature> resultList = new ArrayList<Feature>();
        ResultPage<Feature> page;
        int pageOffset = 0;

        do {
            page = getFeatureList(whereClause, pageOffset, getCredential().getToken());
            pageOffset += page.getItemsSize();
            resultList.addAll(page.asItemsList());
        } while (page.hasNext());

        return resultList;
    }

    /**
     * 
     * @param whereClause
     * @param token
     * @return
     * @throws ArcgisException
     */
    public ResultPage<Feature> getFeatureList(String whereClause, int pageOffset, String token)
            throws ArcgisException {

        LOGGER.debug("getFeatureList - Connecting Feature table: " + serviceUrl);

        String responseJSON = null;

        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("outSR", "{\"wkid\":4326}");
        params.put("outFields", "*");
        if (pageOffset >= 0) {
            params.put("resultOffset", String.valueOf(pageOffset));
        }
        params.put("where", whereClause);
        if (token != null && !"".equals(token)) {
            params.put("token", token);
        }
        params.put("f", "pjson");

        String fullUrl = serviceUrl.toString();
        if (!fullUrl.endsWith("/query")) {
            fullUrl += "/query";
        }

        HttpResponse response = httpGet(fullUrl, params);
        LOGGER.debug("Response code: " + response.getResponseCode() + "\n\t" + response.getBody());

        checkResponse(response);

        responseJSON = response.getBody();

        return resultPageFromJson(responseJSON, "features", pageOffset);

    }

    /**
     * 
     * @param response
     * @throws ArcgisException
     */
    protected void checkResponse(HttpResponse response) throws ArcgisException {
        if (!checkHttpResponse(response)) {
            String errorMsg = "Error: " + response.getErrorCode() + "\n"
                    + response.getErrorMessage();
            LOGGER.error(errorMsg);
            throw new ArcgisException(errorMsg);
        } else {
            LOGGER.debug("Feature/Feature action ended successfully");
        }
    }

    /**
     * Añade una lista de entidades/features a la capa.
     * 
     * @param featureList
     * @throws ArcgisException
     */
    public void sendFeatureList(List<Feature> featureList, String action) throws ArcgisException {
        LOGGER.debug(action + " feature list(" + featureList.size() + "), into Feature table: "
                + serviceUrl);

        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("outSR", "{\"wkid\":4326}");
        if (getCredential() != null && !"".equals(getCredential().getToken())) {
            params.put("token", getCredential().getToken());
        }
        params.put("rollbackOnFailure", "true");

        Map<String, String> bodyParams = new LinkedHashMap<String, String>();
        bodyParams.put("features", featureListToStrArray(featureList));
        bodyParams.put("f", "json");

        String fullUrl = serviceUrl.toString();
        if (!fullUrl.endsWith("/" + action)) {
            fullUrl += "/" + action;
        }

        HttpResponse response = httpPost(fullUrl, params, bodyParams);
        LOGGER.debug("Response code: " + response.getResponseCode() + "\n\t" + response.getBody());

        checkResponse(response);
    }

    /**
     * Añade una entidad/feature a la capa.
     * 
     * @param feature
     * @throws ArcgisException
     */
    public void addFeature(Feature feature) throws ArcgisException {
        LOGGER.debug("Adding feature (" + feature.toString() + ") to Feature table: " + serviceUrl);

        List<Feature> featureList = new ArrayList<Feature>();
        featureList.add(feature);
        sendFeatureList(featureList, "addFeatures");
    }

    /**
     * Añade una lista de entidad/feature a la capa.
     * 
     * @param feature
     * @throws ArcgisException
     */
    public void addFeatureList(List<Feature> featureList) throws ArcgisException {
        LOGGER.debug(
                "Adding feature List (" + featureList.size() + ") to Feature table: " + serviceUrl);

        sendFeatureList(featureList, "addFeatures");
    }

    /**
     * Actualiza una entidad/feature en la capa.
     * 
     * @param feature
     * @throws ArcgisException
     */
    public void updateFeature(Feature feature) throws ArcgisException {
        LOGGER.debug("Adding feature (" + feature.toString() + ") to Feature table: " + serviceUrl);

        List<Feature> featureList = new ArrayList<Feature>();
        featureList.add(feature);
        sendFeatureList(featureList, "updateFeatures");
    }

    /**
     * Actualiza una lista de entidades/features en la capa.
     * 
     * @param feature
     * @throws ArcgisException
     */
    public void updateFeatureList(List<Feature> featureList) throws ArcgisException {
        LOGGER.debug(
                "Adding feature list (" + featureList.size() + ") to Feature table: " + serviceUrl);

        sendFeatureList(featureList, "updateFeatures");
    }

    /**
     * Elimina lista de entidades/features a la capa dados sus ObjectIds.
     * 
     * @param ObjectIdList
     * @throws ArcgisException
     */
    public void deleteEntities(List<String> objectIdList) throws ArcgisException {
        LOGGER.debug("Deleting entities (" + objectIdList.size() + ") from Feature table: "
                + serviceUrl);

        Map<String, String> params = new LinkedHashMap<String, String>();
        if (getCredential() != null && !"".equals(getCredential().getToken())) {
            params.put("token", getCredential().getToken());
        }
        params.put("returnDeleteResults", "false");

        Map<String, String> bodyParams = new LinkedHashMap<String, String>();
        bodyParams.put("objectIds", joinList(objectIdList, ","));
        bodyParams.put("f", "json");

        String fullUrl = serviceUrl.toString();
        if (!fullUrl.endsWith("/deleteFeatures")) {
            fullUrl += "/deleteFeatures";
        }

        HttpResponse response = httpPost(fullUrl, params, bodyParams);
        LOGGER.debug("Response code: " + response.getResponseCode() + "\n\t" + response.getBody());

        checkResponse(response);

    }

    /**
     * Borra las entidades de la capa.
     * 
     * @param featureList
     * @throws ArcgisException
     */
    public void deleteFeatureList(List<Feature> featureList) throws ArcgisException {
        LOGGER.debug("Deleting feature list (" + featureList.size() + ") from Feature table: "
                + serviceUrl);

        List<String> objectIdList = new ArrayList<String>();

        for (Feature feature : featureList) {
            objectIdList.add(Long.toString(feature.getObjectId()));
        }
        deleteEntities(objectIdList);
    }

    /**
     * Obtiene la información de los campos de la featureTable.
     * 
     * @throws ArcgisException
     */
    public void getTableAttributesInfo() throws ArcgisException {
        LOGGER.debug("Retriving Feature table info: " + serviceUrl.toString());
        String fullUrl = serviceUrl.toString();

        String responseJSON = null;
        String token = "";

        try {

            Map<String, String> params = new LinkedHashMap<String, String>();
            if (getCredential() != null && !"".equals(getCredential().getToken())) {
                token = getCredential().getToken();
                params.put("token", token);
            }
            params.put("f", "pjson");

            LOGGER.debug("HttpGet " + fullUrl.toString() + " number of params: " + params.size());
            HttpResponse response = httpGet(fullUrl, params);
            LOGGER.debug("Response code: " + response.getResponseCode() + "\n\t" + response.getBody());

            checkResponse(response);

            if (response.isSuccessful()) {

                responseJSON = response.getBody();
                LOGGER.debug("    tokenJSON: " + responseJSON);

                getUniqueIdFieldFromJson(responseJSON);
                getAttributeInfoFromJson(responseJSON);
                getAttributeIndexFromJson(responseJSON);

            } else {
                String errorMsg = "getTableAttributesInfo: Unexpected server response, Error: "
                        + response.getErrorCode() + "\n" + response.getErrorMessage();
                errorMsg += " \n\t token: " + token;
                LOGGER.error(errorMsg);
                throw new ArcgisException(errorMsg);
            }

        } catch (ArcgisException e) {
            throw e;
        } catch (Exception e) {
            throw new ArcgisException(
                    "getTableAttributesInfo, Unexpected Exception " + e.toString());
        }
    }

    /**
     * 
     * @param jsonStr
     * @throws ArcgisException
     */
    protected void getUniqueIdFieldFromJson(String jsonStr) throws ArcgisException {
        try {
            tableAttributes.clear();
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(jsonStr).getAsJsonObject();

            // Unique field
            if (json.has("uniqueIdField")) {
                JsonObject uniqueId = json.get("uniqueIdField").getAsJsonObject();
                this.uniqueIdField = uniqueId.get("name").getAsString();
                LOGGER.debug("Unique id field detected: " + this.uniqueIdField);
            } else if (json.has("objectIdField")) {
                this.uniqueIdField = json.get("objectIdField").getAsString();
                LOGGER.debug("Unique id field detected (objectIdField): " + this.uniqueIdField);
            } else {
                LOGGER.info("WARN: Feature table has not uniqueIdField");
            }
        } catch (Exception e) {
            throw new ArcgisException("Error getting uniqueIdField, " + e.getLocalizedMessage());
        }
    }

    /**
     * 
     * @param jsonStr
     * @throws ArcgisException
     */
    protected void getAttributeInfoFromJson(String jsonStr) throws ArcgisException {

        try {
            tableAttributes.clear();
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(jsonStr).getAsJsonObject();

            // Attribute list
            JsonArray fields = json.get("fields").getAsJsonArray();
            for (JsonElement fieldElement : fields) {
                Field field = Field.createInstanceFromJson(fieldElement.getAsJsonObject());
                this.tableAttributes.put(field.getName(), field);
            }
        } catch (Exception e) {
            LOGGER.error("Can't cast Attributes from Json " + jsonStr);
            throw new ArcgisException("Can't cast attributes from Json, " + jsonStr);
        }
    }

    /**
     * 
     * @param jsonStr
     * @throws ArcgisException
     */
    protected void getAttributeIndexFromJson(String jsonStr) throws ArcgisException {

        try {
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(jsonStr).getAsJsonObject();
            JsonArray jsonFieldsArray = json.get("indexes").getAsJsonArray();
            for (JsonElement fieldElement : jsonFieldsArray) {
                JsonObject index = fieldElement.getAsJsonObject();
                if (index.get("isUnique").getAsBoolean()) {
                    String fields = index.get("fields").getAsString();
                    String[] fieldsArray = fields.split(",");
                    for (String fieldName : fieldsArray) {
                        if (tableAttributes.containsKey(fieldName)) {
                            tableAttributes.get(fieldName).setUnique(true);
                            uniqueAttributes.put(fieldName, true);
                        }
                    }

                }
            }
        } catch (Exception e) {
            LOGGER.error("Error setting unique attributes");
            throw new ArcgisException("Error setting unique attributes, "
                    + e.getClass().getSimpleName() + "  " + e.getMessage());
        }
    }

    /**
     * 
     * @param featureList
     * @return
     */
    protected String featureListToStrArray(List<Feature> featureList) {
        JsonArray array = new JsonArray();

        for (Feature feature : featureList) {
            array.add(feature.toJson());
        }

        return array.toString();
    }

    /**
     * Joins a list into a string.
     * 
     * @param list
     * @param connector
     * @return
     */
    @SuppressWarnings("rawtypes")
    protected String joinList(List list, String connector) {
        String conn = "";
        String result = "";

        for (Object element : list) {
            result += conn + element;
            conn = connector;
        }

        return result;
    }

    /**
     * 
     * @param responseJson
     * @param listTag
     * @param pageOffset
     * @return
     * @throws ArcgisException
     */
    protected ResultPage<Feature> resultPageFromJson(String responseJson, String listTag,
            int pageOffset) throws ArcgisException {

        boolean hasMore = false;

        if ("".equals(listTag)) {
            listTag = "features";
        }
        List<Feature> featureList = new ArrayList<Feature>();

        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(responseJson).getAsJsonObject();
        JsonElement node = json.get(listTag);

        if (json.has("exceededTransferLimit")) {
            hasMore = json.get("exceededTransferLimit").getAsBoolean();
        } else {
            hasMore = false;
        }

        if (node != null && node.isJsonArray()) {
            JsonArray jsonArray = node.getAsJsonArray();

            for (JsonElement jsonElement : jsonArray) {
                JsonElement attributes = jsonElement.getAsJsonObject().get("attributes");
                JsonElement geometry = jsonElement.getAsJsonObject().get("geometry");

                Feature feature = Feature.createInstanceFromJson(jsonElement.toString());

                LOGGER.debug("Adding feature to result " + feature.toString());
                featureList.add(feature);
            }

        } else {
            String errorDesc = "No entities found in Json response.";
            if (json.get("error") != null) {
                errorDesc = json.get("error").toString();
            }
            throw new ArcgisException(errorDesc);
        }

        return new ResultPage<Feature>(pageOffset, featureList.size(), hasMore, featureList);
    }

}
