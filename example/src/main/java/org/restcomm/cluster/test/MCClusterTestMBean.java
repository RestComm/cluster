package org.restcomm.cluster.test;

import javax.management.MXBean;
import javax.transaction.TransactionManager;

import org.restcomm.timers.timer.FaultTolerantTimer;

@MXBean
public interface MCClusterTestMBean {

	public TransactionManager getJta();

	public void setJta(TransactionManager jta);

	public void setFaultTolerantTimer(FaultTolerantTimer faultTolerantTimer);

	public FaultTolerantTimer getFaultTolerantTimer();

	public void createTimer(long milis);
	
	public void removeTimer(String id);
	

}
