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
    private String dataset;
    private HttpClientFactory httpClientFactory;
    private CKANBackend persistenceBackend;
    
    @Override
    public void configure(Context context) {
        logger = Logger.getLogger(OrionCKANSink.class);
        apiKey = context.getString("api_key", "nokey");
        ckanHost = context.getString("ckan_host", "localhost");
        ckanPort = context.getString("ckan_port", "80");
        dataset = context.getString("dataset", "cygnus");
    } // configure

    @Override
    public void start() {
        // create a Http clients factory (no SSL)
        httpClientFactory = new HttpClientFactory(false);

        try {
            // create and init persistenceBackend backend
            persistenceBackend = new CKANBackendImpl(apiKey, ckanHost, ckanPort, dataset);
            persistenceBackend.init(httpClientFactory.getHttpClient(false));
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        } // try catch

        super.start();
    } // start
    
    @Override
    void persist(String username, ArrayList contextResponses) throws Exception {
        // FIXME: username is given in order to support multi-tenancy... should be used instead of the current
        // cosmosUsername
        
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
                String attrValue = contextAttribute.getContextValue();
                Date date = new Date();

                // persist the data
                logger.info("Persisting data: <" + date + ", "
                        + entity + ", "
                        + attrName + ", "
                        + attrType + ", "
                        + attrValue + ">");
                persistenceBackend.persist(httpClientFactory.getHttpClient(false), date, entity,
                        attrName, attrType, attrValue);
            } // for
        } // for
    } // persist
    
} // OrionHDFSSink
