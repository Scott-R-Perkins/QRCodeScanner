package com.scott.locationtesting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.List;



public class StudentLogsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_logs);

        RecyclerView recyclerView = findViewById(R.id.my_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
        List<AttendanceLog> attendanceLogs = dbHelper.getAttendanceLogs();

        AttendanceLogAdapter adapter = new AttendanceLogAdapter(attendanceLogs);
        recyclerView.setAdapter(adapter);
    }
}