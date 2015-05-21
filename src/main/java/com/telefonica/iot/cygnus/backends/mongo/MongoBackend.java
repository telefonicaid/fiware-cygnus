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

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.telefonica.iot.cygnus.backends.mysql.MySQLBackend;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.OrionMongoBaseSink.DataModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.bson.Document;

/**
 *
 * @author frb
 */
public class MongoBackend {
    
    /**
     * Available resolutions for aggregated data.
     */
    public enum Resolution { SECOND, MINUTE, HOUR, DAY, MONTH }
    
    private MongoClient client;
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
        client = null;
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

        if (db.getCollection(collectionName) == null || db.getCollection(collectionName).count() == 0) {
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
            String entityId, String entityType, String attrName, String attrType, String attrValue, String attrMd)
        throws Exception {
        MongoDatabase db = getDatabase(dbName);
        MongoCollection collection = db.getCollection(collectionName);
        Document doc = new Document("recvTime", new Date(recvTimeTs * 1000));
        
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
     * Inserts a new document in the given aggregated collection within the given database (row-like mode).
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
    public void insertContextDataAggregated(String dbName, String collectionName, long recvTimeTs, String recvTime,
            String entityId, String entityType, String attrName, String attrType, String attrValue, String attrMd)
        throws Exception {
        // preprocess some values
        double value = new Double(attrValue);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(recvTimeTs * 1000);
        
        // insert the data in an aggregated fashion for each resolution type
        insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                attrName, attrType, value, Resolution.SECOND);
        insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                attrName, attrType, value, Resolution.MINUTE);
        insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                attrName, attrType, value, Resolution.HOUR);
        insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                attrName, attrType, value, Resolution.DAY);
        insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                attrName, attrType, value, Resolution.MONTH);
    } // insertContextDataAggregated
        
    /**
     * Inserts a new document with given resolution in the given aggregated collection within the given database
     * (row-like mode).
     * @param dbName
     * @param collectionName
     * @param recvTimeTs
     * @param entityId
     * @param entityType
     * @param attrName
     * @param attrType
     * @param attrValue
     * @param resolution
     */
    private void insertContextDataAggregatedForResoultion(String dbName, String collectionName, GregorianCalendar calendar,
            String entityId, String entityType, String attrName, String attrType, double attrValue,
            Resolution resolution) {
        // get database and collection
        MongoDatabase db = getDatabase(dbName);
        MongoCollection collection = db.getCollection(collectionName);
        
        // build the query
        BasicDBObject query = buildQuery(calendar, entityId, entityType, attrName, resolution);
        
        // prepopulate if needed
        BasicDBObject insert = buildInsertForPrepopulate(attrType, resolution);
        UpdateResult res = collection.updateOne(query, insert, new UpdateOptions().upsert(true));
        
        if (res.getMatchedCount() == 0) {
            LOGGER.debug("Prepopulating data within collection=" + collectionName + ", query=" + query.toString()
                + ", insert=" + insert.toString());
        } // if

        // do the update
        BasicDBObject update = buildUpdateForUpdate(attrType, attrValue);
        LOGGER.debug("Updating data within collection=" + collectionName + ", query=" + query.toString()
                + ", update=" + update.toString());
        collection.updateOne(query, update);
    } // insertContextDataAggregated

    /**
     * Builds the Json query used both to prepopulate and update an aggregated collection.
     * @param recvTimeTs
     * @param entityId
     * @param entityType
     * @param attrName
     * @param attrType
     * @param resolution
     * @return
     */
    private BasicDBObject buildQuery(GregorianCalendar calendar, String entityId, String entityType, String attrName,
            Resolution resolution) {
        int offset = 0;
        
        switch (resolution) {
            case SECOND:
                offset = calendar.get(Calendar.SECOND);
                break;
            case MINUTE:
                offset = calendar.get(Calendar.MINUTE);
                break;
            case HOUR:
                offset = calendar.get(Calendar.HOUR_OF_DAY);
                break;
            case DAY:
                offset = calendar.get(Calendar.DAY_OF_MONTH);
                break;
            case MONTH:
                offset = calendar.get(Calendar.MONTH) + 1;
                break;
            default:
                // this should never be reached;
        } // switch
        
        BasicDBObject query = new BasicDBObject();
                
        switch (dataModel) {
            case COLLECTIONPERSERVICEPATH:
                query.append("_id", new BasicDBObject("entityId", entityId)
                            .append("entityType", entityType)
                            .append("attrName", attrName)
                            .append("origin", getOrigin(calendar, resolution))
                            .append("resolution", resolution.toString().toLowerCase())
                            .append("range", getRange(resolution)))
                        .append("points.offset", offset);
                break;
            case COLLECTIONPERENTITY:
                query.append("_id", new BasicDBObject("attrName", attrName)
                            .append("origin", getOrigin(calendar, resolution))
                            .append("resolution", resolution.toString().toLowerCase())
                            .append("range", getRange(resolution)))
                        .append("points.offset", offset);
                break;
            case COLLECTIONPERATTRIBUTE:
                query.append("_id", new BasicDBObject("origin", getOrigin(calendar, resolution))
                            .append("resolution", resolution.toString().toLowerCase())
                            .append("range", getRange(resolution)))
                        .append("points.offset", offset);
                break;
            default:
                // this will never be reached
        } // switch
        
        return query;
    } // buildQuery
    
    /**
     * Builds the Json update used when updating an aggregated collection.
     * @param attrType
     * @param attrValue
     * @return
     */
    private BasicDBObject buildUpdateForUpdate(String attrType, double attrValue) {
        BasicDBObject update = new BasicDBObject();
        update.append("$set", new BasicDBObject("attrType", attrType))
                .append("$inc", new BasicDBObject("points.$.samples", 1)
                        .append("points.$.sum", attrValue)
                        .append("points.$.sum2", Math.pow(attrValue, 2)))
                .append("$min", new BasicDBObject("points.$.min", attrValue))
                .append("$max", new BasicDBObject("points.$.max", attrValue));
        return update;
    } // buildUpdateForUpdate
    
    /**
     * Builds the Json used to prepopulate an aggregated collection.
     * @param attrType
     * @param resolution
     * @return
     */
    private BasicDBObject buildInsertForPrepopulate(String attrType, Resolution resolution) {
        BasicDBObject update = new BasicDBObject();
        update.append("$setOnInsert", new BasicDBObject("attrType", attrType)
                .append("points", buildPrepopulatedPoints(resolution)));
        return update;
    } // buildInsertForPrepopulate
    
    /**
     * Builds the points part for the Json used to prepopulate.
     * @param resolution
     */
    private BasicDBList buildPrepopulatedPoints(Resolution resolution) {
        BasicDBList prepopulatedData = new BasicDBList();
        int offsetOrigin = 0;
        int numValues = 0;
        
        switch (resolution) {
            case SECOND:
                numValues = 60;
                break;
            case MINUTE:
                numValues = 60;
                break;
            case HOUR:
                numValues = 24;
                break;
            case DAY:
                numValues = 32;
                offsetOrigin = 1;
                break;
            case MONTH:
                numValues = 13;
                offsetOrigin = 1;
                break;
            default:
                // should never be reached
        } // switch
        
        for (int i = offsetOrigin; i < numValues; i++) {
            prepopulatedData.add(new BasicDBObject("offset", i)
                    .append("samples", 0)
                    .append("sum", 0)
                    .append("sum2", 0)
                    .append("min", Double.POSITIVE_INFINITY)
                    .append("max", Double.NEGATIVE_INFINITY));
        } // for
        
        return prepopulatedData;
    } // buildPrepopulatedPoints
    
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
        
        if (client == null) {
            if (mongoUsername.length() != 0) {
                MongoCredential credential = MongoCredential.createCredential(mongoUsername, dbName,
                        mongoPassword.toCharArray());
                client = new MongoClient(servers, Arrays.asList(credential));
            } else {
                client = new MongoClient(servers);
            } // if else
        } // if
        
        // get the database
        return client.getDatabase(dbName);
    } // getDatabase
    
    /**
     * Gets the range related to a resolution.
     * @param resolution
     * @return
     */
    private String getRange(Resolution resolution) {
        switch(resolution) {
            case SECOND:
                return "minute";
            case MINUTE:
                return "hour";
            case HOUR:
                return "day";
            case DAY:
                return "month";
            case MONTH:
                return "year";
            default:
                return null; // this should never be returned
        } // switch
    } // getDataModel
    
    /**
     * Gets the origin date for the aggregated collection.
     * @param calendar
     * @param resolution
     * @return
     */
    private Date getOrigin(GregorianCalendar calendar, Resolution resolution) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second;
        
        switch (resolution) {
            case MONTH:
                month = 0;
                // falls through
            case DAY:
                day = 1;
                // falls through
            case HOUR:
                hour = 0;
                // falls through
            case MINUTE:
                minute = 0;
                // falls through
            case SECOND:
                second = 0;
                break;
            default:
                // should never be reached
                return null;
        } // switch
        
        GregorianCalendar gc = new GregorianCalendar(year, month, day, hour, minute, second);
        gc.setTimeZone(TimeZone.getTimeZone("UTC"));
        return new Date(gc.getTimeInMillis());
    } // getOrigin
    
} // MongoBackend
