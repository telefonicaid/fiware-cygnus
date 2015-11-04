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

package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.containers.NotifyContextRequest;

/**
 *
 * @author frb
 * 
 * Class representing a Cygnus event.
 * 
 */
public class CygnusEvent {

    private final long recvTimeTs;
    private final String service;
    private final String servicePath;
    private final String destination;
    private final NotifyContextRequest.ContextElement contextElement;

    /**
     * Constructor.
     * @param recvTimeTs
     * @param service
     * @param servicePath
     * @param destination
     * @param contextElement
     */
    public CygnusEvent(long recvTimeTs, String service, String servicePath, String destination,
            NotifyContextRequest.ContextElement contextElement) {
        this.recvTimeTs = recvTimeTs;
        this.service = service;
        this.servicePath = servicePath;
        this.destination = destination;
        this.contextElement = contextElement;
    } // CygnusEvent

    /**
     * Gets the reception time.
     * @return The receptcion time
     */
    public long getRecvTimeTs() {
        return recvTimeTs;
    } // getRecvTimeTs

    /**
     * Gets the service.
     * @return The service
     */
    public String getService() {
        return service;
    } // getService

    /**
     * Gets the service path.
     * @return The servive path
     */
    public String getServicePath() {
        return servicePath;
    } // getServicePath

    /**
     * Gets the destination.
     * @return The destination
     */
    public String getDestination() {
        return destination;
    } // getDestination

    /**
     * Gets the context element.
     * @return The context elemtn
     */
    public NotifyContextRequest.ContextElement getContextElement() {
        return contextElement;
    } // getContextElement
    
} // CygnusEvent
