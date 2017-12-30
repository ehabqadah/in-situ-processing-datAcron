package eu.datacron.insitu.maritime;

import java.io.Serializable;

import eu.datacron.insitu.maritime.statistics.AbstractStatisticsWrapper;

/**
 * @author ehab.qadah
 */
public interface PositionMessage extends Serializable {

  String getId();

  void setId(String id);

  Long getTimestamp();

  void setTimestamp(Long value);

  AbstractStatisticsWrapper getStatistics();

  void setStatistics(AbstractStatisticsWrapper value);

  String toCsv(String delimiter);

}
