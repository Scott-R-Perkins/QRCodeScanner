package com.scott.locationtesting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


// TODO: 20/06/2023 Make this actually do something lol

// TODO: 20/06/2023 Grab current user information from the db and populate the info boxes here

// TODO: 20/06/2023 Submit button chnages the user information in the db then sends them back to the home menu
public class StudentDetailActivity extends AppCompatActivity {

    MyDatabaseHelper dbHelper;
    Cursor c;
    EditText eTName;
    EditText eTAge;
    EditText eTGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        eTName = findViewById(R.id.studentfname_input);
        eTAge = findViewById(R.id.student_age_input);
        //eTGender = findViewById(R.id.student_gender_input);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                dbHelper = new MyDatabaseHelper(StudentDetailActivity.this);
                final String userInfo = dbHelper.getUserInfo();
                String[] userInfoSplit = userInfo.split(",");
                if (userInfoSplit.length >= 2) {
                    try {
                        String name = userInfoSplit[0];
                        int age = Integer.parseInt(userInfoSplit[1]);
                        String gender = userInfoSplit[2];

                        runOnUiThread(() -> {
                            eTName.setText(name);
                            eTAge.setText(String.valueOf(age));
                            //eTGender.setText(gender);
                        });
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    public void handleSubmit(View view) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                MyDatabaseHelper dbHelper = new MyDatabaseHelper(StudentDetailActivity.this);
                try {
                    dbHelper.insertOrUpdateUserInfo(eTName.getText().toString(), Integer.parseInt(eTAge.getText().toString()), "Male");
                    //dbHelper.insertOrUpdateUserInfo(eTName.getText().toString(), Integer.parseInt(eTAge.getText().toString()), eTGender.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });
        Intent intent = new Intent(StudentDetailActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
        c.close();
    }
}