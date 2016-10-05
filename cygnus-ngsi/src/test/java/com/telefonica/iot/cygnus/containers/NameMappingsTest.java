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
package com.telefonica.iot.cygnus.containers;

import com.telefonica.iot.cygnus.containers.NameMappings.ServiceMapping;
import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import com.telefonica.iot.cygnus.utils.TestUtils;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
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
            + "         \"originalService\": \"frb\","
            + "         \"newService\": \"new_frb\","
            + "         \"servicePathMappings\": ["
            + "            {"
            + "               \"originalServicePath\": \"/any\","
            + "               \"newServicePath\": \"/new_any\","
            + "               \"entityMappings\": ["
            + "                  {"
            + "                     \"originalEntityId\": \"Room1\","
            + "                     \"originalEntityType\": \"Room\","
            + "                     \"newEntityId\": \"new_room1\","
            + "                     \"newEntityType\": \"new_room\","
            + "                     \"attributeMappings\": ["
            + "                        {"
            + "                           \"originalAttributeName\": \"temperature\","
            + "                           \"originalAttributeType\": \"centigrade\","
            + "                           \"newAttributeName\": \"new_temp\","
            + "                           \"newAttributeType\": \"new_cent\""
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
    public void testGetNameMappings() throws Exception {
        System.out.println(getTestTraceHead("[NameMappings.getServiceMappings]")
                + "-------- Service mappings can be retrieved");
        NameMappings nameMappings = TestUtils.createJsonNameMappings(nameMappingsOK);
        
        try {
            ArrayList<ServiceMapping> result = nameMappings.getServiceMappings();
            assertEquals(1, result.size());
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NameMappings.getServiceMappings]")
                    + "- FAIL - The retrieved service mappings don't match the expected ones");
            throw e;
        } // try catch
    } // testGetSubscriptionID
    
} // NameMappingsTest
