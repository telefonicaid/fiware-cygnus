/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package es.tid.fiware.fiwareconnectors.cygnus.backends.ckan;

import org.apache.http.entity.StringEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.mockito.Mockito;
import org.apache.http.client.HttpClient;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*; // this is required by "fail" like assertions
import static org.mockito.Mockito.*; // this is required by "when" like functions

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class CKANRequesterTest {
    
    // instance to be tested
    private CKANRequester requester;
    
    // constants
    private final String host = "localhost";
    private final String port = "80";
    private final boolean ssl = false;
    private final String apiKey = "1234567890abcdefgh";
    private final String method = "GET";
    private final String url = "http://any_url";
    private final String payload = "whatever_payload";
    private final String resultData = "{\"whatever\":\"whatever\"}";
    private final int statusCode = 200;
    
    // mocks
    @Mock
    private HttpClient mockHttpClient;
        
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        requester = new CKANRequester(mockHttpClient, host, port, ssl, apiKey);
        
        // set up the behaviour of the mocked classes
        BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), statusCode, "ok");
        response.setEntity(new StringEntity("{\"result\":" + resultData + "}"));
        when(mockHttpClient.execute(Mockito.any(HttpUriRequest.class))).thenReturn(response);
    } // setUp
    
    /**
     * Test of getOrgId method, of class CKANCache.
     */
    @Test
    public void testDoCKANRequestNoPayload() {
        System.out.println("Testing CKANCache.doCKANRequest (no payload)");
        
        try {
            CKANResponse resp = requester.doCKANRequest(method, url);
            assertEquals(resultData, resp.getJsonObject().get("result").toString());
            assertEquals(statusCode, resp.getStatusCode());
        } catch (Exception e) {
            fail(e.getMessage());
        } // try catch
    } // testDoCKANRequestNoPayload
    
    /**
     * Test of getOrgId method, of class CKANCache.
     */
    @Test
    public void testDoCKANRequestWithPayload() {
        System.out.println("Testing CKANCache.doCKANRequest (with payload)");
        
        try {
            CKANResponse resp = requester.doCKANRequest(method, url, payload);
            assertEquals(resultData, resp.getJsonObject().get("result").toString());
            assertEquals(statusCode, resp.getStatusCode());
        } catch (Exception e) {
            fail(e.getMessage());
        } // try catch
    } // testDoCKANRequestWithPayload
    
} // CKANRequesterTest
