/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.TimerTask;

import org.mobicents.timers.PeriodicScheduleStrategy;
import org.mobicents.timers.TimerTaskData;

/**
 * 
 * @author martins
 *
 */
public class FaultTolerantTimerTimerTaskData extends TimerTaskData {

	/**
	 * 
	 */
	private TimerTask javaUtilTimerTask;
	
	public FaultTolerantTimerTimerTaskData() {
		super();
	}
	
	/**
	 * 
	 * @param javaUtilTimerTask
	 * @param startTime
	 * @param period
	 * @param periodicScheduleStrategy
	 */
	public FaultTolerantTimerTimerTaskData(TimerTask javaUtilTimerTask, long startTime, long period, PeriodicScheduleStrategy periodicScheduleStrategy) {
		super(startTime,period, periodicScheduleStrategy);
		this.javaUtilTimerTask = javaUtilTimerTask;		
	}

	/**
	 * 
	 * @return
	 */
	public TimerTask getJavaUtilTimerTask() {
		return javaUtilTimerTask;
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		javaUtilTimerTask = (TimerTask) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(javaUtilTimerTask);
	}
}
