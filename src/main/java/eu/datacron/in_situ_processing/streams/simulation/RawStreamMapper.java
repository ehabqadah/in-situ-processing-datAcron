package eu.datacron.in_situ_processing.streams.simulation;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.json.JSONObject;

/**
 * @author ehab.qadah
 */
public class RawStreamMapper implements MapFunction<String, Tuple3<String, Long, String>> {

  private static final long serialVersionUID = -7969686242238108964L;
  String parsingJsonConfigs;
  private transient JSONObject parsingConfigs;

  public RawStreamMapper() {}


  public RawStreamMapper(String parsingJsonConfigs) {
    this.parsingJsonConfigs = parsingJsonConfigs;
    initParsingConfigObject();
  }

  private void initParsingConfigObject() {
    // make sure that parsing configs object is initialized
    if (parsingConfigs == null) {
      this.parsingConfigs = new JSONObject(parsingJsonConfigs);
    }
  }

  @Override
  public Tuple3<String, Long, String> map(String value) throws Exception {
    initParsingConfigObject();
    return parseRawLine(value);
  }

  /**
   * Extract the message id & timestamp
   * 
   * @param value
   * @return Tuple3<String, Long, String>(id, timestamp, value)
   */
  private Tuple3<String, Long, String> parseRawLine(String value) {
    String delimiter = parsingConfigs.getString("delimiter");
    String[] fieldsValue = value.split(delimiter);
    // get message id
    int idIndex = parsingConfigs.getInt("id");
    String id = fieldsValue[idIndex];

    // get message timestamp
    int timestampIndex = parsingConfigs.getInt("timestamp");
    long timestamp = Long.parseLong(fieldsValue[timestampIndex]);

    return new Tuple3<String, Long, String>(id, timestamp, value);
  }
}
