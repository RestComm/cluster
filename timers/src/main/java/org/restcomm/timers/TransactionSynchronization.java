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

import javax.transaction.Status;
import javax.transaction.Synchronization;

/**
 * @author martins
 *
 */
public class TransactionSynchronization implements Synchronization {
	
	private final TransactionContext txContext;
	
	/**
	 * 
	 * @param txContext
	 */
	TransactionSynchronization(TransactionContext txContext) {
		this.txContext = txContext;
		TransactionContextThreadLocal.setTransactionContext(txContext);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.transaction.Synchronization#afterCompletion(int)
	 */
	public void afterCompletion(int status) {
		switch (status) {
			case Status.STATUS_COMMITTED:
				txContext.run();
				break;
			default:				
		}
		TransactionContextThreadLocal.setTransactionContext(null);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.transaction.Synchronization#beforeCompletion()
	 */
	public void beforeCompletion() {}
	
}
