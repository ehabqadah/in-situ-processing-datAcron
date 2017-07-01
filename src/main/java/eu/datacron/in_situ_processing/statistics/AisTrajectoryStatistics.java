package eu.datacron.in_situ_processing.statistics;

import eu.datacron.in_situ_processing.maritime.AisMessage;
import eu.datacron.in_situ_processing.maritime.PositionMessage;

/**
 * @author ehab.qadah
 */
public class AisTrajectoryStatistics extends StatisticsWrapper {

  private static final long serialVersionUID = -4223639731431853133L;

  public AisTrajectoryStatistics() {
    setNumberOfPoints(0);
  }

  @Override
  public void processNewPosition(PositionMessage positionMessage) {
    if (positionMessage instanceof AisMessage) {
      AisMessage aisMessage = (AisMessage) positionMessage;
      //TODO: compute all others statistics
      increasePintsCount();
    }

  }

  @Override
  public String toString() {
    return "AisTrajectoryStatistics [numberOfPoints=" + getNumberOfPoints() + "]";
  }

  @Override
  public String toCsv() {
    return "" + getNumberOfPoints() + "";
  }


}
