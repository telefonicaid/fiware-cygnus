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

package com.telefonica.iot.cygnus.management;

import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.backends.orion.OrionBackendImpl;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*; // this is required by "when" like functions
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import java.io.BufferedWriter;
import java.io.FileWriter;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author frb
 */

@RunWith(MockitoJUnitRunner.class)
public class ManagementInterfaceTest {
    
     private ManagementInterface managementInterface;
    /**
     * Constructor.
     */
    public ManagementInterfaceTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // ManagementInterfaceTest
    
    /**
     * Custom implementation of HttpServletResponse.
     */
    public class HttpServletResponseImpl extends HttpServletResponseWrapper {

        private int httpStatus;

        /**
         * Constructor.
         * @param response
         */
        public HttpServletResponseImpl(HttpServletResponse response) {
            super(response);
        } // HttpServletResponseImpl

        @Override
        public void sendError(int sc) throws IOException {
            httpStatus = sc;
            super.sendError(sc);
        } // sendError

        @Override
        public void sendError(int sc, String msg) throws IOException {
            httpStatus = sc;
            super.sendError(sc, msg);
        } // sendError

        @Override
        public void setStatus(int sc) {
            httpStatus = sc;
            super.setStatus(sc);
        } // setStatus

        public int getStatus() {
            return httpStatus;
        } // getStatus

    } // HttpServletResponseImpl
    
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();
    public final TemporaryFolder folderInstance = new TemporaryFolder();
    
    // mocks
    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private OrionBackendImpl orionBackend;
    
