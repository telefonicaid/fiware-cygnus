## Performance Tests

This is the repository for scripts, used for Performance tests.

#### Pre-conditions:

* "Jmeter" app exists in Launcher VM
* "ServerAgent" app exists in Cygnus Nodes and Balancer (only in cluster case)
* have a account in Loadosophia - (http://loadosophia.org)
* "nginx" app exists in Balancer VM (only in cluster case)
* Verify nginx configuration for each scenario (only in cluster case)
* Verify that the mock exists and it is installed correctly

#### Pre-steps:

* Launch "ServerAgent" in Balancer and each Cygnus Node VMs
```
nohup sh startAgent.sh --udp-port 0 --tcp-port 4444 &
```

#### Scripts:

**loadTest_cygnus_mock_v1.0.jmx**:
    Load testing is the process of putting demand on a system and measuring its response. Load testing is performed to determine a system's behavior under anticipated peak load conditions

  >**Scenario**:
```
* N threads at the same time against a specific sink
```
  >**Steps**:
```
 start the mock in specific port
- cygnus configuration (sink, mock host, mock port, etc)
- restart cygnus service
- launch jmeter script
- reports path (/tmp/JMeter_result/<TESTNAME>_result_<date>)
```
  >**Properties**:
```
* RUNTIME      - time of test duration (1 second by default)
* THREADS      - threads number (1 by default)
* RAMPUP       - ramp up of threads (0 by default)
* HOST         - IP or hostname main node (in case of clusters is Nginx)  (127.0.0.1 by default)
* PORT         - port used by cygnus (5050 by default)
* TESTNAME     - sink name (channel to test)
* SERVICE      - FIWARE service to be notified (loadtest by default)
* SERVICE_PATH - FIWARE service path to be notified (/any by default)
```

  >**example**:
```
<jmeter_path>/jmeter.sh -n -t <path>/loadTest_cygnus_v1.0.jmx -JHOST=X.X.X.X -JPORT=5050 -JTHREADS=20 -JRAMPUP=10 -JRUNTIME=180 -JTESTNAME=CKAN -JSERVICE=loadtest -JSERVICE_PATH=/any > <log_path>/soaktest_cygnus_`date +%FT%T`.log &
```

#### Post-steps:
  * Upload in Loadosophia web Loadosophia_xxxxxxxxxxxxxxxxxxxxx.jtl.gz and perfmon_xxxxxxxxxxxxxxxxxxxx.jtl.gz (where "xxxxxxxxxxxxxxxxxxx" is a hash value).
  * Create Final Report (recommend google docs)

```
Comments:
    /tmp/error_xxxxxxxxxxxxxxxxxxx.html is created, because does not have access at loadosophia, the token is wrong intentionally
    This is made to not constantly access and penalizes the test times. We only store dates manually when finished test. So "xxxxxxxxxxxxxxxxxxx" is a hash value.
```
