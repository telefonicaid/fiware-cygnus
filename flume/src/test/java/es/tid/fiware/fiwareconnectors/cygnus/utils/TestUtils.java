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
     * Constructor. It is private because utility classes should not have a public constructor.
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
            InputSource is = new InputSource(new StringReader(xmlStr));
            saxParser.parse(is, handler);
            notification = handler.getNotifyContextRequest();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } // try catch
        
        return notification;
    } // createXMLNotifyContextRequest
    
} // TestUtils