    // constants
    private final String requestURI = "/v1/version";
    private final String postURIv1 = "/v1/subscriptions?ngsi_version=1";
    private final String postURIv2 = "/v1/subscriptions?ngsi_version=2";
    private final String deleteURIv1 = "/v1/subscriptions?ngsi_version=1&subscription_id=12345";
    private final String deleteURIv2 = "/v1/subscriptions?ngsi_version=2&subscription_id=12345";
    private final String getURIv2 = "/v1/subscriptions?ngsi_version=2&subscription_id=12345";
    private final String getAllURIv2 = "/v1/subscriptions?ngsi_version=2";
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final HttpServletRequest mockRequestV1 = mock(HttpServletRequest.class);
    private final HttpServletRequest mockRequestV2 = mock(HttpServletRequest.class);
    private final HttpServletRequest mockRequestNoSubscriptionV1 = mock(HttpServletRequest.class);
    private final HttpServletRequest mockRequestEmptyFieldV1 = mock(HttpServletRequest.class);
    private final HttpServletRequest mockRequestMissingFieldV1 = mock(HttpServletRequest.class);
    private final HttpServletRequest mockRequestNoSubscriptionV2 = mock(HttpServletRequest.class);
    private final HttpServletRequest mockRequestEmptyFieldV2 = mock(HttpServletRequest.class);
    private final HttpServletRequest mockRequestMissingFieldV2 = mock(HttpServletRequest.class);
    private final HttpServletRequest mockDeleteSubscriptionV1 = mock(HttpServletRequest.class);
    private final HttpServletRequest mockDeleteSubscriptionV2 = mock(HttpServletRequest.class);
    private final HttpServletRequest mockGetSubscriptionV2 = mock(HttpServletRequest.class);
    private final HttpServletRequest mockGetAllSubscriptionsV2 = mock(HttpServletRequest.class);
    private final HttpServletRequest mockGetAllAgentParameters = mock(HttpServletRequest.class);
    private final HttpServletRequest mockGetOneAgentParameter = mock(HttpServletRequest.class);
    private final HttpServletRequest mockPostOneAgentParameter = mock(HttpServletRequest.class);
    private final HttpServletRequest mockPutOneAgentParameter = mock(HttpServletRequest.class);
    private final HttpServletRequest mockDeleteOneAgentParameter = mock(HttpServletRequest.class);
    private final HttpServletRequest mockRequestBadFileName = mock(HttpServletRequest.class);
    private final HttpServletRequest mockGetAllInstanceParameters = mock(HttpServletRequest.class);
    private final HttpServletRequest mockGetOneInstanceParameter = mock(HttpServletRequest.class);
    private final HttpServletRequest mockPutOneInstanceParameter = mock(HttpServletRequest.class);
    private final HttpServletRequest mockPostOneInstanceParameter = mock(HttpServletRequest.class);
    private final HttpServletRequest mockDeleteOneInstanceParameter = mock(HttpServletRequest.class);
    private final HttpServletRequest mockGetLoggingLevel = mock(HttpServletRequest.class);
    private final HttpServletRequest mockPutLoggingLevel = mock(HttpServletRequest.class);
    private final HttpServletRequest mockPutInvalidLoggingLevel = mock(HttpServletRequest.class);
    private final HttpServletRequest mockInvalidAPI = mock(HttpServletRequest.class);
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // Define subscriptions for each case
        String subscriptionV1 = "{\"subscription\":{\"entities\": [{\"type\": \"Trainer\",\"isPattern\": \"false\",\"id\": \"Trainer1\"}],\"attributes\": [],\"reference\": \"http://localhost:5050/notify\",\"duration\": \"P1M\",\"notifyConditions\": [{\"type\": \"ONCHANGE\",\"condValues\": []}],\"throttling\": \"PT5S\"}, \"endpoint\":{\"host\":\"orion.lab.fiware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String subscriptionV2 = "{\"subscription\":{\"description\": \"One subscription to rule them all\",\"subject\": {\"entities\": [{\"idPattern\": \".*\",\"type\": \"Room\"}],\"condition\": {\"attrs\": [\"temperature\"],\"expression\": {\"q\": \"temperature>40\"}}},\"notification\": {\"http\": {\"url\": \"http://localhost:1234\"},\"attrs\": [\"temperature\",\"humidity\"]},\"expires\": \"2016-05-05T14:00:00.00Z\",\"throttling\": 5}, \"endpoint\":{\"host\":\"orion.lab.fiware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String missingSubscription = "{\"endpoint\":{\"host\":\"orion.lab.fiware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String subsEmptyfieldV1 = "{\"subscription\":{\"entities\": [{\"type\": \"Trainer\",\"isPattern\": \"false\",\"id\": \"Trainer1\"}],\"attributes\": [],\"reference\": \"http://localhost:5050/notify\",\"duration\": \"P1M\",\"notifyConditions\": [{\"type\": \"\",\"condValues\": []}],\"throttling\": \"PT5S\"}, \"endpoint\":{\"host\":\"orion.lab.fiware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String subsMissingfieldV1 = "{\"subscription\":{\"entities\": [{\"type\": \"Trainer\",\"isPattern\": \"false\",\"id\": \"Trainer1\"}],\"attributes\": [],\"reference\": \"http://localhost:5050/notify\",\"notifyConditions\": [{\"type\": \"ONCHANGE\",\"condValues\": []}],\"throttling\": \"PT5S\"}, \"endpoint\":{\"host\":\"orion.lab.fiware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String subsEmptyfieldV2 = "{\"subscription\":{\"description\": \"One subscription to rule them all\",\"subject\": {\"entities\": [{\"idPattern\": \".*\",\"type\": \"Room\"}],\"condition\": {\"attrs\": [\"temperature\"],\"expression\": {\"q\": \"temperature>40\"}}},\"notification\": {\"http\": {\"url\": \"http://localhost:1234\"},\"attrs\": [\"temperature\",\"humidity\"]},\"expires\": \"\",\"throttling\": 5}, \"endpoint\":{\"host\":\"orion.lab.fiware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String subsV2MissingfieldV2 = "{\"subscription\":{\"description\": \"One subscription to rule them all\",\"subject\": {\"entities\": [{\"idPattern\": \".*\",\"type\": \"Room\"}],\"condition\": {\"attrs\": [\"temperature\"],\"expression\": {\"q\": \"temperature>40\"}}},\"notification\": {\"http\": {\"url\": \"http://localhost:1234\"},\"attrs\": [\"temperature\",\"humidity\"]},\"throttling\": 5}, \"endpoint\":{\"host\":\"orion.lab.fiware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String subscriptionDelete = "{\"host\":\"orion.lab.fiware.org\", \"port\": \"1026\", \"ssl\": \"false\", \"xauthtoken\": \"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}";
        String subscriptionGet = "{\"host\":\"orion.lab.fiware.org\", \"port\": \"1026\", \"ssl\": \"false\", \"xauthtoken\": \"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}";
        String token = "QsENv67AJj7blC2qJ0YvfSc5hMWYrs";
        String service = "default";
        String servicePath = "/default";
        
        // Define the readers with the subscriptions
        BufferedReader readerV1 = new BufferedReader(new StringReader(subscriptionV1));
        BufferedReader readerV2 = new BufferedReader(new StringReader(subscriptionV2));
        BufferedReader readerMissingSubsV1 = new BufferedReader(new StringReader(missingSubscription));
        BufferedReader readerMissingSubsV2 = new BufferedReader(new StringReader(missingSubscription));
        BufferedReader readerEmptyFieldV1 = new BufferedReader(new StringReader(subsEmptyfieldV1));
        BufferedReader readerMissingFieldV1 = new BufferedReader(new StringReader(subsMissingfieldV1));
        BufferedReader readerEmptyFieldV2 = new BufferedReader(new StringReader(subsEmptyfieldV2));
        BufferedReader readerMissingFieldV2 = new BufferedReader(new StringReader(subsV2MissingfieldV2));
        BufferedReader readerDeleteSubscription = new BufferedReader(new StringReader(subscriptionDelete));
        BufferedReader readerGetSubscription = new BufferedReader(new StringReader(subscriptionGet));
        PrintWriter writer = new PrintWriter(new ByteArrayOutputStream());
        
        JsonResponse responseDeleteV1 = new JsonResponse(null, 200, deleteURIv1, null);
        JsonResponse responseDeleteV2 = new JsonResponse(null, 200, deleteURIv2, null);
        
        // Set up the behaviour of the mocked classes
        when(mockRequest.getRequestURI()).thenReturn(requestURI);
        when(mockRequest.getMethod()).thenReturn("GET");
        
        when(mockRequestV1.getRequestURI()).thenReturn(postURIv1);
        when(mockRequestV1.getMethod()).thenReturn("POST");
        when(mockRequestV1.getReader()).thenReturn(readerV1);
        when(mockRequestV1.getParameter("ngsi_version")).thenReturn("1");
        
        when(mockRequestV2.getRequestURI()).thenReturn(postURIv2);
        when(mockRequestV2.getMethod()).thenReturn("POST");
        when(mockRequestV2.getReader()).thenReturn(readerV2);
        when(mockRequestV2.getParameter("ngsi_version")).thenReturn("2");
        
        when(mockRequestNoSubscriptionV1.getRequestURI()).thenReturn(postURIv1);
        when(mockRequestNoSubscriptionV1.getMethod()).thenReturn("POST");
        when(mockRequestNoSubscriptionV1.getReader()).thenReturn(readerMissingSubsV1);
        when(mockRequestNoSubscriptionV1.getParameter("ngsi_version")).thenReturn("1");
        
        when(mockRequestNoSubscriptionV2.getRequestURI()).thenReturn(postURIv2);
        when(mockRequestNoSubscriptionV2.getMethod()).thenReturn("POST");
        when(mockRequestNoSubscriptionV2.getReader()).thenReturn(readerMissingSubsV2);
        when(mockRequestNoSubscriptionV2.getParameter("ngsi_version")).thenReturn("2");
        
        when(mockRequestEmptyFieldV1.getRequestURI()).thenReturn(postURIv1);
        when(mockRequestEmptyFieldV1.getMethod()).thenReturn("POST");
        when(mockRequestEmptyFieldV1.getReader()).thenReturn(readerEmptyFieldV1);
        when(mockRequestEmptyFieldV1.getParameter("ngsi_version")).thenReturn("1");
        
        when(mockRequestMissingFieldV1.getRequestURI()).thenReturn(postURIv1);
        when(mockRequestMissingFieldV1.getMethod()).thenReturn("POST");
        when(mockRequestMissingFieldV1.getReader()).thenReturn(readerMissingFieldV1);
        when(mockRequestMissingFieldV1.getParameter("ngsi_version")).thenReturn("1");
        
        when(mockRequestEmptyFieldV2.getRequestURI()).thenReturn(postURIv2);
        when(mockRequestEmptyFieldV2.getMethod()).thenReturn("POST");
        when(mockRequestEmptyFieldV2.getReader()).thenReturn(readerEmptyFieldV2);
        when(mockRequestEmptyFieldV2.getParameter("ngsi_version")).thenReturn("2");
        
        when(mockRequestMissingFieldV2.getRequestURI()).thenReturn(postURIv2);
        when(mockRequestMissingFieldV2.getMethod()).thenReturn("POST");
        when(mockRequestMissingFieldV2.getReader()).thenReturn(readerMissingFieldV2);
        when(mockRequestMissingFieldV2.getParameter("ngsi_version")).thenReturn("2");
        
        when(mockDeleteSubscriptionV1.getRequestURI()).thenReturn(deleteURIv1);
        when(mockDeleteSubscriptionV1.getMethod()).thenReturn("DELETE");
        when(mockDeleteSubscriptionV1.getReader()).thenReturn(readerDeleteSubscription);
        when(mockDeleteSubscriptionV1.getParameter("ngsi_version")).thenReturn("1");
        when(mockDeleteSubscriptionV1.getParameter("subscription_id")).thenReturn("12345");
        
        when(mockDeleteSubscriptionV2.getRequestURI()).thenReturn(deleteURIv2);
        when(mockDeleteSubscriptionV2.getMethod()).thenReturn("DELETE");
        when(mockDeleteSubscriptionV2.getReader()).thenReturn(readerDeleteSubscription);
        when(mockDeleteSubscriptionV2.getParameter("ngsi_version")).thenReturn("2");
        when(mockDeleteSubscriptionV2.getParameter("subscription_id")).thenReturn("12345");
        
        when(mockGetSubscriptionV2.getRequestURI()).thenReturn(getURIv2);
        when(mockGetSubscriptionV2.getMethod()).thenReturn("GET");
        when(mockGetSubscriptionV2.getReader()).thenReturn(readerGetSubscription);
        when(mockGetSubscriptionV2.getParameter("ngsi_version")).thenReturn("2");
        when(mockGetSubscriptionV2.getParameter("subscription_id")).thenReturn("12345");
        
        when(mockGetAllSubscriptionsV2.getRequestURI()).thenReturn(getAllURIv2);
        when(mockGetAllSubscriptionsV2.getMethod()).thenReturn("GET");
        when(mockGetAllSubscriptionsV2.getReader()).thenReturn(readerGetSubscription);
        when(mockGetAllSubscriptionsV2.getParameter("ngsi_version")).thenReturn("2");
        
        when(response.getWriter()).thenReturn(writer);
        
        when(orionBackend.deleteSubscriptionV1(subscriptionDelete, token, service, servicePath)).
                thenReturn(responseDeleteV1);
        when(orionBackend.deleteSubscriptionV2(subscriptionDelete, token, service, servicePath)).
                thenReturn(responseDeleteV2);
        
        File fileGetAll;
        
        try {
            fileGetAll = folder.newFile("agent_cygnus_all.conf");
            
            try (PrintWriter out = new PrintWriter(fileGetAll)) {
                out.println("cygnusagent.sources = http-source\n"
                        + "cygnusagent.sinks = mysql-sink\n"
                        + "cygnusagent.channels = mysql-channel");
                out.flush();
            } // try
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        } // try catch
        
        File fileGetOnePutPostDelete;
        
        try {
            fileGetOnePutPostDelete = folder.newFile("agent_cygnus_rest.conf");
            
            try (PrintWriter out = new PrintWriter(fileGetOnePutPostDelete)) {
                out.println("cygnusagent.sources = http-source\n"
                        + "cygnusagent.sinks = mysql-sink\n"
                        + "cygnusagent.channels = mysql-channel\n"
                        + "cygnusagent.delete_param = delete_value");
                out.flush();
            } // try
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        } // try catch
        
        File fileBadFileName;
        
        try {
            fileBadFileName = folder.newFile("cygnus_agent.conf");
        } catch (IOException e) {
            throw new AssertionError(e.getMessage());
        } // try catch
        
        when(mockGetAllAgentParameters.getMethod()).thenReturn("GET");
        when(mockGetAllAgentParameters.getRequestURI()).thenReturn("/admin/configuration/agent/"
                + fileGetAll.getAbsolutePath());
        
        when(mockGetOneAgentParameter.getMethod()).thenReturn("GET");
        when(mockGetOneAgentParameter.getParameter("param")).thenReturn("cygnusagent.sources");
        when(mockGetOneAgentParameter.getRequestURI()).thenReturn("/admin/configuration/agent/"
                + fileGetOnePutPostDelete.getAbsolutePath());
        
        when(mockPostOneAgentParameter.getMethod()).thenReturn("POST");
        when(mockPostOneAgentParameter.getParameter("param")).thenReturn("cygnusagent.put_parameter");
        when(mockPostOneAgentParameter.getParameter("value")).thenReturn("put_value");
        when(mockPostOneAgentParameter.getRequestURI()).thenReturn("/admin/configuration/agent/"
                + fileGetOnePutPostDelete.getAbsolutePath());
        
        when(mockPutOneAgentParameter.getMethod()).thenReturn("PUT");
        when(mockPutOneAgentParameter.getParameter("param")).thenReturn("cygnusagent.post_parameter");
        when(mockPutOneAgentParameter.getParameter("value")).thenReturn("post_value");
        when(mockPutOneAgentParameter.getRequestURI()).thenReturn("/admin/configuration/agent/"
                + fileGetOnePutPostDelete.getAbsolutePath());
        
        when(mockDeleteOneAgentParameter.getMethod()).thenReturn("DELETE");
        when(mockDeleteOneAgentParameter.getParameter("param")).thenReturn("cygnusagent.delete_param");
        when(mockDeleteOneAgentParameter.getRequestURI()).thenReturn("/admin/configuration/agent/"
                + fileGetOnePutPostDelete.getAbsolutePath());
        
        when(mockRequestBadFileName.getMethod()).thenReturn("GET");
        when(mockRequestBadFileName.getRequestURI()).thenReturn("/admin/configuration/agent/"
                + fileBadFileName.getAbsolutePath());
        
        File instanceGetAll;
        folderInstance.create();
        instanceGetAll = folderInstance.newFolder("usr");
        instanceGetAll.mkdir();
        instanceGetAll = folderInstance.newFolder("usr/cygnus");
        instanceGetAll.mkdir();
        instanceGetAll = folderInstance.newFolder("usr/cygnus/conf");
        instanceGetAll.mkdir();
        instanceGetAll = folderInstance.newFile("usr/cygnus/conf/cygnus_instance.conf");
        
        try (BufferedWriter out = new BufferedWriter(new FileWriter(instanceGetAll))) {
            out.write("LOGFILE_NAME=cygnus.log\n"
                    + "ADMIN_PORT=8081\n"
                    + "POLLING_INTERVAL=30\n"
                    + "RANDOM_PARAM=true");
        } // try
        
        when(mockGetAllInstanceParameters.getMethod()).thenReturn("GET");
        when(mockGetAllInstanceParameters.getRequestURI()).thenReturn("/admin/configuration/instance"
                + instanceGetAll.getAbsolutePath());
        
        when(mockGetOneInstanceParameter.getMethod()).thenReturn("GET");
        when(mockGetOneInstanceParameter.getParameter("param")).thenReturn("ADMIN_PORT");
        when(mockGetOneInstanceParameter.getRequestURI()).thenReturn("/admin/configuration/instance"
                + instanceGetAll.getAbsolutePath());
        
        when(mockPutOneInstanceParameter.getMethod()).thenReturn("PUT");
        when(mockPutOneInstanceParameter.getParameter("param")).thenReturn("RANDOM_PARAM");
        when(mockPutOneInstanceParameter.getParameter("value")).thenReturn("false");
        when(mockPutOneInstanceParameter.getRequestURI()).thenReturn("/admin/configuration/instance"
                + instanceGetAll.getAbsolutePath());
        
        when(mockPostOneInstanceParameter.getMethod()).thenReturn("POST");
        when(mockPostOneInstanceParameter.getParameter("param")).thenReturn("POSTED_PARAM");
        when(mockPostOneInstanceParameter.getParameter("value")).thenReturn("posted_value");
        when(mockPostOneInstanceParameter.getRequestURI()).thenReturn("/admin/configuration/instance"
                + instanceGetAll.getAbsolutePath());
        
        when(mockDeleteOneInstanceParameter.getMethod()).thenReturn("DELETE");
        when(mockDeleteOneInstanceParameter.getParameter("param")).thenReturn("RANDOM_PARAM");
        when(mockDeleteOneInstanceParameter.getRequestURI()).thenReturn("/admin/configuration/instance"
                + instanceGetAll.getAbsolutePath());
        
        when(mockGetLoggingLevel.getMethod()).thenReturn("GET");
        when(mockGetLoggingLevel.getRequestURI()).thenReturn("/admin/log");
        
        when(mockPutLoggingLevel.getMethod()).thenReturn("PUT");
        when(mockPutLoggingLevel.getParameter("level")).thenReturn("INFO");
        when(mockPutLoggingLevel.getRequestURI()).thenReturn("/admin/log");
        
        when(mockPutInvalidLoggingLevel.getMethod()).thenReturn("PUT");
        when(mockPutInvalidLoggingLevel.getParameter("level")).thenReturn("CUSTOM");
        when(mockPutInvalidLoggingLevel.getRequestURI()).thenReturn("/admin/log");
    } // setUp
    
    /**
     * Cleans up.
     * @throws Exception
     */
    @After
    public void cleanUp() throws Exception {
        folder.delete();
        folderInstance.delete();
    } // cleanUp
    
    /**
     * Test of handle method, of class ManagementInterface.
     */
    @Test
    public void testHandle() {
        System.out.println(getTestTraceHead("[ManagementInterface.handle]") + " - Testing ManagementInterface.handle");
        ManagementInterface managementInterface = new ManagementInterface(null, new File(""), null, null, null,
                8081, 8082);
        
        try {
            managementInterface.handle(null, mockRequest, mockResponse, 1);
        } catch (IOException | ServletException e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch
        
    } // testHandle
    
    /**
     * [ManagementInterface] -------- 'POST method posts a valid subscription (ngsi_version = 1)'.
     * @throws java.lang.Exception
     */
    // @Test
    public void testPostMethodPostAValidSubscriptionV1() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - POST method posts a "
                + "valid subscription (ngsi_version = 1)");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
             
        try {
            SubscriptionsHandlers.post(mockRequestV1, responseWrapper);
        } catch (Exception e) {
            System.out.println("There was some problem when handling the POST subscription");
            throw e;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " -  OK  - Valid "
                    + "subscription");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - FAIL - Invalid "
                    + "subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostAValidSubscriptionV1
    
    /**
     * [ManagementInterface] -------- 'POST method posts a valid subscription (ngsi_version = 2)'.
     * @throws java.lang.Exception
     */
    // @Test
    public void testPostMethodPostAValidSubscriptionV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - POST method posts a "
                + "valid subscription (ngsi_version = 2)");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            SubscriptionsHandlers.post(mockRequestV2, responseWrapper);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " -  OK  - Valid "
                    + "subscription");
            throw e;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " -  OK  - Valid "
                    + "subscription");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - FAIL - Invalid "
                    + "subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostAValidSubscriptionV2
    
    /**
     * [ManagementInterface] -------- 'POST method doesn't find a subscription (ngsi_version = 1)'.
     * @throws java.lang.Exception
     */
    // @Test
    public void testPostMethodPostHasNotSubscriptionV1() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - POST method doesn't "
                + "find a subscription (ngsi_version = 1)");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            SubscriptionsHandlers.post(mockRequestNoSubscriptionV1, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST subscription");
            throw x;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " -  OK  - "
                    + "Subscription not found");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - FAIL - "
                    + "Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostHasNotSubscriptionV1
    
    /**
     * [ManagementInterface] -------- 'POST method doesn't find a subscription (ngsi_version = 2)'.
     * @throws java.lang.Exception
     */
    // @Test
    public void testPostMethodPostHasNotSubscriptionV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - POST method doesn't "
                + "find a subscription (ngsi_version = 2)");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            SubscriptionsHandlers.post(mockRequestNoSubscriptionV2, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST subscription");
            throw x;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " -  OK  - "
                    + "Subscription not found");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - FAIL - "
                    + "Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostHasNotSubscriptionV2
    
    /**
     * [ManagementInterface] -------- 'POST method post a subscription with any empty field (ngsi_version = 1)'.
     * @throws java.lang.Exception
     */
    // @Test
    public void testPostMethodPostHasEmptyFieldsV1() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - POST method post a "
                + "subscription with empty fields (ngsi_version = 1)");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            SubscriptionsHandlers.post(mockRequestEmptyFieldV1, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST subscription");
            throw x;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " -  OK  - "
                    + "Subscription has empty fields");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - FAIL - "
                    + "Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostHasEmptyFieldsV1
    
