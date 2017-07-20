package eu.datacron.in_situ_processing.maritime.streams.operators;

import java.util.Comparator;

import eu.datacron.in_situ_processing.maritime.AisMessage;

/**
 * @author ehab.qadah
 */
public class CompareByTimestampAscending implements Comparator<AisMessage> {

  @Override
  public int compare(AisMessage a, AisMessage b) {
    if (a.timestamp > b.timestamp) {
      return 1;
    }
    if (a.timestamp == b.timestamp) {
      return 0;
    }
    return -1;
  }
}
