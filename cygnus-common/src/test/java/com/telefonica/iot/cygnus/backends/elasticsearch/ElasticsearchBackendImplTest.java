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
// import org.apache.log4j.Level;
// import org.apache.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
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
    private final static String charSet = "UTF-8";

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
            //LogManager.getRootLogger().setLevel(Level.FATAL);
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
            loggerConfig.setLevel(Level.FATAL);
        } // setUpClass

        @Mock
        private HttpClient mockHttpClient;

        @Captor
        private ArgumentCaptor<HttpPost> requestCaptor;

        @Parameters
        public static Collection<Object[]> getParameters() {
            return Arrays.asList(new Object[][] {
                {"http", false, "{\"message\": \"en - test data 1\"}", "CA7219C0B311573D5D84EC08C80DB1E0", "US-ASCII"},
                {"http", false, "{\"message\": \"en - test data 1\"}", "CA7219C0B311573D5D84EC08C80DB1E0", "ISO-8859-1"},
                {"http", false, "{\"message\": \"en - test data 1\"}", "CA7219C0B311573D5D84EC08C80DB1E0", "UTF-8"},
                {"http", false, "{\"message\": \"ja - テストデータ 1\"}", "60DF702634A1341562C192FB0876D04A", "utf-8"},
                {"http", false, "{\"message\": \"de - Ä Ü Ö ä ü ö ß\"}", "6FE8AB2A91C5AC47461EC40FB85C08EE", "uTf-8"},
                {"https", true, "{\"message\": \"en - test data 1\"}", "CA7219C0B311573D5D84EC08C80DB1E0", "us-ascii"},
                {"https", true, "{\"message\": \"en - test data 1\"}", "CA7219C0B311573D5D84EC08C80DB1E0", "iso-8859-1"},
                {"https", true, "{\"message\": \"en - test data 1\"}", "CA7219C0B311573D5D84EC08C80DB1E0", "uTF-8"},
                {"https", true, "{\"message\": \"ja - テストデータ 1\"}", "60DF702634A1341562C192FB0876D04A", "Utf-8"},
                {"https", true, "{\"message\": \"de - Ä Ü Ö ä ü ö ß\"}", "6FE8AB2A91C5AC47461EC40FB85C08EE", "UTf-8"},
            });
        } // getParameters

        private String schema;
        private boolean flag;
        private String msg;
        private String tsHash;
        private String charSet;

        public HttpSuccessTest(String schema, boolean flag, String msg, String tsHash, String charSet) {
            this.schema = schema;
            this.flag = flag;
            this.msg = msg;
            this.tsHash = tsHash;
            this.charSet = charSet;
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
                    put("data", msg);
                }
            });
            String expected = "";
            expected += String.format("{\"index\":{\"_id\":\"test_recvTimeTs_1-%s\"}}\n", tsHash);
            expected += String.format("%s\n", msg);

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
                    put("data", msg);
                }
            });
            data.add(new HashMap<String, String>(){
                {
                    put("recvTimeTs", "test_recvTimeTs_2");
                    put("data", "{\"message\": \"en - test data 2\"}");
                }
            });
            String recvTimeTs2Hash = "0739692298D884D622D004C2C479F0B3";
            String expected = "";
            expected += String.format("{\"index\":{\"_id\":\"test_recvTimeTs_1-%s\"}}\n", tsHash);
            expected += String.format("%s\n", msg);
            expected += String.format("{\"index\":{\"_id\":\"test_recvTimeTs_2-%s\"}}\n", recvTimeTs2Hash);
            expected += String.format("{\"message\": \"en - test data 2\"}\n");

            assertBulkInsert(data, expected);
        } // testBulkInsert_oneData

        private void assertBulkInsert(List<Map<String, String>> data, String expected) {
            try {
                ElasticsearchBackendImpl backend = new ElasticsearchBackendImpl(host, port, flag, maxConns, maxConnsPerRoute, charSet);
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
            //LogManager.getRootLogger().setLevel(Level.FATAL);
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
            loggerConfig.setLevel(Level.FATAL);
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
                ElasticsearchBackendImpl backend = new ElasticsearchBackendImpl(host, port, flag, maxConns, maxConnsPerRoute, charSet);
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
            //LogManager.getRootLogger().setLevel(Level.FATAL);
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
            loggerConfig.setLevel(Level.FATAL);
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
                ElasticsearchBackendImpl backend = new ElasticsearchBackendImpl(host, port, false, maxConns, maxConnsPerRoute, charSet);
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
            //LogManager.getRootLogger().setLevel(Level.FATAL);
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
            loggerConfig.setLevel(Level.FATAL);
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
                ElasticsearchBackendImpl backend = new ElasticsearchBackendImpl(host, port, false, maxConns, maxConnsPerRoute, charSet);
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

    @RunWith(Parameterized.class)
    public static class InvalidCharsetTest {
        /**
         * setup test class
         */
        @BeforeClass
        public static void setUpClass() {
            //LogManager.getRootLogger().setLevel(Level.FATAL);
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
            loggerConfig.setLevel(Level.FATAL);
        } // setUpClass

        @Mock
        private HttpClient mockHttpClient;

        @Captor
        private ArgumentCaptor<HttpPost> requestCaptor;

        @Parameters
        public static Collection<Object[]> getParameters() {
            return Arrays.asList(new Object[][] {
                {false, "invalid"},
                {false, ""},
                {false, null},
                {true, "invalid"},
                {true, ""},
                {true, null},
            });
        } // getParameters

        private boolean flag;
        private String charSet;
        private final String msg = "{\"message\": \"en - test data 1\"}";

        public InvalidCharsetTest(boolean flag, String charSet) {
            this.flag = flag;
            this.charSet = charSet;
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
         * [ElasticsearchBackendImpl#InvalidCharsetTest.testBulkInsert]
         * Test to insert a data successfully.
         */
        @Test
        public void testBulkInsert() {
            System.out.println(getTestTraceHead(String.format("[ElasticsearchBackendImpl#InvalidCharsetTest.bulkInsert OK, TLS=%b, Data=1]", flag)));

            List<Map<String, String>> data = new ArrayList<Map<String, String>>();
            data.add(new HashMap<String, String>(){
                {
                    put("recvTimeTs", "test_recvTimeTs_1");
                    put("data", msg);
                }
            });

            try {
                ElasticsearchBackendImpl backend = new ElasticsearchBackendImpl(host, port, flag, maxConns, maxConnsPerRoute, charSet);
                backend.setHttpClient(mockHttpClient);
                backend.bulkInsert(idx, type, data);
            } catch (CygnusPersistenceError e) {
                String errorData = String.format("[{data=%s, recvTimeTs=test_recvTimeTs_1}]", msg);
                String rootCause;
                if (charSet == null) {
                    rootCause = "java.lang.IllegalArgumentException: Null charset name";
                } else if (charSet == "") {
                    rootCause = "java.nio.charset.IllegalCharsetNameException: ";
                } else {
                    rootCause = String.format("java.nio.charset.UnsupportedCharsetException: %s", charSet);
                }
                assertEquals(String.format("CygnusPersistenceError. Could not create StringEntity (data=%s, charSet=%s, rootCause=%s). ", errorData, charSet, rootCause), e.getMessage());
                try {
                    verify(mockHttpClient, times(0)).execute(requestCaptor.capture());
                } catch (Exception ex) {
                    fail(ex.getMessage());
                } // try-catch
            } catch (Exception e) {
                fail(e.getMessage());
            } // try-catch
        } // testBulkInsert_oneData
    } // InvalidCharsetTest
} // ElasticsearchBackendImplTest
