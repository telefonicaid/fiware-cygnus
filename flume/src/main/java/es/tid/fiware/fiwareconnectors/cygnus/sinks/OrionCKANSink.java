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
 
package es.tid.fiware.fiwareconnectors.cygnus.sinks;

import es.tid.fiware.fiwareconnectors.cygnus.backends.ckan.CKANBackendImpl;
import es.tid.fiware.fiwareconnectors.cygnus.backends.ckan.CKANBackend;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextAttribute;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElement;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import es.tid.fiware.fiwareconnectors.cygnus.http.HttpClientFactory;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Utils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import org.apache.flume.Context;

/**
 * 
 * @author frb
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
    private HttpClientFactory httpClientFactory;
    private CKANBackend persistenceBackend;

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
    
    /**
     * Sets the time helper. It is protected due to it is only required for testing purposes.
     * @param timeHelper
     */
    protected void setTimeHelper(TimeHelper timeHelper) {
        this.timeHelper = timeHelper;
    } // setTimeHelper
    
    @Override
    public void configure(Context context) {
        logger = Logger.getLogger(OrionCKANSink.class);
        apiKey = context.getString("api_key", "nokey");
        ckanHost = context.getString("ckan_host", "localhost");
        ckanPort = context.getString("ckan_port", "80");
        defaultDataset = context.getString("default_dataset", "cygnus");
    } // configure

    @Override
    public void start() {
        // create a Http clients factory (no SSL)
        httpClientFactory = new HttpClientFactory(false);

        try {
            // create and init persistenceBackend backend
            persistenceBackend = new CKANBackendImpl(apiKey, ckanHost, ckanPort, defaultDataset);
            //persistenceBackend.init(httpClientFactory.getHttpClient(false));
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        } // try catch

        super.start();
    } // start
    
    @Override
    void persist(String organization, ArrayList contextResponses) throws Exception {

        // initialize organization
        persistenceBackend.initOrg(httpClientFactory.getHttpClient(false), organization);

        // iterate in the contextResponses
        for (int i = 0; i < contextResponses.size(); i++) {
            ContextElementResponse contextElementResponse = (ContextElementResponse) contextResponses.get(i);
            ContextElement contextElement = contextElementResponse.getContextElement();
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            for (int j = 0; j < contextAttributes.size(); j++) {
                ContextAttribute contextAttribute = contextAttributes.get(j);
                String entity = Utils.encode(contextElement.getId()) + "-" + Utils.encode(contextElement.getType());
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                Date date = new Date();

                // persist the data
                logger.info("Persisting data: <" + date + ", "
                        + organization + ", "
                        + entity + ", "
                        + attrName + ", "
                        + attrType + ", "
                        + attrValue + ">");
                persistenceBackend.persist(httpClientFactory.getHttpClient(false), date, organization, entity,
                        attrName, attrType, attrValue);
            } // for
        } // for
    } // persist
    
} // OrionHDFSSink
