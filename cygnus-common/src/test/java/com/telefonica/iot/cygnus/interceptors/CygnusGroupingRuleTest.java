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
package com.telefonica.iot.cygnus.interceptors;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class CygnusGroupingRuleTest {
    
    /**
     * Constructor.
     */
    public CygnusGroupingRuleTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // CygnusGroupingRuleTest
    
    /**
     * [GroupingRule.isValid] -------- fiware-servicePath field in a grouping rule must start with '/'.
     */
    @Test
    public void testFiwareServicePathStartsWithSlash() {
        System.out.println(getTestTraceHead("[GroupingRule.isValid]")
                + "-------- fiware-servicePath field in a grouping rule must start with '/'");
        JSONObject jsonRule = createJsonObject();
        
        try {
            assertEquals(0, CygnusGroupingRule.isValid(jsonRule, true));
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
    
    /**
     * [GroupingRule.getXXXX] -------- Rule's attributes are not null.
     */
    @Test
    public void testIfGetsAreNotNull() {
        System.out.println(getTestTraceHead("[GroupingRule.getXXXX]")
                + "-------- Rule's attributes are not null");
        
        JSONObject jsonObject = createJsonObject();
        jsonObject.put("id", 1L);
        
        // Create the rule for doing the tests
        CygnusGroupingRule rule = new CygnusGroupingRule(jsonObject);
        
        try {
            assertTrue(rule.getPattern() != null);
            System.out.println(getTestTraceHead("[GroupingRule.getPattern]")
                    + "-  OK  - Rule’s pattern is not null");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[GroupingRule.getPattern]")
                    + "- FAIL - Rule’s pattern is null");
            throw e;
        } // try catch
        
        try {
            assertTrue(rule.getId() > 0L);
            System.out.println(getTestTraceHead("[GroupingRule.getId]")
                    + "-  OK  - Rule’s id is upper than 0");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[GroupingRule.getId]")
                    + "- FAIL - Rule’s id is invalid");
            throw e;
        } // try catch
        
        try {
            assertTrue(rule.getFields() != null);
            System.out.println(getTestTraceHead("[GroupingRule.getFields]")
                    + "-  OK  - Rule’s fields are not null");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[GroupingRule.getFields]")
                    + "- FAIL - Rule’s fields are null");
            throw e;
        } // try catch
        
        try {
            assertTrue(rule.getRegex() != null);
            System.out.println(getTestTraceHead("[GroupingRule.getRegex]")
                    + "-  OK  - Rule’s regex is not null");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[GroupingRule.getRegex]")
                    + "- FAIL - Rule’s regex is null");
            throw e;
        } // try catch
        
        try {
            assertTrue(rule.getDestination() != null);
            System.out.println(getTestTraceHead("[GroupingRule.getDestination]")
                    + "-  OK  - Rule’s destination is not null");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[GroupingRule.getDestination]")
                    + "- FAIL - Rule’s destination is null");
            throw e;
        } // try catch
        
        try {
            assertTrue(rule.getNewFiwareServicePath() != null);
            System.out.println(getTestTraceHead("[GroupingRule.getNewFiwareServicePath]")
                    + "-  OK  - Rule’s newFiwareServicePath is not null");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[GroupingRule.getNewFiwareServicePath]")
                    + "- FAIL - Rule’s newFiwareServicePath is null");
            throw e;
        } // try catch
           
    } // testIfGetsAreNotNull
    
    private JSONObject createJsonObject() {
        JSONObject jsonRule = new JSONObject();
        JSONArray fields = new JSONArray();
        fields.add("entityId");
        jsonRule.put("fields", fields);
        jsonRule.put("regex", "room1");
        jsonRule.put("fiware_service_path", "/rooms");
        jsonRule.put("destination", "all_rooms");
        return jsonRule;
    } // createJsonObject
    
} // CygnusGroupingRuleTest
