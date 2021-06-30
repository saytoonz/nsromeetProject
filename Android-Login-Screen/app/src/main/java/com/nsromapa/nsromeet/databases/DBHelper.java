package com.nsromapa.nsromeet.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "awt.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_SCHEDULE_TABLE =
            "CREATE TABLE schedules (id INTEGER PRIMARY KEY AUTOINCREMENT ,forid TEXT ,tid TEXT, db_id TEXT," +
                    " title TEXT,  conference_id TEXT, date_string TEXT," +
                    "duration_hours TEXT, duration_minutes TEXT, time_stamp TEXT, " +
                    "host_type TEXT, status TEXT, created_by TEXT, creator_name TEXT, created_idtype TEXT," +
                    "sched_type TEXT,sched_type_id TEXT, sched_type_name TEXT, deleted TEXT, active TEXT)";

    private static final String SQL_CREATE_GROUP_TABLE =
            "CREATE TABLE groups (id INTEGER PRIMARY KEY AUTOINCREMENT ,forid TEXT , db_id TEXT," +
            " gid TEXT,  name TEXT, description TEXT," +
            "conference_id TEXT, privacy TEXT, link TEXT, " +
            "created_by TEXT, creator_name TEXT, created_idtype TEXT, date_added TEXT,  active TEXT)";
    

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS schgroupsedules";
    private static final String SQL_DELETE_ENTRIES_ = "DROP TABLE IF EXISTS groups";



    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SCHEDULE_TABLE);
        db.execSQL(SQL_CREATE_GROUP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_ENTRIES_);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
