/**
 * Copyright 2020 Telefonica Investigación y Desarrollo, S.A.U
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


import com.telefonica.iot.cygnus.containers.NotifyContextRequestLD;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import org.apache.flume.Event;
import java.util.Map;

/**
* @author anmunoz
*
* */

public class NGSILDEvent implements Event {
    private Map<String, String> headers;
    private byte[] body;
    private NotifyContextRequestLD.ContextElement originalCELD;
    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIEvent.class);

    /**
     * Constructor.
     * @param headers
     * @param body
     * @param originalCELD
     */
    public NGSILDEvent(Map<String, String> headers, byte[] body, NotifyContextRequestLD.ContextElement originalCELD){
        this.headers = headers;
        this.body = body;
        this.originalCELD = originalCELD;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public NotifyContextRequestLD.ContextElement getOriginalCELD() {
        return originalCELD;
    }

    public void setOriginalCELD(NotifyContextRequestLD.ContextElement originalCELD) {
        this.originalCELD = originalCELD;
    }
    /*
     * From here on, methods used by custom sinks. They are smart wrappers of the above methods.
     */

    public long getRecvTimeTs() {
        return Long.valueOf(headers.get(NGSIConstants.FLUME_HEADER_TIMESTAMP));
    } // getRecvTimeTs

    public String getServiceForData() {
        return headers.get(CommonConstants.HEADER_FIWARE_SERVICE);
    } // getServiceForData

    /**
     * Gets the service both for data and for naming.
     * @return The service both for data and for naming
     */
    public String getServiceForNaming() {
        return headers.get(CommonConstants.HEADER_FIWARE_SERVICE);
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
     * @return The service path for naming
     */
    public String getServicePathForNaming(boolean enableGrouping) {
        if (enableGrouping) {
            return headers.get(NGSIConstants.FLUME_HEADER_GROUPED_SERVICE_PATH);
        } else {
            return headers.get(CommonConstants.HEADER_FIWARE_SERVICE_PATH);
        } // if else
    } // getMappedServicePath

    /**
     * Gets the entity for naming.
     * @param enableGrouping
     * @param enableEncoding
     * @return The entity for naming
     */
    public String getEntityForNaming(boolean enableGrouping, boolean enableEncoding) {

        if (enableGrouping) {
            return headers.get(NGSIConstants.FLUME_HEADER_GROUPED_ENTITY);
        }
        else {
            if (originalCELD.getType() == null || originalCELD.getType().isEmpty()) {
                return originalCELD.getId(); // should never occur since Orion does not allow it
            } else {
                return originalCELD.getId();
            } // if else
        }

    } // getEntityForNaming

    /**
     * Gets the entity type for naming.
     * @param enableGrouping
     * @return The entity for naming
     */
    public String getEntityTypeForNaming(boolean enableGrouping) {
        if (enableGrouping) {
            return headers.get(NGSIConstants.FLUME_HEADER_GROUPED_ENTITY);
        }else {
            if (originalCELD.getType() == null || originalCELD.getType().isEmpty()) {
                // should never occur since Orion does not allow it
                LOGGER.error("[NGSIEvent] Entity Type has not be empty or null while grouping by entity type. ("
                        + originalCELD.getId() + ")");
                return "";
            } else {
                return originalCELD.getType();
            } // if else
        } // if else
    } // getEntityTypeForNaming
    /**
     * Gets the attribute for naming.
     * @return The attribute for naming
     */
    public String getAttributeForNaming() {
        String key = originalCELD.getAttributes().entrySet().stream().findFirst().get().getKey();
        return key;
    }

    public NotifyContextRequestLD.ContextElement getContextElement() {
        return originalCELD;
    } // getContextElement

} // NGSIEvent
