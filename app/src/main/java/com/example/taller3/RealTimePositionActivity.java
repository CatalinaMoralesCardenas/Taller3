package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RealTimePositionActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Permisos
    String permLocation = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_PERMISSION_ID = 15;

    //Sensores && Firebase
    SensorManager sensorManager;
    Sensor lightSensor;
    private DatabaseReference dbReference;

    //Settings
    private static final int SETTINGS_GPS = 20;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient clientLocation;
    private Marker locationMarker;
    private Marker otherLocation;
    private LatLng posicionActual = null;
    private LatLng otherLatlng = null;
    private double latitud;
    private double longitud;

    // Variables
    private GoogleMap mMap;
    String userID;
    String name;
    double latitude;
    double longitude;
    int contador = 0;
    double dist = 0.0;

    private TextView distanceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_position);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null ) {
            userID = bundle.getString("userKey");
        } else {
            finish();
        }

        distanceText = findViewById(R.id.positionText);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapPosition);
        mapFragment.getMapAsync(this);

        clientLocation = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = createLocationRequest();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateLocation();
                }
            }
        };
    }

    //Subscripciones
    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            clientLocation.requestLocationUpdates(locationRequest, locationCallback, null); }
    }

    private void stopLocationUpdates(){
        clientLocation.removeLocationUpdates(locationCallback);

    }

    private LocationRequest createLocationRequest() {
        LocationRequest myRequest = new LocationRequest();
        myRequest.setInterval(1000);
        myRequest.setFastestInterval(5000);
        myRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return myRequest;
    }

    private void updateLocation() {
        if (mMap != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

                Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(RealTimePositionActivity.this).checkLocationSettings(builder.build());

                result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                        try {

                            LocationSettingsResponse response = task.getResult(ApiException.class);

                            if (ActivityCompat.checkSelfPermission(RealTimePositionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RealTimePositionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                clientLocation.getLastLocation().addOnSuccessListener(RealTimePositionActivity.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            Log.i(" LOCATION ", "Longitud: " + location.getLongitude());
                                            Log.i(" LOCATION ", "Latitud: " + location.getLatitude());
                                            marcadorUbicacion(location);
                                        }
                                    }
                                });
                            }

                        } catch (ApiException exception) {
                            switch (exception.getStatusCode()) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        ResolvableApiException resolvable = (ResolvableApiException) exception;
                                        resolvable.startResolutionForResult(RealTimePositionActivity.this, LocationRequest.PRIORITY_HIGH_ACCURACY);
                                    } catch (IntentSender.SendIntentException e) {
                                    } catch (ClassCastException e) {
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    break;
                            }
                        }
                    }
                });
            }
        }
    }

    private void marcadorUbicacion(Location location) {
        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (locationMarker != null) {
            locationMarker.remove();
        } else {
            posicionActual = myLocation;
        }
        if (myLocation != posicionActual) {
            latitud = myLocation.latitude;
            longitud = myLocation.longitude;
            posicionActual = myLocation;
        }
        locationMarker = mMap.addMarker(new MarkerOptions().position(myLocation).title("Tú Ubicación").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        if (contador == 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(12));
            contador++;
        }
        showDistance();
    }

    private void showDistance(){
        if(otherLocation != null && posicionActual != null ) {
            dist = distance(otherLatlng.latitude, otherLatlng.longitude, posicionActual.latitude, posicionActual.longitude);
            distanceText.setText("Distancia: " + dist + "Km");
        }
    }

    private void checkSettingsLocation(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                int statusCode = ((ApiException)e).getStatusCode();
                switch (statusCode){
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(RealTimePositionActivity.this,SETTINGS_GPS );
                        } catch (IntentSender.SendIntentException sendEx) {
                        } break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SETTINGS_GPS){
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOCATION_PERMISSION_ID){
            initView();
        }
    }

    private void initView(){
        if(ContextCompat.checkSelfPermission(this, permLocation)== PackageManager.PERMISSION_GRANTED) {
            checkSettingsLocation();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        dbReference = FirebaseDatabase.getInstance().getReference("users/" + userID);
        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    latitude = Double.parseDouble(snapshot.child("lalitude").getValue().toString());
                    longitude = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                    name = snapshot.child("name").getValue().toString() + snapshot.child("lastname").getValue().toString();
                }

                sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

                if (ActivityCompat.checkSelfPermission(RealTimePositionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(RealTimePositionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                LatLng position = new LatLng(latitude,longitude);
                otherLatlng = position;
                placeMarker(position);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }

    private void placeMarker(LatLng position) {
        if (otherLocation != null) {
            otherLocation.remove();
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(12));
        }

        otherLocation = mMap.addMarker(new MarkerOptions().position(position).title("User Position"));
        showDistance();
    }

    public double distance(double lat1, double long1, double lat2, double long2) {
        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = 6371 * c;
        return Math.round(result*100.0)/100.0;
    }
}