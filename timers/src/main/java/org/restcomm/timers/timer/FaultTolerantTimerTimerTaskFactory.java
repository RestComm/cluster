/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2017, Telestax Inc and individual contributors
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


import org.restcomm.timers.FaultTolerantScheduler;
import org.restcomm.timers.TimerTask;
import org.restcomm.timers.TimerTaskData;

/**
 * 
 * @author martins
 *
 */
public class FaultTolerantTimerTimerTaskFactory implements org.restcomm.timers.TimerTaskFactory {
	
	private FaultTolerantScheduler scheduler;
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.core.timers.TimerTaskFactory#newTimerTask(org.mobicents.slee.core.timers.TimerTaskData)
	 */
	public TimerTask newTimerTask(TimerTaskData data) {
		if (scheduler == null) {
			throw new IllegalStateException("unable to create data, scheduler is not set");
		}
		return new FaultTolerantTimerTimerTask((org.restcomm.timers.timer.FaultTolerantTimerTimerTaskData) data,scheduler);
	}

	/**
	 *  
	 * @param scheduler the scheduler to set
	 */
	public void setScheduler(FaultTolerantScheduler scheduler) {
		this.scheduler = scheduler;
	}
	
}
