/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.ckan;

import com.telefonica.iot.cygnus.backends.ckan.CKANResponse;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Test;
import static org.junit.Assert.*; // this is required by "fail" like assertions

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class CKANResponseTest {
    
    // instance to be tested
    private CKANResponse response;
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        JSONObject obj = new JSONObject();
        obj.put("test", "test");
        response = new CKANResponse(obj, 200);
    } // setUp
    
    /**
     * Test of isCachedRes method, of class CKANCache.
     */
    @Test
    public void testGetJsonObject() {
        System.out.println("Testing CKANResponseTest.getJsonObject");
        assertTrue(response.getJsonObject().containsKey("test"));
    } // testIsCachedRes

    /**
     * Test of isCachedRes method, of class CKANCache.
     */
    @Test
    public void testGetStatusCode() {
        System.out.println("Testing CKANResponseTest.getStatusCode");
        assertTrue(response.getJsonObject().containsKey("test"));
    } // testGetStatusCode
    
} // CKANResponseTest
