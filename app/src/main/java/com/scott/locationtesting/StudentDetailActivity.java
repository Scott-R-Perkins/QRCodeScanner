package com.scott.locationtesting;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

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


        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                MyDatabaseHelper dbHelper = new MyDatabaseHelper(StudentDetailActivity.this);
                final String userInfo = dbHelper.getUserInfo();
                String[] userInfoSplit = userInfo.split(",");
                if (userInfoSplit.length >= 1) {
                    try {
                        String name = userInfoSplit[0];
                        int age = Integer.parseInt(userInfoSplit[1]);
                        String gender = userInfoSplit[2];

                        eTName = findViewById(R.id.studentfname_input);
                        eTAge = findViewById(R.id.student_age_input);
                        //Look at changing the gender to a dropdown or something, that way can set the default text and use a string array for the options?
                    }
                    catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }

    public void handleSubmit(){
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                MyDatabaseHelper dbHelper = new MyDatabaseHelper(StudentDetailActivity.this);
                try{
                    dbHelper.insertOrUpdateUserInfo(eTName.getText().toString(), Integer.parseInt(eTAge.getText().toString()), eTGender.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
        c.close();
    }
}