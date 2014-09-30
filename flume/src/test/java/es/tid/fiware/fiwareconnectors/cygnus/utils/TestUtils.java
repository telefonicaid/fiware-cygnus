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

package es.tid.fiware.fiwareconnectors.cygnus.utils;

import com.google.gson.Gson;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest;
import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequestSAXHandler;
import java.io.StringReader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

/**
 *
 * @author frb
 */
public final class TestUtils {
    
    /**
     * Constructor. It is private since utility clasess should not have a public or default constructor.
     */
    private TestUtils() {
    } // TestUtils
    
    /**
     * Create a XML-based notificationContextRequest given the string representation of such XML.
     * @param xmlStr
     * @return The XML-based notificationContextRequest
     */
    public static NotifyContextRequest createXMLNotifyContextRequest(String xmlStr) {
        Logger logger = Logger.getLogger(Utils.class);
        NotifyContextRequest notification = null;
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            NotifyContextRequestSAXHandler handler = new NotifyContextRequestSAXHandler();
            saxParser.parse(new InputSource(new StringReader(xmlStr)), handler);
            notification = handler.getNotifyContextRequest();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } // try catch
        
        return notification;
    } // createXMLNotifyContextRequest
    
    /**
     * Create a Json-based notificationContextRequest given the string representation of such Json.
     * @param jsonStr
     * @return The Json-based notificationContextRequest
     */
    public static NotifyContextRequest createJsonNotifyContextRequest(String jsonStr) {
        Logger logger = Logger.getLogger(Utils.class);
        NotifyContextRequest notification = null;
        Gson gson = new Gson();

        try {
            notification = gson.fromJson(jsonStr, NotifyContextRequest.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } // try catch
        
        return notification;
    } // createJsonNotifyContextRequest
    
    /**
     * Encodes a string replacing all the non alphanumeric characters by '_'.
     * 
     * @param in
     * @return The encoded version of the input string.
     */
    public static String encode(String in) {
        return in.replaceAll("[^a-zA-Z0-9]", "_");
    } // encode
    
} // TestUtils
