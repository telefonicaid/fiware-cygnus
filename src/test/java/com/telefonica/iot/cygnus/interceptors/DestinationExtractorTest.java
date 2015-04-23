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
import com.telefonica.iot.cygnus.interceptors.DestinationExtractor.MatchingRule;
import com.telefonica.iot.cygnus.utils.Constants;
import java.util.ArrayList;
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
public class DestinationExtractorTest {
    
    // instance to be tested
    private DestinationExtractor destExtractor;
    
    // other instances
    private Map<String, String> beforeInterceptingEventHeaders;
    private Event event;

    // constants
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
    } // setUp
    
    /**
     * Test of initialize method, of class DestinationExtractor.
     */
    @Test
    public void testInitialize() {
        System.out.println("Testing DestinationExtractor.initialize");
        destExtractor = new DestinationExtractor("conf/matching_table.conf.template");
        destExtractor.initialize();
        ArrayList<MatchingRule> matchingTable = destExtractor.getMatchingTable();
        assertTrue(matchingTable.size() == 3);
        MatchingRule firstRule = matchingTable.get(0);
        assertEquals(1, firstRule.getId());
        assertTrue(firstRule.getFields().size() == 2);
        assertEquals("Room\\.(\\d*)Room", firstRule.getRegex());
        assertEquals("numeric_rooms", firstRule.getDestination());
        assertEquals("rooms", firstRule.getDataset());
        MatchingRule secondRule = matchingTable.get(1);
        assertEquals(2, secondRule.getId());
        assertTrue(secondRule.getFields().size() == 1);
        assertEquals("Car", secondRule.getRegex());
        assertEquals("cars", secondRule.getDestination());
        assertEquals("vehicles", secondRule.getDataset());
        MatchingRule thirdRule = matchingTable.get(2);
        assertEquals(3, thirdRule.getId());
        assertTrue(thirdRule.getFields().size() == 1);
        assertEquals("GARDENS", thirdRule.getRegex());
        assertEquals("gardens", thirdRule.getDestination());
        assertEquals("city_indicators", thirdRule.getDataset());
    } // testInitialize
    
    /**
     * Test of intercept method, of class DestinationExtractor.
     */
    @Test
    public void testIntercept() {
        System.out.println("Testing DestinationExtractor.intercept (matching_table.conf exists)");
        destExtractor = new DestinationExtractor("conf/matching_table.conf.template");
        destExtractor.initialize();
        event = EventBuilder.withBody(eventData.getBytes(), beforeInterceptingEventHeaders);
        Event interceptedEvent = destExtractor.intercept(event);
        Map<String, String> afterInterceptingEventHeaders = interceptedEvent.getHeaders();
        String destinations = afterInterceptingEventHeaders.get(Constants.DESTINATION);
        assertEquals(destinations, "numeric_rooms,numeric_rooms");
        String datasets = afterInterceptingEventHeaders.get(Constants.HEADER_SERVICE_PATH);
        assertEquals(datasets, "rooms,rooms");
        
        System.out.println("Testing DestinationExtractor.intercept (matching_table.conf is not set)");
        destExtractor = new DestinationExtractor(null);
        destExtractor.initialize();
        event = EventBuilder.withBody(eventData.getBytes(), beforeInterceptingEventHeaders);
        interceptedEvent = destExtractor.intercept(event);
        afterInterceptingEventHeaders = interceptedEvent.getHeaders();
        destinations = afterInterceptingEventHeaders.get(Constants.DESTINATION);
        assertEquals(destinations, "room.1_room,room.22_room");
        datasets = afterInterceptingEventHeaders.get(Constants.HEADER_SERVICE_PATH);
        assertEquals(datasets, "def_servpath,def_servpath");
        
        System.out.println("Testing DestinationExtractor.intercept (matching_table.conf does not exist)");
        destExtractor = new DestinationExtractor("conf/anything.conf");
        destExtractor.initialize();
        event = EventBuilder.withBody(eventData.getBytes(), beforeInterceptingEventHeaders);
        interceptedEvent = destExtractor.intercept(event);
        afterInterceptingEventHeaders = interceptedEvent.getHeaders();
        destinations = afterInterceptingEventHeaders.get(Constants.DESTINATION);
        assertEquals(destinations, "room.1_room,room.22_room");
        datasets = afterInterceptingEventHeaders.get(Constants.HEADER_SERVICE_PATH);
        assertEquals(datasets, "def_servpath,def_servpath");
    } // testIntercept

} // DestinationExtractorTest
