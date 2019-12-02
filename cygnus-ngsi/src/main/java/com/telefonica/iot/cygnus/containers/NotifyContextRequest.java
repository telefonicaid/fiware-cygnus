/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

import com.google.gson.JsonElement;
import com.telefonica.iot.cygnus.utils.CommonConstants;

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
        contextResponses = new ArrayList<>();
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
     * Gets a deep copy of this object.
     * @return A deep copy of this object
     */
    public NotifyContextRequest deepCopy() {
        NotifyContextRequest copy = new NotifyContextRequest();
        copy.subscriptionId = this.subscriptionId;
        copy.originator = this.originator;
        
        if (this.contextResponses != null) {
            copy.contextResponses = new ArrayList<>();

            for (ContextElementResponse cer : this.contextResponses) {
                copy.contextResponses.add(cer.deepCopy());
            } // for
        } else {
            copy.contextResponses = null;
        } // if else
        
        return copy;
    } // deepCopy
    
    @Override
    public String toString() {
        String s = "{\"subscriptionId\":\"" + subscriptionId + "\",\"originator\":\"" + originator
                + "\",\"contextResponses\":[";
        
        if (contextResponses != null) {
            boolean first = true;

            for (ContextElementResponse cer : contextResponses) {
                if (first) {
                    s += cer.toString();
                    first = false;
                } else {
                    s += "," + cer.toString();
                } // if else
            } // for
        } // if
        
        return s + "]}";
    } // toString
    
    /**
     * Class for storing contextElementResponse information from a notifyContextRequest.
     */
    public static class ContextElementResponse {
        
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
        
        /**
         * Gets a deep copy of this object.
         * @return A deep copy of this object
         */
        public ContextElementResponse deepCopy() {
            ContextElementResponse cer = new ContextElementResponse();
            cer.contextElement = this.contextElement.deepCopy();
            cer.statusCode = this.statusCode.deepCopy();
            return cer;
        } // deepCopy
        
        @Override
        public String toString() {
            return "{\"contextElement\":" + contextElement.toString()
                    + ",\"statusCode\":" + statusCode.toString() + "}";
        } // toString
        
    } // ContextElementResponse
    
    /**
     * Class for storing contextElement information from a notifyContextRequest.
     */
    public static class ContextElement {
        
        private ArrayList<ContextAttribute> attributes;
        private String type;
        private String isPattern;
        private String id;
        
        /**
         * Constructor for Gson, a Json parser.
         */
        public ContextElement() {
            attributes = new ArrayList<>();
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
            switch (fieldName) {
                case "entityId":
                    return getId();
                case "entityType":
                    return getType();
                default:
                    return null; // if else
            } // switch
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
        
        /**
         * Gets a deep copy of this object.
         * @return A deep copy of this object
         */
        public ContextElement deepCopy() {
            ContextElement ce = new ContextElement();
            ce.id = this.id;
            ce.type = this.type;
            ce.isPattern = this.isPattern;
            
            if (this.attributes != null) {
                ce.attributes = new ArrayList<>();

                for (ContextAttribute ca : this.attributes) {
                    ce.attributes.add(ca.deepCopy());
                } // for
            } else {
                ce.attributes = null;
            } // if else
            
            return ce;
        } // deepCopy
        
        @Override
        public String toString() {
            String s = "{\"id\":\"" + id + "\",\"type\":\"" + type + "\",\"isPattern\":\"" + isPattern
                    + "\",\"attributes\":[";
            
            if (attributes != null) {
                boolean first = true;

                for (ContextAttribute ca : attributes) {
                    if (first) {
                        s += ca.toString();
                        first = false;
                    } else {
                        s += "," + ca.toString();
                    } // if else
                } // for
            } // if
        
            return s + "]}";
        } // toString
        
    } // ContextElement

    /**
     * Class for storing contextAttribute information from a notifyContextRequest.
     */
    public static class ContextAttribute {
    
        private String name;
        private String type;
        private JsonElement value;
        private ArrayList<ContextMetadata> metadatas;
        
        /**
         * Constructor for Gson, a Json parser.
         */
        public ContextAttribute() {
            metadatas = new ArrayList<>();
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
            if (value.isJsonNull()) {
                return null;
            } else if (value.isJsonObject()) {
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
         * Gets context value as is in the input.
         * Quoted or not quoted.
         * @return The context value for this context attribute in String format.
         */
        public String getContextValue() {
            if (value.isJsonNull()) {
                return null;
            } else if (value.isJsonObject()) {
                return value.getAsJsonObject().toString();
            } else if (value.isJsonArray()) {
                return value.getAsJsonArray().toString();
            } else {
                return  "" + value;
            }  // if then else if
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
        
        /**
         * Gets a deep copy of this object.
         * @return A deep copy of this object
         */
        public ContextAttribute deepCopy() {
            ContextAttribute ca = new ContextAttribute();
            ca.name = this.name;
            ca.type = this.type;
            ca.value = this.value;
            
            if (this.metadatas != null) {
                ca.metadatas = new ArrayList<>();
                
                for (ContextMetadata cm : this.metadatas) {
                    ca.metadatas.add(cm.deepCopy());
                } // for
            } else {
                ca.metadatas = null;
            } // if else
            
            return ca;
        } // deepCopy
        
        @Override
        public String toString() {
            String s = "{\"name\":\"" + name + "\",\"type\":\"" + type + "\",\"value\":" + value
                    + ",\"metadatas\":[";
            if (metadatas != null) {
                boolean first = true;

                for (ContextMetadata cm : metadatas) {
                    if (first) {
                        s += cm.toString();
                        first = false;
                    } else {
                        s += "," + cm.toString();
                    } // if else
                } // for
            } // if
            
            return s + "]}";
        } // toString
        
    } // ContextAttribute
    
    /**
     * Class for storing contextMetadata information from a contestAttribute.
     */
    public static class ContextMetadata {
        
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
        
        /**
         * Gets a deep copy of this object.
         * @return A deep copy of this object
         */
        public ContextMetadata deepCopy() {
            ContextMetadata cm = new ContextMetadata();
            cm.name = this.name;
            cm.type = this.type;
            cm.value = this.value;
            return cm;
        } // deepCopy
        
        @Override
        public String toString() {
            return "{\"name\":\"" + name + "\",\"type\":\"" + type + "\",\"value\":" + value + "}";
        } // toStrng
        
    } // ContextMetadata
    
    /**
     * Class for storing statusCode information from a notifyContextRequest.
     */
    public static class StatusCode {
        
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
        
        /**
         * Gets a deep copy of this object.
         * @return A deep copy of this object
         */
        public StatusCode deepCopy() {
            StatusCode sc = new StatusCode();
            sc.code = this.code;
            sc.reasonPhrase = this.reasonPhrase;
            return sc;
        } // deepCopy
        
        @Override
        public String toString() {
            return "{\"code\":\"" + code + "\",\"reasonPhrase\":\"" + reasonPhrase + "\"}";
        } // toString
        
    } // StatusCode
    
} // NotifyContextRequest