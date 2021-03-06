# Streaming course final project assignment

Base project for a final assignment, contains:
  - local environment set up
  - deployment code for staging
  - scaffolding, interfaces and code snippets

# Environment

## Prerequisites
  - Python 2.7
  - Docker 1.11 or greater, docker-compose
  - JVM 1.8
  - sbt
  - AWS CLI tools
  - SSH client

## Build

To build all of the components - simply run `sbt docker` from the root project folder. This will compile code, build artifacts and push to the local docker repo.

## Deploy

### Local

To deploy local environment and start testing - simply run `docker-compose up` from the root project folder.

   On the first run, you will se topic not found errors in logs - thats because KAFKA_AUTO_CREATE_TOPICS_ENABLE is turned off - you should create topic with appropriate replication factor and number of partitions (see below), while not stopping running cluster.

Create the following topics:
```
docker run --net=host --rm confluentinc/cp-kafka:5.1.0 kafka-topics --create --topic twitter-topic --partitions 4 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
```

```
docker run --net=host --rm confluentinc/cp-kafka:5.1.0 kafka-topics --create --topic reddit-topic --partitions 4 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
```

```
docker run --net=host --rm confluentinc/cp-kafka:5.1.0 kafka-topics --create --topic test-topic-out --partitions 4 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
```
Then stop `docker-compose up` and run it again, you should see logs with messages from twitter and reddit.

### Staging

#### Configure

 - First of all change `STUDENT_NAME` environment variable in `.env` file to your identifier so you will not interfere with other student's deployments

 - Install [ecs-cli](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html)

 - Get user credentials with AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY from teacher.

 - Configure cli and login to ecr to be able to push images. You can simply use provided script:
   ```
   ./staging_configure.sh <AWS_ACCESS_KEY_ID> <AWS_SECRET_ACCESS_KEY>
   ```
   - On Windows, run `aws configure` first then proceed with `staging_configure.cmd`

###### **!!!** Do not share or commit provided credentials anywhere on the internet

#### Build and push docker images to ECR

   You will be publishing your built docker images to a shared ECR registry.
   Configurations provided to you here will tag images with *$STUDENT_NAME* by default - please, do not change this behaviour.
   You will not be able to fetch someone else's image to your machine.

   After building an image with `sbt docker` you can do `sbt dockerPush` to push. Or you can do both with `dockerBuildAndPush`

#### Deployment

   You're going to operate with three services separately:
   - `weather-provider` - generator of weather data
   - `solar-panel-emulator` - generator of sensor data
   - `streaming-app` - streaming application

   Use provided script with the similar to docker-compose style commands.
   You should specify as a first argument name of the service (same names as specified above) and then follows command like `up`, `ps`, `start`, `stop`, `down`, `scale 3`.

   On Windows you have same script available (.cmd)

   - to start your service:
   ```
   ./staging_compose.sh <name-of-the-service> up
   ```
   - do not forget to stop and clean up:
   ```
   ./staging_compose.sh <name-of-the-service> rm --delete-namespace
   ```
   - of course you can just stop without deleting the service and then start again using `start` and `stop` commands
   - to scale:
   ```
   ./staging_compose.sh <name-of-the-service> scale 2
   ```
   - list running containers
   ```
   ./staging_compose.sh <name-of-the-service> ps
   ```

   Read AWS ecs-cli documentation if you want/need - above scripts are just wrappers around *ecs-cli*.

##### Important
   **!!!** If you use ecs-cli directly make sure you specify --project-name parameter properly, otherwise you may interfere with someone else's deployment.
   By default in staging_composes script `--project-name` equals to `$STUDENT_NAME-$SERVICE_NAME`. Service name created will have a prefix `service-`. Cloudwatch logs will go into the same group but will have prefix with `$STUDENT_NAME-`

#### Logs and debugging your app

   This is essential for you to debug. Having the output of the `staging_compose ps` command with taskId, you can access logs of your service run like this:
   ```
   ecs-cli logs --task-id 6ef4bc73-9bed-499c-91ee-390da6d2a851 --follow
   ```

