package eu.datacron.insitu.maritime.streams.operators;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.util.serialization.SerializationSchema;
import org.apache.log4j.Logger;

import eu.datacron.insitu.maritime.AisMessage;

/**
 * This is a sink to write a stream of AIS messages to file in CSV format
 * 
 * @author ehab.qadah
 */
public final class AisMessagesToFileSinkWriter extends RichSinkFunction<AisMessage> {

  private static final long serialVersionUID = -3639226717442955215L;
  static Logger logger = Logger.getLogger(AisMessagesToFileSinkWriter.class.getName());

  private SerializationSchema<AisMessage> serializationSchema;
  private String filePath;

  public AisMessagesToFileSinkWriter() {}

  public AisMessagesToFileSinkWriter(String filePath,
      SerializationSchema<AisMessage> aisMessageSchema) {
    this.serializationSchema = aisMessageSchema;
    this.filePath = filePath;
  }

  @Override
  public void invoke(AisMessage value) throws Exception {

    byte[] messageBytes = serializationSchema.serialize(value);
    Files.write(Paths.get(this.filePath), messageBytes, StandardOpenOption.APPEND);
  }
}
