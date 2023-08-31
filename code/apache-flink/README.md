# Flink Inventory Signals

This section covers the aspects related to the Proof Of Concept (PoC) to select the best alternative to OSA

## Setup

The Flink operations requires a Docker image, as well as public images for Flink, Kafka, and ZooKeeper. 

The `docker-compose.yaml` file of the project is located in the root directory.

```bash
cd flink-inventory-signals
```

### Building the custom Docker image

Build the Docker image by running

```bash
docker-compose build
```

### Preparing the Checkpoint and Savepoint Directories

Create the checkpoint and savepoint directories on the Docker host machine (these volumes are mounted by the jobmanager and taskmanager, as specified in docker-compose.yaml):

```bash
mkdir -p /tmp/flink-checkpoints-directory
mkdir -p /tmp/flink-savepoints-directory
```

### Starting the Project

Once you built the Docker image, run the following command to start the project

```bash
docker-compose up
```

Or

```bash
docker-compose up -d
```

You can check if the project was successfully started by accessing the WebUI of the Flink cluster at [http://localhost:8081](http://localhost:8081).

#### for Windows Users

If you get the error "Unhandled exception: Filesharing has been cancelled", you should configure file sharing in Docker Desktop before starting.
In Settings > Resources > File Sharing, add the flink-inventory-signals directory.

### Stopping the Project

To stop the project, run the following command

```bash
docker-compose down
```