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

package com.telefonica.iot.cygnus.utils;

import com.google.gson.*;
import com.google.gson.JsonObject;
import com.telefonica.iot.cygnus.containers.NotifyContextRequestNGSIv2;
import com.telefonica.iot.cygnus.containers.NotifyContextRequestNGSIv2.Attribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequestNGSIv2.Data;
import com.telefonica.iot.cygnus.containers.NotifyContextRequestNGSIv2.Metadata;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The type Notify context request ngsiv2 deserializer for gson.
 */
public class NotifyContextRequestNGSIv2Deserializer implements JsonDeserializer<NotifyContextRequestNGSIv2> {

    @Override
    public NotifyContextRequestNGSIv2 deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String subscriptionId = jsonObject.get("subscriptionId").getAsString();
        ArrayList<Data> data = deserializeAllData(jsonObject.getAsJsonArray("data"));
        NotifyContextRequestNGSIv2 ncr = new NotifyContextRequestNGSIv2();
        ncr.setSubscriptionId(subscriptionId);
        ncr.setData(data);
        return ncr;
    }

    private ArrayList<Data> deserializeAllData(JsonArray jsonArray) {
        ArrayList<Data> dataList = new ArrayList<>(jsonArray.size());
        for (JsonElement element : jsonArray) {
            dataList.add(deserializeData(element.getAsJsonObject()));
        }
        return dataList;
    }

    private Data deserializeData(JsonObject jsonObject) {
        Data data = new Data();

        data.setId(jsonObject.get("id").getAsString());
        data.setType(jsonObject.get("type").getAsString());
        data.setAtributes(deserializeAttributes(jsonObject));

        return data;
    }

    private Map<String, Attribute> deserializeAttributes(JsonObject jsonObject) {
        Map<String, Attribute> attributes = new HashMap<>();
        Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
        for(Map.Entry<String, JsonElement> entry : entrySet) {
            if (!entry.getKey().equals("id") && !entry.getKey().equals("type")) {
                attributes.put(entry.getKey(), deserializeAttribute(entry.getValue().getAsJsonObject()));
            }
        }
        return attributes;
    }

    private Attribute deserializeAttribute(JsonObject jsonObject) {
        Attribute attribute = new Attribute();
        attribute.setType(jsonObject.get("type").getAsString());
        attribute.setValue(jsonObject.get("value"));
        attribute.setMetadata(deserializeMetadatas(jsonObject.get("metadata").getAsJsonObject()));

        return attribute;
    }

    private Map<String, Metadata> deserializeMetadatas(JsonObject jsonObject) {
        Map<String, Metadata> metadatas = new HashMap<>();
        Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
        for(Map.Entry<String, JsonElement> entry : entrySet) {
            if (!entry.getKey().equals("value") && !entry.getKey().equals("type")) {
                metadatas.put(entry.getKey(), deserializeMetadata(entry.getValue().getAsJsonObject()));
            }
        }
        return metadatas;
    }

    private Metadata deserializeMetadata(JsonObject jsonObject) {
        Metadata metadata = new Metadata();
        metadata.setType(jsonObject.get("type").getAsString());
        metadata.setValue(jsonObject.get("value"));
        return metadata;
    }

}
