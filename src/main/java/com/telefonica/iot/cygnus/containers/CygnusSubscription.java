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

package com.telefonica.iot.cygnus.containers;

import java.util.ArrayList;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.management.ManagementInterface;

/**
 *
 * @author pcoello25
 */
public class CygnusSubscription {
    
    private final OrionSubscription subscription;
    private final OrionEndpoint endpoint;
    private static final CygnusLogger LOGGER = new CygnusLogger(ManagementInterface.class);    
    
    public CygnusSubscription() {
        subscription = new OrionSubscription();
        endpoint = new OrionEndpoint();
    } // CygnusSubscription
    
    public OrionSubscription getOrionSubscription() {
        return subscription;
    } // getSubscription
    
    public OrionEndpoint getOrionEndpoint() {
        return endpoint;
    } // getEndpoint
    
    public class OrionSubscription {
        private final ArrayList<SubscriptionEntity> entities;
        private final ArrayList<String> attributes;
        private String reference;
        private String duration;
        private final ArrayList<SubscriptionConditions> notifyConditions;
        private String throttling;
        
        public OrionSubscription() {
            entities = new ArrayList<SubscriptionEntity>();
            attributes = new ArrayList<String>();
            notifyConditions = new ArrayList<SubscriptionConditions>();
        }
        
        public ArrayList<SubscriptionEntity> getSubscriptionEntity() {
            return entities;
        } // getSubscriptionEntity
        
        public ArrayList<String> getSubscriptionAtrributes() {
            return attributes;
        } // getSubscriptionAtrributes
        
        public ArrayList<SubscriptionConditions> getSubscriptionConditions() {
            return notifyConditions;
        } // getSubscriptionConditions
        
        public String getReference() {
            return reference;
        } // getReference
        
        public String getDuration() {
            return duration;
        } // getDuration
        
        public String getThrottling() {
            return throttling;
        } // getThrottling
        
        public int getSubscriptionSize() {
            int size = 0;
            size += this.getReference().length();
            size += this.getDuration().length();
            size += this.getThrottling().length();
            size += this.getSubscriptionEntity().size();
            size += this.getSubscriptionAtrributes().size();
            size += this.getSubscriptionConditions().size();
            return size;
        } // getSubscriptionSize
    }
    
    public class OrionEndpoint {
        private String host;
        private String port;
        private String ssl;
        private String xauthtoken;
        
        public OrionEndpoint() {
        } // endpoint
        
        public String getHost() {
            return host;
        } // gethost
        
        public String getPort() {
            return port;
        } // getport
        
        public String getSsl() {
            return ssl;
        } // getSsl
        
        public String getAuthToken() {
            return xauthtoken;
        } // getAuthToken
        
        public boolean hasAuthToken() {
            String token = this.getAuthToken();
            return (token != null);
        }
        
        public int getEndpointSize() {
            int size = 0;
            size += this.getHost().length();
            size += this.getPort().length();
            size += this.getSsl().length();
            if (this.hasAuthToken()) {
                size += this.getAuthToken().length();
            }
            return size;
        } // getEndpointSize
    }
    
    public class SubscriptionEntity {
        private String type;
        private String isPattern;
        private String id;
        
        public String getEntityType() {
            return type;
        } // getEntityType
        
        public String getPattern() {
            return isPattern;
        } // getPattern
        
        public String getId() {
            return id;
        } // getid
    }
    
    public class SubscriptionConditions {
        private String type;
        private ArrayList<String> condValues;
        
        public String getCondType() {
            return type;
        } // getType
        
        public ArrayList<String> getCondValues() {
            return condValues;
        } // getCondValues
    }
    
