/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.security.MessageDigest;
import org.apache.commons.codec.binary.Hex;
import org.apache.flume.Context;

/**
 *
 * @author frb
 * @author xdelox
 */
public abstract class OrionMongoBaseSink extends OrionSink {
    
    /**
     * Available data models when persisting data at Mongo.
     */
    public enum DataModel { COLLECTIONPERSERVICEPATH, COLLECTIONPERENTITY, COLLECTIONPERATTRIBUTE }
    
    protected static final CygnusLogger LOGGER = new CygnusLogger(OrionMongoBaseSink.class);
    protected String mongoHosts;
    protected String mongoUsername;
    protected String mongoPassword;
    protected DataModel dataModel;
    protected String dbPrefix;
    protected String collectionPrefix;
    protected boolean shouldHash;
    protected MongoBackendImpl backend;
    protected boolean rowAttrPersistence;

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
     * Gets the mongo data model. It is protected since it is used by the tests.
     * @return
     */
    protected DataModel getDataModel() {
        return dataModel;
    } // getDataModel
    
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

    /**
     * Gets the rowAttrPersistence. It is protected since it is used by the tests.
     * @return
     */
    protected boolean getRowAttrPersistence() {
        return this.rowAttrPersistence;
    }
    
    @Override
    public void configure(Context context) {
        mongoHosts = context.getString("mongo_hosts", "localhost:27017");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mongo_hosts=" + mongoHosts + ")");
        mongoUsername = context.getString("mongo_username", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mongo_username=" + mongoUsername + ")");
        // FIXME: mongoPassword should be read as a SHA1 and decoded here
        mongoPassword = context.getString("mongo_password", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mongo_password=" + mongoPassword + ")");
        dataModel = getDataModel(context.getString("data_model", "collection-per-entity"));
        LOGGER.debug("[" + this.getName() + "] Reading configuration (data_model=" + dataModel + ")");
        dbPrefix = Utils.encode(context.getString("db_prefix", "sth_"));
        LOGGER.debug("[" + this.getName() + "] Reading configuration (db_prefix=" + dbPrefix + ")");
        collectionPrefix = Utils.encode(context.getString("collection_prefix", "sth_"));
        LOGGER.debug("[" + this.getName() + "] Reading configuration (collection_prefix=" + collectionPrefix + ")");
        shouldHash = context.getBoolean("should_hash", false);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (should_hash=" + shouldHash + ")");
        super.configure(context);
        this.rowAttrPersistence = context.getString("attr_persistence", "row").equals("row");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (attr_persistence="
                + (this.rowAttrPersistence ? "row" : "column") + ")");
    } // configure
    
    @Override
    public void start() {
        // create the persistence backend
        backend = new MongoBackendImpl(mongoHosts, mongoUsername, mongoPassword, dataModel);
        LOGGER.debug("[" + this.getName() + "] Mongo persistence backend created");
        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
    } // start
    
    /**
     * Gets the data model given its string representation.
     * @param dataModelStr
     * @return
     */
    protected DataModel getDataModel(String dataModelStr) {
        if (dataModelStr.equals("collection-per-service-path")) {
            return DataModel.COLLECTIONPERSERVICEPATH;
        } else if (dataModelStr.equals("collection-per-entity")) {
            return DataModel.COLLECTIONPERENTITY;
        } else if (dataModelStr.equals("collection-per-attribute")) {
            return DataModel.COLLECTIONPERATTRIBUTE;
        } else {
            return null;
        } // if else if
    } // getDataModel
    
    /**
     * Gets a trng reprensentation of a given data model.
     * @param dataModel
     * @return The string representation of the given data model
     */
    public static String getStrDataModel(DataModel dataModel) {
        switch(dataModel) {
            case COLLECTIONPERSERVICEPATH:
                return "collection-per-service-path";
            case COLLECTIONPERENTITY:
                return "collection-per-entity";
            case COLLECTIONPERATTRIBUTE:
                return "collection-per-attribute";
            default:
                return null;
        } // switch
    } // getStrDataModel

    /**
     * Builds a database name given a fiwareService. It throws an exception if the naming conventions are violated.
     * @param fiwareService
     * @return
     * @throws Exception
     */
    protected String buildDbName(String fiwareService) throws Exception {
        String dbName = dbPrefix + fiwareService;
        
        if (dbName.length() > Constants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building dbName=fiwareService (" + dbName + ") and its length is greater "
                    + "than " + Constants.MAX_NAME_LEN);
        } // if
        
        return dbName;
    } // buildDbName
    
    /**
     * Builds a collection name given a fiwareServicePath and a destination. It throws an exception if the naming
     * conventions are violated.
     * @param dbName
     * @param fiwareServicePath
     * @param destination
     * @param attrName
     * @param isAggregated
     * @param entityId
     * @param entityType
     * @param fiwareService
     * @return
     * @throws Exception
     */
    protected String buildCollectionName(String dbName, String fiwareServicePath, String destination, String attrName,
            boolean isAggregated, String entityId, String entityType, String fiwareService)
        throws Exception {
        String collectionName;
        
        switch (dataModel) {
            case COLLECTIONPERSERVICEPATH:
                collectionName = fiwareServicePath;
                break;
            case COLLECTIONPERENTITY:
                collectionName = fiwareServicePath + "_" + destination;
                break;
            case COLLECTIONPERATTRIBUTE:
                collectionName = fiwareServicePath + "_" + destination + "_" + attrName;
                break;
            default:
                // this should never be reached
                collectionName = null;
        } // switch
        
        if (shouldHash) {
            int limit = getHashSizeInBytes(dbName);
            
            if (limit < Constants.STH_MIN_HASH_SIZE_IN_BYTES) {
                LOGGER.error("The available bytes for the hashes to be used as part of the collection names is not "
                        + "big enough (at least " + Constants.STH_MIN_HASH_SIZE_IN_BYTES + " bytes are needed), "
                        + "please reduce the size of the database prefix, the fiware-service and/or the collection "
                        + "prefix");
                return null;
            } // if
            
            String hash = generateHash(collectionName, limit);
            collectionName = collectionPrefix + hash;
            backend.storeCollectionHash(dbName, hash, isAggregated, fiwareService, fiwareServicePath, entityId,
                    entityType, attrName, destination);
        } else {
            collectionName = collectionPrefix + collectionName;
            
            if (collectionName.getBytes().length > Constants.STH_MAX_NAMESPACE_SIZE_IN_BYTES) {
                LOGGER.error("");
                return null;
            } // if
        } // if else
        
        return collectionName;
    } // buildCollectionName
    
    private int getHashSizeInBytes(String dbName) {
        return Constants.STH_MAX_NAMESPACE_SIZE_IN_BYTES - dbName.getBytes().length
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
    
} // OrionMongoBaseSink
