package com.scott.locationtesting;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AttendanceLogAdapter extends RecyclerView.Adapter<AttendanceLogAdapter.AttendanceLogViewHolder> {
    private List<AttendanceLog> attendanceLogs;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    // Provide a reference to the views for each data item
    public static class AttendanceLogViewHolder extends RecyclerView.ViewHolder {
        public TextView classId, className, scanTime, withinBounds;

        public AttendanceLogViewHolder(View v) {
            super(v);
            classId = v.findViewById(R.id.text_class_id);
            className = v.findViewById(R.id.text_class_name);
            scanTime = v.findViewById(R.id.text_scan_time);
            withinBounds = v.findViewById(R.id.text_withinBounds);
        }
    }

    public AttendanceLogAdapter(List<AttendanceLog> attendanceLogs) {
        this.attendanceLogs = attendanceLogs;
    }

    @NonNull
    @Override
    public AttendanceLogAdapter.AttendanceLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_log_item, parent, false);
        return new AttendanceLogViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(AttendanceLogViewHolder holder, int position) {
        AttendanceLog log = attendanceLogs.get(position);
        holder.classId.setText("Class Id: " + log.getClassId());
        holder.className.setText("Class Name: " + log.getClassName());
        holder.scanTime.setText("Time scanned: " + dateFormat.format(log.getTimeLogged()));
        holder.withinBounds.setText("User was at location: " + log.getWithinBounds());
    }

    @Override
    public int getItemCount() {
        return attendanceLogs.size();
    }
}
