package eu.datacron.in_situ_processing.statistics;

import java.io.Serializable;

import eu.datacron.in_situ_processing.maritime.PositionMessage;

/**
 * @author ehab.qadah
 */
public abstract class StatisticsWrapper implements Serializable {

  private static final long serialVersionUID = -8266975301330743697L;
  private long numberOfPoints;


  public long getNumberOfPoints() {
    return numberOfPoints;
  }

  public void setNumberOfPoints(long numberOfPoints) {
    this.numberOfPoints = numberOfPoints;
  }

  public void increasePintsCount() {

    setNumberOfPoints(this.numberOfPoints + 1);
  }

  /**
   * Process new incoming position message to update the statisticss
   * 
   * @param positionMessage
   */
  public abstract void processNewPosition(PositionMessage positionMessage);
}
