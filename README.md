# In-Situ Processing - datAcron

This is a component within the [datAcron EU](http://www.datacron-project.eu/) project.

This module aims to provide a Flink component that process a stream of raw messages
(i.e., AIS Dynamic Messages) and enrich it with derived attributes such as min/max, average and variance of original fields.

In addition, a stream simulator for the raw messages is developed in the context of this module, which provides a functionality to replay the original stream of raw messages by generating a simulated new Kafka Stream and taking into the account the time delay between two consecutive messages of a trajectory, furthermore, this delay can be scaled in/out by a configuration parameter.

### Contributers:
 ehab.qadah@iais.fraunhofer.de,<br/>
 michael.mock@iais.fraunhofer.de

# Output Format and Samples:
 * The output line header for the maritime use case is as the following:
  ` timestamp,id,longitude,latitude,speed,heading,msgErrorFlag,turn,course,status,
NumberOfPoints,AverageDiffTime,LastDiffTime,MinDiffTime,MaxDiffTime,MaxSpeed,
MinSpeed,AverageSpeed,VarianceSpeed,MinLong,MaxLong,MinLat,MaxLat,Minturn,Maxturn,
MinHeading,MaxHeading `
  * The following four records of a trajectory (id=228037600​):
```csv
2443660589,228037600,-4.342825,48.186092,9.1,511,,-127.0,197.7,15,686,14.83965014577259,11,7,311,10.2,5.6,9.12900874635568,0.3503100961334131,-4.45445,-4.316455,48.113506,48.20134,-127.0,-127.0,511,511
1443660600,228037600,-4.34293,48.18563,9.1,511,,-127.0,190.4,15,687,14.834061135371174,11,7,311,10.2,5.6,9.128966521106255,0.34980140644830476,-4.45445,-4.316455,48.113506,48.20134,-127.0,-127.0,511,511
1443660610,228037600,-4.3429365,48.18521,9.1,511,,-127.0,180.5,15,688,14.827034883720925,10,7,311,10.2,5.6,9.128924418604647,0.3492941919618716,-4.45445,-4.316455,48.113506,48.20134,-127.0,-127.0,511,511 1443660620,228037600,-4.3428435,48.184795,9.1,511,,-127.0,171.3,15,689,14.820029027576192,10,7,311,10.2,5.6,9.128882438316397,0.34878844626633326,-4.45445,-4.316455,48.113506,48.20134,-127.0,-127.0,511,511
1443660629,228037600,-4.3426933,48.184425,9.2,511,,-127.0,166.0,15,690,14.811594202898545,9,7,311,10.2,5.6,9.128985507246373,0.3482902751522813,-4.45445,-4.316455,48.113506,48.20134,-127.0,-127.0,511,511
```

# Output Description:
 * We use the same order of attributes and the delimiter (i.e., ",") as the AIS messages of NARI source with adding addition attributes computed by this module as depicted in the following table:

| Attribute        | Data type           |Description  |
| ------------- |:-------------:|:-----|
|  id 	        | integer       |A globally unique identifier for the moving object (usually, the MMSI of vessels).|
|  status 	|integer        |  Navigational status
|  turn 	|double         |  Rate of turn, right or left, 0 to 720 degrees per minute
|  speed 	|double         |  Speed over ground in knotsint (allowed values: 0-102.2 knots)
|  course 	|double         |  Course over ground (allowed values: 0-359.9 degrees)
|  heading 	|integer      	|  True heading in degrees (0-359), relative to true north
|  longitude    |double         |  Longitude (georeference: WGS 1984)
| latitude 	|double         | Latitude  (georeference: WGS 1984)
| timestamp 	|long           | timestamp in UNIX epochs (i.e., milliseconds elapsed since 1970-01-01 00:00:00.000).
|AverageDiffTime|long           | The average of difference time between the positions message of a trajectory |
|NumberOfPoints |long            | The accumulated number of the received points |
|LastDiffTime   | long        | The time difference of the current message and the last previous received message|
|MinSpeed       | double        | The minimum value of speed until current message. |
|MinDiffTime    |long           | The minimum value of time difference until current message.|
|MaxSpeed       | double        | The maximum value of speed until current message.|
|MaxDiffTime    | double        | The maximum value of time difference until current message.|
|MinLong        | double        | The minimum value of longitude  until current message.|
|MaxLong        | double        | The maximum value of longitude until current message.|
|MinLat         |double         |The minimum value of latitude  until current message. |
|MaxLat         | double        |The maximum value of latitude  until current message. |
|AverageSpeed   | double        | The average of the speed|
|VarianceSpeed  |double         | The variance of speed |

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
| ------------- |:-------------:| :-----:|:------------:|
| `bootstrapServers`| localhost:9092,localhost:9093| Kafka zookeeper host string| `InSituProcessingApp` & `RawStreamSimulator`|
| `zookeeper`  | localhost:2181|A list of host/port pairs to use for establishing the initial connection to the Kafka cluster, for more details check [here](https://kafka.apache.org/documentation/#brokerconfigs) |`InSituProcessingApp` & `RawStreamSimulator`|
| `inputStreamTopicName` | aisInsituIn|This is the topic name of the output stream of the RawStreamSimulator and the topic name of the input stream of `InSituProcessingApp`, so both components are connected through a Kafka stream |`InSituProcessingApp` & `RawStreamSimulator`|
| `outputStreamTopicName` | aisInsituOut|The topic name of the output stream of the In-Situ Processing (i.e., **enriched stream**)|`InSituProcessingApp` |
| `kafkaGroupId` | myGroup|The Kafka consumer group name for the `InSituProcessingApp` if the `streamSourceType` is set as `KAFKA` |`InSituProcessingApp`|
| `aisMessagesFilePath` | ./data/nari_test_dataset.csv|The path of the input dataset file|`InSituProcessingApp` & `RawStreamSimulator`|
| `streamSourceType` | FILE or KAFKA |This to select which source to be used as input for the In-Situ Processing module eitherdirectly by reading an input file (**aisMessagesFilePath**) <br/>or by ingesting a Kafka stream (**inputStreamTopicName**)|`InSituProcessingApp` |
| `streamDelayScale` | 10.0|Scale factor of the time delay between the raw messages in the Stream simulator (i.e., simulated delay = actual delay *streamDelayScale )| `RawStreamSimulator`|
| `inputDataSchema` | [nariRawStreamSchema.json](/src/main/resources/nariRawStreamSchema.json) or [imisRawStreamSchema.json](/src/main/resources/imisRawStreamSchema.json)| Specifies the schema of the input raw messages to support multiple sources such as IMIS Global and NARI files in the maritime use case|`InSituProcessingApp` & `RawStreamSimulator`|
