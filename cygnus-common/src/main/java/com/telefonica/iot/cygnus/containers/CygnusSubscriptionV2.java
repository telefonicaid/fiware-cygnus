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
 * @author pcoello25
 */
public class CygnusSubscriptionV2 {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(CygnusSubscriptionV2.class);
    private final OrionSubscription subscription;
    private final OrionEndpoint endpoint;
    
    /**
     * Constructor.
     */
    public CygnusSubscriptionV2() {
        subscription = new OrionSubscription();
        endpoint = new OrionEndpoint();
    } // CygnusSubscriptionV1
    
    public OrionSubscription getOrionSubscription() {
        return subscription;
    } // getOrionSubscription
    
    public OrionEndpoint getOrionEndpoint() {
        return endpoint;
    } // getOrionEndpoint

    /**
     * Checks if this Cygnus subscription is valid (i.e. it contains all the rquired fields, with valid content).
     * @throws Exception
     */
    public void validate() throws Exception {
        this.subscription.validate();
        this.endpoint.validate();
    } // validate
    
    /**
     * Class for Orion subscriptions (v2).
     */
    public class OrionSubscription {
        private String description;
        private final Subject subject;
        private final Notification notification;
        private String expires;
        private String throttling;
        
        /**
         * Constructor.
         */
        public OrionSubscription() {
            subject = new Subject();
            notification = new Notification();
        } // OrionSubscription
        
