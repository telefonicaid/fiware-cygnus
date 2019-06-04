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
package com.telefonica.iot.cygnus.interceptors;

import com.jcraft.jsch.Logger;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.NGSIMySQLSink;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import java.util.ArrayList;
import java.util.Map;
import org.apache.flume.Event;

/**
 *
 * @author frb
 */
public class NGSIEvent implements Event {
    
    private Map<String, String> headers;
    private byte[] body;
    private ContextElement originalCE;
    private ContextElement mappedCE;
    
    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIEvent.class);
    
    /**
     * Constructor.
     * @param headers
     * @param body
     * @param originalCE
     * @param mappedCE
     */
    public NGSIEvent(Map<String, String> headers, byte[] body, ContextElement originalCE, ContextElement mappedCE) {
        this.headers = headers;
        this.body = body;
        this.originalCE = originalCE;
        this.mappedCE = mappedCE;
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
        return body;
    } // getBody

    @Override
    public void setBody(byte[] body) {
        this.body = body;
    } // setBody
    
    public ContextElement getOriginalCE() {
        return originalCE;
    } // getOriginalNCR
    
    public void setOriginalCE(ContextElement originalCE) {
        this.originalCE = originalCE;
    } // setOriginalNCR
    
    public ContextElement getMappedCE() {
        return mappedCE;
    } // getMappedNCR
    
    public void setMappedCE(ContextElement mappedCE) {
        this.mappedCE = mappedCE;
    } // setMappedNCR
    
    /*
     * From here on, methods used by custom sinks. They are smart wrappers of the above methods.
     */
    
    public long getRecvTimeTs() {
        return new Long(headers.get(NGSIConstants.FLUME_HEADER_TIMESTAMP));
    } // getRecvTimeTs
    
    public String getServiceForData() {
        return headers.get(CommonConstants.HEADER_FIWARE_SERVICE);
    } // getServiceForData
    
    /**
     * Gets the service both for data and for naming.
     * @param enableMappings
     * @return The service both for data and for naming
     */
    public String getServiceForNaming(boolean enableMappings) {
        if (enableMappings) {
            return headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE);
        } else {
            return headers.get(CommonConstants.HEADER_FIWARE_SERVICE);
        } // if else
    } // getServiceForNaming
    
    /**
     * Gets the service path for data.
     * @return The service path for data
     */
    public String getServicePathForData() {
        return headers.get(CommonConstants.HEADER_FIWARE_SERVICE_PATH);
    } // getOriginalServicePath

    /**
     * Gets the service path for naming.
     * @param enableGrouping
     * @param enableMappings
     * @return The service path for naming
     */
    public String getServicePathForNaming(boolean enableGrouping, boolean enableMappings) {
        if (enableGrouping) {
            return headers.get(NGSIConstants.FLUME_HEADER_GROUPED_SERVICE_PATH);
        } else if (enableMappings) {
            return headers.get(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH);
        } else {
            return headers.get(CommonConstants.HEADER_FIWARE_SERVICE_PATH);
        } // if else
    } // getMappedServicePath
    
    /**
     * Gets the entity for naming.
     * @param enableGrouping
     * @param enableMappings
     * @param enableEncoding
     * @return The entity for naming
     */
    public String getEntityForNaming(boolean enableGrouping, boolean enableMappings, boolean enableEncoding) {
        if (enableGrouping) {
            return headers.get(NGSIConstants.FLUME_HEADER_GROUPED_ENTITY);
        } else if (enableMappings) {
            if (mappedCE.getType() == null || mappedCE.getType().isEmpty()) {
                return mappedCE.getId();
            } else {
                return mappedCE.getId()
                        + (enableEncoding ? CommonConstants.INTERNAL_CONCATENATOR : CommonConstants.OLD_CONCATENATOR)
                        + mappedCE.getType();
            } // if else
        } else {
            if (originalCE.getType() == null || originalCE.getType().isEmpty()) {
                return originalCE.getId(); // should never occur since Orion does not allow it
            } else {
                return originalCE.getId()
                        + (enableEncoding ? CommonConstants.INTERNAL_CONCATENATOR : CommonConstants.OLD_CONCATENATOR)
                        + originalCE.getType();
            } // if else
        } // if else
    } // getEntityForNaming

    /**
     * Gets the entity type for naming.
     * @param enableGrouping
     * @param enableMappings
     * @return The entity for naming
     */
    public String getEntityTypeForNaming(boolean enableGrouping, boolean enableMappings) {
        if (enableGrouping) {
            return headers.get(NGSIConstants.FLUME_HEADER_GROUPED_ENTITY_TYPE);
        } else if (enableMappings) {
            if (mappedCE.getType() == null || mappedCE.getType().isEmpty()) {
             // should never occur since Orion does not allow it
                LOGGER.error("[NGSIEvent] Entity Type musn´t be empty or null while grouping by entity type. ("
                        + originalCE.getId() + ")");
                return "";
            } else {
                return mappedCE.getType();
            } // if else
        } else {
            if (originalCE.getType() == null || originalCE.getType().isEmpty()) {
             // should never occur since Orion does not allow it
                LOGGER.error("[NGSIEvent] Entity Type musn´t be empty or null while grouping by entity type. ("
                        + originalCE.getId() + ")");
                return "";
            } else {
                return  originalCE.getType();
            } // if else
        } // if else
    } // getEntityTypeForNaming
    /**
     * Gets the attribute for naming.
     * @param enableMappings
     * @return The attribute for naming
     */
    public String getAttributeForNaming(boolean enableMappings) {
        if (enableMappings) {
            ArrayList<ContextAttribute> attrs = mappedCE.getAttributes();
            if (attrs != null && attrs.get(0) != null) {
                return attrs.get(0).getName(); // the CE has been filtered for having just one attribute
            } else {
                return "";
            }
        } else {
            ArrayList<ContextAttribute> attrs = originalCE.getAttributes();
            if (attrs  != null && attrs.get(0)  != null) {
                return attrs.get(0).getName(); // the CE has been filtered for having just one attribute
            } else {
                return "";
            }
        } // if else
    } // getAttributeForNaming
    
    public ContextElement getContextElement() {
        return originalCE;
    } // getContextElement
    
} // NGSIEvent
