package eu.datacron.insitu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import eu.datacron.insitu.common.utils.Configs;

/**
 * This class is responsible for publishing the raw stream data to kafka to be consumed by the
 * in-situ processing component
 * 
 * @author ehab.qadah
 *
 */
public class InputStreamSimulatorApp {
  private static Configs configs = Configs.getInstance();

  public static void main(String[] args) throws Exception {

    String outputStreamTopicName = configs.getStringProp("inputStreamTopicName");
    double streamDelayScale = configs.getDoubleProp("streamDelayScale");
    Properties producerProps = getKafkaProducerProperties();
    String inputDataPath = configs.getStringProp("aisDataSetFilePath");
    System.out.println("Publishing " + inputDataPath + " on " + outputStreamTopicName);
    // Get the json config for parsing the raw input stream
    String parsingConfig = getParsingJsonConfig();
    InputStreamSimulator streamSimulator =
        new InputStreamSimulator(inputDataPath, parsingConfig, true, streamDelayScale,
            outputStreamTopicName, producerProps);
    streamSimulator.run();
  }


  public static Properties getKafkaProducerProperties() {
    Properties props = new Properties();
    props.put("bootstrap.servers", configs.getStringProp("bootstrapServers"));
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    return props;
  }

  public static String getParsingJsonConfig() {
    InputStream input = null;
    String schemaFileName = configs.getStringProp("inputDataSchema");
    input = Configs.class.getResourceAsStream("/" + schemaFileName);

    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    StringBuilder out = new StringBuilder();
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        out.append(line);
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
      return null;
    }
    return out.toString();

  }
}
