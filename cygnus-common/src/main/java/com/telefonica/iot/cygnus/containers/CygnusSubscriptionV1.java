/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.containers;

import java.util.ArrayList;
import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 *
 * @author pcoello25, frb
 */
public class CygnusSubscriptionV1 {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(CygnusSubscriptionV1.class);
    private final OrionSubscription subscription;
    private final OrionEndpoint endpoint;
    
    /**
     * Constructor.
     */
    public CygnusSubscriptionV1() {
        subscription = new OrionSubscription();
        endpoint = new OrionEndpoint();
    } // CygnusSubscriptionV1
    
    public OrionSubscription getOrionSubscription() {
        return subscription;
    } // getSubscription
    
    public OrionEndpoint getOrionEndpoint() {
        return endpoint;
    } // getEndpoint

    /**
     * Checks if this Cygnus subscription is valid (i.e. it contains all the rquired fields, with valid content).
     * @throws Exception
     */
    public void validate() throws Exception {
        this.subscription.validate();
        this.endpoint.validate();
    } // validate
    
    /**
     * Class for Orion subscriptions (v1).
     */
    public class OrionSubscription {
        private final ArrayList<Entity> entities;
        private final ArrayList<String> attributes;
        private String reference;
        private String duration;
        private final ArrayList<NotifyCondition> notifyConditions;
        private String throttling;
        
        /**
         * Constructor.
         */
        public OrionSubscription() {
            entities = new ArrayList<>();
            attributes = new ArrayList<>();
            notifyConditions = new ArrayList<>();
        } // OrionSubscription
        
        /**
         * Checks if this subscription is valid (i.e. it contains all the rquired fields, with valid content).
         * @throws Exception
         */
        public void validate() throws Exception {
            // Validate the entities
            for (Entity entity : entities) {
                entity.validate();
            } // for
            
            // Validate the attributes
            if (attributes == null) {
                LOGGER.debug("Parsing error, field 'attributes' is missing in the subscription");
                throw new Exception("Parsing error, field 'attributes' is missing in the subscription");
            } // if
            
            // Validate the reference
            if (reference == null) {
                LOGGER.debug("Parsing error, field 'reference' is missing in the subscription");
                throw new Exception("Parsing error, field 'reference' is missing in the subscription");
            } // if
            
            if (reference.isEmpty()) {
                LOGGER.debug("Parsing error, field 'reference' is empty in the subscription");
                throw new Exception("Parsing error, field 'reference' is empty in the subscription");
            } // if
            
            // Validate the duration
            if (duration == null) {
                LOGGER.debug("Parsing error, field 'duration' is missing in the subscription");
                throw new Exception("Parsing error, field 'duration' is missing in the subscription");
            } // if
            
            if (duration.isEmpty()) {
                LOGGER.debug("Parsing error, field 'duration' is empty in the subscription");
                throw new Exception("Parsing error, field 'duration' is empty in the subscription");
            } // if
            
            // Validate the notify conditions
            for (NotifyCondition notifyCondition : notifyConditions) {
                notifyCondition.validate();
            } // for

            // Validate the throttling
            if (throttling == null) {
                LOGGER.debug("Parsing error, field 'throttling' is missing in the subscription");
                throw new Exception("Parsing error, field 'throttling' is missing in the subscription");
            } // if

            if (throttling.isEmpty()) {
                LOGGER.debug("Parsing error, field 'throttling' is empty in the subscription");
                throw new Exception("Parsing error, field 'throttling' is empty in the subscription");
            } // if
        } // validate

    } // OrionSubscription
    
    /**
     * Class for subscription's entities.
     */
    public class Entity {
        
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
        
        /**
         * Checks if these entities are valid (i.e. it contains all the rquired fields, with valid content).
         * @throws Exception
         */
        public void validate() throws Exception {
            // Validate the type
            if (type == null) {
                LOGGER.debug("Parsing error, field 'type' is missing in the subscription");
                throw new Exception("Parsing error, field 'type' is missing in the subscription");
            } // if

            if (type.isEmpty()) {
                LOGGER.debug("Parsing error, field 'type' is empty in the subscription");
                throw new Exception("Parsing error, field 'type' is empty in the subscription");
            } // if
            
            // Validate the pattern flag
            if (isPattern == null) {
                LOGGER.debug("Parsing error, field 'isPattern' is missing in the subscription");
                throw new Exception("Parsing error, field 'isPattern' is missing in the subscription");
            } // if

            if (isPattern.isEmpty()) {
                LOGGER.debug("Parsing error, field 'isPattern' is empty in the subscription");
                throw new Exception("Parsing error, field 'isPattern' is empty in the subscription");
            } // if
            
            // Validate the id
            if (id == null) {
                LOGGER.debug("Parsing error, field 'id' is missing in the subscription");
                throw new Exception("Parsing error, field 'id' is missing in the subscription");
            } // if

            if (id.isEmpty()) {
                LOGGER.debug("Parsing error, field 'id' is empty in the subscription");
                throw new Exception("Parsing error, field 'id' is empty in the subscription");
            } // if
        } // validate
        
    } // Entity
    
    /**
     * Class for subscription's notify conditions.
     */
    public class NotifyCondition {
        
        private String type;
        private ArrayList<String> condValues;
        
        public String getCondType() {
            return type;
        } // getType
        
        public ArrayList<String> getCondValues() {
            return condValues;
        } // getCondValues
        
        /**
         * Checks if these notify conditions are valid (i.e. it contains all the rquired fields, with valid content).
         * @throws Exception
         */
        public void validate() throws Exception {
            // Validate the type
            if (type == null) {
                LOGGER.debug("Parsing error, field 'type' is missing in the subscription");
                throw new Exception("Parsing error, field 'type' is missing in the subscription");
            } // if

            if (type.isEmpty()) {
                LOGGER.debug("Parsing error, field 'type' is empty in the subscription");
                throw new Exception("Parsing error, field 'type' is empty in the subscription");
            } // if
            
            // Validate the condition values
            if (condValues == null) {
                LOGGER.debug("Parsing error, field 'condValues' is missing in the subscription");
                throw new Exception("Parsing error, condValues 'type' is missing in the subscription");
            } // if
        } // validate
        
    } // NotifyCondition
    
} // CygnusSubscriptionV1