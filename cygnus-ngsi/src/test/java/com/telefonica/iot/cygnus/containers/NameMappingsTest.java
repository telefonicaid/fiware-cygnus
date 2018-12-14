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
package com.telefonica.iot.cygnus.containers;

import com.telefonica.iot.cygnus.containers.NameMappings.AttributeMapping;
import com.telefonica.iot.cygnus.containers.NameMappings.EntityMapping;
import com.telefonica.iot.cygnus.containers.NameMappings.ServiceMapping;
import com.telefonica.iot.cygnus.containers.NameMappings.ServicePathMapping;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import java.util.ArrayList;
import java.util.regex.Pattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NameMappingsTest {
    
    private final String nameMappingsOK = ""
            + "{"
            + "   \"serviceMappings\": ["
            + "      {"
            + "         \"originalService\": \"s1\","
            + "         \"newService\": \"new_s1\","
            + "         \"servicePathMappings\": ["
            + "            {"
            + "               \"originalServicePath\": \"/sp1\","
            + "               \"newServicePath\": \"/new_sp1\","
            + "               \"entityMappings\": ["
            + "                  {"
            + "                     \"originalEntityId\": \"Room1\","
            + "                     \"originalEntityType\": \"Room\","
            + "                     \"newEntityId\": \"new_Room1\","
            + "                     \"newEntityType\": \"new_Room\","
            + "                     \"attributeMappings\": ["
            + "                        {"
            + "                           \"originalAttributeName\": \"temperature\","
            + "                           \"originalAttributeType\": \"centigrade\","
            + "                           \"newAttributeName\": \"new_temperature\","
            + "                           \"newAttributeType\": \"new_centigrade\""
            + "                        },"
            + "                        {"
            + "                           \"originalAttributeName\": \"humidity\","
            + "                           \"originalAttributeType\": \"percentage\","
            + "                           \"newAttributeName\": \"new_humidity\","
            + "                           \"newAttributeType\": \"new_percentage\""
            + "                        }"
            + "                     ]"
            + "                  },"
            + "                  {"
            + "                     \"originalEntityId\": \"Room2\","
            + "                     \"originalEntityType\": \"Room\","
            + "                     \"newEntityId\": \"new_Room2\","
            + "                     \"newEntityType\": \"new_Room\","
            + "                     \"attributeMappings\": ["
            + "                        {"
            + "                           \"originalAttributeName\": \"temperature\","
            + "                           \"originalAttributeType\": \"centigrade\","
            + "                           \"newAttributeName\": \"new_temperature\","
            + "                           \"newAttributeType\": \"new_centigrade\""
            + "                        },"
            + "                        {"
            + "                           \"originalAttributeName\": \"humidity\","
            + "                           \"originalAttributeType\": \"percentage\","
            + "                           \"newAttributeName\": \"new_humidity\","
            + "                           \"newAttributeType\": \"new_percentage\""
            + "                        }"
            + "                     ]"
            + "                  }"
            + "               ]"
            + "            },"
            + "            {"
            + "               \"originalServicePath\": \"/sp2\","
            + "               \"newServicePath\": \"/new_sp2\","
            + "               \"entityMappings\": ["
            + "                  {"
            + "                     \"originalEntityId\": \"Room1\","
            + "                     \"originalEntityType\": \"Room\","
            + "                     \"newEntityId\": \"new_Room1\","
            + "                     \"newEntityType\": \"new_Room\","
            + "                     \"attributeMappings\": ["
            + "                        {"
            + "                           \"originalAttributeName\": \"temperature\","
            + "                           \"originalAttributeType\": \"centigrade\","
            + "                           \"newAttributeName\": \"new_temperature\","
            + "                           \"newAttributeType\": \"new_centigrade\""
            + "                        },"
            + "                        {"
            + "                           \"originalAttributeName\": \"humidity\","
            + "                           \"originalAttributeType\": \"percentage\","
            + "                           \"newAttributeName\": \"new_humidity\","
            + "                           \"newAttributeType\": \"new_percentage\""
            + "                        }"
            + "                     ]"
            + "                  },"
            + "                  {"
            + "                     \"originalEntityId\": \"Room2\","
            + "                     \"originalEntityType\": \"Room\","
            + "                     \"newEntityId\": \"new_Room2\","
            + "                     \"newEntityType\": \"new_Room\","
            + "                     \"attributeMappings\": ["
            + "                        {"
            + "                           \"originalAttributeName\": \"temperature\","
            + "                           \"originalAttributeType\": \"centigrade\","
            + "                           \"newAttributeName\": \"new_temperature\","
            + "                           \"newAttributeType\": \"new_centigrade\""
            + "                        },"
            + "                        {"
            + "                           \"originalAttributeName\": \"humidity\","
            + "                           \"originalAttributeType\": \"percentage\","
            + "                           \"newAttributeName\": \"new_humidity\","
            + "                           \"newAttributeType\": \"new_percentage\""
            + "                        }"
            + "                     ]"
            + "                  }"
            + "               ]"
            + "            }"
            + "         ]"
            + "      },"
            + "      {"
            + "         \"originalService\": \"s2\","
            + "         \"newService\": \"new_s2\","
            + "         \"servicePathMappings\": ["
            + "            {"
            + "               \"originalServicePath\": \"/sp1\","
            + "               \"newServicePath\": \"/new_sp1\","
            + "               \"entityMappings\": ["
            + "                  {"
            + "                     \"originalEntityId\": \"Room1\","
            + "                     \"originalEntityType\": \"Room\","
            + "                     \"newEntityId\": \"new_Room1\","
            + "                     \"newEntityType\": \"new_Room\","
            + "                     \"attributeMappings\": ["
            + "                        {"
            + "                           \"originalAttributeName\": \"temperature\","
            + "                           \"originalAttributeType\": \"centigrade\","
            + "                           \"newAttributeName\": \"new_temperature\","
            + "                           \"newAttributeType\": \"new_centigrade\""
            + "                        },"
            + "                        {"
            + "                           \"originalAttributeName\": \"humidity\","
            + "                           \"originalAttributeType\": \"percentage\","
            + "                           \"newAttributeName\": \"new_humidity\","
            + "                           \"newAttributeType\": \"new_percentage\""
            + "                        }"
            + "                     ]"
            + "                  },"
            + "                  {"
            + "                     \"originalEntityId\": \"Room2\","
            + "                     \"originalEntityType\": \"Room\","
            + "                     \"newEntityId\": \"new_Room2\","
            + "                     \"newEntityType\": \"new_Room\","
            + "                     \"attributeMappings\": ["
            + "                        {"
            + "                           \"originalAttributeName\": \"temperature\","
            + "                           \"originalAttributeType\": \"centigrade\","
            + "                           \"newAttributeName\": \"new_temperature\","
            + "                           \"newAttributeType\": \"new_centigrade\""
            + "                        },"
            + "                        {"
            + "                           \"originalAttributeName\": \"humidity\","
            + "                           \"originalAttributeType\": \"percentage\","
            + "                           \"newAttributeName\": \"new_humidity\","
            + "                           \"newAttributeType\": \"new_percentage\""
            + "                        }"
            + "                     ]"
            + "                  }"
            + "               ]"
            + "            },"
            + "            {"
            + "               \"originalServicePath\": \"/sp2\","
            + "               \"newServicePath\": \"/new_sp2\","
            + "               \"entityMappings\": ["
            + "                  {"
            + "                     \"originalEntityId\": \"Room1\","
            + "                     \"originalEntityType\": \"Room\","
            + "                     \"newEntityId\": \"new_Room1\","
            + "                     \"newEntityType\": \"new_Room\","
            + "                     \"attributeMappings\": ["
            + "                        {"
            + "                           \"originalAttributeName\": \"temperature\","
            + "                           \"originalAttributeType\": \"centigrade\","
            + "                           \"newAttributeName\": \"new_temperature\","
            + "                           \"newAttributeType\": \"new_centigrade\""
            + "                        },"
            + "                        {"
            + "                           \"originalAttributeName\": \"humidity\","
            + "                           \"originalAttributeType\": \"percentage\","
            + "                           \"newAttributeName\": \"new_humidity\","
            + "                           \"newAttributeType\": \"new_percentage\""
            + "                        }"
            + "                     ]"
            + "                  },"
            + "                  {"
            + "                     \"originalEntityId\": \"Room2\","
            + "                     \"originalEntityType\": \"Room\","
            + "                     \"newEntityId\": \"new_Room2\","
            + "                     \"newEntityType\": \"new_Room\","
            + "                     \"attributeMappings\": ["
            + "                        {"
            + "                           \"originalAttributeName\": \"temperature\","
            + "                           \"originalAttributeType\": \"centigrade\","
            + "                           \"newAttributeName\": \"new_temperature\","
            + "                           \"newAttributeType\": \"new_centigrade\""
            + "                        },"
            + "                        {"
            + "                           \"originalAttributeName\": \"humidity\","
            + "                           \"originalAttributeType\": \"percentage\","
            + "                           \"newAttributeName\": \"new_humidity\","
            + "                           \"newAttributeType\": \"new_percentage\""
            + "                        }"
            + "                     ]"
            + "                  }"
            + "               ]"
            + "            }"
            + "         ]"
            + "      }"
            + "   ]"
            + "}";
    
    private final String nameMappingsRegex = "" 
            + "{"
            + "   \"serviceMappings\": ["
            + "      {"
            + "         \"originalService\": \".*\","
            + "         \"newService\": \"new_default\","
            + "         \"servicePathMappings\": ["
            + "            {"
            + "               \"originalServicePath\": \"/.*\","
            + "               \"newServicePath\": \"/new_default\","
            + "               \"entityMappings\": ["
            + "                  {"
            + "                     \"originalEntityId\": \"Room\\.(\\d*)\","
            + "                     \"originalEntityType\": \"Room\","
            + "                     \"newEntityId\": \"new_Room1\","
            + "                     \"newEntityType\": \"new_Room\","
            + "                     \"attributeMappings\": ["
            + "                        {"
            + "                           \"originalAttributeName\": \"temp*\","
            + "                           \"originalAttributeType\": \"cent*\","
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
    
    /**
     * [NameMappings.getServiceMappings] -------- Service mappings can be retrieved.
     * @throws java.lang.Exception
     */
    @Test
    public void testNameMappingsSuccessfullyParsed() throws Exception {
        System.out.println(getTestTraceHead("[NameMappings.parse]")
                + "-------- Service mappings can be retrieved");
        NameMappings nameMappings = NGSIUtilsForTests.createJsonNameMappings(nameMappingsOK);
        ArrayList<ServiceMapping> serciceMappings = nameMappings.getServiceMappings();
        
        try {
            assertEquals(2, serciceMappings.size());
            System.out.println(getTestTraceHead("[NameMappings.parse]")
                    + "-  OK  - The retrieved service mappings match the expected ones");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NameMappings.parse]")
                    + "- FAIL - The retrieved service mappings don't match the expected ones");
            throw e;
        } // try catch
        
        for (ServiceMapping serviceMapping : serciceMappings) {
            if (serviceMapping.getOriginalService().equals("s1")) {
                try {
                    assertEquals("new_s1", serviceMapping.getNewService());
                    System.out.println(getTestTraceHead("[NameMappings.parse]")
                            + "-  OK  - The retrieved mapped service for 's1' matches the expected one");
                } catch (AssertionError e) {
                    System.out.println(getTestTraceHead("[NameMappings.parse]")
                            + "- FAIL - The retrieved mapped service for 's1' does not match the expected one");
                    throw e;
                } // try catch
            } else if (serviceMapping.getOriginalService().equals("s2")) {
                try {
                    assertEquals("new_s2", serviceMapping.getNewService());
                    System.out.println(getTestTraceHead("[NameMappings.parse]")
                            + "-  OK  - The retrieved mapped service for 's2' matches the expected one");
                } catch (AssertionError e) {
                    System.out.println(getTestTraceHead("[NameMappings.parse]")
                            + "- FAIL - The retrieved mapped service for 's2' does not match the expected one");
                    throw e;
                } // try catch
            } else {
                System.out.println(getTestTraceHead("[NameMappings.parse]")
                        + "- FAIL - The retrieved mapped service does not exist");
                assertTrue(false);
            } // if else
            
            ArrayList<ServicePathMapping> servicePathMappings = serviceMapping.getServicePathMappings();
            
            try {
                assertEquals(2, servicePathMappings.size());
                System.out.println(getTestTraceHead("[NameMappings.parse]")
                        + "-  OK  - The retrieved service path mappings match the expected ones");
            } catch (AssertionError e) {
                System.out.println(getTestTraceHead("[NameMappings.parse]")
                        + "- FAIL - The retrieved service path mappings don't match the expected ones");
                throw e;
            } // try catch
            
            for (ServicePathMapping servicePathMapping : servicePathMappings) {
                if (servicePathMapping.getOriginalServicePath().equals("/sp1")) {
                    try {
                        assertEquals("/new_sp1", servicePathMapping.getNewServicePath());
                        System.out.println(getTestTraceHead("[NameMappings.parse]")
                                + "-  OK  - The retrieved mapped service path for '/sp1' matches the expected one");
                    } catch (AssertionError e) {
                        System.out.println(getTestTraceHead("[NameMappings.parse]")
                                + "- FAIL - The retrieved mapped service path for '/sp1' does not match the "
                                + "expected one");
                        throw e;
                    } // try catch
                } else if (servicePathMapping.getOriginalServicePath().equals("/sp2")) {
                    try {
                        assertEquals("/new_sp2", servicePathMapping.getNewServicePath());
                        System.out.println(getTestTraceHead("[NameMappings.parse]")
                                + "-  OK  - The retrieved mapped service path for '/sp2' matches the expected one");
                    } catch (AssertionError e) {
                        System.out.println(getTestTraceHead("[NameMappings.parse]")
                                + "- FAIL - The retrieved mapped service path for '/sp2' does not match the "
                                + "expected one");
                        throw e;
                    } // try catch
                } else {
                    System.out.println(getTestTraceHead("[NameMappings.parse]")
                            + "- FAIL - The retrieved mapped service path does not exist");
                    assertTrue(false);
                } // if else
                
                ArrayList<EntityMapping> entityMappings = servicePathMapping.getEntityMappings();
                
                try {
                    assertEquals(2, entityMappings.size());
                    System.out.println(getTestTraceHead("[NameMappings.parse]")
                            + "-  OK  - The retrieved entity mappings match the expected ones");
                } catch (AssertionError e) {
                    System.out.println(getTestTraceHead("[NameMappings.parse]")
                            + "- FAIL - The retrieved entity mappings don't match the expected ones");
                    throw e;
                } // try catch
            
                for (EntityMapping entityMapping : entityMappings) {
                    
                    if (entityMapping.getOriginalEntityId().equals("Room1")) {
                        try {
                            assertEquals("new_Room1", entityMapping.getNewEntityId());
                            System.out.println(getTestTraceHead("[NameMappings.parse]")
                                    + "-  OK  - The retrieved mapped entity ID for 'Room1' matches the expected one");
                        } catch (AssertionError e) {
                            System.out.println(getTestTraceHead("[NameMappings.parse]")
                                    + "- FAIL - The retrieved mapped entity ID for 'Room1' does not match the "
                                    + "expected one");
                            throw e;
                        } // try catch
                    } else if (entityMapping.getOriginalEntityId().equals("Room2")) {
                        try {
                            assertEquals("new_Room2", entityMapping.getNewEntityId());
                            System.out.println(getTestTraceHead("[NameMappings.parse]")
                                    + "-  OK  - The retrieved mapped entity ID for 'Room2' matches the expected one");
                        } catch (AssertionError e) {
                            System.out.println(getTestTraceHead("[NameMappings.parse]")
                                    + "- FAIL - The retrieved mapped entity ID for 'Room2' does not match the "
                                    + "expected one");
                            throw e;
                        } // try catch
                    } else {
                        System.out.println(getTestTraceHead("[NameMappings.parse]")
                                + "- FAIL - The retrieved mapped entity does not exist");
                        assertTrue(false);
                    } // if else
                    
                    if (entityMapping.getOriginalEntityType().equals("Room")) {
                        try {
                            assertEquals("new_Room", entityMapping.getNewEntityType());
                            System.out.println(getTestTraceHead("[NameMappings.parse]")
                                    + "-  OK  - The retrieved mapped entity type for 'Room' matches the expected one");
                        } catch (AssertionError e) {
                            System.out.println(getTestTraceHead("[NameMappings.parse]")
                                    + "- FAIL - The retrieved mapped entity type for 'Room' does not match the "
                                    + "expected one");
                            throw e;
                        } // try catch
                    } // if
                    
                    ArrayList<AttributeMapping> attributeMappings = entityMapping.getAttributeMappings();
                    
                    try {
                        assertEquals(2, attributeMappings.size());
                        System.out.println(getTestTraceHead("[NameMappings.parse]")
                                + "-  OK  - The retrieved attribute mappings match the expected ones");
                    } catch (AssertionError e) {
                        System.out.println(getTestTraceHead("[NameMappings.parse]")
                                + "- FAIL - The retrieved attribute mappings don't match the expected ones");
                        throw e;
                    } // try catch
                    
                    for (AttributeMapping attributeMapping : attributeMappings) {
                        if (attributeMapping.getOriginalAttributeName().equals("temperature")) {
                            try {
                                assertEquals("new_temperature", attributeMapping.getNewAttributeName());
                                System.out.println(getTestTraceHead("[NameMappings.parse]")
                                        + "-  OK  - The retrieved mapped attribute name for 'temperature' matches the "
                                        + "expected one");
                            } catch (AssertionError e) {
                                System.out.println(getTestTraceHead("[NameMappings.parse]")
                                        + "- FAIL - The retrieved mapped attribute name for 'temperature' does not "
                                        + "match the expected one");
                                throw e;
                            } // try catch
                        } else if (attributeMapping.getOriginalAttributeName().equals("humidity")) {
                            try {
                                assertEquals("new_humidity", attributeMapping.getNewAttributeName());
                                System.out.println(getTestTraceHead("[NameMappings.parse]")
                                        + "-  OK  - The retrieved mapped attribute name for 'humidity' matches the "
                                        + "expected one");
                            } catch (AssertionError e) {
                                System.out.println(getTestTraceHead("[NameMappings.parse]")
                                        + "- FAIL - The retrieved mapped attribute name for 'humidity' does not "
                                        + "match the expected one");
                                throw e;
                            } // try catch
                        } else {
                            System.out.println(getTestTraceHead("[NameMappings.parse]")
                                    + "- FAIL - The retrieved mapped attribute does not exist");
                            assertTrue(false);
                        } // if else
                    } // for
                } // for
            } // for
        } // for
    } // testNameMappingsSuccessfullyParsed
    
    /**
     * [NameMappings.compilePatterns] -------- Patterns are successfully compiled.
     * @throws java.lang.Exception
     */
    @Test
    public void testNameMappingsPatternsCompiled() throws Exception {
        System.out.println(getTestTraceHead("[NameMappings.compilePatterns]")
                + "-------- Patterns are successfully compiled");
        NameMappings nameMappings = NGSIUtilsForTests.createJsonNameMappings(nameMappingsRegex);
        nameMappings.compilePatterns();
        ServiceMapping serviceMapping = nameMappings.getServiceMappings().get(0);
       
        try {
            assertEquals(Pattern.compile(serviceMapping.getOriginalService()).pattern(),
                    serviceMapping.getOriginalServicePattern().pattern());
            System.out.println(getTestTraceHead("[NameMappings.compilePatterns]")
                    + "-  OK  - The retrieved pattern for the original service matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NameMappings.compilePatterns]")
                    + "- FAIL - The retrieved pattern for the original service does not match the expected one");
            throw e;
        } // try catch
        
        ServicePathMapping servicePathMapping = serviceMapping.getServicePathMappings().get(0);
        
        try {
            assertEquals(Pattern.compile(servicePathMapping.getOriginalServicePath()).pattern(),
                    servicePathMapping.getOriginalServicePathPattern().pattern());
            System.out.println(getTestTraceHead("[NameMappings.compilePatterns]")
                    + "-  OK  - The retrieved pattern for the original service path matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NameMappings.compilePatterns]")
                    + "- FAIL - The retrieved pattern for the original service path does not match the expected one");
            throw e;
        } // try catch
        
        EntityMapping entityMapping = servicePathMapping.getEntityMappings().get(0);
        
        try {
            assertEquals(Pattern.compile(entityMapping.getOriginalEntityId()).pattern(),
                    entityMapping.getOriginalEntityIdPattern().pattern());
            System.out.println(getTestTraceHead("[NameMappings.compilePatterns]")
                    + "-  OK  - The retrieved pattern for the original entity ID matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NameMappings.compilePatterns]")
                    + "- FAIL - The retrieved pattern for the original entity ID does not match the expected one");
            throw e;
        } // try catch
        
        try {
            assertEquals(Pattern.compile(entityMapping.getOriginalEntityType()).pattern(),
                    entityMapping.getOriginalEntityTypePattern().pattern());
            System.out.println(getTestTraceHead("[NameMappings.compilePatterns]")
                    + "-  OK  - The retrieved pattern for the original entity type matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NameMappings.compilePatterns]")
                    + "- FAIL - The retrieved pattern for the original entity type does not match the expected one");
            throw e;
        } // try catch
        
        AttributeMapping attributeMapping = entityMapping.getAttributeMappings().get(0);
        
        try {
            assertEquals(Pattern.compile(attributeMapping.getOriginalAttributeName()).pattern(),
                    attributeMapping.getOriginalAttributeNamePattern().pattern());
            System.out.println(getTestTraceHead("[NameMappings.compilePatterns]")
                    + "-  OK  - The retrieved pattern for the original attribute name matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NameMappings.compilePatterns]")
                    + "- FAIL - The retrieved pattern for the original attribute name does not match the expected one");
            throw e;
        } // try catch
        
        try {
            assertEquals(Pattern.compile(attributeMapping.getOriginalAttributeType()).pattern(),
                    attributeMapping.getOriginalAttributeTypePattern().pattern());
            System.out.println(getTestTraceHead("[NameMappings.compilePatterns]")
                    + "-  OK  - The retrieved pattern for the original attribute type matches the expected one");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NameMappings.compilePatterns]")
                    + "- FAIL - The retrieved pattern for the original attribute type does not match the expected one");
            throw e;
        } // try catch
    } // testNameMappingsPatternsCompiled
    
} // NameMappingsTest
