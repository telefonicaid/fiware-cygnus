/**
* Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
*
* This file is part of fiware-connectors (FI-WARE project).
*
* cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
* Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
* later version.
* cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
* warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
* http://www.gnu.org/licenses/.
*
* For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
* frb@tid.es
*/

package es.tid.fiware.fiwareconnectors.cygnus.containers;

import com.google.gson.JsonElement;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import es.tid.fiware.fiwareconnectors.cygnus.utils.Utils;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
*
* @author frb
*
* Container classes mapping an Orion Context Broker nofifyContextRequest notification. These are necessaries in order
* Gson (a Json parser) and DOM (a XML parser) can store in memory a notification.
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
    
    /**
     * Constructor for DOM, a XML parser.
     *
     * @param doc
     * @throws Exception
     */
    public NotifyContextRequest(Document doc) throws Exception {
        NodeList domSubscriptionIds = doc.getElementsByTagName("subscriptionId");
        
        if (domSubscriptionIds.getLength() == 0) {
            throw new Exception("No <subscriptionId> tag in the XML document");
        } // if
        
        subscriptionId = domSubscriptionIds.item(0).getTextContent();
        NodeList domOriginators = doc.getElementsByTagName("originator");
        
        if (domOriginators.getLength() == 0) {
            throw new Exception();
        } // if
        
        originator = domOriginators.item(0).getTextContent();
        NodeList domContextResponseLists = doc.getElementsByTagName("contextResponseList");
        
        if (domContextResponseLists.getLength() == 0) {
            throw new Exception("No <contextResponseList> tag in the XML document");
        } // if
        
        Element contextResponseList = (Element) domContextResponseLists.item(0);
        NodeList domContextElementResponses = contextResponseList.getElementsByTagName("contextElementResponse");
        contextResponses = new ArrayList<ContextElementResponse>();
        
        for (int i = 0; i < domContextElementResponses.getLength(); i++) {
            contextResponses.add(new ContextElementResponse((Element) domContextElementResponses.item(i)));
        } // for
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
        
        /**
         * Constructor for DOM, a XML parser.
         *
         * @param domContextElementResponse
         */
        public ContextElementResponse(Element domContextElementResponse) throws Exception {
            NodeList domContextElements = domContextElementResponse.getElementsByTagName("contextElement");

            if (domContextElements.getLength() == 0) {
                throw new Exception("No <contextElement> tag in the XML document");
            } // if

            contextElement = new ContextElement((Element) domContextElements.item(0));
            NodeList domStatusCodes = domContextElementResponse.getElementsByTagName("statusCode");

            if (domStatusCodes.getLength() == 0) {
                throw new Exception("No <statusCode> tag in the XML document");
            } // if

            statusCode = new StatusCode((Element) domStatusCodes.item(0));
        } // ContextElementResponse
        
        public ContextElement getContextElement() {
            return contextElement;
        } // getContextElement
        
        public StatusCode getStatusCode() {
            return statusCode;
        } // getStatusCode
        
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
        
        /**
         * Constructor for DOM, a XML parser.
         *
         * @param domContextElement
         * @throws Exception
         */
        public ContextElement(Element domContextElement) throws Exception {
            NodeList domIdentityIds = domContextElement.getElementsByTagName("entityId");
            
            if (domIdentityIds.getLength() == 0) {
                throw new Exception("No <entityId> tag in the XML document");
            } // if
            
            type = domIdentityIds.item(0).getAttributes().getNamedItem("type").getTextContent();
            isPattern = domIdentityIds.item(0).getAttributes().getNamedItem("isPattern").getTextContent();
            NodeList domIds = ((Element) domIdentityIds.item(0)).getElementsByTagName("id");
            
            if (domIds.getLength() == 0) {
                throw new Exception("No <id> tag in the XML document");
            } // if
            
            id = domIds.item(0).getTextContent();
            NodeList domContextAttributeLists = domContextElement.getElementsByTagName("contextAttributeList");
            
            if (domContextAttributeLists.getLength() == 0) {
                throw new Exception("No <contextAttributeList> tag in the XML document");
            } // if

            NodeList domContextAttributes = ((Element) domContextAttributeLists.item(0)).
                    getElementsByTagName("contextAttribute");
            attributes = new ArrayList<ContextAttribute>();
            
            for (int i = 0; i < domContextAttributes.getLength(); i++) {
                attributes.add(new ContextAttribute((Element) domContextAttributes.item(i)));
            } // for
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
        
    } // ContextElement
    
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
        
        /**
         * Constructor for DOM, a XML parser.
         *
         * @param domContextAttribute
         * @exception
         */
        public ContextAttribute(Element domContextAttribute) throws Exception {
            NodeList domNames = domContextAttribute.getElementsByTagName("name");
            
            if (domNames.getLength() == 0) {
                throw new Exception("No <name> tag in the XML document");
            } // if
            
            name = domNames.item(0).getTextContent();
            NodeList domTypes = domContextAttribute.getElementsByTagName("type");
            
            if (domTypes.getLength() == 0) {
                throw new Exception("No <type> tag in the XML document");
            } // if
            
            type = domTypes.item(0).getTextContent();
            NodeList domValues = domContextAttribute.getElementsByTagName("contextValue");
            
            if (domValues.getLength() == 0) {
                throw new Exception("No <contextValue> tag in the XML document");
            } // if

            value = Utils.basicXml2Json(domValues.item(0));
            metadatas = new ArrayList<ContextMetadata>();

            NodeList domMetadata = domContextAttribute.getElementsByTagName("metadata");
            
            if (domMetadata.getLength() == 0) {
                return;
            } // if

            NodeList domContextMetadata = ((Element) domMetadata.item(0)).
                    getElementsByTagName("contextMetadata");
            
            if (domMetadata.getLength() == 0) {
                return;
            } // if
            
            for (int i = 0; i < domContextMetadata.getLength(); i++) {
                metadatas.add(new ContextMetadata((Element) domContextMetadata.item(i)));
            } // for
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
                return Constants.EMPTY_MD;
            } // if
            
            if (metadatas.isEmpty()) {
                return Constants.EMPTY_MD;
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
        
        /**
         * Constructor for DOM, a XML parser.
         *
         * @param domContextAttribute
         * @exception
         */
        public ContextMetadata(Element domContextMetadata) throws Exception {
            NodeList domNames = domContextMetadata.getElementsByTagName("name");
            
            if (domNames.getLength() == 0) {
                throw new Exception("No <name> tag in the XML document");
            } // if
            
            name = domNames.item(0).getTextContent();
            NodeList domTypes = domContextMetadata.getElementsByTagName("type");
            
            if (domTypes.getLength() == 0) {
                throw new Exception("No <type> tag in the XML document");
            } // if
            
            type = domTypes.item(0).getTextContent();
            NodeList domValues = domContextMetadata.getElementsByTagName("value");
            
            if (domValues.getLength() == 0) {
                throw new Exception("No <value> tag in the XML document");
            } // if
            
            value = Utils.basicXml2Json(domValues.item(0));
        } // ContextAttribute
        
        public String getName() {
            return name;
        } // getName
        
        public String getType() {
            return type;
        } // getType
        
        /**
         * Gets metadata value.
         * @param asStringRepresentation
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
        
        /**
         * Constructor for DOM, a SML parser.
         *
         * @param domStatusCode
         * @throws Exception
         */
        public StatusCode(Element domStatusCode) throws Exception {
            NodeList domCodes = domStatusCode.getElementsByTagName("code");

            if (domCodes.getLength() == 0) {
                throw new Exception("No <code> tag in the XML document");
            } // if

            code = domCodes.item(0).getTextContent();
            NodeList domReasonPhrases = domStatusCode.getElementsByTagName("reasonPhrase");

            if (domReasonPhrases.getLength() == 0) {
                throw new Exception("No <reasonPhrase> tag in the XML document");
            } // if

            reasonPhrase = domReasonPhrases.item(0).getTextContent();
        } // StatusCode
        
        public String getCode() {
            return code;
        } // getCode
        
        public String getReasonPhrase() {
            return reasonPhrase;
        } // getReasonPhrase
        
    } // StatusCode
    
} // NotifyContextRequest