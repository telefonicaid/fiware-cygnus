/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
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
import java.util.Arrays;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Custom interceptor in charge of extracting the destination where the data must be persisted. This destination is
 * added as a 'destination' header.
 * 
 * @author frb
 */
public class DestinationExtractor implements Interceptor {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(DestinationExtractor.class);
    private final String matchingTableFile;
    private ArrayList<MatchingRule> matchingTable;
    
    /**
     * Constructor.
     * @param matchingTableFile
     */
    public DestinationExtractor(String matchingTableFile) {
        this.matchingTableFile = matchingTableFile;
    } // DestinationExtractor
    
    /**
     * Gets the matching table. This is protected since it is only going to be used in the tests.
     * @return
     */
    protected ArrayList<MatchingRule> getMatchingTable() {
        return matchingTable;
    } // getMatchingTable
    
    @Override
    public void initialize() {
        // load the matching table from the file where it is described
        matchingTable = new ArrayList<MatchingRule>();
        BufferedReader reader;
        
        try {
            reader = new BufferedReader(new FileReader(matchingTableFile));
        } catch (FileNotFoundException e) {
            LOGGER.error("Runtime error (File not found. Details=" + e.getMessage() + ")");
            return;
        } // try catch // try catch
        
        String line;
        
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.length() == 0) {
                    continue;
                } // if
                
                String[] tokens = line.split("\\|");
                
                if (tokens.length < 5 || tokens[0].length() == 0 || tokens[1].length() == 0 || tokens[2].length() == 0
                        || tokens[3].length() == 0 || tokens[4].length() == 0) {
                    LOGGER.warn("Malformed matching rule, it will be discarded. Details=" + line);
                    continue;
                } // if
                
                int id = new Integer(tokens[0]);
                ArrayList<String> fields = new ArrayList<String>(Arrays.asList(tokens[1].split(",")));
                matchingTable.add(new MatchingRule(id, fields, tokens[2], tokens[3], tokens[4]));
            } // while
        } catch (IOException e) {
            LOGGER.error("Runtime error (I/O exception. Details=" + e.getMessage() + ")");
        } // try catch // try catch
    } // initialize
 
    @Override
    public Event intercept(Event event) {
        // get the original headers and body
        Map<String, String> headers = event.getHeaders();
        String body = new String(event.getBody());
        
        // get some original header values
        String fiwareServicePath = headers.get(Constants.HEADER_SERVICE_PATH);
        
        // parse the original body
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
        ArrayList<String> destinations = new ArrayList<String>();
        ArrayList<String> datasets = new ArrayList<String>();
        ArrayList<ContextElementResponse> contextResponses = notification.getContextResponses();
        
        for (ContextElementResponse contextElementResponse : contextResponses) {
            ContextElement contextElement = contextElementResponse.getContextElement();
            
            // iterate on the matching rules
            boolean added = false;
            
            for (MatchingRule rule : matchingTable) {
                String concat = concatenateFields(rule.fields, contextElement,
                        headers.get(Constants.HEADER_SERVICE_PATH));
                Matcher matcher = rule.pattern.matcher(concat);
                
                if (matcher.matches()) {
                    destinations.add(rule.destination);
                    datasets.add(rule.dataset);
                    added = true;
                    break;
                } // if
            } // for
            
            // check if no matching was found, in that case the default destination ('<entityId>_<entityType>') and the
            // notified fiware-servicePath are used
            if (!added) {
                destinations.add(Utils.encode(contextElement.getId() + "_" + contextElement.getType()));
                datasets.add(fiwareServicePath);
            } // if
        } // for
 
        // set the final header values
        headers.put(Constants.DESTINATION,
                destinations.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", ""));
        headers.put(Constants.HEADER_SERVICE_PATH,
                datasets.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", ""));
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
        private String matchingTableFile;
 
        @Override
        public void configure(Context context) {
            matchingTableFile = context.getString("matching_table");
        } // configure
 
        @Override
        public Interceptor build() {
            return new DestinationExtractor(matchingTableFile);
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
    protected class MatchingRule {
        
        private final int id;
        private ArrayList<String> fields;
        private Pattern pattern;
        private String destination;
        private String dataset;
        
        /**
         * Constructor.
         * @param id
         * @param fields
         * @param regex
         * @param destination
         * @param group
         */
        public MatchingRule(int id, ArrayList<String> fields, String regex, String destination, String group) {
            this.id = id;
            this.fields = fields;
            this.pattern = Pattern.compile(regex);
            this.destination = Utils.encode(destination);
            this.dataset = Utils.encode(group);
        } // MatchingRule
        
        /**
         * Gets the rule's id.
         * @return
         */
        public int getId() {
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
         * Gets the rule's dataset.
         * @return The rule's dataset.
         */
        public String getDataset() {
            return dataset;
        } // getDataset
        
    } // MatchingRule
  
} // NotificationSpliter
