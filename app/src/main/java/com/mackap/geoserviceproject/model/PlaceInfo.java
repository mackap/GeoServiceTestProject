package com.mackap.geoserviceproject.model;

import android.net.Uri;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Makarov Mikhail on 22.11.2018.
 */
public class PlaceInfo {

  private String name;
  private String address;
  private String id;
  private LatLng latlng;

  public PlaceInfo() {

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }



  public LatLng getLatlng() {
    return latlng;
  }

  public void setLatlng(LatLng latlng) {
    this.latlng = latlng;
  }


}
