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

/**
 *
 * @author pcoello25
 */
public class CygnusSubscriptionV2 {
    
    private final OrionSubscription subscription;
    private final OrionEndpoint endpoint;
    private static final CygnusLogger LOGGER = new CygnusLogger(CygnusSubscriptionV2.class);    
    
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
    
    public class OrionSubscription {
        private String description;
        private final Subject subject;
        private final Notification notification;
        private String expires;
        private String throttling;
        
        public OrionSubscription() {
            subject = new Subject();
            notification = new Notification();
        } // OrionSubscription
        
        public int isValid () {

            // check error messages from subfields of subscription 
            int subjectMsg = isSubjectValid(subject);
            int notificationMsg = isNotificationValid(notification);
            
            LOGGER.info("subject: " + subjectMsg + ", notification: " + notificationMsg);
                        
            // check if entire subscription is emtpy        
            if ((description == null) && (subjectMsg == 11) && 
                    (notificationMsg == 1) &&  (expires == null)
                    && (throttling == null)) {
                LOGGER.debug("Empty subscription in the request");
                return 11;
            } // if
            
            // check if entire subscription is missing
            if ((description == null) && (subjectMsg == 2) &&
                    (notificationMsg == 2) && (expires == null)
                    && (throttling == null)) {
                LOGGER.debug("Missing subscription in the request");
                return 12;
            }
            
            if (subjectMsg == 1211) {
                LOGGER.debug("Field 'entities' is missing in the subscription");
                return 1211;
            } // if
            
            if (subjectMsg == 1212) {
                LOGGER.debug("Field 'entities' has missing fields in the subscription");
                return 1212;
            } // if
            
            if (subjectMsg == 1213) {
                LOGGER.debug("Field 'entities' has empty fields in the subscription");
                return 1213;
            } // if
            
            if (subjectMsg == 1311) {
                LOGGER.debug("Field 'condition' is missing in the subscription");
                return 1311;
            } // if
            
            if (subjectMsg == 1312) {
                LOGGER.debug("Field 'condition' has missing fields in the subscription");
                return 1312;
            } // if
            
            if (subjectMsg == 1313) {
                LOGGER.debug("Field 'condition' has empty fields in the subscription");
                return 1313;
            } // if
            
            if (description == null) {
                LOGGER.debug("Field 'description' is missing in the subscription");
                return 141;
            } // if
            
            if (description.length() == 0) {
                LOGGER.debug("Field 'description' is empty in the description");
                return 142;
            } // if       
                        
            if (expires == null) {
                LOGGER.debug("Field 'expires' is missing in the subscription");
                return 171;
            } // if
            
            if (expires.length() == 0) {
                LOGGER.debug("Field 'expires' is empty in the subscription");
                return 172;
            } // if
            
            if (throttling == null) {
                LOGGER.debug("Field 'throttling' is missing in the subscription");
                return 125;
            } // if
            
            if (throttling.length() == 0) {
                LOGGER.debug("Field 'throttling' is empty in the subscription");
                return 135;
            } // if
            
            if (subjectMsg == 11) {
                LOGGER.debug("Field 'subject' is missing in the subscription");
                return 15111;
            } // if
            
            if (subjectMsg == 12) {
                LOGGER.debug("Field 'subject' is empty in the subscription");
                return 15112;
            } // if
            
            if (subjectMsg == 2) {
                LOGGER.debug("Field 'subject' has missing fields in the subscription");
                return 1512;
            } // if
            
            if (subjectMsg == 3) {
                LOGGER.debug("Field 'subject' has empty fields in the subscription");
                return 1513;
            } // if
            
            if (notificationMsg == 1) {
                LOGGER.debug("Field 'notification' is missing in the subscription");
                return 1611;
            } // if
            
            if (notificationMsg == 2) {
                LOGGER.debug("Field 'notification' is empty in the subscription");
                return 1612;
            } // if
            
            if (notificationMsg == 3) {
                LOGGER.debug("Field 'notification' has empty fields in the subscription");
                return 1613;
            } // if

            // return 0 if valid subscription
            LOGGER.debug("Valid subscription");
            return 0;
        } // isValid
        
