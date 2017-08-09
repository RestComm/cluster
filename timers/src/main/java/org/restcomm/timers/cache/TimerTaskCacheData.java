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

import org.restcomm.cache.MobicentsCache;
import org.restcomm.cluster.cache.ClusteredCacheData;
import org.restcomm.timers.TimerTask;
import org.restcomm.timers.TimerTaskData;

/**
 * 
 * Proxy object for timer task data management through Infinispan Cache
 * 
 * @author martins
 * @author András Kőkuti
 * 
 */

public class TimerTaskCacheData extends ClusteredCacheData<Serializable,TimerTaskData> {	
	/**
	 * 
	 */
	//@SuppressWarnings("unchecked")
	public TimerTaskCacheData(Serializable taskID, MobicentsCache cache) {
		super(taskID,cache);
	}

	/**
	 * Sets the task data.
	 * 
	 * @param taskData
	 */
	public void setTaskData(TimerTaskData taskData) {
		putValue(taskData);
	}

	/**
	 * Retrieves the task data
	 * @return
	 */
	public TimerTaskData getTaskData() {
		return getValue();		
	}

	/**
	 * Retrieves the {@link TimerTask} id from the specified {@link ClusteredCacheData}.
	 * 
	 * @param clusteredCacheData
	 * @return
	 *  
	 */
	public static Serializable getTaskID(ClusteredCacheData<Serializable,TimerTaskData> clusteredCacheData) throws IllegalArgumentException {
		return (Serializable) clusteredCacheData.getKey();
	}
}
