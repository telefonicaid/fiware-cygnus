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

package com.telefonica.iot.cygnus.backends.slicingdice;

import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SlicingDiceBackendImplTest {

    // constants
    private static final String DATABASE_KEY = "oiasdiondasidasndasomn";
    private static final String COLUMNS_TO_CREATE = "[" +
            "    {" +
            "        \"name\": \"temperature\"," +
            "        \"api-name\": \"temperature\"," +
            "        \"type\": \"decimal-event\"," +
            "        \"decimal-places\": 5" +
            "    }" +
            "]";

    private static final String VALUES_TO_INSERT = "{" +
            "  \"ROOM1\": {" +
            "    \"temperature\": {" +
            "      \"value\": 15.5, " +
            "      \"date\": \"2018-10-21T00:00:00,000Z\"" +
            "    }" +
            "  }," +
            "  \"auto-create\": [\"dimension\", \"column\"]" +
            "}";

    // instance to be tested
    private SlicingDiceBackendImpl backend;

    // mocks
    @Mock
    private HttpClient mockHttpClient;

    @Before
    public void setUp() throws Exception {
        backend = new SlicingDiceBackendImpl(DATABASE_KEY);
        final BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "ok");
        response.setEntity(new StringEntity("{\"result\": {\"whatever\":\"whatever\"}}"));
        when(mockHttpClient.execute(Mockito.any(HttpUriRequest.class))).thenReturn(response);
    }

    @Test
    public void testCreateColumns() {
        System.out.println("Testing SlicingDiceBackendImpl.createColumns");

        try {
            backend.setHttpClient(mockHttpClient);
            backend.createColumns(COLUMNS_TO_CREATE);
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testColumnAlreadyExist() throws IOException, CygnusRuntimeError, CygnusPersistenceError {
        System.out.println("Testing SlicingDiceBackendImpl.createColumns");

        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = factory
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 400, null), null);
        response.setHeader("Content-Type", "application/json");
        String responseStr = "{\"errors\": [{\"code\": 3003, \"message\": \"Column: Column already exists.\"}]}";
        response.setEntity(new StringEntity(responseStr));
        final JsonResponse jsonRes = backend.createJsonResponse(response);

        final SlicingDiceBackendImpl mockedBackend = mock(SlicingDiceBackendImpl.class);
        when(mockedBackend.doSlicingDiceRequest(anyString(), anyString(), anyString())).thenReturn(
                jsonRes);
        doCallRealMethod().when(mockedBackend).createColumns(anyString());

        try {
            mockedBackend.setHttpClient(mockHttpClient);
            mockedBackend.createColumns(COLUMNS_TO_CREATE);
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testColumnError400() throws IOException, CygnusRuntimeError, CygnusPersistenceError {
        System.out.println("Testing SlicingDiceBackendImpl.createColumns");

        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = factory
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 400, null), null);
        response.setHeader("Content-Type", "application/json");
        String responseStr = "{\"errors\": [{\"code\": 4026, \"message\": \"Query: Invalid query format. Must be a list.\"}]}";
        response.setEntity(new StringEntity(responseStr));
        final JsonResponse jsonRes = backend.createJsonResponse(response);

        final SlicingDiceBackendImpl mockedBackend = mock(SlicingDiceBackendImpl.class);
        when(mockedBackend.doSlicingDiceRequest(anyString(), anyString(), anyString())).thenReturn(
                jsonRes);
        doCallRealMethod().when(mockedBackend).createColumns(anyString());

        try {
            mockedBackend.setHttpClient(mockHttpClient);
            mockedBackend.createColumns(COLUMNS_TO_CREATE);
            Assert.fail();
        } catch (final Exception e) {
            // this response should call an exception because isn't a normal behavior
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testInsertContextData() {
        System.out.println("Testing SlicingDiceBackendImpl.insertContextData");

        try {
            backend.setHttpClient(mockHttpClient);
            backend.insertContextData(VALUES_TO_INSERT);
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            Assert.assertTrue(true);
        }
    }
}
