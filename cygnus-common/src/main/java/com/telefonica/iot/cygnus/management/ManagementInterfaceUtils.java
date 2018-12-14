/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;
import org.slf4j.MDC;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import com.telefonica.iot.cygnus.utils.CommonConstants;

/**
 *
 * @author pcoello25
 */
public class ManagementInterfaceUtils {

    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private ManagementInterfaceUtils() {
    } // CommonUtils
    
    public static String setCorrelator (HttpServletRequest request) {
        // Get an internal transaction ID.
        String transId = CommonUtils.generateUniqueId(null, null);

        // Get also a correlator ID if not sent in the notification. Id correlator ID is not notified
        // then correlator ID and transaction ID must have the same value.
        String corrId = CommonUtils.generateUniqueId
           (request.getHeader(CommonConstants.HEADER_CORRELATOR_ID), transId);

        // set the given header to the response or create it
        MDC.put(CommonConstants.LOG4J_CORR, corrId);
        MDC.put(CommonConstants.LOG4J_TRANS, transId);
        return corrId;
    } // setCorrelator
    
    /** 
     * OrderedPrintWriter: Creates a writer with the original order's agent file.
     * 
     * @param properties 
     * @param file 
     * @throws java.io.FileNotFoundException 
     */
    public static void orderedPrinting(Properties properties, File file) throws FileNotFoundException {
                
        PrintWriter printWriter = new PrintWriter(file);
        String agentName = "";
        
        for (Object key : properties.keySet()) {
            String name = (String) key;
            String[] nameParts = name.split("\\.");
            agentName = nameParts[0];
            break;
        } // for
                
        ArrayList<String> sourceNames = null;
        ArrayList<String> channelNames = null;
        ArrayList<String> sinkNames=  null;
        
        for (Object key : properties.keySet()) {
            String name = (String) key;
            String value = (String) properties.getProperty(name);
            
            if (name.equals(agentName + ".sources")) {
                sourceNames = new ArrayList<String>(Arrays.asList(value.split("\\s+")));
                printWriter.println(name + " = " + value);
            } // if
            
            if (name.equals(agentName + ".channels")) {
                channelNames = new ArrayList<String>(Arrays.asList(value.split("\\s+")));
                printWriter.println(name + " = " + value);
            } // if
            
            if (name.equals(agentName + ".sinks")) {
                sinkNames = new ArrayList<String>(Arrays.asList(value.split("\\s+")));
                printWriter.println(name + " = " + value);
            } // if
            
        } // for
        
        printWriter.println();
        
        for (String sourceName : sourceNames) {
            
            for (Object key : properties.keySet()) {
                String name = (String) key;
                String value = (String) properties.getProperty(name);
                
                if (name.startsWith(agentName + ".sources." + sourceName)) {
                    printWriter.println(name + " = " + value);
                } // if
                
            } // for
            
            printWriter.println();

        } // for
        
        for (String channelName : channelNames) {
            
            for (Object key : properties.keySet()) {
                String name = (String) key;
                String value = (String) properties.getProperty(name);
                
                if (name.startsWith(agentName + ".channels." + channelName)) {
                    printWriter.println(name + " = " + value);
                } // if
                
            } // for
            
            printWriter.println();
            
        } // for
        
        for (String sinkName : sinkNames) {
            
            for (Object key : properties.keySet()) {
                String name = (String) key;
                String value = (String) properties.getProperty(name);
                
                if (name.startsWith(agentName + ".sinks." + sinkName)) {
                    printWriter.println(name + " = " + value);
                } // if
                
            } // for
            
            printWriter.println();
            
        } // for
        
        printWriter.close();     
    } // orderedPrinting
    
    /** 
     * instancePrinting: Creates a writer with for print an instance.
     * 
     * @param properties 
     * @param file 
     * @param descriptions 
     * @throws java.io.FileNotFoundException 
     */
    public static void instancePrinting (Properties properties, File file, Map<String,String> descriptions) 
            throws FileNotFoundException {
                
        PrintWriter printWriter = new PrintWriter(file);      
        printWriter.println(CommonConstants.CYGNUS_IPR_HEADER);
        printWriter.println();
        
        for (Object key : properties.keySet()) {
            String name = (String) key;
            String value = (String) properties.getProperty(name);
            
            if (descriptions.containsKey(name)) {
                String description = (String) descriptions.get(name);
                printWriter.print(description);
            } // if 
            
            printWriter.println(name + "=" + value);
            printWriter.println();
        } // for
        
        printWriter.close();
        
    } // instancePrinting
    
    /** 
    * getFileName: Gets the name of the agent from the given path.
    * 
    * @param url 
    * @return  
    */
    public static String getFileName (String url) {
        String[] pathElements = url.split("/");
        return pathElements[pathElements.length - 1];
    } // getFileName
    
