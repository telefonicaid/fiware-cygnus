package com.telefonica.iot.cygnus.backends.slicingdice;

import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 *
 * @author joaosimbiose
 */
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
