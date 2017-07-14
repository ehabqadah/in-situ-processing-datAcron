package eu.datacron.in_situ_processing.maritime.streams.operators;

import java.util.List;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import eu.datacron.in_situ_processing.maritime.AisMessage;
import eu.datacron.in_situ_processing.statistics.AisTrajectoryStatistics;
import eu.datacron.in_situ_processing.statistics.StatisticsWrapper;

/**
 * This a map operator that processes the AIS messages stream and enrich it with new derived
 * attributes i.e., statistics
 * 
 * @author ehab.qadah
 */
public final class AisStreamEnricher extends RichFlatMapFunction<AisMessage, AisMessage> {

  private static final long serialVersionUID = -8949204796030799073L;
  /**
   * The ValueState handle for the last statistics of a trajectory
   */
  private transient ValueState<StatisticsWrapper<AisMessage>> statisticsOfTrajectory;


  @Override
  public void open(Configuration config) {
    ValueStateDescriptor<StatisticsWrapper<AisMessage>> descriptor =
        new ValueStateDescriptor<StatisticsWrapper<AisMessage>>("trajectoryStatistics",
            TypeInformation.of(new TypeHint<StatisticsWrapper<AisMessage>>() {}));

    statisticsOfTrajectory = getRuntimeContext().getState(descriptor);

  }

  @Override
  public void flatMap(AisMessage value, Collector<AisMessage> out) throws Exception {
    StatisticsWrapper<AisMessage> curreStatistics =
        statisticsOfTrajectory.value() == null ? new AisTrajectoryStatistics()
            : statisticsOfTrajectory.value();

    List<AisMessage> processedList = curreStatistics.processNewPosition(value);

    // Attached statistics to the AIS message
    value.setStatistics(curreStatistics);
    statisticsOfTrajectory.update(curreStatistics);

    for (AisMessage aisMessage : processedList) {
      out.collect(aisMessage);
    }

  }
}
