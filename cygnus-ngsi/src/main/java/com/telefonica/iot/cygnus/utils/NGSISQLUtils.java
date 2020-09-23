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

    private static final String TEXT_MARK = "'";
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
                    query,
                    lastData,
                    attrNativeTypes);
        } catch (SQLException e) {
            LOGGER.error(sqlInstance + " SQLEXCEPTION Error creating upsert statement " + e);
        } catch (Exception e) {
            LOGGER.error(sqlInstance + " GENERICEXCEPTION Error creating upsert statement " + e);
        }

        return preparedStatement;

    }


    /*

    insert into pruebapostmanx.subpruebapostman_5dde93a46c54998b7f89fb9d_wastecontainer_last_v
(Recvtimets,recvTime,fiwareServicePath,entityId,entityType,attrName,attrType,attrValue,attrMd)
values ('1600680094906','2020-09-21 09:24:40.902','/subPruebaPostman','5dde93a46c54998b7f89fb9d2','WasteContainer','fillingLevel','Number',0.40,'[{"name":"TimeInstant","type":"DateTime","value":"2020-01-20T12:01:25.00Z"}]')
ON CONFLICT (entityId) DO
UPDATE SET Recvtimets=EXCLUDED.Recvtimets, recvTime=EXCLUDED.recvTime, fiwareServicePath=EXCLUDED.fiwareServicePath, entityId=EXCLUDED.entityId, entityType=EXCLUDED.entityType,
attrName=EXCLUDED.attrName, attrType=EXCLUDED.attrType, attrValue=EXCLUDED.attrValue, attrMd=EXCLUDED.attrMd
WHERE pruebapostmanx.subpruebapostman_5dde93a46c54998b7f89fb9d_wastecontainer_last_v.entityId=EXCLUDED.entityId
AND to_timestamp(pruebapostmanx.subpruebapostman_5dde93a46c54998b7f89fb9d_wastecontainer_last_v.recvTime, 'YYYY-MM-DD HH24:MI:SS.MS') < to_timestamp(EXCLUDED.recvTime, 'YYYY-MM-DD HH24:MI:SS.MS')

     */

    private static StringBuffer sqlUpsertQuery(LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                              LinkedHashMap<String, ArrayList<JsonElement>> lastData,
                                              String tableName,
                                              String tableSuffix,
                                              String uniqueKey,
                                              String timestampKey,
                                              String timestampFormat,
                                              String sqlInstance,
                                              String destination) throws SQLException {

        StringBuffer fieldsForInsert = getFieldsForInsert(lastData);
        StringBuffer valuesForInsert = sqlQuestionValues(lastData.keySet());
        StringBuffer updateSet = new StringBuffer();
        StringBuffer postgisTempReference = new StringBuffer("EXCLUDED");
        StringBuffer postgisDestination = new StringBuffer(destination).append(".").append(tableName).append(tableSuffix);
        StringBuffer query = new StringBuffer();

        for (String key : lastData.keySet()) {
            if (!key.equals(uniqueKey)) {
                updateSet.append(key).append("=").append(postgisTempReference).append(".").append(key).append(" ");
            }
        }

        if (sqlInstance.equals("postgresql")) {
            query.append("INSERT INTO ").append(postgisDestination).append(" ").append(fieldsForInsert).append(" ").
                    append("VALUES ").append(valuesForInsert).append(" ").
                    append("ON CONFLICT ").append("(").append(uniqueKey).append(")").
                    append("DO ").
                    append("UPDATE SET ").append(updateSet).append(" ").
                    append("WHERE ").append(postgisDestination).append(".").append(uniqueKey).append("=").append(postgisTempReference).append(".").append(uniqueKey).append(" ").
                    append("AND ").append("to_timestamp(").append(postgisDestination).append(".").append(timestampKey).append(", '").append(timestampFormat).append("' ").
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
            } else {
                questionValues.append(", ?");
            }
        }
        questionValues.append(")");
        return questionValues;
    }

    public static StringBuffer getFieldsForInsert(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        StringBuffer fieldsForInsert = new StringBuffer("(");
        boolean first = true;
        Iterator<String> it = aggregation.keySet().iterator();
        while (it.hasNext()) {
            if (first) {
                fieldsForInsert.append(TEXT_MARK).append((String) it.next()).append(TEXT_MARK);
                first = false;
            } else {
                fieldsForInsert.append(SEPARATION_MARK).append(TEXT_MARK).append((String) it.next()).append(TEXT_MARK);
            } // if else
        } // while
        fieldsForInsert.append(")");
        return fieldsForInsert;
    } // getFieldsForInsert

    private static PreparedStatement addJsonValues (PreparedStatement previousStatement,
                                                   String query,
                                                   LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                                   boolean attrNativeTypes) throws SQLException {

        PreparedStatement preparedStatement = previousStatement;

        int numEvents = NGSIUtils.collectionSizeOnLinkedHashMap(aggregation);

        for (int i = 0; i < numEvents; i++) {
            boolean first = true;
            Iterator<String> it = aggregation.keySet().iterator();
            while (it.hasNext()) {
                int position = 1;
                String entry = (String) it.next();
                ArrayList<JsonElement> values = (ArrayList<JsonElement>) aggregation.get(entry);
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
