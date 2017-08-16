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

package org.restcomm.timers;

import java.io.Serializable;

import org.apache.log4j.Logger;


/**
 * Runnable to cancel a timer task after the tx commits.
 * @author martins
 *
 */
public class CancelTimerAfterTxCommitRunnable extends AfterTxCommitRunnable {

	private static final Logger logger = Logger.getLogger(CancelTimerAfterTxCommitRunnable.class);
	
	CancelTimerAfterTxCommitRunnable(TimerTask task,FaultTolerantScheduler scheduler) {
		super(task,scheduler);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.timers.AfterTxCommitRunnable#getType()
	 */
	public Type getType() {
		return AfterTxCommitRunnable.Type.CANCEL;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		final TimerTaskData taskData = task.getData();
		final Serializable taskID = taskData.getTaskID();
		
		if (logger.isDebugEnabled()) {
			logger.debug("Cancelling timer task for timer ID "+taskID);
		}
		
		scheduler.getLocalRunningTasksMap().remove(taskID);
		
		try {
			task.cancel();					
		}
		catch (Throwable e) {
			logger.error(e.getMessage(),e);
		}
	}
	
}
