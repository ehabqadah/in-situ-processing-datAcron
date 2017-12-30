package eu.datacron.insitu.maritime.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;

/**
 * @author ehab.qadah
 */
public abstract class AbstractStatisticsWrapper<T> implements Serializable {

  private static final String AREAS_DELIMITER = ";";
  private static final long serialVersionUID = -8266975301330743697L;
  protected long numberOfPoints = 0;
  protected long lastTimestamp;

  protected long lastDifftime;
  /** min/max values of attributes **/
  protected double minSpeed;
  protected double maxSpeed;
  // min diff time can not be set as zero initially
  protected long minDiffTime = Long.MAX_VALUE;
  protected long maxDiffTime;
  protected int minHeading;
  protected double minTurn;

  protected double minLong;
  protected double maxLong;
  protected double minLat;
  protected double maxLat;
  protected int maxHeading;
  protected double maxTurn;
  private Set<String> detectedAreas;
  private boolean changeInArea;

  private List<Double> prevSpeeds = new ArrayList<Double>();

  /** Average of attributes **/
  public double averageSpeed;
  public double averageDiffTime;

  /** Variance of attributes **/
  public double varianceSpeed;

  public double getAverageDiffTime() {
    return averageDiffTime;
  }

  public void setAverageDiffTime(double averageDiffTime) {
    this.averageDiffTime = averageDiffTime;
  }

  public long getNumberOfPoints() {
    return numberOfPoints;
  }

  public void setNumberOfPoints(long numberOfPoints) {
    this.numberOfPoints = numberOfPoints;
  }

  public long getLastTimestamp() {
    return lastTimestamp;
  }

  public void setLastTimestamp(long lastTimestamp) {
    this.lastTimestamp = lastTimestamp;
  }

  public void increasePointssCount() {

    setNumberOfPoints(this.numberOfPoints + 1);
  }


  public long getLastDiffTime() {
    return lastDifftime;
  }

  public void setLastDiffTime(long lastDifftime) {
    this.lastDifftime = lastDifftime;
  }

  public double getMinSpeed() {
    return minSpeed;
  }

  public long getMinDiffTime() {
    return minDiffTime;
  }

  public double getMaxSpeed() {
    return maxSpeed;
  }

  public long getMaxDiffTime() {
    return maxDiffTime;
  }

  public double getMinLong() {
    return minLong;
  }

  public double getMaxLong() {
    return maxLong;
  }

  public double getMinLat() {
    return minLat;
  }

  public double getMaxLat() {
    return maxLat;
  }

  public double getVarianceSpeed() {
    return varianceSpeed;
  }

  public void setVarianceSpeed(double varianceSpeed) {
    this.varianceSpeed = varianceSpeed;
  }

  public void setMinSpeed(double minSpeed) {
    this.minSpeed = Math.min(this.minSpeed, minSpeed);
  }

  public void setMaxSpeed(double maxSpeed) {
    this.maxSpeed = Math.max(this.maxSpeed, maxSpeed);
  }

  public void setMinDiffTime(long minDifftime) {
    this.minDiffTime = Math.min(this.minDiffTime, minDifftime);
  }

  public void setMaxDiffTime(long maxDifftime) {
    this.maxDiffTime = Math.max(this.maxDiffTime, maxDifftime);
  }

  public void setMinLong(double minLong) {
    this.minLong = Math.min(this.minLong, minLong);;
  }

  public void setMaxLong(double maxLong) {
    this.maxLong = Math.max(this.maxLong, maxLong);
  }

  public void setMinLat(double minLat) {
    this.minLat = Math.min(this.minLat, minLat);
  }

  public void setMaxLat(double maxLat) {
    this.maxLat = Math.max(this.maxLat, maxLat);
  }

  public void setMinHeading(int minHeading) {
    this.minHeading = Math.min(this.minHeading, minHeading);
  }

  public void setMinTurn(double minturn) {
    this.minTurn = Math.min(this.minTurn, minturn);
  }

  public void setMaxHeading(int maxHeading) {
    this.maxHeading = Math.max(this.maxHeading, maxHeading);
  }

  public void setMaxTurn(double maxturn) {
    this.maxTurn = Math.max(this.maxTurn, maxturn);
  }

  public void setLastDifftime(long lastDifftime) {
    this.lastDifftime = lastDifftime;
  }

  public void setAverageSpeed(double averageSpeed) {
    this.averageSpeed = averageSpeed;
  }

  public int getMinHeading() {
    return minHeading;
  }

  public double getMinturn() {
    return minTurn;
  }

  public int getMaxHeading() {
    return maxHeading;
  }

  public double getMaxturn() {
    return maxTurn;
  }

  public long getLastDifftime() {
    return lastDifftime;
  }

  public double getAverageSpeed() {
    return averageSpeed;
  }

  /**
   * Process new incoming position message to update the statistics
   * 
   * @param positionMessage
   * @throws Exception
   */
  public abstract T processNewPosition(T positionMessage) throws Exception;

  public abstract String toCsv(String delimiter);

  public List<Double> getPrevSpeeds() {
    return prevSpeeds;
  }

  public void setPrevSpeeds(List<Double> prevSpeeds) {
    this.prevSpeeds = prevSpeeds;
  }

  public Set<String> getDetectedAreas() {
    return detectedAreas;
  }

  public void setDetectedAreas(Set<String> detectedAreas) {
    this.detectedAreas = detectedAreas;
  }

  public boolean isChangeInArea() {
    return changeInArea;
  }

  public void setChangeInArea(boolean changeInArea) {
    this.changeInArea = changeInArea;
  }

  public String getDetectedAreasStr() {
    if (detectedAreas == null) {
      return "";
    }

    Optional<String> joinedString = detectedAreas.stream().reduce(new BinaryOperator<String>() {

      @Override
      public String apply(String t, String u) {

        return t + AREAS_DELIMITER + u;
      }
    });
    return joinedString.orElse("");
  }
}
