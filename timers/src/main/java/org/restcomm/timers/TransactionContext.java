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
import java.util.HashMap;
import java.util.Map;

import org.restcomm.timers.AfterTxCommitRunnable.Type;

public class TransactionContext implements Runnable {
	
	private Map<Serializable,AfterTxCommitRunnable> map = new HashMap<Serializable, AfterTxCommitRunnable>(); 
	
	public void put(Serializable taskId, AfterTxCommitRunnable r) {
		final AfterTxCommitRunnable q = map.put(taskId,r);
		if (q != null && q.getType() == Type.SET) {
			// if there was a set timer runnable then we don't need to keep the cancel one
			map.remove(taskId);
		}
	}
	
	public AfterTxCommitRunnable remove(Serializable taskId) {
		return map.remove(taskId);
	}
	
	@Override
	public void run() {
		for(AfterTxCommitRunnable r : map.values()) {
			r.run();		
		}
		map = null;
	}
}