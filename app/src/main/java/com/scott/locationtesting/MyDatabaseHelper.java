package com.scott.locationtesting;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        db.execSQL("CREATE TABLE USERINFO (_id INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, INT AGE, GENDER TEXT, CURRENT_TOKEN TEXT)");
        db.execSQL("CREATE TABLE ATTENDANCELOG(_id INTEGER PRIMARY KEY AUTOINCREMENT, CLASSID TEXT, CLASS_NAME TEXT, STUDENT_ID TEXT, SCAN_TIME DATETIME)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
