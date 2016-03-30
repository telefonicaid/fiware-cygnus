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

package com.telefonica.iot.cygnus.containers;

import com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import java.util.ArrayList;
import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 *
 * @author pcoello25
 */
public class CygnusSubscription {
    
    private final OrionSubscription subscription;
    private final OrionEndpoint endpoint;
    private static final CygnusLogger LOGGER = new CygnusLogger(CygnusSubscription.class);    
    
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
        } // OrionSubscription
        
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
        
        public boolean isSubscriptionEmpty() {
            return ((this.entities.isEmpty()) && (this.attributes.isEmpty()) && 
                   (this.notifyConditions.isEmpty()) && (this.reference == null)
                   && (this.duration == null) && (this.throttling == null));
        } // isSubscriptionEmpty
        
    } // OrionSubscription
    
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
        } // hasAuthToken
        
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
        
        public boolean isEndpointEmpty() {
            return ((this.host == null) && (this.port == null) && 
                    (this.ssl == null) && (this.xauthtoken == null));
        } // isEndpointEmpty
         
        public String toStrain() {
            String out = " host = " + this.host;
            out += "  port = " + this.port;
            out += "  ssl = " + this.ssl;
            out += "  token = " + this.xauthtoken;
            return out;
        }
         
    } // OrionEndpoint
    
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
        
        public boolean isEntitiesEmpty() {
            return ((type == null) && (isPattern == null) && (id == null));
        }
    } // SubscriptionEntity
    
    public class SubscriptionConditions {
        private String type;
        private ArrayList<String> condValues;
        
        public String getCondType() {
            return type;
        } // getType
        
        public ArrayList<String> getCondValues() {
            return condValues;
        } // getCondValues
        
        public boolean isConditionsEmpty() {
            return ((type == null) && (condValues == null));
        }
    } // SubscriptionConditions
    
    /**
     * Checks if the given Gson has subscription and endpoint parameters.
     * @return True if the given Json is valid as grouping rule, otherwise false
     */
    public int isValid() {
        OrionSubscription orionSubscription = this.getOrionSubscription();
        OrionEndpoint orionEndpoint = this.getOrionEndpoint();
        
        // Error codes: 
        //      - First digit: 1 if subscription, 2 if endpoint
        //      - Second digit: 1 if missing entire field, 2 if missing fields, 
        //                      3 if empty fields, 4 if invalid fields
        //      - Third digit: different cases
        //  Example: 125 =  Subscription/MissingFields/notifyConditionsField
        
        int subscriptionMsg = isSubscriptionValid(orionSubscription);  
        int endpointMsg = isEndpointValid(orionEndpoint);
        
        switch (subscriptionMsg) {
            // case of missing entire subscription
            case 11:
                LOGGER.error("Subscription is missing");
                return 11;
                
            // cases of missing fields in subscription
                
            // cases for entities
            case 12111:
                LOGGER.error("Field 'entities' is missing in the subscription");
                return 12111;
            case 12112:
                LOGGER.error("Field 'entities' is empty");
                return 12112;
            case 1212:
                LOGGER.error("Field 'entities' has missing fields ");
                return 1212;
            case 1213:
                LOGGER.error("Field 'entities' has empty fields");
                return 1213;
                
            case 122:
                LOGGER.error("Field 'reference' is missing in the subscription");
                return 122;
            case 123:
                LOGGER.error("Field 'duration' is missing in the subscription");
                return 123;
                
            // cases for notifyConditions
            case 12411:
                LOGGER.error("Field 'notifyConditions' is missing in the subscription");
                return 12411;
            case 12412:
                LOGGER.error("Field 'notifyConditions' is empty");
                return 12412;
            case 1242:
                LOGGER.error("Field 'notifyConditions' has missing fields in the subcription");
                return 1242;
            case 1243:
                LOGGER.error("Field 'notifyConditions' has emtpy fields in the subcription");
                return 1243;
                
            case 125: 
                LOGGER.error("Field 'throttling' is missing in the subscription");
                return 125;
            case 126:
                LOGGER.error("Field 'attributes' is missing in the subscription");
                return 126;
                
            // cases of empty fields in subscription
            case 131:
                LOGGER.error("Field 'entity' is empty in the subscription");
                return 131;
            case 132:
                LOGGER.error("Field 'reference' is empty in the subscription");
                return 132;
            case 133:
                LOGGER.error("Field 'duration' is empty in the subscription");
                return 133;
            case 134:
                LOGGER.error("Field 'notifyConditions' has empty fields in the subscription");
                return 134;
            case 135:
                LOGGER.error("Field 'throttling' is empty in the subscription");
                return 135;
            default:
        }
        
        switch (endpointMsg) {
            // case of missing entire endpoint
            case 21:
                LOGGER.error("Endpoint is missing");
                return 21;
                
            // cases of missing fields in endpoint
            case 221:
                LOGGER.error("Field 'host' is missing in the endpoint");
                return 221;
            case 222:
                LOGGER.error("Field 'port' is missing in the endpoint");
                return 222;
            case 223:
                LOGGER.error("Field 'ssl' is missing in the endpoint");
                return 223;
                
            // cases of empty fields in endpoint
            case 231:
                LOGGER.error("Field 'host' is empty in the endpoint");
                return 231;
            case 232:
                LOGGER.error("Field 'port' is empty in the endpoint");
                return 232;
            case 233:
                LOGGER.error("Field 'ssl' is empty in the endpoint");
                return 233;
                
            // case of invalid field in the endpoint
            case 24:
                LOGGER.error("Field 'ssl' is invalid");
                return 24;
            default:
        } // switch
      
        LOGGER.debug("Valid input JSON.");
        return 0;

    } // isValid
    
    private int isSubscriptionValid (OrionSubscription orionSubscription) {
        // get entities arrayList
        ArrayList<SubscriptionEntity> entity = 
                orionSubscription.getSubscriptionEntity();
        
        // get attributes arrayList
        ArrayList<String> attributes = 
                orionSubscription.getSubscriptionAtrributes();
        
        // get conditions arrayList
        ArrayList<SubscriptionConditions> notifyConditions = 
                orionSubscription.getSubscriptionConditions();
        
        // get throttling,reference and duration
        String reference = orionSubscription.getReference();
        String duration = orionSubscription.getDuration();
        String throttling = orionSubscription.getThrottling();
        
        // check error messages from subfields of subscription 
        int entitiesMsg = isEntitiesValid(entity);
        int notifyConditionsMsg = isNotifyConditionsValid(notifyConditions);

        // check if entire subscription is missing        
        if ((entitiesMsg == 11) && (reference == null) && 
                (duration == null) && (notifyConditionsMsg == 11) 
                && (throttling == null)) {
            LOGGER.error("Missing subscription.");
            return 11;
        } // if
                
        // check if subscription contains entities
        if (entitiesMsg == 11) {
            LOGGER.error("Field 'entities' is missing in the subscription");
            return 12111;
        } // if
                   
        // check if subscription has an empty entities
        if (entitiesMsg == 12) {
            LOGGER.error("Field 'entities' is empty");
            return 12112;
        }
        
        // check if subscription.entities has missing fields
        if (entitiesMsg == 2) {
            LOGGER.error("Field 'entities' has missing fields");
            return 1212;
        } // if
        
        // check if subscription.entities has empty fields
        if (entitiesMsg == 3) {
            LOGGER.error("Field 'entities' has empty fields");
            return 1213;
        } // if
        
        // check if subscription contains reference
        if (reference == null) {
            LOGGER.error("Field 'reference' is missing in the subscription");
            return 122;
        } // if
        
        // check if subscription contains duration
        if (duration == null) {
            LOGGER.error("Field 'duration' is missing in the subscription");
            return 123;
        } // if
        
        // check if subscription contains notifyConditions
        if (notifyConditionsMsg == 11) {
            LOGGER.error("Field 'notifyConditions' is missing in the subscription");
            return 12411;
        } // if 
        
        // check if subscription contains notifyConditions
        if (notifyConditionsMsg == 12) {
            LOGGER.error("Field 'notifyConditions' is empty");
            return 12412;
        } // if         
         
        // check if subscription.notifyConditions has missing fields
        if (notifyConditionsMsg == 2) {
            LOGGER.error("Field 'notifyConditions' has missing fields");
            return 1242;
        } // if 
        
        // check if subscription.notifyConditions has empty fields
        if (notifyConditionsMsg == 3) {
            LOGGER.error("Field 'notifyConditions' has empty fields");
            return 1243;
        } // if 
        
        // check if subscription contains throttlings
        if (throttling == null) {
            LOGGER.error("Field 'throttling' is missing in the subscription");
            return 125;
        } // if
        
        // check if subscription contains attributes (with values or empty)
        if (attributes == null) {
            LOGGER.error("Field 'attributes' is missing in the subscription");
            return 126;
        } // if
      
        // check if subscription has an empty entity
        if (entitiesMsg==3) {
            LOGGER.error("Field 'entity' is empty in the subscription");
            return 131;
        } // if        
        
        // check if subscription has an empty reference
        if (reference.length()==0) {
            LOGGER.error("Field 'reference' is empty in the subscription");
            return 132;
        } // if
        
        // check if subscription has an empty duration
        if (duration.length()==0) {
            LOGGER.error("Field 'duration' is empty in the subscription");
            return 133;
        } // if
                
        // check if subscription has an empty notifyConditions
        if (notifyConditionsMsg == 3) {
            LOGGER.error("Field 'notifyConditions' is empty in the subscription");
            return 134;
        } // if
        
        // check if subscription has an empty throttling
        if (throttling.length()==0) {
            LOGGER.error("Field 'throttling' is empty in the subscription");
            return 135;
        } // if
        
        // return 0 if valid subscription
        LOGGER.debug("Valid subscription");
        return 0;
    } // isSubscriptionValid
    
    private int isEntitiesValid (ArrayList<SubscriptionEntity> entities) {
        boolean emptyFields = true;
        boolean validFields = true;
        
        if (entities == null) {
            LOGGER.error("Field 'entities' is missing");
            return 11;
        } // if
        
        if (entities.isEmpty()) {
            LOGGER.error("Field 'entities' is empty");
            return 12;
        }
        
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
            LOGGER.error("There are missing fields in entities.");
            return 2;
        } // if
        
        // check if entities has any empty field
        if (emptyFields) {
            LOGGER.error("There are empty fields in entities.");
            return 3;
        } // if
        
        LOGGER.debug("Valid entities.");
        return 0;
    } // isEntitiesValid
    
    private int isNotifyConditionsValid (ArrayList<SubscriptionConditions> conditions) {
        
        boolean validFields = true;
        boolean emptyFields = true;
                
        if (conditions == null) {
            LOGGER.error("Field 'notifyConditions' is missing");
            return 11;
        } // if
        
        if (conditions.isEmpty()) {
            LOGGER.error("Field 'notifyConditions' is empty");
            return 12;
        }
        
        for (SubscriptionConditions condition : conditions) {
            
            String type = condition.getCondType();
            ArrayList<String> values = condition.getCondValues();
            validFields &= ((type != null) && (values != null));
            emptyFields &= validFields && (type.length() == 0);
        } // for
        
        // check if notifyConditions contains all the required fields
        if (!validFields) {
           LOGGER.error("There are missing fields in notifyConditions.");
           return 2;
        } // if
        
        // check if notifyConditions has any empty field
        if (emptyFields) {
            LOGGER.error("There are empty fields in notifyConditions.");
            return 3;
        } // if
        
        LOGGER.debug("Valid notifyConditions.");
        return 0;
    } // isNotifyConditionsValid  
    
    private int isEndpointValid (OrionEndpoint orionEndpoint) {
        
        // get host, port and ssl
        String host = orionEndpoint.getHost();
        String port = orionEndpoint.getPort();
        String ssl = orionEndpoint.getSsl();
        boolean isValidSsl;
        
        // check if entire endpoint is missing        
        if ((host == null) && (port == null) && (ssl == null)) {
            LOGGER.error("Missing endpoint.");
            return 21;
        } // if
        
        // check if endpoint contains ssl
        if (ssl == null) {
            LOGGER.error("Field 'ssl' is missing in the endpoint");
            return 223;
        } else if ((ssl.equals("true") || ssl.equals("false")) ? 
                (isValidSsl=true):(isValidSsl=false));  
        // if else
        
        // check if endpoint contains host
        if (host == null) {
            LOGGER.error("Field 'host' is missing in the endpoint");
            return 221;
        } // if
        
        // check if endpoint contains port 
        if (port == null) {
            LOGGER.error("Field 'port' is missing in the endpoint");
            return 222;
        } // if
        
        // check if endpoint has an empty host
        if (host.length() == 0) {
            LOGGER.error("Field 'host' is empty");
            return 231;
        } // if
        
        // check if endpoint has an empty port
        if (port.length() == 0) {
            LOGGER.error("Field 'port' is empty");
            return 232;
        } // if
        
        // check if endpoint has an empty ssl
        if (ssl.length() == 0) {
            LOGGER.error("Field 'ssl' is empty");
            return 233;
        } // if
        
        // check if endpoint contains invalid fields
        if (!(isValidSsl)) {
            LOGGER.error("There are invalid fields in endpoint");
            return 24;
        } // if
                
        LOGGER.debug("Valid endpoint.");
        return 0;
    } // isEndpointValid
    
} // CygnusSubscription
