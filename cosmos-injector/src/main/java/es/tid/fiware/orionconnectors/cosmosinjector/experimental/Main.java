package es.tid.fiware.orionconnectors.cosmosinjector.experimental;

import es.tid.fiware.orionconnectors.cosmosinjector.OrionHDFSSink;
import es.tid.fiware.orionconnectors.cosmosinjector.containers.NotifyContextRequest;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.channel.ReplicatingChannelSelector;
import org.apache.flume.conf.Configurables;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author frb
 * 
 * Main program mimicking the behaviour of a Flume agent. Just for training purposes.
 * 
 * DO NOT USE
 */
public final class Main {
    
    private Main() {
    } // Main
    
    /**
     * Main function.
     * 
     * @param args
     */
    public static void main(String[] args) {
        // create a channel
        Channel channel = new MemoryChannel();
        Context channelCtx = new Context();
        channelCtx.put("capacity", "1000");
        channelCtx.put("transactionCapacity", "1000");
        Configurables.configure(channel, channelCtx);
        channel.setName("orion_channel");
        
        // start the sink
        OrionHDFSSink sink = new OrionHDFSSink();
        sink.setChannel(channel);
        Context sinkCtx = new Context();
        sinkCtx.put("cosmos_host", "130.206.80.46");
        sinkCtx.put("cosmos_port", "14000");
        sinkCtx.put("cosmos_username", "frb");
        sinkCtx.put("cosmos_basedir", "mydata");
        sinkCtx.put("hdfs_api", "httpfs");
        sink.configure(sinkCtx);
        sink.setName("orion_sink");
        
        // start the source
        OrionSource source = new OrionSource();
        ReplicatingChannelSelector channelSelector = new ReplicatingChannelSelector();
        ArrayList<Channel> channelList = new ArrayList<Channel>();
        channelList.add(channel);
        channelSelector.setChannels(channelList);
        ChannelProcessor channelProcessor = new ChannelProcessor(channelSelector);
        source.setChannelProcessor(channelProcessor);
        Context sourceCtx = new Context();
        sourceCtx.put("notifications_port", "12345");
        source.configure(sourceCtx);
        source.setName("orion_source");
        
        source.start();
        sink.start();
        
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                System.out.println("Something went wrong...");
            } // try catch
        } // while
    } // main
    
} // Main
