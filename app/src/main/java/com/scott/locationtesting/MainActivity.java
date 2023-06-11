package com.scott.locationtesting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*Make sure to go to the build.gradle(Module:app) file and check for version updates for dependencies, then also sync the gradle file.
Should move some more of the functionality into their own methods
Need to move *some* of the stuff here to a different activity, basically just the scan button+functionally
TODO: Use the figma wireframe to design the other intents, get navigation working, then work on storing student info (ID/Name/Whatever else is needed), then try and make it work with web app, when that gets made.
 */

// TODO: 6/06/2023 Find a way to disable the scan button while the location has not been obtained, while also
//  providing plenty of visual feedback for the user that their location has not yet been obtained.
//Done, needs to look cleaner.

// TODO: 6/06/2023 Button for users to click to Re-obtain their location should it be in-accurate that the buffer
//  window doesn't cover it. This may not need to be done as it should continue to get location updates
// Maybe? Might not be needed as it should keep updating location.

// TODO: 6/06/2023 Work w/ Rowan to make sure the connection to the backend is working right and sending the
//  right information, After this both sections are separate and I can focus on the mobile app
// This needs done still 07/06/2023

// TODO: 6/06/2023 Create the rest of the Activities and link them all together (app bar or nah?) Look into
//    //  recyclerViews for the local attendance log and local db storage for storing things such as the class
//    //  session/user loc and student ID.
// Need to figure out a 3rd activity that makes sense (Home page with buttons for scan+log)

// TODO: 6/06/2023 Find out why the scanner stopped working for QR's. Fuck
// Think this was just because I was scanning a code that wasnt 4 fields separated by commas
// Broken again? tf
// Think its just a case of the QR code not meeting the specifications for the length in the if

// TODO: 9/06/2023 Hope that the http request works, if so its time to massive crunch the rest of the
//  shit, should be easy from there.

// TODO: 12/06/2023 Add local DB after finishing lab 12. Move everything to a new activity for "Home Page", add login page.
//  Ask JoshGpt4 about adding user token bearer shit with jwt, save the jwt token in the local db.

// TODO: 12/06/2023 Fix UI elements to display information to the user. Use Snackbars, change toast to snackbars.



public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;
    private LocationManager locationManager;
    private Location currentLocation = null;
    private boolean isLocationUpdatesEnabled = false;

    private Button scanButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Checks if application has location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }

        //Open QR scanner

        scanButton = findViewById(R.id.button_Scan);
        scanButton.setOnClickListener(V ->
        {
            if (scanButton.isEnabled()) {
                scanCode();
            }
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

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        // Handle location updates here

        //Look into saving this to the local database maybe? instead of a global variable.
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        TextView tVLocation = findViewById(R.id.textViewBasic2);
        tVLocation.setText("Location obtained");
        tVLocation.setTextColor(Color.rgb(0, 200, 0));
        // Look at adding some sort of loader to indicate to users that it is attempting to find the users location
        scanButton.setEnabled(true);
        // change this to a snackbar
        Toast.makeText(this, "Lat: " + latitude + ", Long: " + longitude, Toast.LENGTH_SHORT).show();
    }


    public void stopLocationUpdates(View view) {
        locationManager.removeUpdates(this);
        isLocationUpdatesEnabled = false;
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
        if (result != null && result.getContents() != null) {
            //this is where the Code is read and things need to be parsed out + added to the localDB
            //Need to figure out how we plan to structure the QR code.
            String[] QRContents = result.getContents().split(",");
            if (QRContents.length >= 1) {
                try {
                    //Need to see if its possible to pause the startLocationUpdates() method onPause(), unless
                    //      it already is paused by default



                    // Sets class location data based on the information in the QR code.
                    double neLat = Double.parseDouble(QRContents[1]), neLong = Double.parseDouble(QRContents[2]),
                            swLat = Double.parseDouble(QRContents[3]), swLong = Double.parseDouble(QRContents[4]);

                    double bufferInMeters = 20;
                    //Creating the GeoBox
                    GeoBox geoBox = new GeoBox(swLat, swLong, neLat, neLong, bufferInMeters);


                    // Checks that the user is within the GeoBox for the classroom
                    //boolean isUserInGeoBox = geoBox.contains(currentLocation.getLatitude(), currentLocation.getLongitude());

                    //If true that the user is in the GeoBox
                    //if (isUserInGeoBox) {
                    //Could maybe make the code smaller by changing this to
                    if (geoBox.contains(currentLocation.getLatitude(), currentLocation.getLongitude())) {
                        //Sends info to sendToWebApi to handle the postrequest
                        int classId = Integer.parseInt(QRContents[1]);
                        sendIdToWebApp(classId);

                        // Maybe look at doing a confirmation box here (above the isUserInGeoBox (would make sure the class is correct before
                        // sending attendance.
                        AlertDialog.Builder notAtClassBox = new AlertDialog.Builder(MainActivity.this);
                        notAtClassBox.setTitle("Cor' Blimey mate");
                        notAtClassBox.setMessage("Your attendance for this class has been sent");
                        notAtClassBox.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                    } else {
                        AlertDialog.Builder notAtClassBox = new AlertDialog.Builder(MainActivity.this);
                        notAtClassBox.setTitle("Cor' Blimey mate");
                        notAtClassBox.setMessage("Get to class you sneaky little shit.");
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
    //Final version should: Take in classSession string parsed from the QR code, read the studentID from either SharedPreferences or local DB,
    // send these as an httppostreq to our webapp


    private void sendIdToWebApp(int classId) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                StringBuilder response = new StringBuilder();
                HttpURLConnection conn = null;
                try {
                    //URL url = new URL("http://192.168.246.52:5078/api/Attendance");
                    String studentId = "4";
                    String status = "Present";

                    //URL url = new URL("http://192.168.246.52:5078/api/Attendance?classId2=" + classId + "&studentId2=" + studentId + "&newAttendanceStatus=" + status);
                    URL url = new URL("https://schoolattendanceapi.azurewebsites.net/api/Attendance?classId2=" + classId + "&studentId2=" + studentId + "&newAttendanceStatus=" + status );


                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    //conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json");

                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.flush();
                    outputStream.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                builder.setTitle("Success");
                                builder.setMessage("Response from server: " + response.toString());
                            } else {
                                builder.setTitle("Failure");
                                builder.setMessage("Failed to connect to server. Response Code: " + responseCode);
                            }
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        });
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
                // May need to remove the toast, as it doesn't align with material design principals
                Toast.makeText(this, "Location permission is required for this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //starting the change details activity
    public void openChangeDetails(View view) {
        Intent intent = new Intent(this, StudentDetailActivity.class);
        startActivity(intent);
    }

    public void openStudentLogs(View view) {
        Intent intent = new Intent(this, StudentLogsActivity.class);
        startActivity(intent);
    }
}




/*
Intructions for getting it to work.
Mobile hotspot on mobile data NOT WIFI
Connect laptop to mobile hotspot, whitelist the laptop on Azure, needs to be done on edge/chrome not ff
Change the BASE_URL here to the ipv4 from ipconfig (this will eventually be a static IP from the azure webservice)
Launch the backend (dotnet run) in the webapi dir
Launch the frontend (npm start) in the reactapp dir
Disable Mcafee firewall temporarily
Launch the mobile app from Android Studio while connected and try to scan

These might need done again
118.148.81.193 ip on azure
192.168.246.52 ip from ipconfig

*/
