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

if [ $prop == "kafkaDir" ]; then
  KAFKA_DIR=$(echo $line | awk -F"=" '{print $2}')   
set -- $KAFKA_DIR
fi

done < ../src/main/resources/config.properties



cd $KAFKA_DIR

#Start the kafka consumer for output stream of in-situ processing 
./bin/kafka-console-consumer.sh --topic $topicName --bootstrap-server $bootstrapServers    --from-beginning


#./bin/kafka-topics.sh --zookeeper 192.168.1.1:2181,192.168.1.2:2181,192.168.1.3:2181 --list
#./bin/kafka-console-consumer.sh --topic ais_messages_in_situ_processing_out --bootstrap-server localhost:9095,localhost:9093,localhost:9094--from-beginning

# ./bin/kafka-console-consumer.sh --topic rdfizer_output_v2 --bootstrap-server 192.168.1.2:9092,192.168.1.3:9092,192.168.1.5:9092 --from-beginning

#./kafkaTunnel.sh --remote-machine ehabq@datacron1.ds.unipi.gr --zookeeper 192.168.1.2:2181,192.168.1.3:2181,192.168.1.5:2181 --bootstrap-server 192.168.1.2:9092,192.168.1.3:9092,192.168.1.5:9092


 #kafkatunnel manual 10.11.85.128,10.11.82.30,10.11.83.9 10.11.80.7,10.11.80.123,10.11.81.13
 
#  kafkatunnel manual 192.168.1.2:2181,192.168.1.3:2181,192.168.1.5:2181     192.168.1.2:9092,192.168.1.3:9092,192.168.1.5:9092
