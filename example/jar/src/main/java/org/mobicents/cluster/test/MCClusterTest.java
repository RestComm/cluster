package org.mobicents.cluster.test;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.mobicents.cluster.Cluster;
import org.mobicents.timers.timer.FaultTolerantTimer;


public class MCClusterTest implements MCClusterTestMBean {

	private TransactionManager jta;
	private FaultTolerantTimer faultTolerantTimer;
	private Cluster<?> cluster;
	
	public void start() {
		cluster.startCluster();
		System.out.println("Mobicents FT Timers Example Service started");
	}
	
	public void stop() {
		cluster.stopCluster();
		System.out.println("Mobicents FT Timers Example Service stopped");
	}
	
	public Cluster<?> getCluster() {
		return cluster;
	}
	
	public void setCluster(Cluster<?> cluster) {
		this.cluster = cluster;
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
		System.out.println("Timer set, delay = "+milis);
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
