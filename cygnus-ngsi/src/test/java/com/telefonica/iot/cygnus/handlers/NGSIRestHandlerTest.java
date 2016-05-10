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

import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.CommonUtilsForTests;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
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
public class NGSIRestHandlerTest {
    
    // Mocks
    @Mock
    private HttpServletRequest mockHttpServletRequest;
    
    // Other variables
    private JSONObject notification;
    
    /**
     * Constructor.
     */
    public NGSIRestHandlerTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSIRestHandlerTest
    
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
        when(mockHttpServletRequest.getHeader("content-type")).thenReturn("application/json; charset=utf-8");
        when(mockHttpServletRequest.getHeader("fiware-service")).thenReturn("myservice");
        when(mockHttpServletRequest.getHeader("fiware-servicepath")).thenReturn("/myservicepath");
        notification = CommonUtilsForTests.createNotification();
        when(mockHttpServletRequest.getReader()).thenReturn(
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
                        notification.toJSONString().getBytes()))));
    } // setUp
    
    /**
     * [NGSIRestHandler.configure] -------- When not configured, the default values are used for non mandatory
 parameters.
     */
    @Test
    public void testConfigureNotMandatoryParameters() {
        System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                + "-------- When not configured, the default values are used for non mandatory parameters");
        NGSIRestHandler handler = new NGSIRestHandler();
        handler.configure(createContext(null, null, null)); // default configuration
        
        try {
            assertEquals("/notify", handler.getNotificationTarget());
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "-  OK  - The default configuration value for 'notification_target' is '/notify'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "- FAIL - The default configuration value for 'notification_target' is '"
                    + handler.getNotificationTarget() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals("default", handler.getDefaultService());
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "-  OK  - The default configuration value for 'default_service' is 'default'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "- FAIL - The default configuration value for 'default_service' is '"
                    + handler.getDefaultService() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals("/", handler.getDefaultServicePath());
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "-  OK  - The default configuration value for 'default_service_path' is '/'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "- FAIL - The default configuration value for 'default_service_path' is '"
                    + handler.getDefaultServicePath() + "'");
            throw e;
        } // try catch
    } // testConfigureNotMandatoryParameters
    
    /**
     * [NGSIRestHandler.configure] -------- The configured default service can only contain alphanumerics and
     * underscores.
     */
    @Test
    public void testConfigureDefaultServiceIsLegal() {
        System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                + "-------- The configured default service can only contain alphanumerics and underscores");
        NGSIRestHandler handler = new NGSIRestHandler();
        String configuredDefaultService = "default-service!!";
        handler.configure(createContext(null, configuredDefaultService, null));
        
        try {
            assertTrue(handler.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "-  OK  - The configured default service '" + configuredDefaultService
                    + "' was detected as invalid");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "- FAIL - The configured default service '" + configuredDefaultService
                    + "' was not detected as invalid");
            throw e;
        } // try catch
    } // testConfigureDefaultServiceIsLegal
    
    /**
     * [NGSIRestHandler.configure] -------- The configured default service path can only contain alphanumerics
     * and underscores.
     */
    @Test
    public void testConfigureDefaultServicePathIsLegal() {
        System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                + "-------- The configured default service path can only contain alphanumerics and underscores");
        NGSIRestHandler handler = new NGSIRestHandler();
        String configuredDefaultServicePath = "/something.?";
        handler.configure(createContext(null, null, configuredDefaultServicePath));
        
        try {
            assertTrue(handler.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "-  OK  - The configured default service path '" + configuredDefaultServicePath
                    + "' was detected as invalid");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "- FAIL - The configured default service path '" + configuredDefaultServicePath
                    + "' was not detected as invalid");
            throw e;
        } // try catch
    } // testConfigureDefaultServicePathIsLegal
    
    /**
     * [NGSIRestHandler.configure] -------- The configured default service path must start with '/'.
     */
    @Test
    public void testConfigureDefaultServicePathStartsWithSlash() {
        System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                + "-------- The configured default service path must start with '/'");
        NGSIRestHandler handler = new NGSIRestHandler();
        String configuredDefaultServicePath = "/something";
        handler.configure(createContext(null, null, configuredDefaultServicePath));
        
        try {
            assertEquals(configuredDefaultServicePath, handler.getDefaultServicePath());
            assertTrue(!handler.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "-  OK  - The configured default service path '" + configuredDefaultServicePath
                    + "' starts with '/'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "- FAIL - The configured default service path '" + configuredDefaultServicePath
                    + "' does not start with '/'");
            throw e;
        } // try catch
    } // testConfigureDefaultServicePathStartsWithSlash
    
    /**
     * [NGSIRestHandler.configure] -------- The configured notification target must start with '/'.
     */
    @Test
    public void testConfigureNotificationTargerStartsWithSlash() {
        System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                + "-------- The configured notification target must start with '/'");
        NGSIRestHandler handler = new NGSIRestHandler();
        String configuredNotificationTarget = "/notify";
        handler.configure(createContext(configuredNotificationTarget, null, null));
        
        try {
            assertEquals(configuredNotificationTarget, handler.getNotificationTarget());
            assertTrue(!handler.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "-  OK  - The configured notification target '" + configuredNotificationTarget
                    + "' starts with '/'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.configure]")
                    + "- FAIL - The configured notification target '" + configuredNotificationTarget
                    + "' does not start with '/'");
            throw e;
        } // try catch // try catch
    } // testConfigureDefaultServicePathStartsWithSlash
    
    /**
     * [NGSIRestHandler.getEvents] -------- When a notification is sent, the headers are valid.
     */
    @Test
    public void testGetEventsContentTypeHeader() {
        System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                + "-------- When a notification is sent, the headers are valid");
        NGSIRestHandler handler = new NGSIRestHandler();
        handler.configure(createContext(null, null, null)); // default configuration
        
        try {
            handler.getEvents(mockHttpServletRequest);
            assertTrue(true);
            System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                    + "-  OK  - The value for 'Content-Type' header is 'application/json; charset=utf-8'");
            System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                    + "-  OK  - The value for 'fiware-servicePath' header starts with '/'");
            System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                    + "-  OK  - The length of 'fiware-service' header value is "
                    + " less or equal than '" + CommonConstants.SERVICE_HEADER_MAX_LEN + "'");
            System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                    + "-  OK  - The length of 'fiware-servicePath' header value "
                    + "is less or equal than '" + CommonConstants.SERVICE_PATH_HEADER_MAX_LEN + "'");
        } catch (Exception e) {
            if (e.getMessage().contains("content type not supported")) {
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "- FAIL - The value for 'Content-Type' is not 'application/json; charset=utf-8'");
            } else if (e.getMessage().contains("header value must start with '/'")) {
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "- FAIL - The value for 'fiware-servicePath' does not start with '/'");
            } else if (e.getMessage().contains("'fiware-service' header length greater than")) {
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "- FAIL - The length of 'fiware-service' header value "
                        + "is greater than '" + CommonConstants.SERVICE_HEADER_MAX_LEN + "'");
            } else if (e.getMessage().contains("'fiware-servicePath' header length greater than")) {
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "- FAIL - The length of 'fiware-servicePath' header "
                        + "value is greater than '" + CommonConstants.SERVICE_PATH_HEADER_MAX_LEN + "'");
            } // if else
            
            assertTrue(false);
        } // try catch // try catch
    } // testGetEventsContentTypeHeader
    
    /**
     * [NGSIRestHandler.getEvents] -------- When a the configuration is wrong, no events are obtained.
     */
    @Test
    public void testGetEventsNullEventsUponInvalidConfiguration() {
        System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                + "-------- When a the configuration is wrong, no evetns are obtained");
        NGSIRestHandler handler = new NGSIRestHandler();
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
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "-  OK  - No events are processed since the configuration is wrong");
            } catch (AssertionError e1) {
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "- FAIL - The events are being processed despite of the configuration is wrong");
                throw e1;
            } // try catch
        } catch (Exception ex) {
            System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                    + "- FAIL - There was some problem while processing the events");
            assertTrue(false);
        } // try catch
    } // testGetEventsNullEventsUponInvalidConfiguration
    
    /**
     * [NGSIRestHandler.getEvents] -------- When a notification is sent as a Http message, a single Flume event
 is generated.
     */
    @Test
    public void testGetEventsSingleEvent() {
        System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                + "-------- When a notification is sent as a Http message, a single Flume event is generated");
        NGSIRestHandler handler = new NGSIRestHandler();
        handler.configure(createContext(null, null, null)); // default configuration
        List<Event> events;
        
        try {
            events = handler.getEvents(mockHttpServletRequest);
            
            try {
                assertEquals(1, events.size());
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "-  OK  - A single event has been generated");
            } catch (AssertionError e1) {
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "- FAIL - 0, 2 or more than an event were generated");
                throw e1;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                    + "- FAIL - There was some problem while processing the events");
            assertTrue(false);
        } // try catch
    } // testGetEventsSingleEvent
    
    /**
     * [NGSIRestHandler.getEvents] -------- When a Flume event is generated, it contains fiware-service,
 fiware-servicepath, fiware-correlator and transaction-id headers.
     */
    @Test
    public void testGetEventsHeadersInFlumeEvent() {
        System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                + "-------- When a Flume event is generated, it contains fiware-service, fiware-servicepath, "
                + "fiware-correlator and transaction-id headers");
        NGSIRestHandler handler = new NGSIRestHandler();
        handler.configure(createContext(null, null, null)); // default configuration
        Map<String, String> headers;
        
        try {
            headers = handler.getEvents(mockHttpServletRequest).get(0).getHeaders();
            
            try {
                assertTrue(headers.containsKey("fiware-service"));
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "-  OK  - The generated Flume event contains 'fiware-service'");
            } catch (AssertionError e1) {
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "- FAIL - The generated Flume event does not contain 'fiware-service'");
                throw e1;
            } // try catch
            
            try {
                assertTrue(headers.containsKey("fiware-servicepath"));
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "-  OK  - The generated Flume event contains 'fiware-servicepath'");
            } catch (AssertionError e2) {
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "- FAIL - The generated Flume event does not contain 'fiware-servicepath'");
                throw e2;
            } // try catch

            try {
                assertTrue(headers.containsKey("fiware-correlator"));
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "-  OK  - The generated Flume event contains 'fiware-correlator'");
            } catch (AssertionError e3) {
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "- FAIL - The generated Flume event does not contain 'fiware-correlator'");
                throw e3;
            } // try catch
            
            try {
                assertTrue(headers.containsKey("transaction-id"));
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "-  OK  - The generated Flume event contains 'transaction-id'");
            } catch (AssertionError e4) {
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "- FAIL - The generated Flume event does not contain 'transaction-id'");
                throw e4;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                    + "- FAIL - There was some problem while processing the events");
            assertTrue(false);
        } // try catch
    } // testGetEventsHeadersInFlumeEvent
    
    /**
     * [NGSIRestHandler.getEvents] -------- When a Flume event is generated, it contains the payload of the Http
 notification as body.
     */
    @Test
    public void testGetEventsBodyInFlumeEvent() {
        System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                + "-------- When a Flume event is generated, it contains "
                + "the payload of the Http notification as body");
        NGSIRestHandler handler = new NGSIRestHandler();
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
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "-  OK  - The event body '" + new String(body)
                        + "' is equal to the notification Json '" + new String(notificationBytes) + "'");
            } catch (AssertionError e1) {
                System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                        + "- FAIL - The event body '" + new String(body)
                        + "' is nbot equal to the notification Json '" + new String(notificationBytes) + "'");
                throw e1;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.getEvents]")
                    + "- FAIL - There was some problem while processing the events");
            assertTrue(false);
        } // try catch
    } // testGetEventsBodyInFlumeEvent
    
    /**
     * [NGSIRestHandler.generateUniqueId] -------- An internal transaction ID is generated.
     */
    @Test
    public void testGenerateUniqueIdTransIdGenerated() {
        System.out.println(getTestTraceHead("[OrionRestHandler.generateUniqueId]")
                + "-------- An internal transaction ID is generated");
        NGSIRestHandler handler = new NGSIRestHandler();
        String generatedTransId = handler.generateUniqueId(null, null);
        
        try {
            assertTrue(generatedTransId != null);
            System.out.println(getTestTraceHead("[OrionRestHandler.generateUniqueId]")
                    + "-  OK  - An internal transaction ID '" + generatedTransId + "' has been generated");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.generateUniqueId]")
                    + "- FAIL - An intertnal transaction ID was not "
                    + "generated");
            throw e;
        } // try catch // try catch
    } // testGenerateUniqueIdTransIdGenerated
    
    /**
     * [NGSIRestHandler.generateUniqueId] -------- When a correlator ID is notified, it is reused.
     */
    @Test
    public void testGenerateUniqueIdCorrIdReused() {
        System.out.println(getTestTraceHead("[OrionRestHandler.generateUniqueId]")
                + "-------- When a correlator ID is notified, it is reused");
        NGSIRestHandler handler = new NGSIRestHandler();
        String generatedTransId = "1111111111-111-1111111111";
        String notifiedCorrId = "1234567890-123-1234567890";
        String generatedCorrId = handler.generateUniqueId(notifiedCorrId, generatedTransId);
        
        try {
            assertEquals(notifiedCorrId, generatedCorrId);
            System.out.println(getTestTraceHead("[OrionRestHandler.generateUniqueId]")
                    + "-  OK  - The notified transaction ID '" + notifiedCorrId + "' has been reused");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.generateUniqueId]")
                    + "- FAIL - The notified transaction ID '" + notifiedCorrId + "' has not not reused, '"
                    + generatedCorrId + "' has been generated instead");
            throw e;
        } // try catch // try catch
    } // testGenerateUniqueIdCorrIdReused
    
    /**
     * [NGSIRestHandler.generateUniqueId] -------- When a correlation ID is not notified, it is generated.
     */
    @Test
    public void testGenerateUniqueIdCorrIdGenerated() {
        System.out.println(getTestTraceHead("[OrionRestHandler.generateUniqueId]")
                + "-------- When a correlation ID is not notified, it is generated");
        NGSIRestHandler handler = new NGSIRestHandler();
        String generatedTransId = "1234567890-123-1234567890";
        String notifiedCorrId = null;
        
        try {
            assertTrue(handler.generateUniqueId(notifiedCorrId, generatedTransId) != null);
            System.out.println(getTestTraceHead("[OrionRestHandler.generateUniqueId]")
                    + "-  OK  - The transaction ID has been generated");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.generateUniqueId]")
                    + "- FAIL - The transaction ID has not been generated");
            throw e;
        } // try catch
    } // testGenerateUniqueIdCorrIdGenerated
    
    /**
     * [NGSIRestHandler.generateUniqueId] -------- When a correlation ID is generated, both generated correlation ID
 and generated transaction ID have the same value.
     */
    @Test
    public void testGenerateUniqueIdSameCorrAndTransIds() {
        System.out.println(getTestTraceHead("[OrionRestHandler.generateUniqueId]")
                + "-------- When a correlation ID is genereated, both "
                + "generated correlation ID and generated transaction ID have the same value");
        NGSIRestHandler handler = new NGSIRestHandler();
        String generatedTransId = "1234567890-123-1234567890";
        String notifiedCorrId = null;
        String generatedCorrId = handler.generateUniqueId(notifiedCorrId, generatedTransId);
        
        try {
            assertEquals(generatedTransId, generatedCorrId);
            System.out.println(getTestTraceHead("[OrionRestHandler.generateUniqueId]")
                    + "-  OK  - The generated transaction ID '" + generatedTransId
                    + "' is equals to the generated correlator ID '" + generatedCorrId + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[OrionRestHandler.generateUniqueId]")
                    + "- FAIL - The generated transaction ID '" + generatedTransId
                    + "' is not equals to the generated correlator ID '" + generatedCorrId + "'");
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

} // NGSIRestHandlerTest