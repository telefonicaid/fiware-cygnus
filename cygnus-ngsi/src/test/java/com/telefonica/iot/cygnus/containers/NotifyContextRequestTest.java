/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.containers;

import static org.junit.Assert.*; // this is required by "fail" like assertions
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.StatusCode;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NotifyContextRequestTest {

    private final String contextElement = ""
            + "{"
            +    "\"attributes\" : ["
            +       "{"
            +          "\"name\" : \"an0\","
            +          "\"type\" : \"at0\","
            +          "\"value\" : 0,"
            +          "\"metadatas\": ["
            +             "{"
            +                "\"name\": \"mn1\","
            +                "\"type\": \"mt1\","
            +                "\"value\": \"mv1\""
            +             "}"
            +          "]"
            +       "},"
            +       "{"
            +          "\"name\" : \"an1\","
            +          "\"type\" : \"at1\","
            +          "\"value\" : \"av1\","
            +          "\"metadatas\": ["
            +             "{"
            +                "\"name\": \"mn1\","
            +                "\"type\": \"mt1\","
            +                "\"value\": \"mv1\""
            +             "}"
            +          "]"
            +       "},"
            +       "{"
            +          "\"name\" : \"an2\","
            +          "\"type\" : \"at2\","
            +          "\"value\" : { \"a\": \"1\", \"b\": \"2\" },"
            +          "\"metadatas\": ["
            +             "{"
            +                "\"name\": \"mn2\","
            +                "\"type\": \"mt2\","
            +                "\"value\": \"mv2\""
            +             "}"
            +          "]"
            +       "},"
            +       "{"
            +          "\"name\" : \"an3\","
            +          "\"type\" : \"at3\","
            +          "\"value\" : [ \"v1\", \"v2\" ],"
            +          "\"metadatas\": ["
            +             "{"
            +                "\"name\": \"mn3\","
            +                "\"type\": \"mt3\","
            +                "\"value\": \"mv3\""
            +             "}"
            +          "]"
            +       "},"
            +       "{"
            +          "\"name\" : \"an4\","
            +          "\"type\" : \"at4\","
            +          "\"value\" : null,"
            +          "\"metadatas\": []"
            +       "}"
            +    "],"
            +    "\"id\" : \"ei\","
            +    "\"type\" : \"et\","
            +    "\"isPattern\" : \"false\""
            + "}";
    private final String statusCode = ""
            + "{"
            +    "\"code\" : \"200\","
            +    "\"reasonPhrase\" : \"OK\""
            + "}";
    private final String notification = ""
            + "{"
            +   "\"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\","
            +   "\"originator\" : \"localhost\","
            +   "\"contextResponses\" : ["
            +     "{"
            +       "\"contextElement\" :"
            +          contextElement + ","
            +       "\"statusCode\" :"
            +          statusCode
            +     "}"
            +   "]"
            + "}";
    
    /**
     * [NotifyContextRequest.getSubscriptionId] -------- Subscription ID can be retrieved.
     */
    @Test
    public void testNotifyContextRequestGetSubscriptionId() {
        System.out.println(getTestTraceHead("[NotifyContextRequest.getSubscriptionId]")
                + "-------- Subscription ID can be retrieved");
        NotifyContextRequest ncr;
        
        try {
            ncr = NGSIUtilsForTests.createJsonNotifyContextRequest(notification);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.getSubscriptionId]")
                    + "- FAIL - There was a problem when creating the NotifyContextRequest");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        try {
            assertEquals("51c0ac9ed714fb3b37d7d5a8", ncr.getSubscriptionId());
            System.out.println(getTestTraceHead("[NotifyContextRequest.getSubscriptionId]")
                    + "-  OK  - The retrieved subscription ID matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.getSubscriptionId]")
                    + "- FAIL - The retrieved subscription ID does not match the expected one");
            throw e;
        } // try catch
    } // testNotifyContextRequestGetSubscriptionId

    /**
     * [NotifyContextRequest.getOriginator] -------- Originator can be retrieved.
     */
    @Test
    public void testNotifyContextRequestGetOriginator() {
        System.out.println(getTestTraceHead("[NotifyContextRequest.getOriginator]")
                + "-------- Originator can be retrieved");
        NotifyContextRequest ncr;
        
        try {
            ncr = NGSIUtilsForTests.createJsonNotifyContextRequest(notification);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.getOriginator]")
                    + "- FAIL - There was a problem when creating the NotifyContextRequest");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        try {
            assertEquals("localhost", ncr.getOriginator());
            System.out.println(getTestTraceHead("[NotifyContextRequest.getOriginator]")
                    + "-  OK  - The retrieved originator matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.getOriginator]")
                    + "- FAIL - The retrieved originator does not match the expected one");
            throw e;
        } // try catch
    } // testNotifyContextRequestGetOriginator
    
    /**
     * [NotifyContextRequest.toString] -------- String representation of this object is OK.
     */
    @Test
    public void testNotifyContextRequestToString() {
        System.out.println(getTestTraceHead("[NotifyContextRequest.toString]")
                + "-------- String representation of this object is OK");
        NotifyContextRequest ncr;
        
        try {
            ncr = NGSIUtilsForTests.createJsonNotifyContextRequest(notification);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.toString]")
                    + "- FAIL - There was a problem when creating the NotifyContextRequest");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        try {
            NGSIUtilsForTests.createJsonNotifyContextRequest(ncr.toString());
            System.out.println(getTestTraceHead("[NotifyContextRequest.toString]")
                    + "-  OK  - The string representation of this object is OK");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.toString]")
                    + "- FAIL - The string representation of this object is not OK");
            throw new AssertionError(e.getMessage());
        } // try catch
    } // testNotifyContextRequestToString
    
    /**
     * [NotifyContextRequest.ContextElement.getId] -------- The entity ID can be retrieved.
     */
    @Test
    public void testContextElementGetId() {
        System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getId]")
                + "-------- The entity ID can be retrieved");
        ContextElement ce;
        
        try {
            ce = NGSIUtilsForTests.createJsonContextElement(contextElement);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getId]")
                    + "- FAIL - There was a problem when creating the ContextElement");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        try {
            assertEquals("ei", ce.getId());
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getId]")
                    + "-  OK  - The retrieved entity ID matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getId]")
                    + "- FAIL - The retrieved entity ID does not match the expected one");
            throw e;
        } // try catch
    } // testContextElementGetId
    
    /**
     * [NotifyContextRequest.ContextElement.getType] -------- The entity type can be retrieved.
     */
    @Test
    public void testContextElementGetType() {
        System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getType]")
                + "-------- The entity type can be retrieved");
        ContextElement ce;
        
        try {
            ce = NGSIUtilsForTests.createJsonContextElement(contextElement);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getType]")
                    + "- FAIL - There was a problem when creating the ContextElement");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        try {
            assertEquals("et", ce.getType());
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getType]")
                    + "-  OK  - The retrieved entity type matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getType]")
                    + "- FAIL - The retrieved entity type does not match the expected one");
            throw e;
        } // try catch
    } // testContextElementGetType
    
    /**
     * [NotifyContextRequest.ContextElement.getIsPattern] -------- The entity type can be retrieved.
     */
    @Test
    public void testContextElementGetIsPattern() {
        System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getIsPattern]")
                + "-------- The entity type can be retrieved");
        ContextElement ce;
        
        try {
            ce = NGSIUtilsForTests.createJsonContextElement(contextElement);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getIsPattern]")
                    + "- FAIL - There was a problem when creating the ContextElement");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        try {
            assertEquals("false", ce.getIsPattern());
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getIsPattern]")
                    + "-  OK  - The retrieved entity type matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getIsPattern]")
                    + "- FAIL - The retrieved entity type does not match the expected one");
            throw e;
        } // try catch
    } // testContextElementGetIsPattern
    
    /**
     * [NotifyContextRequest.ContextElement.toString] -------- String representation of this object is OK.
     */
    @Test
    public void testContextElementToString() {
        System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.toString]")
                + "-------- String representation of this object is OK");
        ContextElement ce;
        
        try {
            ce = NGSIUtilsForTests.createJsonContextElement(contextElement);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.toString]")
                    + "- FAIL - There was a problem when creating the ContextElement");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        try {
            NGSIUtilsForTests.createJsonContextElement(ce.toString());
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.toString]")
                    + "-  OK  - The string representation of this object is OK");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.toString]")
                    + "- FAIL - The string representation of this object is not OK");
            throw new AssertionError(e.getMessage());
        } // try catch
    } // testContextElementToString
    
    /**
     * [NotifyContextRequest.StatusCode.getCode] -------- The code can be retrieved.
     */
    @Test
    public void testStatusCodeGetCode() {
        System.out.println(getTestTraceHead("[NotifyContextRequest.StatusCode.getCode]")
                + "-------- The code can be retrieved");
        StatusCode sc;
        
        try {
            sc = NGSIUtilsForTests.createJsonStatusCode(statusCode);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.StatusCode.getCode]")
                    + "- FAIL - There was a problem when creating the StatusCode");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        try {
            assertEquals("200", sc.getCode());
            System.out.println(getTestTraceHead("[NotifyContextRequest.StatusCode.getCode]")
                    + "-  OK  - The retrieved code matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.StatusCode.getCode]")
                    + "- FAIL - The retrieved code does not match the expected one");
            throw e;
        } // try catch
    } // testStatusCodeGetCode
    
    /**
     * [NotifyContextRequest.StatusCode.getReasonPhrase] -------- The code can be retrieved.
     */
    @Test
    public void testStatusCodeGetReasonPhrase() {
        System.out.println(getTestTraceHead("[NotifyContextRequest.StatusCode.getReasonPhrase]")
                + "-------- The code can be retrieved");
        StatusCode sc;
        
        try {
            sc = NGSIUtilsForTests.createJsonStatusCode(statusCode);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.StatusCode.getReasonPhrase]")
                    + "- FAIL - There was a problem when creating the StatusCode");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        try {
            assertEquals("OK", sc.getReasonPhrase());
            System.out.println(getTestTraceHead("[NotifyContextRequest.StatusCode.getReasonPhrase]")
                    + "-  OK  - The retrieved reason phrase matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.StatusCode.getReasonPhrase]")
                    + "- FAIL - The retrieved reason phrase does not match the expected one");
            throw e;
        } // try catch
    } // testStatusCodeGetReasonPhrase
    
    /**
     * [NotifyContextRequest.StatusCode.toString] -------- String representation of this object is OK.
     */
    @Test
    public void testStatusCodeToString() {
        System.out.println(getTestTraceHead("[NotifyContextRequest.StatusCode.toString]")
                + "-------- String representation of this object is OK");
        StatusCode sc;
        
        try {
            sc = NGSIUtilsForTests.createJsonStatusCode(statusCode);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.StatusCode.toString]")
                    + "- FAIL - There was a problem when creating the StatusCode");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        try {
            NGSIUtilsForTests.createJsonContextElement(sc.toString());
            System.out.println(getTestTraceHead("[NotifyContextRequest.StatusCode.toString]")
                    + "-  OK  - The string representation of this object is OK");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.StatusCode.toString]")
                    + "- FAIL - The string representation of this object is not OK");
            throw new AssertionError(e.getMessage());
        } // try catch
    } // testStatusCodeToString

} // NotifyContextRequestTest
