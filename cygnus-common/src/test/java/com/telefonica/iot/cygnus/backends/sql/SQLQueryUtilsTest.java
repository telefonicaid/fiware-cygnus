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
import com.google.gson.JsonPrimitive;
import org.junit.Before;
import org.junit.Test;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class SQLQueryUtilsTest {

    Connection connection = mock(Connection.class);

    PreparedStatement statement = mock(PreparedStatement.class);

    @Before
    public void setUp() throws SQLException{
        when(connection.prepareStatement(anyString())).thenReturn(statement);
    }

    private LinkedHashMap<String, ArrayList<JsonElement>> getValueFieldsMultipleBatch() {
        LinkedHashMap<String, ArrayList<JsonElement>> aggregation = new LinkedHashMap<>();
        ArrayList<JsonElement> recvTime = new ArrayList<>();
        recvTime.add(new JsonPrimitive(Long.parseLong("1461136795801")));
        recvTime.add(new JsonPrimitive(Long.parseLong("1461136795802")));
        recvTime.add(new JsonPrimitive(Long.parseLong("1461136795800")));
        aggregation.put("recvTime", recvTime);
        ArrayList<JsonElement> recvTimeS = new ArrayList<>();
        recvTimeS.add(new JsonPrimitive("2016-04-20 07:19:55.801"));
        recvTimeS.add(new JsonPrimitive("2016-04-20 07:19:55.802"));
        recvTimeS.add(new JsonPrimitive("2016-04-20 07:19:55.800"));
        aggregation.put("recvTimeS", recvTimeS);
        ArrayList<JsonElement> fiwareServicePath = new ArrayList<>();
        fiwareServicePath.add(new JsonPrimitive("somePath1"));
        fiwareServicePath.add(new JsonPrimitive("somePath2"));
        fiwareServicePath.add(new JsonPrimitive("somePath3"));
        aggregation.put("fiwareServicePath", fiwareServicePath);
        ArrayList<JsonElement> entityId = new ArrayList<>();
        entityId.add(new JsonPrimitive("entityId1"));
        entityId.add(new JsonPrimitive("entityId1"));
        entityId.add(new JsonPrimitive("entityId1"));
        aggregation.put("entityId", entityId);
        ArrayList<JsonElement> entityType = new ArrayList<>();
        entityType.add(new JsonPrimitive("entityType"));
        entityType.add(new JsonPrimitive("entityType"));
        entityType.add(new JsonPrimitive("entityType"));
        aggregation.put("entityType", entityType);
        ArrayList<JsonElement> loadStr = new ArrayList<>();
        loadStr.add(new JsonPrimitive("load1"));
        loadStr.add(new JsonPrimitive("load2"));
        loadStr.add(new JsonPrimitive("load3"));
        aggregation.put("loadStr", loadStr);
        ArrayList<JsonElement> loadBool = new ArrayList<>();
        loadBool.add(new JsonPrimitive(true));
        loadBool.add(new JsonPrimitive(false));
        loadBool.add(new JsonPrimitive(false));
        aggregation.put("loadBool", loadBool);
        ArrayList<JsonElement> loadNumber = new ArrayList<>();
        loadNumber.add(new JsonPrimitive(1));
        loadNumber.add(new JsonPrimitive(23));
        loadNumber.add(new JsonPrimitive(8));
        aggregation.put("loadNumber", loadNumber);
        ArrayList<JsonElement> load_md = new ArrayList<>();
        load_md.add(new JsonPrimitive("load_md"));
        load_md.add(new JsonPrimitive("load_md"));
        load_md.add(new JsonPrimitive("load_md"));
        aggregation.put("load_md", load_md);
        return aggregation;
    }// getValueFields

    private LinkedHashMap<String, ArrayList<JsonElement>> getValueFieldsSingleBatch() {
        LinkedHashMap<String, ArrayList<JsonElement>> aggregation = new LinkedHashMap<String, ArrayList<JsonElement>>();
        ArrayList<JsonElement> recvTime = new ArrayList<>();
        recvTime.add(new JsonPrimitive(Long.parseLong("1461136795801")));
        aggregation.put("recvTime", recvTime);
        ArrayList<JsonElement> recvTimeS = new ArrayList<>();
        recvTimeS.add(new JsonPrimitive("2016-04-20 07:19:55.801"));
        aggregation.put("recvTimeS", recvTimeS);
        ArrayList<JsonElement> fiwareServicePath = new ArrayList<>();
        fiwareServicePath.add(new JsonPrimitive("somePath1"));
        aggregation.put("fiwareServicePath", fiwareServicePath);
        ArrayList<JsonElement> entityId = new ArrayList<>();
        entityId.add(new JsonPrimitive("entityId1"));
        aggregation.put("entityId", entityId);
        ArrayList<JsonElement> entityType = new ArrayList<>();
        entityType.add(new JsonPrimitive("entityType"));
        aggregation.put("entityType", entityType);
        ArrayList<JsonElement> loadStr = new ArrayList<>();
        loadStr.add(new JsonPrimitive("load1"));
        aggregation.put("loadStr", loadStr);
        ArrayList<JsonElement> loadBool = new ArrayList<>();
        loadBool.add(new JsonPrimitive(true));
        aggregation.put("loadBool", loadBool);
        ArrayList<JsonElement> loadNumber = new ArrayList<>();
        loadNumber.add(new JsonPrimitive(1));
        aggregation.put("loadNumber", loadNumber);
        ArrayList<JsonElement> load_md = new ArrayList<>();
        load_md.add(new JsonPrimitive("load_md"));
        aggregation.put("load_md", load_md);
        return aggregation;
    }// getValueFields

    @Test
    public void testSQLUpsertQuerySingleBatch() {
        String tableName = "exampleTable";
        String tableSuffix = "_last_data";
        String uniqueKey = "entityId";
        String timestampKey = "recvTimeS";
        String timestampFormat = "YYYY-MM-DD HH24:MI:SS.MS";
        String sqlInstance = "postgresql";
        String destination = "example";
        StringBuffer sqlupsertQuery;
        sqlupsertQuery = SQLQueryUtils.sqlUpsertQuery(getValueFieldsSingleBatch(),
                getValueFieldsSingleBatch(),
                tableName,
                tableSuffix,
                uniqueKey,
                timestampKey,
                timestampFormat,
                sqlInstance,
                destination);

        String correctQuery = "INSERT INTO example.exampleTable_last_data " +
                "(recvTime,recvTimeS,fiwareServicePath,entityId,entityType,loadStr,loadBool,loadNumber,load_md) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (entityId) " +
                "DO UPDATE SET recvTime=EXCLUDED.recvTime, recvTimeS=EXCLUDED.recvTimeS, fiwareServicePath=EXCLUDED.fiwareServicePath, " +
                "entityType=EXCLUDED.entityType, loadStr=EXCLUDED.loadStr, loadBool=EXCLUDED.loadBool, loadNumber=EXCLUDED.loadNumber, load_md=EXCLUDED.load_md " +
                "WHERE example.exampleTable_last_data.entityId=EXCLUDED.entityId " +
                "AND to_timestamp(example.exampleTable_last_data.recvTimeS, 'YYYY-MM-DD HH24:MI:SS.MS') " +
                "< to_timestamp(EXCLUDED.recvTimeS, 'YYYY-MM-DD HH24:MI:SS.MS')";

        try {
            assertEquals(sqlupsertQuery.toString(), correctQuery);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testSQLUpsertQuerySingleBatch]")
                    + "-  OK  - testSQLUpsertQuerySingleBatch");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testPostgreSQLUpsertQueryMultipleBatch() {
        String tableName = "exampleTable";
        String tableSuffix = "_last_data";
        String uniqueKey = "entityId";
        String timestampKey = "recvTimeS";
        String timestampFormat = "YYYY-MM-DD HH24:MI:SS.MS";
        String sqlInstance = "postgresql";
        String destination = "example";
        StringBuffer sqlupsertQuery;
        sqlupsertQuery = SQLQueryUtils.sqlUpsertQuery(getValueFieldsMultipleBatch(),
                getValueFieldsMultipleBatch(),
                tableName,
                tableSuffix,
                uniqueKey,
                timestampKey,
                timestampFormat,
                sqlInstance,
                destination);

        String correctQuery = "INSERT INTO example.exampleTable_last_data " +
                "(recvTime,recvTimeS,fiwareServicePath,entityId,entityType,loadStr,loadBool,loadNumber,load_md) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (entityId) " +
                "DO UPDATE SET recvTime=EXCLUDED.recvTime, recvTimeS=EXCLUDED.recvTimeS, fiwareServicePath=EXCLUDED.fiwareServicePath, " +
                "entityType=EXCLUDED.entityType, loadStr=EXCLUDED.loadStr, loadBool=EXCLUDED.loadBool, loadNumber=EXCLUDED.loadNumber, load_md=EXCLUDED.load_md " +
                "WHERE example.exampleTable_last_data.entityId=EXCLUDED.entityId " +
                "AND to_timestamp(example.exampleTable_last_data.recvTimeS, 'YYYY-MM-DD HH24:MI:SS.MS') " +
                "< to_timestamp(EXCLUDED.recvTimeS, 'YYYY-MM-DD HH24:MI:SS.MS')";

        try {
            assertEquals(sqlupsertQuery.toString(), correctQuery);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testPostgreSQLUpsertQueryMultipleBatch]")
                    + "-  OK  - testPostgreSQLUpsertQueryMultipleBatch");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testPostgreSQLUpsertQueryEmptyBatch() {
        String tableName = "exampleTable";
        String tableSuffix = "_last_data";
        String uniqueKey = "entityId";
        String timestampKey = "recvTimeS";
        String timestampFormat = "YYYY-MM-DD HH24:MI:SS.MS";
        String sqlInstance = "postgresql";
        String destination = "example";
        StringBuffer sqlupsertQuery;
        sqlupsertQuery = SQLQueryUtils.sqlUpsertQuery(new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                tableName,
                tableSuffix,
                uniqueKey,
                timestampKey,
                timestampFormat,
                sqlInstance,
                destination);

        String correctQuery = "INSERT INTO example.exampleTable_last_data () VALUES () ON CONFLICT (entityId) DO UPDATE SET  " +
                "WHERE example.exampleTable_last_data.entityId=EXCLUDED.entityId AND to_timestamp(example.exampleTable_last_data.recvTimeS, " +
                "'YYYY-MM-DD HH24:MI:SS.MS') < to_timestamp(EXCLUDED.recvTimeS, 'YYYY-MM-DD HH24:MI:SS.MS')";

        try {
            assertEquals(sqlupsertQuery.toString(), correctQuery);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testPostgreSQLUpsertQueryEmptyBatch]")
                    + "-  OK  - testPostgreSQLUpsertQueryEmptyBatch");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testMySQLUpsertQueryMultipleBatch() {
        String tableName = "exampleTable";
        String tableSuffix = "_last_data";
        String uniqueKey = "entityId";
        String timestampKey = "recvTimeS";
        String timestampFormat = "%Y-%m-%d %H:%i:%s.%f";
        String sqlInstance = "mysql";
        String destination = "example";
        StringBuffer sqlupsertQuery;
        sqlupsertQuery = SQLQueryUtils.sqlUpsertQuery(getValueFieldsMultipleBatch(),
                getValueFieldsMultipleBatch(),
                tableName,
                tableSuffix,
                uniqueKey,
                timestampKey,
                timestampFormat,
                sqlInstance,
                destination);

        String correctQuery = "INSERT INTO `exampleTable_last_data` " +
                "(`recvTime`,`recvTimeS`,`fiwareServicePath`,`entityId`,`entityType`,`loadStr`,`loadBool`,`loadNumber`,`load_md`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "recvTime=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(recvTime), recvTime), " +
                "fiwareServicePath=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(fiwareServicePath), fiwareServicePath), " +
                "entityType=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(entityType), entityType), " +
                "loadStr=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(loadStr), loadStr), " +
                "loadBool=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(loadBool), loadBool), " +
                "loadNumber=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(loadNumber), loadNumber), " +
                "load_md=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(load_md), load_md), " +
                "recvTimeS=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(recvTimeS), recvTimeS)";
        try {
            assertEquals(sqlupsertQuery.toString(), correctQuery);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testMySQLUpsertQueryMultipleBatch]")
                    + "-  OK  - testMySQLUpsertQueryMultipleBatch");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMySQLUpsertQuerySingleBatch() {
        String tableName = "exampleTable";
        String tableSuffix = "_last_data";
        String uniqueKey = "entityId";
        String timestampKey = "recvTimeS";
        String timestampFormat = "%Y-%m-%d %H:%i:%s.%f";
        String sqlInstance = "mysql";
        String destination = "example";
        StringBuffer sqlupsertQuery;
        sqlupsertQuery = SQLQueryUtils.sqlUpsertQuery(getValueFieldsSingleBatch(),
                getValueFieldsSingleBatch(),
                tableName,
                tableSuffix,
                uniqueKey,
                timestampKey,
                timestampFormat,
                sqlInstance,
                destination);

        String correctQuery = "INSERT INTO `exampleTable_last_data` " +
                "(`recvTime`,`recvTimeS`,`fiwareServicePath`,`entityId`,`entityType`,`loadStr`,`loadBool`,`loadNumber`,`load_md`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "recvTime=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(recvTime), recvTime), " +
                "fiwareServicePath=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(fiwareServicePath), fiwareServicePath), " +
                "entityType=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(entityType), entityType), " +
                "loadStr=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(loadStr), loadStr), " +
                "loadBool=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(loadBool), loadBool), " +
                "loadNumber=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(loadNumber), loadNumber), " +
                "load_md=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(load_md), load_md), " +
                "recvTimeS=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTimeS, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(recvTimeS), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(recvTimeS), recvTimeS)";
        try {
            assertEquals(sqlupsertQuery.toString(), correctQuery);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testMySQLUpsertQuerySingleBatch]")
                    + "-  OK  - testMySQLUpsertQuerySingleBatch");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMySQLUpsertQueryEmptyBatch() {
        String tableName = "exampleTable";
        String tableSuffix = "_last_data";
        String uniqueKey = "entityId";
        String timestampKey = "recvTimeS";
        String timestampFormat = "%Y-%m-%d %H:%i:%s.%f";
        String sqlInstance = "mysql";
        String destination = "example";
        StringBuffer sqlupsertQuery;
        sqlupsertQuery = SQLQueryUtils.sqlUpsertQuery(new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                tableName,
                tableSuffix,
                uniqueKey,
                timestampKey,
                timestampFormat,
                sqlInstance,
                destination);

        String correctQuery = "INSERT INTO `exampleTable_last_data` () VALUES () ON DUPLICATE KEY UPDATE";
        try {
            assertEquals(sqlupsertQuery.toString().trim(), correctQuery);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testMySQLUpsertQueryEmptyBatch]")
                    + "-  OK  - testMySQLUpsertQueryEmptyBatch");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMySQLUpdateRecordQuery() {
        String key = "key";
        String uniqueKey = "uniqueKey";
        String timestampKey = "timestampKey";
        String timestampFormat = "%Y-%m-%d %H:%i:%s.%f";
        StringBuffer sqlupsertQuery;
        sqlupsertQuery = SQLQueryUtils.mySQLUpdateRecordQuery(key,
                uniqueKey,
                timestampKey,
                timestampFormat);

        String correctQuery = "key=IF((uniqueKey=VALUES(uniqueKey)) AND (STR_TO_DATE(timestampKey, '%Y-%m-%d %H:%i:%s.%f') < " +
                "(STR_TO_DATE(VALUES(timestampKey), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(key), key)";
        try {
            assertEquals(sqlupsertQuery.toString(), correctQuery);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testMySQLUpdateRecordQuery]")
                    + "-  OK  - testMySQLUpdateRecordQuery");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPostgreSQLInsertQuerySingleBatch() {
        String tableName = "exampleTable";
        String sqlInstance = "postgresql";
        String destination = "example";
        StringBuffer sqlupsertQuery;
        sqlupsertQuery = SQLQueryUtils.sqlInsertQuery(getValueFieldsSingleBatch(),
                tableName,
                sqlInstance,
                destination);

        String correctQuery = "INSERT INTO example.exampleTable " +
                "(recvTime,recvTimeS,fiwareServicePath,entityId,entityType,loadStr,loadBool,loadNumber,load_md) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ";

        try {
            assertEquals(sqlupsertQuery.toString(), correctQuery);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testPostgreSQLInsertQuerySingleBatch]")
                    + "-  OK  - testPostgreSQLInsertQuerySingleBatch");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testMySQLInsertQuerySingleBatch() {
        String tableName = "exampleTable";
        String sqlInstance = "mysql";
        String destination = "example";
        StringBuffer sqlupsertQuery;
        sqlupsertQuery = SQLQueryUtils.sqlInsertQuery(getValueFieldsSingleBatch(),
                tableName,
                sqlInstance,
                destination);

        String correctQuery = "INSERT INTO `exampleTable` " +
                "(`recvTime`,`recvTimeS`,`fiwareServicePath`,`entityId`,`entityType`,`loadStr`,`loadBool`,`loadNumber`,`load_md`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ";

        try {
            assertEquals(sqlupsertQuery.toString(), correctQuery);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testMySQLInsertQuerySingleBatch]")
                    + "-  OK  - testPostgreSQLInsertQuerySingleBatch");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testPostgreSQLInsertQueryMultipleBatch() {
        String tableName = "exampleTable";
        String sqlInstance = "postgresql";
        String destination = "example";
        StringBuffer sqlupsertQuery;
        sqlupsertQuery = SQLQueryUtils.sqlInsertQuery(getValueFieldsMultipleBatch(),
                tableName,
                sqlInstance,
                destination);

        String correctQuery = "INSERT INTO example.exampleTable " +
                "(recvTime,recvTimeS,fiwareServicePath,entityId,entityType,loadStr,loadBool,loadNumber,load_md) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ";

        try {
            assertEquals(sqlupsertQuery.toString(), correctQuery);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testPostgreSQLInsertQueryMultipleBatch]")
                    + "-  OK  - testPostgreSQLInsertQueryMultipleBatch");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testMySQLInsertQueryMultipleBatch() {
        String tableName = "exampleTable";
        String sqlInstance = "mysql";
        String destination = "example";
        StringBuffer sqlupsertQuery;
        sqlupsertQuery = SQLQueryUtils.sqlInsertQuery(getValueFieldsMultipleBatch(),
                tableName,
                sqlInstance,
                destination);

        String correctQuery = "INSERT INTO `exampleTable` " +
                "(`recvTime`,`recvTimeS`,`fiwareServicePath`,`entityId`,`entityType`,`loadStr`,`loadBool`,`loadNumber`,`load_md`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ";

        try {
            assertEquals(sqlupsertQuery.toString(), correctQuery);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testMySQLInsertQueryMultipleBatch]")
                    + "-  OK  - testMySQLInsertQueryMultipleBatch");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSQLQuestionMultipleValues() {
        Set<String> keyList = getValueFieldsMultipleBatch().keySet();
        String multipleList = "(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            StringBuffer questionValues = SQLQueryUtils.sqlQuestionValues(keyList);
            assertEquals(questionValues.toString(), multipleList);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testSQLQuestionMultipleValues]")
                    + "-  OK  - testSQLQuestionMultipleValues");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSQLQuestionSingleValue() {
        Set<String> keyList = new HashSet<>();
        keyList.add("value");
        String multipleList = "(?)";
        try {
            StringBuffer questionValues = SQLQueryUtils.sqlQuestionValues(keyList);
            assertEquals(questionValues.toString(), multipleList);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testSQLQuestionSingleValue]")
                    + "-  OK  - testSQLQuestionSingleValue");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSQLQuestionNoneValue() {
        Set<String> keyList = new HashSet<>();
        String multipleList = "()";
        try {
            StringBuffer questionValues = SQLQueryUtils.sqlQuestionValues(keyList);
            assertEquals(questionValues.toString(), multipleList);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testSQLQuestionSingleValue]")
                    + "-  OK  - testSQLQuestionSingleValue");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetFieldsForInsertMultiple() {
        Set<String> keyList = getValueFieldsMultipleBatch().keySet();
        String multipleList = "(recvTime,recvTimeS,fiwareServicePath,entityId,entityType,loadStr,loadBool,loadNumber,load_md)";
        try {
            StringBuffer questionValues = SQLQueryUtils.getFieldsForInsert(keyList, "");
            assertEquals(questionValues.toString(), multipleList);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testGetFieldsForInsertMultiple]")
                    + "-  OK  - testGetFieldsForInsertMultiple");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetFieldsForInsertSingle() {
        Set<String> keyList = new HashSet<>();
        keyList.add("value");
        String multipleList = "(value)";
        try {
            StringBuffer questionValues = SQLQueryUtils.getFieldsForInsert(keyList, "");
            assertEquals(questionValues.toString(), multipleList);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testGetFieldsForInsertSingle]")
                    + "-  OK  - testGetFieldsForInsertSingle");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetFieldsForInsertNone() {
        Set<String> keyList = new HashSet<>();
        String multipleList = "()";
        try {
            StringBuffer questionValues = SQLQueryUtils.getFieldsForInsert(keyList, "");
            assertEquals(questionValues.toString(), multipleList);
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testGetFieldsForInsertNone]")
                    + "-  OK  - testGetFieldsForInsertNone");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddJsonValuesSingleNativeTypes() throws SQLException {
        String query = "INSERT INTO example.exampleTable_last_data " +
                "(recvTime,recvTimeS,fiwareServicePath,entityId,entityType,loadStr,loadNumber,load_md) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        boolean attrNativeTypes = true;
        PreparedStatement previousStatement = connection.prepareStatement(query);
        PreparedStatement preparedStatement;
        try {
            preparedStatement = SQLQueryUtils.addJsonValues(previousStatement,
                    getValueFieldsSingleBatch(),
                    attrNativeTypes);
            verify(preparedStatement).setDouble(1, Long.parseLong("1461136795801"));
            verify(preparedStatement).setBoolean(7, true);
            verify(preparedStatement).setString(9, "load_md");
            verify(preparedStatement).addBatch();
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testAddJsonValuesSingleNativeTypes]")
                    + "-  OK  - testAddJsonValuesSingleNativeTypes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddJsonValuesSingleNotNativeTypes() throws SQLException {
        String query = "INSERT INTO example.exampleTable_last_data " +
                "(recvTime,recvTimeS,fiwareServicePath,entityId,entityType,loadStr,loadNumber,load_md) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        boolean attrNativeTypes = false;
        PreparedStatement previousStatement = connection.prepareStatement(query);
        PreparedStatement preparedStatement;
        try {
            preparedStatement = SQLQueryUtils.addJsonValues(previousStatement,
                    getValueFieldsSingleBatch(),
                    attrNativeTypes);
            verify(preparedStatement).setString(1, "1461136795801");
            verify(preparedStatement).setString(7, "true");
            verify(preparedStatement).setString(9, "load_md");
            verify(preparedStatement).addBatch();
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testAddJsonValuesSingleNotNativeTypes]")
                    + "-  OK  - testAddJsonValuesSingleNotNativeTypes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddJsonValuesMultipleNativeTypes() throws SQLException {
        String query = "INSERT INTO example.exampleTable_last_data " +
                "(recvTime,recvTimeS,fiwareServicePath,entityId,entityType,loadStr,loadNumber,load_md) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        boolean attrNativeTypes = true;
        PreparedStatement previousStatement = connection.prepareStatement(query);
        PreparedStatement preparedStatement;
        try {
            preparedStatement = SQLQueryUtils.addJsonValues(previousStatement,
                    getValueFieldsMultipleBatch(),
                    attrNativeTypes);
            verify(preparedStatement).setDouble(1, Long.parseLong("1461136795801"));
            verify(preparedStatement).setDouble(1, Long.parseLong("1461136795802"));
            verify(preparedStatement).setDouble(1, Long.parseLong("1461136795800"));
            verify(preparedStatement).setString(2, "2016-04-20 07:19:55.801");
            verify(preparedStatement, times(3)).setString(4, "entityId1");
            verify(preparedStatement, times(2)).setBoolean(7, false);
            verify(preparedStatement, times(3)).setString(9, "load_md");
            verify(preparedStatement, times(3)).addBatch();
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testAddJsonValuesMultipleNativeTypes]")
                    + "-  OK  - testAddJsonValuesMultipleNativeTypes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddJsonValuesMultipleNoNativeTypes() throws SQLException {
        String query = "INSERT INTO example.exampleTable_last_data " +
                "(recvTime,recvTimeS,fiwareServicePath,entityId,entityType,loadStr,loadNumber,load_md) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        boolean attrNativeTypes = false;
        PreparedStatement previousStatement = connection.prepareStatement(query);
        PreparedStatement preparedStatement;
        try {
            preparedStatement = SQLQueryUtils.addJsonValues(previousStatement,
                    getValueFieldsMultipleBatch(),
                    attrNativeTypes);
            verify(preparedStatement).setString(1, "1461136795801");
            verify(preparedStatement).setString(1, "1461136795802");
            verify(preparedStatement).setString(1, "1461136795800");
            verify(preparedStatement).setString(2, "2016-04-20 07:19:55.801");
            verify(preparedStatement, times(3)).setString(4, "entityId1");
            verify(preparedStatement, times(2)).setString(7, "false");
            verify(preparedStatement, times(3)).setString(9, "load_md");
            verify(preparedStatement, times(3)).addBatch();
            System.out.println(getTestTraceHead("[NGSISQLUtilsTest.testAddJsonValuesMultipleNoNativeTypes]")
                    + "-  OK  - testAddJsonValuesMultipleNoNativeTypes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
