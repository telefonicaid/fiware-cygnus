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

import java.util.ArrayList;
import java.util.HashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
public class CKANCacheTest {
    
    // instance to be tested
    private CKANCache cache;
    
    // mocks
    @Mock
    private HashMap<String, HashMap<String, ArrayList<String>>> tree;
    @Mock
    private HashMap<String, String> orgMap;
    @Mock
    private HashMap<String, String> pkgMap;
    @Mock
    private HashMap<String, String> resMap;
    
    // constants
    private final String host = "localhost";
    private final String port = "80";
    private final boolean ssl = false;
    private final String apiKey = "xxxxxxxxxxxxx";
    private final String orgName = "rooms";
    private final String pkgName = "numeric-rooms";
    private final String resName = "room1-room";
    private final String orgId = "org_id";
    private final String pkgId = "pkg_id";
    private final String resId = "res_id";
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
        // set up the instance of the tested class
        cache = new CKANCache(host, port, ssl, apiKey, maxConns, maxConnsPerRoute);

        // set up the behaviour of the mocked classes
        when(orgMap.get(orgName)).thenReturn(orgId);
        when(pkgMap.get(orgName + "_" + pkgName)).thenReturn(pkgId);
        when(resMap.get(orgName + "_" + pkgName + "_" + resName)).thenReturn(resId);
        
        ArrayList<String> ress = new ArrayList<String>();
        ress.add(resName);
        HashMap<String, ArrayList<String>> pkgs = new HashMap<String, ArrayList<String>>();
        pkgs.put(pkgName, ress);
        tree = new HashMap<String, HashMap<String, ArrayList<String>>>();
        tree.put(orgName, pkgs);
    } // setUp
    
    /**
     * Test of getOrgId method, of class CKANCache.
     */
    @Test
    public void testGetOrgId() {
        System.out.println("Testing CKANCache.getOrgId");
        cache.setOrgMap(orgMap);
        assertEquals(orgId, cache.getOrgId(orgName));
    } // testGetOrgId
    
    /**
     * Test of getPkgId method, of class CKANCache.
     */
    @Test
    public void testGetPkgId() {
        System.out.println("Testing CKANCache.getPkgId");
        cache.setPkgMap(pkgMap);
        assertEquals(pkgId, cache.getPkgId(orgName, pkgName));
    } // testGetPkgId
    
    /**
     * Test of getResId method, of class CKANCache.
     */
    @Test
    public void testGetResId() {
        System.out.println("Testing CKANCache.getResId");
        cache.setResMap(resMap);
        assertEquals(resId, cache.getResId(orgName, pkgName, resName));
    } // testGetResId
    
    /**
     * Test of setOrgId method, of class CKANCache.
     */
    @Test
    public void testSetOrgId() {
        System.out.println("Testing CKANCache.setOrgId");
        cache.setOrgId(orgName, orgId);
        assertEquals(orgId, cache.getOrgId(orgName));
    } // testSetOrgId
    
    /**
     * Test of setPkgId method, of class CKANCache.
     */
    @Test
    public void testSetPkgId() {
        System.out.println("Testing CKANCache.setPkgId");
        cache.setPkgId(orgName, pkgName, pkgId);
        assertEquals(pkgId, cache.getPkgId(orgName, pkgName));
    } // testSetPkgId
    
    /**
     * Test of setResId method, of class CKANCache.
     */
    @Test
    public void testSetResId() {
        System.out.println("Testing CKANCache.setResId");
        cache.setResId(orgName, pkgName, resName, resId);
        assertEquals(resId, cache.getResId(orgName, pkgName, resName));
    } // testSetResId
    
    /**
     * Test of isCachedOrg method, of class CKANCache.
     */
    @Test
    public void testIsCachedOrg() {
        System.out.println("Testing CKANCache.isCachedOrg");
        cache.setTree(tree);
        
        try {
            assertTrue(cache.isCachedOrg(orgName));
        } catch (Exception e) {
            fail(e.getMessage());
        } // try catch
    } // testIsCachedOrg
    
    /**
     * Test of isCachedPkg method, of class CKANCache.
     */
    @Test
    public void testIsCachedPkg() {
        System.out.println("Testing CKANCache.isCachedPkg");
        cache.setTree(tree);
        
        try {
            assertTrue(cache.isCachedPkg(orgName, pkgName));
        } catch (Exception e) {
            fail(e.getMessage());
        } // try catch
    } // testIsCachedPkg

    /**
     * Test of isCachedRes method, of class CKANCache.
     */
    @Test
    public void testIsCachedRes() {
        System.out.println("Testing CKANCache.isCachedRes");
        cache.setTree(tree);
        
        try {
            assertTrue(cache.isCachedRes(orgName, pkgName, resName));
        } catch (Exception e) {
            fail(e.getMessage());
        } // try catch
    } // testIsCachedRes
    
} // CKANCacheTest
