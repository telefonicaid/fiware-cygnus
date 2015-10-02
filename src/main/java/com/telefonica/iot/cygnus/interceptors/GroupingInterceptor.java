/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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

import com.google.gson.Gson;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import com.telefonica.iot.cygnus.containers.NotifyContextRequestSAXHandler;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Custom interceptor in charge of extracting the destination where the data must be persisted. This destination is
 * added as a 'destination' header.
 * 
 * @author frb
 */
public class GroupingInterceptor implements Interceptor {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(GroupingInterceptor.class);
    private final String groupingRulesFileName;
    private LinkedList<GroupingRule> groupingRules;
    
    /**
     * Constructor.
     * @param groupingRulesFileName
     */
    public GroupingInterceptor(String groupingRulesFileName) {
        this.groupingRulesFileName = groupingRulesFileName;
    } // GroupingInterceptor
    
    /**
     * Gets the grouping rules. This is protected since it is only going to be used in the tests.
     * @return
     */
    protected LinkedList<GroupingRule> getGroupingRules() {
        return groupingRules;
    } // getGroupingRules
    
    @Override
    public void initialize() {
        // read the grouping rules file; a JSONParse(Reader) method cannot be used since the file may contain comment
        // lines starting by the '#' character
        String jsonStr = readGroupingRulesFile(groupingRulesFileName);
        
        if (jsonStr == null) {
            LOGGER.info("No grouping rules read");
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
        groupingRules = createGroupingRules(jsonGroupingRules);
        
        if (groupingRules == null) {
            LOGGER.warn("Grouping rules regex'es could not be compiled");
        }
        
        LOGGER.info("Grouping rules regex'es have been compiled");
    } // initialize
    
    /**
     * Reads a file containing Json-based grouing rules. The file may contain lines representing comments, which start
     * by the '#' character.
     * @param fileName File to be read
     * @return A string containing the Json, discarding the comment lines
     */
    private String readGroupingRulesFile(String fileName) {
        String jsonStr = "";
        
        if (fileName == null) {
            // despite configuring the interceptor, no table_matching.conf file has been specified
            return null;
        } // if
        
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
    
    private LinkedList<GroupingRule> createGroupingRules(JSONArray jsonGroupingRules) {
        if (jsonGroupingRules == null) {
            return null;
        } // if
        
        groupingRules = new LinkedList<GroupingRule>();
        
        for (Object jsonGroupingRule : jsonGroupingRules) {
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
                } // swtich
            } // if else
        } // for
        
        return groupingRules;
    } // createGroupingRules
    
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
 
