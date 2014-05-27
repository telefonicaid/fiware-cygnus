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

import es.tid.fiware.fiwareconnectors.cygnus.containers.NotifyContextRequest;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author frb
 */
public class TestUtils {
    
    /**
     * Create a XML-based notificationContextRequest given the string representation of such XML.
     * @param xmlStr
     * @return The XML-based notificationContextRequest
     */
    public static NotifyContextRequest createXMLNotifyContextRequest(String xmlStr) {
        Logger logger = Logger.getLogger(Utils.class);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logger.fatal(e.getMessage());
        } // try catch

        InputSource is = new InputSource(new StringReader(xmlStr));
        Document doc = null;
        
        try {
            doc = dBuilder.parse(is);
        } catch (SAXException e) {
            logger.fatal(e.getMessage());
        } catch (IOException e) {
            logger.fatal(e.getMessage());
        } // try catch
        
        doc.getDocumentElement().normalize();
        NotifyContextRequest instance = null;
        
        try {
            instance = new NotifyContextRequest(doc);
        } catch (Exception e) {
            logger.fatal(e.getMessage());
        } // try catch
        
        return instance;
    } // createXMLNotifyContextRequest
    
} // TestUtils
