/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.interceptors;

import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import java.util.Map;
import org.apache.flume.Event;

/**
 *
 * @author frb
 */
public class NGSIEvent implements Event {
    
    private Map<String, String> headers;
    private NotifyContextRequest originalNCR;
    private NotifyContextRequest mappedNCR;
    
    /**
     * Constructor.
     * @param headers
     * @param originalNCR
     * @param mappedNCR
     */
    public NGSIEvent(Map<String, String> headers, NotifyContextRequest originalNCR,
            NotifyContextRequest mappedNCR) {
        this.headers = headers;
        this.originalNCR = originalNCR;
        this.mappedNCR = mappedNCR;
    } // NGSIEvent

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    } // getHeaders

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    } // setHeaders

    @Override
    public byte[] getBody() {
        return null;
    } // getBody

    @Override
    public void setBody(byte[] bytes) {
    } // setBody
    
    public NotifyContextRequest getOriginalNCR() {
        return originalNCR;
    } // getOriginalNCR
    
    public void setOriginalNCR(NotifyContextRequest originalNCR) {
        this.originalNCR = originalNCR;
    } // setOriginalNCR
    
    public NotifyContextRequest getMappedNCR() {
        return mappedNCR;
    } // getMappedNCR
    
    public void setMappedNCR(NotifyContextRequest mappedNCR) {
        this.mappedNCR = mappedNCR;
    } // setMappedNCR
    
} // NGSIEvent
