/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.hdfs;

import org.apache.http.message.BasicHeader;
import org.apache.http.client.methods.HttpUriRequest;
import org.mockito.Mockito;
import org.apache.http.client.HttpClient;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
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
public class HDFSBackendImplRESTTest {
    
    // instance to be tested
    private HDFSBackendImplREST backend;
    
    // mocks
    // the DefaultHttpClient class cannot be mocked:
    // http://stackoverflow.com/questions/4547852/why-does-my-mockito-mock-object-use-real-the-implementation
    @Mock
    private HttpClient mockHttpClientCreateFile;
    @Mock
    private HttpClient mockHttpClientAppend;
    @Mock
    private HttpClient mockHttpClientExistsCreateDir;
    
    // constants
    private final String hdfsHost = "1.2.3.4";
    private final String hdfsPort = "50070";
    private final String user = "hdfs-user";
    private final String password = "12345abcde";
    private final String token = "erwfgwegwegewtgewtg";
    private final String hiveServerVersion = "2";
    private final String hiveHost = "1.2.3.4";
    private final String hivePort = "10000";
    private final String dirPath = "path/to/my/data";
    private final String data = "this is a lot of data";
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
        backend = new HDFSBackendImplREST(hdfsHost, hdfsPort, user, password, token, hiveServerVersion, hiveHost,
                hivePort, false, null, null, null, null, false, maxConns, maxConnsPerRoute);
        
        // set up other instances
        BasicHttpResponse resp200 = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
        resp200.addHeader("Content-Type", "application/json");
        BasicHttpResponse resp201 = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 201, "Created");
        resp201.addHeader("Content-Type", "application/json");
        BasicHttpResponse resp307 = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 307, "Temporary Redirect");
        resp307.addHeader("Content-Type", "application/json");
        resp307.addHeader(new BasicHeader("Location", "http://localhost:14000/"));
        
        // set up the behaviour of the mocked classes
        when(mockHttpClientExistsCreateDir.execute(Mockito.any(HttpUriRequest.class))).thenReturn(resp200);
        when(mockHttpClientCreateFile.execute(Mockito.any(HttpUriRequest.class))).thenReturn(resp307, resp201);
        when(mockHttpClientAppend.execute(Mockito.any(HttpUriRequest.class))).thenReturn(resp307, resp200);
    } // setUp
    
    /**
     * Test of createDir method, of class HDFSBackendImplREST.
     */
    @Test
    public void testCreateDir() {
        System.out.println("Testing HDFSBackendImplREST.createDir");
        
        try {
            backend.setHttpClient(mockHttpClientExistsCreateDir);
            backend.createDir(dirPath);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testCreateDir
    
    /**
     * Test of createFile method, of class HDFSBackendImplREST.
     */
    @Test
    public void testCreateFile() {
        System.out.println("Testing HDFSBackendImplREST.createFile");
        
        try {
            backend.setHttpClient(mockHttpClientCreateFile);
            backend.createFile(dirPath, data);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testCreateFile
    
    /**
     * Test of append method, of class HDFSBackendImplREST.
     */
    @Test
    public void testAppend() {
        System.out.println("Testing HDFSBackendImplREST.append");
        
        try {
            backend.setHttpClient(mockHttpClientAppend);
            backend.append(dirPath, data);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testAppend
    
    /**
     * Test of exists method, of class HDFSBackendImplREST.
     */
    @Test
    public void testExists() {
        System.out.println("Testing HDFSBackendImplREST.exists");
        
        try {
            backend.setHttpClient(mockHttpClientExistsCreateDir);
            backend.exists(dirPath);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testExists
    
} // HDFSBackendImplRESTTest
