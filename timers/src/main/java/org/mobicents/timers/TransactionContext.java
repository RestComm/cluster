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

package org.mobicents.timers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mobicents.timers.AfterTxCommitRunnable.Type;

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