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
 *
 * This file incorporates work covered by the following copyright contributed under the GNU LGPL : Copyright 2007-2011 Red Hat.
 */
package org.mobicents.timers;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.mobicents.cluster.Cluster;
import org.mobicents.cluster.ClusterNodeAddress;
import org.mobicents.cluster.data.ClusterData;
import org.mobicents.cluster.data.ClusterDataKey;
import org.mobicents.cluster.elector.LocalFailoverElector;
import org.mobicents.cluster.listener.ClusterDataFailOverListener;
import org.mobicents.cluster.listener.ClusterDataRemovalListener;
import org.mobicents.timers.cluster.FaultTolerantSchedulerClusterDataKey;
import org.mobicents.timers.cluster.TimerTaskClusterDataKey;

/**
 * 
 * @author martins
 * 
 */
public class FaultTolerantScheduler {

	private static final Logger logger = Logger
			.getLogger(FaultTolerantScheduler.class);

	public static final Object DATA_REMOVAL_LISTENER_ID = new Object();

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
	private final ClusterData schedulerClusterData;

	/**
	 * the jta tx manager
	 */
	private final TransactionManager txManager;

	/**
	 * the local running tasks. NOTE: never ever check for values, class
	 * instances may differ due cache replication, ALWAYS use keys.
	 */
	private final ConcurrentHashMap<String, TimerTask> localRunningTasks = new ConcurrentHashMap<String, TimerTask>();

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
	public <T extends TimerTaskData> FaultTolerantScheduler(String name, int corePoolSize,
			Cluster<?> cluster, byte priority, TransactionManager txManager,
			TimerTaskFactory timerTaskFactory) {
		this(name, corePoolSize, cluster, priority, txManager, timerTaskFactory, 0);
	}
	
	/**
	 * 
	 * @param name
	 * @param corePoolSize
	 * @param cluster
	 * @param priority
	 * @param txManager
	 * @param timerTaskFactory
	 * @param purgePeriod
	 */
	public <T extends TimerTaskData> FaultTolerantScheduler(String name, int corePoolSize,
			Cluster<?> cluster, byte priority, TransactionManager txManager,
			TimerTaskFactory timerTaskFactory, int purgePeriod) {
		this(name, corePoolSize, cluster, priority, txManager, timerTaskFactory, purgePeriod, Executors.defaultThreadFactory());
	}