    /**
     * Checks if the given Gson has subscription and endpoint parameters.
     * @return True if the given Json is valid as grouping rule, otherwise false
     */
    public int isValid() {
        OrionSubscription orionSubscription = this.getOrionSubscription();
        OrionEndpoint orionEndpoint = this.getOrionEndpoint();

        int subscriptionMsg = isSubsciptionValid(orionSubscription);
        int endpointMsg = isEndpointValid(orionEndpoint);
                
        // check if endpoint or subscription is missing
        if ((subscriptionMsg == 1) || (endpointMsg == 1)) {
            LOGGER.debug("Subscription or endpoint are missing.");
            return 1;
        }
        
        // check if subscription and endpoint contain all the required fields
        if ((subscriptionMsg == 2) || (endpointMsg == 2)) {
            LOGGER.debug("There are missing fields in input JSON.");
            return 2;
        } // if
        
        // check if subscription and endpoint has any empty field
        if ((orionSubscription.getSubscriptionSize() == 0) || 
                (orionEndpoint.getEndpointSize() == 0) || 
                (subscriptionMsg == 3) || (endpointMsg == 3)) {
            LOGGER.debug("There are empty fields in input JSON.");
            return 3;
        } // if
        
        if (endpointMsg == 4) {
            LOGGER.debug("There are invalid fields in input JSON");
            return 4;
        }
                
        LOGGER.debug("Valid input JSON.");
        return 0;

    } // isValid
     
    public int isEndpointValid (OrionEndpoint orionEndpoint) {
        
        // get host, port, ssl and authtoken
        String host = orionEndpoint.getHost();
        String port = orionEndpoint.getPort();
        String ssl = orionEndpoint.getSsl();
        boolean isValidSsl;
        
        // check if entire endpoint is missing        
        if ((host == null) && (port == null) && (ssl == null)) {
            LOGGER.debug("Missing entire endpoint.");
            return 1;
        }
        
        // check if ssl field contains valid value
        if ((ssl.equals("true") || ssl.equals("false")) ? 
                (isValidSsl=true):(isValidSsl=false));  
        
        // check if endpoint contains all the required fields
        if ((host == null) || (port == null) || (ssl == null)) {
            LOGGER.debug("There are missing fields in endpoint.");
            return 2;
        } // if
        
        // check if endpoint has empty fields
        if ((host.length() == 0) || (port.length() == 0) || (ssl.length() == 0)) {
            LOGGER.debug("There are empty fields in endpoint.");
            return 3;
        } // if
        
        // check if endpoint contains invalid fields
        if (!(isValidSsl)) {
            LOGGER.debug("There are invalid fields in endpoint");
            return 4;
        }
                
        LOGGER.debug("Valid endpoint.");
        return 0;
    } // isEndpointValid
    
    public int isSubsciptionValid (OrionSubscription orionSubscription) {
        // get entities arrayList
        ArrayList<SubscriptionEntity> entity = 
                orionSubscription.getSubscriptionEntity();
        
        // get attributes arrayList
        ArrayList<String> attributes = 
                orionSubscription.getSubscriptionAtrributes();
        
        // get reference and duration
        String reference = orionSubscription.getReference();
        String duration = orionSubscription.getDuration();
        
        // get conditions arrayList
        ArrayList<SubscriptionConditions> notifyConditions = 
                orionSubscription.getSubscriptionConditions();
        
        // get throttling
        String throttling = orionSubscription.getThrottling();
        
        // check error messages from subfields of subscription
        int entitiesMsg = isEntitiesValid(entity);
        int notifyConditionsMsg = isNotifyConditionsValid(notifyConditions);
        
        // check if entire subscription is missing        
        if ((entitiesMsg == 1) && (attributes.isEmpty()) && (reference == null) && 
                (duration == null) && (notifyConditionsMsg == 1) && (throttling == null)) {
            LOGGER.debug("Missing entire subscription.");
            return 1;
        }
        
        // check if subscription contains all the required fields
        if ((reference == null) || (duration == null) || (throttling == null)
                || (entitiesMsg == 2) || (notifyConditionsMsg == 2)) {
            LOGGER.debug("There are missing fields in subscription.");
            return 2;
        }
        
        // check if subscription has any empty field
        if ((reference.length()==0) || (duration.length()==0) || (throttling.length()==0) || 
                (entitiesMsg==3) || (notifyConditionsMsg == 3)) {
            LOGGER.debug("There are empty fields in subscription.");
            return 3;
        }
        
        // return 0 if valid subscription
        LOGGER.debug("Valid subscription");
        return 0;
    } // isSubscriptionValid
    
