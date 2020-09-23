package com.telefonica.iot.cygnus.utils;

import com.google.gson.JsonElement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class NGSISQLUtils {

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

    /*
    public static PreparedStatement upsert (LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                            LinkedHashMap<String, ArrayList<JsonElement>> lastData,
                                            String tableName,
                                            String tableSuffix,
                                            String uniqueKey,
                                            String timestampKey,
                                            String timestampFormat,
                                            String sqlInstance,
                                            String destination,
                                            Connection connection,
                                            boolean attrNativeTypes) {

        String query


    }
    */

    private static String sqlUpsertQuery(LinkedHashMap<String, ArrayList<JsonElement>> aggregation,
                                              LinkedHashMap<String, ArrayList<JsonElement>> lastData,
                                              String tableName,
                                              String tableSuffix,
                                              String uniqueKey,
                                              String timestampKey,
                                              String timestampFormat,
                                              String sqlInstance,
                                              String destination) throws SQLException {

        String fieldsForInsert = NGSIUtils.getFieldsForInsert(lastData);
        String valuesForInsert = sqlQuestionValues(lastData.keySet());
        String updateSet = "";
        String postgisTempReference = "EXCLUDED";
        String postgisDestination = destination + "." + tableName + tableSuffix;
        String query = "";

        for (String key : lastData.keySet()) {
            if (!key.equals(uniqueKey)) {
                updateSet += key + "=" + postgisTempReference + "." + key + " ";
            }
        }

        if (sqlInstance.equals("postgresql")) {
            query = "INSERT INTO " + postgisDestination + " " + fieldsForInsert + " " +
                    "VALUES " + valuesForInsert + " " +
                    "ON CONFLICT " + "(" + uniqueKey + ")" +
                    "DO " +
                    "UPDATE SET " + updateSet + " " +
                    "WHERE " + postgisDestination + "." + uniqueKey + "=" + postgisTempReference + "." + uniqueKey + " " +
                    "AND " + "to_timestamp(" + postgisDestination + "." + timestampKey + ", '" + timestampFormat + "' " +
                    "< " + "to_timestamp(" + postgisTempReference + "." + timestampKey + ", '" + timestampFormat + "'" + ")";
        }

        return query;
    }

    public static String sqlQuestionValues(Set<String> keyList) {
        String questionValues = "(";
        boolean first = true;
        for (String key : keyList) {
            if (first) {
                questionValues += "?";
            } else {
                questionValues += ", ?";
            }
        }
        questionValues += ")";
        return questionValues;
    }



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
