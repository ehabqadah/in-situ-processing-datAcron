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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010.FlinkKafkaProducer010Configuration;

import eu.datacron.in_situ_processing.common.utils.Configs;
import eu.datacron.in_situ_processing.flink.utils.AisMessagesFileWriter;
import eu.datacron.in_situ_processing.flink.utils.StreamExecutionEnvBuilder;
import eu.datacron.in_situ_processing.maritime.AisMessage;
import eu.datacron.in_situ_processing.maritime.AisMessageCsvSchema;
import eu.datacron.in_situ_processing.maritime.streams.operators.AisMessagesTimeAssigner;
import eu.datacron.in_situ_processing.maritime.streams.operators.AisStreamEnricher;


public class InSituProcessingApp {

  private static Configs configs = Configs.getInstance();

  public static void main(String[] args) throws Exception {
    boolean writeOnlyToFile = args.length > 0;
    // set up the execution environment
   
    final StreamExecutionEnvironment env = new StreamExecutionEnvBuilder().build();

    StreamSourceType streamSource =
        StreamSourceType.valueOf(configs.getStringProp("streamSourceType").toUpperCase());
    // Get the json config for parsing the raw input stream
    String parsingConfig = AppUtils.getParsingJsonConfig();

    KeyedStream<AisMessage, Tuple> kaydAisMessagesStream =
        setupKayedAisMessagesStream(env, streamSource, parsingConfig);

    KeyedStream<AisMessage, Tuple> kaydAisMessagesStreamWithOrder =
        setupOrderStream(kaydAisMessagesStream);

    // kaydAisMessagesStream.print();
    DataStream<AisMessage> enrichedAisMessagesStream =
        kaydAisMessagesStreamWithOrder.flatMap(new AisStreamEnricher());

    // enrichedAisMessagesStream.print();
    // write the enriched stream to Kafka or file
    writeEnrichedStream(enrichedAisMessagesStream, parsingConfig, writeOnlyToFile);

    // execute program
    env.execute("datAcron In-Situ Processing");

  }

  private static KeyedStream<AisMessage, Tuple> setupOrderStream(
      KeyedStream<AisMessage, Tuple> kaydAisMessagesStream) {
    // KeyedStream<AisMessage, Tuple> kaydAisMessagesStreamWithOrder =
    // kaydAisMessagesStream.countWindow(2, 2).reduce(new ReduceFunction<AisMessage>() {
    //
    // @Override
    // public AisMessage reduce(AisMessage value1, AisMessage value2) throws Exception {
    //
    // if (value2.getTimestamp() > value1.getTimestamp()) {
    // // value2.prevAisMessages.addAll(value1.prevAisMessages);
    // value2.prevAisMessages.add(value1);
    // return value2;
    // } else {
    //
    // // value1.prevAisMessages.addAll(value2.prevAisMessages);
    // value1.prevAisMessages.add(value2);
    // return value1;
    // }
    // }
    // }).keyBy("id");
    // return kaydAisMessagesStreamWithOrder;
    return kaydAisMessagesStream;
  }

  private static void writeEnrichedStream(DataStream<AisMessage> enrichedAisMessagesStream,
      String parsingConfig, boolean writeOnlyToFile) throws IOException {

    if (writeOnlyToFile) {
      String outputFile = configs.getStringProp("outputFilePath");


      if (!new File(outputFile).isFile()) {
        Path p = Paths.get(outputFile);
        Files.createFile(p);

      }
      // write to file
      enrichedAisMessagesStream.addSink(
          new AisMessagesFileWriter(outputFile, new AisMessageCsvSchema(parsingConfig, true)))
          .setParallelism(1);
    } else {
      // Write to Kafka
      Properties producerProps = AppUtils.getKafkaProducerProperties();
      String outputStreamTopic = configs.getStringProp("outputStreamTopicName");

      FlinkKafkaProducer010Configuration<AisMessage> myProducerConfig =
          FlinkKafkaProducer010.writeToKafkaWithTimestamps(enrichedAisMessagesStream,
              outputStreamTopic, new AisMessageCsvSchema(parsingConfig), producerProps);

      // the following is necessary for at-least-once delivery guarantee
      myProducerConfig.setLogFailuresOnly(false); // "false" by default
      myProducerConfig.setFlushOnCheckpoint(true); // "false" by default

    }

  }

  /***
   * Setup the kayed stream of AIS messages from a raw stream.
   * 
   * @param env
   * @param streamSource
   * @param parsingConfig
   * @return
   */
  private static KeyedStream<AisMessage, Tuple> setupKayedAisMessagesStream(
      final StreamExecutionEnvironment env, StreamSourceType streamSource, String parsingConfig) {
    DataStream<AisMessage> aisMessagesStream =
        AppUtils.getAISMessagesStream(env, streamSource, getSourceLocationProperty(streamSource),
            parsingConfig);

    // Assign the timestamp of the AIS messages based on their timestamps
    DataStream<AisMessage> aisMessagesStreamWithTimeStamp =
        aisMessagesStream.assignTimestampsAndWatermarks(new AisMessagesTimeAssigner());
    // .filter(
    // ais -> ais.getId().equals("228037600"));// debug code

    // Construct the keyed stream (i.e., trajectories stream) of the AIS messages by grouping them
    // based on the message ID (MMSI for vessels)
    KeyedStream<AisMessage, Tuple> kaydAisMessagesStream =
        aisMessagesStreamWithTimeStamp.keyBy("id");
    return kaydAisMessagesStream;
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
        return "inputStreamTopicName";
      default:
        return null;


    }

  }
}
