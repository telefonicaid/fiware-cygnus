/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import static org.junit.Assert.*; // this is required by "fail" like assertions
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import org.junit.Assert;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class NGSIRestHandlerTest {
    
    // Mocks
    @Mock
    private HttpServletRequest mockHttpServletRequest;
    @Mock
    private HttpServletRequest mockHttpServletRequest2;
    @Mock
    private HttpServletRequest mockHttpServletRequest3;
    @Mock
    private HttpServletRequest mockHttpServletRequest4;
    
    // Other variables

    private final String notification3 = "{\n" +
            "\"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\",\n" +
            "\"data\":\n" +
            "[\n" +
            "   {\n" +
            "    \"id\": \"urn:ngsi-ld:Vehicle:V123\",\n" +
            "    \"type\": \"Vehicle\",\n" +
            "    \"speed\": {\n" +
            "      \"type\": \"Property\",\n" +
            "      \"value\": 23,\n" +
            "      \"accuracy\": {\n" +
            "        \"type\": \"Property\",\n" +
            "        \"value\": 0.7\n" +
            "      },\n" +
            "      \"providedBy\": {\n" +
            "        \"type\": \"Relationship\",\n" +
            "        \"object\": \"urn:ngsi-ld:Person:Bob\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"closeTo\": {\n" +
            "      \"type\": \"Relationship\",\n" +
            "      \"object\": \"urn:ngsi-ld:Building:B1234\"\n" +
            "    },\n" +
            "    \"location\": {\n" +
            "        \"type\": \"GeoProperty\",\n" +
            "        \"value\": {\n" +
            "          \"type\":\"Point\",\n" +
            "          \"coordinates\": [-8,44]\n" +
            "        }\n" +
            "    },\n" +
            "    \"@context\": [\n" +
            "        \"https://example.org/ld/vehicle.jsonld\",\n" +
            "        \"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"\n" +
            "    ]\n" +
            "  }]\n"+
            "}";

    private final String notification4 = "{\n" +
            "\"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\",\n" +
            "\"data\":\n" +
            "[\n" +
            "   {\n" +
            "    \"id\": \"urn:ngsi-ld:Vehicle:V123\",\n" +
            "    \"type\": \"Vehicle\",\n" +
            "    \"speed\": {\n" +
            "      \"type\": \"Property\",\n" +
            "      \"value\": 23,\n" +
            "      \"accuracy\": {\n" +
            "        \"type\": \"Property\",\n" +
            "        \"value\": 0.7\n" +
            "      },\n" +
            "      \"providedBy\": {\n" +
            "        \"type\": \"Relationship\",\n" +
            "        \"object\": \"urn:ngsi-ld:Person:Bob\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"closeTo\": {\n" +
            "      \"type\": \"Relationship\",\n" +
            "      \"object\": \"urn:ngsi-ld:Building:B1234\"\n" +
            "    },\n" +
            "    \"location\": {\n" +
            "        \"type\": \"GeoProperty\",\n" +
            "        \"value\": {\n" +
            "          \"type\":\"Point\",\n" +
            "          \"coordinates\": [-8,44]\n" +
            "        }\n" +
            "    },\n" +
            "    \"@context\": [\n" +
            "        \"https://example.org/ld/vehicle.jsonld\",\n" +
            "        \"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"urn:ngsi-ld:OffStreetParking:Downtown1\",\n" +
            "    \"type\": \"OffStreetParking\",\n" +
            "    \"name\": {\n" +
            "      \"type\": \"Property\",\n" +
            "        \"value\": \"Downtown One\"\n" +
            "    },\n" +
            "    \"availableSpotNumber\": {\n" +
            "      \"type\": \"Property\",\n" +
            "      \"value\": 121,\n" +
            "      \"observedAt\": \"2017-07-29T12:05:02Z\",\n" +
            "      \"reliability\": {\n" +
            "            \"type\": \"Property\",\n" +
            "            \"value\": 0.7\n" +
            "      },\n" +
            "      \"providedBy\": {\n" +
            "            \"type\": \"Relationship\",\n" +
            "            \"object\": \"urn:ngsi-ld:Camera:C1\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"totalSpotNumber\": {\n" +
            "        \"type\": \"Property\",\n" +
            "        \"value\": 200\n" +
            "    },\n" +
            "    \"location\": {\n" +
            "      \"type\": \"GeoProperty\",\n" +
            "      \"value\": {\n" +
            "        \"type\": \"Point\",\n" +
            "        \"coordinates\": [-8.5, 41.2]\n" +
            "      }\n" +
            "    },\n" +
            "    \"@context\": [\n" +
            "        \"http://example.org/ngsi-ld/parking.jsonld\",\n" +
            "        \"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"\n" +
            "    ]\n" +
            "  }\n" +
            "]\n" +
            "}";
    
    /**
     * Constructor.
     */
    public NGSIRestHandlerTest() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.FATAL);
        ctx.updateLoggers();
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
        when(mockHttpServletRequest.getReader()).thenReturn(
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(notification3.getBytes()))));
        when(mockHttpServletRequest2.getMethod()).thenReturn("POST");
        when(mockHttpServletRequest2.getRequestURI()).thenReturn("/notify");
        String[] headerNames2 = {"Content-Type", "fiware-service", "fiware-servicePath"};
        when(mockHttpServletRequest2.getHeaderNames()).thenReturn(
                Collections.enumeration(new ArrayList(Arrays.asList(headerNames2))));
        when(mockHttpServletRequest2.getHeader("content-type")).thenReturn("application/json; charset=utf-8");
        when(mockHttpServletRequest2.getHeader("fiware-service")).thenReturn("myservice");
        when(mockHttpServletRequest2.getHeader("fiware-servicepath")).thenReturn("/a,/b");
        when(mockHttpServletRequest2.getReader()).thenReturn(
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(notification3.getBytes()))));
        when(mockHttpServletRequest3.getMethod()).thenReturn("POST");
        when(mockHttpServletRequest3.getRequestURI()).thenReturn("/notify");
        String[] headerNames3 = {"Content-Type", "fiware-service", "fiware-servicePath"};
        when(mockHttpServletRequest3.getHeaderNames()).thenReturn(
                Collections.enumeration(new ArrayList(Arrays.asList(headerNames))));
        when(mockHttpServletRequest3.getHeader("content-type")).thenReturn("application/json; charset=utf-8");
        when(mockHttpServletRequest3.getHeader("fiware-service")).thenReturn("myservice");
        when(mockHttpServletRequest3.getHeader("fiware-servicepath")).thenReturn("/myservicepath");
        when(mockHttpServletRequest3.getReader()).thenReturn(
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(notification3.getBytes()))));
        when(mockHttpServletRequest4.getMethod()).thenReturn("POST");
        when(mockHttpServletRequest4.getRequestURI()).thenReturn("/notify");
        String[] headerNames4 = {"Content-Type", "fiware-service", "fiware-servicePath"};
        when(mockHttpServletRequest4.getHeaderNames()).thenReturn(
                Collections.enumeration(new ArrayList(Arrays.asList(headerNames2))));
        when(mockHttpServletRequest4.getHeader("content-type")).thenReturn("application/json; charset=utf-8");
        when(mockHttpServletRequest4.getHeader("fiware-service")).thenReturn("myservice");
        when(mockHttpServletRequest4.getHeader("fiware-servicepath")).thenReturn("/a,/b");
        when(mockHttpServletRequest4.getReader()).thenReturn(
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(notification4.getBytes()))));
    } // setUp
    
    /**
     * [NGSIRestHandler.configure] -------- When not configured, the default values are used for non mandatory
     * parameters.
     */
    @Test
    public void testConfigureNotMandatoryParameters() {
        System.out.println(getTestTraceHead("[NGSIRestHandler.configure]")
                + "-------- When not configured, the default values are used for non mandatory parameters");
        NGSIRestHandler handler = new NGSIRestHandler();
        handler.configure(createContext(null, null, null,null)); // default configuration
        
        try {
            assertEquals("/notify", handler.getNotificationTarget());
            System.out.println(getTestTraceHead("[NGSIRestHandler.configure]")
                    + "-  OK  - The default configuration value for 'notification_target' is '/notify'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIRestHandler.configure]")
                    + "- FAIL - The default configuration value for 'notification_target' is '"
                    + handler.getNotificationTarget() + "'");
            throw e;
        } // try catch
        
        try {
            assertEquals("default", handler.getDefaultService());
            System.out.println(getTestTraceHead("[NGSIRestHandler.configure]")
                    + "-  OK  - The default configuration value for 'default_service' is 'default'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIRestHandler.configure]")
                    + "- FAIL - The default configuration value for 'default_service' is '"
                    + handler.getDefaultService() + "'");
            throw e;
        } // try catch

    } // testConfigureNotMandatoryParameters
    
    /**
     * [NGSIRestHandler.configure] -------- The configured default service can only contain alphanumerics and
     * underscores.
     */
    @Test
    public void testConfigureDefaultServiceIsLegal() {
        System.out.println(getTestTraceHead("[NGSIRestHandler.configure]")
                + "-------- The configured default service can only contain alphanumerics and underscores");
        NGSIRestHandler handler = new NGSIRestHandler();
        String configuredDefaultService = "default-service!!";
        handler.configure(createContext(null, configuredDefaultService, null,null));
        
        try {
            assertTrue(handler.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIRestHandler.configure]")
                    + "-  OK  - The configured default service '" + configuredDefaultService
                    + "' was detected as invalid");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIRestHandler.configure]")
                    + "- FAIL - The configured default service '" + configuredDefaultService
                    + "' was not detected as invalid");
            throw e;
        } // try catch
    } // testConfigureDefaultServiceIsLegal
    

    /**
     * [NGSIRestHandler.configure] -------- The configured notification target must start with '/'.
     */
    @Test
    public void testConfigureNotificationTargerStartsWithSlash() {
        System.out.println(getTestTraceHead("[NGSIRestHandler.configure]")
                + "-------- The configured notification target must start with '/'");
        NGSIRestHandler handler = new NGSIRestHandler();
        String configuredNotificationTarget = "/notify";
        handler.configure(createContext(configuredNotificationTarget, null, null,null));
        
        try {
            assertEquals(configuredNotificationTarget, handler.getNotificationTarget());
            assertTrue(!handler.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSIRestHandler.configure]")
                    + "-  OK  - The configured notification target '" + configuredNotificationTarget
                    + "' starts with '/'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIRestHandler.configure]")
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
        System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                + "-------- When a notification is sent, the headers are valid");
        NGSIRestHandler handler = new NGSIRestHandler();
        handler.configure(createContext(null, null, null,null)); // default configuration
        
        try {
            handler.getEvents(mockHttpServletRequest);
            System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                    + "-  OK  - The value for 'Content-Type' header is 'application/json; charset=utf-8'");
            System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                    + "-  OK  - The value for 'fiware-servicePath' header starts with '/'");
            System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                    + "-  OK  - The length of 'fiware-service' header value is "
                    + " less or equal than '" + NGSIConstants.SERVICE_HEADER_MAX_LEN + "'");
            System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                    + "-  OK  - The length of 'fiware-servicePath' header value "
                    + "is less or equal than '" + NGSIConstants.SERVICE_PATH_HEADER_MAX_LEN + "'");
            assertTrue(true);
        } catch (Exception e) {
            if (e.getMessage().contains("content type not supported")) {
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "- FAIL - The value for 'Content-Type' is not 'application/json; charset=utf-8'");
            } else if (e.getMessage().contains("header value must start with '/'")) {
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "- FAIL - The value for 'fiware-servicePath' does not start with '/'");
            } else if (e.getMessage().contains("'fiware-service' header length greater than")) {
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "- FAIL - The length of 'fiware-service' header value "
                        + "is greater than '" + NGSIConstants.SERVICE_HEADER_MAX_LEN + "'");
            } else if (e.getMessage().contains("'fiware-servicePath' header length greater than")) {
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "- FAIL - The length of 'fiware-servicePath' header "
                        + "value is greater than '" + NGSIConstants.SERVICE_PATH_HEADER_MAX_LEN + "'");
            } // if
            
            assertTrue(false);
        } // try catch // try catch
    } // testGetEventsContentTypeHeader
    
    /**
     * [NGSIRestHandler.getEvents] -------- When a the configuration is wrong, no events are obtained.
     */
    @Test
    public void testGetEventsNullEventsUponInvalidConfiguration() {
        System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                + "-------- When a the configuration is wrong, no evetns are obtained");
        NGSIRestHandler handler = new NGSIRestHandler();
        String configuredNotificationTarget = "notify"; // wrong value
        String configuredDefaultService = "default";
        String configuredDefaultServicePath = "something"; // wrong value
        handler.configure(createContext(configuredNotificationTarget, configuredDefaultService,
                configuredDefaultServicePath,null));
        List<Event> events;
        
        try {
            events = handler.getEvents(mockHttpServletRequest);
            
            try {
                assertEquals(0, events.size());
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "-  OK  - No events are processed since the configuration is wrong");
            } catch (AssertionError e1) {
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "- FAIL - The events are being processed despite of the configuration is wrong");
                throw e1;
            } // try catch
        } catch (Exception ex) {
            System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                    + "- FAIL - There was some problem while processing the events");
            assertTrue(false);
        } // try catch
    } // testGetEventsNullEventsUponInvalidConfiguration

    /**
     * [NGSIRestHandler.getEvents] -------- When a notification LD is sent as a Http message, a single Flume event
     * is generated.
     */
    @Test
    public void testGetEventsSingleEvent() {
        System.out.println(getTestTraceHead("[NGSIRestHandler.getEventsLD]")
                + "-------- When a notification LD is sent as a Http message, a single Flume event is generated");
        NGSIRestHandler handler = new NGSIRestHandler();
        handler.configure(createContext(null, null, null,"ld")); // default configuration
        List<Event> events;

        try {
            events = handler.getEvents(mockHttpServletRequest3);

            try {
                assertEquals(1, events.size());
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "-  OK  - A single LD event has been generated");
            } catch (AssertionError e1) {
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "- FAIL - 0, 2 or more than an LD event were generated");
                throw e1;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                    + "- FAIL - There was some problem while processing the events"+ e);
            assertTrue(false);
        } // try catch
    } // testGetEventsSingleEvent
    
    /**
     * [NGSIRestHandler.getEvents] -------- When a Flume event is generated, it contains fiware-service,
     * fiware-servicepath, fiware-correlator and transaction-id headers.
     */
    @Test
    public void testGetEventsHeadersInFlumeEvent() {
        System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                + "-------- When a Flume event is generated, it contains fiware-service,"
                + "fiware-correlator and transaction-id headers");
        NGSIRestHandler handler = new NGSIRestHandler();
        handler.configure(createContext(null, null, null,null)); // default configuration
        Map<String, String> headers;
        
        try {
            headers = handler.getEvents(mockHttpServletRequest).get(0).getHeaders();
            try {
                assertTrue(headers.containsKey("fiware-service"));
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "-  OK  - The generated Flume event contains 'fiware-service'");
            } catch (AssertionError e1) {
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "- FAIL - The generated Flume event does not contain 'fiware-service'");
                throw e1;
            } // try catch

            try {
                assertTrue(headers.containsKey("fiware-correlator"));
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "-  OK  - The generated Flume event contains 'fiware-correlator'");
            } catch (AssertionError e3) {
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "- FAIL - The generated Flume event does not contain 'fiware-correlator'");
                throw e3;
            } // try catch
            
            try {
                assertTrue(headers.containsKey("transaction-id"));
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "-  OK  - The generated Flume event contains 'transaction-id'");
            } catch (AssertionError e4) {
                System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                        + "- FAIL - The generated Flume event does not contain 'transaction-id'");
                throw e4;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIRestHandler.getEvents]")
                    + "- FAIL - There was some problem while processing the events");
            assertTrue(false);
        } // try catch
    } // testGetEventsHeadersInFlumeEvent


    
    /**
     * [NGSIRestHandler.generateUniqueId] -------- An internal transaction ID is generated.
     */
    @Test
    public void testGenerateUniqueIdTransIdGenerated() {
        System.out.println(getTestTraceHead("[NGSIRestHandler.generateUniqueId]")
                + "-------- An internal transaction ID is generated");
        NGSIRestHandler handler = new NGSIRestHandler();
        String generatedTransId = CommonUtils.generateUniqueId(null, null);
        
        try {
            assertTrue(generatedTransId != null);
            System.out.println(getTestTraceHead("[NGSIRestHandler.generateUniqueId]")
                    + "-  OK  - An internal transaction ID '" + generatedTransId + "' has been generated");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIRestHandler.generateUniqueId]")
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
        System.out.println(getTestTraceHead("[NGSIRestHandler.generateUniqueId]")
                + "-------- When a correlator ID is notified, it is reused");
        NGSIRestHandler handler = new NGSIRestHandler();
        String generatedTransId = "1111111111-111-1111111111";
        String notifiedCorrId = "1234567890-123-1234567890";
        String generatedCorrId = CommonUtils.generateUniqueId(notifiedCorrId, generatedTransId);
        
        try {
            assertEquals(notifiedCorrId, generatedCorrId);
            System.out.println(getTestTraceHead("[NGSIRestHandler.generateUniqueId]")
                    + "-  OK  - The notified transaction ID '" + notifiedCorrId + "' has been reused");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIRestHandler.generateUniqueId]")
                    + "- FAIL - The notified transaction ID '" + notifiedCorrId + "' has not not reused, '"
                    + generatedCorrId + "' has been generated instead");
            throw e;
        } // try catch
    } // testGenerateUniqueIdCorrIdReused
    
    /**
     * [NGSIRestHandler.generateUniqueId] -------- When a correlation ID is not notified, it is generated.
     */
    @Test
    public void testGenerateUniqueIdCorrIdGenerated() {
        System.out.println(getTestTraceHead("[NGSIRestHandler.generateUniqueId]")
                + "-------- When a correlation ID is not notified, it is generated");
        NGSIRestHandler handler = new NGSIRestHandler();
        String generatedTransId = "1234567890-123-1234567890";
        String notifiedCorrId = null;
        
        try {
            assertTrue(CommonUtils.generateUniqueId(notifiedCorrId, generatedTransId) != null);
            System.out.println(getTestTraceHead("[NGSIRestHandler.generateUniqueId]")
                    + "-  OK  - The transaction ID has been generated");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIRestHandler.generateUniqueId]")
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
        System.out.println(getTestTraceHead("[NGSIRestHandler.generateUniqueId]")
                + "-------- When a correlation ID is genereated, both "
                + "generated correlation ID and generated transaction ID have the same value");
        NGSIRestHandler handler = new NGSIRestHandler();
        String generatedTransId = "1234567890-123-1234567890";
        String notifiedCorrId = null;
        String generatedCorrId = CommonUtils.generateUniqueId(notifiedCorrId, generatedTransId);
        
        try {
            assertEquals(generatedTransId, generatedCorrId);
            System.out.println(getTestTraceHead("[NGSIRestHandler.generateUniqueId]")
                    + "-  OK  - The generated transaction ID '" + generatedTransId
                    + "' is equals to the generated correlator ID '" + generatedCorrId + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIRestHandler.generateUniqueId]")
                    + "- FAIL - The generated transaction ID '" + generatedTransId
                    + "' is not equals to the generated correlator ID '" + generatedCorrId + "'");
            throw e;
        } // try catch
    } // testGenerateUniqueIdSameCorrAndTransIds
    
    /**
     * [NGSIRestHandler.wrongServiceHeader] -------- A content-type different than 'application/json; charset=utf-8' is
 detected.
     */
    @Test
    public void testWrongContentType() {
        System.out.println(getTestTraceHead("[NGSIRestHandler.wrongContentType]")
                + "-------- A content-type different than 'application/json; charset=utf-8' is detected");
        NGSIRestHandler handler = new NGSIRestHandler();
        String wrongContentType = "application/url-encode";
        
        try {
            assertTrue(handler.wrongContentType(wrongContentType));
            System.out.println(getTestTraceHead("[NGSIRestHandler.wrongContentType]")
                    + "-  OK  - A wrong content-type '" + wrongContentType + "' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIRestHandler.wrongContentType]")
                    + "- FAIL - A wrong content-type '" + wrongContentType + "' has not been detected");
            throw e;
        } // try catch
    } // testWrongContentType
    
    /**
     * [NGSIRestHandler.wrongServiceHeaderLength] -------- A FIWARE service header whose length is greater than 50
     * characters is detected.
     */
    @Test
    public void testWrongServiceHeaderLength() {
        System.out.println(getTestTraceHead("[NGSIRestHandler.wrongServiceHeaderLength]")
                + "-------- A FIWARE service header whose length is greater than 50 characters is detected");
        NGSIRestHandler handler = new NGSIRestHandler();
        String wrongServiceHeader = "thisIsAFiwareServiceHeaderWhoseLengthIsGreaterThanTheAccepted50CharactersLimit";
        
        try {
            assertTrue(handler.wrongServiceHeaderLength(wrongServiceHeader));
            System.out.println(getTestTraceHead("[NGSIRestHandler.wrongServiceHeaderLength]")
                    + "-  OK  - A wrong FIWARE service header '" + wrongServiceHeader + "' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIRestHandler.wrongServiceHeaderLength]")
                    + "- FAIL - A wrong FIWARE service header '" + wrongServiceHeader + "' has not been detected");
            throw e;
        } // try catch
    } // testWrongServiceHeaderLength
    

    
    private Context createContext(String notificationTarget, String defaultService, String defaultServicePath,String ngsiVersion) {
        Context context = new Context();
        context.put("notification_target", notificationTarget);
        context.put("default_service", defaultService);
        context.put("default_service_path", defaultServicePath);
        return context;
    } // createContext

} // NGSIRestHandlerTest
