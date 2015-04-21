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

import com.telefonica.iot.cygnus.backends.mongo.MongoBackend;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.flume.Context;

/**
 * OrionMongoSink will be in charge of persisting Orion context data in a historic fashion within a MongoDB deployment.
 * 
 * The way this sink will build the historics will be very similar to the already existent OrionHDFSSink,
 * OrionMySQLSink and OrionCKANSink, i.e. by appending ("to append" has several means, deppending on the final backend)
 * new raw data to the already existent one.
 * 
 * Because raw data is stored, this sinks differentiates from OrionSTHSink (issue #19), which is in charge of updating
 * already exitent data with new notified data since the goal is to offer aggregated measures to the end user.
 * Nevertheless, in the future most probably the usage of the Mongo Aggregation Framework will allow us to generate
 * such aggregated measures based on the stored raw data; in that case the usage of OrionSTHSink becomes deprecated.
 * 
 * @author frb
 */
public class OrionMongoSink extends OrionSink {
    
    /**
     * Available data models when persisting data at Mongo.
     */
    public enum DataModel { COLLECTIONPERSERVICEPATH, COLLECTIONPERENTITY, COLLECTIONPERATTRIBUTE }
    
    private static final CygnusLogger LOGGER = new CygnusLogger(OrionMongoSink.class);
    private String mongoHost;
    private int mongoPort;
    private String mongoUsername;
    private String mongoPassword;
    private DataModel dataModel;
    private String dbPrefix;
    private String collectionPrefix;
    private MongoBackend backend;

    /**
     * Constructor.
     */
    public OrionMongoSink() {
        super();
    } // OrionMongoSink
    
    /**
     * Gets the mongo host. It is protected since it is used by the tests.
     * @return
     */
    protected String getHost() {
        return mongoHost;
    } // getHost
    
    /**
     * Gets the mongo port. It is protected since it is used by the tests.
     * @return
     */
    protected int getPort() {
        return mongoPort;
    } // getPort
    
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
    protected void setBackend(MongoBackend backend) {
        this.backend = backend;
    } // setBackend
    
    /**
     * Gets the backend. It is protected since it is used by the tests.
     * @return
     */
    protected MongoBackend getBackend() {
        return backend;
    } // getBackend
    
