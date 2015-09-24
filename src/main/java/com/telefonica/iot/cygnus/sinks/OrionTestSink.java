/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. fiware-cygnus is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with fiware-cygnus. If not, see http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please
 * contact with iot_support at tid dot es
 */
package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.apache.flume.Context;

/**
 * Sink for testing purposes. It does not persist the notified context data but
 * prints logs about it. This can configured by the users in order to test the
 * connectivity with Orion Context Broker.
 *
 * @author frb
 */
public class OrionTestSink extends OrionSink {

    private static final CygnusLogger LOGGER = new CygnusLogger(OrionTestSink.class);
    
    /**
     * Constructor.
     */
    public OrionTestSink() {
        super();
    } // OrionTestSink

    @Override
    public void configure(Context context) {
        // nothing to configure... this is a testing sink and should be simple!
    } // configure

    @Override
    public void start() {
        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

    @Override
    void persist(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        // get some header values
        Long recvTimeTs = new Long(eventHeaders.get("timestamp"));
        String fiwareService = eventHeaders.get(Constants.HEADER_NOTIFIED_SERVICE);
        String[] fiwareServicePaths = eventHeaders.get(Constants.HEADER_DEFAULT_SERVICE_PATHS).split(",");
        String[] destinations = eventHeaders.get(Constants.HEADER_DEFAULT_DESTINATIONS).split(",");

        // human readable version of the reception time
        String recvTime = Utils.getHumanReadable(recvTimeTs, true);
        
        // log about the event headers with deliberated INFO level
        LOGGER.info("[" + this.getName() + "] Processing headers (recvTimeTs=" + recvTimeTs + " (" + recvTime
                + "), fiwareService=" + fiwareService + ", fiwareServicePath=" + Arrays.toString(fiwareServicePaths)
                + ", destinations=" + Arrays.toString(destinations) + ")");
        
        // iterate on the contextResponses
        ArrayList<ContextElementResponse> contextResponses = notification.getContextResponses();

        for (ContextElementResponse contextElementResponse: contextResponses) {
            ContextElement contextElement = contextElementResponse.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            
            // log about the context element with deliberated INFO level
            LOGGER.info("[" + this.getName() + "] Processing context element (id=" + entityId + ", type= "
                    + entityType + ")");

            // iterate on all this entity's attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                continue;
            } // if

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                
                // log about the context attribute with deliberated INFO level
                LOGGER.info("[" + this.getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ", value=" + attrValue + ", metadata=" + attrMetadata + ")");
            } // for
        } // for
    } // persist

} // OrionTestSink
