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
import com.telefonica.iot.cygnus.handlers.CygnusHandler;
import com.telefonica.iot.cygnus.metrics.CygnusMetrics;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.CygnusSink;
import java.io.IOException;
import java.lang.reflect.Field;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public final class MetricsHandlers {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(MetricsHandlers.class);
    
    /**
     * Constructor. Utility classes should not have a public or default constructor.
     */
    private MetricsHandlers() {
    } // MetricsHandlers
    
    /**
     * Handles GET /v1/admin/metrics and /admin/metrics. It is synchronized in order to ensure atomic modifications
     * on the metrics.
     * @param request
     * @param response
     * @param sources
     * @param sinks
     * @throws IOException
     */
    public static synchronized void get(HttpServletRequest request, HttpServletResponse response,
            ImmutableMap<String, SourceRunner> sources, ImmutableMap<String, SinkRunner> sinks) throws IOException {
        // Let's assume everything goes well
        response.setStatus(HttpServletResponse.SC_OK);
        
        // Get the reset parameter
        String reset = request.getParameter("reset");
        
        if (reset != null && !reset.equals("true") && !reset.equals("false")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } // if
        
        if (reset != null && reset.equals("true")) {
            CygnusMetrics metrics = mergeMetrics(sources, sinks);
            response.getWriter().println(metrics.toJsonString());
            deleteMetrics(sources, sinks);
            response.setContentType("application/json; charset=utf-8");
        } else {
            CygnusMetrics metrics = mergeMetrics(sources, sinks);
            response.getWriter().println(metrics.toJsonString());
            response.setContentType("application/json; charset=utf-8");
        } // else
    } // get
    
    /**
     * Handles DELETE /v1/admin/metrics and /admin/metrics. It is synchronized in order to ensure atomic modifications
     * on the metrics.
     * @param response
     * @param sources
     * @param sinks
     * @throws java.io.IOException
     */
    public static synchronized void delete(HttpServletResponse response, ImmutableMap<String, SourceRunner> sources,
            ImmutableMap<String, SinkRunner> sinks) throws IOException {
        deleteMetrics(sources, sinks);
        response.setStatus(HttpServletResponse.SC_OK);
    } // delete
    
    /**
     * Merges metrics from all the given sources and sinks. It is protected in order it can be tested.
     * @param sources
     * @param sinks
     * @return
     */
    protected static CygnusMetrics mergeMetrics(ImmutableMap<String, SourceRunner> sources,
            ImmutableMap<String, SinkRunner> sinks) {
        CygnusMetrics mergedMetrics = new CygnusMetrics();
    
        if (sources != null) {
            for (String key : sources.keySet()) {
                Source source;
                HTTPSourceHandler handler;

                try {
                    SourceRunner sr = sources.get(key);
                    source = sr.getSource();
                    Field f = source.getClass().getDeclaredField("handler");
                    f.setAccessible(true);
                    handler = (HTTPSourceHandler) f.get(source);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                        | SecurityException e) {
                    LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                    continue;
                } // try catch

                if (handler instanceof CygnusHandler) {
                    CygnusHandler ch = (CygnusHandler) handler;
                    CygnusMetrics sourceMetrics = ch.getServiceMetrics();
                    mergedMetrics.merge(sourceMetrics);
                } // if
            } // for
        } // if
        
        if (sinks != null) {
            for (String key : sinks.keySet()) {
                Sink sink;

                try {
                    SinkRunner sr = sinks.get(key);
                    SinkProcessor sp = sr.getPolicy();
                    Field f = sp.getClass().getDeclaredField("sink");
                    f.setAccessible(true);
                    sink = (Sink) f.get(sp);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                        | SecurityException e) {
                    LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                    continue;
                } // try catch

                if (sink instanceof CygnusSink) {
                    CygnusSink cs = (CygnusSink) sink;
                    CygnusMetrics sinkMetrics = cs.getServiceMetrics();
                    mergedMetrics.merge(sinkMetrics);
                } // if
            } // for
        } // if

        return mergedMetrics;
    } // mergeMetrics
    
    /**
     * Deletes metrics from all the given sources and sinks.
     * @param sources
     * @param sinks
     */
    private static void deleteMetrics(ImmutableMap<String, SourceRunner> sources,
            ImmutableMap<String, SinkRunner> sinks) {
        if (sources != null) {
            for (String key : sources.keySet()) {
                Source source;
                HTTPSourceHandler handler;

                try {
                    SourceRunner sr = sources.get(key);
                    source = sr.getSource();
                    Field f = source.getClass().getDeclaredField("handler");
                    f.setAccessible(true);
                    handler = (HTTPSourceHandler) f.get(source);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                        | SecurityException e) {
                    LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                    continue;
                } // try catch

                if (handler instanceof CygnusHandler) {
                    CygnusHandler ch = (CygnusHandler) handler;
                    ch.setServiceMetrics(new CygnusMetrics());
                } // if
            } // for
        } // if
        
        if (sinks != null) {
            for (String key : sinks.keySet()) {
                Sink sink;

                try {
                    SinkRunner sr = sinks.get(key);
                    SinkProcessor sp = sr.getPolicy();
                    Field f = sp.getClass().getDeclaredField("sink");
                    f.setAccessible(true);
                    sink = (Sink) f.get(sp);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                        | SecurityException e) {
                    LOGGER.error("There was a problem when getting a sink. Details: " + e.getMessage());
                    continue;
                } // try catch

                if (sink instanceof CygnusSink) {
                    CygnusSink cs = (CygnusSink) sink;
                    cs.setServiceMetrics(new CygnusMetrics());
                } // if
            } // for
        } // if
    } // deleteMetrics
    
} // MetricsHandlers
