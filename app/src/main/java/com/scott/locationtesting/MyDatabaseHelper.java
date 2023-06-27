package com.scott.locationtesting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "localData";
    private static final int DB_VERSION = 1;
    private static final String ATTENDANCELOG = "ATTENDANCELOG";
    //Check with Josh how this works with updating the database with new records, may have to update the version with every new log posted?
    //Should the logs have a smaller compact view in the list, expanding to a bigger more detailed view? or just display classId/Name/Sign in time and id used in the main list view.

    MyDatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // this table is used to store the userID and Token required for scanning
        db.execSQL("CREATE TABLE SCANINFO (_id INTEGER PRIMARY KEY AUTOINCREMENT, USER_ID INTEGER, CURRENT_TOKEN TEXT)");
        // This table is used to store personal information about the student
        //Maybe also look into storing the img in here for the profile picture thing?
        db.execSQL("CREATE TABLE USERINFO (_id INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, AGE INTEGER, GENDER TEXT)");
        // This table is used to keep information from every attempted attendance scan and is then later displayed in StudentLogActivity
        db.execSQL("CREATE TABLE ATTENDANCELOG(_id INTEGER PRIMARY KEY AUTOINCREMENT, CLASSID TEXT, CLASS_NAME TEXT, STUDENT_ID TEXT, SCAN_TIME TEXT, WITHIN_BOUNDS TEXT)");
    }

    public long insertOrUpdateScanInfo(int userId, String token) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("USER_ID", userId);
        values.put("CURRENT_TOKEN", token);

        long rowId = db.insertWithOnConflict("SCANINFO", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        if (rowId != -1) {
            return db.update("SCANINFO", values, null, null);
        } else {
            return rowId;
        }
    }




    public long insertOrUpdateUserInfo(String name, int age, String gender){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NAME", name);
        values.put("AGE", age);
        values.put("GENDER", gender);
        long rowId = db.insertWithOnConflict("USERINFO", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        if(rowId != -1){
            return db.update("USERINFO", values, null, null);
        } else {
            return rowId;
        }
    }


    public boolean recordExists(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM USERINFO";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.getCount()>0){
            return true;
        }else{
            return false;
        }
    }

    public long insertIntoAttendancelog(AttendanceLog log){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("CLASSID", log.getClassId());
        values.put("CLASS_NAME", log.getClassName());;
        values.put("SCAN_TIME", log.getFormattedTimeLogged());
        values.put("WITHIN_BOUNDS",log.getWithinBounds());
        return db.insert("ATTENDANCElOG", null, values);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public String getUserName() {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM USERINFO LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);
        String name = "";

        if (cursor.moveToFirst()) {
            name = cursor.getString(1); // Getting the first column value.
        }
        cursor.close();

        return name;
    }

    public String getUserInfo() {
        StringBuilder userInfoString = new StringBuilder();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM USERINFO LIMIT 1";
        Cursor c = db.rawQuery(selectQuery, null);
        if(c.moveToFirst()) {
            do{
                int nameIndex = c.getColumnIndex("NAME");
                if (nameIndex != -1) {
                    userInfoString.append(c.getString(nameIndex));
                }

                int ageIndex = c.getColumnIndex("AGE");
                if (ageIndex != -1) {
                    userInfoString.append(",").append(c.getString(ageIndex));
                }

                int genderIndex = c.getColumnIndex("GENDER");
                if(genderIndex != -1){
                    userInfoString.append(",").append(c.getString(genderIndex));
                }

            } while (c.moveToNext());
        }
        c.close();
        return userInfoString.toString();
    }

    public List<AttendanceLog> getAttendanceLogs() {
        List<AttendanceLog> attendanceLogs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        String selectQuery = "SELECT * FROM ATTENDANCELOG";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                AttendanceLog log = new AttendanceLog();
                int idIndex = cursor.getColumnIndex("_id");
                if (idIndex != -1) {
                    log.set_id(cursor.getInt(idIndex));
                } else {
                    // Handle error. For example, log an error message or throw an exception
                }


                int classIdIndex = cursor.getColumnIndex("CLASSID");
                if (classIdIndex != -1) {
                    log.setClassId(cursor.getString(classIdIndex));
                } else {
                    // Handle error. For example, log an error message or throw an exception
                }

                int classNameIndex = cursor.getColumnIndex("CLASS_NAME");
                if (classNameIndex != -1) {
                    log.setClassName(cursor.getString(classNameIndex));
                } else {
                    // Handle error. For example, log an error message or throw an exception
                }

                int scanTimeIndex = cursor.getColumnIndex("SCAN_TIME");
                if(scanTimeIndex != -1){
                    String timeLoggedString = cursor.getString(scanTimeIndex);
                    try {
                        Date timeLogged = dateFormat.parse(timeLoggedString);
                        log.setTimeLogged(timeLogged);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle error. For example, log an error message or throw an exception
                }

                int withinBoundsIndex = cursor.getColumnIndex("WITHIN_BOUNDS");
                if (withinBoundsIndex != -1) {
                    log.setWithinBounds(cursor.getString(withinBoundsIndex));
                } else {
                    // Handle error. For example, log an error message or throw an exception
                }

                attendanceLogs.add(log);
            } while (cursor.moveToNext());
        }
        cursor.close();

        Collections.reverse(attendanceLogs);

        return attendanceLogs;
    }


    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "_id = ?";
        String[] selectionArgs = { String.valueOf(id) };
        int deletedRows = db.delete(ATTENDANCELOG, selection, selectionArgs);
        Log.d("Database", "Deleted rows: " + deletedRows);
        db.close();
    }

}
