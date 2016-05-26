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
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
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
public class NGSIGroupingInterceptor implements Interceptor {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIGroupingInterceptor.class);
    private final String groupingRulesFileName;
    private CygnusGroupingRules groupingRules;
    private ConfigurationReader configurationReader;
    private Boolean invalidConfiguration = false;
    
    /**
     * Constructor.
     * @param groupingRulesFileName
     * @param invalidConfiguration
     */
    public NGSIGroupingInterceptor(String groupingRulesFileName, Boolean invalidConfiguration) {
        this.groupingRulesFileName = groupingRulesFileName;
        this.invalidConfiguration = invalidConfiguration;
    } // NGSIGroupingInterceptor
    
    /**
     * Gets the grouping rules. This is protected since it is only going to be used in the tests.
     * @return
     */
    protected CygnusGroupingRules getGroupingRules() {
        return groupingRules;
    } // getGroupingRules
    
    @Override
    public void initialize() {
        if (!invalidConfiguration) {
            groupingRules = new CygnusGroupingRules(groupingRulesFileName);
            configurationReader = new ConfigurationReader(30000);
            configurationReader.start();
        }
    } // initialize
    
    @Override
    public Event intercept(Event event) {
        
        if (invalidConfiguration) {
            return event;
        } else {
            LOGGER.debug("Event intercepted, id=" + event.hashCode());

            // get the original headers and body
            Map<String, String> headers = event.getHeaders();
            String body = new String(event.getBody());

            // get some original header values
            String fiwareServicePath = headers.get(CommonConstants.HEADER_FIWARE_SERVICE_PATH);

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
            ArrayList<NotifyContextRequest.ContextElementResponse> contextResponses = notification.getContextResponses();

            if (contextResponses == null || contextResponses.isEmpty()) {
                LOGGER.warn("No context responses within the notified entity, nothing is done");
                return null;
            } // if

            String[] splitedNotifiedServicePaths = fiwareServicePath.split(",");

            for (int i = 0; i < contextResponses.size(); i++) {
                NotifyContextRequest.ContextElementResponse contextElementResponse = contextResponses.get(i);
                NotifyContextRequest.ContextElement contextElement = contextElementResponse.getContextElement();

                // get the matching rule
                CygnusGroupingRule matchingRule = groupingRules.getMatchingRule(fiwareServicePath, contextElement.getId(),
                        contextElement.getType());

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
            String defaultDestinationsStr = CommonUtils.toString(defaultDestinations);
            headers.put(NGSIConstants.FLUME_HEADER_NOTIFIED_ENTITIES, defaultDestinationsStr);
            LOGGER.debug("Adding flume event header (name=" + NGSIConstants.FLUME_HEADER_NOTIFIED_ENTITIES
                    + ", value=" + defaultDestinationsStr + ")");
            String groupedDestinationsStr = CommonUtils.toString(groupedDestinations);
            headers.put(NGSIConstants.FLUME_HEADER_GROUPED_ENTITIES, groupedDestinationsStr);
            LOGGER.debug("Adding flume event header (name=" + NGSIConstants.FLUME_HEADER_GROUPED_ENTITIES
                    + ", value=" + groupedDestinationsStr + ")");
            String groupedServicePathsStr = CommonUtils.toString(groupedServicePaths);
            headers.put(NGSIConstants.FLUME_HEADER_GROUPED_SERVICE_PATHS, groupedServicePathsStr);
            LOGGER.debug("Adding flume event header (name=" + NGSIConstants.FLUME_HEADER_GROUPED_SERVICE_PATHS
                    + ", value=" + groupedServicePathsStr + ")");
            event.setHeaders(headers);
            LOGGER.debug("Event put in the channel, id=" + event.hashCode());
            return event;
        } // if else
        
    } // intercept
 
    @Override
    public List<Event> intercept(List<Event> events) {

        if (invalidConfiguration) {
            return events;
        } else {
            List<Event> interceptedEvents = new ArrayList<Event>(events.size());
        
            for (Event event : events) {
                Event interceptedEvent = intercept(event);
                interceptedEvents.add(interceptedEvent);
            } // for
 
            return interceptedEvents;
        } // if else
        
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
        private Boolean invalidConfiguration;
 
        @Override
        public void configure(Context context) {
            String groupingRulesFileNameTmp = context.getString("grouping_rules_conf_file");
            
            if (groupingRulesFileNameTmp != null && groupingRulesFileNameTmp.length() > 0) {
                groupingRulesFileName = groupingRulesFileNameTmp;
                LOGGER.debug("[gi] Reading configuration (grouping_rules_file=" + groupingRulesFileName + ")");
            } else {
                invalidConfiguration = true;
                LOGGER.debug("[gi] Grouping rules file cannot be empty");
            } // if else
        } // configure
 
        @Override
        public Interceptor build() {
            return new NGSIGroupingInterceptor(groupingRulesFileName, invalidConfiguration);
        } // build
    } // Builder
    
    /**
     * Class in charge or periodically reading the NGSIGroupingInterceptor configuration file.
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
                groupingRules = new CygnusGroupingRules(groupingRulesFileName);

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
  
} // NGSIGroupingInterceptor
