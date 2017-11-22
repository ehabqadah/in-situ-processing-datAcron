package eu.datacron.insitu.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;



/**
 * Configurations wrapper
 * 
 * @author ehab.qadah
 *
 */

public class Configs implements Serializable {

  private static final long serialVersionUID = 3829172077808658556L;

  private static final String CONFIG_PROPERTIES_FILE = "/config.properties";
  // static Logger logger = Logger.getLogger(Configs.class.getName());
  /** A delimiter for multiple values of the same config parameter **/
  public static final String MUTIL_VALUES_DELIMITER = ",";

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
        System.err.println(ex.getMessage());
      } finally {
        if (input != null) {
          try {
            input.close();
          } catch (IOException e) {
            System.err.println(e.getMessage());
          }
        }
      }
    }
    return singleTonInstance;
  }

  /**
   * Get a string value property
   * 
   * @param propName
   * @return
   */
  public String getStringProp(String propName) {

    return this.props.getProperty(propName);
  }

  /**
   * Get integer value property
   * 
   * @param propName
   * @return
   */
  public int getIntProp(String propName) {

    return Integer.parseInt(this.props.getProperty(propName));
  }

  /**
   * Get double value property
   * 
   * @param propName
   * @return
   */

  public double getDoubleProp(String propName) {

    return Double.parseDouble(this.props.getProperty(propName));
  }

  /**
   * Get list value property
   * 
   * @param propName
   * @return
   */
  public List<String> getListProp(String propertyName) {

    String value = getStringProp(propertyName);
    return Arrays.asList(value.split(MUTIL_VALUES_DELIMITER));

  }

  /**
   * Get boolean value property
   * 
   * @param propName
   * @return
   */

  public boolean getBooleanProp(String propName) {

    return Boolean.parseBoolean(this.props.getProperty(propName));
  }
}
