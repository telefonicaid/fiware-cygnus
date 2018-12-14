/**
 * Copyright 2017 Telefonica Investigación y Desarrollo, S.A.U
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

import com.google.common.collect.ImmutableMap;
import com.telefonica.iot.cygnus.channels.CygnusChannel;
import com.telefonica.iot.cygnus.handlers.CygnusHandler;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.CygnusSink;
import com.telefonica.iot.cygnus.utils.CommonUtils;
import java.io.IOException;
import java.lang.reflect.Field;
import javax.servlet.http.HttpServletResponse;
import org.apache.flume.Channel;
import org.apache.flume.Sink;
import org.apache.flume.SinkProcessor;
import org.apache.flume.SinkRunner;
import org.apache.flume.Source;
import org.apache.flume.SourceRunner;
import org.apache.flume.source.http.HTTPSourceHandler;

/**
 *
 * @author frb
 */
public final class StatsHandlers {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(StatsHandlers.class);
    
    /**
     * Constructor. Utility classes should not have a public or default constructor.
     */
    private StatsHandlers() {
    } // StatsHandlers
    
    /**
     * Handles GET /v1/stats.
     * @param response
     * @param sources
     * @param channels
     * @param sinks
     * @throws IOException
     */
    public static void get(HttpServletResponse response, ImmutableMap<String, SourceRunner> sources,
            ImmutableMap<String, Channel> channels, ImmutableMap<String, SinkRunner> sinks) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String jsonStr = "{\"success\":\"true\",\"stats\":{\"sources\":[";
        boolean first = true;

        for (String key : sources.keySet()) {
            if (first) {
                first = false;
            } else {
                jsonStr += ",";
            } // if else

            Source source;
            HTTPSourceHandler handler;

            try {
                SourceRunner sr = sources.get(key);
                source = sr.getSource();
                Field f = source.getClass().getDeclaredField("handler");
                f.setAccessible(true);
                handler = (HTTPSourceHandler) f.get(source);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } // try catch

            jsonStr += "{\"name\":\"" + source.getName() + "\","
                    + "\"status\":\"" + source.getLifecycleState().toString() + "\",";

            if (handler instanceof CygnusHandler) {
                CygnusHandler ch = (CygnusHandler) handler;
                jsonStr += "\"setup_time\":\"" + CommonUtils.getHumanReadable(ch.getBootTime(), true) + "\","
                        + "\"num_received_events\":" + ch.getNumReceivedEvents() + ","
                        + "\"num_processed_events\":" + ch.getNumProcessedEvents() + "}";
            } else {
                jsonStr += "\"setup_time\":\"unknown\","
                        + "\"num_received_events\":-1,"
                        + "\"num_processed_events\":-1}";
            } // if else
        } // for

        jsonStr += "],\"channels\":[";
        first = true;

        for (String key : channels.keySet()) {
            if (first) {
                first = false;
            } else {
                jsonStr += ",";
            } // if else

            Channel channel = channels.get(key);
            jsonStr += "{\"name\":\"" + channel.getName() + "\","
                    + "\"status\":\"" + channel.getLifecycleState().toString() + "\",";

            if (channel instanceof CygnusChannel) {
                CygnusChannel cc = (CygnusChannel) channel;
                jsonStr += "\"setup_time\":\"" + CommonUtils.getHumanReadable(cc.getSetupTime(), true) + "\","
                        + "\"num_events\":" + cc.getNumEvents() + ","
                        + "\"num_puts_ok\":" + cc.getNumPutsOK() + ","
                        + "\"num_puts_failed\":" + cc.getNumPutsFail() + ","
                        + "\"num_takes_ok\":" + cc.getNumTakesOK() + ","
                        + "\"num_takes_failed\":" + cc.getNumTakesFail() + "}";
            } else {
                jsonStr += "\"setup_time\":\"unknown\","
                        + "\"num_events\":-1,"
                        + "\"num_puts_ok\":-1,"
                        + "\"num_puts_failed\":-1,"
                        + "\"num_takes_ok\":-1,"
                        + "\"num_takes_failed\":-1}";
            } // if else
        } // for

        jsonStr += "],\"sinks\":[";
        first = true;

        for (String key : sinks.keySet()) {
            if (first) {
                first = false;
            } else {
                jsonStr += ",";
            } // if else

            Sink sink;

            try {
                SinkRunner sr = sinks.get(key);
                SinkProcessor sp = sr.getPolicy();
                Field f = sp.getClass().getDeclaredField("sink");
                f.setAccessible(true);
                sink = (Sink) f.get(sp);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } // try catch

            jsonStr += "{\"name\":\"" + sink.getName() + "\","
                    + "\"status\":\"" + sink.getLifecycleState().toString() + "\",";

            if (sink instanceof CygnusSink) {
                CygnusSink cs = (CygnusSink) sink;
                jsonStr += "\"setup_time\":\"" + CommonUtils.getHumanReadable(cs.getSetupTime(), true) + "\","
                        + "\"num_processed_events\":" + cs.getNumProcessedEvents() + ","
                        + "\"num_persisted_events\":" + cs.getNumPersistedEvents() + "}";
            } else {
                jsonStr += "\"setup_time\":\"unknown\","
                        + "\"num_processed_events\":-1,"
                        + "\"num_persisted_events\":-1}";
            } // if else
        } // for

        jsonStr += "]}}";
        response.getWriter().println(jsonStr);
    } // handleGetStats
    
    /**
     * Handles PUT /v1/stats (reset).
     * @param response
     * @param sources
     * @param channels
     * @param sinks
     * @throws IOException
     */
    public static void put(HttpServletResponse response, ImmutableMap<String, SourceRunner> sources,
            ImmutableMap<String, Channel> channels, ImmutableMap<String, SinkRunner> sinks) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        
        for (String key : sources.keySet()) {
            Source source;
            HTTPSourceHandler handler;
            
            try {
                SourceRunner sr = sources.get(key);
                source = sr.getSource();
                Field f = source.getClass().getDeclaredField("handler");
                f.setAccessible(true);
                handler = (HTTPSourceHandler) f.get(source);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } // try catch
            
            if (handler instanceof CygnusHandler) {
                CygnusHandler ch = (CygnusHandler) handler;
                ch.setNumProcessedEvents(0);
                ch.setNumReceivedEvents(0);
            } // if
        } // for

        for (String key : channels.keySet()) {
            Channel channel = channels.get(key);
            
            if (channel instanceof CygnusChannel) {
                CygnusChannel cc = (CygnusChannel) channel;
                cc.setNumPutsOK(0);
                cc.setNumPutsFail(0);
                cc.setNumTakesOK(0);
                cc.setNumTakesFail(0);
            } // if
        } // for

        for (String key : sinks.keySet()) {
            Sink sink;
            
            try {
                SinkRunner sr = sinks.get(key);
                SinkProcessor sp = sr.getPolicy();
                Field f = sp.getClass().getDeclaredField("sink");
                f.setAccessible(true);
                sink = (Sink) f.get(sp);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                continue;
            } // try catch

            if (sink instanceof CygnusSink) {
                CygnusSink cs = (CygnusSink) sink;
                cs.setNumProcessedEvents(0);
                cs.setNumPersistedEvents(0);
            } // if
        } // for
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{\"success\":\"true\"}");
        LOGGER.debug("Statistics reseted");
    } // handlePutStats
    
} // StatsHandlers
