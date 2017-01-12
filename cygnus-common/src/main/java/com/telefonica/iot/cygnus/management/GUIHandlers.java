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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.apache.flume.Channel;

/**
 *
 * @author frb
 */
public final class GUIHandlers {
    
    private static int numPoints = 0;
    private static final String SOURCE_ROWS = "";
    private static String channelRows = "";
    private static final String SINK_ROWS = "";
    
    /**
     * Constructor. Utility classes should not have a public or default constructor.
     */
    private GUIHandlers() {
    } // GUIHandlers
    
    /**
     * Handles GET /.
     * @param response
     * @throws IOException
     */
    public static void getRoot(HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String indexJSP = "";
        BufferedReader reader = new BufferedReader(new FileReader(
                "src/main/java/com/telefonica/iot/cygnus/management/index.html"));
        String line;

        while ((line = reader.readLine()) != null) {
            indexJSP += line + "\n";
        } // while

        response.getWriter().println(indexJSP);
    } // getRoot

    /**
     * Handles GET /points.
     * @param response
     * @param channels
     * @throws IOException
     */
    public static void getPoints(HttpServletResponse response, ImmutableMap<String, Channel> channels)
        throws IOException {
        // add a new source row
        String sourceColumns = "";

        // add a new channel row
        String channelColumns = "\"count\"";
        String point = "[" + numPoints;

        for (String key : channels.keySet()) {
            Channel channel = channels.get(key);
            channelColumns += ",\"" + channel.getName() + "\"";

            if (channel instanceof CygnusChannel) {
                CygnusChannel cygnusChannel = (CygnusChannel) channel;
                point += "," + cygnusChannel.getNumEvents();
            } // if12
        } // for

        point += "]";

        if (channelRows.length() == 0) {
            channelRows += point;
        } else {
            channelRows += "," + point;
        } // if else

        // add a new sink row
        String sinkColumns = "";

        // increase the points counter
        numPoints++;

        // return the points
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{\"source_points\":{\"columns\":[" + sourceColumns + "],\"rows\":["
                + SOURCE_ROWS + "]}," + "\"channel_points\":{\"columns\":[" + channelColumns + "],\"rows\":["
                + channelRows + "]}," + "\"sink_points\":{\"columns\":[" + sinkColumns + "],\"rows\":["
                + SINK_ROWS + "]}}");
    } // getPoints
    
} // GUIHandlers
