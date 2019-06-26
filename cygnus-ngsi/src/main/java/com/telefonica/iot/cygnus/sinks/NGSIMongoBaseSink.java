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

import com.telefonica.iot.cygnus.backends.mongo.MongoBackendImpl;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSICharsets;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import org.apache.flume.Context;

/**
 *
 * @author frb
 * @author xdelox
 */
public abstract class NGSIMongoBaseSink extends NGSISink {

    protected static final CygnusLogger LOGGER = new CygnusLogger(NGSIMongoBaseSink.class);
    protected String mongoHosts;
    protected String mongoUsername;
    protected String mongoPassword;
    protected String dbPrefix;
    protected String collectionPrefix;
    protected MongoBackendImpl backend;
    protected long dataExpiration;
    protected boolean ignoreWhiteSpaces;

    /**
     * Gets the mongo hosts. It is protected since it is used by the tests.
     * @return
     */
    protected String getMongoHosts() {
        return mongoHosts;
    } // getMongoHosts

    /**
     * Gets the mongo username. It is protected since it is used by the tests.
     * @return
     */
    protected String getUsername() {
        return mongoUsername;
    } // getUsername

    /**
     * Gets the mongo password. It is protected since it is used by the tests.
     * @return
     */
    protected String getPassword() {
        return mongoPassword;
    } // getPassword

    /**
     * Gets the database prefix. It is protected since it is used by the tests.
     * @return
     */
    protected String getDbPrefix() {
        return dbPrefix;
    } // getDBPrefix

    /**
     * Gets the collection prefix. It is protected since it is used by the tests.
     * @return
     */
    protected String getCollectionPrefix() {
        return collectionPrefix;
    } // getCollectionPrefix

    /**
     * Sets the backend. It is protected since it is used by the tests.
     * @param backend
     */
    protected void setBackend(MongoBackendImpl backend) {
        this.backend = backend;
    } // setBackend

    /**
     * Gets the backend. It is protected since it is used by the tests.
     * @return
     */
    protected MongoBackendImpl getBackend() {
        return backend;
    } // getBackend
    
    @Override
    public void configure(Context context) {
        super.configure(context);
        
        mongoHosts = context.getString("mongo_hosts", "localhost:27017");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mongo_hosts=" + mongoHosts + ")");
        mongoUsername = context.getString("mongo_username", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mongo_username=" + mongoUsername + ")");
        // FIXME: mongoPassword should be read as a SHA1 and decoded here
        mongoPassword = context.getString("mongo_password", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mongo_password=" + mongoPassword + ")");
        
        if (enableEncoding) {
            dbPrefix = NGSICharsets.encodeMongoDBDatabase(context.getString("db_prefix", "sth_"));
        } else {
            dbPrefix = NGSIUtils.encodeSTHDB(context.getString("db_prefix", "sth_"));
        } // if else
        
        LOGGER.debug("[" + this.getName() + "] Reading configuration (db_prefix=" + dbPrefix + ")");
        
        if (enableEncoding) {
            collectionPrefix = NGSICharsets.encodeMongoDBCollection(context.getString("collection_prefix", "sth_"));
        } else {
            collectionPrefix = NGSIUtils.encodeSTHCollection(context.getString("collection_prefix", "sth_"));
        } // if else
        
        if (collectionPrefix.equals("system.")) {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (collection_prefix="
                + collectionPrefix + ") -- Cannot be 'system.'");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (collection_prefix=" + collectionPrefix + ")");
        } // if else

        dataExpiration = context.getLong("data_expiration", 0L);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (data_expiration=" + dataExpiration + ")");
        
        String ignoreWhiteSpacesStr = context.getString("ignore_white_spaces", "true");
        
        if (ignoreWhiteSpacesStr.equals("true") || ignoreWhiteSpacesStr.equals("false")) {
            ignoreWhiteSpaces = Boolean.valueOf(ignoreWhiteSpacesStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (ignore_white_spaces="
                + ignoreWhiteSpacesStr + ")");
        }  else {
            invalidConfiguration = true;
            LOGGER.warn("[" + this.getName() + "] Invalid configuration (ignore_white_spaces="
                + ignoreWhiteSpacesStr + ") -- Must be 'true' or 'false'");
        }  // if else
    } // configure

