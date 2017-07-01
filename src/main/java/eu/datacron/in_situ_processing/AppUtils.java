package eu.datacron.in_situ_processing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.apache.flink.streaming.util.serialization.SerializationSchema;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import eu.datacron.in_situ_processing.common.utils.Configs;
import eu.datacron.in_situ_processing.flink.utils.FileLinesStreamSource;
import eu.datacron.in_situ_processing.maritime.beans.AISMessage;
import eu.datacron.in_situ_processing.maritime.beans.AISMessageCSVSchema;



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
  public static DataStream<AISMessage> getAISMessagesStream(StreamExecutionEnvironment env,
      StreamSourceType streamSource, String filePathOrTopicProperty, String parsingConfig) {
    DataStream<AISMessage> aisMessagesStream = null;
    switch (streamSource) {
      case KAFKA:
        Properties kafakaProps = getKafkaConsumerProperties();
        // create a Kafka consumer
        FlinkKafkaConsumer010<AISMessage> kafkaConsumer =
            new FlinkKafkaConsumer010<AISMessage>(configs.getStringProp(filePathOrTopicProperty),
                new AISMessageCSVSchema(parsingConfig), kafakaProps);
        // kafkaConsumer.assignTimestampsAndWatermarks(arg0)
        aisMessagesStream = env.addSource(kafkaConsumer);
        break;
      case FILE:
        aisMessagesStream =
            env.addSource(new FileLinesStreamSource(configs.getStringProp(filePathOrTopicProperty)))
                .setParallelism(1).map(new CSVLineToAISMessageMapper(parsingConfig));

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

  public static final class CSVLineToAISMessageMapper implements MapFunction<String, AISMessage> {

    private static final long serialVersionUID = -7969686242238108964L;
    DeserializationSchema<AISMessage> deserializationSchema;

    public CSVLineToAISMessageMapper() {}

    // map a csv line to AISMessage using the AIS messages deserialization schema
    public CSVLineToAISMessageMapper(String parsingJsonConfigs) {
      deserializationSchema = new AISMessageCSVSchema(parsingJsonConfigs);
    }

    @Override
    public AISMessage map(String value) throws Exception {
      return deserializationSchema.deserialize(value.getBytes(StandardCharsets.UTF_8));

    }
  }

}
