/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package es.tid.fiware.fiwareconnectors.cygnus.handlers;

import es.tid.fiware.fiwareconnectors.cygnus.utils.TestConstants;
import es.tid.fiware.fiwareconnectors.cygnus.utils.TestUtils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*; // this is required by "fail" like assertions
import static org.mockito.Mockito.*; // this is required by "when" like functions
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Before;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.junit.Test;
import org.mockito.Mock;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class OrionRestHandlerTest {

    // instance to be tested
    private OrionRestHandler handler;
    
    // other instances
    private Context context;
    
    // mocks
    @Mock
    private HttpServletRequest mockRequest;
    
    // constants
    private final String configuredNotificationTarget = "/notify";
    private final String configuredEventsTTL = "10";
    private final String configuredDefaultService = "a.SERV_with-rare chars%@";
    private final String configuredDefaultServicePath = "a.SERVPATH_with-rare chars%@";
    private final String rootServicePath = "/";
    private final String[] notificationHeaderNamesStr = {"user-agent", "content-type", "fiware-service",
        "fiware-servicepath"};
    private final String notificationNotificationTarget = "/notify";
    private final String notificationContentType = "application/json";
    private final String notificationRequestMethod = "POST";
    private final String notificationUserAgent = "orion/0.9.0";
    private final String notificationService = "a.SERV_with-rare chars%@";
    private final String notificationServicePath = "a.SERVPATH_with-rare chars%@";
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        handler = new OrionRestHandler();
        
        // set up other instances
        context = new Context();
        context.put(TestConstants.PARAM_DEFAULT_SERVICE, configuredDefaultService);
        context.put(TestConstants.PARAM_DEFAULT_SERVICE_PATH, configuredDefaultServicePath);
        context.put(TestConstants.PARAM_EVENTS_TTL, configuredEventsTTL);
        context.put(TestConstants.PARAM_NOTIFICATION_TARGET, configuredNotificationTarget);
        
        // set up the behaviour of the mocked classes
        when(mockRequest.getMethod()).thenReturn(notificationRequestMethod);
        when(mockRequest.getRequestURI()).thenReturn(notificationNotificationTarget);
        when(mockRequest.getHeaderNames()).thenReturn(
                Collections.enumeration(new ArrayList(Arrays.asList(notificationHeaderNamesStr))),
                Collections.enumeration(new ArrayList(Arrays.asList(notificationHeaderNamesStr))));
        when(mockRequest.getHeader("user-agent")).thenReturn(notificationUserAgent);
        when(mockRequest.getHeader("content-type")).thenReturn(notificationContentType);
        when(mockRequest.getHeader("fiware-service")).thenReturn(notificationService);
        when(mockRequest.getHeader("fiware-servicepath")).thenReturn(notificationServicePath, rootServicePath);
        when(mockRequest.getReader()).thenReturn(
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
                        "<tag1>1</tag1>      <tag2>2</tag2>".getBytes()))),
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
                        "<tag1>1</tag1>      <tag2>2</tag2>".getBytes()))));
    } // setUp
    
    /**
     * Test of configure method, of class OrionRestHandler.
     */
    @Test
    public void testConfigure() {
        System.out.println("Testing 'configure' method from class 'OrionRestHandler'");
        handler.configure(context);
        assertEquals(configuredNotificationTarget, handler.getNotificationTarget());
        assertEquals(configuredEventsTTL, handler.getEventsTTL());
        assertEquals(TestUtils.encode(configuredDefaultService), handler.getDefaultService());
        assertEquals(TestUtils.encode(configuredDefaultServicePath), handler.getDefaultServicePath());
    } // testConfigure

    /**
     * Test of getEvents method, of class OrionRestHandler.
     */
    @Test
    public void testGetEvents() throws Exception {
        System.out.println("Testing 'getEvents' method from class 'OrionRestHandler' (invalid characters");
        handler.configure(context);
        List result = handler.getEvents(mockRequest);
        assertTrue(result.size() == 1);
        Event event = (Event) result.get(0);
        Map<String, String> eventHeaders = event.getHeaders();
        byte[] eventMessage = event.getBody();
        assertTrue(eventHeaders.size() == 5);
        assertTrue(eventHeaders.containsKey("content-type"));
        assertTrue(eventHeaders.get("content-type").equals("application/json")
                || eventHeaders.get("content-type").equals("application/xml"));
        assertTrue(eventHeaders.containsKey(TestConstants.HEADER_SERVICE));
        assertEquals(eventHeaders.get(TestConstants.HEADER_SERVICE), TestUtils.encode(notificationService));
        assertTrue(eventHeaders.containsKey(TestConstants.HEADER_SERVICE_PATH));
        assertEquals(eventHeaders.get(TestConstants.HEADER_SERVICE_PATH), TestUtils.encode(notificationServicePath));
        assertTrue(eventMessage.length != 0);
        
        System.out.println("Testing 'getEvents' method from class 'OrionRestHandler' (\"root\" servicePath name");
        context.put(TestConstants.PARAM_DEFAULT_SERVICE_PATH, rootServicePath);
        handler.configure(context);
        result = handler.getEvents(mockRequest);
        assertTrue(result.size() == 1);
        event = (Event) result.get(0);
        eventHeaders = event.getHeaders();
        eventMessage = event.getBody();
        assertTrue(eventHeaders.size() == 5);
        assertTrue(eventHeaders.containsKey("content-type"));
        assertTrue(eventHeaders.get("content-type").equals("application/json")
                || eventHeaders.get("content-type").equals("application/xml"));
        assertTrue(eventHeaders.containsKey(TestConstants.HEADER_SERVICE));
        assertEquals(eventHeaders.get(TestConstants.HEADER_SERVICE), TestUtils.encode(notificationService));
        assertTrue(eventHeaders.containsKey(TestConstants.HEADER_SERVICE_PATH));
        assertEquals(eventHeaders.get(TestConstants.HEADER_SERVICE_PATH), TestUtils.encode(rootServicePath));
        assertTrue(eventMessage.length != 0);
    } // testGetEvents
    
} // OrionRestHandlerTest