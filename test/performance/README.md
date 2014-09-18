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

* Launch "ServerAgent" in Cygnus in Balancer and each Cygnus Node 
```
nohup sh startAgent.sh --udp-port 0 --tcp-port 3450 > monitor.log &
```

#### Scripts:

**soakTest_cygnus_v1.0.jmx**: implemented for soak test. Soak testing involves testing a system with a significant load extended over a significant period of time, to discover how the system behaves under sustained use:

 > **Scenario (Random requests)**:
```
		* notification simple - xml
		* notification simple - json
		* notification compound - xml
		* notification compound - json
		* notification metadata - xml
		* notification metadata - json 
```	
  >**Properties**:
``` 
		* TEST_TIME    - test duration time (60 Sec by default)
		* THREADS      - number of concurrent threads (1 by default)		
		* HOST         - IP or hostname (in case of clusters is Nginx)  (127.0.0.1 by default)		
		* TPS          - transactions per seconds maximum (10 by default) 
		* ORGANIZATION - organization name (soaktest by default)
``` 	

>**example**:
```
<jmeter_path>/jmeter.sh -n -t ~/test/scripts/jmeter/cygnus/soakTest_cygnus_v1.0.jmx -JHOST=X.X.X.X -JTHREADS=5 -JTEST_TIME=86400 -JTPS=40 -JORGANIZATION=soaktest > <log_path>/soaktest_cygnus_`date +%FT%T`.log &
```

**HA_cygnus_v1.0.jmx**: implemented for High Availability. High availability refers to a system or component that is continuously operational for a desirably long length of time.

 > **Scenario **:
```
		* N notifications with un delay 
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
<jmeter_path>/jmeter.sh -n -t ~/test/scripts/jmeter/cygnus/HA_cygnus_v1.0.jmx -JHOST=X.X.X.X -JITERATIONS=10 -JDELAY=500 -JORGANIZATION=soaktest > <log_path>/soaktest_cygnus_`date +%FT%T`.log &
```