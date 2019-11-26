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

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Map;

/**
 * The type Notify context request ngs iv 2.
 */
public class NotifyContextRequestNGSIv2 {

    private String subscriptionId;
    private ArrayList <Data> data;

    /**
     * Gets subscription id.
     *
     * @return the subscription id
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets subscription id.
     *
     * @param subscriptionId the subscription id
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * Gets data.
     *
     * @return the data
     */
    public ArrayList<Data> getData() {
        return data;
    }

    /**
     * Sets data.
     *
     * @param data the data
     */
    public void setData(ArrayList<Data> data) {
        this.data = data;
    }


    /**
     * To notify context request notify context request.
     * This casts NotifyContextRequestNGSIv2 to NotifyContextRequest
     *
     * @return the notify context request
     */
    public NotifyContextRequest toNotifyContextRequest() {
        NotifyContextRequest notifyContextRequest = new NotifyContextRequest();
        notifyContextRequest.setSubscriptionId(this.subscriptionId);
        ArrayList<NotifyContextRequest.ContextElementResponse> contextElementResponses = new ArrayList<NotifyContextRequest.ContextElementResponse>();
        for (Data dataElement : data) {
            NotifyContextRequest.ContextElementResponse contextElementResponse = new NotifyContextRequest.ContextElementResponse();
            contextElementResponse.setContextElement(dataElement.dataToContextElement());
            contextElementResponses.add(contextElementResponse);
        }
        notifyContextRequest.setContextResponses(contextElementResponses);
        return notifyContextRequest;
    }

    /**
     * The type Data.
     */
    public static class Data {

        private String id;
        private String type;
        private Map<String, Attribute> attributes;

        /**
         * Gets id.
         *
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * Sets id.
         *
         * @param id the id
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * Gets type.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Sets type.
         *
         * @param type the type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Gets atributes.
         *
         * @return the atributes
         */
        public Map<String, Attribute> getAtributes() {
            return attributes;
        }

        /**
         * Sets atributes.
         *
         * @param atributes the atributes
         */
        public void setAtributes(Map<String, Attribute> atributes) {
            this.attributes = atributes;
        }

        /**
         * Data to context element notify context request . context element.
         *
         * @return the notify context request . context element
         */
        public NotifyContextRequest.ContextElement dataToContextElement () {
            NotifyContextRequest.ContextElement contextElement = new NotifyContextRequest.ContextElement();
            contextElement.setId(this.id);
            contextElement.setType(this.type);
            ArrayList<NotifyContextRequest.ContextAttribute> attributes = new ArrayList<NotifyContextRequest.ContextAttribute>();
            for(Map.Entry<String, Attribute> map : this.attributes.entrySet()) {
                attributes.add(map.getValue().attributeToContextAttribute(map.getKey()));
            }
            contextElement.setAttributes(attributes);
                return contextElement;
        }
    }

    /**
     * The type Attribute.
     */
    public static class Attribute {
        private JsonElement value;
        private String type;
        private Map<String, Metadata> metadata;

        /**
         * Gets value.
         *
         * @return the value
         */
        public JsonElement getValue() {
            return value;
        }

        /**
         * Sets value.
         *
         * @param value the value
         */
        public void setValue(JsonElement value) {
            this.value = value;
        }

        /**
         * Gets type.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Sets type.
         *
         * @param type the type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Gets metadata.
         *
         * @return the metadata
         */
        public Map<String, Metadata> getMetadata() {
            return metadata;
        }

        /**
         * Sets metadata.
         *
         * @param metadata the metadata
         */
        public void setMetadata(Map<String, Metadata> metadata) {
            this.metadata = metadata;
        }

        /**
         * Attribute to context attribute notify context request . context attribute.
         *
         * @param name the name
         * @return the notify context request . context attribute
         */
        public NotifyContextRequest.ContextAttribute attributeToContextAttribute (String name) {
            NotifyContextRequest.ContextAttribute contextAttribute = new NotifyContextRequest.ContextAttribute();
            contextAttribute.setName(name);
            contextAttribute.setType(this.type);
            contextAttribute.setContextValue(this.value);
            ArrayList<NotifyContextRequest.ContextMetadata> metadatas = new ArrayList<NotifyContextRequest.ContextMetadata>();
            for(Map.Entry<String, Metadata> map : this.metadata.entrySet()) {
                metadatas.add(map.getValue().metadataToContextMetadata(map.getKey()));
            }
            contextAttribute.setContextMetadata(metadatas);
            return contextAttribute;
        }
    }

    /**
     * The type Metadata.
     */
    public static class Metadata {

        private JsonElement value;
        private String type;

        /**
         * Gets value.
         *
         * @return the value
         */
        public JsonElement getValue() {
            return value;
        }

        /**
         * Sets value.
         *
         * @param value the value
         */
        public void setValue(JsonElement value) {
            this.value = value;
        }

        /**
         * Gets type.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Sets type.
         *
         * @param type the type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Metadata to context metadata notify context request . context metadata.
         *
         * @param name the name
         * @return the notify context request . context metadata
         */
        public NotifyContextRequest.ContextMetadata metadataToContextMetadata (String name) {
            NotifyContextRequest.ContextMetadata contextMetadata = new NotifyContextRequest.ContextMetadata();
            contextMetadata.setName(name);
            contextMetadata.setType(type);
            contextMetadata.setContextMetadata(value);
            return contextMetadata;
        }
    }
}
