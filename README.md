# In-Situ Processing - datAcron

This is a component within the [datAcron EU](http://www.datacron-project.eu/) project.

This project aims to provide a module that process a stream of raw messages
(i.e., AIS messages) and enrich it with derived attributes such min/max, average and variance of original fields.

In addition, a stream simulator for the raw messages is developed in the context of this module, which provides a functionality to replay the original stream of raw messages by generating a simulated new Kafka Stream and taking into the account the time delay between two consecutive messages of a trajectory, furthermore, this delay can be scaled in/out by a configuration parameter.

### Contributers:
 ehab.qadah@iais.fraunhofer.de,<br/>
 michael.mock@iais.fraunhofer.de 

# Output Format:
 * The output line header for the maritime use case is as the following:
 `id,timestamp,longitude,latitude,turn,speed,heading,course,status,AverageDiffTime,NumberOfPoints,LastTimestamp,LastDiffTime,MinSpeed,MinDiffTime, MaxSpeed, MaxDiffTime, MinLong,MaxLong,MinLat,MaxLat,LastDifftime, AverageSpeed,VarianceSpeed `
  * The following four records for a trajectory (id=228037600​):
```json
   228037600,1443650419,-4.4480133,48.157574,-127.0,9.1,511,87.6,15,0.0,1,1443650419,0,9.1,92  23372036854775807,9.1,0,-4.4480133,-4.4480133,48.157574,48.157574,0,9.1,0.0

    228037600,1443650430,-4.4473267,48.15763,-127.0,9.1,511,87.2,15,5.5,2,1443650430,11,9.1,11 ,9.1,11,-4.4480133,-4.4473267,48.157574,48.15763,11,9.1,0.0

    228037600,1443650439,-4.4467,48.157658,-127.0,9.1,511,86.8,15,6.666666666666667,3,14436504 39,9,9.1,9,9.1,11,-4.4480133,-4.4467,48.157574,48.157658,9,9.1,0.0

    228037600,1443650450,-4.4460735,48.15768,-127.0,9.0,511,87.2,15,7.75,4,1443650450,11,9.0,9  ,9.1,11,-4.4480133,-4.4460735,48.157574,48.15768,11,9.075,0.0018749999999841
```
# Run on Flink (locally):
 * To run the **In-Situ Processing module** on Flink cluster (locally):
    * Go the root directory of the project.
    * Edit the 'scripts/runInSituOnFlinkLocally.sh' and update the correct local directory of the Flink installation for the `FLINK_DIR`  in the script.
    * Make sure that the script file has a permission to be executed, use 'chmod +x scripts/runInSituOnFlinkLocally.sh'.
    * Run the script by `./scripts/runInSituOnFlinkLocally.sh` that submit the In-Situ Processing job to the local Flink cluster.

 * To run the **Stream Simulator** component on Flink cluster (locally):
      * Go the root directory of the project.
      * Edit the 'scripts/runSimulatorOnFlinkLocally.sh' and update the correct local directory of the Flink installation for the `FLINK_DIR`  in the script.
      * Make sure that the script file has a permission to be executed, use 'chmod +x scripts/runSimulatorOnFlinkLocally.sh'.
      * Run the script by `./scripts/runSimulatorOnFlinkLocally.sh` that submit the Stream simulator job to the local Flink cluster.    

# Setup in development mode:
* To Setup the Kafka cluster locally, run a predefined shell script `scrips/setup-kafka.sh`
* To execute the simulator for the stream of raw messages, run `eu.datacron.in_situ_processing.streams.simulation.RawStreamSimulator`.
*  To execute the In-Situ Processing module:  `eu.datacron.in_situ_processing.InSituProcessingApp`.
* To check the module output run the `scrips/runOutputStreamKafkaConsumer.sh` that lunches a consumer console for the output stream of the module.


# Configurations:

This section describes the different configurations/parameter that cusomize the execution of the In-Situ Processing module, the following are the all parameter of the mdoule along side with their description and usage, and all configs are located in the [config.properties](/src/main/resources/config.properties) file.

| Parameter  Name        | Example           | Description  | Used In  |
| ------------- |:-------------:| -----:|------------|
| `bootstrapServers`| localhost:9092,localhost:9093| Kafka zookeeper host string| `InSituProcessingApp` & `RawStreamSimulator`|
| `zookeeper`  | localhost:2181|A list of host/port pairs to use for establishing the initial connection to the Kafka cluster, for more details check [here](https://kafka.apache.org/documentation/#brokerconfigs) |`InSituProcessingApp` & `RawStreamSimulator`|
| `inputStreamTopicName` | aisInsituIn|This is the topic name of the output stream of the RawStreamSimulator and the topic name of the input stream of `InSituProcessingApp`, so both components are connected through a Kafka stream |`InSituProcessingApp` & `RawStreamSimulator`|
| `outputStreamTopicName` | aisInsituOut|The topic name of the output stream of the In-Situ Processing (i.e., **enriched stream**)|`InSituProcessingApp` |
| `kafkaGroupId` | myGroup|The Kafka consumer group name for the `InSituProcessingApp` if the `streamSourceType` is set as `KAFKA` |`InSituProcessingApp`|
| `aisMessagesFilePath` | ./data/nari_test_dataset.csv|The path of the input dataset file|`InSituProcessingApp` & `RawStreamSimulator`|
| `streamSourceType` | FILE or KAFKA |This to select which source to be used as input for the In-Situ Processing module eitherdirectly by reading an input file (**aisMessagesFilePath**) <br/>or by ingesting a Kafka stream (**inputStreamTopicName**)|`InSituProcessingApp` |
| `streamDelayScale` | 10.0|Scale factor of the time delay between the raw messages in the Stream simulator (i.e., simulated delay = actual delay *streamDelayScale )| `RawStreamSimulator`|
| `inputDataSchema` | [nariRawStreamSchema.json](/src/main/resources/nariRawStreamSchema.json) or [imisRawStreamSchema.json](/src/main/resources/imisRawStreamSchema.json)| Specifies the schema of the input raw messages to support multiple sources such as IMIS Global and NARI files in the maritime use case|`InSituProcessingApp` & `RawStreamSimulator`|
