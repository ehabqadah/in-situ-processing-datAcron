package eu.datacron.insitu.areas;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Polygon;

import eu.datacron.insitu.AppUtils;

/**
 * @author ehab.qadah
 */
public class AreasUtils {

  public static final String FIELDS_DELIMITER = "\\|";
  public static final String COORDS_DELIMITER = ",";
  static Logger logger = Logger.getLogger(AreasUtils.class.getName());

  /**
   * Get all static areas
   * 
   * @param polygonFilePath
   * @return
   * @throws IOException
   */
  public static List<Area> getAllAreas(String polygonFilePath) {
    List<Area> areas = new ArrayList<Area>();

    try (BufferedReader br = AppUtils.getResourceReader(polygonFilePath)) {
      String areaLine;

      while ((areaLine = br.readLine()) != null) {

        String[] fields = areaLine.split(FIELDS_DELIMITER);
        String areaID = fields[0];
        Polygon polygon = getPolygonOfArea(Arrays.copyOfRange(fields, 1, fields.length));
        Area newArea = new Area(areaID, polygon);
        // newArea.originalWKT = areaLine;
        areas.add(newArea);
      }
    } catch (IOException e) {

      logger.warn("uable to load areas from" + polygonFilePath, e);
    }

    return areas;
  }

  private static Polygon getPolygonOfArea(String[] coordinates) {
    String polygonWKT = "POLYGON ((";


    for (String coord : coordinates) {

      polygonWKT += coord.replace(COORDS_DELIMITER, " ") + COORDS_DELIMITER;

    }
    polygonWKT += coordinates[0].replace(COORDS_DELIMITER, " ") + "))";
    return GeoUtils.getPolygon(polygonWKT);
  }
}
