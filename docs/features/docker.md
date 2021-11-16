# Docker containers

Docker containers allow anyone to easily reproduce our experiments without any prior knowledge of how our apps works. The container contains all the instances, code, and libraries necessary to easily execute the experiments with a single command.

## Before starting

1. **Install Docker**: installation instructions are available at
[https://docs.docker.com/engine/install/](https://docs.docker.com/engine/install/).
2. **Verify that Docker is working**: execute `docker run hello-world` and check the output.
3. Before publishing any container, you need to **create an account in a Docker Registry**. If you do not know what a Docker Registry is, create an account in [Docker Hub Registry](https://hub.docker.com/).

## Building and publishing your container

Inside the `docker` folder, there are several scripts to help with the container lifecycle. We will use them, and modify them if necessary, in order to build, run and publish our container.

### Building the container

Execute `docker/build.sh username/projectname` to build and tag a Docker container with your name and the project name.
Both the projectname and the username should be in lowercase.
The build process is consists of two steps: first, a [fat jar](https://dzone.com/articles/the-skinny-on-fat-thin-hollow-and-uber) is created using `maven`, and then a container is built using the steps in `docker/Dockerfile`.
The container build steps can be changed to include additional files, dependencies, directories or custom Java VM parameters.

### Testing the container
Once the container is built, and before publishing it, you should test that it correctly works and generates the expected results.
Use `docker/run.sh username/projectname` to run the container and verify that the results inside the `results` folder are correct.

### Publishing your container

Publishing your container is extremely simple, execute `docker/publish.sh username/projectname` and follow the steps.
The first time, the script will ask for the Docker Hub account credentials.

For a more detailed step by step guide check the official Docker documentation [https://docs.docker.com/docker-hub/repos/#pushing-a-docker-container-image-to-docker-hub](https://docs.docker.com/docker-hub/repos/#pushing-a-docker-container-image-to-docker-hub).

## Common questions

### How can people reproduce my experiments?

Once the container is published, anyone can run it using the following command:
```bash
docker run -t username/projectname
```
The only requisite is that they must have Docker installed.

### Can I change the configuration after publishing the container?
Yes, you may provide a different application.yml or custom properties.
For a more detailed explanation on how or why the following works, see [the docs config page](config.md).

#### Using custom properties
Configuration values can be changed via environment variables. For example, changing the solver random seed is as easy as:

```bash
docker run -e "SOLVER_SEED=1234" -t username/projectname
```


#### Using a different application.yml
It some cases, it may be more confortable to provide a configuration file instead of manually configuring properties.
```bash
docker run -v "$(pwd)"/application.yml:/application.yml -t username/projectname
```

Remember that any property defined in the new `application.yml` overrides the corresponding property in the embedded `application.yml`. If a property is not defined, the embedded value is used.

### Can the container solve a different set of instances?

Absolutely, you can mount a directory inside the container containing the new set of instances to solve.
```bash
docker run -v "$(pwd)"/newInstances:/newInstances -e "INSTANCES_PATH_DEFAULT=newInstances" -t username/projectname
```
