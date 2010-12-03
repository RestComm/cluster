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

import javax.transaction.Status;
import javax.transaction.Synchronization;

/**
 * @author martins
 *
 */
public class TransactionSynchronization implements Synchronization {
	
	private final Runnable beforeCommitAction;
	private final Runnable commitAction;
	private final Runnable rollbackAction;
	
	/**
	 * @param beforeCommitAction
	 * @param commitAction
	 * @param rollbackAction
	 */
	TransactionSynchronization(Runnable beforeCommitAction,Runnable commitAction,
			Runnable rollbackAction) {
		this.beforeCommitAction = beforeCommitAction;
		this.commitAction = commitAction;
		this.rollbackAction = rollbackAction;
	}


	/*
	 * (non-Javadoc)
	 * @see javax.transaction.Synchronization#afterCompletion(int)
	 */
	public void afterCompletion(int status) {
		switch (status) {
			case Status.STATUS_COMMITTED:
				if (commitAction != null)
					commitAction.run();
				break;

			case Status.STATUS_ROLLEDBACK:
				if (rollbackAction != null)
					rollbackAction.run();
				break;

			default:				
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.transaction.Synchronization#beforeCompletion()
	 */
	public void beforeCompletion() {			
		if (beforeCommitAction != null) {
			beforeCommitAction.run();
		}
	}
	
}
