package org.mobicents.cluster.example.as5;

import javax.transaction.TransactionManager;

import org.mobicents.timers.timer.FaultTolerantTimer;

public interface FaultTolerantTimerServiceExampleMBean {

	public TransactionManager getJta();

	public void setJta(TransactionManager jta);

	public void setFaultTolerantTimer(FaultTolerantTimer faultTolerantTimer);

	public FaultTolerantTimer getFaultTolerantTimer();

	public void createTimer(long milis);

}
