package eu.datacron.in_situ_processing.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.datacron.in_situ_processing.maritime.PositionMessage;

/**
 * @author ehab.qadah
 */
public abstract class StatisticsWrapper<T> implements Serializable {

  private static final long serialVersionUID = -8266975301330743697L;
  protected long numberOfPoints = 0;
  protected long lastTimestamp;

  protected long lastDifftime;
  /** min/max values of attributes **/
  protected double minSpeed;
  protected double maxSpeed;
  protected long minDiffTime = Long.MAX_VALUE;// min diff time can not be set as zero initially
  protected long maxDiffTime;
  protected double minLong;
  protected double maxLong;
  protected double minLat;
  protected double maxLat;
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

  public long getLastDifftime() {
    return lastDifftime;
  }

  public void setLastDifftime(long lastDifftime) {
    this.lastDifftime = lastDifftime;
  }

  public double getAverageSpeed() {
    return averageSpeed;
  }

  public void setAverageSpeed(double averageSpeed) {
    this.averageSpeed = averageSpeed;
  }

  /**
   * Process new incoming position message to update the statisticss
   * 
   * @param positionMessage
   * @throws Exception 
   */
  public abstract List<T> processNewPosition(T positionMessage) throws Exception;

  public abstract String toCsv(String delimiter);

  public List<Double> getPrevSpeeds() {
    return prevSpeeds;
  }

  public void setPrevSpeeds(List<Double> prevSpeeds) {
    this.prevSpeeds = prevSpeeds;
  }
}
