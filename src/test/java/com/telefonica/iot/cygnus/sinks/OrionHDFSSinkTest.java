/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImpl;
import static org.junit.Assert.*; // this is required by "fail" like assertions
import static org.mockito.Mockito.*; // this is required by "when" like functions
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.backends.http.HttpClientFactory;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.TestUtils;
import java.util.HashMap;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.lifecycle.LifecycleState;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class OrionHDFSSinkTest {
    
    // mocks
    @Mock
    private HttpClientFactory mockHttpClientFactory;
    @Mock
    private HDFSBackendImpl mockWebHDFSBackend;
    
    // instance to be tested
    private OrionHDFSSink sink;
    
    // other instances
    private Context context;
    private NotifyContextRequest singleNotifyContextRequest;
    private NotifyContextRequest multipleNotifyContextRequest;
    
    // context constants
    private final String[] cosmosHost = {"localhost"};
    private final String cosmosPort = "14000";
    private final String hdfsUsername = "user1";
    private final String oauth2Token = "tokenabcdefghijk";
    private final String serviceAsNamespace = "false";
    private final String fileFormat = "json-row";
    private final String hiveHost = "localhost";
    private final String hivePort = "10000";
    private final String krb5Auth = "false";
    private final String enableGrouping = "true";
    
    // header contants
    private final String timestamp = "123456789";
    private final String normalServiceName = "vehicles";
    private final String abnormalServiceName =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongservname";
    private final String singleServicePathName = "4wheels";
    private final String multipleServicePathName = "4wheelsSport,4wheelsUrban";
    private final String abnormalServicePathName =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongservpathname";
    private final String rootServicePathName = "";
    private final String singleDestinationName = "car1_car";
    private final String multipleDestinationName = "sport1,urban1";
    private final String abnormalDestinationName =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongdestname";
    
    // notification constants
    private final String singleContextElementNotification = ""
            + "{\n"
            + "    \"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\",\n"
            + "    \"originator\" : \"localhost\",\n"
            + "    \"contextResponses\" : [\n"
            + "        {\n"
            + "            \"contextElement\" : {\n"
            + "                \"attributes\" : [\n"
            + "                    {\n"
            + "                        \"name\" : \"speed\",\n"
            + "                        \"type\" : \"float\",\n"
            + "                        \"value\" : \"112.9\"\n"
            + "                    }\n"
            + "                ],\n"
            + "                \"type\" : \"car\",\n"
            + "                \"isPattern\" : \"false\",\n"
            + "                \"id\" : \"car1\"\n"
            + "            },\n"
            + "            \"statusCode\" : {\n"
            + "                \"code\" : \"200\",\n"
            + "                \"reasonPhrase\" : \"OK\"\n"
            + "            }\n"
            + "        }\n"
            + "    ]\n"
            + "}";
    private final String multipleContextElementNotification = ""
            + "{\n"
            + "    \"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\",\n"
            + "    \"originator\" : \"localhost\",\n"
            + "    \"contextResponses\" : [\n"
            + "        {\n"
            + "            \"contextElement\" : {\n"
            + "                \"attributes\" : [\n"
            + "                    {\n"
            + "                        \"name\" : \"speed\",\n"
            + "                        \"type\" : \"float\",\n"
            + "                        \"value\" : \"112.9\"\n"
            + "                    }\n"
            + "                ],\n"
            + "                \"type\" : \"car\",\n"
            + "                \"isPattern\" : \"false\",\n"
            + "                \"id\" : \"car1\"\n"
            + "            },\n"
            + "            \"statusCode\" : {\n"
            + "                \"code\" : \"200\",\n"
            + "                \"reasonPhrase\" : \"OK\"\n"
            + "            }\n"
            + "        },\n"
            + "        {\n"
            + "            \"contextElement\" : {\n"
            + "                \"attributes\" : [\n"
            + "                    {\n"
            + "                        \"name\" : \"speed\",\n"
            + "                        \"type\" : \"float\",\n"
            + "                        \"value\" : \"115.8\"\n"
            + "                    }\n"
            + "                ],\n"
            + "                \"type\" : \"car\",\n"
            + "                \"isPattern\" : \"false\",\n"
            + "                \"id\" : \"car2\"\n"
            + "            },\n"
            + "            \"statusCode\" : {\n"
            + "                \"code\" : \"200\",\n"
            + "                \"reasonPhrase\" : \"OK\"\n"
            + "            }\n"
            + "        }\n"
            + "    ]\n"
            + "}";

    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        sink = new OrionHDFSSink();
        sink.setPersistenceBackend(mockWebHDFSBackend);
        
        // set up other instances
        context = new Context();
        context.put("hdfs_host", cosmosHost[0]);
        context.put("hdfs_port", cosmosPort);
        context.put("hdfs_username", hdfsUsername);
        context.put("oauth2_token", oauth2Token);
        context.put("service_as_namespace", serviceAsNamespace);
        context.put("file_format", fileFormat);
        context.put("hive_host", hiveHost);
        context.put("hive_port", hivePort);
        context.put("krb5_auth", krb5Auth);
        context.put("enable_grouping", enableGrouping);
        singleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(singleContextElementNotification);
        multipleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(multipleContextElementNotification);
        
        // set up the behaviour of the mocked classes
        when(mockHttpClientFactory.getHttpClient(true, false)).thenReturn(null);
        when(mockHttpClientFactory.getHttpClient(false, false)).thenReturn(null);
        when(mockWebHDFSBackend.exists(null)).thenReturn(true);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).createDir(null);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).createFile(null, null);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).append(null, null);
    } // setUp

    /**
     * Test of configure method, of class OrionHDFSSink.
     */
    @Test
    public void testConfigure() {
        System.out.println("configure");
        sink.configure(context);
        assertEquals(cosmosHost[0], sink.getHDFSHosts()[0]);
        assertEquals(cosmosPort, sink.getHDFSPort());
        assertEquals(hdfsUsername, sink.getHDFSUsername());
        assertEquals(oauth2Token, sink.getOAuth2Token());
        assertEquals(serviceAsNamespace, sink.getServiceAsNamespace());
        assertEquals(fileFormat, sink.getFileFormat());
        assertEquals(hiveHost, sink.getHiveHost());
        assertEquals(hivePort, sink.getHivePort());
        assertEquals(krb5Auth, sink.getKrb5Auth());
        assertEquals(enableGrouping, sink.getEnableGrouping() ? "true" : "false");
    } // testConfigure

    /**
     * Test of start method, of class OrionHDFSSink.
     */
    @Test
    public void testStart() {
        System.out.println("start");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        sink.start();
        assertTrue(sink.getPersistenceBackend() != null);
        assertEquals(LifecycleState.START, sink.getLifecycleState());
    } // testStart

    /**
     * Test of persist method, of class OrionHDFSSink. File formats are tested.
     * @throws java.lang.Exception
     */
    @Test
    public void testPersistFileFormats() throws Exception {
        System.out.println("Testing OrionHDFSSinkTest.persist (json-row file format)");
        context.put("file_format", "json-row");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, singleServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, singleDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionHDFSSinkTest.persist (json-column file format)");
        context.put("file_format", "json-column");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, singleServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, singleDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionHDFSSinkTest.persist (csv-row file format)");
        context.put("file_format", "csv-row");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, singleServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, singleDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionHDFSSinkTest.persist (csv-column file format)");
        context.put("file_format", "csv-column");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, singleServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, singleDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersistFileFormats
    
    /**
     * Test of persist method, of class OrionHDFSSink. Special resources length is tested.
     * @throws java.lang.Exception
     */
    @Test
    public void testPersistResourceLengths() throws Exception {
        System.out.println("Testing OrionHDFSSinkTest.persist (normal resource lengths)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, singleServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, singleDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionHDFSSinkTest.persist (too long service name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, abnormalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, singleServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, singleDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionHDFSSinkTest.persist (too long servicePath name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, abnormalServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, singleDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionHDFSSinkTest.persist (too long destination name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, singleServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, abnormalDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
    } // testPersistResourceLengths
    
    /**
     * Test of persist method, of class OrionHDFSSink. Special service and service-path are tested.
     * @throws java.lang.Exception
     */
    @Test
    public void testPersistServiceServicePath() throws Exception {
        System.out.println("Testing OrionHDFSSinkTest.persist (\"root\" servicePath name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, rootServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, singleDestinationName);
        
        try {
            sink.persist(headers, singleNotifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionHDFSSinkTest.persist (multiple destinations and "
                + "fiware-servicePaths)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_NOTIFIED_SERVICE, normalServiceName);
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS, multipleServicePathName);
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS, multipleDestinationName);
        
        try {
            sink.persist(headers, multipleNotifyContextRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersistServiceServicePath
    
} // OrionHDFSSinkTest
