package eu.datacron.in_situ_processing.streams.simulation;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This a map operator that processes the raw stream lines and extract the id and timestamps to be
 * used in the stream simulation.
 * 
 * @author ehab.qadah
 */
public class RawStreamMapper implements FlatMapFunction<String, Tuple3<String, Long, String>> {

  private static final long serialVersionUID = -7969686242238108964L;
  private static final Logger logger = LoggerFactory.getLogger(RawStreamMapper.class.getName());
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
    long timestamp;
    try {
      timestamp = Long.parseLong(fieldsValue[timestampIndex]);
    } catch (NumberFormatException ex) {

      logger.error(ex.getMessage());
      return null;
    }
    return new Tuple3<String, Long, String>(id, timestamp, value);
  }

  @Override
  public void flatMap(String value, Collector<Tuple3<String, Long, String>> out) throws Exception {
    initParsingConfigObject();
    Tuple3<String, Long, String> parsedTuple = parseRawLine(value);
    if (parsedTuple != null) {
      out.collect(parsedTuple);
    }

  }
}
