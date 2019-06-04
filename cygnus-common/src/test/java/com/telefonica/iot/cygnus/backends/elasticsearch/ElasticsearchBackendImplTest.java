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
package com.telefonica.iot.cygnus.backends.elasticsearch;

import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author Nobuyuki Matsui
 */
@RunWith(Enclosed.class)
public class ElasticsearchBackendImplTest {
    // constants
    private final static String host = "localhost";
    private final static String port = "9200";
    private final static int maxConns = 50;
    private final static int maxConnsPerRoute = 10;
    private final static String idx = "idx";
    private final static String type = "type";

    private static void assertRequestHeader(HttpPost requested, String schema) {
        assertEquals("POST", requested.getMethod());
        assertEquals(String.format("%s://%s:%s/%s/%s/_bulk", schema, host, port, idx, type), requested.getURI().toString());
        assertEquals(2, requested.getAllHeaders().length);
        assertEquals(1, requested.getHeaders("Content-type").length);
        assertEquals(1, requested.getFirstHeader("Content-type").getElements().length);
        assertEquals("application/json", requested.getFirstHeader("Content-type").getElements()[0].getName());
        assertEquals(1, requested.getHeaders("Accept").length);
        assertEquals(1, requested.getFirstHeader("Accept").getElements().length);
        assertEquals("application/json", requested.getFirstHeader("Accept").getElements()[0].getName());
    } // assertRequestHeader

    @RunWith(Parameterized.class)
    public static class HttpSuccessTest {
        /**
         * setup test class
         */
        @BeforeClass
        public static void setUpClass() {
            LogManager.getRootLogger().setLevel(Level.FATAL);
        } // setUpClass

        @Mock
        private HttpClient mockHttpClient;

        @Captor
        private ArgumentCaptor<HttpPost> requestCaptor;

        @Parameters
        public static Collection<Object[]> getParameters() {
            return Arrays.asList(new Object[][] {
                {"http", false},
                {"https", true}
            });
        } // getParameters

        private String schema;
        private boolean flag;

        public HttpSuccessTest(String schema, boolean flag) {
            this.schema = schema;
            this.flag = flag;
        } // constructor

        /**
         * setup test case
         *
         * @throws Exception
         */
        @Before
        public void setUp() throws Exception {
            BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "ok");
            response.setEntity(new StringEntity("{\"result\": {\"whatever\":\"whatever\"}}"));

            MockitoAnnotations.initMocks(this);
            when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(response);
        } // setUp

        /**
         * [ElasticsearchBackendImplTest$HttpSuccessTest.testBulkInsert_woData]
         * Test to insert no data successfully.
         */
        @Test
        public void testBulkInsert_woData() {
            System.out.println(getTestTraceHead(String.format("[ElasticsearchBackendImplTest.bulkInsert OK, TLS=%b, Data=0]", flag)));

            List<Map<String, String>> data = new ArrayList<Map<String, String>>();
            String expected = "";

            assertBulkInsert(data, expected);
        } // testBulkInsert_woData

        /**
         * [ElasticsearchBackendImplTest$SuccessTest.testBulkInsert_oneData]
         * Test to insert a data successfully.
         */
        @Test
        public void testBulkInsert_oneData() {
            System.out.println(getTestTraceHead(String.format("[ElasticsearchBackendImplTest.bulkInsert OK, TLS=%b, Data=1]", flag)));

            List<Map<String, String>> data = new ArrayList<Map<String, String>>();
            data.add(new HashMap<String, String>(){
                {
                    put("recvTimeTs", "test_recvTimeTs_1");
                    put("data", "{\"message\": \"test data 1\"}");
                }
            });
            String recvTimeTs1Hash = "599F4FB6AFDB1BEF51126B47DAC2696F";
            String expected = "";
            expected += String.format("{\"index\":{\"_id\":\"test_recvTimeTs_1-%s\"}}\n", recvTimeTs1Hash);
            expected += String.format("{\"message\": \"test data 1\"}\n");

            assertBulkInsert(data, expected);
        } // testBulkInsert_oneData

        /**
         * [ElasticsearchBackendImplTest$SuccessTest.testBulkInsert_twoData]
         * Test to insert two data successfully.
         */
        @Test
        public void testBulkInsert_twoData() {
            System.out.println(getTestTraceHead(String.format("[ElasticsearchBackendImplTest.bulkInsert OK, TLS=%b, Data=2]", flag)));

            List<Map<String, String>> data = new ArrayList<Map<String, String>>();
            data.add(new HashMap<String, String>(){
                {
                    put("recvTimeTs", "test_recvTimeTs_1");
                    put("data", "{\"message\": \"test data 1\"}");
                }
            });
            String recvTimeTs1Hash = "599F4FB6AFDB1BEF51126B47DAC2696F";
            data.add(new HashMap<String, String>(){
                {
                    put("recvTimeTs", "test_recvTimeTs_2");
                    put("data", "{\"message\": \"test data 2\"}");
                }
            });
            String recvTimeTs2Hash = "6E4BD4F33D72977355D951C986D7DCF8";
            String expected = "";
            expected += String.format("{\"index\":{\"_id\":\"test_recvTimeTs_1-%s\"}}\n", recvTimeTs1Hash);
            expected += String.format("{\"message\": \"test data 1\"}\n");
            expected += String.format("{\"index\":{\"_id\":\"test_recvTimeTs_2-%s\"}}\n", recvTimeTs2Hash);
            expected += String.format("{\"message\": \"test data 2\"}\n");

            assertBulkInsert(data, expected);
        } // testBulkInsert_oneData

