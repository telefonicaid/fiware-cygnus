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
package com.telefonica.iot.cygnus.backends.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.Enums.DataModel;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYATTRIBUTE;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYENTITY;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYSERVICEPATH;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.bson.Document;

/**
 *
 * @author frb
 * @author xdelox
 */
public class MongoBackendImpl implements MongoBackend {

    /**
     * Available resolutions for aggregated data.
     */
    public enum Resolution { SECOND, MINUTE, HOUR, DAY, MONTH }

    private MongoClient client;
    private final String mongoHosts;
    private final String mongoUsername;
    private final String mongoPassword;
    private final DataModel dataModel;
    private static final CygnusLogger LOGGER = new CygnusLogger(MongoBackendImpl.class);

    /**
     * Constructor.
     * @param mongoHosts
     * @param mongoUsername
     * @param mongoPassword
     * @param dataModel
     */
    public MongoBackendImpl(String mongoHosts, String mongoUsername, String mongoPassword,
            DataModel dataModel) {
        client = null;
        this.mongoHosts = mongoHosts;
        this.mongoUsername = mongoUsername;
        this.mongoPassword = mongoPassword;
        this.dataModel = dataModel;
    } // MongoBackendImpl

    /**
     * Creates a database, given its name, if not exists.
     * @param dbName
     * @throws Exception
     */
    @Override
    public void createDatabase(String dbName) throws Exception {
        LOGGER.debug("Creating Mongo database=" + dbName);
        getDatabase(dbName); // getting a non existent database automatically creates it
    } // createDatabase

    /**
     * Creates a collection for STH Comet, given its name, if not exists in the given database. Time-based limits are set,
     * if possible.
     * @param dbName
     * @param collectionName
     * @param dataExpiration
     * @throws Exception
     */
    @Override
    public void createCollection(String dbName, String collectionName, long dataExpiration) throws MongoException {
        LOGGER.debug("Creating Mongo collection=" + collectionName + " at database=" + dbName);
        MongoDatabase db = getDatabase(dbName);

        // create the collection
        try {
            db.createCollection(collectionName);
        } catch (MongoException e) {
            ErrorCategory errorCategory = ErrorCategory.fromErrorCode( e.getCode() );
            if (errorCategory == ErrorCategory.fromErrorCode(48)){
                LOGGER.debug("Collection already exists, nothing to create");
            } else {
                throw e;
            } // if else
        } // try catch

        // check STH indexes documentation at https://github.com/telefonicaid/fiware-sth-comet/blob/master/doc/manuals/db_indexes.md
        BasicDBObject keys;
        IndexOptions options;
        try {
            keys = new BasicDBObject()
                .append("_id.entityId", 1)
                .append("_id.entityType", 1)
                .append("_id.attrName", 1)
                .append("_id.resolution", 1)
                .append("_id.origin", 1);
            options = new IndexOptions().name("cyg_agg_opt");
            db.getCollection(collectionName).createIndex(keys, options);
        } catch (Exception e) {
            LOGGER.warn("Error in collection " + collectionName + " creating index ex=" + e.getMessage());
        } // try catch
        try {
            if (dataExpiration != 0) {
                keys = new BasicDBObject().append("_id.origin", 1);
                options = new IndexOptions().name("cyg_agg_exp").expireAfter(dataExpiration, TimeUnit.SECONDS);
                db.getCollection(collectionName).createIndex(keys, options);
            } // if
        } catch (Exception e) {
            LOGGER.warn("Error in collection " + collectionName + " creating index ex=" + e.getMessage());
        } // try catch
    } // createCollection

