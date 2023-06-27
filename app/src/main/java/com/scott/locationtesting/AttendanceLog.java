package com.scott.locationtesting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AttendanceLog {
    private int _id;
    private String classId;
    private String className;
    private Date timeLogged;
    private String withinBounds;


    public AttendanceLog(int _id, String classId, String className, Date timeLogged, String withinBounds){
        this._id = _id; //
        this.classId = classId;
        this.className = className;
        this.timeLogged = timeLogged;
        this.withinBounds = withinBounds;
    }
    public AttendanceLog(String classId, String className, Date timeLogged, String withinBounds){
        this.classId = classId;
        this.className = className;
        this.timeLogged = timeLogged;
        this.withinBounds = withinBounds;
    }
    public AttendanceLog(){

    }

    //Takes the Date variable and converts it into a specified format
    public String getFormattedTimeLogged() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(timeLogged);
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getWithinBounds() {
        return withinBounds;
    }

    public void setWithinBounds(String withinBounds) {
        this.withinBounds = withinBounds;
    }
    public Date getTimeLogged() {
        return timeLogged;
    }

    public void setTimeLogged(Date timeLogged) {
        this.timeLogged = timeLogged;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }
}
