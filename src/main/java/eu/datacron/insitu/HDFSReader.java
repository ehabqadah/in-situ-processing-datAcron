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
 * A Flink hdfs file reader
 * 
 * @author ehab.qadah
 *
 */
public class HDFSReader {

  private static Configs configs = Configs.getInstance();

  public static void main(String[] args) throws Exception {
    boolean writeOutputStreamToFile = configs.getBooleanProp("writeOutputStreamToFile");

    String cehkPointsPath =
        Paths.get(configs.getStringProp("flinkCheckPointsPath") + "/" + System.currentTimeMillis())
            .toUri().toString();


    int parallelism = configs.getIntProp("parallelism");

    // Set up the execution environment
    final StreamExecutionEnvironment env =
        new StreamExecutionEnvBuilder().setParallelism(parallelism).setStateBackend(cehkPointsPath)
            .build();

    DataStreamSource<String> inputTextStream =
        env.readTextFile("hdfs://dnode1:8020/user/ehabq/sorted_nari_dynamic.csv");

    FlinkKafkaProducer010Configuration<String> myProducerConfig =
        FlinkKafkaProducer010.writeToKafkaWithTimestamps(inputTextStream, "nari-hdfs",
            new SimpleStringSchema(), AppUtils.getKafkaProducerProperties());


    myProducerConfig.setLogFailuresOnly(false);
    myProducerConfig.setFlushOnCheckpoint(true);


    System.out.println(env.getExecutionPlan());

    JobExecutionResult executionResult = null;
    try {
      executionResult = env.execute(" HDFS test");
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    System.out.println("Full execution time=" + executionResult.getNetRuntime(TimeUnit.MINUTES));
  }

}
