/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.interceptors;

import java.util.HashMap;
import java.util.Map;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.Event;
import com.telefonica.iot.cygnus.interceptors.GroupingInterceptor.GroupingRule;
import com.telefonica.iot.cygnus.utils.Constants;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class GroupingInterceptorTest {
    
    // instance to be tested
    private GroupingInterceptor groupingInterceptor;
    
    // other instances
    private Map<String, String> beforeInterceptingEventHeaders;
    private Event event;

    // constants
    private final String groupingRulesFileName = "conf/grouping_rules.conf.deleteme";
    private final String jsonRules = ""
            + "{\n"
            + "    \"grouping_rules\": [\n"
            + "        {\n"
            + "            \"id\": 1,\n"
            + "            \"fields\": [\n"
            + "                \"entityId\",\n"
            + "                \"entityType\"\n"
            + "            ],\n"
            + "            \"regex\": \"Room\\.(\\d*)Room\",\n"
            + "            \"destination\": \"numeric_rooms\",\n"
            + "            \"fiware_service_path\": \"rooms\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"id\": 2,\n"
            + "            \"fields\": [\n"
            + "                \"entityId\"\n"
            + "            ],\n"
            + "            \"regex\": \"Car\",\n"
            + "            \"destination\": \"cars\",\n"
            + "            \"fiware_service_path\": \"vehicles\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"id\": 3,\n"
            + "            \"fields\": [\n"
            + "                \"servicePath\"\n"
            + "            ],\n"
            + "            \"regex\": \"GARDENS\",\n"
            + "            \"destination\": \"gardens\",\n"
            + "            \"fiware_service_path\": \"city_indicators\",\n"
            + "            \"some_other_field_to_be_ignored\": \"xxx\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"id\": 4\n"
            + "        },\n"
            + "        {\n"
            + "            \"id\": 5,\n"
            + "            \"fields\": [\n"
            + "            ],\n"
            + "            \"regex\": \"GARDENS\",\n"
            + "            \"destination\": \"gardens\",\n"
            + "            \"fiware_service_path\": \"city_indicators\",\n"
            + "            \"some_other_field_to_be_ignored\": \"xxx\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"id\": 6,\n"
            + "            \"fields\": [\n"
            + "                \"servicePath\"\n"
            + "            ],\n"
            + "            \"regex\": \"GARDENS\",\n"
            + "            \"destination\": \"gardens\",\n"
            + "            \"fiware_service_path\": \"\",\n"
            + "            \"some_other_field_to_be_ignored\": \"xxx\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"id\": \"abc\",\n"
            + "            \"fields\": [\n"
            + "                \"servicePath\"\n"
            + "            ],\n"
            + "            \"regex\": \"GARDENS\",\n"
            + "            \"destination\": \"gardens\",\n"
            + "            \"fiware_service_path\": \"\",\n"
            + "            \"some_other_field_to_be_ignored\": \"xxx\"\n"
            + "        }\n"
            + "    ]\n"
            + "}";
    
    private final String eventData = ""
            + "<notifyContextRequest>"
            +   "<subscriptionId>51c0ac9ed714fb3b37d7d5a8</subscriptionId>"
            +   "<originator>localhost</originator>"
            +   "<contextResponseList>"
            +     "<contextElementResponse>"
            +       "<contextElement>"
            +         "<entityId type=\"Room\" isPattern=\"false\">"
            +           "<id>Room.1</id>"
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
            +     "<contextElementResponse>"
            +       "<contextElement>"
            +         "<entityId type=\"Room\" isPattern=\"false\">"
            +           "<id>Room.22</id>"
            +         "</entityId>"
            +         "<contextAttributeList>"
            +           "<contextAttribute>"
            +             "<name>temperature</name>"
            +             "<type>centigrade</type>"
            +             "<contextValue>32.1</contextValue>"
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
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // the instance of the tested class can not be setup here since it depends on a variable
        
        // set up other instances
        beforeInterceptingEventHeaders = new HashMap<String, String>();
        beforeInterceptingEventHeaders.put(Constants.HEADER_CONTENT_TYPE, "application/xml");
        beforeInterceptingEventHeaders.put(Constants.HEADER_SERVICE_PATH, "def_servpath");
        
        // create a temporal grouping rules file
        PrintWriter writer = new PrintWriter(groupingRulesFileName, "UTF-8");
        writer.println(jsonRules);
        writer.close();
    } // setUp
    
    /**
     * Tears down the tests.
     */
    @After
    public void tearDown() {
        File file = new File(groupingRulesFileName);
        file.delete();
    } // tearDown
    
    /**
     * Test of initialize method, of class GroupingInterceptor.
     */
    @Test
    public void testInitialize() {
        System.out.println("Testing DestinationExtractor.initialize");
        groupingInterceptor = new GroupingInterceptor(groupingRulesFileName);
        groupingInterceptor.initialize();
        LinkedList<GroupingRule> groupingRules = groupingInterceptor.getGroupingRules();
        assertTrue(groupingRules.size() == 3); // there are 5 rules, but two are invalid
        GroupingRule firstRule = groupingRules.get(0);
        assertEquals(1, firstRule.getId());
        assertTrue(firstRule.getFields().size() == 2);
        assertEquals("Room\\.(\\d*)Room", firstRule.getRegex());
        assertEquals("numeric_rooms", firstRule.getDestination());
        assertEquals("rooms", firstRule.getNewFiwareServicePath());
        GroupingRule secondRule = groupingRules.get(1);
        assertEquals(2, secondRule.getId());
        assertTrue(secondRule.getFields().size() == 1);
        assertEquals("Car", secondRule.getRegex());
        assertEquals("cars", secondRule.getDestination());
        assertEquals("vehicles", secondRule.getNewFiwareServicePath());
        GroupingRule thirdRule = groupingRules.get(2);
        assertEquals(3, thirdRule.getId());
        assertTrue(thirdRule.getFields().size() == 1);
        assertEquals("GARDENS", thirdRule.getRegex());
        assertEquals("gardens", thirdRule.getDestination());
        assertEquals("city_indicators", thirdRule.getNewFiwareServicePath());
    } // testInitialize
    
    /**
     * Test of intercept method, of class GroupingInterceptor.
     */
    @Test
    public void testIntercept() {
        System.out.println("Testing DestinationExtractor.intercept (grouping_rules.conf exists)");
        
        // create a grouping interceptor
        groupingInterceptor = new GroupingInterceptor(groupingRulesFileName);
        groupingInterceptor.initialize();
        
        // create a list of events to be intercepted
        event = EventBuilder.withBody(eventData.getBytes(), beforeInterceptingEventHeaders);
        ArrayList<Event> events = new ArrayList<Event>();
        events.add(event);
        
        // intercept the events
        List<Event> interceptedEvents = groupingInterceptor.intercept(events);
        
        // analyze the validity of the intercepted events
        assertTrue(interceptedEvents.size() == 1);
        Event interceptedEvent = interceptedEvents.get(0);
        Map<String, String> afterInterceptingEventHeaders = interceptedEvent.getHeaders();
        String destinations = afterInterceptingEventHeaders.get(Constants.DESTINATION);
        assertEquals(destinations, "numeric_rooms,numeric_rooms");
        String datasets = afterInterceptingEventHeaders.get(Constants.HEADER_SERVICE_PATH);
        assertEquals(datasets, "rooms,rooms");
        
        System.out.println("Testing DestinationExtractor.intercept (grouping_rules.conf is not set)");
        
        // create a grouping interceptor
        groupingInterceptor = new GroupingInterceptor(null);
        groupingInterceptor.initialize();
        
        // create a list of events to be intercepted
        event = EventBuilder.withBody(eventData.getBytes(), beforeInterceptingEventHeaders);
        events = new ArrayList<Event>();
        events.add(event);
        
        // intercept the events
        interceptedEvents = groupingInterceptor.intercept(events);
        
        // analyze the validity of the intercepted events
        assertTrue(interceptedEvents.size() == 1);
        interceptedEvent = interceptedEvents.get(0);
        afterInterceptingEventHeaders = interceptedEvent.getHeaders();
        destinations = afterInterceptingEventHeaders.get(Constants.DESTINATION);
        assertEquals(destinations, "room.1_room,room.22_room");
        datasets = afterInterceptingEventHeaders.get(Constants.HEADER_SERVICE_PATH);
        assertEquals(datasets, "def_servpath,def_servpath");
        
        System.out.println("Testing DestinationExtractor.intercept (grouping_rules.conf does not exist)");
        
        // create a grouping interceptor
        groupingInterceptor = new GroupingInterceptor("whatever");
        groupingInterceptor.initialize();
        
        // create a list of events to be intercepted
        event = EventBuilder.withBody(eventData.getBytes(), beforeInterceptingEventHeaders);
        events = new ArrayList<Event>();
        events.add(event);
        
        // intercept the events
        interceptedEvents = groupingInterceptor.intercept(events);
        
        // analyze the validity of the intercepted events
        assertTrue(interceptedEvents.size() == 1);
        interceptedEvent = interceptedEvents.get(0);
        afterInterceptingEventHeaders = interceptedEvent.getHeaders();
        destinations = afterInterceptingEventHeaders.get(Constants.DESTINATION);
        assertEquals(destinations, "room.1_room,room.22_room");
        datasets = afterInterceptingEventHeaders.get(Constants.HEADER_SERVICE_PATH);
        assertEquals(datasets, "def_servpath,def_servpath");
    } // testIntercept

} // GroupingInterceptorTest
