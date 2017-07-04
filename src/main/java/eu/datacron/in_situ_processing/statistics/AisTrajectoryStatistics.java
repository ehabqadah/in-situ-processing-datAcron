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

      if (getNumberOfPoints() == 0) {
        initFirstMessageAttributes(aisMessage);
      }
      // update location related attributes
      updateLocationAttributes(aisMessage);
      // update time related attributes
      updateTimeAttributes(aisMessage);
      updateSpeedAttributes(aisMessage);
      increasePointssCount();
    }
  }

  /**
   * Initialize the derived attributes for the first received message
   * 
   * @param aisMessage
   */
  private void initFirstMessageAttributes(AisMessage aisMessage) {
    this.minLat = aisMessage.getLatitude();
    this.maxLat = aisMessage.getLatitude();

    this.minLong = aisMessage.getLongitude();
    this.maxLong = aisMessage.getLongitude();

    this.minSpeed = aisMessage.getSpeed();
    this.maxSpeed = aisMessage.getSpeed();

    this.lastTimestamp = aisMessage.getTimestamp();

  }

  private void updateSpeedAttributes(AisMessage aisMessage) {
    Double speed = aisMessage.getSpeed();
    setMinSpeed(speed);
    setMaxSpeed(speed);
    double oldAverageSpeed = getAverageSpeed();
    double aggreagtedSpeedSum = oldAverageSpeed * getNumberOfPoints() + speed;
    double pointsCount = getNumberOfPoints() + 1.0;
    // compute new average
    double newAverageSpeed = aggreagtedSpeedSum / pointsCount;
    setAverageSpeed(newAverageSpeed);
    // compute the speed variance
    this.prevSpeeds.add(speed);
    double sumOfSquareMeanDiff = 0.0;
    for (double prevSpeed : this.prevSpeeds) {
      sumOfSquareMeanDiff += Math.pow(prevSpeed - newAverageSpeed, 2);
    }
    // set variance of speed
    setVarianceSpeed(sumOfSquareMeanDiff / pointsCount);
  }

  private void updateTimeAttributes(AisMessage aisMessage) {
    long newTimestamp = aisMessage.getTimestamp();
    long oldTimestamp = getLastTimestamp();
    long timeDiff = getNumberOfPoints() == 0 ? 0 : newTimestamp - oldTimestamp;
    setLastDiffTime(timeDiff);
    setMaxDiffTime(timeDiff);
    // don't update the min for the first message
    if (getNumberOfPoints() > 0) {
      setMinDiffTime(timeDiff);
    }
    // set the message's timestamp as the last seen timestamp
    setLastTimestamp(newTimestamp);

    // update time difference average
    double aggreagtedTimediffSum = getAverageDiffTime() * getNumberOfPoints() + timeDiff;
    double pointsCount = getNumberOfPoints() + 1.0;
    // compute new average
    double newAverageTimeDiff = aggreagtedTimediffSum / pointsCount;
    setAverageDiffTime(newAverageTimeDiff);
  }

  private void updateLocationAttributes(AisMessage aisMessage) {
    setMaxLat(aisMessage.getLatitude());
    setMinLat(aisMessage.getLatitude());
    setMaxLong(aisMessage.getLongitude());
    setMinLong(aisMessage.getLongitude());
  }


  @Override
  public String toString() {
    return " \n AisTrajectoryStatistics [getAverageDiffTime()=" + getAverageDiffTime()
        + ", getNumberOfPoints()=" + getNumberOfPoints() + ", getLastTimestamp()="
        + getLastTimestamp() + ", getLastDiffTime()=" + getLastDiffTime() + ", getMinSpeed()="
        + getMinSpeed() + ", getMinDiffTime()=" + getMinDiffTime() + ", getMaxSpeed()="
        + getMaxSpeed() + ", getMaxDiffTime()=" + getMaxDiffTime() + ", getMinLong()="
        + getMinLong() + ", getMaxLong()=" + getMaxLong() + ", getMinLat()=" + getMinLat()
        + ", getMaxLat()=" + getMaxLat() + ", getLastDifftime()=" + getLastDifftime()
        + ", getAverageSpeed()=" + getAverageSpeed() + ", getVarianceSpeed()=" + getVarianceSpeed()
        + "] \n";
  }

  @Override
  public String toCsv(String delimiter) {

    return getAverageDiffTime() + delimiter + getNumberOfPoints() + delimiter + getLastTimestamp()
        + delimiter + getLastDiffTime() + delimiter + getMinSpeed() + delimiter + getMinDiffTime()
        + delimiter + getMaxSpeed() + delimiter + getMaxDiffTime() + delimiter + getMinLong()
        + delimiter + getMaxLong() + delimiter + getMinLat() + delimiter + getMaxLat() + delimiter
        + getLastDifftime() + delimiter + getAverageSpeed() + delimiter + getVarianceSpeed();
  }

}
