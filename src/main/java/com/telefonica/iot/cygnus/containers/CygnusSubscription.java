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
        
        public OrionEndpoint() {
        } // endpoint
        
        public String getHost() {
            return host;
        } // gethost
        
        public String getPort() {
            return port;
        } // getport
        
        public int getSubscriptionSize() {
            int size = 0;
            size += this.getHost().length();
            size += this.getPort().length();
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
                
        int subscriptionMsg = isSubsciptionValid(orionSubscription, true);
        int endpointMsg = isEndpointValid(orionEndpoint, true);
        
        // check if subscription contains all the required fields
        if ((orionSubscription == null) || (orionEndpoint == null) || (subscriptionMsg == 1) || 
                (endpointMsg == 1)) {
            LOGGER.debug("There are missing fields in input JSON.");
            return 1;
        } // if
        
        // check if subscription has any empty field
        if ((orionSubscription.getSubscriptionSize() == 0) || 
                (orionEndpoint.getSubscriptionSize() == 0) || 
                (subscriptionMsg == 2) || (endpointMsg == 2)) {
            LOGGER.debug("There are empty fields in input JSON.");
            return 2;
        } // if
        
        // // TO DO: CHECK EXTRA FIELDS
        
        LOGGER.debug("Valid JSON");
        return 0;

    } // isValid
    
    public int isSubsciptionValid (OrionSubscription orionSubscription, boolean checkExtraFields) {
        ArrayList<SubscriptionEntity> entity = 
                orionSubscription.getSubscriptionEntity();
        ArrayList<String> attributes = 
                orionSubscription.getSubscriptionAtrributes();
        String reference = orionSubscription.getReference();
        String duration = orionSubscription.getDuration();
        ArrayList<SubscriptionConditions> notifyConditions = 
                orionSubscription.getSubscriptionConditions();
        String throttling = orionSubscription.getThrottling();
        
        int entitiesMsg = isEntitiesValid(entity, true);
        int notifyConditionsMsg = isNotifyConditionsValid(notifyConditions, true);
        
        if ((reference == null) || (duration == null) || (throttling == null)
                || (entitiesMsg == 1) || (notifyConditionsMsg == 1)) {
            LOGGER.debug("There are missing fields in 'subscription' fields of input JSON");
            return 1;
        }
        
        if ((reference.length()==0) || (duration.length()==0) || (throttling.length()==0) || 
                (entitiesMsg==2) || (notifyConditionsMsg == 2)) {
            LOGGER.debug("There are empty fields in 'subscription' of input JSON");
            return 2;
        }
        
        LOGGER.debug("Valid field 'subscription'");
        return 0;
    }
    
    public int isEndpointValid (OrionEndpoint orionEndpoint, boolean checkExtraFields) {
        String host = orionEndpoint.getHost();
        String port = orionEndpoint.getPort();
        
        // check if subscription contains all the required fields
        if ((host == null) || (port == null)) {
            LOGGER.debug("There are missing fields in 'endpoint' of input JSON");
            return 1;
        } // if
        
        // check if subscription has empty fields
        if ((host.length() == 0) || (port.length() == 0)) {
            LOGGER.debug("There are empty fields in 'endpoint' of input JSON");
            return 2;
        } // if
        
        // TO DO: CHECK EXTRA FIELDS
        
        LOGGER.debug("Valid field 'endpoint'");
        return 0;
    }
    
    public int isEntitiesValid (ArrayList<SubscriptionEntity> entities, 
            boolean checkExtraFields) {
        boolean emptyFields = true;
        boolean validFields = true;
        
        for (SubscriptionEntity entity : entities) {
            String type = entity.getEntityType();
            String isPattern = entity.getPattern();
            String id = entity.getId();
            
            validFields &= ((type != null) || (isPattern != null) || (id != null));
            emptyFields &= validFields && ((type.length()== 0) || (isPattern.length() == 0) || 
                    (id.length() == 0));
        }
        
        if (!validFields) {
            LOGGER.debug("There are missing fields in 'entities' of input JSON");
            return 1;
        }
        
        if (emptyFields) {
            LOGGER.debug("There are empty fields in 'entities' of input JSON");
            return 2;
        }
        
        LOGGER.debug("Valid field 'entities'");
        return 0;
    }
    
    public int isNotifyConditionsValid (ArrayList<SubscriptionConditions> conditions, 
            boolean checkExtraFields) {
        boolean validFields = true;
        boolean emptyFields = true;
                
        for (SubscriptionConditions condition : conditions) {
            String type = condition.getCondType();
            ArrayList<String> values = condition.getCondValues();
            validFields &= ((type != null) || (values != null));
            emptyFields &= validFields && (type.length() == 0);
        } // for
        
        // check if there are missing fields
        if (!validFields) {
           LOGGER.debug("There are missing fields in 'notifyConditions' of input JSON");
           return 1;
        }
        
        // check if there are empty fields
        if (emptyFields) {
            LOGGER.debug("There are empty fields in 'notifyConditions' of input JSON");
            return 2;
        }
        
        LOGGER.debug("Valid field 'notifyConditions'");
        return 0;
    }
    
    public static String toString (CygnusSubscription cygnusSubscription) {
        OrionSubscription subs = cygnusSubscription.getOrionSubscription();
        int counter = 0;
        String subscription = ""
                + "{"
                + "\"entities\": [";
        // FOR ENTITIES
        ArrayList<SubscriptionEntity> entities = subs.getSubscriptionEntity();
        for (SubscriptionEntity entity : entities) {
            subscription += "{"
                + "\"type\":\"" + entity.getEntityType() + "\","
                + "\"isPattern\":\"" + entity.getPattern() + "\","
                + "\"id\":\"" + entity.getId() + "\"";
            if (counter == (entities.size()-1)) {
                subscription += "}";
            } else {
                subscription += "},";
            }
            counter += 1;
        } 
         
        subscription += "]," 
                + "\"attributes\": [";

        ArrayList<String> attributes = subs.getSubscriptionAtrributes();
        int size = attributes.size();
        
        for (String attribute : attributes) {
            if (counter == (size-1)) {
                subscription += "\"" + attribute + "\"";
            } else {
                subscription += "\"" + attribute + "\",";
            }
            counter += 1;
        } // for
        
        subscription += "],"
                + "\"reference\":\"" + subs.getReference() + "\","
                + "\"duration\":\"" + subs.getDuration() + "\","
                + "\"notifyConditions\":[";
        // FOR CONDVALUES
        ArrayList<SubscriptionConditions> conditions = subs.getSubscriptionConditions();
        counter = 0;
        
        for (SubscriptionConditions condition : conditions) {
            subscription += "{"
                + "\"type\":\"" + condition.getCondType() + "\","
                + "\"condValues\": [";
            ArrayList<String> condValues = condition.getCondValues();
            int c = 0;
            for (String condValue : condValues) {
                if (c == (condValues.size()-1)) {
                    subscription += "\"" + condValue + "\"";
                } else {
                    subscription += "\"" + condValue + "\",";
                }
                c += 1;
            }
            subscription += "]";
            if (counter == (conditions.size()-1)) {
                subscription += "}";
            } else {
                subscription += "},";
            }
        }              
        subscription += "],"
                + "\"throttling\":\"" + subs.getThrottling() + "\""
                + "}";
        return subscription;
    }
    
}