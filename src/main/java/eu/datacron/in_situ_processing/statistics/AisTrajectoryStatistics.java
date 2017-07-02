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
      if(getNumberOfPoints() ==0){
        initFirstMessageAttributes(aisMessage);
      }
      //update location related attributes
      updateLocationAttributes(aisMessage);
      //update time related attributes
      updateTimeAttributes(aisMessage);
      
      updateSpeedAttributes(aisMessage);
      
      increasePointssCount();
      
    }

  }

  /**
   * Initialize  the derived attributes for the first received message
   * @param aisMessage
   */
  private void initFirstMessageAttributes(AisMessage aisMessage) {
   this.minLat=aisMessage.getLatitude();
   this.maxLat=aisMessage.getLatitude();
   
   this.minLong=aisMessage.getLongitude();
   this.maxLong=aisMessage.getLongitude();
   
   this.minSpeed=aisMessage.getSpeed();
   this.maxSpeed=aisMessage.getSpeed();
   
   this.lastTimestamp=aisMessage.getTimestamp();
    
  }

  private void updateSpeedAttributes(AisMessage aisMessage) {
    setMinSpeed(aisMessage.getSpeed());
    setMaxSpeed(aisMessage.getSpeed());
  }

  private void updateTimeAttributes(AisMessage aisMessage) {
    long newTimestamp=aisMessage.getTimestamp();
    long oldTimestamp=getLastTimestamp();
    long timeDiff=getNumberOfPoints() ==0? 0:newTimestamp-oldTimestamp;
    setLastDifftime(timeDiff);
    setMaxDifftime(timeDiff);
  // don't update the min for the first message
    if(getNumberOfPoints() >0){
      setMinDifftime(timeDiff);
     
    }
    // set the message's timestamp as the last seen timestamp
    setLastTimestamp(newTimestamp);
  }

  private void updateLocationAttributes(AisMessage aisMessage) {
    setMaxLat(aisMessage.getLatitude());
    setMinLat(aisMessage.getLatitude());
    
    setMaxLong(aisMessage.getLongitude());
    setMinLong(aisMessage.getLongitude());
  }


  @Override
  public String toString() {
    return "\n AisTrajectoryStatistics [getNumberOfPoints()=" + getNumberOfPoints()
        + ", getLastTimestamp()=" + getLastTimestamp() + ", getLastDifftime()=" + getLastDifftime()
        + ", getMinSpeed()=" + getMinSpeed() + ", getMinDifftime()=" + getMinDifftime()
        + ", getMaxSpeed()=" + getMaxSpeed() + ", getMaxDifftime()=" + getMaxDifftime()
        + ", getMinLong()=" + getMinLong() + ", getMaxLong()=" + getMaxLong() + ", getMinLat()="
        + getMinLat() + ", getMaxLat()=" + getMaxLat() + "] \n";
  }

  @Override
  public String toCsv() {
    return toString();
  }


}
