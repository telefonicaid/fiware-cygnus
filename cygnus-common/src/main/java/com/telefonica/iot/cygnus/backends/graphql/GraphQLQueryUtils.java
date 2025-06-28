/**
 * Copyright 2014-2020 Telefonica Investigacion y Desarrollo, S.A.U
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

import com.google.gson.JsonElement;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.backends.sql.SQLQueryUtils;

import java.sql.PreparedStatement;
//import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Arrays;
import java.util.List;


// import org.apache.http.client.methods.HttpPost;
// import org.apache.http.entity.StringEntity;
// import org.apache.http.impl.client.CloseableHttpClient;
// import org.apache.http.impl.client.HttpClients;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;



/**
 * The type Ngsigraphql utils.
 */
public class GraphQLQueryUtils {

    private static final CygnusLogger LOGGER = new CygnusLogger(GraphQLQueryUtils.class);

    public static final String GRAPHQL_FIELDS_MARK = "";
    public static final String SEPARATION_MARK = ",";

    /**
     * Graphql upsert query for Graphql string buffer.
     *
     * @param aggregation     the aggregation
     * @param lastData        the last data
     * @param lastDataDelete  the last data delete
     * @param tableName       the table name
     * @param tableSuffix     the table suffix
     * @param uniqueKey       the unique key
     * @param timestampKey    the timestamp key
     * @param timestampFormat the timestamp format
     * @param schema          the destination
     * @return the string buffer
     */
    protected static ArrayList<StringBuffer> graphqlUpsertQuery(LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                                        LinkedHashMap<String, ArrayList<JsonElement>> lastData,
                                                        LinkedHashMap<String, ArrayList<JsonElement>> lastDataDelete,
                                                        String tableName,
                                                        String tableSuffix,
                                                        String uniqueKey,
                                                        String timestampKey,
                                                        String timestampFormat,
                                                        String dataBase,
                                                        String schema,
                                                        boolean attrNativeTypes) {

        ArrayList<StringBuffer> upsertList = new ArrayList<>();

        LOGGER.debug("[GraphqlQueryUtils.graphqlUpsertQuery] tableName: " + tableName + " tableSuffix " + tableSuffix + " uniqueKey " + uniqueKey + " dataBase " + dataBase + " schema " + schema);
        
        for (int i = 0 ; i < collectionSizeOnLinkedHashMap(lastData) ; i++) {
            StringBuffer query = new StringBuffer();
            // StringBuffer values = new StringBuffer("(");
            // StringBuffer fields = new StringBuffer("(");
            // StringBuffer updateSet = new StringBuffer();
            // String valuesSeparator = "";
            // String fieldsSeparator = "";
            // String updateSetSeparator = "";

            // Convertir atributos NGSI a variables GraphQL
            JsonObject variables = new JsonObject();
            JsonObject input = new JsonObject();

            
            ArrayList<String> keys = new ArrayList<>(aggregation.keySet());
            for (int j = 0 ; j < keys.size() ; j++) {
                // values
                JsonElement value = lastData.get(keys.get(j)).get(i);
                String valueToAppend = value == null ? "null" : SQLQueryUtils.getStringValueFromJsonElement(value, "'", attrNativeTypes);
                LOGGER.debug("[GraphqlQueryUtils.graphqlUpsertQuery] key: " + keys.get(j) + " value: " + value + " valueToAppend " + valueToAppend);
            //     values.append(valuesSeparator).append(valueToAppend);
            //     valuesSeparator = ",";

            //     // fields
            //     fields.append(fieldsSeparator).append(keys.get(j));
            //     fieldsSeparator = ",";

            //     // updateSet
            //     if (!Arrays.asList(uniqueKey.split("\\s*,\\s*")).contains(keys.get(j))) {
            //         updateSet.append(updateSetSeparator).append(keys.get(j)).append("=").append(postgisTempReference).append(".").append(keys.get(j));
            //         updateSetSeparator = ",";
            //     }

                input.addProperty(keys.get(j), valueToAppend);
            }

            variables.add("input", input);

            // TBD: Create mutation for GrpahQL
            String graphqlMutation = String.format(
                "mutation UpdateEntity($input: EntityInput!) { " +
                "  updateEntity(input: $input) { id type } " +
                "}", variables);

            JsonObject payload = new JsonObject();
            payload.addProperty("query", graphqlMutation);
            payload.add("variables", variables);
            
            
            upsertList.add(query);
        }
        // for (int i = 0 ; i < collectionSizeOnLinkedHashMap(lastDataDelete) ; i++) {
        //     StringBuffer query = new StringBuffer();
        //     // TBD
        // }

        
        LOGGER.debug("[GraphqlQueryUtils.graphqlUpsertQuery] Preparing Upsert querys: " + upsertList.toString());
        return upsertList;
    }

    /**
     * Collection size on linked hash map int.
     *
     * @param aggregation the aggregation
     * @return the number of attributes contained on the aggregation object.
     */
    protected static int collectionSizeOnLinkedHashMap(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        ArrayList<ArrayList<JsonElement>> list = new ArrayList<>(aggregation.values());
        if (list.size() > 0)
            return list.get(0).size();
        else
            return 0;
    }

}
