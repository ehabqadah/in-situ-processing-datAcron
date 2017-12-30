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
import eu.datacron.insitu.maritime.statistics.AisTrajectoryStatistics;
import eu.datacron.insitu.maritime.statistics.AbstractStatisticsWrapper;

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

  private void updateAreaInfo(AisMessage value, AbstractStatisticsWrapper<AisMessage> curreStatistics) {

    long startTime = System.currentTimeMillis();
    Set<String> detectedAreas = new HashSet<String>();
    // Get all area which vessel within
    for (Area area : areas) {
      if (GeoUtils.isPointInPolygon(area.getPolygon(), value.getLongitude(), value.getLatitude())) {
        detectedAreas.add(area.getId());

      }
    }
    boolean changeInArea = false;
    if (curreStatistics.getDetectedAreas() != null
        && !curreStatistics.getDetectedAreas().equals(detectedAreas)) {
      changeInArea = true;
    }
    // update area info
    curreStatistics.setDetectedAreas(detectedAreas);
    curreStatistics.setChangeInArea(changeInArea);

    // System.out.println("latency:" + (System.currentTimeMillis() - startTime));
  }
}
