package com.sayt.godslove;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sayt.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS link_lists";

    private static final String SQL_CREATE_GROUP_TABLE =
            "CREATE TABLE classes (id INTEGER PRIMARY KEY AUTOINCREMENT ,tid TEXT , student TEXT," +
                    " _class TEXT,  secret_key TEXT, month TEXT, conferenceId TEXT," +
                    "permit TEXT, active TEXT)";


    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_GROUP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
