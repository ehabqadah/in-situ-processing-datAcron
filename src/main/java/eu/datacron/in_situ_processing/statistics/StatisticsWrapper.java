package eu.datacron.in_situ_processing.statistics;

import java.io.Serializable;

import eu.datacron.in_situ_processing.maritime.PositionMessage;

/**
 * @author ehab.qadah
 */
public abstract class StatisticsWrapper implements Serializable {

  private static final long serialVersionUID = -8266975301330743697L;
  protected long numberOfPoints=0;
  protected long lastTimestamp;
 

  protected long lastDifftime;
  /** Initialize the min/max values of  attributes **/
  protected double minSpeed;
  protected double maxSpeed;
  protected long minDifftime=Long.MAX_VALUE;// min diff time can not be set as zero initially 
  protected long maxDifftime;
  protected double minLong;
  protected double maxLong;
  protected double minLat;
  protected double maxLat;


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

  
  public long getLastDifftime() {
    return lastDifftime;
  }

  public void setLastDifftime(long lastDifftime) {
    this.lastDifftime =lastDifftime;
  }

  public double getMinSpeed() {
    return minSpeed;
  }

  public long getMinDifftime() {
    return minDifftime;
  }

  public double getMaxSpeed() {
    return maxSpeed;
  }
  public long getMaxDifftime() {
    return maxDifftime;
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

  
  public void setMinSpeed(double minSpeed) {
    this.minSpeed = Math.min(this.minSpeed,minSpeed);
  }
  public void setMaxSpeed(double maxSpeed) {
    this.maxSpeed = Math.max(this.maxSpeed,maxSpeed);
  }

  public void setMinDifftime(long minDifftime) {
    this.minDifftime = Math.min(this.minDifftime,minDifftime);
  }

  public void setMaxDifftime(long maxDifftime) {
    this.maxDifftime = Math.max(this.maxDifftime,maxDifftime);
  }

  public void setMinLong(double minLong) {
    this.minLong = Math.min(this.minLong,minLong);;
  }

  public void setMaxLong(double maxLong) {
    this.maxLong = Math.max(this.maxLong,maxLong);
  }

  public void setMinLat(double minLat) {
    this.minLat =Math.min( this.minLat,minLat);
  }
  
  public void setMaxLat(double maxLat) {
    this.maxLat = Math.max(this.maxLat,maxLat);
  }
  /**
   * Process new incoming position message to update the statisticss
   * 
   * @param positionMessage
   */
  public abstract void processNewPosition(PositionMessage positionMessage);
  
  public abstract String toCsv();
}
