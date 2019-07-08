/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. fiware-cygnus is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with fiware-cygnus. If not, see http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please
 * contact with iot_support at tid dot es
 */
package com.telefonica.iot.cygnus.sinks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.flume.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.ArcgisLog;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.EntityArcGisUtils;
import com.telefonica.iot.cygnus.utils.NGSIConstants;

import es.santander.smartcity.arcgisutils.Arcgis;
import es.santander.smartcity.arcgisutils.Entity;

/**
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 *
 */
public class NGSIArcGisSink extends NGSISink {

    private static final String FEATURE_SERVER_0 = "/FeatureServer/0";

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIArcGisSink.class);

    private String arcGisUrl;
    private String arcGisUsername;
    private String arcGisPassword;
    private String subservice;

    private Arcgis arcgisUtils;

    private EntityArcGisUtils entityArcGisUtils;

    /**
     * Constructor.
     */
    public NGSIArcGisSink() {
        super();
    } // NGSIArcGisSink

    /**
     * @return the arcGisUrl
     */
    public String getArcGisUrl() {
        return arcGisUrl;
    } // getArcGisUrl

    /**
     * @param arcGisUrl
     *            the arcGisUrl to set
     */
    public void setArcGisUrl(String arcGisUrl) {
        this.arcGisUrl = arcGisUrl;
    } // setArcGisUrl

    /**
     * @return the arcGisUsername
     */
    public String getArcGisUsername() {
        return arcGisUsername;
    } // getArcGisUsername

    /**
     * @param arcGisUsername
     *            the arcGisUsername to set
     */
    public void setArcGisUsername(String arcGisUsername) {
        this.arcGisUsername = arcGisUsername;
    } // setArcGisUsername

    /**
     * @return the arcGisPassword
     */
    public String getArcGisPassword() {
        return arcGisPassword;
    } // getArcGisPassword

    /**
     * @param arcGisPassword
     *            the arcGisPassword to set
     */
    public void setArcGisPassword(String arcGisPassword) {
        this.arcGisPassword = arcGisPassword;
    } // setArcGisPassword

    /**
     * @return the subservice
     */
    public String getSubservice() {
        return subservice;
    } // getSubservice

    /**
     * @param subservice
     *            the subservice to set
     */
    public void setSubservice(String subservice) {
        this.subservice = subservice;
    } // setSubservice

    public String getUrlFinal() {
        return getArcGisUrl() + "/" + getSubservice() + FEATURE_SERVER_0;
    } // getUrlFinal

    /**
     * @return the arcgisUtils
     */
    public Arcgis getArcgisUtils() {
        return arcgisUtils;
    } // getArcgisUtils

    /**
     * @param arcgisUtils
     *            the arcgisUtils to set
     */
    public void setArcgisUtils(Arcgis arcgisUtils) {
        this.arcgisUtils = arcgisUtils;
    } // setArcgisUtils

    /**
     * @return the entityArcGisUtils
     */
    public EntityArcGisUtils getEntityArcGisUtils() {
        if (entityArcGisUtils == null) {
            entityArcGisUtils = new EntityArcGisUtils();
        }
        return entityArcGisUtils;
    }

    /**
     * @param entityArcGisUtils
     *            the entityArcGisUtils to set
     */
    public void setEntityArcGisUtils(EntityArcGisUtils entityArcGisUtils) {
        this.entityArcGisUtils = entityArcGisUtils;
    }

    /**
     * @return the logger
     */
    public static CygnusLogger getLogger() {
        return LOGGER;
    } // getLogger

    @Override
    public void configure(Context context) {
        LOGGER.debug("Init config --> " + context);
        arcGisUrl = context.getString("arcgis_url", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (url=" + arcGisUrl + ")");

        arcGisUsername = context.getString("arcgis_username", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (username=" + arcGisUsername + ")");

        arcGisPassword = context.getString("arcgis_password", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (password)");

        super.configure(context);
        LOGGER.debug("Fin configure --> " + context);
    } // configure

    @Override
    public void start() {
        LOGGER.info("Init start()");
        super.start();
    } // start

    @Override
    void persistBatch(NGSIBatch batch)
            throws CygnusBadConfiguration, CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
        LOGGER.trace("Init persistBatch( batch --> " + batch + ")");
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if

        // Iterate on the destinations
        batch.startIterator();

        while (batch.hasNext()) {
            String destination = batch.getNextDestination();
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the " + destination + " destination");

            // Get the sub-batch for this destination
            ArrayList<NGSIEvent> events = batch.getNextEvents();

            // Get an aggregator for this destination and initialize it
            ArcGisAggregator aggregator = new ArcGisAggregator();
            aggregator.initialize(events.get(0));

            for (NGSIEvent event : events) {
                aggregator.aggregate(event);
            } // for

            // Persist the aggregation
            persistAggregation(aggregator);
            batch.setNextPersisted(true);
        } // for
    } // persistBatch

    @Override
    public void capRecords(NGSIBatch batch, long maxRecords) throws CygnusCappingError {
    } // capRecords

    @Override
    public void expirateRecords(long expirationTime) throws CygnusExpiratingError {
    } // expirateRecords

    /**
     * 
     * @author PMO Santander Smart City – Ayuntamiento de Santander
     *
     */
    private class ArcGisAggregatorDomain {

        // string containing the data aggregation
        private String aggregation;
        private String service;
        private String subService;
        private JSONObject entityJSON;

        /**
         * 
         * @param aggregation
         * @param service
         * @param subService
         * @param entityJSON
         */
        ArcGisAggregatorDomain(String aggregation, String service, String subService, JSONObject entityJSON) {
            super();
            this.aggregation = aggregation;
            this.service = service;
            this.subService = subService;
            this.entityJSON = entityJSON;
        } // ArcGisAggregatorDomain

        public String getAggregation() {
            return aggregation;
        } // getAggregation

        /**
         * 
         * @return
         */
        public JSONObject getEntityJSON() {
            return entityJSON;
        } // getEntityJSON()

        /**
         * @return the service
         */
        public String getService() {
            return service;
        } // getService()

        /**
         * @param service
         *            the service to set
         */
        public void setService(String service) {
            this.service = service;
        } // setService()

        /**
         * @return the subService
         */
        public String getSubService() {
            return subService;
        } // getSubService()

        /**
         * @param subService
         *            the subService to set
         */
        public void setSubService(String subService) {
            this.subService = subService;
        } // setSubService()

        /**
         * @param aggregation
         *            the aggregation to set
         */
        public void setAggregation(String aggregation) {
            this.aggregation = aggregation;
        } // setAggregation()

        /**
         * @param entityJSON
         *            the entityJSON to set
         */
        public void setEntityJSON(JSONObject entityJSON) {
            this.entityJSON = entityJSON;
        } // setEntityJSON()

    }

    /**
     * Class for aggregating aggregation.
     */
    private class ArcGisAggregator {

        private List<ArcGisAggregatorDomain> listArcGisAggregatorDomain = new ArrayList<ArcGisAggregatorDomain>();

        ArcGisAggregator() {
        } // ArcGisAggregator

        public void initialize(NGSIEvent event) {
        } // initialize

        /**
         * @return the listArcGisAggregatorDomain
         */
        public List<ArcGisAggregatorDomain> getListArcGisAggregatorDomain() {
            return listArcGisAggregatorDomain;
        } // getListArcGisAggregatorDomain()

        /**
         * Determines if input sitring is quoted or not.
         * 
         * @param string
         * @return boolean
         */

        private boolean isQuoted(String string) {
            return string.matches("^[\\s]*\\\"(.*)\\\"[\\s]*$");
        }

        /**
         * Unquote input string.
         * 
         * @param Unquoted
         *            string
         * @return
         */
        private String unquote(String string) {
            return string.replaceAll("^[\\s]*\\\"(.*)\\\"[\\s]*$", "$1");
        }

        /**
         * 
         * @param event
         */
        @SuppressWarnings("unchecked")
        public void aggregate(NGSIEvent event) {
            String aggregation = "";
            String service = "";
            String subService = "";
            JSONObject entityJSON = new JSONObject();

            String line = "{";

            // get the getRecvTimeTs headers
            line += "Processing headers={";
            Map<String, String> headers = event.getHeaders();

            boolean first = true;

            for (Entry<String, String> entry : headers.entrySet()) {
                LOGGER.debug("Header entry key --> " + entry.getKey().toString() + ", value --> "
                        + entry.getValue().toString());
                if (entry.getKey() != null
                        && NGSIConstants.FLUME_HEADER_MAPPED_SERVICE.equals(entry.getKey().toString())) {
                    service = entry.getValue().toString();
                } else if (entry.getKey() != null
                        && NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH.equals(entry.getKey().toString())) {
                    subService = entry.getValue().toString();
                } // if else
                if (first) {
                    line += "\"" + entry.getKey() + "\"=\"" + entry.getValue() + "\"";
                    first = false;
                } else {
                    line += ", \"" + entry.getKey() + "\"=\"" + entry.getValue() + "\"";
                } // if else
            } // for

            line += "}";

            LOGGER.debug("[ArcGisSink] ContextElement ->" + event.getContextElement());
            LOGGER.debug("[ArcGisSink] MappedCE ->" + event.getMappedCE());
            LOGGER.debug("[ArcGisSink] OriginalCE ->" + event.getOriginalCE());
            LOGGER.debug("[ArcGisSink] enableNameMappings state -> " + enableNameMappings);

            // get the getRecvTimeTs body
            ContextElement contextElement = null;
            if (!enableNameMappings) {
                contextElement = event.getContextElement();
            } else {
                contextElement = event.getMappedCE();
            } // if else

            LOGGER.debug("[ArcGisSink] Selected context ->" + contextElement);

            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            line += ", Processing context element={id=" + entityId + ", type=" + entityType + "}";

            // iterate on all this context element attributes, if there are
            // attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            try {
                entityJSON.put("id", entityId);
                entityJSON.put("type", entityType);

                if (contextAttributes == null || contextAttributes.isEmpty()) {
                    line += ", Processing attribute={no attributes within the notified entity}";
                } else {
                    for (ContextAttribute contextAttribute : contextAttributes) {
                        LOGGER.debug("[ArcGisSink-Agregate] Processing Attribute: " + contextAttribute.toString());
                        String attrName = contextAttribute.getName();
                        String attrType = contextAttribute.getType();
                        // get attribute value as is, quoted or not
                        String attrValue = contextAttribute.getContextValue();
                        // return metadata
                        String attrMetadata = parseJsonArrayStrToMetadataJson(contextAttribute.getContextMetadata());
                        LOGGER.debug("[ArcGisSink-Agregate] - Atribute (name --> " + attrName + ", type --> " + attrType
                                + ", value --> " + attrValue + ", metadata=" + attrMetadata + ")");
                        line += ", Processing attribute={name=" + attrName + ", type=" + attrType + ", value="
                                + attrValue + ", metadata=" + attrMetadata + "}, ";

                        // New attribute creation
                        JSONObject attribute = new JSONObject();
                        attribute.put("type", attrType);

                        LOGGER.debug("Tratando valor de atributo:  " + attrName + " --> " + attrValue);
                        JSONParser jsonParser = new JSONParser();
                        try {
                            // If it is a JSON value....
                            JSONObject attrValueJSON = (JSONObject) jsonParser.parse(attrValue);
                            attribute.put("value", attrValueJSON);
                        } catch (Exception e) {
                            try {

                                // If it is an array
                                JSONArray arrayValue = (JSONArray) jsonParser.parse(attrValue);
                                attribute.put("value", arrayValue);
                            } catch (Exception e1) {
                                // Plain value

                                // Verify if it is a string (it is in quotation
                                // marks)
                                if (isQuoted(attrValue)) {
                                    // Insert unquoted
                                    attribute.put("value", unquote(attrValue));
                                } else {
                                    try {
                                        // Try to insert as Integer
                                        attribute.put("value", Integer.parseInt(attrValue));
                                    } catch (NumberFormatException e2) {
                                        try {
                                            // Try to insert as Double
                                            attribute.put("value", Double.parseDouble(attrValue));
                                        } catch (NumberFormatException e3) {
                                            // If all fails, insert as String
                                            LOGGER.warn("[ArcGisSink] Unquoted String attribute: " + attrName + ":"
                                                    + attrValue);
                                            attribute.put("value", attrValue);
                                        }
                                    }
                                }

                            }
                        }

                        if (!CommonConstants.EMPTY_MD.equals(attrMetadata)) {
                            LOGGER.debug("metadata=" + attrMetadata);
                            attribute.put("metadata", attrMetadata);
                        }

                        // Add attribute to body
                        entityJSON.put(attrName, attribute);
                    } // for
                } // if else
            } catch (CygnusRuntimeError error) {
                LOGGER.error("Error parsing metadata: " + error.getMessage());
                entityJSON = null;
            } // try catch
            line += "}";

            if (aggregation.isEmpty()) {
                aggregation = line;
            } else {
                aggregation += "," + line;
            } // if else

            ArcGisAggregatorDomain arcGisAggregatorDomain = new ArcGisAggregatorDomain(aggregation, service, subService,
                    entityJSON);
            listArcGisAggregatorDomain.add(arcGisAggregatorDomain);
        } // aggregate

    } // ArcGisAggregator

    /**
     * 
     * @param aggregator
     * @throws CygnusPersistenceError
     * @throws CygnusRuntimeError
     * @throws CygnusBadContextData
     */
    private void persistAggregation(ArcGisAggregator aggregator)
            throws CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
        if (aggregator != null && aggregator.getListArcGisAggregatorDomain() != null
                && !aggregator.getListArcGisAggregatorDomain().isEmpty()) {
            Map<String, ArcGISDomain> map = new HashMap<String, ArcGISDomain>();
            for (ArcGisAggregatorDomain arcGisAggregatorDomain : aggregator.getListArcGisAggregatorDomain()) {

                String serviceFiware = arcGisAggregatorDomain.getService();
                String servicePathFiware = arcGisAggregatorDomain.getSubService();
                LOGGER.debug(
                        "Persisting aggregation for service: " + serviceFiware + "  Subservice: " + servicePathFiware);
                LOGGER.debug("[" + this.getName() + "] Persisting data at NGSIArcGisSink. Data ("
                        + arcGisAggregatorDomain.getEntityJSON().toString() + ")");

                ArcGISDomain arcGisDomain = map.get(serviceFiware + servicePathFiware);
                if (arcGisDomain == null) {
                    map.put(serviceFiware + servicePathFiware,
                            new ArcGISDomain(serviceFiware, servicePathFiware, arcGisAggregatorDomain.getEntityJSON()));
                } else {
                    arcGisDomain.addJSONObject(arcGisAggregatorDomain.getEntityJSON());

                } // if else
            } // for
            for (String key : map.keySet()) {
                ArcGISDomain arcGISDomain = map.get(key);
                try {
                    setArcgisUtils(ArcgisLog.getInstance(LOGGER,
                            generateURL(getArcGisUrl(), arcGISDomain.getServicePathFiware(), FEATURE_SERVER_0),
                            getArcGisUsername(), getArcGisPassword()));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CygnusRuntimeError("Data insertion error", "Exception", e.getMessage());
                } // try catch
                insertFeature(arcGISDomain);
            } // for

        } else {
            LOGGER.error("ArcGisSink Agregator: Error pharsing JSON body");
            throw new CygnusBadContextData("ArcGisSink Agregator: Error pharsing JSON body");
        } // if else

    } // persistAggregation

    /**
     * 
     * 
     * @param bodyJSONS
     * @throws CygnusRuntimeError
     */
    protected void insertFeature(ArcGISDomain arcGisDomain) throws CygnusRuntimeError {
        LOGGER.debug("init updateRemoteContext");
        try {
            LOGGER.debug("init arcgisUtils");

            arcgisUtils.setBatchSize(getBatchSize());
            LOGGER.debug("inited arcgisUtils" + arcgisUtils);

            // Wait until connected
            LOGGER.debug("Waiting until Arcgis connection is ready.");
            arcgisUtils.waitUntilLoaded();
            if (arcgisUtils.hasError()) {
                LOGGER.error("Error connecting arcgis");
                throw new CygnusRuntimeError("\"Error connecting arcgis.");
            } else {
                if (arcgisUtils != null && arcgisUtils.isLoaded()) {
                    LOGGER.debug("Connection loaded: " + arcgisUtils.isLoaded() + "    connection Error: "
                            + arcgisUtils.hasError());

                    LOGGER.debug("create Entities");
                    List<Entity> listEntities = getEntityArcGisUtils().createEntities(arcGisDomain.getJsonArray(),
                            arcGisDomain.getServiceFiware(), arcGisDomain.getServicePathFiware());

                    LOGGER.debug("addToBatch listEnties --> " + listEntities);
                    arcgisUtils.addToBatch(listEntities);

                    arcgisUtils.commitEntities();
                    LOGGER.debug("Finished.");
                } else {
                    LOGGER.error("canAdd() --> " + arcgisUtils.canAdd());
                    LOGGER.error("Argis connection not ready.");
                    throw new CygnusRuntimeError("Argis connection not ready.");
                } // if else
            } // if else
        } catch (Exception e) {
            e.printStackTrace();
            throw new CygnusRuntimeError("Data insertion error", "Exception", e.getMessage());
        } // try catch
    } // insertFeature

    /**
     * Crear url.
     * 
     * @param arcGisUrl
     * @param servicePathFiware
     * @param featureServer0
     * @return
     */
    private String generateURL(String arcGisUrl, String servicePathFiware, String featureServer0) {
        String arcGisChar = arcGisUrl.trim().substring(arcGisUrl.length() - 1, arcGisUrl.length());
        if (!"/".equals(arcGisChar)) {
            arcGisUrl += "/";
        }
        return arcGisUrl + servicePathFiware + featureServer0;
    }

    /**
     * 
     * 
     * @param jsonArrayStr
     *            String containing JSON array metadata
     * @throws CygnusRuntimeError
     */
    @SuppressWarnings("unchecked")
    private String parseJsonArrayStrToMetadataJson(String jsonArrayStr) throws CygnusRuntimeError {

        JSONObject outMetadata = null;
        String salida = CommonConstants.EMPTY_MD;
        JSONParser jsonParser = new JSONParser();
        JSONArray metadataJson = new JSONArray();

        if (jsonArrayStr.equals("")) {
            try {
                LOGGER.debug("Parsing Metadata : \"" + jsonArrayStr + "\"");
                metadataJson = (JSONArray) jsonParser.parse(jsonArrayStr);

            } catch (ParseException e) {
                LOGGER.error("BadRequest : metadata must be a JSON Array");
                throw new CygnusRuntimeError("BadRequest : metadata must be a JSON Array");
            }
            try {

                outMetadata = new JSONObject();
                for (int i = 0; i < metadataJson.size(); i++) {
                    JSONObject meta = (JSONObject) metadataJson.get(i);

                    String name = meta.get("name").toString();
                    String type = meta.get("type").toString();
                    String value = meta.get("value").toString();

                    JSONObject out = new JSONObject();
                    out.put("type", type);
                    out.put("value", value);
                    outMetadata.put(name, out);
                }

                if (metadataJson.size() > 0) {
                    salida = outMetadata.toString();
                }
            } catch (Exception e) {
                LOGGER.error("BadRequest : metadata must contain Json object (name,type,value");
                throw new CygnusRuntimeError("BadRequest : metadata must contain Json object (name,type,value");
            } // try catch
        } // if
        return salida;
    }

    /**
     * @author PMO Santander Smart City – Ayuntamiento de Santander
     *
     */
    class ArcGISDomain {

        private String serviceFiware;
        private String servicePathFiware;
        private JSONArray jsonArray = new JSONArray();

        /**
         * @param serviceFiware
         * @param servicePathFiware
         * @param jsonArray
         */
        @SuppressWarnings("unchecked")
        ArcGISDomain(String serviceFiware, String servicePathFiware, JSONObject jsonObject) {
            super();
            this.serviceFiware = serviceFiware;
            if (StringUtils.isNotBlank(servicePathFiware)) {
                this.servicePathFiware = servicePathFiware.replace("/", "");
            } else {
                this.servicePathFiware = "";
            }

            this.jsonArray.add(jsonObject);
        }

        /**
         * Add JSONObject.
         * 
         * @param jsonObject
         */
        @SuppressWarnings("unchecked")
        public void addJSONObject(JSONObject jsonObject) {
            this.jsonArray.add(jsonObject);
        }

        /**
         * @return the serviceFiware
         */
        public String getServiceFiware() {
            return serviceFiware;
        }

        /**
         * @param serviceFiware
         *            the serviceFiware to set
         */
        public void setServiceFiware(String serviceFiware) {
            this.serviceFiware = serviceFiware;
        }

        /**
         * @return the servicePathFiware
         */
        public String getServicePathFiware() {
            return servicePathFiware;
        }

        /**
         * @param servicePathFiware
         *            the servicePathFiware to set
         */
        public void setServicePathFiware(String servicePathFiware) {
            this.servicePathFiware = servicePathFiware;
        }

        /**
         * @return the jsonArray
         */
        public JSONArray getJsonArray() {
            return jsonArray;
        }

        /**
         * @param jsonArray
         *            the jsonArray to set
         */
        public void setJsonArray(JSONArray jsonArray) {
            this.jsonArray = jsonArray;
        }

    }
} // NGSIArcGisSink
