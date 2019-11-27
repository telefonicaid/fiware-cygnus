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
 * The type Notify context request NGSIv2.
 */
public class NotifyContextRequestNGSIv2 {

    private String subscriptionId;
    private ArrayList <Data> data;

    /**
     * Gets subscription id from NGSIv2 Object.
     *
     * @return the subscription id
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets subscription id for NGSIv2 Object.
     *
     * @param subscriptionId the subscription id
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * Gets data object from NGSIv2 Object.
     *
     * @return the data
     */
    public ArrayList<Data> getData() {
        return data;
    }

    /**
     * Sets data object for NGSIv2 Object.
     *
     * @param data the data
     */
    public void setData(ArrayList<Data> data) {
        this.data = data;
    }


    /**
     * notifycontextrequestNGSIv2 object to notifycontextrequest object.
     * This function casts NotifyContextRequestNGSIv2 object to NotifyContextRequest object
     *
     * @return the notifycontextrequest
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
     * Class that contains data objects from NGSIv2 object
     */
    public static class Data {

        private String id;
        private String type;
        private Map<String, Attribute> attributes;

        /**
         * Gets id from NGSIv2.Data object.
         *
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * Sets id for NGSIv2.Data object.
         *
         * @param id the id
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * Gets type from NGSIv2.Data object.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Sets type for NGSIv2.Data object.
         *
         * @param type the type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Gets atributes from NGSIv2.Data object.
         *
         * @return the atributes
         */
        public Map<String, Attribute> getAtributes() {
            return attributes;
        }

        /**
         * Sets atributes for NGSIv2.Data object.
         *
         * @param atributes the atributes
         */
        public void setAtributes(Map<String, Attribute> atributes) {
            this.attributes = atributes;
        }

        /**
         * dataToContextElement Data object to NotifyContextRequest.ContextElement object.
         * This function casts NotifyContextRequestNGSIv2.Data object to NotifyContextRequest.ContextElement object
         *
         * @return the NotifyContextRequest.ContextElement object
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
     * Class that contains attribute objects from NGSIv2.Data object
     */
    public static class Attribute {
        private JsonElement value;
        private String type;
        private Map<String, Metadata> metadata;

        /**
         * Gets value from NGSIv2.Attribute object.
         *
         * @return the value
         */
        public JsonElement getValue() {
            return value;
        }

        /**
         * Sets value for data NGSIv2.Attribute object.
         *
         * @param value the value
         */
        public void setValue(JsonElement value) {
            this.value = value;
        }

        /**
         * Gets type from NGSIv2.Attribute object.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Sets type for NGSIv2.Attribute object.
         *
         * @param type the type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Gets metadata from NGSIv2.Attribute object.
         *
         * @return the metadata
         */
        public Map<String, Metadata> getMetadata() {
            return metadata;
        }

        /**
         * Sets metadata for NGSIv2.Attribute object.
         *
         * @param metadata the metadata
         */
        public void setMetadata(Map<String, Metadata> metadata) {
            this.metadata = metadata;
        }

        /**
         * dataToContextAttribute Attribute object to NotifyContextRequest.ContextAttribute object.
         * This function casts NotifyContextRequestNGSIv2.Attribute object to NotifyContextRequest.ContextAttribute object
         *
         * @param name the name of the attribute
         * @return the NotifyContextRequest.ContextAttribute object
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
     * Class that contains metadata objects from NGSIv2.Attribute object
     */
    public static class Metadata {

        private JsonElement value;
        private String type;

        /**
         * Gets value from NGSIv2.Metadata object.
         *
         * @return the value
         */
        public JsonElement getValue() {
            return value;
        }

        /**
         * Sets value for NGSIv2.Metadata object.
         *
         * @param value the value
         */
        public void setValue(JsonElement value) {
            this.value = value;
        }

        /**
         * Gets type from NGSIv2.Metadata object.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Sets type for NGSIv2.Metadata object.
         *
         * @param type the type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * dataToContextMetadata Metadata object to NotifyContextRequest.ContextMetadata object.
         * This function casts NotifyContextRequestNGSIv2.Metadata object to NotifyContextRequest.ContextMetadata object
         *
         * @param name the name of the metadata
         * @return the NotifyContextRequest.ContextMetadata object.
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
