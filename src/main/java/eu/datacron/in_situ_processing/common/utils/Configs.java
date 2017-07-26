package eu.datacron.in_situ_processing.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Configurations wrapper
 * 
 * @author ehab.qadah
 *
 */

public class Configs implements Serializable {


  private static final String CONFIG_PROPERTIES_FILE = "/config.properties";
  static Logger logger = Logger.getLogger(Configs.class.getName());
  private static final long serialVersionUID = 3829172077808658556L;

  private Configs(Properties props) {
    this.props = props;
  }

  private static Configs singleTonInstance = null;

  private Properties props;

  public static Configs getInstance() {

    if (singleTonInstance == null) {
      Properties props = new Properties();
      InputStream input = null;

      try {
        input = Configs.class.getResourceAsStream(CONFIG_PROPERTIES_FILE);
        // load a properties file
        props.load(input);
        singleTonInstance = new Configs(props);
      } catch (IOException ex) {
        logger.error(ex.getMessage(), ex);
      } finally {
        if (input != null) {
          try {
            input.close();
          } catch (IOException e) {
            logger.error(e.getMessage(), e);
          }
        }
      }
    }
    return singleTonInstance;
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