    /**
     * Creates a collection for plain MongoDB, given its name, if not exists in the given database. Size-based limits
     * are set, if possible. Time-based limits are also set, if possible.
     * @param dbName
     * @param collectionName
     * @param collectionsSize
     * @param maxDocuments
     * @throws Exception
     */
    @Override
    public void createCollection(String dbName, String collectionName, long collectionsSize, long maxDocuments,
            long dataExpiration) throws MongoException {
        MongoDatabase db = getDatabase(dbName);

        // create the collection, with size-based limits if possible
        try {
            if (collectionsSize != 0 && maxDocuments != 0) {
                CreateCollectionOptions options = new CreateCollectionOptions()
                        .capped(true)
                        .sizeInBytes(collectionsSize)
                        .maxDocuments(maxDocuments);
                LOGGER.debug("Creating Mongo collection=" + collectionName + " at database=" + dbName + " with "
                        + "collections_size=" + collectionsSize + " and max_documents=" + maxDocuments + " options");
                db.createCollection(collectionName, options);
            } else {
                LOGGER.debug("Creating Mongo collection=" + collectionName + " at database=" + dbName);
                db.createCollection(collectionName);
            } // if else
        } catch (MongoException e) {
            ErrorCategory errorCategory = ErrorCategory.fromErrorCode( e.getCode() );
            if (errorCategory == ErrorCategory.fromErrorCode(48)){
                LOGGER.debug("Collection already exists, nothing to create");
            } else {
                throw e;
            } // if else
        } // try catch

        // check STH indexes documentation at https://github.com/telefonicaid/fiware-sth-comet/blob/master/doc/manuals/db_indexes.md
        BasicDBObject keys;
        IndexOptions options;
        try {
            keys = new BasicDBObject()
                .append("entityId", 1)
                .append("entityType", 1)
                .append("attrName", 1)
                .append("recvTime", 1);
            options = new IndexOptions().name("cyg_raw_opt");
            db.getCollection(collectionName).createIndex(keys, options);
        } catch (Exception e) {
            LOGGER.warn("Error in collection " + collectionName + " creating index ex=" + e.getMessage());
        } // try catch
        try {
            if (dataExpiration != 0) {
                keys = new BasicDBObject().append("recvTime", 1);
                options = new IndexOptions().name("cyg_raw_exp").expireAfter(dataExpiration, TimeUnit.SECONDS);
                db.getCollection(collectionName).createIndex(keys, options);
            } // if
        } catch (Exception e) {
            LOGGER.warn("Error in collection " + collectionName + " creating index ex=" + e.getMessage());
        } // try catch
    } // createCollection

    /**
     * Inserts a new document in the given raw collection within the given database (row-like mode).
     * @param dbName
     * @param collectionName
     * @param aggregation
     * @throws Exception
     */
    @Override
    public void insertContextDataRaw(String dbName, String collectionName, ArrayList<Document> aggregation)
        throws Exception {
        MongoDatabase db = getDatabase(dbName);
        MongoCollection collection = db.getCollection(collectionName);
        collection.insertMany(aggregation);
    } // insertContextDataRaw
    
    @Override
    public void insertContextDataAggregated(String dbName, String collectionName, long recvTimeTs, String entityId,
            String entityType, String attrName, String attrType, double max, double min, double sum, double sum2,
            int numSamples, boolean[] resolutions) throws Exception {
        // Preprocess some values
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(recvTimeTs);

        // Insert the data in an aggregated fashion for each resolution type
        if (resolutions[0]) {
            insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                    attrName, attrType, max, min, sum, sum2, numSamples, Resolution.SECOND);
        } // if

