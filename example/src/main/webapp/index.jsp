 <%@page import="org.restcomm.cluster.test.InitMBean"%>
<%@page import="org.restcomm.cluster.test.MCClusterTest"%>
<%@page import="org.restcomm.cluster.test.MCClusterTestMBean"%>
<%@page import="org.restcomm.timers.timer.FaultTolerantTimer"%>
<%@page import="javax.transaction.TransactionManager"%>
<%@page import="org.restcomm.cluster.DefaultMobicentsCluster"%>
<%@page import="org.restcomm.cluster.election.DefaultClusterElector"%>
<%@page import="org.infinispan.manager.CacheContainer"%>
<%@page import="javax.naming.InitialContext"%>
<%@page import="org.restcomm.cache.MobicentsCache"%>
<%@page import="org.infinispan.manager.DefaultCacheManager"%>
<%@page import="org.infinispan.configuration.cache.ConfigurationBuilder"%>
<%@page import="org.infinispan.configuration.cache.Configuration"%>
<%@ page import="javax.management.*,javax.management.remote.*" %>
 
 
  <%
     MBeanServer mbeanServer = java.lang.management.ManagementFactory.getPlatformMBeanServer(); 
     System.out.println("Got the MBeanServer.... "+mbeanServer);
 
     
      
     MCClusterTestMBean mBean = new MCClusterTest();
     
     InitMBean tb = (InitMBean) new InitialContext().lookup("java:app/example-war-3.0.0-SNAPSHOT/org.restcomm.cluster.test.InitMBean"); 
    		 
     
     
     ObjectName objectName=new ObjectName("test.startup:service=ClusterTest");
     
     
     mBean.setFaultTolerantTimer(tb.getTimer());
     mBean.setJta(tb.getTManager());
     
     mbeanServer.registerMBean(mBean, objectName);
     
     
     System.out.println("MBean Registered with ObjectName:  "+objectName);
  %>
 
<h1> Check the JBossAS7 Console to see if Your MXBean!!  is registered properly or not.   You can also use the "$JBOSS_HOME/bin/jconsole.sh" script as well to start the JConsole and to see if the MXBean is accessible through it or not </h1>