package eu.datacron.insitu.maritime.statistics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eu.datacron.insitu.maritime.AisMessage;

/**
 * A class to aggregate the AIS messages statistics
 * 
 * @author ehab.qadah
 */
public class AisTrajectoryStatistics extends AbstractStatisticsWrapper<AisMessage> {

  private static final int MIN_NUMBER_OF_PPINTS_BEFORE_COMPUTE_VARIANCE = 2;
  private static final long serialVersionUID = -4223639731431853133L;
  private double onlineSpeedMean;
  private double aggregatedSumOfSquaresMeanDiffs;

  public AisTrajectoryStatistics() {
    setNumberOfPoints(0);
  }

  @Override
  public AisMessage processNewPosition(AisMessage aisMessage) throws Exception {

    processNewAisMessage(aisMessage);

    return aisMessage;
  }

  public double getOnlineSpeedMean() {
    return onlineSpeedMean;
  }

  public void setOnlineSpeedMean(double onlineSpeedMean) {
    this.onlineSpeedMean = onlineSpeedMean;
  }

  public double getAggregatedSumOfSquaresMeanDiffs() {
    return aggregatedSumOfSquaresMeanDiffs;
  }

  public void setAggregatedSumOfSquaresMeanDiffs(double aggregatedSumOfSquaresMeanDiffs) {
    this.aggregatedSumOfSquaresMeanDiffs = aggregatedSumOfSquaresMeanDiffs;
  }

  public List<AisMessage> processNewPositionList(AisMessage aisMessage) throws Exception {

    List<AisMessage> unorderedList = new ArrayList<AisMessage>();
    List<AisMessage> result = new ArrayList<AisMessage>();

    List<AisMessage> prevAisMessages = aisMessage.prevAisMessages;

    if (prevAisMessages != null) {
      unorderedList.addAll(prevAisMessages);
    }

    unorderedList.add(aisMessage);
    Iterator<AisMessage> sortedMessagesIterator = unorderedList.stream().sorted((msg1, msg2) -> {

      return msg1.getTimestamp().compareTo(msg2.getTimestamp());
    }).iterator();

    while (sortedMessagesIterator.hasNext()) {
      AisMessage next = sortedMessagesIterator.next();
      processNewAisMessage(next);
      result.add(next);
    }
    return result;
  }

  private void processNewAisMessage(AisMessage aisMessage) throws Exception {


    if (getNumberOfPoints() == 0) {
      initStatisticsAttributes(aisMessage);
    }
    // Update location related attributes
    updateLocationAttributes(aisMessage);
    // Update time related attributes
    updateTimeAttributes(aisMessage);
    updateSpeedAttributes(aisMessage);
    increasePointssCount();

  }

  /**
   * Initialize the derived attributes for the first received message
   * 
   * @param aisMessage
   */
  private void initStatisticsAttributes(AisMessage aisMessage) {
    this.minLat = aisMessage.getLatitude();
    this.maxLat = aisMessage.getLatitude();

    this.minLong = aisMessage.getLongitude();
    this.maxLong = aisMessage.getLongitude();

    this.minSpeed = aisMessage.getSpeed();
    this.maxSpeed = aisMessage.getSpeed();

    this.minTurn = aisMessage.getTurn();
    this.maxTurn = aisMessage.getTurn();

    this.minHeading = aisMessage.getHeading();
    this.maxHeading = aisMessage.getHeading();

    this.lastTimestamp = aisMessage.getTimestamp();

  }

  private void updateSpeedAttributes(AisMessage aisMessage) {
    Double speed = aisMessage.getSpeed();
    setMinSpeed(speed);
    setMaxSpeed(speed);
    double oldAverageSpeed = getAverageSpeed();
    double aggregatedSpeedSum = oldAverageSpeed * getNumberOfPoints() + speed;
    double pointsCount = getNumberOfPoints() + 1.0;
    // compute new average and variance of the speed
    double newAverageSpeed = aggregatedSpeedSum / pointsCount;
    setAverageSpeed(newAverageSpeed);
    computeOnlineVarianceOfSpeed(speed);
  }

