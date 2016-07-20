/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.telefonica.iot.cygnus.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

/**
 *
 * @author pcoello25
 */
public final class ManagementInterfaceUtils {

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
   
} // ManagementInterfaceUtils
