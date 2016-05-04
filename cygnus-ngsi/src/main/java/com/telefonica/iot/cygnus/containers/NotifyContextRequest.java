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

import com.google.gson.JsonElement;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import java.util.ArrayList;

/**
*
* @author frb
*
* Container classes mapping an Orion Context Broker nofifyContextRequest notification. These are necessaries in order
* Gson (a Json parser) can store in memory a notification.
*/
public class NotifyContextRequest {
    
    private String subscriptionId;
    private String originator;
    private ArrayList<ContextElementResponse> contextResponses;
    
    /**
     * Constructor for Gson, a Json parser.
     */
    public NotifyContextRequest() {
        contextResponses = new ArrayList<ContextElementResponse>();
    } // NotifyContextRequest

    public String getSubscriptionId() {
        return subscriptionId;
    } // getSubscriptionId
    
    public String getOriginator() {
        return originator;
    } // getOriginator
    
    public ArrayList<ContextElementResponse> getContextResponses() {
        return contextResponses;
    } // getContextResponses
    
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    } // setSubscriptionId
    
    public void setOriginator(String originator) {
        this.originator = originator;
    } // setSubscriptionId
    
    public void setContextResponses(ArrayList<ContextElementResponse> contextResponses) {
        this.contextResponses = contextResponses;
    } // setContextResponses
    
    /**
     * Class for storing contextElementResponse information from a notifyContextRequest.
     */
    public class ContextElementResponse {
        
        private ContextElement contextElement;
        private StatusCode statusCode;
        
        /**
         * Constructor for Gson, a Json parser.
         */
        public ContextElementResponse() {
            contextElement = new ContextElement();
            statusCode = new StatusCode();
        } // ContextElementResponse
        
        public ContextElement getContextElement() {
            return contextElement;
        } // getContextElement
        
        public StatusCode getStatusCode() {
            return statusCode;
        } // getStatusCode
        
        public void setContextElement(ContextElement contextElement) {
            this.contextElement = contextElement;
        } // getContextElement
        
        public void setStatusCode(StatusCode statusCode) {
            this.statusCode = statusCode;
        } // setStatusCode
        
    } // ContextElementResponse
    
    /**
     * Class for storing contextElement information from a notifyContextRequest.
     */
    public class ContextElement {
        
        private ArrayList<ContextAttribute> attributes;
        private String type;
        private String isPattern;
        private String id;
        
        /**
         * Constructor for Gson, a Json parser.
         */
        public ContextElement() {
            attributes = new ArrayList<ContextAttribute>();
        } // ContextElement
        
        public ArrayList<ContextAttribute> getAttributes() {
            return attributes;
        } // getAttributes
        
        public String getType() {
            return type;
        } // getType
        
        public String getIsPattern() {
            return isPattern;
        } // getIsPattern

        public String getId() {
            return id;
        } // getId
        
        /**
         * Gets a field of the contextElement given its name.
         * @param fieldName
         * @return
         */
        public String getString(String fieldName) {
            if (fieldName.equals("entityId")) {
                return getId();
            } else if (fieldName.equals("entityType")) {
                return getType();
            } else {
                return null;
            } // if else
        } // getString
        
        public void setAttributes(ArrayList<ContextAttribute> attributes) {
            this.attributes = attributes;
        } // setAttributes
        
        public void setType(String type) {
            this.type = type;
        } // setType
        
        public void setIsPattern(String isPattern) {
            this.isPattern = isPattern;
        } // setIsPattern
        
        public void setId(String id) {
            this.id = id;
        } // setId
        
        /**
         * Gets a copy containing only the given attribute.
         * @param attrName
         * @return
         */
        public ContextElement filter(String attrName) {
            ContextElement contextElement = new ContextElement();
            contextElement.type = this.type;
            contextElement.isPattern = this.isPattern;
            contextElement.id = this.id;
            
            for (ContextAttribute contextAttribute : this.attributes) {
                if (contextAttribute.getName().equals(attrName)) {
                    contextElement.attributes.add(contextAttribute);
                    break;
                } // if
            } // for
            
            return contextElement;
        } // filter
        
    } // ContextElement
    
    /**
     * Class for storing entityId information from a contextElement.
     */
    public class EntityId {
        
        private String type;
        private String isPattern;
        private String id;
        
        /**
         * Constructor for Gson, a Json parser.
         */
        public EntityId() {
        } // EntityId
        
        public String getType() {
            return type;
        } // getType
        
        public String getIsPattern() {
            return isPattern;
        } // getIsPattern
        
        public String getId() {
            return id;
        } // getId
        
        public void setType(String type) {
            this.type = type;
        } // setType
        
        public void setIsPattern(String isPattern) {
            this.isPattern = isPattern;
        } // setIsPattern
        
        public void setId(String id) {
            this.id = id;
        } // setId
        
    } // EntityId
    
    /**
     * Class for storing contextAttribute information from a notifyContextRequest.
     */
    public class ContextAttribute {
        
        private String name;
        private String type;
        private JsonElement value;
        private ArrayList<ContextMetadata> metadatas;
        
        /**
         * Constructor for Gson, a Json parser.
         */
        public ContextAttribute() {
            metadatas = new ArrayList<ContextMetadata>();
        } // ContextAttribute
        
        public String getName() {
            return name;
        } // gertName
        
        public String getType() {
            return type;
        } // getType
        
        /**
         * Gets context value.
         * @param asStringRepresentation
         * @return The context value for this context attribute in String format.
         */
        public String getContextValue(boolean asStringRepresentation) {
            if (value.isJsonObject()) {
                return value.getAsJsonObject().toString();
            } else if (value.isJsonArray()) {
                return value.getAsJsonArray().toString();
            } else if (asStringRepresentation) {
                return "\"" + value.getAsString() + "\"";
            } else {
                return value.getAsString();
            } // if then else if
        } // getContextValue
        
        /**
         * Gets the context metadata.
         * @return The context metadata for this context attribute in String format.
         */
        public String getContextMetadata() {
            if (metadatas == null) {
                return CommonConstants.EMPTY_MD;
            } // if
            
            if (metadatas.isEmpty()) {
                return CommonConstants.EMPTY_MD;
            } // if
            
            String res = "[";
            
            for (ContextMetadata contextMetadata : metadatas) {
                if (contextMetadata == null) {
                    continue;
                } // if
                
                res += "{\"name\":\"" + contextMetadata.getName() + "\","
                        + "\"type\":\"" + contextMetadata.getType() + "\","
                        + "\"value\":" + contextMetadata.getValue() + "},";
            } // for
            
            return res.substring(0, res.length() - 1) + "]";
        } // getContextMetadata
        
        public void setName(String name) {
            this.name = name;
        } // setName
        
        public void setType(String type) {
            this.type = type;
        } // setType
        
        public void setContextValue(JsonElement value) {
            this.value = value;
        } // setContextMetadata
        
        public void setContextMetadata(ArrayList<ContextMetadata> metadatas) {
            this.metadatas = metadatas;
        } // setContextMetadata
        
    } // ContextAttribute
    
    /**
     * Class for storing contextMetadata information from a contestAttribute.
     */
    public class ContextMetadata {
        
        private String name;
        private String type;
        private JsonElement value;
        
        /**
         * Constructor for Gson, a Json parser.
         */
        public ContextMetadata() {
        } // ContextMetadata
        
        public String getName() {
            return name;
        } // getName
        
        public String getType() {
            return type;
        } // getType
        
        /**
         * Gets metadata value.
         * @return The metadata value for this metadata attribute in String format.
         */
        public String getValue() {
            if (value.isJsonObject()) {
                return value.getAsJsonObject().toString();
            } else if (value.isJsonArray()) {
                return value.getAsJsonArray().toString();
            } else {
                return "\"" + value.getAsString() + "\"";
            } // if else if
        } // getValue
        
        public void setName(String name) {
            this.name = name;
        } // setName
        
        public void setType(String type) {
            this.type = type;
        } // setType
        
        public void setContextMetadata(JsonElement value) {
            this.value = value;
        } // setContextMetadata
        
    } // ContextMetadata
    
    /**
     * Class for storing statusCode information from a notifyContextRequest.
     */
    public class StatusCode {
        
        private String code;
        private String reasonPhrase;
        
        /**
         * Constructor for Gson, a Json parser.
         */
        public StatusCode() {
        } // StatusCode

        public String getCode() {
            return code;
        } // getCode
        
        public String getReasonPhrase() {
            return reasonPhrase;
        } // getReasonPhrase
        
        public void setCode(String code) {
            this.code = code;
        } // setCode
        
        public void setReasonPhrase(String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
        } // setReasonPhrase
        
    } // StatusCode
    
} // NotifyContextRequest