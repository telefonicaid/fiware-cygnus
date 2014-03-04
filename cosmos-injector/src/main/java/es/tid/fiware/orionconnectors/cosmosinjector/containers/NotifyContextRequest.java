package es.tid.fiware.orionconnectors.cosmosinjector.containers;

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
            throw new Exception();
        } // if
        
        subscriptionId = domSubscriptionIds.item(0).getTextContent();
        NodeList domOriginators = doc.getElementsByTagName("originator");
        
        if (domOriginators.getLength() == 0) {
            throw new Exception();
        } // if
        
        originator = domOriginators.item(0).getTextContent();
        NodeList domContextResponseLists = doc.getElementsByTagName("contextResponseList");
        
        if (domContextResponseLists.getLength() == 0) {
            throw new Exception();
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
    
    public ArrayList<ContextElementResponse> getContextResponse() {
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
                throw new Exception();
            } // if

            contextElement = new ContextElement((Element) domContextElements.item(0));
            NodeList domStatusCodes = domContextElementResponse.getElementsByTagName("statusCode");

            if (domStatusCodes.getLength() == 0) {
                throw new Exception();
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
                throw new Exception();
            } // if
            
            type = domIdentityIds.item(0).getAttributes().getNamedItem("type").getTextContent();
            isPattern = domIdentityIds.item(0).getAttributes().getNamedItem("isPattern").getTextContent();
            NodeList domIds = ((Element) domIdentityIds.item(0)).getElementsByTagName("id");
            
            if (domIds.getLength() == 0) {
                throw new Exception();
            } // if
            
            id = domIds.item(0).getTextContent();
            NodeList domContextAttributeLists = domContextElement.getElementsByTagName("contextAttributeList");
            attributes = new ArrayList<ContextAttribute>();
            
            for (int i = 0; i < domContextAttributeLists.getLength(); i++) {
                attributes.add(new ContextAttribute((Element) domContextAttributeLists.item(i)));
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
        private String value;
        
        /**
         * Constructor for Gson, a Json parser.
         */
        public ContextAttribute() {
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
                throw new Exception();
            } // if
            
            name = domNames.item(0).getTextContent();
            NodeList domTypes = domContextAttribute.getElementsByTagName("type");
            
            if (domTypes.getLength() == 0) {
                throw new Exception();
            } // if
            
            type = domTypes.item(0).getTextContent();
            NodeList domValues = domContextAttribute.getElementsByTagName("contextValue");
            
            if (domValues.getLength() == 0) {
                throw new Exception();
            } // if
            
            value = domValues.item(0).getTextContent();
        } // ContextAttribute
        
        public String getName() {
            return name;
        } // gertName
        
        public String getType() {
            return type;
        } // getType
        
        public String getContextValue() {
            return value;
        } // getContextValue
        
    } // ContextAttribute
    
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
                throw new Exception();
            } // if

            code = domCodes.item(0).getTextContent();
            NodeList domReasonPhrases = domStatusCode.getElementsByTagName("reasonPhrase");

            if (domReasonPhrases.getLength() == 0) {
                throw new Exception();
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
