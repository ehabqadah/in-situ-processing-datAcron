package eu.datacron.in_situ_processing.maritime;

import java.io.Serializable;

import eu.datacron.in_situ_processing.statistics.StatisticsWrapper;

/**
 * @author ehab.qadah
 */
public abstract class PositionMessage implements Serializable {

  abstract String getId();

  abstract void setId(String id);

  abstract Long getTimestamp();

  abstract void setTimestamp(Long value);


  abstract StatisticsWrapper getStatistics();

  abstract void setStatistics(StatisticsWrapper value);

  abstract String toCsv(String delimiter);

}
