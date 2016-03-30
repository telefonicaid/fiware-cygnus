/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 * <p>
 * This file is part of fiware-cygnus (FI-WARE project).
 * <p>
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 * <p>
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.backends.cassandra.CassandraBackendImpl;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.utils.TestUtils;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.lifecycle.LifecycleState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Adapted from {@link OrionPostgreSQLSinkTest}
 *
 * @author jdegenhardt
 */
@SuppressWarnings("FieldCanBeLocal")
@RunWith(MockitoJUnitRunner.class)
public class OrionCassandraSinkTest {

    // context constants
    private final String[] cassandraHost = {"localhost"};
    private final String cassandraUsername = "user1";
    private final String cassandraPassword = "pass1234";
    private final String attrPersistence = "row";
    private final String enableGrouping = "true";
    // batches constants
    private final Long recvTimeTs = 123456789L;
    private final String normalService = "vehicles";
    private final String abnormalService =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongservname";
    private final String rootServicePath = "";
    private final String normalGroupedServicePath = "cars";
    private final String abnormalGroupedServicePath =
            "tooooooooooooooooooooooooooooooooooooooooooooooooooooooooolongservpathname";
    private final String normalGroupedDestination = "my_cars";
    private final String abnormalGroupedDestination =
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
    // mocks
    @Mock
    private CassandraBackendImpl mockCassandraBackend;
    // instance to be tested
    private OrionCassandraSink sink;
    // other inmutable instances
    private NotifyContextRequest singleNotifyContextRequest;
    private NotifyContextRequest multipleNotifyContextRequest;

    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        sink = new OrionCassandraSink();
        sink.setPersistenceBackend(mockCassandraBackend);

        // set up other immutable instances
        singleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(singleContextElementNotification);
        multipleNotifyContextRequest = TestUtils.createJsonNotifyContextRequest(multipleContextElementNotification);
    } // setUp

    /**
     * Test of configure method, of class OrionCassandraSink.
     */
    @Test
    public void testConfigure() {
        System.out.println("Testing OrionCassandraSink.configure");
        Context context = createContext();
        sink.configure(context);
        assertArrayEquals(cassandraHost, sink.getCassandraHosts());
        assertEquals(cassandraUsername, sink.getCassandraUsername());
        assertEquals(cassandraPassword, sink.getCassandraPassword());
        assertEquals(attrPersistence, sink.getRowAttrPersistence() ? "row" : "column");
        assertEquals(enableGrouping, sink.getEnableGrouping() ? "true" : "false");
    } // testConfigure

    /**
     * Test of start method, of class OrionCassandraSink.
     */
    @Test
    public void testStart() {
        System.out.println("Testing OrionCassandraSink.start");
        Context context = createContext();
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        sink.start();
        assertTrue(sink.getPersistenceBackend() != null);
        assertEquals(LifecycleState.START, sink.getLifecycleState());
    } // testStart

    /**
     * Test of persistBatch method, of class OrionCassandraSink. Null batches are tested.
     */
    @Test
    public void testPersistNullBatches() {
        System.out.println("Testing OrionCassandraSinkTest.persist (null batches)");
        Context context = createContext();
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
     * Test of persistBath method, of class OrionCassandraSink. Special resources length are tested.
     *
     * @throws Exception
     */
    @Test
    public void testPersistResourceLengths() throws Exception {
        // common objects
        Context context = createContext();

        System.out.println("Testing OrionCassandraSinkTest.persistBatch (normal resource lengths)");
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

        System.out.println("Testing OrionCassandraSinkTest.persistBatch (too long service name)");
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

        System.out.println("Testing OrionCassandraSinkTest.persistBatch (too long servicePath name)");
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

        System.out.println("Testing OrionCassandraSinkTest.persistBatch (too long destination name)");
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
     * Test of persistBatch method, of class OrionCassandraSink. Special service and service-path are tested.
     *
     * @throws Exception
     */
    @Test
    public void testPersistServiceServicePath() throws Exception {
        // common objects
        Context context = createContext();

        System.out.println("Testing OrionCassandraSinkTest.persistBatch (\"root\" servicePath name)");
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

        System.out.println("Testing OrionCassandraSinkTest.persistBatch (multiple destinations and "
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
                              NotifyContextRequest.ContextElement contextElement) {
        CygnusEvent groupedEvent = new CygnusEvent(recvTimeTs, service, servicePath, destination, null,
                contextElement);
        Batch batch = new Batch();
        batch.addEvent(destination, groupedEvent);
        return batch;
    } // createBatch

    private Context createContext() {
        Context context = new Context();
        context.put("cassandra_host", Arrays.toString(cassandraHost));
        context.put("cassandra_username", cassandraUsername);
        context.put("cassandra_password", cassandraPassword);
        context.put("attr_persistence", attrPersistence);
        context.put("enable_grouping", enableGrouping);
        return context;
    } // createContext

} // OrionCassandraSinkTest
