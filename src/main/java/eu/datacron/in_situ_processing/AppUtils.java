package eu.datacron.in_situ_processing;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;

import eu.datacron.in_situ_processing.common.utils.Configs;
import eu.datacron.in_situ_processing.flink.utils.FileLinesStreamSource;
import eu.datacron.in_situ_processing.maritime.beans.AISMessage;
import eu.datacron.in_situ_processing.maritime.beans.AISMessageCSVSchema;



/**
 * @author ehab.qadah
 */
public class AppUtils {

  private static Configs configs = Configs.getInstance();

  /**
   * Get the AIS messages stream from file or kafka stream
   * 
   * @param env
   * @param streamSource
   * @param filePathOrTopicProperty the data file path or the topic name of the input kafka stream
   * @return
   */
  public static DataStream<AISMessage> getAISMessagesStream(StreamExecutionEnvironment env,
      StreamSourceType streamSource, String filePathOrTopicProperty) {
    DataStream<AISMessage> aisMessagesStream = null;
    switch (streamSource) {
      case KAFKA:
        Properties kafakaProps = getKafkaConsumerProperties();
        // create a Kafka consumer
        FlinkKafkaConsumer010<AISMessage> kafkaConsumer =
            new FlinkKafkaConsumer010<AISMessage>(configs.getStringProp(filePathOrTopicProperty),
                new AISMessageCSVSchema(), kafakaProps);
        // kafkaConsumer.assignTimestampsAndWatermarks(arg0)
        aisMessagesStream = env.addSource(kafkaConsumer);
        break;
      case FILE:
        aisMessagesStream =
            env.addSource(new FileLinesStreamSource(configs.getStringProp(filePathOrTopicProperty)))
                .setParallelism(1)
                .map(
                    value -> new AISMessageCSVSchema().deserialize(value
                        .getBytes(StandardCharsets.UTF_8)));
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
  };

}
