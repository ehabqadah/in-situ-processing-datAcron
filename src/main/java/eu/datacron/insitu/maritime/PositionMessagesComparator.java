package eu.datacron.insitu.maritime;

import java.util.Comparator;

/**
 * A comparator of position messages based on their timestamp
 * 
 * @author ehab.qadah
 */
public class PositionMessagesComparator implements Comparator<PositionMessage> {

  @Override
  public int compare(PositionMessage a, PositionMessage b) {
    if (a.getTimestamp() > b.getTimestamp()) {
      return 1;
    }
    if (a.getTimestamp() == b.getTimestamp()) {
      return 0;
    }
    return -1;
  }
}
