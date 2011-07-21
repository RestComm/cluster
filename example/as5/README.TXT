Simple app which shows Mobients Cluster timers running in AS5.

DEPLOY:
 Simply run "mvn clean install". Once maven is done duplicate $JBOSS_HOME dir to $JBOSS_HOME_ 
 
RUN SERVER 1
Start one terminal/bash, "cd" to $JBOSS_HOME, then "unset JBOSS_HOME"
Start server with command "./bin/run.sh -c all -Djboss.messaging.ServerPeerID=1 -Djboss.service.binding.set=ports-default"

RUN SERVER 2
Start one terminal/bash, "cd" to $JBOSS_HOME_, then "unset JBOSS_HOME"
Start server with command "./bin/run.sh -c all -Djboss.messaging.ServerPeerID=2 -Djboss.service.binding.set=ports-01"

Now access on of JMX consoles: http://127.0.0.1:8080/jmx-console or http://127.0.0.1:8180/jmx-console
Go to org.mobicents:name=FaultTolerantTimerServiceExample bean, schedule timer, wait to execute. Now set timer again, with bigger delay, so node which starts it, can shutdown before 
timer fires. Once timer is set, kill/shutdown server on which the timer started. Wait for it to fire on second server. 
 