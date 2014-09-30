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
 * francisco.romerobueno@telefonica.com
 */

package es.tid.fiware.fiwareconnectors.cygnus.interceptors;

import java.util.HashMap;
import java.util.Map;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.Event;
import es.tid.fiware.fiwareconnectors.cygnus.interceptors.DestinationExtractor.MatchingRule;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
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
    private Map<String, String> eventHeaders;
    private Event event;

    // constants
    private final String matchingTableFile = "conf/matching_table.conf.template";
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
            +             "<value>26.5</value>"
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
            +             "<value>32.1</value>"
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
        // set up the instance of the tested class
        destExtractor = new DestinationExtractor(matchingTableFile);
        
        // set up other instances
        eventHeaders = new HashMap<String, String>();
        eventHeaders.put(Constants.CONTENT_TYPE, "application/xml");
        event = EventBuilder.withBody(eventData.getBytes(), eventHeaders);
    } // setUp
    
    /**
     * Test of initialize method, of class DestinationExtractor.
     */
    @Test
    public void testInitialize() {
        System.out.println("Testing EntityDescriptorExtractor.initialize");
        destExtractor.initialize();
        ArrayList<MatchingRule> matchingTable = destExtractor.getMatchingTable();
        assertTrue(matchingTable.size() == 2);
        MatchingRule firstRule = matchingTable.get(0);
        assertEquals(1, firstRule.getId());
        assertTrue(firstRule.getFields().size() == 2);
        assertEquals("Room\\.(\\d*)Room", firstRule.getRegex());
        assertEquals("rooms", firstRule.getDestination());
        MatchingRule secondRule = matchingTable.get(1);
        assertEquals(2, secondRule.getId());
        assertTrue(secondRule.getFields().size() == 1);
        assertEquals("Car", secondRule.getRegex());
        assertEquals("cars", secondRule.getDestination());
    } // testInitialize
    
    /**
     * Test of intercept method, of class DestinationExtractor.
     */
    @Test
    public void testIntercept() {
        destExtractor.initialize();
        Event interceptedEvent = destExtractor.intercept(event);
        String destination = interceptedEvent.getHeaders().get(Constants.DESTINATION);
        assertEquals(destination, "rooms,rooms");
    } // testIntercept

} // DestinationExtractorTest
