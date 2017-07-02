package eu.datacron.in_situ_processing.flink.utils;

import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import org.apache.log4j.Logger;

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
  private int lineSlideTimeDelay = 1;
  boolean running = true;
  int i = 0;

  public FileLinesStreamSource() {}

  public FileLinesStreamSource(String dataFilePath) {
    this.dataFilePath = dataFilePath;

  }

  public FileLinesStreamSource(String dataFilePath, int lineSlideTimeDelay) {

    this.lineSlideTimeDelay = lineSlideTimeDelay;
    this.dataFilePath = dataFilePath;
  }

  @Override
  public void run(SourceContext<String> ctx) throws Exception {

    while (running) {


      try (BufferedReader br = new BufferedReader(new FileReader(dataFilePath))) {
        String messageLine;
        while ((messageLine = br.readLine()) != null) {
          i++;
          ctx.collect(messageLine);
          if (lineSlideTimeDelay > 0)
            Thread.sleep(lineSlideTimeDelay);
        }
      } catch (Exception e) {

        logger.error(e.getMessage(), e);
        break;
      }

      Thread.sleep(100000);
      running = false;
    }
  }

  @Override
  public void cancel() {
    running = false;
  }

}
