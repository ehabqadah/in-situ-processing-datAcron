package eu.datacron.insitustreams.simulation;

import java.util.Properties;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

/**
 * This class is responsible to simulate/replay the raw message stream by delay sending some records
 * based on the difference of time stamp of a trajectory's points
 * 
 * @author ehab.qadah
 */
public class StreamPlayer extends
    RichMapFunction<Tuple3<String, Long, String>, Tuple3<String, Long, String>> {

  private static final long serialVersionUID = -8094032551260660913L;
  static Logger logger = Logger.getLogger(StreamPlayer.class.getName());

  // --------------------------------------------------------------------------------------------
  // Fields
  // --------------------------------------------------------------------------------------------

  private Properties kafkaProps;
  private transient KafkaProducer<String, String> producer;
  private String topicName;
  /**
   * The ValueState handle for the last timestamp of a trajectory
   */
  private transient ValueState<Long> lastTimestamp;
  private double simulationWaitingScale = 1.0;

  public StreamPlayer() {}

  public StreamPlayer(double simulationWaitingScale, String topicName, Properties kafkaProps) {
    this.simulationWaitingScale = simulationWaitingScale;
    this.topicName = topicName;
    this.kafkaProps = kafkaProps;
    setupProducer();
  }

  @Override
  public Tuple3<String, Long, String> map(Tuple3<String, Long, String> rawMessageTuple)
      throws Exception {
    long delay = getSimulatedTimeDelayBetweenRawMessages(rawMessageTuple);
    // write the message to Kafka after the delay
    writeRawMessageWithDelay(rawMessageTuple, delay);
    return rawMessageTuple;
  }

  /**
   * Find the delay between the new raw message and the last received one
   * 
   * @param rawMessageTuple
   * @return
   * @throws Exception
   */
  private long getSimulatedTimeDelayBetweenRawMessages(Tuple3<String, Long, String> rawMessageTuple)
      throws Exception {
    // access the state value
    long currentPointTimestamp = rawMessageTuple.f1;
    long lastPointTimeStamp =
        lastTimestamp.value() == null ? currentPointTimestamp : lastTimestamp.value();
    lastTimestamp.update(currentPointTimestamp);
    long delay = (long) ((currentPointTimestamp - lastPointTimeStamp) * simulationWaitingScale);

    if (delay < 0) {
      String errorMessage =
          "negative delay" + delay + "for " + rawMessageTuple + " old timestamp" + lastTimestamp;
      logger.error(errorMessage);
    }
    return delay;
  }

  private void setupProducer() {
    // Override key & value serialization classes
    kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    this.producer = new KafkaProducer<String, String>(kafkaProps);
  }

  public void writeRawMessageWithDelay(Tuple3<String, Long, String> rawMessageTuple, long writeDelay) {

    if (writeDelay == 0) {
      // send the message to Kafka with creating a thread
      writeMessageToKafka(rawMessageTuple);
      return;
    }
    try {
      new Thread(new Runnable() {
        @Override
        public void run() {

          if (writeDelay > 0) {
            try {
              Thread.sleep((writeDelay));
            } catch (InterruptedException e) {
              logger.error(e.getMessage());
            }
          }

          // send the message to Kafka
          writeMessageToKafka(rawMessageTuple);
        }

      }).run();
    } catch (Exception e) {

      logger.error(e.getMessage());
      // send the message to Kafka anyway
      writeMessageToKafka(rawMessageTuple);
    }
  }

  /**
   * Send the original raw message back to Kafka after simulating the time delay
   * 
   * @param rawMessageTuple
   */
  private void writeMessageToKafka(Tuple3<String, Long, String> rawMessageTuple) {
    if (producer == null)
      setupProducer();

    String rawMessage = rawMessageTuple.f2;
    String id = rawMessageTuple.f0;
    // send the record to Kafka
    ProducerRecord<String, String> record =
        new ProducerRecord<String, String>(topicName, id, rawMessage);
    producer.send(record);

    // logger.info("Send a raw message:" + rawMessage);
  }

  @Override
  public void open(Configuration config) {
    ValueStateDescriptor<Long> descriptor =
        new ValueStateDescriptor<Long>("lastTimestamp", TypeInformation.of(new TypeHint<Long>() {}));
    lastTimestamp = getRuntimeContext().getState(descriptor);

  }
}
