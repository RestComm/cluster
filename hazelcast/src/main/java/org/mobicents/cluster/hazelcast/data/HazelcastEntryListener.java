package org.mobicents.cluster.hazelcast.data;

import org.apache.log4j.Logger;
import org.mobicents.cluster.data.ClusterDataKey;
import org.mobicents.cluster.listener.ClusterDataRemovalListener;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;

@SuppressWarnings("rawtypes")
public class HazelcastEntryListener implements EntryListener {

	private static final Logger LOGGER = Logger
	.getLogger(HazelcastEntryListener.class);
	
	private final ClusterDataRemovalListener dataRemovalListener;

	public HazelcastEntryListener(ClusterDataRemovalListener dataRemovalListener) {
		this.dataRemovalListener = dataRemovalListener;
	}
	
	public ClusterDataRemovalListener getDataRemovalListener() {
		return dataRemovalListener;
	}
	
	@Override
	public void entryAdded(EntryEvent arg0) {
		// ignore
	}

	@Override
	public void entryEvicted(EntryEvent arg0) {
		// ignore
	}

	@Override
	public void entryRemoved(EntryEvent event) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("entryRemoved( event = " + event + ")");
		}

		if (!event.getMember().localMember()) {
			final ClusterDataKey dataKey = event.getKey() instanceof ClusterDataKey ? ((ClusterDataKey) event
					.getKey()) : null;
			if (dataKey == null) {
				return;
			}
			final Object listenerID = dataKey.getDataRemovalListenerID();
			if (listenerID == null || !listenerID.equals(dataRemovalListener.getDataRemovalListenerID())) {
				return;
			}
			dataRemovalListener.dataRemoved(dataKey);
		}
	}

	@Override
	public void entryUpdated(EntryEvent arg0) {
		// ignore
	}

}
