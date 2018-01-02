package eu.datacron.insitu.maritime;

import java.util.ArrayList;
import java.util.List;

import eu.datacron.insitu.maritime.statistics.AbstractStatisticsWrapper;

/**
 * AIS (Automatic Identification System) message wrapper.
 * 
 * @author ehab.qadah
 *
 */
public class AisMessage implements PositionMessage {
  private static final long serialVersionUID = 7555537850826069540L;

  /** timestamp in UNIX epochs (i.e., milliseconds elapsed since 1970-01-01 00:00:00.000). */
  public long timestamp;
  /** A globally unique identifier for the moving object (usually, the MMSI of vessels). */
  public String id;
  /** Longitude coordinate in decimal degrees (georeference: WGS84) of this point location. */
  public double longitude;
  /** Latitude coordinate in decimal degrees (georeference: WGS84) of this point location. */
  public double latitude;
  /** Rate of turn, right or left, 0 to 720 degrees per minute. */
  public double turn;
  /** Speed over ground in knots (allowed values: 0-102.2 knots). */
  public double speed;
  /** True heading in degrees (0-359), relative to true north. */
  public int heading;
  /** Course over ground (allowed values: 0-359.9 degrees). */
  public double course;
  /** Navigational status. */
  public int status;

  /** Error Message Flag **/
  private String msgErrorFlag = "";
  /** The statistics of ais message trajectory */
  public AbstractStatisticsWrapper<AisMessage> trajectoryStatistics;

  public AisMessage prevAisMessage = null;
  public List<AisMessage> prevAisMessages = new ArrayList<AisMessage>();

  private String originalRawMessage;

  /**
   * Default constructor.
   */
  public AisMessage() {}

  /**
   * All-args constructor.
   * 
   * @param timestamp timestamp in UNIX epochs (i.e., milliseconds elapsed since 1970-01-01
   *        00:00:00.000).
   * @param id A globally unique identifier for the moving object (usually, the MMSI of vessels).
   * @param longitude Longitude coordinate in decimal degrees (georeference: WGS84) of this point
   *        location.
   * @param latitude Latitude coordinate in decimal degrees (georeference: WGS84) of this point
   *        location.
   * @param turn Rate of turn, right or left, 0 to 720 degrees per minute.
   * @param speed Speed over ground in knots (allowed values: 0-102.2 knots).
   * @param heading True heading in degrees (0-359), relative to true north.
   * @param course Course over ground (allowed values: 0-359.9 degrees).
   * @param status Navigational status.
   */
  public AisMessage(Long timestamp, String id, Double longitude, Double latitude, Double turn,
      Double speed, Integer heading, Double course, Integer status) {
    this.timestamp = timestamp;
    this.id = id;
    this.longitude = longitude;
    this.latitude = latitude;
    this.turn = turn;
    this.speed = speed;
    this.heading = heading;
    this.course = course;
    this.status = status;
  }



  /**
   * Gets the value of the 'timestamp' field.
   * 
   * @return timestamp in UNIX epochs (i.e., milliseconds elapsed since 1970-01-01 00:00:00.000).
   */
  @Override
  public Long getTimestamp() {
    return timestamp;
  }

  /**
   * Sets the value of the 'timestamp' field. timestamp in UNIX epochs (i.e., milliseconds elapsed
   * since 1970-01-01 00:00:00.000).
   * 
   * @param value the value to set.
   */
  @Override
  public void setTimestamp(Long value) {
    this.timestamp = value;
  }

