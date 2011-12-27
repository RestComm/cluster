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

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.transaction.TransactionManager;

import org.mobicents.cluster.MobicentsCluster;
import org.mobicents.timers.FaultTolerantScheduler;
import org.mobicents.timers.PeriodicScheduleStrategy;

/**
 * A fault tolerant implementation of a {@link Timer}, using the
 * {@link FaultTolerantScheduler}.
 * 
 * There is no guarantee that fail over recover works correctly with not serializable {@link TimerTask}s
 * 
 * @author martins
 * 
 */
public class FaultTolerantTimer extends java.util.Timer {

	/**
	 * 
	 */
	private final FaultTolerantScheduler scheduler;
	
	/**
	 * 
	 */
	private final FaultTolerantTimerTimerTaskFactory timerTaskFactory;
	
	/**
	 * 
	 * @param name
	 * @param cluster
	 * @param priority
	 * @param txManager
	 */
	public FaultTolerantTimer(String name, MobicentsCluster cluster, byte priority, TransactionManager txManager) {
		this(name, cluster, priority, txManager, 0);
	}
	
	/**
	 * 
	 * @param name
	 * @param cluster
	 * @param priority
	 * @param txManager
	 * @param purgePeriod
	 */
	public FaultTolerantTimer(String name, MobicentsCluster cluster, byte priority, TransactionManager txManager, int purgePeriod) {
		timerTaskFactory = new FaultTolerantTimerTimerTaskFactory();
		scheduler = new FaultTolerantScheduler(name,16, cluster, priority, txManager, timerTaskFactory,purgePeriod);
		timerTaskFactory.setScheduler(scheduler);
	}
	
	/**
	 *  
	 * @return the scheduler
	 */
	public FaultTolerantScheduler getScheduler() {
		return scheduler;
	}
	
	@Override
	public void cancel() {
		scheduler.shutdownNow();
	}
	
	@Override
	public int purge() {
		int count = 0;
		for (org.mobicents.timers.TimerTask timerTask : scheduler.getLocalRunningTasks()) {
			FaultTolerantTimerTimerTask ftTimerTask = (FaultTolerantTimerTimerTask) timerTask;
			if (ftTimerTask.isCanceled()) {
				scheduler.cancel(ftTimerTask.getData().getTaskID());
				count++;
			}
		}
		return count;
	}
	
	@Override
	public void schedule(TimerTask task, Date firstTime, long period) {
		final org.mobicents.timers.TimerTask taskWrapper = timerTaskFactory.newTimerTask(new FaultTolerantTimerTimerTaskData(task, UUID.randomUUID(),firstTime.getTime() - System.currentTimeMillis(),period,PeriodicScheduleStrategy.withFixedDelay));
		scheduler.schedule(taskWrapper);
	}
	
	@Override
	public void schedule(TimerTask task, Date time) {
		final org.mobicents.timers.TimerTask taskWrapper = timerTaskFactory.newTimerTask(new FaultTolerantTimerTimerTaskData(task, UUID.randomUUID(),time.getTime() - System.currentTimeMillis(),-1,null));
		scheduler.schedule(taskWrapper);		
	}
	
	@Override
	public void schedule(TimerTask task, long delay) {
		final org.mobicents.timers.TimerTask taskWrapper = timerTaskFactory.newTimerTask(new FaultTolerantTimerTimerTaskData(task, UUID.randomUUID(),System.currentTimeMillis()+delay,-1,null));
		scheduler.schedule(taskWrapper);	
	}
	
	@Override
	public void schedule(TimerTask task, long delay, long period) {
		final org.mobicents.timers.TimerTask taskWrapper = timerTaskFactory.newTimerTask(new FaultTolerantTimerTimerTaskData(task, UUID.randomUUID(),System.currentTimeMillis()+delay,period,PeriodicScheduleStrategy.withFixedDelay));
		scheduler.schedule(taskWrapper);		
	}
	
	@Override
	public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
		final org.mobicents.timers.TimerTask taskWrapper = timerTaskFactory.newTimerTask(new FaultTolerantTimerTimerTaskData(task, UUID.randomUUID(),firstTime.getTime() - System.currentTimeMillis(),period,PeriodicScheduleStrategy.atFixedRate));
		scheduler.schedule(taskWrapper);
	}
	
	@Override
	public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
		final org.mobicents.timers.TimerTask taskWrapper = timerTaskFactory.newTimerTask(new FaultTolerantTimerTimerTaskData(task, UUID.randomUUID(),System.currentTimeMillis()+delay,period,PeriodicScheduleStrategy.atFixedRate));
		scheduler.schedule(taskWrapper);	
	}
}
