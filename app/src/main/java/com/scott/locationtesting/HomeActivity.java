package com.scott.locationtesting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


// TODO: 16/06/2023 Fix all the null/error checks to actually do something




public class HomeActivity extends AppCompatActivity implements LocationListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;
    private LocationManager locationManager;
    private Location currentLocation = null;
    private boolean isLocationUpdatesEnabled = false;
    private Button scanButton;

    public static final String studentId = "";

    public MyDatabaseHelper dbHelper;
    String wasWithinBounds = "False";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Checks if application has location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }

        //Testing
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                MyDatabaseHelper dbHelper = new MyDatabaseHelper(HomeActivity.this);
                final String name = dbHelper.getUserName();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView studentNameTextView = (TextView) findViewById(R.id.studentName);
                        studentNameTextView.setText(name);
                    }
                });
            }
        });


        //Open QR scanner

        scanButton = findViewById(R.id.button_Scan);
        scanButton.setOnClickListener(V ->
        {
            if (scanButton.isEnabled()) {
                scanCode();
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
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



        View view = findViewById(android.R.id.content);
        Snackbar.make(view, "Lat: " + latitude + ", Long: " + longitude, Snackbar.LENGTH_SHORT).show();
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
            String[] QRContents = result.getContents().split(",");
            if (QRContents.length >= 1) {
                try {
                    // Sets class location data based on the information in the QR code.
                    double neLat = Double.parseDouble(QRContents[1]), neLong = Double.parseDouble(QRContents[2]),
                            swLat = Double.parseDouble(QRContents[3]), swLong = Double.parseDouble(QRContents[4]);

                    double bufferInMeters = 20;
                    //Creating the GeoBox
                    GeoBox geoBox = new GeoBox(swLat, swLong, neLat, neLong, bufferInMeters);


                    if (geoBox.contains(currentLocation.getLatitude(), currentLocation.getLongitude())) {
                    //if true just used for easy testing
                    //if(true){
                        //Sends info to sendToWebApi to handle the postrequest
                        wasWithinBounds = "Yes";
                        sendIdToWebApp(Integer.parseInt(QRContents[0]), QRContents[5]);
                    } else {
                        wasWithinBounds = "No";

                        AlertDialog.Builder notAtClassBox = new AlertDialog.Builder(HomeActivity.this);
                        notAtClassBox.setTitle("Failed");
                        notAtClassBox.setMessage("Your location was incorrect, please wait for a location update and try again.");
                        notAtClassBox.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();

                    }
                    // Log attendance here, set text within the If's for the within bounds column

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



    private void sendIdToWebApp(int classId, String className) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                StringBuilder response = new StringBuilder();
                HttpURLConnection conn = null;
                String studentId = "";
                String jwt = "";
                try {
                    String status = "Present";
                    dbHelper = new MyDatabaseHelper(HomeActivity.this);
                    try{
                        String sql = "SELECT * FROM SCANINFO";
                        SQLiteDatabase db = dbHelper.getReadableDatabase();
                        Cursor c = db.rawQuery(sql, null);
                        if (c.moveToFirst()) {
                            studentId = c.getString(1);
                            jwt = c.getString(2);
                        }
                        c.close();


                    } catch (SQLiteException ex){
                        View view = findViewById(android.R.id.content);
                        Snackbar.make(view, "SQL Error", Snackbar.LENGTH_SHORT).show();
                    }

                    URL url = new URL("https://schoolattendanceapi.azurewebsites.net/api/Attendance?classId2=" + classId + "&studentId2=" + studentId + "&newAttendanceStatus=" + status );

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");

                    conn.setRequestProperty("Authorization","Bearer " + jwt);


                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.flush();
                    outputStream.close();

                    Date currentDate = new Date();
                    AttendanceLog logItem = new AttendanceLog(Integer.toString(classId), className, currentDate, wasWithinBounds);
                    dbHelper.insertIntoAttendancelog(logItem);

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                    }

                    String finalStudentId = studentId;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                //builder.setTitle("Success");
                                //builder.setMessage("Response from server: " + response.toString());
                                builder.setTitle("Success");
                                builder.setMessage("Your attendance for " + className +" has been sent");
                            } else {
                                builder.setTitle("Failure");
                                builder.setMessage("Failed to connect to server. Response Code: " + responseCode);
                                AlertDialog.Builder notAtClassBox = new AlertDialog.Builder(HomeActivity.this);
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

                View view = findViewById(android.R.id.content);
                Snackbar.make(view,"Location permission is required for this feature." , Snackbar.LENGTH_SHORT).show();
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

