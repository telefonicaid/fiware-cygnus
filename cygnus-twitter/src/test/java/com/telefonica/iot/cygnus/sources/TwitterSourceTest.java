package com.telefonica.iot.cygnus.sources;

import org.apache.flume.*;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.channel.ReplicatingChannelSelector;
import org.apache.flume.conf.Configurables;
import org.apache.flume.sink.DefaultSinkProcessor;
import org.apache.flume.sink.LoggerSink;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author jpalanca
 */

@RunWith(MockitoJUnitRunner.class)
public class TwitterSourceTest {

    private String consumerKey = "asdfghjkl";
    private String consumerSecret = "qwertyuiop";
    private String accessToken = "zxcvbnm";
    private String accessTokenSecret = "1234567890";


    @Test
    public void testConfigure() {
        Context context = new Context();
        context.put("consumerKey", consumerKey);
        context.put("consumerSecret", consumerSecret);
        context.put("accessToken", accessToken);
        context.put("accessTokenSecret", accessTokenSecret);
        context.put("maxBatchDurationMillis", "1000");

        TwitterSource source = new TwitterSource();
        source.configure(context);

        assertEquals(consumerKey, source.getConsumerKey()[0]);
        assertEquals(consumerSecret, source.getConsumerSecret());
        assertEquals(accessToken, source.getAccessToken());
        assertEquals(accessTokenSecret, source.getAccessTokenSecret());
    }

    /* From flume v1.7.0 */
    @Test
    public void testBasic() throws Exception {
        String consumerKey = System.getProperty("twitter.consumerKey");
        Assume.assumeNotNull(consumerKey);

        String consumerSecret = System.getProperty("twitter.consumerSecret");
        Assume.assumeNotNull(consumerSecret);

        String accessToken = System.getProperty("twitter.accessToken");
        Assume.assumeNotNull(accessToken);

        String accessTokenSecret = System.getProperty("twitter.accessTokenSecret");
        Assume.assumeNotNull(accessTokenSecret);

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
        source.start();

        Thread.sleep(5000);
        source.stop();
        sinkRunner.stop();
        sink.stop();
    }
}
