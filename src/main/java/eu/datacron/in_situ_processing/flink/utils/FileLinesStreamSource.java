package eu.datacron.in_situ_processing.flink.utils;

import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * File lines stream source function that read the message from text file
 * 
 * @author Ehab Qadah
 * 
 */
public class FileLinesStreamSource implements SourceFunction<String> {

  private static final long serialVersionUID = 2174904787118597072L;
  static Logger logger = Logger.getLogger(FileLinesStreamSource.class.getName());
  private String dataFilePath;

  boolean running = true;
  int i = 0;
  private int timeStampIndex;
  private String delimiter;

  public FileLinesStreamSource() {}

  public FileLinesStreamSource(String dataFilePath, String parsingJsonConfigsStr) {
    this.dataFilePath = dataFilePath;
    JSONObject parsingJsonObject = new JSONObject(parsingJsonConfigsStr);
    this.timeStampIndex = parsingJsonObject.getInt("timestamp");
    this.delimiter = parsingJsonObject.getString("delimiter");
  }


  @Override
  public void run(SourceContext<String> ctx) throws Exception {

    Thread.sleep(10000);
    long oldTimeStamp = 0;
    while (running) {

      try (BufferedReader br = new BufferedReader(new FileReader(dataFilePath))) {
        String messageLine;
        while ((messageLine = br.readLine()) != null) {
          i++;

          long newTime = Long.parseLong(messageLine.split(delimiter)[timeStampIndex]);
          ctx.collectWithTimestamp(messageLine, newTime);
          long delay = 0;
          if (oldTimeStamp != 0) {
            // System.out.println(delay);
            delay = newTime - oldTimeStamp;
          }
          oldTimeStamp = newTime;
          if (delay > 0) {
            if (delay > 0) {
              // Thread.sleep(delay);
            }
          }

//          if (i % 1000 == 0) {
//            break;
//          }
        }
      } catch (Exception e) {

        logger.error(e.getMessage(), e);
        break;
      }

      Thread.sleep(1000000);
      running = false;
    }
  }

  @Override
  public void cancel() {
    running = false;
  }

}
