# Introduction #

This tutorial was created to demonstrate SAMM's automatic management capabilities. It will guide You through installation, configuration and sample monitoring and management session.

# Installation and configuration #

Installation of tools used in this tutorial is straight-forward. Just download the SAMM release and the sample application and unpack SAMM archive file.

```
mkdir samm
tar zxvf samm-0.1.0-bin.tar.gz -C samm
```

Configuration file required to be able to monitor the sample tutorial application is already prepared and available in file `config_tutorial.xml`

# Monitoring and management session #

**Notice: the sample workload application uses randomly created expressions - thus the integration time can be very different depending on what actually is processed.**

1. Start test application in first terminal window

```
java -jar testApp-1.2.jar
```

By default application exposes JMX monitoring facilities on port 33333. The HTTP server will be listening for connections on port 8080.

2. Verify test application is running - open `localhost:8080` in web browser and observe the initial metric values. You can also check JMX connectivity by using JVisualVM or JConsole tools (the connection is insecure, ie. the password is not set and an insecure communication protocol is used).

3. Estimate the number of expressions which can be handled by a single slave (in our case it was around 2500 per minute). _Remember about stopping/starting workload after changing the number of expressions to generate per minute._

4. Start workload with a number of expressions set to a value slightly higher than the value estimated - observe how the number of processed expressions slowly increases.

5. Stop workload generation - wait for the number of waiting expressions fall down to 0.

6. Start SAMM

```
./start.sh config_tutorial.xml
```

7. Verify that the tutorial application is actually being monitored: wait for metric values to appear on console.

_Samples:_

_`22268 [pool-2-thread-1] DEBUG pl.edu.agh.samm.metrics.MetricTask  - Metric: http://www.icsr.agh.edu.pl/samm_1.owl#ExpressionsQueueLengthMetric resource: /TutorialApp value: 0`_

_`22299 [pool-2-thread-3] DEBUG pl.edu.agh.samm.metrics.MetricTask  - Metric: http://www.icsr.agh.edu.pl/samm_1.owl#SlavesCountMetric resource: /TutorialApp value: 1`_

8. Start workload generation with a number of generated expressions higher than a single slave could handle - observe how SAMM adds new slaves and removes them if they are not used anymore.

![https://sammanager.googlecode.com/git/wiki/samm_slaves_up_down.png](https://sammanager.googlecode.com/git/wiki/samm_slaves_up_down.png)

9. Experiment with generating higher amount of expressions - watch how SAMM increases the number of slaves and with metric monitoring time intervals. **Notice: at some point the CPU will get overloaded - adding more slaves won't improve the number of expressions processed; it may even cause the actual throughput of the system to decrease. Shortening the monitoring time interval allows SAMM to take actions faster - however it will increase the CPU cost of monitoring and decrease the overall system throughput**

10. Create a rule which would force SAMM to add a slave in case the number of slaves drops to 0. Follow steps from below:

  1. Copy file `config_tutorial.xml` as `config_tutorial_2.xml` and open it in a text editor.
  1. In `<ruleSet>` element create a new `<rule>` with attribute **`name="myNewCustomRule"`**
  1. Specify the resource which is going to be monitored (the `resourceUri` element)
  1. Copy the `<actionToExecute>` element from **`ruleForAddingASlave`** - it contains the URI of action we want to execute (in this case - `#ExecuteMBeanAction`) and required parameters (in this case - JMX connection URL, MBean name and name of method to invoke)
  1. Specify the URI of metric, which is going to be observed. Because the objective is to have at least one slave if only the system is running, we want to observe the number of slaves: `http://www.icsr.agh.edu.pl/samm_1.owl#SlavesCountMetric`
  1. Specify the conditions which have to be met to execute the action - in our case metric value has to be equal to 0: `cast(value,double) = 0`

**Notice: SAMM uses the [Esper](http://esper.codehaus.org/) engine to process monitoring events stream - the condition itself is written in Event Processing Language (EPL) and is a part of a statement in this language. In fact You can write Your own statements in EPL and pass them in `<customStatement>` (see `ruleForRemovingASlave` in `config_tutorial.xml`)**

You should end up with following XML snippet:

```
        <rule name="myNewCustomRule">
            <resourceUri>%TutorialApp%</resourceUri>
            <condition>cast(value,double) = 0</condition>
            <metricUri>http://www.icsr.agh.edu.pl/samm_1.owl#SlavesCountMetric</metricUri>
            <actionToExecute>
                <actionURI>http://www.icsr.agh.edu.pl/samm_1.owl#ExecuteMBeanAction</actionURI>
                <parameterValues>
                    <entry>
                        <string>MBEAN_JMXURL</string>
                        <string>service:jmx:rmi:///jndi/rmi://localhost:33333/jmxrmi</string>
                    </entry>
                    <entry>
                        <string>MBEAN_URI</string>
                        <string>pl.edu.agh.samm.testapp:name=WorkloadGenerator</string>
                    </entry>
                    <entry>
                        <string>MBEAN_METHOD_NAME</string>
                        <string>addSlave</string>
                    </entry>
                </parameterValues>
            </actionToExecute>
        </rule>
```

11. Start SAMM again - with new configuration file - and try to remove initially created slave. Don't start the generation of workload - it would cause the rule observing expressions queue to trigger.

Sample chart showing when SAMM takes action (green circles mark removing slaves, red - adding slaves with use of the `myNewCustomRule`):

![https://sammanager.googlecode.com/git/wiki/custom_rule_effect.png](https://sammanager.googlecode.com/git/wiki/custom_rule_effect.png)