package eu.datacron.insitu.maritime.streams.operators;

import java.util.PriorityQueue;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.log4j.Logger;

import eu.datacron.insitu.maritime.AisMessage;
import eu.datacron.insitu.maritime.PositionMessagesComparator;

/**
 * This a sort process function that sorts the stream of AIS messages based on their timestamp
 * 
 * @author ehab.qadah
 */
public final class AisMessagesStreamSorter extends ProcessFunction<AisMessage, AisMessage> {


  private static final int MAX_NUMBER_OF_QUEUED_ELEMENTS = 25;
  private static final long serialVersionUID = 5650060885845557953L;
  static Logger logger = Logger.getLogger(AisMessagesStreamSorter.class.getName());
  private ValueState<PriorityQueue<AisMessage>> queueState = null;

  @Override
  public void open(Configuration config) {
    ValueStateDescriptor<PriorityQueue<AisMessage>> descriptor = new ValueStateDescriptor<>(
    // state name
        "sorted-ais-messages",
        // type information of state
        TypeInformation.of(new TypeHint<PriorityQueue<AisMessage>>() {}));
    queueState = getRuntimeContext().getState(descriptor);
  }

  @Override
  public void processElement(AisMessage message, Context context, Collector<AisMessage> out)
      throws Exception {

    TimerService timerService = context.timerService();

    PriorityQueue<AisMessage> queue = queueState.value();
    if (queue == null) {
      queue = new PriorityQueue<>(15, new PositionMessagesComparator());
    }
    long timestamp = System.currentTimeMillis();
    if (context.timestamp() > timerService.currentWatermark()) {
      queue.add(message);
      queueState.update(queue);
      // register a timer to be fired when the watermark passes this message timestamp
      timerService.registerEventTimeTimer(timestamp);
    } else {
      // logger.info("out of order message: " + message.toString());
      // throw new Exception(timerService.currentWatermark() + "out of order message: "
      // + message.toString());
      queue.add(message);
      queueState.update(queue);

    }
  }

  @Override
  public void onTimer(long timestamp, OnTimerContext context, Collector<AisMessage> out)
      throws Exception {
    PriorityQueue<AisMessage> queue = queueState.value();
    Long watermark = context.timerService().currentWatermark();

    AisMessage head = queue.peek();
    boolean emitAll = queue.size() > MAX_NUMBER_OF_QUEUED_ELEMENTS;

    while (head != null && (head.timestamp <= watermark || emitAll)) {
      out.collect(head);
      queue.remove(head);
      head = queue.peek();
    }
  }
}
