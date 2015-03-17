/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. fiware-connectors is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with fiware-connectors. If not, see http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please
 * contact with iot_support at tid dot es
 */
package es.tid.fiware.fiwareconnectors.cygnus.sinks;

import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextAttribute;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElement;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import es.tid.fiware.fiwareconnectors.cygnus.log.CygnusLogger;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.apache.flume.Context;
import org.slf4j.LoggerFactory;

/**
 * Sink for testing purposes. It does not persist the notified context data but
 * prints logs about it. This can configured by the users in order to test the
 * connectivity with Orion Context Broker.
 *
 * @author frb
 */
public class OrionTestSink extends OrionSink {

    private final CygnusLogger cygnusLogger;

    /**
     * Constructor.
     */
    public OrionTestSink() {
        super();
        cygnusLogger = new CygnusLogger(LoggerFactory.getLogger(OrionTestSink.class), true);
    } // OrionTestSink

    @Override
    public void configure(Context context) {
        // nothing to configure... this is a testing sink and should be simple!
    } // configure

    @Override
    public void start() {
        super.start();
        cygnusLogger.info("[" + this.getName() + "] Startup completed");
    } // start

    @Override
    void persist(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        // get some header values
        Long recvTimeTs = new Long(eventHeaders.get("timestamp"));
        String fiwareService = eventHeaders.get(Constants.HEADER_SERVICE);
        String fiwareServicePath = eventHeaders.get(Constants.HEADER_SERVICE_PATH);
        String[] destinations = eventHeaders.get(Constants.DESTINATION).split(",");

        // human readable version of the reception time
        String recvTime = new Timestamp(recvTimeTs).toString().replaceAll(" ", "T");
        
        // lob about the event headers with deliberated INFO level
        cygnusLogger.info("[" + this.getName() + "] Processing headers (recvTimeTs=" + recvTimeTs + " (" + recvTime
                + "), fiwareService=" + fiwareService + ", fiwareServicePath=" + fiwareServicePath
                + ", destinations=" + Arrays.toString(destinations) + ")");
        
        // iterate on the contextResponses
        ArrayList<ContextElementResponse> contextResponses = notification.getContextResponses();

        for (ContextElementResponse contextElementResponse: contextResponses) {
            ContextElement contextElement = contextElementResponse.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            
            // log about the context element with deliberated INFO level
            cygnusLogger.info("[" + this.getName() + "] Processing context element (id=" + entityId + ", type= "
                    + entityType + ")");

            // iterate on all this entity's attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                cygnusLogger.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                continue;
            } // if

            for (ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                
                // log about the context attribute with deliberated INFO level
                cygnusLogger.info("[" + this.getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ", value=" + attrValue + ", metadata=" + attrMetadata + ")");
            } // for
        } // for
    } // persist

} // OrionTestSink
