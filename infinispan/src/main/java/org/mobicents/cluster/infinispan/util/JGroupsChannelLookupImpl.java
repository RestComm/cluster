package org.mobicents.cluster.infinispan.util;

import java.util.Properties;

import org.infinispan.remoting.transport.jgroups.JGroupsChannelLookup;
import org.jgroups.Channel;
//import org.jgroups.ChannelFactory;

@SuppressWarnings("deprecation")
public class JGroupsChannelLookupImpl implements JGroupsChannelLookup {

	public static final String STACK_PROPERTY_NAME = "stack";
	public static final String CHANNEL_ID_PROPERTY_NAME = "channelId";
	public static final String CHANNEL_FACTORY_PROPERTY_NAME = "channelFactory";
		
	@Override
	public Channel getJGroupsChannel(Properties properties) {		
        /**final ChannelFactory channelFactory = (ChannelFactory) properties.get(CHANNEL_FACTORY_PROPERTY_NAME);
		final String stack = properties.getProperty(STACK_PROPERTY_NAME);
        final String id = properties.getProperty(CHANNEL_ID_PROPERTY_NAME);
        try {
           return channelFactory.createMultiplexerChannel(stack, id);
        }
        catch (Throwable e) {
           throw new IllegalArgumentException(e);
        }*/
		return null;
	}

	@Override
	public boolean shouldClose() {
		return true;
	}

	@Override
	public boolean shouldDisconnect() {
		return true;
	}

	@Override
	public boolean shouldConnect() {
		return true;
	}

}
