package eu.datacron.insitu.common.utils;

import java.lang.reflect.Field;

/**
 * This is a utility class for reflection related methods.
 * 
 * @author ehab.qadah
 */
public class ReflectionUtils {

  private static final String DOUBLE_CLASS = double.class.getName();
  private static final String LONG_CLASS = long.class.getName();
  private static final String INT_CLASS = int.class.getName();

  /**
   * Get the casted field value from the string based on the field type
   * 
   * @param field
   * @param fieldStringValue
   * @return
   */
  public static Object getCastedFieldValue(Field field, String fieldStringValue) {
    String fieldType = field.getType().getName();

    if (fieldType == DOUBLE_CLASS) {
      if (fieldStringValue.isEmpty()) {
        return 0.0;
      }
      return Double.parseDouble(fieldStringValue);

    } else if (fieldType == LONG_CLASS) {
      if (fieldStringValue.isEmpty()) {
        return (long) 0;
      }
      return Long.parseLong(fieldStringValue);

    } else if (fieldType == INT_CLASS) {

      if (fieldStringValue.isEmpty()) {
        return (int) 0;
      }
      // just to parse also the double values as integer
      return (int) Double.parseDouble(fieldStringValue);
    }
    return fieldStringValue;
  }

  /**
   * Private constructor to prevent instantiation.
   */
  private ReflectionUtils() {
    throw new RuntimeException();
  }
}
