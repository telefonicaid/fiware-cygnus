package com.telefonica.iot.cygnus.containers;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class NotifyContextRequestNGSIv2 {

    private String subscriptionId;
    private ArrayList <Data> data;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public ArrayList<Data> getData() {
        return data;
    }

    public void setData(ArrayList<Data> data) {
        this.data = data;
    }


    public NotifyContextRequest toNotifyContextRequest() {
        NotifyContextRequest notifyContextRequest = new NotifyContextRequest();
        notifyContextRequest.setSubscriptionId(this.subscriptionId);
        ArrayList<NotifyContextRequest.ContextElementResponse> contextElementResponses = null;
        for (Data dataElement : data) {
            NotifyContextRequest.ContextElementResponse contextElementResponse = new NotifyContextRequest.ContextElementResponse();
            contextElementResponse.setContextElement(dataElement.dataToContextElement());
            contextElementResponses.add(contextElementResponse);
        }
        notifyContextRequest.setContextResponses(contextElementResponses);
        return notifyContextRequest;
    }

    public class Data {

        private String id;
        private String type;
        private TreeMap<String, Attribute> attributes;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Attribute> getAtributes() {
            return attributes;
        }

        public void setAtributes(TreeMap<String, Attribute> atributes) {
            this.attributes = atributes;
        }

        public NotifyContextRequest.ContextElement dataToContextElement () {
            NotifyContextRequest.ContextElement contextElement = new NotifyContextRequest.ContextElement();
            contextElement.setId(this.id);
            contextElement.setType(this.type);
            ArrayList<NotifyContextRequest.ContextAttribute> attributes = null;
            for(Map.Entry<String, Attribute> map : this.attributes.entrySet()) {
                attributes.add(map.getValue().attributeToContextAttribute(map.getKey()));
            }
                return contextElement;
        }
    }

    public class Attribute {
        private JsonElement value;
        private String type;
        private Map<String, Metadata> metadata;

        public JsonElement getValue() {
            return value;
        }

        public void setValue(JsonElement value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Metadata> getMetadata() {
            return metadata;
        }

        public void setMetadata(TreeMap<String, Metadata> metadata) {
            this.metadata = metadata;
        }

        public NotifyContextRequest.ContextAttribute attributeToContextAttribute (String name) {
            NotifyContextRequest.ContextAttribute contextAttribute = new NotifyContextRequest.ContextAttribute();
            contextAttribute.setName(name);
            contextAttribute.setType(this.type);
            contextAttribute.setContextValue(this.value);
            ArrayList<NotifyContextRequest.ContextMetadata> metadatas = null;
            for(Map.Entry<String, Metadata> map : this.metadata.entrySet()) {
                metadatas.add(map.getValue().metadataToContextMetadata(map.getKey()));
            }
            return contextAttribute;
        }
    }

    public class Metadata {

        private JsonElement value;
        private String type;

        public JsonElement getValue() {
            return value;
        }

        public void setValue(JsonElement value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public NotifyContextRequest.ContextMetadata metadataToContextMetadata (String name) {
            NotifyContextRequest.ContextMetadata contextMetadata = new NotifyContextRequest.ContextMetadata();
            contextMetadata.setName(name);
            contextMetadata.setType(type);
            contextMetadata.setContextMetadata(value);
            return contextMetadata;
        }
    }
}
