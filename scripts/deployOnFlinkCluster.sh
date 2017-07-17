#!/bin/bash

mvn clean package 

projectWorkDir=$(pwd)

FLINK_DIR="/home/ehabqadah/frameworks/flink-1.3.1"

cd $FLINK_DIR

# Start Flink 
sudo ./bin/start-local.sh

sudo ./bin/flink run $projectWorkDir/target/in-situ-processing-1.0.2.jar true > deployLog.log &