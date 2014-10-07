/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
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
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * francisco.romerobueno@telefonica.com
 */

package es.tid.fiware.fiwareconnectors.cygnus.interceptors;

import com.google.gson.Gson;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElement;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequestSAXHandler;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
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
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

/**
 * Custom interceptor in charge of extracting the destination where the data must be persisted. This destination is
 * added as a 'destination' header.
 * 
 * @author frb
 */
public class DestinationExtractor implements Interceptor {
    
    private Logger logger;
    private String matchingTableFile;
    private ArrayList<MatchingRule> matchingTable;
    
    /**
     * Constructor.
     */
    public DestinationExtractor(String matchingTableFile) {
        logger = Logger.getLogger(DestinationExtractor.class);
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
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(matchingTableFile));
        } catch (FileNotFoundException e) {
            logger.error("Runtime error (File not found. Details=" + e.getMessage() + ")");
        } // try catch
        
        String line;
        
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.length() == 0) {
                    continue;
                } // if
                
                String[] tokens = line.split("\\|");
                int id = new Integer(tokens[0]).intValue();
                ArrayList<String> fields = new ArrayList<String>(Arrays.asList(tokens[1].split(",")));
                matchingTable.add(new MatchingRule(id, fields, tokens[2], tokens[3]));
            } // while
        } catch (IOException e) {
            logger.error("Runtime error (I/O exception. Details=" + e.getMessage() + ")");
        } // try catch
    } // initialize
 
    @Override
    public Event intercept(Event event) {
        Map<String, String> headers = event.getHeaders();
        String body = new String(event.getBody());
        NotifyContextRequest notification = null;

        if (headers.get(Constants.CONTENT_TYPE).contains("application/json")) {
            Gson gson = new Gson();

            try {
                notification = gson.fromJson(body, NotifyContextRequest.class);
            } catch (Exception e) {
                logger.error("Runtime error (" + e.getMessage() + ")");
                return null;
            } // try catch
        } else if (headers.get(Constants.CONTENT_TYPE).contains("application/xml")) {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            
            try {
                SAXParser saxParser = saxParserFactory.newSAXParser();
                NotifyContextRequestSAXHandler handler = new NotifyContextRequestSAXHandler();
                saxParser.parse(new InputSource(new StringReader(body)), handler);
                notification = handler.getNotifyContextRequest();
            } catch (Exception e) {
                logger.error("Runtime error (" + e.getMessage() + ")");
                return null;
            } // try catch
        } else {
            // this point should never be reached since the content type has been checked when receiving the
            // notification
            logger.error("Runtime error (Unrecognized content type (not Json nor XML)");
            return null;
        } // if else if
        
        // iterate on the contextResponses
        ArrayList<String> destinations = new ArrayList<String>();
        ArrayList<ContextElementResponse> contextResponses = notification.getContextResponses();
        
        for (ContextElementResponse contextElementResponse : contextResponses) {
            ContextElement contextElement = contextElementResponse.getContextElement();
            
            // iterate on the matching rules
            boolean added = false;
            
            for (MatchingRule rule : matchingTable) {
                String concat = concatenateFields(rule.fields, contextElement,
                        headers.get(Constants.SERVICE_PATH_HEADER));
                Matcher matcher = rule.pattern.matcher(concat);
                
                if (matcher.matches()) {
                    destinations.add(rule.destination);
                    added = true;
                    break;
                } // if
            } // for
            
            // check if no matching was found, in that case the default destination ('<entityId>-<entityType>') is used
            if (!added) {
                destinations.add(contextElement.getId() + "-" + contextElement.getType());
            } // if
        } // for
 
        headers.put(Constants.DESTINATION,
                destinations.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", ""));
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
        
        private int id;
        private ArrayList<String> fields;
        private Pattern pattern;
        private String destination;
        
        /**
         * Constructor.
         * @param id
         * @param fields
         * @param regex
         * @param destination
         */
        public MatchingRule(int id, ArrayList<String> fields, String regex, String destination) {
            this.id = id;
            this.fields = fields;
            this.pattern = Pattern.compile(regex);
            this.destination = destination;
        } // MatchingRule
        
        /**
         * Gets the rule's id.
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
        
    } // MatchingRule
  
} // NotificationSpliter
