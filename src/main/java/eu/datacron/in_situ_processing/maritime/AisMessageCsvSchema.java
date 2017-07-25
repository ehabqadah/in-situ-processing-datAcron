package eu.datacron.in_situ_processing.maritime;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.apache.flink.streaming.util.serialization.SerializationSchema;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.datacron.in_situ_processing.common.utils.ReflectionUtils;


/**
 * A serialization schema for the {@link AisMessage}
 * 
 * @author ehab.qadah
 */
public class AisMessageCsvSchema implements SerializationSchema<AisMessage>,
    DeserializationSchema<AisMessage> {

  private static final String DEFAULT_OUTPUT_LINE_DELIMITER = ",";
  private static final long serialVersionUID = 4339578918900034257L;
  private static final Logger logger = LoggerFactory.getLogger(AisMessageCsvSchema.class.getName());
  private transient JSONObject parsingJsonConfigs;
  private String parsingJsonConfigsStr;
  String delimiter;
  private boolean addNewLine;
  private String outputLineDelimiter;

  public AisMessageCsvSchema() {}

  public AisMessageCsvSchema(String parsingJsonConfigsStr) {
    setupSchema(parsingJsonConfigsStr, DEFAULT_OUTPUT_LINE_DELIMITER, false);
  }

  public AisMessageCsvSchema(String parsingJsonConfigsStr, String outputLineDelimiter) {
    setupSchema(parsingJsonConfigsStr, outputLineDelimiter, false);
  }

  public AisMessageCsvSchema(String parsingJsonConfigsStr, String outputLineDelimiter,
      boolean addNewLine) {
    setupSchema(parsingJsonConfigsStr, outputLineDelimiter, addNewLine);
  }

  private void setupSchema(String parsingJsonConfigsStr, String outputLineDelimiter,
      boolean addNewLine) {
    this.parsingJsonConfigsStr = parsingJsonConfigsStr;
    this.addNewLine = addNewLine;
    this.outputLineDelimiter = outputLineDelimiter;
    initParsingConfigObject();
  }


  private void initParsingConfigObject() {
    // make sure that parsing config object is initialized
    if (parsingJsonConfigs == null) {
      this.parsingJsonConfigs = new JSONObject(parsingJsonConfigsStr);
      delimiter = parsingJsonConfigs.getString("delimiter");
    }
  }

  @Override
  public TypeInformation<AisMessage> getProducedType() {
    return TypeExtractor.getForClass(AisMessage.class);
  }

  @Override
  public byte[] serialize(AisMessage element) {

    String csvLine = element.toCsv(outputLineDelimiter);
    if (addNewLine) {
      csvLine += "\n";
    }
    return csvLine.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public AisMessage deserialize(byte[] aisMessageBytes) {
    // Deserialize the byte array of csv line
    String csvLine = new String(aisMessageBytes, StandardCharsets.UTF_8);
    return parseCSVline(csvLine);
  }

  /**
   * 
   * @param csvLine
   * 
   * @return an AisMessage for valid csv line or null in case of any parsing error
   */
  private AisMessage parseCSVline(String csvLine) {
    initParsingConfigObject();
    AisMessage aisMessage = new AisMessage();

    String[] fieldsValue = csvLine.split(delimiter, -1);// -1 to include trailing empty strings

    for (Field field : AisMessage.class.getFields()) {
      String fieldName = field.getName();

      // check if the JSON has a corresponding key for the given field
      if (!parsingJsonConfigs.isNull(fieldName)) {
        try {
          // Get value of the field from the csv line based on its index
          int fieldIndex = parsingJsonConfigs.getInt(fieldName);
          // Casr the string value of the field based on its acutal type
          Object castedFieldValue =
              ReflectionUtils.getCastedFieldValue(field, fieldsValue[fieldIndex]);


          // set the value of the field from the csv line using reflection
          field.set(aisMessage, castedFieldValue);
        } catch (Exception e) {
          logger.error(e.getMessage());
          // invalid line
          return null;
        }
      }
    }
    aisMessage.setOriginalRawMessage(csvLine);
    return aisMessage;
  }

  @Override
  public boolean isEndOfStream(AisMessage nextElement) {
    return false;
  }
}
