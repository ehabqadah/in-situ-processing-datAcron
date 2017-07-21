#!/bin/bash

mvn clean package 

projectWorkDir=$(pwd)

FLINK_DIR="/home/ehabqadah/frameworks/flink-1.3.1"

cd $FLINK_DIR

# Start Flink 
sudo ./bin/start-local.sh

jarFile=$(find $projectWorkDir/target/in-situ-processing*.jar)

sudo ./bin/flink run $jarFile true > deployLog.log &