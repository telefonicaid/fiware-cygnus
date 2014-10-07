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
 
package es.tid.fiware.fiwareconnectors.cygnus.sinks;

import es.tid.fiware.fiwareconnectors.cygnus.backends.ckan.CKANBackendImpl;
import es.tid.fiware.fiwareconnectors.cygnus.backends.ckan.CKANBackend;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextAttribute;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElement;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import es.tid.fiware.fiwareconnectors.cygnus.http.HttpClientFactory;
import es.tid.fiware.fiwareconnectors.cygnus.log.CygnusLogger;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Utils;
import java.sql.Timestamp;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.flume.Context;

/**
 * 
 * @author fermin
 *
 * CKAN sink for Orion Context Broker.
 *
 */
public class OrionCKANSink extends OrionSink {

    private Logger logger;
    private String apiKey;
    private String ckanHost;
    private String ckanPort;
    private String defaultDataset;
    private String orionUrl;
    private boolean rowAttrPersistence;
    private HttpClientFactory httpClientFactory;
    private CKANBackend persistenceBackend;
    
    /**
     * Constructor.
     */
    public OrionCKANSink() {
        super();
        logger = CygnusLogger.getLogger(OrionCKANSink.class);
    } // OrionCKANSink

    /**
     * Gets the CKAN host. It is protected due to it is only required for testing purposes.
     * @return The KCAN host
     */
    protected String getCKANHost() {
        return ckanHost;
    } // getCKANHost

    /**
     * Gets the CKAN port. It is protected due to it is only required for testing purposes.
     * @return The CKAN port
     */
    protected String getCKANPort() {
        return ckanPort;
    } // getCKANPort
    
    /**
     * Gets the CKAN API key. It is protected due to it is only required for testing purposes.
     * @return The CKAN API key
     */
    protected String getAPIKey() {
        return apiKey;
    } // getAPIKey
    
    /**
     * Gets the defaultDataset. It is protected due to it is only required for testing purposes.
     * @return The CKAN defaultDataset
     */
    protected String getDefaultDataset() {
        return defaultDataset;
    } // getDefaultDataset

    /**
     * Returns if the attribute persistence is row-based. It is protected due to it is only required for testing
     * purposes.
     * @return True if the attribute persistence is row-based, false otherwise
     */
    protected boolean getRowAttrPersistence() {
        return rowAttrPersistence;
    } // getRowAttrPersistence
    
    /**
     * Gets the Http client factory. It is protected due to it is only required for testing purposes.
     * @return The Http client factory
     */
    protected HttpClientFactory getHttpClientFactory() {
        return httpClientFactory;
    } // getHttpClientFactory
    
    /**
     * Returns the persistence backend. It is protected due to it is only required for testing purposes.
     * @return The persistence backend
     */
    protected CKANBackend getPersistenceBackend() {
        return persistenceBackend;
    } // getPersistenceBackend
    
