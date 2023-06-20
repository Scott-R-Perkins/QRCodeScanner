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
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


// TODO: 14/06/2023 Figure out if its possible to auto-login the user if they still have an active token

public class MainActivity extends AppCompatActivity {

    String token = null;
    String errorMessage = null;
    int userId = 0;
    int studentId = 0;
    String userName = null;

    public MyDatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    public void handleLogin(View view) {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                StringBuilder response = new StringBuilder();
                HttpURLConnection conn = null;
                try {
                    EditText editTextEmail = findViewById(R.id.editEmail);
                    String user = editTextEmail.getText().toString();
                    EditText editTextPassword = findViewById(R.id.editPassword);
                    String pass = editTextPassword.getText().toString();
                    URL url = new URL("https://schoolattendanceapi.azurewebsites.net/api/Login?email=" + user + "&pass=" + pass);


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
                            try {
                                JSONObject jsonObject = new JSONObject(String.valueOf(response));
                                if (!jsonObject.isNull("token")) {
                                    token = jsonObject.getString("token");
                                    errorMessage = jsonObject.getString("errorMessage");
                                    //Put a loader here until checks and db saving is done
                                if(!jsonObject.isNull("userInfo")){
                                    JSONObject userDTO = jsonObject.getJSONObject("userInfo");
                                    //if(!userDTO.isNull("userId") && !userDTO.isNull("userName") && !userDTO.isNull("studentId")) {
                                    if(!userDTO.isNull("userName")) {
                                        userId = userDTO.getInt("userId");
                                        userName = userDTO.getString("userName");
                                        studentId = userDTO.getInt("studentId");
                                        //Could be causing an error when logging in as ken bc he doesn't have a studentId? idk


                                        //Save to database here, Token, userId and userName
                                        dbHelper = new MyDatabaseHelper(MainActivity.this);
                                        dbHelper.insertOrUpdateScanInfo(studentId, token);
                                        dbHelper.insertOrUpdateUserInfo(userName, 0, "Unspecified");

                                       /* if(studentId != 0){
                                            dbHelper.insertOrUpdateScanInfo(studentId, token);
                                        } else {
                                            dbHelper.insertOrUpdateScanInfo(userId, token);
                                        }*/


                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                    }
                                }
                                    progressBar.setVisibility(View.GONE);
                                }
                                else if (token == null && !jsonObject.isNull("errorMessage")) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    errorMessage = jsonObject.getString("errorMessage");
                                    builder.setTitle("Failed login");
                                    builder.setMessage("Incorrect username or password, please try again.");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            } catch (JSONException e) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Error");
                                builder.setMessage("An unexpected error occurred, please try again");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                                progressBar.setVisibility(View.GONE);
                            }

                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        });
    }
}