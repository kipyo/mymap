package com.work.kipyo.mymap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by q on 2016-09-25.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public final static String ID = "_id";
    public final static String DATE_FIELD = "date";
    public final static String MILEAGE_FIELD = "mileage";
    public final static String METER_FIELD = "meter";
    public DatabaseHelper(Context context) {
        super(context, MapProvider.MAPDB, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + MapProvider.MAPDB
                + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE_FIELD + " TEXT, " + MILEAGE_FIELD + " INTEGER, " + METER_FIELD + " INTEGER)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