    @Override
    public void start() {
        try {
            backend = new MongoBackendImpl(mongoHosts, mongoUsername, mongoPassword, dataModel);
            LOGGER.debug("[" + this.getName() + "] MongoDB persistence backend created");
        } catch (Exception e) {
            LOGGER.error("Error while creating the MongoDB persistence backend. Details="
                    + e.getMessage());
        } // try catch

        super.start();
    } // start

    /**
     * Builds a database name given a fiwareService. It throws an exception if the naming conventions are violated.
     * @param fiwareService
     * @return
     * @throws CygnusBadConfiguration
     */
    protected String buildDbName(String fiwareService) throws CygnusBadConfiguration {
        String dbName;
        
        if (enableEncoding) {
            dbName = dbPrefix + NGSICharsets.encodeMongoDBDatabase(fiwareService);
        } else {
            dbName = dbPrefix + NGSIUtils.encodeSTHDB(fiwareService);
        } // if else

        if (dbName.length() > NGSIConstants.MONGO_DB_MAX_NAMESPACE_SIZE_IN_BYTES) {
            throw new CygnusBadConfiguration("Building database name '" + dbName + "' and its length is greater "
                    + "than " + NGSIConstants.MONGO_DB_MAX_NAMESPACE_SIZE_IN_BYTES);
        } // if

        return dbName;
    } // buildDbName

    /**
     * Builds a collection name given a fiwareServicePath and a destination. It throws an exception if the naming
     * conventions are violated.
     * @param fiwareServicePath
     * @param entity
     * @param attribute
     * @return
     * @throws CygnusBadConfiguration
     */
    protected String buildCollectionName(String fiwareServicePath, String entity, String attribute)
        throws CygnusBadConfiguration {
        String collectionName;

        if (enableEncoding) {
            switch (dataModel) {
                case DMBYSERVICEPATH:
                    collectionName = NGSICharsets.encodeMongoDBCollection(fiwareServicePath);
                    break;
                case DMBYENTITY:
                    collectionName = NGSICharsets.encodeMongoDBCollection(fiwareServicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodeMongoDBCollection(entity);
                    break;
                case DMBYATTRIBUTE:
                    collectionName = NGSICharsets.encodeMongoDBCollection(fiwareServicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodeMongoDBCollection(entity)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodeMongoDBCollection(attribute);
                    break;
                default:
                    throw new CygnusBadConfiguration("Unknown data model '" + dataModel.toString()
                            + "'. Please, use dm-by-service-path, dm-by-entity or dm-by-attribute");
            } // switch
        } else {
            switch (dataModel) {
                case DMBYSERVICEPATH:
                    collectionName = NGSIUtils.encodeSTHCollection(fiwareServicePath);
                    break;
                case DMBYENTITY:
                    collectionName = NGSIUtils.encodeSTHCollection(fiwareServicePath) + "_"
                            + NGSIUtils.encodeSTHCollection(entity);
                    break;
                case DMBYATTRIBUTE:
                    collectionName = NGSIUtils.encodeSTHCollection(fiwareServicePath)
                            + "_" + NGSIUtils.encodeSTHCollection(entity)
                            + "_" + NGSIUtils.encodeSTHCollection(attribute);
                    break;
                default:
                    // this should never be reached
                    collectionName = null;
            } // switch
        } // else

        collectionName = collectionPrefix + collectionName;

        if (collectionName.getBytes().length > NGSIConstants.MONGO_DB_MAX_NAMESPACE_SIZE_IN_BYTES) {
            throw new CygnusBadConfiguration("Building collection name '" + collectionName + "' and its length is "
                    + "greater than " + NGSIConstants.MONGO_DB_MAX_NAMESPACE_SIZE_IN_BYTES);
        } // if

        return collectionName;
    } // buildCollectionName

} // NGSIMongoBaseSink