    /**
     * Sets the Http client factory. It is protected due to it is only required for testing purposes.
     * @param httpClientFactory
     */
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    } // setHttpClientFactory
    
    /**
     * Sets the persistence backend. It is protected due to it is only required for testing purposes.
     * @param persistenceBackend
     */
    protected void setPersistenceBackend(CKANBackend persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    } // setPersistenceBackend
    
    @Override
    public void configure(Context context) {
        apiKey = context.getString("api_key", "nokey");
        logger.debug("[" + this.getName() + "] Reading configuration (api_key=" + apiKey + ")");
        ckanHost = context.getString("ckan_host", "localhost");
        logger.debug("[" + this.getName() + "] Reading configuration (ckan_host=" + ckanHost + ")");
        ckanPort = context.getString("ckan_port", "80");
        logger.debug("[" + this.getName() + "] Reading configuration (ckan_port=" + ckanPort + ")");
        defaultDataset = context.getString("default_dataset", "cygnus");
        logger.debug("[" + this.getName() + "] Reading configuration (default_dataset=" + defaultDataset + ")");
        orionUrl = context.getString("orion_url", "http://localhost:1026");
        logger.debug("[" + this.getName() + "] Reading configuration (orion_url=" + orionUrl + ")");
        rowAttrPersistence = context.getString("attr_persistence", "row").equals("row");
        logger.debug("[" + this.getName() + "] Reading configuration (attr_persistence=" + rowAttrPersistence + ")");
    } // configure

    @Override
    public void start() {
        // create a Http clients factory (no SSL)
        httpClientFactory = new HttpClientFactory(false);

        try {
            // create persistenceBackend backend
            persistenceBackend = new CKANBackendImpl(apiKey, ckanHost, ckanPort, defaultDataset, orionUrl);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        } // try catch

        super.start();
        logger.info("[" + this.getName() + "] Startup completed");
    } // start
    
    @Override
    void persist(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        // get some header values
        Long recvTimeTs = new Long(eventHeaders.get("timestamp")).longValue();
        String organization = eventHeaders.get(Constants.HEADER_SERVICE);
        String resourceName = eventHeaders.get(Constants.DESTINATION);
        
        // human readable version of the reception time
        String recvTime = new Timestamp(recvTimeTs).toString().replaceAll(" ", "T");

        // initialize organization
        persistenceBackend.initOrg(httpClientFactory.getHttpClient(false), organization);

        // iterate in the contextResponses
        ArrayList contextResponses = notification.getContextResponses();
        
        for (int i = 0; i < contextResponses.size(); i++) {
            ContextElementResponse contextElementResponse = (ContextElementResponse) contextResponses.get(i);
            ContextElement contextElement = contextElementResponse.getContextElement();
            String entityId = Utils.encode(contextElement.getId());
            String entityType = Utils.encode(contextElement.getType());
            logger.debug("[" + this.getName() + "] Processing context element (id=" + entityId + ", type=" + entityType
                    + ")");
            
            if (resourceName.length() > Constants.CKAN_RESOURCE_MAX_LEN) {
                logger.error("[" + this.getName() + "] Bad configuration (A CKAN resource name '" + resourceName
                        + "' has been built and its length is greater than " + Constants.CKAN_RESOURCE_MAX_LEN
                        + ". This resource name generation is based on the contatenation of the notified entity "
                        + "identifier, a '-' character and the notified entity type, thus adjust them)");
                throw new Exception("The length of the CKAN resource name '" + resourceName + "' is greater than "
                        + Constants.CKAN_RESOURCE_MAX_LEN);
            } // if

            // iterate on all this resourceName's attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();
            
            if (contextAttributes == null || contextAttributes.isEmpty()) {
                logger.warn("No attributes within the notified entity, nothing is done (id=" + entityId + ", type="
                        + entityType + ")");
                continue;
            } // if
            
            // this is used for storing the attribute's names and values when dealing with a per column attributes
            // persistence; in that case the persistence is not done attribute per attribute, but persisting all of them
            // at the same time
            HashMap<String, String> attrs = new HashMap<String, String>();

            // this is used for storing the attribute's names (sufixed with "-md") and metadata when dealing with a per
            // column attributes persistence; in that case the persistence is not done attribute per attribute, but
            // persisting all of them at the same time
            HashMap<String, String> mds = new HashMap<String, String>();

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(true);
                String attrMd = contextAttribute.getContextMetadata();
                logger.debug("[" + this.getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");

                if (rowAttrPersistence) {
                    logger.info("[" + this.getName() + "] Persisting data at OrionCKANSink. <" + recvTimeTs + ", "
                            + recvTime + ", " + organization + ", " + resourceName + ", " + attrName + ", " + attrType
                            + ", " + attrValue + ", " + attrMd + ">");
                    persistenceBackend.persist(httpClientFactory.getHttpClient(false), recvTimeTs, recvTime,
                            organization, resourceName, attrName, attrType, attrValue, attrMd);
                } else {
                    attrs.put(attrName, attrValue);
                    mds.put(attrName + "_md", attrMd);
                } // if else
            } // for

            // if the attribute persistence mode is per column, now is the time to insert a new row containing full
            // attribute list of name-values.
            if (!rowAttrPersistence) {
                logger.info("[" + this.getName() + "] Persisting data at OrionCKANSink. <" + recvTime + ", "
                        + organization + ", " + resourceName + ", " + attrs.toString() + ", " + mds.toString() + ">");
                persistenceBackend.persist(httpClientFactory.getHttpClient(false), recvTime, organization, resourceName,
                        attrs, mds);
            } // if
        } // for
    } // persist
    
} // OrionHDFSSink
