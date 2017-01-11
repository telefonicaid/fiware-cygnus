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

import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImplBinary.FSGetter;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*; // this is required by "fail" like assertions
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class HDFSBackendImplBinaryTest {
    
    // instance to be tested
    private HDFSBackendImplBinary backend;
    
    // mocks
    @Mock
    private FSGetter mockFSGetter;
    @Mock
    private FileSystem mockFileSystem;
    @Mock
    private FSDataOutputStream fsDataOutputStreamMock;
    
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
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        backend = new HDFSBackendImplBinary(hdfsHost, hdfsPort, user, password, token, hiveServerVersion, hiveHost,
                hivePort, false, null, null, null, null, false);
        
        // set up the behaviour of the mocked classes
        when(mockFSGetter.get()).thenReturn(mockFileSystem);
        when(mockFileSystem.mkdirs(Mockito.any(Path.class))).thenReturn(true);
        when(mockFileSystem.create(Mockito.any(Path.class))).thenReturn(fsDataOutputStreamMock);
        when(mockFileSystem.append(Mockito.any(Path.class))).thenReturn(fsDataOutputStreamMock);
        when(mockFileSystem.exists(Mockito.any(Path.class))).thenReturn(true);
    } // setUp
    
    /**
     * Test of createDir method, of class HDFSBackendImplBinary.
     */
    @Test
    public void testCreateDir() {
        System.out.println("Testing HDFSBackendImplBinary.createDir");
        
        try {
            backend.setFSGetter(mockFSGetter);
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
        System.out.println("Testing HDFSBackendImpl.createFile");
        
        try {
            backend.setFSGetter(mockFSGetter);
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
        System.out.println("Testing HDFSBackendImpl.append");
        
        try {
            backend.setFSGetter(mockFSGetter);
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
        System.out.println("Testing HDFSBackendImpl.exists");
        
        try {
            backend.setFSGetter(mockFSGetter);
            backend.exists(dirPath);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testExists
    
} // HDFSBackendImplRESTTest