        private void assertBulkInsert(List<Map<String, String>> data, String expected) {
            try {
                ElasticsearchBackendImpl backend = new ElasticsearchBackendImpl(host, port, flag, maxConns, maxConnsPerRoute);
                backend.setHttpClient(mockHttpClient);
                backend.bulkInsert(idx, type, data);

                verify(mockHttpClient, times(1)).execute(requestCaptor.capture());

                HttpPost requested = requestCaptor.getValue();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                requested.getEntity().writeTo(out);

                assertRequestHeader(requested, schema);
                assertEquals(expected, out.toString());
            } catch (Exception e) {
                fail(e.getMessage());
            } // try-catch
        } // assertBulkInsert
    } // HttpSuccessTest

    @RunWith(Parameterized.class)
    public static class HttpFailureTest {
        /**
         * setup test class
         */
        @BeforeClass
        public static void setUpClass() {
            LogManager.getRootLogger().setLevel(Level.FATAL);
        } // setUpClass

        @Mock
        private HttpClient mockHttpClient;

        @Captor
        private ArgumentCaptor<HttpPost> requestCaptor;

        @Parameters
        public static Collection<Object[]> getParameters() {
            return Arrays.asList(new Object[][] {
                {"http", false},
                {"https", true}
            });
        } // getParameters

        private String schema;
        private boolean flag;

        public HttpFailureTest(String schema, boolean flag) {
            this.schema = schema;
            this.flag = flag;
        } // constructor

        private int statusCode = 500;

        /**
         * setup test case
         *
         * @throws Exception
         */
        @Before
        public void setUp() throws Exception {
            BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), statusCode, "Internal Server Error");
            response.setEntity(new StringEntity("{\"result\": {\"whatever\":\"whatever\"}}"));

            MockitoAnnotations.initMocks(this);
            when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(response);
        } // setUp

        /**
         * [ElasticsearchBackendImplTest$HttpFailureTest.testBulkInsert_woData]
         * Test to insert no data unsuccessfully.
         */
        @Test
        public void testBulkInsert_woData() {
            System.out.println(getTestTraceHead(String.format("[ElasticsearchBackendImplTest.bulkInsert Failure, TLS=%b, StatusCode=%d]", flag, statusCode)));

            List<Map<String, String>> data = new ArrayList<Map<String, String>>();
            String expected = "";

            try {
                ElasticsearchBackendImpl backend = new ElasticsearchBackendImpl(host, port, flag, maxConns, maxConnsPerRoute);
                backend.setHttpClient(mockHttpClient);
                backend.bulkInsert(idx, type, data);
            } catch (CygnusPersistenceError e) {
                assertEquals("CygnusPersistenceError. Could not insert (index=idx, type=type, jsonLines=). ", e.getMessage());
                try {
                    verify(mockHttpClient, times(1)).execute(requestCaptor.capture());

                    HttpPost requested = requestCaptor.getValue();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    requested.getEntity().writeTo(out);

                    assertRequestHeader(requested, schema);
                    assertEquals(expected, out.toString());
                } catch (Exception ex) {
                    fail(ex.getMessage());
                } // try-catch
            } catch (Exception e) {
                fail(e.getMessage());
            } // try-catch
        } // testBulkInsert_woData
    } // HttpFailureTest

    @RunWith(Parameterized.class)
    public static class InvalidIndexAndOrTypeTest {
        /**
         * setup test class
         */
        @BeforeClass
        public static void setUpClass() {
            LogManager.getRootLogger().setLevel(Level.FATAL);
        } // setUpClass

        @Mock
        private HttpClient mockHttpClient;

        @Parameters
        public static Collection<Object[]> getParameters() {
            List<Object[]> params = new ArrayList<>();
            for (String i : new String[]{"idx", "", "\t", null}) {
                for (String t: new String[]{"type", "", "\t", null}) {
                    if (i == "idx" && t == "type") continue;
                    Object[] ary = {i, t};
                    params.add(ary);
                }
            }
            return params;
        } // getParameters

        private String invalidIndex;
        private String invalidType;

        public InvalidIndexAndOrTypeTest(String i, String t) {
            this.invalidIndex = i;
            this.invalidType = t;
        } // constructor

        /**
         * setup test case
         *
         * @throws Exception
         */
        @Before
        public void setUp() throws Exception {
            MockitoAnnotations.initMocks(this);
        } // setUp

        /**
         * [ElasticsearchBackendImplTest$SuccessTest.testBulkInsert_invalidIndexAndOrType]
         * Test to insert invalid index and/or type.
         */
        @Test
        public void testBulkInsert_invlalidIndexAndOrType() {
            System.out.println(getTestTraceHead(String.format("[ElasticsearchBackendImplTest.bulkInsert Failure, index=%s, type=%s]", invalidIndex, invalidType)));

            List<Map<String, String>> data = new ArrayList<Map<String, String>>();

            try {
                ElasticsearchBackendImpl backend = new ElasticsearchBackendImpl(host, port, false, maxConns, maxConnsPerRoute);
                backend.setHttpClient(mockHttpClient);
                backend.bulkInsert(invalidIndex, invalidType, data);
            } catch (CygnusPersistenceError e) {
                assertEquals("CygnusPersistenceError. invalid arguments (index=" + invalidIndex + ", type=" + invalidType + ", data=" + data + "). ", e.getMessage());
                try {
                    verify(mockHttpClient, times(0)).execute(Mockito.any(HttpPost.class));
                } catch (Exception ex) {
                    fail(ex.getMessage());
                } // try-catch
            } catch (Exception e) {
                fail(e.getMessage());
            } // try-catch
        } // testBulkInsert_invlalidIndexAndOrType
    } // InvalidIndexAndOrTypeTest

    @RunWith(Parameterized.class)
    public static class InvalidDataTest {
        /**
         * setup test class
         */
        @BeforeClass
        public static void setUpClass() {
            LogManager.getRootLogger().setLevel(Level.FATAL);
        } // setUpClass

        @Mock
        private HttpClient mockHttpClient;

        @Parameters
        public static Collection<Object[]> getParameters() {
            return Arrays.asList(new Object[][] {
                {null},
                {new HashMap<String, String>()},
                {new HashMap<String, String>(){
                    {
                        put("dummy", "dummy");
                    }
                }},
                {new HashMap<String, String>(){
                    {
                        put("recvTimeTs", "test_recvTimeTs_1");
                    }
                }},
                {new HashMap<String, String>(){
                    {
                        put("data", "{\"message\": \"test data 1\"}");
                    }
                }},
                {new HashMap<String, String>(){
                    {
                        put("recvTimeTs", "");
                        put("data", "{\"message\": \"test data 1\"}");
                    }
                }},
                {new HashMap<String, String>(){
                    {
                        put("recvTimeTs", null);
                        put("data", "{\"message\": \"test data 1\"}");
                    }
                }},
                {new HashMap<String, String>(){
                    {
                        put("recvTimeTs", "test_recvTimeTs_1");
                        put("data", " ");
                    }
                }},
                {new HashMap<String, String>(){
                    {
                        put("recvTimeTs", "test_recvTimeTs_1");
                        put("data", "\t");
                    }
                }}
            });
        } // getParameters

        private Map<String, String> elem;

        public InvalidDataTest(Map<String, String> elem) {
            this.elem = elem;
        } // constructor

        /**
         * setup test case
         *
         * @throws Exception
         */
        @Before
        public void setUp() throws Exception {
            MockitoAnnotations.initMocks(this);
        } // setUp

        /**
         * [ElasticsearchBackendImplTest$SuccessTest.testBulkInsert_invalidData]
         * Test to insert invalid data.
         */
        @Test
        public void testBulkInsert_invlalidData() {
            System.out.println(getTestTraceHead(String.format("[ElasticsearchBackendImplTest.bulkInsert Failure, invalidData=%s]", elem)));
            List <Map<String, String>> data = null;
            if (elem != null) {
                data = new ArrayList<Map<String, String>>();
                data.add(elem);
            }

            try {
                ElasticsearchBackendImpl backend = new ElasticsearchBackendImpl(host, port, false, maxConns, maxConnsPerRoute);
                backend.setHttpClient(mockHttpClient);
                backend.bulkInsert(idx, type, data);
            } catch (CygnusPersistenceError e) {
                if (elem == null) {
                    assertEquals("CygnusPersistenceError. invalid arguments (index=" + idx + ", type=" + type + ", data=" + data + "). ", e.getMessage());
                } else {
                    assertEquals("CygnusPersistenceError. invalid data format (data=" + data + "). ", e.getMessage());
                }
                try {
                    verify(mockHttpClient, times(0)).execute(Mockito.any(HttpPost.class));
                } catch (Exception ex) {
                    fail(ex.getMessage());
                } // try-catch
            } catch (Exception e) {
                fail(e.getMessage());
            } // try-catch
        } // testBulkInsert_invlalidData
    } // InvaidDataTest
} // ElasticsearchBackendImplTest
