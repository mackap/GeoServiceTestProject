package com.mackap.geoserviceproject.net;


import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.mackap.geoserviceproject.BuildConfig;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {

  private RestClient() {
  }

  private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";

  private static final Retrofit retrofit = new Retrofit.Builder()
      .baseUrl(BASE_URL)
      .client(getClient())
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build();
  private static final ApiService apiService = retrofit.create(ApiService.class);


  public static ApiService getApiService() {
    return apiService;
  }

  private static OkHttpClient getClient() {
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    loggingInterceptor.setLevel(getLogLevel());

    return new OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(3, TimeUnit.MINUTES)
        .readTimeout(3, TimeUnit.MINUTES)
        .writeTimeout(3, TimeUnit.MINUTES)
        .build();
  }

  private static HttpLoggingInterceptor.Level getLogLevel() {
    return BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY
        : HttpLoggingInterceptor.Level.NONE;
  }
}
