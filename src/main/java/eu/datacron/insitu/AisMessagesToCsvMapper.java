package eu.datacron.insitu;

import org.apache.flink.api.common.functions.MapFunction;

import eu.datacron.insitu.maritime.AisMessage;

/**
 * A map operator of AIS messages to CSV format
 * 
 * @author ehab.qadah
 *
 */
public class AisMessagesToCsvMapper implements MapFunction<AisMessage, String> {

  private static final long serialVersionUID = 5306666449608883748L;
  private String delimiter;

  public AisMessagesToCsvMapper() {}

  public AisMessagesToCsvMapper(String outputLineDelimiter) {
    this.delimiter = outputLineDelimiter;
  }

  @Override
  public String map(AisMessage value) throws Exception {

    return value.toCsv(delimiter);
  }
}
