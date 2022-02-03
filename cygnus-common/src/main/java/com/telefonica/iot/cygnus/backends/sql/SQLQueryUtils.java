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
import com.telefonica.iot.cygnus.backends.sql.Enum.SQLInstance;
import org.apache.commons.lang.StringEscapeUtils;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Arrays;

/**
 * The type Ngsisql utils.
 */
public class SQLQueryUtils {

    private static final CygnusLogger LOGGER = new CygnusLogger(SQLQueryUtils.class);

    public static final String POSTGRES_FIELDS_MARK = "";
    public static final String MYSQL_FIELDS_MARK = "`";
    public static final String SEPARATION_MARK = ",";

    /**
     * Sql upsert string buffer.
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
    protected static ArrayList<StringBuffer> sqlUpsertQuery(LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                                 LinkedHashMap<String, ArrayList<JsonElement>> lastData,
                                                 String tableName,
                                                 String tableSuffix,
                                                 String uniqueKey,
                                                 String timestampKey,
                                                 String timestampFormat,
                                                 SQLInstance sqlInstance,
                                                 String dataBase,
                                                 String schema,
                                                 boolean attrNativeTypes) {

    if (sqlInstance == SQLInstance.POSTGRESQL){
            return postgreSqlUpsertQuery(aggregation,
                    lastData,
                    tableName,
                    tableSuffix,
                    uniqueKey,
                    timestampKey,
                    timestampFormat,
                    sqlInstance,
                    schema,
                    attrNativeTypes);
        } else if (sqlInstance == SQLInstance.MYSQL) {
            return mySqlUpsertQuery(aggregation,
                    lastData,
                    tableName,
                    tableSuffix,
                    uniqueKey,
                    timestampKey,
                    timestampFormat,
                    sqlInstance,
                    dataBase,
                    attrNativeTypes);
        }
        return null;
    }

    /**
     * Sql upsert query for PostgresSQL string buffer.
     *
     * @param aggregation     the aggregation
     * @param lastData        the last data
     * @param tableName       the table name
     * @param tableSuffix     the table suffix
     * @param uniqueKey       the unique key
     * @param timestampKey    the timestamp key
     * @param timestampFormat the timestamp format
     * @param sqlInstance     the sql instance
     * @param schema          the destination
     * @return the string buffer
     */
    protected static ArrayList<StringBuffer> postgreSqlUpsertQuery(LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                                        LinkedHashMap<String, ArrayList<JsonElement>> lastData,
                                                        String tableName,
                                                        String tableSuffix,
                                                        String uniqueKey,
                                                        String timestampKey,
                                                        String timestampFormat,
                                                        SQLInstance sqlInstance,
                                                        String schema,
                                                        boolean attrNativeTypes) {

        ArrayList<StringBuffer> upsertList = new ArrayList<>();
        StringBuffer postgisTempReference = new StringBuffer("EXCLUDED");
        StringBuffer postgisDestination = new StringBuffer(schema).append(".").append(tableName).append(tableSuffix);

        for (int i = 0 ; i < collectionSizeOnLinkedHashMap(lastData) ; i++) {
            StringBuffer query = new StringBuffer();
            StringBuffer values = new StringBuffer("(");
            StringBuffer fields = new StringBuffer("(");
            StringBuffer updateSet = new StringBuffer();
            ArrayList<String> keys = new ArrayList<>(aggregation.keySet());
            for (int j = 0 ; j < keys.size() ; j++) {
                if (lastData.get(keys.get(j)).get(i) != null) {
                    JsonElement value = lastData.get(keys.get(j)).get(i);
                    if (j == 0) {
                        values.append(getStringValueFromJsonElement(value, "'", attrNativeTypes));
                        fields.append(keys.get(j));
                        if (!Arrays.asList(uniqueKey.split("\\s*,\\s*")).contains(keys.get(j))) {
                            updateSet.append(keys.get(j)).append("=").append(postgisTempReference).append(".").append(keys.get(j));
                        }
                    } else {
                        values.append(",").append(getStringValueFromJsonElement(value, "'", attrNativeTypes));
                        fields.append(",").append(keys.get(j));
                        if (!Arrays.asList(uniqueKey.split("\\s*,\\s*")).contains(keys.get(j))) {
                            updateSet.append(", ").append(keys.get(j)).append("=").append(postgisTempReference).append(".").append(keys.get(j));
                        }
                    }
                }
            }
            query.append("INSERT INTO ").append(postgisDestination).append(" ").append(fields).append(") ").
                    append("VALUES ").append(values).append(") ");
            query.append("ON CONFLICT ").append("(").append(uniqueKey).append(") ").
                    append("DO ").
                    append("UPDATE SET ").append(updateSet).append(" ").
                    append("WHERE ");
            // for key in uniqueKey
            String[] uniqueKeys = uniqueKey.split("\\s*,\\s*");
            for (String uniKey : uniqueKeys) {
                query.append(postgisDestination).append(".").append(uniKey).append("=").append(postgisTempReference).append(".").append(uniKey).append(" ").append("AND ");
            }
            query.append("to_timestamp(").append(postgisDestination).append(".").append(timestampKey).append("::text, '").append(timestampFormat).append("') ").
                    append("< ").append("to_timestamp(").append(postgisTempReference).append(".").append(timestampKey).append("::text, '").append(timestampFormat).append("')");
            upsertList.add(query);
        }
        LOGGER.debug("[SQLQueryUtils.postgreSqlUpsertQuery] Preparing Upsert querys: " + upsertList.toString());
        return upsertList;
    }

