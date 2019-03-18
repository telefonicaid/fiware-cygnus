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

    // FIXME: This attribute overrides Apache flume v1.4 bug stoping sinks
    // https://github.com/apache/flume/commit/f017ce5aca00d280ad6ee94e63fe3b44c326c5cf#diff-6cbd0fd434c136c1fa50dc7e33b736e1
    // This attribute must be removed when Apache Flume version >= 1.7.0
    private LifecycleState fixedState;

    @Override
    public void configure(Context context) {
        super.configure(context);

        // Set a common connection Driver to all NGSIMySQLSinks
        List<Sink> sinkList = getSinks();
        if (sinkList.size() > 1) {
            Sink firstSink = sinkList.get(0);
            LOGGER.debug("Loading LoadBalancingMySQLProcessor for " + sinkList.size() + " sinks");
            if (firstSink instanceof NGSIMySQLSink) {
                NGSIMySQLSink firstMysqlSink = (NGSIMySQLSink) firstSink;

                for (Sink s : sinkList) {
                    if (s != firstSink) { // Skip first one, who's used as parent.
                        if (s instanceof NGSIMySQLSink) {
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
            } else {
                LOGGER.error("All Sinks in MySQL load balancer must be NGSIMySQLSink. " + firstSink.getName()
                        + " isn't. All sinks skipped.");
            }
        } else {
            LOGGER.error("The LoadBalancingMySQLProcessor cannot be used for a single sink. "
                    + "Please configure more than one sinks and try again.");
        }
    }

    @Override
    public void start() { // FIXME remove if Apache Flume version >= 1.7.0

        List<Sink> sinkList = getSinks();
        LOGGER.debug("Load Balancer starting " + sinkList.size() + " sinks.");
        for (Sink s : sinkList) {
            s.start();
        }

        fixedState = LifecycleState.START;
    }

    @Override
    public void stop() { // FIXME remove if Apache Flume version >= 1.7.0

        List<Sink> sinkList = getSinks();
        LOGGER.debug("Load Balancer stopping " + sinkList.size() + " sinks.");
        for (Sink s : sinkList) {
            s.stop();
        }
        fixedState = LifecycleState.STOP;
    }

    @Override
    public LifecycleState getLifecycleState() { // FIXME remove if Apache Flume
                                                // version >= 1.7.0
        return fixedState;
    }

}
