package eu.datacron.insitu;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010.FlinkKafkaProducer010Configuration;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import eu.datacron.insitu.common.utils.Configs;
import eu.datacron.insitu.flink.utils.StreamExecutionEnvBuilder;



/**
 * A Flink hdfs to kafka writer
 * 
 * @author ehab.qadah
 *
 */
public class HdfsToKafkaProducer {

  private static Configs configs = Configs.getInstance();

  /**
   * The main entry method
   * 
   */
  public static void main(String[] args) throws Exception {

    String cehkPointsPath =
        Paths.get(configs.getStringProp("flinkCheckPointsPath") + "/" + System.currentTimeMillis())
            .toUri().toString();


    int parallelism = configs.getIntProp("parallelism");
    String inputHdfsFile = configs.getStringProp("inputHDFSFilePath");
    String outputTopicName = configs.getStringProp("outputHDFSKafkaTopic");

    // Set up the execution environment
    final StreamExecutionEnvironment env =
        new StreamExecutionEnvBuilder().setParallelism(parallelism).setStateBackend(cehkPointsPath)
            .build();
    // Read the HDFS file
    DataStreamSource<String> inputTextStream =
        env.readTextFile(inputHdfsFile).setParallelism(parallelism);

    FlinkKafkaProducer010Configuration<String> myProducerConfig =
        FlinkKafkaProducer010.writeToKafkaWithTimestamps(inputTextStream, outputTopicName,
            new SimpleStringSchema(), AppUtils.getKafkaProducerProperties());


    myProducerConfig.setLogFailuresOnly(false);
    myProducerConfig.setFlushOnCheckpoint(true);


    System.out.println(env.getExecutionPlan());

    JobExecutionResult executionResult = null;

    try {
      executionResult = env.execute(" HDFS to Kafka stream producer");
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    System.out.println("Full execution time=" + executionResult.getNetRuntime(TimeUnit.MINUTES));
  }
}
