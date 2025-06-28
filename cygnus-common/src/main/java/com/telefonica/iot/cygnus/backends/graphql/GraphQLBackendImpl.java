/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.graphql;

import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;

import com.google.gson.JsonElement;

import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonUtils;

import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.Date;


import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

/**
 * The type graphQL backend.
 */
public class GraphQLBackendImpl implements GraphQLBackend{

    private static final CygnusLogger LOGGER = new CygnusLogger(GraphQLBackendImpl.class);
    private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager connectionManager;
    private String graphqlEndpoint;
    private String nlsTimestampFormat;
    private String nlsTimestampTzFormat;

    
    /**
     * Constructor.
     *
     * @param graphqlEndpoint
     * @param maxPoolSize
     * @param maxPoolPerRoute
     * @param graphqlOptions
     */
    public GraphQLBackendImpl(String graphqlEndpoint, int maxPoolSize, int maxPoolPerRoute, String graphqlOptions) {

        graphqlEndpoint = graphqlEndpoint;

        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxPoolSize);
        connectionManager.setDefaultMaxPerRoute(maxPoolPerRoute);

        httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .build();

    } // GraphqlBackendImpl

    /**
     * Releases resources
     */
    public void close(){
        try {
            connectionManager.close(); // Libera recursos
            httpClient.close();
        } catch (Exception e) {
            LOGGER.error("Error cerrando conexiones: " + e.getMessage());
        }
    } // close


    /**
     * Set NLS_TIMESTAMP_FORMAT and NLS_TIMESTAMP_TZ_FORMAT
     *
     * @param format
     **/
    public void setNlsTimestampFormat(String format) {
        this.nlsTimestampFormat = format;
    } // setNlsTimestampFormat

    public String getNlsTimestampFormat() {
        return nlsTimestampFormat;
    } // getNlsTImestampFormat

    public void setNlsTimestampTzFormat(String format) {
        this.nlsTimestampTzFormat = format;
    } // setNlsTimestampTzFormat

    public String getNlsTimestampTzFormat() {
        return nlsTimestampTzFormat;
    } // getNlsTImestampTzFormat


    /**
     * Upsert transaction.
     *
     * @param aggregation     the aggregation
     * @param lastData        the last data
     * @param lastDataDelete  the last data delete
     * @param dataBase        the dataBase
     * @param tableName       the table name
     * @param tableSuffix     the table suffix
     * @param uniqueKey       the unique key
     * @param timestampKey    the timestamp key
     * @param timestampFormat the timestamp format
     * @param attrNativeTypes the attr native types
     * @throws CygnusPersistenceError the cygnus persistence error
     * @throws CygnusBadContextData   the cygnus bad context data
     * @throws CygnusRuntimeError     the cygnus runtime error
     * @throws CygnusPersistenceError the cygnus persistence error
     */
    public void upsertTransaction (LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                   LinkedHashMap<String, ArrayList<JsonElement>> lastData,
                                   LinkedHashMap<String, ArrayList<JsonElement>> lastDataDelete,
                                   String dataBase,
                                   String schema,
                                   String tableName,
                                   String tableSuffix,
                                   String uniqueKey,
                                   String timestampKey,
                                   String timestampFormat,
                                   boolean attrNativeTypes)
        throws CygnusPersistenceError, CygnusBadContextData, CygnusRuntimeError {

        String upsertQuerys = new String();
        String currentUpsertQuery = new String();

        try {

            ArrayList<StringBuffer> upsertQuerysList = GraphQLQueryUtils.graphqlUpsertQuery(aggregation,
                    lastData,
                    lastDataDelete,
                    tableName,
                    tableSuffix,
                    uniqueKey,
                    timestampKey,
                    timestampFormat,
                    dataBase,
                    schema,
                    attrNativeTypes);
            
            // Ordering queries to avoid deadlocks. See issue #2197 for more detail
            upsertQuerysList.sort(Comparator.comparing(buff -> buff.toString()));

            for (StringBuffer query : upsertQuerysList) {
                // TDB: ----- INIT ----- http POST with query
                // PreparedStatement upsertStatement;
                // currentUpsertQuery = query.toString();
                // upsertStatement = connection.prepareStatement(currentUpsertQuery);
                // // FIXME https://github.com/telefonicaid/fiware-cygnus/issues/1959
                // upsertStatement.executeUpdate();
                // TDB: ----- INIT ----- http POST with query


                HttpPost request = new HttpPost(graphqlEndpoint);
                request.setHeader("Content-Type", "application/json");

                try {
                    String payload = query.toString(); //buildGraphQLPayload(batch);
                    request.setEntity(new StringEntity(payload));
                    
                    // Reuse connection from pool automatically
                    CloseableHttpResponse response = httpClient.execute(request);
                    LOGGER.info("Response: " + EntityUtils.toString(response.getEntity()));
                } catch (Exception e) {
                    LOGGER.error("Error sending: " + e.getMessage());
                }

                
                upsertQuerys = upsertQuerys + " " + query;
            }

            LOGGER.info(" Finished transactions into graphQL: " +
                        dataBase + " \n upsertQuerys: " + upsertQuerys);

        // } catch (SQLTimeoutException e) {
        //     if (upsertQuerys.isEmpty() && currentUpsertQuery.isEmpty()) {
        //         throw new CygnusPersistenceError(" " + e.getNextException() +
        //                                          " Data insertion error. database: " + dataBase +
        //                                          " connection: " + connection,
        //                                          " SQLTimeoutException", e.getMessage());
        //     } else {
        //         throw new CygnusPersistenceError(" " + e.getNextException() +
        //                                          " Data insertion error. database: " + dataBase +
        //                                          " upsertQuerys: " + upsertQuerys +
        //                                          " currentUpsertQuery: " + currentUpsertQuery,
        //                                          " SQLTimeoutException", e.getMessage());
        //     }
        // } catch (SQLException e) {
        //     if (upsertQuerys.isEmpty() && currentUpsertQuery.isEmpty()) {
        //         throw new CygnusBadContextData(" " + e.getNextException() +
        //                                        " Data insertion error. database: " + dataBase +
        //                                        " connection: " + connection,
        //                                        " SQLException", e.getMessage());

        //     } else {
        //         String allQueries = " upsertQuerys: " + upsertQuerys +
        //             " currentUpsertQuery: " + currentUpsertQuery;
        //         throw new CygnusBadContextData(" " + e.getNextException() +
        //                                        " Data insertion error. database: " + dataBase + allQueries,
        //                                        " SQLException", e.getMessage());
        //     }
        } finally {
            //closeConnection(connection);
        } // try catch
        // tableName = schema + "." + tableName;

        // LOGGER.debug(" Trying to add '" + dataBase + "' and '" + tableName + "' to the cache after upsertion");
        // cache.addDataBase(dataBase);
        // cache.addTable(dataBase, tableName);
    }


}
