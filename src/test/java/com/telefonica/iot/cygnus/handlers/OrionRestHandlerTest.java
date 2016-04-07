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

package com.telefonica.iot.cygnus.handlers;

import com.telefonica.iot.cygnus.utils.Constants;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static org.junit.Assert.*; // this is required by "fail" like assertions
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class OrionRestHandlerTest {
    
    // Mocks
    @Mock
    private HttpServletRequest mockHttpServletRequest;
    
    // Other variables
    JSONObject notification;
    
    /**
     * Constructor.
     */
    public OrionRestHandlerTest() {
        LogManager.getRootLogger().setLevel(Level.ERROR);
    } // OrionRestHandlerTest
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        when(mockHttpServletRequest.getMethod()).thenReturn("POST");
        when(mockHttpServletRequest.getRequestURI()).thenReturn("/notify");
        String[] headerNames = {"Content-Type", "fiware-service", "fiware-servicePath"};
        when(mockHttpServletRequest.getHeaderNames()).thenReturn(
                Collections.enumeration(new ArrayList(Arrays.asList(headerNames))));
        when(mockHttpServletRequest.getHeader("content-type")).thenReturn("application/json");
        when(mockHttpServletRequest.getHeader("fiware-service")).thenReturn("myservice");
        when(mockHttpServletRequest.getHeader("fiware-servicepath")).thenReturn("/myservicepath");
        JSONObject attributes = new JSONObject();
        attributes.put("name", "temperature");
        attributes.put("type", "centigrade");
        attributes.put("value", "26.5");
        JSONObject contextElement = new JSONObject();
        contextElement.put("attributes",attributes);
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
        notification = new JSONObject();
        notification.put("subscriptionId", "51c0ac9ed714fb3b37d7d5a8");
        notification.put("originator", "localhost");
        notification.put("contextResponses", contextResponses);
        when(mockHttpServletRequest.getReader()).thenReturn(
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
                        notification.toJSONString().getBytes()))));
    } // setUp
    
    /**
     * [OrionRestHandler.configure] -------- When not configured, the default values are used for non mandatory
     * parameters.
     */
    @Test
    public void testConfigureNotMandatoryParameters() {
        System.out.println("[OrionRestHandler.configure] -------- When not configured, the default values are used "
                + "for non mandatory parameters");
        OrionRestHandler handler = new OrionRestHandler();
        handler.configure(createContext(null, null, null)); // default configuration
        
        try {
            assertEquals("/notify", handler.getNotificationTarget());
            System.out.println("[OrionRestHandler.configure] -  OK  - The default configuration value for "
                    + "'notification_target' is '/notify'");
        } catch (AssertionError e) {
            System.out.println("[OrionRestHandler.configure] - FAIL - The default configuration value for "
                    + "'notification_target' is '" + handler.getNotificationTarget() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals("default", handler.getDefaultService());
            System.out.println("[OrionRestHandler.configure] -  OK  - The default configuration value for "
                    + "'default_service' is 'default'");
        } catch (AssertionError e) {
            System.out.println("[OrionRestHandler.configure] - FAIL - The default configuration value for "
                    + "'default_service' is '" + handler.getDefaultService() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals("/", handler.getDefaultServicePath());
            System.out.println("[OrionRestHandler.configure] -  OK  - The default configuration value for "
                    + "'default_service_path' is '/'");
        } catch (AssertionError e) {
            System.out.println("[OrionRestHandler.configure] - FAIL - The default configuration value for "
                    + "'default_service_path' is '" + handler.getDefaultServicePath() + "'");
            throw e;
        } // try catch
    } // testConfigureNotMandatoryParameters
    
    /**
     * [OrionRestHandler.configure] -------- The configured default service path must start with '/'.
     */
    @Test
    public void testConfigureDefaultServicePathStartsWithSlash() {
        System.out.println("[OrionRestHandler.configure] -------- The configured default service path must start "
                + "with '/'");
        OrionRestHandler handler = new OrionRestHandler();
        String configuredDefaultServicePath = "/something";
        handler.configure(createContext(null, null, configuredDefaultServicePath));
        
        try {
            assertEquals(configuredDefaultServicePath, handler.getDefaultServicePath());
            assertTrue(!handler.getInvalidConfiguration());
            System.out.println("[OrionRestHandler.configure] -  OK  - The configured default service path '"
                    + configuredDefaultServicePath + "' starts with '/'");
        } catch (AssertionError e) {
            System.out.println("[OrionRestHandler.configure] - FAIL - The configured default service path '"
                    + configuredDefaultServicePath + "' does not start with '/'");
            throw e;
        } // try catch
    } // testConfigureDefaultServicePathStartsWithSlash
    
    /**
     * [OrionRestHandler.configure] -------- The configured notification target must start with '/'.
     */
    @Test
    public void testConfigureNotificationTargerStartsWithSlash() {
        System.out.println("[OrionRestHandler.configure] -------- The configured notification target must start "
                + "with '/'");
        OrionRestHandler handler = new OrionRestHandler();
        String configuredNotificationTarget = "/notify";
        handler.configure(createContext(configuredNotificationTarget, null, null));
        
        try {
            assertEquals(configuredNotificationTarget, handler.getNotificationTarget());
            assertTrue(!handler.getInvalidConfiguration());
            System.out.println("[OrionRestHandler.configure] -  OK  - The configured notification target '"
                    + configuredNotificationTarget + "' starts with '/'");
        } catch (AssertionError e) {
            System.out.println("[OrionRestHandler.configure] - FAIL - The configured notification target '"
                    + configuredNotificationTarget + "' does not start with '/'");
            throw e;
        } // try catch // try catch
    } // testConfigureDefaultServicePathStartsWithSlash
    
    /**
     * [OrionRestHandler.getEvents] -------- When a notification is sent, the headers are valid.
     */
    @Test
    public void testGetEventsContentTypeHeader() {
        System.out.println("[OrionRestHandler.getEvents] -------- When a notification is sent, the headers are valid");
        OrionRestHandler handler = new OrionRestHandler();
        handler.configure(createContext(null, null, null)); // default configuration
        
        try {
            handler.getEvents(mockHttpServletRequest);
            assertTrue(true);
            System.out.println("[OrionRestHandler.getEvents] -  OK  - The value for 'Content-Type' header is "
                    + "'application/json'");
            System.out.println("[OrionRestHandler.getEvents] -  OK  - The value for 'fiware-servicePath' header "
                    + "starts with '/'");
            System.out.println("[OrionRestHandler.getEvents] -  OK  - The length of 'fiware-service' header value is "
                    + " less or equal than '" + Constants.SERVICE_HEADER_MAX_LEN + "'");
            System.out.println("[OrionRestHandler.getEvents] -  OK  - The length of 'fiware-servicePath' header value "
                    + "is less or equal than '" + Constants.SERVICE_PATH_HEADER_MAX_LEN + "'");
        } catch (Exception e) {
            if (e.getMessage().contains("content type not supported")) {
                System.out.println("[OrionRestHandler.getEvents] - FAIL - The value for 'Content-Type' is not "
                        + "'application/json'");
            } else if (e.getMessage().contains("header value must start with '/'")) {
                System.out.println("[OrionRestHandler.getEvents] - FAIL - The value for 'fiware-servicePath' does not "
                        + "start with '/'");
            } else if (e.getMessage().contains("'fiware-service' header length greater than")) {
                System.out.println("[OrionRestHandler.getEvents] - FAIL - The length of 'fiware-service' header value "
                        + "is greater than '" + Constants.SERVICE_HEADER_MAX_LEN + "'");
            } else if (e.getMessage().contains("'fiware-servicePath' header length greater than")) {
                System.out.println("[OrionRestHandler.getEvents] - FAIL - The length of 'fiware-servicePath' header "
                        + "value is greater than '" + Constants.SERVICE_PATH_HEADER_MAX_LEN + "'");
            } // if else
            
            assertTrue(false);
        } // try catch
    } // testGetEventsContentTypeHeader
    
    /**
     * [OrionRestHandler.getEvents] -------- When a the configuration is wrong, no events are obtained.
     */
    @Test
    public void testGetEventsNullEventsUponInvalidConfiguration() {
        System.out.println("[OrionRestHandler.getEvents] -------- When a the configuration is wrong, no evetns "
                + "are obtained");
        OrionRestHandler handler = new OrionRestHandler();
        String configuredNotificationTarget = "notify"; // wrong value
        String configuredDefaultService = "default";
        String configuredDefaultServicePath = "something"; // wrong value
        handler.configure(createContext(configuredNotificationTarget, configuredDefaultService,
                configuredDefaultServicePath));
        List<Event> events;
        
        try {
            events = handler.getEvents(mockHttpServletRequest);
            
            try {
                assertEquals(0, events.size());
                System.out.println("[OrionRestHandler.getEvents] -  OK  - No events are processed since the "
                        + "configuration is wrong");
            } catch (AssertionError e1) {
                System.out.println("[OrionRestHandler.getEvents] - FAIL - The events are being processed "
                        + "despite of the configuration is wrong");
                throw e1;
            } // try catch
        } catch (Exception ex) {
            System.out.println("[OrionRestHandler.getEvents] - FAIL - There was some problem while processing "
                    + "the events");
            assertTrue(false);
        } // try catch
    } // testGetEventsNullEventsUponInvalidConfiguration
    
    /**
     * [OrionRestHandler.getEvents] -------- When a notification is sent as a Http message, a single Flume event
     * is generated.
     */
    @Test
    public void testGetEventsSingleEvent() {
        System.out.println("[OrionRestHandler.getEvents] -------- When a notification is sent as a Http message, "
                + "a single Flume event is generated");
        OrionRestHandler handler = new OrionRestHandler();
        handler.configure(createContext(null, null, null)); // default configuration
        List<Event> events;
        
        try {
            events = handler.getEvents(mockHttpServletRequest);
            
            try {
                assertEquals(1, events.size());
                System.out.println("[OrionRestHandler.getEvents] -  OK  - A single event has been generated");
            } catch (AssertionError e1) {
                System.out.println("[OrionRestHandler.getEvents] - FAIL - 0, 2 or more than an event were generated");
                throw e1;
            } // try catch
        } catch (Exception e) {
            System.out.println("[OrionRestHandler.getEvents] - FAIL - There was some problem while processing "
                    + "the events");
            assertTrue(false);
        } // try catch
    } // testGetEventsSingleEvent
    
    /**
     * [OrionRestHandler.getEvents] -------- When a Flume event is generated, it contains fiware-service,
     * fiware-servicepath, fiware-correlator and transaction-id headers.
     */
    @Test
    public void testGetEventsHeadersInFlumeEvent() {
        System.out.println("[OrionRestHandler.getEvents] -------- When a Flume event is generated, it contains "
                + "fiware-service, fiware-servicepath, fiware-correlator and transaction-id headers");
        OrionRestHandler handler = new OrionRestHandler();
        handler.configure(createContext(null, null, null)); // default configuration
        Map<String, String> headers;
        
        try {
            headers = handler.getEvents(mockHttpServletRequest).get(0).getHeaders();
            
            try {
                assertTrue(headers.containsKey("fiware-service"));
                System.out.println("[OrionRestHandler.getEvents] -  OK  - The generated Flume event contains "
                        + "'fiware-service'");
            } catch (AssertionError e1) {
                System.out.println("[OrionRestHandler.getEvents] - FAIL - The generated Flume event does not "
                        + "contains 'fiware-service'");
                throw e1;
            } // try catch
            
            try {
                assertTrue(headers.containsKey("fiware-servicepath"));
                System.out.println("[OrionRestHandler.getEvents] -  OK  - The generated Flume event contains "
                        + "'fiware-servicepath'");
            } catch (AssertionError e2) {
                System.out.println("[OrionRestHandler.getEvents] - FAIL - The generated Flume event does not "
                        + "contains 'fiware-servicepath'");
                throw e2;
            } // try catch

            try {
                assertTrue(headers.containsKey("fiware-correlator"));
                System.out.println("[OrionRestHandler.getEvents] -  OK  - The generated Flume event contains "
                        + "'fiware-correlator'");
            } catch (AssertionError e3) {
                System.out.println("[OrionRestHandler.getEvents] - FAIL - The generated Flume event does not "
                        + "contains 'fiware-correlator'");
                throw e3;
            } // try catch
            
            try {
                assertTrue(headers.containsKey("transaction-id"));
                System.out.println("[OrionRestHandler.getEvents] -  OK  - The generated Flume event contains "
                        + "'transaction-id'");
            } catch (AssertionError e4) {
                System.out.println("[OrionRestHandler.getEvents] - FAIL - The generated Flume event does not "
                        + "contains 'transaction-id'");
                throw e4;
            } // try catch
        } catch (Exception e) {
            System.out.println("[OrionRestHandler.getEvents] - FAIL - There was some problem while processing "
                    + "the events");
            assertTrue(false);
        } // try catch
    } // testGetEventsHeadersInFlumeEvent
    
    /**
     * [OrionRestHandler.getEvents] -------- When a Flume event is generated, it contains the payload of the Http
     * notification as body.
     */
    @Test
    public void testGetEventsBodyInFlumeEvent() {
        System.out.println("[OrionRestHandler.getEvents] -------- When a Flume event is generated, it contains "
                + "the payload of the Http notification as body");
        OrionRestHandler handler = new OrionRestHandler();
        handler.configure(createContext(null, null, null)); // default configuration
        byte[] body;
        
        try {
            body = handler.getEvents(mockHttpServletRequest).get(0).getBody();
            byte[] notificationBytes = notification.toJSONString().getBytes();
            boolean areEqual = true;
            
            if (body.length != notificationBytes.length) {
                areEqual = false;
            } else {
                for (int i = 0; i < body.length; i++) {
                    if (body[i] != notificationBytes[i]) {
                        areEqual = false;
                        break;
                    } // if
                } // for
            } // if else
            
            try {
                assertTrue(areEqual);
                System.out.println("[OrionRestHandler.getEvents] -  OK  - The event body '" + new String(body)
                        + "' is equal to the notification Json '" + new String(notificationBytes) + "'");
            } catch (AssertionError e1) {
                System.out.println("[OrionRestHandler.getEvents] - FAIL - The event body '" + new String(body)
                        + "' is nbot equal to the notification Json '" + new String(notificationBytes) + "'");
                throw e1;
            } // try catch
        } catch (Exception e) {
            System.out.println("[OrionRestHandler.getEvents] - FAIL - There was some problem while processing "
                    + "the events");
            assertTrue(false);
        } // try catch
    } // testGetEventsBodyInFlumeEvent
    
    /**
     * [OrionRestHandler.generateUniqueId] -------- An internal transaction ID is generated.
     */
    @Test
    public void testGenerateUniqueIdTransIdGenerated() {
        System.out.println("[OrionRestHandler.generateUniqueId] -------- An internal transaction ID is generated");
        OrionRestHandler handler = new OrionRestHandler();
        String generatedTransId = handler.generateUniqueId(null, null);
        
        try {
            assertTrue(generatedTransId != null);
            System.out.println("[OrionRestHandler.generateUniqueId] -  OK  - An internal transaction ID '"
                    + generatedTransId + "' has been generated");
        } catch (AssertionError e) {
            System.out.println("[OrionRestHandler.generateUniqueId] - FAIL - An intertnal transaction ID was not "
                    + "generated");
            throw e;
        } // try catch // try catch
    } // testGenerateUniqueIdTransIdGenerated
    
    /**
     * [OrionRestHandler.generateUniqueId] -------- When a correlator ID is notified, it is reused.
     */
    @Test
    public void testGenerateUniqueIdCorrIdReused() {
        System.out.println("[OrionRestHandler.generateUniqueId] -------- When a correlator ID is notified, it is "
                + "reused");
        OrionRestHandler handler = new OrionRestHandler();
        String generatedTransId = "1111111111-111-1111111111";
        String notifiedCorrId = "1234567890-123-1234567890";
        String generatedCorrId = handler.generateUniqueId(notifiedCorrId, generatedTransId);
        
        try {
            assertEquals(notifiedCorrId, generatedCorrId);
            System.out.println("[OrionRestHandler.generateUniqueId] -  OK  - The notified transaction ID '"
                    + notifiedCorrId + "' has been reused");
        } catch (AssertionError e) {
            System.out.println("[OrionRestHandler.generateUniqueId] - FAIL - The notified transaction ID '"
                    + notifiedCorrId + "' has not not reused, '" + generatedCorrId + "' has been generated instead");
            throw e;
        } // try catch // try catch
    } // testGenerateUniqueIdCorrIdReused
    
    /**
     * [OrionRestHandler.generateUniqueId] -------- When a correlation ID is not notified, it is generated.
     */
    @Test
    public void testGenerateUniqueIdCorrIdGenerated() {
        System.out.println("[OrionRestHandler.generateUniqueId] -------- When a correlation ID is not notified, "
                + "it is generated");
        OrionRestHandler handler = new OrionRestHandler();
        String generatedTransId = "1234567890-123-1234567890";
        String notifiedCorrId = null;
        
        try {
            assertTrue(handler.generateUniqueId(notifiedCorrId, generatedTransId) != null);
            System.out.println("[OrionRestHandler.generateUniqueId] -  OK  - The transaction ID has been generated");
        } catch (AssertionError e) {
            System.out.println("[OrionRestHandler.generateUniqueId] - FAIL - The transaction ID has not been "
                    + "generated");
            throw e;
        } // try catch
    } // testGenerateUniqueIdCorrIdGenerated
    
    /**
     * [OrionRestHandler.generateUniqueId] -------- When a correlation ID is generated, both generated correlation ID
     * and generated transaction ID have the same value.
     */
    @Test
    public void testGenerateUniqueIdSameCorrAndTransIds() {
        System.out.println("[OrionRestHandler.generateUniqueId] -------- When a correlation ID is genereated, both "
                + "generated correlation ID and generated transaction ID have the same value");
        OrionRestHandler handler = new OrionRestHandler();
        String generatedTransId = "1234567890-123-1234567890";
        String notifiedCorrId = null;
        String generatedCorrId = handler.generateUniqueId(notifiedCorrId, generatedTransId);
        
        try {
            assertEquals(generatedTransId, generatedCorrId);
            System.out.println("[OrionRestHandler.generateUniqueId] -  OK  - The generated transaction ID '"
                    + generatedTransId + "' is equals to the generated correlator ID '" + generatedCorrId + "'");
        } catch (AssertionError e) {
            System.out.println("[OrionRestHandler.generateUniqueId] - FAIL - The generated transaction ID '"
                    + generatedTransId + "' is not equals to the generated correlator ID '" + generatedCorrId + "'");
            throw e;
        } // try catch
    } // testGenerateUniqueIdSameCorrAndTransIds
    
    private Context createContext(String notificationTarget, String defaultService, String defaultServicePath) {
        Context context = new Context();
        context.put("notification_target", notificationTarget);
        context.put("default_service", defaultService);
        context.put("default_service_path", defaultServicePath);
        return context;
    } // createContext

} // OrionRestHandlerTest