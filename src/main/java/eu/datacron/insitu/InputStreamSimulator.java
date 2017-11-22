package eu.datacron.insitu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;

/**
 * Input stream simulator that reads the message from text file
 * 
 * @author Ehab Qadah
 * 
 */
public class InputStreamSimulator {


  private static final int BOOTSTRAP_TIME = 1 * 60 * 1000;
  private String dataFilePath;
  int i = 0;
  private int timeStampIndex;
  private String delimiter;
  boolean warmupWait = false;
  private double scaleDelay;

  private Properties kafkaProps;
  private transient KafkaProducer<String, String> producer;
  private String topicName;
  private int idIndex;


  public InputStreamSimulator(String dataFilePath, String parsingJsonConfigsStr,
      boolean warmupWait, double scaleDelay, String topicName, Properties kafkaProps) {

    this.dataFilePath = dataFilePath;
    JSONObject parsingJsonObject = new JSONObject(parsingJsonConfigsStr);
    this.timeStampIndex = parsingJsonObject.getInt("timestamp");
    this.idIndex = parsingJsonObject.getInt("id");
    this.delimiter = parsingJsonObject.getString("delimiter");
    this.warmupWait = warmupWait;
    this.scaleDelay = scaleDelay;
    this.topicName = topicName;
    this.kafkaProps = kafkaProps;
    setupProducer();
  }


  private void setupProducer() {
    // Override key & value serialization classes
    kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    this.producer = new KafkaProducer<String, String>(kafkaProps);
  }

  public void run() {


    if (warmupWait) {
      // warm up waiting time
      try {
        Thread.sleep(BOOTSTRAP_TIME);
      } catch (InterruptedException e) {
        System.out.println("warm up time waiting was interrupted" + e.getMessage());
      }
    }
    long oldTimeStamp = 0;

    try (BufferedReader br = new BufferedReader(new FileReader(dataFilePath))) {
      String rawMessage;
      while ((rawMessage = br.readLine()) != null) {
        i++;
        long newTime = 0;
        String[] messageFields = rawMessage.split(delimiter);
        String id = messageFields[idIndex];
        try {
          newTime = Long.parseLong(messageFields[timeStampIndex]);
        } catch (NumberFormatException e) {
          System.out.println(e.getMessage() + " for line:" + rawMessage);
        }

        long delay = 0;
        if (oldTimeStamp != 0) {

          delay = newTime - oldTimeStamp;
        }
        oldTimeStamp = newTime;
        if (delay < 0) {
          System.out.println("nagative delay" + delay + " ---- i " + i + "---- line" + rawMessage);
        }

        if (delay > 0) {
          try {
            Thread.sleep((long) (delay * scaleDelay));
          } catch (InterruptedException e) {
            System.out.println("waiting time was interrupted for " + i + e);
          }
        }
        writeMessageToKafka(id, rawMessage);

        if (i % 1000 == 0) {
          System.out.println("Sent" + i + " messages");
        }
      }
    } catch (Exception e) {

      System.out.println(e.getMessage() + e);
    }


  }


  /**
   * Send the original raw message back to Kafka after simulating the time delay
   * 
   * @param rawMessageTuple
   */
  private void writeMessageToKafka(String id, String rawMessage) {

    // send the record to Kafka
    ProducerRecord<String, String> record =
        new ProducerRecord<String, String>(topicName, id, rawMessage);
    producer.send(record);
    // logger.info("Send a raw message:" + rawMessage);
  }

}
