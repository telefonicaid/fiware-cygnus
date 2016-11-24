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
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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
    
    private final String host = "somehost";
    private final String port = "12345";
    private final int maxConns = 50;
    private final int maxConnsPerRoute = 10;
    
    /**
     * [HttpBackend.createJsonResponse] -------- A JsonResponse object is created if the response content-type header
     * is 'application/json' and the response contains a location header.
     */
    @Test
    public void testCreateJsonResponseEverythingOK() {
        System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                + "-------- A JsonResponse object is created if the response content-type header is "
                + "'application/json' and the response contains a location header");
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = factory.newHttpResponse(
                new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
        String responseStr =
                "{\"somefield1\":\"somevalue1\",\"somefield2\":\"somevalue2\",\"somefield3\":\"somevalue3\"}";
        
        try {
            response.setEntity(new StringEntity(responseStr));
        } catch (UnsupportedEncodingException e) {
            System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                    + "- FAIL - There was some problem when creating the HttpResponse object");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        response.addHeader("Content-Type", "application/json");
        response.addHeader("Location", "http://someurl.org");
        HttpBackend httpBackend = new HttpBackendImpl(host, port, false, false, null, null, null, null, maxConns,
                maxConnsPerRoute);

        try {
            JsonResponse jsonRes = httpBackend.createJsonResponse(response);
            
            try {
                assertTrue(jsonRes.getJsonObject() != null);
                System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                        + "-  OK  - The JsonResponse object has a Json apyload");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                        + "- FAIL - The JsonResponse object has not a Json payload");
                throw e;
            } // try catch
            
            try {
                assertTrue(jsonRes.getLocationHeader() != null);
                System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                        + "-  OK  - The JsonResponse object has a Location header");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                        + "- FAIL - The JsonResponse object has not a Location header");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                    + "- FAIL - There was some problem when creating the JsonResponse object");
            throw new AssertionError(e.getMessage());
        } // try catch
    } // testCreateJsonResponseEverythingOK
    
    /**
     * [HttpBackend.createJsonResponse] -------- A JsonResponse object is not created if the content-type header does
     * not contains 'application/json'.
     */
    @Test
    public void testCreateJsonResponseNoJsonPayload() {
        System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                + "-------- A JsonResponse object is not created if the content-type header does not contains "
                + "'application/json'");
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = factory.newHttpResponse(
                new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
        String responseStr =
                "{\"somefield1\":\"somevalue1\",\"somefield2\":\"somevalue2\",\"somefield3\":\"somevalue3\"}";
        
        try {
            response.setEntity(new StringEntity(responseStr));
        } catch (UnsupportedEncodingException e) {
            System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                    + "- FAIL - There was some problem when creating the HttpResponse object");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        response.addHeader("Content-Type", "text/html");
        response.addHeader("Location", "http://someurl.org");
        HttpBackend httpBackend = new HttpBackendImpl(host, port, false, false, null, null, null, null, maxConns,
                maxConnsPerRoute);

        try {
            JsonResponse jsonRes = httpBackend.createJsonResponse(response);
            
            try {
                assertEquals(null, jsonRes.getJsonObject());
                System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                        + "-  OK  - The JsonResponse object could not be created with a 'text/html' content type "
                        + "header");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                        + "- FAIL - The JsonResponse object was created with a 'text/html' content type header");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                    + "- FAIL - There was some problem when creating the JsonResponse object");
            throw new AssertionError(e.getMessage());
        } // try catch
    } // testCreateJsonResponseNoJsonPayload
    
    /**
     * [HttpBackend.createJsonResponse] -------- A JsonResponse object is created if the content-type header contains
     * 'application/json' but no location header.
     */
    @Test
    public void testCreateJsonResponseNoLocationHeader() {
        System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                + "-------- A JsonResponse object is created if the content-type header contains 'application/json' "
                + "but no location header");
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = factory.newHttpResponse(
                new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
        String responseStr =
                "{\"somefield1\":\"somevalue1\",\"somefield2\":\"somevalue2\",\"somefield3\":\"somevalue3\"}";
        
        try {
            response.setEntity(new StringEntity(responseStr));
        } catch (UnsupportedEncodingException e) {
            System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                    + "- FAIL - There was some problem when creating the HttpResponse object");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        response.addHeader("Content-Type", "text/html");
        HttpBackend httpBackend = new HttpBackendImpl(host, port, false, false, null, null, null, null, maxConns,
                maxConnsPerRoute);

        try {
            JsonResponse jsonRes = httpBackend.createJsonResponse(response);
            
            try {
                assertEquals(null, jsonRes.getLocationHeader());
                System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                        + "-  OK  - The JsonResponse object was created with null location header");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                        + "- FAIL - The JsonResponse object was not created with null location header");
                throw e;
            } // try catch
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[HttpBackend.createJsonResponse]")
                    + "- FAIL - There was some problem when creating the JsonResponse object");
            throw new AssertionError(e.getMessage());
        } // try catch
    } // testCreateJsonResponseNotJsonPayload

} // HttpBackendTest