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
 * Authorship: TIS Inc.
 *
 */
package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.backends.elasticsearch.ElasticsearchBackend;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.sinks.NGSISink.Accumulator;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.xml.bind.DatatypeConverter;

import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.mockito.Matchers.anyString;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sink for Elasticsearch.
 *
 * @author Nobuyuki Matsui
 */
@RunWith(Enclosed.class)
public class NGSIElasticsearchSinkTest {

    @RunWith(Parameterized.class)
    public static class ConfigureTest {
        /**
         * setup test class
         */
        @BeforeClass
        public static void setUpClass() {
            LogManager.getRootLogger().setLevel(Level.FATAL);
        } // setUpClass

        @Parameters
        public static Collection<Fixture[]> getParameters() {
            return Arrays.asList(new Fixture[][] {
                {new Fixture(new Data<String, String>(null, "localhost"),
                             new Data<String, String>(null, "9200"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, String>(null, "cygnus"),
                             new Data<String, String>(null, "cygnus_type"),
                             new Data<String, Integer>(null, Integer.valueOf(500)),
                             new Data<String, Integer>(null, Integer.valueOf(100)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, String>(null, "UTC"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, Integer>(null, Integer.valueOf(0)),
                             false, null)},
                {new Fixture(new Data<String, String>("test.example.com", "test.example.com"),
                             new Data<String, String>("1234", "1234"),
                             new Data<String, Boolean>("true", Boolean.valueOf(true)),
                             new Data<String, String>("testIndex", "testIndex"),
                             new Data<String, String>("testType", "testType"),
                             new Data<String, Integer>("2147483647", Integer.valueOf(Integer.MAX_VALUE)),
                             new Data<String, Integer>("-2147483648", Integer.valueOf(Integer.MIN_VALUE)),
                             new Data<String, Boolean>("false", Boolean.valueOf(false)),
                             new Data<String, Boolean>("column", Boolean.valueOf(false)),
                             new Data<String, String>("Asia/Tokyo", "Asia/Tokyo"),
                             new Data<String, Boolean>("true", Boolean.valueOf(true)),
                             new Data<String, Integer>("30", Integer.valueOf(30)),
                             false, null)},
                {new Fixture(new Data<String, String>(null, "localhost"),
                             new Data<String, String>(null, "9200"),
                             new Data<String, Boolean>("false", Boolean.valueOf(false)),
                             new Data<String, String>(null, "cygnus"),
                             new Data<String, String>(null, "cygnus_type"),
                             new Data<String, Integer>("500", Integer.valueOf(500)),
                             new Data<String, Integer>("100", Integer.valueOf(100)),
                             new Data<String, Boolean>("true", Boolean.valueOf(true)),
                             new Data<String, Boolean>("row", Boolean.valueOf(true)),
                             new Data<String, String>(null, "UTC"),
                             new Data<String, Boolean>("false", Boolean.valueOf(false)),
                             new Data<String, Integer>("0", Integer.valueOf(0)),
                             false, null)},
                {new Fixture(new Data<String, String>(null, "localhost"),
                             new Data<String, String>("-1", "-1"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, String>(null, "cygnus"),
                             new Data<String, String>(null, "cygnus_type"),
                             new Data<String, Integer>(null, Integer.valueOf(500)),
                             new Data<String, Integer>(null, Integer.valueOf(100)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, String>(null, "UTC"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, Integer>(null, Integer.valueOf(0)),
                             true, null)},
                {new Fixture(new Data<String, String>(null, "localhost"),
                             new Data<String, String>("65536", "65536"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, String>(null, "cygnus"),
                             new Data<String, String>(null, "cygnus_type"),
                             new Data<String, Integer>(null, Integer.valueOf(500)),
                             new Data<String, Integer>(null, Integer.valueOf(100)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, String>(null, "UTC"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, Integer>(null, Integer.valueOf(0)),
                             true, null)},
                {new Fixture(new Data<String, String>(null, "localhost"),
                             new Data<String, String>("1.5", "1.5"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, String>(null, "cygnus"),
                             new Data<String, String>(null, "cygnus_type"),
                             new Data<String, Integer>(null, Integer.valueOf(500)),
                             new Data<String, Integer>(null, Integer.valueOf(100)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, String>(null, "UTC"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, Integer>(null, Integer.valueOf(0)),
                             true, null)},
                {new Fixture(new Data<String, String>(null, "localhost"),
                             new Data<String, String>(null, "9200"),
                             new Data<String, Boolean>("invalid", Boolean.valueOf(false)),
                             new Data<String, String>(null, "cygnus"),
                             new Data<String, String>(null, "cygnus_type"),
                             new Data<String, Integer>(null, Integer.valueOf(500)),
                             new Data<String, Integer>(null, Integer.valueOf(100)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, String>(null, "UTC"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, Integer>(null, Integer.valueOf(0)),
                             true, null)},
                {new Fixture(new Data<String, String>(null, "localhost"),
                             new Data<String, String>(null, "9200"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, String>(null, "cygnus"),
                             new Data<String, String>(null, "cygnus_type"),
                             new Data<String, Integer>("1.5", Integer.valueOf(0)),
                             new Data<String, Integer>(null, Integer.valueOf(0)),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, String>(null, null),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, Integer>(null, Integer.valueOf(0)),
                             false, java.lang.NumberFormatException.class)},
                {new Fixture(new Data<String, String>(null, "localhost"),
                             new Data<String, String>(null, "9200"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, String>(null, "cygnus"),
                             new Data<String, String>(null, "cygnus_type"),
                             new Data<String, Integer>("2147483647", Integer.valueOf(Integer.MAX_VALUE)),
                             new Data<String, Integer>("2147483648", Integer.valueOf(0)),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, String>(null, null),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, Integer>(null, Integer.valueOf(0)),
                             false, java.lang.NumberFormatException.class)},
                {new Fixture(new Data<String, String>(null, "localhost"),
                             new Data<String, String>(null, "9200"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, String>(null, "cygnus"),
                             new Data<String, String>(null, "cygnus_type"),
                             new Data<String, Integer>(null, Integer.valueOf(500)),
                             new Data<String, Integer>(null, Integer.valueOf(100)),
                             new Data<String, Boolean>("invalid", Boolean.valueOf(false)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, String>(null, "UTC"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, Integer>(null, Integer.valueOf(0)),
                             true, null)},
                {new Fixture(new Data<String, String>(null, "localhost"),
                             new Data<String, String>(null, "9200"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, String>(null, "cygnus"),
                             new Data<String, String>(null, "cygnus_type"),
                             new Data<String, Integer>(null, Integer.valueOf(500)),
                             new Data<String, Integer>(null, Integer.valueOf(100)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, Boolean>("true", Boolean.valueOf(false)),
                             new Data<String, String>(null, "UTC"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, Integer>(null, Integer.valueOf(0)),
                             true, null)},
                {new Fixture(new Data<String, String>(null, "localhost"),
                             new Data<String, String>(null, "9200"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, String>(null, "cygnus"),
                             new Data<String, String>(null, "cygnus_type"),
                             new Data<String, Integer>(null, Integer.valueOf(500)),
                             new Data<String, Integer>(null, Integer.valueOf(100)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, String>(null, "UTC"),
                             new Data<String, Boolean>("invalid", Boolean.valueOf(false)),
                             new Data<String, Integer>(null, Integer.valueOf(0)),
                             true, null)},
                {new Fixture(new Data<String, String>(null, "localhost"),
                             new Data<String, String>(null, "9200"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, String>(null, "cygnus"),
                             new Data<String, String>(null, "cygnus_type"),
                             new Data<String, Integer>(null, Integer.valueOf(500)),
                             new Data<String, Integer>("-2147483648", Integer.valueOf(Integer.MIN_VALUE)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, String>(null, "UTC"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, Integer>("-2147483649", Integer.valueOf(0)),
                             false, java.lang.NumberFormatException.class)},
                {new Fixture(new Data<String, String>(null, "localhost"),
                             new Data<String, String>(null, "9200"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, String>(null, "cygnus"),
                             new Data<String, String>(null, "cygnus_type"),
                             new Data<String, Integer>(null, Integer.valueOf(500)),
                             new Data<String, Integer>(null, Integer.valueOf(100)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, Boolean>(null, Boolean.valueOf(true)),
                             new Data<String, String>(null, "UTC"),
                             new Data<String, Boolean>(null, Boolean.valueOf(false)),
                             new Data<String, Integer>("", Integer.valueOf(0)),
                             false, java.lang.NumberFormatException.class)}
            });
        } // getParameters
        private Fixture fixture;
        public ConfigureTest(Fixture fixture) {
            this.fixture = fixture;
        } // constructor

        /**
         * [NGSIElasticsearchSinkTest$ConfigureTest.testConfigure]
         * Test configure()
         *
         */
        @Test
        public void testConfigure() {
            System.out.println(getTestTraceHead("[NGSIElasticsearchSinkTest.testConfigure] - fixture=" + fixture));

            NGSIElasticsearchSink sink = new NGSIElasticsearchSink();
            if (fixture.exceptionClass == null) {
                sink.configure(fixture.createContext());
            } else {
                try {
                    sink.configure(fixture.createContext());
                    fail();
                } catch (Exception e) {
                    assertTrue(fixture.exceptionClass.isAssignableFrom(e.getClass()));
                } // try-catch
            } // if
            assertEquals(fixture.elasticsearchHost.expected, sink.getElasticsearchHost());
            assertEquals(fixture.elasticsearchPort.expected, sink.getElasticsearchPort());
            assertEquals(fixture.ssl.expected, sink.getSSL());
            assertEquals(fixture.indexPrefix.expected, sink.getIndexPrefix());
            assertEquals(fixture.mappingType.expected, sink.getMappingType());
            assertEquals(fixture.backendMaxConns.expected.intValue(), sink.getBackendMaxConns());
            assertEquals(fixture.backendMaxConnsPerRoute.expected.intValue(), sink.getBackendMaxConnsPerRoute());
            assertEquals(fixture.ignoreWhiteSpaces.expected, sink.getIgnoreWhiteSpaces());
            assertEquals(fixture.rowAttrPersistence.expected, sink.getRowAttrPersistence());
            assertEquals(fixture.timezone.expected, sink.getTimezone());
            assertEquals(fixture.castValue.expected, sink.getCastValue());
            assertEquals(fixture.cacheFlashIntervalSec.expected.intValue(), sink.getCacheFlashIntervalSec());
            assertEquals(fixture.invalidConfiguration, sink.getInvalidConfiguration());
        } // testConfigure
    } // ConfigureTest

    public static class StartAndStopInternalTest {
        /**
         * setup test class
         */
        @BeforeClass
        public static void setUpClass() {
            LogManager.getRootLogger().setLevel(Level.FATAL);
        } // setUpClass

        @Mock
        private ElasticsearchBackend mockBackend;

        private Fixture fixture;
        private Map<String, List<Map<String, String>>> aggregations;
        private List<Map<String, String>> data1;
        private List<Map<String, String>> data2;
        private List<Map<String, String>> data3;
        /**
         * setup test case
         */
        @Before
        public void setUp() throws Exception {
            fixture = new Fixture(new Data<String, String>(null, "localhost"),
                                  new Data<String, String>(null, "9200"),
                                  new Data<String, Boolean>(null, Boolean.valueOf(false)),
                                  new Data<String, String>(null, "cygnus"),
                                  new Data<String, String>(null, "cygnus_type"),
                                  new Data<String, Integer>(null, Integer.valueOf(500)),
                                  new Data<String, Integer>(null, Integer.valueOf(100)),
                                  new Data<String, Boolean>(null, Boolean.valueOf(true)),
                                  new Data<String, Boolean>(null, Boolean.valueOf(true)),
                                  new Data<String, String>(null, "UTC"),
                                  new Data<String, Boolean>(null, Boolean.valueOf(false)),
                                  new Data<String, Integer>(null, Integer.valueOf(0)),
                                  false, null);

            aggregations = new TreeMap<>();

            data1 = new ArrayList<>();
            data1.add(new TreeMap<String, String>(){
                {
                    put("data", "{\"message\": \"test data 1-1\"}");
                    put("recvTimeTs", "test_recvTimeTs_1-1");
                }
            });
            data1.add(new TreeMap<String, String>(){
                {
                    put("data", "{\"message\": \"test data 1-2\"}");
                    put("recvTimeTs", "test_recvTimeTs_1-2");
                }
            });
            aggregations.put("idx1", data1);

            data2 = new ArrayList<>();
            data2.add(new TreeMap<String, String>(){
                {
                    put("data", "{\"message\": \"test data 2-1\"}");
                    put("recvTimeTs", "test_recvTimeTs_2-1");
                }
            });
            aggregations.put("idx2", data2);

            data3 = new ArrayList<>();
            data3.add(new TreeMap<String, String>(){
                {
                    put("data", "{\"message\": \"test data 3-1\"}");
                    put("recvTimeTs", "test_recvTimeTs_3-1");
                }
            });

            MockitoAnnotations.initMocks(this);
            when(mockBackend.bulkInsert(anyString(), anyString(), Matchers.<List<Map<String, String>>>any())).thenReturn(null);
        }

        /**
         * [NGSIElasticsearchSinkTest$StartAndStopInternalTest.testStartAndStopInternalWithoutCache]
         * Test startInternal() : cacheFlashIntervalSec = 0
         *
         */
        @Test
        public void testStartAndStopInternalWithoutCache() {
            System.out.println(getTestTraceHead("[NGSIElasticsearchSinkTest.testStartAndStopInternal] - cacheFlashIntervalSec = 0"));

            NGSIElasticsearchSink sink = new NGSIElasticsearchSink();
            sink.setPersistenceBackend(mockBackend);

            fixture.cacheFlashIntervalSec.value = "0";
            sink.configure(fixture.createContext());

            sink.setAggregations(aggregations);
            assertEquals(2, sink.getAggregations().size());
            sink.startInternal();

            try {
                verify(mockBackend, times(0)).bulkInsert(anyString(), anyString(), Matchers.<List<Map<String, String>>>any());
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals(2, sink.getAggregations().size());
            assertNull(sink.getScheduler());

            sink.stopInternal();
            try {
                verify(mockBackend, times(1)).bulkInsert("idx1", "cygnus_type", data1);
                verify(mockBackend, times(1)).bulkInsert("idx2", "cygnus_type", data2);
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals(0, sink.getAggregations().size());
        } // testStartAndStopInternalWithoutCache

        /**
         * [NGSIElasticsearchSinkTest$StartAndStopInternalTest.testStartAndStopInternalWithCache]
         * Test startInternal() : cacheFlashIntervalSec = 3
         *
         */
        @Test
        public void testStartAndStopInternalWithCache() {
            System.out.println(getTestTraceHead("[NGSIElasticsearchSinkTest.testStartAndStopInternal] - cacheFlashIntervalSec = 1"));

            NGSIElasticsearchSink sink = new NGSIElasticsearchSink();
            sink.setPersistenceBackend(mockBackend);

            fixture.cacheFlashIntervalSec.value = "1";
            sink.configure(fixture.createContext());

            assertEquals(0, sink.getAggregations().size());
            sink.startInternal();

            try {
                verify(mockBackend, times(0)).bulkInsert(anyString(), anyString(), Matchers.<List<Map<String, String>>>any());
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertNotNull(sink.getScheduler());

            // after 200 ms (the scheduler has been started but the persistentTask has not been called yet)
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
            }
            sink.setAggregations(aggregations);
            try {
                verify(mockBackend, times(0)).bulkInsert(anyString(), anyString(), Matchers.<List<Map<String, String>>>any());
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals(2, sink.getAggregations().size());

            // after 1000 ms (the persistentTask has been called once)
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
            }
            try {
                verify(mockBackend, times(1)).bulkInsert("idx1", "cygnus_type", data1);
                verify(mockBackend, times(1)).bulkInsert("idx2", "cygnus_type", data2);
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals(0, sink.getAggregations().size());

            aggregations.put("idx1", data3);
            sink.stopInternal();
            try {
                verify(mockBackend, times(1)).bulkInsert("idx1", "cygnus_type", data3);
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals(0, sink.getAggregations().size());

            assertTrue(sink.getScheduler().isShutdown());
            assertTrue(sink.getScheduler().isTerminated());
        } // testStartAndStopInternalWithCache
    } // StartAndStopInternalTest

    @RunWith(Parameterized.class)
    public static class PersistBatchTest {
        /**
         * setup test class
         */
        @BeforeClass
        public static void setUpClass() {
            LogManager.getRootLogger().setLevel(Level.FATAL);
        } // setUpClass

        @Mock
        private ElasticsearchBackend mockBackend;

        @Captor
        private ArgumentCaptor<List<Map<String, String>>> dataCaptor;

        @Captor
        private ArgumentCaptor<String> idxCaptor;

        @Captor
        private ArgumentCaptor<String> mappingTypeCaptor;

        @Parameters
        public static Collection<String[]> getParameters() {
            List<String[]> params = new ArrayList<>();
            for (String castValueStr : new String[]{"true", "false"}) {
                for(String timezoneStr : new String[]{"UTC", "Asia/Tokyo"}) {
                    params.add(new String[]{castValueStr, timezoneStr});
                }
            }
            return params;
        }
        private String castValue;
        private String timezone;
        public PersistBatchTest(String castValueStr, String timezoneStr) {
            this.castValue = castValueStr;
            this.timezone = timezoneStr;
        }

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

        private Fixture fixture;
        /**
         * setup test case
         */
        @Before
        public void setUp() throws Exception {
            fixture = new Fixture(new Data<String, String>(null, "localhost"),
                                  new Data<String, String>(null, "9200"),
                                  new Data<String, Boolean>(null, Boolean.valueOf(false)),
                                  new Data<String, String>(null, "cygnus"),
                                  new Data<String, String>(null, "cygnus_type"),
                                  new Data<String, Integer>(null, Integer.valueOf(500)),
                                  new Data<String, Integer>(null, Integer.valueOf(100)),
                                  new Data<String, Boolean>(null, Boolean.valueOf(true)),
                                  new Data<String, Boolean>(null, Boolean.valueOf(true)),
                                  new Data<String, String>(null, "UTC"),
                                  new Data<String, Boolean>(null, Boolean.valueOf(false)),
                                  new Data<String, Integer>(null, Integer.valueOf(0)),
                                  false, null);

            MockitoAnnotations.initMocks(this);
            when(mockBackend.bulkInsert(anyString(), anyString(), Matchers.<List<Map<String, String>>>any())).thenReturn(null);
        } // setUp

        @Ignore
        private NGSIBatch createBatch(NGSISink sink, String contextElementStr) throws Exception {
            final String timestamp = "1234567890";
            final String correlatorId = "1234567891";
            final String serviceStr = "room_service";
            final String servicePathStr = "/room_service_path";

            Accumulator acc = sink.new Accumulator();
            acc.initialize(new Date().getTime());
            Map<String, String> headers = new HashMap<>();
            headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
            headers.put(CommonConstants.HEADER_CORRELATOR_ID, correlatorId);
            headers.put(CommonConstants.HEADER_FIWARE_SERVICE, serviceStr);
            headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, servicePathStr);
            headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, serviceStr);
            headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, servicePathStr);
            ContextElement contextElement = NGSIUtilsForTests.createJsonContextElement(contextElementStr);
            acc.accumulate(new NGSIEvent(headers, contextElement.toString().getBytes(), contextElement, contextElement));
            return acc.getBatch();
        } // createBatch

        /**
         * [NGSIElasticsearchSinkTest$PersistBatchTest.testPersistBatchWithoutData]
         * Test persistBatch(null)
         *
         */
        @Test
        public void testPersistBatchWithoutData() {
            System.out.println(getTestTraceHead("[NGSIElasticsearchSinkTest.testPersistBatchWithoutData] - castValue=" + castValue + ", timezone=" + timezone));

            NGSIElasticsearchSink sink = new NGSIElasticsearchSink();
            sink.setPersistenceBackend(mockBackend);

            fixture.castValue.value = castValue;
            fixture.timezone.value = timezone;
            sink.configure(fixture.createContext());

            try {
                sink.persistBatch(null);
                verify(mockBackend, times(0)).bulkInsert(anyString(), anyString(), Matchers.<List<Map<String, String>>>any());
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals(0, sink.getAggregations().size());
            assertNull(sink.getScheduler());
        } // testPersistBatchWithoutData

        /**
         * [NGSIElasticsearchSinkTest$PersistBatchTest.testPersistBatchWithOneAttributeRow]
         * Test persistBatch(contextElement with one attribute) - row style
         *
         */
        @Test
        public void testPersistBatchWithOneAttributeRow() throws Exception {
            System.out.println(getTestTraceHead("[NGSIElasticsearchSinkTest.testPersistBatchWithOneAttributeRow] - castValue=" + castValue + ", timezone=" + timezone));

            NGSIElasticsearchSink sink = new NGSIElasticsearchSink();
            sink.setPersistenceBackend(mockBackend);

            fixture.rowAttrPersistence.value = "row";
            fixture.castValue.value = castValue;
            fixture.timezone.value = timezone;
            sink.configure(fixture.createContext());

            NGSIBatch batch = createBatch(sink, contextElementStr1);
            sink.persistBatch(batch);
            verify(mockBackend, times(1)).bulkInsert(idxCaptor.capture(), mappingTypeCaptor.capture(), dataCaptor.capture());

            assertEquals("cygnus-room_service-room_service_path-room1-room-1970.01.15", idxCaptor.getValue());
            assertEquals("cygnus_type", mappingTypeCaptor.getValue());

            List<Map<String, String>> requestedData = dataCaptor.getValue();
            assertEquals(1, requestedData.size());
            assertEquals(2, requestedData.get(0).size());
            assertEquals("1234567890", requestedData.get(0).get("recvTimeTs"));
            assertNotNull(requestedData.get(0).get("data"));
            JSONObject jdata = (JSONObject)(new JSONParser()).parse(requestedData.get(0).get("data"));
            if (timezone == "Asia/Tokyo") {
                assertEquals("1970-01-15T15:56:07.890+0900", jdata.get("recvTime"));
            } else {
                assertEquals("1970-01-15T06:56:07.890+0000", jdata.get("recvTime"));
            }
            assertEquals("Room1", jdata.get("entityId"));
            assertEquals("Room", jdata.get("entityType"));
            assertEquals("temperature", jdata.get("attrName"));
            assertEquals("number", jdata.get("attrType"));
            if (castValue == "true") {
                assertTrue(jdata.get("attrValue") instanceof Double);
                assertEquals(26.5, jdata.get("attrValue"));
            } else {
                assertTrue(jdata.get("attrValue") instanceof String);
                assertEquals("26.5", jdata.get("attrValue"));
            }
            assertTrue(jdata.get("attrMetadata") instanceof JSONArray);
            assertEquals(0, ((JSONArray)jdata.get("attrMetadata")).size());
            assertNull(jdata.get("temperature"));
            assertEquals(0, sink.getAggregations().size());
            assertNull(sink.getScheduler());
        } // testPersistBatchWithOneAttributeRow

        /**
         * [NGSIElasticsearchSinkTest$PersistBatchTest.testPersistBatchWithOneAttributeColumn]
         * Test persistBatch(contextElement with one attribute) - column style
         *
         */
        @Test
        public void testPersistBatchWithOneAttributeColumn() throws Exception {
            System.out.println(getTestTraceHead("[NGSIElasticsearchSinkTest.testPersistBatchWithOneAttributeColumn] - castValue=" + castValue + ", timezone=" + timezone));

            NGSIElasticsearchSink sink = new NGSIElasticsearchSink();
            sink.setPersistenceBackend(mockBackend);

            fixture.rowAttrPersistence.value = "column";
            fixture.castValue.value = castValue;
            fixture.timezone.value = timezone;
            sink.configure(fixture.createContext());

            NGSIBatch batch = createBatch(sink, contextElementStr1);
            sink.persistBatch(batch);
            verify(mockBackend, times(1)).bulkInsert(idxCaptor.capture(), mappingTypeCaptor.capture(), dataCaptor.capture());

            byte[] bytes = MessageDigest.getInstance("md5").digest("temperature:".getBytes(StandardCharsets.UTF_8));
            String hash = DatatypeConverter.printHexBinary(bytes).toLowerCase();

            assertEquals("cygnus-room_service-room_service_path-room1-room-" + hash + "-1970.01.15", idxCaptor.getValue());
            assertEquals("cygnus_type", mappingTypeCaptor.getValue());

            List<Map<String, String>> requestedData = dataCaptor.getValue();
            assertEquals(1, requestedData.size());
            assertEquals(2, requestedData.get(0).size());
            assertEquals("1234567890", requestedData.get(0).get("recvTimeTs"));
            assertNotNull(requestedData.get(0).get("data"));
            JSONObject jdata = (JSONObject)(new JSONParser()).parse(requestedData.get(0).get("data"));
            if (timezone == "Asia/Tokyo") {
                assertEquals("1970-01-15T15:56:07.890+0900", jdata.get("recvTime"));
            } else {
                assertEquals("1970-01-15T06:56:07.890+0000", jdata.get("recvTime"));
            }
            assertEquals("Room1", jdata.get("entityId"));
            assertEquals("Room", jdata.get("entityType"));
            if (castValue == "true") {
                assertTrue(jdata.get("temperature") instanceof Double);
                assertEquals(26.5, jdata.get("temperature"));
            } else {
                assertTrue(jdata.get("temperature") instanceof String);
                assertEquals("26.5", jdata.get("temperature"));
            }
            assertNull(jdata.get("attrName"));
            assertNull(jdata.get("attrType"));
            assertNull(jdata.get("attrValue"));
            assertNull(jdata.get("attrMetadata"));
            assertEquals(0, sink.getAggregations().size());
            assertNull(sink.getScheduler());
        } // testPersistBatchWithOneAttributeColumn

        /**
         * [NGSIElasticsearchSinkTest$PersistBatchTest.testPersistBatchWithTwoAttributes]
         * Test persistBatch(contextElement with two attributes) - row style
         *
         */
        @Test
        public void testPersistBatchWithTwoAttributesIgnoreEmptyRow() throws Exception {
            System.out.println(getTestTraceHead("[NGSIElasticsearchSinkTest.testPersistBatchWithTwoAttributesIgnoreEmptyRow] - castValue=" + castValue + ", timezone=" + timezone));

            NGSIElasticsearchSink sink = new NGSIElasticsearchSink();
            sink.setPersistenceBackend(mockBackend);

            fixture.rowAttrPersistence.value = "row";
            fixture.ignoreWhiteSpaces.value = "true";
            fixture.castValue.value = castValue;
            fixture.timezone.value = timezone;
            sink.configure(fixture.createContext());

            NGSIBatch batch = createBatch(sink, contextElementStr2);
            sink.persistBatch(batch);
            verify(mockBackend, times(1)).bulkInsert(idxCaptor.capture(), mappingTypeCaptor.capture(), dataCaptor.capture());

            assertEquals("cygnus-room_service-room_service_path-room1-room-1970.01.15", idxCaptor.getValue());
            assertEquals("cygnus_type", mappingTypeCaptor.getValue());

            List<Map<String, String>> requestedData = dataCaptor.getValue();
            assertEquals(1, requestedData.size());
            assertEquals(2, requestedData.get(0).size());
            assertEquals("1234567890", requestedData.get(0).get("recvTimeTs"));
            assertNotNull(requestedData.get(0).get("data"));
            JSONObject jdata = (JSONObject)(new JSONParser()).parse(requestedData.get(0).get("data"));
            if (timezone == "Asia/Tokyo") {
                assertEquals("1970-01-15T15:56:07.890+0900", jdata.get("recvTime"));
            } else {
                assertEquals("1970-01-15T06:56:07.890+0000", jdata.get("recvTime"));
            }
            assertEquals("Room1", jdata.get("entityId"));
            assertEquals("Room", jdata.get("entityType"));
            assertEquals("temperature", jdata.get("attrName"));
            assertEquals("number", jdata.get("attrType"));
            if (castValue == "true") {
                assertTrue(jdata.get("attrValue") instanceof Double);
                assertEquals(26.5, jdata.get("attrValue"));
            } else {
                assertTrue(jdata.get("attrValue") instanceof String);
                assertEquals("26.5", jdata.get("attrValue"));
            }
            assertTrue(jdata.get("attrMetadata") instanceof JSONArray);
            assertEquals(0, ((JSONArray)jdata.get("attrMetadata")).size());
            assertNull(jdata.get("temperature"));
            assertEquals(0, sink.getAggregations().size());
            assertNull(sink.getScheduler());
        } // testPersistBatchWithTwoAttributesIgnoreEmptyRow

        /**
         * [NGSIElasticsearchSinkTest$PersistBatchTest.testPersistBatchWithTwoAttributesWithEmptyRow]
         * Test persistBatch(contextElement with two attributes) - row style
         *
         */
        @Test
        public void testPersistBatchWithTwoAttributesWithEmptyRow() throws Exception {
            System.out.println(getTestTraceHead("[NGSIElasticsearchSinkTest.testPersistBatchWithTwoAttributesWithEmptyRow] - castValue=" + castValue + ", timezone=" + timezone));

            NGSIElasticsearchSink sink = new NGSIElasticsearchSink();
            sink.setPersistenceBackend(mockBackend);

            fixture.rowAttrPersistence.value = "row";
            fixture.ignoreWhiteSpaces.value = "false";
            fixture.castValue.value = castValue;
            fixture.timezone.value = timezone;
            sink.configure(fixture.createContext());

            NGSIBatch batch = createBatch(sink, contextElementStr2);
            sink.persistBatch(batch);
            verify(mockBackend, times(1)).bulkInsert(idxCaptor.capture(), mappingTypeCaptor.capture(), dataCaptor.capture());

            assertEquals("cygnus-room_service-room_service_path-room1-room-1970.01.15", idxCaptor.getValue());
            assertEquals("cygnus_type", mappingTypeCaptor.getValue());

            List<Map<String, String>> requestedData = dataCaptor.getValue();
            assertEquals(2, requestedData.size());
            assertEquals(2, requestedData.get(0).size());
            assertEquals("1234567890", requestedData.get(0).get("recvTimeTs"));
            assertNotNull(requestedData.get(0).get("data"));
            JSONObject jdata = (JSONObject)(new JSONParser()).parse(requestedData.get(0).get("data"));
            if (timezone == "Asia/Tokyo") {
                assertEquals("1970-01-15T15:56:07.890+0900", jdata.get("recvTime"));
            } else {
                assertEquals("1970-01-15T06:56:07.890+0000", jdata.get("recvTime"));
            }
            assertEquals("Room1", jdata.get("entityId"));
            assertEquals("Room", jdata.get("entityType"));
            assertEquals("temperature", jdata.get("attrName"));
            assertEquals("number", jdata.get("attrType"));
            if (castValue == "true") {
                assertTrue(jdata.get("attrValue") instanceof Double);
                assertEquals(26.5, jdata.get("attrValue"));
            } else {
                assertTrue(jdata.get("attrValue") instanceof String);
                assertEquals("26.5", jdata.get("attrValue"));
            }
            assertTrue(jdata.get("attrMetadata") instanceof JSONArray);
            assertEquals(0, ((JSONArray)jdata.get("attrMetadata")).size());
            assertNull(jdata.get("temperature"));
            assertEquals(2, requestedData.get(1).size());
            assertEquals("1234567890", requestedData.get(1).get("recvTimeTs"));
            assertNotNull(requestedData.get(1).get("data"));
            jdata = (JSONObject)(new JSONParser()).parse(requestedData.get(1).get("data"));
            if (timezone == "Asia/Tokyo") {
                assertEquals("1970-01-15T15:56:07.890+0900", jdata.get("recvTime"));
            } else {
                assertEquals("1970-01-15T06:56:07.890+0000", jdata.get("recvTime"));
            }
            assertEquals("Room1", jdata.get("entityId"));
            assertEquals("Room", jdata.get("entityType"));
            assertEquals("roomtype", jdata.get("attrName"));
            assertEquals("string", jdata.get("attrType"));
            assertTrue(jdata.get("attrValue") instanceof String);
            assertEquals("", jdata.get("attrValue"));
            assertTrue(jdata.get("attrMetadata") instanceof JSONArray);
            assertEquals(0, ((JSONArray)jdata.get("attrMetadata")).size());
            assertNull(jdata.get("roomtype"));
            assertEquals(0, sink.getAggregations().size());
            assertNull(sink.getScheduler());
        } // testPersistBatchWithTwoAttributesWithEmptyRow

        /**
         * [NGSIElasticsearchSinkTest$PersistBatchTest.testPersistBatchWithTwoAttributesIgnoreEmptyColumn]
         * Test persistBatch(contextElement with two attributes) - column style
         *
         */
        @Test
        public void testPersistBatchWithTwoAttributesIgnoreEmptyColumn() throws Exception {
            System.out.println(getTestTraceHead("[NGSIElasticsearchSinkTest.testPersistBatchWithTwoAttributesIgnoreEmptyColumn] - castValue=" + castValue + ", timezone=" + timezone));

            NGSIElasticsearchSink sink = new NGSIElasticsearchSink();
            sink.setPersistenceBackend(mockBackend);

            fixture.rowAttrPersistence.value = "column";
            fixture.ignoreWhiteSpaces.value = "true";
            fixture.castValue.value = castValue;
            fixture.timezone.value = timezone;
            sink.configure(fixture.createContext());

            NGSIBatch batch = createBatch(sink, contextElementStr2);
            sink.persistBatch(batch);
            verify(mockBackend, times(1)).bulkInsert(idxCaptor.capture(), mappingTypeCaptor.capture(), dataCaptor.capture());

            byte[] bytes = MessageDigest.getInstance("md5").digest("temperature:".getBytes(StandardCharsets.UTF_8));
            String hash = DatatypeConverter.printHexBinary(bytes).toLowerCase();

            assertEquals("cygnus-room_service-room_service_path-room1-room-" + hash + "-1970.01.15", idxCaptor.getValue());
            assertEquals("cygnus_type", mappingTypeCaptor.getValue());

            List<Map<String, String>> requestedData = dataCaptor.getValue();
            assertEquals(1, requestedData.size());
            assertEquals(2, requestedData.get(0).size());
            assertEquals("1234567890", requestedData.get(0).get("recvTimeTs"));
            assertNotNull(requestedData.get(0).get("data"));
            JSONObject jdata = (JSONObject)(new JSONParser()).parse(requestedData.get(0).get("data"));
            if (timezone == "Asia/Tokyo") {
                assertEquals("1970-01-15T15:56:07.890+0900", jdata.get("recvTime"));
            } else {
                assertEquals("1970-01-15T06:56:07.890+0000", jdata.get("recvTime"));
            }
            assertEquals("Room1", jdata.get("entityId"));
            assertEquals("Room", jdata.get("entityType"));
            if (castValue == "true") {
                assertTrue(jdata.get("temperature") instanceof Double);
                assertEquals(26.5, jdata.get("temperature"));
            } else {
                assertTrue(jdata.get("temperature") instanceof String);
                assertEquals("26.5", jdata.get("temperature"));
            }
            assertNull(jdata.get("attrName"));
            assertNull(jdata.get("attrType"));
            assertNull(jdata.get("attrValue"));
            assertNull(jdata.get("attrMetadata"));
            assertEquals(0, sink.getAggregations().size());
            assertNull(sink.getScheduler());
        } // testPersistBatchWithTwoAttributesIgnoreEmptyColumn

        /**
         * [NGSIElasticsearchSinkTest$PersistBatchTest.testPersistBatchWithTwoAttributesWithEmptyColumn]
         * Test persistBatch(contextElement with two attributes) - column style
         *
         */
        @Test
        public void testPersistBatchWithTwoAttributesWithEmptyColumn() throws Exception {
            System.out.println(getTestTraceHead("[NGSIElasticsearchSinkTest.testPersistBatchWithTwoAttributesWithEmptyColumn] - castValue=" + castValue + ", timezone=" + timezone));

            NGSIElasticsearchSink sink = new NGSIElasticsearchSink();
            sink.setPersistenceBackend(mockBackend);

            fixture.rowAttrPersistence.value = "column";
            fixture.ignoreWhiteSpaces.value = "false";
            fixture.castValue.value = castValue;
            fixture.timezone.value = timezone;
            sink.configure(fixture.createContext());

            NGSIBatch batch = createBatch(sink, contextElementStr2);
            sink.persistBatch(batch);
            verify(mockBackend, times(1)).bulkInsert(idxCaptor.capture(), mappingTypeCaptor.capture(), dataCaptor.capture());

            byte[] bytes = MessageDigest.getInstance("md5").digest("roomtype:temperature:".getBytes(StandardCharsets.UTF_8));
            String hash = DatatypeConverter.printHexBinary(bytes).toLowerCase();

            assertEquals("cygnus-room_service-room_service_path-room1-room-" + hash + "-1970.01.15", idxCaptor.getValue());
            assertEquals("cygnus_type", mappingTypeCaptor.getValue());

            List<Map<String, String>> requestedData = dataCaptor.getValue();
            assertEquals(1, requestedData.size());
            assertEquals(2, requestedData.get(0).size());
            assertEquals("1234567890", requestedData.get(0).get("recvTimeTs"));
            assertNotNull(requestedData.get(0).get("data"));
            JSONObject jdata = (JSONObject)(new JSONParser()).parse(requestedData.get(0).get("data"));
            if (timezone == "Asia/Tokyo") {
                assertEquals("1970-01-15T15:56:07.890+0900", jdata.get("recvTime"));
            } else {
                assertEquals("1970-01-15T06:56:07.890+0000", jdata.get("recvTime"));
            }
            assertEquals("Room1", jdata.get("entityId"));
            assertEquals("Room", jdata.get("entityType"));
            if (castValue == "true") {
                assertTrue(jdata.get("temperature") instanceof Double);
                assertEquals(26.5, jdata.get("temperature"));
            } else {
                assertTrue(jdata.get("temperature") instanceof String);
                assertEquals("26.5", jdata.get("temperature"));
            }
            assertTrue(jdata.get("roomtype") instanceof String);
            assertEquals("", jdata.get("roomtype"));
            assertNull(jdata.get("attrName"));
            assertNull(jdata.get("attrType"));
            assertNull(jdata.get("attrValue"));
            assertNull(jdata.get("attrMetadata"));
            assertEquals(0, sink.getAggregations().size());
            assertNull(sink.getScheduler());
        } // testPersistBatchWithTwoAttributesWithEmptyColumn

        /**
         * [NGSIElasticsearchSinkTest$PersistBatchTest.testPersistBatchWithTwoAttributesWithEmptyRow]
         * Test persistBatch(contextElement with two attributes) - row style
         *
         */
        @Test
        public void testPersistBatchWithThreeAttributesWithMetadataRow() throws Exception {
            System.out.println(getTestTraceHead("[NGSIElasticsearchSinkTest.testPersistBatchWithThreeAttributesWithMetadataRow] - castValue=" + castValue + ", timezone=" + timezone));

            NGSIElasticsearchSink sink = new NGSIElasticsearchSink();
            sink.setPersistenceBackend(mockBackend);

            fixture.rowAttrPersistence.value = "row";
            fixture.castValue.value = castValue;
            fixture.timezone.value = timezone;
            sink.configure(fixture.createContext());

            NGSIBatch batch = createBatch(sink, contextElementStr3);
            sink.persistBatch(batch);
            verify(mockBackend, times(1)).bulkInsert(idxCaptor.capture(), mappingTypeCaptor.capture(), dataCaptor.capture());

            if (timezone == "Asia/Tokyo") {
                assertEquals("cygnus-room_service-room_service_path-room1-room-2018.01.02", idxCaptor.getValue());
            } else {
                assertEquals("cygnus-room_service-room_service_path-room1-room-2018.01.01", idxCaptor.getValue());
            }
            assertEquals("cygnus_type", mappingTypeCaptor.getValue());

            List<Map<String, String>> requestedData = dataCaptor.getValue();
            assertEquals(3, requestedData.size());
            assertEquals(2, requestedData.get(0).size());
            assertEquals("1514829845678", requestedData.get(0).get("recvTimeTs"));
            assertNotNull(requestedData.get(0).get("data"));
            JSONObject jdata = (JSONObject)(new JSONParser()).parse(requestedData.get(0).get("data"));
            if (timezone == "Asia/Tokyo") {
                assertEquals("2018-01-02T03:04:05.678+0900", jdata.get("recvTime"));
            } else {
                assertEquals("2018-01-01T18:04:05.678+0000", jdata.get("recvTime"));
            }
            assertEquals("Room1", jdata.get("entityId"));
            assertEquals("Room", jdata.get("entityType"));
            assertEquals("temperature", jdata.get("attrName"));
            assertEquals("number", jdata.get("attrType"));
            if (castValue == "true") {
                assertTrue(jdata.get("attrValue") instanceof Double);
                assertEquals(26.5, jdata.get("attrValue"));
            } else {
                assertTrue(jdata.get("attrValue") instanceof String);
                assertEquals("26.5", jdata.get("attrValue"));
            }
            assertTrue(jdata.get("attrMetadata") instanceof JSONArray);
            assertEquals(1, ((JSONArray)jdata.get("attrMetadata")).size());
            JSONObject metadata = (JSONObject)((JSONArray)jdata.get("attrMetadata")).get(0);
            assertEquals("TimeInstant", metadata.get("name"));
            assertEquals("ISO8601", metadata.get("type"));
            assertEquals("2018-01-02T03:04:05.678+0900", metadata.get("value"));
            assertNull(jdata.get("temperature"));
            assertEquals(2, requestedData.get(1).size());
            assertEquals("1514829845678", requestedData.get(1).get("recvTimeTs"));
            assertNotNull(requestedData.get(1).get("data"));
            jdata = (JSONObject)(new JSONParser()).parse(requestedData.get(1).get("data"));
            if (timezone == "Asia/Tokyo") {
                assertEquals("2018-01-02T03:04:05.678+0900", jdata.get("recvTime"));
            } else {
                assertEquals("2018-01-01T18:04:05.678+0000", jdata.get("recvTime"));
            }
            assertEquals("Room1", jdata.get("entityId"));
            assertEquals("Room", jdata.get("entityType"));
            assertEquals("roomtype", jdata.get("attrName"));
            assertEquals("string", jdata.get("attrType"));
            assertTrue(jdata.get("attrValue") instanceof String);
            assertEquals("single", jdata.get("attrValue"));
            assertTrue(jdata.get("attrMetadata") instanceof JSONArray);
            assertEquals(1, ((JSONArray)jdata.get("attrMetadata")).size());
            metadata = (JSONObject)((JSONArray)jdata.get("attrMetadata")).get(0);
            assertEquals("TimeInstant", metadata.get("name"));
            assertEquals("ISO8601", metadata.get("type"));
            assertEquals("2018-01-02T03:04:05.678+0900", metadata.get("value"));
            assertNull(jdata.get("roomtype"));
            assertEquals(0, sink.getAggregations().size());
            assertNull(sink.getScheduler());
            assertEquals(2, requestedData.get(2).size());
            assertEquals("1514829845678", requestedData.get(2).get("recvTimeTs"));
            assertNotNull(requestedData.get(2).get("data"));
            jdata = (JSONObject)(new JSONParser()).parse(requestedData.get(2).get("data"));
            if (timezone == "Asia/Tokyo") {
                assertEquals("2018-01-02T03:04:05.678+0900", jdata.get("recvTime"));
            } else {
                assertEquals("2018-01-01T18:04:05.678+0000", jdata.get("recvTime"));
            }
            assertEquals("Room1", jdata.get("entityId"));
            assertEquals("Room", jdata.get("entityType"));
            assertEquals("smoking", jdata.get("attrName"));
            assertEquals("boolean", jdata.get("attrType"));
            if (castValue == "true") {
                assertTrue(jdata.get("attrValue") instanceof Boolean);
                assertEquals(true, jdata.get("attrValue"));
            } else {
                assertTrue(jdata.get("attrValue") instanceof String);
                assertEquals("true", jdata.get("attrValue"));
            }
            assertTrue(jdata.get("attrMetadata") instanceof JSONArray);
            assertEquals(1, ((JSONArray)jdata.get("attrMetadata")).size());
            metadata = (JSONObject)((JSONArray)jdata.get("attrMetadata")).get(0);
            assertEquals("TimeInstant", metadata.get("name"));
            assertEquals("ISO8601", metadata.get("type"));
            assertEquals("2018-01-02T03:04:05.678+0900", metadata.get("value"));
            assertNull(jdata.get("smoking"));
        } // testPersistBatchWithThreeAttributesWithMetadataRow

        /**
         * [NGSIElasticsearchSinkTest$PersistBatchTest.testPersistBatchWithThreeAttributesWithMetadataColumn]
         * Test persistBatch(contextElement with two attributes) - column style
         *
         */
        @Test
        public void testPersistBatchWithThreeAttributesWithMetadataColumn() throws Exception {
            System.out.println(getTestTraceHead("[NGSIElasticsearchSinkTest.testPersistBatchWithThreeAttributesWithMetadataColumn] - castValue=" + castValue + ", timezone=" + timezone));

            NGSIElasticsearchSink sink = new NGSIElasticsearchSink();
            sink.setPersistenceBackend(mockBackend);

            fixture.rowAttrPersistence.value = "column";
            fixture.castValue.value = castValue;
            fixture.timezone.value = timezone;
            sink.configure(fixture.createContext());

            NGSIBatch batch = createBatch(sink, contextElementStr3);
            sink.persistBatch(batch);
            verify(mockBackend, times(1)).bulkInsert(idxCaptor.capture(), mappingTypeCaptor.capture(), dataCaptor.capture());

            byte[] bytes = MessageDigest.getInstance("md5").digest("roomtype:smoking:temperature:".getBytes(StandardCharsets.UTF_8));
            String hash = DatatypeConverter.printHexBinary(bytes).toLowerCase();

            if (timezone == "Asia/Tokyo") {
                assertEquals("cygnus-room_service-room_service_path-room1-room-" + hash + "-2018.01.02", idxCaptor.getValue());
            } else {
                assertEquals("cygnus-room_service-room_service_path-room1-room-" + hash + "-2018.01.01", idxCaptor.getValue());
            }
            assertEquals("cygnus_type", mappingTypeCaptor.getValue());

            List<Map<String, String>> requestedData = dataCaptor.getValue();
            assertEquals(1, requestedData.size());
            assertEquals(2, requestedData.get(0).size());
            assertEquals("1514829845678", requestedData.get(0).get("recvTimeTs"));
            assertNotNull(requestedData.get(0).get("data"));
            JSONObject jdata = (JSONObject)(new JSONParser()).parse(requestedData.get(0).get("data"));
            if (timezone == "Asia/Tokyo") {
                assertEquals("2018-01-02T03:04:05.678+0900", jdata.get("recvTime"));
            } else {
                assertEquals("2018-01-01T18:04:05.678+0000", jdata.get("recvTime"));
            }
            assertEquals("Room1", jdata.get("entityId"));
            assertEquals("Room", jdata.get("entityType"));
            if (castValue == "true") {
                assertTrue(jdata.get("temperature") instanceof Double);
                assertEquals(26.5, jdata.get("temperature"));
            } else {
                assertTrue(jdata.get("temperature") instanceof String);
                assertEquals("26.5", jdata.get("temperature"));
            }
            assertTrue(jdata.get("roomtype") instanceof String);
            assertEquals("single", jdata.get("roomtype"));
            if (castValue == "true") {
                assertTrue(jdata.get("smoking") instanceof Boolean);
                assertEquals(true, jdata.get("smoking"));
            } else {
                assertTrue(jdata.get("temperature") instanceof String);
                assertEquals("true", jdata.get("smoking"));
            }
            assertNull(jdata.get("attrName"));
            assertNull(jdata.get("attrType"));
            assertNull(jdata.get("attrValue"));
            assertNull(jdata.get("attrMetadata"));
            assertEquals(0, sink.getAggregations().size());
            assertNull(sink.getScheduler());
        } // testPersistBatchWithThreeAttributesWithMetadataColumn
    } // PersistBatchTest

    @RunWith(Parameterized.class)
    public static class IndexNameTest {
        /**
         * setup test class
         */
        @BeforeClass
        public static void setUpClass() {
            LogManager.getRootLogger().setLevel(Level.FATAL);
        } // setUpClass

        @Parameters
        public static Collection<String[]> getParameters() {
            List<String[]> params = new ArrayList<>();
            for (String prefix : new String[]{"prefix", "PREFIX", "-prefix", "_prefix"}) {
                for(String service : new String[]{"service", "SERVICE", "service/\\*?\"<>| ,#:"}) {
                    for(String servicepath : new String[]{"servicepath", "SERVICEPATH", "servicepath/\\*?\"<>| ,#:"}) {
                        for (String entityid : new String[]{"entityid", "ENTITYID", "entityid/\\*?\"<>| ,#:"}) {
                            for (String entitytype: new String[]{"entitytype", "ENTITYTYPE", "entitytype/\\*?\"<>| ,#:"}) {
                                params.add(new String[]{prefix, service, servicepath, entityid, entitytype});
                            }
                        }
                    }
                }
            }
            return params;
        }

        private String prefix;
        private String service;
        private String servicepath;
        private String entityid;
        private String entitytype;
        public IndexNameTest(String prefix, String service, String servicepath, String entityid, String entitytype) {
            this.prefix = prefix;
            this.service = service;
            this.servicepath = servicepath;
            this.entityid = entityid;
            this.entitytype = entitytype;
        } // IndexNameTest

        @Test
        public void testIndexNameWithReplacedChar() {
            System.out.println(getTestTraceHead("[NGSIElasticsearchSinkTest.testIndexNameWithReplacedChar] - prefix=" + prefix + ", service=" + service + ", servicepath=" + servicepath + ", entityid=" + entityid + ", entitytype=" + entitytype));
            NGSIElasticsearchSink sink = new NGSIElasticsearchSink();

            StringBuilder idx = new StringBuilder();
            if (prefix == "prefix" || prefix == "PREFIX") {
                idx.append("prefix");
            } else {
                idx.append("idx").append(prefix);
            }
            idx.append("-");
            if (service == "service" || service == "SERVICE") {
                idx.append("service");
            } else {
                idx.append("service------------");
            }
            if (servicepath == "servicepath" || servicepath == "SERVICEPATH") {
                idx.append("servicepath");
            } else {
                idx.append("servicepath------------");
            }
            idx.append("-");
            if (entityid == "entityid" || entityid == "ENTITYID") {
                idx.append("entityid");
            } else {
                idx.append("entityid------------");
            }
            idx.append("-");
            if (entitytype == "entitytype" || entitytype == "ENTITYTYPE") {
                idx.append("entitytype");
            } else {
                idx.append("entitytype------------");
            }

            assertEquals(idx.toString(), sink.getIndexName(prefix, service, servicepath, entityid, entitytype));
        } // testIndexNameWithReplacedChar
    } // IndexNameTest

    @Ignore
    private static class Fixture {
        public Data<String, String> elasticsearchHost;
        public Data<String, String> elasticsearchPort;
        public Data<String, Boolean> ssl;
        public Data<String, String> indexPrefix;
        public Data<String, String> mappingType;
        public Data<String, Integer> backendMaxConns;
        public Data<String, Integer> backendMaxConnsPerRoute;
        public Data<String, Boolean> ignoreWhiteSpaces;
        public Data<String, Boolean> rowAttrPersistence;
        public Data<String, String> timezone;
        public Data<String, Boolean> castValue;
        public Data<String, Integer> cacheFlashIntervalSec;
        public boolean invalidConfiguration;
        public Class<? extends Exception> exceptionClass;

        public Fixture(Data<String, String> elasticsearchHost,
                       Data<String, String> elasticsearchPort,
                       Data<String, Boolean> ssl,
                       Data<String, String> indexPrefix,
                       Data<String, String> mappingType,
                       Data<String, Integer> backendMaxConns,
                       Data<String, Integer> backendMaxConnsPerRoute,
                       Data<String, Boolean> ignoreWhiteSpaces,
                       Data<String, Boolean> rowAttrPersistence,
                       Data<String, String> timezone,
                       Data<String, Boolean> castValue,
                       Data<String, Integer> cacheFlashIntervalSec,
                       boolean invalidConfiguration,
                       Class<? extends Exception> exceptionClass) {
            this.elasticsearchHost = elasticsearchHost;
            this.elasticsearchPort = elasticsearchPort;
            this.ssl = ssl;
            this.indexPrefix = indexPrefix;
            this.mappingType = mappingType;
            this.backendMaxConns = backendMaxConns;
            this.backendMaxConnsPerRoute = backendMaxConnsPerRoute;
            this.ignoreWhiteSpaces = ignoreWhiteSpaces;
            this.rowAttrPersistence = rowAttrPersistence;
            this.timezone = timezone;
            this.castValue = castValue;
            this.cacheFlashIntervalSec = cacheFlashIntervalSec;
            this.invalidConfiguration = invalidConfiguration;
            this.exceptionClass = exceptionClass;
        } // constructor

        public Context createContext() {
            Context context = new Context();
            context.put("elasticsearch_host", this.elasticsearchHost.value);
            context.put("elasticsearch_port", this.elasticsearchPort.value);
            context.put("ssl", this.ssl.value);
            context.put("index_prefix", this.indexPrefix.value);
            context.put("mapping_type", this.mappingType.value);
            context.put("backend.max_conns", this.backendMaxConns.value);
            context.put("backend.max_conns_per_route", this.backendMaxConnsPerRoute.value);
            context.put("ignore_white_spaces", this.ignoreWhiteSpaces.value);
            context.put("attr_persistence", this.rowAttrPersistence.value);
            context.put("timezone", this.timezone.value);
            context.put("cast_value", this.castValue.value);
            context.put("cache_flash_interval_sec", this.cacheFlashIntervalSec.value);
            return context;
        } // createContext

        public String toString() {
            return "elasticsearchHost=" + this.elasticsearchHost + ", " +
                   "elasticsearchPort=" + this.elasticsearchPort + ", " +
                   "ssl=" + this.ssl + ", " +
                   "indexPrefix=" + this.indexPrefix + ", " +
                   "mappingType=" + this.mappingType + ", " +
                   "backendMaxConns=" + this.backendMaxConns + ", " +
                   "backendMaxConnsPerRoute=" + this.backendMaxConnsPerRoute + ", " +
                   "ignoreWhiteSpaces=" + this.ignoreWhiteSpaces + ", " +
                   "rowAttrPersistence=" + this.rowAttrPersistence + ", " +
                   "timezone=" + this.timezone + ", " +
                   "castValue=" + this.castValue + ", " +
                   "cacheFlashIntervalSec=" + this.cacheFlashIntervalSec + ", " +
                   "invalidConfiguration=" + this.invalidConfiguration;
        } // toString
    } // Fixture

    @Ignore
    private static class Data<V, E> {
        public V value;
        public E expected;

        public Data(V value, E expected) {
            this.value = value;
            this.expected = expected;
        } // constructor

        public String toString() {
            return "(value=" + this.value + ", expected=" + this.expected + ")";
        } // toString
    } // Data<V, E>
} // NGSIElasticsearchSinkTest
