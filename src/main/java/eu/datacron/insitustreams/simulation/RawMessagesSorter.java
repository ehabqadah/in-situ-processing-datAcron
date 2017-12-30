package eu.datacron.insitustreams.simulation;

import java.util.Comparator;
import java.util.PriorityQueue;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.log4j.Logger;

import eu.datacron.insitu.maritime.streams.operators.AisMessagesStreamSorter;

/**
 * This a process function to sort the raw messages based on their timestamp
 * 
 * @author ehab.qadah
 */
public final class RawMessagesSorter extends
    ProcessFunction<Tuple3<String, Long, String>, Tuple3<String, Long, String>> {

  private static final int MAX_NUMBER_OF_QUEUED_ELEMENTS = 3;
  private static final long serialVersionUID = 5650060885845557953L;
  static Logger logger = Logger.getLogger(AisMessagesStreamSorter.class.getName());
  private ValueState<PriorityQueue<Tuple3<String, Long, String>>> queueState = null;

  @Override
  public void open(Configuration config) {
    ValueStateDescriptor<PriorityQueue<Tuple3<String, Long, String>>> descriptor =
        new ValueStateDescriptor<>(
        // state name
            "sorted-raw-messages",
            // type information of state
            TypeInformation.of(new TypeHint<PriorityQueue<Tuple3<String, Long, String>>>() {}));
    queueState = getRuntimeContext().getState(descriptor);
  }

  @Override
  public void processElement(Tuple3<String, Long, String> message, Context context,
      Collector<Tuple3<String, Long, String>> out) throws Exception {

    TimerService timerService = context.timerService();

    if (context.timestamp() > timerService.currentWatermark()) {
      PriorityQueue<Tuple3<String, Long, String>> queue = queueState.value();
      if (queue == null) {
        queue = new PriorityQueue<>(15, new RawMessageTuplesComparator());
      }
      queue.add(message);
      queueState.update(queue);
      // register a timer to be fired when the watermark passes this message timestamp
      timerService.registerEventTimeTimer(message.f1);
    } else {
      String outOfOrderErrorMessage = "out of order message: " + message.f2;
      logger.info(outOfOrderErrorMessage);
      throw new Exception(outOfOrderErrorMessage);
    }
  }

  @Override
  public void onTimer(long timestamp, OnTimerContext context,
      Collector<Tuple3<String, Long, String>> out) throws Exception {
    PriorityQueue<Tuple3<String, Long, String>> queue = queueState.value();
    Long watermark = context.timerService().currentWatermark();

    Tuple3<String, Long, String> head = queue.peek();
    boolean emitAll = queue.size() > MAX_NUMBER_OF_QUEUED_ELEMENTS;

    while (head != null && (head.f1 <= watermark || emitAll)) {
      out.collect(head);
      queue.remove(head);
      head = queue.peek();
    }
  }

  public static final class RawMessageTuplesComparator implements
      Comparator<Tuple3<String, Long, String>> {
    @Override
    public int compare(Tuple3<String, Long, String> o1, Tuple3<String, Long, String> o2) {

      if (o1.f1 > o2.f1) {
        return 1;
      }
      if (o1.f1 == o2.f1) {
        return 0;
      }
      return -1;
    }
  }
}
