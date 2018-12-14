/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
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

import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import static com.telefonica.iot.cygnus.sinks.NGSIMongoBaseSink.LOGGER;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.flume.Context;

/**
 *
 * @author frb
 */
public class NGSISTHSink extends NGSIMongoBaseSink {
    
    protected final boolean[] resolutions = {false, false, false, false, false};

    /**
     * Constructor.
     */
    public NGSISTHSink() {
        super();
    } // NGSISTHSink
    
    @Override
    public void configure(Context context) {
        String resolutionsStr = context.getString("resolutions", "month,day,hour,minute,second");
        String[] resolutionsArray = resolutionsStr.split(",");
        
        for (String resolution : resolutionsArray) {
            switch (resolution.trim()) {
                case "month":
                    resolutions[4] = true;
                    break;
                case "day":
                    resolutions[3] = true;
                    break;
                case "hour":
                    resolutions[2] = true;
                    break;
                case "minute":
                    resolutions[1] = true;
                    break;
                case "second":
                    resolutions[0] = true;
                    break;
                default:
                    LOGGER.warn("[" + this.getName() + "] Unknown resolution " + resolution);
                    break;
            } // switch
        } // for
        
        LOGGER.debug("[" + this.getName() + "] Reading configuration (resolutions=" + resolutionsStr + ")");
        super.configure(context);
    } // configure
    