    @Override
    public void configure(Context context) {
        mongoHost = context.getString("mongo_host", "localhost");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mongo_host=" + mongoHost + ")");
        mongoPort = context.getInteger("mongo_port", 27017);
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mongo_port=" + mongoPort + ")");
        mongoUsername = context.getString("mongo_username", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mongo_username=" + mongoUsername + ")");
        // FIXME: mongoPassword should be read as a SHA1 and decoded here
        mongoPassword = context.getString("mongo_password", "");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (mongo_password=" + mongoPassword + ")");
        dataModel = getDataModel(context.getString("data_model", "collection-per-attribute"));
        
        if (dataModel == null) {
            LOGGER.error("Invalid data model, using 'collection-per-attribute' by default");
            dataModel = DataModel.COLLECTIONPERATTRIBUTE;
        } // if
        
        LOGGER.debug("[" + this.getName() + "] Reading configuration (data_model=" + dataModel + ")");
        dbPrefix = context.getString("db_prefix", "sth_");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (db_prefix=" + dbPrefix + ")");
        collectionPrefix = context.getString("collection_prefix", "sth_");
        LOGGER.debug("[" + this.getName() + "] Reading configuration (collection_prefix=" + collectionPrefix + ")");
    } // configure
    
    @Override
    public void start() {
        // create the persistence backend
        backend = new MongoBackend(mongoHost, mongoPort, mongoUsername, mongoPassword, dataModel);
        LOGGER.debug("[" + this.getName() + "] Mongo persistence backend created");
        super.start();
        LOGGER.info("[" + this.getName() + "] Startup completed");
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

        // create the database for this fiwareService if not yet existing... the cost of trying to create it is the same
        // than checking if it exits and then creating it
        String dbName = buildDbName(fiwareService);
        backend.createDatabase(dbName);
        
        // collection name container
        String collectionName = null;

        // create the collection at this stage, if the data model is collection-per-service-path
        if (dataModel == DataModel.COLLECTIONPERSERVICEPATH) {
            collectionName = buildCollectionName(fiwareServicePath, null, null);
            backend.createCollection(dbName, collectionName);
        } // if
        
        // iterate on the contextResponses
        ArrayList contextResponses = notification.getContextResponses();
        
        for (int i = 0; i < contextResponses.size(); i++) {
            NotifyContextRequest.ContextElementResponse contextElementResponse;
            contextElementResponse = (NotifyContextRequest.ContextElementResponse) contextResponses.get(i);
            NotifyContextRequest.ContextElement contextElement = contextElementResponse.getContextElement();
            String entityId = contextElement.getId();
            String entityType = contextElement.getType();
            LOGGER.debug("[" + this.getName() + "] Processing context element (id=" + entityId + ", type= "
                    + entityType + ")");
            
            // create the collection at this stage, if the data model is collection-per-entity
            if (dataModel == DataModel.COLLECTIONPERENTITY) {
                collectionName = buildCollectionName(fiwareServicePath, destinations[i], null);
                backend.createCollection(dbName, collectionName);
            } // if
            
            // iterate on all this entity's attributes, if there are attributes
            ArrayList<NotifyContextRequest.ContextAttribute> contextAttributes = contextElement.getAttributes();
            
            if (contextAttributes == null || contextAttributes.isEmpty()) {
                LOGGER.warn("No attributes within the notified entity, nothing is done (id=" + entityId
                        + ", type=" + entityType + ")");
                continue;
            } // if
            
            // this is used for storing the attribute's names and values when dealing with a per column attributes
            // persistence; in that case the persistence is not done attribute per attribute, but persisting all of them
            // at the same time
            HashMap<String, String> attrs = new HashMap<String, String>();
            
            // this is used for storing the attribute's names (sufixed with "-md") and metadata when dealing with a per
            // column attributes persistence; in that case the persistence is not done attribute per attribute, but
            // persisting all of them at the same time
            HashMap<String, String> mds = new HashMap<String, String>();

            for (NotifyContextRequest.ContextAttribute contextAttribute : contextAttributes) {
                String attrName = contextAttribute.getName();
                String attrType = contextAttribute.getType();
                String attrValue = contextAttribute.getContextValue(false);
                String attrMetadata = contextAttribute.getContextMetadata();
                LOGGER.debug("[" + this.getName() + "] Processing context attribute (name=" + attrName + ", type="
                        + attrType + ")");
                
                // create the collection at this stage, if the data model is collection-per-attribute
                if (dataModel == DataModel.COLLECTIONPERATTRIBUTE) {
                    collectionName = buildCollectionName(fiwareServicePath, destinations[i], attrName);
                    backend.createCollection(dbName, collectionName);
                } // if

                LOGGER.info("[" + this.getName() + "] Persisting data at OrionMongoSink. Database: " + dbName
                        + ", Collection: " + collectionName + ", Data: " + recvTimeTs / 1000 + "," + recvTime + ","
                        + entityId + "," + entityType + "," + attrName + "," + entityType + "," + attrValue + ","
                        + attrMetadata);
                backend.insertContextDataRaw(dbName, collectionName, recvTimeTs / 1000, recvTime,
                        entityId, entityType, attrName, attrType, attrValue, attrMetadata);
            } // for
        } // for
    } // persist
 
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
     * Builds a database name given a fiwareService. It throws an exception if the naming conventions are violated.
     * @param fiwareService
     * @return
     * @throws Exception
     */
    private String buildDbName(String fiwareService) throws Exception {
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
     * @param fiwareServicePath
     * @param destination
     * @return
     * @throws Exception
     */
    private String buildCollectionName(String fiwareServicePath, String destination, String attrName) throws Exception {
        String collectionName = collectionPrefix;
        
        switch (dataModel) {
            case COLLECTIONPERSERVICEPATH:
                collectionName += fiwareServicePath;
                break;
            case COLLECTIONPERENTITY:
                collectionName += fiwareServicePath + "_" + destination;
                break;
            case COLLECTIONPERATTRIBUTE:
                collectionName += fiwareServicePath + "_" + destination + "_" + attrName;
                break;
            default:
                // this will never be reached
        } // switch

        if (collectionName.length() > Constants.MAX_NAME_LEN) {
            throw new CygnusBadConfiguration("Building collectionName=fiwareServicePath + '_' + destination ("
                    + collectionName + ") and its length is greater than " + Constants.MAX_NAME_LEN);
        } // if
        
        return collectionName;
    } // buildCollectionName

} // OrionMongoSink