    /**
     * [ManagementInterface] -------- 'POST method post a subscription with any empty field (ngsi_version = 2)'.
     * @throws java.lang.Exception
     */
    // @Test
    public void testPostMethodPostHasEmptyFieldsV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - POST method post a "
                + "subscription with empty fields (ngsi_version = 2)");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            SubscriptionsHandlers.post(mockRequestEmptyFieldV2, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST subscription");
            throw x;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " -  OK  - "
                    + "Subscription has empty fields");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - FAIL - "
                    + "Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostHasEmptyFieldsV2
    
    /**
     * [ManagementInterface] -------- 'POST method post a subscription with any missing field (ngsi_version = 1)'.
     * @throws java.lang.Exception
     */
    // @Test
    public void testPostMethodPostHasMissingFieldsV1() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - POST method post a "
                + "subscription with missing fields (ngsi_version = 1)");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            SubscriptionsHandlers.post(mockRequestEmptyFieldV1, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST subscription");
            throw x;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " -  OK  - "
                    + "Subscription has missing fields");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - FAIL - "
                    + "Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostHasMissingFieldsV1
    
    /**
     * [ManagementInterface] -------- 'POST method post a subscription with any missing field (ngsi_version = 2)'.
     * @throws java.lang.Exception
     */
    // @Test
    public void testPostMethodPostHasMissingFieldsV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - POST method post a "
                + "subscription with missing fields (ngsi_version = 2)");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            SubscriptionsHandlers.post(mockRequestMissingFieldV2, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST subscription");
            throw x;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " -  OK  - "
                    + "Subscription has missing fields");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostSubscription]") + " - FAIL - "
                    + "Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostHasMissingFieldsV2
    
    /**
     * [ManagementInterface] -------- 'DELETE method delete a subscription (ngsi_version = 1)'.
     * @throws java.lang.Exception
     */
    // @Test
    public void testDeleteMethodsDeletesASubscriptionV1() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleDeleteSubscription]") + " - DELETE method "
                + "deletes a subscription (ngsi_version = 1)");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            SubscriptionsHandlers.delete(mockDeleteSubscriptionV1, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the DELETE subscription");
            throw x;
        } // try catch
                
        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handleDeleteSubscription]") + " -  OK  - "
                    + "Subscription deleted");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleDeleteSubscription]") + " - FAIL - "
                    + "Subscription exists yet");
            throw e;
        } // try catch
        
    } // testDeleteMethodsDeletesASubscriptionV1
    
    /**
     * [ManagementInterface] -------- 'DELETE method delete a subscription (ngsi_version = 1)'.
     * @throws java.lang.Exception
     */
    // @Test
    public void testDeleteMethodsDeletesASubscriptionV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleDeleteSubscription]") + " - DELETE method "
                + "deletes a subscription (ngsi_version = 2)");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            SubscriptionsHandlers.delete(mockDeleteSubscriptionV2, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the DELETE subscription");
            throw x;
        } // try catch
                
        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handleDeleteSubscription]") + " -  OK  - "
                    + "Subscription deleted");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleDeleteSubscription]") + " - FAIL - "
                    + "Subscription exists yet");
            throw e;
        } // try catch
        
    } // testDeleteMethodsDeletesASubscriptionV2
    
    /**
     * [ManagementInterface] -------- 'DELETE method deletes a single parameter of a given agent configuration file'.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteMethodDeletesOneInstanceConfigurationParameter() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleDeleteOneInstanceConfParam]") + " - DELETE "
                + "method deletes one instance configuration parameter");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);

        try {
            ConfigurationInstanceHandlers.delete(mockDeleteOneInstanceParameter, responseWrapper, false);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the DELETE request");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handleDeleteOneInstanceConfParam]") + " -  "
                    + "OK  - Instance configuration parameter deleted");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleDeleteOneInstanceConfParam]") + " - "
                    + "FAIL - There are some problems with your request");
            throw e;
        } // try catch
         
        folder.delete();
    } // testDeleteMethodDeletesOneInstanceConfigurationParameter
    
    /**
     * @throws java.lang.Exception
     * [ManagementInterface] -------- 'GET method gets a subscription (ngsi_version = 2)'.
     */
    // @Test
    public void testGetMethodsGetsASubscriptionV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleGetSubscriptions]") + " - GET method gets a "
                + "subscription (ngsi_version = 2)");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            SubscriptionsHandlers.get(mockGetSubscriptionV2, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the GET subscription");
            throw x;
        } // try catch
                
        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetSubscriptions]") + " -  OK  - "
                    + "Subscription obtained");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetSubscriptions]") + " - FAIL - There are "
                    + "some problems with your request");
            throw e;
        } // try catch
    } // testGetMethodsGetsASubscriptionV2
    
    /**
     * [ManagementInterface] -------- 'GET method gets all subscriptions (ngsi_version = 2)'.
     * @throws java.lang.Exception
     */
    // @Test
    public void testGetMethodsGetsAllSubscriptionsV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleGetSubscriptions]") + " - GET method gets "
                + "all subscriptions (ngsi_version = 2)");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);

        try {
            SubscriptionsHandlers.post(mockGetAllSubscriptionsV2, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the GET subscription");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetSubscriptions]") + " -  OK  - "
                    + "Subscription obtained");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetSubscriptions]") + " - FAIL - "
                    + "There are some problems with your request");
            throw e;
        } // try catch
    } // testGetMethodsGetsAllSubscriptionsV2
     
    /**
     * [ManagementInterface] -------- 'GET method gets parameters of a given agent configuration file'.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMethodGetsAllAgentConfigurationParameters() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleGetAgentConfParams]") + " - GET "
                + "method gets all agent configuration parameters");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            ConfigurationAgentHandlers.get(mockGetAllAgentParameters, responseWrapper, false);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the GET request");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetAgentConfParams]") + " -  "
                    + "OK  - All agent configuration parameters obtained");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetAgentConfParams]") + " - "
                    + "FAIL - There are some problems with your request");
            throw e;
        } // try catch
    } // testGetMethodGetsAllAgentConfigurationParameters
    
    /**
     * [ManagementInterface] -------- 'GET method gets a single parameter of a given agent configuration file'.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMethodGetsOneAgentConfigurationParameter() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleGetOneAgentConfParam]") + " - GET "
                + "method gets one agent configuration parameter");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            ConfigurationAgentHandlers.get(mockGetAllAgentParameters, responseWrapper, false);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the GET request");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetOneAgentConfParam]") + " -  "
                    + "OK  - Agent configuration parameter obtained");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetOneAgentConfParam]") + " - "
                    + "FAIL - There are some problems with your request");
            throw e;
        } // try catch
    } // testGetMethodGetsOneAgentConfigurationParameter
    
    /**
     * [ManagementInterface] -------- 'GET method gets parameters of a given agent configuration file'.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMethodGetsAllInstanceConfigurationParameters() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleGetInstanceConfParams]") + " - GET "
                + "method gets all instance configuration parameters");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            ConfigurationInstanceHandlers.get(mockGetAllInstanceParameters, responseWrapper, false);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the GET request");
            throw x;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetInstanceConfParams]") + " -  "
                    + "OK  - All instance configuration parameters obtained");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetInstanceConfParams]") + " - "
                    + "FAIL - There are some problems with your request");
            throw e;
        } // try catch
        
        folder.delete();
    } // testGetMethodGetsAllInstanceConfigurationParameters
    
    /**
     * [ManagementInterface] -------- 'GET method gets a single parameter of a given agent configuration file'.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMethodGetsOneInstanceConfigurationParameter() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleGetOneInstanceConfParam]") + " - GET "
                + "method gets one instance configuration parameter");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            ConfigurationInstanceHandlers.get(mockGetOneInstanceParameter, responseWrapper, false);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the GET request");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetOneInstanceConfParam]") + " -  "
                    + "OK  - Instance configuration parameter obtained");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetOneInstanceConfParam]") + " - "
                    + "FAIL - There are some problems with your request");
            throw e;
        } // try catch

        folder.delete();
    } // testGetMethodGetsOneInstanceConfigurationParameter
    
    /**
     * [ManagementInterface] -------- 'GET method gets cygnus logging level'.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMethodGetCygnusLoggingLevel() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleGetAdminLog]") + " - GET "
                + "method gets Cygnus logging level");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);

        
        try {
            LogHandlers.getLogLevel(mockGetLoggingLevel, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the GET request");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetAdminLog]") + " -  "
                    + "OK  - Cygnus logging level obtained");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleGetAdminLog]") + " - "
                    + "FAIL - There are some problems with your request");
            throw e;
        } // try catch
    } // testGetMethodGetCygnusLoggingLevel
    
    /**
     * [ManagementInterface] -------- 'POST method posts a single parameter in a given agent configuration file'.
     * @throws java.lang.Exception
     */
    @Test
    public void testPostMethodPostOneAgentConfigurationParameter() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePostOneAgentConfParam]") + " - POST "
                + "method post a single parameter in an agent configuration file");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            ConfigurationAgentHandlers.post(mockPostOneAgentParameter, responseWrapper, false);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST request");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostOneAgentConfParam]") + " -  "
                    + "OK  - Agent configuration parameter posted");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostOneAgentConfParam]") + " - "
                    + "FAIL - There are some problems with your request");
            throw e;
        } // try catch
    } // testPostMethodPostOneAgentConfigurationParameter
    
    /**
     * [ManagementInterface] -------- 'GET method gets a single parameter of a given agent configuration file'.
     * @throws java.lang.Exception
     */
    @Test
    public void testPostMethodPostsOneInstanceConfigurationParameter() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePostOneInstanceConfParam]") + " - POST "
                + "method posts one instance configuration parameter");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            ConfigurationInstanceHandlers.put(mockPutOneInstanceParameter, responseWrapper, false);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST request");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostOneInstanceConfParam]") + " -  "
                    + "OK  - Instance configuration parameter posted");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePostOneInstanceConfParam]") + " - "
                    + "FAIL - There are some problems with your request");
            throw e;
        } // try catch

        folder.delete();
    } // testPutMethodPutsOneInstanceConfigurationParameter
    
    /**
     * [ManagementInterface] -------- 'PUT method puts a single parameter in a given agent configuration file'.
     * @throws java.lang.Exception
     */
    @Test
    public void testPutMethodPutOneAgentConfigurationParameter() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePutOneAgentConfParam]") + " - PUT "
                + "method puts a single parameter in an agent configuration file");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            ConfigurationAgentHandlers.put(mockPutOneAgentParameter, responseWrapper, false);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the PUT request");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePutOneAgentConfParam]") + " -  "
                    + "OK  - Agent configuration parameter put");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePutOneAgentConfParam]") + " - "
                    + "FAIL - There are some problems with your request");
            throw e;
        } // try catch
    } // testPutMethodPutOneAgentConfigurationParameter
    
    /**
     * [ManagementInterface] -------- 'GET method gets a single parameter of a given agent configuration file'.
     * @throws java.lang.Exception
     */
    @Test
    public void testPutMethodPutsOneInstanceConfigurationParameter() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePutOneInstanceConfParam]") + " - PUT "
                + "method puts one instance configuration parameter");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            ConfigurationInstanceHandlers.put(mockPutOneInstanceParameter, responseWrapper, false);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the PUT request");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePutOneInstanceConfParam]") + " -  "
                    + "OK  - Instance configuration parameter put");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePutOneInstanceConfParam]") + " - "
                    + "FAIL - There are some problems with your request");
            throw e;
        } // try catch

        folder.delete();
    } // testPutMethodPutsOneInstanceConfigurationParameter
    
    /**
     * [ManagementInterface] -------- 'PUT method puts a valid Cygnus logging level'.
     * @throws java.lang.Exception
     */
    @Test
    public void testPutMethodPutCygnusLoggingLevel() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePutAdminLog]") + " - PUT "
                + "method puts a valid logging level in a running Cygnus");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            LogHandlers.putLogLevel(mockPutLoggingLevel, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the PUT request");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePutAdminLog]") + " -  "
                    + "OK  - Cygnus logging level put");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePutAdminLog]") + " - "
                    + "FAIL - There are some problems with your request");
            throw e;
        } // try catch
    } // testPutMethodPutOneAgentConfigurationParameter
    
    /**
     * [ManagementInterface] -------- 'PUT method puts an invalid Cygnus logging level'.
     * @throws java.lang.Exception
     */
    @Test
    public void testPutMethodPutInvalidCygnusLoggingLevel() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handlePutAdminLog]") + " - PUT "
                + "method puts an invalid logging level in a running Cygnus");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            LogHandlers.putLogLevel(mockPutInvalidLoggingLevel, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the PUT request");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handlePutAdminLog]") + " -  "
                    + "OK  - Invalid Cygnus logging level detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handlePutAdminLog]") + " - "
                    + "FAIL - There are some problems with your request");
            throw e;
        } // try catch
    } // testPutMethodPutOneAgentConfigurationParameter
    
    /**
     * [ManagementInterface] -------- 'DELETE method deletes a single parameter in a given agent configuration file'.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteMethodDeleteOneAgentConfigurationParameter() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleDeleteOneAgentConfParam]") + " - DELETE "
                + "method deletes a single parameter in an agent configuration file");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            ConfigurationAgentHandlers.delete(mockDeleteOneAgentParameter, responseWrapper, false);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the DELETE request");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handleDeleteOneAgentConfParam]") + " -  "
                    + "OK  - Agent configuration parameter deleted");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleDeleteOneAgentConfParam]") + " - "
                    + "FAIL - There are some problems with your request");
            throw e;
        } // try catch
    } // testDeleteMethodDeleteOneAgentConfigurationParameter
    
    /**
     * [ManagementInterface] -------- 'Agent configuration file name starts with "agent_"'.
     * @throws java.lang.Exception
     */
    @Test
    public void testAgentBadConfigurationFileName() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleAgentConfFileName]")
                  + " - Agent configuration doesn't start with 'agent_'");
        HttpServletResponseImpl responseWrapper = new HttpServletResponseImpl(response);
        
        try {
            ConfigurationAgentHandlers.delete(mockRequestBadFileName, responseWrapper, false);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the request");
            throw x;
        } // try catch

        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface.handleAgentConfFileName]") + " -  "
                    + "OK  - An agent configuration file not starting with 'agent_' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleAgentConfFileName]") + " - "
                    + "FAIL - An agent configuration file not starting with 'agent_' has not been detected");
            throw e;
        } // try catch
    } // testAgentBadConfigurationFileName
    
    /**
     * [ManagementInterface] -------- 'Get method if API is invalid"'.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetInvalidAPI() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]"));
        managementInterface = new ManagementInterface(null, new File(""), null, null, null, 8083, 8085);
        when(mockInvalidAPI.getMethod()).thenReturn("GET");
        when(mockInvalidAPI.getRequestURI()).thenReturn("/vkk");
        when(mockInvalidAPI.getLocalPort()).thenReturn(8083);
        try {
            managementInterface.handle(null, mockInvalidAPI, response, 1);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the request");
            throw x;
        }
        try {
            verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
            System.out.println(
                    getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " -  " + "OK  - API not found");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " - "
                    + "FAIL - Problem while handling the request");
            throw e;
        }
    }
    /**
     * [ManagementInterface] -------- 'Post method if API is invalid"'.
     * @throws java.lang.Exception
     */
    @Test
    public void testPostInvalidAPI() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]"));
        ManagementInterface managementInterface = new ManagementInterface(null, new File(""), null, null, null, 8083,
                8085);
        when(mockInvalidAPI.getMethod()).thenReturn("POST");
        when(mockInvalidAPI.getRequestURI()).thenReturn("/vkk");
        when(mockInvalidAPI.getLocalPort()).thenReturn(8083);
        try {
            managementInterface.handle(null, mockInvalidAPI, response, 1);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the request");
            throw x;
        } // try catch
        try {
            verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
            System.out.println(
                    getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " -  " + "OK  - API not found");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " - "
                    + "FAIL - Problem while handling the request");
            throw e;
        } // try catch
    }
    /**
     * [ManagementInterface] -------- 'Put method if API is invalid"'.
     * @throws java.lang.Exception
     */
    @Test
    public void testPutInvalidAPI() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]"));
        ManagementInterface managementInterface = new ManagementInterface(null, new File(""), null, null, null, 8083,
                8085);
        when(mockInvalidAPI.getMethod()).thenReturn("PUT");
        when(mockInvalidAPI.getRequestURI()).thenReturn("/vkk");
        when(mockInvalidAPI.getLocalPort()).thenReturn(8083);
        try {
            managementInterface.handle(null, mockInvalidAPI, response, 1);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the request");
            throw x;
        } // try catch
        try {
            verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
            System.out.println(
                    getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " -  " + "OK  - API not found");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " - "
                    + "FAIL - Problem while handling the request");
            throw e;
        } // try catch
    }
    
    /**
     * [ManagementInterface] -------- 'Delete method if API is invalid"'.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteInvalidAPI() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]"));
        ManagementInterface managementInterface = new ManagementInterface(null, new File(""), null, null, null, 8083,
                8085);
        when(mockInvalidAPI.getMethod()).thenReturn("DELETE");
        when(mockInvalidAPI.getRequestURI()).thenReturn("/vkk");
        when(mockInvalidAPI.getLocalPort()).thenReturn(8083);
        try {
            managementInterface.handle(null, mockInvalidAPI, response, 1);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the request");
            throw x;
        } // try catch
        try {
            verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
            System.out.println(
                    getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " -  " + "OK  - API not found");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " - "
                    + "FAIL - Problem while handling the request");
            throw e;
        } // try catch
    }

    /**
     * [ManagementInterface] -------- 'Default if port is API where API is invalid"'.
     * @throws java.lang.Exception
     */
    @Test
    public void testDefaultInvalidAPI() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]"));
        ManagementInterface managementInterface = new ManagementInterface(null, new File(""), null, null, null, 8083,
                8085);
        when(mockInvalidAPI.getMethod()).thenReturn("");
        when(mockInvalidAPI.getRequestURI()).thenReturn("/vkk");
        when(mockInvalidAPI.getLocalPort()).thenReturn(8083);
        try {
            managementInterface.handle(null, mockInvalidAPI, response, 1);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the request");
            throw x;
        } // try catch
        try {
            verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
            System.out.println(
                    getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " -  " + "OK  - API not found");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " - "
                    + "FAIL - Problem while handling the request");
            throw e;
        } // try catch
    }
    
    /**
     * [ManagementInterface] -------- 'Get method if port is GUI where API is invalid"'.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetInvalidAPIGuiPort() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]"));
        ManagementInterface managementInterface = new ManagementInterface(null, new File(""), null, null, null, 8083,
                8085);
        when(mockInvalidAPI.getMethod()).thenReturn("GET");
        when(mockInvalidAPI.getRequestURI()).thenReturn("/vkk");
        when(mockInvalidAPI.getLocalPort()).thenReturn(8085);
        try {
            managementInterface.handle(null, mockInvalidAPI, response, 1);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the request");
            throw x;
        } // try catch
        try {
            verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
            System.out.println(
                    getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " -  " + "OK  - API not found");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " - "
                    + "FAIL - Problem while handling the request");
            throw e;
        } // try catch
    }
    
    /**
     * [ManagementInterface] -------- 'Default case if port is GUI where API is invalid"'.
     * @throws java.lang.Exception
     */
    @Test
    public void testDefaultInvalidAPIGuiPort() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]"));
        ManagementInterface managementInterface = new ManagementInterface(null, new File(""), null, null, null, 8083,
                8085);
        when(mockInvalidAPI.getMethod()).thenReturn("");
        when(mockInvalidAPI.getRequestURI()).thenReturn("/vkk");
        when(mockInvalidAPI.getLocalPort()).thenReturn(8085);
        try {
            managementInterface.handle(null, mockInvalidAPI, response, 1);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the request");
            throw x;
        } // try catch
        try {
            verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
            System.out.println(
                    getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " -  " + "OK  - API not found");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface.handleInvalidAPI]") + " - "
                    + "FAIL - Problem while handling the request");
            throw e;
        } // try catch
    }
} // ManagementInterfaceTest
