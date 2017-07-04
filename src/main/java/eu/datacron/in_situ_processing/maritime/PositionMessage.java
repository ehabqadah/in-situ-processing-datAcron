package eu.datacron.in_situ_processing.maritime;

import java.io.Serializable;

import eu.datacron.in_situ_processing.statistics.StatisticsWrapper;

/**
 * @author ehab.qadah
 */
public interface PositionMessage extends Serializable {

  String getId();

  void setId(String id);

  Long getTimestamp();

  void setTimestamp(Long value);


  StatisticsWrapper getStatistics();

  void setStatistics(StatisticsWrapper value);

  String toCsv(String delimiter);

}