    @Override
    public Event intercept(Event event) {
        // get the original headers and body
        Map<String, String> headers = event.getHeaders();
        String body = new String(event.getBody());
        
        // get some original header values
        String fiwareServicePath = headers.get(Constants.HEADER_NOTIFIED_SERVICE_PATH);
        
        // parse the original body; this part may be unnecessary if notifications are parsed at the source only once
        // see --> https://github.com/telefonicaid/fiware-cygnus/issues/359
        NotifyContextRequest notification;

        if (headers.get(Constants.HEADER_CONTENT_TYPE).contains("application/json")) {
            Gson gson = new Gson();

            try {
                notification = gson.fromJson(body, NotifyContextRequest.class);
            } catch (Exception e) {
                LOGGER.error("Runtime error (" + e.getMessage() + ")");
                return null;
            } // try catch // try catch
        } else if (headers.get(Constants.HEADER_CONTENT_TYPE).contains("application/xml")) {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            
            try {
                SAXParser saxParser = saxParserFactory.newSAXParser();
                NotifyContextRequestSAXHandler handler = new NotifyContextRequestSAXHandler();
                saxParser.parse(new InputSource(new StringReader(body)), handler);
                notification = handler.getNotifyContextRequest();
            } catch (ParserConfigurationException e) {
                LOGGER.error("Runtime error (" + e.getMessage() + ")");
                return null;
            } catch (SAXException e) {
                LOGGER.error("Runtime error (" + e.getMessage() + ")");
                return null;
            } catch (IOException e) {
                LOGGER.error("Runtime error (" + e.getMessage() + ")");
                return null;
            } // try catch // try catch
        } else {
            // this point should never be reached since the content type has been checked when receiving the
            // notification
            LOGGER.error("Runtime error (Unrecognized content type (not Json nor XML)");
            return null;
        } // if else if
        
        // iterate on the contextResponses
        ArrayList<String> defaultDestinations = new ArrayList<String>();
        ArrayList<String> defaultServicePaths = new ArrayList<String>();
        ArrayList<String> groupedDestinations = new ArrayList<String>();
        ArrayList<String> groupedServicePaths = new ArrayList<String>();
        ArrayList<ContextElementResponse> contextResponses = notification.getContextResponses();
        
        if (contextResponses == null || contextResponses.isEmpty()) {
            LOGGER.warn("No context responses within the notified entity, nothing is done");
            return null;
        } // if
        
        for (ContextElementResponse contextElementResponse : contextResponses) {
            ContextElement contextElement = contextElementResponse.getContextElement();
            
            // iterate on the matching rules
            boolean added = false;
            
            if (groupingRules != null) {
                for (GroupingRule rule : groupingRules) {
                    String concat = concatenateFields(rule.getFields(), contextElement, fiwareServicePath);
                    Matcher matcher = rule.pattern.matcher(concat);

                    if (matcher.matches()) {
                        groupedDestinations.add((String) rule.getDestination());
                        groupedServicePaths.add((String) rule.getNewFiwareServicePath());
                        added = true;
                        break;
                    } // if
                } // for
            } // if
            
            // check if no matching was found, in that case the default destination ('<entityId>_<entityType>') and the
            // notified fiware-servicePath are used
            if (!added) {
                groupedDestinations.add(Utils.encode(contextElement.getId() + "_" + contextElement.getType()));
                groupedServicePaths.add(fiwareServicePath);
            } // if
            
            defaultDestinations.add(Utils.encode(contextElement.getId() + "_" + contextElement.getType()));
            defaultServicePaths.add(fiwareServicePath);
        } // for
 
        // set the final header values
        headers.put(Constants.HEADER_DEFAULT_DESTINATIONS,
                defaultDestinations.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", ""));
        headers.put(Constants.HEADER_DEFAULT_SERVICE_PATHS,
                defaultServicePaths.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", ""));
        headers.put(Constants.HEADER_GROUPED_DESTINATIONS,
                groupedDestinations.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", ""));
        headers.put(Constants.HEADER_GROUPED_SERVICE_PATHS,
                groupedServicePaths.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", ""));
        event.setHeaders(headers);
        return event;
    } // intercept
 
    @Override
    public List<Event> intercept(List<Event> events) {
        List<Event> interceptedEvents = new ArrayList<Event>(events.size());
        
        for (Event event : events) {
            Event interceptedEvent = intercept(event);
            interceptedEvents.add(interceptedEvent);
        } // for
 
        return interceptedEvents;
    } // intercept
 
    @Override
    public void close() {
    } // close
 
    /**
     * Builder class for this new interceptor.
     */
    public static class Builder implements Interceptor.Builder {
        private String groupingRulesFileName;
 
        @Override
        public void configure(Context context) {
            String groupingRulesFileNameTmp = context.getString("grouping_rules_conf_file");
            String matchingTableFileName = context.getString("matching_table");
            
            if (groupingRulesFileNameTmp != null && groupingRulesFileNameTmp.length() > 0) {
                groupingRulesFileName = groupingRulesFileNameTmp;
                LOGGER.debug("[de] Reading configuration (grouping_rules_file=" + groupingRulesFileName + ")");
            } else if (matchingTableFileName != null && matchingTableFileName.length() > 0) {
                groupingRulesFileName = matchingTableFileName;
                LOGGER.debug("[de] Reading configuration (matching_table=" + groupingRulesFileName + ")"
                        + " -- DEPRECATED, use grouping_rules_file instead");
            } else {
                groupingRulesFileName = null;
                LOGGER.debug("[de] Defaulting to grouping_rules_file=null");
            } // if else
        } // configure
 
        @Override
        public Interceptor build() {
            return new GroupingInterceptor(groupingRulesFileName);
        } // build
    } // Builder
    
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
  
} // NotificationSpliter
