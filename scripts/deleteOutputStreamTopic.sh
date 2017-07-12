#!/bin/bash

# Get the bootstrap servers & toic name from the config.properties file
IFS=''
while read line
do
prop=$(echo $line | awk -F"=" '{print $1}')   
set -- $prop


if [ $prop == "zookeeper" ]; then
  zookeeper=$(echo $line | awk -F"=" '{print $2}')   
set -- $zookeeper
fi

if [ $prop == "outputStreamTopicName" ]; then
  topicName=$(echo $line | awk -F"=" '{print $2}')   
set -- $topicName
fi

done < ../src/main/resources/config.properties

KAFKA_DIR="/home/ehabqadah/frameworks/kafka_2.11-0.10.2.0"
cd $KAFKA_DIR

#Delete the kafka topic of the  output stream of in-situ processing 
echo  $zookeeper

sudo ./bin/kafka-topics.sh --zookeeper $zookeeper --delete --topic $topicName