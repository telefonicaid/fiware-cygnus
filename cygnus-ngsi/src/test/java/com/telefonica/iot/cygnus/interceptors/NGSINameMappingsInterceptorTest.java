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

import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NGSINameMappingsInterceptorTest {

    private final String nameMappingsStr = "" + "{" + "   \"serviceMappings\": [" + "      {"
            + "         \"originalService\": \".*\"," + "         \"newService\": \"new_default\","
            + "         \"servicePathMappings\": [" + "            {"
            + "               \"originalServicePath\": \"/.*\"," + "               \"newServicePath\": \"/new_any\","
            + "               \"entityMappings\": [" + "                  {"
            + "                     \"originalEntityId\": \"Room(\\\\d*)\","
            + "                     \"originalEntityType\": \"Room\","
            + "                     \"newEntityId\": \"new_Room1\","
            + "                     \"newEntityType\": \"new_Room\"," + "                     \"attributeMappings\": ["
            + "                        {" + "                           \"originalAttributeName\": \"temp(.*)\","
            + "                           \"originalAttributeType\": \"cent(.*)\","
            + "                           \"newAttributeName\": \"new_temperature\","
            + "                           \"newAttributeType\": \"new_centigrade\"" + "                        }"
            + "                     ]" + "                  }" + "               ]" + "            }" + "         ]"
            + "      }" + "   ]" + "}";
    private final String originalService = "default";
    private final String originalServicePath = "/any";
    private final String originalCEStr = "" + "{" + "\"attributes\" : [" + "{" + "\"name\" : \"temperature\","
            + "\"type\" : \"centigrade\"," + "\"value\" : \"26.5\"" + "}" + "]," + "\"type\" : \"Room\","
            + "\"isPattern\" : \"false\"," + "\"id\" : \"Room1\"" + "}";
    private final String expectedCEStr = "" + "{" + "\"attributes\" : [" + "{" + "\"name\" : \"new_temperature\","
            + "\"type\" : \"new_centigrade\"," + "\"value\" : \"26.5\"" + "}" + "]," + "\"type\" : \"new_Room\","
            + "\"isPattern\" : \"false\"," + "\"id\" : \"new_Room1\"" + "}";
    private final String nameMappingsRegexStr = "" + "{" + "   \"serviceMappings\": [" + "      {"
            + "         \"originalService\": \".*\"," + "         \"newService\": \"new_default\","
            + "         \"servicePathMappings\": [" + "            {"
            + "               \"originalServicePath\": \"/.*\"," + "               \"newServicePath\": \"/new_any\","
            + "               \"entityMappings\": [" + "                  {"
            + "                     \"originalEntityId\": \"(Room)([0-9]*)\","
            + "                     \"originalEntityType\": \"Room\","
            + "                     \"newEntityId\": \"new_Room$2\","
            + "                     \"newEntityType\": \"new_Room\"," + "                     \"attributeMappings\": ["
            + "                        {" + "                           \"originalAttributeName\": \"temp(.*)\","
            + "                           \"originalAttributeType\": \"cent(.*)\","
            + "                           \"newAttributeName\": \"new_temperature\","
            + "                           \"newAttributeType\": \"new_centigrade\"" + "                        }"
            + "                     ]" + "                  }" + "               ]" + "            }" + "         ]"
            + "      }" + "   ]" + "}";

    private final String nameMappingsStrConfig = "" + "{" + "   \"serviceMappings\": [" + "      {"
            + "         \"originalService\": \"service\"," + "         \"newService\": \"service_new\","
            + "         \"servicePathMappings\": [" + "            {"
            + "               \"originalServicePath\": \"/servicePath\","
            + "               \"newServicePath\": \"/servicePath_House\"," + "               \"entityMappings\": ["
            + "                  {" + "                     \"originalEntityId\": \".*\","
            + "                     \"originalEntityType\": \"House\","
            + "                     \"newEntityId\": \".*\"," + "                     \"newEntityType\": \"House\","
            + "                     \"attributeMappings\": []" + "                  }" + "               ]"
            + "            }" + "            , {" + "               \"originalServicePath\": \"/servicePath\","
            + "               \"newServicePath\": \"/servicePath_Room\"," + "               \"entityMappings\": ["
            + "                  {" + "                     \"originalEntityId\": \".*\","
            + "                     \"originalEntityType\": \"RoomT\","
            + "                     \"newEntityId\": \".*\"," + "                     \"newEntityType\": \"RoomNew\","
            + "                     \"attributeMappings\": []" + "                  }" + "               ]"
            + "            }" + "            , {" + "               \"originalServicePath\": \"/servicePath\","
            + "               \"newServicePath\": \"/servicePath_Room2\"," + "               \"entityMappings\": ["
            + "                  {" + "                     \"originalEntityId\": \".*\","
            + "                     \"originalEntityType\": \"Room\"," + "                     \"newEntityId\": \".*\","
            + "                     \"newEntityType\": \"RoomNew2\"," + "                     \"attributeMappings\": []"
            + "                  }" + "               ]" + "            }" + "         ]" + "      }" + "   ]" + "}";
    private final String originalCEStrConfig = "" + "{" + "\"attributes\" : [" + "{" + "\"name\" : \"temperature\","
            + "\"type\" : \"centigrade\"," + "\"value\" : \"26.5\"" + "}" + "]," + "\"type\" : \"RoomT\","
            + "\"isPattern\" : \"false\"," + "\"id\" : \"Room1\"" + "}";

    private final String expectedCEStrConfig = "" + "{" + "\"attributes\" : [" + "{" + "\"name\" : \"temperature\","
            + "\"type\" : \"centigrade\"," + "\"value\" : \"26.5\"" + "}" + "]," + "\"type\" : \"RoomNew\","
            + "\"isPattern\" : \"false\"," + "\"id\" : \"Room1\"" + "}";
    private final String originalServiceConfig = "service";
    private final String originalServicePathConfig = "/servicePath";
    private final String expectedServicePathConfig = "/servicePath_Room";

    private final String nameMappingsStrConfig2 = "" + "{" + "   \"serviceMappings\": [" + "      {"
            + "         \"originalService\": \"service\"," + "         \"newService\": \"service_new\","
            + "         \"servicePathMappings\": [" + "            {"
            + "               \"originalServicePath\": \"/servicePath\"," + "               \"entityMappings\": ["
            + "                  {" + "                     \"originalEntityId\": \".*\","
            + "                     \"originalEntityType\": \"House\","
            + "                     \"newEntityId\": \".*\"," + "                     \"newEntityType\": \"House\","
            + "                     \"attributeMappings\": []" + "                  }" + "               ]"
            + "            }" + "            , {" + "               \"originalServicePath\": \"/servicePath\","
            + "               \"newServicePath\": \"/servicePath_Room\"," + "               \"entityMappings\": ["
            + "                  {" + "                     \"originalEntityId\": \".*\","
            + "                     \"originalEntityType\": \"RoomT\","
            + "                     \"newEntityId\": \".*\"," + "                     \"newEntityType\": \"RoomNew\","
            + "                     \"attributeMappings\": []" + "                  }" + "               ]"
            + "            }" + "            , {" + "               \"originalServicePath\": \"/servicePath\","
            + "               \"newServicePath\": \"/servicePath_Room2\"," + "               \"entityMappings\": ["
            + "                  {" + "                     \"originalEntityId\": \".*\","
            + "                     \"originalEntityType\": \"Room\"," + "                     \"newEntityId\": \".*\","
            + "                     \"newEntityType\": \"RoomNew2\"," + "                     \"attributeMappings\": []"
            + "                  }" + "               ]" + "            }" + "         ]" + "      }" + "   ]" + "}";

    private final String originalCEStrConfig2 = "" + "{" + "\"attributes\" : [" + "]," + "\"type\" : \"House\","
            + "\"isPattern\" : \"false\"," + "\"id\" : \"House1\"" + "}";

    private final String expectedCEStrConfig2 = "" + "{" + "\"attributes\" : [" + "]," + "\"type\" : \"House\","
            + "\"isPattern\" : \"false\"," + "\"id\" : \"House1\"" + "}";
    private final String expectedServicePathConfig2 = "/servicePath";

    private final String nameMappingsStrConfig3 = "" + "{" + "   \"serviceMappings\": [" + "      {"
            + "         \"originalService\": \"service\"," + "         \"newService\": \"service_new\","
            + "         \"servicePathMappings\": [" + "            {"
            + "               \"originalServicePath\": \"/(.+)\"," + "               \"newServicePath\": \"/$1\","
            + "               \"entityMappings\": [" + "                  {"
            + "                     \"originalEntityId\": \".*\","
            + "                     \"originalEntityType\": \"House\","
            + "                     \"newEntityId\": \".*\"," + "                     \"newEntityType\": \"House\","
            + "                     \"attributeMappings\": []" + "                  }" + "               ]"
            + "            }" + "            , {" + "               \"originalServicePath\": \"/servicePath\","
            + "               \"newServicePath\": \"/servicePath_Room\"," + "               \"entityMappings\": ["
            + "                  {" + "                     \"originalEntityId\": \".*\","
            + "                     \"originalEntityType\": \"RoomT\","
            + "                     \"newEntityId\": \".*\"," + "                     \"newEntityType\": \"RoomNew\","
            + "                     \"attributeMappings\": []" + "                  }" + "               ]"
            + "            }" + "            , {" + "               \"originalServicePath\": \"/servicePath\","
            + "               \"newServicePath\": \"/servicePath_Room2\"," + "               \"entityMappings\": ["
            + "                  {" + "                     \"originalEntityId\": \".*\","
            + "                     \"originalEntityType\": \"Room\"," + "                     \"newEntityId\": \".*\","
            + "                     \"newEntityType\": \"RoomNew2\"," + "                     \"attributeMappings\": []"
            + "                  }" + "               ]" + "            }" + "         ]" + "      }" + "   ]" + "}";
    private final String nameMappingsStrConfig4 = "" + "{" + "   \"serviceMappings\": [" + "      {"
            + "         \"newService\": \"service_new\"," + "         \"servicePathMappings\": [" + "            {"
            + "               \"entityMappings\": [" + "                  {"
            + "                     \"newEntityId\": \"$1_new\"," + "                     \"newEntityType\": \"House\","
            + "                     \"attributeMappings\": []" + "                  }" + "               ]"
            + "            }" + "         ]" + "      }" + "   ]" + "}";

    private final String expectedCEStrConfig4 = "" + "{" + "\"attributes\" : [" + "]," + "\"type\" : \"House\","
            + "\"isPattern\" : \"false\"," + "\"id\" : \"House1_new\"" + "}";
    private final String expectedServiceConfig4 = "service_new";

    /**
     * Constructor.
     */
    public NGSINameMappingsInterceptorTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSINameMappingsInterceptorTest

    /**
     * [NGSINameMappingsInterceptor.Builder.configure] -------- Configured
     * 'name_mappings_conf_file' cannot be empty.
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
     * [NGSINameMappingsInterceptor.Builder.configure] -------- Configured
     * 'grouping_rules_conf_file' cannot be null.
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
     * [NGSIGroupingInterceptor.getEvents] -------- When a NGSI getRecvTimeTs is
     * put in the channel, it contains fiware-service, fiware-servicepath,
     * fiware-correlator, transaction-id, mapped-fiware-service and
     * mapped-fiware-servicepath headers.
     */
    @Test
    public void testGetEventsHeadersInNGSIFlumeEvent() {
        System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                + "-------- When a NGSI event is put in the channel, it contains fiware-service, fiware-servicepath, "
                + "fiware-correlator, transaction-id, mapped-fiware-service and mapped-fiware-servicepath headers");
        NGSINameMappingsInterceptor nameMappingsInterceptor = new NGSINameMappingsInterceptor(null, false);
        nameMappingsInterceptor.initialize();
        NGSIEvent originalEvent;

        try {
            originalEvent = NGSIUtilsForTests.createNGSIEvent(originalCEStr, null, originalService, originalServicePath,
                    "12345");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - There was some problem when creating the NGSIEVent");
            throw new AssertionError(e.getMessage());
        } // try catch

        Map<String, String> interceptedEventHeaders = nameMappingsInterceptor.intercept(originalEvent).getHeaders();

        try {
            assertTrue(interceptedEventHeaders.containsKey(CommonConstants.HEADER_FIWARE_SERVICE));
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains '" + CommonConstants.HEADER_FIWARE_SERVICE + "'");
        } catch (AssertionError e1) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain '" + CommonConstants.HEADER_FIWARE_SERVICE
                    + "'");
            throw e1;
        } // try catch

        try {
            assertTrue(interceptedEventHeaders.containsKey(CommonConstants.HEADER_FIWARE_SERVICE_PATH));
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains '" + CommonConstants.HEADER_FIWARE_SERVICE_PATH
                    + "'");
        } catch (AssertionError e2) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain '"
                    + CommonConstants.HEADER_FIWARE_SERVICE_PATH + "'");
            throw e2;
        } // try catch

        try {
            assertTrue(interceptedEventHeaders.containsKey(CommonConstants.HEADER_CORRELATOR_ID));
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains '" + CommonConstants.HEADER_CORRELATOR_ID + "'");
        } catch (AssertionError e3) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain '" + CommonConstants.HEADER_CORRELATOR_ID
                    + "'");
            throw e3;
        } // try catch

        try {
            assertTrue(interceptedEventHeaders.containsKey(NGSIConstants.FLUME_HEADER_TRANSACTION_ID));
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains '" + NGSIConstants.FLUME_HEADER_TRANSACTION_ID + "'");
        } catch (AssertionError e4) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain '" + NGSIConstants.FLUME_HEADER_TRANSACTION_ID
                    + "'");
            throw e4;
        } // try catch

        try {
            assertTrue(interceptedEventHeaders.containsKey(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE));
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains '" + NGSIConstants.FLUME_HEADER_MAPPED_SERVICE + "'");
        } catch (AssertionError e5) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain '" + NGSIConstants.FLUME_HEADER_MAPPED_SERVICE
                    + "'");
            throw e5;
        } // try catch

        try {
            assertTrue(interceptedEventHeaders.containsKey(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH));
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains '" + NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH
                    + "'");
        } catch (AssertionError e6) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain '"
                    + NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH + "'");
            throw e6;
        } // try catch
    } // testGetEventsHeadersInNGSIFlumeEvent

    /**
     * [NGSIGroupingInterceptor.getEvents] -------- When a NGSI event is put in
     * the channel, it contains the original ContextElement and the mapped one
     * as objects.
     */
    @Test
    public void testGetContextElementsInNGSIEvent() {
        System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                + "-------- When a NGSI event is put in the channel, it contains the original ContextElement "
                + "and the mapped one as objects");
        NGSINameMappingsInterceptor nameMappingsInterceptor = new NGSINameMappingsInterceptor(null, false);
        nameMappingsInterceptor.loadNameMappings(nameMappingsStr);
        NGSIEvent originalEvent;

        try {
            originalEvent = NGSIUtilsForTests.createNGSIEvent(originalCEStr, null, originalService, originalServicePath,
                    "12345");
        } catch (Exception e) {
            throw new AssertionError(e.getMessage());
        } // try catch

        NGSIEvent interceptedEvent = (NGSIEvent) nameMappingsInterceptor.intercept(originalEvent);

        try {
            assertTrue(interceptedEvent.getMappedCE() != null);
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains the original ContextElement as object");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain the original ContextElement as object");
            throw e;
        } // try catch

        try {
            assertTrue(interceptedEvent.getOriginalCE() != null);
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains the mapped ContextElement as object");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain the mapped ContextElement as object");
            throw e;
        } // try catch
    } // testGetContextElementsInNGSIEvent

    /**
     * [NGSIGroupingInterceptor.getEvents] -------- When a NGSI event is put in
     * the channel, it contains the original ContextElement and the mapped one
     * as bytes in the body.
     */
    @Test
    public void testGetBodyInNGSIEvent() {
        System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                + "-------- When a NGSI event is put in the channel, it contains the original ContextElement and "
                + "the mapped one as bytes in the body");
        NGSINameMappingsInterceptor nameMappingsInterceptor = new NGSINameMappingsInterceptor(null, false);
        nameMappingsInterceptor.loadNameMappings(nameMappingsStr);
        NGSIEvent originalEvent;

        try {
            originalEvent = NGSIUtilsForTests.createNGSIEvent(originalCEStr, null, originalService, originalServicePath,
                    "12345");
        } catch (Exception e) {
            throw new AssertionError(e.getMessage());
        } // try catch

        NGSIEvent interceptedEvent = (NGSIEvent) nameMappingsInterceptor.intercept(originalEvent);

        try {
            String[] contextElementsStr = new String(interceptedEvent.getBody()).split(CommonConstants.CONCATENATOR);
            assertTrue(contextElementsStr[0] != null && !contextElementsStr[0].isEmpty());
            assertTrue(contextElementsStr[1] != null && !contextElementsStr[1].isEmpty());
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "-  OK  - The generated NGSI event contains the original ContextElement as bytes");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.intercept]")
                    + "- FAIL - The generated NGSI event does not contain the original ContextElement as bytes");
            throw e;
        } // try catch
    } // testGetBodyInNGSIEvent

    /**
     * [NGSIGroupingInterceptor.doMap] -------- A mapped ContextElement can be
     * obtained from the Name Mappings.
     */
    @Test
    public void testDoMap() {
        System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMap]")
                + "-------- A mapped ContextElement can be obtained from the Name Mappings");
        NGSINameMappingsInterceptor nameMappingsInterceptor = new NGSINameMappingsInterceptor(null, false);
        nameMappingsInterceptor.loadNameMappings(nameMappingsStr);
        ContextElement originalCE;
        ContextElement expectedCE;

        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
            expectedCE = NGSIUtilsForTests.createJsonContextElement(expectedCEStr);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMap]")
                    + "- FAIL - There was some problem when parsing the ContextElements");
            throw new AssertionError(e.getMessage());
        } // try catch

        ImmutableTriple<String, String, ContextElement> map = nameMappingsInterceptor.doMap(originalService,
                originalServicePath, originalCE);
        ContextElement mappedCE = map.getRight();
        boolean equals = true;

        if (!mappedCE.getId().equals(expectedCE.getId()) || !mappedCE.getType().equals(expectedCE.getType())) {
            equals = false;
        } else {
            for (int j = 0; j < mappedCE.getAttributes().size(); j++) {
                ContextAttribute mappedCA = mappedCE.getAttributes().get(j);
                ContextAttribute expectedCA = expectedCE.getAttributes().get(j);

                if (!mappedCA.getName().equals(expectedCA.getName())
                        || !mappedCA.getType().equals(expectedCA.getType())) {
                    equals = false;
                    break;
                } // if
            } // for
        } // if else

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

    /**
     * [NGSIGroupingInterceptor.doMapConfig] -------- A mapped ContextElement
     * can be obtained from the Name Mappings.
     */
    @Test
    public void testDoMapConfig() {
        System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig]")
                + "-------- A mapped ContextElement can be obtained from the Name Mappings");
        NGSINameMappingsInterceptor nameMappingsInterceptor = new NGSINameMappingsInterceptor(null, false);
        nameMappingsInterceptor.loadNameMappings(nameMappingsStrConfig);
        ContextElement originalCE;
        ContextElement expectedCE;

        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStrConfig);
            expectedCE = NGSIUtilsForTests.createJsonContextElement(expectedCEStrConfig);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig]")
                    + "- FAIL - There was some problem when parsing the ContextElements");
            throw new AssertionError(e.getMessage());
        } // try catch

        ImmutableTriple<String, String, ContextElement> map = nameMappingsInterceptor.doMap(originalServiceConfig,
                originalServicePathConfig, originalCE);
        ContextElement mappedCE = map.getRight();
        boolean equals = true;

        if (!mappedCE.getType().equals(expectedCE.getType()) || !expectedServicePathConfig.equals(map.getMiddle())) {
            equals = false;
        } else {
            for (int j = 0; j < mappedCE.getAttributes().size(); j++) {
                ContextAttribute mappedCA = mappedCE.getAttributes().get(j);
                ContextAttribute expectedCA = expectedCE.getAttributes().get(j);

                if (!mappedCA.getName().equals(expectedCA.getName())
                        || !mappedCA.getType().equals(expectedCA.getType())) {
                    equals = false;
                    break;
                } // if
            } // for
        } // if else

        try {
            assertTrue(equals);
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig]")
                    + "-  OK  - The mapped NotifyContextRequest is equals to the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig]")
                    + "- FAIL - The mapped NotifyContextRequest is not equals to the expected one");
            throw e;
        } // try catch
    } // testDoMapConfig

    /**
     * [NGSIGroupingInterceptor.doMapConfig2] -------- A mapped ContextElement
     * can be obtained from the Name Mappings.
     */
    @Test
    public void testDoMapConfig2() {
        System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig2]")
                + "-------- A mapped ContextElement can be obtained from the Name Mappings");
        NGSINameMappingsInterceptor nameMappingsInterceptor = new NGSINameMappingsInterceptor(null, false);
        nameMappingsInterceptor.loadNameMappings(nameMappingsStrConfig2);
        ContextElement originalCE;
        ContextElement expectedCE;

        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStrConfig2);
            expectedCE = NGSIUtilsForTests.createJsonContextElement(expectedCEStrConfig2);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig2]")
                    + "- FAIL - There was some problem when parsing the ContextElements");
            throw new AssertionError(e.getMessage());
        } // try catch

        ImmutableTriple<String, String, ContextElement> map = nameMappingsInterceptor.doMap(originalServiceConfig,
                originalServicePathConfig, originalCE);
        ContextElement mappedCE = map.getRight();
        boolean equals = true;

        if (!mappedCE.getType().equals(expectedCE.getType()) || !expectedServicePathConfig2.equals(map.getMiddle())) {
            equals = false;
        } else {
            for (int j = 0; j < mappedCE.getAttributes().size(); j++) {
                ContextAttribute mappedCA = mappedCE.getAttributes().get(j);
                ContextAttribute expectedCA = expectedCE.getAttributes().get(j);

                if (!mappedCA.getName().equals(expectedCA.getName())
                        || !mappedCA.getType().equals(expectedCA.getType())) {
                    equals = false;
                    break;
                } // if
            } // for
        } // if else

        try {
            assertTrue(equals);
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig2]")
                    + "-  OK  - The mapped NotifyContextRequest is equals to the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig2]")
                    + "- FAIL - The mapped NotifyContextRequest is not equals to the expected one");
            throw e;
        } // try catch
    } // testDoMapConfig2

    /**
     * [NGSIGroupingInterceptor.doMapConfig3] -------- A mapped ContextElement
     * can be obtained from the Name Mappings.
     */
    @Test
    public void testDoMapConfig3() {
        System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig3]")
                + "-------- A mapped ContextElement can be obtained from the Name Mappings");
        NGSINameMappingsInterceptor nameMappingsInterceptor = new NGSINameMappingsInterceptor(null, false);
        nameMappingsInterceptor.loadNameMappings(nameMappingsStrConfig3);
        ContextElement originalCE;
        ContextElement expectedCE;

        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStrConfig2);
            expectedCE = NGSIUtilsForTests.createJsonContextElement(expectedCEStrConfig2);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig3]")
                    + "- FAIL - There was some problem when parsing the ContextElements");
            throw new AssertionError(e.getMessage());
        } // try catch

        ImmutableTriple<String, String, ContextElement> map = nameMappingsInterceptor.doMap(originalServiceConfig,
                originalServicePathConfig, originalCE);
        ContextElement mappedCE = map.getRight();
        boolean equals = true;

        if (!mappedCE.getType().equals(expectedCE.getType()) || !expectedServicePathConfig2.equals(map.getMiddle())) {
            equals = false;
        } else {
            for (int j = 0; j < mappedCE.getAttributes().size(); j++) {
                ContextAttribute mappedCA = mappedCE.getAttributes().get(j);
                ContextAttribute expectedCA = expectedCE.getAttributes().get(j);

                if (!mappedCA.getName().equals(expectedCA.getName())
                        || !mappedCA.getType().equals(expectedCA.getType())) {
                    equals = false;
                    break;
                } // if
            } // for
        } // if else

        try {
            assertTrue(equals);
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig3]")
                    + "-  OK  - The mapped NotifyContextRequest is equals to the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig3]")
                    + "- FAIL - The mapped NotifyContextRequest is not equals to the expected one");
            throw e;
        } // try catch
    } // testDoMapConfig3

    /**
     * [NGSIGroupingInterceptor.doMapRegex] -------- A mapped ContextElement can
     * be obtained from the Name Mappings.
     */
    @Test
    public void testDoMapRegex() {
        System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapRegex]")
                + "-------- A mapped ContextElement can be obtained from the Name Mappings");
        NGSINameMappingsInterceptor nameMappingsInterceptor = new NGSINameMappingsInterceptor(null, false);
        nameMappingsInterceptor.loadNameMappings(nameMappingsRegexStr);
        ContextElement originalCE;
        ContextElement expectedCE;

        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStr);
            expectedCE = NGSIUtilsForTests.createJsonContextElement(expectedCEStr);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapRegex]")
                    + "- FAIL - There was some problem when parsing the ContextElements");
            throw new AssertionError(e.getMessage());
        } // try catch

        ImmutableTriple<String, String, ContextElement> map = nameMappingsInterceptor.doMap(originalService,
                originalServicePath, originalCE);
        ContextElement mappedCE = map.getRight();
        boolean equals = true;

        if (!mappedCE.getId().equals(expectedCE.getId()) || !mappedCE.getType().equals(expectedCE.getType())) {
            equals = false;
        } else {
            for (int j = 0; j < mappedCE.getAttributes().size(); j++) {
                ContextAttribute mappedCA = mappedCE.getAttributes().get(j);
                ContextAttribute expectedCA = expectedCE.getAttributes().get(j);

                if (!mappedCA.getName().equals(expectedCA.getName())
                        || !mappedCA.getType().equals(expectedCA.getType())) {
                    equals = false;
                    break;
                } // if
            } // for
        } // if else

        try {
            assertTrue(equals);
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapRegex]")
                    + "-  OK  - The mapped NotifyContextRequest is equals to the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapRegex]")
                    + "- FAIL - The mapped NotifyContextRequest is not equals to the expected one");
            throw e;
        } // try catch
    } // testDoMapRegex

    private Context createBuilderContext(String nameMappingsConfFile) {
        Context context = new Context();
        context.put("name_mappings_conf_file", nameMappingsConfFile);
        return context;
    } // createBuilderContext

    /**
     * [NGSIGroupingInterceptor.doMapConfig4] -------- Original fields can be
     * omitted.
     */
    @Test
    public void testDoMapConfig4() {
        System.out.println(
                getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig4]") + "-------- Original fields can be omitted");
        NGSINameMappingsInterceptor nameMappingsInterceptor = new NGSINameMappingsInterceptor(null, false);
        nameMappingsInterceptor.loadNameMappings(nameMappingsStrConfig4);
        ContextElement originalCE;
        ContextElement expectedCE;

        try {
            originalCE = NGSIUtilsForTests.createJsonContextElement(originalCEStrConfig2);
            expectedCE = NGSIUtilsForTests.createJsonContextElement(expectedCEStrConfig4);
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig4]")
                    + "- FAIL - There was some problem when parsing the ContextElements");
            throw new AssertionError(e.getMessage());
        } // try catch

        ImmutableTriple<String, String, ContextElement> map = nameMappingsInterceptor.doMap(originalServiceConfig,
                originalServicePathConfig, originalCE);
        ContextElement mappedCE = map.getRight();
        boolean equals = true;

        if (!mappedCE.getType().equals(expectedCE.getType())) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig4]")
                    + "-  ERROR  - The mapped type is not equal to the expected one");
            equals = false;
        } else if (!expectedServicePathConfig2.equals(map.getMiddle())) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig4]")
                    + "-  ERROR  - The mapped servicePath is not equal to the expected one");
            equals = false;
        } else if (!mappedCE.getId().equals(expectedCE.getId())) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig4]")
                    + "-  ERROR  - The mapped Id is not equal to the expected one");
            equals = false;
        } else if (!map.getLeft().equals(expectedServiceConfig4)) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig4]")
                    + "-  ERROR  - The Service tyepe is not equal to the expected one");
            equals = false;
        } 

        try {
            assertTrue(equals);
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig4]")
                    + "-  OK  - The mapped NotifyContextRequest is equals to the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIGroupingInterceptor.doMapConfig4]")
                    + "- FAIL - The mapped NotifyContextRequest is not equals to the expected one");
            throw e;
        } // try catch
    } // testDoMapConfig4

} // NGSINameMappingsInterceptorTest
