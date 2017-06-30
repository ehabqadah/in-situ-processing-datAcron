package eu.datacron.in_situ_processing.maritime.beans;

import java.nio.charset.StandardCharsets;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.apache.flink.streaming.util.serialization.SerializationSchema;
import org.apache.log4j.Logger;


/**
 * A serialization schema for the {@link AISMessage}
 * 
 * @author ehab.qadah
 */
public class AISMessageCSVSchema implements SerializationSchema<AISMessage>,
    DeserializationSchema<AISMessage> {

  private static final long serialVersionUID = 4339578918900034257L;
  private static final Logger logger = Logger.getLogger(AISMessageCSVSchema.class.getName());

  @Override
  public TypeInformation<AISMessage> getProducedType() {
    return TypeExtractor.getForClass(AISMessage.class);

  }

  @Override
  public byte[] serialize(AISMessage element) {
    return element.toString().getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public AISMessage deserialize(byte[] aisMessageBytes) {
    // Deserialize the byte array of csv line
    String csvLine = new String(aisMessageBytes, StandardCharsets.UTF_8);
    AISMessage aisMessage = new AISMessage();
    aisMessage.setId(csvLine);
    return aisMessage;

  }

  @Override
  public boolean isEndOfStream(AISMessage nextElement) {
    return false;
  }
}


// --------------------------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------------------------


// --------------------------------------------------------------------------------------------
// Constructors
// --------------------------------------------------------------------------------------------
