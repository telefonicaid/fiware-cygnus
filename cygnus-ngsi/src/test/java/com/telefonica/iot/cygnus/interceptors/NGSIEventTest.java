/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.interceptors;

import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import java.util.HashMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NGSIEventTest {
    
    private final String originalCEStr = ""
            + "{"
            +   "\"attributes\" : ["
            +     "{"
            +       "\"name\" : \"temperature\","
            +       "\"type\" : \"centigrade\","
            +       "\"value\" : \"26.5\""
            +     "}"
            +   "],"
            +   "\"type\" : \"Room\","
            +   "\"isPattern\" : \"false\","
            +   "\"id\" : \"Room1\""
            + "}";
    private final String mappedCEStr = ""
            + "{"
            +   "\"attributes\" : ["
            +     "{"
            +       "\"name\" : \"temp\","
            +       "\"type\" : \"cent\","
            +       "\"value\" : \"26.5\""
            +     "}"
            +   "],"
            +   "\"type\" : \"room\","
            +   "\"isPattern\" : \"false\","
            +   "\"id\" : \"all_rooms\""
            + "}";
    private final String mappedCEEmptyTypeStr = ""
            + "{"
            +   "\"attributes\" : ["
            +     "{"
            +       "\"name\" : \"temp\","
            +       "\"type\" : \"cent\","
            +       "\"value\" : \"26.5\""
            +     "}"
            +   "],"
            +   "\"type\" : \"\","
            +   "\"isPattern\" : \"false\","
            +   "\"id\" : \"all_rooms\""
            + "}";
    private final String timestamp = "123456789";
    private final String originalService = "rooms";
    private final String mappedService = "new_rooms";
    private final String originalServicePath = "/hotel1";
    private final String mappedServicePath = "/hotels";
    private final String groupedServicePath = "/hotels";
    private final String originalEntityNoEncoding = "Room1_Room";
    private final String originalEntityEncoding = "Room1=Room";
    private final String groupedEntity = "all_rooms";
    private final String mappedEntityID = "all_rooms";
    private final String mappedEntityType = "room";
    private final String originalAttribute = "temperature";
    private final String mappedAttribute = "temp";

    /**
     * Constructor.
     */
    public NGSIEventTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSIEventTest
    
    /**
     * [NGSIEvent.getRecvTimeTs] -------- The timestamp is returned.
     */
    @Test
    public void testGetRecvTimeTs() {
        System.out.println(getTestTraceHead("[NGSIEvent.getRecvTimeTs]")
                + "-------- The timestamp is returned");
        HashMap<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_TIMESTAMP, timestamp);
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE = null; // irrelevant for this test
        ContextElement mappedCE = null; // irrelevant for this test
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(Long.parseLong(timestamp), event.getRecvTimeTs());
            System.out.println(getTestTraceHead("[NGSIEvent.getRecvTimeTs]")
                    + "-  OK  - The timestamp has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getRecvTimeTs]")
                    + "- FAIL - The timestamp has not been returned");
            throw e;
        } // try catch
    } // testGetRecvTimeTs
    
    /**
     * [NGSIEvent.getServiceForData] -------- The original service is returned.
     */
    @Test
    public void testGetServiceForData() {
        System.out.println(getTestTraceHead("[NGSIEvent.getServiceForData]")
                + "-------- The original service is returned");
        HashMap<String, String> headers = new HashMap<>();
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE = null; // irrelevant for this test
        ContextElement mappedCE = null; // irrelevant for this test
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(originalService, event.getServiceForData());
            System.out.println(getTestTraceHead("[NGSIEvent.getServiceForData]")
                    + "-  OK  - The original service has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getServiceForData]")
                    + "- FAIL - The original service has not been returned");
            throw e;
        } // try catch
    } // testGetServiceForData
    
    /**
     * [NGSIEvent.getServiceForNaming] -------- When name mappings are not enabled, the original service is returned.
     */
    @Test
    public void testGetServiceForNamingNoNM() {
        System.out.println(getTestTraceHead("[NGSIEvent.getServiceForNaming]")
                + "-------- When name mappings are not enabled, the original service is returned");
        HashMap<String, String> headers = new HashMap<>();
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE = null; // irrelevant for this test
        ContextElement mappedCE = null; // irrelevant for this test
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(originalService, event.getServiceForNaming(false));
            System.out.println(getTestTraceHead("[NGSIEvent.getServiceForNaming]")
                    + "-  OK  - The original service has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getServiceForNaming]")
                    + "- FAIL - The original service has not been returned");
            throw e;
        } // try catch
    } // testGetServiceForNamingNoNM
    
    /**
     * [NGSIEvent.getServiceForNaming] -------- When name mappings are enabled, the mapped service is returned.
     */
    @Test
    public void testGetServiceForNamingNM() {
        System.out.println(getTestTraceHead("[NGSIEvent.getServiceForNaming]")
                + "-------- When name mappings are enabled, the mapped service is returned");
        HashMap<String, String> headers = new HashMap<>();
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE, originalService);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, mappedService);
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE = null; // irrelevant for this test
        ContextElement mappedCE = null; // irrelevant for this test
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(mappedService, event.getServiceForNaming(true));
            System.out.println(getTestTraceHead("[NGSIEvent.getServiceForNaming]")
                    + "-  OK  - The original service has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getServiceForNaming]")
                    + "- FAIL - The original service has not been returned");
            throw e;
        } // try catch
    } // testGetServiceForNamingNM
    
    /**
     * [NGSIEvent.getServicePathForData] -------- The original service path is returned.
     */
    @Test
    public void testGetServicePathForData() {
        System.out.println(getTestTraceHead("[NGSIEvent.getServicePathForData]")
                + "-------- The original service path is returned");
        HashMap<String, String> headers = new HashMap<>();
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_GROUPED_SERVICE_PATH, groupedServicePath);
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE = null; // irrelevant for this test
        ContextElement mappedCE = null; // irrelevant for this test
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(originalServicePath, event.getServicePathForData());
            System.out.println(getTestTraceHead("[NGSIEvent.getServicePathForData]")
                    + "-  OK  - The original service path has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getServicePathForData]")
                    + "- FAIL - The original service path has not been returned");
            throw e;
        } // try catch
    } // testGetServicePathForData
    
    /**
     * [NGSIEvent.getServicePathForNaming] -------- When grouping and mappings are not enabled, the original service
     * path is returned.
     */
    @Test
    public void testGetServicePathForNamingNoGRNoNM() {
        System.out.println(getTestTraceHead("[NGSIEvent.getServicePathForNaming]")
                + "-------- When grouping and mappings are not enabled, the original service path is returned");
        HashMap<String, String> headers = new HashMap<>();
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_GROUPED_SERVICE_PATH, groupedServicePath);
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE = null; // irrelevant for this test
        ContextElement mappedCE = null; // irrelevant for this test
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(originalServicePath, event.getServicePathForNaming(false, false));
            System.out.println(getTestTraceHead("[NGSIEvent.getServicePathForNaming]")
                    + "-  OK  - The original service path has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getServicePathForNaming]")
                    + "- FAIL - The original service path has not been returned");
            throw e;
        } // try catch
    } // testGetServicePathForNamingNoGRNoNM
    
    /**
     * [NGSIEvent.getServicePathForNaming] -------- When grouping is enabled, the grouped service path is returned.
     */
    @Test
    public void testGetServicePathForNamingGR() {
        System.out.println(getTestTraceHead("[NGSIEvent.getServicePathForNaming]")
                + "-------- When grouping is enabled, the grouped service path is returned");
        HashMap<String, String> headers = new HashMap<>();
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_GROUPED_SERVICE_PATH, groupedServicePath);
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE = null; // irrelevant for this test
        ContextElement mappedCE = null; // irrelevant for this test
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(groupedServicePath, event.getServicePathForNaming(true, false));
            System.out.println(getTestTraceHead("[NGSIEvent.getServicePathForNaming]")
                    + "-  OK  - The grouped service path has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getServicePathForNaming]")
                    + "- FAIL - The grouped service path has not been returned");
            throw e;
        } // try catch
    } // testGetServicePathForNamingGR
    
    /**
     * [NGSIEvent.getServicePathForNaming] -------- When mappings are enabled, the mapped service path is returned.
     */
    @Test
    public void testGetServicePathForNamingNM() {
        System.out.println(getTestTraceHead("[NGSIEvent.getServicePathForNaming]")
                + "-------- When mappings are enabled, the mapped service path is returned");
        HashMap<String, String> headers = new HashMap<>();
        headers.put(CommonConstants.HEADER_FIWARE_SERVICE_PATH, originalServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, mappedServicePath);
        headers.put(NGSIConstants.FLUME_HEADER_GROUPED_SERVICE_PATH, groupedServicePath);
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE = null; // irrelevant for this test
        ContextElement mappedCE = null; // irrelevant for this test
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(mappedServicePath, event.getServicePathForNaming(false, true));
            System.out.println(getTestTraceHead("[NGSIEvent.getServicePathForNaming]")
                    + "-  OK  - The grouped service path has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getServicePathForNaming]")
                    + "- FAIL - The grouped service path has not been returned");
            throw e;
        } // try catch
    } // testGetServicePathForNamingNM
    
    /**
     * [NGSIEvent.getEntityForNaming] -------- When grouping, mappings and new encoding are not enabled, the original
     * entity (not encoded) is returned.
     */
    @Test
    public void testGetEntityForNamingNoGRNoNMNoEncoding() {
        System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                + "-------- When grouping, mappings and new encoding are not enabled, the original entity "
                + "(not encoded) is returned");
        HashMap<String, String> headers = null; // irrelevant for this test
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE;
        ContextElement mappedCE;
        
        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
            mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "- FAIL - There was some problem when setting up the test");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(originalEntityNoEncoding, event.getEntityForNaming(false, false, false));
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "-  OK  - The original entity (not encoded) has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "- FAIL - The original entity (not encoded) has not been returned");
            throw e;
        } // try catch
    } // testGetEntityForNamingNoGRNoNMNoEncoding
    
    /**
     * [NGSIEvent.getEntityForNaming] -------- When grouping and mappings are not enabled and new encoding is enabled,
     * the original entity (encoded) is returned.
     */
    @Test
    public void testGetEntityForNamingNoGRNoNMEncoding() {
        System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                + "-------- When grouping and mappings are not enabled and new encoding is enabled, the original "
                + "entity (encoded) is returned");
        HashMap<String, String> headers = null; // irrelevant for this test
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE;
        ContextElement mappedCE;
        
        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
            mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "- FAIL - There was some problem when setting up the test");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(originalEntityEncoding, event.getEntityForNaming(false, false, true));
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "-  OK  - The original entity (not encoded) has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "- FAIL - The original entity (not encoded) has not been returned");
            throw e;
        } // try catch
    } // testGetEntityForNamingNoGRNoNMEncoding
    
    /**
     * [NGSIEvent.getEntityForNaming] -------- When grouping is enabled, independently of the new encoding, the grouped
     * entity is returned.
     */
    @Test
    public void testGetEntityForNamingGR() {
        System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                + "-------- When grouping is enabled, independently of the new encoding, the grouped entity is "
                + "returned");
        HashMap<String, String> headers = new HashMap<>();
        headers.put(NGSIConstants.FLUME_HEADER_GROUPED_ENTITY, groupedEntity);
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE;
        ContextElement mappedCE;
        
        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
            mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "- FAIL - There was some problem when setting up the test");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(groupedEntity, event.getEntityForNaming(true, false, true));
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "-  OK  - The grouped entity has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "- FAIL - The grouped entity has not been returned");
            throw e;
        } // try catch
    } // testGetEntityForNamingGR
    
    /**
     * [NGSIEvent.getEntityForNaming] -------- When mappings is enabled and new encoding is not enabled, the
     * concatenation of the mapped entity ID and type (no encoding) is returned.
     */
    @Test
    public void testGetEntityForNamingNMNoEncoding() {
        System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                + "-------- When mappings is enabled and new encoding is not enabled, the concatenation of the mapped "
                + "entity ID and type (no encoding) is returned");
        HashMap<String, String> headers = null; // irrelevant for this test
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE;
        ContextElement mappedCE;
        
        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
            mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "- FAIL - There was some problem when setting up the test");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(mappedEntityID + "_" + mappedEntityType, event.getEntityForNaming(false, true, false));
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "-  OK  - The concatenation of the mapped entity ID and type (no encoding) has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "- FAIL - The concatenation of the mapped entity ID and type (no encoding) has not been "
                    + "returned");
            throw e;
        } // try catch
    } // testGetEntityForNamingNMNoEncoding
    
    /**
     * [NGSIEvent.getEntityForNaming] -------- When mappings and new encoding are enabled, the concatenation of the
     * mapped entity ID and type (encoding) is returned.
     */
    @Test
    public void testGetEntityForNamingNMEncoding() {
        System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                + "-------- When mappings and new encoding are enabled, the concatenation of the mapped entity ID and "
                + "type (no encoding) is returned");
        HashMap<String, String> headers = null; // irrelevant for this test
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE;
        ContextElement mappedCE;
        
        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
            mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "- FAIL - There was some problem when setting up the test");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(mappedEntityID + "=" + mappedEntityType, event.getEntityForNaming(false, true, true));
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "-  OK  - The concatenation of the mapped entity ID and type (encoding) has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "- FAIL - The concatenation of the mapped entity ID and type (encoding) has not been returned");
            throw e;
        } // try catch
    } // testGetEntityForNamingNMEncoding
    
    /**
     * [NGSIEvent.getEntityForNaming] -------- When mappings is enabled and the mapped type is empty, independently of
     * the new encoding, the mapped entity ID is returned.
     */
    @Test
    public void testGetEntityForNamingNMEmptyType() {
        System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                + "-------- When mappings is enabled and the mapped type is empty, independently of the new encoding, "
                + "the mapped entity ID is returned");
        HashMap<String, String> headers = null; // irrelevant for this test
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE;
        ContextElement mappedCE;
        
        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
            mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEEmptyTypeStr);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "- FAIL - There was some problem when setting up the test");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(mappedEntityID, event.getEntityForNaming(false, true, true));
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "-  OK  - The entity ID has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getEntityForNaming]")
                    + "- FAIL - The entity ID has not been returned");
            throw e;
        } // try catch
    } // testGetEntityForNamingNoNMEmptyType
    
    /**
     * [NGSIEvent.getAttributeForNaming] -------- When mappings is not enabled, the original attribute name is returned.
     */
    @Test
    public void testGetAttributeForNamingNoNM() {
        System.out.println(getTestTraceHead("[NGSIEvent.getAttributeForNaming]")
                + "-------- When mappings is not enabled, the original attribute name is returned");
        HashMap<String, String> headers = null; // irrelevant for this test
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE;
        ContextElement mappedCE;
        
        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
            mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getAttributeForNaming]")
                    + "- FAIL - There was some problem when setting up the test");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(originalAttribute, event.getAttributeForNaming(false));
            System.out.println(getTestTraceHead("[NGSIEvent.getAttributeForNaming]")
                    + "-  OK  - The original attribute name has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getAttributeForNaming]")
                    + "- FAIL - The original attribute has not been returned");
            throw e;
        } // try catch
    } // testGetAttributeForNamingNoNM
    
    /**
     * [NGSIEvent.getAttributeForNaming] -------- When mappings is enabled, the mapped attribute name is returned.
     */
    @Test
    public void testGetAttributeForNamingNM() {
        System.out.println(getTestTraceHead("[NGSIEvent.getAttributeForNaming]")
                + "-------- When mappings is enabled, the mapped attribute name is returned");
        HashMap<String, String> headers = null; // irrelevant for this test
        byte[] body = null; // irrelevant for this test
        ContextElement originalCE;
        ContextElement mappedCE;
        
        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
            mappedCE = NGSIUtilsForTests.createJsonContextElement(mappedCEStr);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getAttributeForNaming]")
                    + "- FAIL - There was some problem when setting up the test");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            assertEquals(mappedAttribute, event.getAttributeForNaming(true));
            System.out.println(getTestTraceHead("[NGSIEvent.getAttributeForNaming]")
                    + "-  OK  - The mapped attribute name has been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getAttributeForNaming]")
                    + "- FAIL - The mapped attribute has not been returned");
            throw e;
        } // try catch
    } // testGetAttributeForNamingNM
    
    /**
     * [NGSIEvent.getBody] -------- Bytes regarding the original context element are returned.
     */
    @Test
    public void testGetBody() {
        System.out.println(getTestTraceHead("[NGSIEvent.getBody]")
                + "-------- Bytes regarding the original context element are returned");
        HashMap<String, String> headers = null; // irrelevant for this test
        byte[] body = originalCEStr.getBytes();
        ContextElement originalCE = null; // irrelevant for this test
        ContextElement mappedCE = null; // irrelevant for this test
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        
        try {
            Assert.assertArrayEquals(body, event.getBody());
            System.out.println(getTestTraceHead("[NGSIEvent.getBody]")
                    + "-  OK  - Bytes regarding the original context element have been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.getBody]")
                    + "- FAIL - Bytes regarding the original context element have not been returned");
            throw e;
        } // try catch
    } // testGetBody
    
    /**
     * [NGSIEvent.setBody] -------- Bytes are correctly set.
     */
    @Test
    public void testSetBody() {
        System.out.println(getTestTraceHead("[NGSIEvent.setBody]")
                + "-------- Bytes are correctly set");
        HashMap<String, String> headers = null; // irrelevant for this test
        byte[] body = originalCEStr.getBytes();
        ContextElement originalCE = null; // irrelevant for this test
        ContextElement mappedCE = null; // irrelevant for this test
        NGSIEvent event = new NGSIEvent(headers, body, originalCE, mappedCE);
        event.setBody((originalCEStr + CommonConstants.CONCATENATOR + mappedCEStr).getBytes());
        
        try {
            Assert.assertArrayEquals((originalCEStr + CommonConstants.CONCATENATOR + mappedCEStr).getBytes(),
                    event.getBody());
            System.out.println(getTestTraceHead("[NGSIEvent.setBody]")
                    + "-  OK  - Bytes regarding the original context element have been returned");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIEvent.setBody]")
                    + "- FAIL - Bytes regarding the original context element have not been returned");
            throw e;
        } // try catch
    } // testSetBody
    
} // NGSIEventTest
