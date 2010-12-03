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
package org.mobicents.timers;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.mobicents.cluster.Cluster;
import org.mobicents.cluster.ClusterData;
import org.mobicents.cluster.ClusterDataFailOverListener;
import org.mobicents.cluster.ClusterDataKey;
import org.mobicents.cluster.ClusterDataRemovalListener;
import org.mobicents.cluster.ClusterNodeAddress;
import org.mobicents.cluster.LocalFailoverElector;
import org.mobicents.timers.cluster.FaultTolerantSchedulerClusterDataKey;
import org.mobicents.timers.cluster.FaultTolerantSchedulerClusterDataMarshaller;
import org.mobicents.timers.cluster.TimerTaskClusterDataKey;
import org.mobicents.timers.cluster.TimerTaskClusterDataMarshaller;

/**
 * 
 * @author martins
 *
 */
public class FaultTolerantScheduler {

	private static final Logger logger = Logger.getLogger(FaultTolerantScheduler.class);
	
	/**
	 * the executor of timer tasks
	 */
	private final ScheduledThreadPoolExecutor executor;
	
	/**
	 * the scheduler cluster data key
	 */
	private final FaultTolerantSchedulerClusterDataKey clusterDataKey;
	
	/**
	 * the interface to scheduler's data in cluster
	 */
	private final ClusterData clusterData;
	
	/**
	 * the jta tx manager
	 */
	private final TransactionManager txManager;
	
	/**
	 * the local running tasks. NOTE: never ever check for values, class instances may differ due cache replication, ALWAYS use keys.
	 */
	private final ConcurrentHashMap<Serializable, TimerTask> localRunningTasks = new ConcurrentHashMap<Serializable, TimerTask>();
	
	/**
	 * the timer task factory associated with this scheduler
	 */
	private TimerTaskFactory timerTaskFactory;
		
	/**
	 * the scheduler name
	 */
	private final String name;
		
	/**
	 * the mobicents cluster 
	 */
	private final Cluster<?> cluster;
	
	/**
	 * listener for events in mobicents cluster
	 */
	private final ClientLocalListener clusterClientLocalListener;
	
	/**
	 * 
	 * @param name
	 * @param corePoolSize
	 * @param cluster
	 * @param priority
	 * @param txManager
	 * @param timerTaskFactory
	 */
	public FaultTolerantScheduler(String name, int corePoolSize, Cluster<?> cluster, byte priority, TransactionManager txManager,TimerTaskFactory timerTaskFactory) {
		this.name = name;
		this.executor = new ScheduledThreadPoolExecutor(corePoolSize);
		this.clusterDataKey = new FaultTolerantSchedulerClusterDataKey(name);
		this.cluster = cluster;
		this.clusterData = cluster.getClusterDataSource().getClusterData(clusterDataKey);
		this.timerTaskFactory = timerTaskFactory;
		this.txManager = txManager;		
		clusterClientLocalListener = new ClientLocalListener(priority);
		if (!cluster.getClusterDataSource().isLocalMode()) {
			cluster.addFailOverListener(clusterClientLocalListener);
			cluster.addDataRemovalListener(clusterClientLocalListener);
			cluster.getClusterDataMarshalerManagement().register(FaultTolerantSchedulerClusterDataMarshaller.ID, new FaultTolerantSchedulerClusterDataMarshaller());
			cluster.getClusterDataMarshalerManagement().register(TimerTaskClusterDataMarshaller.ID, new TimerTaskClusterDataMarshaller());
		}
	}

	/**
	 * Retrieves the {@link TimerTaskData} associated with the specified taskID. 
	 * @param taskID
	 * @return null if there is no such timer task data
	 */
	public TimerTaskData getTimerTaskData(Serializable taskID) {
		final TimerTaskClusterDataKey key = new TimerTaskClusterDataKey(name, taskID);
		final ClusterData clusterData = cluster.getClusterDataSource().getClusterData(key);
		return (TimerTaskData) clusterData.getDataObject();
	}
	
	/**
	 * Retrieves the executor of timer tasks.
	 * @return
	 */
	ScheduledThreadPoolExecutor getExecutor() {
		return executor;
	}
	
	/**
	 * Retrieves local running tasks map.
	 * @return
	 */
	ConcurrentHashMap<Serializable, TimerTask> getLocalRunningTasksMap() {
		return localRunningTasks;
	}
	
