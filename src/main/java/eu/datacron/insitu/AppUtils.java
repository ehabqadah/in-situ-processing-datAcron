package eu.datacron.insitu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.log4j.Logger;

import eu.datacron.insitu.common.utils.Configs;
import eu.datacron.insitu.flink.utils.FileLinesStreamSource;
import eu.datacron.insitu.maritime.AisMessage;
import eu.datacron.insitu.maritime.AisMessageCsvSchema;
import eu.datacron.insitu.maritime.streams.operators.AISMessagesTimeAssigner;
import eu.datacron.insitu.maritime.streams.operators.CsvLineToAisMessageMapper;


/**
 * @author ehab.qadah
 */
public class AppUtils {

  private static Configs configs = Configs.getInstance();
  static Logger logger = Logger.getLogger(AppUtils.class.getName());

  /**
   * Get the AIS messages stream from file or kafka stream
   * 
   * @param env
   * @param streamSource
   * @param filePathOrTopicProperty the data file path or the topic name of the input kafka stream
   * @param parsingConfig
   * @param areas
   * @return
   */
  public static DataStream<AisMessage> getAISMessagesStream(StreamExecutionEnvironment env,
      StreamSourceType streamSource, String filePathOrTopicProperty, String parsingConfig,
      String outputLineDelimiter) {
    DataStream<AisMessage> aisMessagesStream = null;
    String fileOrTopicName = configs.getStringProp(filePathOrTopicProperty);
    switch (streamSource) {
      case KAFKA:
        Properties kafakaProps = getKafkaConsumerProperties();
        // create a Kafka consumer
        FlinkKafkaConsumer010<AisMessage> kafkaConsumer =
            new FlinkKafkaConsumer010<AisMessage>(fileOrTopicName, new AisMessageCsvSchema(
                parsingConfig, outputLineDelimiter), kafakaProps);

        kafkaConsumer.assignTimestampsAndWatermarks(new AISMessagesTimeAssigner());
        aisMessagesStream = env.addSource(kafkaConsumer);
        break;
      case FILE:

        DataStream<AisMessage> aisMessagesStreamWithoutTime =
            env.addSource(new FileLinesStreamSource(fileOrTopicName, parsingConfig))
                .flatMap(new CsvLineToAisMessageMapper(parsingConfig)).setParallelism(1);

        // Assign the timestamp of the AIS messages based on their timestamps
        aisMessagesStream =
            aisMessagesStreamWithoutTime
                .assignTimestampsAndWatermarks(new AISMessagesTimeAssigner());

        break;

      case HDFS:
        aisMessagesStream =
            env.readTextFile(fileOrTopicName).flatMap(new CsvLineToAisMessageMapper(parsingConfig))
                .assignTimestampsAndWatermarks(new AISMessagesTimeAssigner());
        break;
      default:
        return null;
    }
    return aisMessagesStream;
  }

  public static Properties getKafkaConsumerProperties() {
    Properties kafkaProps = new Properties();
    kafkaProps.setProperty("zookeeper.connect", configs.getStringProp("zookeeper"));
    kafkaProps.setProperty("bootstrap.servers", configs.getStringProp("bootstrapServers"));
    kafkaProps.setProperty("group.id", configs.getStringProp("kafkaGroupId"));
    // always read the Kafka topic from the start
    kafkaProps.setProperty("auto.offset.reset", "earliest");
    return kafkaProps;
  }

  public static Properties getKafkaProducerProperties() {
    Properties props = new Properties();
    props.put("bootstrap.servers", configs.getStringProp("bootstrapServers"));
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    return props;
  }


  public static String getParsingJsonConfig() {

    String schemaFileName = configs.getStringProp("inputDataSchema");
    return readResourceFile(schemaFileName);

  }

  /**
   * Read a resource file
   * 
   * @param fileName
   * @return
   */
  private static String readResourceFile(String fileName) {
    InputStream input;
    input = Configs.class.getResourceAsStream("/" + fileName);

    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    StringBuilder out = new StringBuilder();
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        out.append(line);
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
    return out.toString();
  }

  /**
   * Get the app version based on the maven artifact version
   * 
   * @return
   */
  public static String getAppVersion() {

    String appVersion = AppUtils.class.getPackage().getImplementationVersion();
    return appVersion == null ? "development" : appVersion;
  }
}
