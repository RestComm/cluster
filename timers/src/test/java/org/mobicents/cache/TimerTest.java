package org.mobicents.cache;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jboss.cache.Cache;
import org.jboss.cache.TreeCacheViewMBean;
import org.mobicents.cluster.DefaultMobicentsCluster;
import org.mobicents.cluster.election.DefaultClusterElector;
import org.mobicents.timers.FaultTolerantScheduler;
import org.mobicents.timers.PeriodicScheduleStrategy;
import org.mobicents.timers.TimerTaskData;
import org.mobicents.timers.timer.FaultTolerantTimer;
import org.mobicents.timers.timer.FaultTolerantTimerTimerTaskFactory;

import junit.framework.TestCase;

public class TimerTest extends TestCase{
	private DefaultMobicentsCluster startNode(String name) {
		Cache myCache = org.jboss.cache.DefaultCacheFactory.getInstance().createCache(
				TimerTest.class.getClassLoader().getResourceAsStream(
						"config-samples/total-replication.xml"));
		MobicentsCache cache = new MobicentsCache(myCache, name, false);
		cache.startCache();
		DefaultMobicentsCluster cluster = new DefaultMobicentsCluster(cache, null, new DefaultClusterElector());
		return cluster;
	}
	
	public void timerTestWithoutScheduler2nodes1timer() throws Exception {
		DefaultMobicentsCluster c1 = startNode("1");
		DefaultMobicentsCluster c2 = startNode("2");
		FaultTolerantTimer t1 = new FaultTolerantTimer("task", c1, (byte) 1, null);
		FaultTolerantTimer t2 = new FaultTolerantTimer("task", c2, (byte) 1, null);
		t1.schedule(new MyTimerTask(), 0, 1000);
		Thread.sleep(2000);
		long count = timerCallback;
		
		// expect at 1 or 2 ticks here
		assertTrue(count>0);
		
		// kill the owner
		c1.getMobicentsCache().stop();
		
		// wait for another 1 or 2 ticks
		Thread.sleep(2000);
		
		// fail if there are none
		assertTrue(timerCallback>count);
		count = timerCallback;
		
		// kill the last node, no nodes are alive now
		c2.getMobicentsCache().stop();
		Thread.sleep(2000);
		
		// expect no additional ticks
		assertEquals(count, timerCallback);
	}
	
	private static class TestTimerTaskFactory implements
	org.mobicents.timers.TimerTaskFactory {
	               public org.mobicents.timers.TimerTask newTimerTask(TimerTaskData data) {
	                       return new org.mobicents.timers.TimerTask(data) {
	                               @Override
	                               public void runTask() {
	                                       timerCallback++;
	                               }
	                       };
	               }
	       }
	
	public void timerTestScheduler2nodes1timer() throws Exception {
		DefaultMobicentsCluster c1 = startNode("1");
		DefaultMobicentsCluster c2 = startNode("2");
		TestTimerTaskFactory factory1 = new TestTimerTaskFactory();
		TestTimerTaskFactory factory2 = new TestTimerTaskFactory();
		FaultTolerantScheduler scheduler1 = new FaultTolerantScheduler("d", 11, c1, (byte) 1, null, factory1);
		FaultTolerantScheduler scheduler2 = new FaultTolerantScheduler("d", 11, c2, (byte) 1, null, factory2);
		
		scheduler1.schedule(new org.mobicents.timers.TimerTask(new TimerTaskData(UUID.randomUUID().toString(),
		 System.currentTimeMillis()+1000, 1000, PeriodicScheduleStrategy.withFixedDelay)) {
			
			@Override
			public void runTask() {
				timerCallback++;
				
			}
		});
		Thread.sleep(2000);
		long count = timerCallback;
		
		// expect at 1 or 2 ticks here
		assertTrue(count>0);
		
		// kill the owner
		c1.getMobicentsCache().stop();
		
		// wait for another 1 or 2 ticks
		Thread.sleep(2000);
		
		// fail if there are none
		assertTrue(timerCallback>count);
		count = timerCallback;
		
		// kill the last node, no nodes are alive now
		c2.getMobicentsCache().stop();
		Thread.sleep(2000);
		
		// expect no additional ticks
		assertEquals(count, timerCallback);
	}
	
	public void sysclockChangeObservation1() throws Exception {
		ScheduledExecutorService s = Executors.newScheduledThreadPool(10);
		s.scheduleAtFixedRate(new Runnable() {
			
			public void run() {
				System.out.println(System.currentTimeMillis() - timerCallback);
				timerCallback = System.currentTimeMillis();
			}
		}, 1000, 1000, TimeUnit.MILLISECONDS);
		
		
		Thread.sleep(4000);
		s.shutdownNow();
	}
	
	public void sysclockChangeObservation2() throws Exception {
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			
			@Override
			public void run() {
				System.out.println(System.currentTimeMillis() - timerCallback);
				timerCallback = System.currentTimeMillis();
			}
		}, 1000, 1000);
		
		Thread.sleep(4000);
		t.cancel();
	}
	
	public void sysclockChangeObservation3() throws Exception {
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			
			@Override
			public void run() {
				System.out.println(System.nanoTime() - timerCallback);
				timerCallback = System.nanoTime();
			}
		}, 1000, 1000);
		
		Thread.sleep(400000);
		t.cancel();
	}
	
	
	
	private static long timerCallback = 0;
	
	private static class MyTimerTask extends TimerTask implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void run() {
			timerCallback++;
			
		}
	}
	
	protected void setUp(){}
	protected void tearDown(){}
}