	/**
	 * Retrieves a set containing all local running tasks. Removals on the set
	 * will not be propagated to the internal state of the scheduler.
	 * 
	 * @return
	 */
	public Set<TimerTask> getLocalRunningTasks() {
		return new HashSet<TimerTask>(localRunningTasks.values());
	}
	
	/**
	 *  Retrieves the scheduler name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 *  Retrieves the priority of the scheduler as a client local listener of the mobicents cluster.
	 * @return the priority
	 */
	public byte getPriority() {
		return clusterClientLocalListener.getPriority();
	}
	
	/**
	 * Retrieves the jta tx manager.
	 * @return
	 */
	public TransactionManager getTransactionManager() {
		return txManager;
	}

	/**
	 * Retrieves the timer task factory associated with this scheduler.
	 * @return
	 */
	public TimerTaskFactory getTimerTaskFactory() {
		return timerTaskFactory;
	}
	
	// logic 
	
	public void schedule(TimerTask task) {
		schedule(task, true);
	}
	/**
	 * Schedules the specified task.
	 * 
	 * @param task
	 */
	public void schedule(TimerTask task, boolean checkIfAlreadyPresent) {
		
		final TimerTaskData taskData = task.getData(); 
		final Serializable taskID = taskData.getTaskID();
		task.setScheduler(this);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Scheduling task with id "+taskID);
		}
		
		// store the task and data
		final ClusterData timerTaskClusterData = cluster.getClusterDataSource().getClusterData(new TimerTaskClusterDataKey(name,taskID));
		if (timerTaskClusterData.getDataObject() == null) {
			// create
			timerTaskClusterData.setOwner();
			timerTaskClusterData.setDataObject(taskData);
		} else if(checkIfAlreadyPresent) {
            throw new IllegalStateException("timer task " + taskID + " already scheduled");
		}
				
