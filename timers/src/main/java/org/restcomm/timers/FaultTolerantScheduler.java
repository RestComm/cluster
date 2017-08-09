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

package org.restcomm.timers;

import java.io.Serializable;
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
import org.infinispan.remoting.transport.Address;
import org.restcomm.cluster.DataRemovalListener;
import org.restcomm.cluster.FailOverListener;
import org.restcomm.cluster.MobicentsCluster;
import org.restcomm.cluster.MobicentsClusterFactory;
import org.restcomm.cluster.cache.ClusteredCacheData;
import org.restcomm.cluster.election.ClientLocalListenerElector;
import org.restcomm.timers.cache.TimerTaskCacheData;

/**
 * 
 * @author martins
 * @author András Kőkuti
 *
 */
public class FaultTolerantScheduler {

	private static final Logger logger = Logger.getLogger(FaultTolerantScheduler.class);
	
	/**
	 * the executor of timer tasks
	 */
	private final ScheduledThreadPoolExecutor executor;
	
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
	
	//private FaultTolerantSchedulerCacheData cacheData;
	
	/**
	 * the scheduler name
	 */
	private final String name;
		
	/**
	 * the restcomm cluster 
	 */
	private final MobicentsCluster cluster;
	
	/**
	 * the restcomm cluster factory 
	 */
	private final MobicentsClusterFactory clusterFactory;
	