    @Override
    public void persistBatch(NGSIBatch batch) throws CygnusBadConfiguration, CygnusPersistenceError {
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

            // Get the events within the current sub-batch
            ArrayList<NGSIEvent> events = batch.getNextEvents();
            
            // Get an aggregator for this destination and initialize it
            STHAggregator aggregator = new STHAggregator();
            aggregator.initialize(events.get(0));

            // Iterate on the events within the sub-batch and aggregate them
            for (NGSIEvent event : events) {
                aggregator.aggregate(event);
            } // for
            
            // Persist the aggregation
            aggregator.persist(this.getName());
            
            // Set the sub-batch as persisted
            batch.setNextPersisted(true);
        } // for
    } // persistBatch
    
    @Override
    public void capRecords(NGSIBatch batch, long maxRecords) throws CygnusCappingError {
    } // capRecords

    @Override
    public void expirateRecords(long expirationTime) throws CygnusExpiratingError {
    } // expirateRecords
    
    private class STHNumericAggregation {
        
        private final String attrName;
        private final String attrType;
        private double max;
        private double min;
        private double sum;
        private double sum2;
        private int numSamples;
        
        public STHNumericAggregation(String attrName, String attrType) {
            this.attrName = attrName;
            this.attrType = attrType;
            max = Double.MIN_VALUE;
            min = Double.MAX_VALUE;
            sum = 0;
            sum2 = 0;
            numSamples = 0;
        } // STHNumericAggregation
        
        public void update(double v) {
            if (v > max) {
                max = v;
            } // if
                    
            if (v < min) {
                min = v;
            } // if

            sum += v;
            sum2 += (v * v);
            numSamples++;
        } // update
        
        public String getAttrName() {
            return attrName;
        } // getAttrName
        
        public String getAttrType() {
            return attrType;
        } // getAttrType
        
        public double getMax() {
            return max;
        } // getMax
        
        public double getMin() {
            return min;
        } // getMin
        
        public double getSum() {
            return sum;
        } // getSum
        
        public double getSum2() {
            return sum2;
        } // getSum2
        
        public int getNumSamples() {
            return numSamples;
        } // getNumSamples
        
    } // STHNumericAggregation
    
    private class STHStringAggregation {
        
        private final String attrName;
        private final String attrType;
        private HashMap<String, Integer> counts;
        private int numSamples;
        
        public STHStringAggregation(String attrName, String attrType) {
            this.attrName = attrName;
            this.attrType = attrType;
            counts = new HashMap<>();
            numSamples = 0;
        } // STHStringAggregation
        
        public void update(String v) {
            Integer count = counts.get(v);
            
            if (count == null) {
                count = 1;
            } else {
                count++;
            } // if else
            
            counts.put(v, count);
            numSamples++;
        } // update
        
        public String getAttrName() {
            return attrName;
        } // getAttrName
        
        public String getAttrType() {
            return attrType;
        } // getAttrType
        
        public HashMap<String, Integer> getCounts() {
            return counts;
        } // getCounts
        
        public int getNumSamples() {
            return numSamples;
        } // getNumSamples
        
    } // STHStringAggregation
    
    private class STHAggregator {

        // Aggregations
        private long lastRecvTimeTs;
        private final HashMap<String, STHNumericAggregation> numericAggrs;
        private final HashMap<String, STHStringAggregation> stringAggrs;
        
        // MongoDB names 
        private String dbName;
        private String collectionName;
        
        // Entity info
        private String entityId;
        private String entityType;
        
        public STHAggregator() {
            lastRecvTimeTs = 0;
            numericAggrs = new HashMap<>();
            stringAggrs = new HashMap<>();
        } // STHAggregator
        
        public HashMap<String, STHNumericAggregation> getNumericAggr() {
            return numericAggrs;
        } // getNumericAggr
        
        public HashMap<String, STHStringAggregation> getStringAggr() {
            return stringAggrs;
        } // getStringAggr
        
        public String getDbName(boolean enableLowercase) {
            if (enableLowercase) {
                return dbName.toLowerCase();
            } else {
                return dbName;
            } // if else
        } // getDbName
        
        public String getCollectionName(boolean enableLowercase) {
            if (enableLowercase) {
                return collectionName.toLowerCase();
            } else {
                return collectionName;
            } // if else
        } // getCollectionName
        
        public String getEntityId() {
            return entityId;
        } // getEntityId
        
        public String getEntityType() {
            return entityType;
        } // getEntityType
        
        public void initialize(NGSIEvent event) throws CygnusBadConfiguration {
            String service = event.getServiceForNaming(enableNameMappings);
            String servicePathForData = event.getServicePathForData();
            String servicePathForNaming = event.getServicePathForNaming(enableGrouping, enableNameMappings);
            String entityForNaming = event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding);
            String attributeForNaming = event.getAttributeForNaming(enableNameMappings);
            dbName = buildDbName(service);
            collectionName = buildCollectionName(servicePathForNaming, entityForNaming, attributeForNaming) + ".aggr";
        } // initialize
        
        public void aggregate(NGSIEvent event) {
            // Get notified reception time
            long notifiedRecvTimeTs = event.getRecvTimeTs();
            
            // Get the event body
            ContextElement contextElement = event.getContextElement();
            
            // Get entity info
            entityId = contextElement.getId();
            entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");
            
            // Iterate on all this context element attributes, if there are attributes
            ArrayList<NotifyContextRequest.ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if
            
            for (NotifyContextRequest.ContextAttribute contextAttribute : contextAttributes) {
                // Get attribute info
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                
                // Check if the attribute value is based on white spaces
                if (ignoreWhiteSpaces && attrValue.trim().length() == 0) {
                    continue;
                } // if
                
                // Check if the metadata contains a TimeInstant value; use the notified reception time instead
                Long timeInstant = CommonUtils.getTimeInstant(attrMetadata);

                if (timeInstant != null) {
                    this.lastRecvTimeTs = timeInstant;
                } else {
                    this.lastRecvTimeTs = notifiedRecvTimeTs;
                } // if else
                
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                
                // Get current aggregation for this attribute, or cerate them if not existing
                if (CommonUtils.isANumber(attrValue)) {
                    STHNumericAggregation aggr = numericAggrs.get(attrName + "xffff" + attrType);
                    
                    if (aggr == null) {
                        aggr = new STHNumericAggregation(attrName, attrType);
                        numericAggrs.put(attrName + "xffff" + attrType, aggr);
                    } // if
                    
                    aggr.update(new Double(attrValue));
                } else {
                    STHStringAggregation aggr = stringAggrs.get(attrName + "xffff" + attrType);
                    
                    if (aggr == null) {
                        aggr = new STHStringAggregation(attrName, attrType);
                        stringAggrs.put(attrName + "xffff" + attrType, aggr);
                    } // if
                    
                    aggr.update(attrValue);
                } // if else
            } // for
        } // aggregate
        
        public void persist(String sinkName) throws CygnusBadConfiguration, CygnusPersistenceError {
            try {
                backend.createDatabase(dbName);
            } catch (Exception e) {
                throw new CygnusPersistenceError("-, " + e.getMessage());
            } // try catch

            try {
                backend.createCollection(dbName, collectionName, dataExpiration);
            } catch (Exception e) {
                throw new CygnusPersistenceError("-, " + e.getMessage());
            } // try catch
                
            for (String key : numericAggrs.keySet()) {
                STHNumericAggregation numericAggr = numericAggrs.get(key);
                
                LOGGER.info("[" + sinkName + "] Persisting data at NGSISTHSink. Database: " + dbName
                        + ", Collection: " + collectionName + ", Data: " + lastRecvTimeTs + ","
                        + entityId + "," + entityType + "," + numericAggr.getAttrName() + ","
                        + numericAggr.getAttrType() + ",[" + numericAggr.getMax() + ","
                        + numericAggr.getMin() + "," + numericAggr.getSum() + "," + numericAggr.getSum2()
                        + "," + numericAggr.getNumSamples() + "]");

                try {
                    backend.insertContextDataAggregated(dbName, collectionName, lastRecvTimeTs,
                            entityId, entityType, numericAggr.getAttrName(), numericAggr.getAttrType(),
                            numericAggr.getMax(), numericAggr.getMin(), numericAggr.getSum(),
                            numericAggr.getSum2(), numericAggr.getNumSamples(), resolutions);
                } catch (Exception e) {
                    throw new CygnusPersistenceError("-, " + e.getMessage());
                } // try catch
            } // for

            for (String key : stringAggrs.keySet()) {
                STHStringAggregation stringAggr = stringAggrs.get(key);
                
                LOGGER.info("[" + sinkName + "] Persisting data at NGSISTHSink. Database: " + dbName
                        + ", Collection: " + collectionName + ", Data: " + lastRecvTimeTs + ","
                        + entityId + "," + entityType + "," + stringAggr.getAttrName() + ","
                        + stringAggr.getAttrType() + ",[" + stringAggr.getCounts().toString()
                        + "," + stringAggr.getNumSamples() + "]");

                try {
                    backend.insertContextDataAggregated(dbName, collectionName, lastRecvTimeTs,
                            entityId, entityType, stringAggr.getAttrName(), stringAggr.getAttrType(),
                            stringAggr.getCounts(), resolutions);
                } catch (Exception e) {
                    throw new CygnusPersistenceError("-, " + e.getMessage());
                } // try catch
            } // for
        } // persist
        
    } // STHAggregator

} // NGSISTHSink
