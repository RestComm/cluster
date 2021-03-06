/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.restcomm.timers.cache;

import java.util.Collections;
import java.util.Set;

import org.jboss.cache.Node;
import org.restcomm.cache.CacheData;
import org.restcomm.cache.FqnWrapper;
import org.restcomm.cluster.MobicentsCluster;


/**
 * 
 * Proxy object for timer facility entity data management through JBoss Cache
 * 
 * @author martins
 * 
 */

public class FaultTolerantSchedulerCacheData extends CacheData {
			
	/**
	 * 
	 * @param baseFqnWrapper
	 * @param cluster
	 */
	@SuppressWarnings("unchecked")
	public FaultTolerantSchedulerCacheData(FqnWrapper baseFqnWrapper, MobicentsCluster cluster) {
		//super(baseFqn,cluster.getMobicentsCache());
		super(baseFqnWrapper,cluster.getMobicentsCache());
	}

	public Set<?> getTaskIDs() {
		final Node<?,?> node = getNode();
		if (!node.isLeaf()) {
			return node.getChildrenNames();			
		}
		else {
			return Collections.EMPTY_SET;
		}
	}
	
}
