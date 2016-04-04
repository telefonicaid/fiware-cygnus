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
import javax.servlet.http.HttpServletRequest;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
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
    private HttpServletRequest mockHttpServletRequest1;
    
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
        when(mockHttpServletRequest1.getMethod()).thenReturn("POST");
        when(mockHttpServletRequest1.getRequestURI()).thenReturn("/notify");
        String[] headerNames = {"Content-Type", "fiware-service", "fiware-servicePath"};
        when(mockHttpServletRequest1.getHeaderNames()).thenReturn(
                Collections.enumeration(new ArrayList(Arrays.asList(headerNames))));
        when(mockHttpServletRequest1.getHeader("content-type")).thenReturn("application/json");
        when(mockHttpServletRequest1.getHeader("fiware-service")).thenReturn("myservice");
        when(mockHttpServletRequest1.getHeader("fiware-servicepath")).thenReturn("/myservicepath");
        when(mockHttpServletRequest1.getReader()).thenReturn(
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
                        "something_not_empty".getBytes()))));
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
            handler.getEvents(mockHttpServletRequest1);
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
        
        try {
            assertEquals(0, handler.getEvents(mockHttpServletRequest1).size());
            System.out.println("[OrionRestHandler.getEvents] -  OK  - No events are processed since the "
                    + "configuration is wrong");
        } catch (AssertionError e1) {
            System.out.println("[OrionRestHandler.getEvents] - FAIL - The events are being processed "
                    + "despite of the configuration is wrong");
            throw e1;
        } catch (Exception e2) {
            System.out.println("[OrionRestHandler.getEvents] - FAIL - There was some problem while processing "
                    + "the events");
            assertTrue(false);
        } // try catch
    } // testGetEventsNullEventsUponInvalidConfiguration
    
    /**
     * [OrionRestHandler.generateTransId] -------- When a transcation ID is notified, it is reused.
     */
    @Test
    public void testGenerateTransIdReused() {
        System.out.println("[OrionRestHandler.generateTransId] -------- When a transcation ID is notified, it is "
                + "reused");
        OrionRestHandler handler = new OrionRestHandler();
        String notifiedTransId = "1234567890-123-1234567890";
        
        try {
            assertEquals(notifiedTransId, handler.generateTransId(notifiedTransId));
            System.out.println("[OrionRestHandler.generateTransId] -  OK  - The notified transaction ID '"
                    + notifiedTransId + "' is reused");
        } catch (AssertionError e) {
            System.out.println("[OrionRestHandler.generateTransId] - FAIL - The notified transaction ID '"
                    + notifiedTransId + "' is not reused");
            throw e;
        } // try catch
    } // testGenerateTransIdReused
    
    /**
     * [OrionRestHandler.generateTransId] -------- When a transcation ID is notified, it is reused.
     */
    @Test
    public void testGenerateTransIdGenerated() {
        System.out.println("[OrionRestHandler.generateTransId] -------- When a transcation ID is not notified, "
                + "it is generated");
        OrionRestHandler handler = new OrionRestHandler();
        String notifiedTransId = null;
        
        try {
            assertTrue(handler.generateTransId(notifiedTransId) != null);
            System.out.println("[OrionRestHandler.generateTransId] -  OK  - The transaction ID has been generated");
        } catch (AssertionError e) {
            System.out.println("[OrionRestHandler.generateTransId] - FAIL - The transaction ID has not been "
                    + "generated");
            throw e;
        } // try catch
    } // testGenerateTransIdGenerated
    
    private Context createContext(String notificationTarget, String defaultService, String defaultServicePath) {
        Context context = new Context();
        context.put("notification_target", notificationTarget);
        context.put("default_service", defaultService);
        context.put("default_service_path", defaultServicePath);
        return context;
    } // createContext

} // OrionRestHandlerTest