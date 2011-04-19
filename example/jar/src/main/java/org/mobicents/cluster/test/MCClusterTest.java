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

package org.mobicents.cluster.test;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.jboss.cache.factories.annotations.Start;
import org.mobicents.timers.timer.FaultTolerantTimer;


public class MCClusterTest implements MCClusterTestMBean {

	private TransactionManager jta;
	private FaultTolerantTimer faultTolerantTimer;

	@Start
	public void start() {
		System.err.println("Started");
	}
	
	public TransactionManager getJta() {
		return jta;
	}

	public void setJta(TransactionManager jta) {
		this.jta = jta;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.jb51.cluster.MCClusterTestMBean#createTimer(long)
	 */
	public void createTimer(long milis) {

		try {
			jta.begin();
		} catch (NotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.faultTolerantTimer.schedule(new SerTimerTask(), milis);
		System.err.println("Timer set, delay = "+milis);
		try {
			jta.commit();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RollbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeuristicMixedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeuristicRollbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	/* (non-Javadoc)
	 * @see org.mobicents.cluster.test.MCClusterTestMBean#getFaultTolerantTimer()
	 */
	public FaultTolerantTimer getFaultTolerantTimer() {
		return faultTolerantTimer;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.cluster.test.MCClusterTestMBean#setFaultTolerantTimer(org.mobicents.timers.timer.FaultTolerantTimer)
	 */
	public void setFaultTolerantTimer(FaultTolerantTimer faultTolerantTimer) {
		this.faultTolerantTimer = faultTolerantTimer;
	}
	
}
