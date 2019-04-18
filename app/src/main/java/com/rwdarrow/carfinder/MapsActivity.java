package com.rwdarrow.carfinder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final int REQUEST_LOCATION_PERMISSIONS = 0;
    private boolean locationIsSaved;
    private final String TAG = MapsActivity.class.getSimpleName();
    private final float ZOOM_LEVEL = 17f;
    private final int UPDATE_INTERVAL = 500;
    private final int FAST_UPDATE_INTERVAL = 100;

    private GoogleMap mMap;
    private FusedLocationProviderClient mClient;
    private LocationRequest mLocationRequest;
    private LatLng currentLocationLatLng;
    private Toast mLocationNotification;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        final View mFabNote = findViewById(R.id.addNoteBtn);

        // initialize saved car location
        sp = getSharedPreferences("carLocation", Context.MODE_PRIVATE);
        editor = sp.edit();

        FloatingActionButton mFabSave = findViewById(R.id.saveLocationBtn);
        mFabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence text;

                if (!locationIsSaved) {
                    getLocation();

                    mFabNote.setVisibility(View.VISIBLE);

                    // saved the location to shared preferences file
                    editor.putFloat("LAT", (float) currentLocationLatLng.latitude);
                    editor.putFloat("LONG", (float) currentLocationLatLng.longitude);
                    editor.putBoolean("IS_SAVED", true);
                    editor.apply();

                    // clear the map and add the marker in the new location
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().title("Car Location")
                            .position(currentLocationLatLng));

                    CameraUpdate update = CameraUpdateFactory.
                            newLatLngZoom(currentLocationLatLng, 15);
                    mMap.animateCamera(update);

                    text = "Vehicle location saved";
                } else {
                    mMap.clear();
                    editor.clear();

                    mFabNote.setVisibility(View.GONE);

                    text = "Vehicle location unsaved";
                }

                locationIsSaved = toggleSaveLocation();

                mLocationNotification = Toast.makeText(getApplicationContext(),
                        text, Toast.LENGTH_SHORT);

                mLocationNotification.show();
            }
        });

        mFabNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = LayoutInflater.from(MapsActivity.this);
                View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog
                        .Builder(MapsActivity.this);
                alertDialogBuilder.setView(promptView);

                final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
                editText.setText(sp.getString("NOTE", ""));

                // setup a dialog window
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                editor.putString("NOTE", editText.getText().toString());
                                editor.apply();
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create an alert dialog
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FAST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap mMap) {
        this.mMap = mMap;

        // determine whether a location has been saved
        locationIsSaved = sp.getBoolean("IS_SAVED", false);

        // if it has, display saved location on map
        if (locationIsSaved) {
            mMap.addMarker(new MarkerOptions().title("Car Location")
                    .position(new LatLng(sp.getFloat("LAT", 0),
                            sp.getFloat("LONG", 0))));

            View mFabNote = findViewById(R.id.addNoteBtn);
            mFabNote.setVisibility(View.VISIBLE);
        }

        if (hasLocationPermission()) {
            getLocation();

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
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