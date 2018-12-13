/**
 * Copyright 2018 Telefonica Investigación y Desarrollo, S.A.U
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
 *
 * Authorship: SlicingDice
 *
 */

package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.backends.slicingdice.SlicingDiceBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(Enclosed.class)
public class NGSISlicingDiceSinkTest {

    /**
     * Constructor
     */
    public NGSISlicingDiceSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSISlicingDiceSinkTest

    public static class ConfigureTest {
        @Test
        public void testConfigureGetConfiguration() {
            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "false";
            final String dataModel = "dm-by-service-path";

            final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, null));

            try {
                Assert.assertEquals(databaseKey, sink.getDatabaseKey());
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                        + "-  OK  - 'database_key=oasdisadnasoi' was configured");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                        + "- FAIL - 'database_key=oasdisadnasoi' was not configured");
                throw e;
            } // try catch

            try {
                Assert.assertFalse(sink.isAutoCreate());
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                        + "-  OK  - 'auto_create=false' was configured");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                        + "- FAIL - 'auto_create=false' was not configured");
                throw e;
            } // try catch
        } // testConfigureGetConfiguration

        @Test
        public void testConfigureAutoCreateTrue() {
            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "true";
            final String dataModel = "dm-by-service-path";

            final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, null));

            try {
                Assert.assertTrue(sink.isAutoCreate());
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                        + "-  OK  - 'auto_create=false' was configured");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                        + "- FAIL - 'auto_create=false' was not configured");
                throw e;
            } // try catch
        } // testConfigureAutoCreateTrue

        @Test
        public void testInvalidConfiguration() {
            final String databaseKey = null;
            final String isAutoCreate = "true";
            final String dataModel = "dm-by-service-path";

            final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, null));

            try {
                assertTrue(sink.getInvalidConfiguration());
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                        + "-  OK  - 'database_key=null' detected");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                        + "- FAIL - database_key=null' was not detected");
                throw e;
            } // try catch
        } // testConfigureAutoCreateTrue
    } // ConfigureTest

    public static class AuxiliaryMethodsTest {
        @Test
        public void testBuildDimensionNameOnlyServicePath() throws CygnusBadConfiguration {
            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "true";
            final String dataModel = "dm-by-service-path";

            final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, null));

            String servicePath = "/users_a_x_15_$";
            String entity = null; // irrelevant for this test
            String attribute = null; // irrelevant for this test
            Assert.assertEquals("users-a-x-15--", sink.buildDimensionName(servicePath, entity,
                    attribute));
        } // testBuildDimensionNameOnlyServicePath

        @Test
        public void testBuildDimensionNameEntity() throws CygnusBadConfiguration {
            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "true";
            final String dataModel = "dm-by-entity";

            final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, null));

            String servicePath = "/users_a_x_15_$";
            String entity = "some_entity_x";
            String attribute = null; // irrelevant for this test
            Assert.assertEquals("users-a-x-15---some-entity-x", sink.buildDimensionName(
                    servicePath, entity, attribute));
        } // testBuildDimensionNameEntity

        @Test
        public void testBuildDimensionNameAttribute() throws CygnusBadConfiguration {
            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "true";
            final String dataModel = "dm-by-attribute";

            final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, null));

            String servicePath = "/users_a_x_15_$";
            String entity = "some_entity_x";
            String attribute = "some_attribute_%";
            Assert.assertEquals("users-a-x-15---some-entity-x-some-attribute--", sink.buildDimensionName(
                    servicePath, entity, attribute));
        } // testBuildDimensionNameAttribute

        @Test
        public void testBuildDimensionNameEncodingOnlyServicePath() throws CygnusBadConfiguration {
            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "true";
            final String dataModel = "dm-by-service-path";
            final String encoding = "true";

            final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, encoding));

            String servicePath = "/users_a_x_15_$";
            String entity = null; // irrelevant for this test
            String attribute = null; // irrelevant for this test
            Assert.assertEquals("x002fusersx005fax005fxx005f15x005fx0024", sink.buildDimensionName(servicePath, entity,
                    attribute));
        } // testBuildDimensionNameEncodingOnlyServicePath

        @Test
        public void testBuildDimensionNameEncodingEntity() throws CygnusBadConfiguration {
            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "true";
            final String dataModel = "dm-by-entity";
            final String encoding = "true";

            final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, encoding));

            String servicePath = "/users_a_x";
            String entity = "some";
            String attribute = null; // irrelevant for this test
            Assert.assertEquals("x002fusersx005fax005fxxffffsome", sink.buildDimensionName(
                    servicePath, entity, attribute));
        } // testBuildDimensionNameEncodingEntity

        @Test
        public void testBuildDimensionNameEncodingAttribute() throws CygnusBadConfiguration {
            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "true";
            final String dataModel = "dm-by-attribute";
            final String encoding = "true";

            final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, encoding));

            String servicePath = "/users_";
            String entity = "some_";
            String attribute = "attribute";
            Assert.assertEquals("x002fusersx005fxffffsomex005fxffffattribute", sink.buildDimensionName(
                    servicePath, entity, attribute));
        } // testBuildDimensionNameEncodingAttribute

        @Test
        public void testBuildDimensionNameLengthOnlyServicePath() throws CygnusBadConfiguration {
            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "true";
            final String dataModel = "dm-by-service-path";
            final String encoding = "true";

            final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, encoding));

            String servicePath = "/users_a_x_15__some_length_name_for_the_service_path";
            String entity = null; // irrelevant for this test
            String attribute = null; // irrelevant for this test
            try {
                sink.buildDimensionName(servicePath, entity, attribute);
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.buildDimensionName]")
                        + "-  FAIL  - A table name length greater than 64 characters has not been detected");
                Assert.fail();
            } catch (final Exception e) {
                Assert.assertTrue(true);
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.buildDimensionName]")
                        + "-  OK  - A table name length greater than 64 characters has been detected");
            }
        } // testBuildDimensionNameLengthOnlyServicePath

        @Test
        public void testBuildDimensionNameLengthEntity() throws CygnusBadConfiguration {
            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "true";
            final String dataModel = "dm-by-entity";
            final String encoding = "true";

            final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, encoding));

            String servicePath = "/users_a_x";
            String entity = "some_giant_name_on_the_entity";
            String attribute = null; // irrelevant for this test
            try {
                sink.buildDimensionName(servicePath, entity, attribute);
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.buildDimensionName]")
                        + "-  FAIL  - A table name length greater than 64 characters has not been detected");
                Assert.fail();
            } catch (final Exception e) {
                Assert.assertTrue(true);
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.buildDimensionName]")
                        + "-  OK  - A table name length greater than 64 characters has been detected");
            }
        } // testBuildDimensionNameLengthEntity

        @Test
        public void testBuildDimensionNameLengthAttribute() throws CygnusBadConfiguration {
            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "true";
            final String dataModel = "dm-by-attribute";
            final String encoding = "true";

            final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, encoding));

            String servicePath = "/users_";
            String entity = "some_";
            String attribute = "some_giant_attribute_name_for_attribute";
            try {
                sink.buildDimensionName(servicePath, entity, attribute);
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.buildDimensionName]")
                        + "-  FAIL  - A table name length greater than 64 characters has not been detected");
                Assert.fail();
            } catch (final Exception e) {
                Assert.assertTrue(true);
                System.out.println(getTestTraceHead("[NGSISlicingDiceSink.buildDimensionName]")
                        + "-  OK  - A table name length greater than 64 characters has been detected");
            }
        } // testBuildDimensionNameLengthAttribute

        @Test
        public void testStringEncode() {
            final String[][] tests = new String[][]{
                    new String[]{"temperature", "temperature"},
                    new String[]{"code_review", "code-review"},
                    new String[]{"columns-123", "columns-123"},
                    new String[]{"columns_123", "columns-123"},
                    new String[]{"some_path", "some-path"}
            };

            // with slash
            for (final String[] test : tests) {
                final String result = NGSISlicingDiceSink.encode("/" + test[0], true, false);
                Assert.assertEquals(test[1], result);
            }

            // without slash
            for (final String[] test : tests) {
                final String result = NGSISlicingDiceSink.encode(test[0], false, true);
                Assert.assertEquals(test[1], result);
            }
        } // testStringEncodeWithoutSlash
    } // AuxiliaryMethodsTest

    public static class PersistBatchTest {

        private static final String DATABASE_KEY = "oiasdiondasidasndasomn";

        @Mock
        private SlicingDiceBackendImpl mockBackend;

        @Captor
        private ArgumentCaptor<String> idxCaptor;

        private final String contextElementStr1 = ""
                + "{"
                +   "\"attributes\" : ["
                +     "{"
                +       "\"name\" : \"temperature\","
                +       "\"type\" : \"number\","
                +       "\"value\" : \"26.5\""
                +     "}"
                +   "],"
                +   "\"type\" : \"Room\","
                +   "\"isPattern\" : \"false\","
                +   "\"id\" : \"Room1\""
                + "}";
        private final String contextElementStr2 = ""
                + "{"
                +   "\"attributes\" : ["
                +     "{"
                +       "\"name\" : \"temperature\","
                +       "\"type\" : \"number\","
                +       "\"value\" : \"26.5\""
                +     "},"
                +     "{"
                +       "\"name\" : \"roomtype\","
                +       "\"type\" : \"string\","
                +       "\"value\" : \"\""
                +     "}"
                +   "],"
                +   "\"type\" : \"Room\","
                +   "\"isPattern\" : \"false\","
                +   "\"id\" : \"Room1\""
                + "}";
        private final String contextElementStr3 = ""
                + "{"
                +   "\"attributes\" : ["
                +     "{"
                +       "\"name\" : \"temperature\","
                +       "\"type\" : \"number\","
                +       "\"value\" : \"26.5\","
                +       "\"metadatas\" : ["
                +         "{"
                +           "\"name\" : \"TimeInstant\","
                +           "\"type\" : \"ISO8601\","
                +           "\"value\" : \"2018-01-02T03:04:05.678+0900\""
                +         "}"
                +       "]"
                +     "},"
                +     "{"
                +       "\"name\" : \"roomtype\","
                +       "\"type\" : \"string\","
                +       "\"value\" : \"single\","
                +       "\"metadatas\" : ["
                +         "{"
                +           "\"name\" : \"TimeInstant\","
                +           "\"type\" : \"ISO8601\","
                +           "\"value\" : \"2018-01-02T03:04:05.678+0900\""
                +         "}"
                +       "]"
                +     "},"
                +     "{"
                +       "\"name\" : \"smoking\","
                +       "\"type\" : \"boolean\","
                +       "\"value\" : \"true\","
                +       "\"metadatas\" : ["
                +         "{"
                +           "\"name\" : \"TimeInstant\","
                +           "\"type\" : \"ISO8601\","
                +           "\"value\" : \"2018-01-02T03:04:05.678+0900\""
                +         "}"
                +       "]"
                +     "}"
                +   "],"
                +   "\"type\" : \"Room\","
                +   "\"isPattern\" : \"false\","
                +   "\"id\" : \"Room1\""
                + "}";

        @Before
        public void setUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            doNothing().when(mockBackend).createColumns(anyString());
            doNothing().when(mockBackend).insertContextData(anyString());
        }

        @Test
        public void testPersistBatchWithoutData() {
            System.out.println(getTestTraceHead("[NGSISlicingDiceSink.persistBatch]"));

            NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.setPersistenceBackend(mockBackend);

            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "true";
            final String dataModel = "dm-by-service-path";
            final String encoding = null;

            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, encoding));

            try {
                sink.persistBatch(null);
                verify(mockBackend, times(0)).createColumns(anyString());
                verify(mockBackend, times(0)).insertContextData(anyString());
            } catch (final Exception e) {
                Assert.fail(e.getMessage());
            } // try catch
        } // testPersistBatchWithoutData

        @Test
        public void testPersistBatchWithFirstExample() throws Exception {
            System.out.println(getTestTraceHead("[NGSISlicingDiceSink.persistBatch]"));

            NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.setPersistenceBackend(mockBackend);

            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "false";
            final String dataModel = "dm-by-service-path";
            final String encoding = null;

            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, encoding));

            final NGSIBatch batch = createBatch(sink, contextElementStr1);
            sink.persistBatch(batch);

            verify(mockBackend, times(1)).createColumns(idxCaptor.capture());
            Assert.assertEquals("[{\"name\": \"fiwareServicePath\", \"api-name\": " +
                    "\"fiwareServicePath\", \"type\": \"string-event\", \"description\": " +
                    "\"Created using CYGNUS.\", \"dimension\": \"room-service-path\"},{\"name\": " +
                    "\"temperature\", \"api-name\": \"temperature\", \"type\": \"decimal-event\", " +
                    "\"description\": \"Created using CYGNUS.\", \"decimal-places\": 5, " +
                    "\"dimension\": \"room-service-path\"}]", idxCaptor.getValue());

            verify(mockBackend, times(1)).insertContextData(idxCaptor.capture());
            Assert.assertEquals("{\"Room1\": {\"dimension\": \"room-service-path\", " +
                    "\"fiwareServicePath\": [{\"value\": \"/room_service_path\", \"date\": " +
                    "\"1970-01-15T06:56:07.890\"}], \"temperature\": [{\"value\": 26.5, \"date\": " +
                    "\"1970-01-15T06:56:07.890\"}]}\"auto-create\": [\"dimension\"]}", idxCaptor.getValue());
        } // testPersistBatchWithOneAttribute

        @Test
        public void testPersistBatchWithSecondExample() throws Exception {
            System.out.println(getTestTraceHead("[NGSISlicingDiceSink.persistBatch]"));

            NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.setPersistenceBackend(mockBackend);

            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "false";
            final String dataModel = "dm-by-entity";
            final String encoding = null;

            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, encoding));

            final NGSIBatch batch = createBatch(sink, contextElementStr2);
            sink.persistBatch(batch);

            verify(mockBackend, times(1)).createColumns(idxCaptor.capture());
            Assert.assertEquals("[{\"name\": \"fiwareServicePath\", \"api-name\": " +
                    "\"fiwareServicePath\", \"type\": \"string-event\", \"description\": " +
                    "\"Created using CYGNUS.\", \"dimension\": \"room-service-path-Room1-Room\"}," +
                    "{\"name\": \"temperature\", \"api-name\": \"temperature\", \"type\": " +
                    "\"decimal-event\", \"description\": \"Created using CYGNUS.\", " +
                    "\"decimal-places\": 5, \"dimension\": \"room-service-path-Room1-Room\"}," +
                    "{\"name\": \"roomtype\", \"api-name\": \"roomtype\", \"type\": " +
                    "\"string-event\", \"description\": \"Created using CYGNUS.\", \"dimension\": " +
                    "\"room-service-path-Room1-Room\"}]", idxCaptor.getValue());

            verify(mockBackend, times(1)).insertContextData(idxCaptor.capture());
            Assert.assertEquals("{\"Room1\": {\"dimension\": \"room-service-path-Room1" +
                    "-Room\", \"fiwareServicePath\": [{\"value\": \"/room_service_path\", \"date\":" +
                    " \"1970-01-15T06:56:07.890\"}], \"temperature\": [{\"value\": 26.5, \"date\":" +
                    " \"1970-01-15T06:56:07.890\"}], \"roomtype\": [{\"value\": \"\", \"date\":" +
                    " \"1970-01-15T06:56:07.890\"}]}\"auto-create\": [\"dimension\"]}",
                    idxCaptor.getValue());
        } // testPersistBatchWithSecondExample

        @Test
        public void testPersistBatchWithThirdExample() throws Exception {
            System.out.println(getTestTraceHead("[NGSISlicingDiceSink.persistBatch]"));

            NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
            sink.setPersistenceBackend(mockBackend);

            final String databaseKey = "oasdisadnasoi";
            final String isAutoCreate = "false";
            final String dataModel = "dm-by-entity";
            final String encoding = null;

            sink.configure(createContext(databaseKey, isAutoCreate, dataModel, encoding));

            final NGSIBatch batch = createBatch(sink, contextElementStr3);
            sink.persistBatch(batch);

            verify(mockBackend, times(1)).createColumns(idxCaptor.capture());
            Assert.assertEquals("[{\"name\": \"fiwareServicePath\", \"api-name\": " +
                    "\"fiwareServicePath\", \"type\": \"string-event\", \"description\": " +
                    "\"Created using CYGNUS.\", \"dimension\": \"room-service-path-Room1-Room\"}," +
                    "{\"name\": \"temperature\", \"api-name\": \"temperature\", \"type\": " +
                    "\"decimal-event\", \"description\": \"Created using CYGNUS.\", " +
                    "\"decimal-places\": 5, \"dimension\": \"room-service-path-Room1-Room\"}," +
                    "{\"name\": \"roomtype\", \"api-name\": \"roomtype\", \"type\": " +
                    "\"string-event\", \"description\": \"Created using CYGNUS.\", \"dimension\": " +
                    "\"room-service-path-Room1-Room\"},{\"name\": \"smoking\", \"api-name\": " +
                    "\"smoking\", \"type\": \"string-event\", \"description\": " +
                    "\"Created using CYGNUS.\", \"dimension\": \"room-service-path-Room1-Room\"}]",
                    idxCaptor.getValue());

            verify(mockBackend, times(1)).insertContextData(idxCaptor.capture());
            Assert.assertEquals("{\"Room1\": {\"dimension\": \"room-service-path-Room1-" +
                            "Room\", \"fiwareServicePath\": [{\"value\": \"/room_service_path\", " +
                            "\"date\": \"1970-01-15T06:56:07.890\"}], \"temperature\": [{\"value\":" +
                            " 26.5, \"date\": \"1970-01-15T06:56:07.890\"}], \"roomtype\": " +
                            "[{\"value\": \"single\", \"date\": \"1970-01-15T06:56:07.890\"}], " +
                            "\"smoking\": [{\"value\": \"true\", \"date\": \"1970-01-15T06:56:" +
                            "07.890\"}]}\"auto-create\": [\"dimension\"]}",
                    idxCaptor.getValue());
        } // testPersistBatchWithSecondExample

        @Ignore
        private NGSIBatch createBatch(NGSISink sink, String contextElementStr) throws Exception {
            final String timestamp = "1234567890";
            final String correlatorId = "1234567891";
            final String serviceStr = "room_service";
            final String servicePathStr = "/room_service_path";
            NGSISink.Accumulator acc = sink.new Accumulator();
            acc.initialize(new Date().getTime());
            Map<String, String> headers = new ConcurrentHashMap<>();
            headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
            headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
            headers.put(CommonConstants.HEADER_FIWARE_SERVICE, serviceStr);
            headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, servicePathStr);
            headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, serviceStr);
            headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, servicePathStr);
            NotifyContextRequest.ContextElement contextElement = NGSIUtilsForTests.createJsonContextElement(contextElementStr);
            acc.accumulate(new NGSIEvent(headers, contextElement.toString().getBytes(), contextElement, contextElement));
            return acc.getBatch();
        } // createBatch
    }

    private static Context createContext(final String databaseKey, final String autoCreate,
                                         final String dataModel, final String encoding) {
        Context context = new Context();
        context.put("database_key", databaseKey);
        context.put("auto_create", autoCreate);
        context.put("data_model", dataModel);
        context.put("enable_encoding", encoding);
        return context;
    } // createContext
}
