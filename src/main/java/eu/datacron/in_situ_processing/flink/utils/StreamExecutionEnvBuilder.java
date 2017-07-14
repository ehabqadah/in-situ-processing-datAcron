package eu.datacron.in_situ_processing.flink.utils;

import org.apache.flink.api.common.ExecutionMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Flink Stream execution builder.
 * 
 * @author ehab.qadah
 *
 */
public class StreamExecutionEnvBuilder {

  private StreamExecutionEnvironment env;

  public StreamExecutionEnvBuilder() {
    // setup the environment with default values
    // set up streaming execution environment
    env = StreamExecutionEnvironment.getExecutionEnvironment();
    // configure event-time characteristics
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    
    // generate a Watermark every second
    setAutoWatermarkInterval(100);
    env.setBufferTimeout(100);
    env.getConfig().setExecutionMode(ExecutionMode.PIPELINED);
  }

  public StreamExecutionEnvBuilder setAutoWatermarkInterval(long interval) {

    env.getConfig().setAutoWatermarkInterval(interval);
    return this;
  }

  public StreamExecutionEnvBuilder setParallelism(int parallelism) {
    env.setParallelism(parallelism);
    return this;
  }

  public StreamExecutionEnvironment build() {
    return env;
  }
}
