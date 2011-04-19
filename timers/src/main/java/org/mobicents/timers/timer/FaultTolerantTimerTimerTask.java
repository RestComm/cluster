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

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.log4j.Logger;
import org.mobicents.timers.FaultTolerantScheduler;
import org.mobicents.timers.TimerTask;

/**
 * A concrete {@link FaultTolerantScheduler} {@link TimerTask} to wrap a {@link java.util.TimerTask}.
 * 
 * @author martins
 *
 */
public class FaultTolerantTimerTimerTask extends TimerTask {

	private static final Logger logger = Logger.getLogger(FaultTolerantTimerTimerTask.class);
	
	/**
	 * 
	 */
	private final FaultTolerantScheduler scheduler;
	
	/**
	 * 
	 */
	private final FaultTolerantTimerTimerTaskData taskData;
	
	/**
	 * 
	 * @param taskData
	 */
	public FaultTolerantTimerTimerTask(FaultTolerantTimerTimerTaskData taskData,FaultTolerantScheduler scheduler) {
		super(taskData);
		this.taskData = taskData;
		this.scheduler = scheduler;
		setPeriod(taskData.getJavaUtilTimerTask(),taskData.getPeriod());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.timers.TimerTask#runTask()
	 */
	public void runTask() {
		if (isCanceled()) {
			scheduler.cancel(this.getData().getTaskID());			
		}
		else {
			try{
				taskData.getJavaUtilTimerTask().run();
				if(taskData.getPeriodicScheduleStrategy() == null) {
					scheduler.cancel(this.getData().getTaskID());
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("task data has a periodic schedule strategy, not cancelling the task");
					}
				}
			} catch(Throwable e) {
				logger.error(e.getMessage(),e);
			}					
		}				
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isCanceled() {
		
		if(getTaskStatus(taskData.getJavaUtilTimerTask()) == 3 ) {
			return true;
		} 
		else {
			return false;
		}
	}
	
	private int getTaskStatus(final java.util.TimerTask timerTask) {
		if(System.getSecurityManager()!=null)
		{
			return AccessController.doPrivileged(new PrivilegedAction<Integer>(){

				public Integer run() {
					return _getTaskStatus(timerTask);
				}

			});
		}else
		{
			return _getTaskStatus(timerTask);
		}
	}
	
	private Integer _getTaskStatus(java.util.TimerTask timerTask) {
		Class<?> cc = java.util.TimerTask.class;
		try {
			Field stateField=cc.getDeclaredField("state");
			stateField.setAccessible(true);
			Integer taskStatus= (Integer) stateField.get(taskData.getJavaUtilTimerTask());
			stateField.setAccessible(false);
			return taskStatus;
		} catch (Exception e) {
			throw new RuntimeException("Fialed to get status",e);
		}
	}
	
	/**
	 * @param javaUtilTimerTask
	 * @param period
	 */
	private void setPeriod(final java.util.TimerTask javaUtilTimerTask,final long period) {
		if(System.getSecurityManager()!=null)
		{
			 AccessController.doPrivileged(new PrivilegedAction<Object>(){

				public Object run() {
					_setPeriod(javaUtilTimerTask,period);
					return null;
				}

			});
		}else
		{
			_setPeriod(javaUtilTimerTask,period);
		}
	}

	/**
	 * @param javaUtilTimerTask
	 * @param period
	 */
	private void _setPeriod(java.util.TimerTask javaUtilTimerTask, long period) {
		Class<?> cc = java.util.TimerTask.class;
		try {
			Field stateField=cc.getDeclaredField("period");
			stateField.setAccessible(true);
			stateField.set(javaUtilTimerTask,new Long(period));
			stateField.setAccessible(false);
			return;
		} catch (Throwable e) {
			throw new RuntimeException("Failed to set task period",e);
		}
		
	}
}