    /**
     * readDescriptions: Read commented lines from a file and store in a LinkedHashMap
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static Map<String,String> readDescriptions(File file) throws IOException {
                
        // read the comments and the properties
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String header = "";
        String description = "";
        String line;
        Map<String,String> descriptions = new LinkedHashMap<String,String>();

        while ((line = reader.readLine()) != null) {
            
            if (line.startsWith("#")) {
                description += line + "\n";
            } // if
            
            if (line.isEmpty()) {
                description = "";
            } else if ((!(line.startsWith("#")) && (!(line.isEmpty())))) {
                String[] nameValue = line.split("=");
                String name = nameValue[0];
                descriptions.put(name, description);
                description = "";
            } // if
            
        } // while
        
        reader.close();
        return descriptions;
    } // readDescriptions
    
    /**
     * getStringAppender: Returns the active appender 
     * 
     * @param appenders
     * @return 
     */
    public static String getStringAppender (Enumeration appenders) {
        Appender appender = (Appender) appenders.nextElement();
        String name = appender.getName();
        PatternLayout layout = (PatternLayout) appender.getLayout();
        String appendersJson = "[{\"name\":\"" + name + "\",\"layout\":\"" 
                + layout.getConversionPattern() + "\",\"active\":\"true\"}]";   
        return appendersJson;
    } // getStringAppender
    
    /**
     * getAppendersFromProperties: Returns an ArrayList with the appenders.
     * 
     * @param properties
     * @return 
     *
     */
    public static ArrayList<String> getAppendersFromProperties (Properties properties) {
        ArrayList<String> appendersName = new ArrayList<String>();
        
        for (Object property: properties.keySet()) {
            String name = (String) property;
            
            if (name.startsWith("log4j.appender.")) {
                String[] splitAppender = name.split("\\.");
                String appender = splitAppender[2];
                
                if (!appendersName.contains(appender)) {
                    appendersName.add(appender);
                } // if
                
            } // if
            
        } // for
        
        return appendersName;
    } // getAppendersFromProperties
	
	/**
     * getLoggersFromProperties: Returns an ArrayList with the loggers.
     * 
     * @param properties
     * @return 
     *
     */
    public static ArrayList<String> getLoggersFromProperties (Properties properties) {
        ArrayList<String> appendersName = new ArrayList<String>();
        
        for (Object property: properties.keySet()) {
            String name = (String) property;
            
            if (name.startsWith("log4j.logger.")) {
                String[] splitAppender = name.split("\\.");
                String appender = "";
                int length = splitAppender.length;
                
                for (int i=2; i < length; i++) {
                    appender += splitAppender[i];
                    
                    if (i < (length-1)) {
                        appender += ".";
                    } // if
                    
                } // for
                
                if (!appendersName.contains(appender)) {
                    appendersName.add(appender);
                } // if
                
            } // if
            
        } // for
        
        return appendersName;
    } // getLoggersFromProperties
	
    /** 
     * readLogDescriptions: Read the descriptions from a log4j file.
     * 
     * @param file
     * @return 
     * @throws java.io.IOException
     */
    public static Map<String,String> readLogDescriptions(File file) throws IOException {
                
        // read the comments and the properties
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String description = "";
        String line;
        Map<String,String> descriptions = new LinkedHashMap<String,String>();
        boolean flag = true;

        while ((line = reader.readLine()) != null) {
                        
            if (line.startsWith("#")) {
                description += line + "\n";
                flag = true;
            } // if
            
            if (line.isEmpty()) {
                description = "";
                flag = true;
            } else if ((!(line.startsWith("#")) && (!(line.isEmpty())))) {
                
                if (flag == true) {
                    String[] appenderFields = line.split("(\\.)|(=)");
                    String name = appenderFields[0] + "." + appenderFields[1];
                
                    if (appenderFields[1].equals("appender")) {
                        name += "." + appenderFields[2];
                    } // if
                    
                    descriptions.put(name, description);
                    description = "";
                    flag = false;
                } // if

            } // if else if
            
        } // while
        
        reader.close();
        return descriptions;
    } // readLogDescriptions
    
    /** 
     * OrderedPrintWriter: Creates a writer with the original order's log4j file.
     * 
     * @param properties 
     * @param descriptions 
     * @param file 
     * @throws java.io.FileNotFoundException 
     */
    public static void orderedLogPrinting (Properties properties, Map<String,String> descriptions, File file) 
            throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(file);
        printWriter.println(CommonConstants.CYGNUS_IPR_HEADER + "\n");      
        printWriter.println("# To be put in APACHE_FLUME_HOME/conf/log4j.properties \n");
        
        for (Object description : descriptions.keySet()) {
            String name = (String) description;
            String desc = (String) descriptions.get(name);
                    
            if (!properties.keySet().isEmpty()) {
                printWriter.print(desc);
            } // if
            
            for (Object property: properties.keySet()) {
                String prop = (String) property;
                String value = (String) properties.getProperty(prop);
                
                if ((prop.equals(name)) || (prop.startsWith(name))) {
                    printWriter.println(prop + "=" + value);
                } // if
                
            } // for
            
            printWriter.println();
 
        } // for
        
        printWriter.close();
        
    } // orderedLogPrinting
   
} // ManagementInterfaceUtils
