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

import static com.telefonica.iot.cygnus.utils.TestUtils.getTestTraceHead;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class GroupingRuleTest {
    
    /**
     * Constructor.
     */
    public GroupingRuleTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // GroupingRuleTest
    
    /**
     * [Groupingrule.isValid] -------- fiware-servicePath field in a grouping rule must start with '/'.
     */
    @Test
    public void testFiwareServicePathStartsWithSlash() {
        System.out.println(getTestTraceHead("[GroupingRule.isValid]")
                + "-------- fiware-servicePath field in a grouping rule must start with '/'");
        JSONObject jsonRule = new JSONObject();
        JSONArray fields = new JSONArray();
        fields.add("entityId");
        jsonRule.put("fields", fields);
        jsonRule.put("regex", "room1");
        jsonRule.put("fiware_service_path", "/rooms");
        jsonRule.put("destination", "all_rooms");
        
        try {
            assertEquals(0, GroupingRule.isValid(jsonRule, true));
            System.out.println(getTestTraceHead("[GroupingRule.isValid]")
                    + "-  OK  - The fiware-servicePath field in the rule '"
                    + jsonRule.toJSONString() + "' starts with '/'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[GroupingRule.isValid]")
                    + "- FAIL - The fiware-servicePath field in the rule '"
                    + jsonRule.toJSONString() + "' does not start with '/'");
            throw e;
        } // try catch
    } // testFiwareServicePathStartsWithSlash
    
} // GroupingRuleTest
