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

package com.telefonica.iot.cygnus.backends.arcgis;

import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.util.HashMap;

import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.http.HttpResponse;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.RestApi;

/**
 * @author dmartinez
 *
 */
public class RestApiTest extends RestApi{

    /**
     * 
     */
    @Before
    public void setUp() {
        Configurator.initialize(new DefaultConfiguration());
    }

    /**
     * 
     */
    @Test
    public void simpleGetTest() {
        String urlRequest = "http://www.google.es";

        HashMap<String, String> params = new HashMap<String, String>();

        try {
            HttpResponse response = RestApi.httpGet(urlRequest, params, 0, 0);
            System.out.println(response.toString());
            Assert.assertTrue(response.getResponseCode() == 200);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    /**
     * 
     */
    @Test
    public void parameterTest() {

        String urlRequest = "http://www.google.es";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("f", "pjson");

        try {
            HttpResponse response = RestApi.httpGet(urlRequest, params, 0, 0);
            System.out.println(response.toString());
            Assert.assertTrue(response.getResponseCode() == 200);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    /**
     * 
     */
    @Test
    public void checkResponseTest() {
        HttpResponse response = new HttpResponse(200, "{OK}");
        RestApi.checkHttpResponse(response);

        Assert.assertTrue("Response should be ok.", response.isSuccessful());
    }

    /**
     * 
     */
    @Test
    public void checkMultiResponseTest() {
        HttpResponse response = new HttpResponse(200,
                "{\"addResults\":[{\"objectId\":null,\"uniqueId\":null,\"globalId\":\"B7131F8B-CCE4-DFAA-BA75-CD474D8F05EF\",\"success\":false,\"error\":{\"code\":1000,\"description\":\"Cannot insert duplicate key row in object 'user_19420.FeatureTable_POINT_LAYER' with unique index 'Id_Index'. The duplicate key value is (8).\\r\\nThe statement has been terminated.\"}}]}");
        RestApi.checkHttpResponse(response);

        Assert.assertTrue("Response should be error.", response.hasError());
    }
    
    /**
     * 
     * @throws MalformedURLException
     */
    @Test
    public void checkResponse() throws MalformedURLException {
        String responseError = "{\"error\":{\"code\":500,\"message\":\"Unable to complete operation.\",\"details\":[\"Parser error: Some parameters could not be recognized.\"]}}";
        String responseInvalid = "<html></html>";
        String responseOk = "{}";

        try {
            System.out.println("----------------  checkResponse");

            try {
                RestApi.checkHttpResponse(responseError);
                fail("FAILED - Failed detecting error response");
            } catch (ArcgisException e) {
                System.out.println(
                        "SUCCESS - Error response successfully detected: " + e.getMessage());
            }

            try {
                RestApi.checkHttpResponse(responseInvalid);
                fail("FAILED - Failed detecting error response");
            } catch (ArcgisException e) {
                System.out.println(
                        "SUCCESS - Invalid response successfully detected: " + e.getMessage());
            }

            try {
                RestApi.checkHttpResponse(responseOk);
                System.out.println("SUCCESS - Correct response successfully detected.");
            } catch (ArcgisException e) {
                fail("FAILED - Failed detecting correct response: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.print("FAILED: " + e.getMessage());
            fail(e.getMessage());
        }
    }


}
