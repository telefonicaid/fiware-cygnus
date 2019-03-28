/**
 * 
 */
package com.telefonica.iot.cygnus.processors;

import java.util.List;

import org.apache.flume.Context;
import org.apache.flume.Sink;
import org.apache.flume.sink.LoadBalancingSinkProcessor;

import com.telefonica.iot.cygnus.backends.mysql.MySQLBackendImpl;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.NGSIMySQLSink;

/**
 * @author dmartinez
 *
 */
public class LoadBalancingMySQLProcessor extends LoadBalancingSinkProcessor {

    private static final String CONFIG_PARENT_SINK = "parentSink";
    private static final CygnusLogger LOGGER = new CygnusLogger(MySQLBackendImpl.class);
    private String parentSinkName = "";
    private Sink parentSink = null;
    
    @Override
    public void configure(Context context) {
        super.configure(context);
        
        
        parentSinkName = context.getString(CONFIG_PARENT_SINK, "").trim();
        LOGGER.debug("[LoadBalancingMySQLProcessor] Reading configuration (parentSink="
                    + parentSinkName + ")");

        LOGGER.debug("[LoadBalancingMySQLProcessor] type: " + context.getString(CONFIG_SELECTOR,
                "DEFAULT VALUE= " + SELECTOR_NAME_ROUND_ROBIN));
        
        // Set a common connection Driver to all NGSIMySQLSinks
        List<Sink> sinkList = getSinks();
        int sinksCount = sinkList.size();
        
        if (sinksCount > 1) {
            
            LOGGER.debug("Loading LoadBalancingMySQLProcessor for " + sinksCount + " sinks");
            
            // Search parent sink
            int i = 0;
            while (parentSink == null  && i < sinksCount) {
                Sink s = sinkList.get(i);
                if (s.getName().equals(parentSinkName)) {
                    parentSink = s;
                    LOGGER.debug("parent sink found.");
                                        
                }
                i++;
            }
            
            if (parentSink == null) {
                if (parentSinkName.equals("")) {
                    LOGGER.debug("No parent sink set, using standard load balancing.");
                } else {
                    LOGGER.error("[LoadBalancingMySQLProcessor] Can't find parent sink " + parentSinkName
                            + " Using standard Load Balancer.");
                }
            } else if (parentSink instanceof NGSIMySQLSink) {
                NGSIMySQLSink parentMysqlSink = (NGSIMySQLSink) parentSink;
                
                int numberOfSinks = 1;
                for (Sink s : sinkList) {
                    if (s != parentSink) { // Skip parentSink.
                        if (s instanceof NGSIMySQLSink) {
                            numberOfSinks++;
                            NGSIMySQLSink sink = (NGSIMySQLSink) s;
                            sink.shareConnectionsFrom(parentMysqlSink);
                            LOGGER.debug("Configuring sink " + sink.getName() + " to use connections from "
                                    + parentMysqlSink.getName());
                        } else {
                            LOGGER.error("Sinks in MySQL load balancer must be NGSIMySQLSink. " + s.getName()
                                    + " whos type is " + s.getClass() + ",was skipped.");
                        }
                    }
                }
                // Set default pool size to be equal than Sinks size
                parentMysqlSink.setDefaultMaxPoolSize(numberOfSinks);
                
            } else {
                parentSink = null;
                LOGGER.error("All Sinks in MySQL load balancer must be NGSIMySQLSink. " + parentSink.getName()
                        + " isn't. All sinks skipped.");
            }
        } else {
            LOGGER.error("The LoadBalancingMySQLProcessor cannot be used for a single sink. "
                    + "Please configure more than one sinks and try again.");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.flume.sink.LoadBalancingSinkProcessor#start()
     */
    @Override
    public void start() {
        LOGGER.debug("Starting LB sinks.");
        
        List<Sink> sinkList = getSinks();
        
        if (parentSink != null) {
            parentSink.start(); // Start Parent First
        }
        
        super.start();
        
        if (parentSink != null) {
            // Processs each sink in his own thread
            for (Sink s : sinkList) {
                NGSIMySQLSink sink = (NGSIMySQLSink) s;
                sink.runBackgroundProcess();
                LOGGER.debug("[" + s.getName() + "] Sink configured as concurrent.");
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.flume.sink.LoadBalancingSinkProcessor#stop()
     */
    @Override
    public void stop() {
        LOGGER.debug("Stopping LB sinks.");
        super.stop();
    }
    
}
