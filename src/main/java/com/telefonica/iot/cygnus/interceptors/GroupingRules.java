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
import com.telefonica.iot.cygnus.utils.Utils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author frb
 */
public class GroupingRules {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(GroupingRules.class);
    private LinkedList<GroupingRule> groupingRules;

    /**
     * Constructor.
     */
    public GroupingRules() {
        groupingRules = null;
    } // GroupingRules

    public synchronized LinkedList<GroupingRule> getRules() {
        return groupingRules;
    } // getRules

    /**
     * Sets the grouping rules given a Json Array of rules.
     * @param jsonRules
     */
    public synchronized void setRules(JSONArray jsonRules) {
        groupingRules = new LinkedList<GroupingRule>();

        for (Object jsonGroupingRule : jsonRules) {
            JSONObject jsonRule = (JSONObject) jsonGroupingRule;
            int err = isValid(jsonRule);

            if (err == 0) {
                GroupingRule rule = new GroupingRule(jsonRule);
                groupingRules.add(rule);
            } else {
                switch (err) {
                    case 1:
                        LOGGER.warn("Invalid grouping rule, some field is missing. It will be discarded. Details="
                                + jsonRule.toJSONString());
                        break;
                    case 2:
                        LOGGER.warn("Invalid grouping rule, the id is not numeric or it is missing. It will be "
                                + "discarded. Details=" + jsonRule.toJSONString());
                        break;
                    case 3:
                        LOGGER.warn("Invalid grouping rule, some field is empty. It will be discarded. Details="
                                + jsonRule.toJSONString());
                        break;
                    default:
                } // switch
            } // if else
        } // for
    } // setRules

    /**
     * Gets the rule matching the given context element for the given service path.
     * @param contextElement
     * @param servicePath
     * @return
     */
    public synchronized GroupingRule getMatchingRule(ContextElement contextElement, String servicePath) {
        if (groupingRules != null) {
            for (GroupingRule rule : groupingRules) {
                String concat = concatenateFields(rule.getFields(), contextElement, servicePath);
                Matcher matcher = rule.pattern.matcher(concat);

                if (matcher.matches()) {
                    return rule;
                } // if
            } // for

            return null;
        } else {
            return null;
        } // if else
    } // getMatchingRule
    
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

    private int isValid(JSONObject jsonRule) {
        // check if the rule contains all the required fields
        if (!jsonRule.containsKey("id")
                || !jsonRule.containsKey("fields")
                || !jsonRule.containsKey("regex")
                || !jsonRule.containsKey("destination")
                || !jsonRule.containsKey("fiware_service_path")) {
            return 1;
        } // if

        // check if the id is numeric
        try {
            Long l = (Long) jsonRule.get("id");
        } catch (Exception e) {
            return 2;
        } // catch

        // check if the rule has any empty field
        if (((JSONArray) jsonRule.get("fields")).size() == 0
                || ((String) jsonRule.get("regex")).length() == 0
                || ((String) jsonRule.get("destination")).length() == 0
                || ((String) jsonRule.get("fiware_service_path")).length() == 0) {
            return 3;
        } // if

        return 0;
    } // isValid
    
    /**
     * Each one of the entries of the matching table.
     */
    protected class GroupingRule {
        
        private final long id;
        private final ArrayList<String> fields;
        private final Pattern pattern;
        private final String destination;
        private final String newFiwareServicePath;
        
        /**
         * Constructor.
         * @param jsonRule
         */
        public GroupingRule(JSONObject jsonRule) {
            this.id = (Long) jsonRule.get("id");
            this.fields = (JSONArray) jsonRule.get("fields");
            this.pattern = Pattern.compile((String) jsonRule.get("regex"));
            this.destination = Utils.encode((String) jsonRule.get("destination"));
            this.newFiwareServicePath = Utils.encode((String) jsonRule.get("fiware_service_path"));
        } // GroupingRule
        
        /**
         * Gets the rule's id.
         * @return
         */
        public long getId() {
            return id;
        } // getId
        
        /**
         * Gets the rule's fields array.
         * @return The rule's fields array.
         */
        public ArrayList<String> getFields() {
            return fields;
        } // getFields
        
        /**
         * Gets the rule's regular expression.
         * @return the rule's regular expression.
         */
        public String getRegex() {
            return pattern.toString();
        } // getRegex
        
        /**
         * Gets the rule's destination.
         * @return The rule's destination.
         */
        public String getDestination() {
            return destination;
        } // destination
        
        /**
         * Gets the rule's newFiwareServicePath.
         * @return The rule's newFiwareServicePath.
         */
        public String getNewFiwareServicePath() {
            return newFiwareServicePath;
        } // getNewFiwareServicePath
        
    } // GroupingRule
    
} // GroupingRules
