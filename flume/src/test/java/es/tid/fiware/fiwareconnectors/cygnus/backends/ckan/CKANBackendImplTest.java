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

import org.json.simple.JSONObject;
import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusBadConfiguration;
import java.util.HashMap;
import org.mockito.Mockito;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*; // this is required by "fail" like assertions
import static org.mockito.Mockito.*; // this is required by "when" like functions

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
    private CKANRequester mockRequester;
    
    // constants
    private final String apiKey = "1a2b3c4d5e6f7g8h9i0j";
    private final String host = "localhost";
    private final String port = "80";
    private final String orionURL = "http://orion-vm:1026/";
    private final boolean ssl = false;
    private final String orgName = "rooms";
    private final String pkgName = "numeric-rooms";
    private final String resName = "room1-room";
    private final String orgId = "org_id";
    private final String pkgId = "pkg_id";
    private final String resId = "res_id";
    private final String recvTime = "2014-09-23T11:26:45";
    private final long recvTimeTs = new Long("123456789").longValue();
    private final String attrName = "temperature";
    private final String attrType = "centigrade";
    private final String attrValue = "26.5";
    private final String attrMd = "[]";
    private final HashMap<String, String> attrList = new HashMap<String, String>();
    private final HashMap<String, String> attrMdList = new HashMap<String, String>();
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        backend = new CKANBackendImpl(apiKey, host, port, orionURL, ssl);
        
        // set up other instances
        CKANResponse ckanResp = new CKANResponse(new JSONObject(), 200);

        // set up the behaviour of the mocked classes
        when(mockCache.isCachedOrg(orgName)).thenReturn(true);
        when(mockCache.isCachedPkg(orgName, pkgName)).thenReturn(true);
        when(mockCache.isCachedRes(orgName, pkgName, resName)).thenReturn(true);
        when(mockCache.getOrgId(orgName)).thenReturn("org_id");
        when(mockCache.getPkgId(pkgName)).thenReturn("pkg_id");
        when(mockCache.getResId(resName)).thenReturn("res_id");
        when(mockRequester.doCKANRequest(Mockito.anyString(), Mockito.anyString())).thenReturn(ckanResp);
        when(mockRequester.doCKANRequest(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(ckanResp);
    } // setUp

    /**
     * Test of persist (row) method, of class CKANBackendImpl.
     */
    @Test
    public void testPersistRow() {
        System.out.println("Testing MySQLBackend.persist (row)");
        
        try {
            backend.setCache(mockCache);
            backend.setRequester(mockRequester);
            backend.persist(recvTimeTs, recvTime, orgName, pkgName, resName, attrName, attrType, attrValue, attrMd);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersistRow
    
    /**
     * Test of persist (column) method, of class CKANBackendImpl.
     */
    @Test
    public void testPersistColumn() {
        System.out.println("Testing MySQLBackend.persist (column)");
        
        try {
            backend.setCache(mockCache);
            backend.setRequester(mockRequester);
            backend.persist(recvTime, orgName, pkgName, resName, attrList, attrMdList);
        } catch (Exception e) {
            // Check if the raised exception type is CygnusBadConfiguration. This exception means the resource does not
            // exist in CKAN and, due to we are running in "column" mode, it cannot be created. By checking this
            // exception is the most far we can go with this test.
            if (e instanceof CygnusBadConfiguration) {
                assertTrue(true);
            } else {
                fail(e.getMessage());
            } // if else
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersistColumn
    
} // CKANBackendImplTest
