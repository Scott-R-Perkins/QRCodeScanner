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

/*Make sure to go to the build.gradle(Module:app) file and check for version updates for dependencies, then also sync the gradle file.
Should move some more of the functionality into their own methods
Need to move *some* of the stuff here to a different activity, basically just the scan button+functionaliy
TODO: Use the figma wireframe to design the other intents, get navigation working, then work on storing student info (ID/Name/Whatever else is needed), then try and make it work with web app, when that gets made.
*/


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

        //Checks if application has location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
        //Once permissions have been granted, update user location
       /* if (currentLocation == null) {
            startLocationUpdates();
        }*/

        //Open QR scanner
        //This needs to be adjusted so it can't be opened before location has been obtained
        //Basically just disable the onClick/Grey out the button
        //Could also set a global boolean for "locationObtained" which, when the onLocationChanged method runs and sets the location is set to true
        //the scan button will have a method to return if locationObtained is false. Or could just return is currLocation is null
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
        //this AlertDialog probably isnt needed, it was originally there to check the scanner was reading information correctly, it could still be used to double check the right information
        //is being read after we finalize what the QR string will be
        //Once removed, this method *should* be a lot smaller
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


                    //This one
                    //builder.setMessage("NE Lat: " + Double.parseDouble(classLocations[0]) + "\nNE Lng: " + Double.parseDouble(classLocations[1]) + "\nSW Lat: " + Double.parseDouble(classLocations[2])
                            //+ "\nSW Lng: " + Double.parseDouble(classLocations[3]) + "\nClass Code: " +  classLocations[4] + "\nClass Time: " + classLocations[5] + "\nClass Day: " + classLocations[6]);


                    //maybe hide the scan button/make it unclickable until user location is set/isnt null
                    //Probably have a global boolean for locationSet, if false, returns from the scan method, if true it scans. Init false, set true after location set
                    //Also change button to grey when false, colour when true

                    //Need to add the override methods for onPause/Resume/Start to stop/start location tracking

                    //this is where I'll check if the user is within the bounds or distanceTo location is close enough
                    //then send to another method to handle the http post request
                    String classSession = null;
                    sendIdToWebApp(classSession);

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


    //Currently sends a hardcoded class session and student ID to the httpostreq, which is currently set to httpbin
    //Final version should: Take in classSession string parsed from the QR code, read the studentID from either SharedPreferences or local DB, send these as an httppostreq to our webapp
    private void sendIdToWebApp(String classSession) {
        classSession = "212";
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL("https://httpbin.org/post");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String postData = "2013004474"; //This would also send the class session that is passed into the method from the QR code
            byte[] postDataBytes = postData.getBytes("UTF-8");

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
                }
                reader.close();
            }
            conn.disconnect();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Result");
        builder.setMessage("Info sent: " + "2013004474" + classSession);
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

