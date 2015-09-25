# How to use this Dockerfile

The process of building this application and packing it into a container, could be a little long in time terms. Addressing this issue, the process is separated in two steps: Building the jar and packing the cygnus container.

## Run the two steps at once

(TODO)

## Build the jar

Run this command from the root of this repository

	docker-compose -f ./docker/0.compose.jar-compiler.yml -p cygnus run --rm compiler

This will create an image containing maven, then will call a script which will mount this directory, retrieve the maven dependencies and start the compilation of the jar into the `target` directory.

NOTE: Be careful not to have any symlinks in your route, as there is a weird bug that does not permit you to build your image (`unable to prepare context: The Dockerfile (/Users/mikehappy/code/fiware-cygnus/docker/Dockerfile.test) must be within the build context (.)`).

In the case mentioned above, make sure that your _pwd_ does not contain any symlink.

Links to the bug mentioned above

* [Link 1](https://github.com/docker/docker/issues/14339)
* [Link 2](https://github.com/docker/docker/issues/15642)

### OK. Where is my jar?

Look into the `maven-deps` directory. Don't worry, thought the container disappears, you keep the work done.

## Pack the cygnus container

OK. So we got our _jar_. Let's pack it into a container.

We build the image with this command

	docker build -f ./docker/1.Dockerfile.cygnus-container -t tidchile/fiware-cygnus .

To execute the container use this command

	docker-compose -f ./docker/1.compose.cygnus-container.yml

Be advised that you need to specify in the docker compose file the link to the database you are using. And have the right configuration files in the `conf` directory, Refer to the documentation.

