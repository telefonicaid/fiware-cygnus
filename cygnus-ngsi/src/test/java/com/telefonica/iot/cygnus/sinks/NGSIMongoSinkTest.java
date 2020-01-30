package com.telefonica.iot.cygnus.sinks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericColumnAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericRowAggregator;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;


import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NGSIMongoSinkTest {

    public static final String OPEN_ENTITY_CHAR = "(";
    public static final String CLOSE_ENTITY_CHAR = ")";
    public static final String SEPARATOR_CHAR = ",";
    public static final String QUOTATION_MARK_CHAR = "";

    public NGSIMongoSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }


    private NotifyContextRequest.ContextElement createContextElement() {
        NotifyContextRequest notifyContextRequest = new NotifyContextRequest();
        NotifyContextRequest.ContextMetadata contextMetadata = new NotifyContextRequest.ContextMetadata();
        contextMetadata.setName("location");
        contextMetadata.setType("string");
        contextMetadata.setContextMetadata(new JsonPrimitive("WGS84"));
        ArrayList<NotifyContextRequest.ContextMetadata> metadata = new ArrayList<>();
        metadata.add(contextMetadata);
        NotifyContextRequest.ContextAttribute contextAttribute1 = new NotifyContextRequest.ContextAttribute();
        contextAttribute1.setName("someName1");
        contextAttribute1.setType("someType1");
        contextAttribute1.setContextValue(new JsonPrimitive("-3.7167, 40.3833"));
        contextAttribute1.setContextMetadata(metadata);
        NotifyContextRequest.ContextAttribute contextAttribute2 = new NotifyContextRequest.ContextAttribute();
        contextAttribute2.setName("someName2");
        contextAttribute2.setType("someType2");
        contextAttribute2.setContextValue(new JsonPrimitive("someValue2"));
        contextAttribute2.setContextMetadata(null);
        ArrayList<NotifyContextRequest.ContextAttribute> attributes = new ArrayList<>();
        attributes.add(contextAttribute1);
        attributes.add(contextAttribute2);
        NotifyContextRequest.ContextElement contextElement = new NotifyContextRequest.ContextElement();
        contextElement.setId("someId");
        contextElement.setType("someType");
        contextElement.setIsPattern("false");
        contextElement.setAttributes(attributes);
        return contextElement;
    } // createContextElement

    private Context createContextforNativeTypes(String attrPersistence, String batchSize, String batchTime, String batchTTL,
                                                String dataModel, String enableEncoding, String enableGrouping, String enableLowercase, String host,
                                                String password, String port, String username, String cache, String attrNativeTypes) {
        Context context = new Context();
        context.put("attr_persistence", attrPersistence);
        context.put("batch_size", batchSize);
        context.put("batch_time", batchTime);
        context.put("batch_ttl", batchTTL);
        context.put("data_model", dataModel);
        context.put("enable_encoding", enableEncoding);
        context.put("enable_grouping", enableGrouping);
        context.put("enable_lowercase", enableLowercase);
        context.put("mysql_host", host);
        context.put("mysql_password", password);
        context.put("mysql_port", port);
        context.put("mysql_username", username);
        context.put("backend.enable_cache", cache);
        context.put("attr_native_types", attrNativeTypes);
        return context;
    } // createContext

    private NotifyContextRequest.ContextElement createContextElementForNativeTypes() {
        NotifyContextRequest notifyContextRequest = new NotifyContextRequest();
        NotifyContextRequest.ContextMetadata contextMetadata = new NotifyContextRequest.ContextMetadata();
        contextMetadata.setName("someString");
        contextMetadata.setType("string");
        ArrayList<NotifyContextRequest.ContextMetadata> metadata = new ArrayList<>();
        metadata.add(contextMetadata);
        NotifyContextRequest.ContextAttribute contextAttribute1 = new NotifyContextRequest.ContextAttribute();
        contextAttribute1.setName("someNumber");
        contextAttribute1.setType("number");
        contextAttribute1.setContextValue(new JsonPrimitive(2));
        contextAttribute1.setContextMetadata(null);
        NotifyContextRequest.ContextAttribute contextAttribute2 = new NotifyContextRequest.ContextAttribute();
        contextAttribute2.setName("somneBoolean");
        contextAttribute2.setType("Boolean");
        contextAttribute2.setContextValue(new JsonPrimitive(true));
        contextAttribute2.setContextMetadata(null);
        NotifyContextRequest.ContextAttribute contextAttribute3 = new NotifyContextRequest.ContextAttribute();
        contextAttribute3.setName("someDate");
        contextAttribute3.setType("DateTime");
        contextAttribute3.setContextValue(new JsonPrimitive("2016-09-21T01:23:00.00Z"));
        contextAttribute3.setContextMetadata(null);
        NotifyContextRequest.ContextAttribute contextAttribute4 = new NotifyContextRequest.ContextAttribute();
        contextAttribute4.setName("someGeoJson");
        contextAttribute4.setType("geo:json");
        contextAttribute4.setContextValue(new JsonPrimitive("{\"type\": \"Point\",\"coordinates\": [-0.036177,39.986159]}"));
        contextAttribute4.setContextMetadata(null);
        NotifyContextRequest.ContextAttribute contextAttribute5 = new NotifyContextRequest.ContextAttribute();
        contextAttribute5.setName("someJson");
        contextAttribute5.setType("json");
        contextAttribute5.setContextValue(new JsonPrimitive("{\"String\": \"string\"}"));
        contextAttribute5.setContextMetadata(null);
        NotifyContextRequest.ContextAttribute contextAttribute6 = new NotifyContextRequest.ContextAttribute();
        contextAttribute6.setName("someString");
        contextAttribute6.setType("string");
        contextAttribute6.setContextValue(new JsonPrimitive("foo"));
        contextAttribute6.setContextMetadata(null);
        NotifyContextRequest.ContextAttribute contextAttribute7 = new NotifyContextRequest.ContextAttribute();
        contextAttribute7.setName("someString2");
        contextAttribute7.setType("string");
        contextAttribute7.setContextValue(new JsonPrimitive(""));
        contextAttribute7.setContextMetadata(null);
        ArrayList<NotifyContextRequest.ContextAttribute> attributes = new ArrayList<>();
        attributes.add(contextAttribute1);
        attributes.add(contextAttribute2);
        attributes.add(contextAttribute3);
        attributes.add(contextAttribute4);
        attributes.add(contextAttribute5);
        attributes.add(contextAttribute6);
        attributes.add(contextAttribute7);
        NotifyContextRequest.ContextElement contextElement = new NotifyContextRequest.ContextElement();
        contextElement.setId("someId");
        contextElement.setType("someType");
        contextElement.setIsPattern("false");
        contextElement.setAttributes(attributes);
        return contextElement;
    } // createContextElementForNativeTypes

    public NGSIBatch setUpBatch() {
        String timestamp = "1461136795801";
        String correlatorId = "123456789";
        String transactionId = "123456789";
        String originalService = "someService";
        String originalServicePath = "somePath";
        String mappedService = "newService";
        String mappedServicePath = "newPath";
        String destination = "someDestination";
        Map<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
        headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
        headers.put(NGSIConstants.FLUME_HEADER_TRANSACTION_ID, transactionId);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        NotifyContextRequest.ContextElement contextElement = createContextElementForNativeTypes();
        NotifyContextRequest.ContextElement contextElement2 = createContextElement();
        NGSIEvent ngsiEvent = new NGSIEvent(headers, contextElement.toString().getBytes(), contextElement, null);
        NGSIEvent ngsiEvent2 = new NGSIEvent(headers, contextElement2.toString().getBytes(), contextElement2, null);
        NGSIBatch batch = new NGSIBatch();
        batch.addEvent(destination, ngsiEvent);
        batch.addEvent(destination, ngsiEvent2);
        return batch;
    }

    @Test
    public void testNativeTypeColumnBatch() throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError, CygnusBadContextData {
        NGSIBatch batch = setUpBatch();
        String destination = "someDestination";NGSIMongoSink ngsiMongoSink = new NGSIMongoSink();
        ngsiMongoSink.configure(createContextforNativeTypes("column", null, null, null, null, null, null, null, null, null, null, null, null, null));
        try {
            batch.startIterator();
            NGSIGenericAggregator aggregator = new NGSIGenericColumnAggregator();
            while (batch.hasNext()) {
                destination = batch.getNextDestination();
                ArrayList<NGSIEvent> events = batch.getNextEvents();
                aggregator.setService(events.get(0).getServiceForNaming(false));
                aggregator.setServicePathForData(events.get(0).getServicePathForData());
                aggregator.setServicePathForNaming(events.get(0).getServicePathForNaming(false, false));
                aggregator.setEntityForNaming(events.get(0).getEntityForNaming(false, false, false));
                aggregator.setEntityType(events.get(0).getEntityTypeForNaming(false, false));
                aggregator.setAttribute(events.get(0).getAttributeForNaming(false));
                aggregator.setDbName(ngsiMongoSink.buildDbName(aggregator.getService()));
                aggregator.setCollectionName(ngsiMongoSink.buildCollectionName(aggregator.getServicePathForNaming(), aggregator.getEntityForNaming(), aggregator.getAttribute()));
                aggregator.initialize(events.get(0));
                for (NGSIEvent event : events) {
                    aggregator.aggregate(event);
                }
            }
            ArrayList<String> keysToCrop = ngsiMongoSink.getKeysToCrop(false);
            LinkedHashMap<String, ArrayList<JsonElement>> cropedAggregation = NGSIUtils.cropLinkedHashMap(aggregator.getAggregationToPersist(), keysToCrop);
            ArrayList<JsonObject> jsonObjects = NGSIUtils.linkedHashMapToJsonList(cropedAggregation);
            ArrayList<Document> documents = new ArrayList<>();
            for (JsonObject jsonObject : jsonObjects) {
                documents.add(Document.parse(jsonObject.toString()));
            }
            System.out.println(documents);
        } catch (Exception e) {
            System.out.println(e);
            assertFalse(true);
        }
    }

    @Test
    public void testNativeTypeRowBatch() throws CygnusBadConfiguration, CygnusRuntimeError, CygnusPersistenceError, CygnusBadContextData {
        NGSIBatch batch = setUpBatch();
        String destination = "someDestination";NGSIMongoSink ngsiMongoSink = new NGSIMongoSink();
        ngsiMongoSink.configure(createContextforNativeTypes("row", null, null, null, null, null, null, null, null, null, null, null, null, null));
        try {
            batch.startIterator();
            while (batch.hasNext()) {
                destination = batch.getNextDestination();
                ArrayList<NGSIEvent> events = batch.getNextEvents();
                NGSIGenericAggregator aggregator = new NGSIGenericRowAggregator();
                aggregator.setService(events.get(0).getServiceForNaming(false));
                aggregator.setServicePathForData(events.get(0).getServicePathForData());
                aggregator.setServicePathForNaming(events.get(0).getServicePathForNaming(false, false));
                aggregator.setEntityForNaming(events.get(0).getEntityForNaming(false, false, false));
                aggregator.setEntityType(events.get(0).getEntityTypeForNaming(false, false));
                aggregator.setAttribute(events.get(0).getAttributeForNaming(false));
                aggregator.setDbName(ngsiMongoSink.buildDbName(aggregator.getService()));
                aggregator.setCollectionName(ngsiMongoSink.buildCollectionName(aggregator.getServicePathForNaming(), aggregator.getEntityForNaming(), aggregator.getAttribute()));
                aggregator.initialize(events.get(0));
                for (NGSIEvent event : events) {
                    aggregator.aggregate(event);
                }
                ArrayList<String> keysToCrop = ngsiMongoSink.getKeysToCrop(true);
                LinkedHashMap<String, ArrayList<JsonElement>> cropedAggregation = NGSIUtils.cropLinkedHashMap(aggregator.getAggregationToPersist(), keysToCrop);
                ArrayList<JsonObject> jsonObjects = NGSIUtils.linkedHashMapToJsonList(cropedAggregation);
                ArrayList<Document> documents = new ArrayList<>();
                for (JsonObject jsonObject : jsonObjects) {
                    documents.add(Document.parse(jsonObject.toString()));
                }
                System.out.println(documents);
            }
        } catch (Exception e) {
            System.out.println(e);
            assertFalse(true);
        }
    }

}
