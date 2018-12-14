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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author frb
 */
public class CygnusGroupingRulesTest {
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    /**
     * Constructor.
     */
    public CygnusGroupingRulesTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // CygnusGroupingRulesTest
    
    /**
     * [GroupingRules.constructor] -------- Unexistent/unreadable grouping rules file is detected.
     */
    @Test
    public void testConstructorUnexistentFile() {
        System.out.println(getTestTraceHead("[CygnusGroupingRules.constructor]")
                + "-------- Unexistent/unreadable grouping rules file is detected");
        CygnusGroupingRules cygnusGroupingRules = new CygnusGroupingRules("/a/b/c/unexistent.txt");
        
        try {
            assertTrue(cygnusGroupingRules.getRules().isEmpty());
            System.out.println(getTestTraceHead("[CygnusGroupingRules.constructor]")
                    + "-  OK  - An unexistent/unreadble file '/a/b/c/unexistent.txt' has been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusGroupingRules.constructor]")
                    + "- FAIL - An unexistent/unreadble file '/a/b/c/unexistent.txt' has not been detected");
            throw e;
        } // try catch
    } // testConstructorUnexistentFile
    
    /**
     * [GroupingRules.constructor] -------- Missing or empty fields in all the grouping rules are detected.
     */
    @Test
    public void testConstructorAllRulesWithMissingOrEmptyFields() {
        System.out.println(getTestTraceHead("[CygnusGroupingRules.constructor]")
                + "-------- Missing or empty fields in all the grouping rules are detected");
        String groupingRulesStr = "{\"grouping_rules\":[{\"fields\":[],\"destination\":\"\"}]}";
        File file;
        
        try {
            file = folder.newFile("grouping_rules.conf");
            PrintWriter out = new PrintWriter(file);
            out.println(groupingRulesStr);
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(getTestTraceHead("[CygnusGroupingRules.constructor]")
                    + "- FAIL - There was some problem when mocking the grouping rules file");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        CygnusGroupingRules cygnusGroupingRules = new CygnusGroupingRules(file.getAbsolutePath());
        
        try {
            assertTrue(cygnusGroupingRules.getRules().isEmpty());
            System.out.println(getTestTraceHead("[CygnusGroupingRules.constructor]")
                    + "-  OK  - Missing or empty fields in all the grouping rules '" + groupingRulesStr
                    + "' have been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusGroupingRules.constructor]")
                    + "- FAIL - Missing or empty fields in all the grouping rules '" + groupingRulesStr
                    + "'have not been detected");
            throw e;
        } // try catch
    } // testConstructorAllRulesWithMissingOrEmptyFields
    
    /**
     * [GroupingRules.constructor] -------- Syntax errors in the grouping rules are detected.
     */
    @Test
    public void testConstructorSyntaxErrorsInRules() {
        System.out.println(getTestTraceHead("[CygnusGroupingRules.constructor]")
                + "-------- Syntax errors in the grouping rules are detected");
        String groupingRulesStr = "{\"grouping_rules\":[}";
        File file;
        
        try {
            file = folder.newFile("grouping_rules.conf");
            PrintWriter out = new PrintWriter(file);
            out.println(groupingRulesStr);
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(getTestTraceHead("[CygnusGroupingRules.constructor]")
                    + "- FAIL - There was some problem when mocking the grouping rules file");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        CygnusGroupingRules cygnusGroupingRules = new CygnusGroupingRules(file.getAbsolutePath());
        
        try {
            assertTrue(cygnusGroupingRules.getRules().isEmpty());
            System.out.println(getTestTraceHead("[CygnusGroupingRules.constructor]")
                    + "-  OK  - Syntax errors in the grouping rules '" + groupingRulesStr + "' have been detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusGroupingRules.constructor]")
                    + "- FAIL - Syntax errors in the grouping rules '" + groupingRulesStr + "'have not been detected");
            throw e;
        } // try catch
    } // testConstructorSyntaxErrorsInRules
    
    /**
     * [GroupingRules.getMatchingRule] -------- Service path based matching rules match.
     */
    @Test
    public void testGetMatchingRuleServicePath() {
        System.out.println(getTestTraceHead("[CygnusGroupingRules.getMatchingRule]")
                + "-------- Service path based matching rules match");
        ArrayList<String> ruleFields = new ArrayList<String>();
        ruleFields.add("servicePath");
        String ruleRegex = "/someServicePath";
        String ruleDestination = "new_dest";
        String ruleServicePath = "/new_svc_path";
        CygnusGroupingRules cygnusGroupingRules = createSingleRuleGroupingRules(
                ruleFields, ruleRegex, ruleDestination, ruleServicePath, "getMatchingRule");
        String servicePath = "/someServicePath";
        String entityId = "someId";
        String entityType = "someType";
        CygnusGroupingRule rule = cygnusGroupingRules.getMatchingRule(servicePath, entityId, entityType);
        
        try {
            assertEquals(ruleDestination, rule.getDestination());
            assertEquals(ruleServicePath, rule.getNewFiwareServicePath());
            System.out.println(getTestTraceHead("[CygnusGroupingRules.getMatchingRule]")
                    + "-  OK  - Matching rules '" + cygnusGroupingRules.toString(true) + "' match service path '"
                    + servicePath + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusGroupingRules.getMatchingRule]")
                    + "- FAIL - Matching rules '" + cygnusGroupingRules.toString(true) + "' don't match service path '"
                    + servicePath + "'");
            throw e;
        } // try catch
    } // testGetMatchingRuleServicePath
    
    /**
     * [GroupingRules.getMatchingRule] -------- Service path and entity ID based matching rules match.
     */
    @Test
    public void testGetMatchingRuleServicePathEntityId() {
        System.out.println(getTestTraceHead("[CygnusGroupingRules.getMatchingRule]")
                + "-------- Service path and entity ID based matching rules match");
        ArrayList<String> ruleFields = new ArrayList<String>();
        ruleFields.add("servicePath");
        ruleFields.add("entityId");
        String ruleRegex = "/someServicePathsomeId";
        String ruleDestination = "new_dest";
        String ruleServicePath = "/new_svc_path";
        CygnusGroupingRules cygnusGroupingRules = createSingleRuleGroupingRules(
                ruleFields, ruleRegex, ruleDestination, ruleServicePath, "getMatchingRule");
        String servicePath = "/someServicePath";
        String entityId = "someId";
        String entityType = "someType";
        CygnusGroupingRule rule = cygnusGroupingRules.getMatchingRule(servicePath, entityId, entityType);
        
        try {
            assertEquals(ruleDestination, rule.getDestination());
            assertEquals(ruleServicePath, rule.getNewFiwareServicePath());
            System.out.println(getTestTraceHead("[CygnusGroupingRules.getMatchingRule]")
                    + "-  OK  - Matching rules '" + cygnusGroupingRules.toString(true) + "' match service path '"
                    + servicePath + "' and entity ID '" + entityId + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusGroupingRules.getMatchingRule]")
                    + "- FAIL - Matching rules '" + cygnusGroupingRules.toString(true) + "' don't match service path '"
                    + servicePath + "' nor entity ID '" + entityId + "'");
            throw e;
        } // try catch
    } // testGetMatchingRuleServicePathEntityId
    
    /**
     * [GroupingRules.getMatchingRule] -------- Service path, entity ID and entity type based matching rules match.
     */
    @Test
    public void testGetMatchingRuleServicePathEntityIdEntityType() {
        System.out.println(getTestTraceHead("[CygnusGroupingRules.getMatchingRule]")
                + "-------- Service path, entity ID and entity type based matching rules match");
        ArrayList<String> ruleFields = new ArrayList<String>();
        ruleFields.add("servicePath");
        ruleFields.add("entityId");
        ruleFields.add("entityType");
        String ruleRegex = "/someServicePathsomeIdsomeType";
        String ruleDestination = "new_dest";
        String ruleServicePath = "/new_svc_path";
        CygnusGroupingRules cygnusGroupingRules = createSingleRuleGroupingRules(
                ruleFields, ruleRegex, ruleDestination, ruleServicePath, "getMatchingRule");
        String servicePath = "/someServicePath";
        String entityId = "someId";
        String entityType = "someType";
        CygnusGroupingRule rule = cygnusGroupingRules.getMatchingRule(servicePath, entityId, entityType);
        
        try {
            assertEquals(ruleDestination, rule.getDestination());
            assertEquals(ruleServicePath, rule.getNewFiwareServicePath());
            System.out.println(getTestTraceHead("[CygnusGroupingRules.getMatchingRule]")
                    + "-  OK  - Matching rules '" + cygnusGroupingRules.toString(true) + "' match service path '"
                    + servicePath + "', entity ID '" + entityId + "' and entity type '" + entityType + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusGroupingRules.getMatchingRule]")
                    + "- FAIL - Matching rules '" + cygnusGroupingRules.toString(true) + "' don't match service path '"
                    + servicePath + "' nor entity ID '" + entityId + "' nor entity type '" + entityType + "'");
            throw e;
        } // try catch
    } // testGetMatchingRuleServicePathEntityIdEntityType
    
    /**
     * [GroupingRules.addRule] -------- Adding a new rule works.
     */
    @Test
    public void testAddRule() {
        System.out.println(getTestTraceHead("[CygnusGroupingRules.addRule]")
                + "-------- Adding a new rule works");
        ArrayList<String> ruleFields1 = new ArrayList<String>();
        ruleFields1.add("servicePath");
        String ruleRegex1 = "/someServicePath1";
        String ruleDestination1 = "new_dest1";
        String ruleServicePath1 = "/new_svc_path1";
        CygnusGroupingRules cygnusGroupingRules = createSingleRuleGroupingRules(
                ruleFields1, ruleRegex1, ruleDestination1, ruleServicePath1, "addRule");
        ArrayList<String> ruleFields2 = new ArrayList<String>();
        ruleFields2.add("servicePath");
        String ruleRegex2 = "/someServicePath2";
        String ruleDestination2 = "new_dest2";
        String ruleServicePath2 = "/new_svc_path2";
        CygnusGroupingRule cygnusGroupingRule = new CygnusGroupingRule(createJsonRule(ruleFields2, ruleRegex2,
                ruleDestination2, ruleServicePath2));
        cygnusGroupingRules.addRule(cygnusGroupingRule);
        
        try {
            assertTrue(cygnusGroupingRules.getRules().size() == 2);
            System.out.println(getTestTraceHead("[CygnusGroupingRules.addRule]")
                    + "-  OK  - New rule has been added");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusGroupingRules.addRule]")
                    + "- FAIL - New rule has not been added");
            throw e;
        } // try catch
    } // testAddRule
    
    /**
     * [GroupingRules.addRule] -------- When a rule is added, the index increases by 1.
     */
    @Test
    public void testAddRuleIndexIncreased() {
        System.out.println(getTestTraceHead("[CygnusGroupingRules.addRule]")
                + "-------- When a rule is added, the index increases by 1");
        ArrayList<String> ruleFields1 = new ArrayList<String>();
        ruleFields1.add("servicePath");
        String ruleRegex1 = "/someServicePath1";
        String ruleDestination1 = "new_dest1";
        String ruleServicePath1 = "/new_svc_path1";
        CygnusGroupingRules cygnusGroupingRules = createSingleRuleGroupingRules(
                ruleFields1, ruleRegex1, ruleDestination1, ruleServicePath1, "addRule");
        ArrayList<String> ruleFields2 = new ArrayList<String>();
        ruleFields2.add("servicePath");
        String ruleRegex2 = "/someServicePath2";
        String ruleDestination2 = "new_dest2";
        String ruleServicePath2 = "/new_svc_path2";
        CygnusGroupingRule cygnusGroupingRule = new CygnusGroupingRule(createJsonRule(ruleFields2, ruleRegex2,
                ruleDestination2, ruleServicePath2));
        long prevLastIndex = cygnusGroupingRules.getLastIndex();
        cygnusGroupingRules.addRule(cygnusGroupingRule);
        long currLastIndex = cygnusGroupingRules.getLastIndex();
        
        try {
            assertTrue(currLastIndex == (prevLastIndex + 1));
            System.out.println(getTestTraceHead("[CygnusGroupingRules.addRule]")
                    + "-  OK  - Last index before adding a rule was '" + prevLastIndex + "', now it is '"
                    + currLastIndex + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusGroupingRules.addRule]")
                    + "- FAIL - Last index before adding a rule was '" + prevLastIndex + "', now it is not '"
                    + (prevLastIndex + 1) + "'");
            throw e;
        } // try catch
    } // testAddRuleIndexIncreased
    
    /**
     * [GroupingRules.deleteRule] -------- Deleting an existent rule works.
     */
    @Test
    public void testDeleteRule() {
        System.out.println(getTestTraceHead("[CygnusGroupingRules.deleteRule]")
                + "-------- Deleting an existent rule works");
        ArrayList<String> ruleFields = new ArrayList<String>();
        ruleFields.add("servicePath");
        String ruleRegex = "/someServicePath";
        String ruleDestination = "new_dest";
        String ruleServicePath = "/new_svc_path";
        CygnusGroupingRules cygnusGroupingRules = createSingleRuleGroupingRules(
                ruleFields, ruleRegex, ruleDestination, ruleServicePath, "deleteRule");
        cygnusGroupingRules.deleteRule(1);
        
        try {
            assertTrue(cygnusGroupingRules.getRules().size() == 0);
            System.out.println(getTestTraceHead("[CygnusGroupingRules.deleteRule]")
                    + "-  OK  - Rule with ID 1 has been deleted");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusGroupingRules.deleteRule]")
                    + "- FAIL - Rule with ID 1 has not been deleted");
            throw e;
        } // try catch
    } // testDeleteRule
    
    /**
     * [GroupingRules.updateRule] -------- Updating an existent rule works.
     */
    @Test
    public void testUpdateRule() {
        System.out.println(getTestTraceHead("[CygnusGroupingRules.updateRule]")
                + "-------- Updating an existent rule works");
        ArrayList<String> ruleFields1 = new ArrayList<String>();
        ruleFields1.add("servicePath");
        String ruleRegex1 = "/someServicePath1";
        String ruleDestination1 = "new_dest1";
        String ruleServicePath1 = "/new_svc_path1";
        CygnusGroupingRules cygnusGroupingRules = createSingleRuleGroupingRules(
                ruleFields1, ruleRegex1, ruleDestination1, ruleServicePath1, "addRule");
        ArrayList<String> ruleFields2 = new ArrayList<String>();
        ruleFields2.add("servicePath");
        String ruleRegex2 = "/someServicePath2";
        String ruleDestination2 = "new_dest2";
        String ruleServicePath2 = "/new_svc_path2";
        CygnusGroupingRule cygnusGroupingRule = new CygnusGroupingRule(createJsonRule(ruleFields2, ruleRegex2,
                ruleDestination2, ruleServicePath2));
        cygnusGroupingRules.updateRule(1, cygnusGroupingRule);
        CygnusGroupingRule updatedRule = cygnusGroupingRules.getRules().get(0);
        
        try {
            assertTrue(updatedRule.getFields().get(0).equals("servicePath"));
            assertTrue(updatedRule.getRegex().equals(ruleRegex2));
            assertTrue(updatedRule.getNewFiwareServicePath().equals(ruleServicePath2));
            assertTrue(updatedRule.getDestination().equals(ruleDestination2));
            System.out.println(getTestTraceHead("[CygnusGroupingRules.updateRule]")
                    + "-  OK  - Rule with ID 1 has been updated");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[CygnusGroupingRules.updateRule]")
                    + "- FAIL - Rule with ID 1 has not been updated");
            throw e;
        } // try catch
    } // testUpdateRule
    
    private JSONObject createJsonRule(ArrayList<String> fields, String regex, String destination,
            String fiwareServicePath) {
        JSONObject jsonRule = new JSONObject();
        jsonRule.put("id", (long) 1);
        JSONArray jsonArray = new JSONArray();
        
        for (String field : fields) {
            jsonArray.add(field);
        } // for
        
        jsonRule.put("fields", jsonArray);
        jsonRule.put("regex", regex);
        jsonRule.put("fiware_service_path", fiwareServicePath);
        jsonRule.put("destination", destination);
        return jsonRule;
    } // createJsonRule
    
    private JSONObject createJsonRules(JSONObject jsonRule) {
        JSONArray rulesArray = new JSONArray();
        rulesArray.add(jsonRule);
        JSONObject jsonRules = new JSONObject();
        jsonRules.put("grouping_rules", rulesArray);
        return jsonRules;
    } // createJsonRules
    
    private CygnusGroupingRules createSingleRuleGroupingRules(ArrayList<String> fields, String regex,
            String destination, String fiwareServicePath, String method) {
        JSONObject jsonRule = createJsonRule(fields, regex, destination, fiwareServicePath);
        JSONObject jsonRules = createJsonRules(jsonRule);
        String groupingRulesStr = jsonRules.toJSONString().replaceAll("\\\\", "");
        File file;
        
        try {
            file = folder.newFile("grouping_rules.conf");
            PrintWriter out = new PrintWriter(file);
            out.println(groupingRulesStr);
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(getTestTraceHead("[CygnusGroupingRules." + method + "]")
                    + "- FAIL - There was some problem when mocking the grouping rules file");
            throw new AssertionError(e.getMessage());
        } // try catch
        
        return new CygnusGroupingRules(file.getAbsolutePath());
    } // createSingleRuleGroupingRules
    
} // CygnusGroupingRulesTest
