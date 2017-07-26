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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010.FlinkKafkaProducer010Configuration;

import eu.datacron.in_situ_processing.common.utils.Configs;
import eu.datacron.in_situ_processing.flink.utils.StreamExecutionEnvBuilder;
import eu.datacron.in_situ_processing.maritime.AisMessage;
import eu.datacron.in_situ_processing.maritime.AisMessageCsvSchema;
import eu.datacron.in_situ_processing.maritime.streams.operators.AISMessagesTimeAssigner;
import eu.datacron.in_situ_processing.maritime.streams.operators.AisMessagesStreamSorter;
import eu.datacron.in_situ_processing.maritime.streams.operators.AisStreamEnricher;


public class InSituProcessingApp {

  private static Configs configs = Configs.getInstance();

  public static void main(String[] args) throws Exception {
    boolean writeOnlyToFile = args.length > 0;

    String cehkPointsPath =
        Paths.get(configs.getStringProp("flinkCheckPointsPath") + "/" + System.currentTimeMillis())
            .toUri().toString();
    // set up the execution environment
    final StreamExecutionEnvironment env =
        new StreamExecutionEnvBuilder().setStateBackend(cehkPointsPath).build();

    StreamSourceType streamSource =
        StreamSourceType.valueOf(configs.getStringProp("streamSourceType").toUpperCase());
    String outputLineDelimiter = configs.getStringProp("outputLineDelimiter");
    String outputFile = configs.getStringProp("outputFilePath");
    int outputWriterParallelism = configs.getIntProp("writeOutputParallelism");
    // Get the json config for parsing the raw input stream
    String parsingConfig = AppUtils.getParsingJsonConfig();

    KeyedStream<AisMessage, Tuple> kaydAisMessagesStream =
        setupKayedAisMessagesStream(env, streamSource, parsingConfig, outputLineDelimiter);

    KeyedStream<AisMessage, Tuple> kaydAisMessagesStreamWithOrder =
        setupOrderStream(kaydAisMessagesStream);

    // kaydAisMessagesStream.print();
    DataStream<AisMessage> enrichedAisMessagesStream =
        kaydAisMessagesStreamWithOrder.map(new AisStreamEnricher());

    // enrichedAisMessagesStream.print();
    // write the enriched stream to Kafka or file
    writeEnrichedStream(enrichedAisMessagesStream, parsingConfig, writeOnlyToFile,
        outputLineDelimiter, outputFile, outputWriterParallelism);

    // execute program
    env.execute("datAcron In-Situ Processing " + AppUtils.getAppVersion());
  }

  private static KeyedStream<AisMessage, Tuple> setupOrderStream(
      KeyedStream<AisMessage, Tuple> kaydAisMessagesStream) {
    return kaydAisMessagesStream.process(new AisMessagesStreamSorter()).keyBy("id");
  }

  private static void writeEnrichedStream(DataStream<AisMessage> enrichedAisMessagesStream,
      String parsingConfig, boolean writeOnlyToFile, String outputLineDelimiter, String outputFile,
      int writeParallelism) throws IOException {

    // enrichedAisMessagesStream.addSink(
    // new AisMessagesFileWriter(outputFile, new AisMessageCsvSchema(parsingConfig, true)));
    enrichedAisMessagesStream.map(new AisMessagesToCsvMapper(outputLineDelimiter))
        .writeAsText(outputFile, WriteMode.OVERWRITE).setParallelism(writeParallelism);
    if (!writeOnlyToFile) {
      // Write to Kafka
      Properties producerProps = AppUtils.getKafkaProducerProperties();
      String outputStreamTopic = configs.getStringProp("outputStreamTopicName");

      FlinkKafkaProducer010Configuration<AisMessage> myProducerConfig =
          FlinkKafkaProducer010.writeToKafkaWithTimestamps(enrichedAisMessagesStream,
              outputStreamTopic, new AisMessageCsvSchema(parsingConfig, outputLineDelimiter),
              producerProps);
      // the following is necessary for at-least-once delivery guarantee
      myProducerConfig.setLogFailuresOnly(false); // "false" by default
      myProducerConfig.setFlushOnCheckpoint(true); // "false" by default
      myProducerConfig.setParallelism(writeParallelism);
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
      final StreamExecutionEnvironment env, StreamSourceType streamSource, String parsingConfig,
      String outputLineDelimiter) {
    DataStream<AisMessage> aisMessagesStream =
        AppUtils.getAISMessagesStream(env, streamSource, getSourceLocationProperty(streamSource),
            parsingConfig, outputLineDelimiter);

    // Assign the timestamp of the AIS messages based on their timestamps
    DataStream<AisMessage> aisMessagesStreamWithTimeStamp =
        aisMessagesStream.assignTimestampsAndWatermarks(new AISMessagesTimeAssigner());
    // aisMessagesStream.print();

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

  /**
   * A map operator of AIS messages to CSV format
   * 
   * @author ehab.qadah
   *
   */
  public static final class AisMessagesToCsvMapper implements MapFunction<AisMessage, String> {

    private static final long serialVersionUID = 5306666449608883748L;
    private String delimiter;

    public AisMessagesToCsvMapper() {}

    public AisMessagesToCsvMapper(String outputLineDelimiter) {
      this.delimiter = outputLineDelimiter;
    }

    @Override
    public String map(AisMessage value) throws Exception {

      return value.toCsv(delimiter);
    }
  }
}
