/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.channels;

import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.lang.reflect.Field;
import java.util.Date;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.instrumentation.ChannelCounter;

/**
 * CygnusMemoryChannel is an extension of Flume's MemoryChannel. Basically, it is the same channel but having methods
 * for sizing control.
 * 
 * @author frb
 */
public class CygnusMemoryChannel extends MemoryChannel implements CygnusChannel {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(CygnusMemoryChannel.class);
    private long setupTime;
    private ChannelCounter channelCounterRef;
    private long accPutsOK;
    private long accPutsFail;
    private long accTakesOK;
    private long accTakesFail;
    
    @Override
    protected void initialize() {
        super.initialize();
        
        try {
            Field f = MemoryChannel.class.getDeclaredField("channelCounter");
            f.setAccessible(true);
            channelCounterRef = (ChannelCounter) f.get(this);
        } catch (NoSuchFieldException e) {
            LOGGER.error(e.getMessage());
        } catch (SecurityException e) {
            LOGGER.error(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage());
        } // try catch
        
        setupTime = new Date().getTime();
        accPutsOK = 0;
        accPutsFail = 0;
        accTakesOK = 0;
        accTakesFail = 0;
    } // initialize

    @Override
    public long getSetupTime() {
        return setupTime;
    } // getSetupTime
    
    @Override
    public long getNumEvents() {
        return channelCounterRef.getChannelSize();
    } // getNumEvents
    
    @Override
    public long getNumPutsOK() {
        return channelCounterRef.getEventPutSuccessCount()
                - accPutsOK;
    } // getNumPutsOK
    
    @Override
    public long getNumPutsFail() {
        return channelCounterRef.getEventPutAttemptCount()
                - channelCounterRef.getEventPutSuccessCount()
                - accPutsFail;
    } // getNumPutsFail
    
    @Override
    public long getNumTakesOK() {
        return channelCounterRef.getEventTakeSuccessCount()
                - accTakesOK;
    } // getNumTakesOK
    
    @Override
    public long getNumTakesFail() {
        return channelCounterRef.getEventTakeAttemptCount()
                - channelCounterRef.getEventTakeSuccessCount()
                - accTakesFail;
    } // getNumTakesFail
    
    @Override
    public void setNumPutsOK(long n) {
        accPutsOK = channelCounterRef.getEventPutSuccessCount() - n;
    } // setNumPutsOK
    
    @Override
    public void setNumPutsFail(long n) {
        accPutsFail = channelCounterRef.getEventPutAttemptCount() - channelCounterRef.getEventPutSuccessCount() - n;
    } // setNumPutsFail
    
    @Override
    public void setNumTakesOK(long n) {
        accTakesOK = channelCounterRef.getEventTakeSuccessCount() - n;
    } // setNumTakesOK

    @Override
    public void setNumTakesFail(long n) {
        accTakesFail = channelCounterRef.getEventTakeAttemptCount() - channelCounterRef.getEventTakeSuccessCount();
    } // setNumTakesFail

} // CygnusMemoryChannel
