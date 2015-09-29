# Cygnus Containerization

The process of building this application and packing it into a container could be a little long in time terms. Addressing this issue, the process is separated in two steps: Building the jar and packing the cygnus container.

## Maven settings template

In order to improve your chances, should `maven` have any error downloading your dependencies, and you need to restart, or should you need to do this compilation process many times, `maven` is configured to put the downloaded dependencies in this repository's directory `maven-deps`, whis is `.gitignore`d. To accomplish that, we overwrite maven's settings file with our own `docker/0.maven-settings.xml`. The only change being the following line added:

    <localRepository>/cygnus-compiler/maven-deps</localRepository>

## Build the jar

Run this command from the root of this repository:

    docker-compose -f ./docker/0.compose.jar-compiler.yml -p cygnus run --rm compiler

This will create an image containing maven, then, call a script which will mount this directory into the container, retrieve the maven dependencies and start the compilation of the jar into the `target` directory.

NOTE: Be careful not to have any symlinks in your route, as there is a weird bug that does not permit you to build your image (`unable to prepare context: The Dockerfile (/Users/mikehappy/code/fiware-cygnus/docker/Dockerfile.test) must be within the build context (.)`).

In the case mentioned above, make sure that your _pwd_ does not contain any symlink.

Links to the bug mentioned above

* [Link 1](https://github.com/docker/docker/issues/14339)
* [Link 2](https://github.com/docker/docker/issues/15642)

### OK. Where is my jar?

Look into the `target` directory. Don't worry, though the container disappears, you keep the work done.

## Pack the cygnus container

OK. So we got our _jar_. And our configuration. Let's pack it into a container.

We build the image `fiware/cygnus` with this command:

    docker build -f ./docker/Dockerfile -t fiware/cygnus .

To execute the container use this command:

    docker-compose -f ./docker/docker-compose.yml up

It will mount your configuration files from `conf` and run the Cygnus application. There are no databases setup _a_priori_ in this systems, nor linked systems. Everything should be specified in this repository `conf` directory.

### Cygnus command

Docker compose will run the following command. Feel free to modify it on your required circumstances:

    flume/bin/cygnus-flume-ng agent --conf flume/conf -f flume/conf/agent_0.conf -n cygnusagent -Dflume.root.logger=INFO,console

## Contact

	Herman Junge
	herman.junge@telefonica.com
