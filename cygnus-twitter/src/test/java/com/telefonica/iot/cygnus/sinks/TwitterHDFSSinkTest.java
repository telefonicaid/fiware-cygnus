package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImplREST;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.lifecycle.LifecycleState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * @author jpalanca
 */
@RunWith(MockitoJUnitRunner.class)
public class TwitterHDFSSinkTest {

    // mocks
    @Mock
    private HDFSBackendImplREST mockWebHDFSBackend;

    // instance to be tested
    private TwitterHDFSSink sink;

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

    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        sink = new TwitterHDFSSink();
        sink.setPersistenceBackend(mockWebHDFSBackend);

        // set up the behaviour of the mocked classes
        when(mockWebHDFSBackend.exists(null)).thenReturn(true);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).createDir(null);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).createFile(null, null);
        doNothing().doThrow(new Exception()).when(mockWebHDFSBackend).append(null, null);
    } // setUp


    /**
     * Test of configure method, of class TwitterHDFSSink.
     */
    @Test
    public void testConfigure() {
        System.out.println("Testing TwitterHDFSSinkTest.configure");
        Context context = createContext();
        sink.configure(context);
        assertEquals(cosmosHost[0], sink.getHDFSHosts()[0]);
        assertEquals(cosmosPort, sink.getHDFSPort());
        assertEquals(hdfsUsername, sink.getHDFSUsername());
        assertEquals(hdfsPassword, sink.getHDFSPassword());
        assertEquals(oauth2Token, sink.getOAuth2Token());
        assertEquals(serviceAsNamespace, sink.getServiceAsNamespace());
        assertEquals(enableHive, sink.getEnableHive() ? "true" : "false");
        assertEquals(hiveServerVersion, sink.getHiveServerVersion());
        assertEquals(hiveHost, sink.getHiveHost());
        assertEquals(hivePort, sink.getHivePort());
        assertEquals(enableKrb5Auth, sink.getEnableKrb5Auth());
        assertEquals(enableGrouping, sink.getEnableGrouping() ? "true" : "false");
    } // testConfigure

    /**
     * Test of start method, of class TwitterHDFSSink.
     */
    @Test
    public void testStart() {
        System.out.println("Testing TwitterHDFSSink.start");
        Context context = createContext();
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        sink.start();
        assertTrue(sink.getPersistenceBackend() != null);
        assertEquals(LifecycleState.START, sink.getLifecycleState());
    } // testStart


    private Context createContext() {
        Context context = new Context();
        context.put("hdfs_password", hdfsPassword);

        context.put("hdfs_host", cosmosHost[0]);
        context.put("hdfs_port", cosmosPort);
        context.put("hdfs_username", hdfsUsername);

        context.put("oauth2_token", oauth2Token);
        context.put("service_as_namespace", serviceAsNamespace);
        context.put("hive", enableHive);
        context.put("hive.server_version", hiveServerVersion);
        context.put("hive.host", hiveHost);
        context.put("hive.port", hivePort);

        context.put("krb5_auth", enableKrb5Auth);
        context.put("enable_grouping", enableGrouping);
        return context;
    } // createContext
}
