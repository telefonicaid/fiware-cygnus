/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
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

import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.google.gson.JsonPrimitive;
import com.mongodb.binding.AsyncReadWriteBinding;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.Enums;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYATTRIBUTE;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYENTITY;
import static com.telefonica.iot.cygnus.sinks.Enums.DataModel.DMBYSERVICEPATH;
import com.mysql.jdbc.Driver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import kafka.common.OffsetOutOfRangeException;
import kafka.tools.KafkaMigrationTool;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.flume.interceptor.RegexExtractorInterceptorMillisSerializer;
import org.apache.hive.jdbc.HivePreparedStatement;
import org.apache.hadoop.hive.ql.exec.AbstractMapJoinOperator;
import org.apache.hadoop.metrics.spi.AbstractMetricsContext;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.log4j.pattern.SequenceNumberPatternConverter;
import org.codehaus.groovy.control.ErrorCollector;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.Yytoken;
import org.postgresql.largeobject.BlobOutputStream;
import parquet.org.apache.thrift.meta_data.ListMetaData;

/**
 *
 * @author frb
 */
public final class CommonUtils {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(CommonUtils.class);
    private static final DateTimeFormatter FORMATTER1 = DateTimeFormat.forPattern(
            "yyyy-MM-dd'T'HH:mm:ss'Z'").withOffsetParsed().withZoneUTC();
    private static final DateTimeFormatter FORMATTER2 = DateTimeFormat.forPattern(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withOffsetParsed().withZoneUTC();
    private static final DateTimeFormatter FORMATTER3 = DateTimeFormat.forPattern(
            "yyyy-MM-dd HH:mm:ss").withOffsetParsed().withZoneUTC();
    private static final DateTimeFormatter FORMATTER4 = DateTimeFormat.forPattern(
            "yyyy-MM-dd HH:mm:ss.SSS").withOffsetParsed().withZoneUTC();
    private static final DateTimeFormatter FORMATTER5 = DateTimeFormat.forPattern(
            "yyyy-MM-dd'T'HH:mm:ssZ").withOffsetParsed();
    private static final DateTimeFormatter FORMATTER6 = DateTimeFormat.forPattern(
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ").withOffsetParsed();
    private static final Pattern FORMATTER6_PATTERN = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})\\.(\\d+)([+-][\\d:]+)$");

    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9_]*$");
    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private CommonUtils() {
    } // CommonUtils
    
    /**
     * Encodes a string from an ArrayList.
     * @param in
     * @return The encoded string
     */
    public static String toString(ArrayList in) {
        String out = "";
        
        for (int i = 0; i < in.size(); i++) {
            if (i == 0) {
                out = in.get(i).toString();
            } else {
                out += "," + in.get(i).toString();
            } // if else
        } // for
        
        return out;
    } // toString
    
    /**
     * Gets the Cygnus version from the pom.xml.
     * @return The Cygnus version
     */
    public static String getCygnusVersion() {
        InputStream stream = CommonUtils.class.getClassLoader().getResourceAsStream("pom.properties");
        
        if (stream == null) {
            return "UNKNOWN";
        } // if
        
        Properties props = new Properties();
        
        try {
            props.load(stream);
            stream.close();
            return (String) props.get("version");
        } catch (IOException e) {
            return "UNKNOWN";
        } // try catch
    } // getCygnusVersion
    
    /**
     * Gets the hash regarding the last Git commit.
     * @return The hash regarding the last Git commit.
     */
    public static String getLastCommit() {
        InputStream stream = CommonUtils.class.getClassLoader().getResourceAsStream("last_git_commit.txt");
        
        if (stream == null) {
            return "UNKNOWN";
        } // if
        
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            return reader.readLine();
        } catch (Exception e) {
            return "UNKNOWN";
        } // catch
    } // getLastCommit
    
    /**
     * Gets the human redable version of timestamp expressed in miliseconds.
     * @param ts
     * @param addUTC
     * @return
     */
    public static String getHumanReadable(long ts, boolean addUTC) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String humanRedable = sdf.format(new Date(ts));
        humanRedable += (addUTC ? "T" : " ");
        sdf = new SimpleDateFormat("HH:mm:ss.S");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        humanRedable += sdf.format(new Date(ts)) + (addUTC ? "Z" : "");
        return humanRedable;
    } // getHumanRedable
    
    /**
     * Gets the milliseconds version of the given timestamp.
     * @param timestamp
     * @return The milliseconds version of the given timestamp
     * @throws java.text.ParseException
     */
    public static long getMilliseconds(String timestamp) throws java.text.ParseException {
        return DatatypeConverter.parseDateTime(timestamp).getTime().getTime();
    } // getMilliseconds
    
    /**
     * Decides if a given string is a number.
     * http://stackoverflow.com/questions/5439529/determine-if-a-string-is-an-integer-in-java
     * @param s
     * @return
     */
    public static boolean isANumber(String s) {
        return isANumber(s, 10);
    } // isANumber

    /**
     * Decides if a given string is a number, given its radix.
     * http://stackoverflow.com/questions/5439529/determine-if-a-string-is-an-integer-in-java
     * @param s
     * @param radix
     * @return
     */
    public static boolean isANumber(String s, int radix) {
        if (s.isEmpty()) {
            return false;
        } // if
        
        for (int i = 0; i < s.length(); i++) {
            if ((i == 0) && (s.charAt(i) == '-')) {
                if (s.length() == 1) {
                    return false;
                } // if
                
                continue;
            } // if
            
            if ((i == 0) && (s.charAt(i) == '.')) {
                return false;
            } // if
            
            if ((i == s.length() - 1) && (s.charAt(i) == '.')) {
                return false;
            } // if
            
            if (s.charAt(i) == '.') {
                continue;
            } // if
            
            if (Character.digit(s.charAt(i), radix) < 0) {
                return false;
            } // if
            
        } // for
        
        return true;
    } // isANumber
    
    /**
     * Gets a string reprensentation of a given data model.
     * @param dataModel
     * @return The string representation of the given data model
     */
    public static String getStrDataModel(Enums.DataModel dataModel) {
        switch(dataModel) {
            case DMBYSERVICEPATH:
                return "dm-by-service-path";
            case DMBYENTITY:
                return "dm-by-entity";
            case DMBYENTITYTYPE:
                return "dm-by-entity-type";
            case DMBYATTRIBUTE:
                return "dm-by-attribute";
            default:
                return null;
        } // switch
    } // getStrDataModel
        
    /**
     * Gets the timestamp within a TimeInstant metadata, if exists.
     * @param metadata
     * @return The timestamp within a TimeInstant metadata
     */
    public static Long getTimeInstant(String metadata) {
        Long res = null;
        JSONParser parser = new JSONParser();
        JSONArray mds;
        
        try {
            mds = (JSONArray) parser.parse(metadata);
        } catch (ParseException e) {
            LOGGER.error("Error while parsing the metadaga. Details: " + e.getMessage());
            return null;
        } // try catch
        
        for (Object mdObject : mds) {
            JSONObject md = (JSONObject) mdObject;
            String mdName = (String) md.get("name");
            
            if (mdName.equals("TimeInstant")) {
                String mdValue = (String) md.get("value");
                
                if (isANumber(mdValue)) {
                    res = new Long(mdValue);
                } else {
                    DateTime dateTime;
                    
                    try {
                        // ISO 8601 without miliseconds
                        dateTime = FORMATTER1.parseDateTime(mdValue);
                    } catch (Exception e1) {
                        LOGGER.debug(e1.getMessage());
                        
                        try {
                            // ISO 8601 with miliseconds
                            dateTime = FORMATTER2.parseDateTime(mdValue);
                        } catch (Exception e2) {
                            LOGGER.debug(e2.getMessage());
                            
                            try {
                                // ISO 8601 with microsencods
                                String mdValueTruncated = mdValue.substring(0, mdValue.length() - 4) + "Z";
                                dateTime = FORMATTER2.parseDateTime(mdValueTruncated);
                            } catch (Exception e3) {
                                LOGGER.debug(e3.getMessage());
                                
                                try {
                                    // SQL timestamp without miliseconds
                                    dateTime = FORMATTER3.parseDateTime(mdValue);
                                } catch (Exception e4) {
                                    LOGGER.debug(e4.getMessage());

                                    try {
                                        // SQL timestamp with miliseconds
                                        dateTime = FORMATTER4.parseDateTime(mdValue);
                                    } catch (Exception e5) {
                                        LOGGER.debug(e5.getMessage());
                                        
                                        try {
                                            // SQL timestamp with microseconds
                                            String mdValueTruncated = mdValue.substring(0, mdValue.length() - 3);
                                            dateTime = FORMATTER4.parseDateTime(mdValueTruncated);
                                        } catch (Exception e6) {
                                            LOGGER.debug(e6.getMessage());

                                            try {
                                                // ISO 8601 with offset (without milliseconds)
                                                dateTime = FORMATTER5.parseDateTime(mdValue);
                                            } catch (Exception e7) {
                                                LOGGER.debug(e7.getMessage());

                                                try {
                                                    // ISO 8601 with offset (with milliseconds)
                                                    Matcher matcher = FORMATTER6_PATTERN.matcher(mdValue);
                                                    if (matcher.matches()) {
                                                        String mdValueTruncated = matcher.group(1) + "."
                                                          + matcher.group(2).substring(0, 3)
                                                          + matcher.group(3);
                                                        dateTime = FORMATTER6.parseDateTime(mdValueTruncated);
                                                    } else {
                                                        LOGGER.debug("ISO8601 format does not match");
                                                        return null;
                                                    } // if
                                                } catch (Exception e8) {
                                                    LOGGER.debug(e8.getMessage());
                                                    return null;
                                                } // try catch
                                            } // try catch
                                        } // try catch
                                    } // try catch
                                } // try catch
                            } // try catch
                        } // try catch
                    } // try catch

                    GregorianCalendar cal = dateTime.toGregorianCalendar();
                    res = cal.getTimeInMillis();
                } // if else
                
                break;
            } // if
        } // for
        
        return res;
    } // getTimeInstant
    
    /**
     * Gets is a string is made of alphanumerics and/or underscores.
     * @param s
     * @return True is the string is made of alphanumerics and/or underscores, otherwise false.
     */
    public static boolean isMAdeOfAlphaNumericsOrUnderscores(String s) {
        return PATTERN.matcher(s).matches();
    } // isMAdeOfAlphaNumericsOrUnderscores
    
    /**
     * Generates a new unique identifier. The format for this notifiedId is:
     * bootTimeSecond-bootTimeMilliseconds-transactionCount%10000000000
     * @param notifiedId An alrady existent identifier, most probably a notified one
     * @param transactionId An already existent internal identifier
     * @return A new unique identifier
     */
    public static String generateUniqueId(String notifiedId, String transactionId) {
        if (notifiedId != null) {
            return notifiedId;
        } else if (transactionId != null) {
            return transactionId;
        } else {
            return UUID.randomUUID().toString();
        } // else
    } // generateUniqueId
        
    /**
     * Only works in DEBUG level.
     * Prints the loaded .jar files at the start of Cygnus run.
     */
    public static void printLoadedJars() {
        // trace the file containing the httpclient library
        URL myClassURL = PoolingClientConnectionManager.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading httpclient from " + myClassURL.toExternalForm());
        
        // trace the file containing the httpcore library
        myClassURL = DefaultBHttpServerConnection.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading httpcore from " + myClassURL.toExternalForm());
        
        // trace the file containing the junit library
        myClassURL = ErrorCollector.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading junit from " + myClassURL.toExternalForm());
        
        // trace the file containing the flume-ng-node library
        myClassURL =
                RegexExtractorInterceptorMillisSerializer.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading flume-ng-node from " + myClassURL.toExternalForm());
        
        // trace the file containing the libthrift library
        myClassURL = ListMetaData.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading libthrift from " + myClassURL.toExternalForm());
        
        // trace the file containing the gson library
        myClassURL = JsonPrimitive.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading gson from " + myClassURL.toExternalForm());
        
        // trace the file containing the json-simple library
        myClassURL = Yytoken.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading json-simple from " + myClassURL.toExternalForm());
        
        // trace the file containing the mysql-connector-java library
        myClassURL = Driver.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading mysql-connector-java from " + myClassURL.toExternalForm());
        
        // trace the file containing the postgresql library
        myClassURL = BlobOutputStream.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading postgresql from " + myClassURL.toExternalForm());
        
        // trace the file containing the log4j library
        myClassURL = SequenceNumberPatternConverter.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading log4j from " + myClassURL.toExternalForm());
        
        // trace the file containing the hadoop-core library
        myClassURL = AbstractMetricsContext.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading hadoop-core from " + myClassURL.toExternalForm());
        
        // trace the file containing the hive-exec library
        myClassURL = AbstractMapJoinOperator.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading hive-exec from " + myClassURL.toExternalForm());
        
        // trace the file containing the hive-jdbc library
        myClassURL = HivePreparedStatement.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading hive-jdbc from " + myClassURL.toExternalForm());
        
        // trace the file containing the mongodb-driver library
        myClassURL = AsyncReadWriteBinding.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading mongodb-driver from " + myClassURL.toExternalForm());
        
        // trace the file containing the kafka-clients library
        myClassURL = OffsetOutOfRangeException.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading kafka-clientsc from " + myClassURL.toExternalForm());
        
        // trace the file containing the zkclient library
        myClassURL = ZkNoNodeException.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading zkclient from " + myClassURL.toExternalForm());
        
        // trace the file containing the kafka_2.11 library
        myClassURL = KafkaMigrationTool.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading kafka_2.11 from " + myClassURL.toExternalForm());
        
        // trace the file containing the aws-java-sdk-dynamodb library
        myClassURL = WriteRequest.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.debug("Loading aws-java-sdk-dynamodb from " + myClassURL.toExternalForm());

    } // printLoadedJars

} // CommonUtils
