package com.mackap.geoserviceproject.model;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Makarov Mikhail on 22.11.2018.
 */
public class DataUtils {

  private DataUtils() {
  }

  public static String getStringToDistantion(long distantion) {
    StringBuilder stringBuilder = new StringBuilder();
    int kmValue = (int) distantion / 1000;
    if (kmValue >0) {
      stringBuilder.append(kmValue + " km ");
    }
    long metersValue = distantion - (kmValue * 1000);
    if (metersValue > 0) {
      stringBuilder.append(metersValue + " m");
    }
    return stringBuilder.toString();
  }

  public static String getStringToTime(long totalSeconds){
    int days = (int) totalSeconds / 86400;
    int hours = (int)(totalSeconds - days * 86400) / 3600;
    int minutes = (int) (totalSeconds - days * 86400 - hours * 3600) / 60;

    StringBuilder stringBuilder = new StringBuilder();
    if(days>0){
      stringBuilder.append(days+" days ");
    }
    if(hours>0){
      stringBuilder.append(hours+" hours ");
    }
    if(minutes>0){
      stringBuilder.append(minutes+" min");
    }
    return stringBuilder.toString();
  }

}
