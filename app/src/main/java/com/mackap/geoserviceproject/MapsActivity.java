package com.mackap.geoserviceproject;

import static com.mackap.geoserviceproject.MapsActivity.POINT_NAMES.POINT_A;
import static com.mackap.geoserviceproject.MapsActivity.POINT_NAMES.POINT_B;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.mackap.geoserviceproject.model.PlaceInfo;
import com.mackap.geoserviceproject.model.DataUtils;
import com.mackap.geoserviceproject.model.pojo.GoogleDirectionsResponse;
import com.mackap.geoserviceproject.model.pojo.Leg;
import com.mackap.geoserviceproject.model.pojo.Route;
import com.mackap.geoserviceproject.net.RestClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
    OnConnectionFailedListener {

  enum POINT_NAMES {POINT_A, POINT_B}

  private POINT_NAMES currentPoint;
  private final String TAG = getClass().getSimpleName();
  private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
      new LatLng(-40, -168),
      new LatLng(71, 136));

  private static final float DEFAULT_ZOOM = 15f;
  private GoogleMap mMap;

  private AutoCompleteTextView tvLocationA, tvLocationB;
  private TextView tvDistancevValue, tvTimesValue;
  private Button butGenerateRoad;
  private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
  private GoogleApiClient mGoogleApiClient;
  private GeoDataClient mGeoDataClient;
  private PlaceInfo mPlaceA, mPlaceB;
  private Marker mMarkerA, mMarkerB;
  com.google.android.gms.maps.model.Polyline mPolylineOptions;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);
    tvLocationA = findViewById(R.id.autocomplete_tv_locationA);
    tvLocationB = findViewById(R.id.autocomplete_tv_locationB);
    tvDistancevValue = findViewById(R.id.tv_distance_value);
    tvTimesValue = findViewById(R.id.tv_time_value);
    butGenerateRoad = findViewById(R.id.but_generate_road);
    butGenerateRoad.setOnClickListener(view -> generateRoad());

    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);

    mGoogleApiClient = new GoogleApiClient
        .Builder(this)
        .addApi(Places.GEO_DATA_API)
        .addApi(Places.PLACE_DETECTION_API)
        .enableAutoManage(this, this)
        .build();
    mGeoDataClient = Places.getGeoDataClient(this);

    mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, LAT_LNG_BOUNDS,
        null);

    tvLocationA.setAdapter(mPlaceAutocompleteAdapter);
    tvLocationB.setAdapter(mPlaceAutocompleteAdapter);

    tvLocationA.setOnItemClickListener((adapterView, view, i, l) -> {
      currentPoint = POINT_A;
      processingSearchResult(i);
    });

    tvLocationB.setOnItemClickListener((adapterView, view, i, l) -> {
      currentPoint = POINT_B;
      processingSearchResult(i);
    });
  }

  private void generateRoad() {
    if (mPlaceA != null && mPlaceB != null) {
      clearRoad();
      LatLng origin = mMarkerA.getPosition();
      LatLng dest = mMarkerB.getPosition();
      String strOrigin = origin.latitude + "," + origin.longitude;
      String strDest = dest.latitude + "," + dest.longitude;
      String sensor = "false";
      String apiKey = BuildConfig.GoogleMapKey;

      RestClient.getApiService().getDirectionData(strOrigin, strDest, sensor, apiKey).enqueue(
          new Callback<GoogleDirectionsResponse>() {
            @Override
            public void onResponse(final Call<GoogleDirectionsResponse> call,
                final Response<GoogleDirectionsResponse> response) {

              List<LatLng> routList = new ArrayList<>();
              GoogleDirectionsResponse directResp = response.body();
              long totalDistance = 0;
              int totalSeconds = 0;

              if (directResp != null && directResp.getRoutes() != null) {
                for (Route route : directResp.getRoutes()) {
                 routList.addAll(  PolyUtil.decode(route.getOverviewPolyline().getPoints()));
                  for (Leg leg : route.getLegs()) {
                    totalDistance += leg.getDistance().getValue();
                    totalSeconds += leg.getDuration().getValue();
                  }
                }
              }

              tvDistancevValue.setText(DataUtils.getStringToDistantion(totalDistance));
              tvTimesValue.setText(DataUtils.getStringToTime(totalSeconds));
              mPolylineOptions = mMap.addPolyline(new PolylineOptions().addAll(routList));
            }

            @Override
            public void onFailure(final Call<GoogleDirectionsResponse> call, final Throwable t) {
              Log.d(TAG, "---------------------- onFaulure(), t=" + t.getMessage());

            }
          });

      //move map camera
      mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
      mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
    }
  }

  private void clearRoad() {
    if (mPolylineOptions != null) {
      mPolylineOptions.remove();
    }
  }

  private void processingSearchResult(final int i) {
    final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
    final String placeId = item.getPlaceId();

    PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
        .getPlaceById(mGoogleApiClient, placeId);
    placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
  }

  private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
    @Override
    public void onResult(@NonNull PlaceBuffer places) {
      if (!places.getStatus().isSuccess()) {
        places.release();
        return;
      }
      final Place place = places.get(0);

      PlaceInfo mPlace = null;
      try {
        mPlace = new PlaceInfo();
        mPlace.setName(place.getName().toString());
        mPlace.setAddress(place.getAddress().toString());
        mPlace.setId(place.getId());
        mPlace.setLatlng(place.getLatLng());

      } catch (NullPointerException e) {
        Log.e(TAG, "onResult: NullPointerException: " + e.getMessage());
      }

      if (currentPoint == POINT_A) {
        mPlaceA = mPlace;
      } else if (currentPoint == POINT_B) {
        mPlaceB = mPlace;
      }

      moveCamera(new LatLng(place.getViewport().getCenter().latitude,
          place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace);

      places.release();
    }
  };

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
    mMap.getUiSettings().setZoomControlsEnabled(true);
  }

  @Override
  public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
///// todo show error message
  }

  private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo) {

    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

    if (placeInfo != null) {
      try {
        MarkerOptions options = new MarkerOptions()
            .position(latLng)
            .title(placeInfo.getName());

        if (currentPoint == POINT_A) {
          mMarkerA = mMap.addMarker(options);
          mPlaceA.setLatlng(latLng);
        } else if (currentPoint == POINT_B) {
          mMarkerB = mMap.addMarker(options);
          mPlaceB.setLatlng(latLng);
        }
      } catch (NullPointerException e) {
        e.printStackTrace();
      }
    } else {
      mMap.addMarker(new MarkerOptions().position(latLng));
    }

    hideSoftKeyboard();
  }
  private void hideSoftKeyboard() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
    View view = getCurrentFocus();
    if (view == null) {
      view = new View(this);
    }
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }
}
