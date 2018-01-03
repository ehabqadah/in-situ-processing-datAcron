package eu.datacron.insitu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.json.JSONObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import eu.datacron.insitu.areas.Area;
import eu.datacron.insitu.areas.AreasUtils;
import eu.datacron.insitu.common.utils.Configs;

/**
 * @author ehab.qadah
 */
public class TestApp {

  private static Configs configs = Configs.getInstance();

  public static void main(String[] args) throws IOException {

    int i = 0;
    try (BufferedReader br =
        new BufferedReader(new FileReader("/opt/datAcron/insitu/data/output/nari_out.csv"))) {
      String messageLine;
      while ((messageLine = br.readLine()) != null) {
        i++;
        System.out.println(messageLine.split(",").length);
      }

    }

    System.out.println("File lines:" + i);
    List<Area> areas = AreasUtils.getAllAreas("static-data/polygons.csv");
    System.out.println(areas.size());
    // for (Area area : areas) {
    //
    // byte[] messageBytes=(area.getId()+"|"+area.getPolygon().toText()+"\n").getBytes();
    // Files.write(Paths.get("static-data/areas.csv"), messageBytes, StandardOpenOption.APPEND);
    // }

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

    InputStream input = null;
    input = Configs.class.getResourceAsStream("/IMIS_Global_CSV_Schema.json");


    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    StringBuilder out = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      out.append(line);
    }
    JSONObject criticalPointJson = new JSONObject(out.toString());
    System.out.println(out.toString()); // Prints the string content read from input stream
    reader.close();



  }



}
