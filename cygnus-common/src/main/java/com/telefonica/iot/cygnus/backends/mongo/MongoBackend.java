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

import java.util.ArrayList;
import java.util.HashMap;
import org.bson.Document;

/**
 * Interface for those backends implementing the persistence in MongoDB.
 *
 * @author frb
 */

public interface MongoBackend {

    /**
     * Creates a database, given its name, if not exists.
     * @param dbName
     * @throws Exception
     */
    void createDatabase(String dbName) throws Exception;

    /**
     * Creates a collection for STH Comet, given its name, if not exists in the given database. Time-based limits are
     * set, if possible.
     * @param dbName
     * @param collectionName
     * @param dataExpiration
     * @throws Exception
     */
    void createCollection(String dbName, String collectionName, long dataExpiration) throws Exception;

    /**
     * Creates a collection for plain MongoDB, given its name, if not exists in the given database. Size-based limits
     * are set, if possible. Time-based limits are also set, if possible.
     * @param dbName
     * @param collectionName
     * @param collectionsSize
     * @param maxDocuments
     * @param dataExpiration
     * @throws Exception
     */
    void createCollection(String dbName, String collectionName, long collectionsSize, long maxDocuments,
            long dataExpiration) throws Exception;

    /**
     * Inserts a new document in the given raw collection within the given database (row-like mode).
     * @param dbName
     * @param collectionName
     * @param aggregation
     * @throws Exception
     */
    void insertContextDataRaw(String dbName, String collectionName, ArrayList<Document> aggregation) throws Exception;
    
    /**
     * Inserts pre-aggregated numeric context data in the related aggregated collection for the resolutions given.
     * @param dbName
     * @param collectionName
     * @param recvTimeTs
     * @param entityId
     * @param entityType
     * @param attrName
     * @param attrType
     * @param max
     * @param min
     * @param sum
     * @param resolutions
     * @param numSamples
     * @param sum2
     * @throws Exception
     */
    void insertContextDataAggregated(String dbName, String collectionName, long recvTimeTs, String entityId,
            String entityType, String attrName, String attrType, double max, double min, double sum, double sum2,
            int numSamples, boolean[] resolutions)
        throws Exception;
    
    /**
     * Inserts pre-aggregated string-based context data in the related aggregated collection for the resolutions given.
     * @param dbName
     * @param collectionName
     * @param recvTimeTs
     * @param entityId
     * @param entityType
     * @param attrName
     * @param attrType
     * @param counts
     * @param resolutions
     * @throws Exception
     */
    void insertContextDataAggregated(String dbName, String collectionName, long recvTimeTs, String entityId,
            String entityType, String attrName, String attrType, HashMap<String, Integer> counts,
            boolean[] resolutions)
        throws Exception;

} // MongoBackend
