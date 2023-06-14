package com.scott.locationtesting;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: 14/06/2023 New login page if you see it before I mention it @Missandu, all the code from the previous
//  MainActivity is now within HomeActivity, along with the activity_main.xml being moved to activity_home

//Few things to check before committing, mainly that the app still functions.

// TODO: 14/06/2023 Create the login form on this page, with the http post request to the server and token grabbing
//  + saving to the db

// TODO: 14/06/2023 From the login, save the token, name and user id in the database.
//  Then on home page can pull those out to display and for sending attendance.

// TODO: 14/06/2023 Figure out if its possible to auto-login the user if they still have an active token

public class MainActivity extends AppCompatActivity {

    public static boolean canlogIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void handleLogin(View view) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                StringBuilder response = new StringBuilder();
                HttpURLConnection conn = null;
                try {
                    EditText editTextEmail = findViewById(R.id.editEmail);
                    //Read these from edit texts
                    //String user = "Ken@gmail.com";
                    String user = editTextEmail.getText().toString();
                    EditText editTextPassword = findViewById(R.id.editPassword);
                    //String pass = "bigchung";
                    String pass = editTextPassword.getText().toString();
                    URL url = new URL("https://schoolattendanceapi.azurewebsites.net/api/Login?email=" + user +"&pass=" + pass);


                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
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
                            try {
                                JSONObject jsonObject = new JSONObject(String.valueOf(response));
                                if (!jsonObject.isNull("token")) {
                                    String token = jsonObject.getString("token");
                                    //Need to figure out how to get the userInfo stuff out so I can save it in the DB
                                    //Save data to database here, can use json
                                    //Put a loader here
                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    startActivity(intent);

                                } else if (!jsonObject.isNull("errorMessage")) {
                                    String errorMessage = jsonObject.getString("errorMessage");
                                    builder.setTitle("Failed login");
                                    builder.setMessage("Incorrect username or password, please try again.");

                                }
                            } catch (JSONException e) {
                                // handle the exception
                            }builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
}