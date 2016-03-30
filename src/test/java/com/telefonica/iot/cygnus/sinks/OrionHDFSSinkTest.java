/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
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

import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImplREST;
import static org.junit.Assert.*; // this is required by "fail" like assertions
import static org.mockito.Mockito.*; // this is required by "when" like functions
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.utils.TestUtils;
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
    private HDFSBackendImplREST mockWebHDFSBackend;
    
    // instance to be tested
    private OrionHDFSSink sink;
    
    // other inmutable instances
    private NotifyContextRequest singleNotifyContextRequest;
    private NotifyContextRequest multipleNotifyContextRequest;
    
    // context constants
    private final String[] cosmosHost = {"localhost"};
    private final String cosmosPort = "14000";
    private final String hdfsUsername = "user1";
    private final String hdfsPassword = "12345";
    private final String oauth2Token = "tokenabcdefghijk";
    private final String serviceAsNamespace = "false";
    private final String enableHive = "true";
    private final String hiveServerVersion = "2";
    private final String hiveHost = "localhost";
    private final String hivePort = "10000";
    private final String enableKrb5Auth = "false";
    private final String enableGrouping = "true";
    private final String csvSeparator = ",";
    
    // batches constants
    private final Long recvTimeTs = 123456789L;
    private final String normalService = "vehicles";
    private final String abnormalService =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "longservname";
    private final String normalDefaultServicePath = "/4wheels";
    private final String rootServicePath = "/";
    private final String abnormalDefaultServicePath =
            "/tooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "longservpathname";
    private final String normalGroupedServicePath = "cars";
    private final String abnormalGroupedServicePath =
            "/tooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "longservpathname";
    private final String normalDefaultDestination = "car1_car";
    private final String abnormalDefaultDestination =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "longdestname";
    private final String normalGroupedDestination = "my_cars";
    private final String abnormalGroupedDestination =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
            + "longdestname";
    
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
        
        // set up other immutable instances
        singleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(singleContextElementNotification);
        multipleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(multipleContextElementNotification);
        
        // set up the behaviour of the mocked classes
        when(mockWebHDFSBackend.exists(null)).thenReturn(true);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).createDir(null);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).createFile(null, null);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).append(null, null);
    } // setUp

    /**
     * Test of configure method, of class OrionHDFSSink. Deprecated parameters are used.
     */
    @Test
    public void testConfigureDeprecatedParams() {
        System.out.println("Testing OrionHDFSSink.configure (deprecated parameters are used)");
        String fileFormat = "json-row";
        Context context = createContext(fileFormat);
        sink.configure(context);
        assertEquals(cosmosHost[0], sink.getHDFSHosts()[0]);
        assertEquals(cosmosPort, sink.getHDFSPort());
        assertEquals(hdfsUsername, sink.getHDFSUsername());
        assertEquals(hdfsPassword, sink.getHDFSPassword());
        assertEquals(oauth2Token, sink.getOAuth2Token());
        assertEquals(serviceAsNamespace, sink.getServiceAsNamespace());
        assertEquals(fileFormat, sink.getFileFormat());
        assertEquals(enableHive, sink.getEnableHive() ? "true" : "false");
        assertEquals(hiveServerVersion, sink.getHiveServerVersion());
        assertEquals(hiveHost, sink.getHiveHost());
        assertEquals(hivePort, sink.getHivePort());
        assertEquals(enableKrb5Auth, sink.getEnableKrb5Auth());
        assertEquals(enableGrouping, sink.getEnableGrouping() ? "true" : "false");
    } // testConfigureDeprecatedParams
    
    /**
     * Test of configure method, of class OrionHDFSSink. No deprecated parameters are used.
     */
    @Test
    public void testConfigure() {
        System.out.println("Testing OrionHDFSSinkTest.configure");
        String fileFormat = "json-row";
        Context context = createContext(fileFormat);
        sink.configure(context);
        assertEquals(cosmosHost[0], sink.getHDFSHosts()[0]);
        assertEquals(cosmosPort, sink.getHDFSPort());
        assertEquals(hdfsUsername, sink.getHDFSUsername());
        assertEquals(hdfsPassword, sink.getHDFSPassword());
        assertEquals(oauth2Token, sink.getOAuth2Token());
        assertEquals(serviceAsNamespace, sink.getServiceAsNamespace());
        assertEquals(fileFormat, sink.getFileFormat());
        assertEquals(enableHive, sink.getEnableHive() ? "true" : "false");
        assertEquals(hiveServerVersion, sink.getHiveServerVersion());
        assertEquals(hiveHost, sink.getHiveHost());
        assertEquals(hivePort, sink.getHivePort());
        assertEquals(enableKrb5Auth, sink.getEnableKrb5Auth());
        assertEquals(enableGrouping, sink.getEnableGrouping() ? "true" : "false");
    } // testConfigure

    /**
     * Test of start method, of class OrionHDFSSink.
     */
    @Test
    public void testStart() {
        System.out.println("Testing OrionHDFSSink.start");
        String fileFormat = "json-row";
        Context context = createContext(fileFormat);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        sink.start();
        assertTrue(sink.getPersistenceBackend() != null);
        assertEquals(LifecycleState.START, sink.getLifecycleState());
    } // testStart

    /**
     * Test of persistBatch method, of class OrionHDFSSink. Null batches are tested.
     */
    @Test
    public void testPersistNullBatches() {
        System.out.println("Testing OrionHDFSSink.persistBatch (null batches)");
        String fileFormat = "json-row";
        Context context = createContext(fileFormat);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(null);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersistNullBatches
    
    /**
     * Test of persistBatch method, of class OrionHDFSSink. File formats are tested.
     */
    @Test
    public void testPersistFileFormats() {
        // common objects
        Batch groupedBatch = createBatch(recvTimeTs, normalService, normalGroupedServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        System.out.println("Testing OrionHDFSSink.persistBatch (json-row file format)");
        String fileFormat = "json-row";
        Context context = createContext(fileFormat);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionHDFSSink.persistBatch (json-column file format)");
        fileFormat = "json-column";
        context = createContext(fileFormat);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionHDFSSink.persistBatch (csv-row file format)");
        fileFormat = "csv-row";
        context = createContext(fileFormat);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionHDFSSink.persistBatch (csv-column file format)");
        fileFormat = "csv-column";
        context = createContext(fileFormat);
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersistFileFormats
    
    /**
     * Test of persistBatch method, of class OrionHDFSSink. Special resources length is tested.
     * @throws java.lang.Exception
     */
    @Test
    public void testPersistResourceLengths() throws Exception {
        // common objects
        String fileFormat = "json-row";
        Context context = createContext(fileFormat);
        
        System.out.println("Testing OrionHDFSSink.persistBatch (normal resource lengths)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        Batch groupedBatch = createBatch(recvTimeTs, normalService, normalGroupedServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionHDFSSink.persistBatch (too long service name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        groupedBatch = createBatch(recvTimeTs, abnormalService, normalGroupedServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(groupedBatch);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionHDFSSink.persistBatch (too long servicePath name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        groupedBatch = createBatch(recvTimeTs, normalService, abnormalGroupedServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(groupedBatch);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
        
        System.out.println("Testing OrionHDFSSink.persistBatch (too long destination name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        groupedBatch = createBatch(recvTimeTs, normalService, normalGroupedServicePath, abnormalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(groupedBatch);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        } // try catch
    } // testPersistResourceLengths
    
    /**
     * Test of persistBatch method, of class OrionHDFSSink. Special service and service-path are tested.
     * @throws java.lang.Exception
     */
    @Test
    public void testPersistServiceServicePath() throws Exception {
        // common objects
        String fileFormat = "json-row";
        Context context = createContext(fileFormat);
        
        System.out.println("Testing OrionHDFSSink.persistBatch (\"root\" servicePath name)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        Batch groupedBatch = createBatch(recvTimeTs, normalService, rootServicePath, normalGroupedDestination,
                singleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
        
        System.out.println("Testing OrionHDFSSink.persistBatch (multiple destinations and "
                + "fiware-servicePaths)");
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        groupedBatch = createBatch(recvTimeTs, normalService, normalGroupedServicePath, normalGroupedDestination,
                multipleNotifyContextRequest.getContextResponses().get(0).getContextElement());
        
        try {
            sink.persistBatch(groupedBatch);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch finally
    } // testPersistServiceServicePath
    
    private Batch createBatch(long recvTimeTs, String service, String servicePath, String destination,
            ContextElement contextElement) {
        CygnusEvent groupedEvent = new CygnusEvent(recvTimeTs, service, servicePath, destination, null,
            contextElement);
        Batch batch = new Batch();
        batch.addEvent(destination, groupedEvent);
        return batch;
    } // createBatch
    
    private Context createContext(String fileFormat) {
        Context context = new Context();
        context.put("hdfs_password", hdfsPassword);

        context.put("hdfs_host", cosmosHost[0]);
        context.put("hdfs_port", cosmosPort);
        context.put("hdfs_username", hdfsUsername);
        
        context.put("csv_separator", csvSeparator);
        context.put("oauth2_token", oauth2Token);
        context.put("service_as_namespace", serviceAsNamespace);
        context.put("file_format", fileFormat);
        context.put("hive", enableHive);
        context.put("hive.server_version", hiveServerVersion);
        context.put("hive.host", hiveHost);
        context.put("hive.port", hivePort);
        
        context.put("krb5_auth", enableKrb5Auth);
        context.put("enable_grouping", enableGrouping);
        return context;
    } // createContext
    
} // OrionHDFSSinkTest
