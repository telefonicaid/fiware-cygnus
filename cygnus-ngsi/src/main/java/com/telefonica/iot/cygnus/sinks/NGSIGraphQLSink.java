/**
 * Copyright 2025 Telefonica Espana
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

import com.telefonica.iot.cygnus.aggregation.NGSIGenericAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericColumnAggregator;
import com.telefonica.iot.cygnus.aggregation.NGSIGenericRowAggregator;
import com.telefonica.iot.cygnus.backends.graphql.GraphQLBackendImpl;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusCappingError;
import com.telefonica.iot.cygnus.errors.CygnusExpiratingError;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.*;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.flume.Context;

/**
 *
 * @author smartcities

 Detailed documentation can be found at:
  *** TDB *** 
 */
public class NGSIGraphQLSink extends NGSISink {

    //private static final String DEFAULT_ROW_ATTR_PERSISTENCE = "row";
    private static final String DEFAULT_GRAPHQL_ENDPOINT = "http://localhost:4000";
    private static final String DEFAULT_ENABLE_CACHE = "false";
    private static final int DEFAULT_MAX_POOL_SIZE = 100;
    private static final int DEFAULT_MAX_POOL_PER_ROUTE = 50;
    private static final String DEFAULT_POSTGIS_TYPE = "geometry";
    private static final String DEFAULT_ATTR_NATIVE_TYPES = "false";
    private static final String DEFAULT_FIWARE_SERVICE = "default";
    private static final String ESCAPED_DEFAULT_FIWARE_SERVICE = "default_service";
    private static final String DEFAULT_LAST_DATA_MODE = "upsert";
    private static final String DEFAULT_LAST_DATA_TABLE_SUFFIX = "_last_data";
    private static final String DEFAULT_LAST_DATA_UNIQUE_KEY = NGSIConstants.ENTITY_ID;
    private static final String DEFAULT_LAST_DATA_TIMESTAMP_KEY = NGSIConstants.RECV_TIME;
    private static final String DEFAULT_LAST_DATA_SQL_TS_FORMAT = "YYYY-MM-DD HH24:MI:SS.MS";

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIGraphQLSink.class);
    private String graphqlEndpoint;
    //private boolean rowAttrPersistence;
    private int maxPoolSize;
    private int maxPoolPerRoute;
    private GraphQLBackendImpl graphqlPersistenceBackend;
    private boolean enableCache;
    private boolean swapCoordinates;
    private boolean attrNativeTypes;
    private boolean attrMetadataStore;
    private String graphqlOptions;
    //private boolean persistErrors;
    private String lastDataMode;
    private String lastDataTableSuffix;
    private String lastDataUniqueKey;
    private String lastDataTimeStampKey;
    private String lastDataSQLTimestampFormat;

    /**
     * Constructor.
     */
    public NGSIGraphQLSink() {
        super();
    } // NGSIGraphQLSink

    /**
     * Gets the GraphQL host. It is protected due to it is only required for testing purposes.
     * @return The GraphQL host
     */
    protected String getGraphQLEndpoint() {
        return graphqlEndpoint;
    } // getGraphQLEndpoint
    
    // /**
    //  * Returns if the attribute persistence is row-based. It is protected due to it is only required for testing
    //  * purposes.
    //  * @return True if the attribute persistence is row-based, false otherwise
    //  */
    // protected boolean getRowAttrPersistence() {
    //     return rowAttrPersistence;
    // } // getRowAttrPersistence

    /**
     * Gets the GraphQL options. It is protected due to it is only required for testing purposes.
     * @return The GraphQL options
     */
    protected String getGraphQLOptions() {
        return graphqlOptions;
    } // getGraphQLOptions

    /**
     * Returns if the attribute value will be native or stringfy. It will be stringfy due to backward compatibility
     * purposes.
     * @return True if the attribute value will be native, false otherwise
     */
    protected boolean getNativeAttrTypes() {
        return attrNativeTypes;
    } // attrNativeTypes

    /**
     * Returns the persistence backend. It is protected due to it is only required for testing purposes.
     * @return The persistence backend
     */
    protected GraphQLBackendImpl getPersistenceBackend() {
        return graphqlPersistenceBackend;
    } // getPersistenceBackend

    /**
     * Sets the persistence backend. It is protected due to it is only required for testing purposes.
     * @param virtuosoPersistenceBackend
     */
    protected void setPersistenceBackend(GraphQLBackendImpl graphqlPersistenceBackend) {
        this.graphqlPersistenceBackend = graphqlPersistenceBackend;
    } // setPersistenceBackend

    @Override
    public void configure(Context context) {
        // Read NGSISink general configuration
        super.configure(context);
        
        // Impose enable lower case, since GraphQL only accepts lower case
        enableLowercase = true;
        
        graphqlEndpoint = context.getString("graphql_endpoint", DEFAULT_GRAPHQL_ENDPOINT);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (graphql_endpoint=" + graphqlEndpoint + ")");

        maxPoolSize = context.getInteger("graphql_maxPoolSize", DEFAULT_MAX_POOL_SIZE);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (graphql_maxPoolSize=" + maxPoolSize + ")");

        maxPoolPerRoute = context.getInteger("graphql_maxPoolPerRoute", DEFAULT_MAX_POOL_PER_ROUTE);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (graphql_maxPoolPerRoute=" + maxPoolPerRoute + ")");

        // rowAttrPersistence = context.getString("attr_persistence", DEFAULT_ROW_ATTR_PERSISTENCE).equals("row");
        // String persistence = context.getString("attr_persistence", DEFAULT_ROW_ATTR_PERSISTENCE);

        // if (persistence.equals("row") || persistence.equals("column")) {
        //     LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_persistence="
        //         + persistence + ")");
        // } else {
        //     invalidConfiguration = true;
        //     LOGGER.warn("[" + this.getName() + "] Invalid configuration (attr_persistence="
        //         + persistence + ") -- Must be 'row' or 'column'");
        // }  // if else
                
        String enableCacheStr = context.getString("backend.enable_cache", DEFAULT_ENABLE_CACHE);
        
        if (enableCacheStr.equals("true") || enableCacheStr.equals("false")) {
            enableCache = Boolean.valueOf(enableCacheStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (backend.enable_cache=" + enableCache + ")");
        }  else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (backend.enable_cache="
                + enableCacheStr + ") -- Must be 'true' or 'false'");
        }  // if else

        // TBD: possible option for graphqlSink
        swapCoordinates = false;


        String attrNativeTypesStr = context.getString("attr_native_types", DEFAULT_ATTR_NATIVE_TYPES);
        if (attrNativeTypesStr.equals("true") || attrNativeTypesStr.equals("false")) {
            attrNativeTypes = Boolean.valueOf(attrNativeTypesStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_native_types=" + attrNativeTypesStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (attr_native_types="
                + attrNativeTypesStr + ") -- Must be 'true' or 'false'");
        } // if else

        String attrMetadataStoreStr = context.getString("attr_metadata_store", "true");

        if (attrMetadataStoreStr.equals("true") || attrMetadataStoreStr.equals("false")) {
            attrMetadataStore = Boolean.parseBoolean(attrMetadataStoreStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_metadata_store="
                    + attrMetadataStore + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (attr_metadata_store="
                    + attrMetadataStoreStr + ") -- Must be 'true' or 'false'");
        } // if else

        graphqlOptions = context.getString("graphql_options", null);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (graphql_options=" + graphqlOptions + ")");

        // String persistErrorsStr = context.getString("persist_errors", "true");

        // if (persistErrorsStr.equals("true") || persistErrorsStr.equals("false")) {
        //     persistErrors = Boolean.parseBoolean(persistErrorsStr);
        //     LOGGER.debug("[" + this.getName() + "] Reading configuration (persist_errors="
        //             + persistErrors + ")");
        // } else {
        //     invalidConfiguration = true;
        //     LOGGER.debug("[" + this.getName() + "] Invalid configuration (persist_errors="
        //             + persistErrorsStr + ") -- Must be 'true' or 'false'");
        // } // if else

        lastDataMode = context.getString("last_data_mode", DEFAULT_LAST_DATA_MODE);

        if (lastDataMode.equals("upsert") || lastDataMode.equals("insert") || lastDataMode.equals("both")) {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (last_data_mode="
                    + lastDataMode + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (last_data_mode="
                    + lastDataMode + ") -- Must be 'upsert', 'insert' or 'both'");
        } // if else

        lastDataTableSuffix = context.getString("last_data_table_suffix", DEFAULT_LAST_DATA_TABLE_SUFFIX);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (last_data_table_suffix="
                + lastDataTableSuffix + ")");

        lastDataUniqueKey = context.getString("last_data_unique_key", DEFAULT_LAST_DATA_UNIQUE_KEY);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (last_data_unique_key="
                + lastDataUniqueKey + ")");

        lastDataTimeStampKey = context.getString("last_data_timestamp_key", DEFAULT_LAST_DATA_TIMESTAMP_KEY);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (last_data_timestamp_key="
                + lastDataTimeStampKey + ")");

        lastDataSQLTimestampFormat = context.getString("last_data_sql_timestamp_format", DEFAULT_LAST_DATA_SQL_TS_FORMAT);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (last_data_sql_timestamp_format="
                + lastDataSQLTimestampFormat + ")");

    } // configure

    @Override
    public void stop() {
        super.stop();
        if (graphqlPersistenceBackend != null) graphqlPersistenceBackend.close();
    } // stop

    @Override
    public void start() {
        try {
            createPersistenceBackend(graphqlEndpoint, maxPoolSize, maxPoolPerRoute, graphqlOptions);
            LOGGER.debug("[" + this.getName() + "] GRAPHQL persistence backend created");
        } catch (Exception e) {
            String configParams = " graphqlEndpoint " + graphqlEndpoint +
                " maxPoolSize " +  maxPoolSize + " maxPoolPerRoute " +  maxPoolPerRoute + " graphqlOptions " +
                graphqlOptions;
            LOGGER.error("Error while creating the GraphQL persistence backend. " +
                         "Config params= " + configParams +
                         "Details=" + e.getMessage() +
                         "Stack trace: " + Arrays.toString(e.getStackTrace()));
        } // try catch

        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start

    /**
     * Initialices a lazy singleton to share among instances on JVM
     */
    private void createPersistenceBackend(String graphqEndpoint, int maxPoolSize, int maxPoolPerRoute, String graphqlOptions) {
        if (graphqlPersistenceBackend == null) {
            graphqlPersistenceBackend = new GraphQLBackendImpl(graphqlEndpoint, maxPoolSize, maxPoolPerRoute, graphqlOptions);
        }
    }

    @Override
    void persistBatch(NGSIBatch batch)
            throws CygnusBadConfiguration, CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {
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

            // get the sub-batch for this destination
            ArrayList<NGSIEvent> events = batch.getNextEvents();

            // get an aggregator for this destination and initialize it
            NGSIGenericAggregator aggregator = getAggregator(/*rowAttrPersistence*/);
            aggregator.setService(events.get(0).getServiceForNaming(enableNameMappings));
            aggregator.setServicePathForData(events.get(0).getServicePathForData());
            aggregator.setServicePathForNaming(events.get(0).getServicePathForNaming(enableNameMappings));
            aggregator.setEntityForNaming(events.get(0).getEntityForNaming(enableNameMappings, enableEncoding));
            aggregator.setEntityType(events.get(0).getEntityTypeForNaming(enableNameMappings));
            aggregator.setAttribute(events.get(0).getAttributeForNaming(enableNameMappings));
            // TDB ??
            aggregator.setSchemeName(buildSchemaName(aggregator.getService(), aggregator.getServicePathForNaming()));
            aggregator.setDbName(buildDBName(events.get(0).getServiceForNaming(enableNameMappings)));
            aggregator.setTableName(buildTableName(aggregator.getServicePathForNaming(), aggregator.getEntityForNaming(), aggregator.getEntityType(), aggregator.getAttribute()));
            aggregator.setAttrNativeTypes(attrNativeTypes);
            aggregator.setAttrMetadataStore(attrMetadataStore);
            aggregator.setEnableGeoParse(true);
            aggregator.setEnableNameMappings(enableNameMappings);
            aggregator.setLastDataMode(lastDataMode);
            aggregator.setLastDataTimestampKey(lastDataTimeStampKey);
            aggregator.setLastDataUniqueKey(lastDataUniqueKey);
            aggregator.initialize(events.get(0));
            for (NGSIEvent event : events) {
                aggregator.aggregate(event);
            } // for
            // LOGGER.debug("[" + getName() + "] adding event to aggregator object  (name=" +
            //              SQLQueryUtils.getFieldsForInsert(aggregator.getAggregation().keySet(), SQLQueryUtils.POSTGRES_FIELDS_MARK) + ", values=" +
            //              SQLQueryUtils.getValuesForInsert(aggregator.getAggregation(), attrNativeTypes) + ")");
            // persist the fieldValues
            persistAggregation(aggregator);
            batch.setNextPersisted(true);
        } // for
    } // persistBatch
    
    protected NGSIGenericAggregator getAggregator(/*boolean rowAttrPersistence*/) {
        // if (rowAttrPersistence) {
        //     return new NGSIGenericRowAggregator();
        // } else {
            return new NGSIGenericColumnAggregator();
        // } // if else
    } // getAggregator

    @Override
    public void capRecords(NGSIBatch batch, long maxRecords) throws CygnusCappingError {
    } // capRecords
    
    @Override
    public void expirateRecords(long expirationTime) throws CygnusExpiratingError {
    } // expirateRecords

    
    private void persistAggregation(NGSIGenericAggregator aggregator) throws CygnusPersistenceError, CygnusRuntimeError, CygnusBadContextData {

        String dataBaseName = aggregator.getDbName(enableLowercase);
        String schemaName = aggregator.getSchemeName(enableLowercase);
        String tableName = aggregator.getTableName(enableLowercase);

        // Escape a syntax error in SQL
        if (schemaName.equals(DEFAULT_FIWARE_SERVICE)) {
            schemaName = ESCAPED_DEFAULT_FIWARE_SERVICE;
        }

        if (lastDataMode.equals("upsert")) {
            // if (rowAttrPersistence) {
            //     LOGGER.warn("[" + this.getName() + "] no upsert due to row mode");
            // } else {
                graphqlPersistenceBackend.upsertTransaction(aggregator.getAggregationToPersist(),
                                                            aggregator.getLastDataToPersist(),
                                                            aggregator.getLastDataDeleteToPersist(),
                                                            dataBaseName,
                                                            schemaName,
                                                            tableName,
                                                            lastDataTableSuffix,
                                                            lastDataUniqueKey,
                                                            lastDataTimeStampKey,
                                                            lastDataSQLTimestampFormat,
                                                            attrNativeTypes);
          //}
        }
    } // persistAggregation

    /**
     * Creates a GraphQL DB name given the FIWARE service.
     * @param service
     * @return The GraphQL DB name
     * @throws CygnusBadConfiguration
     */
    public String buildDBName(String service) throws CygnusBadConfiguration {
        String name = null;

        if (enableEncoding) {
            switch(dataModel) {
                case DMBYENTITYDATABASE:
                case DMBYENTITYDATABASESCHEMA:
                case DMBYENTITYTYPEDATABASE:
                case DMBYENTITYTYPEDATABASESCHEMA:
                case DMBYFIXEDENTITYTYPEDATABASE:
                case DMBYFIXEDENTITYTYPEDATABASESCHEMA:
                    if (service != null)
                        name = NGSICharsets.encodePostgreSQL(service);
                    break;
                default:
                    name = null;
            }
        } else {
            switch(dataModel) {
                case DMBYENTITYDATABASE:
                case DMBYENTITYDATABASESCHEMA:
                case DMBYENTITYTYPEDATABASE:
                case DMBYENTITYTYPEDATABASESCHEMA:
                case DMBYFIXEDENTITYTYPEDATABASE:
                case DMBYFIXEDENTITYTYPEDATABASESCHEMA:
                    if (service != null)
                        name = NGSIUtils.encode(service, false, true);
                    break;
                default:
                    name = null;
            }
        } // if else
        if (name.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building DB name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildSchemaName

    /**
     * Creates a GraphQL scheme name given the FIWARE service.
     * @param service
     * @return The GraphQL scheme name
     * @throws CygnusBadConfiguration
     */
    public String buildSchemaName(String service, String subService) throws CygnusBadConfiguration {
        String name;
        
        if (enableEncoding) {
            switch(dataModel) {
                case DMBYENTITYDATABASESCHEMA:
                case DMBYENTITYTYPEDATABASESCHEMA:
                case DMBYFIXEDENTITYTYPEDATABASESCHEMA:
                    name = NGSICharsets.encodePostgreSQL(subService);
                    break;
                default:
                    name = NGSICharsets.encodePostgreSQL(service);
            }
        } else {
            switch(dataModel) {
                case DMBYENTITYDATABASESCHEMA:
                case DMBYENTITYTYPEDATABASESCHEMA:
                case DMBYFIXEDENTITYTYPEDATABASESCHEMA:
                    name = NGSIUtils.encode(subService, true, false);
                    break;
                default:
                    name = NGSIUtils.encode(service, false, true);
            }
        } // if else

        if (name.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building schema name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildSchemaName

    /**
     * Creates a GraphQL table name given the FIWARE service path, the entity and the attribute.
     * @param servicePath
     * @param entity
     * @param attribute
     * @return The GraphQL table name
     * @throws CygnusBadConfiguration
     */
    public String buildTableName(String servicePath, String entity, String entityType, String attribute) throws CygnusBadConfiguration {
        String name;

        if (enableEncoding) {
            switch(dataModel) {
                case DMBYSERVICEPATH:
                    name = NGSICharsets.encodePostgreSQL(servicePath);
                    break;
                case DMBYENTITYDATABASE:
                case DMBYENTITYDATABASESCHEMA:
                case DMBYENTITY:
                    name = NGSICharsets.encodePostgreSQL(servicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodePostgreSQL(entity);
                    break;
                case DMBYENTITYTYPEDATABASE:
                case DMBYENTITYTYPEDATABASESCHEMA:
                case DMBYENTITYTYPE:
                    name = NGSICharsets.encodePostgreSQL(servicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodePostgreSQL(entityType);
                    break;
                case DMBYATTRIBUTE:
                    name = NGSICharsets.encodePostgreSQL(servicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodePostgreSQL(entity)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodePostgreSQL(attribute);
                    break;
                case DMBYFIXEDENTITYTYPE:
                case DMBYFIXEDENTITYTYPEDATABASE:
                case DMBYFIXEDENTITYTYPEDATABASESCHEMA:
                    name = NGSICharsets.encodePostgreSQL(entityType);
                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use dm-by-service-path, dm-by-entity, dm-by-entity-database, dm-by-entity-database-schema, dm-by-entity-type, dm-by-entity-type-database, dm-by-entity-type-database-schema, dm-by-fixed-entity-type-database, dm-by-fixed-entity-type-database-schema or dm-by-attribute");
            } // switch
        } else {
            switch(dataModel) {
                case DMBYSERVICEPATH:
                    if (servicePath.equals("/")) {
                        throw new CygnusBadConfiguration("Default service path '/' cannot be used with "
                                + "dm-by-service-path data model");
                    } // if
                    
                    name = NGSIUtils.encode(servicePath, true, false);
                    break;
                case DMBYENTITYDATABASE:
                case DMBYENTITYDATABASESCHEMA:
                case DMBYENTITY:
                    String truncatedServicePath = NGSIUtils.encode(servicePath, true, false);
                    name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '_')
                            + NGSIUtils.encode(entity, false, true);
                    break;
                case DMBYENTITYTYPEDATABASE:
                case DMBYENTITYTYPEDATABASESCHEMA:
                case DMBYENTITYTYPE:
                    truncatedServicePath = NGSIUtils.encode(servicePath, true, false);
                    name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '_')
                            + NGSIUtils.encode(entityType, false, true);
                    break;
                case DMBYATTRIBUTE:
                    truncatedServicePath = NGSIUtils.encode(servicePath, true, false);
                    name = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '_')
                            + NGSIUtils.encode(entity, false, true)
                            + '_' + NGSIUtils.encode(attribute, false, true);
                    break;
                case DMBYFIXEDENTITYTYPEDATABASE:
                case DMBYFIXEDENTITYTYPEDATABASESCHEMA:
                case DMBYFIXEDENTITYTYPE:
                    name = NGSIUtils.encode(entityType, false, true);
                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use DMBYSERVICEPATH, DMBYENTITYDATABASE, DMBYENTITYDATABASESCHEMA, DMBYENTITY, DMBYENTITYTYPEDATABASE, DMBYENTITYTYPEDATABASESCHEMA, DMBYENTITYTYPE, DMBYFIXEDENTITYTYPE, DMBYFIXEDENTITYTYPEDATABASE, DMBYFIXEDENTITYTYPEDATABASESCHEMA or DMBYATTRIBUTE");
            } // switch
        } // if else

        if (name.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building table name '" + name
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if

        return name;
    } // buildTableName

} // NGSIGraphQLSink
