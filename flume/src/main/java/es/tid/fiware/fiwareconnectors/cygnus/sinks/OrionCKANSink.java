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

import com.google.gson.Gson;
import es.tid.fiware.fiwareconnectors.cygnus.backends.ckan.CKANBackendImpl;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextAttribute;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElement;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import es.tid.fiware.fiwareconnectors.cygnus.backends.ckan.CKANBackend;
import es.tid.fiware.fiwareconnectors.cygnus.http.HttpClientFactory;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * 
 * @author fermin
 *
 * CKAN sink for Orion Context Broker.
 *
 */
public class OrionCKANSink extends AbstractSink implements Configurable {

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
    public void stop() {
        httpClientFactory = null;
        persistenceBackend = null;
        super.stop();
    } // stop

    @Override
    // FIXME: this code is common with OrionHDFSSInk. Maybe we should define a common parent class
    // OrionSink and made OrionCKANSink and OrionHDFSSink derivated classes from it
    public Status process() throws EventDeliveryException {
        Status status = null;

        // start transaction
        Channel ch = getChannel();
        Transaction txn = ch.getTransaction();
        txn.begin();

        try {
            // get an event
            Event event = ch.take();
            
            if (event == null) {
                // prematurely close the transaction
                txn.close();
                return Status.BACKOFF;
            } // if
            
            // persist the event
            persist(event);
            
            // specify the transaction has succeed
            txn.commit();
            status = Status.READY;
        } catch (Throwable t) {
            // specify something went wrong
            txn.rollback();
            status = Status.BACKOFF;

            // rethrow all errors
            if (t instanceof Error) {
                throw (Error) t;
            } // if
        } finally {
            // close the transaction
            txn.close();
        } // try catch finally

        return status;
    } // process
    
    /**
     * Given an event, it is persisted in CKAN using the datastore associated with the entity
     * 
     * @param event A Flume event containing the data to be persisted and certain metadata (headers).
     * @throws Exception
     */
    // FIXME: most of this code could be also factorized in a common sink class
    private void persist(Event event) throws Exception {
        String eventData = new String(event.getBody());
        Map<String, String> eventHeaders = event.getHeaders();
        
        // parse the eventData
        NotifyContextRequest notification = null;
        
        if (eventHeaders.get("content-type").contains("application/json")) {
            Gson gson = new Gson();
            notification = gson.fromJson(eventData, NotifyContextRequest.class);
        } else if (eventHeaders.get("content-type").contains("application/xml")) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(eventData));
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            notification = new NotifyContextRequest(doc);
        } else {
            throw new Exception("Unrecognized content type (not Json nor XML)");
        } // if else if

        // process the event data
        ArrayList<ContextElementResponse> contextResponses = notification.getContextResponse();

        for (int i = 0; i < contextResponses.size(); i++) {
            ContextElementResponse contextElementResponse = contextResponses.get(i);
            ContextElement contextElement = contextElementResponse.getContextElement();
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            for (int j = 0; j < contextAttributes.size(); j++) {
                ContextAttribute contextAttribute = contextAttributes.get(j);
                String entity = encode(contextElement.getId()) + "-" + encode(contextElement.getType());
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue();
                Date date = new Date();

                logger.info("Persisting data: <" + date + ", " +
                        entity + ", " +
                        attrName + ", " +
                        attrType + ", " +
                        attrValue + ">");

                persistenceBackend.persist(httpClientFactory.getHttpClient(false), date, entity,
                        attrName, attrType, attrValue);

            } // for
        } // for
    } // persist
    
    /**
     * Encodes a string replacing ":" by "_".
     * 
     * @param in
     * @return The encoded version of the input string.
     */
    // FIXME: factorize in common class
    private String encode(String in) {
        return in.replaceAll(":", "_").replaceAll("-", "_");
    } // encode
    
} // OrionHDFSSink
