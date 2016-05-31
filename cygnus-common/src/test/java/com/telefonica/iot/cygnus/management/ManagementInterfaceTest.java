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
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

/**
 *
 * @author frb
 */

@RunWith(MockitoJUnitRunner.class)
public class ManagementInterfaceTest {
    
    public ManagementInterfaceTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }
    
    public class StatusExposingServletResponse extends HttpServletResponseWrapper {

        private int httpStatus;

        public StatusExposingServletResponse(HttpServletResponse response) {
            super(response);
        } // StatusExposingServletResponse

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

    } // StatusExposingServletResponse
    
    // mocks
    @Mock
    protected HttpServletRequest mockRequest;
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
    
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        
        // Define subscriptions for each case
        String subscriptionV1 = "{\"subscription\":{\"entities\": [{\"type\": \"Trainer\",\"isPattern\": \"false\",\"id\": \"Trainer1\"}],\"attributes\": [],\"reference\": \"http://localhost:5050/notify\",\"duration\": \"P1M\",\"notifyConditions\": [{\"type\": \"ONCHANGE\",\"condValues\": []}],\"throttling\": \"PT5S\"}, \"endpoint\":{\"host\":\"orion.lab.fi-ware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String subscriptionV2 = "{\"subscription\":{\"description\": \"One subscription to rule them all\",\"subject\": {\"entities\": [{\"idPattern\": \".*\",\"type\": \"Room\"}],\"condition\": {\"attrs\": [\"temperature\"],\"expression\": {\"q\": \"temperature>40\"}}},\"notification\": {\"http\": {\"url\": \"http://localhost:1234\"},\"attrs\": [\"temperature\",\"humidity\"]},\"expires\": \"2016-05-05T14:00:00.00Z\",\"throttling\": 5}, \"endpoint\":{\"host\":\"orion.lab.fiware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String missingSubscription = "{\"endpoint\":{\"host\":\"orion.lab.fi-ware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String subsEmptyfieldV1 = "{\"subscription\":{\"entities\": [{\"type\": \"Trainer\",\"isPattern\": \"false\",\"id\": \"Trainer1\"}],\"attributes\": [],\"reference\": \"http://localhost:5050/notify\",\"duration\": \"P1M\",\"notifyConditions\": [{\"type\": \"\",\"condValues\": []}],\"throttling\": \"PT5S\"}, \"endpoint\":{\"host\":\"orion.lab.fi-ware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String subsMissingfieldV1 = "{\"subscription\":{\"entities\": [{\"type\": \"Trainer\",\"isPattern\": \"false\",\"id\": \"Trainer1\"}],\"attributes\": [],\"reference\": \"http://localhost:5050/notify\",\"notifyConditions\": [{\"type\": \"ONCHANGE\",\"condValues\": []}],\"throttling\": \"PT5S\"}, \"endpoint\":{\"host\":\"orion.lab.fi-ware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String subsEmptyfieldV2 = "{\"subscription\":{\"description\": \"One subscription to rule them all\",\"subject\": {\"entities\": [{\"idPattern\": \".*\",\"type\": \"Room\"}],\"condition\": {\"attrs\": [\"temperature\"],\"expression\": {\"q\": \"temperature>40\"}}},\"notification\": {\"http\": {\"url\": \"http://localhost:1234\"},\"attrs\": [\"temperature\",\"humidity\"]},\"expires\": \"\",\"throttling\": 5}, \"endpoint\":{\"host\":\"orion.lab.fiware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String subsV2MissingfieldV2 = "{\"subscription\":{\"description\": \"One subscription to rule them all\",\"subject\": {\"entities\": [{\"idPattern\": \".*\",\"type\": \"Room\"}],\"condition\": {\"attrs\": [\"temperature\"],\"expression\": {\"q\": \"temperature>40\"}}},\"notification\": {\"http\": {\"url\": \"http://localhost:1234\"},\"attrs\": [\"temperature\",\"humidity\"]},\"throttling\": 5}, \"endpoint\":{\"host\":\"orion.lab.fiware.org\", \"port\":\"1026\", \"ssl\":\"false\", \"xauthtoken\":\"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}}";
        String subscriptionDelete = "{\"host\":\"orion.lab.fi-ware.org\", \"port\": \"1026\", \"ssl\": \"false\", \"xauthtoken\": \"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}";
        String subscriptionGet = "{\"host\":\"orion.lab.fi-ware.org\", \"port\": \"1026\", \"ssl\": \"false\", \"xauthtoken\": \"QsENv67AJj7blC2qJ0YvfSc5hMWYrs\"}";        
        String token = "QsENv67AJj7blC2qJ0YvfSc5hMWYrs";
        
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
        
        // set up the behaviour of the mocked classes
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
        
        when(orionBackend.deleteSubscriptionV1(subscriptionDelete, token)).thenReturn(responseDeleteV1);
        when(orionBackend.deleteSubscriptionV2(subscriptionDelete, token)).thenReturn(responseDeleteV2);
        
} // setUp
    
    /**
     * Test of handle method, of class ManagementInterface.
     */
    @Test
    public void testHandle() {
        System.out.println(getTestTraceHead("[ManagementInterface]") + "- Testing ManagementInterface.handle");
        ManagementInterface managementInterface = new ManagementInterface(new File(""), null, null, null, 8081, 8082);
        
        try {
            managementInterface.handle(null, mockRequest, mockResponse, 1);
        } catch (IOException e) {
            fail(e.getMessage());
        } catch (ServletException e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch
        
    } // testHandle
    
    /**
     * [ManagementInterface] -------- 'POST method posts a valid subscription (ngsi_version = 1)'.
     * @throws java.lang.Exception
     */
    @Test
    public void testPostMethodPostAValidSubscriptionV1() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface]") + "- 'POST method posts a valid subscription (ngsi_version = 1)'.");
        StatusExposingServletResponse responseWrapper = new StatusExposingServletResponse(response);
        ManagementInterface managementInterface = new ManagementInterface(new File(""), null, null, null, 8081, 8082);
        managementInterface.setOrionBackend(orionBackend);
             
        try {
            managementInterface.handlePostSubscription(mockRequestV1, responseWrapper);
        } catch (Exception e) {
            System.out.println("There was some problem when handling the POST subscription");
            throw e;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface]") + "-  OK  - Valid subscription");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface]") + " - FAIL - Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostAValidSubscriptionV1
    
    /**
     * [ManagementInterface] -------- 'POST method posts a valid subscription (ngsi_version = 2)'.
     * @throws java.lang.Exception
     */
    @Test
    public void testPostMethodPostAValidSubscriptionV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface]") + "- 'POST method posts a valid subscription (ngsi_version = 2)'.");
        StatusExposingServletResponse responseWrapper = new StatusExposingServletResponse(response);
        ManagementInterface managementInterface = new ManagementInterface(new File(""), null, null, null, 8081, 8082);
        managementInterface.setOrionBackend(orionBackend);
        
        try {
            managementInterface.handlePostSubscription(mockRequestV2, responseWrapper);
        } catch (Exception e) {
            System.out.println("There was some problem when handling the POST subscription");
            throw e;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface]") + "-  OK  - Valid subscription");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface]") + " - FAIL - Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostAValidSubscriptionV2
    
    /**
     * [ManagementInterface] -------- 'POST method doesn't find a subscription (ngsi_version = 1)'.
     */
    @Test
    public void testPostMethodPostHasNotSubscriptionV1() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface]") + "- 'POST method doesn't find a subscription (ngsi_version = 1)'.");
        StatusExposingServletResponse responseWrapper = new StatusExposingServletResponse(response);
        ManagementInterface managementInterface = new ManagementInterface(new File(""), null, null, null, 8081, 8082);
        managementInterface.setOrionBackend(orionBackend);        
        
        try {
            managementInterface.handlePostSubscription(mockRequestNoSubscriptionV1, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST subscription");
            throw x;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface]") + "-  OK  - Subscription not found");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface]") + " - FAIL - Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostHasNotSubscriptionV1
    
    /**
     * [ManagementInterface] -------- 'POST method doesn't find a subscription (ngsi_version = 2)'.
     */
    @Test
    public void testPostMethodPostHasNotSubscriptionV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface]") + "- 'POST method doesn't find a subscription (ngsi_version = 2)'.");
        StatusExposingServletResponse responseWrapper = new StatusExposingServletResponse(response);
        ManagementInterface managementInterface = new ManagementInterface(new File(""), null, null, null, 8081, 8082);
        managementInterface.setOrionBackend(orionBackend);        
        
        try {
            managementInterface.handlePostSubscription(mockRequestNoSubscriptionV2, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST subscription");
            throw x;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface]") + "-  OK  - Subscription not found");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface]") + " - FAIL - Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostHasNotSubscriptionV2
    
    /**
     * [ManagementInterface] -------- 'POST method post a subscription with any empty field (ngsi_version = 1)'.
     */
    @Test
    public void testPostMethodPostHasEmptyFieldsV1() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface]") + "- 'POST method post a subscription with empty fields (ngsi_version = 1)'.");
        StatusExposingServletResponse responseWrapper = new StatusExposingServletResponse(response);
        ManagementInterface managementInterface = new ManagementInterface(new File(""), null, null, null, 8081, 8082);
        managementInterface.setOrionBackend(orionBackend);        
        
        try {
            managementInterface.handlePostSubscription(mockRequestEmptyFieldV1, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST subscription");
            throw x;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface]") + "-  OK  - Subscription has empty fields");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface]") + " - FAIL - Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostHasEmptyFieldsV1
    
    /**
     * [ManagementInterface] -------- 'POST method post a subscription with any empty field (ngsi_version = 2)'.
     */
    @Test
    public void testPostMethodPostHasEmptyFieldsV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface]") + "- 'POST method post a subscription with empty fields (ngsi_version = 2)'.");
        StatusExposingServletResponse responseWrapper = new StatusExposingServletResponse(response);
        ManagementInterface managementInterface = new ManagementInterface(new File(""), null, null, null, 8081, 8082);
        managementInterface.setOrionBackend(orionBackend);        
        
        try {
            managementInterface.handlePostSubscription(mockRequestEmptyFieldV2, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST subscription");
            throw x;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface]") + "-  OK  - Subscription has empty fields");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface]") + " - FAIL - Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostHasEmptyFieldsV2
    
    /**
     * [ManagementInterface] -------- 'POST method post a subscription with any missing field (ngsi_version = 1)'.
     */
    @Test
    public void testPostMethodPostHasMissingFieldsV1() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface]") + "- 'POST method post a subscription with missing fields (ngsi_version = 1)'.");
        StatusExposingServletResponse responseWrapper = new StatusExposingServletResponse(response);
        ManagementInterface managementInterface = new ManagementInterface(new File(""), null, null, null, 8081, 8082);
        managementInterface.setOrionBackend(orionBackend);        
        
        try {
            managementInterface.handlePostSubscription(mockRequestEmptyFieldV1, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST subscription");
            throw x;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface]") + "-  OK  - Subscription has missing fields");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface]") + " - FAIL - Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostHasMissingFieldsV1
    
    /**
     * [ManagementInterface] -------- 'POST method post a subscription with any missing field (ngsi_version = 2)'.
     */
    @Test
    public void testPostMethodPostHasMissingFieldsV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface]") + "- 'POST method post a subscription with missing fields (ngsi_version = 2)'.");
        StatusExposingServletResponse responseWrapper = new StatusExposingServletResponse(response);
        ManagementInterface managementInterface = new ManagementInterface(new File(""), null, null, null, 8081, 8082);
        managementInterface.setOrionBackend(orionBackend);        
        
        try {
            managementInterface.handlePostSubscription(mockRequestMissingFieldV2, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the POST subscription");
            throw x;
        } // try catch
        
        try {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface]") + "-  OK  - Subscription has missing fields");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface]") + " - FAIL - Invalid subscription");
            throw e;
        } // try catch
        
    } // testPostMethodPostHasMissingFieldsV2
    
    /**
     * [ManagementInterface] -------- 'DELETE method delete a subscription (ngsi_version = 1)'.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteMethodsDeletesASubscriptionV1() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface]") + "- 'DELETE method deletes a subscription (ngsi_version = 1)'.");
        StatusExposingServletResponse responseWrapper = new StatusExposingServletResponse(response);
        ManagementInterface managementInterface = new ManagementInterface(new File(""), null, null, null, 8081, 8082);
        managementInterface.setOrionBackend(orionBackend);        
        
        try {
            managementInterface.handleDeleteSubscription(mockDeleteSubscriptionV1, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the DELETE subscription");
            throw x;
        } // try catch
                
        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface]") + "-  OK  - Subscription deleted");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface]") + " - FAIL - Subscription exists yet");
            throw e;
        } // try catch
        
    } // testDeleteMethodsDeletesASubscriptionV1
    
    /**
     * [ManagementInterface] -------- 'DELETE method delete a subscription (ngsi_version = 1)'.
     */
    @Test
    public void testDeleteMethodsDeletesASubscriptionV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface]") + "- 'DELETE method deletes a subscription (ngsi_version = 2)'.");
        StatusExposingServletResponse responseWrapper = new StatusExposingServletResponse(response);
        ManagementInterface managementInterface = new ManagementInterface(new File(""), null, null, null, 8081, 8082);
        managementInterface.setOrionBackend(orionBackend);        
        
        try {
            managementInterface.handleDeleteSubscription(mockDeleteSubscriptionV2, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the DELETE subscription");
            throw x;
        } // try catch
                
        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface]") + "-  OK  - Subscription deleted");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface]") + " - FAIL - Subscription exists yet");
            throw e;
        } // try catch
        
    } // testDeleteMethodsDeletesASubscriptionV2
    
    /**
     * [ManagementInterface] -------- 'GET method gets a subscription (ngsi_version = 2)'.
     */
    @Test
    public void testGetMethodsGetsASubscriptionV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface]") + "- 'GET method gets a subscription (ngsi_version = 2)'.");
        StatusExposingServletResponse responseWrapper = new StatusExposingServletResponse(response);
        ManagementInterface managementInterface = new ManagementInterface(new File(""), null, null, null, 8081, 8082);
        managementInterface.setOrionBackend(orionBackend);        
        
        try {
            managementInterface.handleGetSubscriptions(mockGetSubscriptionV2, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the GET subscription");
            throw x;
        } // try catch
                
        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface]") + "-  OK  - Subscription got");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface]") + " - FAIL - There are some problems with your request");
            throw e;
        } // try catch
        
    } // testGetMethodsGetsASubscriptionV2
    
    /**
     * [ManagementInterface] -------- 'GET method gets all subscriptions (ngsi_version = 2)'.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetMethodsGetsAllSubscriptionsV2() throws Exception {
        System.out.println(getTestTraceHead("[ManagementInterface]") + "- 'GET method gets all subscriptions (ngsi_version = 2)'.");
        StatusExposingServletResponse responseWrapper = new StatusExposingServletResponse(response);
        ManagementInterface managementInterface = new ManagementInterface(new File(""), null, null, null, 8081, 8082);
        managementInterface.setOrionBackend(orionBackend);        
        
        try {
            managementInterface.handleGetSubscriptions(mockGetAllSubscriptionsV2, responseWrapper);
        } catch (Exception x) {
            System.out.println("There was some problem when handling the GET subscription");
            throw x;
        } // try catch
                
        try {
            assertEquals(HttpServletResponse.SC_OK, responseWrapper.getStatus());
            System.out.println(getTestTraceHead("[ManagementInterface]") + "-  OK  - Subscription got");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[ManagementInterface]") + " - FAIL - There are some problems with your request");
            throw e;
        } // try catch
        
    } // testGetMethodsGetsAllSubscriptionsV2
    
} // ManagementInterfaceTest
