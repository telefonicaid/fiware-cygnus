/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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
package es.tid.fiware.fiwareconnectors.cygnus.channels;

import org.apache.flume.Event;
import org.apache.flume.channel.file.FileChannel;

/**
 * CygnusFileChannel is an extension of Flume's FileChannel. Basically, it is the same channel but having methods
 * for sizing control.
 * 
 * @author frb
 */
public class CygnusFileChannel extends FileChannel implements CygnusChannel {
    
    private int numEvents;
    
    @Override
    protected void initialize() {
        super.initialize();
        numEvents = 0;
    } // initialize
    
    @Override
    public void put(Event event) {
        super.put(event);
        numEvents++;
    } // put
    
    @Override
    public Event take() {
        numEvents--;
        return super.take();
    } // take
    
    @Override
    public int getNumEvents() {
        return numEvents;
    } // getNumEvents
    
    @Override
    public void rollback() {
        numEvents++;
    } // rollback
    
} // CygnusFileChannel
