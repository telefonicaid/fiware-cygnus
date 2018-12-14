/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.metrics.CygnusMetrics;
import java.util.Date;
import org.apache.flume.sink.AbstractSink;

/**
 *
 * @author frb
 */
public abstract class CygnusSink extends AbstractSink {
    
    // statistics
    protected final long setupTime = new Date().getTime();
    protected long numProcessedEvents = 0;
    protected long numPersistedEvents = 0;
    protected CygnusMetrics serviceMetrics = new CygnusMetrics();
    
    /**
     * Gets the setup time.
     * @return The setup time (in miliseconds)
     */
    public long getSetupTime() {
        return setupTime;
    } // getSetupTime

    /**
     * Gets the number of processed events.
     * @return The number of processed events
     */
    public long getNumProcessedEvents() {
        return numProcessedEvents;
    } // getNumProcessedEvents

    /**
     * Gets the number of persisted events.
     * @return The number of persisted events.
     */
    public long getNumPersistedEvents() {
        return numPersistedEvents;
    } // getNumPersistedEvents
    
    /**
     * Sets the number of processed events.
     * @param n The number of processed events to be set
     */
    public void setNumProcessedEvents(long n) {
        numProcessedEvents = n;
    } // setNumProcessedEvents
    
    /**
     * Sets the number of persisted events.
     * @param n The number of persisted events to be set
     */
    public void setNumPersistedEvents(long n) {
        numPersistedEvents = n;
    } // setNumPersistedEvents
    
    /**
     * Gets serviceMetrics.
     * @return serviceMetrics
     */
    public CygnusMetrics getServiceMetrics() {
        return serviceMetrics;
    } // getServiceSubserviceMetrics
    
    /**
     * Sets serviceMetrics.
     * @param serviceMetrics
     */
    public void setServiceMetrics(CygnusMetrics serviceMetrics) {
        this.serviceMetrics = serviceMetrics;
    } // setServiceMetrics
    
} // CygnusSink