        if (resolutions[1]) {
            insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                    attrName, attrType, max, min, sum, sum2, numSamples, Resolution.MINUTE);
        } // if

        if (resolutions[2]) {
            insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                    attrName, attrType, max, min, sum, sum2, numSamples, Resolution.HOUR);
        } // if

        if (resolutions[3]) {
            insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                    attrName, attrType, max, min, sum, sum2, numSamples, Resolution.DAY);
        } // if

        if (resolutions[4]) {
            insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                    attrName, attrType, max, min, sum, sum2, numSamples, Resolution.MONTH);
        } // if
    } // insertContextDataAggregated
    
    @Override
    public void insertContextDataAggregated(String dbName, String collectionName, long recvTimeTs, String entityId,
            String entityType, String attrName, String attrType, HashMap<String, Integer> counts,
            boolean[] resolutions) throws Exception {
        // Preprocess some values
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(recvTimeTs);

        // Insert the data in an aggregated fashion for each resolution type
        if (resolutions[0]) {
            insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                    attrName, attrType, counts, Resolution.SECOND);
        } // if

        if (resolutions[1]) {
            insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                    attrName, attrType, counts, Resolution.MINUTE);
        } // if

        if (resolutions[2]) {
            insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                    attrName, attrType, counts, Resolution.HOUR);
        } // if

        if (resolutions[3]) {
            insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                    attrName, attrType, counts, Resolution.DAY);
        } // if

        if (resolutions[4]) {
            insertContextDataAggregatedForResoultion(dbName, collectionName, calendar, entityId, entityType,
                    attrName, attrType, counts, Resolution.MONTH);
        } // if
    } // insertContextDataAggregated
    
    private void insertContextDataAggregatedForResoultion(String dbName, String collectionName,
            GregorianCalendar calendar, String entityId, String entityType, String attrName, String attrType,
            double max, double min, double sum, double sum2, int numSamples, Resolution resolution) {
        // Get database and collection
        MongoDatabase db = getDatabase(dbName);
        MongoCollection collection = db.getCollection(collectionName);

        // Build the query
        BasicDBObject query = buildQueryForInsertAggregated(calendar, entityId, entityType, attrName, resolution);

        // Prepopulate if needed
        BasicDBObject insert = buildInsertForPrepopulate(attrType, resolution, true);
        UpdateResult res = collection.updateOne(query, insert, new UpdateOptions().upsert(true));

        if (res.getMatchedCount() == 0) {
            LOGGER.debug("Prepopulating data, database=" + dbName + ", collection=" + collectionName + ", query="
                    + query.toString() + ", insert=" + insert.toString());
        } // if

        // Do the update
        BasicDBObject update = buildUpdateForUpdate(attrType, calendar, max, min, sum, sum2, numSamples);
        LOGGER.debug("Updating data, database=" + dbName + ", collection=" + collectionName + ", query="
                + query.toString() + ", update=" + update.toString());
        collection.updateOne(query, update);
    } // insertContextDataAggregated
    
    private void insertContextDataAggregatedForResoultion(String dbName, String collectionName,
            GregorianCalendar calendar, String entityId, String entityType, String attrName, String attrType,
            HashMap<String, Integer> counts, Resolution resolution) {
        // Get database and collection
        MongoDatabase db = getDatabase(dbName);
        MongoCollection collection = db.getCollection(collectionName);

        // Build the query
        BasicDBObject query = buildQueryForInsertAggregated(calendar, entityId, entityType, attrName, resolution);

        // Prepopulate if needed
        BasicDBObject insert = buildInsertForPrepopulate(attrType, resolution, false);
        UpdateResult res = collection.updateOne(query, insert, new UpdateOptions().upsert(true));

        if (res.getMatchedCount() == 0) {
            LOGGER.debug("Prepopulating data, database=" + dbName + ", collection=" + collectionName + ", query="
                    + query.toString() + ", insert=" + insert.toString());
        } // if

        // Do the update
        for (String key : counts.keySet()) {
            int count = counts.get(key);
            BasicDBObject update = buildUpdateForUpdate(attrType, resolution, calendar, key, count);
            LOGGER.debug("Updating data, database=" + dbName + ", collection=" + collectionName + ", query="
                    + query.toString() + ", update=" + update.toString());
            collection.updateOne(query, update);
        } // for
    } // insertContextDataAggregated

    /**
     * Builds the Json query used both to prepopulate and update an aggregated collection. It is protected for testing
     * purposes.
     * @param calendar
     * @param entityId
     * @param entityType
     * @param attrName
     * @param resolution
     * @return
     */
    protected BasicDBObject buildQueryForInsertAggregated(GregorianCalendar calendar, String entityId, String entityType,
            String attrName, Resolution resolution) {
        int offset = getOffset(calendar, resolution);
        BasicDBObject query = new BasicDBObject();

        switch (dataModel) {
            case DMBYSERVICEPATH:
                query.append("_id", new BasicDBObject("entityId", entityId)
                            .append("entityType", entityType)
                            .append("attrName", attrName)
                            .append("origin", getOrigin(calendar, resolution))
                            .append("resolution", resolution.toString().toLowerCase())
                            .append("range", getRange(resolution)))
                        .append("points.offset", offset);
                break;
            case DMBYENTITY:
                query.append("_id", new BasicDBObject("attrName", attrName)
                            .append("origin", getOrigin(calendar, resolution))
                            .append("resolution", resolution.toString().toLowerCase())
                            .append("range", getRange(resolution)))
                        .append("points.offset", offset);
                break;
            case DMBYATTRIBUTE:
                query.append("_id", new BasicDBObject("origin", getOrigin(calendar, resolution))
                            .append("resolution", resolution.toString().toLowerCase())
                            .append("range", getRange(resolution)))
                        .append("points.offset", offset);
                break;
            default:
                // this will never be reached
        } // switch

        return query;
    } // buildQueryForInsertAggregated

    /**
     * Builds the Json to be inserted as prepopulation. It is protected for testing purposes.
     * @param attrType
     * @param resolution
     * @param isANumber
     * @return 
     */
    protected BasicDBObject buildInsertForPrepopulate(String attrType, Resolution resolution, boolean isANumber) {
        BasicDBObject update = new BasicDBObject();
        update.append("$setOnInsert", new BasicDBObject("attrType", attrType)
                .append("points", buildPrepopulatedPoints(resolution, isANumber)));
        return update;
    } // buildInsertForPrepopulate
    
    /**
     * Builds the points part for the Json used to prepopulate.
     * @param resolution
     */
    private BasicDBList buildPrepopulatedPoints(Resolution resolution, boolean isANumber) {
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

        if (isANumber) {
            for (int i = offsetOrigin; i < numValues; i++) {
                prepopulatedData.add(new BasicDBObject("offset", i)
                        .append("samples", 0)
                        .append("sum", 0)
                        .append("sum2", 0)
                        .append("min", Double.POSITIVE_INFINITY)
                        .append("max", Double.NEGATIVE_INFINITY));
            } // for
        } else {
            for (int i = offsetOrigin; i < numValues; i++) {
                prepopulatedData.add(new BasicDBObject("offset", i)
                        .append("samples", 0)
                        .append("occur", new BasicDBObject()));
            } // for
        } // if else

        return prepopulatedData;
    } // buildPrepopulatedPoints
    
    protected BasicDBObject buildUpdateForUpdate(String attrType, GregorianCalendar calendar,
            double max, double min, double sum, double sum2, int numSamples) {
        BasicDBObject update = new BasicDBObject();
        return update.append("$set", new BasicDBObject("attrType", attrType))
                .append("$inc", new BasicDBObject("points.$.samples", numSamples)
                        .append("points.$.sum", sum)
                        .append("points.$.sum2", sum2))
                .append("$min", new BasicDBObject("points.$.min", min))
                .append("$max", new BasicDBObject("points.$.max", max));
    } // buildUpdateForUpdate

    protected BasicDBObject buildUpdateForUpdate(String attrType, Resolution resolution, GregorianCalendar calendar,
            String value, int numSamples) {
        BasicDBObject update = new BasicDBObject();
        int offset = getOffset(calendar, resolution);
        int modifiedOffset = offset - (resolution == Resolution.DAY || resolution == Resolution.MONTH ? 1 : 0);
        update.append("$set", new BasicDBObject("attrType", attrType))
                .append("$inc", new BasicDBObject("points." + modifiedOffset + ".samples", numSamples)
                        .append("points." + modifiedOffset + ".occur." + value, numSamples));
        return update;
    } // buildUpdateForUpdate

    /**
     * Gets a Mongo database.
     * @param dbName
     * @return
     */
    private MongoDatabase getDatabase(String dbName) {
        // create a ServerAddress object for each configured URI
        List<ServerAddress> servers = new ArrayList<>();
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
     * Given a resolution, gets the range. It is protected for testing purposes.
     * @param resolution
     * @return
     */
    protected String getRange(Resolution resolution) {
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
     * Given a calendar and a resolution, gets the origin. It is protected for testing purposes.
     * @param calendar
     * @param resolution
     * @return
     */
    protected Date getOrigin(GregorianCalendar calendar, Resolution resolution) {
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

    /**
     * Given a calendar and a resolution, gets the offset. It is protected for testing purposes.
     * @param calendar
     * @param resolution
     * @return 
     */
    protected int getOffset(GregorianCalendar calendar, Resolution resolution) {
        int offset;

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
                // This offset has to be modified since Java data classes enum months starting by 0 (January)
                offset = calendar.get(Calendar.MONTH) + 1;
                break;
            default:
                // should never be reached
                offset = 0;
        } // switch

        return offset;
    } // getOffset

} // MongoBackendImpl