    /**
     * Sql upsert query for MySQL string buffer.
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
    protected static ArrayList<StringBuffer> mySqlUpsertQuery(LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                                   LinkedHashMap<String, ArrayList<JsonElement>> lastData,
                                                   String tableName,
                                                   String tableSuffix,
                                                   String uniqueKey,
                                                   String timestampKey,
                                                   String timestampFormat,
                                                   SQLInstance sqlInstance,
                                                   String destination,
                                                   boolean attrNativeTypes) {

        ArrayList<StringBuffer> upsertList = new ArrayList<>();

        for (int i = 0 ; i < collectionSizeOnLinkedHashMap(lastData) ; i++) {
            StringBuffer query = new StringBuffer();
            StringBuffer dateKeyUpdate = new StringBuffer();
            StringBuffer values = new StringBuffer("(");
            StringBuffer fields = new StringBuffer("(");
            StringBuffer updateSet = new StringBuffer();
            ArrayList<String> keys = new ArrayList<>(aggregation.keySet());
            for (int j = 0 ; j < keys.size() ; j++) {
                if (lastData.get(keys.get(j)).get(i) != null) {
                    JsonElement value = lastData.get(keys.get(j)).get(i);
                    if (j == 0) {
                        values.append(getStringValueFromJsonElement(value, "'", attrNativeTypes));
                        fields.append(MYSQL_FIELDS_MARK).append(keys.get(j)).append(MYSQL_FIELDS_MARK);
                        if (!Arrays.asList(uniqueKey.split("\\s*,\\s*")).contains(keys.get(j))) {
                            if (keys.get(j).equalsIgnoreCase(timestampKey)) {
                                dateKeyUpdate.append(mySQLUpdateRecordQuery(keys.get(j), uniqueKey, timestampKey, timestampFormat));
                            } else {
                                updateSet.append(mySQLUpdateRecordQuery(keys.get(j), uniqueKey, timestampKey, timestampFormat));
                            }
                        }
                    } else {
                        values.append(",").append(getStringValueFromJsonElement(value, "'", attrNativeTypes));
                        fields.append(",").append(MYSQL_FIELDS_MARK).append(keys.get(j)).append(MYSQL_FIELDS_MARK);
                        if (!Arrays.asList(uniqueKey.split("\\s*,\\s*")).contains(keys.get(j))) {
                            if (keys.get(j).equalsIgnoreCase(timestampKey)) {
                                dateKeyUpdate.append(mySQLUpdateRecordQuery(keys.get(j), uniqueKey, timestampKey, timestampFormat));
                            } else {
                                if (!(updateSet.length() == 0)) {
                                    updateSet.append(", ");
                                }
                                updateSet.append(mySQLUpdateRecordQuery(keys.get(j), uniqueKey, timestampKey, timestampFormat));
                            }
                        }
                    }
                }
            }
            query.append("INSERT INTO ").append(MYSQL_FIELDS_MARK).append(tableName.concat(tableSuffix)).append(MYSQL_FIELDS_MARK).append(" ").append(fields).append(") ").
                    append("VALUES ").append(values).append(") ");
            query.append("ON DUPLICATE KEY ").
                    append("UPDATE ").append(updateSet).append(", ").append(dateKeyUpdate);
            upsertList.add(query);
        }
        LOGGER.debug("[SQLQueryUtils.mySqlUpsertQuery] Preparing Upsert querys: " + upsertList.toString());
        return upsertList;
    }

    /**
     * Creates a update statement for an upsert query
     *
     * @param key     the table suffix
     * @param uniqueKey       the unique key
     * @param timestampKey    the timestamp key
     * @param timestampFormat the timestamp format
     * @return the string buffer like the following one
     * recvTime=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTime, '%Y-%m-%d %H:%i:%s.%f') < (STR_TO_DATE(VALUES(recvTime), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(recvTime), recvTime)
     * recvTime=IF((entityId=VALUES(entityId)) AND (entityType=VALUES(entityType)) AND (STR_TO_DATE(recvTime, '%Y-%m-%d %H:%i:%s.%f') < (STR_TO_DATE(VALUES(recvTime), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(recvTime), recvTime)
     */

