/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.interceptors;

import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.JsonUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author frb
 */
public class CygnusGroupingRules {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(CygnusGroupingRules.class);
    private LinkedList<CygnusGroupingRule> groupingRules = new LinkedList<CygnusGroupingRule>();
    private long lastIndex = 0;
    
    /**
     * Constructor.
     * @param groupingRulesFileName
     */
    public CygnusGroupingRules(String groupingRulesFileName) {
        // Read the grouping rules file
        String jsonStr;
        
        try {
            jsonStr = JsonUtils.readJsonFile(groupingRulesFileName);
        } catch (Exception e) {
            LOGGER.warn("No grouping rules have been read. Details: " + e.getMessage());
            return;
        } // try catch

        LOGGER.info("Grouping rules read: " + jsonStr);

        // Parse the Json containing the grouping rules
        JSONArray jsonGroupingRules;
        
        try {
            jsonGroupingRules = (JSONArray) JsonUtils.parseJsonString(jsonStr).get("grouping_rules");
        } catch (Exception e) {
            LOGGER.warn("Grouping rules syntax has errors. Details: " + e.getMessage());
            return;
        } // try catch

        LOGGER.info("Grouping rules syntax is OK");

        // Create a list of grouping rules, with precompiled regex
        setRules(jsonGroupingRules);
        
        if (groupingRules.isEmpty()) {
            LOGGER.warn("Grouping rules discarded due to missing or empty values");
        } else {
            LOGGER.info("Grouping rules loaded in memory");
        } // if else
    } // CygnusGroupingRules

    private void setRules(JSONArray jsonRules) {
        for (Object jsonGroupingRule : jsonRules) {
            JSONObject jsonRule = (JSONObject) jsonGroupingRule;
            int err = CygnusGroupingRule.isValid(jsonRule, false);

            if (err == 0) {
                CygnusGroupingRule rule = new CygnusGroupingRule(jsonRule);
                groupingRules.add(rule);
                lastIndex = rule.getId();
            } else {
                switch (err) {
                    case 1:
                        LOGGER.debug("Invalid grouping rule, some field is missing. It will be discarded. Details:"
                                + jsonRule.toJSONString());
                        break;
                    case 2:
                        LOGGER.debug("Invalid grouping rule, some field is empty. It will be discarded. Details:"
                                + jsonRule.toJSONString());
                        break;
                    case 3:
                        LOGGER.debug("Invalid grouping rule, some field is not allowed. It will be discarded. Details:"
                                + jsonRule.toJSONString());
                        break;
                    case 4:
                        LOGGER.debug("Invalid grouping rule, the fiware-servicePath does not start with '/'. "
                                + "It will be discarded. Details:" + jsonRule.toJSONString());
                        break;
                    default:
                        LOGGER.debug("Invalid grouping rule. It will be discarded. Details:"
                                + jsonRule.toJSONString());
                } // switch
            } // if else
        } // for
    } // setRules
    
    /**
     * Gets the rule matching the given string.
     * @param servicePath
     * @param entityId
     * @param entityType
     * @return The grouping rule matching the give string
     */
    public CygnusGroupingRule getMatchingRule(String servicePath, String entityId, String entityType) {
        if (groupingRules == null) {
            return null;
        } // if
        
        for (CygnusGroupingRule rule : groupingRules) {
            String s = concatenateFields(rule.getFields(), servicePath, entityId, entityType);
            Matcher matcher = rule.getPattern().matcher(s);

            if (matcher.matches()) {
                return rule;
            } // if
        } // for

        return null;
    } // getMatchingRule
    
    private String concatenateFields(ArrayList<String> fields, String servicePath, String entityId,
            String entityType) {
        String concat = "";

        for (String field : fields) {
            if (field.equals("entityId")) {
                concat += entityId;
            } else if (field.equals("entityType")) {
                concat += entityType;
            } else if (field.equals("servicePath")) {
                concat += servicePath;
            } // if else
        } // for
        
        return concat;
    } // concatenateFields
    
    /**
     * Adds a new rule to the grouping rules.
     * @param rule
     */
    public void addRule(CygnusGroupingRule rule) {
        lastIndex++;
        rule.setId(this.lastIndex);
        this.groupingRules.add(rule);
    } // CygnusGroupingRule
    
    /**
     * Deletes a rule given its ID.
     * @param id
     * @return True, if the rule was deleted, otherwise false
     */
    public boolean deleteRule(long id) {
        if (groupingRules == null) {
            return false;
        } // if
        
        for (int i = 0; i < groupingRules.size(); i++) {
            CygnusGroupingRule groupingRule = groupingRules.get(i);
            
            if (groupingRule.getId() == id) {
                groupingRules.remove(i);
                return true;
            } // if
        } // for
        
        return false;
    } // deleteRule
    
    /**
     * Updates a rule given its ID.
     * @param id
     * @param rule
     * @return True, if the rule was updated, otherwise false
     */
    public boolean updateRule(long id, CygnusGroupingRule rule) {
        if (groupingRules == null) {
            return false;
        } // if
        
        for (int i = 0; i < groupingRules.size(); i++) {
            CygnusGroupingRule groupingRule = groupingRules.get(i);
            
            if (groupingRule.getId() == id) {
                groupingRules.remove(i);
                rule.setId(id);
                groupingRules.add(i, rule);
                return true;
            } // if
        } // for
        
        return false;
    } // updateRule
    
    /**
     * Gets a stringified version of the grouping rules.
     * @param asField
     * @return A stringified version of the grouping rules
     */
    public String toString(boolean asField) {
        if (groupingRules == null) {
            if (asField) {
                return "\"grouping_rules\": []";
            } else {
                return "{\"grouping_rules\": []}";
            } // if else
        } else {
            if (asField) {
                return "\"grouping_rules\": " + groupingRules.toString();
            } else {
                return "{\"grouping_rules\": " + groupingRules.toString() + "}";
            } // if else
        } // if else
    } // toString
    
    /**
     * Gets the grouping rules as a list of CygnusGroupingRule objects. It is protected since it is only used
     * by the tests.
     * @return The grouping rules as a list of CygnusGroupingRule objects
     */
    protected LinkedList<CygnusGroupingRule> getRules() {
        return groupingRules;
    } // getRules
    
    /**
     * Gets the last index used for indexing rules. It is protected since it is only used by the tests.
     * @return
     */
    protected long getLastIndex() {
        return lastIndex;
    } // getLastIndex

} // CygnusGroupingRules
