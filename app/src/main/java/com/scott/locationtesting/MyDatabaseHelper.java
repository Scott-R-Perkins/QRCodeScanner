package com.scott.locationtesting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    //Will probably need 2 databases? or 2 tables. One db/table that stores user/token info, and one for attendance info
    //user/token info:first/last/age/gender/currentToken
    //attendance info: create a class to store attendance info and store the variables in the DB? May not need to create an actual attendance object if all the data is being saved in the db.
    //classid/class name/student id/scan time
    private static final String DB_NAME = "localData";
    private static final int DB_VERSION = 1;
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
        db.execSQL("CREATE TABLE USERINFO (_id INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, INT AGE, GENDER TEXT)");
        // This table is used to keep information from every attempted attendance scan and is then later displayed in StudentLogActivity
        db.execSQL("CREATE TABLE ATTENDANCELOG(_id INTEGER PRIMARY KEY AUTOINCREMENT, CLASSID TEXT, CLASS_NAME TEXT, STUDENT_ID TEXT, SCAN_TIME TEXT, WITHIN_BOUNDS TEXT)");
    }

    public long insertOrUpdateScanInfo(int userId, String token) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("USER_ID", userId);
        values.put("CURRENT_TOKEN", token);

        long rowId = db.insertWithOnConflict("SCANINFO", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        if (rowId == -1) {
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

        if(rowId == -1){
            return db.update("USERINFO", values, null, null);
        } else {
            return rowId;
        }
    }

    public long insertIntoAttendancelog(String classId, String className, String studentId, String withinBounds){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("CLASSID", classId);
        values.put("CLASS_NAME", className);
        values.put("STUDENT_ID", studentId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        values.put("SCAN_TIME", dateFormat.format(new Date()));
        values.put("WITHIN_BOUNDS",withinBounds);
        return db.insert("ATTENDANCElOG", null, values);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public String getUserInfo() {
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

}
