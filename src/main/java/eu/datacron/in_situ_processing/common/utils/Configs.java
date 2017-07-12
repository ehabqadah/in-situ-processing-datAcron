package eu.datacron.in_situ_processing.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 * Configurations wrapper
 * 
 * @author ehab.qadah
 *
 */

public class Configs implements Serializable {


  private static final long serialVersionUID = 3829172077808658556L;

  private Configs(Properties props) {
    this.props = props;
  }

  private static Configs _instance = null;

  private Properties props;

  public static Configs getInstance() {

    if (_instance == null) {
      Properties props = new Properties();
      InputStream input = null;

      try {
        input = Configs.class.getResourceAsStream("/config.properties");
        // load a properties file
        props.load(input);
        _instance = new Configs(props);
      } catch (IOException ex) {
        ex.printStackTrace();
      } finally {
        if (input != null) {
          try {
            input.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }

    }
    return _instance;
  }

  public String getStringProp(String propName) {

    return this.props.getProperty(propName);
  }

  public int getIntProp(String propName) {

    return Integer.parseInt(this.props.getProperty(propName));
  }

  public double getDoubleProp(String propName) {

    return Double.parseDouble(this.props.getProperty(propName));
  }

}