    protected static StringBuffer mySQLUpdateRecordQuery(String key,
                                                         String uniqueKey,
                                                         String timestampKey,
                                                         String timestampFormat) {

        StringBuffer updateSet = new StringBuffer();
        updateSet.append(key).append("=").
                append("IF").
                append("(");
        String[] uniqueKeys = uniqueKey.split("\\s*,\\s*");
        for (String uniKey : uniqueKeys) {
            updateSet.append("(").append(uniKey).append("=").append("VALUES(").append(uniKey).append(")");
            updateSet.append(")").append(" AND ");
        }
        updateSet.append("(").append("STR_TO_DATE(").append(timestampKey).append(", '").append(timestampFormat).append("')").
                append(" < ").
                append("(").append("STR_TO_DATE(VALUES(").append(timestampKey).append("), '").append(timestampFormat).append("')").append(")").
                append(")").
                append(", ").append("VALUES(").append(key).append(")").append(", ").append(key).
                append(")");
        return updateSet;

    }

    /**
     * Sql insert query string buffer.
     *
     * @param aggregation     the aggregation
     * @param tableName       the table name
     * @param sqlInstance     the sql instance
     * @param destination     the destination
     * @return the string buffer
     */
    protected static StringBuffer sqlInsertQuery(LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                                 String tableName,
                                                 SQLInstance sqlInstance,
                                                 String database,
                                                 String schema,
                                                 boolean attrNativeTypes) {

        StringBuffer fieldsForInsert;
        /*

        FIXME https://github.com/telefonicaid/fiware-cygnus/issues/1959

        Add SQLSafe values with native PreparedStatement methods

        StringBuffer valuesForInsert = sqlQuestionValues(aggregation.keySet());

        */
        StringBuffer valuesForInsert = new StringBuffer(getValuesForInsert(aggregation, attrNativeTypes));


        StringBuffer query = new StringBuffer();

        if (valuesForInsert.equals("")) {
            LOGGER.debug("[SQLQueryUtils.sqlInsertQuery] no values for insert");
            return query;
        }

        if (sqlInstance == SQLInstance.POSTGRESQL){
            StringBuffer postgisDestination = new StringBuffer(schema).append(".").append(tableName);            
            fieldsForInsert = getFieldsForInsert(aggregation.keySet(), POSTGRES_FIELDS_MARK);
            query.append("INSERT INTO ").append(postgisDestination).append(" ").append(fieldsForInsert).append(" ").
                    append("VALUES ").append(valuesForInsert).append(" ");
        } else if (sqlInstance == SQLInstance.MYSQL) {
            fieldsForInsert = getFieldsForInsert(aggregation.keySet(), MYSQL_FIELDS_MARK);
            query.append("INSERT INTO ").append(MYSQL_FIELDS_MARK).append(tableName).append(MYSQL_FIELDS_MARK).append(" ").append(fieldsForInsert).append(" ").
                    append("VALUES ").append(valuesForInsert).append(" ");
        }

        LOGGER.debug("[SQLQueryUtils.sqlInsertQuery] Preparing Insert query: " + query.toString());
        return query;
    }

