package com.example.rssDima;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDataBaseChannelHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public static final String _ID = "_id";
    public static final String DATABASE_NAME = "feeddb";
    public static final String TITLE = "title";
    public static final String SUMMARIES = "summaries";
    public static final String LINK = "link";
    public int channel_id;

    public static final String CREATE_DATABASE_BEFORE_ID = "CREATE TABLE " + DATABASE_NAME;

    public static final String CREATE_DATABASE_AFTER_ID = " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + TITLE + " TEXT," + SUMMARIES + " TEXT," + LINK + " TEXT);";


    public static final String DROP_DATABASE_NO_ID = "DROP TABLE IF EXISTS " + DATABASE_NAME;

    public MyDataBaseChannelHelper(Context context, int id) {
        super(context, DATABASE_NAME + id, null, DATABASE_VERSION);
        channel_id = id;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DATABASE_BEFORE_ID + channel_id + CREATE_DATABASE_AFTER_ID);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion != oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME + channel_id);
            onCreate(db);
        }
    }
}