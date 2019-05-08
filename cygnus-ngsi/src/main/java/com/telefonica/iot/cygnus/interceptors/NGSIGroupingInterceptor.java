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

import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonConstants;
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
    private final boolean enableEncoding;
    private ConfigurationReader configurationReader;
    private boolean invalidConfiguration = false;
    
    /**
     * Constructor.
     * @param groupingRulesFileName
     * @param enableEncoding
     * @param invalidConfiguration
     */
    public NGSIGroupingInterceptor(String groupingRulesFileName, boolean enableEncoding,
            boolean invalidConfiguration) {
        this.groupingRulesFileName = groupingRulesFileName;
        this.enableEncoding = enableEncoding;
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
        } // if
    } // initialize
    
    @Override
    public Event intercept(Event event) {
        if (invalidConfiguration) {
            return event;
        } // if
        
        LOGGER.debug("[gi] Event intercepted, id=" + event.hashCode());
        
        // Casting to NGSIEvent
        NGSIEvent ngsiEvent = (NGSIEvent) event;

        // Get the original headers and original ContextElement
        Map<String, String> headers = ngsiEvent.getHeaders();
        ContextElement originalCE = ngsiEvent.getOriginalCE();

        // Get some original header values
        String fiwareServicePath = headers.get(CommonConstants.HEADER_FIWARE_SERVICE_PATH);

        // Get the matching rule
        CygnusGroupingRule matchingRule = groupingRules.getMatchingRule(fiwareServicePath,
                originalCE.getId(), originalCE.getType());

        // Get values for headers added by this interceptor
        String notifiedEntity = originalCE.getId()
                + (enableEncoding ? CommonConstants.INTERNAL_CONCATENATOR : CommonConstants.OLD_CONCATENATOR)
                + originalCE.getType();
        String groupedDestination;
        String groupedDestinationByType;
        String groupedServicePath;
        
        if (matchingRule == null) {
            groupedDestination = notifiedEntity;
            groupedServicePath = fiwareServicePath;
            groupedDestinationByType = originalCE.getType();
        } else {
            LOGGER.debug("[gi] Rule Matched, ruleId = " + matchingRule.getId());
            groupedDestination = matchingRule.getDestination();
            groupedServicePath = matchingRule.getNewFiwareServicePath();
            groupedDestinationByType = matchingRule.getDestination();
        } // if else

        // Set the final header values
        headers.put(NGSIConstants.FLUME_HEADER_NOTIFIED_ENTITY, notifiedEntity);
        LOGGER.debug("[gi] Adding flume event header (" + NGSIConstants.FLUME_HEADER_NOTIFIED_ENTITY
                + ": " + notifiedEntity + ")");
        headers.put(NGSIConstants.FLUME_HEADER_GROUPED_ENTITY, groupedDestination);
        LOGGER.debug("[gi] Adding flume event header (" + NGSIConstants.FLUME_HEADER_GROUPED_ENTITY
                + ": " + groupedDestination + ")");
        headers.put(NGSIConstants.FLUME_HEADER_GROUPED_ENTITY_TYPE, groupedDestinationByType);
        LOGGER.debug("[gi] Adding flume event header (" + NGSIConstants.FLUME_HEADER_GROUPED_ENTITY_TYPE
                + ": " + groupedDestinationByType + ")");
        headers.put(NGSIConstants.FLUME_HEADER_GROUPED_SERVICE_PATH, groupedServicePath);
        LOGGER.debug("[gi] Adding flume event header (" + NGSIConstants.FLUME_HEADER_GROUPED_SERVICE_PATH
                + ": " + groupedServicePath + ")");
        
        // Return the intercepted getRecvTimeTs
        LOGGER.debug("[gi] Event put in the channel, id=" + event.hashCode());
        return ngsiEvent;
    } // intercept
 
    @Override
    public List<Event> intercept(List<Event> events) {
        if (invalidConfiguration) {
            return events;
        } else {
            List<Event> interceptedEvents = new ArrayList<>(events.size());
        
            for (Event event : events) {
                Event interceptedEvent = intercept(event);
                interceptedEvents.add(interceptedEvent);
            } // for
 
            return interceptedEvents;
        } // if else
    } // intercept
 
    @Override
    public void close() {
        if (!invalidConfiguration) {
            configurationReader.signalForStop();
        
            try {
                configurationReader.join();
            } catch (InterruptedException e) {
                LOGGER.error("There was a problem while joining the ConfigurationReader. Details: "
                        + e.getMessage());
            } // try catch
        } // if
    } // close
 
    /**
     * Builder class for this new interceptor.
     */
    public static class Builder implements Interceptor.Builder {
        private String groupingRulesFileName;
        private boolean enableEncoding;
        private boolean invalidConfiguration;
 
        @Override
        public void configure(Context context) {
            groupingRulesFileName = context.getString("grouping_rules_conf_file");
            
            if (groupingRulesFileName == null) {
                invalidConfiguration = true;
                LOGGER.debug("[gi] Invalid configuration (grouping_rules_conf_file = null) -- Must be configured");
            } else if (groupingRulesFileName.length() == 0) {
                invalidConfiguration = true;
                LOGGER.debug("[gi] Invalid configuration (grouping_rules_conf_file = ) -- Cannot be empty");
            } else {
                LOGGER.debug("[gi] Reading configuration (grouping_rules_conf_file=" + groupingRulesFileName + ")");
            } // if else

            String enableEncodingStr = context.getString("enable_encoding", "false");

            if (enableEncodingStr.equals("true") || enableEncodingStr.equals("false")) {
                enableEncoding = Boolean.valueOf(enableEncodingStr);
                LOGGER.debug("[gi] Reading configuration (enable_encoding="
                        + enableEncodingStr + ")");
            }  else {
                invalidConfiguration = true;
                LOGGER.debug("[gi] Invalid configuration (enable_encoding="
                        + enableEncodingStr + ") -- Must be 'true' or 'false'");
            }  // if else
        } // configure
 
        @Override
        public Interceptor build() {
            return new NGSIGroupingInterceptor(groupingRulesFileName, enableEncoding, invalidConfiguration);
        } // build
        
        protected boolean getEnableNewEncoding() {
            return enableEncoding;
        } // getEnableNewEncoding
        
        protected boolean getInvalidConfiguration() {
            return invalidConfiguration;
        } // getInvalidConfiguration
        
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