#### Interacting with Kafka

   At the moment access to kafka is managed from single client ec2 machine. To log in to the machine ask teacher to provide you a pem file and put it to the project root.

   To log in to that client machine simply run
   ```
   ./kafka_client.sh
   ```

   If you're on Windows - then configure Putty accordingly - use kafka-client-ec2.pem file provided to you. And make sure you have appropriate permissions set up for the file, otherwise ssh client will warn you (on unix machines `chmod 600 kafka-client-ec2.pem`)

   Once logged in - you can run commands below from the home dir.

   Basically, you need 3 types of operations:

   - create/describe/list topics
     - create
     ```
     kafka-topics.sh --zookeeper z-3.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181,z-2.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181,z-1.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181 --create --topic test_topic_out --replication-factor 3 --partitions 2
     ```
     - describe
     ```
     kafka-topics.sh --zookeeper z-3.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181,z-2.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181,z-1.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181 --describe --topic test_topic_out
     ```
     - list
     ```
     kafka-topics.sh --zookeeper z-3.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181,z-2.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181,z-1.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181 --list
     ```
   - consume topic
     ```
     kafka-console-consumer.sh --bootstrap-server z-3.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181,z-2.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181,z-1.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181 --topic sensor-data --from-beginning
     ```
   - produce into topic
     ```
     kafka-console-producer.sh --broker-list z-3.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181,z-2.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181,z-1.ucustreamingclass.mf5zba.c7.kafka.us-east-1.amazonaws.com:2181 --topic sensor-data
     ```

###### **!!!** Important: the IP addresses of Kafka brokers may change and data in topics deleted. If so - you will be informed in the chat.

#### Windows

   On Windows, make sure you configure aws-cli (`aws configure`) before running `staging_configure.cmd`

   Scripts provided to you were mostly tested on Unix environment - please contact the teacher if you face any problems.

#### Hints

   When debugging `ecs-cli compose` task create outputs with --debug you may find useful piping through

   ```
   ... | awk '{gsub(/\\n/,"\n")}1'
   ```
   to substitute \n with actual newline

## Logging & Debugging

Adjust levels in log4j.properties and in docker-compose KAFKA_LOG4J_LOGGERS. By default they set to WARN for library (kafka/zookeeper/streams/akka/etc) and DEBUG for application code

While debugging streaming app for instance, start from changing log4j.rootLogger from WARN to INFO (DEBUG will give you too much bloat)

## Docker

  We are using Confluent docker images for kafka stack - https://github.com/confluentinc/cp-docker-images.
  
  Control center deployment can be added for visual monitoring kafka cluster but requires additional components - review compose here - https://github.com/confluentinc/cp-docker-images/blob/5.1.0-post/examples/cp-all-in-one/docker-compose.yml

### Running Docker Commands as a Non-Root User

```
sudo usermod -aG docker <username>
newgrp docker
```

### Management UI
Use portainer if you don't want to interact with docker through cli
```
docker container run -d -p 9000:9000 -v /var/run/docker.sock:/var/run/docker.sock portainer/portainer
```

### Java base image
To keep containers lightweight - minimal alpine-linux based image is used for java:
```
docker pull anapsix/alpine-java
```

### Docker clean up

In some cases you may want to recreate everything from scratch and clean up environment, for that, you can use
```
docker system prune
```

You can remove particular images as well, by first listing them and then removing:
```
docker image ls -a
```

```
docker rmi <few starting letters of image id>
```

## Kafka

### Create topic

You can use confluent bundled tools to interact with the cluster, e.g. to create topics:

Command to create topic named foo with 4 partitions and replication-factor 1
```
docker run --net=host --rm confluentinc/cp-kafka:5.1.0 kafka-topics --create --topic foo --partitions 4 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
```

### Produce/consume topic

Or you can use https://github.com/edenhill/kafkacat tool as well, e.g.:

#### consume
Keep in mind that outbound address that kafka broker is listening to is different in this compose configuration, see KAFKA_ADVERTISED_LISTENERS
When interacting with broker from external (local) environment e.g. when using kafkacat, you should use localhost:39092 address in this configuration.
```
kafkacat -C -b localhost:39092 -t test_topic_out -p 0
```

#### produce
```
echo 'publish to partition 0' | kafkacat -P -b localhost:19092,localhost:29092,localhost:39092 -t foo -p 0
```

#### list topics
```
kafkacat -L -b localhost
```

# Components

## solar-panel-emulator

This service is responsible for generating reddit real-time comments. In fact, it reads the csv files in 5 streams emulating five different sources.

## weather-provider

This service uses Twitter API to get the real-time tweets. 

## streaming-app

Joins KStream (Twitter data) with the KTable (Reddit data) 

# Scaling streaming app

Scaling kafka-streams is as easy as pie - just start one more instance of it. As we use docker-compose we can do so by executing:
```
docker-compose scale streaming-app=4
```

Keep in mind that maximum parallelism level is number of partitions in the topic that streaming app consumes.
