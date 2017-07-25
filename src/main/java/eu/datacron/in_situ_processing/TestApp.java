package eu.datacron.in_situ_processing;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import eu.datacron.in_situ_processing.common.utils.Configs;
import org.json.JSONObject;

/**
 * @author ehab.qadah
 */
public class TestApp {

  private static Configs configs = Configs.getInstance();

  public static void main(String[] args) throws IOException {

    String [] fields="1453984747,1,215130000,1,29.2796666666667,40.8366833333333,307,0,,".split(",",-1);
    System.out.println(fields.length);
//    System.out.println(configs.getStringProp("streamSourceType"));
//
//    InputStream input = null;
//    input = Configs.class.getResourceAsStream("/IMIS_Global_CSV_Schema.json");
//
//
//    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
//    StringBuilder out = new StringBuilder();
//    String line;
//    while ((line = reader.readLine()) != null) {
//      out.append(line);
//    }
//    JSONObject criticalPointJson = new JSONObject(out.toString());
//    System.out.println(out.toString()); // Prints the string content read from input stream
//    reader.close();
  }



}
