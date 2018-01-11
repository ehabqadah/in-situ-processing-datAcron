#!/bin/bash


# YARN Cluster configs 

numberOfTaskManagers=10
memoryPerTaskManager=15360
processingSlotsPerTaskManager=8

# Pull new code changes 

git pull 

# Build the project

mvn clean package 

projectWorkDir=$(pwd)



# Get the flink dir and parallelism from the config.properties file
IFS=''
while read line
do
prop=$(echo $line | awk -F"=" '{print $1}')   
set -- $prop


if [ $prop == "parallelism" ]; then
  parallelism=$(echo $line | awk -F"=" '{print $2}')   
set -- $bootstrapServers
fi


if [ $prop == "flinkDir" ]; then
  FLINK_DIR=$(echo $line | awk -F"=" '{print $2}')   
set -- $KAFKA_DIR
fi

done < ./src/main/resources/config.properties

cd $FLINK_DIR

# Start Yarn session  

#if [ $numberOfArgs -gt 0 ]; then

#./bin/yarn-session.sh -n $numberOfTaskManger -tm $taskMangerMemory -s $processingSlots & 

#fi 

#sleep 90

jarFile=$(find $projectWorkDir/target/in-situ-processing*.jar)


jobName="in-situ-p:$parallelism-tm:$numberOfTaskManagers"


# Submit a Flink program to the YARN cluster

./bin/flink run -m yarn-cluster -yn $numberOfTaskManagers -ytm $memoryPerTaskManager -ys $processingSlotsPerTaskManager  -ynm  $jobName $jarFile  > deployOnYarn.log &
