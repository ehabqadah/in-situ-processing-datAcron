package eu.datacron.in_situ_processing.statistics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.flink.api.common.ExecutionMode;

// import com.google.common.collect.Lists;
import eu.datacron.in_situ_processing.maritime.AisMessage;
import eu.datacron.in_situ_processing.maritime.PositionMessage;

/**
 * @author ehab.qadah
 */
public class AisTrajectoryStatistics extends StatisticsWrapper<AisMessage> {

  private static final long serialVersionUID = -4223639731431853133L;

  public AisTrajectoryStatistics() {
    setNumberOfPoints(0);
  }

  @Override
  public List<AisMessage> processNewPosition(AisMessage aisMessage) throws Exception {

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

    // System.out.println(aisMessage);
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
    double aggreagtedSpeedSum = oldAverageSpeed * getNumberOfPoints() + speed;
    double pointsCount = getNumberOfPoints() + 1.0;
    // compute new average
    double newAverageSpeed = aggreagtedSpeedSum / pointsCount;
    setAverageSpeed(newAverageSpeed);
    // compute the speed variance
    this.getPrevSpeeds().add(speed);
    double sumOfSquareMeanDiff = 0.0;
    for (double prevSpeed : this.getPrevSpeeds()) {
      sumOfSquareMeanDiff += Math.pow(prevSpeed - newAverageSpeed, 2);
    }
    // set variance of speed
    setVarianceSpeed(sumOfSquareMeanDiff / pointsCount);
  }

  private void updateTimeAttributes(AisMessage aisMessage) throws Exception {
    long newTimestamp = aisMessage.getTimestamp();
    long oldTimestamp = getLastTimestamp();
    long timeDiff = getNumberOfPoints() == 0 ? 0 : newTimestamp - oldTimestamp;

    if (timeDiff < 0) {
      System.out.println("**** error key=" + aisMessage.getId() + "newTimestamp" + newTimestamp
          + "timeDiff=" + timeDiff);
      throw new Exception("**** error key=" + aisMessage.getId() + "newTimestamp" + newTimestamp
          + "timeDiff=" + timeDiff);
    }

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

    setMaxHeading(aisMessage.getHeading());
    setMinHeading(aisMessage.getHeading());

    setMaxTurn(aisMessage.getTurn());
    setMinTurn(aisMessage.getTurn());
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

    return getNumberOfPoints() + delimiter + getAverageDiffTime() + delimiter + getLastDiffTime()
        + delimiter + getMinDiffTime() + delimiter + getMaxDiffTime() + delimiter + getMaxSpeed()
        + delimiter + getMinSpeed() + delimiter + getAverageSpeed() + delimiter
        + getVarianceSpeed() + delimiter + getMinLong() + delimiter + getMaxLong() + delimiter
        + getMinLat() + delimiter + getMaxLat() + delimiter + getMinturn() + delimiter
        + getMaxturn() + delimiter + getMinHeading() + delimiter + getMaxHeading();
  }

}
