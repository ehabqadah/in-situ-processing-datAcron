# in-situ-processing-datAcron

This is a component within the datAcron EU project.

This project aims to provide a module that process a stream of raw messages
(i.e., AIS messages) and enrich it with derived attributes such min/max, average and variance of original fields.

In addition, a stream simulator for the raw messages is developed in the context of this module, which provides a functionality to replay the original stream of raw messages by generating a simulated new Kafka Stream and taking into the account the time delay between two consecutive messages of a trajectory, furthermore, this delay can be scaled in/out by a configuration parameter.

### Contributors:
 * Ehab Qadah <br/>
 * PD Dr. Michael Mock

# Output Format:
 * The output line header for the maritime use case is as the following:
 `id,timestamp,longitude,latitude,turn,speed,heading,course,status,AverageDiffTime,NumberOfPoints,LastTimestamp,LastDiffTime,MinSpeed,MinDiffTime, MaxSpeed, MaxDiffTime, MinLong,MaxLong,MinLat,MaxLat,LastDifftime, AverageSpeed,VarianceSpeed `
  * The following four records for a trajectory (id=228037600​) :
   '228037600,1443650419,-4.4480133,48.157574,-127.0,9.1,511,87.6,15,0.0,1,1443650419,0,9.1,92  23372036854775807,9.1,0,-4.4480133,-4.4480133,48.157574,48.157574,0,9.1,0.0

    228037600,1443650430,-4.4473267,48.15763,-127.0,9.1,511,87.2,15,5.5,2,1443650430,11,9.1,11 ,9.1,11,-4.4480133,-4.4473267,48.157574,48.15763,11,9.1,0.0

    228037600,1443650439,-4.4467,48.157658,-127.0,9.1,511,86.8,15,6.666666666666667,3,14436504 39,9,9.1,9,9.1,11,-4.4480133,-4.4467,48.157574,48.157658,9,9.1,0.0

    228037600,1443650450,-4.4460735,48.15768,-127.0,9.0,511,87.2,15,7.75,4,1443650450,11,9.0,9  ,9.1,11,-4.4480133,-4.4460735,48.157574,48.15768,11,9.075,0.0018749999999841

'

# Generate jar file
 * To generate a .jar file that you can submit on your Flink cluster, run the  'mvn  clean package' command  in the projects root directory.
 * The file is located in 'target/in-situ-processing-1.0.1.jar'.

# Setup in development mode:
* To Setup the Kafka cluster locally, run a predefined shell script `scrips/setup-kafka.sh`
* To execute the simulator for the stream of raw messages, run `eu.datacron.in_situ_processing.streams.simulation.RawStreamSimulator`.
*  To execute the In-Situ Processing module:  `eu.datacron.in_situ_processing.InSituProcessingApp`.
* To check the module output run the `scrips/runOutputStreamKafkaConsumer.sh` that lunches a consumer console for the output stream of the module.


# Configurations:

This section describes the different configurations/parameter that cusomize the execution of the In-Situ Processing module, the following are the all parameter of the mdoule along side with their description and usage, and all configs are located in the [config.properties](/src/main/resources/config.properties) file.
 *
