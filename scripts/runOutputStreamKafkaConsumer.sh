#!/bin/bash

# Get the bootstrap servers & toic name from the config.properties file
IFS=''
while read line
do
prop=$(echo $line | awk -F"=" '{print $1}')   
set -- $prop


if [ $prop == "bootstrapServers" ]; then
  bootstrapServers=$(echo $line | awk -F"=" '{print $2}')   
set -- $bootstrapServers
fi

if [ $prop == "outputStreamTopicName" ]; then
  topicName=$(echo $line | awk -F"=" '{print $2}')   
set -- $topicName
fi

done < ../src/main/resources/config.properties

KAFKA_DIR="/home/ehabqadah/frameworks/kafka_2.11-0.10.2.0"
cd $KAFKA_DIR

#Start the kafka consumer for output stream od in-situ processing 
sudo ./bin/kafka-console-consumer.sh --topic $topicName --bootstrap-server $bootstrapServers

