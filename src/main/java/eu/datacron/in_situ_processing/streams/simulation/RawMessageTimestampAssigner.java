package eu.datacron.in_situ_processing.streams.simulation;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;

/**
 * This a time assigner for the records of type Tuple3<id, timestamp, rawData>.
 * 
 * @author ehab.qadah
 */
public final class RawMessageTimestampAssigner extends
    AscendingTimestampExtractor<Tuple3<String, Long, String>> {
  private static final long serialVersionUID = -3203754108824557827L;

  @Override
  public long extractAscendingTimestamp(Tuple3<String, Long, String> element) {
    // use the timestamp of the raw message
    return element.f1;
  }
}
