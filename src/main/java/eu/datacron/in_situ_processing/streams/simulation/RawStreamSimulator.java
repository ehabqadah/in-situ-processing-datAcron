package eu.datacron.in_situ_processing.streams.simulation;

import java.util.Properties;

import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import eu.datacron.in_situ_processing.AppUtils;
import eu.datacron.in_situ_processing.common.utils.Configs;
import eu.datacron.in_situ_processing.flink.utils.FileLinesStreamSource;
import eu.datacron.in_situ_processing.flink.utils.StreamExecutionEnvBuilder;

/**
 * This class is responsible for raw streams simulation/replay
 * 
 * @author ehab.qadah
 */
public class RawStreamSimulator {

  private static Configs configs = Configs.getInstance();

  public static void main(String[] args) throws Exception {
    // set up the execution environment
    final StreamExecutionEnvironment env = new StreamExecutionEnvBuilder().build();

    // Get the json config for parsing the raw input stream
    String parsingConfig = AppUtils.getParsingJsonConfig();

    KeyedStream<Tuple3<String, Long, String>, Tuple> kaydRawMessagesStream =
        setupKayedRawMessagesStream(env, parsingConfig);

    String outputStreamTopicName = configs.getStringProp("inputStreamTopicName");
    double streamDelayScale = configs.getDoubleProp("streamDelayScale");
    Properties producerProps = AppUtils.getKafkaProducerProperties();

    // replay the stream
    kaydRawMessagesStream.map(new StreamPlayer(streamDelayScale, outputStreamTopicName,
        producerProps));

    // execute program
    env.execute("datAcron In-Situ Processing Stream Simulator");
  }

  /***
   * Setup the kayed stream of a raw stream.
   * 
   * @param env
   * @param streamSource
   * @param parsingConfig
   * @return
   */
  private static KeyedStream<Tuple3<String, Long, String>, Tuple> setupKayedRawMessagesStream(
      final StreamExecutionEnvironment env, String parsingConfig) {
    DataStream<Tuple3<String, Long, String>> rawStream =
        env.addSource(
            new FileLinesStreamSource(configs.getStringProp("aisMessagesFilePath"), parsingConfig))
            .map(new RawStreamMapper(parsingConfig)).setParallelism(1);

    // assign the timestamp of the AIS messages based on their timestamps
    DataStream<Tuple3<String, Long, String>> rawStreamWithTimeStamp =
        rawStream.assignTimestampsAndWatermarks(new RawMessageTimestampAssigner());

    // Construct the keyed stream (i.e., trajectories stream) of the raw messages by grouping them
    // based on the message ID (MMSI for vessels)
    KeyedStream<Tuple3<String, Long, String>, Tuple> kaydAisMessagesStream =
        rawStreamWithTimeStamp.keyBy(0);
    return kaydAisMessagesStream;
  }

}
