package eu.datacron.in_situ_processing.flink.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.flink.api.common.ExecutionMode;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;

/**
 * Flink Stream execution builder.
 * 
 * @author ehab.qadah
 *
 */
public class StreamExecutionEnvBuilder {
  // number of jobs restart attempts
  private static final int DEFAULT_NUMBER_JOB_RESTART = 2;
  private StreamExecutionEnvironment env;

  public StreamExecutionEnvBuilder() throws IOException {
    // setup the environment with default values
    // set up streaming execution environment
    env = StreamExecutionEnvironment.getExecutionEnvironment();
    // configure event-time characteristics
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    // enable check pointing every 3 minutes
    env.enableCheckpointing(3 * 60 * 1000);
    // generate a Watermark every second
    setAutoWatermarkInterval(1000);
    env.setBufferTimeout(1000);
    env.getConfig().setExecutionMode(ExecutionMode.PIPELINED);
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(DEFAULT_NUMBER_JOB_RESTART,
        Time.of(10, TimeUnit.SECONDS) // delay
        ));
  }

  public StreamExecutionEnvBuilder setAutoWatermarkInterval(long interval) {

    env.getConfig().setAutoWatermarkInterval(interval);
    return this;
  }

  public StreamExecutionEnvBuilder setStateBackend(String path) throws IOException {

    env.setStateBackend(new FsStateBackend(path, true));
    return this;
  }

  public StreamExecutionEnvBuilder setParallelism(int parallelism) {
    env.setParallelism(parallelism);
    return this;
  }

  public StreamExecutionEnvBuilder disableJobsRestart() {
    env.setRestartStrategy(RestartStrategies.noRestart());
    return this;
  }

  public StreamExecutionEnvironment build() {
    return env;
  }

}
