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

import com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImplREST;
import org.apache.flume.Context;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.lifecycle.LifecycleState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
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
        System.out.println(getTestTraceHead("[TwitterHDFSSinkTest.configure]")
                + "-------- Configure HDFS parameters.");
        Context context = createContext();
        sink.configure(context);
        try {
            assertEquals(cosmosHost[0], sink.getHDFSHosts()[0]);
            assertEquals(cosmosPort, sink.getHDFSPort());
            assertEquals(hdfsUsername, sink.getHDFSUsername());
            assertEquals(hdfsPassword, sink.getHDFSPassword());
            assertEquals(oauth2Token, sink.getOAuth2Token());
            assertEquals(enableHive, sink.getEnableHive() ? "true" : "false");
            assertEquals(enableKrb5Auth, sink.getEnableKrb5Auth());
            System.out.println(getTestTraceHead("[TwitterHDFSSinkTest.configure]")
                    + "-  OK  - HDFS parameters detected in context.");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[TwitterHDFSSinkTest.configure]")
                    + "- FAIL - HDFS parameters not detected in context.");
            throw e;
        } // try catch
    } // testConfigure

    /**
     * Test of start method, of class TwitterHDFSSink.
     */
    @Test
    public void testStart() {
        System.out.println(getTestTraceHead("[TwitterHDFSSinkTest.start]")
                + "-------- Start HDFS sink.");
        Context context = createContext();
        sink.configure(context);
        sink.setChannel(new MemoryChannel());
        sink.start();
        try {
            assertTrue(sink.getPersistenceBackend() != null);
            assertEquals(LifecycleState.START, sink.getLifecycleState());
            System.out.println(getTestTraceHead("[TwitterHDFSSinkTest.start]")
                    + "-  OK  - HDFS sink started.");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[TwitterHDFSSinkTest.start]")
                    + "- FAIL - HDFS sink could not start.");
            throw e;
        } // try catch
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
