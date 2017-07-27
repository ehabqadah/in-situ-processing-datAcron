package eu.datacron.in_situ_processing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.log4j.Logger;

import eu.datacron.in_situ_processing.common.utils.Configs;
import eu.datacron.in_situ_processing.flink.utils.FileLinesStreamSource;
import eu.datacron.in_situ_processing.maritime.AisMessage;
import eu.datacron.in_situ_processing.maritime.AisMessageCsvSchema;
import eu.datacron.in_situ_processing.maritime.streams.operators.CsvLineToAisMessageMapper;



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
   * @return
   */
  public static DataStream<AisMessage> getAISMessagesStream(StreamExecutionEnvironment env,
      StreamSourceType streamSource, String filePathOrTopicProperty, String parsingConfig,
      String outputLineDelimiter) {
    DataStream<AisMessage> aisMessagesStream = null;
    switch (streamSource) {
      case KAFKA:
        Properties kafakaProps = getKafkaConsumerProperties();
        // create a Kafka consumer
        FlinkKafkaConsumer010<AisMessage> kafkaConsumer =
            new FlinkKafkaConsumer010<AisMessage>(configs.getStringProp(filePathOrTopicProperty),
                new AisMessageCsvSchema(parsingConfig, outputLineDelimiter), kafakaProps);
        
        // kafkaConsumer.assignTimestampsAndWatermarks(arg0)
        aisMessagesStream = env.addSource(kafkaConsumer);
        break;
      case FILE:

        aisMessagesStream =
            env.addSource(
                new FileLinesStreamSource(configs.getStringProp(filePathOrTopicProperty),
                    parsingConfig))
                .flatMap(new CsvLineToAisMessageMapper(parsingConfig)).setParallelism(1);
        // env.readTextFile(configs.getStringProp(filePathOrTopicProperty)).flatMap(
        // new CSVLineToAISMessageMapper(parsingConfig));

        break;
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
    InputStream input = null;
    String schemaFileName = configs.getStringProp("inputDataSchema");
    input = Configs.class.getResourceAsStream("/" + schemaFileName);

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

    return AppUtils.class.getPackage().getImplementationVersion();
  }
}
