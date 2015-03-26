/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.containers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElementResponse;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextMetadata;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.EntityId;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.StatusCode;
import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author frb
 */
public class NotifyContextRequestSAXHandler extends DefaultHandler {
    
    private NotifyContextRequest notifyContextRequest = null;
    private boolean subscriptionId = false;
    private boolean originator = false;
    private ArrayList<ContextElementResponse> contextResponseList = null;
    private ContextElementResponse contextElementResponse = null;
    private ContextElement contextElement = null;
    private EntityId entityId = null;
    private boolean id = false;
    private ArrayList<ContextAttribute> contextAttributeList = null;
    private ContextAttribute contextAttribute = null;
    private boolean name = false;
    private boolean type = false;
    private boolean contextValue = false;
    private boolean value = false;
    private ArrayList<ContextMetadata> contextMetadataList = null;
    private ContextMetadata contextMetadata = null;
    private StatusCode statusCode = null;
    private boolean code = false;
    private boolean reasonPhrase = false;
    private ArrayList<JsonElement> createdElements = new ArrayList<JsonElement>();
    private ArrayList<String> seenTags = new ArrayList<String>();
    private boolean isMd = false;
    
    /**
     * Gets the notifyContextRequest parsed object.
     * @return
     */
    public NotifyContextRequest getNotifyContextRequest() {
        return notifyContextRequest;
    } // getNotifyContextRequest
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("notifyContextRequest")) {
            notifyContextRequest = new NotifyContextRequest();
        } else if (qName.equalsIgnoreCase("subscriptionId")) {
            subscriptionId = true;
        } else if (qName.equalsIgnoreCase("originator")) {
            originator = true;
        } else if (qName.equalsIgnoreCase("contextResponseList")) {
            contextResponseList = new ArrayList<ContextElementResponse>();
        } else if (qName.equalsIgnoreCase("contextElementResponse")) {
            contextElementResponse = notifyContextRequest.new ContextElementResponse();
        } else if (qName.equalsIgnoreCase("contextElement")) {
            contextElement = notifyContextRequest.new ContextElement();
        } else if (qName.equalsIgnoreCase("entityId")) {
            entityId = notifyContextRequest.new EntityId();
            entityId.setIsPattern(attributes.getValue("isPattern"));
            entityId.setType(attributes.getValue("type"));
        } else if (qName.equalsIgnoreCase("id")) {
            id = true;
        } else if (qName.equalsIgnoreCase("contextAttributeList")) {
            contextAttributeList = new ArrayList<ContextAttribute>();
        } else if (qName.equalsIgnoreCase("contextAttribute")) {
            contextAttribute = notifyContextRequest.new ContextAttribute();
        } else if (qName.equalsIgnoreCase("name")) {
            name = true;
        } else if (qName.equalsIgnoreCase("type")) {
            type = true;
        } else if (qName.equalsIgnoreCase("value")) {
            if (isVector(attributes)) {
                createdElements.add(new JsonArray());
            } else {
                createdElements.add(new JsonObject()); // if finally it is not an object, this will be deleted
            } // if else
            
            value = true;
        } else if (qName.equalsIgnoreCase("contextValue")) {
            if (isVector(attributes)) {
                createdElements.add(new JsonArray());
            } else {
                createdElements.add(new JsonObject()); // if finally it is not an object, this will be deleted
            } // if else
            
            contextValue = true;
        } else if (qName.equalsIgnoreCase("metadata")) {
            contextMetadataList = new ArrayList<ContextMetadata>();
        } else if (qName.equalsIgnoreCase("contextMetadata")) {
            contextMetadata = notifyContextRequest.new ContextMetadata();
            isMd = true;
        } else if (qName.equalsIgnoreCase("statusCode")) {
            statusCode = notifyContextRequest.new StatusCode();
        } else if (qName.equalsIgnoreCase("code")) {
            code = true;
        } else if (qName.equalsIgnoreCase("reasonPhrase")) {
            reasonPhrase = true;
        } else { // other element different than a known one (including "item")
            JsonElement curr = null;
            
            if (isVector(attributes)) {
                curr = new JsonArray();
                createdElements.add(curr);
            } else {
                curr = new JsonObject();
                createdElements.add(curr); // if finally it is not an object, this will be deleted
            } // if else

            seenTags.add(qName);
        } // if else if
    } // startElement
 
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("contextResponseList")) {
            notifyContextRequest.setContextResponses(contextResponseList);
        } else if (qName.equalsIgnoreCase("contextElementResponse")) {
            contextResponseList.add(contextElementResponse);
        } else if (qName.equalsIgnoreCase("contextElement")) {
            // if contextAttributeList is null means that tag was not seen, and thus the contextElement attributes must
            // be set to null
            if (contextAttributeList == null) {
                contextElement.setAttributes(null);
            } // if
            
            contextElementResponse.setContextElement(contextElement);
        } else if (qName.equalsIgnoreCase("entityId")) {
            contextElement.setId(entityId.getId());
            contextElement.setType(entityId.getType());
            contextElement.setIsPattern(entityId.getIsPattern());
        } else if (qName.equalsIgnoreCase("contextAttributeList")) {
            contextElement.setAttributes(contextAttributeList);
        } else if (qName.equalsIgnoreCase("contextAttribute")) {
            contextAttributeList.add(contextAttribute);
        } else if (qName.equalsIgnoreCase("metadata")) {
            contextAttribute.setContextMetadata(contextMetadataList);
        } else if (qName.equalsIgnoreCase("contextMetadata")) {
            contextMetadataList.add(contextMetadata);
            isMd = false;
        } else if (qName.equalsIgnoreCase("statusCode")) {
            contextElementResponse.setStatusCode(statusCode);
        } else if (qName.equalsIgnoreCase("value")) {
            contextMetadata.setContextMetadata(createdElements.get(0));
            createdElements.remove(createdElements.size() - 1);
            value = false;
        } else if (qName.equalsIgnoreCase("contextValue")) {
            contextAttribute.setContextValue(createdElements.get(0));
            createdElements.remove(createdElements.size() - 1);
            contextValue = false;
        } else if (!qName.equalsIgnoreCase("notifyContextRequest") && !qName.equalsIgnoreCase("subscriptionId")
                && !qName.equalsIgnoreCase("originator") && !qName.equalsIgnoreCase("code")
                && !qName.equalsIgnoreCase("reasonPhrase") && !qName.equalsIgnoreCase("name")
                && !qName.equalsIgnoreCase("type") && !qName.equalsIgnoreCase("id")) {
            JsonElement last = createdElements.get(createdElements.size() - 1);
            JsonElement prev = createdElements.get(createdElements.size() - 2);
            String lastTag = seenTags.get(seenTags.size() - 1);
            
            if (prev instanceof JsonArray) {
                ((JsonArray) prev).add(last);
            } else {
                ((JsonObject) prev).add(lastTag, last);
            } // if else
            
            createdElements.remove(createdElements.size() - 1);
            seenTags.remove(seenTags.size() - 1);
        } // if else if
    } // endElement
 
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (subscriptionId) {
            notifyContextRequest.setSubscriptionId(new String(ch, start, length));
            subscriptionId = false;
        } else if (originator) {
            notifyContextRequest.setOriginator(new String(ch, start, length));
            originator = false;
        } else if (id) {
            entityId.setId(new String(ch, start, length));
            id = false;
        } else if (name) {
            if (isMd) {
                contextMetadata.setName(new String(ch, start, length));
            } else {
                contextAttribute.setName(new String(ch, start, length));
            } // if else
            
            name = false;
        } else if (type) {
            if (isMd) {
                contextMetadata.setType(new String(ch, start, length));
            } else {
                contextAttribute.setType(new String(ch, start, length));
            } // if else
            
            type = false;
        } else if (value && seenTags.isEmpty()) {
            // the last created element is not an object but a primitive
            createdElements.remove(createdElements.size() - 1);
            createdElements.add(new JsonPrimitive(new String(ch, start, length)));
        } else if (contextValue && seenTags.isEmpty()) {
            // the last created element is not an object but a primitive
            createdElements.remove(createdElements.size() - 1);
            createdElements.add(new JsonPrimitive(new String(ch, start, length)));
        } else if (code) {
            statusCode.setCode(new String(ch, start, length));
            code = false;
        } else if (reasonPhrase) {
            statusCode.setReasonPhrase(new String(ch, start, length));
            reasonPhrase = false;
        } else {
            JsonElement prev = createdElements.get(createdElements.size() - 1);
            JsonPrimitive curr = new JsonPrimitive(new String(ch, start, length));
            
            if (prev instanceof JsonArray) {
                ((JsonArray) prev).add(curr);
            } else {
                // the last created element is not an object but a primitive
                createdElements.remove(createdElements.size() - 1);
                createdElements.add(curr);
            } // if else
        } // if else if
    } // characters

    private boolean isVector(Attributes attributes) {
        String valueType = attributes.getValue("type");
            
        if (valueType != null) {
            if (valueType.equals("vector")) {
                return true;
            } // if
        } // if
        
        return false;
    } // is Vector
    
} // NotifyContextRequestSAXHandler
