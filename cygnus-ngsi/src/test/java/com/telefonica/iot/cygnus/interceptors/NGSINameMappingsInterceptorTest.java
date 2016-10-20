/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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

import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.createEvent;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NGSINameMappingsInterceptorTest {
    
    private final String nameMappingsStr = ""
            + "{"
            + "   \"serviceMappings\": ["
            + "      {"
            + "         \"originalService\": \".*\","
            + "         \"newService\": \"new_default\","
            + "         \"servicePathMappings\": ["
            + "            {"
            + "               \"originalServicePath\": \"/.*\","
            + "               \"newServicePath\": \"/new_any\","
            + "               \"entityMappings\": ["
            + "                  {"
            + "                     \"originalEntityId\": \"Room(\\\\d*)\","
            + "                     \"originalEntityType\": \"Room\","
            + "                     \"newEntityId\": \"new_Room1\","
            + "                     \"newEntityType\": \"new_Room\","
            + "                     \"attributeMappings\": ["
            + "                        {"
            + "                           \"originalAttributeName\": \"temp(.*)\","
            + "                           \"originalAttributeType\": \"cent(.*)\","
            + "                           \"newAttributeName\": \"new_temperature\","
            + "                           \"newAttributeType\": \"new_centigrade\""
            + "                        }"
            + "                     ]"
            + "                  }"
            + "               ]"
            + "            }"
            + "         ]"
            + "      }"
            + "   ]"
            + "}";
    private final String originalService = "default";
    private final String originalServicePath = "/any";
    private final String originalNCRStr = ""
            + "{"
            +   "\"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\","
            +   "\"originator\" : \"localhost\","
            +   "\"contextResponses\" : ["
            +     "{"
            +       "\"contextElement\" : {"
            +         "\"attributes\" : ["
            +           "{"
            +             "\"name\" : \"temperature\","
            +             "\"type\" : \"centigrade\","
            +             "\"value\" : \"26.5\""
            +           "}"
            +         "],"
            +         "\"type\" : \"Room\","
            +         "\"isPattern\" : \"false\","
            +         "\"id\" : \"Room1\""
            +       "},"
            +       "\"statusCode\" : {"
            +         "\"code\" : \"200\","
            +         "\"reasonPhrase\" : \"OK\""
            +       "}"
            +     "}"
            +   "]"
            + "}";
    private final String expectedNCRStr = ""
            + "{"
            +   "\"subscriptionId\" : \"51c0ac9ed714fb3b37d7d5a8\","
            +   "\"originator\" : \"localhost\","
            +   "\"contextResponses\" : ["
            +     "{"
            +       "\"contextElement\" : {"
            +         "\"attributes\" : ["
            +           "{"
            +             "\"name\" : \"new_temperature\","
            +             "\"type\" : \"new_centigrade\","
            +             "\"value\" : \"26.5\""
            +           "}"
            +         "],"
            +         "\"type\" : \"new_Room\","
            +         "\"isPattern\" : \"false\","
            +         "\"id\" : \"new_Room1\""
            +       "},"
            +       "\"statusCode\" : {"
            +         "\"code\" : \"200\","
            +         "\"reasonPhrase\" : \"OK\""
            +       "}"
            +     "}"
            +   "]"
            + "}";
    
    /**
     * Constructor.
     */
    public NGSINameMappingsInterceptorTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSINameMappingsInterceptorTest
    
    /**
     * [NGSINameMappingsInterceptor.Builder.configure] -------- Configured 'name_mappings_conf_file' cannot be empty.
     */
    @Test
    public void testBuilderConfigureNameMappingsConfFileNotEmpty() {
        System.out.println(getTestTraceHead("[NGSINameMappingInterceptor.Builder.configure]")
                + "-------- Configured 'name_mappings_conf_file' cannot be empty");
        NGSINameMappingsInterceptor.Builder builder = new NGSINameMappingsInterceptor.Builder();
        String nameMappingsConfFile = ""; // wrong value
        Context context = createBuilderContext(nameMappingsConfFile);
        builder.configure(context);
        
        try {
            assertTrue(builder.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSINameMappingInterceptor.Builder.configure]")
                    + "-  OK  - Empty 'name_mappings_conf_file' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSINameMappingInterceptor.Builder.configure]")
                    + "- FAIL - Empty 'name_mappings_conf_file' has not been detected");
            throw e;
        } // try catch
    } // testBuilderConfigureNameMappingsConfFileNotEmpty
    
    /**
     * [NGSINameMappingsInterceptor.Builder.configure] -------- Configured 'grouping_rules_conf_file' cannot be null.
     */
    @Test
    public void testBuilderConfigureNameMappingsConfFileNotNull() {
        System.out.println(getTestTraceHead("[NGSINameMappingInterceptor.Builder.configure]")
                + "-------- Configured 'name_mappings_conf_file' cannot be null");
        NGSINameMappingsInterceptor.Builder builder = new NGSINameMappingsInterceptor.Builder();
        String nameMappingsConfFile = null; // wrong value
        Context context = createBuilderContext(nameMappingsConfFile);
        builder.configure(context);
        
        try {
            assertTrue(builder.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSINameMappingInterceptor.Builder.configure]")
                    + "-  OK  - Null 'name_mappings_conf_file' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSINameMappingInterceptor.Builder.configure]")
                    + "- FAIL - Null 'name_mappings_conf_file' has not been detected");
            throw e;
        } // try catch
    } // testBuilderConfigureNameMappingsConfFileNotNull
    
    /**
     * [NGSIGroupingInterceptor.getEvents] -------- When a NGSI event is put in the channel, it contains
     * fiware-service, fiware-servicepath, fiware-correlator, transaction-id, mapped-fiware-service and
     * mapped-fiware-servicepath headers.
     */
    @Test
    public void testGetEventsHeadersInNGSIFlumeEvent() {
        System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                + "-------- When a NGSI event is put in the channel, it contains fiware-service, fiware-servicepath, "
                + "fiware-correlator, transaction-id, mapped-fiware-service and mapped-fiware-servicepath headers");
        NGSINameMappingsInterceptor nameMappingsInterceptor = new NGSINameMappingsInterceptor(null, false);
        nameMappingsInterceptor.initialize();
        Event originalEvent = createEvent();
        Map<String, String> interceptedEventHeaders = nameMappingsInterceptor.intercept(originalEvent).getHeaders();

        try {
            assertTrue(interceptedEventHeaders.containsKey(CommonConstants.HEADER_FIWARE_SERVICE));
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains '"
                    + CommonConstants.HEADER_FIWARE_SERVICE + "'");
        } catch (AssertionError e1) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain '"
                    + CommonConstants.HEADER_FIWARE_SERVICE + "'");
            throw e1;
        } // try catch

        try {
            assertTrue(interceptedEventHeaders.containsKey(CommonConstants.HEADER_FIWARE_SERVICE_PATH));
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains '"
                    + CommonConstants.HEADER_FIWARE_SERVICE_PATH + "'");
        } catch (AssertionError e2) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain '"
                    + CommonConstants.HEADER_FIWARE_SERVICE_PATH + "'");
            throw e2;
        } // try catch

        try {
            assertTrue(interceptedEventHeaders.containsKey(CommonConstants.HEADER_CORRELATOR_ID));
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains '"
                    + CommonConstants.HEADER_CORRELATOR_ID + "'");
        } catch (AssertionError e3) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain '"
                    + CommonConstants.HEADER_CORRELATOR_ID + "'");
            throw e3;
        } // try catch

        try {
            assertTrue(interceptedEventHeaders.containsKey(NGSIConstants.FLUME_HEADER_TRANSACTION_ID));
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains '"
                    + NGSIConstants.FLUME_HEADER_TRANSACTION_ID + "'");
        } catch (AssertionError e4) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain '"
                    + NGSIConstants.FLUME_HEADER_TRANSACTION_ID + "'");
            throw e4;
        } // try catch
        
        try {
            assertTrue(interceptedEventHeaders.containsKey(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE));
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains '"
                    + NGSIConstants.FLUME_HEADER_MAPPED_SERVICE + "'");
        } catch (AssertionError e5) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain '"
                    + NGSIConstants.FLUME_HEADER_MAPPED_SERVICE + "'");
            throw e5;
        } // try catch
        
        try {
            assertTrue(interceptedEventHeaders.containsKey(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH));
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains '"
                    + NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH + "'");
        } catch (AssertionError e6) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain '"
                    + NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH + "'");
            throw e6;
        } // try catch
    } // testGetEventsHeadersInNGSIFlumeEvent
    
    /**
     * [NGSIGroupingInterceptor.getEvents] -------- When a NGSI event is put in the channel, it contains
     * the original NotifyContextRequest and the mapped one.
     */
    @Test
    public void testGetNCRsInNGSIEvent() {
        System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                + "-------- When a Flume event is put in the channel, it contains the original NotifyContextRequest "
                + "and the mapped one");
        NGSINameMappingsInterceptor nameMappingsInterceptor = new NGSINameMappingsInterceptor(null, false);
        nameMappingsInterceptor.loadNameMappings(nameMappingsStr);
        Event originalEvent = createEvent();
        NGSIEvent ngsiEvent = (NGSIEvent) nameMappingsInterceptor.intercept(originalEvent);

        try {
            assertTrue(ngsiEvent.getMappedNCR() != null);
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains the original NotifyContextRequest");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain the original NotifyContextRequest");
            throw e;
        } // try catch
        
        try {
            assertTrue(ngsiEvent.getOriginalNCR() != null);
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains the mapped NotifyContextRequest");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain the mapped NotifyContextRequest");
            throw e;
        } // try catch
    } // testGetNCRsInNGSIEvent
    
    /**
     * [NGSIGroupingInterceptor.doMap] -------- A mapped NotifyContextRequest can be obtained from the Name Mappings.
     */
    @Test
    public void testDoMap() {
        System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMap]")
                + "-------- A mapped NotifyContextRequest can be obtained from the Name Mappings");
        NGSINameMappingsInterceptor nameMappingsInterceptor = new NGSINameMappingsInterceptor(null, false);
        nameMappingsInterceptor.loadNameMappings(nameMappingsStr);
        NotifyContextRequest originalNCR = NGSIUtilsForTests.createNotifyContextRequest(originalNCRStr);
        NotifyContextRequest expectedNCR = NGSIUtilsForTests.createNotifyContextRequest(expectedNCRStr);
        ImmutableTriple<String, String, NotifyContextRequest> map = nameMappingsInterceptor.doMap(
                originalService, originalServicePath, originalNCR);
        NotifyContextRequest mappedNCR = map.getRight();
        boolean equals = true;
        
        for (int i = 0; i < mappedNCR.getContextResponses().size(); i++) {
            ContextElement ce = mappedNCR.getContextResponses().get(i).getContextElement();
            ContextElement expectedCE = expectedNCR.getContextResponses().get(i).getContextElement();
            
            if (!ce.getId().equals(expectedCE.getId()) || !ce.getType().equals(expectedCE.getType())) {
                equals = false;
                break;
            } // if
            
            for (int j = 0; j < ce.getAttributes().size(); j++) {
                ContextAttribute ca = ce.getAttributes().get(j);
                ContextAttribute expectedCA = expectedCE.getAttributes().get(j);
                
                if (!ca.getName().equals(expectedCA.getName()) || !ca.getType().equals(expectedCA.getType())) {
                    equals = false;
                    break;
                } // if
            } // for
        } // for
        
        try {
            assertTrue(equals);
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMap]")
                    + "-  OK  - The mapped NotifyContextRequest is equals to the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMap]")
                    + "- FAIL - The mapped NotifyContextRequest is not equals to the expected one");
            throw e;
        } // try catch
    } // testDoMap
    
    private Context createBuilderContext(String nameMappingsConfFile) {
        Context context = new Context();
        context.put("name_mappings_conf_file", nameMappingsConfFile);
        return context;
    } // createBuilderContext

} // NGSINameMappingsInterceptorTest
