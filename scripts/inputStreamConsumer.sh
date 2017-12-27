#!/bin/bash

# Get the bootstrap servers & topic name from the config.properties file
IFS=''
while read line
do
prop=$(echo $line | awk -F"=" '{print $1}')   
set -- $prop


if [ $prop == "bootstrapServers" ]; then
  bootstrapServers=$(echo $line | awk -F"=" '{print $2}')   
set -- $bootstrapServers
fi

if [ $prop == "inputStreamTopicName" ]; then
  topicName=$(echo $line | awk -F"=" '{print $2}')   
set -- $topicName
fi

if [ $prop == "kafkaDir" ]; then
  KAFKA_DIR=$(echo $line | awk -F"=" '{print $2}')   
set -- $KAFKA_DIR
fi

done < ../src/main/resources/config.properties

cd $KAFKA_DIR

#Start the kafka consumer for output stream of in-situ processing 
./bin/kafka-console-consumer.sh --topic  $topicName --bootstrap-server $bootstrapServers --from-beginning

