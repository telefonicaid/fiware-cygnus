/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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
package com.telefonica.iot.cygnus.utils;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author frb
 */
public final class CommonUtilsForTests {
    
    private static final int MAX_LEN_TEST_TRACE_HEAD = 55;
    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private CommonUtilsForTests() {
    } // CommonUtilsForTests
    
    /**
     * Gets a trace head.
     * @param originalHead
     * @return A trace head
     */
    public static String getTestTraceHead(String originalHead) {
        String traceHead = originalHead;
        traceHead += " " + StringUtils.repeat("-", MAX_LEN_TEST_TRACE_HEAD - originalHead.length());
        return traceHead;
    } // getTestTraceHead
    
    /**
     * Create a Flume event.
     * @return A Flume event
     */
    public static Event createEvent() {
        String eventData = createNotification().toJSONString();
        Map<String, String> eventHeaders = new HashMap<String, String>();
        eventHeaders.put("fiware-service", "default");
        eventHeaders.put("fiware-servicepath", "/");
        eventHeaders.put("fiware-correlator", "0123456789ABCDEF");
        eventHeaders.put("transaction-id", "0123456789ABCDEF");
        return EventBuilder.withBody(eventData.getBytes(), eventHeaders);
    } // createEvent
    
    /**
     * Creates a JSONObject-like notification.
     * @return A JSONObject-like notification
     */
    public static JSONObject createNotification() {
        JSONObject attribute = new JSONObject();
        attribute.put("name", "temperature");
        attribute.put("type", "centigrade");
        attribute.put("value", "26.5");
        JSONArray attributes = new JSONArray();
        attributes.add(attribute);
        JSONObject contextElement = new JSONObject();
        contextElement.put("attributes", attributes);
        contextElement.put("type", "Room");
        contextElement.put("isPattern", "false");
        contextElement.put("id", "room1");
        JSONObject statusCode = new JSONObject();
        statusCode.put("code", "200");
        statusCode.put("reasonPhrase", "OK");
        JSONObject contextResponse = new JSONObject();
        contextResponse.put("contextElement", contextElement);
        contextResponse.put("statusCode", statusCode);
        JSONArray contextResponses = new JSONArray();
        contextResponses.add(contextResponse);
        JSONObject notification = new JSONObject();
        notification.put("subscriptionId", "51c0ac9ed714fb3b37d7d5a8");
        notification.put("originator", "localhost");
        notification.put("contextResponses", contextResponses);
        return notification;
    } // createNotification
    
    /**
     * Creates a Flume context for Mongo/STH sinks.
     * @param collectionPrefix
     * @param dbPrefix
     * @param dataModel
     * @return A Flume context for Mongo/STH sinks.
     */
    public static Context createContextForMongoSTH(String collectionPrefix, String dbPrefix, String dataModel) {
        Context context = new Context();
        context.put("attr_persistence", "row");
        context.put("batch_size", "100");
        context.put("batch_timeout", "30");
        context.put("batch_ttl", "10");
        context.put("collection_prefix", collectionPrefix);
        context.put("collection_size", "0");
        context.put("data_expiration", "0");
        context.put("data_model", dataModel);
        context.put("db_prefix", dbPrefix);
        context.put("enable_grouping", "false");
        context.put("enable_lowercase", "false");
        context.put("max_documents", "0");
        context.put("mongo_hosts", "localhost:27017");
        context.put("mongo_password", "");
        context.put("mongo_username", "");
        context.put("should_hash", "false");
        return context;
    } // createContextForMongoSTH
    
} // CommonUtilsForTests
