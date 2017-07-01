package eu.datacron.in_situ_processing;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import eu.datacron.in_situ_processing.common.utils.Configs;
import eu.datacron.in_situ_processing.flink.utils.StreamExecutionEnvBuilder;
import eu.datacron.in_situ_processing.maritime.beans.AisMessage;



public class InSituProcessingApp {

  private static Configs configs = Configs.getInstance();

  public static void main(String[] args) throws Exception {
    // set up the execution environment
    final StreamExecutionEnvironment env = new StreamExecutionEnvBuilder().build();

    StreamSourceType streamSource =
        StreamSourceType.valueOf(configs.getStringProp("streamSourceType").toUpperCase());
    // Get the json config for parsing the raw input stream
    String parsingConfig = AppUtils.getParsingJsonConfig();

    DataStream<AisMessage> aisMessagesStream =
        AppUtils.getAISMessagesStream(env, streamSource, getSourceLocationProperty(streamSource),
            parsingConfig);

    // assign the timestamp of the AIS messages based on their timestamps
    DataStream<AisMessage> aisMessagesStreamWithTimeStamp =
        aisMessagesStream.assignTimestampsAndWatermarks(new AisMessagesTimeAssigner());

    // Construct the keyed stream (i.e., trajectories stream) of the AIS messages by grouping them
    // based on the message ID (MMSI for vessels)
    KeyedStream<AisMessage, Tuple> kaydAisMessagesStream =
        aisMessagesStreamWithTimeStamp.keyBy("id");
    kaydAisMessagesStream.print();
    // execute program
    env.execute("datAcron In-Situ Processing");
  }

  /***
   * Get the actual data file path or kafka topic based on the stream source type value
   * 
   * @param streamSource
   * @return
   */
  private static String getSourceLocationProperty(StreamSourceType streamSource) {
    switch (streamSource) {
      case FILE:
        return "aisMessagesFilePath";
      case KAFKA:
        return "inputAisTopicName";
      default:
        return null;


    }

  }
}
