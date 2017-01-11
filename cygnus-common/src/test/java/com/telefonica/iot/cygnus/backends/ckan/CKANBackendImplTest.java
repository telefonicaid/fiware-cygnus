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

package com.telefonica.iot.cygnus.backends.ckan;

import java.util.HashMap;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.mockito.Mockito;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class CKANBackendImplTest {
    
    // instance to be tested
    private CKANBackendImpl backend;
    
    // mocks
    // the DefaultHttpClient class cannot be mocked:
    // http://stackoverflow.com/questions/4547852/why-does-my-mockito-mock-object-use-real-the-implementation
    @Mock
    private CKANCache mockCache;
    @Mock
    private HttpClient mockHttpClient;
      
    // constants
    private final String apiKey = "1a2b3c4d5e6f7g8h9i0j";
    private final String host = "localhost";
    private final String port = "80";
    private final String orionURL = "http://orion-vm:1026/";
    private final boolean ssl = false;
    private final String orgName = "rooms";
    private final String pkgName = "numeric-rooms";
    private final String resName = "room1-room";
    private final String recvTime = "2014-09-23T11:26:45";
    private final String data = "this is a lot of data";
    private final HashMap<String, String> attrList = new HashMap<String, String>();
    private final HashMap<String, String> attrMdList = new HashMap<String, String>();
    private final int maxConns = 50;
    private final int maxConnsPerRoute = 10;
    private final String viewer = "recline_grid_view";
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        backend = new CKANBackendImpl(apiKey, host, port, orionURL, ssl, maxConns, maxConnsPerRoute, viewer);

        // set up the behaviour of the mocked classes
        when(mockCache.isCachedOrg(orgName)).thenReturn(true);
        when(mockCache.isCachedPkg(orgName, pkgName)).thenReturn(true);
        when(mockCache.isCachedRes(orgName, pkgName, resName)).thenReturn(true);
        when(mockCache.getOrgId(orgName)).thenReturn("org_id");
        when(mockCache.getPkgId(orgName, pkgName)).thenReturn("pkg_id");
        when(mockCache.getResId(orgName, pkgName, resName)).thenReturn("res_id");
        BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "ok");
        response.setEntity(new StringEntity("{\"result\": {\"whatever\":\"whatever\"}}"));
        when(mockHttpClient.execute(Mockito.any(HttpUriRequest.class))).thenReturn(response);
    } // setUp

    /**
     * Test of persist method, of class CKANBackendImpl.
     */
    @Test
    public void testPersist() {
        System.out.println("Testing CKANBackendImpl.persist");
        
        try {
            backend.setCache(mockCache);
            backend.setHttpClient(mockHttpClient);
            backend.persist(orgName, pkgName, resName, data, true);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersist
    
} // CKANBackendImplTest
