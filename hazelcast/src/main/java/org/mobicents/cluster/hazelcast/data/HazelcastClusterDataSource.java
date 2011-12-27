package org.mobicents.cluster.hazelcast.data;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.mobicents.cluster.data.ClusterData;
import org.mobicents.cluster.data.ClusterDataKey;
import org.mobicents.cluster.data.ClusterDataSource;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;

public class HazelcastClusterDataSource implements ClusterDataSource<HazelcastInstance>{

	private final static String MAP_NAME  = "m";
	private final static String MMAP_NAME  = "mm";
	
	private HazelcastInstance hazelcastInstance;
	private final TransactionManager txManager;
	private ConnectionFactory connectionFactory;
	
	public HazelcastClusterDataSource(TransactionManager txManager) {
		this.txManager = txManager;
	}
	
	@Override
	public HazelcastInstance getWrappedDataSource() {
		return hazelcastInstance;
	}

	@Override
	public ClusterData getClusterData(ClusterDataKey key) {
		return new HazelcastClusterData(key, this);
	}
	
	private ConnectionFactory getConnectionFactory() throws NamingException {
		if (connectionFactory == null) {
			Context context = new InitialContext();
			connectionFactory = (ConnectionFactory) context.lookup ("java:HazelcastCF");
		}
		return connectionFactory;
	}
	
	private Map<Transaction, Connection> getThreadLocals() {
		Map<Transaction, Connection> threadLocals = HazelcastThreadLocals.getTxConnections();
		if (threadLocals == null) {
			threadLocals = new HashMap<Transaction, Connection>();
		}
		return threadLocals;
	}
	
	private void bindHazelcastToJTA() {
		if (txManager != null) {
			try {
				final Transaction tx = txManager.getTransaction();
				if (tx != null && (tx.getStatus() == Status.STATUS_ACTIVE || tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)) {
					final Map<Transaction, Connection> threadLocals = getThreadLocals();
					if (!threadLocals.containsKey(tx)) {
						ConnectionFactory cf = getConnectionFactory();
						final Connection c = cf.getConnection();
						threadLocals.put(tx, c);
						HazelcastThreadLocals.setTxConnections(threadLocals);
						Synchronization synchronization = new Synchronization() {
							@Override
							public void beforeCompletion() {
								
							}
							@Override
							public void afterCompletion(int status) {								
								final Connection c = threadLocals.remove(tx);
								if (c != null) {
									try {
										c.close();
									} catch (ResourceException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						};
						tx.registerSynchronization(synchronization);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public <K,V> IMap<K,V> getMap() {
		bindHazelcastToJTA();
		return hazelcastInstance.getMap(MAP_NAME);
	}
	
	public <K,V> MultiMap<K,V> getMultiMap() {
		bindHazelcastToJTA();
		return hazelcastInstance.getMultiMap(MMAP_NAME);
	}
	
	@Override
	public void init() {
		hazelcastInstance = Hazelcast.getDefaultInstance();
		hazelcastInstance.getConfig().setProperty("hazelcast.shutdownhook.enabled","false");		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void shutdown() {
		hazelcastInstance.shutdown(); 
	}
}
