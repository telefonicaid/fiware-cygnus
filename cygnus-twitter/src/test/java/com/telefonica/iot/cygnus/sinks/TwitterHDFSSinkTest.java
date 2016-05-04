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
    private final String hdfsFolder = "test_folder";
    private final String hdfsFile = "test_file.txt";
    private final String oauth2Token = "tokenabcdefghijk";
    private final String enableHive = "false";
    private final String enableKrb5Auth = "false";

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
        assertEquals(enableHive, sink.getEnableHive() ? "true" : "false");
        assertEquals(enableKrb5Auth, sink.getEnableKrb5Auth());
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

        context.put("hdfs_folder", hdfsFolder);
        context.put("hdfs_file", hdfsFile);

        context.put("oauth2_token", oauth2Token);
        context.put("krb5_auth", enableKrb5Auth);
        return context;
    } // createContext
}
