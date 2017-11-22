package eu.datacron.insitu.maritime;

import java.io.Serializable;

import eu.datacron.insitu.maritime.statistics.StatisticsWrapper;

/**
 * @author ehab.qadah
 */
public interface  PositionMessage extends Serializable {

   String getId();

   void setId(String id);

   Long getTimestamp();

   void setTimestamp(Long value);


   StatisticsWrapper getStatistics();

   void setStatistics(StatisticsWrapper value);

   String toCsv(String delimiter);

}
