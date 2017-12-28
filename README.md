# In-Situ Processing - datAcron

A component within the [datAcron EU](http://www.datacron-project.eu/) project; it provides a Flink component that processes a stream of raw messages (i.e., AIS Dynamic Messages), and enriches it with derived attributes such as min/max, average, and variance of original fields (See `Output Format and Samples` & `Output Schema Description`).
In addition, a stream simulator for the raw messages is developed in the context of this module. It provides a feature to re-play the original stream of raw messages by generating a simulated new Kafka Stream and taking into the account the time delay between two consecutive messages of a trajectory. Furthermore, this delay can be scaled in/out by a configuration parameter (`streamDelayScale`).


# Deployment on datAcron YARN cluster:

 * Clone in-situ code: `https://github.com/ehabqadah/in-situ-processing-datAcron.git`
 * Go the project directory `cd in-situ-processing-datAcron`.
 * Update the [config.properties](http://datacron2.ds.unipi.gr:9081/eqadah/in-situ-processing-datAcron/blob/master/src/main/resources/config.properties) as described the `Configurations` section below.
 * Run `mvn clean package -Pbuild-jar` to compile and build the project jar.
 * Run the raw input stream  simulator ` java -cp  ..../in-situ-processing-datAcron/target/in-situ-processing-2.0.0.jar eu.datacron.insitu.InputStreamSimulatorApp`.
 * Go the Flink directory `cd .../flink-1.4.1`
 * Start Flink yarn session using  
    ```
   cd ../flink-1.4.0/
  ./bin/yarn-session.sh -n 10 -tm 15360 -s 8
```
 that allocates 10 Task Managers, with 15 GB of memory and 8 processing slots for each task manger.


* To run In-situ module:

 ```
   cd ../flink-1.4.0/
   ./bin/flink run -m yarn-cluster -yn 10  -ynm insitu_p..../in-situ-processing-datAcron/target/in-situ-processing-*.jar
```
* Check the output stream that has the kafka topic name in [config.properties](http://datacron2.ds.unipi.gr:9081/eqadah/in-situ-processing-datAcron/blob/master/src/main/resources/config.properties)  file, in particular, the value of `outputStreamTopicName` (e.g., ais_messages_in_situ_processing_out_v2).

# Output Format and Samples:
 * The data schema of the In-Situ Processing component's output as the following:
  ` timestamp,id,longitude,latitude,speed,heading,msgErrorFlag,turn,course,status,
NumberOfPoints,AverageDiffTime,LastDiffTime,MinDiffTime,MaxDiffTime,MaxSpeed,
MinSpeed,AverageSpeed,VarianceSpeed,MinLong,MaxLong,MinLat,MaxLat,MinTurn,MaxTurn,
MinHeading,MaxHeading,isChangeInArea,detectedAreas`
  * The following four samples of a trajectory (id=228037600​):
```javascript
2443660589,228037600,-4.342825,48.186092,9.1,511,,-127.0,197.7,15,686,14.83965014577259,11,7,311,10.2,5.6,9.12900874635568,0.3503100961334131,-4.45445,-4.316455,48.113506,48.20134,-127.0,-127.0,511,511,false,area1112315100;area1173315100
1443660600,228037600,-4.34293,48.18563,9.1,511,,-127.0,190.4,15,687,14.834061135371174,11,7,311,10.2,5.6,9.128966521106255,0.34980140644830476,-4.45445,-4.316455,48.113506,48.20134,-127.0,-127.0,511,511,true,
1443660610,228037600,-4.3429365,48.18521,9.1,511,,-127.0,180.5,15,688,14.827034883720925,10,7,311,10.2,5.6,9.128924418604647,0.3492941919618716,-4.45445,-4.316455,48.113506,48.20134,-127.0,-127.0,511,511,false, 1443660620,228037600,-4.3428435,48.184795,9.1,511,,-127.0,171.3,15,689,14.820029027576192,10,7,311,10.2,5.6,9.128882438316397,0.34878844626633326,-4.45445,-4.316455,48.113506,48.20134,-127.0,-127.0,511,511,true,area1112315100
1443660629,228037600,-4.3426933,48.184425,9.2,511,,-127.0,166.0,15,690,14.811594202898545,9,7,311,10.2,5.6,9.128985507246373,0.3482902751522813,-4.45445,-4.316455,48.113506,48.20134,-127.0,-127.0,511,511,false,area1112315100
```

# Output Description:
 * We use the same attributes and the delimiter (i.e., ",") as the AIS messages of NARI source with adding addition attributes computed by this module as depicted in the following table:

|Index| Attribute     | Data type           |Description  |
|-------------| ------------- |:-------------:|:-----|
|[0]| timestamp 	    |long           | timestamp in UNIX epochs (i.e., milliseconds elapsed since 1970-01-01 00:00:00.000).
|[1]|  id 	          | integer       |A globally unique identifier for the moving object (usually, the MMSI of vessels).|
|[2]|  longitude      |double         |  Longitude (georeference: WGS 1984).
|[3]| latitude 	      |double         | Latitude  (georeference: WGS 1984).
|[4]|  speed 	        |double         |  Speed over ground in knotsint (allowed values: 0-102.2 knots).
|[5]|  heading 	      |integer      	|  True heading in degrees (0-359), relative to true north.
|[6]|  msgErrorFlag 	|string         |  The MariWeb Security Appliance (MSA) error flags (if available).
|[7]|  turn 	        |double         |  Rate of turn, right or left, 0 to 720 degrees per minute.
|[8]|  course 	      |double         |  Course over ground (allowed values: 0-359.9 degrees).
|[9]|  status 	      |integer        |  AIS Navigational Status reported by the vessel.
|[10]|NumberOfPoints  |long           | The accumulated number of the received points i.e., AIS messages of the trajectory.|
|[11]|AverageDiffTime |double         | The average of the time difference between the consecutive AIS messages of the trajectory |
|[12]|LastDiffTime    | long          | The time difference of the current message and the previous received message|
|[13]|MinDiffTime     |long           | The minimum value of time difference until the current AIS message.|
|[14]|MaxDiffTime     |long           | The maximum value of time difference until the current AIS message.|
|[15]|MaxSpeed        | double        | The maximum value of speed within the trajectory until the current AIS message.|
|[16]|MinSpeed        | double        | The minimum value of speed with the trajectory until the current AIS message. |
|[17]|AverageSpeed    | double        | The average value of the speed|
|[18]|VarianceSpeed   |double         | The sample variance of speed |
|[19]|MinLong         | double        | The minimum value of longitude of the trajectory until the current AIS message.|
|[20]|MaxLong         | double        | The maximum value of longitude of the trajectory until the current AIS message.|
|[21]|MinLat          |double         |The minimum value of latitude of the trajectory until the current AIS message. |
|[22]|MaxLat          | double        |The maximum value of latitude of the trajectory until the current AIS message. |
|[23]|MinTurn         |double         |The minimum value of turn until of the trajectory the current AIS message. |
|[24]|MaxTurn         | double        |The maximum value of turn until the of the trajectory the current AIS message. |
|[25]|MinHeading      |int            |The minimum value of heading until of the trajectory the current AIS message. |
|[26]|MaxHeading      | int           |The maximum value of heading until of the trajectory the current AIS message. |
|[27]|isChangeInArea  | boolean       |A flag to indicate if there is change in the area or not. |
|[28]|detectedAreas   | string        |empty string or list of area codes/names separated by `;` |




# Configurations:

This section describes the different configurations/parameters that customize the execution of the In-Situ Processing module, and the following are the full parameters list of the module alongside with their description and usage, can be changed in the [config.properties](http://datacron2.ds.unipi.gr:9081/eqadah/in-situ-processing-datAcron/blob/master/src/main/resources/config.properties)  file.

| Parameter  Name        | Example           | Description  | Used In  |
| ------------- |:-------------:| :-----:|:------------:|
| `bootstrapServers`| localhost:9092,localhost:9093| Kafka zookeeper host string| `InSituProcessingApp` & `RawStreamSimulator`|
| `zookeeper`  | localhost:2181|A list of host/port pairs to use for establishing the initial connection to the Kafka cluster, for more details check [here](https://kafka.apache.org/documentation/#brokerconfigs) |`InSituProcessingApp` & `RawStreamSimulator`|
| `inputStreamTopicName` | aisInsituIn|This is the topic name of the output stream of the RawStreamSimulator and the topic name of the input stream of `InSituProcessingApp`, so both components are connected through a Kafka stream |`InSituProcessingApp` & `RawStreamSimulator`|
| `outputStreamTopicName` | aisInsituOut|The topic name of the output stream of the In-Situ Processing (i.e., **enriched stream**)|`InSituProcessingApp` |
| `kafkaGroupId` | in_situ|The Kafka consumer group name for the `InSituProcessingApp` if the `streamSourceType` is set as `KAFKA` |`InSituProcessingApp`|
| `aisDataSetFilePath` | ./data/nari_test_dataset.csv|The path of the input dataset file|`InSituProcessingApp` & `RawStreamSimulator`|
| `streamSourceType` | FILE or KAFKA |This to select which source to be used as input for the In-Situ Processing module either directly by reading an input file (**aisDataSetFilePath**) <br/>or by ingesting a Kafka stream (**inputStreamTopicName**)|`InSituProcessingApp` |
| `streamDelayScale` | 1.0|Scale factor of the time delay between the raw messages in the Stream simulator (i.e., simulated delay = actual delay *streamDelayScale )| `RawStreamSimulator`|
| `inputDataSchema` | [nariRawStreamSchema.json](/src/main/resources/nariRawStreamSchema.json) ,  [imisRawStreamSchema.json](/src/main/resources/imisRawStreamSchema.json) and [imisFullRawStreamSchema.json](/src/main/resources/imisFullRawStreamSchema.json) | Specifies the schema of the input raw messages to support multiple sources such as IMIS Global and NARI files in the maritime use case|`InSituProcessingApp` & `RawStreamSimulator`|
|`flinkCheckPointsPath`|checkpoints|The directory of the Flink checkpoints |`InSituProcessingApp` & `RawStreamSimulator`|
|`parallelism`|8|The parallelism level of the Flink job |`InSituProcessingApp`|
|`outputLineDelimiter`|,|The separator of the line of the output stream|`InSituProcessingApp`|
|`outputFilePath`||The output path of enriched stream file if `writeOutputStreamToFile` is true|`InSituProcessingApp`|
|`polygonsFilePath`|[polygons.csv](/static-data/polygons.csv)|The file path of areas polygons |`InSituProcessingApp`|

# Run on stand alone Flink cluster (locally):
 * To deploy the **In-Situ Processing module** on Flink cluster (locally):
    * Go the root directory of the project.
    * Edit the `/in-situ-processing-datAcron/scripts/deployOnFlinkCluster.sh` and update the correct directory of the Flink installation by changing the `FLINK_DIR` variable in the script.
    * Make sure that the script file has a permission to be executed, use `/in-situ-processing-datAcron/scripts/deployOnFlinkCluster.sh`.
    * Run the script by `/in-situ-processing-datAcron/scripts/deployOnFlinkCluster.sh` that submit the In-Situ Processing job to the local Flink cluster.

 * To run the **Raw AIS Messages Stream Simulator** component on Flink cluster (locally):
      * Go the root directory of the project.
      * Edit the `/in-situ-processing-datAcron/scripts/deploySimulatorOnFlink.sh` and update the correct local directory of the Flink installation by changing the `FLINK_DIR` variable in the script.
      * Make sure that the script file has a permission to be executed, use `chmod +x /in-situ-processing-datAcron/scripts/deploySimulatorOnFlink.sh`.
      * Run the script by `./scripts/runSimulatorOnFlinkLocally.sh` that submit the Stream simulator job to the Flink cluster.

# Setup for local development:
* To Setup the Kafka cluster locally, run a predefined shell script `scrips/setup-kafka.sh`
* To execute the simulator for the stream of raw messages, run `eu.datacron.insitu.InputStreamSimulatorApp`.
*  To execute the In-Situ Processing module:  `eu.datacron.insitu.InSituProcessingApp`.
* To check the module output run the `scrips/runOutputStreamKafkaConsumer.sh` that lunches a consumer console for the output stream of the module.


### Contributers:
 ehab.qadah@iais.fraunhofer.de,<br/>
 michael.mock@iais.fraunhofer.de
