/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.channels;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.channel.MemoryChannel;

/**
 * CygnusMemoryChannel is an extension of Flume's MemoryChannel. Basically, it is the same channel but having methods
 * for sizing control.
 * 
 * @author frb
 */
public class CygnusMemoryChannel extends MemoryChannel implements CygnusChannel {
    
    private int numEvents;
    private int capacity;
    
    @Override
    public void configure(Context context) {
        super.configure(context);
        capacity = context.getInteger("capacity");
    } // configure
    
    @Override
    protected void initialize() {
        super.initialize();
        numEvents = 0;
    } // initialize
    
    @Override
    public void put(Event event) {
        if (numEvents != capacity) {
            numEvents++;
        } // if
        
        // independently of the remaining capacity, call the super version of the method in order to behave as a
        // MemoryChannel (exceptions, errors, etc)
        super.put(event);
    } // put
    
    @Override
    public Event take() {
        Event event = super.take();
        
        if (event != null) {
            numEvents--;
        } // if
        
        return event;
    } // take
    
    @Override
    public int getNumEvents() {
        return numEvents;
    } // getNumEvents
    
    @Override
    public void rollback() {
        numEvents++;
    } // rollback
    
} // CygnusMemoryChannel