    /**
     * Sql question values string buffer.
     *
     * @param keyList the key list
     * @return the string buffer
     */
    protected static StringBuffer sqlQuestionValues(Set<String> keyList) {
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
        LOGGER.debug("[SQLQueryUtils.sqlQuestionValues] Preparing question marks for statement query: " + questionValues.toString());
        return questionValues;
    }


    // INSERT INTO database (recvtime, location) VALUES ('valor', )

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
        LOGGER.debug("[SQLQueryUtils.getFieldsForInsert] Preparing fields for insert for statement: " + fieldsForInsert.toString());
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
                        LOGGER.debug("[SQLQueryUtils.addJsonValues] " + "Added NULL as String");
                    } else {
                        if (value.isJsonPrimitive()) {
                            if (value.getAsJsonPrimitive().isNumber()) {
                                preparedStatement.setDouble(position, value.getAsDouble());
                                LOGGER.debug("[SQLQueryUtils.addJsonValues] " + "Added " + value.getAsDouble() + " as Number");
                                position++;
                            } else if (value.getAsJsonPrimitive().isBoolean()) {
                                preparedStatement.setBoolean(position, value.getAsBoolean());
                                LOGGER.debug("[SQLQueryUtils.addJsonValues] " + "Added " + value.getAsBoolean() + " as Boolean");
                                position++;
                            } else {
                                String stringValue = value.getAsString();
                                if (stringValue.contains("ST_GeomFromGeoJSON") || stringValue.contains("ST_SetSRID")) {
                                    preparedStatement.setObject(position, stringValue);
                                    LOGGER.debug("[SQLQueryUtils.addJsonValues] " + "Added postgis Function " + stringValue + " as Object");
                                    position++;
                                } else {
                                    preparedStatement.setObject(position, stringValue);
                                    LOGGER.debug("[SQLQueryUtils.addJsonValues] " + "Added " + stringValue + " as Object");
                                    position++;
                                }
                            } // else
                        } else { // if (value.isJsonPrimitive())
                            preparedStatement.setString(position, value.toString());
                            LOGGER.debug("[SQLQueryUtils.addJsonValues] " + "Added " + value.toString() + " as String");
                            position++;
                        } // else
                    } // else
                } else { //if (attrNativeTypes)
                    if (value != null && value.isJsonPrimitive()) {
                        String stringValue = value.getAsString();
                        if (stringValue.contains("ST_GeomFromGeoJSON") || stringValue.contains("ST_SetSRID")) {
                            preparedStatement.setObject(position, stringValue);
                            LOGGER.debug("[SQLQueryUtils.addJsonValues] " + "Added postgis Function " + stringValue + " as Object");
                            position++;
                        } else {
                            preparedStatement.setObject(position, stringValue);
                            LOGGER.debug("[SQLQueryUtils.addJsonValues] " + "Added " + stringValue + " as String");
                            position++;
                        }
                    } else {
                        if (value == null){
                            preparedStatement.setObject(position, "NULL");
                            LOGGER.debug("[SQLQueryUtils.addJsonValues] " + "Added NULL as String");
                            position++;
                        } else {
                            preparedStatement.setObject(position, value.toString());
                            LOGGER.debug("[SQLQueryUtils.addJsonValues] " + "Added " + value.toString() + " as String");
                            position++;
                        }
                    }
                }
            } // while
            preparedStatement.addBatch();
            LOGGER.debug("[SQLQueryUtils.addJsonValues] Batch added");
        } // for
        return preparedStatement;
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


    protected static String getEscapedJsonString(JsonElement value, String quotationMark) {
        // String escaped = StringEscapeUtils.escapeSql(value.getAsString());
        // escaped = escaped.replaceAll("\n", "\\\\n");
        // escaped = escaped.replaceAll("\r", "\\\\r");
        String escaped = value.getAsString();
        escaped = escaped.replaceAll("'", "''");
        escaped = quotationMark + escaped + quotationMark;
        return escaped;
    }

    protected static String getEscapedString(JsonElement value, String quotationMark) {
        // String escaped = StringEscapeUtils.escapeSql(value.getAsString());
        // escaped = escaped.replaceAll("\n", "\\\\n");
        // escaped = escaped.replaceAll("\r", "\\\\r");
        String escaped = value.toString();
        escaped = escaped.replaceAll("'", "''");
        escaped = quotationMark + escaped + quotationMark;
        return escaped;
    }


    /**
     * Gets string value from json element.
     *
     * @param value           the value to process
     * @param quotationMark   the quotation mark
     * @param attrNativeTypes the attr native types
     * @return the string value from json element
     */
    protected static String getStringValueFromJsonElement(JsonElement value, String quotationMark, boolean attrNativeTypes) {
        String stringValue;
        if (attrNativeTypes) {
            if (value == null || value.isJsonNull()) {
                stringValue = "NULL";
            } else if (value.isJsonPrimitive()) {
                if (value.getAsJsonPrimitive().isBoolean()) {
                    stringValue = value.getAsString().toUpperCase();
                } else if (value.getAsJsonPrimitive().isNumber()) {
                    stringValue = value.getAsString();
                } else {
                    if (value.toString().contains("ST_GeomFromGeoJSON") || value.toString().contains("ST_SetSRID")) {
                        stringValue = value.getAsString().replace("\\", "");
                    } else {
                        stringValue = getEscapedJsonString(value, quotationMark);
                    }
                }
            } else {
                stringValue = getEscapedString(value, quotationMark);
            }
        } else {
            if (value != null && value.isJsonPrimitive()) {
                if (value.toString().contains("ST_GeomFromGeoJSON") || value.toString().contains("ST_SetSRID")) {
                    stringValue = value.getAsString().replace("\\", "");
                } else {
                    stringValue = getEscapedJsonString(value, quotationMark);
                }
            } else {
                if (value == null){
                    stringValue = quotationMark + "NULL" + quotationMark;
                } else {
                    stringValue = getEscapedString(value, quotationMark);
                }
            }
        }
        return stringValue;
    }

    /**
     * Gets values for insert.
     *
     * @param aggregation     the aggregation
     * @param attrNativeTypes the attr native types
     * @return a String with all VALUES in SQL query format.
     */
    public static String getValuesForInsert(LinkedHashMap<String, ArrayList<JsonElement>> aggregation, boolean attrNativeTypes) {
        String valuesForInsert = "";
        int numEvents = collectionSizeOnLinkedHashMap(aggregation);

        for (int i = 0; i < numEvents; i++) {
            if (i == 0) {
                valuesForInsert += "(";
            } else {
                valuesForInsert +=  ",(";
            } // if else
            boolean first = true;
            Iterator<String> it = aggregation.keySet().iterator();
            while (it.hasNext()) {
                String entry = (String) it.next();
                ArrayList<JsonElement> values = (ArrayList<JsonElement>) aggregation.get(entry);
                JsonElement value = values.get(i);
                String stringValue = getStringValueFromJsonElement(value, "'", attrNativeTypes);
                if (first) {
                    valuesForInsert += stringValue;
                    first = false;
                } else {
                    valuesForInsert += "," + stringValue;
                } // if else
            } // while
            valuesForInsert += ")";
        } // for
        return valuesForInsert;
    } // getValuesForInsert


    /**
     * Gets fields for create.
     *
     * @param aggregation the aggregation
     * @return the fields (column names) for create in SQL format.
     */
    public static String getFieldsForCreate(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        String fieldsForCreate = "(";
        boolean first = true;
        Iterator<String> it = aggregation.keySet().iterator();
        while (it.hasNext()) {
            if (first) {
                fieldsForCreate += (String) it.next() + " text";
                first = false;
            } else {
                fieldsForCreate += "," + (String) it.next() + " text";
            } // if else
        } // while

        return fieldsForCreate + ")";
    } // getFieldsForCreate
}
