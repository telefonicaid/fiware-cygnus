/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.sources;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Sink;
import org.apache.flume.SinkRunner;
import org.apache.flume.ChannelSelector;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.channel.ReplicatingChannelSelector;
import org.apache.flume.conf.Configurables;
import org.apache.flume.sink.DefaultSinkProcessor;
import org.apache.flume.sink.LoggerSink;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author jpalanca
 */
@RunWith(MockitoJUnitRunner.class)
public class TwitterSourceTest {

    private String consumerKey = "iAtYJ4HpUVfIUoNnif1DA";
    private String consumerSecret = "172fOpzuZoYzNYaU3mMYvE8m8MEyLbztOdbrUolU";
    private String accessToken = "zxcvbnm";
    private String accessTokenSecret = "1234567890";
    private String southWestLatitude = "40.748433";
    private String southWestLongitude = "-73.985656";
    private String northEastLatitude = "40.758611";
    private String northEastLongitude = "-73.979167";
    private String keywords = "keywords, more_keywords";
    private double[][] coordinates = {{-73.985656, 40.748433}, {-73.979167, 40.758611}};
    private String[] keywords_array = {"keywords", "more_keywords"};


    @Test
    public void testConfigure() {
        System.out.println(getTestTraceHead("[TwitterSourceTest.configure]")
                + "-------- Configure Twitter parameters.");
        Context context = new Context();
        context.put("consumerKey", consumerKey);
        context.put("consumerSecret", consumerSecret);
        context.put("accessToken", accessToken);
        context.put("accessTokenSecret", accessTokenSecret);
        context.put("south_west_latitude", southWestLatitude);
        context.put("south_west_longitude", southWestLongitude);
        context.put("north_east_latitude", northEastLatitude);
        context.put("north_east_longitude", northEastLongitude);
        context.put("keywords", keywords);

        context.put("maxBatchDurationMillis", "1000");

        TwitterSource source = new TwitterSource();
        source.configure(context);

        try {
            assertEquals(consumerKey, source.getConsumerKey());
            assertEquals(consumerSecret, source.getConsumerSecret());
            assertEquals(accessToken, source.getAccessToken());
            assertEquals(accessTokenSecret, source.getAccessTokenSecret());
            assertArrayEquals(coordinates, source.getBoundingBox());
            assertArrayEquals(keywords_array, source.getKeywords());
            System.out.println(getTestTraceHead("[TwitterSourceTest.configure]")
                    + "-  OK  - Twitter parameters detected in context.");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[TwitterSourceTest.configure]")
                    + "- FAIL - Twitter parameters not detected in context.");
            throw e;
        } // try catch
    }

    // From flume v1.7.0
    @Test
    public void testBasic() throws Exception {
        System.out.println(getTestTraceHead("[TwitterSourceTest.basic]")
                + "-------- Start source.");
        Context context = new Context();
        context.put("consumerKey", consumerKey);
        context.put("consumerSecret", consumerSecret);
        context.put("accessToken", accessToken);
        context.put("accessTokenSecret", accessTokenSecret);
        context.put("maxBatchDurationMillis", "1000");

        TwitterSource source = new TwitterSource();
        source.configure(context);

        Map<String, String> channelContext = new HashMap();
        channelContext.put("capacity", "1000000");
        channelContext.put("keep-alive", "0"); // for faster tests
        Channel channel = new MemoryChannel();
        Configurables.configure(channel, new Context(channelContext));

        Sink sink = new LoggerSink();
        sink.setChannel(channel);
        sink.start();
        DefaultSinkProcessor proc = new DefaultSinkProcessor();
        proc.setSinks(Collections.singletonList(sink));
        SinkRunner sinkRunner = new SinkRunner(proc);
        sinkRunner.start();

        ChannelSelector rcs = new ReplicatingChannelSelector();
        rcs.setChannels(Collections.singletonList(channel));
        ChannelProcessor chp = new ChannelProcessor(rcs);
        source.setChannelProcessor(chp);

        try {
            source.start();

            Thread.sleep(500);
            source.stop();
            System.out.println(getTestTraceHead("[TwitterSourceTest.basic]")
                    + "-  OK  - Twitter source started properly.");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[TwitterSourceTest.basic]")
                    + "- FAIL - Twitter source could not start.");
            throw e;
        } // try catch
        sinkRunner.stop();
        sink.stop();
    }
}
