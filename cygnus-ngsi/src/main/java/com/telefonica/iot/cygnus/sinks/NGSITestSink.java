/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
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

import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.Enums.DataModel;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.flume.Context;

/**
 * Sink for testing purposes. It does not persistOne the notified context data but
 prints logs about it. This can configured by the users in order to test the
 connectivity with Orion Context Broker.
 *
 * @author frb
 */
public class NGSITestSink extends NGSISink {

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSITestSink.class);
    
    /**
     * Constructor.
     */
    public NGSITestSink() {
        super();
    } // NGSITestSink

    @Override
    public void configure(Context context) {
        super.configure(context);
        // Techdebt: allow this sink to work with all the data models
        dataModel = DataModel.DMBYENTITY;
    } // configure

    @Override
    public void start() {
        super.start();
    } // start
    
    @Override
    void persistBatch(NGSIBatch batch) {
        if (batch == null) {
            LOGGER.debug("[" + this.getName() + "] Null batch, nothing to do");
            return;
        } // if
 
        // Iterate on the destinations
        batch.startIterator();
        
        while (batch.hasNext()) {
            String destination = batch.getNextDestination();
            LOGGER.debug("[" + this.getName() + "] Processing sub-batch regarding the "
                    + destination + " destination");

            // Get the sub-batch for this destination
            ArrayList<NGSIEvent> events = batch.getNextEvents();
            
            // Get an aggregator for this destination and initialize it
            TestAggregator aggregator = new TestAggregator();
            aggregator.initialize(events.get(0));

            for (NGSIEvent event : events) {
                aggregator.aggregate(event);
            } // for
            
            // Persist the aggregation
            persistAggregation(aggregator);
            batch.setNextPersisted(true);
        } // for
    } // persistBatch
    
    @Override
    public void capRecords(NGSIBatch batch, long maxRecords) throws CygnusCappingError {
    } // capRecords

    @Override
    public void expirateRecords(long expirationTime) throws CygnusExpiratingError {
    } // expirateRecords
    
    /**
     * Class for aggregating aggregation.
     */
    private class TestAggregator {

        // string containing the data aggregation
        private String aggregation;
        
        public TestAggregator() {
            aggregation = "";
        } // TestAggregator
        
        public String getAggregation() {
            return aggregation;
        } // getAggregation
        
        public void initialize(NGSIEvent event) {
        } // initialize
        
        public void aggregate(NGSIEvent event) {
            String line = "Processing event={";
            
            // get the getRecvTimeTs headers
            line += "Processing headers={";
            Map<String, String> headers = event.getHeaders();
            boolean first = true;
            
            for (Entry entry : headers.entrySet()) {
                if (first) {
                    line += entry.getKey() + "=" + entry.getValue();
                    first = false;
                } else {
                    line += "," + entry.getKey() + "=" + entry.getValue();
                } // if else
            } // for

            line += "}";
            
            // get the getRecvTimeTs body
            ContextElement contextElement = event.getContextElement();
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
    
    private void persistAggregation(TestAggregator aggregator) {
        String aggregation = aggregator.getAggregation();
        
        LOGGER.info("[" + this.getName() + "] Persisting data at NGSITestSink. Data (" + aggregation + ")");
    } // persistAggregation

} // NGSITestSink
