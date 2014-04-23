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
 
package es.tid.fiware.orionconnectors.cosmosinjector;

import com.google.gson.Gson;
import es.tid.fiware.fiwareconnectors.cygnus.hive.HiveClient;
import es.tid.fiware.orionconnectors.cosmosinjector.containers.NotifyContextRequest;
import es.tid.fiware.orionconnectors.cosmosinjector.containers.NotifyContextRequest.ContextAttribute;
import es.tid.fiware.orionconnectors.cosmosinjector.containers.NotifyContextRequest.ContextElement;
import es.tid.fiware.orionconnectors.cosmosinjector.containers.NotifyContextRequest.ContextElementResponse;
import es.tid.fiware.orionconnectors.cosmosinjector.hdfs.HDFSBackend;
import es.tid.fiware.orionconnectors.cosmosinjector.hdfs.HttpFSBackend;
import es.tid.fiware.orionconnectors.cosmosinjector.hdfs.WebHDFSBackend;
import es.tid.fiware.orionconnectors.cosmosinjector.http.HttpClientFactory;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * 
 * @author frb
 * 
 * Custom HDFS sink for Orion Context Broker. There exists a default HDFS sink in Flume which serializes the data in
 * files, a file per event. This is not suitable for Orion, where the persisted files and its content must have specific
 * formats:
 *  - File names format: cygnus-<hdfs_user>-<hdfs_dataset>-<entity_id>-<entity_type>.txt
 *  - File lines format: {“ts”:”XXX”, “dateStr”:”XXX”, “entityId”:”XXX”, “entityType”:”XXX”, “attrName”:”XXX”, “attrType”:”XXX”,
 *                       “attrVlaue:”XXX”}
 * 
 * As can be seen, a file is created per each entity, containing all the historical values this entity's attributes
 * have had.
 * 
 * It is important to note that certain degree of reliability is achieved by using a rolling back mechanism in the
 * channel, i.e. an event is not removed from the channel until it is not appropriately persisted.
 */
public class OrionHDFSSink extends AbstractSink implements Configurable {

    private Logger logger;
    private String cosmosHost;
    private String cosmosPort;
    private String cosmosUsername;
    private String cosmosDataset;
    private String hdfsAPI;
    private HttpClientFactory httpClientFactory;
    private HDFSBackend persistenceBackend;
    
    @Override
    public void configure(Context context) {
        logger = Logger.getLogger(OrionHDFSSink.class);
        cosmosHost = context.getString("cosmos_host", "localhost");
        logger.debug("Reading cosmos_host=" + cosmosHost);
        cosmosPort = context.getString("cosmos_port", "14000");
        logger.debug("Reading cosmos_port=" + cosmosPort);
        cosmosUsername = context.getString("cosmos_username", "opendata");
        logger.debug("Reading cosmos_username=" + cosmosUsername);
        cosmosDataset = context.getString("cosmos_dataset", "unknown");
        logger.debug("Reading cosmos_dataset=" + cosmosDataset);
        hdfsAPI = context.getString("hdfs_api", "httpfs");
        logger.debug("Reading hdfs_api=" + hdfsAPI);
    } // configure

