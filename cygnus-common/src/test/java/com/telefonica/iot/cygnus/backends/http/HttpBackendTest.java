/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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
package com.telefonica.iot.cygnus.backends.http;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import java.util.ArrayList;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author pcoello25
 */

@RunWith(MockitoJUnitRunner.class)
public class HttpBackendTest {
    
    /**
     * This class is used to test once and only once the common functionality shared by all the real extending sinks.
     */
    private class HttpBackendImpl extends HttpBackend {

        public HttpBackendImpl(String host, String port, boolean ssl, boolean krb5, String krb5User,
                String krb5Password, String krb5LoginConfFile, String krb5ConfFile, int maxConns,
                int maxConnsPerRoute) {
            super(host, port, ssl, krb5, krb5User, krb5Password, krb5LoginConfFile, krb5ConfFile, maxConns,
                    maxConnsPerRoute);
        } // HttpBackendImpl
   
    } // HttpBackendImpl
    
    @Mock
    private HttpClient httpclient;
    
    // instance to be tested
    private HttpBackend httpBackend;
    
    private final HttpResponse mockResponse = mock(HttpResponse.class);
    private final HttpResponse mockArrayResponse = mock(HttpResponse.class);
    private final HttpRequestBase mockRequest = mock(HttpRequestBase.class);
    private final HttpRequestBase mockArrayRequest = mock(HttpRequestBase.class);
    private final ArrayList<Header> headers = new ArrayList<Header>();
    private StringEntity normalEntity;
    private StringEntity arrayEntity;
    private String normalURL;
    private String arrayURL;
    private String host;
    private final int maxConns = 50;
    private final int maxConnsPerRoute = 10;
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        String normalResponse =
                "{\"somefield\":\"somevalue\",\"somefield2\":\"somevalue2\",\"somefield2\":\"somevalue2\"}";
        String arrayResponse =
                "[{\"somefield\":{\"somesubfield1\":\"somevalue1\",\"sumesuffield1\":\"somevalue2\","
                + "\"http\":{\"field1\":\"value1\"},}},{\"somefield\":{\"somesubfield1\":\"somevalue1\","
                + "\"sumesuffield1\":\"somevalue2\",\"http\":{\"field1\":\"value1\"},}}]";
        normalURL = "http://someurl:1234";
        arrayURL = "http://someurl:1234";
        normalEntity = new StringEntity(normalResponse);
        arrayEntity = new StringEntity(arrayResponse);
        host = "someurl.org";
        headers.add(new BasicHeader("Content-type", "application/json"));
        headers.add(new BasicHeader("Accept", "application/json"));
        headers.add(new BasicHeader("X-Auth-token", "12345"));
        when(mockResponse.getEntity()).thenReturn(normalEntity);
        when(mockArrayResponse.getEntity()).thenReturn(arrayEntity);
        when(httpclient.execute(mockRequest)).thenReturn(mockResponse);
        when(httpclient.execute(mockArrayRequest)).thenReturn(mockArrayResponse);
    } // setUp
    
    @Test
    public void testDoRequestWithNormalResponse() throws Exception {
        System.out.println(getTestTraceHead("[HttpBackend.doRequest]")
                + " - Gets a valid Json object based JSONResponse");
        httpBackend = new HttpBackendImpl(host, normalURL, false, false, null, null, null, null, maxConns,
                maxConnsPerRoute);
        httpBackend.setHttpClient(httpclient);
        
        try {
            httpBackend.doRequest("GET", normalURL, headers, normalEntity);
            System.out.println(getTestTraceHead("[HttpBackend.doRequest]") + " -  OK  - Succesfully got");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[HttpBackend.doRequest]")
                    + " - FAIL - There was some problem when handling the request.");
            throw e;
        } // try catch
    } // testDoRequestWithNormalResponse
    
    @Test
    public void testDoRequestWithArrayResponse() throws Exception {
        System.out.println(getTestTraceHead("[HttpBackend.doRequest]")
                + " - Gets a valid Json array based JSONResponse");
        httpBackend = new HttpBackendImpl(host, arrayURL, false, false, null, null, null, null, maxConns,
                maxConnsPerRoute);
        httpBackend.setHttpClient(httpclient);
        
        try {
            httpBackend.doRequest("GET", arrayURL, headers, arrayEntity);
            System.out.println(getTestTraceHead("[HttpBackend.doRequest]") + " -  OK  - Succesfully got");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[HttpBackend.doRequest]")
                    + " - FAIL - There was some problem when handling the request.");
            throw e;
        } // try catch
    } // testDoRequestWithArrayResponse

} // HttpBackendTest
