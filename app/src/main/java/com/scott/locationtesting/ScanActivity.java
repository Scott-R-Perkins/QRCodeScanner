package com.scott.locationtesting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ScanActivity extends AppCompatActivity implements LocationListener  {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;
    private LocationManager locationManager;
    private Location currentLocation = null;
    private TextView textViewLocation;
    private boolean isLocationUpdatesEnabled = false;

    // TODO: 6/06/2023 Find a way to disable the scan button while the location has not been obtained, while also
    //  providing plenty of visual feedback for the user that their location has not yet been obtained.

    // TODO: 6/06/2023 Button for users to click to Re-obtain their location should it be in-accurate that the buffer
    //  window doesn't cover it. This may not need to be done as it should continue to get location updates

    // TODO: 6/06/2023 Work w/ Rowan to make sure the connection to the backend is working right and sending the
    //  right information, After this both sections are separate and I can focus on the mobile app

    // TODO: 6/06/2023 Create the rest of the Activities and link them all together (app bar or nah?) Look into
    //    //  recyclerViews for the local attendance log and local db storage for storing things such as the class
    //    //  session/user loc and student ID.

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Checks if application has location permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
        //Once permissions have been granted, update user location
        //this may not be needed anymore
       /* if (currentLocation == null) {
            startLocationUpdates();
        }*/

        //Open QR scanner
        //This needs to be adjusted so it can't be opened before location has been obtained
        //Basically just disable the onClick/Grey out the button
        //Could also set a global boolean for "locationObtained" which, when the onLocationChanged method runs and sets the location is set to true
        //the scan button will have a method to return if locationObtained is false. Or could just return is currLocation is null

        Button scanButton = findViewById(R.id.button_Scan);
        scanButton.setOnClickListener(V ->
        {
            scanCode();
        });


    }

    // Start location updates when the run
    public void startLocationUpdates() {
        // Request location updates every 10 seconds or when the user has moved at least 10 meters
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        // this is where we would add the users location to the local DB so that it can be accessed later
        // during the location check.
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        textViewLocation.setText("Lat: " + latitude + ", Lng: " + longitude);
        Toast.makeText(this, "Lat: " + latitude + ", Long: " + longitude, Toast.LENGTH_SHORT).show();
    }


    private void scanCode() {

        if (getApplicationContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
        //this AlertDialog probably isn't needed, it was originally there to check the scanner was reading information correctly, it could still be used to double check the right information
        //is being read after we finalize what the QR string will be
        //Once removed, this method *should* be a lot smaller
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
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


                    //This one
                    //builder.setMessage("NE Lat: " + Double.parseDouble(classLocations[0]) + "\nNE Lng: " + Double.parseDouble(classLocations[1]) + "\nSW Lat: " + Double.parseDouble(classLocations[2])
                    //+ "\nSW Lng: " + Double.parseDouble(classLocations[3]) + "\nClass Code: " +  classLocations[4] + "\nClass Time: " + classLocations[5] + "\nClass Day: " + classLocations[6]);


                    //maybe hide the scan button/make it not clickable until user location is set/isn't null
                    //Probably have a global boolean for locationSet, if false, returns from the scan method, if true it scans. Init false, set true after location set
                    //Also change button to grey when false, colour when true

                    //Need to add the override methods for onPause/Resume/Start to stop/start location tracking

                    //this is where I'll check if the user is within the bounds or distanceTo location
                    // is close enough
                    //These are set from the 3rd/5th member, will be obtaining these from the QR code
                    double swLat = 34.0522, swLong = -118.2437;  // Los Angeles
                    double neLat = 40.7128, neLong = -74.0060;  // New York
                    double bufferInMeters = 20;
                    GeoBox geoBox = new GeoBox(swLat, swLong, neLat, neLong, bufferInMeters);

                    double userLat = currentLocation.getLatitude(),
                            userLong = currentLocation.getLongitude();  // userLocation infomation
                    boolean isUserInGeoBox = geoBox.contains(userLat, userLong);

                    //If true that the user is in the GeoBox
                    if(isUserInGeoBox){
                        //then send to another method to handle the http post request
                        int classId = 0; //This may not need to be passed/expected in the method, once I figure
                                            //out saving information to the local db
                        sendIdToWebApp(classId);
                    }
                    else{
                        AlertDialog.Builder notAtClassBox = new AlertDialog.Builder(ScanActivity.this);
                        notAtClassBox.setTitle("Cor' Blimey mate");
                        notAtClassBox.setMessage("Get to class you sneaky little shit, However, if you " +
                                "are there try clicking the 'Re-obtain location' button and try again");
                        notAtClassBox.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                    }





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
    });


    //Currently sends a hardcoded class session and student ID to the httpostreq, which is currently set to httpbin
    //Final version should: Take in classSession string parsed from the QR code, read the studentID from either SharedPreferences or local DB, send these as an httppostreq to our webapp
    private void sendIdToWebApp(int classId) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL("https://httpbin.org/post");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");



            classId= 2;
            String studentId = "4";
            String status = "present";
            String postData = "classId=" + URLEncoder.encode(Integer.toString(classId), "UTF-8")
                    + "&studentId=" + URLEncoder.encode(studentId, "UTF-8")
                    + "&status=" + URLEncoder.encode(status, "UTF-8");
            byte[] postDataBytes = postData.getBytes("UTF-8");


            //String postData = "2013004474"; //This would also send the class session that is passed into the method from the QR code
            //byte[] postDataBytes = postData.getBytes("UTF-8");

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(postDataBytes);
            outputStream.flush();
            outputStream.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                    AlertDialog.Builder responseBuilder = new AlertDialog.Builder(ScanActivity.this);
                    responseBuilder.setTitle("Response from backend");
                    responseBuilder.setMessage(response.toString());
                    responseBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
                }
                reader.close();
            }
            conn.disconnect();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
        builder.setTitle("Result");
        builder.setMessage("Info sent: " + "2013004474" + classId);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates();
            } else {
                // Permission denied, show an appropriate message
                Toast.makeText(this, "Location permission is required for this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}