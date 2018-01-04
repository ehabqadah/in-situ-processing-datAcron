#!/bin/bash

# Get the bootstrap servers & topic name from the config.properties file
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


if [ $prop == "kafkaDir" ]; then
  KAFKA_DIR=$(echo $line | awk -F"=" '{print $2}')   
set -- $KAFKA_DIR
fi


done < ../src/main/resources/config.properties



cd $KAFKA_DIR

# Delete the kafka topic of the  output stream of in-situ processing 


#./bin/kafka-topics.sh --zookeeper $zookeeper --delete --topic $topicName



# Re-create the topic 

./bin/kafka-topics.sh --create --zookeeper $zookeeper --replication-factor 1 --partitions 12 --topic $topicName