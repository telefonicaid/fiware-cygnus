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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author frb
 */
/**
 * Each one of the entries of the matching table.
 */
public class CygnusGroupingRule {

    private final JSONObject jsonRule;
    private final Pattern pattern;

    /**
     * Constructor.
     * @param jsonRule
     */
    public CygnusGroupingRule(JSONObject jsonRule) {
        this.jsonRule = jsonRule;
        this.pattern = Pattern.compile((String) jsonRule.get("regex"));
    } // CygnusGroupingRule

    /**
     * Sets the rule's id.
     * @param id
     */
    public void setId(long id) {
        this.jsonRule.put("id", id);
    } // setId
    
    /**
     * Gets the rule's id.
     * @return
     */
    public long getId() {
        return (Long) jsonRule.get("id");
    } // getId

    /**
     * Gets the rule's fields array.
     * @return The rule's fields array.
     */
    public ArrayList<String> getFields() {
        return (JSONArray) jsonRule.get("fields");
    } // getFields

    /**
     * Gets the rule's regular expression.
     * @return the rule's regular expression.
     */
    public String getRegex() {
        return (String) jsonRule.get("regex");
    } // getRegex
    
    /**
     * Gets the compiled pattern.
     * @return The compiled pattern
     */
    public Pattern getPattern() {
        return pattern;
    } // getPattern

    /**
     * Gets the rule's destination.
     * @return The rule's destination.
     */
    public String getDestination() {
        return (String) jsonRule.get("destination");
    } // destination

    /**
     * Gets the rule's newFiwareServicePath.
     * @return The rule's newFiwareServicePath.
     */
    public String getNewFiwareServicePath() {
        return (String) jsonRule.get("fiware_service_path");
    } // getNewFiwareServicePath
    
    /**
     * Checks if the given Json is valid as grouping rule.
     * @param jsonRule
     * @param checkExtraFields
     * @return True if the given Json is valid as grouping rule, otherwise false
     */
    public static int isValid(JSONObject jsonRule, boolean checkExtraFields) {
        boolean containsFields = false;
        boolean containsRegex = false;
        boolean containsDestination = false;
        boolean containsFiwareServicePath = false;
        boolean containsExtraFields = false;
        boolean servicePathStartsWithSlash = false;
        int fieldsSize = 0;
        int regexLength = 0;
        int destinationLength = 0;
        int fiwareServicePathLength = 0;
        
        // iterate on the fields
        Iterator it = jsonRule.keySet().iterator();
        
        while (it.hasNext()) {
            String field = (String) it.next();
            
            if (field.equals("fields")) {
                containsFields = true;
                fieldsSize = ((JSONArray) jsonRule.get("fields")).size();
            } else if (field.equals("regex")) {
                containsRegex = true;
                regexLength = ((String) jsonRule.get("regex")).length();
            } else if (field.equals("destination")) {
                containsDestination = true;
                destinationLength = ((String) jsonRule.get("destination")).length();
            } else if (field.equals("fiware_service_path")) {
                containsFiwareServicePath = true;
                servicePathStartsWithSlash = ((String) jsonRule.get("fiware_service_path")).startsWith("/");
                fiwareServicePathLength = ((String) jsonRule.get("fiware_service_path")).length();
            } else {
                containsExtraFields = true;
            } // if else
        } // while
        
        // check if the rule contains all the required fields
        if (!containsFields || !containsRegex || !containsDestination || !containsFiwareServicePath) {
            return 1;
        } // if
        
        // check if the rule has any empty field
        if (fieldsSize == 0 || regexLength == 0 || destinationLength == 0 || fiwareServicePathLength == 0) {
            return 2;
        } // if
        
        // check if the rule has extra fields not allowed
        if (checkExtraFields && containsExtraFields) {
            return 3;
        } // if
        
        // check if the service-path starts with '/'
        if (!servicePathStartsWithSlash) {
            return 4;
        } // if

        return 0;
    } // isValid
    
    @Override
    public String toString() {
        return jsonRule.toJSONString();
    } // toString

} // CygnusGroupingRule