		// schedule task
		final SetTimerAfterTxCommitRunnable setTimerAction = new SetTimerAfterTxCommitRunnable(task, this);
		if (txManager != null) {
			try {
				final Transaction tx = txManager.getTransaction();
				if (tx != null) {
					// schedules timer on commit
					// TODO confirm if action runs after infinispan, otherwise need to find a way to force such order
					tx.registerSynchronization(new TransactionSynchronization(null,setTimerAction,null));					
					task.setSetTimerTransactionalAction(setTimerAction);
				}
				else {
					setTimerAction.run();
				}
			}
			catch (Throwable e) {
				remove(taskID,true);
				throw new RuntimeException("Unable to register tx synchronization object",e);
			}
		}
		else {
			setTimerAction.run();
		}		
	}

	/**
	 * Cancels a local running task with the specified ID.
	 * 
	 * @param taskID
	 * @return the task canceled
	 */
	public TimerTask cancel(Serializable taskID) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Canceling task with timer id "+taskID);
		}
		
		TimerTask task = localRunningTasks.get(taskID);
		if (task != null) {
			// remove task data
			removeTaskDataFromCluster(taskID);

			final SetTimerAfterTxCommitRunnable setAction = task.getSetTimerTransactionalAction();
			if (setAction != null) {
				// we have a tx action scheduled to run when tx commits, to set the timer, lets simply cancel it
				setAction.cancel();
			}
			else {
				// do cancellation
				Runnable cancelAction = new CancelTimerAfterTxCommitRunnable(task,this);
				if (txManager != null) {
					try {
						// TODO confirm order is corrected
						// Fixes Issue 2131 http://code.google.com/p/mobicents/issues/detail?id=2131
 						// Calling cancel then schedule on a timer with the same Id in Transaction Context make them run reversed
 						// so registerItAtTail to have them ordered correctly
						final Transaction tx = txManager.getTransaction();
						if (tx != null) {
							tx.registerSynchronization(new TransactionSynchronization(null,cancelAction,null));					
						}
						else {
							cancelAction.run();
						}
					}
					catch (Throwable e) {
						throw new RuntimeException("unable to register tx synchronization object",e);
					}
				}
				else {
					cancelAction.run();
				}			
			}		
		}
		else {
			// not found locally, we remove it from the cache still in case it is present
			remove(taskID, true);
		}
		
		return task;
	}
	
	void remove(Serializable taskID,boolean removeFromCache) {
		if(logger.isDebugEnabled())
		{
			logger.debug("remove() : "+taskID+" - "+removeFromCache);
		}
		
		localRunningTasks.remove(taskID);
		if(removeFromCache) {
			removeTaskDataFromCluster(taskID);
		}
	}
	
	private void removeTaskDataFromCluster(Serializable taskID) {
		final TimerTaskClusterDataKey ttKey = new TimerTaskClusterDataKey(name, taskID);
		clusterData.removeReference(ttKey);
		cluster.getClusterDataSource().getClusterData(ttKey).remove(false);
	}
	
	/**
	 * Recovers a timer task that was running in another node.
	 * 
	 * @param taskData
	 */
	private void recover(TimerTaskData taskData) {
		TimerTask task = timerTaskFactory.newTimerTask(taskData);
		if (logger.isDebugEnabled()) {
			logger.debug("Recovering task with id "+taskData.getTaskID());
		}
		task.beforeRecover();
		// on recovery the task will already be in the cache so we don't check for it
		// or an IllegalStateException will be thrown
		schedule(task, false);
	}

	public void shutdownNow() {
		if (logger.isDebugEnabled()) {
			logger.debug("Shutdown now.");
		}
		if (!cluster.getClusterDataSource().isLocalMode()) {
			cluster.removeFailOverListener(clusterClientLocalListener);
			cluster.removeDataRemovalListener(clusterClientLocalListener);
			cluster.getClusterDataMarshalerManagement().unregister(FaultTolerantSchedulerClusterDataMarshaller.ID);
			cluster.getClusterDataMarshalerManagement().unregister(TimerTaskClusterDataMarshaller.ID);
		}		
		executor.shutdownNow();
		localRunningTasks.clear();
	}
	
	@Override
	public String toString() {
		return "FaultTolerantScheduler [ name = "+name+" ]";
	}
	
	public String toDetailedString() {
		return "FaultTolerantScheduler [ name = "+name+" , local tasks = "+localRunningTasks.size()+" , all tasks "+clusterData.getReferences().length+" ]";
	}
	
	public void stop() {
		this.shutdownNow();		
	}
	
	private class ClientLocalListener implements ClusterDataFailOverListener, ClusterDataRemovalListener {

		/**
		 * the priority of the scheduler as a client local listener of the mobicents cluster
		 */
		private final byte priority;
				
		/**
		 * @param priority
		 */
		public ClientLocalListener(byte priority) {
			this.priority = priority;
		}

		/*
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.ClusterDataFailOverListener#getListenerKey()
		 */
		@Override
		public ClusterDataKey getListenerKey() {
			return clusterDataKey;
		}

		/*
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.ClusterDataFailOverListener#getLocalElector()
		 */
		@Override
		public LocalFailoverElector getLocalElector() {
			return null;
		}
		
		/* 
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.FailOverListener#getPriority()
		 */
		public byte getPriority() {
			return priority;
		}

		/*
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.ClusterDataFailOverListener#failOverClusterMember(org.mobicents.cluster.ClusterNodeAddress)
		 */
		@Override
		public void failOverClusterMember(ClusterNodeAddress address) {
			
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.ClusterDataFailOverListener#lostOwnership(org.mobicents.cluster.ClusterData)
		 */
		@Override
		public void lostOwnership(ClusterData clusterData) {
			
		}

		/*
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.ClusterDataFailOverListener#wonOwnership(org.mobicents.cluster.ClusterData)
		 */
		@Override
		public void wonOwnership(ClusterData clusterData) {
			if (logger.isDebugEnabled()) {
				logger.debug("wonOwnership( clusterData = "+clusterData+")");
			}

			try {
				recover((TimerTaskData) clusterData.getDataObject());
			}
			catch (Throwable e) {
				logger.error(e.getMessage(),e);
			}
			
		}
		
		@Override
		public void dataRemoved(ClusterDataKey removedReferencedKey) {
			final Serializable taskId = ((TimerTaskClusterDataKey)removedReferencedKey).getTaskID();
			final TimerTask task = localRunningTasks.remove(taskId);
			if (task != null) {
				task.cancel();
			}
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return FaultTolerantScheduler.this.toString();
		}
		
	}
}
