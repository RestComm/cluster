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

package org.mobicents.timers.cache;

import java.io.Serializable;

import org.jboss.cache.Fqn;
import org.mobicents.cluster.MobicentsCluster;
import org.mobicents.cluster.cache.ClusteredCacheData;
import org.mobicents.timers.TimerTask;
import org.mobicents.timers.TimerTaskData;

/**
 * 
 * Proxy object for timer task data management through JBoss Cache
 * 
 * @author martins
 * 
 */

public class TimerTaskCacheData extends ClusteredCacheData {
	
	/**
	 * the node's data map key where task data is stored
	 */
	private static final String CACHE_NODE_MAP_KEY = "taskdata";
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public TimerTaskCacheData(Serializable taskID, Fqn baseFqn, MobicentsCluster mobicentsCluster) {
		super(Fqn.fromRelativeElements(baseFqn, taskID),mobicentsCluster);
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public TimerTaskCacheData(Fqn fqn, MobicentsCluster mobicentsCluster) {
		super(fqn,mobicentsCluster);
	}
	
	/**
	 * Sets the task data.
	 * 
	 * @param taskData
	 */
	@SuppressWarnings("unchecked")
	public void setTaskData(TimerTaskData taskData) {
		getNode().put(CACHE_NODE_MAP_KEY,taskData);
	}

	/**
	 * Retrieves the task data
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TimerTaskData getTaskData() {
		return (TimerTaskData) getNode().get(CACHE_NODE_MAP_KEY);		
	}

	/**
	 * Retrieves the {@link TimerTask} id from the specified {@link ClusteredCacheData}.
	 * 
	 * @param clusteredCacheData
	 * @return
	 *  
	 */
	public static Serializable getTaskID(ClusteredCacheData clusteredCacheData) throws IllegalArgumentException {
		return (Serializable) clusteredCacheData.getNodeFqn().getLastElement();
	}
	
}
