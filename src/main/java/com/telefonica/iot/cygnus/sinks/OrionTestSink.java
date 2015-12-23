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
import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import org.apache.flume.Context;

/**
 * Sink for testing purposes. It does not persistOne the notified context data but
 prints logs about it. This can configured by the users in order to test the
 connectivity with Orion Context Broker.
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
        super.configure(context);
    } // configure

    @Override
    public void start() {
        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

    // TBD: to be removed once all the sinks have been migrated to persistBatch method
    @Override
    void persistOne(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception {
        Accumulator accumulator = new Accumulator();
        accumulator.initializeBatching(new Date().getTime());
        accumulator.accumulate(eventHeaders, notification);
        persistBatch(accumulator.getBatch());
    } // persistOne
    
    @Override
    void persistBatch(Batch batch) throws Exception {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if
 
        // iterate on the destinations, for each one a single create / append will be performed
        for (String destination : batch.getDestinations()) {
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the " + destination
                    + " destination");

            // get the sub-batch for this destination
            ArrayList<CygnusEvent> subBatch = batch.getEvents(destination);
            
            // get an aggregator for this destination and initialize it
            TestAggregator aggregator = new TestAggregator();
            aggregator.initialize(subBatch.get(0));

            for (CygnusEvent cygnusEvent : subBatch) {
                aggregator.aggregate(cygnusEvent);
            } // for
            
            // persist the aggregation
            persistAggregation(aggregator);
            batch.setPersisted(destination);
        } // for
    } // persistBatch
    
    /**
     * Class for aggregating aggregation.
     */
    private class TestAggregator {
        
        // string containing the data aggregation
        private String aggregation;
        private String service;
        private String servicePath;
        private String destination;
        
        public TestAggregator() {
            aggregation = "";
        } // TestAggregator
        
        public String getAggregation() {
            return aggregation;
        } // getAggregation
        
        public void initialize(CygnusEvent cygnusEvent) throws Exception {
            service = cygnusEvent.getService();
            servicePath = cygnusEvent.getServicePath();
            destination = cygnusEvent.getDestination();
        } // initialize
        
        public void aggregate(CygnusEvent cygnusEvent) throws Exception {
            String line = "Processing event={";
            
            // get the event headers
            long recvTimeTs = cygnusEvent.getRecvTimeTs();
            
            line += "Processing headers={recvTimeTs=" + recvTimeTs
                    + ", fiwareService=" + service
                    + ", fiwareServicePath=" + servicePath
                    + ", destinations=" + destination + "}";
            
            // get the event body
            ContextElement contextElement = cygnusEvent.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            line += ", Processing context element={id=" + entityId
                    + ", type=" + entityType + "}";
            
            // iterate on all this context element attributes, if there are attributes
            ArrayList<ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                line += ", Processing attribute={no attributes within the notified entity}";
            } else {
                for (ContextAttribute contextAttribute : contextAttributes) {
                    String attrName = contextAttribute.getName();
                    String attrType = contextAttribute.getType();
                    String attrValue = contextAttribute.getContextValue(true);
                    String attrMetadata = contextAttribute.getContextMetadata();
                    line += ", Processing attribute={name=" + attrName
                            + ", type=" + attrType
                            + ", value=" + attrValue
                            + ", metadata=" + attrMetadata + "}";
                } // for
            } // if else
            
            line += "}";
                
            if (aggregation.isEmpty()) {
                aggregation = line;
            } else {
                aggregation += "," + line;
            } // if else
        } // aggregate
        
    } // TestAggregator
    
    private void persistAggregation(TestAggregator aggregator) throws Exception {
        String aggregation = aggregator.getAggregation();
        
        LOGGER.info("[" + this.getName() + "] Persisting data at OrionTestSink. Data (" + aggregation + ")");
    } // persistAggregation

} // OrionTestSink
