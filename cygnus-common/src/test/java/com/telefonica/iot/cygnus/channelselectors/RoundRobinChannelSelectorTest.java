/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.channelselectors;

import java.util.ArrayList;
import java.util.List;
import org.apache.flume.Channel;
import static org.junit.Assert.*; // this is required by "fail" like assertions
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.channel.MemoryChannel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */

@RunWith(MockitoJUnitRunner.class)
public class RoundRobinChannelSelectorTest {
    
    // mocks
    @Mock
    private Event event;
    
    // instance to be tested
    private RoundRobinChannelSelector channelSelector;
    
    // other instances
    private Context context;

    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        channelSelector = new RoundRobinChannelSelector();
        Channel channel1 = new MemoryChannel();
        channel1.setName("ch1");
        Channel channel2 = new MemoryChannel();
        channel2.setName("ch2");
        Channel channel3 = new MemoryChannel();
        channel3.setName("ch3");
        Channel channel4 = new MemoryChannel();
        channel4.setName("ch4");
        Channel channel5 = new MemoryChannel();
        channel5.setName("ch5");
        Channel channel6 = new MemoryChannel();
        channel6.setName("ch6");
        ArrayList<Channel> allChannels = new ArrayList<Channel>();
        allChannels.add(channel1);
        allChannels.add(channel2);
        allChannels.add(channel3);
        allChannels.add(channel4);
        allChannels.add(channel5);
        allChannels.add(channel6);
        channelSelector.setChannels(allChannels);
        
        // set up other instances
        context = new Context();
        context.put("storages", "3");
        context.put("storages.storage1", "ch1");
        context.put("storages.storage2", "ch2,ch3");
        context.put("storages.storage3", "ch4,ch5,ch6");
    } // setUp
    
    /**
     * Test of configure method, of class RoundRobinChannelSelector.
     */
    @Test
    public void testConfigure() {
        System.out.println("Testing RoundRobinChannelSelector.configure");
        channelSelector.configure(context);
        assertTrue(true); // nothing to test, really
    } // testConfigure
    
    /**
     * Test of getOptionalChannels method, of class RoundRobinChannelSelector.
     */
    @Test
    public void testGetOptionalChannels() {
        System.out.println("Testing RoundRobinChannelSelector.getOptionalChannels");
        channelSelector.configure(context);
        assertEquals(0, channelSelector.getOptionalChannels(event).size());
    } // testGetOptionalChannels
    
    /**
     * Test of getRequiredChannels method, of class RoundRobinChannelSelector.
     */
    @Test
    public void testGetRequiredChannels() {
        System.out.println("Testing RoundRobinChannelSelector.getRequiredChannels");
        channelSelector.configure(context);
        
        // first round
        List<Channel> requiredChannels = channelSelector.getRequiredChannels(event);
        assertEquals(3, requiredChannels.size());
        assertEquals("ch1", requiredChannels.get(0).getName());
        assertEquals("ch2", requiredChannels.get(1).getName());
        assertEquals("ch4", requiredChannels.get(2).getName());
        
        // second round
        requiredChannels = channelSelector.getRequiredChannels(event);
        assertEquals(3, requiredChannels.size());
        assertEquals("ch1", requiredChannels.get(0).getName());
        assertEquals("ch3", requiredChannels.get(1).getName());
        assertEquals("ch5", requiredChannels.get(2).getName());
        
        // third round
        requiredChannels = channelSelector.getRequiredChannels(event);
        assertEquals(3, requiredChannels.size());
        assertEquals("ch1", requiredChannels.get(0).getName());
        assertEquals("ch2", requiredChannels.get(1).getName());
        assertEquals("ch6", requiredChannels.get(2).getName());
        
        // fourth round
        requiredChannels = channelSelector.getRequiredChannels(event);
        assertEquals(3, requiredChannels.size());
        assertEquals("ch1", requiredChannels.get(0).getName());
        assertEquals("ch3", requiredChannels.get(1).getName());
        assertEquals("ch4", requiredChannels.get(2).getName());
    } // testGetRequiredChannels
    
} // RoundRobinChannelSelectorTest
