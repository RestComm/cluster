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

package org.restcomm.timers.timer;

import java.io.Serializable;
import java.util.TimerTask;

import org.restcomm.timers.PeriodicScheduleStrategy;
import org.restcomm.timers.TimerTaskData;

/**
 * 
 * @author martins
 *
 */
public class FaultTolerantTimerTimerTaskData extends TimerTaskData {

	/**
	 * 
	 */
	private final TimerTask javaUtilTimerTask;
	
	/**
	 * 
	 * @param javaUtilTimerTask
	 * @param id
	 * @param startTime
	 * @param period
	 * @param periodicScheduleStrategy
	 */
	public FaultTolerantTimerTimerTaskData(TimerTask javaUtilTimerTask, Serializable id, long startTime, long period, PeriodicScheduleStrategy periodicScheduleStrategy) {
		super(id,startTime,period, periodicScheduleStrategy);
		this.javaUtilTimerTask = javaUtilTimerTask;		
	}

	/**
	 * 
	 * @return
	 */
	public TimerTask getJavaUtilTimerTask() {
		return javaUtilTimerTask;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
