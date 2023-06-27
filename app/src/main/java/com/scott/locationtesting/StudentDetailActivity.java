package com.scott.locationtesting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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
    RadioGroup rBGGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        eTName = findViewById(R.id.editName);
        eTAge = findViewById(R.id.editAge);
        rBGGender = findViewById(R.id.studentgender_input);

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
                            if (gender.equalsIgnoreCase(getString(R.string.female_input_rg))) {
                                ((RadioButton) findViewById(R.id.radioButton)).setChecked(true);
                            } else if (gender.equalsIgnoreCase(getString(R.string.male_input_rg))) {
                                ((RadioButton) findViewById(R.id.radioButton2)).setChecked(true);
                            }
                            //eTGender.setText(gender);
                        });
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


  /*  public void handleSubmit(View view) {
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
    }*/
  public void handleSubmit(View view) {
      Executor executor = Executors.newSingleThreadExecutor();
      executor.execute(new Runnable() {
          @Override
          public void run() {
              String selectedGender = ((RadioButton) findViewById(rBGGender.getCheckedRadioButtonId())).getText().toString();

              MyDatabaseHelper dbHelper = new MyDatabaseHelper(StudentDetailActivity.this);
              try {
                  dbHelper.insertOrUpdateUserInfo(eTName.getText().toString(), Integer.parseInt(eTAge.getText().toString()), selectedGender);
              } catch (NumberFormatException e) {
                  e.printStackTrace();
              }
          }
      });

      Intent intent = new Intent(StudentDetailActivity.this, HomeActivity.class);
      intent.putExtra("defaultGender", ((RadioButton) findViewById(rBGGender.getCheckedRadioButtonId())).getText().toString());
      startActivity(intent);
  }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
        c.close();
    }
}