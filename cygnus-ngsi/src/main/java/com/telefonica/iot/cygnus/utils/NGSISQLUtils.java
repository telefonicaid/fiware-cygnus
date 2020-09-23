package com.telefonica.iot.cygnus.utils;

import com.google.gson.JsonElement;
import com.telefonica.iot.cygnus.log.CygnusLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class NGSISQLUtils {

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSISQLUtils.class);

    private static final String POSTGRES_FIELDS_MARK = "";
    private static final String MYSQL_FIELDS_MARK = "'";
    private static final String SEPARATION_MARK = ",";

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

        return preparedStatement;

    }

    protected static StringBuffer sqlUpsertQuery(LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                              LinkedHashMap<String, ArrayList<JsonElement>> lastData,
                                              String tableName,
                                              String tableSuffix,
                                              String uniqueKey,
                                              String timestampKey,
                                              String timestampFormat,
                                              String sqlInstance,
                                              String destination) throws SQLException {

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

        return query;
    }

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
        return questionValues;
    }

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
        return fieldsForInsert;
    } // getFieldsForInsert

    protected static PreparedStatement addJsonValues (PreparedStatement previousStatement,
                                                   LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                                   boolean attrNativeTypes) throws SQLException {

        PreparedStatement preparedStatement = previousStatement;
        int numEvents = NGSIUtils.collectionSizeOnLinkedHashMap(aggregation);
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
                    } else {
                        if (value.isJsonPrimitive()) {
                            if (value.getAsJsonPrimitive().isNumber()) {
                                preparedStatement.setDouble(position, value.getAsDouble());
                                position++;
                            } else if (value.getAsJsonPrimitive().isBoolean()) {
                                preparedStatement.setBoolean(position, value.getAsBoolean());
                                position++;
                            } else {
                                String stringValue = value.getAsString();
                                if (stringValue.contains("ST_GeomFromGeoJSON") || stringValue.contains("ST_SetSRID")) {
                                    preparedStatement.setObject(position, stringValue);
                                    position++;
                                } else {
                                    preparedStatement.setString(position, stringValue);
                                    position++;
                                }
                            } // else
                        } else { // if (value.isJsonPrimitive())
                            preparedStatement.setString(position, value.toString());
                            position++;
                        } // else
                    } // else
                } else { //if (attrNativeTypes)
                    if (value != null && value.isJsonPrimitive()) {
                        String stringValue = value.getAsString();
                        if (stringValue.contains("ST_GeomFromGeoJSON") || stringValue.contains("ST_SetSRID")) {
                            preparedStatement.setObject(position, stringValue);
                            position++;
                        } else {
                            preparedStatement.setString(position, stringValue);
                            position++;
                        }
                    } else {
                        if (value == null){
                            preparedStatement.setString(position, "NULL");
                            position++;
                        } else {
                            preparedStatement.setString(position, value.toString());
                            position++;
                        }
                    }
                }
            } // while
            preparedStatement.addBatch();
        } // for
        return preparedStatement;
    }

}
