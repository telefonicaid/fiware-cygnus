package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.backends.slicingdice.SlicingDiceBackend;
import com.telefonica.iot.cygnus.backends.slicingdice.SlicingDiceBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.utils.NGSICharsets;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.flume.Context;

/**
 *
 * @author joaosimbiose
 *
 * SlicingDice (https://www.slicingdice.com) sink for Orion Context Broker.
 *
 */
public class NGSISlicingDiceSink extends NGSISink {

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSISlicingDiceSink.class);

    private String databaseKey;
    private boolean autoCreate;
    private SlicingDiceBackend persistenceBackend;

    public String getDatabaseKey() {
        return databaseKey;
    } // getDatabaseKey

    public boolean isAutoCreate() {
        return autoCreate;
    } // isAutoCreate

    @Override
    public void configure(final Context context) {
        super.configure(context);
        this.databaseKey = context.getString("database_key");

        if (this.databaseKey == null) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (database_key=" + databaseKey + ")"
                    + " -- Must be different than null");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (api_key=" + databaseKey + ")");
        }
        this.autoCreate = context.getBoolean("auto_create", true);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (auto_create=" + autoCreate + ")");
    } // configure

    @Override
    void persistBatch(final NGSIBatch batch) throws CygnusBadConfiguration, CygnusBadContextData, CygnusRuntimeError, CygnusPersistenceError {
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

            SlicingDiceAggregator aggregator = getAggregator();
            aggregator.initialize(events.get(0));

            for (NGSIEvent event : events) {
                aggregator.aggregate(event);
            } // for

            // Persist the aggregation
            persistAggregation(aggregator);
            batch.setNextPersisted(true);
        } // while
    } // persistBatch

    @Override
    void capRecords(final NGSIBatch batch, final long maxRecords) throws CygnusCappingError {
    }

    @Override
    void expirateRecords(final long expirationTime) throws CygnusExpiratingError {
    }

    @Override
    public void start() {
        try {
            this.persistenceBackend = new SlicingDiceBackendImpl(databaseKey);
            LOGGER.debug("[" + this.getName() + "] SlicingDice persistence backend created");
        } catch (Exception e) {
            LOGGER.error("Error while creating the SlicingDice persistence backend. Details="
                    + e.getMessage());
        } // try catch

        super.start();
    } // start

    /**
     * Class for aggregating.
     */
    private abstract class SlicingDiceAggregator {

        // command to create string and integer columns on SlicingDice
        private static final String COLUMN_CREATION_COMMAND =
                "{\"name\": \"%s\", \"api-name\": \"%s\", \"type\": \"%s\", " +
                        "\"description\": \"Created using CYGNUS.\", \"dimension\": \"%s\"}";

        // command to create decimal columns on SlicingDice
        private static final String DECIMAL_COLUMN_CREATION_COMMAND =
                "{\"name\": \"%s\", \"api-name\": \"%s\", \"type\": \"%s\", " +
                        "\"description\": \"Created using CYGNUS.\", \"decimal-places\": 5, " +
                        "\"dimension\": \"%s\"}";

        // object containing the aggregted data
        protected LinkedHashMap<String, ArrayList<String>> aggregation;
        protected LinkedHashMap<String, String> fieldToType;
        protected ArrayList<String> timeSeries;

        protected String service;
        protected String servicePathForData;
        protected String servicePathForNaming;
        protected String entityForNaming;
        protected String attribute;
        protected String dimensionName;

        public SlicingDiceAggregator() {
            aggregation = new LinkedHashMap<>();
            fieldToType = new LinkedHashMap<>();
            timeSeries = new ArrayList<>();
        } // SlicingDiceAggregator

        public String getTableName(boolean enableLowercase) {
            if (enableLowercase) {
                return dimensionName.toLowerCase();
            } else {
                return dimensionName;
            } // if else
        } // getTableName

        public String getValuesForInsert() {
            final StringBuilder valuesForInsert = new StringBuilder("{");
            final int numEvents = aggregation.get(NGSIConstants.FIWARE_SERVICE_PATH).size();

            for (int i = 0; i < numEvents; i++) {
                // get entity id
                final String entityId = aggregation.get(NGSIConstants.ENTITY_ID).get(i);

                if (i == 0) {
                    valuesForInsert.append("\"").append(entityId).append("\": {");
                } else {
                    valuesForInsert.append(",").append(" \"").append(entityId).append("\": {");
                } // if else

                // add dimension to the entity
                valuesForInsert.append("\"dimension\": \"").append(dimensionName).append("\"");

                for (final Map.Entry<String, ArrayList<String>> aggregationEntrySet : aggregation.entrySet()) {
                    final String columnName = aggregationEntrySet.getKey();

                    // we already used saved the entity id below so we can safely ignore it now
                    if (columnName.equals(NGSIConstants.ENTITY_ID)) {
                        continue;
                    } // if

                    final String value = aggregationEntrySet.getValue().get(i);
                    final boolean isString;

                    if (columnName.equals(NGSIConstants.FIWARE_SERVICE_PATH)) {
                        isString = true;
                    } else {
                        isString = fieldToType.get(columnName).equals("string-event");
                    }
                    final String date = timeSeries.get(i);
                    if (isString) {
                        valuesForInsert.append(", \"").append(columnName).append("\": [")
                                .append("{\"value\": \"").append(value).append("\", \"date\": \"")
                                .append(date).append("\"}]");
                    } else {
                        valuesForInsert.append(", \"").append(columnName).append("\": [")
                                .append("{\"value\": ").append(value).append(", \"date\": \"")
                                .append(date).append("\"}]");
                    } // if else
                } // for
                valuesForInsert.append("}");
            } // for

            if (autoCreate) {
                valuesForInsert.append("\"auto-create\": [\"dimension\", \"column\"]");
            } else {
                valuesForInsert.append("\"auto-create\": [\"dimension\"]");
            } // if else

            return valuesForInsert + "}";
        } // getValuesForInsert

        public String getFieldsForCreate() {
            boolean first = true;
            final StringBuilder fieldsForCreate = new StringBuilder("[");

            for (final Map.Entry<String, String> fieldToTypeEntry : fieldToType.entrySet()) {
                final String columnName = fieldToTypeEntry.getKey();
                final String type = fieldToTypeEntry.getValue();

                final String columnCommand;
                if (type.contains("decimal")) {
                    columnCommand = String.format(DECIMAL_COLUMN_CREATION_COMMAND, columnName,
                            columnName, type, dimensionName);
                } else {
                    columnCommand = String.format(COLUMN_CREATION_COMMAND, columnName, columnName,
                            type, dimensionName);
                }

                if (first) {
                    fieldsForCreate.append(columnCommand);
                    first = false;
                } else {
                    fieldsForCreate.append(",").append(columnCommand);
                } // if else
            } // for

            return fieldsForCreate + "]";
        } // getFieldsForCreate

        public void initialize(NGSIEvent event) throws CygnusBadConfiguration {
            service = event.getServiceForNaming(enableNameMappings);
            servicePathForData = event.getServicePathForData();
            servicePathForNaming = event.getServicePathForNaming(enableGrouping, enableNameMappings);
            entityForNaming = event.getEntityForNaming(enableGrouping, enableNameMappings, enableEncoding);
            attribute = event.getAttributeForNaming(enableNameMappings);
            dimensionName = buildDimensionName(servicePathForNaming, entityForNaming, attribute);
        } // initialize

        public abstract void aggregate(NGSIEvent cygnusEvent);

    }

     /**
     * Class for aggregating batches in column mode.
     */
    private class ColumnAggregator extends NGSISlicingDiceSink.SlicingDiceAggregator {

        @Override
        public void initialize(NGSIEvent cygnusEvent) throws CygnusBadConfiguration {
            super.initialize(cygnusEvent);

            // particular initialization
            aggregation.put(NGSIConstants.FIWARE_SERVICE_PATH, new ArrayList<String>());
            aggregation.put(NGSIConstants.ENTITY_ID, new ArrayList<String>());

            // iterate on all this context element attributes, if there are attributes
            ArrayList<NotifyContextRequest.ContextAttribute> contextAttributes = cygnusEvent.getContextElement().getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                return;
            } // if

            for (NotifyContextRequest.ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                aggregation.put(attrName, new ArrayList<String>());
            } // for
        } // initialize

        @Override
        public void aggregate(NGSIEvent event) {
            // Number of previous values
            int numPreviousValues = aggregation.get(NGSIConstants.FIWARE_SERVICE_PATH).size();

            // Get the event headers
            long recvTimeTs = event.getRecvTimeTs();
            String recvTime = CommonUtils.getHumanReadable(recvTimeTs, false);

            // get the event body
            NotifyContextRequest.ContextElement contextElement = event.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + getName() + "] Processing context element (id=" + entityId + ", type="
                    + entityType + ")");

            // Iterate on all this context element attributes, if there are attributes
            ArrayList<NotifyContextRequest.ContextAttribute> contextAttributes = contextElement.getAttributes();

            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                return;
            } // if

            timeSeries.add(recvTime);

            aggregation.get(NGSIConstants.FIWARE_SERVICE_PATH).add(servicePathForData);
            fieldToType.put(NGSIConstants.FIWARE_SERVICE_PATH, "string-event");

            aggregation.get(NGSIConstants.ENTITY_ID).add(entityId);

            for (NotifyContextRequest.ContextAttribute contextAttribute : contextAttributes) {
                String attrName = encode(contextAttribute.getName(), false, true);
                String attrType = contextAttribute.getType();
                String slicingDiceType = translateType(attrType);
                String attrValue = contextAttribute.getContextValue(false);
                LOGGER.debug("[" + getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");

                fieldToType.put(attrName, slicingDiceType);

                // Check if the attribute already exists in the form of 2 columns (one for metadata); if not existing,
                // add an empty value for all previous rows
                if (aggregation.containsKey(attrName)) {
                    aggregation.get(attrName).add(attrValue);
                } else {
                    ArrayList values = new ArrayList<>(Collections.nCopies(numPreviousValues, ""));
                    values.add(attrValue);
                    aggregation.put(attrName, values);
                } // if else
            } // for

            // Iterate on all the aggregations, checking for not updated attributes; add an empty value if missing
            for (String key : aggregation.keySet()) {
                ArrayList values = aggregation.get(key);

                if (values.size() == numPreviousValues) {
                    values.add("");
                } // if
            } // for
        } // aggregate

         /**
          * Translate the attribute type to the SlicingDice format so we can create the column on
          * SlicingDice.
          * @param type - the type to be translated
          * @return the translated type to the SlicingDice format
          */
         private String translateType(final String type) {
             final String lowerCaseType = type.toLowerCase();
             switch (lowerCaseType) {
                 case "float":
                 case "number":
                     return "decimal-event";
                 case "integer":
                     return "integer-event";
                 default:
                     return "string-event";
             }
         } // translateType

    } // ColumnAggregator

    private SlicingDiceAggregator getAggregator() {
        return new NGSISlicingDiceSink.ColumnAggregator();
    } // getAggregator

    private void persistAggregation(NGSISlicingDiceSink.SlicingDiceAggregator aggregator)
            throws CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
        String fieldsForCreate = aggregator.getFieldsForCreate();
        String valuesForInsert = aggregator.getValuesForInsert();
        String tableName = aggregator.getTableName(enableLowercase);

        LOGGER.info("[" + this.getName() + "] Persisting data at NGSIMySQLSink. Table ("
                + tableName + "), Fields (" + fieldsForCreate + "), Values ("
                + valuesForInsert + ")");

        // creating the needed columns on SlicingDice, the dimension will be automatically created
        if (!autoCreate) {
            persistenceBackend.createColumns(fieldsForCreate);
        } // if

        persistenceBackend.insertContextData(valuesForInsert);
    } // persistAggregation

    /**
     * Creates a SlicingDice dimension name given the FIWARE service path, the entity and the attribute.
     * @param servicePath
     * @param entity
     * @param attribute
     * @return The SlicingDice dimension name
     * @throws CygnusBadConfiguration
     */
    protected String buildDimensionName(final String servicePath, final String entity,
                                        final String attribute) throws CygnusBadConfiguration {
        String name;

        if (enableEncoding) {
            switch(dataModel) {
                case DMBYSERVICEPATH:
                    name = NGSICharsets.encodeSlicingDice(servicePath);
                    break;
                case DMBYENTITY:
                    name = NGSICharsets.encodeSlicingDice(servicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodeSlicingDice(entity);
                    break;
                case DMBYATTRIBUTE:
                    name = NGSICharsets.encodeSlicingDice(servicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodeSlicingDice(entity)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodeSlicingDice(attribute);
                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use dm-by-service-path, dm-by-entity or dm-by-attribute");
            } // switch
        } else {
            switch(dataModel) {
                case DMBYSERVICEPATH:
                    if (servicePath.equals("/")) {
                        throw new CygnusBadConfiguration("Default service path '/' cannot be used with "
                                + "dm-by-service-path data model");
                    } // if

                    name = encode(servicePath, true, false);
                    break;
                case DMBYENTITY:
                    String truncatedServicePath = encode(servicePath, true, false);
                    name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '-')
                            + encode(entity, false, true);
                    break;
                case DMBYATTRIBUTE:
                    truncatedServicePath = encode(servicePath, true, false);
                    name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '-')
                            + encode(entity, false, true)
                            + '-' + encode(attribute, false, true);
                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use DMBYSERVICEPATH, DMBYENTITY or DMBYATTRIBUTE");
            } // switch
        } // if else

        if (name.length() > NGSIConstants.SLICINGDICE_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building table name '" + name
                    + "' and its length is greater than " + NGSIConstants.SLICINGDICE_MAX_NAME_LEN);
        } // if

        return name;
    } // buildTableName

    /**
     * Encodes a string replacing all the non alphanumeric characters by '-' (except by '-' and '.').
     * This should be only called when building a persistence element name, such as table names, file paths, etc.
     * We had to create this specific method instead of using the existent one on NSGIUtils because
     * SlicingDice doesn't accept slashes, only '-'.
     *
     * @param in
     * @param deleteSlash
     * @param encodeSlash
     * @return The encoded version of the input string.
     */
    public static String encode(String in, boolean deleteSlash, boolean encodeSlash) {
        if (deleteSlash) {
            return NGSIUtils.getENCODEPATTERN().matcher(in.substring(1)).replaceAll("-");
        } else if (encodeSlash) {
            return NGSIUtils.getENCODEPATTERN().matcher(in).replaceAll("-");
        } else {
            return NGSIUtils.getENCODEPATTERNSLASH().matcher(in).replaceAll("-");
        } // if else
    } // encode

     public void setPersistenceBackend(final SlicingDiceBackend persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
    }
}
