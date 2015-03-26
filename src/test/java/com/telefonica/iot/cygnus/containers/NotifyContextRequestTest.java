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
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.containers;

import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import static org.junit.Assert.*; // this is required by "fail" like assertions
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.StatusCode;
import com.telefonica.iot.cygnus.utils.TestUtils;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NotifyContextRequestTest {
    
    private Logger logger;
    private final String notifyXMLSimple = ""
            + "<notifyContextRequest>"
            +   "<subscriptionId>51c0ac9ed714fb3b37d7d5a8</subscriptionId>"
            +   "<originator>localhost</originator>"
            +   "<contextResponseList>"
            +     "<contextElementResponse>"
            +       "<contextElement>"
            +         "<entityId type=\"Room\" isPattern=\"false\">"
            +           "<id>Room1</id>"
            +         "</entityId>"
            +         "<contextAttributeList>"
            +           "<contextAttribute>"
            +             "<name>temperature</name>"
            +             "<type>centigrade</type>"
            +             "<contextValue>26.5</contextValue>"
            +           "</contextAttribute>"
            +         "</contextAttributeList>"
            +       "</contextElement>"
            +       "<statusCode>"
            +         "<code>200</code>"
            +         "<reasonPhrase>OK</reasonPhrase>"
            +       "</statusCode>"
            +     "</contextElementResponse>"
            +   "</contextResponseList>"
            + "</notifyContextRequest>";
    private final String notifyXMLCompound = ""
            + "<notifyContextRequest>"
            +   "<subscriptionId>51c0ac9ed714fb3b37d7d5a8</subscriptionId>"
            +   "<originator>localhost</originator>"
            +   "<contextResponseList>"
            +     "<contextElementResponse>"
            +       "<contextElement>"
            +         "<entityId type=\"Room\" isPattern=\"false\">"
            +           "<id>Room2</id>"
            +         "</entityId>"
            +         "<contextAttributeList>"
            +           "<contextAttribute>"
            +             "<name>field1</name>"
            +             "<type>type1</type>"
            +             "<contextValue>"
            +               "<a>1</a>"
            +               "<b>2</b>"
            +             "</contextValue>"
            +           "</contextAttribute>"
            +           "<contextAttribute>"
            +             "<name>field2</name>"
            +             "<type>type2</type>"
            +             "<contextValue type=\"vector\">"
            +               "<item>v1</item>"
            +               "<item>v2</item>"
            +             "</contextValue>"
            +           "</contextAttribute>"
            +         "</contextAttributeList>"
            +       "</contextElement>"
            +       "<statusCode>"
            +         "<code>200</code>"
            +         "<reasonPhrase>OK</reasonPhrase>"
            +       "</statusCode>"
            +     "</contextElementResponse>"
            +   "</contextResponseList>"
            + "</notifyContextRequest>";
    private final String notifyXMLCompoundNested = ""
            + "<notifyContextRequest>"
            +   "<subscriptionId>51c0ac9ed714fb3b37d7d5a8</subscriptionId>"
            +   "<originator>localhost</originator>"
            +   "<contextResponseList>"
            +     "<contextElementResponse>"
            +       "<contextElement>"
            +         "<entityId type=\"Room\" isPattern=\"false\">"
            +           "<id>Room2</id>"
            +         "</entityId>"
            +         "<contextAttributeList>"
            +           "<contextAttribute>"
            +             "<name>field1</name>"
            +             "<type>type1</type>"
            +             "<contextValue>"
            +               "<a>"
            +                 "<x>"
            +                   "<y>v1</y>"
            +                 "</x>"
            +                 "<z>v2</z>"
            +               "</a>"
            +               "<b>v3</b>"
            +             "</contextValue>"
            +           "</contextAttribute>"
            +           "<contextAttribute>"
            +             "<name>field2</name>"
            +             "<type>type2</type>"
            +             "<contextValue type=\"vector\">"
            +               "<item type=\"vector\">"
            +                 "<item type=\"vector\">"
            +                   "<item>v1</item>"
            +                   "<item>v2</item>"
            +                 "</item>"
            +               "</item>"
            +               "<item>"
            +                 "<x type=\"vector\">"
            +                   "<item>v3</item>"
            +                   "<item>v4</item>"
            +                 "</x>"
            +               "</item>"
            +             "</contextValue>"
            +           "</contextAttribute>"
            +         "</contextAttributeList>"
            +       "</contextElement>"
            +       "<statusCode>"
            +         "<code>200</code>"
            +         "<reasonPhrase>OK</reasonPhrase>"
            +       "</statusCode>"
            +     "</contextElementResponse>"
            +   "</contextResponseList>"
            + "</notifyContextRequest>";
    private final String notifyXMLMetadata = ""
            + "<notifyContextRequest>"
            +   "<subscriptionId>51c0ac9ed714fb3b37d7d5a8</subscriptionId>"
            +   "<originator>localhost</originator>"
            +   "<contextResponseList>"
            +     "<contextElementResponse>"
            +       "<contextElement>"
            +         "<entityId type=\"Room\" isPattern=\"false\">"
            +           "<id>Room1</id>"
            +         "</entityId>"
            +         "<contextAttributeList>"
            +           "<contextAttribute>"
            +             "<name>temperature</name>"
            +             "<type>centigrade</type>"
            +             "<contextValue>26.5</contextValue>"
            +             "<metadata>"
            +               "<contextMetadata>"
            +                 "<name>ID</name>"
            +                 "<type>string</type>"
            +                 "<value>wall</value>"
            +               "</contextMetadata>"
            +             "</metadata>"
            +           "</contextAttribute>"
            +         "</contextAttributeList>"
            +       "</contextElement>"
            +       "<statusCode>"
            +         "<code>200</code>"
            +         "<reasonPhrase>OK</reasonPhrase>"
            +       "</statusCode>"
            +     "</contextElementResponse>"
            +   "</contextResponseList>"
            + "</notifyContextRequest>";
    private final String notifyXMLSimpleUnordered = ""
            + "<notifyContextRequest>"
            +   "<subscriptionId>51c0ac9ed714fb3b37d7d5a8</subscriptionId>"
            +   "<originator>localhost</originator>"
            +   "<contextResponseList>"
            +     "<contextElementResponse>"
            +       "<statusCode>"
            +         "<code>200</code>"
            +         "<reasonPhrase>OK</reasonPhrase>"
            +       "</statusCode>"
            +       "<contextElement>"
            +         "<entityId type=\"Room\" isPattern=\"false\">"
            +           "<id>Room1</id>"
            +         "</entityId>"
            +         "<contextAttributeList>"
            +           "<contextAttribute>"
            +             "<name>temperature</name>"
            +             "<type>centigrade</type>"
            +             "<contextValue>26.5</contextValue>"
            +           "</contextAttribute>"
            +         "</contextAttributeList>"
            +       "</contextElement>"
            +     "</contextElementResponse>"
            +   "</contextResponseList>"
            + "</notifyContextRequest>";
    private final String notifyXMLSimpleNullAttrs = ""
            + "<notifyContextRequest>"
            +   "<subscriptionId>51c0ac9ed714fb3b37d7d5a8</subscriptionId>"
            +   "<originator>localhost</originator>"
            +   "<contextResponseList>"
            +     "<contextElementResponse>"
            +       "<contextElement>"
            +         "<entityId type=\"Room\" isPattern=\"false\">"
            +           "<id>Room1</id>"
            +         "</entityId>"
            +       "</contextElement>"
            +       "<statusCode>"
            +         "<code>200</code>"
            +         "<reasonPhrase>OK</reasonPhrase>"
            +       "</statusCode>"
            +     "</contextElementResponse>"
            +   "</contextResponseList>"
            + "</notifyContextRequest>";
    private final String notifyJsonSimple = ""
            + "{"
            +   "\"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\","
            +   "\"originator\" : \"localhost\","
            +   "\"contextResponses\" : ["
            +     "{"
            +       "\"contextElement\" : {"
            +         "\"attributes\" : ["
            +           "{"
            +             "\"name\" : \"temperature\","
            +             "\"type\" : \"centigrade\","
            +             "\"value\" : \"26.5\""
            +           "}"
            +         "],"
            +         "\"type\" : \"Room\","
            +         "\"isPattern\" : \"false\","
            +         "\"id\" : \"Room1\""
            +       "},"
            +       "\"statusCode\" : {"
            +         "\"code\" : \"200\","
            +         "\"reasonPhrase\" : \"OK\""
            +       "}"
            +     "}"
            +   "]"
            + "}";
    private final String notifyJsonCompound = ""
            + "{"
            +   "\"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\","
            +   "\"originator\" : \"localhost\","
            +   "\"contextResponses\" : ["
            +     "{"
            +       "\"contextElement\" : {"
            +         "\"attributes\" : ["
            +           "{"
            +             "\"name\" : \"field1\","
            +             "\"type\" : \"type1\","
            +             "\"value\" : { \"a\": \"1\", \"b\": \"2\" }"
            +           "},"
            +           "{"
            +             "\"name\" : \"field2\","
            +             "\"type\" : \"type2\","
            +             "\"value\" : [ \"v1\", \"v2\" ]"
            +           "}"
            +         "],"
            +         "\"type\" : \"Room\","
            +         "\"isPattern\" : \"false\","
            +         "\"id\" : \"Room2\""
            +       "},"
            +       "\"statusCode\" : {"
            +         "\"code\" : \"200\","
            +         "\"reasonPhrase\" : \"OK\""
            +       "}"
            +     "}"
            +   "]"
            + "}";
    private final String notifyJsonCompoundNested = ""
            + "{"
            +   "\"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\","
            +   "\"originator\" : \"localhost\","
            +   "\"contextResponses\" : ["
            +     "{"
            +       "\"contextElement\" : {"
            +         "\"attributes\" : ["
            +           "{"
            +             "\"name\" : \"field1\","
            +             "\"type\" : \"type1\","
            +             "\"value\" : {\"a\":{\"x\":{\"y\":\"v1\"},\"z\":\"v2\"},\"b\":\"v3\"}"
            +           "},"
            +           "{"
            +             "\"name\" : \"field2\","
            +             "\"type\" : \"type2\","
            +             "\"value\" : [[[\"v1\",\"v2\"]],{\"x\":[\"v3\",\"v4\"]}]"
            +           "}"
            +         "],"
            +         "\"type\" : \"Room\","
            +         "\"isPattern\" : \"false\","
            +         "\"id\" : \"Room2\""
            +       "},"
            +       "\"statusCode\" : {"
            +         "\"code\" : \"200\","
            +         "\"reasonPhrase\" : \"OK\""
            +       "}"
            +     "}"
            +   "]"
            + "}";
    private final String notifyJsonMetadata = ""
            + "{"
            +   "\"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\","
            +   "\"originator\" : \"localhost\","
            +   "\"contextResponses\" : ["
            +     "{"
            +       "\"contextElement\" : {"
            +         "\"attributes\" : ["
            +           "{"
            +             "\"name\" : \"temperature\","
            +             "\"type\" : \"centigrade\","
            +             "\"value\" : \"26.5\","
            +             "\"metadatas\": ["
            +               "{"
            +                 "\"name\": \"ID\","
            +                 "\"type\": \"string\","
            +                 "\"value\": \"ground\""
            +               "}"
            +             "]"
            +           "}"
            +         "],"
            +         "\"type\" : \"Room\","
            +         "\"isPattern\" : \"false\","
            +         "\"id\" : \"Room1\""
            +       "},"
            +       "\"statusCode\" : {"
            +         "\"code\" : \"200\","
            +         "\"reasonPhrase\" : \"OK\""
            +       "}"
            +     "}"
            +   "]"
            + "}";
    private final String notifyJsonSimpleUnordered = ""
            + "{"
            +   "\"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\","
            +   "\"originator\" : \"localhost\","
            +   "\"contextResponses\" : ["
            +     "{"
            +       "\"statusCode\" : {"
            +         "\"code\" : \"200\","
            +         "\"reasonPhrase\" : \"OK\""
            +       "},"
            +       "\"contextElement\" : {"
            +         "\"attributes\" : ["
            +           "{"
            +             "\"type\" : \"centigrade\","
            +             "\"name\" : \"temperature\","
            +             "\"value\" : \"26.5\""
            +           "}"
            +         "],"
            +         "\"type\" : \"Room\","
            +         "\"isPattern\" : \"false\","
            +         "\"id\" : \"Room1\""
            +       "}"
            +     "}"
            +   "]"
            + "}";
    private final String notifyJsonSimpleNullAttrs = ""
            + "{"
            +   "\"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\","
            +   "\"originator\" : \"localhost\","
            +   "\"contextResponses\" : ["
            +     "{"
            +       "\"statusCode\" : {"
            +         "\"code\" : \"200\","
            +         "\"reasonPhrase\" : \"OK\""
            +       "},"
            +       "\"contextElement\" : {"
            +         "\"type\" : \"Room\","
            +         "\"isPattern\" : \"false\","
            +         "\"id\" : \"Room1\""
            +       "}"
            +     "}"
            +   "]"
            + "}";

    /**
     * Test of getSubscriptionId method, of class NotifyContextRequest.
     */
    @Test
    public void testGetSubscriptionId() {
        String expResult = "51c0ac9ed714fb3b37d7d5a8";
        // test case for nofity-xml-simple
        System.out.println("getSubscriptionId (notify-xml-simple)");
        NotifyContextRequest instance = TestUtils.createXMLNotifyContextRequest(notifyXMLSimple);
        String result = instance.getSubscriptionId();
        assertEquals(expResult, result);

        // test case for notify-xml-compound
        System.out.println("getSubscriptionId (notify-xml-compound)");
        instance = TestUtils.createXMLNotifyContextRequest(notifyXMLCompound);
        result = instance.getSubscriptionId();
        assertEquals(expResult, result);

        // test case for notify-xml-metadata
        System.out.println("getSubscriptionId (notify-xml-metadata)");
        instance = TestUtils.createXMLNotifyContextRequest(notifyXMLMetadata);
        result = instance.getSubscriptionId();
        assertEquals(expResult, result);
        
        // test case for nofity-json-simple
        System.out.println("getSubscriptionId (notify-json-simple)");
        instance = TestUtils.createJsonNotifyContextRequest(notifyJsonSimple);
        result = instance.getSubscriptionId();
        assertEquals(expResult, result);
        
        // test case for nofify-json-compound
        System.out.println("getSubscriptionId (notify-json-compound)");
        instance = TestUtils.createJsonNotifyContextRequest(notifyJsonCompound);
        result = instance.getSubscriptionId();
        assertEquals(expResult, result);
        
        // test case for nofify-json-metadata
        System.out.println("getSubscriptionId (notify-json-metadata)");
        instance = TestUtils.createJsonNotifyContextRequest(notifyJsonMetadata);
        result = instance.getSubscriptionId();
        assertEquals(expResult, result);
    } // testGetSubscriptionID

    /**
     * Test of getOriginator method, of class NotifyContextRequest.
     */
    @Test
    public void testGetOriginator() {
        String expResult = "localhost";
        // test case for nofity-xml-simple
        System.out.println("getOriginator (notify-xml-simple)");
        NotifyContextRequest instance = TestUtils.createXMLNotifyContextRequest(notifyXMLSimple);
        String result = instance.getOriginator();
        assertEquals(expResult, result);
        
        // test case for notify-xml-compound
        System.out.println("getOriginator (notify-xml-compound)");
        instance = TestUtils.createXMLNotifyContextRequest(notifyXMLCompound);
        result = instance.getOriginator();
        assertEquals(expResult, result);
        
        // test case for notify-xml-metadata
        System.out.println("getOriginator (notify-xml-metadata)");
        instance = TestUtils.createXMLNotifyContextRequest(notifyXMLMetadata);
        result = instance.getOriginator();
        assertEquals(expResult, result);
        
        // test case for nofity-json-simple
        System.out.println("getOriginator (notify-json-simple)");
        instance = TestUtils.createJsonNotifyContextRequest(notifyJsonSimple);
        result = instance.getOriginator();
        assertEquals(expResult, result);
        
        // test case for nofify-json-compound
        System.out.println("getOriginator (notify-json-compound)");
        instance = TestUtils.createJsonNotifyContextRequest(notifyJsonCompound);
        result = instance.getOriginator();
        assertEquals(expResult, result);
        
        // test case for nofify-json-metadata
        System.out.println("getOriginator (notify-json-metadata)");
        instance = TestUtils.createJsonNotifyContextRequest(notifyJsonMetadata);
        result = instance.getOriginator();
        assertEquals(expResult, result);
    } // testGetOriginator

    /**
     * Test of getContextResponses method, of class NotifyContextRequest.
     */
    @Test
    public void testGetContextResponses() {
        testGetCxtResXMLSimple();
        testGetCxtResXMLCompound();
        testGetCxtResXMLCompoundNested();
        testGetCxtResXMLMd();
        testGetCxtResXMLSimpleUnordered();
        testGetCxtResXMLSimpleNullAttrs();
        testGetCxtResJsonSimple();
        testGetCxtResJsonCompound();
        testGetCxtResJsonCompoundNested();
        testGetCxtResJsonMd();
        testGetCxtResJsonSimpleUnordered();
        testGetCxtResJsonSimpleNullAttrs();
    } // testGetContextResponses

    /**
     * Sub-test case for nofity-xml-simple.
     */
    private void testGetCxtResXMLSimple() {
        System.out.println("getOriginator (notify-xml-simple)");
        NotifyContextRequest instance = TestUtils.createXMLNotifyContextRequest(notifyXMLSimple);
        ArrayList<ContextElementResponse> cerList = instance.getContextResponses();
        assertTrue(cerList != null);
        
        ContextElementResponse cer = cerList.get(0);
        
        ContextElement ce = cer.getContextElement();
        assertEquals(ce.getId(), "Room1");
        assertEquals(ce.getIsPattern(), "false");
        assertEquals(ce.getType(), "Room");
        ArrayList<ContextAttribute> caList = ce.getAttributes();
        ContextAttribute ca = caList.get(0);
        assertEquals(ca.getName(), "temperature");
        assertEquals(ca.getType(), "centigrade");
        assertEquals(ca.getContextValue(true), "\"26.5\"");
        assertEquals(ca.getContextMetadata(), "[]");

        StatusCode sc = cer.getStatusCode();
        assertEquals(sc.getCode(), "200");
        assertEquals(sc.getReasonPhrase(), "OK");
    } // testGetCxtResXMLSimple

    /**
     * Sub-test case for nofity-xml-compound.
     */
    private void testGetCxtResXMLCompound() {
        System.out.println("getOriginator (notify-xml-compound)");
        NotifyContextRequest instance = TestUtils.createXMLNotifyContextRequest(notifyXMLCompound);
        ArrayList<ContextElementResponse> cerList = instance.getContextResponses();
        assertTrue(cerList != null);
        
        ContextElementResponse cer = cerList.get(0);
        
        ContextElement ce = cer.getContextElement();
        assertEquals("Room2", ce.getId());
        assertEquals("false", ce.getIsPattern());
        assertEquals("Room", ce.getType());
        ArrayList<ContextAttribute> caList = ce.getAttributes();
        ContextAttribute ca = caList.get(0);
        assertEquals("field1", ca.getName());
        assertEquals("type1", ca.getType());
        assertEquals("{\"a\":\"1\",\"b\":\"2\"}", ca.getContextValue(true));
        assertEquals("[]", ca.getContextMetadata());
        ca = caList.get(1);
        assertEquals("field2", ca.getName());
        assertEquals("type2", ca.getType());
        assertEquals("[\"v1\",\"v2\"]", ca.getContextValue(true));
        assertEquals("[]", ca.getContextMetadata());

        StatusCode sc = cer.getStatusCode();
        assertEquals("200", sc.getCode());
        assertEquals("OK", sc.getReasonPhrase());
    } // testGetCxtResXMLCompound
    
    /**
     * Sub-test case for nofity-xml-compound.
     */
    private void testGetCxtResXMLCompoundNested() {
        System.out.println("getOriginator (notify-xml-compound-nested)");
        NotifyContextRequest instance = TestUtils.createXMLNotifyContextRequest(notifyXMLCompoundNested);
        ArrayList<ContextElementResponse> cerList = instance.getContextResponses();
        assertTrue(cerList != null);
        
        ContextElementResponse cer = cerList.get(0);
        
        ContextElement ce = cer.getContextElement();
        assertEquals("Room2", ce.getId());
        assertEquals("false", ce.getIsPattern());
        assertEquals("Room", ce.getType());
        ArrayList<ContextAttribute> caList = ce.getAttributes();
        ContextAttribute ca = caList.get(0);
        assertEquals("field1", ca.getName());
        assertEquals("type1", ca.getType());
        assertEquals("{\"a\":{\"x\":{\"y\":\"v1\"},\"z\":\"v2\"},\"b\":\"v3\"}", ca.getContextValue(true));
        assertEquals("[]", ca.getContextMetadata());
        ca = caList.get(1);
        assertEquals("field2", ca.getName());
        assertEquals("type2", ca.getType());
        assertEquals("[[[\"v1\",\"v2\"]],{\"x\":[\"v3\",\"v4\"]}]", ca.getContextValue(true));
        assertEquals("[]", ca.getContextMetadata());

        StatusCode sc = cer.getStatusCode();
        assertEquals("200", sc.getCode());
        assertEquals("OK", sc.getReasonPhrase());
    } // testGetCxtResXMLCompoundNested

    /**
     * Sub-test case for nofity-xml-metadata.
     */
    private void testGetCxtResXMLMd() {
        System.out.println("getOriginator (notify-xml-metadata)");
        NotifyContextRequest instance = TestUtils.createXMLNotifyContextRequest(notifyXMLMetadata);
        ArrayList<ContextElementResponse> cerList = instance.getContextResponses();
        assertTrue(cerList != null);
        
        ContextElementResponse cer = cerList.get(0);
        
        ContextElement ce = cer.getContextElement();
        assertEquals(ce.getId(), "Room1");
        assertEquals(ce.getIsPattern(), "false");
        assertEquals(ce.getType(), "Room");
        ArrayList<ContextAttribute> caList = ce.getAttributes();
        ContextAttribute ca = caList.get(0);
        assertEquals(ca.getName(), "temperature");
        assertEquals(ca.getType(), "centigrade");
        assertEquals(ca.getContextValue(true), "\"26.5\"");
        assertEquals(ca.getContextMetadata(), "[{\"name\":\"ID\",\"type\":\"string\",\"value\":\"wall\"}]");

        StatusCode sc = cer.getStatusCode();
        assertEquals(sc.getCode(), "200");
        assertEquals(sc.getReasonPhrase(), "OK");
    } // testGetCxtResXMLMd
    
    /**
     * Sub-test case for nofity-xml-simple.
     */
    private void testGetCxtResXMLSimpleUnordered() {
        System.out.println("getOriginator (notify-xml-simple-unordered)");
        NotifyContextRequest instance = TestUtils.createXMLNotifyContextRequest(notifyXMLSimpleUnordered);
        ArrayList<ContextElementResponse> cerList = instance.getContextResponses();
        assertTrue(cerList != null);
        
        ContextElementResponse cer = cerList.get(0);
        
        ContextElement ce = cer.getContextElement();
        assertEquals(ce.getId(), "Room1");
        assertEquals(ce.getIsPattern(), "false");
        assertEquals(ce.getType(), "Room");
        ArrayList<ContextAttribute> caList = ce.getAttributes();
        ContextAttribute ca = caList.get(0);
        assertEquals(ca.getName(), "temperature");
        assertEquals(ca.getType(), "centigrade");
        assertEquals(ca.getContextValue(true), "\"26.5\"");
        assertEquals(ca.getContextMetadata(), "[]");

        StatusCode sc = cer.getStatusCode();
        assertEquals(sc.getCode(), "200");
        assertEquals(sc.getReasonPhrase(), "OK");
    } // testGetCxtResXMLSimpleUnordered
    
        /**
     * Sub-test case for nofity-xml-simple.
     */
    private void testGetCxtResXMLSimpleNullAttrs() {
        System.out.println("getOriginator (notify-xml-simple-null-attrs)");
        NotifyContextRequest instance = TestUtils.createXMLNotifyContextRequest(notifyXMLSimpleNullAttrs);
        ArrayList<ContextElementResponse> cerList = instance.getContextResponses();
        assertTrue(cerList != null);
        
        ContextElementResponse cer = cerList.get(0);
        
        ContextElement ce = cer.getContextElement();
        assertEquals(ce.getId(), "Room1");
        assertEquals(ce.getIsPattern(), "false");
        assertEquals(ce.getType(), "Room");
        ArrayList<ContextAttribute> caList = ce.getAttributes();
        
        assertTrue(caList == null);
        
        StatusCode sc = cer.getStatusCode();
        assertEquals(sc.getCode(), "200");
        assertEquals(sc.getReasonPhrase(), "OK");
    } // testGetCxtResXMLSimpleNullAttrs

    /**
     * Sub-test case for nofity-json-simple.
     */
    private void testGetCxtResJsonSimple() {
        System.out.println("getOriginator (notify-json-simple)");
        NotifyContextRequest instance = TestUtils.createJsonNotifyContextRequest(notifyJsonSimple);
        ArrayList<ContextElementResponse> cerList = instance.getContextResponses();
        assertTrue(cerList != null);
        
        ContextElementResponse cer = cerList.get(0);
        
        ContextElement ce = cer.getContextElement();
        assertEquals(ce.getId(), "Room1");
        assertEquals(ce.getIsPattern(), "false");
        assertEquals(ce.getType(), "Room");
        ArrayList<ContextAttribute> caList = ce.getAttributes();

        ContextAttribute ca = caList.get(0);
        assertEquals(ca.getName(), "temperature");
        assertEquals(ca.getType(), "centigrade");
        assertEquals(ca.getContextValue(true), "\"26.5\"");
        assertEquals(ca.getContextMetadata(), "[]");

        StatusCode sc = cer.getStatusCode();
        assertEquals(sc.getCode(), "200");
        assertEquals(sc.getReasonPhrase(), "OK");
    } // testGetCxtResJsonSimple
        
    /**
     * Sub-test case for nofity-json-compound.
     */
    private void testGetCxtResJsonCompound() {
        System.out.println("getOriginator (notify-json-compound)");
        NotifyContextRequest instance = TestUtils.createJsonNotifyContextRequest(notifyJsonCompound);
        ArrayList<ContextElementResponse> cerList = instance.getContextResponses();
        assertTrue(cerList != null);
        
        ContextElementResponse cer = cerList.get(0);
        
        ContextElement ce = cer.getContextElement();
        assertEquals("Room2", ce.getId());
        assertEquals("false", ce.getIsPattern());
        assertEquals("Room", ce.getType());
        ArrayList<ContextAttribute> caList = ce.getAttributes();
        ContextAttribute ca = caList.get(0);
        assertEquals("field1", ca.getName());
        assertEquals("type1", ca.getType());
        assertEquals("{\"a\":\"1\",\"b\":\"2\"}", ca.getContextValue(true));
        assertEquals("[]", ca.getContextMetadata());
        ca = caList.get(1);
        assertEquals("field2", ca.getName());
        assertEquals("type2", ca.getType());
        assertEquals("[\"v1\",\"v2\"]", ca.getContextValue(true));
        assertEquals("[]", ca.getContextMetadata());
        
        StatusCode sc = cer.getStatusCode();
        assertEquals("200", sc.getCode());
        assertEquals("OK", sc.getReasonPhrase());
    } // testGetCxtResJsonCompound
    
    /**
     * Sub-test case for nofity-json-compound.
     */
    private void testGetCxtResJsonCompoundNested() {
        System.out.println("getOriginator (notify-json-compound-nested)");
        NotifyContextRequest instance = TestUtils.createJsonNotifyContextRequest(notifyJsonCompoundNested);
        ArrayList<ContextElementResponse> cerList = instance.getContextResponses();
        assertTrue(cerList != null);
        
        ContextElementResponse cer = cerList.get(0);
        
        ContextElement ce = cer.getContextElement();
        assertEquals("Room2", ce.getId());
        assertEquals("false", ce.getIsPattern());
        assertEquals("Room", ce.getType());
        ArrayList<ContextAttribute> caList = ce.getAttributes();
        ContextAttribute ca = caList.get(0);
        assertEquals("field1", ca.getName());
        assertEquals("type1", ca.getType());
        assertEquals("{\"a\":{\"x\":{\"y\":\"v1\"},\"z\":\"v2\"},\"b\":\"v3\"}", ca.getContextValue(true));
        assertEquals("[]", ca.getContextMetadata());
        ca = caList.get(1);
        assertEquals("field2", ca.getName());
        assertEquals("type2", ca.getType());
        assertEquals("[[[\"v1\",\"v2\"]],{\"x\":[\"v3\",\"v4\"]}]", ca.getContextValue(true));
        assertEquals("[]", ca.getContextMetadata());
        
        StatusCode sc = cer.getStatusCode();
        assertEquals("200", sc.getCode());
        assertEquals("OK", sc.getReasonPhrase());
    } // testGetCxtResJsonCompoundNested
        
    /**
     * Sub-test case for nofity-json-metadata.
     */
    private void testGetCxtResJsonMd() {
        System.out.println("getOriginator (notify-json-metadata)");
        NotifyContextRequest instance = TestUtils.createJsonNotifyContextRequest(notifyJsonMetadata);
        ArrayList<ContextElementResponse> cerList = instance.getContextResponses();
        assertTrue(cerList != null);
        
        ContextElementResponse cer = cerList.get(0);
        
        ContextElement ce = cer.getContextElement();
        assertEquals(ce.getId(), "Room1");
        assertEquals(ce.getIsPattern(), "false");
        assertEquals(ce.getType(), "Room");
        ArrayList<ContextAttribute> caList = ce.getAttributes();
        ContextAttribute ca = caList.get(0);
        assertEquals(ca.getName(), "temperature");
        assertEquals(ca.getType(), "centigrade");
        assertEquals(ca.getContextValue(true), "\"26.5\"");
        String s = ca.getContextMetadata();
        assertEquals(ca.getContextMetadata(), "[{\"name\":\"ID\",\"type\":\"string\",\"value\":\"ground\"}]");

        StatusCode sc = cer.getStatusCode();
        assertEquals(sc.getCode(), "200");
        assertEquals(sc.getReasonPhrase(), "OK");
    } // testGetCxtResJsonMd
    
    /**
     * Sub-test case for nofity-json-simple-unordered.
     */
    private void testGetCxtResJsonSimpleUnordered() {
        System.out.println("getOriginator (notify-json-simple-unordered)");
        NotifyContextRequest instance = TestUtils.createJsonNotifyContextRequest(notifyJsonSimpleUnordered);
        ArrayList<ContextElementResponse> cerList = instance.getContextResponses();
        assertTrue(cerList != null);
        
        ContextElementResponse cer = cerList.get(0);
        
        ContextElement ce = cer.getContextElement();
        assertEquals(ce.getId(), "Room1");
        assertEquals(ce.getIsPattern(), "false");
        assertEquals(ce.getType(), "Room");
        ArrayList<ContextAttribute> caList = ce.getAttributes();

        ContextAttribute ca = caList.get(0);
        assertEquals(ca.getName(), "temperature");
        assertEquals(ca.getType(), "centigrade");
        assertEquals(ca.getContextValue(true), "\"26.5\"");
        assertEquals(ca.getContextMetadata(), "[]");

        StatusCode sc = cer.getStatusCode();
        assertEquals(sc.getCode(), "200");
        assertEquals(sc.getReasonPhrase(), "OK");
    } // testGetCxtResJsonSimpleUnordered
    
    /**
     * Sub-test case for nofity-json-simple-unordered.
     */
    private void testGetCxtResJsonSimpleNullAttrs() {
        System.out.println("getOriginator (notify-json-simple-null-attrs)");
        NotifyContextRequest instance = TestUtils.createJsonNotifyContextRequest(notifyJsonSimpleNullAttrs);
        ArrayList<ContextElementResponse> cerList = instance.getContextResponses();
        assertTrue(cerList != null);
        
        ContextElementResponse cer = cerList.get(0);
        
        ContextElement ce = cer.getContextElement();
        assertEquals(ce.getId(), "Room1");
        assertEquals(ce.getIsPattern(), "false");
        assertEquals(ce.getType(), "Room");
        ArrayList<ContextAttribute> caList = ce.getAttributes();

        assertTrue(caList == null);

        StatusCode sc = cer.getStatusCode();
        assertEquals(sc.getCode(), "200");
        assertEquals(sc.getReasonPhrase(), "OK");
    } // testGetCxtResJsonSimpleNullAttrs

} // NotifyContextRequestTest