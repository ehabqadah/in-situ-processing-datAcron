package eu.datacron.insitu.flink.utils;

import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File lines stream source function that read the message from text file
 * 
 * @author Ehab Qadah
 * 
 */
public class FileLinesStreamSource implements SourceFunction<String> {

  private static final int BOOTSTRAP_TIME = 2 * 60 * 1000;
  private static final long serialVersionUID = 2174904787118597072L;
  static Logger logger = LoggerFactory.getLogger(FileLinesStreamSource.class.getName());
  private String dataFilePath;

  boolean running = true;
  int i = 0;
  private int timeStampIndex;
  private String delimiter;
  boolean warmupWait = false;

  public FileLinesStreamSource() {}

  public FileLinesStreamSource(String dataFilePath, String parsingJsonConfigsStr) {
    initFields(dataFilePath, parsingJsonConfigsStr, false);
  }

  public FileLinesStreamSource(String dataFilePath, String parsingJsonConfigsStr, boolean warmupWait) {
    initFields(dataFilePath, parsingJsonConfigsStr, warmupWait);
  }

  private void initFields(String dataFilePath, String parsingJsonConfigsStr, boolean warmupWait) {
    this.dataFilePath = dataFilePath;
    JSONObject parsingJsonObject = new JSONObject(parsingJsonConfigsStr);
    this.timeStampIndex = parsingJsonObject.getInt("timestamp");
    this.delimiter = parsingJsonObject.getString("delimiter");
    this.warmupWait = warmupWait;
  }


  @Override
  public void run(SourceContext<String> ctx) throws Exception {

    while (running) {

      try (BufferedReader br = new BufferedReader(new FileReader(dataFilePath))) {
        String messageLine;
        while ((messageLine = br.readLine()) != null) {


          ctx.collect(messageLine);
          i++;
        }
      } catch (Exception e) {

        logger.info(e.getMessage());
        System.out.println(i + e.getMessage());
        break;
      }

      Thread.sleep(1000 * 60);
      running = false;
    }
  }

  @Override
  public void cancel() {
    running = false;
  }

}
