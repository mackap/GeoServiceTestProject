package com.mackap.geoserviceproject.net;

import android.support.annotation.Keep;
import com.mackap.geoserviceproject.BuildConfig;
import com.mackap.geoserviceproject.model.pojo.GoogleDirectionsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


@Keep
public interface ApiService {

  String apiKey = BuildConfig.GoogleMapKey;

  @GET("directions/json?")
  Call<GoogleDirectionsResponse> getDirectionData(
      @Query("origin") String strOrigin,
      @Query("destination") String strDest,
      @Query("sensor") String strSensor,
      @Query("key") String apiKey);
}
