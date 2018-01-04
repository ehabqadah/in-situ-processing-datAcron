package eu.datacron.insitu.maritime.streams.operators;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;

import eu.datacron.insitu.areas.Area;
import eu.datacron.insitu.areas.GeoUtils;
import eu.datacron.insitu.maritime.AisMessage;
import eu.datacron.insitu.maritime.statistics.AbstractStatisticsWrapper;
import eu.datacron.insitu.maritime.statistics.AisTrajectoryStatistics;

/**
 * This a map operator that processes the AIS messages stream and enrich it with new derived
 * attributes i.e., statistics
 * 
 * @author ehab.qadah
 */
public final class AisStreamEnricher extends RichMapFunction<AisMessage, AisMessage> {

  private static final long serialVersionUID = -8949204796030799073L;
  /**
   * The ValueState handle for the last statistics of a trajectory
   */
  private transient ValueState<AbstractStatisticsWrapper<AisMessage>> statisticsOfTrajectory;
  private List<Area> areas;

  public AisStreamEnricher() {}

  public AisStreamEnricher(List<Area> areas) {
    this.areas = areas;
  }

  @Override
  public void open(Configuration config) {
    ValueStateDescriptor<AbstractStatisticsWrapper<AisMessage>> descriptor =
        new ValueStateDescriptor<AbstractStatisticsWrapper<AisMessage>>("trajectoryStatistics",
            TypeInformation.of(new TypeHint<AbstractStatisticsWrapper<AisMessage>>() {}));

    statisticsOfTrajectory = getRuntimeContext().getState(descriptor);

  }

  @Override
  public AisMessage map(AisMessage value) throws Exception {
    AbstractStatisticsWrapper<AisMessage> curreStatistics =
        statisticsOfTrajectory.value() == null ? new AisTrajectoryStatistics()
            : statisticsOfTrajectory.value();

    // Compute new statistics attributes for the new received position message
    curreStatistics.processNewPosition(value);

    updateAreaInfo(value, curreStatistics);
    // Attached statistics to the AIS message
    value.setStatistics(curreStatistics);
    statisticsOfTrajectory.update(curreStatistics);
    return value;
  }

  private void updateAreaInfo(AisMessage newMessage,
      AbstractStatisticsWrapper<AisMessage> curreStatistics) {

    long startTime = System.currentTimeMillis();
    Set<Area> currentDetectedAreas = curreStatistics.getDetectedAreas();
    Set<Area> newDetectedAreas = new HashSet<Area>();

    // Check first previous areas if they still valid

    if (currentDetectedAreas != null) {

      for (Area area : currentDetectedAreas) {
        if (GeoUtils.isPointInPolygon(area.getPolygon(), newMessage.getLongitude(),
            newMessage.getLatitude())) {
          newDetectedAreas.add(area);
        }
      }
    }
    // Get all area which position message within
    for (Area area : areas) {
      // Only attach two areas
      if (newDetectedAreas.size() > 1) {
        break;
      }

      if (GeoUtils.isPointInPolygon(area.getPolygon(), newMessage.getLongitude(),
          newMessage.getLatitude())) {
        newDetectedAreas.add(area);
      }

    }
    boolean changeInArea = false;
    // Check for change in area
    if (currentDetectedAreas != null) {
      String currentDetectedAreasStr =
          AbstractStatisticsWrapper.getDetectedAreasStr(currentDetectedAreas);
      String newDetectedAreasStr = AbstractStatisticsWrapper.getDetectedAreasStr(newDetectedAreas);

      if (!currentDetectedAreasStr.equals(newDetectedAreasStr)) {
        changeInArea = true;
      }
    }
    // update area info
    curreStatistics.setDetectedAreas(newDetectedAreas);
    curreStatistics.setChangeInArea(changeInArea);

    // System.out.println("latency:" + (System.currentTimeMillis() - startTime));
  }
}
