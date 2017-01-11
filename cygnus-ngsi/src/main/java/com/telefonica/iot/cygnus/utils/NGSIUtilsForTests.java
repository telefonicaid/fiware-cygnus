/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.utils;

import org.apache.flume.Context;

/**
 *
 * @author frb
 */
public final class NGSIUtilsForTests {
    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private NGSIUtilsForTests() {
    } // NGSIUtilsForTests
    
    /**
     * Creates a Flume context for Mongo/STH sinks.
     * @param collectionPrefix
     * @param dbPrefix
     * @param dataModel
     * @param enableEncoding
     * @return A Flume context for Mongo/STH sinks.
     */
    public static Context createContextForMongoSTH(String collectionPrefix, String dbPrefix, String dataModel,
            String enableEncoding) {
        Context context = new Context();
        context.put("attr_persistence", "row");
        context.put("batch_size", "100");
        context.put("batch_timeout", "30");
        context.put("batch_ttl", "10");
        context.put("collection_prefix", collectionPrefix);
        context.put("collection_size", "0");
        context.put("data_expiration", "0");
        context.put("data_model", dataModel);
        context.put("db_prefix", dbPrefix);
        context.put("enable_encoding", enableEncoding);
        context.put("enable_grouping", "false");
        context.put("enable_lowercase", "false");
        context.put("max_documents", "0");
        context.put("mongo_hosts", "localhost:27017");
        context.put("mongo_password", "");
        context.put("mongo_username", "");
        return context;
    } // createContextForMongoSTH
    
} // NGSIUtilsForTests
