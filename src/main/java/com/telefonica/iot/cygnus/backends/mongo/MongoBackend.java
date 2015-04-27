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
package com.telefonica.iot.cygnus.backends.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.telefonica.iot.cygnus.backends.mysql.MySQLBackend;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.OrionMongoSink.DataModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bson.Document;

/**
 *
 * @author frb
 */
public class MongoBackend {
    
    private final String mongoHosts;
    private final String mongoUsername;
    private final String mongoPassword;
    private final DataModel dataModel;
    private static final CygnusLogger LOGGER = new CygnusLogger(MySQLBackend.class);
            
    /**
     * Constructor.
     * @param mongoHosts
     * @param mongoUsername
     * @param mongoPassword
     * @param dataModel
     */
    public MongoBackend(String mongoHosts, String mongoUsername, String mongoPassword,
            DataModel dataModel) {
        this.mongoHosts = mongoHosts;
        this.mongoUsername = mongoUsername;
        this.mongoPassword = mongoPassword;
        this.dataModel = dataModel;
    } // MongoBackend
    
    /**
     * Creates a database, given its name, if not exists.
     * @param dbName
     * @throws Exception
     */
    public void createDatabase(String dbName) throws Exception {
        LOGGER.debug("Creating Mongo database=" + dbName);
        getDatabase(dbName); // getting a non existent database automatically creates it
    } // createDatabase
    
    /**
     * Creates a collection, given its name, if not exists in the given database.
     * @param dbName
     * @param collectionName
     * @throws Exception
     */
    public void createCollection(String dbName, String collectionName) throws Exception {
        LOGGER.debug("Creating Mongo collection=" + collectionName + " at database=" + dbName);
        MongoDatabase db = getDatabase(dbName);

        if (db.getCollection(collectionName) == null) {
            db.createCollection(collectionName);
        } // if
    } // createCollection
    
    /**
     * Inserts a new document in the given raw collection within the given database (row-like mode).
     * @param dbName
     * @param collectionName
     * @param recvTimeTs
     * @param recvTime
     * @param entityId
     * @param entityType
     * @param attrName
     * @param attrType
     * @param attrValue
     * @param attrMd
     * @throws Exception
     */
    public void insertContextDataRaw(String dbName, String collectionName, long recvTimeTs, String recvTime,
            String entityId, String entityType, String attrName, String attrType, String attrValue,
            String attrMd) throws Exception {
        MongoDatabase db = getDatabase(dbName);
        MongoCollection collection = db.getCollection(collectionName);
        Document doc = new Document("recvTimeTs", recvTimeTs)
                .append("recvTime", recvTime);
        
        switch (dataModel) {
            case COLLECTIONPERSERVICEPATH:
                doc.append("entityId", entityId)
                        .append("entityType", entityType)
                        .append("attrName", attrName)
                        .append("attrType", attrType)
                        .append("attrValue", attrValue);
                break;
            case COLLECTIONPERENTITY:
                doc.append("attrName", attrName)
                        .append("attrType", attrType)
                        .append("attrValue", attrValue);
                break;
            case COLLECTIONPERATTRIBUTE:
                doc.append("attrType", attrType)
                        .append("attrValue", attrValue);
                break;
            default:
                // this will never be reached
        } // switch

        LOGGER.debug("Inserting data=" + doc.toString() + " within collection=" + collectionName);
        collection.insertOne(doc);
    } // insertContextDataRaw
    
    /**
     * Inserts a new document in the given raw collection within the given database (column-like mode).
     * changes.
     * @param dbName
     * @param collectionName
     * @param recvTime
     * @param attrs
     * @param mds
     * @throws Exception
     */
    public void insertContextDataRaw(String dbName, String collectionName, String recvTime,
            Map<String, String> attrs, Map<String, String> mds) throws Exception {
        // FIXME: the row-like column insertion mode is not currently available for Mongo
    } // insertContextDataRaw
    
    /**
     * Gets a Mongo database.
     * @param dbName
     * @return
     */
    private MongoDatabase getDatabase(String dbName) {
        // create a ServerAddress object for each configured URI
        List<ServerAddress> servers = new ArrayList<ServerAddress>();
        String[] uris = mongoHosts.split(",");
        
        for (String uri: uris) {
            String[] uriParts = uri.split(":");
            servers.add(new ServerAddress(uriParts[0], new Integer(uriParts[1])));
        } // for
        
        // create a Mongo client
        MongoClient client;
        
        if (mongoUsername.length() != 0) {
            MongoCredential credential = MongoCredential.createCredential(mongoUsername, dbName,
                    mongoPassword.toCharArray());
            client = new MongoClient(servers, Arrays.asList(credential));
        } else {
            client = new MongoClient(servers);
        } // if else
        
        // get the database
        return client.getDatabase(dbName);
    } // getDatabase
    
} // MongoBackend
