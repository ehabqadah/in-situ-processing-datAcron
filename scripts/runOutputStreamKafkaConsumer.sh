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

#Start the kafka consumer for output stream of in-situ processing 
./bin/kafka-console-consumer.sh --topic $topicName --bootstrap-server $bootstrapServers --from-beginning


#./bin/kafka-console-consumer.sh --topic ais_messages_in_situ_processing_out --bootstrap-server 192.168.1.2:9092,192.168.1.3:9092,192.168.1.5:9092 --from-beginning

#./bin/kafka-console-consumer.sh --topic ais_messages_in_situ_processing_out --bootstrap-server localhost:9095,localhost:9093,localhost:9094--from-beginning


#./kafkaTunnel.sh --remote-machine ehabq@datacron2.ds.unipi.gr --zookeeper 192.168.1.2:2181,192.168.1.3:2181,192.168.1.5:2181 --bootstrap-server 192.168.1.2:9092,192.168.1.3:9092,192.168.1.5:9092