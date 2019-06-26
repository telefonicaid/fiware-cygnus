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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.flume.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.telefonica.iot.cygnus.backends.orion.OrionBackend;
import com.telefonica.iot.cygnus.backends.orion.OrionBackendImpl;
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
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.auth.keystone.KeyStoneUtils;
import com.telefonica.iot.cygnus.utils.auth.keystone.KeyStoneUtilsImpl;

/**
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 *
 */
public class NGSIOrionSink extends NGSISink {

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIOrionSink.class);
    private static final int MAX_CONNS = 50;
    private static final int MAX_CONNS_PER_ROUTE = 10;
    private String orionHost;
    private String orionPort;
    private boolean orionSsl;
    private String keystoneHost;
    private String keystonePort;
    private boolean keystoneSsl;
    private String orionToken;
    private String orionFiware;
    private String orionFiwarePath;
    private String orionUsername;
    private String orionPassword;
    private OrionBackend orionBackend;

    private KeyStoneUtils keyStoneUtils;

    /**
     * Constructor.
     */
    public NGSIOrionSink() {
        super();
    } // NGSIOrionSink

    /**
     * @return the orionHost
     */
    public String getOrionHost() {
        return orionHost;
    }

    /**
     * @param orionHost
     *            the orionHost to set
     */
    public void setOrionHost(String orionHost) {
        this.orionHost = orionHost;
    }

    /**
     * @return the orionPort
     */
    public String getOrionPort() {
        return orionPort;
    }

    /**
     * @param orionPort
     *            the orionPort to set
     */
    public void setOrionPort(String orionPort) {
        this.orionPort = orionPort;
    }

    /**
     * @return the orionSsl
     */
    public boolean isOrionSsl() {
        return orionSsl;
    }

    /**
     * @param orionSsl
     *            the orionSsl to set
     */
    public void setOrionSsl(boolean orionSsl) {
        this.orionSsl = orionSsl;
    }

    /**
     * @return the keystoneHost
     */
    public String getKeystoneHost() {
        return keystoneHost;
    }

    /**
     * @param keystoneHost
     *            the keystoneHost to set
     */
    public void setKeystoneHost(String keystoneHost) {
        this.keystoneHost = keystoneHost;
    }

    /**
     * @return the keystonePort
     */
    public String getKeystonePort() {
        return keystonePort;
    }

    /**
     * @param keystonePort
     *            the keystonePort to set
     */
    public void setKeystonePort(String keystonePort) {
        this.keystonePort = keystonePort;
    }

    /**
     * @return the keystoneSsl
     */
    public boolean isKeystoneSsl() {
        return keystoneSsl;
    }

    /**
     * @param keystoneSsl
     *            the keystoneSsl to set
     */
    public void setKeystoneSsl(boolean keystoneSsl) {
        this.keystoneSsl = keystoneSsl;
    }

    /**
     * @return the orionToken
     */
    public String getOrionToken() {
        return orionToken;
    }

    /**
     * @param orionToken
     *            the orionToken to set
     */
    public void setOrionToken(String orionToken) {
        this.orionToken = orionToken;
    }

    /**
     * @return the orionFiware
     */
    public String getOrionFiware() {
        return orionFiware;
    }

    /**
     * @param orionFiware
     *            the orionFiware to set
     */
    public void setOrionFiware(String orionFiware) {
        this.orionFiware = orionFiware;
    }

    /**
     * @return the orionFiwarePath
     */
    public String getOrionFiwarePath() {
        return orionFiwarePath;
    }

    /**
     * @param orionFiwarePath
     *            the orionFiwarePath to set
     */
    public void setOrionFiwarePath(String orionFiwarePath) {
        this.orionFiwarePath = orionFiwarePath;
    }

    /**
     * @return the orionUsername
     */
    public String getOrionUsername() {
        return orionUsername;
    }

    /**
     * @param orionUsername
     *            the orionUsername to set
     */
    public void setOrionUsername(String orionUsername) {
        this.orionUsername = orionUsername;
    }

    /**
     * @return the orionPassword
     */
    public String getOrionPassword() {
        return orionPassword;
    }

    /**
     * @param orionPassword
     *            the orionPassword to set
     */
    public void setOrionPassword(String orionPassword) {
        this.orionPassword = orionPassword;
    }

    /**
     * @return the orionBackend
     */
    public OrionBackend getOrionBackend() {
        return orionBackend;
    }

    /**
     * @param orionBackend
     *            the orionBackend to set
     */
    public void setOrionBackend(OrionBackendImpl orionBackend) {
        this.orionBackend = orionBackend;
    }

    /**
     * @param orionBackend
     *            the orionBackend to set
     */
    public void setOrionBackend(OrionBackend orionBackend) {
        this.orionBackend = orionBackend;
    }

    /**
     * @return the keyStoneUtils
     */
    public KeyStoneUtils getKeyStoneUtils() {
        return keyStoneUtils;
    }

    /**
     * @param keyStoneUtils
     *            the keyStoneUtils to set
     */
    public void setKeyStoneUtils(KeyStoneUtils keyStoneUtils) {
        this.keyStoneUtils = keyStoneUtils;
    }

    @Override
    public void configure(Context context) {
        LOGGER.info("Init config --> " + context);
        orionHost = context.getString("orion_host", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (orion_host=" + orionHost + ")");
        orionPort = context.getString("orion_port", "10027");
        int intPort = Integer.parseInt(orionPort);

        if ((intPort <= 0) || (intPort > 65535)) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (orion_port=" + orionPort + ") "
                    + "must be between 0 and 65535");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (orion_port=" + orionPort + ")");
        } // if else

        orionSsl = context.getBoolean("orion_ssl", false);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (orion_ssl=" + orionSsl + ")");

        keystoneHost = context.getString("keystone_host", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (orionHostKey=" + keystoneHost + ")");
        keystonePort = context.getString("keystone_port", "15001");
        int intPortKey = Integer.parseInt(keystonePort);

        if ((intPortKey <= 0) || (intPortKey > 65535)) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (orion_port_key=" + keystonePort + ") "
                    + "must be between 0 and 65535");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (orion_port_key=" + keystonePort + ")");
        } // if else

        keystoneSsl = context.getBoolean("keystone_ssl", false);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (keystone_ssl=" + keystoneSsl + ")");

        orionUsername = context.getString("orion_username", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (orion_username=" + orionUsername + ")");
        // FIXME: orionPassword should be read as a SHA1 and decoded here
        orionPassword = context.getString("orion_password", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (orion_password=" + orionPassword + ")");

        orionFiware = context.getString("orion_fiware", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (orion_fiware=" + orionFiware + ")");

        orionFiwarePath = context.getString("orion_fiware_path", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (orion_fiware_path=" + orionFiwarePath + ")");

        super.configure(context);
        LOGGER.info("Fin configure --> " + context);
    } // configure

    @Override
    public void start() {
        try {
            LOGGER.info("Init start()");
            keyStoneUtils = new KeyStoneUtilsImpl(getKeystoneHost(), getKeystonePort(), isKeystoneSsl(), MAX_CONNS,
                    MAX_CONNS_PER_ROUTE);
            orionToken = keyStoneUtils.getSessionToken(getOrionUsername(), getOrionPassword(), getOrionFiware(),
                    getOrionFiwarePath());
            orionBackend = new OrionBackendImpl(getOrionHost(), getOrionPort(), isOrionSsl(), MAX_CONNS,
                    MAX_CONNS_PER_ROUTE);
            LOGGER.debug("[" + this.getName() + "] Orion persistence backend created");
            LOGGER.info("Fin start() keyStoneUtils --> " + keyStoneUtils + ", orionToken --> " + orionToken
                    + ", orionBackend --> " + orionBackend);
        } catch (Exception e) {
            LOGGER.error("Error while creating the Orion persistence backend. Details=" + e.getLocalizedMessage());
            e.printStackTrace();
        } // try catch

        super.start();
    } // start

    @Override
    void persistBatch(NGSIBatch batch)
            throws CygnusBadConfiguration, CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
        LOGGER.info("Init persistBatch( batch --> " + batch + ")");
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
            OrionAggregator aggregator = new OrionAggregator();
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
    private class OrionAggregatorDomain {

        // string containing the data aggregation
        private String aggregation;

        private String service;

        private String subService;

        //
        private JSONObject entityJSON;

        /**
         * 
         */
        OrionAggregatorDomain() {
            aggregation = "";
        } // OrionAggregatorDomain

        /**
         * 
         * @param aggregation
         * @param service
         * @param subService
         * @param entityJSON
         */
        OrionAggregatorDomain(String aggregation, String service, String subService, JSONObject entityJSON) {
            super();
            this.aggregation = aggregation;
            this.service = service;
            this.subService = subService;
            this.entityJSON = entityJSON;
        }

        public String getAggregation() {
            return aggregation;
        } // getAggregation

        public JSONObject getEntityJSON() {
            return entityJSON;
        }

        /**
         * @return the service
         */
        public String getService() {
            return service;
        }

        /**
         * @param service
         *            the service to set
         */
        public void setService(String service) {
            this.service = service;
        }

        /**
         * @return the subService
         */
        public String getSubService() {
            return subService;
        }

        /**
         * @param subService
         *            the subService to set
         */
        public void setSubService(String subService) {
            this.subService = subService;
        }

        /**
         * @param aggregation
         *            the aggregation to set
         */
        public void setAggregation(String aggregation) {
            this.aggregation = aggregation;
        }

        /**
         * @param entityJSON
         *            the entityJSON to set
         */
        public void setEntityJSON(JSONObject entityJSON) {
            this.entityJSON = entityJSON;
        }

    }

    /**
     * Class for aggregating aggregation.
     */
    private class OrionAggregator {

        private List<OrionAggregatorDomain> listOrionAggregatorDomain = new ArrayList<OrionAggregatorDomain>();

        OrionAggregator() {
        } // OrionAggregator

        public void initialize(NGSIEvent event) {
        } // initialize

        /**
         * @return the listOrionAggregatorDomain
         */
        public List<OrionAggregatorDomain> getListOrionAggregatorDomain() {
            return listOrionAggregatorDomain;
        }

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
                }
                if (first) {
                    line += "\"" + entry.getKey() + "\"=\"" + entry.getValue() + "\"";
                    first = false;
                } else {
                    line += ", \"" + entry.getKey() + "\"=\"" + entry.getValue() + "\"";
                } // if else
            } // for

            line += "}";

            LOGGER.debug("[OrionSink] ContextElement ->" + event.getContextElement());
            LOGGER.debug("[OrionSink]  MappedCE ->" + event.getMappedCE());
            LOGGER.debug("[OrionSink] OriginalCE ->" + event.getOriginalCE());
            LOGGER.debug("[OrionSink] enableNameMappings state -> " + enableNameMappings);
            // get the getRecvTimeTs body
            ContextElement contextElement = null;
            if (!enableNameMappings) {
                contextElement = event.getContextElement();
            } else {
                contextElement = event.getMappedCE();
            }

            LOGGER.debug("[OrionSink] Selected context ->" + contextElement);

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
                        LOGGER.debug("[OrionSink-Agregate] Processing Attribute: " + contextAttribute.toString());
                        String attrName = contextAttribute.getName();
                        String attrType = contextAttribute.getType();
                        // get attribute value as is, quoted or not
                        String attrValue = contextAttribute.getContextValue();
                        String attrMetadata = parseJsonArrayStrToMetadataJson(contextAttribute.getContextMetadata());
                        LOGGER.debug("[OrionSink-Agregate] - Atribute (name --> " + attrName + ", type --> " + attrType
                                + ", value --> " + attrValue + ", metadata=" + attrMetadata + ")");
                        line += ", Processing attribute={name=" + attrName + ", type=" + attrType + ", value="
                                + attrValue + ", metadata=" + attrMetadata + "}, ";

                        // New attribute creation
                        JSONObject attribute = new JSONObject();
                        attribute.put("type", attrType);

                        LOGGER.debug("Tratando valor de atributo:  " + attrName + " --> " + attrValue);
                        try {
                            // If it is a JSON value....
                            JSONObject attrValueJSON = new JSONObject(attrValue);
                            attribute.put("value", attrValueJSON);
                        } catch (Exception e) {
                            try {
                                // If it is an array
                                JSONArray arrayValue = new JSONArray(attrValue);
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
                                            LOGGER.warn("[OrionSink] Unquoted String attribute: " + attrName + ":"
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
            } catch (JSONException e) {
                LOGGER.error("OrionSink Agregator: Error pharsing JSON BODY");
                entityJSON = null;
            } catch (CygnusRuntimeError error) {
                LOGGER.error("Error parsing metadata: " + error.getMessage());
                entityJSON = null;
            }
            line += "}";

            if (aggregation.isEmpty()) {
                aggregation = line;
            } else {
                aggregation += "," + line;
            } // if else

            OrionAggregatorDomain orionAggregatorDomain = new OrionAggregatorDomain(aggregation, service, subService,
                    entityJSON);
            listOrionAggregatorDomain.add(orionAggregatorDomain);
        } // aggregate

    } // OrionAggregator

    private void persistAggregation(OrionAggregator aggregator)
            throws CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
        if (aggregator != null && aggregator.getListOrionAggregatorDomain() != null
                && !aggregator.getListOrionAggregatorDomain().isEmpty()) {
            for (OrionAggregatorDomain orionAggregatorDomain : aggregator.getListOrionAggregatorDomain()) {

                String serviceFiware = orionAggregatorDomain.service;
                String servicePathFiware = orionAggregatorDomain.subService;
                LOGGER.debug(
                        "Persisting aggregation for service: " + serviceFiware + "  Subservice: " + servicePathFiware);
                String bodyJSON = orionAggregatorDomain.getEntityJSON().toString();
                LOGGER.debug("[" + this.getName() + "] Persisting data at NGSIOrionSink. Data (" + bodyJSON + ")");

                updateRemoteContext(bodyJSON, serviceFiware, servicePathFiware);
            }
        } else {
            LOGGER.error("OrionSink Agregator: Error pharsing JSON body");
            throw new CygnusBadContextData("OrionSink Agregator: Error pharsing JSON body");
        }

    } // persistAggregation

    /**
     * 
     * 
     * @param bodyJSONS
     * @throws CygnusRuntimeError
     */
    protected void updateRemoteContext(String bodyJSON, String serviceFiware, String servicePathFiware)
            throws CygnusRuntimeError {
        LOGGER.debug("init updateRemoteContext");
        try {
            if (orionBackend != null) {
                if (StringUtils.isBlank(serviceFiware)) {
                    serviceFiware = getOrionFiware();
                }
                if (StringUtils.isBlank(servicePathFiware)) {
                    servicePathFiware = getOrionFiwarePath();
                }
                String token = keyStoneUtils.getSessionToken(getOrionUsername(), getOrionPassword(), serviceFiware,
                        servicePathFiware);
                orionBackend.updateRemoteContext(bodyJSON, token, serviceFiware, servicePathFiware);
            } else {
                LOGGER.error("OrionBackend is null");
                throw new Exception("OrionBackend is null");
            }
        } catch (Exception e) {
            throw new CygnusRuntimeError("Data insertion error", "Exception", e.getMessage());
        } // try catch
    }

    /**
     * 
     * 
     * @param jsonArrayStr
     *            String containing JSON array metadata
     * @throws CygnusRuntimeError
     */
    private static String parseJsonArrayStrToMetadataJson(String jsonArrayStr) throws CygnusRuntimeError {

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
                for (int i = 0; i < metadataJson.length(); i++) {
                    JSONObject meta = (JSONObject) metadataJson.get(i);

                    String name = meta.get("name").toString();
                    String type = meta.get("type").toString();
                    String value = meta.get("value").toString();

                    JSONObject out = new JSONObject();
                    out.put("type", type);
                    out.put("value", value);
                    outMetadata.put(name, out);
                }

                if (metadataJson.length() > 0) {
                    salida = outMetadata.toString();
                }
            } catch (JSONException e) {
                LOGGER.error("BadRequest : metadata must contain Json object (name,type,value");
                throw new CygnusRuntimeError("BadRequest : metadata must contain Json object (name,type,value");
            }
        } // if
        return salida;
    }
} // NGSIOrionSink
