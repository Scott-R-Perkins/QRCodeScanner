package com.scott.locationtesting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.BarcodeFormat;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;


public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;
    private LocationManager locationManager;
    private Location currentLocation = null;
    private TextView textViewLocation;
    private boolean isLocationUpdatesEnabled = false;


    private Button scanButton;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewLocation = findViewById(R.id.textViewBasic);
        textViewLocation.setText("Waiting for location...");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }

        if(currentLocation == null){
            startLocationUpdates();
        }

        scanButton = findViewById(R.id.button_Scan);
        scanButton.setOnClickListener(V ->
        {
            scanCode();
        });


    }

    // Start location updates when the run
    public void startLocationUpdates() {
        // Request location updates every 10 seconds or when the user has moved at least 10 meters
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (!isLocationUpdatesEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 10F, this);
            isLocationUpdatesEnabled = true;
        }
    }

    // Stop location updates when the button is released
    public void stopLocationUpdates(View view) {
        locationManager.removeUpdates(this);
        isLocationUpdatesEnabled = false;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        // Handle location updates here
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        textViewLocation.setText("Lat: " + latitude + ", Lng: " + longitude);
        Toast.makeText(this, "Lat: " + latitude + ", Long: " + longitude, Toast.LENGTH_SHORT).show();
    }


    private void scanCode() {

        if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Location permissions are on
            ScanOptions options = new ScanOptions();
            options.setPrompt("Volume up to turn on flash");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureAct.class);
            barLauncher.launch(options);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); // may need to change 1
        }


    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Result");
        if (result != null && result.getContents() != null) {
            String[] classLocations = result.getContents().split(",");
            if (classLocations.length >= 4) {
                try {
                    Location classNEBound = new Location("");
                    classNEBound.setLatitude(Double.parseDouble(classLocations[0]));
                    classNEBound.setLongitude(Double.parseDouble(classLocations[1]));

                    Location classSWBound = new Location("");
                    classSWBound.setLatitude(Double.parseDouble(classLocations[2]));
                    classSWBound.setLongitude(Double.parseDouble(classLocations[3]));
                    //builder.setMessage("NE Lat: " + Double.parseDouble(classLocations[0]) + " \nNE Lng: " + Double.parseDouble(classLocations[1]) + "\nSW Lat: " + Double.parseDouble(classLocations[2])
                            //+ " \nSW Lng: " + Double.parseDouble(classLocations[3]));
                    builder.setMessage("NE Lat: " + Double.parseDouble(classLocations[0]) + "\nNE Lng: " + Double.parseDouble(classLocations[1]) + "\nSW Lat: " + Double.parseDouble(classLocations[2])
                            + "\nSW Lng: " + Double.parseDouble(classLocations[3]) + "\nClass Code: " +  classLocations[4] + "\nClass Time: " + classLocations[5] + "\nClass Day: " + classLocations[6]);


                    //maybe hide the scan button/make it unclickable until user location is set/isnt null
                    //Probably have a global boolean for locationSet, if false, returns from the scan method, if true it scans. Init false, set true after location set
                    //Also change button to grey when false, colour when true

                    //Need to add the override methods for onPause/Resume/Start to stop/start location tracking

                    //this is where I'll check if the user is within the bounds or distanceTo location is close enough
                    //then send to another method to handle the http post request

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    // handle parsing error
                }
            } else {
                // handle invalid location data
            }
        } else {
            // handle null result
        }
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    });
}

