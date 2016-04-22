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

import java.util.ArrayList;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.containers.OrionEndpoint;

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
        private ArrayList<SubscriptionEntity> entities;
        private ArrayList<String> attributes;
        private String reference;
        private String duration;
        private ArrayList<SubscriptionConditions> notifyConditions;
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
        
        public int isValid () {
            // get entities arrayList
             entities = this.getSubscriptionEntity();

            // get attributes arrayList
            attributes = this.getSubscriptionAtrributes();

            // get conditions arrayList
            notifyConditions = this.getSubscriptionConditions();

            // get throttling,reference and duration
            reference = this.getReference();
            duration = this.getDuration();
            throttling = this.getThrottling();

            // check error messages from subfields of subscription 
            int entitiesMsg = isEntitiesValid(entities);
            int notifyConditionsMsg = isNotifyConditionsValid(notifyConditions);

            // check if entire subscription is missing        
            if ((entitiesMsg == 1) && (reference == null) && 
                    (duration == null) && (notifyConditionsMsg == 1) 
                    && (throttling == null)) {
                LOGGER.debug("Missing subscription in the request");
                return 11;
            } // if

            // check if subscription contains entities
            if (entitiesMsg == 1) {
                LOGGER.debug("Field 'entities' is missing in the subscription");
                return 1211;
            } // if

            // check if subscription.entities has missing fields
            if (entitiesMsg == 2) {
                LOGGER.debug("Field 'entities' has missing fields in the subscription");
                return 1212;
            } // if

            // check if subscription.entities has empty fields
            if (entitiesMsg == 3) {
                LOGGER.debug("Field 'entities' has empty fields in the subscription");
                return 1213;
            } // if

            // check if subscription contains reference
            if (reference == null) {
                LOGGER.debug("Field 'reference' is missing in the subscription");
                return 122;
            } // if

            // check if subscription contains duration
            if (duration == null) {
                LOGGER.debug("Field 'duration' is missing in the subscription");
                return 123;
            } // if

            // check if subscription contains notifyConditions
            if (notifyConditionsMsg == 1) {
                LOGGER.debug("Field 'notifyConditions' is missing in the subscription");
                return 1241;
            } // if         

            // check if subscription.notifyConditions has missing fields
            if (notifyConditionsMsg == 2) {
                LOGGER.debug("Field 'notifyConditions' has missing fields in the subscription");
                return 1242;
            } // if 

            // check if subscription.notifyConditions has empty fields
            if (notifyConditionsMsg == 3) {
                LOGGER.debug("Field 'notifyConditions' has empty fields in the subscription");
                return 1243;
            } // if 

            // check if subscription contains throttlings
            if (throttling == null) {
                LOGGER.debug("Field 'throttling' is missing in the subscription");
                return 125;
            } // if

            // check if subscription contains attributes (with values or empty)
            if (attributes == null) {
                LOGGER.debug("Field 'attributes' is missing in the subscription");
                return 126;
            } // if

            // check if subscription has an empty entity
            if (entitiesMsg==3) {
                LOGGER.debug("Field 'entity' is empty in the subscription");
                return 131;
            } // if        

            // check if subscription has an empty reference
            if (reference.length()==0) {
                LOGGER.debug("Field 'reference' is empty in the subscription");
                return 132;
            } // if

            // check if subscription has an empty duration
            if (duration.length()==0) {
                LOGGER.debug("Field 'duration' is empty in the subscription");
                return 133;
            } // if

            // check if subscription has an empty notifyConditions
            if (notifyConditionsMsg == 3) {
                LOGGER.debug("Field 'notifyConditions' is empty in the subscription");
                return 134;
            } // if

            // check if subscription has an empty throttling
            if (throttling.length()==0) {
                LOGGER.debug("Field 'throttling' is empty in the subscription");
                return 135;
            } // if

            // return 0 if valid subscription
            LOGGER.debug("Valid subscription");
            return 0;
        } // isValid
        
    } // OrionSubscription
    
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
        //      - Rest of digits: different elements into the case
        //  Example: 125 =  Subscription/MissingFields/notifyConditionsField
        
        int subscriptionMsg = orionSubscription.isValid();  
        int endpointMsg = orionEndpoint.isValid();
        
        switch (subscriptionMsg) {
            // case of missing entire subscription
            case 11:
                LOGGER.debug("Subscription is missing");
                return 11;
                
            // cases of missing fields in subscription 
            // cases for entities
            case 1211:
                LOGGER.debug("Field 'entities' is missing in the subscription");
                return 1211;
            case 1212:
                LOGGER.debug("Field 'entities' has missing fields ");
                return 1212;
            case 1213:
                LOGGER.debug("Field 'entities' has empty fields");
                return 1213;
                
            case 122:
                LOGGER.debug("Field 'reference' is missing in the subscription");
                return 122;
            case 123:
                LOGGER.debug("Field 'duration' is missing in the subscription");
                return 123;
                
            // cases for notifyConditions
            case 1241:
                LOGGER.debug("Field 'notifyConditions' is missing in the subscription");
                return 1241;
            case 1242:
                LOGGER.debug("Field 'notifyConditions' has missing fields in the subcription");
                return 1242;
            case 1243:
                LOGGER.debug("Field 'notifyConditions' has emtpy fields in the subcription");
                return 1243;
                
            case 125: 
                LOGGER.debug("Field 'throttling' is missing in the subscription");
                return 125;
            case 126:
                LOGGER.debug("Field 'attributes' is missing in the subscription");
                return 126;
                
            // cases of empty fields in subscription
            case 131:
                LOGGER.debug("Field 'entity' is empty in the subscription");
                return 131;
            case 132:
                LOGGER.debug("Field 'reference' is empty in the subscription");
                return 132;
            case 133:
                LOGGER.debug("Field 'duration' is empty in the subscription");
                return 133;
            case 134:
                LOGGER.debug("Field 'notifyConditions' has empty fields in the subscription");
                return 134;
            case 135:
                LOGGER.debug("Field 'throttling' is empty in the subscription");
                return 135;
            default:
        }
        
        switch (endpointMsg) {
            // case of missing entire endpoint
            case 21:
                LOGGER.debug("Endpoint is missing");
                return 21;
                
            // cases of missing fields in endpoint
            case 221:
                LOGGER.debug("Field 'host' is missing in the endpoint");
                return 221;
            case 222:
                LOGGER.debug("Field 'port' is missing in the endpoint");
                return 222;
            case 223:
                LOGGER.debug("Field 'ssl' is missing in the endpoint");
                return 223;
                
            // cases of empty fields in endpoint
            case 231:
                LOGGER.debug("Field 'host' is empty in the endpoint");
                return 231;
            case 232:
                LOGGER.debug("Field 'port' is empty in the endpoint");
                return 232;
            case 233:
                LOGGER.debug("Field 'ssl' is empty in the endpoint");
                return 233;
                
            // case of invalid field in the endpoint
            case 24:
                LOGGER.debug("Field 'ssl' is invalid");
                return 24;
            default:
        } // switch
      
        LOGGER.debug("Valid input JSON.");
        return 0;

    } // isValid
    
    private int isEntitiesValid (ArrayList<SubscriptionEntity> entities) {
                
        boolean emptyFields = true;
        boolean validFields = true;
        
        if ((entities == null) || (entities.isEmpty())) {
            LOGGER.debug("Field 'entities' is missing");
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
            LOGGER.debug("There are missing fields in entities");
            return 2;
        } // if
        
        // check if entities has any empty field
        if (emptyFields) {
            LOGGER.debug("There are empty fields in entities");
            return 3;
        } // if
        
        LOGGER.debug("Valid entities");
        return 0;
    } // isEntitiesValid
    
    private int isNotifyConditionsValid (ArrayList<SubscriptionConditions> conditions) {
        
        boolean validFields = true;
        boolean emptyFields = true;
                
        if ((conditions == null)|| (conditions.isEmpty())) {
            LOGGER.debug("Field 'notifyConditions' is missing");
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
           LOGGER.debug("There are missing fields in notifyConditions");
           return 2;
        } // if
        
        // check if notifyConditions has any empty field
        if (emptyFields) {
            LOGGER.debug("There are empty fields in notifyConditions");
            return 3;
        } // if
        
        LOGGER.debug("Valid notifyConditions");
        return 0;
    } // isNotifyConditionsValid  
    
} // CygnusSubscription
