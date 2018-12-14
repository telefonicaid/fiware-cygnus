/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.http;

import static org.junit.Assert.assertArrayEquals;
// this is required by "fail" like assertions
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonResponseTest {

    // instance to be tested
    private JsonResponse response;

    /**
     * Sets up tests by creating a unique instance of the tested class, and by
     * defining the behaviour of the mocked classes.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        JSONObject obj = new JSONObject();
        obj.put("test", "test");
        Header[] headers = {new BasicHeader("Content-type", "application/x-www-form-urlencoded"),
            new BasicHeader("Content-type", "application/x-www-form-urlencoded"),
            new BasicHeader("Accep", "text/html,text/xml,application/xml"),
            new BasicHeader("Connection", "keep-alive"), new BasicHeader("keep-alive", "115"),
            new BasicHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.2) Firefox/3.6.2") };
        response = new JsonResponse(obj, 200, "OK", null);
    } // setUp

    /**
     * Test of getJsonObject method, of class JsonResponse.
     */
    @Test
    public void testGetJsonObject() {
        System.out.println("Testing JsonResponseTest.getJsonObject");
        assertTrue(response.getJsonObject().containsKey("test"));
    } // testIsCachedRes

    /**
     * Test of getStatusCode method, of class JsonResponse.
     */
    @Test
    public void testGetStatusCode() {
        System.out.println("Testing JsonResponseTest.getStatusCode");
        assertEquals(200, response.getStatusCode());
    } // testGetStatusCode

    /**
     * Test of getReasonPhrase method, of class JsonResponse.
     */
    @Test
    public void testGetReasonPhrase() {
        System.out.println("Testing JsonResponseTest.getReasonPhrase");
        assertEquals("OK", response.getReasonPhrase());
    } // testGetReasonPhrase

    /**
     * Test of getLocationHeader method, of class CKANCache.
     */
    @Test
    public void testGetLocationHeader() {
        System.out.println("Testing JsonResponseTest.getLocationHeader");
        assertEquals(null, response.getLocationHeader());
    } // testGetLocationHeader

    /**
     * Test of getHeaders method, of class CKANCache.
     */
    @Test
    public void testGetHeaders() {
        System.out.println("Testing JsonResponseTest.getHeaders");
        assertArrayEquals(null, response.getHeaders());
    } // testGetHeaders

} // JsonResponseTest
