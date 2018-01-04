package eu.datacron.insitu.areas;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Polygon;


/**
 * @author ehab.qadah
 */
public class Area implements Serializable {

  private static final long serialVersionUID = -5744572024749473339L;
  /**
   * --------------------------------------------------------------------------------------------
   * Fields
   * --------------------------------------------------------------------------------------------
   **/
  private String id;
  private Polygon polygon;
  public String originalWKT;

  /**
   * --------------------------------------------------------------------------------------------
   * Constructors
   * --------------------------------------------------------------------------------------------
   **/


  public Area(String id, Polygon polygon) {

    this.id = id;
    this.polygon = polygon;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Polygon getPolygon() {
    return polygon;
  }

  public void setPolygon(Polygon polygon) {
    this.polygon = polygon;
  }


}
