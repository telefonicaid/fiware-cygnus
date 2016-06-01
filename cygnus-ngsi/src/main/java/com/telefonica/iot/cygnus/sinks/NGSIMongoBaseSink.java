/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtils;
import java.security.MessageDigest;
import org.apache.commons.codec.binary.Hex;
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
    protected boolean shouldHash;
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
        mongoHosts = context.getString("mongo_hosts", "localhost:27017");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mongo_hosts=" + mongoHosts + ")");
        mongoUsername = context.getString("mongo_username", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mongo_username=" + mongoUsername + ")");
        // FIXME: mongoPassword should be read as a SHA1 and decoded here
        mongoPassword = context.getString("mongo_password", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mongo_password=" + mongoPassword + ")");
        dbPrefix = NGSIUtils.encodeSTHDB(context.getString("db_prefix", "sth_"));
        LOGGER.debug("[" + this.getName() + "] Reading configuration (db_prefix=" + dbPrefix + ")");
        collectionPrefix = NGSIUtils.encodeSTHCollection(context.getString("collection_prefix", "sth_"));
        
        if (collectionPrefix.equals("system.")) {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (collection_prefix="
                + collectionPrefix + ") -- Cannot be 'system.'");
        } else {
            LOGGER.debug("[" + this.getName() + "] Reading configuration (collection_prefix=" + collectionPrefix + ")");
        } // if else
        
        String shouldHashStr = context.getString("should_hash", "false");
        
        if (shouldHashStr.equals("true") || shouldHashStr.equals("false")) {
            shouldHash = Boolean.valueOf(shouldHashStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (should_hash="
                + shouldHashStr + ")");
        } else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (should_hash="
                + shouldHashStr + ") -- Must be 'true' or 'false'");
        }  // if else

        dataExpiration = context.getLong("data_expiration", 0L);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (data_expiration=" + dataExpiration + ")");
        
        String ignoreWhiteSpacesStr = context.getString("ignore_white_spaces", "true");
        
        if (ignoreWhiteSpacesStr.equals("true") || ignoreWhiteSpacesStr.equals("false")) {
            ignoreWhiteSpaces = Boolean.valueOf(ignoreWhiteSpacesStr);
            LOGGER.debug("[" + this.getName() + "] Reading configuration (ignore_white_spaces="
                + ignoreWhiteSpacesStr + ")");
        }  else {
            invalidConfiguration = true;
            LOGGER.debug("[" + this.getName() + "] Invalid configuration (ignore_white_spaces="
                + ignoreWhiteSpacesStr + ") -- Must be 'true' or 'false'");
        }  // if else
        
        super.configure(context);
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
     * @return A database name
     */
    protected String buildDbName(String fiwareService) {
        return dbPrefix + NGSIUtils.encodeSTHDB(fiwareService);
    } // buildDbName

    /**
     * Builds a collection name given a fiwareServicePath and a destination. It throws an exception if the naming
     * conventions are violated.
     * @param dbName
     * @param fiwareServicePath
     * @param entity
     * @param attribute
     * @param isAggregated
     * @param entityId
     * @param entityType
     * @param fiwareService
     * @return
     * @throws Exception
     */
    protected String buildCollectionName(String dbName, String fiwareServicePath, String entity, String attribute,
            boolean isAggregated, String entityId, String entityType, String fiwareService)
        throws Exception {
        String collectionName;

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

        if (shouldHash) {
            int limit = getHashSizeInBytes(dbName);

            if (limit < NGSIConstants.STH_MIN_HASH_SIZE_IN_BYTES) {
                LOGGER.error("The available bytes for the hashes to be used as part of the collection names is not "
                        + "big enough (at least " + NGSIConstants.STH_MIN_HASH_SIZE_IN_BYTES + " bytes are needed), "
                        + "please reduce the size of the database prefix, the fiware-service and/or the collection "
                        + "prefix");
                return null;
            } // if

            String hash = generateHash(collectionName, limit);
            collectionName = collectionPrefix + hash;
            backend.storeCollectionHash(dbName, hash, isAggregated, fiwareService, fiwareServicePath, entityId,
                    entityType, attribute, entity);
        } else {
            collectionName = collectionPrefix + collectionName;
        } // if else

        return collectionName;
    } // buildCollectionName

    private int getHashSizeInBytes(String dbName) {
        return NGSIConstants.STH_MAX_NAMESPACE_SIZE_IN_BYTES - dbName.getBytes().length
                - collectionPrefix.getBytes().length - ".aggr".getBytes().length - 1;
    } // getHashSizeInBytes

    private String generateHash(String collectionName, int limit) throws Exception {
        MessageDigest messageDigest;
        messageDigest = MessageDigest.getInstance("SHA-512");
        byte [] digest = messageDigest.digest(collectionName.getBytes());
        String hash = Hex.encodeHexString(digest);

        if (limit > 0) {
            hash = hash.substring(0, limit);
        } // if

        return hash;
    } // generateHash

} // NGSIMongoBaseSink
