/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package es.tid.fiware.fiwareconnectors.cygnus.channelselectors;

import java.util.ArrayList;
import java.util.List;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.channel.AbstractChannelSelector;
import org.apache.log4j.Logger;

/**
 *
 * @author frb
 */
public class RoundRobinChannelSelector extends AbstractChannelSelector {
    
    private Logger logger;
    private int numChannels;
    private int lastUsedChannel;
    
    /**
     * Constructor.
     */
    public RoundRobinChannelSelector() {
        logger = Logger.getLogger(RoundRobinChannelSelector.class);
    } // RoundRobinChannelSelector
    
    @Override
    public void setChannels(List<Channel> channels) {
        super.setChannels(channels);
        this.numChannels = channels.size();
        this.lastUsedChannel = -1;
    } // setChannels
    
    @Override
    public void configure(Context context) {
    } // configure
    
    @Override
    public List<Channel> getOptionalChannels(Event event) {
        logger.debug("Returning empty optional channels");
        return new ArrayList<Channel>();
    } // getOptionalChannels
    
    @Override
    public List<Channel> getRequiredChannels(Event event) {
        List<Channel> res = new ArrayList<Channel>(1);
        lastUsedChannel = (lastUsedChannel + 1) % numChannels;
        Channel channel = getAllChannels().get(lastUsedChannel);
        res.add(channel);
        logger.debug("Returning " + channel.getName() + " channel");
        return res;
    } // getRequiredChannels

} // RoundRobinChannelSelector