    @Override
    public void start() {
        // create a Http clients factory (no SSL)
        httpClientFactory = new HttpClientFactory(false);
        
        // create a persistenceBackend backend
        persistenceBackend = null;
        
        if (hdfsAPI.equals("httpfs")) {
            persistenceBackend = new HttpFSBackend(cosmosHost, cosmosPort, cosmosUsername, cosmosDataset);
            logger.debug("HttpFS persistence backend created");
        } else if (hdfsAPI.equals("webhdfs")) {
            persistenceBackend = new WebHDFSBackend(cosmosHost, cosmosPort, cosmosUsername, cosmosDataset);
            logger.debug("WebHDFS persistence backend created");
        } else {
            logger.error("Unrecognized HDFS API. The sink can start, but the data is not going to be persisted!");
        } // if else if
        
        try {
            // create (if not exists) the /user/myuser/mydataset folder and the related HiveQL external table
            if (persistenceBackend != null) {
                logger.info("Creating /user/" + cosmosUsername + "/" + cosmosDataset);
                persistenceBackend.createDir(httpClientFactory.getHttpClient(false), "");
                logger.info("Creating Hive external table " + cosmosUsername + "_"
                        + cosmosDataset.replaceAll("/", "_"));
                HiveClient hiveClient = new HiveClient(cosmosHost, "10000", cosmosUsername);
                String query = "create external table " + cosmosUsername + "_"
                        + cosmosDataset.replaceAll("/", "_") + " (ts bigint, dateStr string, entityId string, "
                        + "entityType string, attrName string, attrType string, attrValue string) row format serde "
                        + "'org.openx.data.jsonserde.JsonSerDe' location '/user/" + cosmosUsername + "/" + cosmosDataset
                        + "'";
                
                if (!hiveClient.doCreateTable(query)) {
                    logger.warn("The HiveQL external table could not be created, but Cygnus can continue working... "
                            + "Check your Hive/Shark installation");
                } // if
            } // if
        } catch (Exception e) {
            logger.error(e.getMessage());
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
                txn.commit();
                return Status.READY;
            } // if
            
            // persist the event
            logger.info("An event was taken from the channel, it must be persisted");
            persist(event);
            
            // specify the transaction has succeded
            txn.commit();
            status = Status.READY;
        } catch (Throwable t) {
            txn.rollback();
            logger.error(t.getMessage());
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
     * Given an event, it is persisted in HDFS by appending a new data line in the apropriate entity-related file.
     * 
     * @param event A Flume event containing the data to be persisted and certain metadata (headers).
     * @throws Exception
     */
    private void persist(Event event) throws Exception {
        String eventData = new String(event.getBody());
        Map<String, String> eventHeaders = event.getHeaders();
        
        // parse the eventData
        NotifyContextRequest notification = null;
        
        if (eventHeaders.get("content-type").contains("application/json")) {
            logger.debug("The content-type was application/json");
            Gson gson = new Gson();
            notification = gson.fromJson(eventData, NotifyContextRequest.class);
        } else if (eventHeaders.get("content-type").contains("application/xml")) {
            logger.debug("The content-type was application/xml");
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
            String fileName = "cygnus-" + cosmosUsername + "-" + cosmosDataset.replaceAll("/", "_") + "-"
                    + encode(contextElement.getId()) + "-" + encode(contextElement.getType()) + ".txt";
            
            // check if the file exists in HDFS right now, i.e. when its name has been got
            boolean fileExists = false;
            
            if (persistenceBackend.exists(httpClientFactory.getHttpClient(false), fileName)) {
                fileExists = true;
            } // if
            
            // iterate on all this entity's attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            for (int j = 0; j < contextAttributes.size(); j++) {
                ContextAttribute contextAttribute = contextAttributes.get(j);
                Date date = new Date();
                
                // create a line to be persisted, the timestamp must be devided by 1000 since it is expressed in terms
                // of microsenconds and we want miliseconds
                String line = "{\"timestamp\":\"" + (date.getTime() / 1000) + "\",\"dateStr\":\""
                        + new Timestamp(date.getTime()).toString().replaceAll(" ", "T") + "\",\"entityId\":\""
                        + encode(contextElement.getId()) + "\",\"entityType\":\"" + encode(contextElement.getType())
                        + "\",\"attrName\":\"" + encode(contextAttribute.getName()) + "\",\"attrType\":\""
                        + encode(contextAttribute.getType()) + "\",\"attrValue\":\""
                        + contextAttribute.getContextValue() + "\"}";
                logger.info("Persisting data. File: " + fileName + ", Data: " + line);
                
                // if the file exists, append the line to it; otherwise, create it with initial content and mark as
                // existing (this avoids checking if the file exists each time a line is going to be persisted)
                if (fileExists) {
                    persistenceBackend.append(httpClientFactory.getHttpClient(false), fileName, line);
                } else {
                    persistenceBackend.createFile(httpClientFactory.getHttpClient(false), fileName, line);
                    fileExists = true;
                } // if else
            } // for
        } // for
    } // persist
    
    /**
     * Encodes a string replacing ":" by "_".
     * 
     * @param in
     * @return The encoded version of the input string.
     */
    private String encode(String in) {
        return in.replaceAll(":", "_").replaceAll("-", "_");
    } // encode
    
} // OrionHDFSSink
