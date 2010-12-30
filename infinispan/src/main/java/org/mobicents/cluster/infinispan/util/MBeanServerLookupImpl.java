package org.mobicents.cluster.infinispan.util;

import java.util.Properties;

import javax.management.MBeanServer;

import org.infinispan.jmx.MBeanServerLookup;

public class MBeanServerLookupImpl implements MBeanServerLookup {

	private final MBeanServer mBeanServer;

	public MBeanServerLookupImpl(MBeanServer mBeanServer) {
		this.mBeanServer = mBeanServer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.infinispan.jmx.MBeanServerLookup#getMBeanServer(java.util.Properties)
	 */
	@Override
	public MBeanServer getMBeanServer(Properties arg0) {
		return mBeanServer;
	}

}