    /**
     *
     * @param name
     * @param corePoolSize
     * @param cluster
     * @param priority
     * @param txManager
     * @param timerTaskFactory
     * @param purgePeriod
     * @param threadFactory
     */
    public <T extends TimerTaskData> FaultTolerantScheduler(String name, int corePoolSize, Cluster<?> cluster, byte priority, TransactionManager txManager,TimerTaskFactory timerTaskFactory, int purgePeriod, ThreadFactory threadFactory) {
        this.name = name;
        this.executor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
        if(purgePeriod > 0) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        executor.purge();
                    }
                    catch (Exception e) {
                        logger.error("failed to execute purge",e);
                    }
                }
            };
            this.executor.scheduleWithFixedDelay(r, purgePeriod, purgePeriod, TimeUnit.MINUTES);
        }
        this.clusterDataKey = new FaultTolerantSchedulerClusterDataKey(name);
	this.cluster = cluster;
	this.schedulerClusterData = cluster.getClusterDataSource()
			.getClusterData(clusterDataKey);
	this.timerTaskFactory = timerTaskFactory;
	this.txManager = txManager;
	if (!cluster.isLocalMode()) {
		clusterClientLocalListener = new ClientLocalListener(priority);
		cluster.addFailOverListener(clusterClientLocalListener);
		cluster.addDataRemovalListener(clusterClientLocalListener);
		if (cluster.isStarted()) {
			schedulerClusterData.initReferences();
		}
	}		
	else {
		clusterClientLocalListener = null;
	}
    }

	/**
	 * Retrieves the {@link TimerTaskData} associated with the specified taskID.
	 * 
	 * @param taskID
	 * @return null if there is no such timer task data
	 */
	public TimerTaskData getTimerTaskData(String taskID) {
		final TimerTaskClusterDataKey key = new TimerTaskClusterDataKey(name,
				taskID);
		final ClusterData clusterData = cluster.getClusterDataSource()
				.getClusterData(key);
		return (TimerTaskData) clusterData.getDataObject();
	}

	/**
	 * Retrieves the executor of timer tasks.
	 * 
	 * @return
	 */
	ScheduledThreadPoolExecutor getExecutor() {
		return executor;
	}

	/**
	 * Retrieves local running tasks map.
	 * 
	 * @return
	 */
	ConcurrentHashMap<String, TimerTask> getLocalRunningTasksMap() {
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
	 * Retrieves a local running task by its id
	 * 
	 * @return the local task if found, null otherwise
	 */
	public TimerTask getLocalRunningTask(String taskId) {
		return localRunningTasks.get(taskId);
	}

	/**
	 * Retrieves the scheduler name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves the priority of the scheduler as a client local listener of the
	 * mobicents cluster.
	 * 
	 * @return the priority
	 */
	public byte getPriority() {
		return clusterClientLocalListener.getPriority();
	}

	/**
	 * Retrieves the jta tx manager.
	 * 
	 * @return
	 */
	public TransactionManager getTransactionManager() {
		return txManager;
	}

	/**
	 * Retrieves the timer task factory associated with this scheduler.
	 * 
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
		final String taskID = task.getTaskID();
		task.setScheduler(this);

		if (logger.isDebugEnabled()) {
			logger.debug("Scheduling task with id " + taskID);
		}

		// store the task and data
		final ClusterData timerTaskClusterData = cluster.getClusterDataSource()
				.getClusterData(new TimerTaskClusterDataKey(name, taskID));
		if (timerTaskClusterData.getDataObject() == null) {
			// create
			timerTaskClusterData.setOwner();
			timerTaskClusterData.setDataObject(taskData);
			schedulerClusterData.addReference(timerTaskClusterData.getKey());
		} else if (checkIfAlreadyPresent) {
			throw new IllegalStateException("timer task " + taskID
					+ " already scheduled");
		}

		// schedule task
		final SetTimerAfterTxCommitRunnable setTimerAction = new SetTimerAfterTxCommitRunnable(
				task, this);
		if (txManager != null) {
			try {
				Transaction tx = txManager.getTransaction();
				if (tx != null) {
					TransactionContext txContext = TransactionContextThreadLocal.getTransactionContext();
					if (txContext == null) {
						txContext = new TransactionContext();
						tx.registerSynchronization(new TransactionSynchronization(txContext));
					}
					txContext.put(taskID, setTimerAction);					
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
		} else {
			setTimerAction.run();
		}
	}

	/**
	 * Cancels a local running task with the specified ID.
	 * 
	 * @param taskID
	 * @return the task canceled
	 */
	public TimerTask cancel(String taskID) {

		if (logger.isDebugEnabled()) {
			logger.debug("Canceling task with timer id " + taskID);
		}

		TimerTask task = localRunningTasks.get(taskID);
		if (task != null) {
			// remove task data
			removeTaskDataFromCluster(taskID);

			final SetTimerAfterTxCommitRunnable setAction = task
					.getSetTimerTransactionalAction();
			if (setAction != null) {
				// we have a tx action scheduled to run when tx commits, to set
				// the timer, lets simply cancel it
				setAction.cancel();
			} else {
				// do cancellation
				AfterTxCommitRunnable runnable = new CancelTimerAfterTxCommitRunnable(task,this);
				if (txManager != null) {
					try {
						Transaction tx = txManager.getTransaction();
						if (tx != null) {
							TransactionContext txContext = TransactionContextThreadLocal.getTransactionContext();
							if (txContext == null) {
								txContext = new TransactionContext();
								tx.registerSynchronization(new TransactionSynchronization(txContext));
							}
							txContext.put(taskID, runnable);					
						}
						else {
							runnable.run();
						}
					}
					catch (Throwable e) {
						throw new RuntimeException("Unable to register tx synchronization object",e);
					}
				}
				else {
					runnable.run();
				}	
			}
		} else {
			// not found locally
			// if there is a tx context there may be a set timer action there
			if (txManager != null) {
				try {
					Transaction tx = txManager.getTransaction();
					if (tx != null) {
						TransactionContext txContext = TransactionContextThreadLocal.getTransactionContext();
						if (txContext != null) {
							final AfterTxCommitRunnable r = txContext.remove(taskID);
							if (r != null) {
								task = r.task;
								// remove from cluster
								removeTaskDataFromCluster(taskID);
							}							
						}											
					}
				}
				catch (Throwable e) {
					throw new RuntimeException("Failed to check tx context.",e);
				}
			}		
		}

		return task;
	}

	void remove(String taskID, boolean removeFromCache) {
		if (logger.isDebugEnabled()) {
			logger.debug("remove() : " + taskID + " - " + removeFromCache);
		}

		localRunningTasks.remove(taskID);
		if (removeFromCache) {
			removeTaskDataFromCluster(taskID);
		}
	}

	private void removeTaskDataFromCluster(String taskID) {
		final TimerTaskClusterDataKey ttKey = new TimerTaskClusterDataKey(name,
				taskID);
		schedulerClusterData.removeReference(ttKey);
		cluster.getClusterDataSource().getClusterData(ttKey).remove(false);
	}

	/**
	 * Recovers a timer task that was running in another node.
	 * 
	 * @param taskData
	 */
	private void recover(String taskID, TimerTaskData taskData) {
		TimerTask task = timerTaskFactory.newTimerTask(taskID, taskData);
		if (task != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Recovering task with id " + task.getTaskID());
			}
			task.beforeRecover();
			// on recovery the task will already be in the cache so we don't
			// check for it
			// or an IllegalStateException will be thrown
			schedule(task, false);
		}
	}

	public void shutdownNow() {
		if (logger.isDebugEnabled()) {
			logger.debug("Shutdown now.");
		}
		if (clusterClientLocalListener != null) {
			cluster.removeFailOverListener(clusterClientLocalListener);
			cluster.removeDataRemovalListener(clusterClientLocalListener);		
		}
		executor.shutdownNow();
		localRunningTasks.clear();
	}

	@Override
	public String toString() {
		return "FaultTolerantScheduler [ name = " + name + " ]";
	}

	public String toDetailedString() {
		return "FaultTolerantScheduler [ name = " + name + " , local tasks = "
				+ localRunningTasks.size() + " , all tasks "
				+ schedulerClusterData.getReferences().length + " ]";
	}

	public void stop() {
		this.shutdownNow();
	}

	private class ClientLocalListener implements ClusterDataFailOverListener,
			ClusterDataRemovalListener {
		
		/**
		 * the priority of the scheduler as a client local listener of the
		 * mobicents cluster
		 */
		private final byte priority;

		/**
		 * @param priority
		 */
		ClientLocalListener(byte priority) {
			this.priority = priority;
		}

		@Override
		public Object getDataRemovalListenerID() {
			return DATA_REMOVAL_LISTENER_ID;
		}
		
		@Override
		public ClusterDataKey getDataFailoverListenerKey() {
			return clusterDataKey;
		}

		@Override
		public LocalFailoverElector getLocalElector() {
			return null;
		}

		public byte getPriority() {
			return priority;
		}

		@Override
		public void failOverClusterMember(ClusterNodeAddress address) {

		}

		@Override
		public void failover(ClusterData clusterData) {
			if (logger.isDebugEnabled()) {
				logger.debug("failover( clusterData = " + clusterData + ")");
			}

			try {
				// this node now owns the timer task
				clusterData.setOwner();
				// recover the timer task 
				final TimerTaskClusterDataKey key = (TimerTaskClusterDataKey) clusterData
						.getKey();
				final TimerTaskData taskData = (TimerTaskData) clusterData
						.getDataObject();
				recover(key.getTaskID(), taskData);
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}

		}

		@Override
		public void dataRemoved(ClusterDataKey removedReferencedKey) {
			final String taskId = ((TimerTaskClusterDataKey) removedReferencedKey)
					.getTaskID();
			final TimerTask task = localRunningTasks.remove(taskId);
			if (task != null) {
				task.cancel();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return FaultTolerantScheduler.this.toString();
		}

	}
}
