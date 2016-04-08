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

import com.google.gson.Gson;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

/**
 * Custom interceptor in charge of extracting the destination where the data must be persisted. This destination is
 * added as a 'destination' header.
 * 
 * @author frb
 */
public class GroupingInterceptor implements Interceptor {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(GroupingInterceptor.class);
    private final String groupingRulesFileName;
    private GroupingRules groupingRules;
    private ConfigurationReader configurationReader;
    
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
    protected GroupingRules getGroupingRules() {
        return groupingRules;
    } // getGroupingRules
    
    @Override
    public void initialize() {
        groupingRules = new GroupingRules(groupingRulesFileName);
        configurationReader = new ConfigurationReader(30000);
        configurationReader.start();
    } // initialize
 
    @Override
    public Event intercept(Event event) {
        // get the original headers and body
        Map<String, String> headers = event.getHeaders();
        String body = new String(event.getBody());
        
        // get some original header values
        String fiwareServicePath = headers.get(Constants.HEADER_FIWARE_SERVICE_PATH);
        
        // parse the original body; this part may be unnecessary if notifications are parsed at the source only once
        // see --> https://github.com/telefonicaid/fiware-cygnus/issues/359
        NotifyContextRequest notification;
        Gson gson = new Gson();

        try {
            notification = gson.fromJson(body, NotifyContextRequest.class);
        } catch (Exception e) {
            LOGGER.error("Runtime error (" + e.getMessage() + ")");
            return null;
        } // try catch
        
        // iterate on the context responses and notified service paths
        ArrayList<String> defaultDestinations = new ArrayList<String>();
        ArrayList<String> groupedDestinations = new ArrayList<String>();
        ArrayList<String> groupedServicePaths = new ArrayList<String>();
        ArrayList<ContextElementResponse> contextResponses = notification.getContextResponses();
        
        if (contextResponses == null || contextResponses.isEmpty()) {
            LOGGER.warn("No context responses within the notified entity, nothing is done");
            return null;
        } // if
        
        String[] splitedNotifiedServicePaths = fiwareServicePath.split(",");
        
        for (int i = 0; i < contextResponses.size(); i++) {
            ContextElementResponse contextElementResponse = contextResponses.get(i);
            ContextElement contextElement = contextElementResponse.getContextElement();
            
            // get the matching rule
            GroupingRule matchingRule = groupingRules.getMatchingRule(contextElement, fiwareServicePath);
            
            if (matchingRule == null) {
                groupedDestinations.add(contextElement.getId() + "_" + contextElement.getType());
                groupedServicePaths.add(splitedNotifiedServicePaths[i]);
            } else {
                groupedDestinations.add((String) matchingRule.getDestination());
                groupedServicePaths.add((String) matchingRule.getNewFiwareServicePath());
            } // if else
            
            defaultDestinations.add(contextElement.getId() + "_" + contextElement.getType());
        } // for
        
        // set the final header values
        headers.put(Constants.FLUME_HEADER_NOTIFIED_ENTITIES,
                Utils.toString(defaultDestinations));
        headers.put(Constants.FLUME_HEADER_GROUPED_ENTITIES,
                Utils.toString(groupedDestinations));
        headers.put(Constants.FLUME_HEADER_GROUPED_SERVICE_PATHS,
                Utils.toString(groupedServicePaths));
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
        configurationReader.signalForStop();
        
        try {
            configurationReader.join();
        } catch (InterruptedException e) {
            LOGGER.error("There was a problem while joining the ConfigurationReader. Details: "
                    + e.getMessage());
        } // try catch
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
    
    /**
     * Class in charge or periodically reading the GroupingInterceptor configuration file.
     */
    private class ConfigurationReader extends Thread {
        
        private final CygnusLogger logger = new CygnusLogger(ConfigurationReader.class);
        private final int interval;
        private long lastModified;
        private boolean stop;
        
        public ConfigurationReader(int interval) {
            this.interval = interval;
            this.lastModified = 0;
            this.stop = false;
        } // ConfigurationReader
        
        @Override
        public void run() {
            while (!stop) {
                // check if the configuration has changed
                File groupingRulesFile = new File(groupingRulesFileName);
                long modified = groupingRulesFile.lastModified();

                if (lastModified != 0 && modified == lastModified) {
                    logger.debug("The configuration has not changed");
                    
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        logger.error("There was a problem with the checking interval. Details: "
                                + e.getMessage());
                    } // try catch

                    continue;
                } // if

                lastModified = modified;
                groupingRules = new GroupingRules(groupingRulesFileName);

                // sleep the configured interval of time
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    logger.error("There was a problem with the checking interval. Details: " + e.getMessage());
                } // try catch
            } // while
        } // run
        
        public void signalForStop() {
            this.stop = true;
        } // signalForStop
        
    } // ConfigurationReader
  
} // GroupingInterceptor
