package eu.datacron.in_situ_processing.streams.simulation;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.watermark.Watermark;

/**
 * This a time assigner for the records of type Tuple3<id, timestamp, rawData>.
 * 
 * @author ehab.qadah
 */
public final class RawMessageTimestampAssigner implements
AssignerWithPunctuatedWatermarks<Tuple3<String, Long, String>> {
  private static final long serialVersionUID = -3203754108824557827L;

  private static final int OUT_OF_ORDER_ALLOWNES = 60000;
  @Override
  public long extractTimestamp(Tuple3<String, Long, String> element, long previousElementTimestamp) {
    
     return element.f1;
  }

  @Override
  public Watermark checkAndGetNextWatermark(Tuple3<String, Long, String> lastElement,
      long extractedTimestamp) {
 // simply emit a watermark with every event
    return new Watermark(extractedTimestamp - OUT_OF_ORDER_ALLOWNES);
  }

 
}