  /**
   * Gets the value of the 'id' field.
   * 
   * @return A globally unique identifier for the moving object (usually, the MMSI of vessels).
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * Sets the value of the 'id' field. A globally unique identifier for the moving object (usually,
   * the MMSI of vessels).
   * 
   * @param value the value to set.
   */
  @Override
  public void setId(String value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'longitude' field.
   * 
   * @return Longitude coordinate in decimal degrees (georeference: WGS84) of this point location.
   */
  public Double getLongitude() {
    return longitude;
  }

  /**
   * Sets the value of the 'longitude' field. Longitude coordinate in decimal degrees (georeference:
   * WGS84) of this point location.
   * 
   * @param value the value to set.
   */
  public void setLongitude(Double value) {
    this.longitude = value;
  }

  /**
   * Gets the value of the 'latitude' field.
   * 
   * @return Latitude coordinate in decimal degrees (georeference: WGS84) of this point location.
   */
  public Double getLatitude() {
    return latitude;
  }

  /**
   * Sets the value of the 'latitude' field. Latitude coordinate in decimal degrees (georeference:
   * WGS84) of this point location.
   * 
   * @param value the value to set.
   */
  public void setLatitude(Double value) {
    this.latitude = value;
  }

  /**
   * Gets the value of the 'turn' field.
   * 
   * @return Rate of turn, right or left, 0 to 720 degrees per minute.
   */
  public Double getTurn() {
    return turn;
  }

  /**
   * Sets the value of the 'turn' field. Rate of turn, right or left, 0 to 720 degrees per minute.
   * 
   * @param value the value to set.
   */
  public void setTurn(Double value) {
    this.turn = value;
  }

  /**
   * Gets the value of the 'speed' field.
   * 
   * @return Speed over ground in knots (allowed values: 0-102.2 knots).
   */
  public Double getSpeed() {
    return speed;
  }

  /**
   * Sets the value of the 'speed' field. Speed over ground in knots (allowed values: 0-102.2
   * knots).
   * 
   * @param value the value to set.
   */
  public void setSpeed(Double value) {
    this.speed = value;
  }

  /**
   * Gets the value of the 'heading' field.
   * 
   * @return True heading in degrees (0-359), relative to true north.
   */
  public Integer getHeading() {
    return heading;
  }

  /**
   * Sets the value of the 'heading' field. True heading in degrees (0-359), relative to true north.
   * 
   * @param value the value to set.
   */
  public void setHeading(Integer value) {
    this.heading = value;
  }

  /**
   * Gets the value of the 'course' field.
   * 
   * @return Course over ground (allowed values: 0-359.9 degrees).
   */
  public Double getCourse() {
    return course;
  }

  /**
   * Sets the value of the 'course' field. Course over ground (allowed values: 0-359.9 degrees).
   * 
   * @param value the value to set.
   */
  public void setCourse(Double value) {
    this.course = value;
  }

  /**
   * Gets the value of the 'status' field.
   * 
   * @return Navigational status.
   */
  public Integer getStatus() {
    return status;
  }

  /**
   * Sets the value of the 'status' field. Navigational status.
   * 
   * @param value the value to set.
   */
  public void setStatus(Integer value) {
    this.status = value;
  }

  public String getMsgErrorFlag() {
    return msgErrorFlag;
  }

  public void setMsgErrorFlag(String msgErrorFlag) {
    this.msgErrorFlag = msgErrorFlag;
  }

  @Override
  public String toString() {
    String message =
        "AisMessage [timestamp=" + timestamp + ", id=" + id + ", longitude=" + longitude
            + ", latitude=" + latitude + ", turn=" + turn + ", speed=" + speed + ", heading="
            + heading + ", course=" + course + ", status=" + status + ", msgErrorFlag="
            + msgErrorFlag;

    if (getStatistics() != null) {

      message += getStatistics().toString();
    }
    return message + "]";
  }

  @Override
  public AbstractStatisticsWrapper getStatistics() {

    return trajectoryStatistics;
  }

  @Override
  public void setStatistics(AbstractStatisticsWrapper value) {
    this.trajectoryStatistics = value;

  }

  @Override
  public String toCsv(String delimiter, boolean addNewLine) {

    String message =
        timestamp + delimiter + id + delimiter + longitude + delimiter + latitude + delimiter
            + speed + delimiter + heading + delimiter + msgErrorFlag + delimiter + turn + delimiter
            + course + delimiter + status;

    if (getStatistics() != null) {

      message += delimiter + getStatistics().toCsv(delimiter);
    }
    if (addNewLine) {
      message += System.lineSeparator();
    }
    return message;

  }

  public String getOriginalRawMessage() {
    return originalRawMessage;
  }

  public void setOriginalRawMessage(String originalRawMessage) {
    this.originalRawMessage = originalRawMessage;
  }



}
