#!/bin/bash

KAFKA_DIR="/home/ehabqadah/frameworks/kafka_2.11-0.10.2.0"
cd $KAFKA_DIR

#kill zookeeper if it is already running
sudo  bin/zookeeper-server-stop.sh

# start zookeeper
sudo  bin/zookeeper-server-start.sh config/zookeeper.properties &


sleep 5
echo "finished zookeeper setup"


sudo bin/kafka-server-stop.sh

sudo bin/kafka-server-start.sh config/server-1.properties & 


sleep 5
echo "finished server1 setup"


sudo bin/kafka-server-start.sh config/server-2.properties & 

sleep 5
echo "finished server2 setup"
