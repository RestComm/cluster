Simple app which shows Mobients Cluster timers running in AS7.

DEPLOY:
 Simply run "mvn clean install". Once maven is done duplicate $JBOSS_HOME dir to $JBOSS_HOME_, edit $JBOSS_HOME_/standalone/standalone.xml to bind to different ports or ip (for instance 127.0.0.2) 
 
RUN SERVERS
Start one terminal/bash, "cd" to $JBOSS_HOME, then "unset JBOSS_HOME"
Start server with command "./bin/standalone.sh"

RUN SERVER 2
Start one terminal/bash, "cd" to $JBOSS_HOME_, then "unset JBOSS_HOME"
Start server with command "./bin/standalone.sh"

Now open jconsole
Go to org.mobicents:name=FaultTolerantTimerServiceExample bean, schedule timer, wait to execute. Now set timer again, with bigger delay, so node which starts it, can shutdown before 
timer fires. Once timer is set, kill/shutdown server on which the timer started. Wait for it to fire on second server. 
 