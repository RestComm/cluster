Simple app which shows how to use Mobients Cluster timers.

PRE-DEPLOY:
 Make two different jboss standalone (just make two different folders with the AS, after the copy delete data dir in each node)
 Copy the infinispan tree (v.5.2.10.Final-redhat-1) jar under the given module and set the module.xml

DEPLOY:
 Simply run "mvn clean install -P jboss-eap-6.4-example". Then copy the war to standalone/deployments (on each node)
 
RUN
Start two terminals, "cd" to each jboss home

Server1: bin/run.sh -c standalone-ha.xml
Server2: bin/run.sh -c standalone-ha.xml -Djboss.socket.binding.port-offset=100 -Djboss.node.name=node2

Open the deployed war on each node:

http://localhost:8080/example-war-1.15.0-SNAPSHOT/
http://localhost:8180/example-war-1.15.0-SNAPSHOT/

Check that eth is ok!

Use jconsole to manage MXBean on each node(https://developer.jboss.org/wiki/UsingJconsoleToConnectToJMXOnAS7)

Test1:
Go to Cluster bean, schedule timer, wait to execute. Now set timer again, with bigger delay, so node which starts it, can shutdown before 
timer fires. Once timer is set, kill server on which it started. Wait for it to fire on second server. 

Test2:
Go to Cluster bean, schedule timer with higher delay (like 40s), then based on the UUID (see the logs) remove the cache entry on the other node and check that the local task will remove from the first node as well (won't fire).
 