#!/bin/bash

mvn clean package 

projectWorkDir=$(pwd)

FLINK_DIR="/home/ehabq/flink-1.3.1"

cd $FLINK_DIR

# Start Flink 
./bin/start-local.sh

jarFile=$(find $projectWorkDir/target/in-situ-processing*.jar)

./bin/flink run $jarFile  > deployLog.log &
