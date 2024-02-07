/**
 * Copyright 2020 Telefonica Investigación y Desarrollo, S.A.U
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;
import com.telefonica.iot.cygnus.management.PatternTypeAdapter;

/**
 *
 * @author anmunoz
 *
 * Container classes mapping an Orion Context Broker nofifyContextRequest notification. These are necessaries in order
 * Gson (a Json parser) can store in memory a notification.
 */
public class NotifyContextRequestLD {
    private String subscriptionId;
    private String context;
    private ArrayList<ContextElementResponse> contextResponses;

    /**
     * Constructor for Gson, a Json parser.
     */


    public NotifyContextRequestLD(JSONObject content) {
        this.subscriptionId = content.get("subscriptionId").toString();
        if (content.has("@context")) {
            this.context = content.get("@context").toString();
        }else
            this.context = "";
        JSONArray data = (JSONArray) content.get("data");
        this.contextResponses =  new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject lData = data.getJSONObject(i);
            contextResponses.add(new ContextElementResponse(lData.toString()));
        }

                //

    } // NotifyContextRequest

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public ArrayList<NotifyContextRequestLD.ContextElementResponse>  getContextResponses() {
        return contextResponses;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void setContextResponses(ArrayList<NotifyContextRequestLD.ContextElementResponse> contextResponses) {
        this.contextResponses = contextResponses;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    /**
     * Gets a deep copy of this object.
     *
     * @return A deep copy of this object
     */
    public NotifyContextRequestLD deepCopy(JSONObject content) {
        NotifyContextRequestLD copy = new NotifyContextRequestLD(content);
        copy.subscriptionId = this.subscriptionId;
        copy.contextResponses = this.contextResponses;
        return copy;
    } // deepCopy

    @Override
    public String toString() {
        String s = "{\"subscriptionId\":\"" + subscriptionId + "\",\"data\":"+
                contextResponses;
        return s + "}";
    } // toString
    public class ContextElementResponse {

        private NotifyContextRequestLD.ContextElement data;

        /**
         * Constructor for Gson, a Json parser.
         */
        public ContextElementResponse(String content) {
            data = new NotifyContextRequestLD.ContextElement(content);
        } // ContextElementResponse

        public NotifyContextRequestLD.ContextElement getContextElement() {
            return data;
        } // getContextElement

        public void setContextElement(NotifyContextRequestLD.ContextElement contextElement) {
            this.data = contextElement;
        } // getContextElement

        @Override
        public String toString() {
            return "{\"data\":" + data.toString()
                    + "}";
        } // toString

    } // ContextElementResponse

    public static class ContextElement {
        private String id;
        private String type;
        private Map<String, Object> attributes;

        /**
         * Constructor for Gson, a Json parser.
         */
        public ContextElement(String contextElement) {
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(Pattern.class, new PatternTypeAdapter())
                .create();
            String cer = contextElement;
            Map<String, Object> map = gson.fromJson(cer, new TypeToken<Map<String, JsonElement>>() {
            }.getType());
            Map<String,Object> attrs = new HashMap<String, Object>();
            this.id= map.get("id").toString().replaceAll("\"","");
            this.type=map.get("type").toString().replaceAll("\"","");

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String x = entry.getKey();
                Object y = entry.getValue();
                if (!"id".equals(x) && !"type".equals(x) && !"@context".equals(x)) {
                    attrs.put(x, y);
                }
            }
            this.attributes=attrs;

        } // ContextElement

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

        public Map<String, Object> getAttributes() {
            return attributes;
        }

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

        public void setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        /**
         * Gets a copy containing only the given attribute.
         * @param attrName
         * @return
         */
        public NotifyContextRequestLD.ContextElement filter(String attrName,String ce) {
            NotifyContextRequestLD.ContextElement contextElement = new NotifyContextRequestLD.ContextElement(ce);
            contextElement.type = this.type;
            contextElement.id = this.id;
            contextElement.attributes = this.attributes;
            return contextElement;
        } // filter

        /**
         * Gets a deep copy of this object.
         * @return A deep copy of this object
         */
        public NotifyContextRequestLD.ContextElement deepCopy(String cep) {
            NotifyContextRequestLD.ContextElement ce = new NotifyContextRequestLD.ContextElement(cep);
            ce.id = this.id;
            ce.type = this.type;
            ce.attributes= this.attributes;

            return ce;
        } // deepCopy

        @Override
        public String toString() {
            String s = "{\"id\":\"" + id + "\",\"type\":\"" + type + "\",";
            if (attributes != null) {
                s +=this.attributes.toString();
            } // if
            return s + "}";

        } // toString

    } // ContextElement

}
