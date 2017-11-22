#!/bin/bash

mvn clean package

projectWorkDir=$(pwd)

FLINK_DIR="/home/ehabqadah/frameworks/flink-1.3.1"

cd $FLINK_DIR

# Start Flink 
./bin/start-local.sh

jarFile=$(find $projectWorkDir/target/in-situ-processing*.jar)
./bin/flink run -c eu.datacron.in_situ_processing.streams.simulation.RawStreamSimulator  $jarFile 