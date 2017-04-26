/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.restcomm.timers.cache;

import java.io.Serializable;

import org.restcomm.cache.FqnWrapper;
import org.restcomm.cluster.MobicentsCluster;
import org.restcomm.cluster.cache.ClusteredCacheData;
import org.restcomm.timers.TimerTask;
import org.restcomm.timers.TimerTaskData;

import org.infinispan.tree.Fqn;

/**
 * 
 * Proxy object for timer task data management through Infinispan Cache
 * 
 * @author martins
 * @author András Kőkuti
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
	//@SuppressWarnings("unchecked")
	public TimerTaskCacheData(Serializable taskID, Fqn baseFqn, MobicentsCluster mobicentsCluster) {
		super(FqnWrapper.fromRelativeElementsWrapper(new FqnWrapper(baseFqn), taskID),mobicentsCluster);
	}

	/**
	 * 
	 */
	//@SuppressWarnings("unchecked")
	public TimerTaskCacheData(Fqn fqn, MobicentsCluster mobicentsCluster) {
		super(new FqnWrapper(fqn),mobicentsCluster);
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
