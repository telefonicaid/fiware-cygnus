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
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * francisco.romerobueno@telefonica.com
 */

package es.tid.fiware.fiwareconnectors.cygnus.backends.ckan;

import es.tid.fiware.fiwareconnectors.cygnus.errors.CygnusBadConfiguration;
import java.util.HashMap;
import org.apache.http.entity.StringEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHttpResponse;
import org.mockito.Mockito;
import org.apache.http.client.HttpClient;
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
    private HttpClient mockHttpClientInitOrg;
    @Mock
    private HttpClient mockHttpClientPersistRow;
    @Mock
    private HttpClient mockHttpClientPersistColumn;
    
    // constants
    private final String apiKey = "1a2b3c4d5e6f7g8h9i0j";
    private final String host = "localhost";
    private final String port = "80";
    private final String defaultPackage = "defaultPackage";
    private final String orionURL = "http://orion-vm:1026/";
    private final String orgName = "defaultOrg";
    private final String recvTime = "2014-09-23T11:26:45";
    private final long recvTimeTs = new Long("123456789").longValue();
    private final String resourceName = "Room1-Room";
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
        backend = new CKANBackendImpl(apiKey, host, port, defaultPackage, orionURL);
        
        // set up other instances
        BasicHttpResponse respOrganizationShow = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
        respOrganizationShow.setEntity(
                new StringEntity("{\"result\":{\"state\":\"active\",\"id\":\"12345\",\"packages\":[]}}"));
        BasicHttpResponse respResourceCreate = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
        respResourceCreate.setEntity(new StringEntity("{\"result\":{\"id\":\"12345\"}}"));
        BasicHttpResponse respDatastoreCreate = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
        respDatastoreCreate.setEntity(new StringEntity("{\"result\":{\"whatever\":\"whatever\"}}"));
        BasicHttpResponse respDatastoreUpsert = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
        respDatastoreUpsert.setEntity(new StringEntity("{\"result\":{\"whatever\":\"whatever\"}}"));
        
        // set up the behaviour of the mocked classes
        when(mockHttpClientInitOrg.execute(Mockito.any(HttpUriRequest.class))).thenReturn(respOrganizationShow);
        when(mockHttpClientPersistRow.execute(Mockito.any(HttpUriRequest.class))).thenReturn(respResourceCreate,
                respDatastoreCreate, respDatastoreUpsert);
        when(mockHttpClientPersistColumn.execute(Mockito.any(HttpUriRequest.class))).thenReturn(respOrganizationShow,
                respResourceCreate, respDatastoreCreate, respDatastoreUpsert);
    } // setUp
    
    /**
     * Test of initOrg method, of class CKANBackendImpl.
     */
    @Test
    public void testInitOrg() {
        System.out.println("Testing MySQLBackend.createDatabase");
        
        try {
            backend.initOrg(mockHttpClientInitOrg, orgName);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testInitOrg
    
    /**
     * Test of persist (row) method, of class CKANBackendImpl.
     */
    @Test
    public void testPersistRow() {
        System.out.println("Testing MySQLBackend.persist (row)");
        
        try {
            backend.persist(mockHttpClientPersistRow, recvTimeTs, recvTime, orgName, resourceName, attrName, attrType,
                    attrValue, attrMd);
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
            backend.persist(mockHttpClientPersistColumn, recvTime, orgName, resourceName, attrList, attrMdList);
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
