package eu.datacron.in_situ_processing.maritime;

import java.io.Serializable;

/**
 * @author ehab.qadah
 */
public interface PositionMessage extends Serializable {

  String getId();

  void setId(String id);

  Long getTimestamp();

  void setTimestamp(Long value);

}
