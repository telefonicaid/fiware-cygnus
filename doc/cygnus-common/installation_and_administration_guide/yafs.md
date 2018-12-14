# Yet Another Flume Supervisor (YAFS)
By default, any Cygnus agent regularly checks for dead threads, and if any was found, Cygnus is stopped. The purpose of this mechanism is to start an ordered dead of Cygnus itself when an issue occurs to any of its threads.

This is achieved thanks to a special thread run by `com.telefonica.iot.cygnus.nodes.CygnusApplication` which is called Yet Another Flume Supervisor (YAFS). Once YAFS detects a dead thread, it forces Cygnus to exit. Then, because a system hook is configured to catch Cygnus exists, all agent components are stoped.

Nevertheless, using YAFS has side effects, specially when a thread dies as part of the normal life cycle of Flume's threads. This occurs, for instance, if using a native sink that starts several parallel threads when persisting data and any of them find the persistence backend is not reachable; then, such a thread stops running.

Of special interest is the case involving a configuration change on the fly. Flume is designed for that purpose, nevertheless using YAFS produce very weird results in conjunction with on the fly configuration changes. This is because reloading a configuration in Flume implies to stop previous running components, i.e. thread stops that YAFS detects and result in a complete Cygnus stop.

In order to avoid scenarios as the ones described above, YAFS can be disabled when configuring the agent, by simply using the `--no-yafs` option. Please check the sections about running a Cygnus agent as a [process](./runninh_as_process.md) or as a [service](./running_as_service.md) for further details.

**NOTE**: Please observe Flume 1.4.0 shows a problem when changing the configuration on the fly, as described in [FLUME-2310](https://issues.apache.org/jira/browse/FLUME-2310). Basically, new components cannot register for JMX monitoring because old ones are not properly deregistered. This problem is inherited by Cygnus since it extends Flume. In any case, it is a minor issue not affecting Flume nor Cygnus functionalityu (except for proper JMX monitoring, of course). You will see this error in Cygnus in traces like this one:

```
time=2017-04-26T15:09:09.805UTC | lvl=INFO | corr=N/A | trans=N/A | srv=N/A | subsrv=N/A | comp=cygnus-ngsi | op=run | msg=org.apache.flume.node.PollingPropertiesFileConfigurationProvider$FileWatcherRunnable[133] : Reloading configuration file:/opt/apache-flume-1.4.0-bin/conf/agent_sth.conf
...
...
time=2017-04-26T15:09:10.377UTC | lvl=ERROR | corr=N/A | trans=N/A | srv=N/A | subsrv=N/A | comp=cygnus-ngsi | op=register | msg=org.apache.flume.instrumentation.MonitoredCounterGroup[113] : Failed to register monitored counter group for type: SOURCE, name: http-source
javax.management.InstanceAlreadyExistsException: org.apache.flume.source:type=http-source
	at com.sun.jmx.mbeanserver.Repository.addMBean(Repository.java:437)
	at com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.registerWithRepository(DefaultMBeanServerInterceptor.java:1898)
	at com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.registerDynamicMBean(DefaultMBeanServerInterceptor.java:966)
	at com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.registerObject(DefaultMBeanServerInterceptor.java:900)
	at com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.registerMBean(DefaultMBeanServerInterceptor.java:324)
	at com.sun.jmx.mbeanserver.JmxMBeanServer.registerMBean(JmxMBeanServer.java:522)
	at org.apache.flume.instrumentation.MonitoredCounterGroup.register(MonitoredCounterGroup.java:108)
	at org.apache.flume.instrumentation.MonitoredCounterGroup.start(MonitoredCounterGroup.java:88)
	at org.apache.flume.source.http.HTTPSource.start(HTTPSource.java:158)
	at org.apache.flume.source.EventDrivenSourceRunner.start(EventDrivenSourceRunner.java:44)
	at org.apache.flume.lifecycle.LifecycleSupervisor$MonitorRunnable.run(LifecycleSupervisor.java:251)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
	at java.util.concurrent.FutureTask.runAndReset(FutureTask.java:308)
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$301(ScheduledThreadPoolExecutor.java:180)
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:294)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at java.lang.Thread.run(Thread.java:745)
```
