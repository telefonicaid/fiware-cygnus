/**
 * Copyright 2014-2020 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.sql;

import com.google.gson.JsonElement;
import com.telefonica.iot.cygnus.log.CygnusLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * The type Ngsisql utils.
 */
public class SQLQueryUtils {

    private static final CygnusLogger LOGGER = new CygnusLogger(SQLQueryUtils.class);

    private static final String POSTGRES_FIELDS_MARK = "";
    private static final String MYSQL_FIELDS_MARK = "'";
    private static final String SEPARATION_MARK = ",";

    /**
     * Upsert statement prepared statement.
     *
     * @param aggregation     the aggregation
     * @param lastData        the last data
     * @param tableName       the table name
     * @param tableSuffix     the table suffix
     * @param uniqueKey       the unique key
     * @param timestampKey    the timestamp key
     * @param timestampFormat the timestamp format
     * @param sqlInstance     the sql instance
     * @param destination     the destination
     * @param connection      the connection
     * @param attrNativeTypes the attr native types
     * @return the prepared statement
     * @throws SQLException the sql exception
     */
    public static PreparedStatement upsertStatement (LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                            LinkedHashMap<String, ArrayList<JsonElement>> lastData,
                                            String tableName,
                                            String tableSuffix,
                                            String uniqueKey,
                                            String timestampKey,
                                            String timestampFormat,
                                            String sqlInstance,
                                            String destination,
                                            Connection connection,
                                            boolean attrNativeTypes) throws SQLException {


        String query = sqlUpsertQuery(aggregation,
                lastData,
                tableName,
                tableSuffix,
                uniqueKey,
                timestampKey,
                timestampFormat,
                sqlInstance,
                destination).toString();

        PreparedStatement previousStatement = connection.prepareStatement(query);
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = addJsonValues(previousStatement,
                    lastData,
                    attrNativeTypes);
        } catch (SQLException e) {
            LOGGER.error(sqlInstance + " SQLEXCEPTION Error creating upsert statement " + e);
        } catch (Exception e) {
            LOGGER.error(sqlInstance + " GENERICEXCEPTION Error creating upsert statement " + e);
        }
        LOGGER.info("[NGSISQLUtils.upsertStatement] PreparedStatement for upsert created successfully, all batches added. " + query);
        return preparedStatement;

    }

    /**
     * Sql upsert query string buffer.
     *
     * @param aggregation     the aggregation
     * @param lastData        the last data
     * @param tableName       the table name
     * @param tableSuffix     the table suffix
     * @param uniqueKey       the unique key
     * @param timestampKey    the timestamp key
     * @param timestampFormat the timestamp format
     * @param sqlInstance     the sql instance
     * @param destination     the destination
     * @return the string buffer
     */
    protected static StringBuffer sqlUpsertQuery(LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                              LinkedHashMap<String, ArrayList<JsonElement>> lastData,
                                              String tableName,
                                              String tableSuffix,
                                              String uniqueKey,
                                              String timestampKey,
                                              String timestampFormat,
                                              String sqlInstance,
                                              String destination) {

        StringBuffer fieldsForInsert;
        StringBuffer valuesForInsert = sqlQuestionValues(lastData.keySet());
        StringBuffer updateSet = new StringBuffer();
        StringBuffer postgisTempReference = new StringBuffer("EXCLUDED");
        StringBuffer postgisDestination = new StringBuffer(destination).append(".").append(tableName).append(tableSuffix);
        StringBuffer query = new StringBuffer();
        boolean first = true;

        for (String key : lastData.keySet()) {
            if (!key.equals(uniqueKey) && first) {
                updateSet.append(key).append("=").append(postgisTempReference).append(".").append(key);
                first = false;
            } else if (!key.equals(uniqueKey)) {
                updateSet.append(", ").append(key).append("=").append(postgisTempReference).append(".").append(key);
            }
        }

        if (sqlInstance.equals("postgresql")) {
            fieldsForInsert = getFieldsForInsert(lastData.keySet(), POSTGRES_FIELDS_MARK);
            query.append("INSERT INTO ").append(postgisDestination).append(" ").append(fieldsForInsert).append(" ").
                    append("VALUES ").append(valuesForInsert).append(" ").
                    append("ON CONFLICT ").append("(").append(uniqueKey).append(") ").
                    append("DO ").
                    append("UPDATE SET ").append(updateSet).append(" ").
                    append("WHERE ").append(postgisDestination).append(".").append(uniqueKey).append("=").append(postgisTempReference).append(".").append(uniqueKey).append(" ").
                    append("AND ").append("to_timestamp(").append(postgisDestination).append(".").append(timestampKey).append(", '").append(timestampFormat).append("') ").
                    append("< ").append("to_timestamp(").append(postgisTempReference).append(".").append(timestampKey).append(", '").append(timestampFormat).append("')");
        }
        LOGGER.debug("[NGSISQLUtils.sqlUpsertQuery] Preparing Upsert query: " + query.toString());
        return query;
    }

    /**
     * Sql question values string buffer.
     *
     * @param keyList the key list
     * @return the string buffer
     */
    public static StringBuffer sqlQuestionValues(Set<String> keyList) {
        StringBuffer questionValues = new StringBuffer("(");
        boolean first = true;
        for (String key : keyList) {
            if (first) {
                questionValues.append("?");
                first = false;
            } else {
                questionValues.append(", ?");
            }
        }
        questionValues.append(")");
        LOGGER.debug("[NGSISQLUtils.sqlQuestionValues] Preparing question marks for statement query: " + questionValues.toString());
        return questionValues;
    }

    /**
     * Gets fields for insert.
     *
     * @param keyList   the key list
     * @param fieldMark the field mark
     * @return the fields for insert
     */
    public static StringBuffer getFieldsForInsert(Set<String> keyList, String fieldMark) {
        StringBuffer fieldsForInsert = new StringBuffer("(");
        boolean first = true;
        Iterator<String> it = keyList.iterator();
        while (it.hasNext()) {
            if (first) {
                fieldsForInsert.append(fieldMark).append(it.next()).append(fieldMark);
                first = false;
            } else {
                fieldsForInsert.append(SEPARATION_MARK).append(fieldMark).append(it.next()).append(fieldMark);
            } // if else
        } // while
        fieldsForInsert.append(")");
        LOGGER.debug("[NGSISQLUtils.getFieldsForInsert] Preparing fields for insert for statement: " + fieldsForInsert.toString());
        return fieldsForInsert;
    } // getFieldsForInsert

    /**
     * Add json values prepared statement.
     *
     * @param previousStatement the previous statement
     * @param aggregation       the aggregation
     * @param attrNativeTypes   the attr native types
     * @return the prepared statement
     * @throws SQLException the sql exception
     */
    protected static PreparedStatement addJsonValues (PreparedStatement previousStatement,
                                                   LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                                   boolean attrNativeTypes) throws SQLException {

        PreparedStatement preparedStatement = previousStatement;
        int numEvents = collectionSizeOnLinkedHashMap(aggregation);
        for (int i = 0; i < numEvents; i++) {
            Iterator<String> it = aggregation.keySet().iterator();
            int position = 1;
            while (it.hasNext()) {
                String entry = it.next();
                ArrayList<JsonElement> values = aggregation.get(entry);
                JsonElement value = values.get(i);
                if (attrNativeTypes) {
                    if (value == null || value.isJsonNull()) {
                        preparedStatement.setString(position, "NULL");
                        LOGGER.debug("[NGSISQLUtils.addJsonValues] " + "Added NULL as String");
                    } else {
                        if (value.isJsonPrimitive()) {
                            if (value.getAsJsonPrimitive().isNumber()) {
                                preparedStatement.setDouble(position, value.getAsDouble());
                                LOGGER.debug("[NGSISQLUtils.addJsonValues] " + "Added " + value.getAsDouble() + " as Number");
                                position++;
                            } else if (value.getAsJsonPrimitive().isBoolean()) {
                                preparedStatement.setBoolean(position, value.getAsBoolean());
                                LOGGER.debug("[NGSISQLUtils.addJsonValues] " + "Added " + value.getAsBoolean() + " as Boolean");
                                position++;
                            } else {
                                String stringValue = value.getAsString();
                                if (stringValue.contains("ST_GeomFromGeoJSON") || stringValue.contains("ST_SetSRID")) {
                                    preparedStatement.setObject(position, stringValue);
                                    LOGGER.debug("[NGSISQLUtils.addJsonValues] " + "Added postgis Function " + stringValue + " as Object");
                                    position++;
                                } else {
                                    preparedStatement.setString(position, stringValue);
                                    LOGGER.debug("[NGSISQLUtils.addJsonValues] " + "Added " + stringValue + " as String");
                                    position++;
                                }
                            } // else
                        } else { // if (value.isJsonPrimitive())
                            preparedStatement.setString(position, value.toString());
                            LOGGER.debug("[NGSISQLUtils.addJsonValues] " + "Added " + value.toString() + " as String");
                            position++;
                        } // else
                    } // else
                } else { //if (attrNativeTypes)
                    if (value != null && value.isJsonPrimitive()) {
                        String stringValue = value.getAsString();
                        if (stringValue.contains("ST_GeomFromGeoJSON") || stringValue.contains("ST_SetSRID")) {
                            preparedStatement.setObject(position, stringValue);
                            LOGGER.debug("[NGSISQLUtils.addJsonValues] " + "Added postgis Function " + stringValue + " as Object");
                            position++;
                        } else {
                            preparedStatement.setString(position, stringValue);
                            LOGGER.debug("[NGSISQLUtils.addJsonValues] " + "Added " + stringValue + " as String");
                            position++;
                        }
                    } else {
                        if (value == null){
                            preparedStatement.setString(position, "NULL");
                            LOGGER.debug("[NGSISQLUtils.addJsonValues] " + "Added NULL as String");
                            position++;
                        } else {
                            preparedStatement.setString(position, value.toString());
                            LOGGER.debug("[NGSISQLUtils.addJsonValues] " + "Added " + value.toString() + " as String");
                            position++;
                        }
                    }
                }
            } // while
            preparedStatement.addBatch();
            LOGGER.debug("[NGSISQLUtils.addJsonValues] Batch added");
        } // for
        return preparedStatement;
    }

    /**
     * Collection size on linked hash map int.
     *
     * @param aggregation the aggregation
     * @return the number of attributes contained on the aggregation object.
     */
    private static int collectionSizeOnLinkedHashMap(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        ArrayList<ArrayList<JsonElement>> list = new ArrayList<>(aggregation.values());
        return list.get(0).size();
    }

}