        /**
         * Checks if this Orion subscription is valid (i.e. it contains all the rquired fields, with valid content).
         * @throws Exception
         */
        public void validate() throws Exception {
            // Validate the description
            if (description == null) {
                LOGGER.debug("Parsing error, field 'description' is missing in the subscription");
                throw new Exception("Parsing error, field 'description' is missing in the subscription");
            } // if
            
            if (description.isEmpty()) {
                LOGGER.debug("Parsing error, field 'description' is empty in the subscription");
                throw new Exception("Parsing error, field 'description' is empty in the subscription");
            } // if
            
            subject.validate();
            notification.validate();
            
            // Validate the expiration
            if (expires == null) {
                LOGGER.debug("Parsing error, field 'expires' is missing in the subscription");
                throw new Exception("Parsing error, field 'expires' is missing in the subscription");
            } // if
            
            if (expires.isEmpty()) {
                LOGGER.debug("Parsing error, field 'expires' is empty in the subscription");
                throw new Exception("Parsing error, field 'expires' is empty in the subscription");
            } // if
            
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
     * Class for subscription's subject.
     */
    public class Subject {
        private final ArrayList<Entity> entities;
        private final Condition condition;
        
        /**
         * Constructor.
         */
        public Subject() {
            entities = new ArrayList<>();
            condition = new Condition();
        } // subject
        
        public ArrayList<Entity> getEntities() {
            return entities;
        } // getEntities
        
        public Condition getCondition() {
            return condition;
        } // getCondition
        
        /**
         * Checks if this subject is valid (i.e. it contains all the rquired fields, with valid content).
         * @throws java.lang.Exception
         */
        public void validate() throws Exception {
            for (Entity entity : entities) {
                entity.validate();
            } // for
            
            condition.validate();
        } // validate
        
    } // Subject
    
    /**
     * Class for subscription's notification.
     */
    public class Notification {
        private final SubscriptionHttp http;
        private final ArrayList<String> attrs;
        
        /**
         * Constructor.
         */
        public Notification() {
            http = new SubscriptionHttp();
            attrs = new ArrayList<>();
        } // notification
        
        public SubscriptionHttp getHttp() {
            return http;
        } // getHttp
        
        public ArrayList<String> getAttrs() {
            return attrs;
        } // getAttributes
        
        /**
         * Checks if this notification is valid (i.e. it contains all the rquired fields, with valid content).
         * @throws Exception
         */
        public void validate() throws Exception {
            http.validate();
            
            // Validate the attributes
            if (attrs == null) {
                LOGGER.debug("Parsing error, field 'attrs' is missing in the subscription");
                throw new Exception("Parsing error, field 'attrs' is missing in the subscription");
            } // if
        } // validate
        
    } // Notification
    
    /**
     * Class for subscription's entity.
     */
    public class Entity {
        
        private String idPattern;
        private String type;
        
        public String getEntityType() {
            return type;
        } // getEntityType
        
        public String getIdPattern() {
            return idPattern;
        } // getPattern
        
        /**
         * Checks if this entity is valid (i.e. it contains all the rquired fields, with valid content).
         * @throws Exception
         */
        public void validate() throws Exception {
            // Validate the id pattern
            if (idPattern == null) {
                LOGGER.debug("Parsing error, field 'idPattern' is missing in the subscription");
                throw new Exception("Parsing error, field 'idPattern' is missing in the subscription");
            } // if

            if (idPattern.isEmpty()) {
                LOGGER.debug("Parsing error, field 'idPattern' is empty in the subscription");
                throw new Exception("Parsing error, field 'idPattern' is empty in the subscription");
            } // if
            
            // Validate the type
            if (type == null) {
                LOGGER.debug("Parsing error, field 'type' is missing in the subscription");
                throw new Exception("Parsing error, field 'type' is missing in the subscription");
            } // if

            if (type.isEmpty()) {
                LOGGER.debug("Parsing error, field 'type' is empty in the subscription");
                throw new Exception("Parsing error, field 'type' is empty in the subscription");
            } // if
        } // validate
        
    } // Entity
    
    /**
     * Class for subscription's condition.
     */
    public class Condition {
        private final ArrayList<String> attrs;
        private final Expression expression;
        
        /**
         * Constructor.
         */
        public Condition() {
            attrs = new ArrayList<>();
            expression = new Expression();
        } // Condition
        
        public Expression getExpression() {
            return expression;
        } // getExpression
        
        public ArrayList<String> getAttrs() {
            return attrs;
        } // getAttributes
        
        /**
         * Checks if this condition is valid (i.e. it contains all the rquired fields, with valid content).
         * @throws Exception
         */
        public void validate() throws Exception {
            // Validate the attrs
            if (attrs == null) {
                LOGGER.debug("Parsing error, field 'attrs' is missing in the subscription");
                throw new Exception("Parsing error, field 'attrs' is missing in the subscription");
            } // if
            
            expression.validate();
        } // validate
           
    } // Condition

    /**
     * Class for subscription's expression.
     */
    public class Expression {

        private String q;
        
        public String getQ() {
            return q;
        } // getQ
        
        /**
         * Checks if this expression is valid (i.e. it contains all the rquired fields, with valid content).
         * @throws Exception
         */
        public void validate() throws Exception {
            // Validate the query
            if (q == null) {
                LOGGER.debug("Parsing error, field 'q' is missing in the subscription");
                throw new Exception("Parsing error, field 'q' is missing in the subscription");
            } // if

            if (q.isEmpty()) {
                LOGGER.debug("Parsing error, field 'q' is empty in the subscription");
                throw new Exception("Parsing error, field 'q' is empty in the subscription");
            } // if
        } // validate
        
    } // Expression
    
    /**
     * Class for subscription's http endpoint.
     */
    public class SubscriptionHttp {
        
        private String url;
        
        public String getUrl() {
            return url;
        } // getUrl
        
        /**
         * Checks if this http endpoint is valid (i.e. it contains all the rquired fields, with valid content).
         * @throws Exception
         */
        public void validate() throws Exception {
            // Validate the url
            if (url == null) {
                LOGGER.debug("Parsing error, field 'url' is missing in the subscription");
                throw new Exception("Parsing error, field 'url' is missing in the subscription");
            } // if

            if (url.isEmpty()) {
                LOGGER.debug("Parsing error, field 'url' is empty in the subscription");
                throw new Exception("Parsing error, field 'url' is empty in the subscription");
            } // if
        } // validate
        
    } // SubsctiptionHttp
    
} // CygnusSubscriptionV2
