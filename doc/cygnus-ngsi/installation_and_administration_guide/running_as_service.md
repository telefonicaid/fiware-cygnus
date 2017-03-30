# Running cygnus-ngsi as a service
**Note**: Cygnus can only be run as a service if you installed it through the RPM.

Once the `cygnus_instance_<id>.conf` and `agent_<id>.conf` files are properly configured, just use the `service` command to start, restart, stop or get the status (as a sudoer):

    $ sudo service cygnus status

    $ sudo service cygnus start

    $ sudo service cygnus restart

    $ sudo service cygnus stop

Previous commands affects to **all** of Cygnus instances configured. If only one instance is wanted to be managed by the service script then the instance identifier after the action must be specified:

    $ sudo service cygnus status <id>

    $ sudo service cygnus start <id>

    $ sudo service cygnus restart <id>

    $ sudo service cygnus stop <id>

Where `<id>` is the suffix at the end of the `cygnus_instace_<id>.conf` or `agent_<id>.conf` files you used to configure the instance.
