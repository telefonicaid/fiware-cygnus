/**
 * Copyright 2020 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.containers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.telefonica.iot.cygnus.containers.NotifyContextRequestLD.ContextElement;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import org.junit.Test;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import static org.junit.Assert.assertEquals;
import org.json.JSONObject;

/**
 *
 * @author anmunoz
 */
public class NotifyContextRequestLDTest {

    private final String contextElement = "{\n" +
            "    \"id\": \"urn:ngsi-ld:Vehicle:V123\",\n" +
            "    \"type\": \"Vehicle\",\n" +
            "    \"speed\": {\n" +
            "      \"type\": \"Property\",\n" +
            "      \"value\": 23,\n" +
            "      \"accuracy\": {\n" +
            "        \"type\": \"Property\",\n" +
            "        \"value\": 0.7\n" +
            "      },\n" +
            "      \"providedBy\": {\n" +
            "        \"type\": \"Relationship\",\n" +
            "        \"object\": \"urn:ngsi-ld:Person:Bob\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"closeTo\": {\n" +
            "      \"type\": \"Relationship\",\n" +
            "      \"object\": \"urn:ngsi-ld:Building:B1234\"\n" +
            "    },\n" +
            "    \"location\": {\n" +
            "        \"type\": \"GeoProperty\",\n" +
            "        \"value\": {\n" +
            "          \"type\":\"Point\",\n" +
            "          \"coordinates\": [-8,44]\n" +
            "        }\n" +
            "    },\n" +
            "    \"@context\": [\n" +
            "        \"https://example.org/ld/vehicle.jsonld\",\n" +
            "        \"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"\n" +
            "    ]\n" +
            "  }";

    private final String notification = ""
            + "{"
            +   "\"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\","
            +   "\"data\" : ["
            +          contextElement
            +   "]"
            + "}";

    
    /**
     * [NotifyContextRequest.getSubscriptionId] -------- Subscription ID can be retrieved.
     */
    @Test
    public void testNotifyContextRequestGetSubscriptionId() {
        System.out.println(getTestTraceHead("[NotifyContextRequest.getSubscriptionId]")
                + "-------- Subscription ID can be retrieved");
        NotifyContextRequestLD ncr;
        
        try {
            ncr = NGSIUtilsForTests.createJsonNotifyContextRequestLD(notification);
            System.out.println(ncr);
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
     * [NotifyContextRequest.toString] -------- String representation of this object is OK.
     */
    @Test
    public void testNotifyContextRequestToString() {
        System.out.println(getTestTraceHead("[NotifyContextRequest.toString]")
                + "-------- String representation of this object is OK");
        NotifyContextRequestLD ncr;
        
        try {
            ncr = NGSIUtilsForTests.createJsonNotifyContextRequestLD(notification);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.toString]")
                    + "- FAIL - There was a problem when creating the NotifyContextRequest");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        try {
            NGSIUtilsForTests.createJsonNotifyContextRequestLD(ncr.toString());
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
            ce = NGSIUtilsForTests.createJsonContextElementLD(contextElement);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getId]")
                    + "- FAIL - There was a problem when creating the ContextElement");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        try {
            assertEquals("urn:ngsi-ld:Vehicle:V123", ce.getId());
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
            ce = NGSIUtilsForTests.createJsonContextElementLD(contextElement);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getType]")
                    + "- FAIL - There was a problem when creating the ContextElement");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        try {
            assertEquals("Vehicle", ce.getType());
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getType]")
                    + "-  OK  - The retrieved entity type matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.getType]")
                    + "- FAIL - The retrieved entity type does not match the expected one");
            throw e;
        } // try catch
    } // testContextElementGetType
    
    /**
     * [NotifyContextRequest.ContextElement.toString] -------- String representation of this object is OK.
     */
    @Test
    public void testContextElementToString() {
        System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.toString]")
                + "-------- String representation of this object is OK");
        ContextElement ce;
        
        try {
            ce = NGSIUtilsForTests.createJsonContextElementLD(contextElement);
            System.out.println(ce.toString());
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NotifyContextRequest.ContextElement.toString]")
                    + "- FAIL - There was a problem when creating the ContextElement");
            throw new AssertionError(e.getMessage());
        } // try catch
    } // testContextElementToString
    


} // NotifyContextRequestTest
