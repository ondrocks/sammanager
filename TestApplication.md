# Download #

[link](https://sammanager.googlecode.com/git/wiki/testApp-1.1.jar)

# Description #

The test application is a simple service which generates arithmetic expressions and later tries to compute their integrals (with use of [Apache Commons Math](http://commons.apache.org/proper/commons-math/) library). It has following components:

  * _expressions generator_ - generates arithmetic expressions; the number of expressions generated per minute can be changed dynamically with use of web interface
  * _tasks dispatcher_ - assigns expressions (_tasks_) to particular slaves; if it is not possible to assign the expression to a slave (i.e. all slaves are currently busy) it is queued and waits for resources
  * _slaves pool_ - pool of threads which perform actual computations

The application can be controled and monitored via a web interface (by default it is available on port 8080). Sample URL:

```
http://localhost:8080
```

Starting the application:

```
java -jar testApp-1.0.jar
```

To specify the port number on which the web interface should be available please add a JVM parameter `pl.edu.agh.samm.testapp.httpport` in the commandline:

```
java -Dpl.edu.agh.samm.testapp.httpport=8081 -jar testApp-1.0.jar 
```

To start/stop computations use the "Start workload"/"Stop workload" button. After changing the number of generated expressions, You need to stop/start generation mechanism again. The web interface contains also command log which shows the information about executed actions. It is also possible to manually adjust the number of slaves which integrate expressions. To do that use the panel with "Add slave" and "Remove slave" buttons. Below the slaves control buttons there are three charts which show:
  * the number of slaves
  * the length of queue of expressions waiting for computations
  * the total number of processed expressions

![https://sammanager.googlecode.com/git/wiki/testapp_screenshot.png](https://sammanager.googlecode.com/git/wiki/testapp_screenshot.png)

## Monitoring through JMX ##

Application automatically exposes JMX monitoring interface on port 33333 and awaits connection for localhost. These settings can be adjusted by using `pl.edu.agh.samm.testapp.hostname` and `pl.edu.agh.samm.testapp.jmxport` JVM parameters.

Example:

```
java -Dpl.edu.agh.samm.testapp.hostname=myhostname.com -Dpl.edu.agh.samm.testapp.jmxport=12345 -jar testApp-1.0.jar
```

## Building ##

```
$ git clone https://code.google.com/p/sammanager/
$ cd sammanager/test/testApp/
$ mvn clean package assembly:assembly
```