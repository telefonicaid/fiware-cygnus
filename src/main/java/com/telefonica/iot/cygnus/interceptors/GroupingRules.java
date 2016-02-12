/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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

import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author frb
 */
public class GroupingRules {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(GroupingRules.class);
    private LinkedList<GroupingRule> groupingRules;
    private long lastIndex;
    
    /**
     * Constructor.
     * @param groupingRulesFileName
     */
    public GroupingRules(String groupingRulesFileName) {
        groupingRules = null;
        lastIndex = 0;
        
        // read the grouping rules file
        // a JSONParse(groupingRulesFileName) method cannot be used since the file may contain comment lines
        // starting by the '#' character (comments)
        String jsonStr = readGroupingRulesFile(groupingRulesFileName);

        if (jsonStr == null) {
            LOGGER.info("No grouping rules have been read");
            return;
        } // if

        LOGGER.info("Grouping rules read: " + jsonStr);

        // parse the Json containing the grouping rules
        JSONArray jsonGroupingRules = (JSONArray) parseGroupingRules(jsonStr);

        if (jsonGroupingRules == null) {
            LOGGER.warn("Grouping rules syntax has errors");
            return;
        } // if

        LOGGER.info("Grouping rules syntax is OK");

        // create a list of grouping rules, with precompiled regex
        setRules(jsonGroupingRules);
        LOGGER.info("Grouping rules regex'es have been compiled");
    } // GroupingRules
    
    /**
     * Gets the rule matching the given context element for the given service path.
     * @param contextElement
     * @param servicePath
     * @return
     */
    public GroupingRule getMatchingRule(ContextElement contextElement, String servicePath) {
        if (groupingRules != null) {
            for (GroupingRule rule : groupingRules) {
                String concat = concatenateFields(rule.getFields(), contextElement, servicePath);
                Matcher matcher = rule.getPattern().matcher(concat);

                if (matcher.matches()) {
                    return rule;
                } // if
            } // for

            return null;
        } else {
            return null;
        } // if else
    } // getMatchingRule
    
    /**
     * Adds a new rule to the grouping rules.
     * @param rule
     */
    public void addRule(GroupingRule rule) {
        lastIndex++;
        rule.setId(this.lastIndex);
        this.groupingRules.add(rule);
    } // GroupingRule
    
    /**
     * Gets a stringified version of the grouping rules.
     * @return A stringified version of the grouping rules
     */
    @Override
    public String toString() {
        return "{\"grouping_rules\": " + groupingRules.toString() + "}";
    } // toString
    
    /**
     * Gets the grouping rules as a list of GroupingRule objects. It is protected since it is only used by the tests.
     * @return The grouping rules as a list of GroupingRule objects
     */
    protected LinkedList<GroupingRule> getRules() {
        return groupingRules;
    } // getRules

    private void setRules(JSONArray jsonRules) {
        groupingRules = new LinkedList<GroupingRule>();

        for (Object jsonGroupingRule : jsonRules) {
            JSONObject jsonRule = (JSONObject) jsonGroupingRule;
            int err = GroupingRule.isValid(jsonRule);

            if (err == 0) {
                GroupingRule rule = new GroupingRule(jsonRule);
                groupingRules.add(rule);
                lastIndex = rule.getId();
            } else {
                switch (err) {
                    case 1:
                        LOGGER.warn("Invalid grouping rule, some field is missing. It will be discarded. Details:"
                                + jsonRule.toJSONString());
                        break;
                    case 2:
                        LOGGER.warn("Invalid grouping rule, some field is empty. It will be discarded. Details:"
                                + jsonRule.toJSONString());
                        break;
                    default:
                        LOGGER.warn("Invalid grouping rule. It will be discarded. Details:"
                                + jsonRule.toJSONString());
                } // switch
            } // if else
        } // for
    } // setRules
    
    private String concatenateFields(ArrayList<String> fields, ContextElement contextElement, String servicePath) {
        String concat = "";

        for (String field : fields) {
            if (field.equals("entityId")) {
                concat += contextElement.getString(field);
            } else if (field.equals("entityType")) {
                concat += contextElement.getString(field);
            } else if (field.equals("servicePath")) {
                concat += servicePath;
            } // if else
        } // for
        
        return concat;
    } // concatenateFields
    
    private String readGroupingRulesFile(String groupingRulesFileName) {
        if (groupingRulesFileName == null) {
            return null;
        } // if

        String jsonStr = "";
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(groupingRulesFileName));
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found. Details=" + e.getMessage() + ")");
            return null;
        } // try catch

        String line;

        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.length() == 0) {
                    continue;
                } // if

                jsonStr += line;
            } // while

            return jsonStr;
        } catch (IOException e) {
            LOGGER.error("Error while reading the Json-based grouping rules file. Details=" + e.getMessage() + ")");
            return null;
        } // try catch
    } // readGroupingRulesFile

    private JSONArray parseGroupingRules(String jsonStr) {
        if (jsonStr == null) {
            return null;
        } // if

        JSONParser jsonParser = new JSONParser();

        try {
            return (JSONArray) ((JSONObject) jsonParser.parse(jsonStr)).get("grouping_rules");
        } catch (ParseException e) {
            LOGGER.error("Error while parsing the Json-based grouping rules file. Details=" + e.getMessage());
            return null;
        } // try catch
    } // parseGroupingRules

} // GroupingRules