  /**
   * Compute the online variance of the speed based on {B. P. Welford
   * (1962)."Note on a method for calculating corrected sums of squares and products"}
   * 
   * @see https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
   * @param newSpeed
   */
  void computeOnlineVarianceOfSpeed(double newSpeed) {

    long n = getNumberOfPoints();
    if (n == 0) {
      setOnlineSpeedMean(0.0);
      setAggregatedSumOfSquaresMeanDiffs(0.0);
    }
    n += 1;
    // new speed -old mean
    double delta = newSpeed - getOnlineSpeedMean();
    // new sample mean
    setOnlineSpeedMean(getOnlineSpeedMean() + delta / n);
    // new speed -new mean
    double delta2 = newSpeed - getOnlineSpeedMean();
    // update the sum of squares differences of speeds from mean
    setAggregatedSumOfSquaresMeanDiffs(getAggregatedSumOfSquaresMeanDiffs() + delta * delta2);

    if (n < MIN_NUMBER_OF_PPINTS_BEFORE_COMPUTE_VARIANCE) {
      setVarianceSpeed(0.0);
    } else {
      double variance = getAggregatedSumOfSquaresMeanDiffs() / (n - 1);
      setVarianceSpeed(variance);
    }
  }

  private void updateTimeAttributes(AisMessage aisMessage) throws Exception {
    long newTimestamp = aisMessage.getTimestamp();
    long oldTimestamp = getLastTimestamp();
    long timeDiff = getNumberOfPoints() == 0 ? 0 : newTimestamp - oldTimestamp;

    checkForMessagesOrder(aisMessage, newTimestamp, timeDiff);

    setLastDiffTime(timeDiff);
    setMaxDiffTime(timeDiff);
    // no update the min for the first message
    if (getNumberOfPoints() > 0) {
      setMinDiffTime(timeDiff);
    }
    // Set the message's timestamp as the last seen timestamp
    setLastTimestamp(newTimestamp);

    // update time difference average
    double aggreagtedTimediffSum = getAverageDiffTime() * getNumberOfPoints() + timeDiff;
    double pointsCount = getNumberOfPoints() + 1.0;
    // compute new average
    double newAverageTimeDiff = aggreagtedTimediffSum / pointsCount;
    setAverageDiffTime(newAverageTimeDiff);
  }

  private void checkForMessagesOrder(AisMessage aisMessage, long newTimestamp, long timeDiff)
      throws Exception {
    if (timeDiff < 0) {
      String outOfOrderErrorMesage =
          "**** error key=" + aisMessage.getId() + "newTimestamp" + newTimestamp + "timeDiff="
              + timeDiff;
      System.out.println(outOfOrderErrorMesage);
      // throw new Exception(outOfOrderErrorMesage);
    }
  }

  private void updateLocationAttributes(AisMessage aisMessage) {
    setMaxLat(aisMessage.getLatitude());
    setMinLat(aisMessage.getLatitude());
    setMaxLong(aisMessage.getLongitude());
    setMinLong(aisMessage.getLongitude());
    setMaxHeading(aisMessage.getHeading());
    setMinHeading(aisMessage.getHeading());
    setMaxTurn(aisMessage.getTurn());
    setMinTurn(aisMessage.getTurn());
  }

  @Override
  public String toString() {
    return "AisTrajectoryStatistics [onlineSpeedMean=" + getOnlineSpeedMean()
        + ", aggregatedSumOfSquaresMeanDiffs=" + getAggregatedSumOfSquaresMeanDiffs()
        + ", numberOfPoints=" + numberOfPoints + ", lastTimestamp=" + lastTimestamp
        + ", lastDifftime=" + lastDifftime + ", minSpeed=" + minSpeed + ", maxSpeed=" + maxSpeed
        + ", minDiffTime=" + minDiffTime + ", maxDiffTime=" + maxDiffTime + ", minHeading="
        + minHeading + ", minTurn=" + minTurn + ", minLong=" + minLong + ", maxLong=" + maxLong
        + ", minLat=" + minLat + ", maxLat=" + maxLat + ", maxHeading=" + maxHeading + ", maxTurn="
        + maxTurn + ", averageSpeed=" + averageSpeed + ", averageDiffTime=" + averageDiffTime
        + ", varianceSpeed=" + varianceSpeed + "]";
  }

  @Override
  public String toCsv(String delimiter) {

    return getNumberOfPoints() + delimiter + getAverageDiffTime() + delimiter + getLastDiffTime()
        + delimiter + getMinDiffTime() + delimiter + getMaxDiffTime() + delimiter + getMaxSpeed()
        + delimiter + getMinSpeed() + delimiter + getAverageSpeed() + delimiter
        + getVarianceSpeed() + delimiter + getMinLong() + delimiter + getMaxLong() + delimiter
        + getMinLat() + delimiter + getMaxLat() + delimiter + getMinturn() + delimiter
        + getMaxturn() + delimiter + getMinHeading() + delimiter + getMaxHeading() + delimiter
        + isChangeInArea() + delimiter + getDetectedAreasStr();
  }


}
