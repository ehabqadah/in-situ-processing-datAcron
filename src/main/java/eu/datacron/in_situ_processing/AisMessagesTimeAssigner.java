package eu.datacron.in_situ_processing;

import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;

import eu.datacron.in_situ_processing.maritime.beans.AisMessage;

/**
 * This is a timestamp assigner of the AIS messages by using their timestamps.
 * 
 * @author ehab.qadah
 */
public final class AisMessagesTimeAssigner extends AscendingTimestampExtractor<AisMessage> {

  private static final long serialVersionUID = 8911057142849552886L;

  @Override
  public long extractAscendingTimestamp(AisMessage element) {
    // Use the timestamp of the message
    return element.getTimestamp();
  }
}
