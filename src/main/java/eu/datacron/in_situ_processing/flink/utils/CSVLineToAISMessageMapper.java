package eu.datacron.in_situ_processing.flink.utils;

import java.nio.charset.StandardCharsets;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.apache.flink.util.Collector;

import eu.datacron.in_situ_processing.maritime.AisMessage;
import eu.datacron.in_situ_processing.maritime.AisMessageCsvSchema;

/**
 * This a map operator that process a stream of csv lines and transform them to AisMessages objects
 * 
 * @author ehab.qadah
 */
public final class CSVLineToAISMessageMapper implements FlatMapFunction<String, AisMessage> {

  private static final long serialVersionUID = -7969686242238108964L;
  DeserializationSchema<AisMessage> deserializationSchema;

  public CSVLineToAISMessageMapper() {}

  // map a csv line to AISMessage using the AIS messages deserialization schema
  public CSVLineToAISMessageMapper(String parsingJsonConfigs) {
    deserializationSchema = new AisMessageCsvSchema(parsingJsonConfigs);
  }

  @Override
  public void flatMap(String value, Collector<AisMessage> out) throws Exception {

    AisMessage deserializedValue =
        deserializationSchema.deserialize(value.getBytes(StandardCharsets.UTF_8));
    if (deserializedValue != null) {
      out.collect(deserializedValue);
    }

  }
}
