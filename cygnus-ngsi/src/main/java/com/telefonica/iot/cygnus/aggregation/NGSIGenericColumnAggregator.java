/**
 * Copyright 2014-2020 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.aggregation;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * The type Ngsi generic column aggregator.
 */
public class NGSIGenericColumnAggregator extends NGSIGenericAggregator {

    // Logger
    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIGenericAggregator.class);

    private boolean swapCoordinates;

    @Override
    public void initialize(NGSIEvent event) {
        // TBD: possible option for postgisSink
        swapCoordinates = false;
        // particular initialization
        LinkedHashMap<String, ArrayList<JsonElement>> aggregation = getAggregation();
        aggregation.put(NGSIConstants.RECV_TIME_TS+"C", new ArrayList<JsonElement>());
        aggregation.put(NGSIConstants.RECV_TIME, new ArrayList<JsonElement>());
        aggregation.put(NGSIConstants.FIWARE_SERVICE_PATH, new ArrayList<JsonElement>());
        aggregation.put(NGSIConstants.ENTITY_ID, new ArrayList<JsonElement>());
        aggregation.put(NGSIConstants.ENTITY_TYPE, new ArrayList<JsonElement>());

        // iterate on all this context element attributes, if there are attributes
        ArrayList<NotifyContextRequest.ContextAttribute> contextAttributes = null;
        if (isEnableNameMappings() && event.getMappedCE() != null && event.getMappedCE().getAttributes() != null && !event.getMappedCE().getAttributes().isEmpty()) {
            contextAttributes = event.getMappedCE().getAttributes();
        } else if (event.getContextElement() != null && event.getContextElement().getAttributes() != null && !event.getContextElement().getAttributes().isEmpty()) {
            contextAttributes = event.getContextElement().getAttributes();
        } else {
            LOGGER.warn("No attributes within the notified entity, nothing is done");
            return;
        } // if

        for (NotifyContextRequest.ContextAttribute contextAttribute : contextAttributes) {
            String attrName = contextAttribute.getName();
            aggregation.put(attrName, new ArrayList<JsonElement>());
            aggregation.put(attrName + "_md", new ArrayList<JsonElement>());
            aggregation.put(attrName + "_type", new ArrayList<JsonElement>());
        } // for
        setAggregation(aggregation);
    } // initialize

    @Override
    public void aggregate(NGSIEvent event) {
        // Number of previous values
        int numPreviousValues = getAggregation().get(NGSIConstants.FIWARE_SERVICE_PATH).size();
        // Get the event headers
        long recvTimeTs = event.getRecvTimeTs();
        String recvTime = CommonUtils.getHumanReadable(recvTimeTs, isEnableUTCRecvTime());
        // get the event body
        NotifyContextRequest.ContextElement contextElement = event.getContextElement();
        NotifyContextRequest.ContextElement mappedContextElement = event.getMappedCE();
        String entityId = contextElement.getId();
        String entityType = contextElement.getType();
        LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type=" + entityType + ")");
        // Iterate on all this context element attributes, if there are attributes
        ArrayList<NotifyContextRequest.ContextAttribute> contextAttributes = null;
        if (isEnableNameMappings() && mappedContextElement != null && mappedContextElement.getAttributes() != null && !mappedContextElement.getAttributes().isEmpty()) {
            contextAttributes = mappedContextElement.getAttributes();
        } else if (contextElement!= null && contextElement.getAttributes() != null && !contextElement.getAttributes().isEmpty()) {
            contextAttributes = event.getContextElement().getAttributes();
        } else {
            LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                    + ", type=" + entityType + ")");
            return;
        } // if
        LinkedHashMap<String, ArrayList<JsonElement>> aggregation = getAggregation();
        aggregation.get(NGSIConstants.RECV_TIME_TS+"C").add(new JsonPrimitive(Long.toString(recvTimeTs)));
        aggregation.get(NGSIConstants.RECV_TIME).add(new JsonPrimitive(recvTime));
        aggregation.get(NGSIConstants.FIWARE_SERVICE_PATH).add(new JsonPrimitive(getServicePathForData()));
        aggregation.get(NGSIConstants.ENTITY_ID).add(new JsonPrimitive(entityId));
        aggregation.get(NGSIConstants.ENTITY_TYPE).add(new JsonPrimitive(entityType));
        for (NotifyContextRequest.ContextAttribute contextAttribute : contextAttributes) {
            String attrName = contextAttribute.getName();
            String attrType = contextAttribute.getType();
            JsonElement attrValue = contextAttribute.getValue();
            String attrMetadata = contextAttribute.getContextMetadata();
            LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type=" + attrType + ")");
            if (isEnableGeoParse() && (attrType.equals("geo:json") || attrType.equals("geo:point"))) {
                try {
                    //Process geometry if applyes
                    ImmutablePair<String, Boolean> location = NGSIUtils.getGeometry(attrValue.toString(), attrType, attrMetadata, swapCoordinates);
                    if (location.right) {
                        LOGGER.debug("location=" + location.getLeft());
                        attrValue = new JsonPrimitive(location.getLeft());
                    }
                } catch (Exception e) {
                    LOGGER.error("[" + getName() + "] Processing context attribute (name=" + attrValue.toString());
                }
            }
            // Check if the attribute already exists in the form of 2 columns (one for metadata); if not existing,
            // add an empty value for all previous rows
            if (aggregation.containsKey(attrName)) {
                aggregation.get(attrName).add(attrValue);
                aggregation.get(attrName + "_md").add(new JsonPrimitive(attrMetadata));
                aggregation.get(attrName + "_type").add(new JsonPrimitive(attrType));
            } else {
                ArrayList<JsonElement> values = new ArrayList<JsonElement>(Collections.nCopies(numPreviousValues, null));
                values.add(attrValue);
                aggregation.put(attrName, values);
                ArrayList<JsonElement> valuesMd = new ArrayList<JsonElement>(Collections.nCopies(numPreviousValues, null));
                valuesMd.add(new JsonPrimitive(attrMetadata));
                aggregation.put(attrName + "_md", valuesMd);
                ArrayList<JsonElement> valuesType = new ArrayList<JsonElement>(Collections.nCopies(numPreviousValues, null));
                valuesType.add(new JsonPrimitive(attrType));
                aggregation.put(attrName + "_type", valuesType);
            } // if else
        } // for
        // Iterate on all the aggregations, checking for not updated attributes; add an empty value if missing
        for (String key : aggregation.keySet()) {
            ArrayList<JsonElement> values = aggregation.get(key);
            if (values.size() == numPreviousValues) {
                values.add(null);
            } // if
        } // for
        setAggregation(aggregation);
    }

    private String getName() {
        return "NGSIUtils.GenericColumnAggregator";
    }


}
