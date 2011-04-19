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

package org.mobicents.timers.timer;


import org.mobicents.timers.FaultTolerantScheduler;
import org.mobicents.timers.TimerTask;
import org.mobicents.timers.TimerTaskData;

/**
 * 
 * @author martins
 *
 */
public class FaultTolerantTimerTimerTaskFactory implements org.mobicents.timers.TimerTaskFactory {
	
	private FaultTolerantScheduler scheduler;
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.core.timers.TimerTaskFactory#newTimerTask(org.mobicents.slee.core.timers.TimerTaskData)
	 */
	public TimerTask newTimerTask(TimerTaskData data) {
		if (scheduler == null) {
			throw new IllegalStateException("unable to create data, scheduler is not set");
		}
		return new FaultTolerantTimerTimerTask((org.mobicents.timers.timer.FaultTolerantTimerTimerTaskData) data,scheduler);
	}

	/**
	 *  
	 * @param scheduler the scheduler to set
	 */
	public void setScheduler(FaultTolerantScheduler scheduler) {
		this.scheduler = scheduler;
	}
	
}
