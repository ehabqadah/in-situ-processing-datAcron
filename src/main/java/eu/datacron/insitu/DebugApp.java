package eu.datacron.insitu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.geometry.jts.JTSFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import eu.datacron.insitu.areas.Area;
import eu.datacron.insitu.areas.AreasUtils;
import eu.datacron.insitu.areas.GeoUtils;
import eu.datacron.insitu.common.utils.Configs;

/**
 * @author ehab.qadah
 */
public class DebugApp {

  private static Configs configs = Configs.getInstance();

  public static void main(String[] args) throws IOException {

    List<Area> areas = AreasUtils.getAllAreas("static-data/polygons.csv");
    Map<String, Boolean> areasMap = new HashMap<>();
    int i = 0;
    try (BufferedReader br =
        new BufferedReader(new FileReader("/opt/datAcron/insitu/data/output/nari_out.csv"))) {
      String messageLine;
      while ((messageLine = br.readLine()) != null) {
        i++;
        String[] split = messageLine.split(",");
        if (split.length == 29) {
          String areasStr = split[28];
          Set<Area> newDetectedAreas = new HashSet<Area>();
          for (Area area : areas) {

            boolean x =
                GeoUtils.isPointInPolygon(area.getPolygon(), Double.parseDouble(split[2]),
                    Double.parseDouble(split[3]));
            if (x == true) {
              if (!areasStr.contains(area.getId()))
                System.out.println("old:" + areasStr + "\nnew:" + area.getId());
            }
          }


        }
      }

    }
    //
    System.out.println(areasMap.keySet().size());
    System.out.println("File lines:" + i);

    System.out.println("areas=" + areas.size());
    StringBuilder filterAreas = new StringBuilder();
    long start = System.currentTimeMillis();

    System.out.println("time= " + (System.currentTimeMillis() - start));
    byte[] messageBytes = filterAreas.toString().getBytes();
    // Files.write(Paths.get("src/main/resources/static-data/new-ploygons.csv"), messageBytes,
    // StandardOpenOption.CREATE_NEW);
    String test =
        "area1488486400|-3.599999999999999,49.800705375|-3.604459,49.800148|-3.6047180123333384,49.8|-3.6999999999999993,49.8|-3.6999999999999993,49.9|-3.599999999999999,49.9";
    String[] splits = test.split("\\|");

    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    Coordinate[] coords =
        new Coordinate[] {new Coordinate(4, 0), new Coordinate(2, 2), new Coordinate(4, 4),
            new Coordinate(6, 2), new Coordinate(4, 0)};

    LinearRing ring = geometryFactory.createLinearRing(coords);
    LinearRing holes[] = null; // use LinearRing[] to represent holes
    Polygon polygon = geometryFactory.createPolygon(ring, holes);

    System.out.println(splits);
    String[] fields =
        "1453984747,1,215130000,1,29.2796666666667,40.8366833333333,307,0,,".split(",", -1);
    System.out.println(fields.length);
    System.out.println(configs.getStringProp("streamSourceType"));

  }
}
