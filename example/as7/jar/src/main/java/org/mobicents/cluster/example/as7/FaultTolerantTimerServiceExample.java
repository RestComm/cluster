package org.mobicents.cluster.example.as7;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.infinispan.config.Configuration;
import org.infinispan.config.Configuration.CacheMode;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.transaction.lookup.TransactionManagerLookup;
import org.infinispan.util.concurrent.IsolationLevel;
import org.mobicents.cluster.Cluster;
import org.mobicents.cluster.infinispan.InfinispanCluster;
import org.mobicents.cluster.infinispan.data.InfinispanClusterDataSource;
import org.mobicents.timers.timer.FaultTolerantTimer;

public class FaultTolerantTimerServiceExample implements FaultTolerantTimerServiceExampleMBean {

	private TransactionManager txManager;
	
	private FaultTolerantTimer faultTolerantTimer;
	private Cluster<?> cluster;

	public void start() {
		try {
			txManager = (TransactionManager)  new InitialContext().lookup("java:jboss/TransactionManager");
		} catch (NamingException e) {
			e.printStackTrace();
		}
		/*
		 * <bean name="Mobicents.Cluster.DataSource.GlobalConfig"
		 * class="org.infinispan.config.GlobalConfiguration"> <property
		 * name="clusterName">mobicents-cluster</property> <property
		 * name="transportClass"
		 * >org.infinispan.remoting.transport.jgroups.JGroupsTransport
		 * </property> <property
		 * name="exposeGlobalJmxStatistics">true</property> </bean>
		 */
		GlobalConfiguration globalConfiguration = new GlobalConfiguration();
		globalConfiguration.setClusterName("mobicents-cluster");
		globalConfiguration.setTransportClass(JGroupsTransport.class.getName());
		globalConfiguration.setExposeGlobalJmxStatistics(true);		
		/*
		 * <bean name="Mobicents.Cluster.DataSource.Config"
		 * class="org.infinispan.config.Configuration"> <property
		 * name="transactionManagerLookupClass"
		 * >org.infinispan.transaction.lookup
		 * .JBossTransactionManagerLookup</property> <property
		 * name="isolationLevel">REPEATABLE_READ</property> <property
		 * name="cacheMode">DIST_SYNC</property> <property
		 * name="exposeJmxStatistics">true</property>
		 * <demand>RealTransactionManager</demand> </bean>
		 */		
		Configuration configuration = new Configuration();
		TransactionManagerLookup txManagerLookup = new TransactionManagerLookup() {			
			@Override
			public TransactionManager getTransactionManager() throws Exception {
				return txManager;
			}
		};
		configuration.fluent().transactionManagerLookup(txManagerLookup);
				
		configuration.setIsolationLevel(IsolationLevel.REPEATABLE_READ);
		configuration.setCacheMode(CacheMode.DIST_SYNC);
		configuration.setExposeJmxStatistics(true);		
		/*
		 * <bean name="Mobicents.Cluster.DataSource"
		 * class="org.mobicents.cluster.infinispan.data.InfinispanClusterDataSource"
		 * > <constructor> <parameter
		 * class="org.infinispan.config.GlobalConfiguration"><inject
		 * bean="Mobicents.Cluster.DataSource.GlobalConfig"/></parameter>
		 * <parameter><inject
		 * bean="Mobicents.Cluster.DataSource.Config"/></parameter>
		 * <parameter><inject bean="JMXKernel"
		 * property="mbeanServer"/></parameter> </constructor> </bean>
		*/
		// TODO find out how to get the MBeanServer in AS7
		InfinispanClusterDataSource clusterDataSource = new InfinispanClusterDataSource(
				globalConfiguration, configuration,
				null);
				
		/*
		 * <bean name="Mobicents.Cluster"
		 * class="org.mobicents.cluster.infinispan.InfinispanCluster">
		 * <constructor> <parameter><inject
		 * bean="Mobicents.Cluster.DataSource"/></parameter> <parameter><inject
		 * bean="RealTransactionManager"/></parameter> </constructor> </bean>
		 */
		
		cluster = new InfinispanCluster(clusterDataSource,
				txManager);
				
		/*
		 * <bean name="Mobicents.FaultTolerantJavaUtilTimer"
		 * class="org.mobicents.timers.timer.FaultTolerantTimer"> <constructor>
		 * <parameter>timer</parameter> <parameter><inject
		 * bean="Mobicents.Cluster"/></parameter> <!-- cluster local listener
		 * priority --> <parameter>10</parameter> <parameter><inject
		 * bean="RealTransactionManager"/></parameter> </constructor> </bean>
		 */
		faultTolerantTimer = new FaultTolerantTimer("timer", cluster, (byte)10,
				txManager);
		cluster.startCluster();
		
		System.out.println("Mobicents FT Timers Example Service started");
	}

	public void stop() {
		cluster.stopCluster();		  
		cluster = null;
		faultTolerantTimer = null;
		txManager = null;
		
		System.out.println("Mobicents FT Timers Example Service stopped");
	}
/*
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
	}*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.jb51.cluster.MCClusterTestMBean#createTimer(long)
	 */
	public void createTimer(long milis) {

		try {
			txManager.begin();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.faultTolerantTimer.schedule(new SerTimerTask(), milis);
		System.out.println("Timer set, delay = " + milis);
		try {
			txManager.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.test.MCClusterTestMBean#getFaultTolerantTimer()
	 */
	/*public FaultTolerantTimer getFaultTolerantTimer() {
		return faultTolerantTimer;
	}*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.test.MCClusterTestMBean#setFaultTolerantTimer(org
	 * .mobicents.timers.timer.FaultTolerantTimer)
	 */
	/*public void setFaultTolerantTimer(FaultTolerantTimer faultTolerantTimer) {
		this.faultTolerantTimer = faultTolerantTimer;
	}*/

}
