## Performance Tests

This is the repository for scripts, used for Performance tests.

#### Pre-conditions:

* "Jmeter" app exists in Launcher VM
* "ServerAgent" app exist in Launcher , in Cygnus Nodes and Balancer (only in cluster case) 
* have a account in Loadosophia - (http://loadosophia.org)
* "nginx" app exists in Balancer VM (only in cluster case)
* Verify nginx configuration for each scenario (only in cluster case)
* Verify that CKAN, MySQL, and HADOOP is installed correctly	
	
#### Pre-steps:

* Launch "ServerAgent" in Balancer and each Cygnus Node VMs
```
nohup sh startAgent.sh --udp-port 0 --tcp-port 4444 &
```
 ramp up  of  threads
#### Scripts:

**HA_cygnus_v1.0.jmx**: implemented for High Availability. High availability refers to a system or component that is continuously operational for a desirably long length of time.

 > **Scenario **:
```
		* N notifications with a delay 
			Requests consecutive:
				* notification simple - xml
				* notification simple - json
				* notification compound - xml
				* notification compound - json
				* notification metadata - xml		
				* notification metadata - json
```	
  >**Properties**:
``` 
		* ITERATIONS   - numbers of notifications (20 Sec by default)
		* DELAY        - delay between notifications (1 sec by default)		
		* HOST         - IP or hostname (in case of clusters is Nginx)  (127.0.0.1 by default)				
		* ORGANIZATION - organization name (HA by default)
``` 	

>**example**:
```
<jmeter_path>/jmeter.sh -n -t ~/test/scripts/jmeter/cygnus/HA_cygnus_v1.0.jmx -JHOST=X.X.X.X -JITERATIONS=20 -JDELAY=1000 -JORGANIZATION=ha > <log_path>/soaktest_cygnus_`date +%FT%T`.log &
```

**loadTest_cygnus_v1.0.jmx**: Load testing is the process of putting demand on a system and measuring its response. Load testing is performed to determine a system’s behavior under anticipated peak load conditions

 > **Scenario **:
```
		* N threads in the same time
```	
  >**Properties**:
``` 
		* RUNTIME      - time of test duration (60 Sec by default)
		* THREADS      - threads number (1 by default)	
		* RAMPUP       -  ramp up of threads (0 by default)         		
		* HOST         - IP or hostname main node(in case of clusters is Nginx)  (127.0.0.1 by default)		
		* PORT         - port used by cygnus (5050 by default)
		* HOST_NODE_1  - IP or hostname of Node 1  (127.0.0.1 by default, if the property is not appends it is ignored) 
		* HOST_NODE_2  - IP or hostname of Node 2  (127.0.0.1 by default, if the property is not appends it is ignored) 
		* TESTNAME    - sink name (channel to test)
		* ORGANIZATION - organization name (HA by default)
``` 	

>**example**:
```
<jmeter_path>/jmeter.sh -n -t ~/test/scripts/jmeter/cygnus/loadTest_cygnus_v1.0.jmx -JHOST=X.X.X.X -JPORT=5050 -JTHREADS=20 -JRAMPUP=10 -JRUNTIME=180 -JTESTNAME=CKAN -JORGANIZATION=loadtest > <log_path>/soaktest_cygnus_`date +%FT%T`.log &
```