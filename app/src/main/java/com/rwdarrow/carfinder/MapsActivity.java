package com.rwdarrow.carfinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final int REQUEST_LOCATION_PERMISSIONS = 0;
    private static boolean locationIsSaved = false;
    private static final String TAG = "MapsActivity";
    private final float ZOOM_LEVEL = 15f;

    private GoogleMap mMap;
    private FusedLocationProviderClient mClient;
    private LocationRequest mLocationRequest;
    private LatLng currentLocationLatLng;
    private Toast mLocationNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        FloatingActionButton mFab = findViewById(R.id.saveLocationBtn);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence text;

                if (!locationIsSaved) {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().title("Car Location")
                            .position(currentLocationLatLng));

                    CameraUpdate update = CameraUpdateFactory.
                            newLatLngZoom(currentLocationLatLng, 15);
                    mMap.animateCamera(update);

                    text = "Vehicle location saved";
                } else {
                    mMap.clear();

                    text = "Vehicle location unsaved";
                }

                locationIsSaved = toggleSaveLocation();

                mLocationNotification = Toast.makeText(getApplicationContext(),
                        text, Toast.LENGTH_SHORT);

                mLocationNotification.show();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap mMap) {
        this.mMap = mMap;

        if (hasLocationPermission()) {
            getLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    public void getLocation() {
        mClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (hasLocationPermission()) {
                final Task location = mClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();

                            currentLocationLatLng = new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude());

                            updateMap(currentLocationLatLng, ZOOM_LEVEL);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void updateMap(LatLng latLng, float zoom) {

        // Get current location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        mClient.removeLocationUpdates(mLocationCallback);
//    }
//
//    @SuppressLint("MissingPermission")
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        if (hasLocationPermission()) {
//            mClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
//        }
//    }

    private boolean hasLocationPermission() {

        // Request fine location permission if not already granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this ,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    REQUEST_LOCATION_PERMISSIONS);

            return false;
        }

        return true;
    }

    private boolean toggleSaveLocation() {
        return !locationIsSaved;
    }
}