/**
 * 
 */
package com.telefonica.iot.cygnus.processors;

import java.util.List;

import org.apache.flume.Context;
import org.apache.flume.Sink;
import org.apache.flume.lifecycle.LifecycleState;
import org.apache.flume.sink.LoadBalancingSinkProcessor;

import com.telefonica.iot.cygnus.backends.mysql.MySQLBackendImpl;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.sinks.NGSIMySQLSink;

/**
 * @author dmartinez
 *
 */
public class LoadBalancingMySQLProcessor extends LoadBalancingSinkProcessor {

    private static final CygnusLogger LOGGER = new CygnusLogger(MySQLBackendImpl.class);

    @Override
    public void configure(Context context) {
        super.configure(context);
        
        LOGGER.debug("Configuring LB type: " + context.getString(CONFIG_SELECTOR,
                "DEFAULT VALUE= " + SELECTOR_NAME_ROUND_ROBIN));
        
        // Set a common connection Driver to all NGSIMySQLSinks
        List<Sink> sinkList = getSinks();
        if (sinkList.size() > 1) {
            Sink firstSink = sinkList.get(0);
            LOGGER.debug("Loading LoadBalancingMySQLProcessor for " + sinkList.size() + " sinks");
            if (firstSink instanceof NGSIMySQLSink) {
                NGSIMySQLSink firstMysqlSink = (NGSIMySQLSink) firstSink;
                
                int numberOfSinks = 1;
                for (Sink s : sinkList) {
                    if (s != firstSink) { // Skip first one, who's used as parent.
                        if (s instanceof NGSIMySQLSink) {
                            numberOfSinks++;
                            NGSIMySQLSink sink = (NGSIMySQLSink) s;
                            sink.shareConnectionsFrom(firstMysqlSink);
                            LOGGER.debug("Configuring sink " + sink.getName() + " to use connections from "
                                    + firstMysqlSink.getName());
                        } else {
                            LOGGER.error("Sinks in MySQL load balancer must be NGSIMySQLSink. " + s.getName()
                                    + " whos type is " + s.getClass() + ",was skipped.");
                        }
                    }
                }
                // Set default pool size to be equal than Sinks size
                firstMysqlSink.setMaxPoolSize(numberOfSinks);
                
            } else {
                LOGGER.error("All Sinks in MySQL load balancer must be NGSIMySQLSink. " + firstSink.getName()
                        + " isn't. All sinks skipped.");
            }
        } else {
            LOGGER.error("The LoadBalancingMySQLProcessor cannot be used for a single sink. "
                    + "Please configure more than one sinks and try again.");
        }
    }
 
}