    public int isEntitiesValid (ArrayList<SubscriptionEntity> entities) {
        boolean emptyFields = true;
        boolean validFields = true;
  
        // check if input ArrayList is empty        
        if (entities.isEmpty()) {
            LOGGER.debug("Missing entire entities");
            return 1;
        } // if
        
        for (SubscriptionEntity entity : entities) {
            String type = entity.getEntityType();
            String isPattern = entity.getPattern();
            String id = entity.getId();
            
            validFields &= ((type != null) && (isPattern != null) && (id != null));
            emptyFields &= validFields && ((type.length() == 0) || (isPattern.length() == 0) || 
                    (id.length() == 0));
            
        } // for
        
        // check if entities contains all the required fields
        if (!validFields) {
            LOGGER.debug("There are missing fields in entities.");
            return 2;
        } // if
        
        // check if entities has any empty field
        if (emptyFields) {
            LOGGER.debug("There are empty fields in entities.");
            return 3;
        } // if
        
        LOGGER.debug("Valid entities.");
        return 0;
    } // isEntitiesValid
    
    public int isNotifyConditionsValid (ArrayList<SubscriptionConditions> conditions) {
        
        boolean validFields = true;
        boolean emptyFields = true;
                
        // check if input ArrayList is empty
        if (conditions.isEmpty()) {
            LOGGER.debug("Missing entire notifyConditions");
            return 1;
        } // if
        
        for (SubscriptionConditions condition : conditions) {
            String type = condition.getCondType();
            ArrayList<String> values = condition.getCondValues();
            validFields &= ((type != null) && (values != null));
            emptyFields &= validFields && (type.length() == 0);
        } // for
        
        // check if notifyConditions contains all the required fields
        if (!validFields) {
           LOGGER.debug("There are missing fields in notifyConditions.");
           return 2;
        } // if
        
        // check if notifyConditions has any empty field
        if (emptyFields) {
            LOGGER.debug("There are empty fields in notifyConditions.");
            return 3;
        } // if
        
        LOGGER.debug("Valid notifyConditions.");
        return 0;
    } // isNotifyConditionsValid
    
    public static String toString (CygnusSubscription cygnusSubscription) {
        OrionSubscription subs = cygnusSubscription.getOrionSubscription();
        int size;
        int index = 0;
        String subscription = ""
                + "{"
                + "\"entities\": [";

        ArrayList<SubscriptionEntity> entities = subs.getSubscriptionEntity();
        size = entities.size();
        
        for (SubscriptionEntity entity : entities) {
            subscription += "{"
                + "\"type\":\"" + entity.getEntityType() + "\","
                + "\"isPattern\":\"" + entity.getPattern() + "\","
                + "\"id\":\"" + entity.getId() + "\"";
            
            if (index == (size - 1)) {
                subscription += "}";
            } else {
                subscription += "},";
            } // if else
            
            index += 1;
        }  // for
         
        subscription += "], "
                + "\"attributes\": [";

        ArrayList<String> attributes = subs.getSubscriptionAtrributes();
        size = attributes.size();
        index = 0;
        
        for (String attribute : attributes) {
            
            if (index == (size - 1)) {
                subscription += "\"" + attribute + "\"";
            } else {
                subscription += "\"" + attribute + "\",";
            } // if else
            
            index += 1;
        } // for
        
        subscription += "],"
                + "\"reference\":\"" + subs.getReference() + "\","
                + "\"duration\":\"" + subs.getDuration() + "\","
                + "\"notifyConditions\":[";

        ArrayList<SubscriptionConditions> conditions = subs.getSubscriptionConditions();
        index = 0;
        size = conditions.size();
        
        for (SubscriptionConditions condition : conditions) {
            subscription += "{"
                + "\"type\":\"" + condition.getCondType() + "\","
                + "\"condValues\": [";
            ArrayList<String> condValues = condition.getCondValues();
            int indexCond = 0;
            for (String condValue : condValues) {
                
                if (indexCond == (condValues.size()-1)) {
                    subscription += "\"" + condValue + "\"";
                } else {
                    subscription += "\"" + condValue + "\",";
                } // if else
                
                indexCond += 1;
            } // for 
            
            subscription += "]";
            
            if (index == (size - 1)) {
                subscription += "}";
            } else {
                subscription += "},";
            } // if else
            
        } // for              
        
        subscription += "],"
                + "\"throttling\":\"" + subs.getThrottling() + "\""
                + "}";
        
        return subscription;
    } // toString
    
}