package eu.datacron.in_situ_processing.maritime.streams.operators;

import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

import eu.datacron.in_situ_processing.maritime.AisMessage;

/**
 * This is a timestamp assigner of the AIS messages by using their timestamps.
 * 
 * @author ehab.qadah
 */
public final class AISMessagesTimeAssigner implements AssignerWithPunctuatedWatermarks<AisMessage> {

  private static final int OUT_OF_ORDER_ALLOWANCE = 5;
  private static final long serialVersionUID = -8101115432189285146L;

  @Override
  public long extractTimestamp(AisMessage element, long previousElementTimestamp) {

    return element.getTimestamp();
  }

  @Override
  public Watermark checkAndGetNextWatermark(AisMessage lastElement, long extractedTimestamp) {
    // simply emit a watermark with every event
    return new Watermark(extractedTimestamp - OUT_OF_ORDER_ALLOWANCE);
  }
}
