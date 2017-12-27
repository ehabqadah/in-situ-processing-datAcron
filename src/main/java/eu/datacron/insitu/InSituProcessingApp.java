package eu.datacron.insitu;

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
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010.FlinkKafkaProducer010Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.datacron.insitu.areas.Area;
import eu.datacron.insitu.areas.AreasUtils;
import eu.datacron.insitu.common.utils.Configs;
import eu.datacron.insitu.flink.utils.StreamExecutionEnvBuilder;
import eu.datacron.insitu.maritime.AisMessage;
import eu.datacron.insitu.maritime.AisMessageCsvSchema;
import eu.datacron.insitu.maritime.streams.operators.AisMessagesStreamSorter;
import eu.datacron.insitu.maritime.streams.operators.AisStreamEnricher;

/**
 * The main in-situ driver app
 * 
 * @author ehab.qadah
 *
 */

public class InSituProcessingApp {

  private static Configs configs = Configs.getInstance();
  private static final Logger LOG = LoggerFactory.getLogger(InSituProcessingApp.class);

  /**
   * Main method.
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    boolean writeOutputStreamToFile = configs.getBooleanProp("writeOutputStreamToFile");

    String cehkPointsPath =
        Paths.get(configs.getStringProp("flinkCheckPointsPath") + "/" + System.currentTimeMillis())
            .toUri().toString();

    String appVersion = AppUtils.getAppVersion();
    StreamSourceType streamSource =
        StreamSourceType.valueOf(configs.getStringProp("streamSourceType").toUpperCase());
    String outputLineDelimiter = configs.getStringProp("outputLineDelimiter");
    String outputPath = configs.getStringProp("outputFilePath") + appVersion;
    int parallelism = configs.getIntProp("parallelism");
    String ploygonsFilePath = configs.getStringProp("polygonsFilePath");
    String outputStreamTopic = configs.getStringProp("outputStreamTopicName");
    // Get the json config for parsing the raw input stream
    String parsingConfig = AppUtils.getParsingJsonConfig();

    List<Area> areas = AreasUtils.getAllAreas(ploygonsFilePath);

    // Set up the execution environment
    final StreamExecutionEnvironment env =
        new StreamExecutionEnvBuilder().setParallelism(parallelism).setStateBackend(cehkPointsPath)
            .build();

    KeyedStream<AisMessage, Tuple> kaydAisMessagesStream =
        setupKayedAisMessagesStream(env, streamSource, parsingConfig, outputLineDelimiter);

    KeyedStream<AisMessage, Tuple> kaydAisMessagesStreamWithOrder =
        setupOrderStream(kaydAisMessagesStream);

    DataStream<AisMessage> enrichedAisMessagesStream =
        kaydAisMessagesStreamWithOrder.map(new AisStreamEnricher(areas));

    // Write the enriched stream to Kafka or file
    writeEnrichedStream(enrichedAisMessagesStream, parsingConfig, writeOutputStreamToFile,
        outputLineDelimiter, outputPath, outputStreamTopic);

    // Execute program

    LOG.info(env.getExecutionPlan());

    JobExecutionResult executionResult = null;
    try {
      executionResult = env.execute("datAcron In-Situ Processing " + appVersion);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    System.out.println("Full execution time=" + executionResult.getNetRuntime(TimeUnit.MINUTES));
  }

  private static KeyedStream<AisMessage, Tuple> setupOrderStream(
      KeyedStream<AisMessage, Tuple> kaydAisMessagesStream) {
    return kaydAisMessagesStream.process(new AisMessagesStreamSorter()).keyBy("id");
  }

  private static void writeEnrichedStream(DataStream<AisMessage> enrichedAisMessagesStream,
      String parsingConfig, boolean writeOutputStreamToFile, String outputLineDelimiter,
      String outputPath, String outputStreamTopic) throws IOException {

    if (writeOutputStreamToFile) {
      enrichedAisMessagesStream.map(new AisMessagesToCsvMapper(outputLineDelimiter)).writeAsText(
          outputPath, WriteMode.OVERWRITE);
    }

    // Write to Kafka
    Properties producerProps = AppUtils.getKafkaProducerProperties();

    FlinkKafkaProducer010Configuration<AisMessage> myProducerConfig =
        FlinkKafkaProducer010.writeToKafkaWithTimestamps(enrichedAisMessagesStream,
            outputStreamTopic, new AisMessageCsvSchema(parsingConfig, outputLineDelimiter),
            producerProps);
    myProducerConfig.setLogFailuresOnly(false);
    myProducerConfig.setFlushOnCheckpoint(true);

  }

  /**
   * Setup the kayed stream of AIS messages from a raw stream.
   */
  private static KeyedStream<AisMessage, Tuple> setupKayedAisMessagesStream(
      final StreamExecutionEnvironment env, StreamSourceType streamSource, String parsingConfig,
      String outputLineDelimiter) {
    DataStream<AisMessage> aisMessagesStream =
        AppUtils.getAISMessagesStream(env, streamSource, getSourceLocationProperty(streamSource),
            parsingConfig, outputLineDelimiter);

    // Construct the keyed stream (i.e., trajectories stream) of the AIS messages by grouping them
    // based on the message ID (MMSI for vessels)
    KeyedStream<AisMessage, Tuple> kaydAisMessagesStream = aisMessagesStream.keyBy("id");
    return kaydAisMessagesStream;
  }

  /**
   * Get the actual data file path or kafka topic based on the stream source type value.
   */
  private static String getSourceLocationProperty(StreamSourceType streamSource) {
    switch (streamSource) {
      case FILE:
        return "aisDataSetFilePath";
      case KAFKA:
        return "inputStreamTopicName";
      case HDFS:
        return "aisDataSetHDFSFilePath";
      default:
        return null;
    }
  }
}
