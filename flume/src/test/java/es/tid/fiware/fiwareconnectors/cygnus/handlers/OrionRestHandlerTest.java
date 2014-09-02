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

package es.tid.fiware.fiwareconnectors.cygnus.handlers;

import es.tid.fiware.fiwareconnectors.cygnus.utils.TestConstants;
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
import java.util.regex.Matcher;
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
    private final String orionVersion = "orion/0.9.0";
    private final String orionVersionRegexPattern = ".*";
    private final String notificationsTarget = "/notify";
    private final String[] headerNamesStr = {"user-agent", "content-type", "fiware-service"};
    private final String contentType = "application/json";
    private final String fiwareOrg = "default_org";
    private final String requestMethod = "POST";
    
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
        context.put("orion_version", orionVersionRegexPattern);
        context.put("notification_target", notificationsTarget);
        
        // set up the behaviour of the mocked classes
        when(mockRequest.getMethod()).thenReturn(requestMethod);
        when(mockRequest.getRequestURI()).thenReturn(notificationsTarget);
        when(mockRequest.getHeaderNames()).thenReturn(
                Collections.enumeration(new ArrayList(Arrays.asList(headerNamesStr))));
        when(mockRequest.getHeader("user-agent")).thenReturn(orionVersion);
        when(mockRequest.getHeader("content-type")).thenReturn(contentType);
        when(mockRequest.getHeader("fiware-service")).thenReturn(fiwareOrg);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
                "<tag1>1</tag1>      <tag2>2</tag2>".getBytes()))));
    } // setUp
    
    /**
     * Test of configure method, of class OrionRestHandler.
     */
    @Test
    public void testConfigure() {
        System.out.println("configure");
        handler.configure(context);
        assertEquals(notificationsTarget, handler.getNotificationTarget());
    } // testConfigure

    /**
     * Test of getEvents method, of class OrionRestHandler.
     */
    @Test
    public void testGetEvents() throws Exception {
        System.out.println("getEvents");
        handler.configure(context);
        List result = handler.getEvents(mockRequest);
        assertTrue(result.size() == 1);
        Event event = (Event) result.get(0);
        Map<String, String> eventHeaders = event.getHeaders();
        byte[] eventMessage = event.getBody();
        assertTrue(eventHeaders.size() == 4);
        assertTrue(eventHeaders.containsKey("content-type"));
        assertTrue(eventHeaders.get("content-type").equals("application/json")
                || eventHeaders.get("content-type").equals("application/xml"));
        assertTrue(eventHeaders.containsKey(TestConstants.ORG_HEADER));
        assertTrue(eventHeaders.get(TestConstants.ORG_HEADER).equals("default_org"));
        assertTrue(eventMessage.length != 0);
    } // testGetEvents
    
} // OrionRestHandlerTest