	/**
	 * listener for fail over events in restcomm cluster
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
	public FaultTolerantScheduler(String name, int corePoolSize, MobicentsClusterFactory clusterFactory, byte priority, TransactionManager txManager, TimerTaskFactory timerTaskFactory) {
		this(name, corePoolSize, clusterFactory, priority, txManager, timerTaskFactory, 0, Executors.defaultThreadFactory());
	}

    /**
     *
     * @param name
     * @param corePoolSize
     * @param cluster
     * @param priority
     * @param txManager
     * @param timerTaskFactory
     * @param threadFactory
     */
    public FaultTolerantScheduler(String name, int corePoolSize, MobicentsClusterFactory clusterFactory, byte priority, TransactionManager txManager, TimerTaskFactory timerTaskFactory, ThreadFactory threadFactory) {
        this(name, corePoolSize, clusterFactory, priority, txManager, timerTaskFactory, 0, threadFactory);
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
	public FaultTolerantScheduler(String name, int corePoolSize, MobicentsClusterFactory clusterFactory, byte priority, TransactionManager txManager,TimerTaskFactory timerTaskFactory, int purgePeriod) {
        this(name, corePoolSize, clusterFactory, priority, txManager, timerTaskFactory, purgePeriod, Executors.defaultThreadFactory());
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
    public FaultTolerantScheduler(String name, int corePoolSize, MobicentsClusterFactory clusterFactory, byte priority, TransactionManager txManager,TimerTaskFactory timerTaskFactory, int purgePeriod, ThreadFactory threadFactory) {
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
        
        this.clusterFactory = clusterFactory;
        this.cluster = clusterFactory.getCluster(name);
        this.timerTaskFactory = timerTaskFactory;
        this.txManager = txManager;
        
        clusterClientLocalListener = new ClientLocalListener(priority);
        cluster.addFailOverListener(clusterClientLocalListener);
        cluster.addDataRemovalListener(clusterClientLocalListener);
        cluster.startCluster();
    }

	/**
	 * Retrieves the {@link TimerTaskData} associated with the specified taskID. 
	 * @param taskID
	 * @return null if there is no such timer task data
	 */
	public TimerTaskData getTimerTaskData(Serializable taskID) {
		TimerTaskCacheData timerTaskCacheData = new TimerTaskCacheData(taskID, cluster.getMobicentsCache());
		return timerTaskCacheData.getTaskData();		
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
	 * Retrieves a local running task by its id
	 * 
	 * @return the local task if found, null otherwise
	 */
	public TimerTask getLocalRunningTask(Serializable taskId) {
		return localRunningTasks.get(taskId);
	}
	
	/**
	 *  Retrieves the scheduler name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 *  Retrieves the priority of the scheduler as a client local listener of the restcomm cluster.
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
			logger.debug("Scheduling task with id " + taskID);
		}
		
		// store the task and data
		final TimerTaskCacheData timerTaskCacheData = new TimerTaskCacheData(taskID, cluster.getMobicentsCache());
		if (timerTaskCacheData.create()) {
			if (logger.isInfoEnabled()) {
				logger.info("Storing task data " + taskID);
			}
			timerTaskCacheData.setTaskData(taskData);
		} else if(checkIfAlreadyPresent) {
            throw new IllegalStateException("timer task " + taskID + " already scheduled");
		}
				
		// schedule task
		final SetTimerAfterTxCommitRunnable setTimerAction = new SetTimerAfterTxCommitRunnable(task, this);
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
			new TimerTaskCacheData(taskID, cluster.getMobicentsCache()).removeElement();

			final SetTimerAfterTxCommitRunnable setAction = task.getSetTimerTransactionalAction();
			if (setAction != null) {
				// we have a tx action scheduled to run when tx commits, to set the timer, lets simply cancel it
				setAction.cancel();
			}
			else {
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
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Not a local task");
			}
			// not found locally
			// if there is a tx context there may be a set timer action there
			if (txManager != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Txmanager not null");
				}
				try {
					Transaction tx = txManager.getTransaction();
					if (tx != null) {
						if (logger.isDebugEnabled()) {
							logger.debug("Tx not null");
						}
						TransactionContext txContext = TransactionContextThreadLocal.getTransactionContext();
						if (txContext != null) {
							if (logger.isDebugEnabled()) {
								logger.debug("Tx context not null");
							}
							final AfterTxCommitRunnable r = txContext.remove(taskID);
							if (r != null) {
								logger.debug("removing");
								task = r.task;
								// remove from cluster
								new TimerTaskCacheData(taskID, cluster.getMobicentsCache()).removeElement();
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
	
	void remove(Serializable taskID,boolean removeFromCache) {
		if(logger.isDebugEnabled())
		{
			logger.debug("remove() : "+taskID+" - "+removeFromCache);
		}
		
		localRunningTasks.remove(taskID);
		if(removeFromCache)
			new TimerTaskCacheData(taskID, cluster.getMobicentsCache()).removeElement();
	}
	
	/**
	 * Recovers a timer task that was running in another node.
	 * 
	 * @param taskData
	 */
	private void recover(TimerTaskData taskData) {
		TimerTask task = timerTaskFactory.newTimerTask(taskData);
		if(task != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Recovering task with id "+taskData.getTaskID());
			}
			task.beforeRecover();
			// on recovery the task will already be in the cache so we don't check for it
			// or an IllegalStateException will be thrown
			schedule(task, false);
		}
	}

	public void shutdownNow() {
		if (logger.isDebugEnabled()) {
			logger.debug("Shutdown now.");
		}
		cluster.removeFailOverListener(clusterClientLocalListener);
		cluster.removeDataRemovalListener(clusterClientLocalListener);
		
		clusterFactory.stopCluster(name);
		executor.shutdownNow();
		localRunningTasks.clear();
	}
	
	@Override
	public String toString() {
		return "FaultTolerantScheduler [ name = "+name+" ]";
	}
	
	public String toDetailedString() {		
		return "FaultTolerantScheduler [ name = "+name+" , local tasks = "+localRunningTasks.size() + " ]";
	}
	
	public void stop() {
		this.shutdownNow();		
	}
	
	private class ClientLocalListener implements FailOverListener, DataRemovalListener {

		/**
		 * the priority of the scheduler as a client local listener of the restcomm cluster
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
		 * @see org.mobicents.cluster.FailOverListener#getElector()
		 */
		public ClientLocalListenerElector getElector() {
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
		 * @see org.mobicents.cluster.FailOverListener#failOverClusterMember(org.jgroups.Address)
		 */
		public void failOverClusterMember(Address address) {
			
		}
		
		/* 
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.FailOverListener#lostOwnership(org.mobicents.cluster.cache.ClusteredCacheData)
		 */
		public void lostOwnership(@SuppressWarnings("rawtypes") ClusteredCacheData clusteredCacheData) {
			
		}

		/* 
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.FailOverListener#wonOwnership(org.mobicents.cluster.cache.ClusteredCacheData)
		 */
		public void wonOwnership(@SuppressWarnings("rawtypes") ClusteredCacheData clusteredCacheData) {
			
			if (logger.isDebugEnabled()) {
				logger.debug("wonOwnership( clusterCacheData = "+clusteredCacheData+")");
			}

			try {
				@SuppressWarnings("unchecked")
				Serializable taskID = TimerTaskCacheData.getTaskID(clusteredCacheData);
				TimerTaskCacheData timerTaskCacheData = new TimerTaskCacheData(taskID, cluster.getMobicentsCache());
				recover(timerTaskCacheData.getTaskData());
			}
			catch (Throwable e) {
				logger.error(e.getMessage(),e);
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.DataRemovalListener#dataRemoved(org.jboss.cache.Fqn)
		 */
		public void dataRemoved(Object key) {			
			if (logger.isDebugEnabled()) {
				logger.debug("remote notification dataRemoved, lastElement " + key);
			}
			final TimerTask task = localRunningTasks.remove(key);
			if (task != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("remote notification dataRemoved( task = "+task.getData().getTaskID()+" removed locally cancelling it");
				}
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