        private int isSubjectValid(Subject subject) {
            
            if (subject == null) {
                LOGGER.debug("Field 'subject' is missing");
                return 11;
            } // if
            
            // get fields of subject
            Condition condition = subject.getCondition();
            ArrayList<Entity> entities = subject.getEntities();
            
            // get error numbers of each field
            int conditionMsg = isConditionValid(condition);
            int entitiesMsg = isEntitiesValid(entities);
            
            LOGGER.info("conditions: " + conditionMsg + ", entities: " + entitiesMsg);
                        
            if ((conditionMsg == 1) && (entitiesMsg == 1)) {
                LOGGER.debug("Field 'subject' is empty");
                return 12;
            } // if
            
            if ((conditionMsg == 2) || (entitiesMsg == 2)) {
                LOGGER.debug("There are missing fields in 'subject'");
                return 2;
            } // if
            
            if ((conditionMsg == 3) || (entitiesMsg == 3)) {
                LOGGER.debug("There are empty fields in 'subject'");
                return 3;
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
            
            // check if subscription contains entities
            if (conditionMsg == 1) {
                LOGGER.debug("Field 'condition' is missing in the subscription");
                return 1311;
            } // if

            // check if subscription.entities has missing fields
            if (conditionMsg == 2) {
                LOGGER.debug("Field 'condition' has missing fields in the subscription");
                return 1312;
            } // if

            // check if subscription.entities has empty fields
            if (conditionMsg == 3) {
                LOGGER.debug("Field 'condition' has empty fields in the subscription");
                return 1313;
            } // if
            
            LOGGER.debug("Valid subject");
            return 0;
        } // isSubjectsValid
        
        private int isNotificationValid (Notification notification) {
            
            LOGGER.info(notification);
            
            if (notification == null) {
                LOGGER.debug("Field 'notification' is missing");
                return 1;
            } // if 
            
            SubscriptionHttp http = notification.getHttp();
            ArrayList<String> attrs = notification.getAttrs();
            
            LOGGER.info(http == null);
            LOGGER.info("http: " + http.toString());
            
            int httpMsg = isHttpValid(http); 
            
            LOGGER.info("httpMessage: " + httpMsg);
            
            if ((attrs == null) || (httpMsg == 1)){
                LOGGER.debug("Field 'notification' has missing fields");
                return 2;
            } // if
            
            if (httpMsg == 2) {
                LOGGER.debug("Field 'notification' has empty fields");
                return 3;
            } // if
                        
            LOGGER.debug("Valid notification");
            return 0;
        } // isNotificationsValid
        
        private int isHttpValid (SubscriptionHttp http) {
            
            String url = http.getUrl();
            
            if (url == null) {   
                LOGGER.debug("Field 'http' is missing");
                return 1;
            } else {
                
                if (url.length() == 0) {
                    LOGGER.debug("Field 'http' is empty");
                    return 2;
                } // if
                
            } // if else
            
            LOGGER.debug("Valid http");
            return 0;
        } // isHttpValid
        
        private int isConditionValid (Condition condition) {
            
            if (condition == null) {
                LOGGER.debug("Field 'condition' is missing");
                return 1;
            } // if 
            
            int expressionMsg = isExpressionValid(condition.getExpression());   
                       
            if ((expressionMsg == 1) || (expressionMsg == 2) || 
                    (condition.getAttrs() == null)) {
                LOGGER.debug("There are missing fields in 'condition'");
                return 2;
            } // if
            
            if (expressionMsg == 3) {
                LOGGER.debug("There are empty fields in 'condition'");
                return 3;
            } // if
            
            LOGGER.debug("Valid condition");            
            return 0;
        } // isConditionValid
        
        private int isEntitiesValid (ArrayList<Entity> entities) {

            if (entities == null) {
                LOGGER.debug("Field 'entities' is missing");
                return 1;
            } // if
            
            boolean validFields = !entities.isEmpty();
            boolean emptyFields = true;

            for (Entity entity : entities) {
                String type = entity.getEntityType();
                String isPattern = entity.getIdPattern();
                
                LOGGER.info("type: " + type + ", pattern: " + isPattern);

                validFields &= ((type != null) && (isPattern != null));
                emptyFields &= validFields && ((type.length() == 0) || 
                        (isPattern.length() == 0)); 
            } // for
            
            LOGGER.info("ENTITIES: Valid: " + validFields + ", empty: " + emptyFields);

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
        
        private int isExpressionValid (Expression expression) {
            
            if (expression == null) {
                LOGGER.debug("Field 'expression' is missing");
                return 1;
            } // if
            
            String q = expression.getQ();
            
            if (q == null) {
                LOGGER.debug("Field 'expression' is empty");
                return 2;
            } // if
            
            if (q.isEmpty()) {
                LOGGER.debug("Field 'q' is empty");
                return 3;
            }
            
            LOGGER.debug("Valid expression");
            return 0;
        } // isExpressionValid
        
    } // OrionSubscription
    
    public class Subject {
        private final ArrayList<Entity> entities;
        private final Condition condition;
        
        public Subject() {
            entities = new ArrayList<Entity>();
            condition = new Condition();
        } // subject
        
        public ArrayList<Entity> getEntities() {
            return entities;
        } // getEntities
        
        public Condition getCondition() {
            return condition;
        } // getCondition
        
    } // Subject
    
    public class Notification {
        private final SubscriptionHttp http;
        private final ArrayList<String> attrs;
        
        public Notification() {
            http = new SubscriptionHttp();
            attrs = new ArrayList<String>();
        } // notification
        
        public SubscriptionHttp getHttp() {
            return http;
        } // getHttp
        
        public ArrayList<String> getAttrs() {
            return attrs;
        } // getAttributes
        
    } // Notification
    
    public class Entity {
        private String idPattern;
        private String type;
        
        public String getEntityType() {
            return type;
        } // getEntityType
        
        public String getIdPattern() {
            return idPattern;
        } // getPattern
    } // Entity
    
    public class Condition {
        private final ArrayList<String> attrs;
        private final Expression expression;
        
        public Condition() {
            attrs = new ArrayList<String>();
            expression = new Expression();
        } // Condition
        
        public Expression getExpression() {
            return expression;
        } // getExpression
        
        public ArrayList<String> getAttrs() {
            return attrs;
        } // getAttributes
           
    } // Condition
        
    public class Expression {
        String q;
        
        public String getQ() {
            return q;
        } // getQ
    } // Expression
    
    public class SubscriptionHttp {
        String url;
        
        public String getUrl() {
            return url;
        }
    } // SubsctiptionHttp
 
    
    /**
     * Checks if the given Gson has subscription and endpoint parameters.
     * @return True if the given Json is valid as grouping rule, otherwise false
     */
    public int isValid() {
        OrionSubscription orionSubscription = this.getOrionSubscription();
        OrionEndpoint orionEndpoint = this.getOrionEndpoint();
        
        int subscriptionMsg = orionSubscription.isValid();  
        int endpointMsg = orionEndpoint.isValid();
        
        LOGGER.info("Subs: " + subscriptionMsg + ", Endpoint: " + endpointMsg);

        switch (subscriptionMsg) {
            // case of missing entire subscription
            case 11:
                LOGGER.debug("Subscription is empty");
                return 11;     
            case 12: 
                LOGGER.debug("Subscription is missing");
                return 12;
                
            case 1211:
                LOGGER.debug("Field 'entities' is missing in the subscription");
                return 1211;
            case 1212:
                LOGGER.debug("Field 'entities' has missing fields ");
                return 1212;
            case 1213:
                LOGGER.debug("Field 'entities' has empty fields");
                return 1213;
                
            case 1311:
                LOGGER.debug("Field 'condition' is missing in the subcription");
                return 1311;
            case 1312:
                LOGGER.debug("Field 'condition' has missing fields in the subcription");
                return 1312;
            case 1313: 
                LOGGER.debug("Field 'condition' has empty fields in the subcription");
                return 1313;
                
            case 141:
                LOGGER.debug("Field 'description' is missing in the subscription");
                return 141;
            case 142:
                LOGGER.debug("Field 'description' is empty in the subcription");
                return 142;
                
            case 15111: 
                LOGGER.debug("Field 'subject' is missing in the subscription");
                return 15111;
            case 15112: 
                LOGGER.debug("Field 'subject' is empty in the subscription");
                return 15112;
            case 1512:
                LOGGER.debug("Field 'subject' has missing fields in the subcription");
                return 1512;
            case 1513: 
                LOGGER.debug("Field 'subject' has empty fields in the subcription");
                return 1513;
            
            case 1611:
                LOGGER.debug("Field 'notification' is missing in the subcription");
                return 1611;
            case 1612: 
                LOGGER.debug("Field 'notification' has missing fields in the subcription");
                return 1612;
            case 1613:
                LOGGER.debug("Field 'notification' has empty fields in the subcription");
                return 1613;
            
            case 171: 
                LOGGER.debug("Field 'expires' is missing in the subcription");
                return 171;
            case 172: 
                LOGGER.debug("Field 'expires' is empty in the subcription");
                return 172;
            
            case 125: 
                LOGGER.debug("Field 'throttling' is missing in the subscription");
                return 125;
            case 135:
                LOGGER.debug("Field 'throttling' is empty in the subscription");
                return 135;
            default:
                // Unreachable statement
        } // switch
        
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
                // Unreachable statement
        } // switch
      
        LOGGER.debug("Valid input JSON.");
        return 0;

    } // isValid  
    
} // CygnusSubscriptionV2
